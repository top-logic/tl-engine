/**
 * Browser abstraction layer. 
 * 
 * Specific adjustments for Firefox browsers.
 *
 * Author:  <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 */
BAL.extend({
    
    disableSelection: function(element) {
		element.style.MozUserSelect = "none";
	},
	
	getKeyCode: function(event) {
		var charCode = event.charCode;
        if (charCode > 0) {
        	// Try to map back from char codes to key codes.
        	if (charCode >= 97) {
        		return charCode - 97 + 65;
        	} else {
        		return charCode;
        	}
        } else {
            return event.keyCode;
        }
    },
    
    addEventListener: function(element, eventType, handlerFunction, phase) {
        if (BAL.isSupportedEvent(eventType)) {
            element.addEventListener(eventType, handlerFunction, phase);
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
            element.removeEventListener(eventType, handlerFunction, phase);
        } else {
            var handlers = element[eventType];
            if (handlers != null) {
                arrayRemove(handlers, handlerFunction);
            }
        }
    },
    
    dispatchEvent: function(element, eventType) {
        if (BAL.isSupportedEvent(eventType)) {
	       var theDoc = element.ownerDocument;
	       var evt = theDoc.createEvent("Events");   
	       evt.initEvent(eventType, true, true);
	       element.dispatchEvent(evt);   
        } else {
            try {
                var handlers = element[eventType];
            } catch (e) {
            	return;
            }
            if (handlers != null) {
                for (var i = 0; i < handlers.length; i++) {
                   if (handlers[i] != null) {
                       handlers[i].call();
                   }
                }
            }
        }   
        
    },
    
    isSupportedEvent: function(eventType) {
        return eventType != "DOMNodeRemovedFromDocument";
    }
});

BAL.DOM.extend({
    
    deleteContents: function(range) {
        var reference = BAL.DOM.getNodeAtOffset(range.startContainer, range.startOffset);
        var tmpStopIndex = range.endOffset;
        while (range.startOffset < tmpStopIndex) {
            BAL.DOM.triggerEachNode(reference, "DOMNodeRemovedFromDocument");
            reference = BAL.DOM.getNextSibling(reference);
            tmpStopIndex--;
        }
        range.deleteContents();
    }
});