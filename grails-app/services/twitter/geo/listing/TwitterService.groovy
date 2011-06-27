package twitter.geo.listing

import twitter4j.TwitterFactory

class TwitterService {

    static transactional = false

    private TwitterFactory twitterFactory = new TwitterFactory();
    private final static String KEY = "sRtcK6UwkAkwKeYKs6zglg";
    private final static String SECRET = "nPgRVyCtl9wlk0BIo00T14KH4wB7ukZbJCDCjGIql4";

    private def createTwitter(){
        def twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(KEY,SECRET);
        return twitter;
    }

    def checkIfTokenIsStillAvailable(User userInstance) {
        return false;
    }

    def getRequestToken(def callbackUrl) {
        return createTwitter().getOAuthRequestToken(callbackUrl.toString());
    }
}
