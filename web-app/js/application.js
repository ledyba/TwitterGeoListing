var Ajax;
if (Ajax && (Ajax != null)) {
	Ajax.Responders.register({
	  onCreate: function() {
        if($('spinner') && Ajax.activeRequestCount>0){
          new Effect.Appear('spinner',{duration:0.5,queue:'end'});
        }
	  },
	  onComplete: function() {
        if($('spinner') && Ajax.activeRequestCount==0){
          new Effect.Fade('spinner',{duration:0.5,queue:'end'});
        }
	  }
	});
}
