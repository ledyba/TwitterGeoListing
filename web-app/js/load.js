var Ajax;
if (Ajax && (Ajax != null)) {
	document.observe("dom:loaded", function() {
      Client = new AjaxClient();
      $('load_btn').observe('click', function(event) {
        if (event.stopped) return;
        Client.startLoading();
        Event.stop(event);
        return false;
      });
	});
	var AjaxClient = function(){
	    this.MAX_PLACES = 1500;
	    this.infoWindow = null;
	    this.openWindow = function(event, place){
	        var date = new Date(place.date);
	        var content = "<a href='"+place.url+"' target='_blank'><strong>"+date.toLocaleString()+"</strong></a><br>"+place.text;
	        if(this.infoWindow){
	            this.infoWindow.close();
	        }
	        this.infoWindow = new google.maps.InfoWindow({
	            content: content,
	            position: event.latLng
	        });
	        this.infoWindow.open(this.Map);
	    }
	    this.addPlaces = function(places){
	        var markers = new Array();
	        places.forEach(function(place){
	            var marker = new google.maps.Marker({
                    position: new google.maps.LatLng(place.latitude, place.longitude),
                    title: place.text
                });
                google.maps.event.addListener(marker, 'click', function(event){
                    this.openWindow(event, place);
                }.bind(this));
	            markers.push(marker);
	        }, this);
	        this.MarkerCluster.addMarkers(markers);
	    }
	    this.maxId = -1;
	    this.totalCount = 0;
	    this.loadPlaces = function(){
    	    this.init();
            Element.hide($('server_info'));
            new Ajax.Request(BaseURL+'user/posts', {
              parameters: { maxId: this.maxId },
              onSuccess: function(response) {
                var resp_info = response.responseJSON;
                if(resp_info.status == 'succeed'){
                    var places = resp_info.places;
                    this.totalCount += places.length;
                    this.addPlaces(places);
                    if(places.length > 0 && this.totalCount < this.MAX_PLACES){
                        this.maxId = places.last().statusId;
                        setTimeout(function(){this.loadPlaces()}.bind(this), 100);
                    }else if(this.totalCount <= 0){
                        Element.show($('server_info'));
                        $('server_info').innerHTML="<hr />通信には成功しましたが、一件も見つかりませんでした。";
                    }
                }else{
                    Element.show($('server_info'));
                    $('server_info').innerHTML="<hr /><p>通信に失敗しました。再度お試しください。エラーメッセージ：</p>"+loading_info.message;
                }
              }.bind(this),
              onFailure: function(response) {
                    Element.show($('server_info'));
                    $('server_info').innerHTML="<hr /><p>通信に失敗しました。再度お試しください。</p>";
              }.bind(this)
            });
	    }
	    this.startLoadingInner = function(){
            new Ajax.Request(BaseURL+'user/load', {
              parameters: {},
              method: 'post',
              onSuccess: function(response) {
                var loading_info = response.responseJSON;
                Element.show($('twitter_info'));
                $('twitter_info').innerHTML=loading_info.message;
                if(loading_info.status == 'loading'){
                    setTimeout(function(){this.startLoadingInner()}.bind(this), 1000);
                }else if(loading_info.status == 'loaded'){
                    this.startLoadingFlag = false;
                    this.loadPlaces();
                }else{
                    this.startLoadingFlag = false;
                    $('twitter_info').innerHTML="<p>通信に失敗しました。再度お試しください。エラーメッセージ：</p>"+loading_info.message;
                    this.loadPlaces();
                }
              }.bind(this),
              onFailure: function(response) {
                this.startLoadingFlag = false;
                Element.show($('twitter_info'));
                $('twitter_info').innerHTML="<p>通信に失敗しました。再度お試しください。</p>";
              }.bind(this)
            });
	    }
	    this.startLoadingFlag = false;
	    this.startLoading = function(){
    	    if(!this.startLoadingFlag){
    	        this.startLoadingFlag = true;
    	        this.startLoadingInner();
    	    }
	    }
	    this.initialized = false;
	    this.init = function(){
	        if(!this.initialized){
                var opt = {
                    zoom: 5,
                    navigationControl: true,
                    scaleControl: true,
                    streetViewControl: true,
                    center: new google.maps.LatLng(37,139),
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                }
                Element.show($("map_box"));
                this.Map = new google.maps.Map($("google_map_box"), opt);
                this.MarkerCluster = new MarkerClusterer(this.Map, new Array(), {
                    maxZoom: 15
                });
                this.initialized = true;
            }
	    }
	}
}
