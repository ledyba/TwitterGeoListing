package twitter.geo.listing

class Client {
    String name;

    static constraints = {
        name(nullable: false, blank: false, unique: true);
    }
}
