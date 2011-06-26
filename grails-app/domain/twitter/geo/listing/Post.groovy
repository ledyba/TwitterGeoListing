package twitter.geo.listing

class Post {
    static belongsTo = [client:Client, user:User];

    Long statusId;
    String text;
    Date date;
    Double latitude;
    Double longitude;

    static constraints = {
        client(nullable: false);
        user(nullable: false);
        date(nullable: false);
        statusId(nullable: false, unique: true);
        text(nullable: false, blank: false, unique: true);
    }

}
