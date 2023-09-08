// Script content for loginError.jsp
(function() {
	function getParameterByName(name) {
	    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
	    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
	}
	
	window.addEventListener("load", function(event) {	
		var display = document.getElementById("timer");
		
		var loginPath = getParameterByName("forward");
		if (loginPath == null || loginPath.substring(0, 1) != "/") {
			// For safety reasons. Do not accept URLs pointing to other servers.
			loginPath = "/";
		}
		
		var timeout = parseInt(getParameterByName("timeout"));
		if (timeout <= 0) {
			timeout = 5000;
		}
		var update = function() {
			display.innerHTML = "" + Math.round(timeout / 1000);
			timeout -= 1000;
			if (timeout > 0) {
				window.setTimeout(update, 1000);
			} else {
				window.location = loginPath;
			}
		}
		update();
	});
})();