package twitter.geo.listing

import java.util.concurrent.Callable
import twitter.geo.listing.User
import twitter4j.*

class PostLoadingService {

    static transactional = false
    private static final Long MAX = 1000;
    private static final Long API_MAX = 50;
    def executorService

    private final static def loadingStatus = [:];

    private def startLoading(Long userId, Twitter twitter) {
        Long total = null;
        Long count = 0;
        Long geoCount = 0;
        int apiCount = 0;
        int apiLeft = null;
        try {
            RateLimitStatus rateLimitStatus = twitter.getRateLimitStatus()
            apiLeft = rateLimitStatus.getRemainingHits();
            if (apiLeft < (API_MAX + 30)) {
                throw new Exception("APIが残り${apiLeft}回しか呼べないので途中ですがメッセージの更新を終了しました。次API制限がリセットされるのは${rateLimitStatus.secondsUntilReset}秒後です。");
            }
        } catch (ex) {
            ex.printStackTrace();
           synchronized (loadingStatus) {
                loadingStatus[userId] = [
                        status: 'error',
                        message: "<p>Twitterのエラーです：${ex.getMessage().encodeAsHTML()}</p>"
                ];
            }
            return;
        }
        User userInstance = User.findByUserId(userId);
        boolean firstRun = userInstance.maxId == null && userInstance.sinceId == null;
        boolean getLatest = true;
        boolean errorOccured = false;

        def statuses;

        def paging = new Paging();
        if (!firstRun) {
            paging.setSinceId(userInstance.maxId+1);
        }
        while (paging != null) {
            User.withTransaction { status ->
                try {
                    //リトライ機構
                    apiCount++;
                    def lastError = null;
                    for (int i = 0; i < 5; i++) {
                        try {
                            statuses = twitter.getUserTimeline(userId, paging);
                            lastError = null;
                            break;
                        } catch (exx) {
                            exx.printStackTrace();
                            lastError = exx;
                            synchronized (loadingStatus) {
                                loadingStatus[userId] = [
                                        status: 'loading',
                                        message: "<p>試行${i + 1}回目/全${5}試行でエラー：${exx.localizedMessage}</p>"
                                ];
                            }
                        }
                    }
                    if (lastError != null) {
                        throw lastError;
                    }
                    //DBへの格納
                    if(userInstance.sinceId != null && userInstance.maxId != null){
                        statuses = statuses.findAll{it ->
                            it.id > userInstance.maxId || it.id < userInstance.sinceId;
                        }
                    }
                    count += statuses.size();
                    statuses.each { item ->
                        def geo = item.geoLocation;
                        if (geo != null) {
                            geoCount += 1;
                            Client client = Client.findByName(item.source);
                            if (client == null) {
                                client = new Client(name: item.source);
                                client.save();
                            }
                            Post post = new Post(statusId: item.id, date: item.createdAt, text: item.text, latitude: geo.latitude, longitude: geo.longitude, user: userInstance, client: client);
                            post.save();
                        }
                    }
                    //LIMITをチェック
                    RateLimitStatus rateLimitStatus = twitter.getRateLimitStatus()
                    apiLeft = rateLimitStatus.getRemainingHits();
                    if (apiLeft < 30) {
                        throw new Exception("APIが残り${apiLeft}回しか呼べないので途中ですがメッセージの更新を終了しました。次API制限がリセットされるのは${rateLimitStatus.secondsUntilReset}秒後です。");
                    }
                    synchronized (loadingStatus) {
                        loadingStatus[userId] = [
                                status: 'loading',
                                message: "<p>読込中…${count}件読み込み済み うち位置情報あり${geoCount}件 / API消費回数${apiCount}回(残り${apiLeft})</p>"
                        ];
                    }
                    //次回以降の取り出し範囲
                    if (firstRun) {
                        if (statuses.size() > 0 && (count < MAX && apiCount < API_MAX)) {
                            userInstance.maxId = statuses.first().id;
                            userInstance.sinceId = statuses.last().id;
                            userInstance.merge();
                            paging = new Paging();
                            paging.setMaxId(userInstance.sinceId-1);
                        } else {
                            paging = null;
                        }
                    } else {
                        if (getLatest) {
                            if (statuses.size() > 0) {
                                userInstance.maxId = statuses.first().id;
                                userInstance.merge();
                                if (count >= MAX || apiCount >= API_MAX) {
                                    paging = null;
                                } else {
                                    paging = new Paging();
									paging.setMaxId(userInstance.sinceId-1);
                                }
                            } else {
                                getLatest = false;
                                paging = new Paging();
                                paging.setMaxId(userInstance.sinceId-1);
                            }
                        } else {
                            if (statuses.size() > 0) {
                                userInstance.sinceId = statuses.last().id;
                                userInstance.merge();
                                if (count >= MAX || apiCount >= API_MAX) {
                                    paging = null;
                                } else {
                                    paging = new Paging();
                                    paging.setMaxId(userInstance.sinceId-1);
                                }
                            } else {
                                paging = null;
                            }
                        }
                    }
                } catch (e) {
                    status.setRollbackOnly();
                    e.printStackTrace();
                    synchronized (loadingStatus) {
                        loadingStatus[userId] = [
                                status: 'error',
                                message: "<p>内部エラーです：${e.getMessage().encodeAsHTML()}</p>"
                        ];
                    }
                    errorOccured = true;
                }
            }
        }
        if(!errorOccured){
            total = Post.countByUser(userInstance);
            synchronized (loadingStatus) {
                loadingStatus[userId] = [
                        status: 'loaded',
                        message: "<p>読込が完了しました。今回読み込んだツイート：${count}件　うち位置情報あり：${geoCount}　総位置情報件数：${total}</p>"
                ];
            }
        }
    }

    ;

    def loadPosts(Long userId, Twitter twitter) {
        def ret;
        synchronized (loadingStatus) {
            if (loadingStatus.containsKey(userId)) {
                ret = loadingStatus[userId];
                if (ret.status.equals("error") || ret.status.equals("loaded")) {
                    loadingStatus.remove(userId);
                }
                return ret;
            }
            ret = loadingStatus[userId] = [
                    status: 'loading',
                    message: "<p>読み込みを開始しました。</p>"
            ];
        }
        executorService.submit({
            startLoading(userId, twitter);
        } as Callable);
        return ret;
    }
}
