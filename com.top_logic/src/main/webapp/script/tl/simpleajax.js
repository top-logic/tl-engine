/**
 * Webservice for executing AJAX commands.
 * 
 * Author: <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 * 
 * @see test/xml/simpleajax/simpleajax.xsd
 */
services.AJAXServiceClass = function() {

	this.AJAX_NS = "http://top-logic.com/base/service/ajax";
	this.XHTML_NS = "http://www.w3.org/1999/xhtml";
	
	this.ACTIONS_ELEMENT       = new BAL.QName(this.AJAX_NS, "actions");
	this.ACTION_ELEMENT        = new BAL.QName(this.AJAX_NS, "action");
	this.ID_ELEMENT            = new BAL.QName(this.AJAX_NS, "id");
	this.STOP_ID_ELEMENT       = new BAL.QName(this.AJAX_NS, "stop-id");
	this.FRAGMENT_ELEMENT      = new BAL.QName(this.AJAX_NS, "fragment");
	this.CODE_ELEMENT          = new BAL.QName(this.AJAX_NS, "code");
	this.PROPERTY_NAME_ELEMENT = new BAL.QName(this.AJAX_NS, "property-name");
	this.OLD_VALUE_ELEMENT     = new BAL.QName(this.AJAX_NS, "old-value");
	this.NEW_VALUE_ELEMENT     = new BAL.QName(this.AJAX_NS, "new-value");
	this.PROPERTY_ELEMENT      = new BAL.QName(this.AJAX_NS, "property");
	this.FUNCTION_ELEMENT      = new BAL.QName(this.AJAX_NS, "function");
	this.ARGUMENTS_ELEMENT     = new BAL.QName(this.AJAX_NS, "arguments");
	this.SCRIPT_ELEMENT        = new BAL.QName(this.XHTML_NS, "script");

	this.IS_PROPERTY_ELEMENT = this.PROPERTY_ELEMENT.getPredicate();
	
	this.REQUEST_QUEUE = new queuing.RequestQueue(10);
	// is activated in on load
	this.REQUEST_QUEUE.stop();
	
	this.lazyRequests = new services.LinkedMap();
	
	
	this.parseAction = function(action) {
		var result = {};
		for (var child = action.firstChild; child != null; child = child.nextSibling) {
			if (this.ID_ELEMENT.matches(child)) {
				result.id = BAL.DOM.getTextContent(child);
			}
			else if (this.STOP_ID_ELEMENT.matches(child)) {
				result.stopId = BAL.DOM.getTextContent(child);
			}
			else if (this.FRAGMENT_ELEMENT.matches(child)) {
				result.fragment = BAL.DOM.getTextContent(child);
			}
			else if (this.PROPERTY_NAME_ELEMENT.matches(child)) {
				result.property = BAL.DOM.getTextContent(child);
			}
			else if (this.OLD_VALUE_ELEMENT.matches(child)) {
				result.oldValue = child;
			}
			else if (this.NEW_VALUE_ELEMENT.matches(child)) {
				result.newValue = child;
			}
			else if (this.FUNCTION_ELEMENT.matches(child)) {
				result.functionDef = child;
			}
			else if (this.ARGUMENTS_ELEMENT.matches(child)) {
				result.argumentsDef = child;
			}
		}
		return result;
	}
	
	/**
	 * Extract the identifier of the target element from an action element of
	 * the response message.
	 */
	this.getID = function(action) {
		try {
			return BAL.DOM.getTextContent(BAL.DOM.elementsByQName(action, this.ID_ELEMENT).next());
		} catch (ex) {
			throw new Error("No element '" + this.ID_ELEMENT + "' in action '" + this.getActionType(action) + "'.");
		}
	};

	this.getPropertyName = function(action) {
		try {
			return BAL.DOM.getTextContent(BAL.DOM.elementsByQName(action, this.PROPERTY_NAME_ELEMENT).next());
		} catch (ex) {
			throw new Error("No element '" + this.PROPERTY_NAME_ELEMENT + "' in action '" + this.getActionType(action) + "'.");
		}
	};

	this.getOldValue = function(action) {
		try {
			return BAL.DOM.elementsByQName(action, this.OLD_VALUE_ELEMENT).next();
		} catch (ex) {
			throw new Error("No element '" + this.OLD_VALUE_ELEMENT + "' in action '" + this.getActionType(action) + "'.");
		}
	};

	this.getNewValue = function(action) {
		try {
			return BAL.DOM.elementsByQName(action, this.NEW_VALUE_ELEMENT).next();
		} catch (ex) {
			throw new Error("No element '" + this.NEW_VALUE_ELEMENT + "' in action '" + this.getActionType(action) + "'.");
		}
	};
	
	/**
	 * Extract the identifier of the node, at which the range of a
	 * RangeReplacement should stop.
	 */
	this.getStopID = function(action) {
		try {
			return BAL.DOM.getTextContent(BAL.DOM.elementsByQName(action, this.STOP_ID_ELEMENT).next());
		} catch (ex) {
			throw new Error("No element '" + this.STOP_ID_ELEMENT + "' in action '" + this.getActionType(action) + "'.");
		}
	};

	this.getFunction = function(action) {
		try {
			return BAL.DOM.elementsByQName(action, this.FUNCTION_ELEMENT).next();
		} catch (ex) {
			throw new Error("No element '" + this.FUNCTION_ELEMENT + "' in action '" + this.getActionType(action) + "'.");
		}
	};

	this.getArguments = function(action) {
		try {
			return BAL.DOM.elementsByQName(action, this.ARGUMENTS_ELEMENT).next();
		} catch (ex) {
			throw new Error("No element '" + this.ARGUMENTS_ELEMENT + "' in action '" + this.getActionType(action) + "'.");
		}
	};

	/**
	 * Create a range pointing to the contents that should be displayed inside
	 * the target element.
	 */
	this.getFragment = function(action) {
		try {
			var result = BAL.DOM.createRange(action.ownerDocument);
			result.selectNodeContents(BAL.DOM.elementsByQName(action, this.FRAGMENT_ELEMENT).next());
			return result;
		} catch (ex) {
			throw new Error("No element '" + this.FRAGMENT_ELEMENT + "' in action '" + this.getActionType(action) + "'.");
		}
	};
	
	this.showWaitPane = function() {
		this.topWindow.services.ajax._showWaitPane();
		this.statusBefore = window.status;
		window.status = services.i18n.PLEASE_WAIT_PAGE_BEING_LOADED;
	};
	
	this.hideWaitPane = function() {
		this.topWindow.services.ajax._hideWaitPane();
		window.status = this.statusBefore;
	};
	
	this.showNetworkError = function() {
		this.topWindow.services.ajax._showNetworkError();
	};
	
	this._showNetworkError = function() {
		var waitPane = document.getElementById("waitPane");
		var dialogPane = document.createElement("div");
		waitPane.parentNode.appendChild(dialogPane);
		dialogPane.outerHTML = 
			'<div id="sxNetwork" class="dlgBackground">' + 
			  '<div class="sxError">' + 
			    '<div class="sxMessage">' + 
			      services.i18n.NETWORK_ERROR_TRYING_RECONNECT + 
			    '</div>' + 
			    '<div class="sxFailure">' + 
			    services.i18n.NETWORK_ERROR_RECONNECT_FAILED + 
			    '</div>' + 
			    '<div class="sxButtons">' + 
			      '<span class="tlButton tlDangerButtonPrimary sxButton">' +
			      services.i18n.NETWORK_ERROR_RETRY + 
			      '</span>' + 
			    '</div>' + 
			  '</div>' + 
			'</div>';
	};
	
	this.showSessionTimeout = function(logoutURL) {
		this.topWindow.services.ajax._showSessionTimeout(logoutURL);
	};
	
	this._showSessionTimeout = function(logoutURL) {
		var waitPane = document.getElementById("waitPane");
		var dialogPane = document.createElement("div");
		waitPane.parentNode.appendChild(dialogPane);
		dialogPane.outerHTML = 
			'<div id="sxNetwork" class="dlgBackground sxRetry">' + 
				'<div class="sxError">' + 
					'<div class="sxFailure">' + 
						services.i18n.SETTION_TIMEOUT_FAILURE + 
					'</div>' + 
					'<div class="sxButtons">' + 
						'<span class="tlButton tlDangerButtonPrimary sxButton">' +
							services.i18n.SETTION_TIMEOUT_LOGIN + 
						'</span>' + 
					'</div>' + 
				'</div>' + 
			'</div>';
		
		var dialog = document.getElementById("sxNetwork");
		BAL.addEventListener(dialog, "click", function() {
			window.location = logoutURL;
		});
	};
	
	this.setNetworkErrorRetry = function(onClick) {
		this.topWindow.services.ajax._setNetworkErrorRetry(onClick);
	};
	
	this._setNetworkErrorRetry = function(onClick) {
		var dialogPane = document.getElementById("sxNetwork");
		BAL.DOM.addClass(dialogPane, "sxRetry");
		BAL.addEventListener(dialogPane, "click", onClick);
	};
	
	this.hideNetworkError = function() {
		this.topWindow.services.ajax._hideNetworkError();
	};
	
	this._hideNetworkError = function() {
		var dialogPane = document.getElementById("sxNetwork");
		if (dialogPane != null) {
			dialogPane.parentNode.removeChild(dialogPane);
		}
	};
	
	this._showWaitPane = function() {
		var waitPane = document.getElementById("waitPane");
		BAL.DOM.addClass(waitPane, "waiting");
		waitPane.style.cursor = "wait";

		var counter = window.services.ajax.waitPaneCounter;
		if (counter == null) {
			counter = 0;
		}
		window.services.ajax.waitPaneCounter = counter + 1;
	};

	this._hideWaitPane = function() {
		var counter = window.services.ajax.waitPaneCounter;
		counter = counter - 1;
		window.services.ajax.waitPaneCounter = counter;
		if (counter > 0) {
			return;
		}
		var waitPane = document.getElementById("waitPane");
		BAL.DOM.removeClass(waitPane, "waiting");
		waitPane.style.cursor = "default";
	};

	this.onError = function(message) {
		alert(message);
		services.ajax.mainLayout.location.reload();
		return;
	};

	this.onCompletion = function(result) {
		var self = this;
		this.executeOnCompletion(function() {self.processResult(result)});
	};
	
	this.executeOnCompletion = function(fun) {
		var iFrames = document.getElementsByTagName("IFRAME");
        if (iFrames == null) {
            throw new Error("getElementsByTagName() for \"IFRAME\" returned null");
        }
        var loading = false;
        for (var i = 0; i < iFrames.length; i++) {
        	if (this.isLoading(document, iFrames[i])) {
        		loading = true;
        		break;
        	}
        }
		if (!loading) {
			fun();
		} else {
			var outer = this;
			window.setTimeout(function(){
				outer.executeOnCompletion(fun);
			},	100);
		}
	};
	
	/**
	 * Associates the given AJAX response to its pending request and calls
	 * processLater().
	 * 
	 * This method is called, whenever a response to an AJAX request is
	 * received.
	 */		
	this.processResult = function(result) {
		var actions;
		try {
			actions = BAL.DOM.elementsByQName(result, this.ACTIONS_ELEMENT).next();
		} catch (ex) {
			throw new Error("No element '" + this.ACTIONS_ELEMENT + "' completion action.");
		}
	
		var rxSequenceNrString = BAL.DOM.getAttribute(actions, 'rx');
		if (rxSequenceNrString != null) {
			var rxSequenceNr = Number(rxSequenceNrString);

			var ajax = this.mainLayout.services.ajax;
			if ((ajax.pendingRequests.length == 0) || (rxSequenceNr < ajax.pendingRequests[0].txSequence)) {
				// This is a late response.
				
				// Silently ignore. It has been reported that this happens in unstable networks. 
				// Since the client already received this request, there is no reason to confuse 
				// the user with an error message.
				return;
			}
			
			// Update the time of the request to the time of the arrival of its
			// answer (now). This ensures that requests delayed by the server do
			// not immediately cancel subsequent requests.
			//
			// Search the corresponding request to the current reply.
			var currentRequest = null;
			var currentRequestIndex = 0;
			for (; currentRequestIndex < ajax.pendingRequests.length; currentRequestIndex++) {
				var request = ajax.pendingRequests[currentRequestIndex];
				if (rxSequenceNr == request.txSequence) {
					currentRequest = request;
					break;
				}
			}
			
			if (currentRequest == null) {
				alert("Reply for unknown request, dropped.");
				return;
			}
			
			// Remember request as successfully received.
			ajax.acknowledgedRequests.push(rxSequenceNr);
			
			// Annotate all requests up to the current request with the current time as starting
			// point of the reorder timeout. 
			//
			// The reorder timeout for a requests starts, when a response for an request with a
			// higher sequence number arrives. At that time, it is guaranteed that the server has
			// processed those requests. The only reason why the answers has not yet been receives
			// is network latency.
			var now = new Date().getTime();
			for (var n = 0; n <= currentRequestIndex; n++) {
				var request = ajax.pendingRequests[n];
				if (request.time == undefined) {
					request.time = now;
				}
			}
			
			// Enqueue the reply.
			var outer = this;
			ajax.pendingReplies[rxSequenceNr] = function() {
				outer.processActions(actions);
			};
			
			// Process pending replies.
			this.processLater();
		} else {
			this.processActions(actions);
		}
	};
	
	/**
	 * Create a new timer after calling processPendingReplies().
	 */
	this.processLater = function() {
		this.mainLayout.services.ajax.timer = null;
		
		var now = (new Date()).getTime();
		this.processPendingReplies(now);
		
		// Find the next sequence number, for which a reply is already enqueued.
		// Only, if
		// such already answered request is in the queue, a new timer must be
		// started.
		var firstPendingReplyIndex = -1;
		for (var n = 0; n < this.mainLayout.services.ajax.pendingRequests.length; n++) {
			var sequence = this.mainLayout.services.ajax.pendingRequests[n].txSequence;
			var reply = this.mainLayout.services.ajax.pendingReplies[sequence];
			if (reply != null) {
				firstPendingReplyIndex = n;
				break;
			}
		}
		
		if (firstPendingReplyIndex >= 0) {
			var waitTime = this.mainLayout.services.ajax.pendingRequests[firstPendingReplyIndex].time + this.TIMEOUT - now;
			this.mainLayout.services.ajax.timer = setTimeout(this.processLaterClosure, waitTime);
		}
	};

	/**
	 * Processes all pending replies. Drops all requests that have already timed
	 * out.
	 * 
	 * Cancel no replies, for which no concurrent replies have been received so
	 * far. This approach drops no events, if the user does never create more
	 * than one request at a time (no matter how long the server response time
	 * is).
	 */
	this.processPendingReplies = function(now) {
		// Process all pending replies in the order defined by the pending
		// requests.
		// Stop, if a request with a sequence number is found in the queue that
		// has
		// no reply yet, and the corresponding request has not yet timed out.
		var lastProcessedRequestIndex = -1;
		for (var n = 0; n < this.mainLayout.services.ajax.pendingRequests.length; n++) {
			var request = this.mainLayout.services.ajax.pendingRequests[n];
			var sequence = request.txSequence;
			var reply = this.mainLayout.services.ajax.pendingReplies[sequence];
			if ((reply == null) && ((now - this.mainLayout.services.ajax.pendingRequests[n].time) < this.TIMEOUT)) {
				// No reply, but not yet timed out. Cancel processing.
				break;
			}
		
			if (reply != null) {
				reply.call();
				
				// Process functions attached to this request. See runLater().
				if (request.actions != null) {
					for (var m = 0; m < request.actions.length; m++) {
						try {
							request.actions[m].call();
						}
						catch(ex) {
							// action is not up to date
						}
					}
					request.actions = null;
				}
				
				lastProcessedRequestIndex = n;
				delete this.mainLayout.services.ajax.pendingReplies[sequence];
			}
		}

		// Remove all processed or timed out requests up to and including
		// the the last processed request.
		if (lastProcessedRequestIndex >= 0) {
			this.mainLayout.services.ajax.pendingRequests.splice(0, lastProcessedRequestIndex + 1);
		}
	};

	this.getRequestSourceDocument = function(actions) {
		var requestSourceWindow;
		try {
			var requestSourceExpression = BAL.DOM.getAttribute(actions, 'requestsourcereference');
			if(requestSourceExpression == null || requestSourceExpression == "") {
				requestSourceWindow = null;
			} else {
				requestSourceWindow = eval(requestSourceExpression);
			}
		} catch(ex) {
			requestSourceWindow == null;
		}
		
		return requestSourceWindow != null ? requestSourceWindow.document : null;
	};

	/**
	 * Execute all received client actions.
	 */	
	this.processActions = function(actions) {
		var requestSourceDocumentBeforeUpdates = this.getRequestSourceDocument(actions);
		var topLevelDocument;
		if(requestSourceDocumentBeforeUpdates != null) {
			topLevelDocument = BAL.getTopLevelDocumentOf(requestSourceDocumentBeforeUpdates);
			BAL.getCurrentWindow(requestSourceDocumentBeforeUpdates).services.focusRestore.saveFocusState();
		}
        
        // windowReference -> array of objects for layout
        var layoutNodeByWindow = new Object();
        // windowReference -> array of scripts to execute within that window
        var scriptsByWindow = new Object();
		var errors = null;
		for (var it = BAL.DOM.elementsByQName(actions, this.ACTION_ELEMENT); it.hasNext(); ) {
			var action = it.next();

			var contextExpression = BAL.DOM.getAttribute(action, 'context');
			if (contextExpression == null) {
				contextExpression = "window";
			}

			try {
				// Find the context in which to execute the action.
				var context = eval(contextExpression);
									
				if (context == null) {
					services.log.error(
						"Undeliverable AJAX event.", 
						"The component reference is undefined: '" + contextExpression + "'. Action: " + this.getActionType(action));
					continue;
				}
			
				if (! ("services" in context)) {
					// The event is directed to a page that does not
					// support dispatching AJAX events.
					services.log.error(
						"Undeliverable AJAX event.", 
						"The component referenced in '" + contextExpression + "' does not support dispatching AJAX events.");
					continue;
				}
			
				// Redirect this action to the context for which it was
				// generated.
				context.services.ajax.processAction(action, contextExpression, layoutNodeByWindow, scriptsByWindow);
			} catch (ex) {
				errors = this.addError(errors, "exception: ", ex);
			}
		}
		
		errors = this._doLayout(layoutNodeByWindow, errors);
		
		for (var windowReference in scriptsByWindow) {
			var scripts = scriptsByWindow[windowReference];
			var contextWindow = eval(windowReference);
			for (var i = 0 ; i < scripts.length; i++) {
				var script = scripts[i];
				try {
					contextWindow.eval(script);
				} catch (ex) {
					errors = this.addError(errors, "Script execution of '" + script + "' failed: ", ex);
				}
			}
		}
		
		try {
			var requestSourceDocumentAfterUpdates = this.getRequestSourceDocument(actions);
			if(requestSourceDocumentAfterUpdates != null) {
				BAL.getCurrentWindow(requestSourceDocumentAfterUpdates).services.focusRestore.restoreFocusState();
			} else if(topLevelDocument != null){
				BAL.getCurrentWindow(topLevelDocument).services.focusRestore.restoreFocusState();
			}
		} catch(ex) {
			// Do nothing.
			// Tried to restore focus within an iframe,
			// whose content has not been retrieved until yet.
		}
		
		services.ajax.mainLayout.services.ajax.afterRendering(errors);
		
		if (errors != null) {
			services.log.error("Problems during AJAX event execution.", errors);
			services.ajax.mainLayout.services.ajax.cleanupQueuedFunctions();
		}
	};
	
	/**
	 * Layouts the formerly cached nodes.
	 * 
	 * @param {Object}
	 *            Mapping of window references to arrays containing the nodes in
	 *            that window which must be layouted.
	 * @param {String}
	 *            errors the errors to append potential errors to
	 * 
	 * @return {String} the new error messages
	 */
	this._doLayout = function(layoutNodeByWindow, errors) {
		for (var windowReference in layoutNodeByWindow) {
			var nodeSet = layoutNodeByWindow[windowReference];
			var contextWindow = eval(windowReference);
			var nodesArray;
			if (nodeSet.size() == 1) {
				nodesArray = nodeSet.toArray();
				errors = contextWindow.services.ajax._reinitLayout(nodesArray[0], errors);
			} else {
				var nodesToLayout = this.getNodesWithoutMarkedParentToLayout(nodeSet);
				nodesArray = nodesToLayout.toArray();
				for (var i = 0 ; i < nodesArray.length; i++) {
					var nodeForLayout = nodesArray[i];
					errors = contextWindow.services.ajax._reinitLayout(nodeForLayout, errors);
				}
			}
		}
		return errors;
	};
	
	this.getNodesWithoutMarkedParentToLayout = function(nodeSet) {
		var nodesToLayout = new services.NodeSet();
		var nodeArray = nodeSet.toArray();
		for(var i = 0; i < nodeArray.length; i++) {
			var node = nodeArray[i];
			if(! (this.isChildFromNodeInSet(node, nodeSet) ||
				  this.isChildFromNodeInSet(node, nodesToLayout))) {
				nodesToLayout.add(node);
			}
		}
		
		return nodesToLayout;
	};
	
	this.isChildFromNodeInSet = function(node, nodeSet) {
		if(! nodeSet.isEmpty()) {
			var checkNode = node.parentNode;
			if(checkNode != null) {
				var rootNode = BAL.getCurrentDocument(node);
				while(checkNode != rootNode) {
					if(nodeSet.contains(checkNode)) {
						return true;
					}
					checkNode = checkNode.parentNode;
				}
			}
		}
		
		return false;
	};

	/**
	 * Adds the message of the given error to the previous errors
	 * 
	 * @param {String}
	 *            the errors to append exception to
	 * @param {String}
	 *            an additional message of the exception
	 * @param {Error}
	 *            the new error
	 * 
	 * @return {String} the extended error message
	 */
	this.addError = function(errors, message, ex) {
		if (errors == null) {
			errors = "";
		} else {
			errors += ", ";
		}
		errors += message;
		errors += ex.toString();
		return errors;
	};
	
	this.getActionType = function(action) {
		return BAL.DOM.getAttribute(action, 'xsi:type');
	};
	
	/**
	 * @param {String}
	 *            windowReference a reference from the main layout to the
	 *            current window;
	 * @param {Object}
	 *            layoutNodeByWindow mapping from window references to array of
	 *            nodes in that window to be layed out later. If the given
	 *            action forces a re-layout of some node, that node can be added
	 *            to the corresponding array.
	 * @param {Object}
	 *            scriptsByWindow mapping from window references to array of
	 *            scripts to be evaluated later. If the given action forces
	 *            executing some scripts, they can be added to the corresponding
	 *            array.
	 */
	this.processAction = function(action, windowReference, layoutNodeByWindow, scriptsByWindow) {
		
		var type = this.getActionType(action);
		if (type == 'JSSnipplet') {
			var codeElement;
			try {
				codeElement = BAL.DOM.elementsByQName(action, this.CODE_ELEMENT).next();
			} catch (ex) {
				throw new Error("No element '" + this.CODE_ELEMENT + "' action '" + type + "'.");
			}
			var scripts = scriptsByWindow[windowReference];
			if (scripts == null) {
				scripts = new Array();
				scriptsByWindow[windowReference] = scripts;
			}
			scripts.push(BAL.DOM.getTextContent(codeElement));
		}
		else if (type == 'ContentReplacement') {
            var actionObject = this.parseAction(action);
			var id = actionObject.id;
			
			// Lookup the target element in the current document.
			var targetNode = window.document.getElementById(id);
			if (targetNode == null) {
				throw new Error("There is no element with id '" + id + "'");
			}
			
			for (var child = targetNode.firstChild; child != null; child = child.nextSibling) {
	            BAL.DOM.triggerEachNode(child, "DOMNodeRemovedFromDocument");
			}
			var fragment = actionObject.fragment;
			BAL.setInnerHTML(targetNode, fragment);
			this._addScripts(windowReference, scriptsByWindow, targetNode, null, null);
			this._markNodeForLayout(windowReference, layoutNodeByWindow, targetNode);
		}
		else if (type == 'ElementReplacement') {
            var actionObject = this.parseAction(action);
			var id = actionObject.id;
			var targetNode = window.document.getElementById(id);
			if (targetNode == null) {
				throw new Error("There is no element with id '" + id + "'");
			}
			
            BAL.DOM.triggerEachNode(targetNode, "DOMNodeRemovedFromDocument");
            // This node and all nextSiblings are ignored in script determination
            var next = targetNode.nextSibling;
            // This node and all previousSiblings are ignored in script determination
            var previous = targetNode.previousSibling;
            
			var targetNodeParent = targetNode.parentNode;
			var fragment = actionObject.fragment;
			BAL.setOuterHTML(targetNode, fragment);
			
			this._addScripts(windowReference, scriptsByWindow, targetNodeParent, previous, next);
			this._markNodeForLayout(windowReference, layoutNodeByWindow, targetNodeParent);
		}
		else if (type == 'PropertyUpdate') {
			var id = this.getID(action);
			var target = window.document.getElementById(id);
			if (target == null) {
				throw new Error("There is no element with id '" + id + "'");
			}
			for (var it = BAL.DOM.elementsByQName(action, this.PROPERTY_ELEMENT); it.hasNext(); ) {
				var property = it.next();
				
				var name = property.getAttribute("name");
				var value = property.getAttribute("value");
				
				// Maximal simple parsing of values, only String and Boolean
				// types is supported. Of cause, this might prevent setting a
				// "title" property to "false" (as string). But is is required
				// to set e.g. the "disabled" property at all. The heavy-weight
				// (and correct solution would be to switch to XMLValueDecoder
				// and encode things accordingly on the server side). Another
				// valid solution would be to parse values dependent on the
				// "type" of the property to set.
				var jsValue;
				if (value == "true") {
					jsValue = true;
				} else if (value == "false") {
					jsValue = false;
				} else {
					jsValue = value;
				}
				
				// alert(
				// "Updating property '" + name + "'" +
				// " of element '" + target.tagName + "'." + target.id + "" +
				// " to '" + value + "'.");
				if (property.getAttribute("namespace") == null && name.indexOf("data-") != 0) {
					target[name] = jsValue;
					if (name == "value") {
						// Also update the default value property to be able to decide, whether a value 
						// has actually changed. This is necessary at least when changing the active grid
						// row with RETURN in Opera and the the server sends a value normalization 
						// (e.g. "9" -> "9,000") upon the value change event. Without updating the default
						// value, the onchange handlers of input controls are not able to detect that no
						// change is made. Since `onchange` is triggered twice during keycode handling, 
						// The second invocation of `onchange` must be prevented, even if an intermediate
						// update is performed by the server.
						target["defaultValue"] = jsValue;
					}
				} else {
					BAL.DOM.setNonStandardAttribute(target, name, jsValue);
				}
			}
		}
		else if (type == 'CssClassUpdate') {
			var id = this.getID(action);
			var target = window.document.getElementById(id);
			if (target == null) {
				throw new Error("There is no element with id '" + id + "'");
			}
			for (var it = BAL.DOM.elementsByQName(action, this.PROPERTY_ELEMENT); it.hasNext(); ) {
				var property = it.next();
				var classAttributeName = "class";
				var classAttributeValue = BAL.DOM.getNonStandardAttribute(property, classAttributeName);
				if(classAttributeValue != null) {
					BAL.DOM.setNonStandardAttribute(target, classAttributeName, classAttributeValue);
				} else {
					if(BAL.DOM.getNonStandardAttribute(target, classAttributeName) != null) {
						BAL.DOM.removeNonStandardAttribute(target, classAttributeName);
					}
				}
			}
		}
		else if (type == 'RangeReplacement') {
            var actionObject = this.parseAction(action);
			var startId = actionObject.id;
			var stopId = actionObject.stopId;
			
			var startNode = window.document.getElementById(startId);
			if (startNode == null) {
				throw new Error("There is no element with startId '" + startId + "'");
			}
			var stopNode = window.document.getElementById(stopId);
			if (stopNode == null) {
				throw new Error("There is no element with stopId '" + stopId + "'");
			}
			if (startNode.parentNode != stopNode.parentNode) {
				throw new Error("Start and stop node are not sibblings (" + startId + ", " + stopId + ").'");
			}
			
            // This node and all nextSiblings are ignored in script determination
			var previous = startNode.previousSibling;
			// This node and all previousSiblings are ignored in script determination
			var next = stopNode.nextSibling;
			var parent = startNode.parentNode;
			var fragment = actionObject.fragment;
			BAL.insertAdjacentHTML(startNode, "beforebegin", fragment);
			var node = startNode; 
			while (true) {
				var nextNode = node.nextSibling;
				var lastNode = node == stopNode;

				BAL.DOM.triggerEachNode(node, "DOMNodeRemovedFromDocument");

				parent.removeChild(node);
				if (lastNode) {
					break;
				}
				if (nextNode == null) {
					throw new Error("Stop node not a following-sibling of start node (" + startId + ", " + stopId + ").'");
				}
				node = nextNode;
			}
			
			this._addScripts(windowReference, scriptsByWindow, parent, previous, next);
			this._markNodeForLayout(windowReference, layoutNodeByWindow, parent);
		}
		else if (type == 'FragmentInsertion') {
            var actionObject = this.parseAction(action);
			var id = actionObject.id;
			var position = BAL.DOM.getAttribute(action, 'position');

			var targetNode = window.document.getElementById(id);
			if (targetNode == null) {
				throw new Error("There is no element with id '" + id + "'");
			}

			var layoutParent;
            // This node and all nextSiblings are ignored in script determination
			var previous;
			// This node and all previousSiblings are ignored in script determination
			var next;
			if (position == 'beforebegin') {
				layoutParent = targetNode.parentNode;
				previous = targetNode.previousSibling;
				next = targetNode;
			} else if (position == 'afterend') {
				layoutParent = targetNode.parentNode;
				previous = targetNode;
				next = targetNode.nextSibling;
			} else if (position == 'afterbegin') {
				layoutParent = targetNode;
				previous = null;
				next = targetNode.firstChild;
			} else if (position == 'beforeend') {
				layoutParent = targetNode;
				previous = targetNode.lastChild;
				next = null;
			} else {
				throw new Error("Unknown position specifier: " + position);
			}
			
			var fragment = actionObject.fragment;
			BAL.insertAdjacentHTML(targetNode, position, fragment);
			
			this._addScripts(windowReference, scriptsByWindow, layoutParent, previous, next);
			this._markNodeForLayout(windowReference, layoutNodeByWindow, layoutParent);
		}
		else if (type == 'AJAXPropertyEvent') {
			var id = this.getID(action);
			var decoder = new XMLValueDecoder();

			var event = {
				source: id,
				propertyName: this.getPropertyName(action),
				oldValue: decoder.decode(BAL.DOM.getFirstElementChild(this.getOldValue(action))),
				newValue: decoder.decode(BAL.DOM.getFirstElementChild(this.getNewValue(action)))
			};

			services.form.propertEventQueue.handlePropertyEvent(event);
		}
		else if (type == 'FunctionCall') {
			var decoder = new XMLValueDecoder();
			var target = this.getID(action);
			var fun = this.getFunction(action);
			var ref = fun.getAttribute('ref');
			var name = fun.getAttribute('name');
			var arguments = decoder.decode(BAL.DOM.getFirstElementChild(this.getArguments(action)));

			var targetNode = document.getElementById(target);
			if (targetNode == null) {
				throw new Error("Referenced element '" + target + "' does not exist. action='" + type + "', ref='" + ref + "'");
			}
			
			var obj = eval(ref);
			var functionReference = obj[name];
			if (typeof(functionReference) != 'function') {
				throw new Error("Not a function. reference='" + ref + "', name='" + name + "'");
			}
			arguments.unshift(targetNode);
			functionReference.apply(obj, arguments);
		}
		else {
			alert("Received unknown action: type=" + type + ", contents=" + action);
			throw new Error('Unknown action: ' + type);
		}
	};
	
	/**
	 * Marks the given node to be layouted later.
	 * 
	 * @param {String}
	 *            windowReference a reference of the window inwhich the given
	 *            node lives
	 * @param {Object}
	 *            layoutNodeByWindow the storage object given to
	 *            processActions()
	 * @param {Node}
	 *            the nod to mark to be layouted later.
	 */
	this._markNodeForLayout = function(windowReference, layoutNodeByWindow, node) {
		// Node is not layed out so no need for reinit Layout
		if (node.layout == null) {
			return;
		}
		var layoutNodes = layoutNodeByWindow[windowReference];
		if (layoutNodes == null) {
			layoutNodes = new services.NodeSet();
			layoutNodeByWindow[windowReference] = layoutNodes;
		}
		layoutNodes.add(node);
	};
	
	/**
	 * Reinitializes the layout of the given element.
	 * <p>
	 * If the given element was laid out using layout.js (i.e. the element has a
	 * js-object 'layout' the complete element layout will be rebuild (it may
	 * happen that some new content did not already laid out) and laid out.
	 * </p>
	 */
	this._reinitLayout = function(element, errors) {
		try {
			services.layout.renderLayout(element);
		} catch (ex) {
			errors = this.addError(errors, "Exception: ", ex);
		}
		return errors;

	};
	
	/**
	 * Selects inline scripts in the given action and adds them to the given
	 * scripts.
	 * 
	 * @param {Object}
	 * 		The window in which the scripts must be executed.
	 * @param {Object}
	 * 		Cached scripts by window.
	 * @param {Node}
	 * 		Root of the subtree to inspect.
	 * @param {Node}
	 * 		All direct children (and descendants) of the given subtree root node 
	 * 		"before" this node, and the node itself are not inspected.
	 * @param {Node}
	 * 		All direct children (and descendants) of the given subtree root node 
	 * 		"after" this node, and the node itself are not inspected.
	 */
	this._addScripts = function(windowReference, scriptsByWindow, scriptParent, before, after) {
		var tmp;
		if (before == null) {
			tmp = BAL.DOM.getFirstChild(scriptParent);
		} else {
			tmp = BAL.DOM.getNextSibling(before);
		}
		while (tmp != after){
			if (!BAL.DOM.isElementNode(tmp)) {
				tmp = BAL.DOM.getNextSibling(tmp);
				continue;
			}
			if ("script" == BAL.DOM.getTagName(tmp)) {
				if (BAL.DOM.getAttribute(tmp, "type") != "text/tlscript") {
					var scripts = scriptsByWindow[windowReference];
					if (scripts == null) {
						scripts = new Array();
						scriptsByWindow[windowReference] = scripts;
					}
					scripts.push(BAL.DOM.getTextContent(tmp));
					var sibling = BAL.DOM.getNextSibling(tmp);
					tmp.parentNode.removeChild(tmp);
					tmp = sibling; 
				} else {
					tmp = BAL.DOM.getNextSibling(tmp); 
				}
			} else {
				var scriptTags = tmp.getElementsByTagName("script");
				if (scriptTags.length > 0) {
					var extractedTags = new Array();
				
					for (var n = 0; n < scriptTags.length; n++) { 
						var scriptTag = scriptTags[n];
						if (BAL.DOM.getAttribute(scriptTag, "type") == "text/tlscript") {
							continue;
						}
						extractedTags.push(scriptTag);
						
						var scripts = scriptsByWindow[windowReference];
						if (scripts == null) {
							scripts = new Array();
							scriptsByWindow[windowReference] = scripts;
						}
						
						scripts.push(BAL.DOM.getTextContent(scriptTag));
					}
					
					for (var n = 0; n < extractedTags.length; n++) {
						var scriptTag = extractedTags[n];
						scriptTag.parentNode.removeChild(scriptTag);
					}
				}
				tmp = BAL.DOM.getNextSibling(tmp);
			}
		}
	};
	
	this.isMaster = function() {
		return this.mainLayout == window;
	};
	
	/**
	 * Creates a new (session-unique) sequence number for a request.
	 * 
	 * This function is also called from command handlers immediately before
	 * loading their invocation URL.
	 */
	this.createSequenceNumber = function() {
		return this.mainLayout.services.ajax.txSequence++;
	};
	
	this.execute = function(command, args, useWaitPane, serverResponseCallback, onError, contextInformation, sequential) {
		var requestFunction = this._getRequestFunction(command, args, useWaitPane, onError, contextInformation, sequential);
		this.mainLayout.services.ajax.REQUEST_QUEUE.executeRequest(requestFunction, serverResponseCallback);
		return false;
	};
	
	this._getRequestFunction = function(command, args, useWaitPane, onError, contextInformation, sequential) {
		var self = this;
		var requestFunction = function() {
			var componentId = self.COMPONENT_ID;
			var submitNumber = services.ajax.SUBMIT_NUMBER;
			if (contextInformation != undefined) {
				componentId = contextInformation.componentId;
				submitNumber = contextInformation.submitNumber;
			}
			
			var sequence = self.createSequenceNumber();
			
			// Invoke the command. Source and target components are identical.
			// Could be
			// extended to invoke commands on arbitrary components.
			return self.mainLayout.services.ajax.invoke(self, componentId, componentId, sequence, submitNumber, command, args, useWaitPane, onError, /* ignoreResponse */ false, sequential);
		};
		return requestFunction;
	};
	
	this.executeOrUpdateLazy = function(requestID, command, args, contextInformation) {
		/*
		 * read the submit number at the moment at which the function is called.
		 * The variant to read the submit number directly before sending the
		 * event is not used as then a problem can occur, e.g. when the
		 * corresponding component is repainted the data (for example control
		 * IDs) may be out dated.
		 */ 
		var componentId = this.COMPONENT_ID;
		var submitNumber = this.SUBMIT_NUMBER;
		if (contextInformation != undefined) {
			componentId = contextInformation.componentId;
			submitNumber = contextInformation.submitNumber;
		}
		
		var commandArguments = this.addSystemCommandProperty(args);
		var requ = {
			command : command,
			args : commandArguments,
			componentId : componentId,
			submitNumber : submitNumber
		};
		this.mainLayout.services.ajax.lazyRequests.put(requestID, requ);
		return false;
	};
	
	this.addSystemCommandProperty = function(originalCommandArguments) {
		var modifiedCommandArguments = originalCommandArguments;
		modifiedCommandArguments[services.ajax.systemCommandId] = true;
		return modifiedCommandArguments;
	};
	
	this.createLazyRequestID = function() {
		return this.mainLayout.services.ajax.lazyRequests.newKey();
	};
	
	this.invokeRead = function(command, args, onError, sequential) {
		var executeSequentially = sequential != null ? sequential : false;
		return this.mainLayout.services.ajax.invoke(this, this.COMPONENT_ID, this.COMPONENT_ID, -1, services.ajax.SUBMIT_NUMBER, command, args, /* useWaitPane */ false, onError, /* ignoreResponse */ false, executeSequentially);
	};

	this.invokeStatic = function(command, args, onError, sequential) {
		var executeSequentially = sequential != null ? sequential : false;
		return this.mainLayout.services.ajax.invoke(this, this.COMPONENT_ID, null, -1, services.ajax.SUBMIT_NUMBER, command, args, /* useWaitPane */ false, onError, /* ignoreResponse */ true, executeSequentially);
	};
	
	/**
	 * adds a new object to the list or pending requests, which represents the
	 * given sequence number.
	 * 
	 * <b> Must not be called if the cooresponding request does not need the
	 * answer of the server.</b>
	 */
	this.enqueueRequest = function(txSequence) {
		// Insert the new request in the queue of pending requests. In all
		// regular
		// cases (expect test cases that immitate delayed requests), the new
		// request
		// must be simply appended to the pending request queue to keep the
		// requests
		// in ascending order of their sequence numbers). Just to simplify test
		// cases,
		// functionality is added here that allows to insert a programmatically
		// delayed
		// request (where the sequence number is created some time before
		// actually
		// invoking this method).
	
		var insertIndex = this.mainLayout.services.ajax.pendingRequests.length;
		while ((insertIndex > 0) && (this.mainLayout.services.ajax.pendingRequests[insertIndex - 1].txSequence > txSequence)) {
			// This cannot happen during normal operation (see above).
			insertIndex--;
		}
		
		var currentRequest = {
			txSequence: txSequence, 
			time: undefined
		};
		
		// Insert into the request queue at index insertIndex.
		this.mainLayout.services.ajax.pendingRequests.splice(insertIndex, 0, currentRequest);
		
		return currentRequest;
	};
	
	/**
	 * Attaches the given function to the last AJAX request in the queue. The
	 * given function will be called after the response for the last AJAX
	 * request is received. If there is no pending request, the function is
	 * executed immediately.
	 * 
	 * Returns true, if the function was enqueued, false if the function has
	 * been executed immediately.
	 */
	this.runLater = function(fun) {
		var pendingRequestCount = this.mainLayout.services.ajax.pendingRequests.length;
		if (pendingRequestCount > 0) {
			var lastRequest = this.mainLayout.services.ajax.pendingRequests[pendingRequestCount - 1];
			if (lastRequest.actions == null) {
				lastRequest.actions = new Array();
			}

			// Attach the function to be run later to the last pending request.
			lastRequest.actions[lastRequest.actions.length] = fun;
			return true;
		} else {
			// There is no pending request. Immediately call the given function.
			fun.call();
			return false;
		}
	};
	
	this._getCommandString = function(source, target, submitNumber, command, args) {
		var encoder = new XMLValueEncoder();
		var commandString = '<execute xmlns="http://top-logic.com/base/services/ajax">' +
					'<component xmlns="http://top-logic.com/base/services/cs" ' + 
						'env:role="http://top-logic.com/component"' + 
						' source="' + source + '"' + 
							((target != null) ?	(' target="' + target + '"') : '') +
					'/>' + 
					'<submit xmlns="http://top-logic.com/base/services/cs" ' + 
						'env:role="http://top-logic.com/submit"' + 
						' value="' + submitNumber + '"'+ 
					'/>' +
				   	'<command>' + command + '</command>' + 
				   	'<arguments>' + 
				    	(function() {
			    			var result = '';
			    			for (var arg in args) {
			    				var value = args[arg];
			    				if (value === undefined) continue;
			    				
			    				var encodedValue = encoder.encode(value);
			    				
			    				// alert(encodedValue);
			    				
			    				result += 
			    					'<argument>' + 
			    						'<name>' + arg + '</name>' + 
			    						'<value>' + 
			    							encodedValue + 
			    						'</value>' + 
					    			'</argument>';
					    	}
					    	return result;
					    })() +
			    		
			    	'</arguments>' +
		    	'</execute>\n'
		;

		// alert("Command:\n" + commandString);
		return commandString;
	};

	this.encodeHTML = function (s) {
	    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
	};
	
	this.invoke = function(source, sourceID, targetID, txSequence, submitNumber, command, args, useWaitPane, onError, ignoreResponse, sequential) {
		var request = this._createRequest(sourceID, targetID, txSequence, submitNumber, command, args);
		if (!ignoreResponse && txSequence >= 0) {
			this.enqueueRequest(txSequence);
		}
		
		if (txSequence >= 0) {
			var self = this;
			var customOnError = onError;
			if (customOnError == null) {
				customOnError = this.onError;
			}
			var maxRetry = 6;
			var retry = 1;
			var timeout = 500;
			var totalDelay = 0;
			var onCompletion = null;
			onError = function(message, code) {
				if (code >= 0 && (code < 200 || code >= 500)) {
					var useWaitPaneForRetry = true;
					
					if (retry > maxRetry) {
						if (onCompletion == null) {
							source.topWindow.services.ajax.showNetworkError();
							onCompletion = function() {
								source.topWindow.services.ajax.hideNetworkError();
							}
						}
						source.topWindow.services.ajax.setNetworkErrorRetry(function() {
							source.topWindow.services.ajax.hideNetworkError();
							maxRetry += 6;
							totalDelay = 0;
							timeout = 500;
							onCompletion = null;
							self._sendRequest(source, request, useWaitPaneForRetry, onCompletion, onError, ignoreResponse, sequential, retryInfo);
						});
					} else {
						var retryInfo = retry;
						totalDelay += timeout;
						if (totalDelay > 2000 && onCompletion == null) {
							// Enhanced visual feedback.
							source.topWindow.services.ajax.showNetworkError();
							onCompletion = function() {
								source.topWindow.services.ajax.hideNetworkError();
							}
						}
						
						// Use wait pane to lock the UI during retry.
						window.setTimeout(function() {
							self._sendRequest(source, request, useWaitPaneForRetry, onCompletion, onError, ignoreResponse, sequential, retryInfo);
						}, timeout);
						var increasedTimeout = timeout * 2;
						if (increasedTimeout < 5000) {
							timeout = increasedTimeout;
						}
						retry++;
					}
				} else {
					if (customOnError != null) {
						customOnError(message, code);
					}
				}
			};
		}
		
		this._sendRequest(source, request, useWaitPane, null, onError, ignoreResponse, sequential);
	};
	
	/* Same as in com.top_logic.layout.internal.WindowHandler.writeEncodeFunction(TagWriter) */
	this.encodeWindowName = function(s) {
		var regexp = /[-%'\.]/g;
		return encodeURIComponent(s).replace(regexp, '_');
	};
	
	this._createRequest = function(source, target, txSequence, submitNumber, command, args) {
		var request = 
			'<?xml version="1.0" encoding="UTF-8"?>' + this.NEWLINE +
			'<env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">' + 
				'<env:Header>';
					
		if (txSequence >= 0) {
			request +=
					'<sequence xmlns="http://top-logic.com/base/services/cs" ' + 
						'env:role="http://top-logic.com/sequence"' + 
						' tx="' + txSequence + '"'+ 
					'/>';
			
			var acknowledgedRequests = this.mainLayout.services.ajax.acknowledgedRequests;
			var cnt = acknowledgedRequests.length;
			for (var n = 0; n < cnt; n++) {
				var ack = acknowledgedRequests[n];
				request +=
					'<ack xmlns="http://top-logic.com/base/services/cs" ' + 
						'env:role="http://top-logic.com/ack"' + 
						' tx="' + ack + '"'+ 
					'/>';
			}
			acknowledgedRequests.splice(0, cnt);
		}
		request +=
					'<source xmlns="http://top-logic.com/base/services/cs" window="' + this.encodeHTML(this.encodeWindowName(services.ajax.WINDOW_NAME)) + '">' + source + '</source>' + 
				'</env:Header>' + 
				
				'<env:Body>';
		if (txSequence >= 0) {
			/*
			 * If the event is sent without sequence number, then a problem may
			 * occur when the lazy events need to be send in correct order. E.g.
			 * updateScrollposition is sent with log message. At the "same"
			 * moment the tab is changed which is executed earlier. The
			 * updateScrollposition fails.
			 */
			var requests = this.lazyRequests.values();
			if (requests != null) {
				for (var index = 0 ; index < requests.length; index++) {
					var lazyRequest = requests[index];
					var lazyComponentId = lazyRequest.componentId;
					var lazySubmitNumber = lazyRequest.submitNumber;
					var lazyCommand = lazyRequest.command;
					var lazyArgs = lazyRequest.args;
					request += this._getCommandString(lazyComponentId, lazyComponentId, lazySubmitNumber, lazyCommand, lazyArgs); 
				}
				this.lazyRequests.clear();
			}
		}
		request += this._getCommandString(source, target, submitNumber, command, args); 
		request += '</env:Body>' +
		    '</env:Envelope>';

		    // DEBUG: Display request message.
            //
            // alert("AJAX-REQUEST:\n" + request);
		return request;
	};
	
	this._sendRequest = function(source, request, useWaitPane, onCompletion, onError, ignoreResponse, sequential, retry) {
		try {
			if (onCompletion == null) {
				onCompletion = this.onCompletionClosure;
			} else {
				onCompletion = this.concatFunction(onCompletion, this.onCompletionClosure);
			}
			if (useWaitPane == null || useWaitPane == true){
				source.topWindow.services.ajax.showWaitPane();
				onCompletion = this.concatFunction(
					function() {
						source.topWindow.services.ajax.hideWaitPane();
					}, 
					onCompletion
				);
			} else if (!ignoreResponse) {
				var waitPaneVisible = false;
				var delayedWaitPane = source.topWindow.setTimeout(function() {
					waitPaneVisible = true;
					source.topWindow.services.ajax.showWaitPane();
				}, services.ajax.WAIT_PANE_DELAY);
				onCompletion = this.concatFunction(
					function() {
						if (waitPaneVisible) {
							source.topWindow.services.ajax.hideWaitPane();
						} else {
							source.topWindow.clearTimeout(delayedWaitPane);
						}
					}, 
					onCompletion
				);
				
			}
			this.sendRequest(request, onCompletion, onError, ignoreResponse, sequential, retry);
		} catch(ex) {
			alert(ex);
		}
	};
	
	this.concatFunction = function(f1, f2) {
		return function() {
			f1.apply(self, arguments);
			f2.apply(self, arguments);
		};
	};
	
	/**
	 * This method is called by a script in the top window if a new IFrame is
	 * written. It shows a DIV during loading time using a 1000 ms timeout. When
	 * the loading process has finished, the DIV will be removed using
	 * #hideLoading(windowElement).
	 * 
	 * @param frameID
	 *            the ID of the frame currently loading. The shown div must have
	 *            id frameID + "loading".
	 * @see IFrameLayout
	 * @see hideLoading(windowElement)
	 */
	this.showLoading = function(frameID) {
		try {
			var frame = document.getElementById(frameID);
			if (frame== null) {
				throw new Error("No element with id '" + frameID + "'");
			}
			var outer = this;
			var showArea = function() {
				frame.loadingTimeout = null;
				
				var loadingArea = outer.getLoadingArea(frame);
				if (loadingArea != null) {
					// Actually show the loading div by "removing" the none display style.
					loadingArea.style.display = "";
				}
			};
			frame.loadingTimeout = window.setTimeout(showArea, 1000);
		} catch (e) {
			throw new Error("Unable to show loading area for frame '" + frameID + "': error " + e.message);
		}
	};

	/**
	 * Check whether the given Iframe is currently loading, i.e. if this method
	 * returns true the content window must not be touched
	 */
	this.isLoading = function(parentDocument, iframe) {
		if (iframe.id == null) {
			return false;
		}
		if (iframe.loadingTimeout != null) {
			return true;
		}
		var loadingArea = this.getLoadingArea(iframe);
		if (loadingArea == null) {
			return false;
		}
		
		return true;
	};
	
	/**
	 * This method is called when an iframe has been loaded. The given
	 * <code>windowElement</code> is the window corresponding to the iframe.
	 * The method removes the corresponding waiting DIV (if exists).
	 * 
	 * @param windowElement
	 *            the window of the iframe whose waiting div shall be removed
	 * @see showLoading(frameID)
	 */
	this.hideLoading = function(windowElement) {
		try {
            var iFrameHolder = BAL.searchIFrame(document, windowElement);
            if (iFrameHolder.index == -1) {
            	return
            }
            var resultIFrame = iFrameHolder.iFrames[iFrameHolder.index];
			if (resultIFrame.loadingTimeout != null) {
				window.clearTimeout(resultIFrame.loadingTimeout);
				resultIFrame.loadingTimeout = null;
			}
			var loadingArea = this.getLoadingArea(resultIFrame);
			if (loadingArea != null) {
				loadingArea.parentNode.removeChild(loadingArea);
				BAL.DOM.removeNonStandardAttribute(resultIFrame, "data-loadingid")
			}
		} catch(e) {
			throw new Error("Unable to hide waiting for window '" + windowElement.name + "': error " + e.message);
		}
	};
	
	this.getLoadingArea = function(frame) {
		var loadingId = BAL.DOM.getNonStandardAttribute(frame, "data-loadingid");
		if (loadingId == null) {
			return null;
		}
		return document.getElementById(loadingId);
	};
	
	/**
	 * Redirects the main layout to the given page.
	 * 
	 * @param page
	 *            the page to redirect. page must be a path relative to the
	 *            context path of the application.
	 */
	this.logout = function(page) {
		services.ajax.mainLayout.document.body.onunload = function() {};
		services.ajax.mainLayout.document.location = WebService.prototype.CONTEXT_PATH + page;
	};	
	
	this.executeAfterRendering = function(winObj, funcDef) {
		if (services.ajax.mainLayout.queuedFunctions == null) {
			services.ajax.mainLayout.queuedFunctions = new Array();
		}
		services.ajax.mainLayout.queuedFunctions.push(winObj);
		services.ajax.mainLayout.queuedFunctions.push(funcDef);
	};
	
	this.afterRendering = function(errors) {
		if (services.ajax.mainLayout.queuedFunctions != null) {
			for (var i = 0; i < services.ajax.mainLayout.queuedFunctions.length; i = i + 2) {
				var windowRef = services.ajax.mainLayout.queuedFunctions[i];
				var script = services.ajax.mainLayout.queuedFunctions[i + 1];
				try {
					script.call(windowRef);
				} catch (ex) {
					errors = this.addError(errors, "Script execution of '" + script + "' failed: ", ex);
				}
			}
		}
		services.ajax.mainLayout.services.ajax.cleanupQueuedFunctions();
	};
	
	this.cleanupQueuedFunctions = function() {
		queuedFunctions = null;
	};
	
	
	this._nextID = new Date().getTime();
	/*
	 * generates an id for that document based on the current time: the id is
	 * 'd' + new Date().getTime().
	 */
	this.nextID = function() {
		return 'd' + this._nextID++;
	};
	
};
	
services.AJAXServiceConstructor = function() {

	/**
	 * Temporary storage for the windw's status bar contents.
	 * 
	 * The status bar contents is replaced in the showWaitPane() function and
	 * reset in the hideWaitPane() function.
	 */
	this.statusBefore = "";

	/**
	 * The sequence number that is assigend to the next AJAX request that is
	 * sent.
	 * 
	 * This variable only exists in the MainLayout window. All application
	 * frames create new sequence numbers from this single source. This variable
	 * is initialized from a script in the MainLayout's component header.
	 * 
	 * txSequence = 0;
	 */

	/**
	 * Already received replies by sequence number.
	 * 
	 * Only used in the mainLayout instance.
	 */
	this.pendingReplies = new Object();

	/**
	 * An array of all requests, that are not yet answered by the server or
	 * whose replies have not been executed, because there are earlier requests
	 * that have not yet been answered.
	 * 
	 * Only used in the mainLayout instance.
	 */	
	this.pendingRequests = new Array();
	
	/**
	 * Array of request numbers of requests whose responses have been successfully received.
	 */
	this.acknowledgedRequests = new Array();
	
	/**
	 * Only used in the mainLayout instance.
	 */
	this.timer = null;

	var outer = this;
	this.processLaterClosure = function() {
		outer.processLater();
	};
	
	this.onCompletionClosure = function(result) {
		outer.onCompletion(result);
	};
};

services.AJAXServiceClass.prototype = new WebService("/servlet/AJAXServlet");
services.AJAXServiceConstructor.prototype = new services.AJAXServiceClass();
services.ajax = new services.AJAXServiceConstructor();


function XMLValueDecoder() {
	this.readObjects = new Array();
}

function XMLValueDecoderClass() {
	this.XML_VALUE_NS = "http://top-logic.com/ns/xml-value";
	
	this.IS_STRING_ELEMENT = new BAL.QName(this.XML_VALUE_NS, "string").getPredicate();
	this.IS_BOOLEAN_ELEMENT = new BAL.QName(this.XML_VALUE_NS, "boolean").getPredicate();
	this.IS_INT_ELEMENT = new BAL.QName(this.XML_VALUE_NS, "int").getPredicate();
	this.IS_FLOAT_ELEMENT = new BAL.QName(this.XML_VALUE_NS, "float").getPredicate();
	this.IS_OBJECT_ELEMENT = new BAL.QName(this.XML_VALUE_NS, "object").getPredicate();
	this.IS_ARRAY_ELEMENT = new BAL.QName(this.XML_VALUE_NS, "array").getPredicate();
	this.IS_NULL_ELEMENT = new BAL.QName(this.XML_VALUE_NS, "null").getPredicate();
	this.IS_REF_ELEMENT = new BAL.QName(this.XML_VALUE_NS, "ref").getPredicate();
	
	this.decode = function(node) {
		if (this.IS_STRING_ELEMENT(node)) {
			return BAL.DOM.getTextContent(node);
		}
		else if (this.IS_BOOLEAN_ELEMENT(node)) {
			return BAL.DOM.getTextContent(node) == "true";
		}
		else if (this.IS_NULL_ELEMENT(node)) {
			return null;
		}
		else if (this.IS_ARRAY_ELEMENT(node)) {
			var result = new Array();
			
			for (var child = BAL.DOM.getFirstChild(node); child != null; child = BAL.DOM.getNextSibling(child)) {
				if (! BAL.DOM.isElementNode(child)) continue;
				result.push(this.decode(child));
			}
			
			return result;			
		}
		else if (this.IS_OBJECT_ELEMENT(node)) {
			var result = new Object();
				
			for (var propertyNode = BAL.DOM.getFirstElementChild(node); propertyNode != null; propertyNode = BAL.DOM.getNextElementSibling(propertyNode)) {
				var nameNode = BAL.DOM.getFirstElementChild(propertyNode);
				var valueNode = BAL.DOM.getNextElementSibling(nameNode);

				result[BAL.DOM.getTextContent(nameNode)] = this.decode(valueNode);
			}
				
			return result;			
		}
			
		throw new Error("TODO: Not yet supported. Implement other choices. ");
	};
};

XMLValueDecoder.prototype = new XMLValueDecoderClass();



function XMLValueEncoder() {
}

function XMLValueEncoderClass() {
	this.encodeText = function(s) {
		s = s.replace(/\&/g, '&amp;');
		s = s.replace(/\</g, '&lt;');
		s = s.replace(/\>/g, '&gt;');

		// Filter out characters that cannot be encoded in XML.
		s = s.replace(/[^\u0009,\u000A,\u000D,\u0020-\uFFFF]/g, '');
		return s;
	};

	this.encode = function(value) {
		if (value == null) {
			return '<null xmlns="http://top-logic.com/ns/xml-value"/>';
		}
		else if ((typeof value) == "string") {
			return '<string xmlns="http://top-logic.com/ns/xml-value">' + this.encodeText(value) + '</string>';
		}
		else if ((typeof value) == "boolean") {
			return '<boolean xmlns="http://top-logic.com/ns/xml-value">' + String(value) + '</boolean>';
		}
		else if ((typeof value) == "number") {
			// TODO KHA/KBU value > 0x80000000 && value < 0x7fffffff // Check
			// for Integer range
			if (Math.floor(value) == value) // Check for numbers like 3.1425927
											// ...
			{
				return '<int xmlns="http://top-logic.com/ns/xml-value">' + String(value) + '</int>';
			} else {
				return '<float xmlns="http://top-logic.com/ns/xml-value">' + String(value) + '</float>';
			}
		}
		else if ((typeof value) == "object") {
			// The following line does not work for unknown reasons:
			// 
			// if (Array.isPrototypeOf(value)) {
			// if (value instanceof Array) {
			// if (value instanceof Array.prototype) {
			//
			// Use a fragile workaround instead:
			if ("length" in value) {
				var result = '<array xmlns="http://top-logic.com/ns/xml-value">';
				for (var n = 0; n < value.length; n++) {
					result += this.encode(value[n]);
				}
				result += '</array>';
				return result;
			}
			else {
				var result = '<object xmlns="http://top-logic.com/ns/xml-value">';
				for (var name in value) {
					result += '<property>';
					result += '<name>' + name + '</name>';
					result += this.encode(value[name]);
					result += '</property>';
				}
				result += '</object>';
				return result;
			}
		}
		// TODO: Missing branches for Number, missing reference handling.
	};
}

XMLValueEncoder.prototype = new XMLValueEncoderClass();


/**
 * Service for logging client-side events.
 */
services.log = {
	ERROR: "ERROR",
	WARN: "WARN",
	INFO: "INFO",
	
	COMPONENT_NAME: "unknown",

	error: function(message, ex) {
		this.transmit(this.ERROR, message, ex);
	},

	warn: function(message, ex) {
		this.transmit(this.WARN, message, ex);
	},
	
	info: function(message, ex) {
		this.transmit(this.INFO, message, ex);
	},


	transmit: function(level, message, ex) {
		var handler = window.onerror;
		
		// Disable the error handler while transmitting the event, because an
		// error during transmission is likely to produce an infinite loop.
		window.onerror = function(ex) { 
			alert("Error during AJAX transmission: " + ex); 
		};
		try {
			BAL.getMainLayoutWindow().services.ajax.invokeStatic('log', {
				level: level, 
				message: message,
				exception: ex,
				source: String(document.location), 
				component: this.COMPONENT_NAME
			});
		} finally {
			window.onerror = handler;
		}
	},
	
	/**
	 * Install a global error handler that reports all uncaught client-side
	 * exceptions back to the server.
	 */
	installGlobalHandler: function() {
		window.onerror = function(ex) {
			services.log.error("Uncaught JavaScript exception", ex);
		};
	}
};

services.FocusedElementStackClass = function() {
	
	this.focusedElementPathProperty = "focusedElementPath";
	
	this.setFocusedElementOfVisibleLayer = function(rootDocument, focusedElementPathOnLayer) {
		var visibleLayer = KEYBOARD_NAVIGATION.getTopmostVisibleLayerAnchor(BAL.getTopLevelDocument());
		
		if(this.layerHasNoFocusContainer(visibleLayer)) {
			this.createFocusContainer(visibleLayer);
		}
		
		var stackEntry = new Object();
		stackEntry.rootDocument = rootDocument;
		stackEntry.elementPath = focusedElementPathOnLayer;
		visibleLayer[this.focusedElementPathProperty] = stackEntry;
	};
	
	this.layerHasNoFocusContainer = function(layer) {
		return layer[this.focusedElementPathProperty] == null;
	};
	
	this.createFocusContainer = function (layer) {
		var dummyEntry = new Object();
		dummyEntry.rootDocument = null;
		dummyEntry.elementPath = null;
		layer[this.focusedElementPathProperty] = dummyEntry;
	};
	
	this.resetFocusContainer = function () {
		var visibleLayer = KEYBOARD_NAVIGATION.getTopmostVisibleLayerAnchor(BAL.getTopLevelDocument());
		this.createFocusContainer(visibleLayer);
	};
	
	this.hasNewlyOpenedLayer = function() {
		var visibleLayer = KEYBOARD_NAVIGATION.getTopmostVisibleLayerAnchor(BAL.getTopLevelDocument());
		return this.layerHasNoFocusContainer(visibleLayer);
	};
	
	this.getFocusedElementOfVisibleLayer = function() {
		return this.getElementPath();
	};
	
	this.getElementPath = function() {
		var visibleLayer = KEYBOARD_NAVIGATION.getTopmostVisibleLayerAnchor(BAL.getTopLevelDocument());
		var focusedElementPath = visibleLayer[this.focusedElementPathProperty];
		
		// For each retrieval a read reset must be done to the path object. Otherwise
		// multiple read attempt will fail.
		if(focusedElementPath.elementPath != null) {
			focusedElementPath.elementPath.resetPathRead();
		}
		return focusedElementPath;
	};
};

services.FocusRestoreClass = function() {
	
	this.currentActiveElement = null;
	this.currentTopLevelDocument = null;
	
	this.saveFocusState = function() {
		var documentOfAction = document;
        if(documentOfAction != null) {
        	this.currentActiveElement = BAL.DOM.getCurrentActiveSimpleElement(documentOfAction);
        	this.currentTopLevelDocument = BAL.getTopLevelDocumentOf(documentOfAction);
        	var focusStack = this.getFocusStack();
        	if (this.currentActiveElement != null) {
        		var currentActiveElementPath = BAL.DOM.getPath(BAL.getTopLevelDocumentOf(documentOfAction), this.currentActiveElement);
        		focusStack.setFocusedElementOfVisibleLayer(this.currentTopLevelDocument, currentActiveElementPath);
        	} else if(!focusStack.hasNewlyOpenedLayer()){
        		focusStack.setFocusedElementOfVisibleLayer(this.currentTopLevelDocument, null);
        	}
        }
        
	};
	
	this.getFocusStack = function() {
		return BAL.getTopLevelWindowOf(this.currentTopLevelDocument).services.focusedElementStack;
	};
	
	this.restoreFocusState = function() {
		var documentOfAction = document;
		if(this.currentTopLevelDocument != null) {
			var focusStack = this.getFocusStack();
			if(focusStack.hasNewlyOpenedLayer()) {
				this.setFocusToFirstFocusableElement(this.currentTopLevelDocument);
			} else {
				var currentActiveElement = focusStack.getFocusedElementOfVisibleLayer();
				if (currentActiveElement.elementPath != null) {
					this.restoreFocusOfElement(documentOfAction, currentActiveElement.elementPath);
				}
			}
		}
		
		this.currentActiveElement = null;
		this.currentTopLevelDocument = null;
	};
	
	this.restoreFocusOfElement = function(documentOfAction, elementPath) {
		var bestMatchingNode = BAL.DOM.getNodeFromPath(this.currentTopLevelDocument, elementPath);
		if(KEYBOARD_NAVIGATION.isBelowVisibleTabroot(bestMatchingNode.domReference)) {
			if(bestMatchingNode.pathMatchedFully && BAL.DOM.canBeActive(bestMatchingNode.domReference)) {
				if(documentOfAction == null || BAL.DOM.getCurrentActiveSimpleElement(documentOfAction) != bestMatchingNode.domReference) {
					BAL.focus(bestMatchingNode.domReference);
				}
			} 
		}
		else {
			this.setFocusToFirstFocusableElement(this.currentTopLevelDocument);
		}
	};
	
	this.setFocusToFirstFocusableElement = function(documentOfAction) {
		try {
			if(this.currentActiveElement != null) {
				this.currentActiveElement.blur();
			}
		} catch(ex) {
			// Exception may occur, if the active element is not part of the DOM
			// anymore.
			// In this case it already has been "blured".
		}
		KEYBOARD_NAVIGATION.focusFirstFocusableElementInVisibleLayer(documentOfAction);
		
		var focusStack = this.getFocusStack();
		var visibleLayer = KEYBOARD_NAVIGATION.getTopmostVisibleLayerAnchor(BAL.getTopLevelDocument());
		
		if(focusStack.layerHasNoFocusContainer(visibleLayer)) {
			focusStack.createFocusContainer(visibleLayer);
		}
	};
};

services.focusedElementStack = new services.FocusedElementStackClass();
services.focusRestore = new services.FocusRestoreClass();

services.NodeSet = function() {
	this.map = new services.LinkedMap();
	
	this.clear = function() {
		this.map.clear();
	};
	
	this.add = function(node) {
		if ((node.id == null) || (node.id == "")) {
			/* Prevent collisions of multiple nodes without ids by enforcing an id. */
			/* This is not cryptographically secure. But that is not necessary here. */
			node.id = Math.random().toString();
		}
		this.map.put(node.id, node);
	};
	
	this.toArray = function() {
		return this.map.values();
	};
	
	this.size = function() {
		return this.map.size();
	};
	
	this.contains = function(node) {
		return this.map.contains(node.id);
	};
	
	this.isEmpty = function() {
		return this.map.size() == 0;
	};
};

services.ScriptRecorderClass = function() {
	this.resumeScriptExecution = function(controlID) {
		services.ajax.execute("dispatchControlCommand", {
			controlCommand : "resumeScriptExecution",
			controlID : controlID
		}, true);
	};
};

services.scriptrecorder = new services.ScriptRecorderClass();