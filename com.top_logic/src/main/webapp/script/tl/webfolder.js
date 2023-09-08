/* 
 * JavaScript functions used by the WebFolder
 * 
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

// Creates a window without status, toolbars but with scroll and menu.
function openBareScrollingWindowWithMenubar(id, url, width, height) {
    window.open(url,id,"status=no,resizable=yes,scrollbars=yes,toolbar=no,menubar=yes,width="+width+",height="+height);
}

function openBareScrollingWindow(id, url, width, height) {
    window.open(url,id,"status=no,resizable=yes,scrollbars=yes,toolbar=no,menubar=no,width="+width+",height="+height);
}

// creates a dialog window
function openScrollableDialog(anID, anURL, aWidth, aHeight, withScroll) {
  	var theWindow = top;
  	var theXPos   = theWindow.screenLeft;
  	var theYPos   = theWindow.screenTop;
  	var theWidth  = getFrameWidth(theWindow);
  	var theHeight = getFrameHeight(theWindow);

    var screenX   = theXPos + (theWidth - aWidth) / 2;
    var screenY   = theYPos  + (theHeight - aHeight) / 2;

    if (withScroll == null) {
        withScroll = "yes";
    }

    return window.open (anURL,
                             anID, 
                             "status=no" +
                             ",resizable=yes" + 
                             ",scrollbars=" + withScroll +
                             ",toolbar=no" +
                             ",width=" + aWidth +
                             ",height=" + aHeight +
                             ",top=" + screenY +
                             ",left=" + screenX);

}

// Creates a dialog window without status, scroll/toolbars
function openSimpleDialog (anID, anURL, aWidth, aHeight) {
    return openScrollableDialog(anID, anURL, aWidth, aHeight, "no");
}

// Returns the width (in px) of the given frame. 
// This method is used to position a dialog in the center of a calling window.
function getFrameWidth(aFrame) {
	var frameWidth;
	// NS4
	if (self.innerWidth)
		frameWidth = aFrame.innerWidth;
	// NS6 et al
	else if (document.documentElement && document.documentElement.clientWidth)
		frameWidth = aFrame.document.documentElement.clientWidth;
	// all others
	else if (document.body)
		frameWidth = aFrame.document.body.clientWidth;

	return frameWidth;
}



// Returns the height (in px) of the given frame.
// This method is used to position a dialog in the center of a calling window.
function getFrameHeight(aFrame) {
	var frameHeight;
	// NS4
	if (self.innerHeight)
		frameHeight = aFrame.innerHeight;
	// NS6 et al
	else if (document.documentElement && document.documentElement.clientWidth)
		frameHeight = aFrame.document.documentElement.clientHeight;
	// all others
	else if (document.body)
		frameHeight = aFrame.document.body.clientHeight;

	return frameHeight;
}
