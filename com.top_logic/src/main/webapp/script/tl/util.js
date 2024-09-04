/**
 *
 * Various JavaScript utility functions.
 *
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

// Set the focus into the first editable component of the first form.
//
// This method is only a caller for the selectFirst() with a specific
// timeout for 100 msec.
function focusFirst () {
    window.setTimeout(selectFirst, 100);
} 


// Set the focus into the first editable component of the first form.
// COPIED TO formCheck.js
function selectFirst() {
	
	// alert("scrollTop: " + scrollTop);
	
	// Use last forms in case, a dialog is rendered over an inline rendered form. 
	// In that case, the first input in the last form (the dialog) must be selected.
    var theForm = document.forms[document.forms.length - 1];
    if (theForm && theForm.elements) {
    	var theElement = null;
	    
    	var selectionSuccessful = false;
    	for (var thePos = 0; !selectionSuccessful && (thePos < theForm.elements.length); thePos++) {
            theElement = theForm.elements[thePos];
            selectionSuccessful = selectElement(theElement, false);
        }
    }
}

/**
 * Sets the focus to an element, in case it is focusable.
 * @param theElement - the element, which shall receive focus
 * @param changeScrollPosition - true, if the scroll position shall be changed in order to show
 * 								 the focusable element, false otherwise
 * @returns true, if selection was successful, false otherwise
 */
function selectElement(theElement, changeScrollPosition) {
	if (!theElement.disabled
		&& ((theElement.tagName.toLowerCase() == "textarea")
				|| (('type' in theElement)
				&& (theElement.type == "text")))
    ) {
    	// Check, whether the element and all of its parents are visible.
		var anchestor = theElement;
		if(window.KEYBOARD_NAVIGATION != null) {
			var topmostVisibleTabroot = KEYBOARD_NAVIGATION.getTopmostVisibleLayerAnchor(BAL.getTopLevelDocumentOf(theElement));
			
			if(!KEYBOARD_NAVIGATION.tabCycleRootFilter(topmostVisibleTabroot)) {
				return false;
			}
			
	    	while (KEYBOARD_NAVIGATION.hasParent(anchestor) && anchestor != topmostVisibleTabroot) {
	    		if (!BAL.isVisible(anchestor)) {
	    			return false;
	    		}
				anchestor = KEYBOARD_NAVIGATION.getParentElement(anchestor);
	    	}
	    	
	    	if(anchestor != topmostVisibleTabroot) {
	    		return false;
	    	}
		} else {
			while (anchestor != null && anchestor.tagName.toLowerCase() != "html") {
				if (!BAL.isVisible(anchestor)) {
					return false;
				}
				anchestor = anchestor.parentNode;
			}
		}
		
    	if(!changeScrollPosition && window.TL != null) {
			// Check, whether the element is on the currently visible part of the form.
    		var offsetParent = _getOffsetLayoutParent(theElement);
    		var position = BAL.getElementPositionRelativeTo(theElement, offsetParent);
    		var elementHeight = BAL.getElementHeight(theElement);
    		var viewportHeight;
    		var scrollTop;
    		if(offsetParent != BAL.getBodyElement(document)) {
				viewportHeight = BAL.getElementHeight(offsetParent);
				scrollTop = BAL.getScrollTopElement(offsetParent);
    		} else{
    			viewportHeight = BAL.getViewportHeight();
    			scrollTop = BAL.getScrollTop(window);
    		}
    		if (position.y < scrollTop || position.y + elementHeight > scrollTop + viewportHeight) {
    			return false;
    		}
    	}

    	try {
            BAL.focus(theElement);
            return true;
    	} catch (ex) {
    		// Still not selectable, return false.
    		return false;
    	}
    }
    return false;
}

function _getOffsetLayoutParent(element) {
	var offsetParent = element.offsetParent;
	if(offsetParent == BAL.getBodyElement(document) || _isLayoutElement(offsetParent)) {
		return offsetParent;
	} else {
		return _getOffsetLayoutParent(offsetParent);
	}
	
}

function _isLayoutElement(element) {
	return TL.getTLAttribute(element, "layout") != null; 
}

function blurFocus(element) {
	if(element != undefined && element != null) {
		element.blur();
	}
	resetFocusContainer();
}

function resetFocusContainer() {
	BAL.getTopLevelWindow().services.focusedElementStack.resetFocusContainer();
}

// opens the Clipboard of the current user.
function clipboard(aFunction, anID, aContext, aFrame) {
	var theURL = aContext + "/jsp/display/clipboard/DefaultView.jsp";

	if (aFunction != null) {
		if (anID != null) {
			theURL +='?oid=' + escape (anID) + '&mode=' + aFunction;
		}
		else {
			theURL +='?' + aFunction;
		}
	}

	if (aFunction == "add") {
		var theFrame = findFrame(aFrame);

		if (theFrame == null) {
			theFrame = findFrame(aFrame, opener);
		}

		theURL += "&directClose=1";

//		alert("Execute: " + theURL);
		theFrame.location.href = theURL;

    alert (_i18n_clipboard_add);
 	}
	else {
		openSimpleDialog('ClipBoard', theURL, '640', '480');
	}
}

// Creates a window without status, scroll/toolbars
function openDialog (anID, anURL, aWidth, aHeight) {
    return openScrollableDialog(anID, anURL, aWidth, aHeight, "no");
}

function openScrollableDialog(anID, anURL, aWidth, aHeight, withScroll) {
  	var theWindow = services.ajax.topWindow;
  	var theXPos   = theWindow.screenLeft;
  	var theYPos   = theWindow.screenTop;
  	var theWidth  = BAL.getViewportWidthOfWindowObject(theWindow);
  	var theHeight = BAL.getViewportHeightOfWindowObject(theWindow);

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

function openSimpleDialog (anID, anURL, aWidth, aHeight) {
    openDialog (anID, anURL, aWidth, aHeight);
}

// Opens a fairly defaultish browser window. Use zero values for aWidth and aHeight
// for default values. (I. e. width and height of current window.)
function openBrowserWindow(anId, anURL, aWidth, aHeight) {
	if (!aWidth && !aHeight || aWidth == 0 && aHeight == 0) {
		aWidth = getFrameWidth(services.ajax.topWindow);
		aHeight = getFrameHeight(services.ajax.topWindow);
	}
    
	window.open(anURL, anId, "toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,copyhistory=yes,resizable=yes,screenX=0,screenY=0,width="
        + aWidth + ",height="
        + aHeight + ",top,left");
}

// Creates a window without status, scroll/toolbars
function openBareWindow(id, url, width, height) {
	if (!width && !height || width == 0 && height == 0) {
		width = getFrameWidth(services.ajax.topWindow);
		height = getFrameHeight(services.ajax.topWindow);
	}
	
    return window.open(url,id, "status=no,resizable=yes,scrollbars=no,toolbar=no,width="+width+",height="+height);
}

// Creates a window without status, scroll/toolbars
function openQueryWindow(id, url, query, width, height) {
    queryurl = "&query=" + escape (query);
    window.open(url+queryurl,id, "status=no,resizable=yes,scrollbars=no,toolbar=no,width="+width+",height="+height);
}

// Creates a window without status, toolbars but with scroll.
function openBareScrollingWindow(id, url, width, height) {
	if (!width && !height || width == 0 && height == 0) {
		width = getFrameWidth(services.ajax.topWindow);
		height = getFrameHeight(services.ajax.topWindow);
	}
	
    window.open(url,id,"status=no,resizable=yes,scrollbars=yes,toolbar=no,width="+width+",height="+height);
}

// Creates a window without status, toolbars but with scroll and menu.
function openBareScrollingWindowWithMenubar(id, url, width, height) {
    window.open(url,id,"status=no,resizable=yes,scrollbars=yes,toolbar=no,menubar=yes,width="+width+",height="+height);
}

// Creates a resizable pop-up window with scrollbars.
function openBareResizableWindow(id, url, width, height) {
    window.open(url,id,"status=no,resizable=yes,scrollbars=yes,toolbar=no,locationbar=no,width="+width+",height="+height);
}

// Creates a window that has (nearly) the size of the screen.
function openMaxWindow(id, url) {
    var screenwidth = screen.availWidth - 10;
    var screenheight = screen.availHeight - 30;
    window.open(url,id,"status=no,resizable=yes,scrollbars=yes,toolbar=no,locationbar=no,screenX=0,screenY=0,width="+screenwidth+",height="+screenheight+",top,left");
}

// An executeCommandWindow is a window that can be used to (guess!) execute 'commands'
// by providing an url to forward to. The window itself contains a forward statement
// which makes sure that the exCmdW is closed after the command has finished.
//
// @param    anURL    an URL being used as destination of the forward statement

function openExecuteCommand(anURL) {
	alert("oECW: anURL is "+escape(anURL));
    window.open(anURL,"tl_execute_command","status=no,resizable=yes,scrollbars=no,toolbar=no,width=100,height=100");
	alert("done");
}


// Sets the title of the window (or, rather, the document) to aTitle
function setWindowTitle(aTitle) {
	document.title = aTitle;
}


// Changes the state of all checkboxes with the name provided
// in aName in form aForm to the value aValue. See function changeCheckboxState()
// for details.
function toggleCheckBoxes(aForm, aName, aValue){
	var len = aForm.elements.length;

	var i;
	for (i = 0; i < len; i++) {
		var theElement = aForm.elements[i];
		if (aName == null) {
				changeCheckboxState(theElement, aValue);
		} else if (theElement.name == aName) {
				changeCheckboxState(theElement, aValue);
		}
	}
}

// Changes the value of a given checkbox:
//
//     aValue == 0   =>   checkbox is unchecked
//     aValue == 1   =>   checkbox is checked
//     aValue == 0   =>   checkbox is inverted
//     
function changeCheckboxState(aCheckbox, aValue) {
	if (!aCheckbox.disabled) {
		if (aValue == 0) {
			aCheckbox.checked = false;
		} else if (aValue == 1) {
			aCheckbox.checked = true;
		} else if (aValue == -1) {
			aCheckbox.checked = !aCheckbox.checked;
		}
	}
}


// Returns an Array of all elements whose names match the
// specified RegExp pattern in the specified form.
function getElementsByPattern(aPattern, aForm) {
	var butArray = new Array();
	var len = aForm.elements.length;
	var j = 0;
	for (var i = 0; i < len; i++) {
		var theElement = aForm.elements[i];
		var elName = theElement.name;
		if ( elName.search(aPattern) > -1) {
			butArray[j++] = theElement;
		}
	}

	return butArray;
}


// Parses parameters out of a string.
function getParameter(string, parm, delim) {
    // returns value of parm from string
    if (string.length == 0) return '';
    
    var sPos = string.indexOf(parm + "=");
    if (sPos == -1) return '';
    sPos = sPos + parm.length + 1;
    
    var ePos = string.indexOf(delim, sPos);
    if (ePos == -1) ePos = string.length;
    
    return unescape(string.substring(sPos, ePos));
}

// Get a parameter value from query string
function getPageParameter(parameterName, defaultValue) {
    var s = self.location.search;
    if ((s == null) || (s.length < 1)) {
        return defaultValue;
    }
    return getParameter(s, parameterName, '&');
}


// Retrieves the value of the selected option of a select element
// defined by the id provided.
function getSelectedOptionValue (anID) {
	theSelector = document.getElementById(anID); 
	if (theSelector == null) return null;
	theElement = theSelector.options;
	theValue = theElement[theElement.selectedIndex].value;
	return theValue;
}


// Retrieves the value of the element
// defined by the id provided.
function getValueByID (anID) {
	alert(anID);
	theElement = document.getElementById(anID);
	if (theElement == null) return null;
	theValue = theElement.value;
	return theValue;
}


// Returns the value of the element with id anID
function getElementValueByID(anID) {
	var element = document.getElementById(anID);
	if (element) {
		return element.value;
	} else {
		return null;
	}
}

// Returns the selected value of the (option) element with id anID.
function getSelectedOptionValueByID(anID) {
	var element = document.getElementById(anID);
	if (element) {
		var index = element.selectedIndex;
		return element[index].value;
	}
}

// Returns true if the element with id anID is checked.
function elementChecked(anID) {
	var element = document.getElementById(anID);
	if (element) {
		return element.checked;
	}
}




// Find the frame with the given name and return this.
//
// param    aFrameName     The name of the frame to be found.
// param    aStartFrame    THe frame to start from.
// return   The searched frame or null, if no frame with that name found.
function findFrame(aFrameName, aStartFrame) {
	if (aStartFrame == null) {
		aStartFrame = this;
}

	var theCurrentFrames = aStartFrame.frames;

	if (theCurrentFrames == null) {
		return null;
	}

	for (var i = 0; i < theCurrentFrames.length; i++) {
		if (theCurrentFrames[i].name == aFrameName) {
			return theCurrentFrames[i];
		}
	}

	var theParentFrame = aStartFrame.parent;

	if ((theParentFrame != null) && (theParentFrame != aStartFrame)) {
		return findFrame(aFrameName, theParentFrame);
	} 
	else {
		return null;
	}
}

function getMasterWindow() {
	return getMasterWindowFor(services.ajax.topWindow);
}


// called by getMasterWindow()
function getMasterWindowFor(aWindow) {

	var theWindow = aWindow.services.ajax.topWindow;
	//alert("theWindow is " + theWindow + ", name is " + theWindow.name);

	if ( (typeof theWindow != 'undefined') && (theWindow != null) ) {
		//alert("theWindow is " + theWindow);
		if (theWindow.name == "masterwindow") {
			return theWindow;
		} else {
			if ( (typeof theWindow.opener != 'undefined') && theWindow.opener != null) {
				theDeeperWindow = theWindow.opener;
				//alert("deeperWindow is " + theDeeperWindow);
				return getMasterWindowFor(theDeeperWindow);
			}
		}
	} else {
		//alert("reached " + theWindow + " and returning null");
		return null;
	}
	return null;
}



// Count the chars in an html-input field and display remaining chars that could be entered.
//
// If maxlimit is reached, no more chars could be entered in field.
// The countfield displays the countstatus
//
// Example see: com.top_logic.solutions.tts.util.TtsUtil printTextareaWithCounter()
//
// @param field	name of input field to count the chars
// @param countfield name of the field to display how many chars could be still entered 
// @param maxlimit maximum allowed chars in field

function countChars(field, countfield, maxlimit) {
    if (field.value.length > maxlimit) // if too long...trim it!
        field.value = field.value.substring(0, maxlimit);
    // otherwise, update 'characters left' counter
    else 
        countfield.value = maxlimit - field.value.length;
    }


// Returns the width (in px) of the given frame.
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


// Sets the focus into the first editable element of the first form
// of the current document.
//
// This method actually just invokes _selectFirstFormElement() after a
// 100ms timeout. We assume that the page is loaded when 100ms have passed.
// Oh well ...
function selectFirstFormElement () {
    window.setTimeout("_selectFirstFormElement()",100);
} 

// Sets the focus into the first editable component of the first form
// of the current window.
function _selectFirstFormElement () {
    var theElement = null;
    var thePos     = 0;
    var theForm    = document.forms[0];

	if (theForm == null) return;

    while (theElement == null 
            && thePos < theForm.elements.length) {
        theElement = theForm.elements [thePos];

        if (theElement.type == "hidden" || theElement.disabled) {
            theElement = null;
            thePos++;
        }
    }

    if (theElement != null) {
        theElement.focus ();

        if (theElement.type != "button" 
                && theElement.type != "submit"
                && theElement.type != "reset") {
            theElement.select ();
        }
    }
}

/**
 * 
 * @param elementSize
 * @param paddingAndBorderSizes, container, which holds properties "paddingBefore", "paddingAfter", "borderBefore", "borderAfter" as numbers.
 *
 * @returns {Number}
 */
function calculateEffectiveSize(elementSize, paddingAndBorderSizes) {
	var calculated = elementSize -
		   paddingAndBorderSizes.paddingBefore -
		   paddingAndBorderSizes.paddingAfter -
		   paddingAndBorderSizes.borderBefore -
		   paddingAndBorderSizes.borderAfter;
	return Math.max(calculated, 0);
	
}

function createHideWaitpaneOnClickFunction(callbackFunction) {
	var clickFunction = function(event) {
		var waitpane = services.ajax.topWindow.document.getElementById("waitPane");
		BAL.removeEventListener(waitpane, "mousedown", clickFunction, true);
		services.ajax.hideWaitPane();
		callbackFunction.call(this, event);
	};
	return clickFunction;
}

function showClickResponsiveWaitpane(clickFunction) {
	var waitpane = services.ajax.topWindow.document.getElementById("waitPane");
	BAL.addEventListener(waitpane, "mousedown", clickFunction, true);
	services.ajax.showWaitPane();
}

function hideClickResponsiveWaitpane(clickFunction) {
	clickFunction.call();
}

function showInfoArea() {
	var infoServiceItems = Array.from(arguments);
	BAL.getTopLevelWindow()._showInfoArea(infoServiceItems);
}

let infoServiceContainerId = "tl-info-service";

function _showInfoArea(infoServiceItems) {
	if(_hasPendingInfoItems()) {
		_appendInfoItems(infoServiceItems);
	} else {
		var topLevelDocument = BAL.getTopLevelDocument();
		var infoServiceContainerId = "tl-info-service";
		var infoServiceContainer = topLevelDocument.getElementById(infoServiceContainerId);
		if (infoServiceContainer != null) {
			_stopInfoServiceFadeOut(infoServiceContainer);
		} else {
			infoServiceContainer = topLevelDocument.createElement("div");
			infoServiceContainer.id = infoServiceContainerId;
			BAL.DOM.addClass(infoServiceContainer, infoServiceContainerId);
			
			infoServiceContainer.removeInfoServiceListener = function() {};
			
			var pinningFunction = function(event) {
				_stopInfoServiceFadeOut(infoServiceContainer);
				BAL.removeEventListener(infoServiceContainer, "mouseenter", pinningFunction);
				BAL.DOM.addClass(infoServiceContainer, "tl-info-service_pinned");
				var infoItems = BAL.DOM.getChildElements(infoServiceContainer);
				infoItems.forEach(function(infoItem) {
					BAL.DOM.addClass(infoItem, "pinnedInfoServiceItem");
				});
			};
			BAL.addEventListener(infoServiceContainer, "mouseenter", pinningFunction);
			BAL.getBodyElement(topLevelDocument).appendChild(infoServiceContainer);
		}
		_appendInfoItems(infoServiceItems);
		_showInfoServiceBox(infoServiceContainer);
	}
}

function _stopInfoServiceFadeOut(infoServiceContainer) {
	BAL.getTopLevelWindow().clearTimeout(infoServiceContainer.hideInfoServiceContainerTimerId);
	if(infoServiceContainer.removeInfoServiceListener != null) {
		BAL.DOM.removeClass(infoServiceContainer, "tl-info-service-item__hide-info-service_animation");
		BAL.removeAnimationEndListener(infoServiceContainer, infoServiceContainer.removeInfoServiceListener);
	}
}

function closeInfoItem(itemId) {
	var topLevelDocument = BAL.getTopLevelDocument();
	var infoItem = topLevelDocument.getElementById(itemId);
	if (!infoItem) return;
	BAL.addAnimationEndListener(infoItem, function() {
		var infoServiceContainer = topLevelDocument.getElementById(infoServiceContainerId);
		if(infoItem && infoServiceContainer) {		
			infoServiceContainer.removeChild(infoItem);
		}
		if(infoServiceContainer && BAL.DOM.getChildElementCount(infoServiceContainer) === 0) {
			BAL.getBodyElement(topLevelDocument).removeChild(infoServiceContainer);
		}
	});
	var hideItemCssClass = "tl-info-service-item__hide-info_animation";
	infoItem.style.zIndex = 0;
	_modifyItemAnimation(hideItemCssClass, "100%", infoItem);
	BAL.DOM.addClass(infoItem, hideItemCssClass);
}

function _hasPendingInfoItems() {
	var infoServiceContainer = BAL.getTopLevelDocument().getElementById(infoServiceContainerId);
	return infoServiceContainer != null && _getPendingInfoItems().length > 0;
}

function _appendInfoItems(infoItems) {
	let pendingInfoItems = _getPendingInfoItems();
	pendingInfoItems.push(...infoItems);
}

function _getPendingInfoItems() {
	var infoServiceContainer = BAL.getTopLevelDocument().getElementById(infoServiceContainerId);
	if(infoServiceContainer.pendingInfoItems == null) {
		infoServiceContainer.pendingInfoItems = [];
	}
	return infoServiceContainer.pendingInfoItems;
}

function _showInfoServiceBox(infoServiceContainer) {
	var topLevelDocument = BAL.getTopLevelDocument();
	BAL.insertAdjacentHTML(infoServiceContainer, "beforeend", _getPendingInfoItems()[0]);
	var infoItem = BAL.DOM.getLastElementChild(infoServiceContainer);
	infoItem.style.zIndex = 100000 - BAL.DOM.getChildElementCount(infoServiceContainer);
	if(BAL.DOM.containsClass(infoServiceContainer, "tl-info-service_pinned")) {
		BAL.DOM.addClass(infoItem, "tl-info-service-item__show-pinned-info_animation");
		_modifyItemAnimation("tl-info-service-item__show-pinned-info_animation", "0%", infoItem);
	} else {
		BAL.DOM.addClass(infoItem, "tl-info-service-item__show-info_animation");
		_modifyItemAnimation("tl-info-service-item__show-info_animation", "0%", infoItem);
	}
	var showNextItemListener = function(event) {
		BAL.removeAnimationEndListener(infoItem, showNextItemListener);
		_getPendingInfoItems().shift();
		if(_hasPendingInfoItems()) {
			_showInfoServiceBox(infoServiceContainer);
		} else {
			BAL.setScrollTopElementAnimated(infoServiceContainer, BAL.getElementY(infoItem), 500);
			if(!BAL.DOM.containsClass(infoServiceContainer, "tl-info-service_pinned")) {
				infoServiceContainer.hideInfoServiceContainerTimerId = BAL.getTopLevelWindow().setTimeout(function() {
					BAL.DOM.addClass(infoServiceContainer, "tl-info-service-item__hide-info-service_animation");
					infoServiceContainer.removeInfoServiceListener = function(event) {
						if(BAL.DOM.containsClass(infoServiceContainer, "tl-info-service-item__hide-info-service_animation")) {
							BAL.getBodyElement(topLevelDocument).removeChild(infoServiceContainer);
						}
					};
					BAL.addAnimationEndListener(infoServiceContainer, infoServiceContainer.removeInfoServiceListener);
				}, services.infoServiceFadeoutTimerSeconds * 1000);
			}
		}
	};
	BAL.addAnimationEndListener(infoItem, showNextItemListener);
}

function _modifyItemAnimation(keyframesName, step, infoItem) {
	var keyframes = BAL.findKeyframesRule(keyframesName);
	if (keyframes) {
		BAL.changeRule(keyframes, step, step + " {opacity: 0; margin-top: -" + BAL.getElementHeight(infoItem) + "px;}");
	}
}

if(typeof services === "undefined") {
	var services = {};
}

services.util = {
		
	/**
	 * Creates a two key based map.
	 */
	TwoKeyMap: function() {
		this._map = new Map();
	}

}

services.util.TwoKeyMap.prototype = {
		
		get: function(key1, key2) {
			var innerMap = this._map.get(key1);
			
			if(innerMap !== undefined){
				return innerMap.get(key2);
			}
			
			return undefined;
		},
		
		set: function(key1, key2, value) {
			var value1 = this._map.get(key1);
			
			if(value1 !== undefined){
				value1.set(key2, value);
			} else {
				var innerMap = new Map();
				innerMap.set(key2, value);
				this._map.set(key1, innerMap);
			}
		},
		
		has: function(key1, key2) {
			var innerMap = this._map.get(key1);
			
			if(innerMap !== undefined){
				return innerMap.has(key2);
			}
			
			return false;
		}
}
