/**
 * Browser abstraction layer.
 *
 * Generic interface for all browsers. This object is customized with 
 * browser-specific adjustments in scripts loaded after this script.
 * 
 * Author:  <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 */
BAL = {

	isIE: function() {
		// Internet Explorer 6-11
		if (/*@cc_on!@*/false || !!document.documentMode) {
			this.isIE = function() {
				return true;
			};
		} else {
			this.isIE = function() {
				return false;
			};
		}
		return this.isIE();
	},
	
	/**
	 * Browser with Gecko JS-Engine, e.g. Firefox 1.0+
	 */
	isGecko: function() {
		if (typeof InstallTrigger !== 'undefined') {
			this.isGecko = function() {
				return true;
			};
		} else {
			this.isGecko = function() {
				return false;
			};
		}		
		return this.isGecko();
	},
		
	unloadSequential: function() {
		if (this.isGecko() || this.isIE()) {
			this.unloadSequential = function() {
				return true;
			};
		} else {
			this.unloadSequential = function() {
				return false;
			};
		}
		return this.unloadSequential();		
	},
	
	startsWith: function(str, pattern) {
		if (String.prototype.startsWith === undefined) {
			this.startsWith = function(str, pattern) {
				return str.lastIndexOf(pattern, 0) === 0;
			}
		} else {
			this.startsWith = function(str, pattern) {
				return str.startsWith(pattern);
			}
		}
		
		return this.startsWith(str, pattern);
	},
	
	supportsAnimations: function() {
		return this._keyFramesRuleType() != undefined;
	},
	
	findKeyframesRule: function(name) {
		var keyFramesRuleType = this._keyFramesRuleType();
		
        // Gather all stylesheets into an array
        var sheets = document.styleSheets;
        
        // Loop through the stylesheets.
        for (var i = 0; i < sheets.length; i++) {
            var sheet = sheets[i];
            var rules = sheet.cssRules;
            
            // Loop through all the rules.
            for (var j = 0; j < rules.length; j++) {
                var rule = rules[j];
                
                // Find the keyframes rule whose name matches the given name.
                if (rule.type == keyFramesRuleType && rule.name == name) {
                	return rule;
                }
            }
        }
        
        // Rule not found.
        return null;
    },

	_keyFramesRuleType: function() {
		this._detectKeyFramesRuleType();

		// Try again using the short-cut.
		return this._keyFramesRuleType();
	},
	
	_detectKeyFramesRuleType: function() {
		var css = window.CSSRule;
		if (css != undefined) {
			// The standard way.
			if (css.KEYFRAMES_RULE != undefined) {
				this._keyFramesRuleType = function() {
					return css.KEYFRAMES_RULE;
				};
				
				return;
			}
			
			if (css.WEBKIT_KEYFRAMES_RULE != undefined) {
				// The webkit variant.
				this._keyFramesRuleType = function() {
					return css.WEBKIT_KEYFRAMES_RULE;
				};
				
				return;
			}
		}

		// No CSS object model at all.
		this._keyFramesRuleType = function() {
			return undefined;
		};
	},
	
	findRule: function(keyframes, stepName) {
		this._detectKeyframesRuleMethods(keyframes);
		return this.findRule(keyframes, stepName);
	},
	
	deleteRule: function(keyframes, stepName) {
		this._detectKeyframesRuleMethods(keyframes);
		return this.deleteRule(keyframes, stepName);
	},
	
	_detectKeyframesRuleMethods : function(keyframes) {
		if (keyframes == null) {
			throw new Error("Keyframes must not be null.");
		}
		
		try {
			keyframes.findRule("start");
		} catch (ex) {
			// Make sure, that the arguments is really an IE keyframes rule.
			keyframes.findRule(0);
			
			// The IE way.
			var getRuleIndex = function(keyframes, stepName) {
				var n = 0;
				while (true) {
					var rule = keyframes.findRule(n);
					if (rule == null) {
						return -1;
					}
					if (rule.keyText == stepName) {
						return n;
					}
					n++;
				}
				
				return -1;
			};
			
			this.findRule = function(keyframes, stepName) {
				var n = getRuleIndex(keyframes, stepName);
				if (n < 0) {
					return null;
				}
				return keyframes.findRule(n);
			};
			
			this.deleteRule = function(keyframes, stepName) {
				var n = getRuleIndex(keyframes, stepName);
				if (n < 0) {
					return false;
				}

				keyframes.deleteRule(n);
				return true;
			};
			
			return;
		}
		
		// The standard way.
		this.findRule = function(keyframes, stepName) {
			return keyframes.findRule(stepName);
		};

		this.deleteRule = function(keyframes, stepName) {
			if (keyframes.findRule(stepName) == null) {
				return false;
			}
			
			keyframes.deleteRule(stepName);
			return true;
		};
	},
	
	appendRule: function(keyframes, newRuleCss) {
		this._detectAppendRule();
		return this.appendRule(keyframes, newRuleCss);
	},
	
	addAnimationEndListener: function(element, listener) {
		listener.animationEndListener = BAL._createAnimationEndListener(element, listener, "animationend");
		listener.webkitAnimationEndListener = BAL._createAnimationEndListener(element, listener, "webkitAnimationEnd");
		listener.msAnimationEndListener = BAL._createAnimationEndListener(element, listener, "msanimationend");
		BAL.addEventListener(element, "animationend", listener.animationEndListener);
		BAL.addEventListener(element, "webkitAnimationEnd", listener.webkitAnimationEndListener);
		BAL.addEventListener(element, "msanimationend", listener.msAnimationEndListener);
	},
	
	_createAnimationEndListener: function(element, listener, animationEndType) {
		return function(event) {
			if(listener.animationEndListener != null) {
				BAL.removeAnimationEndListener(element, listener);
				BAL._updateAnimationEndListenerAPI(element, listener, animationEndType);
				BAL.addAnimationEndListener(element, listener);
				listener(event);
			}
		};
	},
	
	_updateAnimationEndListenerAPI: function(element, listener, animationEndType) {
		BAL.addAnimationEndListener = function(element, listener) {
			BAL.addEventListener(element, animationEndType, listener);
		};
		BAL._legacyRemoveAnimationEndListener = BAL.removeAnimationEndListener;
		BAL.removeAnimationEndListener = function(element, listener) {
			if(listener.animationEndListener == null) {
				BAL.removeEventListener(element, animationEndType, listener);
			} else {
				BAL._legacyRemoveAnimationEndListener(element, listener);
			}
		};
		BAL._updateAnimationEndListenerAPI = function(element, listener, animationEndType) {
			// Do nothing
		};
	},
	
	removeAnimationEndListener: function(element, listener) {
		BAL.removeEventListener(element, "animationend", listener.animationEndListener);
		BAL.removeEventListener(element, "webkitAnimationEnd", listener.webkitAnimationEndListener);
		BAL.removeEventListener(element, "msanimationend", listener.msAnimationEndListener);
		delete listener.animationEndListener;
		delete listener.webkitAnimationEndListener;
		delete listener.msAnimationEndListener;
	},
	
	_detectAppendRule: function() {
		// Guess constructor function.
		
		// The Chrome way.
		var keyframesRuleConstructor = window.CSSKeyframesRule;
		if (keyframesRuleConstructor == null) {
			// The Safari way.
			keyframesRuleConstructor = window.WebKitCSSKeyframesRule;
		}
		
		if (keyframesRuleConstructor != null) {
			if (keyframesRuleConstructor.prototype.insertRule != null) {
				// The webkit way.
				this.appendRule = function(keyframes, newRuleCss) {
					keyframes.insertRule(newRuleCss);
				};
				
				return;
			}
		}
		
		// The standard way.
		this.appendRule = function(keyframes, newRuleCss) {
			keyframes.appendRule(newRuleCss);
		};
		
	},
	
	changeRule: function(keyframes, stepName, newRuleCss) {
		if (this.deleteRule(keyframes, stepName)) {
			this.appendRule(keyframes, newRuleCss);
		}
	},
	
	clientLog: function(msg) {
	    try {
	    	console.log(msg);
	    } catch (ex) {
	    	// Ignore.
	    }
	},
	
	isModifierKey: function(keyCode) {
		return (keyCode >= 16) && (keyCode <= 18);
	},
	
	isCursorKey: function(keyCode) {
		// POS 1, END, CURSOR
		return (keyCode >= 35) && (keyCode <= 40);
	},
	
	isTabKey: function(keyCode) {
		return (keyCode == 9);
	},
	
	isReturnKey: function(keyCode) {
		return (keyCode == 13);
	},
	
	isEscapeKey: function(keyCode) {
		return (keyCode == 27);
	},
	
	hasShiftModifier : function(event) {
		return event.shiftKey;
	},
	
	hasCtrlModifier : function(event) {
		return event.ctrlKey;
	},
	
	hasAltModifier : function(event) {
		return event.altKey;
	},
	
	isLeftMouseButton: function(buttonCode) {
		return buttonCode == 0;
	}, 
	
	addBookmark: function(name, url) {
		var newAddBookmark;
		if (window.external != null && typeof(window.external.AddFavorite) == "unknown") {
			/*
			 * Note: can not ask "window.external.AddFavorite != null" or
			 * "window.external.AddFavorite" because this returns false.
			 */
			// IE
			newAddBookmark = function(name, url) {
				window.external.AddFavorite(url, name);
			};
		} else {
			newAddBookmark = function(name, url) {
				alert(services.i18n.NO_BOOKMARK_SUPPORT);
			};
		}
		BAL.addBookmark = newAddBookmark;
		newAddBookmark(name, url);
	},

	/**
	 * Schedules the given URL for download.
	 * 
	 * @param downloadId A DOM element identifier that can be used to trigger the download.
	 * @param url The URL to download.
	 */
	requestDownload: function(downloadId, url) {
		this._enterDownloadFrame(downloadId, url);
	},
	
	_enterDownloadFrame: function(downloadId, url) {
		var downloadElement = document.getElementById(downloadId);
		downloadElement.innerHTML = 
			"<iframe src='" + escape(url) + "' width='0' height='0' frameborder='0' scrolling='no'></iframe>";
	},
	
    getEffectiveWidth: function(element) {
        var paddingLeft   = BAL.getComputedStyleInt(element, "paddingLeft");
        var paddingRight  = BAL.getComputedStyleInt(element, "paddingRight");
        var borderLeftStyle   = BAL.getComputedStyle(element, "borderLeftStyle");
        var borderRightStyle  = BAL.getComputedStyle(element, "borderRightStyle");
        var borderLeft   = 0;
        var borderRight  = 0;
        if (borderLeftStyle != "none") {
            borderLeft   = BAL.getComputedStyleInt(element, "borderLeftWidth");
        }
        if (borderRightStyle != "none") {
            borderRight  = BAL.getComputedStyleInt(element, "borderRightWidth");
        }
        return BAL.getElementWidth(element) - paddingLeft - paddingRight - borderLeft - borderRight;
    },

    getEffectiveHeight: function(element) {
        var paddingTop   = BAL.getComputedStyleInt(element, "paddingTop");
        var paddingBottom  = BAL.getComputedStyleInt(element, "paddingBottom");
        var borderTopStyle   = BAL.getComputedStyle(element, "borderTopStyle");
        var borderBottomStyle  = BAL.getComputedStyle(element, "borderBottomStyle");
        var borderTop   = 0;
        var borderBottom  = 0;
        if (borderTopStyle != "none") {
            borderTop   = BAL.getComputedStyleInt(element, "borderTopWidth");
        }
        if (borderBottomStyle != "none") {
            borderBottom  = BAL.getComputedStyleInt(element, "borderBottomWidth");
        }
        return BAL.getElementHeight(element) - paddingTop - paddingBottom - borderTop - borderBottom;
    },

  /** <<method>>
	 * This method creates a new XMLHttpRequest honoring the various pitfalls of
	 * creating this object.
	 */
	createXMLHttpRequest: function() {
		if (window.XMLHttpRequest) { // Mozilla, Safari,...
			return new XMLHttpRequest();
		} else if (window.ActiveXObject) { // IE
			try {
				return new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				return new ActiveXObject("Microsoft.XMLHTTP");
			}
		}
	},

    installKeyBindingHandler: function(handler) {
        if (document.keyBindingHandlerInstalled == null) {
        	document.keyBindingHandlerInstalled = true;

	    	// Note: Even if this event is transmitted as "keypress" action to the
			// server, the handler must be registered for the "keydown" event.
			// Otherwise IE11 (which has no custom BAL implementation) is not able to capture
			// pressing TAB and other special keys (CTRL-A, CTRL-C, CTRL-V and so on).
        	BAL.addEventListener(document, "keydown", handler);
        }
    },

    componentReload: function(url) {
        this.disableComponentRecursively(window);
        location.href=url;
    },
    
    disableComponentRecursively: function(windowRef) {
        if (windowRef.document.body == null) {
            return;
        }
        if (BAL.DOM.getTagName(windowRef.document.body) == "frameset") {
            for (var n = 0; n < windowRef.frames.length; n++) {
                this.disableComponentRecursively(windowRef.frames[n]);
            }
        } else {
            this.disableComponent(windowRef);
        }
    },
	
	disableComponent: function(windowRef) {
	    var dynamicWaitPane = windowRef.document.createElement("div");
	    dynamicWaitPane.setAttribute("id", "waitPane");
	    dynamicWaitPane.setAttribute("class", "waiting");
	    windowRef.document.body.appendChild(dynamicWaitPane);
	},
	
	isMainLayoutReady: function() {
		return BAL.getMainLayoutWindow().document.readyState == "complete";
	}, 
	
	setOuterHTML: function(targetNode, fragment) {
		targetNode.outerHTML = fragment;
	},
	
	setInnerHTML: function(targetNode, fragment) {
		targetNode.innerHTML = fragment;
	},
	
	insertAdjacentHTML: function(startNode, position, fragment) {
		startNode.insertAdjacentHTML(position, fragment);
	},

	getBodyElement: function(doc) {
		return doc.body;
	},
	
	getTopLevelDocument: function() {
		return BAL.getTopLevelWindow().document;
	},
	
	getTopLevelDocumentOf: function(element) {
		return BAL.getTopLevelWindowOf(element).document;
	},
	
	getCurrentDocument: function(element) {
		if(BAL.DOM.isDocumentNode(element)) {
			return element;
		}
		
		return element.ownerDocument;
	},

	getTopLevelWindow: function() {
		// Must not return "window.top", because the application might run within a
		// web portal and so the real top window could belong to another domain.
		// Therefore "services.ajax.topWindow" marks the application top window.
		return services.ajax.topWindow;
	},
	
	getMainLayoutWindow: function() {
		return services.ajax.mainLayout;
	},
	
	getTopLevelWindowOf: function(element) {
		var currentWindow = BAL.getCurrentWindow(element);
		
		if((currentWindow.services != undefined) && (currentWindow.services != null)) {
			return currentWindow.services.ajax.topWindow;
		} 
		
		if(currentWindow.parent != currentWindow){
			return BAL.getTopLevelWindowOf(currentWindow.parent.document);
		}
		
		return currentWindow;
	},
	
	getFrameWindow: function(frame) {
		return frame.contentDocument.defaultView;
	},
	
	getCurrentWindow: function(element) {
		var currentWindow = null;
		var currentDocument = BAL.getCurrentDocument(element);
		if(currentDocument != null) {
			currentWindow = currentDocument.defaultView;
		}
		return currentWindow;
	},
	
  	getComputedStyle: function(element, property) {
		return window.getComputedStyle(element, null)[property];
  	},
  	
  	getComputedStyleInt: function(element, property) {
  		var styleValue = this.getComputedStyle(element, property);
  		if (styleValue != null && styleValue != "") {
  			return Math.round(parseFloat(styleValue));
  		} else {
  			return 0;
  		}
  	},

  	/**
  	 * Set the given styles at the given element.
  	 * 
  	 * Note: Directly invoked by the server in a JSFunctionCall.
  	 */
	setStyle : function(element, styleString) {
		element.setAttribute("style", styleString);
	},

	getEvent: function(event) {
		return event;
	},
	
	/**
	 * Must be used in event handlers to prevent the browser default action.
	 * 
	 * <p>
	 * Usage:
	 * </p>
	 * 
	 * <pre>
	 * function(event) {
	 *    ...
	 *    
	 *    return BAL.cancelEvent(event);
	 * }
	 * </pre>
	 * 
	 * <p>
	 * This method is an replacement for the old-fashioned way to prevent the default behavior
	 * "return false;". Just returning `false` does not work, if the event listener was registered
	 * using the new API Element.addEventListener(), which is used in BAL.addEventListener() for
	 * newer browsers.
	 * </p>
	 */
	cancelEvent: function(event) {
		event.preventDefault();
		return false;
	},

	getEventTarget: function (event){
		return event.target;
	},	
	
	getEventX: function(event) {
		return event.pageX;
	},
	
	getEventY: function(event) {
		return event.pageY;
	},
	
	getAbsoluteEventPosition: function(event) {
		var x = event.pageX - BAL.getScrollLeft(window);
		var y = event.pageY - BAL.getScrollTop(window);
		var doc = this.getCurrentDocument(event.srcElement);
		var parentIFrame = KEYBOARD_NAVIGATION.getParentElement(doc);
		if (parentIFrame != null) {
			var iFramePos = this.getAbsoluteElementPosition(parentIFrame);
			x += iFramePos.x;
			y += iFramePos.y;
		}
		return new BAL.Coordinates(x, y);
	},
	
	getEventMouseScrollDelta: function(event) {
		var pixelMode = 0;
		var lineMode = 1;
		var pageMode = 2;
		var pixelPerLine = 30; /*randomely chosen*/
		var pixelPerPage = 450; /*randomely chosen*/
		
		var deltaModeFactor = 0;
		switch(event.deltaMode) {
			case pixelMode: {
				deltaModeFactor = 1;
				break;
			}
			case lineMode: {
				deltaModeFactor = pixelPerLine;
				break;
			}
			case pageMode: {
				deltaModeFactor = pixelPerPage;
				break;
			}
		}
		return event.deltaY * deltaModeFactor;
	},
	
	addMouseScrollListener: function(element, handlerFunction) {
		BAL.addEventListener(element, "wheel", handlerFunction);
	},
	
	removeMouseScrollListener: function(element, handlerFunction) {
		BAL.removeEventListener(element, "wheel", handlerFunction);
	},
	
	removeDocumentSelection: function() {
		window.getSelection().removeAllRanges();
	},
	
	/**
	 * Retrieves the mouse coordinates relative to the insert position of a DOM
	 * node
	 * 
	 * @param {Object}
	 *            event the occured event
	 * @param {Node}
	 *            domNode the DOM element to serve as container to compute
	 *            relative positions
	 * @return {BAL.Coordinates} the coordinates of the event relative to the
	 *         position of the given DOM element
	 */
	relativeMouseCoordinates: function(event, domNode) {

		var relativePosition = BAL.mouseCoordinates(event);
		var domNodePosition = BAL.getAbsoluteElementPosition(domNode);
		relativePosition.x -= domNodePosition.x;
		relativePosition.y -= domNodePosition.y;

		return relativePosition;
	},
	
	/**
	 * Returns an object containing the X and Y coordinates of the occured
	 * element
	 * 
	 * @param {Object}
	 *            event the occured event
	 * @return {BAL.Coordinates} containing the X and Y coordinate of the
	 *         occured event
	 */
	mouseCoordinates: function(event) {
		return new BAL.Coordinates(BAL.getEventX(event), BAL.getEventY(event));
	},
	
	/**
	 * Stops further propagation for the given key on the given DOM element.
	 * 
	 * Due to deprecation, the event.key instead of the event.keyCode is used for the key check.
	 * 
	 * @param element 
	 * 			  Document Element that stops the propagation.
	 * @param key 
	 * 			  Key for that the event propagation is stopped.
	 * 
	 */
	stopEventKeyPropagation: function(element, key) {
		BAL.addEventListener(element, "keydown", function(event) {
			if(BAL.getKey(event) === key) {
				BAL.eventStopPropagation(event);
			}
		}, false);
	},
	
	eventStopPropagation: function(event) {
		event.stopPropagation();
	},
	
	/**
	 * @param phase
	 *            If <code>true</code>, phase indicates that the user wishes to initiate capture.
	 *            After initiating capture, all events of the specified type will be dispatched to
	 *            the <code>handlerFunction</code> before being dispatched to any EventTargets
	 *            beneath them in the tree. Events which are bubbling upward through the tree will
	 *            not trigger an EventListener designated to use capture.
	 */
    addEventListener: function(element, eventType, handlerFunction, phase) {
        element.addEventListener(eventType, handlerFunction, phase);
	},
	
	/**
	 * adds an event listener to the given element for the given type which will
	 * be executed once and calls back to the given callback.
	 * 
	 * @param thisObj
	 *            the 'this' which is used during callback.
	 */
	onceEventListener: function(element, eventType, callback, thisObj, phase) {
		var tmp = function() {
			callback.apply(thisObj, arguments);
			BAL.removeEventListener(element, eventType, tmp, phase);
		};
		BAL.addEventListener(element, eventType, tmp, phase);
		return tmp;
	},
	
	removeEventListener: function(element, eventType, handlerFunction, phase) {
		element.removeEventListener(eventType, handlerFunction, phase);
	},
	
	dispatchEvent: function(element, eventType) {
	   var theDoc = element.ownerDocument;
	   var evt = theDoc.createEvent(eventType);   
       evt.initEvent(new Object(), true, true);
       element.dispatchEvent(evt);   
	},
	
    isSupportedEvent: function(eventType) {
       return true;
    },
    
	getViewportWidth: function() {
		return BAL.getViewportWidthOfWindowObject(window);
	},
	
	getViewportHeight: function() {
		return BAL.getViewportHeightOfWindowObject(window);
	},
	
	/**
	 * In every cycle of the while loop the next parent x and y value is added to the positions.
	 * This function is needed for cells in tables.
	 * @param {Object} element
	 * @return JSON with the absolute coordinates of the element relative to to the window
	 */
	getAbsoluteElementPosition: function(element) {
		return BAL.getElementPositionRelativeTo(element, null);
	},
	
	getElementPositionRelativeTo: function(element, container) {
		var topX = 0;
		var topY = 0;
		while (true) {
			var offsetParent = element.offsetParent;
			if (!offsetParent || offsetParent == container
					|| !(element.ownerDocument === offsetParent.ownerDocument)) {
				break;
			}
						
			topX += BAL.getElementX(element);
			topY += BAL.getElementY(element);
			
			do {
				element = element.parentNode;
				topX -= BAL.getScrollLeftElement(element);
				topY -= BAL.getScrollTopElement(element);
			
			} while (!(element === offsetParent));
			
			// Since maps have no offsetParent the parent of a map is defined to
			// be the image which uses the map. If more than one image uses the map
			// the first one is its parent.
			if (BAL.DOM.getTagName(element) == 'map') {
                var images = document.getElementsByTagName('img');
                for (var index = 0; index < images.length; index++) {
                    if (images[index].useMap != undefined && images[index].useMap.substring(1) == element.id) {
                        element = images[index];
                        break;
                    }
                }
			}
		}
        topX += BAL.getElementX(element);
        topY += BAL.getElementY(element);

		return {
			x:topX,
			y:topY
		};
	},
	
	getScrollLeftElement: function(domElement){
    	// Below "document element"
    	if(domElement.offsetParent)
    		return domElement.scrollLeft;
    	
    	// "document element" (handled separate)
    	else
    		return 0;
	},
	
	setScrollLeftElement: function(domElement, value){
		domElement.scrollLeft = value;
	},
	
	setScrollLeftElementAnimated: function(domElement, value, scrollTime, finishCallback, stepCallback){
		BAL._scrollElementAnimated(domElement, value, "scrollLeft", scrollTime, finishCallback, stepCallback);
	},
	
	isAnimated: function(domElement) {
		return $(domElement).is(":animated");
	},
	
	_scrollElementAnimated: function(domElement, value, animatedProperty, scrollTime, finishCallback, stepCallback) {
		var animatedProperties = new Object();
		animatedProperties[animatedProperty] = value;
		
		var animationOptions = new Object();
		animationOptions.easing = "linear";
		animationOptions.duration = scrollTime;
		if(stepCallback != null) {
			animationOptions.step = function(now, fx) {
				var animationStep = new Object();
				animationStep.getStartPosition = function() {
					return fx.start;
				};
				animationStep.getCurrentPosition = function() {
					return fx.now;
				};
				animationStep.getStopPosition = function() {
					return fx.end;
				};
				animationStep.setStopPosition = function(stopPosition) {
					fx.end = stopPosition;
				};
				stepCallback.call(this, animationStep);
			};
		}
		if(finishCallback != null) {
			animationOptions.complete = finishCallback;
		}
		$(domElement).animate(animatedProperties, animationOptions);
	},
	
	getScrollTopElement: function(domElement){
		// Below "document element"
		if(domElement.offsetParent)
			return domElement.scrollTop;
		
        // "document element" (handled separate)
		else
			return 0;
	}, 
	
	setScrollTopElement: function(domElement, value){
		domElement.scrollTop = value;
	},
	
	setScrollTopElementAnimated: function(domElement, value, scrollTime, finishCallback, stepCallback){
		BAL._scrollElementAnimated(domElement, value, "scrollTop", scrollTime, finishCallback, stepCallback);
	},
	
	getScrollLeft: function(windowElement){
		return windowElement.pageXOffset;
	},
	
	getScrollTop: function(windowElement){
		return windowElement.pageYOffset;
	},
	
	getVerticalScrollBarWidth: function() {
		BAL._createScrollBarSizeFunctions();		
	    return BAL.getVerticalScrollBarWidth();
	},
	
	_createScrollBarSizeFunctions: function() {
		var measureContainer = BAL.getTopLevelDocument().createElement("div");
		measureContainer.style.visibility = "hidden";
		measureContainer.style.overflow = "scroll";
		measureContainer.style.width = "100px";
		measureContainer.style.height = "100px";
		measureContainer.style.position = "absolute";
		measureContainer.style.left = "0px";
		measureContainer.style.bottom = "0px";
		BAL.getBodyElement(BAL.getTopLevelDocument()).appendChild(measureContainer);
		
		// + 1 is a workaround for ie 11 - if the extra pixel space is not added, scollbars
		// will be not sensitive to clicks to their arrow buttons (e.g. in frozen tables).
		var verticalScrollBarWidth = measureContainer.offsetWidth - measureContainer.clientWidth + 1;
		BAL.getVerticalScrollBarWidth = function() {
			return verticalScrollBarWidth;
		};
		
		// + 1 is a workaround for ie 11 - if the extra pixel space is not added, scollbars
		// will be not sensitive to clicks to their arrow buttons (e.g. in frozen tables).
		var horizontalScrollBarHeight = measureContainer.offsetHeight - measureContainer.clientHeight + 1;
		BAL.getHorizontalScrollBarHeight = function() {
			return horizontalScrollBarHeight;
		};
		
		BAL.getBodyElement(BAL.getTopLevelDocument()).removeChild(measureContainer);
	},

	getHorizontalScrollBarHeight: function() {
		BAL._createScrollBarSizeFunctions();		
	    return BAL.getHorizontalScrollBarHeight();
	},

    getViewportWidthOfWindowObject: function(windowObject){
    	// Dynamic test, because property depends on the browser mode!
		if (windowObject.innerWidth) {
			return windowObject.innerWidth;
		} else if (windowObject.document.documentElement && windowObject.document.documentElement.clientWidth) {
			// Explorer 6 Strict Mode
			return windowObject.document.documentElement.clientWidth;
		} else if (windowObject.document.body) {
			// other Explorers
			return windowObject.document.body.clientWidth;
		}	
    },

    getViewportHeightOfWindowObject: function(windowObject){
    	// Dynamic test, because property depends on the browser mode!
		if (windowObject.innerHeight) {
			return windowObject.innerHeight;
		} else if (windowObject.document.documentElement && windowObject.document.documentElement.clientHeight) {
			// Explorer 6 Strict Mode
			return windowObject.document.documentElement.clientHeight;
		} else if (windowObject.document.body) {
			// other Explorers
			return windowObject.document.body.clientHeight;
		}	
    },

    getElementX: function(element) {
        return element.offsetLeft;
    },
    
	getElementY: function(element) {
		return element.offsetTop;
	},
	
	getElementWidth: function(element) {
		return element.offsetWidth;
	},
	
	getFullElementWidth: function(element) {
		return element.offsetWidth + BAL.getComputedStyleInt(element, "marginLeft") + BAL.getComputedStyleInt(element, "marginRight");
	},
	
	getElementHeight: function(element) {
		return element.offsetHeight;
	},
	
	getFullElementHeight: function(element) {
		return element.offsetHeight + BAL.getComputedStyleInt(element, "marginTop") + BAL.getComputedStyleInt(element, "marginBottom");
	},
	
	setElementX: function(element, value) {
		element.style.left = value + "px";
	},
	
	setElementY: function(element, value) {
		element.style.top = value + "px";
	},
	
	setElementWidth: function(element, value, unit) {
    	var resultValue;
        if (typeof(unit) == "undefined" || unit == "px") {
	        var paddingAndBorderSizes = BAL.getHorizontalPaddingAndBorderSizes(element);
            resultValue = calculateEffectiveSize(value, paddingAndBorderSizes);
        } else {
        	resultValue = value;
        }
        
        resultValue = Math.max(resultValue, 0);
        element.style.width = resultValue + (typeof(unit) == "undefined" ? "px": unit);
    },
    
    getHorizontalPaddingAndBorderSizes: function(element) {
    	return BAL.getPaddingAndBorderSizes(element, "Left", "Right");
    },
    
    getPaddingAndBorderSizes: function(element, before, after) {
    	var paddingAndBorderSizes = new Object();
    	var borderLeftStyle   = BAL.getComputedStyle(element, "border" + before + "Style");
    	var borderRightStyle  = BAL.getComputedStyle(element, "border" + after + "Style");
    	paddingAndBorderSizes.paddingBefore   = BAL.getComputedStyleInt(element, "padding" + before);
    	paddingAndBorderSizes.paddingAfter  = BAL.getComputedStyleInt(element, "padding" + after);
    	paddingAndBorderSizes.borderBefore   = 0;
    	paddingAndBorderSizes.borderAfter  = 0;
    	if (borderLeftStyle != "none") {
    		paddingAndBorderSizes.borderBefore   = BAL.getComputedStyleInt(element, "border" + before + "Width");
    	}
    	if (borderRightStyle != "none") {
    		paddingAndBorderSizes.borderAfter  = BAL.getComputedStyleInt(element, "border" + after + "Width");
    	}
    	
    	return paddingAndBorderSizes;
    },
    
    setElementHeight: function(element, value, unit) {
    	var resultValue;
        if (typeof(unit) == "undefined" || unit == "px") {
        	var paddingAndBorderSizes = BAL.getVerticalPaddingAndBorderSizes(element);
        	resultValue = calculateEffectiveSize(value, paddingAndBorderSizes);
        } else {
        	resultValue = value;
        }
        
        resultValue = Math.max(resultValue, 0);
        element.style.height = resultValue + (typeof(unit) == "undefined" ? "px": unit);
    },
    
    getVerticalPaddingAndBorderSizes: function(element) {
    	return BAL.getPaddingAndBorderSizes(element, "Top", "Bottom");
    },
    
    setEffectiveWidth: function(element, value, unit) {
    	element.style.width = value + (typeof(unit) == "undefined" ? "px": unit);
    },
    
    setEffectiveHeight: function(element, value, unit) {
    	element.style.height = value + (typeof(unit) == "undefined" ? "px": unit);
    },
	
	getKeyCode: function(event) {
		return event.keyCode;
	},
	
	getKey: function(event) {
		return event.key;
	},
	
	CANCEL_FUNCTION: function() {
		return false;
	},
	
	disableSelection: function(element) {
		// There is no browser independent way to suppress text selection.
		return false;
	},
  
	/** <<function>>
	 * Retrieves the object that triggered the given event from the event.
	 *
	 * @param event 
	 *     The event to read the trigger object.
	 * @returns The DOM element that sent the event.
	 */
	getEventSource: function(event) {
		if (event.srcElement) {
			return event.srcElement;
		} else {
			return event.target;
		}
	},
	
    isVisible : function(element) {
		if (window.jQuery) {
			if (!jQuery(element).is(':visible')) {
				/* Do not use pure jQuery result: E.G. when a button in the button bar is currently
				 * hidden because it is displayed in the menu, jQuery thinks it is visible. */ 
				return false;
			}
		}
		
        var visibilityStyle = BAL.getComputedStyle(element, "visibility");

        // Check, whether the current ancestor is visible. IE may report
        // "inherit" even in the computed styles.
        if (visibilityStyle != "visible" && visibilityStyle != "inherit") {
            return false;
        }
        if (BAL.getComputedStyle(element, "display") == "none") {
            return false;
        }
        return true;
    },
	
	focus: function(element) {
		if (BAL.DOM.canBeActive(element)) {
			try {
                element.focus();
                if ('select' in element) {
                    element.select();
                }
                
                // Sometimes IE does not recognize element focusing, so it has
                // to be done twice (#17492)
                window.setTimeout(function() {
                	element.focus();
                	if ('select' in element) {
                		element.select();
                	}
                }, 0);
                if(window.KEYBOARD_NAVIGATION != null) {
                	var tabmanager = KEYBOARD_NAVIGATION.TABMANAGER_INSTANCE;
                	var tableCellParent = tabmanager.getTableCellParent(element);
                	if(tableCellParent != null) {
                		tabmanager.scrollToTableCell(tableCellParent);
                	}
                }
                return true;
			} catch (e) {
				// in some (unknown) situations the element is not focusable
				return false;
			}
		}
		return false;
	},
	
	/**
	 * Searches the first focusable element within the subtree identified by the given ID and focuses it.
	 */
	focusFirstId: function(elementId) {
		this.focusFirst(document.getElementById(elementId));
	},
	
	/**
	 * Searches the first focusable element within the subtree rooted at the given element.
	 */
	focusFirst: function(element) {
		var selectables = new BAL.DescendantIterator(element, BAL.DOM.canBeActive);
		if (selectables.hasNext()) {
			var selectable = selectables.next();
			BAL.focus(selectable);
			resetFocusContainer();
			return;
		}
	},
	
	/**
	 * Replaces all visible selects except the selects which are children of the
	 * given parent by a textua� representation of them
	 * 
	 * @param {Node}
	 *            parentToIgnore the parent of all select's which must not be
	 *            replaced
	 * @return {Object} some informations to put into BAL.restoreSelects
	 */
	replaceSelects : function(parentToIgnore) {
		// no need to do this
		return null;
	},

	/**
	 * reinstalls the Selects formerly replaced by BAL.replaceSelects
	 * 
	 * @param {Object}
	 *            replacedSelects information formerly received by
	 *            BAL.replaceSelects
	 */
	restoreSelects : function(replacedSelects) {
		// no need to do this
	},
	
	sendMailTo : function(event, eMailAddress) {
		var mailtoLink = "mailto:" + eMailAddress;
		var mainLayout = services.ajax.mainLayout;

		mainLayout.location.href = mailtoLink;
		
		BAL.eventStopPropagation(event);
		return BAL.cancelEvent(event);
	},
	
	requestAnimationFrame: function(animationFunction) {
		if(window.requestAnimationFrame) {
			return window.requestAnimationFrame(animationFunction);
		} else {
			return window.setTimeout(animationFunction, 0);
		}
	},
	
	cancelAnimationFrame: function(id) {
		if(window.requestAnimationFrame) {
			window.cancelAnimationFrame(id);
		} else {
			window.clearTimeout(id);
		}
	},
  
  /** <<constructor>>
   * The constructor to create a new BALRequest wrapper object.
   */
  BALRequest: function() {
    /** 
     * Create a preliminary object that we will use later for specifying the
     * prototype.
     */
  	var constr = function() {
 			this.xmlHttpRequest = BAL.createXMLHttpRequest();
  	};
  	
  	/** <<prototype>>
  	 * Create the prototype for this class literally.
  	 */
  	constr.prototype = {

	  	/** <<method>>
  		 * This method returns true, if the request has finished. This will be
  		 * called from the statechangehandler.
 		 	 *
  		 * @return true, if the request has finished 
  		 */
  		isFinished: function() {
				return this.xmlHttpRequest.readyState == 4;
  		},

			/** <<method>>
  		 * This method returns true, if the status of the request is ok, meaning
  		 * the status is code 200.
  		 *
  		 * @return true, if the status of the request is ok
  		 */
  		isOK: function() {
				return this.xmlHttpRequest.status == 200;
  		},
  	
  		/** <<method>>
 		 	 * This method returns the status of the request.
 		 	 *
 		 	 * @return the status of the request
 		 	 */
 		 	getStatus: function() {
				return this.xmlHttpRequest.status; 
  		},
		
			/** <<method>>
  		 * This method returns the response text inside the request.
  		 *
  		 * @return the response text contained inside the request
  		 */
  		getResponseText: function() {
				return this.xmlHttpRequest.responseText; 
  		},	

			/** <<method>>
 		 	 * This method opens a connection to the host at the provided url via the
  		 * provided method. The resulting request can be send synchronal or
  		 * asynchronal.
  		 *
  		 * @param method the method used for the request. Valid values are 'GET',
  		 *        'POST'
 		 	 * @param url the url to connect to
  		 * @param async make the call synchronal or not
  		 */
  		open: function(method, url, async) {
				this.xmlHttpRequest.open(method, url, async); 
  		},
  		
  		/** <<method>>
  		 * This method sends the request to the specified server, adding additional
  		 * parameters to the request.
  		 *
  		 * @param params the parameter that will be appended to the resulting
  		 *        request. Can be null.
  		 */
  		send: function(params) {
				this.xmlHttpRequest.send(params);
  		},
  		
  		/** <<method>>
  		 * Sets the provided method as a changehandler that will be called on
  		 * ReadyState changes inside the request object.
  		 *
  		 * @param handler the callback handler
  		 */
  		setChangeHandler: function(handler) {
  			var balRequest = this;
				this.xmlHttpRequest.onreadystatechange = function() {
		 	 		handler(balRequest);
			  };
  		}
  	};
  	
  	return constr;
  }(),

	/** <<method>>
	 * Makes a request to the given url.
	 *
	 * @param url the url to connect to
	 * @param handler the callback to call when the request changes state
	 */
	request: function(url, handler) {
    var balRequest = new BAL.BALRequest();
	  balRequest.setChangeHandler(handler);
	  balRequest.open("GET", url, true);
	  balRequest.send(null);
	},

	extend: function(obj) {
		for (var name in obj) {
			// Only members defined in the abstraction layer may be redefined (except 
			// those starting with '_').
			if ((! (name.charAt(0) == '_')) && (! (name in this))) {
				throw Error("Extension function '" + name + "' is not defined in the abstaction layer.");
			}
			this[name] = obj[name];
		}
	},

	DOM: {
		getTextContent: function(node) {
			return node.textContent;
		},
		
		getLocalName: function(node) {
			return node.localName;
		},
		
		getAttribute: function(node, attributeName) {
			return node.getAttribute(attributeName);
		},
		
		getFirstChild: function(node) {
			return node.firstChild;
		},
		
		getLastChild: function(node) {
			return node.lastChild;
		},

		getNextSibling: function(node) {
			return node.nextSibling;
		},
		
		getPreviousSibling: function(node) {
			return node.previousSibling;
		},

		getFirstElementChild: function(node) {
			var child = node.firstChild;
			while ((child != null) && (! this.isElementNode(child))) {
				child = this.getNextSibling(child);
			}
			return child;
		},
		
		getLastElementChild: function(node) {
			var child = node.lastChild;
			while ((child != null) && (! this.isElementNode(child))) {
				child = this.getPreviousSibling(child);
			}
			return child;
		},

		getNextElementSibling: function(node) {
			var child = this.getNextSibling(node);
			while ((child != null) && (! this.isElementNode(child))) {
				child = this.getNextSibling(child);
			}
			return child;
		},
		
		getPreviousElementSibling: function(node) {
			var child = this.getPreviousSibling(node);
			while ((child != null) && (! this.isElementNode(child))) {
				child = this.getPreviousSibling(child);
			}
			return child;
		},
		
		getChildElementCount: function(parentNode) {
			var childCount = 0;
			var child = this.getFirstElementChild(parentNode);
			while(child != null) {
				childCount++;
				child = this.getNextElementSibling(child);
			}
			
			return childCount;
		},
		
		getChildElements: function(parentNode) {
			var childElements = new Array();
			var child = this.getFirstElementChild(parentNode);
			while(child != null) {
				childElements.push(child);
				child = this.getNextElementSibling(child);
			}
			
			return childElements;
		},
		
		getClosest: function(element, selector) {
			if(!Element.prototype.closest) {
				/**
				 * Polyfill for IE11. closest method don't exist, therefore one is created.
				 */
				if (!Element.prototype.matches) {
					Element.prototype.matches = Element.prototype.msMatchesSelector || Element.prototype.webkitMatchesSelector;
				}
				
				Element.prototype.closest = function(s) {
					var el = this;

				    do {
				      if (el.matches(s)) return el;
				      el = el.parentElement || el.parentNode;
				    } while (el !== null && el.nodeType === 1);
				    
				    return null;
				};
			}
			
			return element.closest(selector);
		},

		getClassesArray: function(element) {
			if (element.className === undefined || element.className === "") {
				return [];
			}
			
			// Note: The type of className in an SVG element is SVGAnimatedString, which has no method "split".
			var value = element.className;
			if (typeof value !== "string") {
			    value = value.baseVal;
			}
			return value.split(' ');
		},
		
		setClassesArray: function(element, classes) {
			if(classes.length > 0) {
				element.className = classes.join(' ');
			} else {
				element.removeAttribute("class");
			}
		},
		
		removeClass: function(element, className) {
			var classes = this.getClassesArray(element);
			for (var n = 0; n < classes.length; n++) {
				if (classes[n] == className) {
					classes.splice(n, 1);
					this.setClassesArray(element, classes);
					return true;
				}
			}
			return false;
		},

		containsClass: function(element, className) {
			var classes = this.getClassesArray(element);
			for (var n = 0; n < classes.length; n++) {
				if (classes[n] == className) {
					return true;
				}
			}
			return false;
		},

		addClass: function(element, className) {
			if (this.containsClass(element, className)) return false;

			var classes = this.getClassesArray(element);
			classes.push(className);
			this.setClassesArray(element, classes);
			return true;
		},

		replaceClass: function(element, oldClassName, newClassName) {
			var classes = this.getClassesArray(element);
			var found = false;
			for (var n = 0; n < classes.length; n++) {
				if (classes[n] == oldClassName) {
					classes[n] = newClassName;
					found = true;
					break;
				}
			}
			
			if (! found) {
				classes.push(newClassName);
			}
			
			this.setClassesArray(element, classes);
		},
		
		setClassConditionally: function(element, condition, className) {
			if (condition) {
				this.addClass(element, className);
			} else {
				this.removeClass(element, className);
			}
		},
		
		importNode: function(doc, node) {
			var importedNode = doc.importNode(node, true);
			return importedNode;
		},

		createRange: function(doc) {
			return doc.createRange();
		},

		//
		// Non-standard functionality based on abstractions defined by 
		// the browser abstaction layer.
		//

		isDocumentNode: function(node) {
			return node.nodeType == Node.DOCUMENT_NODE;
		},
		
		isElementNode: function(node) {
			return node.nodeType == Node.ELEMENT_NODE;
		},

		isAttributeNode: function(node) {
			return node.nodeType == Node.ATTRIBUTE_NODE;
		},
		
		isTextNode: function(node) {
			return node.nodeType == Node.TEXT_NODE;
		},
		
		elementsByQName: function(node, qname) {
			return new BAL.DescendantIterator(node, qname.getPredicate());
		},
		
		/**
		 * Retrieves current active element, or <code>null</code> if none is active,
		 * based on the BAL.DOM.simpleFocusableElementFilter.
		 * 
		 * @param log - the optional log function, to store processing info, may be <code>null</code>.
		 */
		getCurrentActiveSimpleElement: function(documentContext, log) {
			return BAL.DOM.getCurrentActiveElement(BAL.DOM.simpleFocusableElementFilter, documentContext, log);
		},
		
        /**
         * Retrieves current active element, or <code>null</code> if none is
         * active.
         * 
         * @param activeElementFilter -
         *            a filter function, which determines, whether an element
         *            belongs to the set of focusable elements, or not.
         * @param log -
         *            the optional log function , to store processing info, may
         *            be <code>null</code>. Thereby the filter function must
         *            support logging.
         */
        getCurrentActiveElement : function(activeElementFilter, documentContext, log) {

            // document to search for an active element
        	var documentElement;
        	if(documentContext == undefined || documentContext == null) {
        		documentElement = BAL.getTopLevelDocument();
        	} else {
        		documentElement = documentContext;
        	}
			
	        var currentActiveElement = documentElement.activeElement;

            if (activeElementFilter(currentActiveElement, log)) {
                return currentActiveElement;
            }
            
            // If no element is active in document, try to find active element in iframes

            var innerActiveElement;
            var processedWindow = null;
            if (currentActiveElement != null && BAL.DOM.getTagName(currentActiveElement) == "iframe") {
                // first inspect the Iframe which is active
                try {
                    innerActiveElement = currentActiveElement.contentWindow.document.activeElement;
                    if (activeElementFilter(innerActiveElement, log)) {
                        return innerActiveElement;
                    } else {
                        processedWindow = currentActiveElement.contentWindow;
                    }
                } catch (e) {
                	// IFrame currentActiveElement is not accessible, e.g. still loading content
                }
            }

            var frames = BAL.getCurrentWindow(documentElement).frames;
            for (var i = 0; i < frames.length; i++) {
                var iFrame = frames[i];
                if (iFrame == processedWindow) {
                    // already inspected
                    continue;
                }
                try {
                    currentActiveElement = iFrame.document.activeElement;
                    if (activeElementFilter(currentActiveElement, log)) {
                    	return currentActiveElement;
                    }
                } catch (e) {
                	// IFrame currentActiveElement is not accessible, e.g. still loading content
                	continue;
                }
            }
            return null;
        },
		
        /**
         * @param endDocument -
         *            the document, where to start finding the element, must not
         *            be <code>null</code>
         * @param {BAL.DOM.Path}
         *            path - the BAL.DOM.Path, which specifies the path from the
         *            root node to the target element, must not be
         *            <code>null</code>
         * 
         * @returns the element, which is defined by the given path or the last valid path
         * 			element, where a section of the given path does not match
         *          the current DOM, or the target element.
         */
        getNodeFromPath : function(endDocument, path) {
            var pathMatch = true;
            var pathSection = path.previousPathSection();
            var currentDocument = endDocument;
            var currentNode = endDocument;

            // Follow path
            while (pathMatch && (pathSection != null)) {
                var matchingChild;
                if (BAL.DOM.getTagName(currentNode) == "iframe") {
                    if (services.ajax.isLoading(currentDocument, currentNode)) {
                        matchingChild = null;
                    } else {
                        matchingChild = BAL.DOM
                                .getFirstElementChild(currentNode.contentWindow.document);
                    }
                } else {
                    matchingChild = BAL.DOM.getFirstElementChild(currentNode);
                }
                var matchingNodeFound = false;
                var indexCounter = 0;

                // Try to match path section
                while (matchingChild != null) {
                    if (pathSection.tagName == BAL.DOM
                            .getTagName(matchingChild)) {
                        if (indexCounter < pathSection.offset) {
                            indexCounter++;
                        } else {
                            matchingNodeFound = true;
                            break;
                        }
                    }
                    matchingChild = BAL.DOM
                            .getNextElementSibling(matchingChild);
                }

                if (matchingNodeFound && BAL.isVisible(matchingChild)) {
                    // Element of path section found
                    currentNode = matchingChild;
                    pathSection = path.previousPathSection();
                } else {
                    // Mismatch of path section and current DOM level
                    pathMatch = false;
                }
            }

            return {domReference:currentNode, pathMatchedFully:pathMatch};
        },
		
		/**
		 * Return the position of the given child node within its parent node.
		 */
		getNodeOffset: function(node) {
			var parent = node.parentNode;
			if (parent == null) 
				throw new Error("Illegal argument: Root elements have no offset.");
			
			var offset = 0;
			for (var child = parent.firstChild; child != null; child = child.nextSibling) {
				if (child === node) return offset;
				offset++;
			}
		
			throw new Error("Assertion failure: Node not found within its parent node.");
		},
		
		getNodeAtOffset: function(parent, index) {
			var result = BAL.DOM.getFirstChild(parent);
			for (var n = 0; n < index; n++) {
				result = BAL.DOM.getNextSibling(result);
			}
			return result;
		},
		
		/**
		 * This methods removes all "iframe" child nodes of the given node
		 * (directly or hereditarily). (Approach for #1761)
		 */
		deleteIFrames: function(node) {
			try {
				var iFrames = node.getElementsByTagName("iframe");
			} catch(e) {
				/*
				 * can not call getElementsByTagName, so node has especially no
				 * iFrame children.
				 */
				return;
			}
            for (var i = iFrames.length - 1; i >= 0; i--) {
            	/*
				 * First approach (setting the src of the IFrame to "") will
				 * enforce IE to keep some connection to server open. see #1760
				 */
                iFrames[i].parentNode.removeChild(iFrames[i]);
            }
        },
        
        removeChild: function(parent, child) {
            parent.removeChild(child);
        },
        
        deleteContents: function(range) {
            range.deleteContents();
        },
        
       canAccessIFrame: function(iframe) {
            var html = null;
            try { 
              var iFrameWindow = BAL.getFrameWindow(iframe);
              html = BAL.getBodyElement(iFrameWindow.document).innerHtml;
            } catch(exception){
              // do nothing
            }

            return html != null;
        },
        
	    triggerEachNode: function(element, evtType) {
        	if (services.ajax.ignoreTLAttributes) {
        		return;
        	}
	    	if (element != null && BAL.DOM.isElementNode(element)) {
	    		if (BAL.DOM.getTagName(element) == "iframe") {
	    			if(BAL.DOM.canAccessIFrame(element)) {
	    				var contentWindow = BAL.getFrameWindow(element);
	    				if (!("services" in contentWindow && "BAL" in contentWindow)) {
	    					return;
	    				}
	    				contentWindow.BAL.DOM.triggerEachNode(contentWindow.document.body, evtType);
	    			}
	    		} else {
	    			BAL.dispatchEvent(element, evtType);
	    			for (var child = element.firstChild; child != null; child = child.nextSibling) {
	    				this.triggerEachNode(child, evtType);
	    			}
	    		}
	    	}
	    },
	    
	    /**
	     * Sets the value of the given attribute in the given element. It is necessary 
	     * to ensure that the given element has attributes.
	     * 
	     * @see BAL.DOM#hasAttributes(element)
	     */
	    setNonStandardAttribute: function(element, attribute, value) {
	    	element.setAttribute(attribute, value);
	    },
	    
	    /**
	     * Removes the given attribute in the given element. It is necessary 
	     * to ensure that the given attribute exists in the given element.
	     * 
	     */
	    removeNonStandardAttribute: function(element, attributeName) {
	    	element.removeAttribute(attributeName);
	    },
	    
	    /**
	     * Returns the value of the given attribute in the given element. It is necessary 
	     * to ensure that the given element has attributes.
	     * 
	     * @see BAL.DOM#hasAttributes(element)
	     */
	    getNonStandardAttribute: function(element, attribute) {
	       return element.getAttribute(attribute);
	    },
	    
	    /**
	     * Returns whether the given element has attributes.
	     */
	    hasAttributes: function(element) {
	    	if (element == null || !BAL.DOM.isElementNode(element)) {
	    		return false;
	    	}
	    	return element.hasAttributes();
	    },
	    
	    createXMLDocument: function(aDOMString) {
	    	var parser = new DOMParser();
	    	return parser.parseFromString(aDOMString, "text/xml").documentElement;
	    },
	    
	    /**
	     * returns the tag name of the given element in lowercase
	     */
	    getTagName: function(element) {
	    	var tagName = element.tagName;
	    	if (tagName == null) {
	    		return null;
	    	}
	    	if (tagName == undefined) {
	    		return undefined;
	    	}
	    	return tagName.toLowerCase();
	    },
	    
        /**
         * Determines the path from the target node to the endDocument.
         * 
         * @param targetNode -
         *            node in DOM, whose path shall be determined, must not be
         *            <code>null</code>
         * @param endDocument -
         *            document which is the end of the path (not included).
         * 
         * @returns {BAL.DOM.Path}, if the root node is an valid ancestor of the
         *          start node, <code>null</code> otherwise
         */
        getPath : function(endDocument, targetNode) {
            var path = new BAL.DOM.Path();
            var currentNode = targetNode;

            while (true) {
                var currentDocument = BAL.getCurrentDocument(currentNode);

                while (currentNode != currentDocument) {

                    var currentNodeTagName = BAL.DOM.getTagName(currentNode);
                    var currentNodeIndex = 0;

                    var previousSibling = currentNode;
                    while (true) {
                        previousSibling = BAL.DOM
                                .getPreviousElementSibling(previousSibling);
                        if (previousSibling == null) {
                            break;
                        }
                        if (BAL.DOM.getTagName(previousSibling) == currentNodeTagName) {
                            currentNodeIndex++;
                        }
                    }
                    path.addTrailingPathSection(currentNodeTagName,
                            currentNodeIndex);

                    currentNode = currentNode.parentNode;
                }

                if (currentDocument == endDocument) {
                    break;
                }

                var currentWindow = BAL.getCurrentWindow(currentDocument);
                var parentWindow = currentWindow.parent;
                currentDocument = parentWindow.document;
                var iFrameResult = BAL.searchIFrame(currentDocument,
                        currentWindow);
                if (iFrameResult.index == -1) {
                    throw new Error("Window is not the window of some IFrame of its parent document");
                }
                currentNode = iFrameResult.iFrames[iFrameResult.index];
            }
            return path;
        },
	    
        /**
         * Filter, to determine, whether the given element belongs to the set of
         * simple focusable elements.
         * 
         * <p>
         * Simple focusable elements are:
         * 
         * <ul>
         * <li>a (with non-empty 'href'-attribute)</li>
         * <li>button</li>
         * <li>input</li>
         * <li>select</li>
         * <li>textarea</li>
         * </ul>
         * </p>
         * These are <code>a, button, input, select and textarea</code>.
         * 
         * @param element -
         *            the node to check
         * @param log -
         *            the optional log function, to store processing info, may
         *            be <code>null</code>
         * 
         * @return true, if the element belongs to the set of focusable
         *         elements, false otherwise.
         */
        simpleFocusableElementFilter : function(element, log) {
            if (element == null || !BAL.DOM.isElementNode(element)) {
                return false;
            }
            var tagName = element.tagName.toLowerCase();
            var widgetMarkerFound = BAL.DOM.canBeActive(element);

            // In case of logging
            if (log != null) {

                var tagID = "";
                if ((element.id != undefined) && (element.id != null)) {
                    tagID = element.id;
                }

                log("<" + tagName + " id='" + tagID + "'>\n");

                if (widgetMarkerFound) {
                    log("--- Simple widget '" + tagName + "' ---\n");
                }
            }

            return widgetMarkerFound;
        },

        /**
         * 
         * Checks whether the given element is potentially an active element
         * 
         * @param {}
         *            element
         * @return {Boolean}
         */
        canBeActive : function(element) {
            var sourceTag = BAL.DOM.getTagName(element);
            if (typeof(sourceTag) == "undefined" || sourceTag == null) {
                return false;
            }

            if (!BAL.isVisible(element) || element.disabled) {
                return false;
            }

            if (sourceTag == "select" || sourceTag == "textarea"
                    || sourceTag == "button") {
                return true;
            }
            if (sourceTag == "input") {
                return element.type != "hidden";
            }
            if (sourceTag == "a") {
                var href = BAL.DOM.getAttribute(element, "href");
                if (href != null && href.length > 0) {
                    return true;
                }
            }
            return false;
        },
        
        /**
         * Checks whether the given element is a visible, enabled textfield (input or textarea), or not
         * @param element
         * @returns {Boolean}
         */
        textFieldFilter : function(element, log) {
        	var sourceTag = BAL.DOM.getTagName(element);
        	if (typeof(sourceTag) == "undefined" || sourceTag == null) {
        		return false;
        	}
        	
        	if (!BAL.isVisible(element) || element.disabled) {
        		return false;
        	}
        	
        	if (sourceTag == "input") {
        		return element.type == "text" || element.type == "password";
        	}
        	
        	if (sourceTag == "textarea") {
        		return true;
        	}
        	return false;
        }
    },

    /**
     * 
     * Checks whether the given windowElement is the window ot an iFrame in the given parentDocument
     * 
     * @param {} parentDocument the document to resiolve iFrames in
     * @param {} windowElement the window to check
     * @return {BAL.iFrameResult}
     */
    searchIFrame: function(parentDocument, windowElement) {
        var iFrames = parentDocument.getElementsByTagName("IFRAME");
        if (iFrames == null) {
            throw new Error("getElementsByTagName() for \"IFRAME\" returned null");
        }
        var i;
        for (i = 0; i < iFrames.length; i++) {
            if (iFrames[i].contentWindow == windowElement) {
                break;
            }
        }
        if (i == iFrames.length) {
            return new BAL.iFrameResult(iFrames, -1);
        } else {
            return new BAL.iFrameResult(iFrames, i);
        }
    },
    
    iFrameResult: function(iFrameArray, index) {
    	this.iFrames = iFrameArray;
    	this.index = index;
    }
};

BAL.DOM.extend = BAL.extend;

BAL.DOM.Path = function() {
	this.pathSectionPointer = -1;
	this.previousPathSectionPointer = 0;
	this.tagNameStack = new Array();
	this.offsetStack = new Array();
};

BAL.DOM.Path.prototype.addLeadingPathSection = function(tagName, index) {
	if (this.previousPathSectionPointer != this.tagNameStack.length) {
		throw new Error("ConcurrentModificationException: Path is currently navigated, can not modify");
	}
	this.tagNameStack.unshift(tagName);
	this.offsetStack.unshift(index);
};

BAL.DOM.Path.prototype.addTrailingPathSection = function(tagName, index) {
	if (this.previousPathSectionPointer != this.tagNameStack.length) {
		throw new Error("ConcurrentModificationException: Path is currently navigated, can not modify");
	}
	this.tagNameStack.push(tagName);
	this.offsetStack.push(index);
	this.previousPathSectionPointer ++;
	
};

/**
 * If this method is called, the path retrieval via #nextPathSection will be resetted to
 * the very begin.
 */
BAL.DOM.Path.prototype.resetPathRead = function() {
	this.pathSectionPointer = -1;
	this.previousPathSectionPointer = this.tagNameStack.length;
};

/**
 * Returns the next section of the path, relative to the path section which was previously
 * returned. The return value is <code>null</code>, after the last path section was returned.
 * The path section has two properties:
 * <ul>
 * <li>tagName - tag name of the element</li>
 * <li>offset - index of the element within the array of elements with the same tag name on
 * 				the same path level/li>
 * </ul>
 */
BAL.DOM.Path.prototype.nextPathSection = function() {
	this.pathSectionPointer++;
	if(this.pathSectionPointer > (this.tagNameStack.length - 1)) {
		return null;
	}
	
	var currentPathSectionPointer = this.pathSectionPointer;
	return {
		tagName: this.tagNameStack[currentPathSectionPointer],
		offset: this.offsetStack[currentPathSectionPointer]
	};
};

BAL.DOM.Path.prototype.previousPathSection = function() {
	this.previousPathSectionPointer--;
	if(this.previousPathSectionPointer < 0) {
		return null;
	}
	
	var currentPathSectionPointer = this.previousPathSectionPointer;
	return {
		tagName: this.tagNameStack[currentPathSectionPointer],
		offset: this.offsetStack[currentPathSectionPointer]
	};
};

BAL.QName = function(namespaceURI, localName) {
	this.namespaceURI = namespaceURI;
	this.localName = localName;
};

BAL.QName.prototype = {
	getPredicate: function() {
		var namespaceURI = this.namespaceURI;
		var localName = this.localName;
		
		return function(node) {
			if (BAL.DOM.getLocalName(node) != localName) {
				return false;
			}
			
			if (node.namespaceURI != namespaceURI) {
				return false;
			}
			
			return true;
		};
	},
	
	matches: function(node) {
		if (BAL.DOM.getLocalName(node) != this.localName) {
			return false;
		}
		
		if (node.namespaceURI != this.namespaceURI) {
			return false;
		}
		
		return true;
	}
};

/**
 * Returns all nodes that are descendants of the given node that match the given
 * filter. (The filter must be a function which returns a boolean for the node
 * in question)<b>
 * 
 * Note: This iterator does *not* return the given node. It is not a
 * descendant-or-self iterator and it cannot easily be changed to be one
 * (without also returning the following siblings of the given node).
 */
BAL.DescendantIterator = function(node, filter) {
	this.stack = new Array();
	this.filter = (filter != null) ? filter : this.ALL_NODES;
		
	this.nextNode = this._findNextAccepted(node.firstChild);
	this.current = null;
};

BAL.DescendantIterator.prototype = {
	ALL_NODES: function(obj) {
		return true;
	},
	
	hasNext: function() {
		return this.nextNode != null;
	},
	
	next: function() {
		if (this.nextNode == null) {
			throw new Error("DescendantIterator has no more elements.");
		}
		this.current = this.nextNode;
		
		this.nextNode = this._findNextAccepted(this._findNextAll(this.nextNode));
		return this.current;
	},
	
	remove: function() {
		if (this.current == null) {
			throw new Error("You must call next() before remove()");
		}
		this.current.parentNode.removeChild(this.current);
		this.current = null;
		
	},

	_findNextAccepted: function(node) {
		while (true) {
			if (node == null) {
				return null;
			}

			if (this.filter(node)) {
				return node;
			}
			
			node = this._findNextAll(node);
		}
	},
	
	_findNextAll: function(node) {
		var child = node.firstChild;
		if (child != null) {
			var sibling = node.nextSibling;
			if (sibling != null) {
				this.stack.push(sibling);
			}
			return child;
		}
		
		var sibling = node.nextSibling;
		if (sibling != null) {
			return sibling;
		}
		
		if (this.stack.length == 0) {
			return null;
		}
		
		return this.stack.pop();
	},
	
	destroy: function() {
		this.nextNode = null;
		this.current = null;
		this.filter = null;
	}
};

BAL.Coordinates = function(x, y) {
	this.x = x;
	this.y = y;
};
