// Copyright (c) BOS GmbH 2002/2003
// author MBA 20.06.2002
// functions for the conference

var redirectModuleURL           = "/top-logic/jsp/communication/conference/conferenceWindowTemplate.jsp?";
var controller  		        = "/top-logic/servlet/ConferenceController";

var moduleURL    			    = controller+"?command=loginToConference&info=";
var adminURL     			    = controller+"?command=cmdAdministrate";
var roomAdminURL 			    = controller+"?command=cmdChangeUserRights&info=";
var editSessionsURL  			= "/top-logic/jsp/communication/conference/EditSessionsPage.jsp";
var readSessionsURL  			= "/top-logic/jsp/communication/conference/ReadSessionsPage.jsp";
var	showSessionAllMessageURL 	= "/top-logic/jsp/communication/conference/SessionAllMessagePage.jsp";
var showAllMessageOfRoomURL		= "/top-logic/jsp/communication/conference/AllMessageOfRoomPage.jsp";
var conferenceWindowWidth             = 800;
var conferenceWindowHeight            = 600;
var conferenceControlWindowWidth      = 800;
var conferenceControlWindowHeight     = 600;
var allMessageWindowWidth 	          = 600;
var allMessageWindowHeight            = 450;

// mba : added I18N strings - TBD: should be loaded from resources_de.js in tl tag
var _i18n_conference_conference      = "&nbsp;Konferenzraum&nbsp;";
var _i18n_conference_administration  = "&nbsp;Administration&nbsp;der&nbsp;Konferenzrauml;ume&nbsp;";
var _i18n_conference_userrights      = "&nbsp;Benutzerrechte&nbsp;Konferenzraum:&nbsp;";
var _i18n_conference_userrights      = "&nbsp;Benutzerrechte&nbsp;Konferenzraum:&nbsp;";
var _i18n_conference_modify          = "&nbsp;Bearbeitung der Konferenzsessions&nbsp;";
var _i18n_conference_allmessages     = "&nbsp;Alle&nbsp;Nachrichten&nbsp;";


// Creates a plain window without any (tool/scroll)-bars.
// If top and left are both negative numbers, the window will be centered on the screen.
 function openPlainWindow (id, url, width, height, top, left) {
 	 if (top  < 0) top  = (screen.availHeight-height)/2;
 	 if (left < 0) left = (screen.availWidth-width)/2; 
    return window.open(url,id,"status=no,resizable=yes,scrollbars=no,toolbar=no,locationbar=no,WIDTH="+width+",HEIGHT="+height+",top="+top+",left="+left);
 }

 /** make any text useable as identifer.
  *  (a quick hack to cretae js-identifierss form KO-Ids
  */
    function makeJSName (aString)  {
        result = "";
        len    = aString.length;
        for (i=0; i<len; i++) {
            c = aString.charCodeAt(i);
            if (((c>=65) && (c<=90)) || ((c>=97) && (c<=122)) || ((c>=48) && (c<=57)))
                result += c;
            else
                result += '_';
        }
        return result;
        alert(result);
    }

 function openConferenceWindowWithRoom(roomId, roomName) {
 	redirectedURL = redirectModuleURL +
 	                "title=" + escape (_i18n_conference_conference+'<b>'+roomName+'</b>') +
 	                "&url="  + escape (moduleURL+roomId);
	//alert(redirectedURL);
	//conferenceRoomWindow = openPlainWindow ("conferenceRoom"+roomId, redirectedURL, conferenceWindowWidth, conferenceWindowHeight, -1, -1);
	// for DEMO use new window for each conference room
	conferenceRoomWindow = openPlainWindow (makeJSName(roomName), redirectedURL, conferenceWindowWidth, conferenceWindowHeight, -1, -1);
 }

 function openConferenceAdminWindow() {
 	redirectedURL = redirectModuleURL +
 	                "title=" + escape (_i18n_conference_administration) +
 	                "&url="  + escape (conferenceAdminURL);
	conferenceAdminWindow = openPlainWindow ("conferenceAdmin", redirectedURL, conferenceControlWindowWidth, conferenceControlWindowHeight, -1, -1);
 }

 function openConferenceRoomAdminWindow(roomId, roomName) {
 	redirectedURL = redirectModuleURL +
 	                "title=" + escape (_i18n_conference_userrights+'<b>'+roomName+'</b>') +
 	                "&url="  + escape (conferenceRoomAdminURL+roomId);
	conferenceAdminWindow = openPlainWindow ("conferenceRoomAdmin"+makeJSName(roomName) (roomId), redirectedURL, conferenceControlWindowWidth, conferenceControlWindowHeight, -1, -1);
 }
 
 
 function openReadSessionsWindow(roomNumber, sessionId) {
 	parameters 	  = "?command=startEditSessionPage&" +
				   	"conferenceRoomId=" + roomNumber +
 					"&sessionId=" + sessionId ;
 	redirectedURL = redirectModuleURL +
 					"title=" + escape(_i18n_conference_modify) +
 					"&url=" + escape(readSessionsURL + parameters);
 					
 					
 	editSessionsWindow = openPlainWindow("EditSession", redirectedURL, conferenceControlWindowWidth, conferenceControlWindowHeight, -1, -1);
 }
  
 function openEditSessionsWindow(roomNumber, sessionId) {
 	parameters 	  = "?command=startEditSessionPage&" +
				   	"conferenceRoomId=" + roomNumber +
 					"&sessionId=" + sessionId ;
 	redirectedURL = redirectModuleURL +
 					"title=" + escape(_i18n_conference_modify) +
 					"&url=" + escape(editSessionsURL + parameters);
 					
 					
 	editSessionsWindow = openPlainWindow("EditSession", redirectedURL, conferenceControlWindowWidth, conferenceControlWindowHeight, -1, -1);
 }
 
 function openSessionAllMessageWindow(command, roomNumber, sessionId) {
 	parameters 	  = "?command=" + command + "&" +
				   	"conferenceRoomId=" + roomNumber +
 					"&sessionId=" + sessionId ;
 	redirectedURL = redirectModuleURL +
 					"title=" + escape(_i18n_conference_allmessages) +
 					"&url=" + escape(showSessionAllMessageURL + parameters); 
 					
 	openSessionAllMessageWindow = openPlainWindow("showSessionAllMessage", redirectedURL, allMessageWindowWidth, allMessageWindowHeight, -1, -1);
 }
 
 function openAllMessageOfRoomWindow(roomNumber) {
 	parameters 	  = "?conferenceRoomId=" + roomNumber;
 	redirectedURL = redirectModuleURL +
 					"title=" + escape(_i18n_conference_allmessages) +
 					"&url=" + escape(showAllMessageOfRoomURL + parameters) +
 					"&closeButton=true";
 					
 	openAllMessageOfWindow = openPlainWindow("showAllMessageOfRoom", redirectedURL, allMessageWindowWidth, allMessageWindowHeight, -1, -1);
 }
 
 