TL = {

	/**
	 * Returns the type attribute of the given element.
	 * 
	 * The type is typically a JS-Object which contains the functions used for
	 * Drag&Drop.
	 */
	getType : function(element) {
		var type = this.getTLAttribute(element, 'type');
		if (type != null && type != "") {
			return eval(type);
		}
		return null;
	},
		
    /**
     * Returns the value of the attribute with name "'data-' + attrName" of the 
     * given element.
     */
    getTLAttribute : function(element, attrName) {
        return BAL.DOM.getNonStandardAttribute(element, 'data-' + attrName);
    },

    /**
     * Sets the value of the attribute with name "'data-' + attrName" of the 
     * given element to the given value.
     */
    setTLAttribute : function(element, attrName, value) {
        return BAL.DOM.setNonStandardAttribute(element, 'data-' + attrName, value);
    },
    
    /**
     * Removes the attribute with name "'data-' + attrName" of the 
     * given element.
     */
    removeTLAttribute : function(element, attrName) {
    	BAL.DOM.removeNonStandardAttribute(element, 'data-' + attrName);
    },

	_parseJson : function(json) {
		return eval("(" + json + ");");
	},
	
	getTooltipTargetIfRelated : function(target) {
		if (target.classList.contains("activeTooltip")) {
			return target;
		} else {
			let tooltip = target.closest(".tooltip");
			if (tooltip) {
				return tooltip.parentElement.querySelector(".activeTooltip");
			}
			return false;
		}
	},
	
	preventCloseTooltip : function(tooltipTarget) {
		let closeTimeout = tooltipTarget.getAttribute("data-ttClose");
		if (closeTimeout) {
			clearTimeout(closeTimeout);
			tooltipTarget.removeAttribute("data-ttClose");
		}
	}
};


BAL.addEventListener(document, "mousedown", function(evt) {
	evt = BAL.getEvent(evt);
	var target = BAL.getEventTarget(evt);

	while (target != null) {
		if (target.parentNode == null) {
			// The document node does not support attributes.
			break;
		}
		
		{
			var attributeValue = BAL.DOM.getNonStandardAttribute(target, 'data-dnd');
			if (attributeValue != null && attributeValue.length > 0) {
				if (target.id == null || target.id == "") {
					services.log.error("Cannot initialize drag and drop for element whithout ID.");
					return true;
				}
				
				var targetIds = TL._parseJson(attributeValue);
				return services.DnD.handleMouseDown(target, targetIds, evt);	
			}
		}

		target = target.parentNode;
	}
});

BAL.addEventListener(document, "mouseover", function(evt) {
	evt = BAL.getEvent(evt);
	
	var tooltip = true;
	var target = BAL.getEventTarget(evt);
	
	while (target != null) {
		if (target.parentNode == null) {
			// The document node does not support attributes.
			break;
		}
		
		let tooltipTarget = TL.getTooltipTargetIfRelated(evt.target);
		if (tooltipTarget) {
			TL.preventCloseTooltip(tooltipTarget);
		} else {
			let outerDocument = document.body.firstElementChild,
				oldTooltip = outerDocument.querySelector(":scope > .tooltip"),
				activeTarget = outerDocument.querySelector(".activeTooltip");
			if (oldTooltip && !activeTarget) {
				oldTooltip.remove();
			}
		}
		
		if (tooltip) {
			var attributeValue = BAL.DOM.getNonStandardAttribute(target, 'data-tooltip');
			if (attributeValue != null && attributeValue.length > 0) {

				if (!target.classList.contains("activeTooltip")) {
					PlaceDialog.generateTooltip(target, attributeValue);
				}

				// Only interpret innermost tooltip.
				tooltip = false;
			}
		}
		
		target = target.parentNode;
	}
});

BAL.addEventListener(document, "mouseout", function(evt) {
	evt = BAL.getEvent(evt);
	
	var target = BAL.getEventTarget(evt);
	while (target != null) {
		if (target.parentNode == null) {
			// The document node does not support attributes.
			break;
		}
		
		let openTimeout = target.getAttribute("data-ttOpen");
		if (openTimeout) {
			clearTimeout(openTimeout);
			target.removeAttribute("data-ttOpen");
		}

		target = target.parentNode;
	}
});
