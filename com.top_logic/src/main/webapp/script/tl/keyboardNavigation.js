/**
 * Collection of keyboard navigation related functions and classes 
 */

/**
 * Static functions
 */
KEYBOARD_NAVIGATION = {
		
	indentCounter: 0,
	
	processingLog: "",
	
	doLog: false,
	
	logIndentStep: " ",
	
	log: function(message) {
		KEYBOARD_NAVIGATION.processingLog += message;
	},

	clearLog: function() {
		KEYBOARD_NAVIGATION.processingLog = "";
		KEYBOARD_NAVIGATION.indentCounter = 0;
	},
	
	submitLog: function() {
		services.log.info(KEYBOARD_NAVIGATION.processingLog);
	},
	
	/**
	 * Filter, to determine, whether an element is a complex widget, or not
	 * 
	 * @param element - the current node
	 */
	complexWidgetFilter: function(element) {
		if(!BAL.DOM.hasAttributes(element)) {
			return false;
		}
		
		var widgetMarker = BAL.DOM.getNonStandardAttribute(element, "data-complexwidget");
		var widgetMarkerFound = (widgetMarker != null) && (widgetMarker == "true");
		
		// In case of logging
		if(KEYBOARD_NAVIGATION.doLog) {
			
			// Use tabbable filter for tag logging
			BAL.DOM.simpleFocusableElementFilter(element, KEYBOARD_NAVIGATION.log);
			
			if(widgetMarkerFound) {
				
				// Write indents
				for(var i = 0; i < KEYBOARD_NAVIGATION.indentCounter; i++) {
					KEYBOARD_NAVIGATION.log(KEYBOARD_NAVIGATION.logIndentStep); 
				}
				
				KEYBOARD_NAVIGATION.log("--- Begin of complex widget ---\n");
			}
		}
		
		return widgetMarkerFound;
	},
	
	/**
	 * Filter, to determine, whether an element is a root of a tab-cycle
	 * 
	 * @param element - the current node
	 */
	tabCycleRootFilter: function(element) {
		if(!BAL.DOM.hasAttributes(element)) {
			return false;
		}
		
		var rootMarker = BAL.DOM.getNonStandardAttribute(element, "data-tabroot");
		var tabRootFound = (rootMarker != null) && (rootMarker == "true");
		
		if(KEYBOARD_NAVIGATION.doLog && tabRootFound) {
			// Write indents
			for(var i = 0; i < KEYBOARD_NAVIGATION.indentCounter; i++) {
				KEYBOARD_NAVIGATION.log(KEYBOARD_NAVIGATION.logIndentStep); 
			}
			
			KEYBOARD_NAVIGATION.log("*** Tab root found ***\n");
		}
		
		return tabRootFound;
	},
	
	/**
	 * Filter, to determine, whether an element is a external frame, or not
	 * 
	 * @param element - the current node
	 */
	externalFrameFilter: function(element) {
		if(!BAL.DOM.hasAttributes(element)) {
			return false;
		}
		
		var frameMarker = BAL.DOM.getNonStandardAttribute(element, "data-externalframe");
		var frameMarkerFound = (frameMarker != null) && (frameMarker == "true");
		
		// In case of logging
		if(KEYBOARD_NAVIGATION.doLog) {
			
			// Use tabbable filter for tag logging
			BAL.DOM.simpleFocusableElementFilter(element, KEYBOARD_NAVIGATION.log);
			
			if(frameMarkerFound) {
				
				// Write indents
				for(var i = 0; i < KEYBOARD_NAVIGATION.indentCounter; i++) {
					KEYBOARD_NAVIGATION.log(KEYBOARD_NAVIGATION.logIndentStep); 
				}
				
				KEYBOARD_NAVIGATION.log("--- External frame found ---\n");
			}
		}
		
		return frameMarkerFound;
	},
	
	/**
	 * Handler, if a key was pressed
	 * 
	 * @param event - the event, which occurred.
	 */
	keyDownHandler: function(event) {
		event = BAL.getEvent(event);
		var keycode = BAL.getKeyCode(event);
		
		// Tab (simple widget) switch
		if (BAL.isTabKey(keycode)) {
			var activeElement = BAL.DOM.getCurrentActiveSimpleElement();
			
			var rootElement = KEYBOARD_NAVIGATION.getRootElement(activeElement,
																 KEYBOARD_NAVIGATION.tabCycleRootFilter);
			
			// If an element below an root element is focused, focus following element
			if(rootElement != null) {
				
				// Forward tabbing
				if(!BAL.hasShiftModifier(event)) {
					KEYBOARD_NAVIGATION.TABMANAGER_INSTANCE.switchToNextSimpleWidget(activeElement);
				}
				
				// Backward tabbing
				else {
					KEYBOARD_NAVIGATION.TABMANAGER_INSTANCE.switchToPreviousSimpleWidget(activeElement);
				}
			}
			
			else {
				KEYBOARD_NAVIGATION.focusFirstFocusableElementInVisibleLayer();
			}
			
			return BAL.cancelEvent(event);
		}
		
		// Complex widget switch
		// 33 = PageUp, 34 = PageDown
		else if (((keycode == 33) || (keycode == 34))&& BAL.hasCtrlModifier(event)) {
			
			var firstTimeSnapshot = null;
			if(KEYBOARD_NAVIGATION.doLog) {
				firstTimeSnapshot = new Date();
			}
			
			var activeElement = BAL.DOM.getCurrentActiveSimpleElement();
			
			var rootElement = KEYBOARD_NAVIGATION.getRootElement(activeElement,
																 KEYBOARD_NAVIGATION.tabCycleRootFilter);
			
			// If an element below an root element is focused, focus following element
			if(rootElement != null) {
				
				// Forward tabbing
				if(keycode == 34) {
					KEYBOARD_NAVIGATION.TABMANAGER_INSTANCE.switchToNextComplexWidget(activeElement);
				}
				
				// Backward tabbing
				else {
					KEYBOARD_NAVIGATION.TABMANAGER_INSTANCE.switchToPreviousComplexWidget(activeElement);
				}
			}
			
			// If no element is focused, try to find first focusable element below layout
			// root nodes in following order:
			// PopupDialogs, dialogs, application window
			else {
				KEYBOARD_NAVIGATION.focusFirstFocusableElementInVisibleLayer();
			}
			
			if(KEYBOARD_NAVIGATION.doLog) {
				var secondTimeSnapshot = new Date();
				var executionTime = secondTimeSnapshot.getTime() - firstTimeSnapshot.getTime();
				KEYBOARD_NAVIGATION.log("Overall focus switch time: " + executionTime + "ms\n");
				KEYBOARD_NAVIGATION.submitLog();
				KEYBOARD_NAVIGATION.clearLog();
			}
			
			return BAL.cancelEvent(event);
		}
		
		return true;
	},
	
	/**
	 * Performs a focus, if no element currently owns a focus. Therefore this function
	 * tries to focus an element in the top-most visible window. First it will check for any
	 * open popup dialogs. If there are no open popup dialogs, it will try to find a focusable
	 * element in any open dialog. If there are also no open dialogs, it will focus the first
	 * focusable element in the application window.
	 */
	focusFirstFocusableElementInVisibleLayer: function (documentContext) {
		
		var topLevelDocument = KEYBOARD_NAVIGATION.getTopLevelDocument(documentContext);
		
		if(KEYBOARD_NAVIGATION.isAnyPopupDialogOpen(topLevelDocument)) {
			KEYBOARD_NAVIGATION.focusElementInTopmostPopupDialog(topLevelDocument);
		}
		
		else if(KEYBOARD_NAVIGATION.isAnyDialogOpen(topLevelDocument)) {
			KEYBOARD_NAVIGATION.focusElementInTopmostDialog(topLevelDocument);
		}
		
		else {
			KEYBOARD_NAVIGATION.focusFirstElementInApplicationWindow(topLevelDocument);
		}
	},
	
	getTopLevelDocument: function (documentContext) {
		
		if(documentContext == undefined || documentContext == null) {
			return BAL.getTopLevelDocument();
		} else {
			return BAL.getTopLevelDocumentOf(documentContext);
		}
	},
	
	isAnyPopupDialogOpen: function(topLevelDocument) {
		var popupDialogsAnchor = KEYBOARD_NAVIGATION.getPopupDialogsAnchor(topLevelDocument);
		return !KEYBOARD_NAVIGATION.hasNoOrTextNodeChildsOnly(popupDialogsAnchor);
	},
	
	getPopupDialogsAnchor: function(topLevelDocument) {
		return topLevelDocument.getElementById("pdlgPopupDialogs");
	},
	
	focusElementInTopmostPopupDialog: function(topLevelDocument) {
		var topmostPopupDialog = KEYBOARD_NAVIGATION.getTopmostPopupDialogContent(topLevelDocument);
		KEYBOARD_NAVIGATION.focusFirstFocusableElementBelow(topmostPopupDialog);
	},
	
	getTopmostPopupDialogContent: function(topLevelDocument) {
		var popupDialog = BAL.DOM.getLastElementChild(KEYBOARD_NAVIGATION.getPopupDialogsAnchor(topLevelDocument));
		var popupDialogId = BAL.DOM.getAttribute(popupDialog, "id");
		return topLevelDocument.getElementById(popupDialogId + "-content");
	},
	
	isAnyDialogOpen: function(topLevelDocument) {
		var dialogsAnchor = KEYBOARD_NAVIGATION.getDialogsAnchor(topLevelDocument);
		return !KEYBOARD_NAVIGATION.hasNoOrTextNodeChildsOnly(dialogsAnchor);
	},
	
	getDialogsAnchor: function(topLevelDocument) {
		return topLevelDocument.getElementById("dlgDialogs");
	},
	
	focusElementInTopmostDialog: function(topLevelDocument) {
		// The standard use case is to focus an element in the content of the
		// dialog and not an element in the title bar.
		var topmostDialog = KEYBOARD_NAVIGATION.getTopmostDialogContent(topLevelDocument);
		KEYBOARD_NAVIGATION.focusFirstFocusableElementBelow(topmostDialog);
	},
	
	getTopmostDialogContent: function(topLevelDocument) {
		var dialog = BAL.DOM.getLastElementChild(KEYBOARD_NAVIGATION.getDialogsAnchor(topLevelDocument));
		var dialogId = BAL.DOM.getAttribute(dialog, "id");
		return topLevelDocument.getElementById(dialogId + "-content");
	},
	
	getTopmostDialog: function(topLevelDocument) {
		var dialog = BAL.DOM.getLastElementChild(KEYBOARD_NAVIGATION.getDialogsAnchor(topLevelDocument));
		var dialogId = BAL.DOM.getAttribute(dialog, "id");
		return topLevelDocument.getElementById(dialogId + "-window");
	},
	
	getDialog: function(node) {
		while (node != null) {
			if (node.id != null && node.id.indexOf("-window") >= 0) {
				return node;
			}
			node = this.getParentElement(node);
		}
		return null;
	},
	
	focusFirstFocusableElementBelowFirstElementNode: function(anchor) {
		var rootNode = BAL.DOM.getFirstElementChild(anchor);
		KEYBOARD_NAVIGATION.focusFirstFocusableElementBelow(rootNode);
	},
	
	focusFirstFocusableElementBelow: function(anchor) {
		var oldActiveElement = BAL.DOM.getCurrentActiveSimpleElement();
		KEYBOARD_NAVIGATION.focusTextFieldBelow(anchor);
		
		var newActiveElement = BAL.DOM.getCurrentActiveSimpleElement();
		if(oldActiveElement == newActiveElement) {
			KEYBOARD_NAVIGATION.focusFocusableElementBelow(anchor);
		}
	},
	
	focusFocusableElementBelow: function(anchor) {
		KEYBOARD_NAVIGATION.focusElementBelow(anchor, BAL.DOM.simpleFocusableElementFilter);
	},
	
	focusElementBelow: function(anchor, elementFilter) {
		if(anchor != null) {
			var focusableCandidate = anchor.firstChild;
			
			var selectionSuccessful = false;
			if (elementFilter(focusableCandidate)) {
				selectionSuccessful = BAL.focus(focusableCandidate);
			}
			
			if(!selectionSuccessful) {
				var tabber = new KEYBOARD_NAVIGATION.TabManager(
						KEYBOARD_NAVIGATION.complexWidgetFilter,
						KEYBOARD_NAVIGATION.tabCycleRootFilter,
						elementFilter);
				tabber.switchToNextSimpleWidget(focusableCandidate);
			}
		}
	},
	
	focusTextFieldBelow: function(anchor) {
		var currentDocument = BAL.getCurrentDocument(anchor);
		KEYBOARD_NAVIGATION.TextFieldFocuser.focusFirstIn(currentDocument);
	},
	
	focusFirstElementInApplicationWindow: function(topLevelDocument) {
		var anchor = KEYBOARD_NAVIGATION.getApplicationWindowAnchor(topLevelDocument);
		if(!KEYBOARD_NAVIGATION.hasNoOrTextNodeChildsOnly(anchor)) {
			KEYBOARD_NAVIGATION.focusFirstFocusableElementBelowFirstElementNode(anchor);
		}
	},
	
	getApplicationWindowAnchor: function(topLevelDocument) {
		return topLevelDocument.getElementById("applicationContent");
	},
	
	/**
	 * Checks, whether the current element has no children, or text node childs only.
	 */
	hasNoOrTextNodeChildsOnly: function(element) {
		if(element == undefined || element == null) {
			return true;
		}
		
		if(element.firstChild == null) {
			return true;
		}
		
		for(var i = 0; i < element.childNodes.length; i++) {
			if(!BAL.DOM.isTextNode(element.childNodes[i])) {
				return false;
			}
		}
		      
		return true;
	},
	
	isBelowVisibleTabroot: function(node) {
		var topmostVisibleTabroot = KEYBOARD_NAVIGATION.getTopmostVisibleLayerAnchor(BAL.getTopLevelDocumentOf(node));
		
		if(!KEYBOARD_NAVIGATION.tabCycleRootFilter(topmostVisibleTabroot)) {
			return false;
		}
		
		var currentNode = node;
		while(KEYBOARD_NAVIGATION.hasParent(currentNode)) {
			currentNode = KEYBOARD_NAVIGATION.getParentElement(currentNode);
			if(currentNode === topmostVisibleTabroot) {
				return true;
			}
		}
		
		return false;
	},
	
	getTopmostVisibleLayerAnchor: function(topLevelDocument) {
		if(KEYBOARD_NAVIGATION.isAnyPopupDialogOpen(topLevelDocument)) {
			return KEYBOARD_NAVIGATION.getTopmostPopupDialogContent(topLevelDocument);
		}
		else if(KEYBOARD_NAVIGATION.isAnyDialogOpen(topLevelDocument)) {
			return KEYBOARD_NAVIGATION.getTopmostDialog(topLevelDocument);
		}
		else {
			return KEYBOARD_NAVIGATION.getApplicationWindowAnchor(topLevelDocument);
		}
	},
	
	hasParent: function(node) {
		return KEYBOARD_NAVIGATION.getParentElement(node) != null;
	},
	
	/**
	 * Retrieves the current element's defined root node.
	 * 
	 * @param currentElement - the element, whose root node shall be retrieved
	 * @param rootElementFilter - a filter, which determines, whether an element is a defined root, or not
	 * 
	 * @returns the current element's root node, or <code>null</code> if none exists.
	 */
	getRootElement: function(currentElement, rootElementFilter) {
		
		var parentNode = currentElement;
		
		var isNotBelowRootNode = !rootElementFilter(parentNode);
		
		while(isNotBelowRootNode && (parentNode != null)) {
			parentNode = KEYBOARD_NAVIGATION.getParentElement(parentNode);
			isNotBelowRootNode = !rootElementFilter(parentNode);
		}
		
		return parentNode;
	},
		
    /**
     * Retrieves the current element's parent in DOM, regardless, whether the
     * current element resides in an iframe, or not.
     * 
     * @param currentElement -
     *            the element, whose parent in the DOM shall be retrieved, must
     *            not be <code>null</code>
     * 
     * @returns the parent element, or <code>null</code> if not present
     */
    getParentElement : function(currentElement) {
        var parentNode = currentElement.parentNode;

        // Check whether the current Element is child of an iframe
        if (parentNode == null
                || parentNode == BAL.getCurrentDocument(currentElement)) {
            var currentWindow = BAL.getCurrentWindow(currentElement);
            if(currentWindow != BAL.getTopLevelWindow(currentElement)) {
	            var searchResult = BAL.searchIFrame(currentWindow.parent.document,
	                    currentWindow);
	            if (searchResult.index != -1) {
	                parentNode = searchResult.iFrames[searchResult.index];
	            }
            }
        }

        // In case of logging
        if (KEYBOARD_NAVIGATION.doLog && (parentNode != null)) {
            KEYBOARD_NAVIGATION.log("- ");
        }

        return parentNode;
    },

    /**
     * Retrieves the current element's first or last child in DOM, regardless,
     * whether the child element resides in an iframe, or not.
     * 
     * @param currentElement -
     *            the element, whose child in the DOM shall be retrieved, must
     *            not be <code>null</code>
     * @param retrieveFirstChild -
     *            true retrieves the first child, false the last child
     * 
     * @returns the child element, according to retrieveFirstChild, or
     *          <code>null</code> if not present
     */
    getChildElement : function(currentElement, retrieveFirstChild) {
        var childNode = retrieveFirstChild ? BAL.DOM
                .getFirstChild(currentElement) : BAL.DOM
                .getLastChild(currentElement);

        // Check for iframe as child
        if ((childNode == null) && (currentElement.tagName != undefined)
                && (currentElement.tagName != null)) {

            var tagName = currentElement.tagName.toLowerCase();
            if (tagName == "iframe"
                    && !services.ajax.isLoading(BAL
                                    .getCurrentDocument(currentElement),
                            currentElement)
                    && !KEYBOARD_NAVIGATION.externalFrameFilter(currentElement)) {
                var frameBody = BAL.getFrameWindow(currentElement).document.body;
                if (frameBody != null) {
                    if (retrieveFirstChild) {
                        childNode = BAL.DOM.getFirstChild(frameBody);
                    } else {
                        childNode = BAL.DOM.getLastChild(frameBody);
                    }
                }
            }
        }

        // In case of logging
        if (KEYBOARD_NAVIGATION.doLog && (childNode != null)) {
            KEYBOARD_NAVIGATION.log("+ ");
        }

        return childNode;
    }
};

/**
 * Creates a new tab manager, which provides tabbing between complex widgets. Thereby several
 * root elements can be specified, which define different tabbing cycles.
 * 
 * @param complexWidgetFilter - filter-function, to determine, whether an element is a container
 * 								of a complex widget, or not.
 * @param tabCycleRootFilter - filter-function, to determine, whether an element is a root for
 * 							   a subtree, wherein tab cycling takes place.
 * @param tabbableElementFilter - filter-function, to determine, whether to an element can be
 * 								 tabbed to, or not. 
 */
KEYBOARD_NAVIGATION.TabManager = function(complexWidgetFilter, tabCycleRootFilter, tabbableElementFilter){
		
	var self = this;
	self._complexWidgetFilter = complexWidgetFilter;
	self._tabCycleRootFilter = tabCycleRootFilter;
	self._tabbableElementFilter = tabbableElementFilter;
	
	/**
	 * Focuses the following focusable element in the following complex widget in search order. If there is no other focusable element,
	 * the current element will remain focus.
	 * 
	 * @param currentElement - the element, which is currently focused, must not be <code>null</code>
	 * @param searchForward - true, if forward search shall be performed, false otherwise
	 */
	self._switchToFollowingInOrder = function(currentElement, searchForward) {
		
		// Failsafe
		if(currentElement == null) {
			throw new Error("Cannot switch element focus, because current element is null!");
		}
		
		var firstTimeSnapshot = null;
		var overallComplexWidgetSearchTime = 0;
		var overallSimpleWidgetSearchTime = 0;
		
		// Remove focus from current element
		currentElement.blur();
		
		if(KEYBOARD_NAVIGATION.doLog) {
			// Create initial indention
			var parentElement = currentElement;
			var initialIndention = 0;
			do {
				parentElement = KEYBOARD_NAVIGATION.getParentElement(parentElement);
				initialIndention++;
			} while(parentElement != null);
			KEYBOARD_NAVIGATION.indentCounter = initialIndention + 1;
			
			KEYBOARD_NAVIGATION.log("=== Lookup for complex widget ===\n");
			
			firstTimeSnapshot = new Date();
		}
		
		// Lookup for next complex widget
		var nextComplexWidgetElement = _nextComplexWidgetElement(currentElement, searchForward);
		
		if(KEYBOARD_NAVIGATION.doLog) {
			overallComplexWidgetSearchTime = (new Date()).getTime() - firstTimeSnapshot.getTime();
			
			if(nextComplexWidgetElement == null) {
				KEYBOARD_NAVIGATION.log("=== NO COMPLEX WIDGET FOUND! ===\n");
			}
		}
		
		if(nextComplexWidgetElement != null) {
			
			// Cycle as long as not a complex widget with a selectable simple widget is found, or
			// a full DOM cycle was performed
			var focussingSuccessful = false;
			var cycleMarkerElement = nextComplexWidgetElement;
			do {
				var simpleWidgetFirstTimeSnapshot = null;
				if(!KEYBOARD_NAVIGATION.hasNoOrTextNodeChildsOnly(nextComplexWidgetElement)) {
					
					if(KEYBOARD_NAVIGATION.doLog) {
						KEYBOARD_NAVIGATION.log("=== Lookup for simple widget ===\n");
						simpleWidgetFirstTimeSnapshot = new Date();
					}
					
					var focusableCandidate = self._nextTabbableElement(nextComplexWidgetElement, true, nextComplexWidgetElement,
							self._complexWidgetFilter);
					
					if(focusableCandidate != null) {
						focussingSuccessful = self._focusFollowing(focusableCandidate, true, nextComplexWidgetElement,
								self._complexWidgetFilter);
					}	
					
					if(KEYBOARD_NAVIGATION.doLog) {
						overallSimpleWidgetSearchTime += (new Date()).getTime() - simpleWidgetFirstTimeSnapshot;
					}
				}
				
				if(!focussingSuccessful) {
					var complexWidgetFirstTimeSnapshot = null;
					if(KEYBOARD_NAVIGATION.doLog) {
						KEYBOARD_NAVIGATION.log("=== Lookup for complex widget ===\n");
						complexWidgetFirstTimeSnapshot = new Date();
					}
					
					nextComplexWidgetElement = _nextComplexWidgetElement(nextComplexWidgetElement, searchForward);
					
					if(KEYBOARD_NAVIGATION.doLog) {
						overallComplexWidgetSearchTime += (new Date()).getTime() - complexWidgetFirstTimeSnapshot;
					}
				}
			} while(!focussingSuccessful && (nextComplexWidgetElement != null) && (nextComplexWidgetElement != cycleMarkerElement))
		}
		
		
		// Focus current element, if no other focusable elements are available
		else {
			BAL.focus(currentElement);
		}
		
		if(KEYBOARD_NAVIGATION.doLog) {
			var secondTimeSnapshot = new Date();
			var executionTime = secondTimeSnapshot.getTime() - firstTimeSnapshot.getTime();
			KEYBOARD_NAVIGATION.log("Complex widget search time: " + overallComplexWidgetSearchTime + "ms\n");
			KEYBOARD_NAVIGATION.log("Simple widget search time: " + overallSimpleWidgetSearchTime + "ms\n");
			KEYBOARD_NAVIGATION.log("Tabbing time: " + executionTime + "ms\n");
			KEYBOARD_NAVIGATION.indentCounter = 0;
			
			// Log will be cleared at function caller
		}
	};
	
	/**
	 * Retrieves the current element's very tabbable neighbor in the DOM.
	 * Based on search forward flag, the lookup for the next element will be performed in depth
	 * first, or in reverse depth first order.
	 * 
	 * @param currentElement - the element, whose very tabbable neighbor in the DOM shall be retrieved,
	 * 						   must not be <code>null</code>
	 * @param searchForward - true, if forward search shall be performed, false otherwise
	 * @param rootElement - the element at which traversal shall be stopped, in case it
	 * 						  will be passed while finding the next element, may be <code>null</code>,
	 * 						  if there is no stop element
	 * @param stopElementFilter - filter to check, whether traversal shall be stopped at analyzed element,
	 * 						  or not, may be <code>null</code>, if there are no stop elements.
	 * 
	 * @returns the element's very tabbable neighbor, may be <code>null</code>.
	 */
    self._nextTabbableElement = function(currentElement, searchForward, rootElement, stopElementFilter) {
    	return _nextElement(currentElement, searchForward, rootElement,
    			self._tabbableElementFilter, stopElementFilter);
    };
    
    /**
	 * Focus the current element, or focusable followers of the current element,
	 * if the current element is not focusable.
	 * 
	 * @param currentElement - the element to focus
	 * @param searchForward - true, if forward search shall be performed, false otherwise
	 * @param rootElement - the element at which traversal shall be stopped, in case it
	 * 						  will be passed while finding the next element, may be <code>null</code>,
	 * 						  if there is no stop element
	 * @param stopElementFilter - filter to check, whether traversal shall be stopped at analyzed element,
	 * 						  or not, may be <code>null</code>, if there are no stop elements.
	 * 
	 * @returns true, if the current element or one of its followers could be focused
	 */
	self._focusFollowing = function(currentElement, searchForward, rootElement, stopElementFilter) {
		
		// Cycle as long as not a selectable simple widget is found, or
		// a full DOM cycle was performed
		var focussingSuccessful = false;
		var cycleMarkerElement = currentElement;
		var focusableCandidate = currentElement;
		do {
			if(KEYBOARD_NAVIGATION.doLog) {
				KEYBOARD_NAVIGATION.log("=== Select simple widget ===\n");
			}
			
			// Set focus to new element
			if(focusableCandidate != null) {
				focussingSuccessful = BAL.focus(focusableCandidate);
			}
			
			if(KEYBOARD_NAVIGATION.doLog && !focussingSuccessful) {
				KEYBOARD_NAVIGATION.log("=== SIMPLE WIDGET NOT FOCUSABLE! ===\n");
			}
			
			if(!focussingSuccessful) {
				focusableCandidate = self._nextTabbableElement(focusableCandidate, searchForward, rootElement,
															   stopElementFilter);
			} 
		} while((!focussingSuccessful) && (focusableCandidate != null) &&
				(focusableCandidate !== cycleMarkerElement));
		
		return focussingSuccessful;
	};
    
    /**
     * Retrieves the current element's complex widget neighbor in the DOM.
     * Based on search forward flag, the lookup for the next element will be performed in depth
	 * first, or in reverse depth first order.
	 * 
     * @param currentElement - the element, whose complex widget neighbor in the DOM shall be retrieved,
     * 						   must not be <code>null</code>
     * @param searchForward - true, if forward search shall be performed, false otherwise
     * 
     * @returns the element's complex widget neighbor, may be <code>null</code>.
     */
    function _nextComplexWidgetElement(currentElement, searchForward) {
    	
    	// In case of reverse search order, retrieve current element complex widget parent.
    	// This element will be ignored while traversing, because we want to retrieve the complex
    	// widget, which resides in DOM before this one.
    	// If the current element is a complex widget itself, there is no need to search for
    	// the parent complex widget, because this can only happen in case of incremental search.
    	// Thereby it is impossible to fall back to the currently selected element, if there
    	// are other selectable elements in DOM below the same tab root.
    	var complexWidgetParent = null;
    	if(!searchForward && !KEYBOARD_NAVIGATION.complexWidgetFilter(currentElement)) {
    		if(KEYBOARD_NAVIGATION.doLog) {
    			KEYBOARD_NAVIGATION.log("=== Complex widget parent search begin ===\n");
    		}
    		
    		var complexWidgetParentCandidate = currentElement;
    		while(!self._complexWidgetFilter(complexWidgetParentCandidate) &&
				  !self._tabCycleRootFilter(complexWidgetParentCandidate)){
    			complexWidgetParentCandidate = KEYBOARD_NAVIGATION.getParentElement(complexWidgetParentCandidate);
    			if(self._complexWidgetFilter(complexWidgetParentCandidate)) {
    				complexWidgetParent = complexWidgetParentCandidate;
    			}
			};
    		
    		if(KEYBOARD_NAVIGATION.doLog) {
    			KEYBOARD_NAVIGATION.log("=== Complex widget parent search end ===\n");
    		}
    	}
    	
    	// Retrieve following complex widget, while ignoring parent complex widget
    	var followingComplexWidget = _nextElement(currentElement, searchForward,
    										 	/*rootElement*/ null,
    										 	self._complexWidgetFilter, null);
    	
    	if((complexWidgetParent != null) && (followingComplexWidget == complexWidgetParent)) {
    		if(KEYBOARD_NAVIGATION.doLog) {
    			KEYBOARD_NAVIGATION.log("!!!! Ignore parent complex widget !!!!\n");
    		}

    		followingComplexWidget = _nextElement(complexWidgetParent, searchForward,
    											/*rootElement*/ null,
    											self._complexWidgetFilter, null);
    	}
    	
    	
    	return followingComplexWidget;
    }
    
    /**
	 * Retrieves the current element's neighbor in the DOM, which fits some filter criteria.
	 * Based on search forward flag, the lookup for the next element will be performed in depth
	 * first, or in reverse depth first order.
	 * 
	 * @param currentElement - the element, whose neighbor in the DOM shall be retrieved,
	 * 						   must not be <code>null</code>
	 * @param searchForward - true, if forward search shall be performed, false otherwise
	 * @param rootElement - the element at which traversal shall be stopped, in case it
	 * 						  will be passed while finding the next element, may be <code>null</code>,
	 * 						  if there is no stop element
	 * @param elementFilter - filter to check, whether a node fits the search criteria, or not
	 * @param stopElementFilter - filter to check, whether traversal shall be stopped at analyzed element,
	 * 						  or not, may be <code>null</code>, if there are no stop elements.
	 * 
	 * @returns the element's neighbor, according to the given element filter, may be <code>null</code>.
	 */
    function _nextElement(currentElement, searchForward, rootElement, elementFilter, stopElementFilter) {
		
    	var startElement = currentElement;
    	var touchingElement = currentElement;
    	var followingElement = null;
    	var elementNotFound = true;
    	var stopElementNotFound = true;
    	do {
			
    		
    		// If following child is present
    		followingElement = _getLowerHierarchyElement(touchingElement, searchForward);
    		if(followingElement == null) {
    			
    			// If following sibling is present
    			followingElement = _getSiblingElement(touchingElement, searchForward);
    			if(followingElement == null) {
    				
    				// If following upper hierarchy element (parent or parent's following sibling) is present
    				if(rootElement == null) {
    					followingElement = _getUpperHierarchyElement(touchingElement, null, searchForward);
    				}
    				else {
    					followingElement = _getUpperHierarchyElement(touchingElement, rootElement, searchForward);
    				}
    			}
    			
    			// Report error, if we detect tab root cycle entered from above
    			else if(self._tabCycleRootFilter(followingElement)) {
    				_reportError(followingElement);
    				return null;
    			}
    		}
    		
    		// Report error, if we detect tab root cycle entered from above
			else if(self._tabCycleRootFilter(followingElement)) {
				_reportError(followingElement);
				return null;
			}
    		
    		
    		// Check if element fits traversal stop criteria
    		stopElementNotFound = (stopElementFilter == null) || !stopElementFilter(followingElement);
    		
    		if(stopElementNotFound) {
    			// Check if element fits selection criteria
    			if(!KEYBOARD_NAVIGATION.doLog) {
    				elementNotFound = !elementFilter(followingElement);
    			} else {
    				elementNotFound = !elementFilter(followingElement, KEYBOARD_NAVIGATION.log);
    			}
    		}
    		
    		touchingElement = followingElement;
		} while(elementNotFound && stopElementNotFound && (followingElement != null) &&
				(startElement !== touchingElement));
    	
    	if(KEYBOARD_NAVIGATION.doLog) {
    		if(followingElement == null) {
    			KEYBOARD_NAVIGATION.log("!!!!! Following node is null !!!!!\n");
    		}
    		
    		if(startElement === touchingElement) {
    			KEYBOARD_NAVIGATION.log("!!!!! DOM round-trip !!!!!\n");
    		}
    	}
    	
    	if(!elementNotFound) {
    		return followingElement;
    	}
    	else {
    		return null;
    	}
    }
    
    function _reportError(followingElement) {
    	var nodeID = "";
		if((BAL.DOM.getAttribute(followingElement, "id") != undefined) &&
		(BAL.DOM.getAttribute(followingElement, "id") != null)) {
			nodeID = BAL.DOM.getAttribute(followingElement, "id");
		}
		services.log.warn("Invalid tab root cycle marker found in subtree at node '" +
							nodeID +"'. Stop finding focusable element.");
    }
    
	/**
	 * Retrieves the current element's upper element in DOM (according to sort order, this may
	 * be the element's parent or the very next parent's sibling).
	 * 
	 * @param currentElement - the element, whose ascending element in the DOM shall be retrieved,
	 * 						   must not be <code>null</code>
	 * @param rootElement - the parent node, where to stop traversing upwards (may be <code>null</code>,
	 * 						if there is no parent node to stop at)
	 * @param searchForward - true, if forward search shall be performed, false otherwise
	 *
	 * @returns the ascending element, or <code>null</code> if not present, or traversal passed the stop element
	 */
	function _getUpperHierarchyElement(currentElement, rootElement, searchForward) {
		var parentNode = KEYBOARD_NAVIGATION.getParentElement(currentElement);
		var rootElementNotFound = (currentElement != rootElement) && (parentNode != rootElement);
		
		if((parentNode == null) || !rootElementNotFound ||
			self._tabCycleRootFilter(parentNode)) {
			return null;
		}
		
		if(searchForward) {
			// Find upper hierarchy node with a sibling
			var parentNodeSibling = null;
			do {
				rootElementNotFound = (parentNode != rootElement);
				parentNodeSibling = _getSiblingElement(parentNode, searchForward);
				parentNode = KEYBOARD_NAVIGATION.getParentElement(parentNode);
			} while(rootElementNotFound && (parentNodeSibling == null) &&
					(parentNode != null) && !self._tabCycleRootFilter(parentNode));
			
			if(rootElementNotFound) {
				return parentNodeSibling;
			}
			
			else {
				KEYBOARD_NAVIGATION.log("*** Defined root element reached ***\n");
				return null;
			}
		}
		else {
			return parentNode;
		}
	}
	
	/**
	 * Retrieves the current element's lower element in DOM (according to sort order, this may
	 * be the element's first child or the very last child of the previous sibling).
	 * 
	 * @param currentElement - the element, whose descending element in the DOM shall be retrieved,
	 *                         must not be <code>null</code>
	 * @param searchForward - true, if forward search shall be performed, false otherwise
	 *
	 * @returns the descending element, or <code>null</code> if not present
	 */
	function _getLowerHierarchyElement(currentElement, searchForward) {
		if(searchForward) {
			return KEYBOARD_NAVIGATION.getChildElement(currentElement, /* retrieveFirstChild */ true);
		}
		else {
			var previousSibling = _getSiblingElement(currentElement, searchForward);
			if(previousSibling != null) {
				var childNode = KEYBOARD_NAVIGATION.getChildElement(previousSibling, /*retrieveFirstChild*/ false);
				if(childNode != null) {
					while(KEYBOARD_NAVIGATION.getChildElement(childNode, /*retrieveFirstChild*/ false) != null) {
						childNode = KEYBOARD_NAVIGATION.getChildElement(childNode, /*retrieveFirstChild*/ false);
					}
				}
				
				return childNode;
			}
			
			return null;
		}
	}
	
	/**
	 * Retrieves the current element's sibling in DOM (according to sort order, this may
	 * be the previous or the next sibling).
	 * 
	 * @param currentElement - the element, whose sibling in the DOM shall be retrieved,
	 *                         must not be <code>null</code>
	 * @param searchForward - true, if forward search shall be performed, false otherwise
	 * 
	 * @returns the sibling element, or <code>null</code> if not present
	 */
	function _getSiblingElement(currentElement, searchForward) {
		if (BAL.DOM.isElementNode(currentElement)) {
			var nextId = BAL.DOM.getNonStandardAttribute(currentElement, searchForward ? "data-tabnext" : "data-tabprev");
			if (nextId != null) {
				var followingElement = document.getElementById(nextId);
				if (followingElement != null) {
					return followingElement;
				}
			}
		}
		
		if(searchForward) {
			var sibling = BAL.DOM.getNextElementSibling(currentElement);
			// Check for tab cycle end 
			if(sibling == null) {
				var rootNodeCandidate = KEYBOARD_NAVIGATION.getParentElement(currentElement);
				if(self._tabCycleRootFilter(rootNodeCandidate)) {
					sibling = KEYBOARD_NAVIGATION.getChildElement(rootNodeCandidate, /*retrieveFirstChild*/ true);
				}
			}
			return sibling;
		}
		else {
			var sibling = BAL.DOM.getPreviousElementSibling(currentElement);
			// Check for tab cycle end
			if(sibling == null) {
				var rootNodeCandidate = KEYBOARD_NAVIGATION.getParentElement(currentElement);
				if(self._tabCycleRootFilter(rootNodeCandidate)) {
					sibling = KEYBOARD_NAVIGATION.getChildElement(rootNodeCandidate, /*retrieveFirstChild*/ false);
				}
			} 
			
			return sibling;
		}
	}
	
	self._lookupForTableCellParent = function(currentNode) {
		var tdCandidate = currentNode;
		while(_isNotTableCell(tdCandidate)) {
			tdCandidate = KEYBOARD_NAVIGATION.getParentElement(tdCandidate);
		}
		return tdCandidate;
	};
	
	function _isNotTableCell(currentNode) {
		return currentNode != null && !_isTableCell(currentNode);
	}
	
	function _isTableCell(currentNode) {
		return BAL.DOM.isElementNode(currentNode) && (BAL.DOM.getTagName(currentNode) === "td" || BAL.DOM.getTagName(currentNode) === "th") &&
				(BAL.DOM.containsClass(currentNode, "tl-table__cell"));
	}
	
	self._scrollToTD = function(tdElement) {
		var trElement = KEYBOARD_NAVIGATION.getParentElement(tdElement);
		var scrollContainer = null;
		if(_isFrozenTable(trElement)) {
			if(_isFlexPart(trElement)) {
				var flexViewport = _getFlexViewport(trElement);
				scrollContainer = flexViewport.getContentElement().parentNode;
				_scrollHorizontal(scrollContainer, tdElement);
				_scrollVertical(scrollContainer, tdElement);
			} else {
				var fixViewport = _getFixViewport(trElement);
				scrollContainer = fixViewport.getContentElement().parentNode;
				_scrollVertical(scrollContainer, tdElement);
			}
		}
	};
	
	self._scrollToTH = function(thElement) {
		var trElement = KEYBOARD_NAVIGATION.getParentElement(thElement);
		if(_isFrozenTable(trElement)) {
			_scrollHorizontal(thElement.offsetParent.offsetParent, thElement);
		}
	};
	
	function _scrollHorizontal(scrollContainer, cell) {
		var horizontalScrollPosition = BAL.getScrollLeftElement(scrollContainer);
		var viewportWidth = BAL.getElementWidth(scrollContainer);
		var cellLeftOffset = BAL.getElementX(cell);
		var cellWidth = BAL.getElementWidth(cell);
		_scroll(scrollContainer, horizontalScrollPosition, viewportWidth, cellLeftOffset, cellWidth, _scrollLeftTo);
	}
	
	function _scrollVertical(scrollContainer, cell) {
		var verticalScrollPosition = BAL.getScrollTopElement(scrollContainer);
		var viewportHeight = BAL.getElementHeight(scrollContainer);
		var sliceDiv = cell.offsetParent.offsetParent;
		var cellTopOffset = BAL.getElementY(cell) + BAL.getElementY(sliceDiv);
		var cellHeight = BAL.getElementHeight(cell);
		_scroll(scrollContainer, verticalScrollPosition, viewportHeight, cellTopOffset, cellHeight, _scrollTopTo);
	}
	
	function _scroll(scrollContainer, currentScrollPosition, viewportSize, cellLowerOffset, cellSize, scrollFunction) {
		if(cellSize < viewportSize) {
			if(cellLowerOffset > currentScrollPosition) {
				var cellUpperOffset = cellLowerOffset + cellSize;
				var viewportUpperOffset = currentScrollPosition + viewportSize;
				if(cellUpperOffset > viewportUpperOffset) {
					var newScrollPosition = cellUpperOffset - viewportSize;
					scrollFunction.call(this, scrollContainer, newScrollPosition);
				}
			} else {
				scrollFunction.call(this, scrollContainer, cellLowerOffset);
			}
		} else {
			scrollFunction.call(this, scrollContainer, cellLowerOffset);
		}
	}
	
	function _scrollLeftTo(scrollContainer, scrollLeft) {
		BAL.setScrollLeftElement(scrollContainer, scrollLeft);
	}
	
	function _scrollTopTo(scrollContainer, scrollTop) {
		BAL.setScrollTopElement(scrollContainer, scrollTop);
	}
	
	function _isFrozenTable(trElement) {
		return BAL.DOM.containsClass(trElement, "tblFrozen");
	}
	
	function _isFlexPart(trElement) {
		return trElement.id.indexOf("fixed") == -1;
	}
	
	function _getFlexViewport(trElement) {
		var ctrlId = _getCtrlId(trElement);
		var sliceManager = TABLE.tableSliceManagers[ctrlId];
		return TABLE.ScrollPositionProvider.getFlexViewport(sliceManager);
	}
	
	function _getFixViewport(trElement) {
		var ctrlId = _getCtrlId(trElement);
		var sliceManager = TABLE.tableSliceManagers[ctrlId];
		return TABLE.ScrollPositionProvider.getFixViewport(sliceManager);
	}
	
	function _getCtrlId(trElement) {
		return trElement.id.split(".")[0];
	}
};

/**
 * Focuses a element within the next complex widget. If the DOM's end is reached, the search will continue
 * at the very first child below the specified root elements. If there is no other focusable element,
 * the current element will remain focus.
 * 
 * @param currentElement - the element, which is currently focused, must not be <code>null</code>
 */
KEYBOARD_NAVIGATION.TabManager.prototype.switchToNextComplexWidget = function(currentElement) {
	this._switchToFollowingInOrder(currentElement, /*searchForward*/ true);
};
		
	
/**
 * Focuses a element within the previous complex widget. If the DOM's begin is reached, the search will continue
 * at the very last child below the specified root elements. If there is no other focusable element,
 * the current element will remain focus.
 * 
 * @param currentElement - the element, which is currently focused, must not be <code>null</code>
 */
KEYBOARD_NAVIGATION.TabManager.prototype.switchToPreviousComplexWidget = function(currentElement) {
	this._switchToFollowingInOrder(currentElement, /*searchForward*/ false);
};

/**
 * Focuses the next focusable element in DOM. If the DOM's end is reached, the search will continue
 * at the very first child below the specified root elements. If there is no other focusable element,
 * the current element will remain focus.
 * 
 * @param currentElement - the element, which is currently focused, must not be <code>null</code>
 */
KEYBOARD_NAVIGATION.TabManager.prototype.switchToNextSimpleWidget = function(currentElement) {
	if(KEYBOARD_NAVIGATION.doLog) {
		// Create initial indention
		var parentElement = currentElement;
		var initialIndention = 0;
		do {
			parentElement = KEYBOARD_NAVIGATION.getParentElement(parentElement);
			initialIndention++;
		} while(parentElement != null);
		KEYBOARD_NAVIGATION.indentCounter = initialIndention + 1;
		
		KEYBOARD_NAVIGATION.log("=== Lookup for simple widget ===\n");
	}
	
	var tabbableWidget = this._nextTabbableElement(currentElement, true, null, null);
	if(tabbableWidget != null) {
		this._focusFollowing(tabbableWidget, true, null, null);
	}
	
	if(KEYBOARD_NAVIGATION.doLog) {
		KEYBOARD_NAVIGATION.submitLog();
		KEYBOARD_NAVIGATION.clearLog();
	}
};

/**
 * Focuses the previous focusable element in DOM. If the DOM's begin is reached, the search will continue
 * at the very last child below the specified root elements. If there is no other focusable element,
 * the current element will remain focus.
 * 
 * @param currentElement - the element, which is currently focused, must not be <code>null</code>
 */
KEYBOARD_NAVIGATION.TabManager.prototype.switchToPreviousSimpleWidget = function(currentElement) {
	if(KEYBOARD_NAVIGATION.doLog) {
		// Create initial indention
		var parentElement = currentElement;
		var initialIndention = 0;
		do {
			parentElement = KEYBOARD_NAVIGATION.getParentElement(parentElement);
			initialIndention++;
		} while(parentElement != null);
		KEYBOARD_NAVIGATION.indentCounter = initialIndention + 1;
		
		KEYBOARD_NAVIGATION.log("=== Lookup for simple widget ===\n");
	}
	
	var tabbableWidget = this._nextTabbableElement(currentElement, false, null, null);
	if(tabbableWidget != null) {
		this._focusFollowing(tabbableWidget, false, null, null);
	}
	
	if(KEYBOARD_NAVIGATION.doLog) {
		KEYBOARD_NAVIGATION.submitLog();
		KEYBOARD_NAVIGATION.clearLog();
	}
};

/**
 * @returns table cell element, wherein the focused element resides, or <code>null</code> if
 * there is no table cell in the parent hierarchy.
 * 
 * @param currentElement - the element, which is currently focused, must not be <code>null</code>
 */
KEYBOARD_NAVIGATION.TabManager.prototype.getTableCellParent = function(focusElement) {
	return this._lookupForTableCellParent(focusElement);
};

/**
 * @param tableCell - to which shall be scrolled to, must not be <code>null</code>
 * 
 * @see KEYBOARD_NAVIGATION.TabManager.prototype.getTableCellParent(focusElement)
 */
KEYBOARD_NAVIGATION.TabManager.prototype.scrollToTableCell = function(tableCell) {
	if(BAL.DOM.getTagName(tableCell) === "td") {
		this._scrollToTD(tableCell);
	} else {
		this._scrollToTH(tableCell);
	}
};

/**
 * Singleton instance of tab manager
 */
KEYBOARD_NAVIGATION.TABMANAGER_INSTANCE = new KEYBOARD_NAVIGATION.TabManager(
								KEYBOARD_NAVIGATION.complexWidgetFilter,
								KEYBOARD_NAVIGATION.tabCycleRootFilter,
								BAL.DOM.simpleFocusableElementFilter);

KEYBOARD_NAVIGATION.TextFieldFocuser = {
		
		focusFirstIn: function(currentDocument) {
			var relevantForms = KEYBOARD_NAVIGATION.TextFieldFocuser.getRelevantForms(currentDocument);
			KEYBOARD_NAVIGATION.TextFieldFocuser.focusFirstFocusableInForms(relevantForms);
		},
		
		getRelevantForms: function(currentDocument) {
			var relevantForms;
			
			var relevantSubDocuments = KEYBOARD_NAVIGATION.TextFieldFocuser.getRelevantSubDocuments(currentDocument);
			if(relevantSubDocuments.length > 0) {
				relevantForms = KEYBOARD_NAVIGATION.TextFieldFocuser.getFormsFromAllDocuments(relevantSubDocuments);
			} else {
				relevantForms = KEYBOARD_NAVIGATION.TextFieldFocuser.getRelevantFormsFromDocument(currentDocument);
			}
			
			return relevantForms;
		},

		getRelevantSubDocuments: function(currentDocument) {
			var allFrames = currentDocument.frames != null ? currentDocument.frames : new Array();
			var relevantSubDocuments = new Array();
			for(var i = allFrames.length - 1; i >= 0; i--) {
				var frameWindow = allFrames[i];
				if(KEYBOARD_NAVIGATION.isBelowVisibleTabroot(frameWindow.document)) {
					relevantSubDocuments.unshift(frameWindow.document);
				}
			}
			
			return relevantSubDocuments;
		},
		
		getFormsFromAllDocuments: function(documents) {
			var forms = new Array();
			for(var i = 0; i < documents.length; i++) {
				var documentForms = documents[i].forms;
				for(var j = 0; j < documentForms.length; j++) {
					forms.push(documentForms[j]);
				}
			}
			
			return forms;
		},
		
		getRelevantFormsFromDocument: function(currentDocument) {
			var forms = new Array();
			var documentForms = currentDocument.forms;
			for(var i = 0; i < documentForms.length; i++) {
				var form = documentForms[i];
				if(KEYBOARD_NAVIGATION.isBelowVisibleTabroot(form)) {
					forms.push(form);
				}
			}
			
			return forms;
		},
		
		focusFirstFocusableInForms: function(forms) {
			for(var i = 0; i < forms.length; i++) {
				var elements = forms[i].elements;
				for(var j = 0; j < elements.length; j++) {
					var successfulSelection = selectElement(elements[j], false);
					if(successfulSelection) {
						return;
					}
				}
			}
		}
};