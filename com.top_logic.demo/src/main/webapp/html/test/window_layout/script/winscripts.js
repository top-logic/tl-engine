function restore (windowID, newHeight) {
	var theWindow = document.getElementById(windowID);
	if (!!theWindow) {
		/* höhe des Rahmens: 5px
		/* damit also Windowrahmen - 20 - 5 = höhe der contentzelle
		*/
		var contentheight = newHeight - 20 - 5;
		
		setHeight(windowID+"_l",contentheight);
		setHeight(windowID+"_c",contentheight);
		setHeight(windowID+"_r",contentheight);
		setHeight(windowID+"_b",5);
		setHeight(windowID, newHeight)
	}
}
 
function minimize (windowID) {
	var theWindow = document.getElementById(windowID);
	 if(!!theWindow) {
		setHeight(windowID+"_l",0);
		setHeight(windowID+"_c",0);
		setHeight(windowID+"_r",0);
		setHeight(windowID+"_b",0);
		setHeight(windowID,20);
	}
}
function setHeight(element_ID, pixel) {
	var element = document.getElementById(element_ID);
	if (!element) {alert ("not found: " + element_ID)};
	element.style.height = pixel+"px";
}

function toogle (windowID){
	var theWindow = document.getElementById(windowID);
	if (!!theWindow) {
		if(theWindow.style.height == "20px") {
			restore(windowID, 400);
		} else {
			minimize(windowID);
		}
	}	
}