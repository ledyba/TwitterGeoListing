package twitter.geo.listing

import twitter4j.Twitter
import twitter4j.auth.RequestToken

class AdminController {

    def adminService;
    def twitterService;

    def index = {
        if(!adminService.isAdmin(request)){
            redirect(uri:"/");
            return
        }
        render(view:"index");
        return
    }

    def login_check = {
        if(!adminService.isAdmin(request)){
            redirect(uri:"/");
            return
        }
        if(params.screenName == null || params.screenName.size() <= 0){
            render(templace:"login_fail", model:[exception: new Exception("Screen name is blank.")]);
            return
        }
        session.removeAttribute('user');
        session.removeAttribute('twitter');
        session['screenName'] = params.screenName;
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
}
