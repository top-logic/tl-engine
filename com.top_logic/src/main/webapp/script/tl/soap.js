/**
 * Webservice implementation in JavaScript. 
 *
 * This class allows to send SOAP messages and receive corresponding 
 * replies. Replies are directed to a custom handler function. 
 * 
 * Author:  <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 */
function WebService(serviceUrl) {
	this.serviceUrl = serviceUrl;
	
	this.NEWLINE = '\r\n';

	this.onError = function(status) {
		alert(status);
	};
	
	this.onCompletion = function() {};

	/**
	 * Browser independent creation of a XMLHTTP object.
	 */
	this.createXMLHTTP = function() {
		try {
			// Native object supported by most browsers (including upcomming MS IE 7.0.
			return new XMLHttpRequest();
		} catch (e) {
			try {
				return new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
				return new ActiveXObject("Msxml2.XMLHTTP");
			}
		}
	};

	/**
	 * Sends a request asynchron to the server
	 * 
	 * @param {String}
	 *            request the XML request send to the server
	 * @param {Object}
	 *            onCompletion handler which is called when the response of the
	 *            server arrives. is calles with the response XML. may be null.
	 * @param {Object}
	 *            onError handler which is called when an error occurs. may be
	 *            null.
	 * @param {Boolean}
	 *            ignoreResponse whether the response of the server is irrelevant
	 * @param {Boolean}
	 *            sequential whether to wait for the answer to be returned from 
	 *            the server before continuing execution
	 */
	this.sendRequest = function(request, onCompletion, onError, ignoreResponse, sequential, retry) {
		var http = this.createXMLHTTP();

		http.open("POST", this.CONTEXT_PATH + this.serviceUrl, /*async*/ !sequential);
		http.setRequestHeader("SOAPAction", "\"\"");
		http.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
		if (retry != null) {
			http.setRequestHeader("X-Retry", "" + retry);
		}
		
		if (!ignoreResponse) {
    		if (onCompletion == null) {
				onCompletion = this.onCompletion;
			}

			if (onError == null) {
				onError = this.onError;
			}

			http.onreadystatechange = function() {
				if (http.readyState == 4) { // means all data are received
					var status = http.status;
					if (status == 200) { // means everything OK
						// see #4213, #6742, and AJAXServlet
						var responseServer = http.getResponseHeader("X-Response-Server");
						if (responseServer == null || responseServer.indexOf("tl-ajax") < 0) {
							BAL.clientLog("Invalid AJAX response: " + http.responseText);
							onError(services.i18n.SERVER_CLIENT_COMMUNICATION_CRASHED, this.ERROR_NO_AJAX_RESPONSE);
							return;
						}

						// DEBUG: Display response message.
						//
						// alert("AJAX-RESPONSE:\n"+ http.responseText);

						var response = http.responseXML;

						if (response == null || response.documentElement == null) {
							// Signals parse error in IE.
							var message = "Illegal AJAX response: "	+ http.responseText;
							services.log.error(message);
							onError(services.i18n.INVALID_XML_RESPONSE, this.ERROR_CANNOT_PARSE_RESPONSE);
							return;
						}

						if (response.documentElement.namespaceURI == "http://www.mozilla.org/newlayout/xml/parsererror.xml") {
							// Signals parse error in FF.
							var message = "Illegal AJAX response ("	+ response.documentElement.textContent + "): " + http.responseText;
							services.log.error(message);
							onError(services.i18n.INVALID_XML_RESPONSE, this.ERROR_CANNOT_PARSE_RESPONSE);
							return;
						}

						onCompletion(response);
					} else {
						onError(services.i18n.NETWORK_ERROR + " (HTTP/" + status + ")", status);
					}

					/**
					 * see #32: must delete the onreadystatechange function of
					 * the http-request object to give the IE a chance to remove
					 * it
					 */
					delete http['onreadystatechange'];
				}
			};
		}
		http.send(request);
	};
};

WebService.prototype = {
	ERROR_NO_AJAX_RESPONSE: -1,
	ERROR_CANNOT_PARSE_RESPONSE: -2
};

if(typeof services === "undefined") {
	var services = {};
}

// Anchor for internationalized texts displayed directly from static JavaScript functions.
services.i18n = {
	INCONSISTENT_SERVER_REPIES: "Inconsistent server replies, page is reloaded. Please check your inputs."
};