// Handle 3 state MetaAttributes
// Switch state don't care->false->true->don't care for search or non-mandatory attributes
// Switch state false->true->false for search or non-mandatory attributes

// Handle 3 state search checkbox
function change3StateCheckbox (anID, aContextPath) {
	if (document.getElementById (anID)) {
		// Calculate new state: dontcare->false->true->dontcare->...
		theState = document.getElementById(anID).value;
		if (theState == "dontcare") {
			theNewState = "false";
		}
		else if (theState == "false") {
			theNewState = "true";
		}
		else {
			theNewState = "dontcare";
		}

		// Set the value in the hidden input
		document.getElementById(anID).value = theNewState;
		
		// Set the image
		theImgID = "img_" + anID;
		if (document.getElementById (theImgID)) {
			document.getElementById (theImgID).src=aContextPath+"/images/tristate/" + theNewState + ".png";;
		}
	}
	    
	return true;
}

// Handle 2 state checkbox
function change2StateCheckbox (anID, aContextPath) {
	if (document.getElementById (anID)) {
		// Calculate new state: dontcare->false->true->dontcare->...
		theState = document.getElementById(anID).value;
		if (theState == "false") {
			theNewState = "true";
		}
		else {
			theNewState = "false";
		}

		// Set the value in the hidden input
		document.getElementById(anID).value = theNewState;
		
		// Set the image
		theImgID = "img_" + anID;
		if (document.getElementById (theImgID)) {
			document.getElementById (theImgID).src=aContextPath+"/images/tristate/" + theNewState + ".png";;
		}
	}
	    
	return true;
}
			