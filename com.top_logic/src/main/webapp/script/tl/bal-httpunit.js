/**
 * Browser abstraction layer. 
 * 
 * Specific adjustments for HttpUnit clients.
 *
 * Author:  <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 */
BAL.extend({
    
	getBodyElement: function(doc) {
		return document.getElementsByTagName("body")[0];
	},

    addEventListener: function(element, eventType, handlerFunction, phase) {
    	// Not supported.
	},
	
  	getComputedStyle: function(element, property) {
		// No styles available.
		return null;
	}

});

BAL.DOM.extend({
	
    hasAttributes: function(element) {
		if (element == null) {
			return false;
		}
		return element.nodeType == 1 /*Node.ELEMENT_NODE*/ && element.attributes.length > 0;
	},

	// Node object not defined.
	isElementNode: function(node) {
		return node.nodeType == 1 /*Node.ELEMENT_NODE*/;
	},
	
	// Node object not defined.
	isAttributeNode: function(node) {
		node.nodeType == 2 /*Node.ATTRIBUTE_NODE*/;
	}
	
});
