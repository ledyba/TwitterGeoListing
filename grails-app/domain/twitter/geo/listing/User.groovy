package twitter.geo.listing

class User {
    Long userId;
    String screenName;
    Long maxId = null;
    Long sinceId = null;

    static hasMany = [ posts:Post, clients:Client ];

    static constraints = {
        userId(nullable: false, blank: false, unique: true);
        screenName(nullable: false, blank: false, unique: true);
        maxId(nullable: true);
        sinceId(nullable: true);
    }

}
