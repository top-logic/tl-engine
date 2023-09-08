/**
 * Browser abstraction layer. 
 * 
 * Specific adjustments for Internet Explorer.
 *
 * Author:  <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 */
BAL.extend({

	isIE : function() {
		return true;
	},

	getVerticalScrollBarWidth : function() {
		return 18;
	},

	getHorizontalScrollBarHeight : function() {
		return 18;
	},
	
	getScrollLeft: function(windowElement){
        var left = 0;
        if( windowElement.document.body && windowElement.document.body.scrollLeft ) {
            //DOM compliant
            left = windowElement.document.body.scrollLeft;
          } else if( windowElement.document.documentElement && windowElement.document.documentElement.scrollLeft  ) {
            //IE6 standards compliant mode
            left = windowElement.document.documentElement.scrollLeft;     
        }
        return left;
    },
    
    getScrollTop: function(windowElement){
        var top = 0;
        if( windowElement.document.body && windowElement.document.body.scrollTop  ) {
            //DOM compliant
            top = windowElement.document.body.scrollTop;
          } else if( windowElement.document.documentElement &&  windowElement.document.documentElement.scrollTop  ) {
            //IE6 standards compliant mode
            top = windowElement.document.documentElement.scrollTop;
          }
        return top;
    },
    
    getEvent: function(event) {
		if (event) {
			return event;
		} else {
			return window.event;
		}
	},
	
	getEventTarget: function (event){
		return event.srcElement;	
	},

	eventStopPropagation : function(event) {
		event.cancelBubble = true;
	},
	
	cancelEvent: function(event) {
		event.returnValue = false;
		return false;
	},
    
    getEventX: function(event) {
	    return event.clientX + this.getScrollLeft(window);
	},
	
	getEventY: function(event) {
	    return event.clientY + this.getScrollTop(window);
	},
	
	getEventMouseScrollDelta: function(event) {
		// IE divides wheel movement into angle steps of 120 degree.
		// To convert this to a pixel distance, the detected angle will be used
		// to calculate how many 120 degree steps must be taken, to reach it.
		// Afterwards an elementary distance of 60px (randomely chosen)
		// will be multiplied to the amount of steps.
		return  -(event.wheelDelta / 120) * 60;
	},
	
	addMouseScrollListener: function(element, handlerFunction) {
		BAL.addEventListener(element, "mousewheel", handlerFunction);
	},
	
	removeMouseScrollListener: function(element, handlerFunction) {
		BAL.removeEventListener(element, "mousewheel", handlerFunction);
	},
	
	addEventListener: function(element, eventType, handlerFunction, phase) {
        if (BAL.isSupportedEvent(eventType)) {
            element.attachEvent("on" + eventType, handlerFunction);
        } else {
            var handlers = element[eventType];
            if (handlers == null) {
                handlers = new Array();
                element[eventType] = handlers;
            }
            handlers.push(handlerFunction);
        }
	},
	
	removeEventListener: function(element, eventType, handlerFunction, phase) {
        if (BAL.isSupportedEvent(eventType)) {
            element.detachEvent("on" + eventType, handlerFunction);
        } else {
            var handlers = element[eventType];
            if (handlers != null) {
                arrayRemove(handlers, handlerFunction);
            }
        }
	},
	
	dispatchEvent: function(element, eventType) {
	    var theDoc = element.ownerDocument;
        if (BAL.isSupportedEvent(eventType)) {
            var evt = theDoc.createEventObject();
            element.fireEvent('on' + eventType, evt);
        } else {
            try {
	            var handlers = element[eventType];
	            if (handlers != null) {
		            for (var i = 0; i < handlers.length; i++) {
		               if (handlers[i] != null) {
		                   handlers[i].call();
		               }
	                }
	            }
	        } catch (e) {
                   throw new Error("Unable to dispatch event '" + eventType + "' for element with nodeType '" + element.nodeType + "'.");
	        }
        }   
        
	},
	
	isSupportedEvent: function(eventType) {
	   return !this._NOT_SUPPORTED_EVENTS[eventType];
	},
	
	_NOT_SUPPORTED_EVENTS : {
	   DOMNodeRemovedFromDocument: true,
	   DOMNodeRemoved: true
	},
	
	disableSelection: function(element) {
		element.onselectstart = BAL.CANCEL_FUNCTION;
	},
	
	sendMailTo : function(event, eMailAddress) {
		var mailtoLink = "mailto:" + eMailAddress;
		var mainLayout = services.ajax.mainLayout;

		/*
		 * If the href of the top window is set to mailto address in IE9 and
		 * IE10, then the application is broken. The mailto address is displayed
		 * in the URL and "no such page" is displayed. Setting mailto in inner
		 * iframe works.
		 */
		var iframe = mainLayout.document.createElement("iframe");
		BAL.setStyle(iframe, "display", "none");
		iframe.src = "about:blank";
		mainLayout.document.body.appendChild(iframe);
		iframe.contentWindow.location.href = mailtoLink;
		mainLayout.document.body.removeChild(iframe);
		
		BAL.eventStopPropagation(event);
		return BAL.cancelEvent(event);
	}
});

BAL.DOM.extend({
	
    removeChild: function(parent, child) {
		BAL.dispatchEvent(child, "DOMNodeRemoved");
		BAL.DOM.triggerEachNode(child, "DOMNodeRemovedFromDocument");
		
		/*
		 * deleteIFrames is necessary in IE7 with BEA since replacing a DOM
		 * node by a node with an equal content the IFRAMES will not be
		 * updated. see #1761
		 * 
		 * Moreover replacing an IFRAME node with the same content can
		 * freeze the GUI, e.g. it is impossible to set cursor in input
		 * fields or selecting text using mouse is not possible. see #1821
		 */
		BAL.DOM.deleteIFrames(child);
		
		parent.removeChild(child);
	},
	
	deleteContents: function(range) {
		var reference = BAL.DOM.getNodeAtOffset(range.startContainer, range.startOffset);
		while (range.startOffset < range.endOffset) {
			var deletedNode = reference;
			reference = BAL.DOM.getNextSibling(reference);
			// Using this function also triggers the DOMNodeRemovedFromDocument event
			BAL.DOM.removeChild(range.startContainer, deletedNode);
			range.endOffset--;
		}
		
		range.deleteContents();
	}
});