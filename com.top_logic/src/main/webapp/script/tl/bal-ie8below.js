/**
 * Browser abstraction layer.
 * 
 * Specific adjustments for older Internet Explorer.
 * 
 * Author: <a href=mailto:bhu@top-logic.com>Stefan Steinert</a>
 */
BAL.extend({

	isLeftMouseButton: function(buttonCode) {
		return buttonCode == 1;
	},
	
	removeDocumentSelection: function() {
		document.selection.empty();
	}
});