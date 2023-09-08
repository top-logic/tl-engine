/**
 * Browser abstraction layer.
 * 
 * Specific adjustments for Internet Explorer.
 * 
 * Author: <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 */
BAL.extend({

	replaceSelects : function(parentToIgnore) {
		var outermostWindow = services.ajax.mainLayout;
		var replacedSelects = new Array();
		this._addSelect(outermostWindow, replacedSelects, parentToIgnore);
		return replacedSelects;
	},

	_addSelect : function(theWindow, replacedSelects, parentToIgnore) {
		var iFrames = theWindow.document.getElementsByTagName('IFRAME');
		var frames = theWindow.document.getElementsByTagName('FRAME');
		for (var i = 0; i < frames.length; i++) {
			this._addSelect(frames[i].contentWindow, replacedSelects,
					parentToIgnore);
		}
		for (var i = 0; i < iFrames.length; i++) {
			this._addSelect(iFrames[i].contentWindow, replacedSelects,
					parentToIgnore);
		}
		var currentSelects = theWindow.document.getElementsByTagName('SELECT');
		var newSpan;
		for (var i = currentSelects.length - 1; i >= 0; i--) {
			if (this._checkIgnoredSelect(currentSelects[i], parentToIgnore)) {
				newSpan = theWindow.document.createElement('span');
				var selectedIndex = currentSelects[i].selectedIndex;
				if (selectedIndex >= 0 && selectedIndex < currentSelects[i].options.length) {
					newSpan.innerHTML = currentSelects[i].options[selectedIndex].innerHTML;
				}
				newSpan.itsSelect = currentSelects[i];
				replacedSelects.push(newSpan);
				currentSelects[i].parentNode.insertBefore(newSpan, currentSelects[i]);
				currentSelects[i].parentNode.removeChild(currentSelects[i]);
			}
		}
	},

	_checkIgnoredSelect : function(currentSelect, parentToIgnore) {
		var notIgnored = true;
		var currentElement = currentSelect;

		while ((notIgnored == true) && currentElement.parentNode) {
			currentElement = currentElement.parentNode;
			if (currentElement === parentToIgnore) {
				notIgnored = false;
			}
		}

		return notIgnored;
	},

	restoreSelects : function(replacedSelects) {
		if (replacedSelects == undefined || replacedSelects == null) {
			return;
		}
		for (var i = 0; i < replacedSelects.length; i++) {
			var span = replacedSelects[i];
			try {
				span.parentNode.insertBefore(span.itsSelect, span);
				span.parentNode.removeChild(span);
			} catch (e) {
				// the span is from a page which is not
				// longer visible, so it is nothing to do
			}
		}
	}

});