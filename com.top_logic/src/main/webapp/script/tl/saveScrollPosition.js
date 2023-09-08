// saveScrollPosition.js
//
// copied code from LayoutComponent.writeSaveScrollPositionScript(HTMLWriter) and deleted there
// renamed old saveScrollPosition.js to saveScrollPosition_old.js

SaveScrollPosition = {};

SaveScrollPosition.getLeftPosition = function(scrollContainerId) {
	return SaveScrollPosition._getItem(scrollContainerId + '_x');
};

SaveScrollPosition.setLeftPosition = function(scrollContainerId, value) {
	return SaveScrollPosition._setItem(scrollContainerId + '_x', value);
};

SaveScrollPosition.getTopPosition = function(scrollContainerId) {
	return SaveScrollPosition._getItem(scrollContainerId + '_y');
};

SaveScrollPosition.setTopPosition = function(scrollContainerId, value) {
	return SaveScrollPosition._setItem(scrollContainerId + '_y', value);
};

SaveScrollPosition.getCatchScrollEvent = function(scrollContainerId) {
	return SaveScrollPosition._getItem(scrollContainerId + '_catchScrollEvent');
};

SaveScrollPosition.setCatchScrollEvent = function(scrollContainerId, value) {
	return SaveScrollPosition._setItem(scrollContainerId + '_catchScrollEvent', value);
};

SaveScrollPosition._getItem = function(id) {
	return services.ajax.mainLayout.scrollMap[id];
};

SaveScrollPosition._setItem = function(id, value) {
	services.ajax.mainLayout.scrollMap[id] = value;
};

/**
 * Called from <code>onload<code> to restore the saved scroll position.
 */
SaveScrollPosition.positionViewPort = function(scrollContainerId) {
    var scrollContainer = document.getElementById(scrollContainerId);
    var viewportHeight; 
	var documentHeight;
    if (scrollContainer == null) {
    	viewportHeight = BAL.getViewportHeight();
    	documentHeight = document.body.offsetHeight;
    } else {
    	viewportHeight = BAL.getElementHeight(scrollContainer);
    	documentHeight = scrollContainer.scrollHeight;
    }
    
    // alert('documentHeight: ' + documentHeight);

    var xOffset = SaveScrollPosition.getLeftPosition(scrollContainerId);
    var rawYOffset = SaveScrollPosition.getTopPosition(scrollContainerId);
    
    SaveScrollPosition.setCatchScrollEvent(scrollContainerId, true);

    if (typeof(xOffset) == "undefined") {
    	xOffset = 0;
    }
	var yOffset;
    if (typeof(rawYOffset) == "undefined") {
        yOffset = 0;
    } else {
        yOffset = Math.round(rawYOffset * (documentHeight - viewportHeight));
    }

    // var scrollTop = BAL.getScrollTop(window);
    // alert('scrollTop: ' + scrollTop);
    
    /* Table and tree components write an element with ID 'viewFocus' to mark 
     * the element that should be made visible. */
    var focusElement = document.getElementById('viewFocus');
    if (focusElement != null) {
        var elementOffset = focusElement.offsetTop;
        while ((focusElement = focusElement.offsetParent) != null) {
            elementOffset += focusElement.offsetTop;
        } 
        
        /* If the focus element is not within the visible range, adjust the
		 * scroll position to show the focus element. */
        if ((elementOffset > yOffset + viewportHeight) || (elementOffset < yOffset)) {
        	yOffset = elementOffset;
        }
    }
    
    if (scrollContainer == null) {
    	window.scrollTo(xOffset, yOffset);
    } else {
    	BAL.setScrollTopElement(scrollContainer, yOffset);
    	BAL.setScrollLeftElement(scrollContainer, xOffset);
    }
    // alert('scrollTo: ' + xOffset + ', ' + yOffset);
};
    
/**
 * Called from <code>onscroll<code> to remember the current scroll position.
 */
SaveScrollPosition.pushScrollPosition = function(scrollContainerId) {
    // alert('catchScrollEvent: ' + this.catchScrollEvent);
	var catchScrollEvent = SaveScrollPosition.getCatchScrollEvent(scrollContainerId);
    if (catchScrollEvent != null && catchScrollEvent) {
    	var scrollContainer = document.getElementById(scrollContainerId);
    	
    	var scrollLeft;
    	var scrollTop;
    	var viewportHeight; 
    	var documentHeight;
    	if (scrollContainer == null) {
    		viewportHeight = BAL.getViewportHeight();
    		documentHeight = document.body.offsetHeight;
    		
    		scrollLeft = BAL.getScrollLeft(window);
    		scrollTop = BAL.getScrollTop(window);
    	} else {
    		viewportHeight = BAL.getElementHeight(scrollContainer);
    		documentHeight = scrollContainer.scrollHeight;
    		scrollLeft = BAL.getScrollLeftElement(scrollContainer);
    		scrollTop = BAL.getScrollTopElement(scrollContainer);
    	}
    	
    	// alert('documentHeight: ' + documentHeight + ", scrollTop: " + scrollTop + ", viewportHeight: " + viewportHeight);
    	
    	if (documentHeight - viewportHeight > 0) {
    		SaveScrollPosition.setLeftPosition(scrollContainerId, scrollLeft);
    		SaveScrollPosition.setTopPosition(scrollContainerId, scrollTop / (documentHeight - viewportHeight));
    	}
    	
    	// alert('scrollLeft: ' + scrollMap[this.getLeftPosition()] + ', scrollTop: ' + scrollMap[this.getTopPosition()]);
    }
};

/**
 * Called from <code>onload<code> to explicitly drop the remembered scroll position.
 */
SaveScrollPosition.resetScrollPosition = function(scrollContainerId) {
    SaveScrollPosition.setLeftPosition(scrollContainerId, 0);
    SaveScrollPosition.setTopPosition(scrollContainerId, 0);
    SaveScrollPosition.setCatchScrollEvent(scrollContainerId, true);
};
