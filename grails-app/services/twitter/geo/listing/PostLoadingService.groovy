package twitter.geo.listing

import java.util.concurrent.Callable
import twitter4j.TwitterFactory
import twitter4j.Twitter
import twitter4j.ProfileImage
import twitter4j.Status
import twitter4j.ResponseList
import twitter4j.Paging
import twitter4j.auth.AccessToken
import twitter4j.RateLimitStatus

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
        User userInstance;
        try {
            RateLimitStatus rateLimitStatus = twitter.getRateLimitStatus()
            apiLeft = rateLimitStatus.getRemainingHits();
            if (apiLeft < (API_MAX + 30)) {
                throw new Exception("APIが残り${apiLeft}回しか呼べないので途中ですがメッセージの更新を終了しました。次API制限がリセットされるのは${rateLimitStatus.secondsUntilReset}秒後です。");
            }
            userInstance = User.findByUserId(userId);
        } catch (e) {
            synchronized (loadingStatus) {
                loadingStatus[userId] = [
                        status: 'error',
                        message: "<p>Twitterのエラーです：${e.getMessage().encodeAsHTML()}</p>"
                ];
            }
            return;
        }
        boolean firstRun = userInstance.maxId == null && userInstance.sinceId == null;
        boolean getLatest = true;
        boolean errorOccured = false;
        Long nowMaxId = null;
        Long nowSinceId = null;

        ResponseList<Status> statuses;

        def paging = new Paging();
        if (!firstRun) {
            paging.sinceId = userInstance.maxId;
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
                        } catch (e) {
                            e.printStackTrace();
                            lastError = e;
                            synchronized (loadingStatus) {
                                loadingStatus[userId] = [
                                        status: 'loading',
                                        message: "<p>試行${i + 1}回目/全${5}試行でエラー：${e.localizedMessage}</p>"
                                ];
                            }
                        }
                    }
                    if (lastError != null) {
                        throw lastError;
                    }
                    //DBへの格納
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
                            if (nowMaxId == null) {
                                nowMaxId = statuses.first().id;
                            }
                            nowSinceId = statuses.last().id;
                            paging.setMaxId(nowSinceId);
                        } else {
                            userInstance.maxId = nowMaxId;
                            userInstance.sinceId = nowSinceId;
                            userInstance.save();
                            paging = null;
                        }
                    } else {
                        if (getLatest) {
                            if (statuses.size() > 0) {
                                userInstance.maxId = nowMaxId = statuses.first().id;
                                userInstance.save();
                                if (count >= MAX || apiCount >= API_MAX) {
                                    paging = null;
                                } else {
                                    paging.setSinceId(statuses.last().id);
                                }
                            } else {
                                getLatest = false;
                                nowSinceId = null;
                                paging = new Paging();
                                paging.maxId = nowMaxId = userInstance.sinceId;
                            }
                        } else {
                            if (statuses.size() > 0) {
                                userInstance.sinceId = nowSinceId = statuses.last().id;
                                userInstance.save();
                                if (count >= MAX || apiCount >= API_MAX) {
                                    paging = null;
                                } else {
                                    paging.setMaxId(nowSinceId);
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
