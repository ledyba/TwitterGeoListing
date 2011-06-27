package twitter.geo.listing

import grails.converters.JSON
import twitter4j.ProfileImage
import twitter4j.Twitter
import twitter4j.auth.RequestToken

class UserController {

    def postLoadingService;
    def adminService;
    def twitterService;

    static allowedMethods = [search: "POST", load: "POST"]

    def login_check = {
        session.removeAttribute('user');
        session.removeAttribute('twitter');
        session.removeAttribute('screenName');
        RequestToken token;
        try{
            token = twitterService.getRequestToken(createLink(controller: "user", action: "login", absolute:true));
        }catch(e){
            render(templace:"login_fail", model:[exception: e]);
            return
        }
        session['token'] = token;
        redirect(url: token.authenticationURL);
    }

    def login = {
        boolean login_ok = false;
        Twitter twitter = session["twitter"];
        twitter4j.User tuser = session["user"];
        if(twitter == null){
            String oauth_verifer = params.oauth_verifier;
            if(oauth_verifer != null && oauth_verifer.length() > 0 && session['token'] != null){
                try{
                    twitter = twitterService.createTwitter()
                    twitter.getOAuthAccessToken(session['token'], oauth_verifer);
                    session.removeValue("token");
                    session["twitter"] = twitter
                    login_ok = true;
                }catch(e){
                    e.printStackTrace()
                }
            }
            if(!login_ok){
                redirect(uri:"/");
                return;
            }
        }
        if(tuser == null){
            try{
                String screenName = session['screenName'];
                if(screenName != null && screenName.length() > 0 && adminService.isAdmin(request)){
                    tuser = twitter.showUser(screenName);
                    if(tuser == null){
                        render(view:"login_fail", model:[exception: new Exception("ご指定のユーザーは存在しません：${session["screenName"].encodeAsHTNML()}")]);
                        return;
                    }
                    session.removeAttribute('screenName');
                }else{
                    tuser = twitter.showUser(twitter.id);
                }
                session["user"] = tuser;
            }catch (e){
                e.printStackTrace()
                render(view:"login_fail", model:[exception: e]);
                return;
            }
        }
        User userInstance = User.findByUserId(tuser.id);

        if (userInstance == null) {
            userInstance = new User(screenName: tuser.screenName, userId: tuser.id);
            userInstance.save();
        }else if(userInstance.screenName != tuser.screenName){
            userInstance.screenName = tuser.screenName;
            userInstance.save();
        }
        def faceImageUrl = null;
        try{
            faceImageUrl = twitter.getProfileImage(userInstance.screenName, ProfileImage.NORMAL)?.URL;
        }catch(e){
        }
        render(view:"login", model:[userInstance: userInstance, faceImageUrl: faceImageUrl]);
    }
    def load = {
        Twitter twitter = session["twitter"];
        twitter4j.User tuser = session["user"]
        if(twitter == null || tuser == null){
            render([status: 'error', message: '<p>セッションエラーです</p>'] as JSON);
            return;
        }
        def return_hash = postLoadingService.loadPosts(tuser.id, twitter);
        render return_hash as JSON;
    }
    def posts = {
        Twitter twitter = session["twitter"];
        twitter4j.User tuser = session["user"]
        if(twitter == null || tuser == null){
            render([status: 'error', message: '<p>セッションエラーです</p>'] as JSON);
            return;
        }
        User userInstance = User.findByUserId(tuser.id);
        def return_hash;
        if (userInstance == null) {
            return_hash = [
                    status: 'error',
                    message: '<p>そのユーザーは存在しません</p>'
            ];
        } else {
            return_hash = [
                    status: 'succeed',
                    message: '<p>データの取得に成功しました。</p>'
            ];
            def places = [];
            Post[] postInstances;
            def maxId = params.maxId.toLong();
            if (params.maxId == null || params.maxId.equals("") || maxId <= 0) {
                postInstances = Post.findAllByUser(userInstance,
                        [
                            sort: "statusId",
                            max: 100,
                            order: "desc"
                        ]);
            } else {
                postInstances = Post.findAllByUserAndStatusIdLessThan(userInstance, maxId,
                        [
                            sort: "statusId",
                            max: 100,
                            order: "desc"
                        ]);
            }
            postInstances.each { postInstance ->
                places.add([
                        statusId: postInstance.statusId.toString(),
                        url: "http://twitter.com/#!/${userInstance.screenName.encodeAsHTML()}/status/${postInstance.statusId.toString()}",
                        date: postInstance.date,
                        text: postInstance.text.encodeAsHTML(),
                        latitude: postInstance.latitude,
                        longitude: postInstance.longitude
                ])
            }
            return_hash["places"] = places;
        }
        render return_hash as JSON;
    }
}
