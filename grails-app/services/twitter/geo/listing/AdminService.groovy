package twitter.geo.listing

class AdminService {

    static transactional = false

    public boolean isAdmin(def request) {
        InetAddress addr = InetAddress.getByName(request.remoteAddr);
        return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
    }
}
