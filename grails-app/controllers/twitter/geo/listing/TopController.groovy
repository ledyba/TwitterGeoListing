package twitter.geo.listing

class TopController {

    def adminService
    def index = {
        if(adminService.isAdmin(request)){
            redirect(uri:'/backdoor');
        }
    }
}
