services.layout = {

	/**
	 * AbortController that controls resize events that updates the layout. 
	 * @see setResizeListenerTo(element)
	 */
	_resizeController: null,
	
	// #16086 - workaround for IE9
	isIE9: false,

	/**
	 * @deprecated Used in resize functions.
	 */
	LayoutFunctionArray : new Array(),
	PostLayoutFunctionArray : new Array(),
	
	/**
	 * @deprecated Used in resize functions.
	 */
	parentLayoutInformation : {
		paddingLeft: 0,
		paddingTop: 0,
		effectiveWidth: 0,
		effectiveHeight: 0
	},
	
	/**
	 * Adds a function, which are going to modify DOM properties.
	 * @param layoutFunction
	 * 
	 * @deprecated Called from resize functions.
	 */
	addLayoutFunction : function(layoutFunction) {
		services.layout.LayoutFunctionArray.push(layoutFunction);
	},
	
	/**
	 * Adds a layout access function, which needs to be run after layouting has taken place. 
	 * @param postLayoutFunction
	 * 
	 * @deprecated Called from resize functions.
	 */
	addPostLayoutFunction : function(postLayoutFunction) {
		services.layout.PostLayoutFunctionArray.push(postLayoutFunction);
	},
	
	/**
	 * @deprecated Called from resize functions.
	 */
	createPositionAndSizeApplianceFunction : function(element, left, top, width, height) {
		return function() {
			element.style.position = 'absolute';
			element.style.left = left + "px";
			element.style.top = top + "px";
			element.style.width = width + "px";
			element.style.height = height + "px";
		};
	},

	/**
	 * @deprecated Called from resize functions.
	 */
	_applyLayout : function() {
		var layoutFunctionArray = services.layout.LayoutFunctionArray;
		var postLayoutFunctionArray = services.layout.PostLayoutFunctionArray;
		// Prevent potential concurrent modification.
		services.layout.LayoutFunctionArray = new Array();
		services.layout.PostLayoutFunctionArray = new Array();
		for ( var i = 0; i < layoutFunctionArray.length; i++) {
			layoutFunctionArray[i].call();
		}
		for ( var i = 0; i < postLayoutFunctionArray.length; i++) {
			postLayoutFunctionArray[i].call();
		}
	},

	isCollapsed : function(element) {
		var collapsedInformation = element.collapsedInformation;
		if (collapsedInformation != null) {
			var isCollapsed = TL.getTLAttribute(element, "collapsed");
			if (isCollapsed == "true") {
				return true;
			}
		}
		return false;
	},

	initialLayout : function(elementID) {
		var element = document.getElementById(elementID);
		
		// #16086 - workaround for IE9
		this.isIE9 = BAL.DOM.containsClass(BAL.getTopLevelDocument().body, "IE9");
		
		this.renderLayout(element);
		this.setResizeListenerTo(element);
	},
	
	renderLayout : function(element, effectiveWidth, effectiveHeight) {
		this._createLayout(element);
		
		if(effectiveWidth != null && effectiveHeight != null) {
			var layoutResult = element.layoutResult;
			if (layoutResult == null) {
				// Allocate a fresh layout result object.
				layoutResult = {};
				element.layoutResult = layoutResult;
			}
			layoutResult._width = effectiveWidth;
			layoutResult._height = effectiveHeight;
		}
		
		element.layout.render();
	},
	
	setResizeListenerTo: function(element) {
		// Delay event driven resize logic until last resize event was fired (e.g. this
		// re-queueing is necessary, due to IE fires two events at every single resize step
		// (first event for horizontal resizing and a second seperate event for vertical
		// resizing)
		var timerID = 0;
		
		var viewportHeight = BAL.getViewportHeight();
		var viewportWidth = BAL.getViewportWidth();
		
		var resizeFunction = function() {
			
			var newViewportHeight = BAL.getViewportHeight();
			var newViewportWidth = BAL.getViewportWidth();
			
			if ((newViewportHeight != viewportHeight)
					|| (newViewportWidth != viewportWidth)) {
				window.clearTimeout(timerID);
				timerID = window.setTimeout(function() {
					element.layout.render();
				}, 10);
				
				viewportWidth = newViewportWidth;
				viewportHeight = newViewportHeight;
			}
		};
		
		if (this._resizeController != null) {
			this._resizeController.abort();
		}
		// AbortController can not be re-used.
		this._resizeController = new AbortController();
		const _controllerSignal = this._resizeController.signal;
		window.addEventListener("resize", resizeFunction, { signal: _controllerSignal });
	},

	_createLayout : function(element) {
		var layoutConstraint = this.getParsedLayoutConstraint(element);
		if (layoutConstraint != null) {
			element.layoutConstraint = layoutConstraint;
		}
		
		var layoutAnnotation = TL.getTLAttribute(element, "layout");
		if (layoutAnnotation != null && layoutAnnotation.length > 0) {
			var layoutInfo = this._parseJson(layoutAnnotation);
			var layout = new services.layout.Layout(element, layoutInfo.horizontal, layoutInfo.maxSize);
			element.layout = layout;
			
			for (var child = element.firstChild; child != null; child = child.nextSibling) {
				if (!BAL.DOM.isElementNode(child)) {
					continue;
				}
				this._createLayout(child);
			}
		}
		
		var resizeAnnotation = TL.getTLAttribute(element, "resize");
		if (resizeAnnotation != null && resizeAnnotation.length > 0) {
			var renderFun = new Function(resizeAnnotation);
			element.layout = {
				render : function () {
					renderFun();
					services.layout._applyLayout();
					
					var customRenderingFunctions = LayoutFunctions.getCustomRenderingFunctions(element);
					if(customRenderingFunctions) {
						for(var i=0; i<customRenderingFunctions.length; i++){
							customRenderingFunctions[i]();
						}
					}
				},
				compute : function (containerWidth, containerHeight) {
					services.layout.parentLayoutInformation = new Object();
					services.layout.parentLayoutInformation.effectiveWidth = containerWidth;
					services.layout.parentLayoutInformation.effectiveHeight = containerHeight;
					this.render();
					services.layout.parentLayoutInformation = null;
				}
			};
		}
		
		var collapsedAnnotation = TL.getTLAttribute(element, "collapsed-size");
		if (collapsedAnnotation != null && collapsedAnnotation.length > 0) {
			var collapsedInformation = this._parseJson(collapsedAnnotation);
			element.collapsedInformation = collapsedInformation;
		}
	},
	
	_parseJson : function(source) {
		return eval("(" + source + ")");
	},
	
	/**
	 * @returns layout constraint, annotated to the given DOM element, or null,
	 * if the DOM element does not contain such an attribute.
	 */
	getParsedLayoutConstraint: function(element) {
		var sizeAnnotation = TL.getTLAttribute(element, "layout-size");
		if (sizeAnnotation != null && sizeAnnotation.length > 0) {
			return this._parseJson(sizeAnnotation);
		} else {
			return null;
		}
	},
	
	/**
	 * Stores the given layoutConstraint as annotation to the given node.
	 */
	setSizeAnnotation: function(node, layoutConstraint) {
		var newAttributeValue = "{size:" + layoutConstraint.size + 
							", minSize:" + layoutConstraint.minSize +
							", unit:'" + layoutConstraint.unit +  "'}";
		TL.setTLAttribute(node, "layout-size", newAttributeValue);
	}

};

services.layout.Layout = function(element, horizontal, maxPercentageOfPixelElements) {
	/* The DOM node, this layout works on. */
	this._element = element;
	
	/* Whether contents is layed out horizontally (vertically otherwise). */
	this._horizontal = horizontal;
	
	// portion of the viewport (in percent) which is maximal used to display the
	// elements whose size is given in pixel
	this._fractionMaxSizePixelElements = maxPercentageOfPixelElements / 100;
};

services.layout.Layout.prototype = {
	render : function(elementWidth, elementHeight) {
		var containerWidth;
		var containerHeight;
		
		var layoutResult = this._element.layoutResult;
		if (layoutResult == null) {
			var parent = this._element.parentNode;
			var parentLayout = parent.layout;
			if (parentLayout == null) {
				// The top-level layout starts with the viewport width.
				containerWidth = BAL.getViewportWidth();
				containerHeight = BAL.getViewportHeight();
				
				// #16086 - workaround for IE9
				if(services.layout.isIE9) {
					this.isTopLevelLayout = true;
				}
			} else {
				// There is no local width information, layout the parent node.
				parentLayout.render();
				return;
			}
		} else if(elementWidth != null && elementHeight != null){
			// This is an incremental layout process, where the own width 
			// and height has been computed before.
			containerWidth = layoutResult._width = elementWidth;
			containerHeight = layoutResult._height = elementHeight;
		} else {
			// This is an incremental layout process, where the own width 
			// and height has been computed before.
			containerWidth = layoutResult._width;
			containerHeight = layoutResult._height;
		}
		this.compute(containerWidth, containerHeight);
	},
	
	compute : function(containerWidth, containerHeight) {
		// The total number of pixels that can be used to layout all components.
		var availTotal = this._horizontal ? containerWidth : containerHeight;
		
		// The reserved space in pixel for component minimum sizes.
		var sumMin = 0;
		
		// The additional space required to layout components with fixed pixel sizes. 
		// This variable only counts the space that fixed pixel components are larger 
		// than their minimum sizes.
		var sumPx = 0;
		
		// The sum of percentage values from all components with relative layout.
		var sumPercent = 0;
		
		for (var child = this._element.firstChild; child != null; child = child.nextSibling) {
			if (!BAL.DOM.isElementNode(child)) {
				continue;
			}
			var layoutConstraint = child.layoutConstraint;
			if (layoutConstraint == null) {
				continue;
			}
			
			if (services.layout.isCollapsed(child)) {
				sumPx += 0;
				sumMin += child.collapsedInformation.size;
			} else if (layoutConstraint.unit == "px") {
				// Note: A fixed pixel component may be smaller than the global minimum 
				// size of components in a flexible layout. Therefore, at maximum the 
				// actual component size must be reserved as minimum size.
				var min = Math.min(layoutConstraint.size, services.form.FlexibleFlowLayout.getNodeMinSizeInPx(child));
				
				sumPx += layoutConstraint.size - min;
				sumMin += min;
			} else {
				sumPercent += layoutConstraint.size;
				sumMin += services.form.FlexibleFlowLayout.getNodeMinSizeInPx(child);
			}
		}
		
		// The number of pixels that can be freely distributed among the components 
		// without violating any defined minimum sizes.
		var availAdditional = Math.max(0, availTotal - sumMin);
		
		// The number of pixels that can be used to fulfill the size requirements 
		// of components with fixed pixel layouts when exceeding the component's minimum size.
		var availPx = Math.min(availAdditional, sumPx);
		
		// The number of pixels that can be freely distributed among components with relative 
		// layout.
		var availPercent = availAdditional - availPx;
		
		// The number of pixels that are assigned to 1% of a component with relative layout.
		var pixelPerPercent;
		
		// In case, there are no components with relative layout and the available space exceeds 
		// the total size required to fulfill the size requirements of components with fixed pixel 
		// layouts, the extra space is distributed among fixed pixel layout components, if they 
		// are not collapsed. This variable holds the number of extra pixels assigned to each pixel 
		// of a fixed layouted component.
		var pixelPerPixel;
		
		if (sumPercent > 0) {
			pixelPerPercent = availPercent / sumPercent;
			pixelPerPixel = 0;
		} else {
			pixelPerPercent = 0;
			pixelPerPixel = availPercent / availPx;
		}
		
		// Apply sizes to contents.
		var pos = 0;
		for (var child = this._element.firstChild; child != null; child = child.nextSibling) {
			if (!BAL.DOM.isElementNode(child)) {
				continue;
			}
			var layoutConstraint = child.layoutConstraint;
			if (layoutConstraint == null) {
				if(this.isIframe(child)) {
					this.loadIframeContent(child);
				}
				continue;
			}
			
			var assignedPx;
			if (services.layout.isCollapsed(child)) {
				assignedPx = child.collapsedInformation.size;
			} else if (layoutConstraint.unit == "px") {
				var min = Math.min(layoutConstraint.size, services.form.FlexibleFlowLayout.getNodeMinSizeInPx(child));
				assignedPx = Math.min(layoutConstraint.size, availPx + min);
				availPx -= (assignedPx - min);
				
				// Distribute additional pixels, if there are no percentage areas.
				 assignedPx += (assignedPx - min) * pixelPerPixel;
			} else {
				// Changes of this calculation must be accordingly done to FlexibleFlowLayoutControl#saveNewSizes in ajax-form.js, that determines
				// relative layout constraint size out of given pixel values.
				assignedPx = services.form.FlexibleFlowLayout.getNodeMinSizeInPx(child) + layoutConstraint.size * pixelPerPercent;
			}
			
			var layoutResult = child.layoutResult;
			if (layoutResult == null) {
				// Allocate a fresh layout result object.
				layoutResult = {};
				child.layoutResult = layoutResult;
			}
			if (this._horizontal) {
				layoutResult._top = 0;
				layoutResult._left = pos;
				layoutResult._width = assignedPx;
				layoutResult._height = containerHeight;
			} else {
				layoutResult._top = pos;
				layoutResult._left = 0;
				layoutResult._width = containerWidth;
				layoutResult._height = assignedPx;
			}
			
			var style = child.style;
			if (assignedPx > 0) {
				// #16086 - workaround for IE9
				if(services.layout.isIE9 && this.isTopLevelLayout) {
					style.position = "relative";
				} else {
					style.position = "absolute";
					style.top = layoutResult._top + "px";
					style.left = layoutResult._left + "px";
					style.width = layoutResult._width + "px";
					style.height = layoutResult._height + "px";
					style.visibility = "";
				}
			} else {
				style.visibility = "hidden";
			}
			
			var childLayout = child.layout;
			if (childLayout != null) {
				childLayout.compute(layoutResult._width, layoutResult._height);
			}
			
			pos += assignedPx;
		}
	},
	
	getMaxTotalSizeOfPixelElements: function(totalAvailableSize) {
		return totalAvailableSize * this._fractionMaxSizePixelElements;
	},
	
	isIframe: function (element) {
		return element.tagName == "IFRAME";
	},
	
	loadIframeContent: function(iframe) {
		var source = TL.getTLAttribute(iframe, "source");
		if(source != null) {
			iframe.src = source;
			TL.removeTLAttribute(iframe, "source");
		}
	}	
};

LayoutFunctions = {
	addCustomRenderingFunction: function(element, func) {
		var customRenderingFunctions = element._customRenderingFunctions;
		
		if(customRenderingFunctions) {
			customRenderingFunctions.push(func);
		} else {
			element._customRenderingFunctions = [func];
		}
	},
	
	removeCustomRenderingFunction : function(element, func) {
		var customRenderingFunctions = element._customRenderingFunctions;
		
		if(customRenderingFunctions) {
			var index = customRenderingFunctions.indexOf(func);
			if(index > -1) {
				customRenderingFunctions.splice(index, 1);
			}
		}
	},
	
	getCustomRenderingFunctions: function(element) {
		return element._customRenderingFunctions;
	}
};