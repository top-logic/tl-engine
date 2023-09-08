/* 
 * JavaScript functions used for various form handling tasks.
 * 
 * Based on trimString.js, originally found in POS.
 * 
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Sets the (text cursor) focus into the first editable component of the first form.
 */
function selectFirst () {
    var theElement = null;
    var thePos     = 0;
    var theForm    = document.forms[0];

    if (theForm && theForm.elements) {
        while (theElement == null && thePos < theForm.elements.length) {
            theElement = theForm.elements [thePos];

            if ((typeof theElement.type == 'undefined') || theElement.type == "hidden" || theElement.disabled) {
                theElement = null;
                thePos++;
            }

        }

        if (theElement != null) {
            theElement.focus();

            if (theElement.type != "select-one"
            		&& theElement.type != "button" 
                    && theElement.type != "submit"
                    && theElement.type != "reset") {
                theElement.select();
            }
        }
    }
}

/** 
 * Trim the leading and trailing spaces of a string.
 */
function trim(value) {
   var temp = value;
   var obj = /^\s*(\S*)\s*$/;
   if (obj.test(temp)) { temp = temp.replace(obj, '$1'); }
   if (temp.length > 0 && temp.charAt(0) == " ") { temp = ""; }
   return temp;
}

/**
 * Trim the leading and trailing spaces of a string 
 * and replaces also the interior multiple spaces to single space.
 */  
function trimString(value) {
    var temp = trim(value);
        temp = temp.replace(/\s+/g, " "); 
        //  replace all empty charactes by one space
    if (temp == " ") { 
        temp = ""; 
    }
    return temp;
}

/** 
 * Checks for Strings using alphanumeric characters
 * including spaces, German Umlauts and Brackets
 */
function makeAlphanumericDE (value) {
    var suchRegExp = /^[a-zA-Z0-9_ ???????()]*$/;
    return suchRegExp.exec(value);
}

/** 
 * make a String conatining alphanumeric characters and undescore only.
 */
function makeAlphanumeric (value) {
    
     return value.replace(/[^\w]/,"");  
     // Replace any no word character by empty String
}

/** 
 * Check the length of a String and eventually truncate it.
 * 
 * Will eventuall complain via JScript about length limits.
 */
function checkMinMax (min, max, field) {
    var value = field.value = trim(field.value);
    if (min != 0 && value.length < min) {
        alert("Not enought characters, need at least " + min);
        field.select();
        return false;
    }
    if (max != 0 && value.length > max) {
        alert("Too many characters, will be truncated to " + max);
        field.value = value.substring(0, max);
        field.select();
        return false;
    }
    field.value = value;
    return true;
}


/* Calendar methods */ 

/**
 *  Opens a new window containing a calendar in which the user can
 *  select a date.
 * 
 * @param appContext   path to webapplication.
 * @param anElementID  The ID of the element (preferrably an input field) in
 *                     which the chosen date will be written.
 * @param aCurrentDate The date displayed by the calendar as 'current'
 */
function openCalendar(appContext, anElementID, aCurrentDate) {
    theurl = appContext + "/jsp/util/Calendar.jsp" + "?Date=" + aCurrentDate + "&elementID=" + anElementID;
    calendarWindow = openDialog ("calendarWindow", theurl, 250, 170);
}

/** 
 * Called by Calendar.jsp. Sets the value of the specified element
 * to aDateString.
 */
function setCalendarDate(aDateString, anElementID) {
    var theElement = document.getElementsByName(anElementID)[0];
    if (theElement != null) {
        theElement.value = aDateString;
        if (theElement.onchange) {
	        theElement.onchange();
		}
    }
    calendarPostProcessing();
}

/** 
 * Empty method to ensure that method call in setCalendarDate() does
 * not raise an error. Overwrite this method in your JSP when needed.
 */
function calendarPostProcessing() {
}

/**
 * Switch the images of a beacon
 * For ever beacon there is an hidden inoutfield: <myName>
 * For <count> beacons ther are images with id <myName>0 ... <myName><count>
 * myImage is the ne image fo the img tag.
 * empty image is the empty image to use fo the other tags
 * value ist to be set as value for the hidden field 
 
 used by form:icon resp. IconTag
 */
 
function setBeacon(thisImg, count, myName, formName, myImg, emptyImg, value) {
    for (i = 0; i < count; i++) {
        document.getElementById(myName + i).src = emptyImg;
    }
    thisImg.src = myImg;
    document[formName][myName].value = value;
}

/** Script used by form:tristate */
function change3StateCheckbox (anID, aContextPath) {
	var element = document.getElementById (anID);
	if (element) {
		// Calculate new state: dontcare (null)->false->true->dontcare->...
		theState    = element.value;
		theNewState = null;
		if (!theState) {
			theNewState = "false";
		}
		else if (theState == "false") {
			theNewState = "true";
		}

		// Set the value in the hidden input
		element.value = theNewState;
		
		// Set the image
		var theImg =  document.getElementById ("img_" + anID)
		if (theImg) {
			theImg.src=aContextPath+"/images/tristate/" + theNewState + ".png";;
		}
	}
	    
	return true;
}


