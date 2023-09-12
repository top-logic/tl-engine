/**
 *  Places the 'content' relative to the position of 'element' and shows it.
 *  The position of the 'content', above, below, right, or left from the element
 *  depends on the size of the viewport and the size of the 'content'.
 * 
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */ 

PlaceDialog = {

	//  ========== Tooltip 2023 by SHA ==========
	generateTooltip: function(target, content) {
		let openTimeout = setTimeout(() => {
			if (target.classList.contains("tlPopupOpen")) {
				return;
			}
			const outerDocument = document.body.firstElementChild;
			const tooltip = document.createElement("div");
			tooltip.classList.add("tooltip");
			tooltip.id = PlaceDialog.tooltipId;
			tooltip.innerHTML = content;
			outerDocument.append(tooltip);
			this.positionTooltip(target, tooltip);
			this.closeTooltip(target, tooltip);
		}, 400);
		target.setAttribute("data-ttOpen", openTimeout);
	},

	closeTooltip: function(target, tooltip) {
		const removeTooltip = function() {
			let closeTimeout = setTimeout(() => {
				target.classList.remove("activeTooltip");
				tooltip.remove();
				target.removeEventListener("mouseleave", removeTooltip);
				target.removeAttribute("data-ttClose");
			}, 250);
			target.setAttribute("data-ttClose", closeTimeout);
		};
		tooltip.addEventListener("mouseleave", removeTooltip);
		target.addEventListener("mouseleave", removeTooltip);
		target.removeAttribute("data-ttOpen");
	},

	positionTooltip: function(target, tooltip) {
		target.classList.add("activeTooltip");

		tooltip.style.left = 0;
		tooltip.style.top = 0;

		if (tooltip.childElementCount > 0) {
			let ttPos = tooltip.getBoundingClientRect(),
				targetPos = target.getBoundingClientRect();
			
			positioning = "vertical";
			if (target.closest(".tooltipHorizontal") || target.closest(".popupMenu")) {
				positioning = "horizontal";
			}

			this.setVerticalPosition(tooltip, ttPos, targetPos, positioning);
			this.setHorizontalPosition(tooltip, ttPos, targetPos, positioning);
		} else {
			tooltip.style.display = "none";
		}
	},
	
	setVerticalPosition: function(tooltip, ttPos, targetPos, positioning) {
		let topSpace = targetPos.top,
			bottomSpace = (window.innerHeight - targetPos.bottom);
			
		let contentStyle = window.getComputedStyle(tooltip.firstElementChild),
			ttPadding = parseFloat(contentStyle.getPropertyValue("--ttPadding")),
			lineH = contentStyle.lineHeight;

		let verticalDir, verticalPos;
		
		if (positioning == "horizontal") {
			[verticalDir, verticalPos] = this.setVPosOnHorizontal(tooltip, ttPos, targetPos, topSpace, bottomSpace, ttPadding);
		} else {
			[verticalDir, verticalPos] = this.setVPosOnVertical(tooltip, ttPos, topSpace, bottomSpace, ttPadding, lineH);
		}
			
		tooltip.style.removeProperty("top");
		tooltip.style.removeProperty("bottom");
		tooltip.style.setProperty(verticalDir, verticalPos + "px");
		if (positioning == "horizontal") {
			let arrowPos = (topSpace + targetPos.height / 2) - tooltip.getBoundingClientRect().top;
			tooltip.style.setProperty("--ttArrow-top", arrowPos + "px");
		}
	},
	
	setVPosOnHorizontal: function(tooltip, ttPos, targetPos, topSpace, bottomSpace, ttPadding) {
		let ttUp = (bottomSpace < topSpace),
			verticalSpace = ttUp ? bottomSpace : topSpace,
			verticalDir = ttUp ? "bottom" : "top",
			verticalPos = (verticalSpace + targetPos.height / 2);

		if (verticalSpace <= ttPadding) {
			verticalPos = 0;
		} else {
			let halfTooltip = (ttPos.height / 2) + ttPadding,
				overflow = (halfTooltip > verticalPos);
			if (overflow) {
				verticalPos = ttPadding;
			} else {
				let shift = ttUp ? "50%" : "-50%";
				tooltip.style.setProperty("transform", "translate(0, " + shift + ")");
			}
		}
		return [verticalDir, verticalPos];
	},
	
	setVPosOnVertical: function(tooltip, ttPos, topSpace, bottomSpace, ttPadding, lineH) {
		let arrowStyle = window.getComputedStyle(tooltip, "::after"),
			arrowDim = parseFloat(arrowStyle.getPropertyValue("--ttArrow-dim")),
			offset = (arrowDim / 2) - 3,
			sumHeight = (ttPos.height + offset),
			ttUp = (sumHeight <= topSpace) || (bottomSpace <= topSpace);
			
		let verticalSpace = ttUp ? topSpace : bottomSpace,
			verticalDir = ttUp ? "top" : "bottom",
			verticalPos = Math.max(0, (verticalSpace - sumHeight)),
			maxHeight = (verticalSpace - offset);
			
		if (maxHeight < ((2 * ttPadding) + lineH)) {
			tooltip.style.display = "none";
			return;
		}
		tooltip.classList.toggle("ttVerticalArrowBottom", ttUp);
		tooltip.classList.toggle("ttVerticalArrowTop", !ttUp);
		tooltip.style.setProperty("max-height", maxHeight + "px");
		
		return [verticalDir, verticalPos];
	},
	
	setHorizontalPosition: function(tooltip, ttPos, targetPos, positioning) {
		let windowW = window.innerWidth,
			ttWidth = ttPos.width,
			arrowStyle = window.getComputedStyle(tooltip, "::after"),
			arrowDim = parseFloat(arrowStyle.getPropertyValue("--ttArrow-dim")),
			offset = (arrowDim / 2) - 3,
			ttPadding = parseFloat(window.getComputedStyle(tooltip.firstElementChild).getPropertyValue("--ttPadding")),
			minWidthTt = (2 * ttPadding) + offset;

		let horizontalSpace, horizontalDir, horizontalPos;
		
		if (positioning == "horizontal") {
			[horizontalSpace, horizontalDir, horizontalPos] = this.setHPosOnHorizontal(tooltip, targetPos, windowW, ttWidth, offset, ttPadding);
		} else {
			[horizontalSpace, horizontalDir, horizontalPos] = this.setHPosOnVertical(tooltip, targetPos, windowW, ttWidth, arrowDim);
		}
		
		if (horizontalSpace <= minWidthTt) {
			tooltip.style.display = "none";
			return;
		}
		tooltip.style.removeProperty("left");
		tooltip.style.removeProperty("right");
		tooltip.style.setProperty(horizontalDir, horizontalPos + "px");
		this.setTooltipWidth(tooltip, ttWidth, horizontalSpace);
	},
	
	setHPosOnHorizontal: function(tooltip, targetPos, windowW, ttWidth, offset, ttPadding) {
		let spaceL = (targetPos.left - offset),
			spaceR = (windowW - (targetPos.right + offset)),
			ttLeft = (spaceL > spaceR) && (spaceR < ttWidth);

		let horizontalSpace = ttLeft ? spaceL : spaceR,
			horizontalDir = ttLeft ? "right" : "left",
			horizontalPos = windowW - horizontalSpace;
		horizontalSpace = horizontalSpace - ttPadding;
		tooltip.classList.toggle("ttHorizontalArrowRight", ttLeft);
		tooltip.classList.toggle("ttHorizontalArrowLeft", !ttLeft);
		
		return [horizontalSpace, horizontalDir, horizontalPos];
	},
	
	setHPosOnVertical: function(tooltip, targetPos, windowW, ttWidth, arrowDim) {
		let spaceL = targetPos.right,
			spaceR = (windowW - targetPos.left),
			ttRight = (ttWidth <= spaceR) || (spaceL <= spaceR),
			shift = Math.min(((targetPos.width / 2) - arrowDim), 0);
		
		let horizontalSpace = ttRight ? spaceR : spaceL,
			horizontalDir = ttRight ? "left" : "right",
			horizontalPos = ttRight ? targetPos.left : (windowW - targetPos.right);
		horizontalPos = Math.max(0, horizontalPos + shift);
		tooltip.classList.toggle("ttVerticalArrowLeft", ttRight);
		tooltip.classList.toggle("ttVerticalArrowRight", !ttRight);
		
		return [horizontalSpace, horizontalDir, horizontalPos];
	},

	setTooltipWidth: function(tooltip, currW, space) {
		if (space < currW) {
			tooltip.style.setProperty("width", space + "px");
		}
	},
//  ========== Tooltip 2023 by SHA ==========


//  ========== Old Tooltip ==========
    removeProperty: "removeFunction",

    placeDialog: function(argObj){
        var element = argObj.element;
        var content = argObj.content;
        var additionalMoveRight = argObj.additionalMoveRight;
		if (additionalMoveRight == undefined) {
            additionalMoveRight = 0;
		}
        var additionalMoveDown = argObj.additionalMoveDown;
		if (additionalMoveDown == undefined) {
            additionalMoveDown = 0;
		}
		var preferBelow = argObj.preferBelow;
		if (preferBelow == undefined) {
            preferBelow = true;
		}

        //Determine the position of the element and place the content
		var anchorWidth = 0;
		var anchorHeight = 0;
		var anchorX = 0;
		var anchorY = 0;
        if (element != null) {
        	var anchorPosition = this._getElementPositionRelativeToOutermostWindow(element);
        	anchorX = anchorPosition.left;
        	anchorY = anchorPosition.top;
        	anchorWidth = BAL.getElementWidth(element);
        	anchorHeight = BAL.getElementHeight(element);
        } else {
        	if (argObj.anchorX != undefined) {
        		anchorX = argObj.anchorX;
        	}
        	if (argObj.anchorY != undefined) {
        		anchorY = argObj.anchorY;
        	}
        }
        
        var hPos = argObj.hPos;
        if (hPos == null) {
        	hPos = "LEFT_ALIGNED";
    	}
        var vPos = argObj.vPos;
        if (vPos == null) {
        	vPos = preferBelow ? "BELOW" : "ABOVE";
    	}
        
        this._placeElement(content, anchorX, anchorY, anchorWidth, anchorHeight, hPos, vPos, additionalMoveRight, additionalMoveDown);

		content.style.display = 'block';
    },
       
    /**
     *  this method returns the position of the upper left corner of the element
     *  computed in the outermost window.
     */
    _getElementPositionRelativeToOutermostWindow: function(element) {
        
        var currentWindow = window;

        var absPosRelToWin = BAL.getAbsoluteElementPosition(element);
        
        while (currentWindow != services.ajax.topWindow) {
            absPosRelToWin.x -= BAL.getScrollLeft(currentWindow);
            absPosRelToWin.y -= BAL.getScrollTop(currentWindow);
            
            // The frame DOM element corresponding to currentWindow within the 
            // currentWindow's parent window.
            var theFrame = null;
             
            var found = false;
            var iFrames = currentWindow.parent.document.getElementsByTagName('IFRAME');
            var frames = currentWindow.parent.document.getElementsByTagName('FRAME');
            for (var i = 0; i < frames.length && !found; i++) {
                theFrame = frames[i];
                if (theFrame.contentWindow == currentWindow) {
                    found = true;
                }
            }
            for (var i = 0; i < iFrames.length && !found; i++) {
                theFrame = iFrames[i];
                if (theFrame.contentWindow == currentWindow) {
                    found = true;
                }
            }
            if (!found){
                throw new Error('Frame not found');
            }
            
            currentWindow = currentWindow.parent;
            
            var absoluteFramePosition = BAL.getAbsoluteElementPosition(theFrame);
            absPosRelToWin.x += absoluteFramePosition.x;
            absPosRelToWin.y += absoluteFramePosition.y;
        }
        return {
                left : absPosRelToWin.x,
                top : absPosRelToWin.y
        };
    },

    /**
     *  this method returns the position where the event occured
     *  computed in the outermost window.
     */
    _getEventPositionRelativeToOutermostWindow: function(event) {
        
        var currentWindow = window;

        var absPosRelToWin = {x: BAL.getEventX(event), y: BAL.getEventY(event)} ;
        
        while (currentWindow != services.ajax.topWindow) {
            absPosRelToWin.x -= BAL.getScrollLeft(currentWindow);
            absPosRelToWin.y -= BAL.getScrollTop(currentWindow);
            
            // The frame DOM element corresponding to currentWindow within the 
            // currentWindow's parent window.
            var theFrame = null;
             
            var found = false;
            var iFrames = currentWindow.parent.document.getElementsByTagName('IFRAME');
            var frames = currentWindow.parent.document.getElementsByTagName('FRAME');
            for (var i = 0; i < frames.length && !found; i++) {
                theFrame = frames[i];
                if (theFrame.contentWindow == currentWindow) {
                    found = true;
                }
            }
            for (var i = 0; i < iFrames.length && !found; i++) {
                theFrame = iFrames[i];
                if (theFrame.contentWindow == currentWindow) {
                    found = true;
                }
            }
            if (!found){
                throw new Error('Frame not found');
            }
            
            currentWindow = currentWindow.parent;
            
            var absoluteFramePosition = BAL.getAbsoluteElementPosition(theFrame);
            absPosRelToWin.x += absoluteFramePosition.x;
            absPosRelToWin.y += absoluteFramePosition.y;
        }
        return {
                left : absPosRelToWin.x,
                top : absPosRelToWin.y
        };
    },
    
    _placeElement: function(dialog, anchorX, anchorY, anchorWidth, anchorHeight, hPos, vPos, additionalShiftHorizontal, additionalShiftVertical) {
        var viewportWidth = BAL.getViewportWidthOfWindowObject(services.ajax.topWindow);
        var viewportHeight = BAL.getViewportHeightOfWindowObject(services.ajax.topWindow);

		// Position must be set *before* element with is computed: If the dialog is a DIV 
		// without explicit width, then the width is the whole viewport. When the position
		// is absolute, the width is just the width of the actual content.         
 		dialog.style.position = 'absolute';

        // Enable scrolling if dialogWidth is to large
        var dialogWidth = BAL.getElementWidth(dialog);
        if (dialogWidth > viewportWidth) { 
            dialogWidth = viewportWidth;
            BAL.setElementWidth(dialog, viewportWidth);
        }
        
        // Enable scrolling if dialog height is to large.
        var dialogHeight = BAL.getElementHeight(dialog);
        if (dialogHeight > viewportHeight) {
            dialogHeight = viewportHeight;
            BAL.setElementHeight(dialog, viewportHeight);
        }
        
        var strategy = new PositionStrategy(viewportWidth, viewportHeight, anchorX, anchorY, anchorWidth, anchorHeight, additionalShiftHorizontal, additionalShiftVertical, dialogWidth, dialogHeight);
		strategy.initHPos(hPos);
		strategy.initVPos(vPos);
		
        dialog.style.left = strategy.leftPosition + 'px';
        dialog.style.top = strategy.topPosition + 'px';
    },
    
    tooltipId: "tooltip",
    
    makeTooltip: function(argObj){
    	var tooltipTimeoutId = null;
        var element = argObj.element;
        var content = argObj.content;
        var tooltipClasses = argObj.tooltipClasses;
        if (tooltipClasses == undefined) {
            tooltipClasses = new Array();
            tooltipClasses.push('tooltip');
        }
        var delay = argObj.delay;
        if (delay == undefined) {
            delay = 250;
        }
        var preferBelow = argObj.preferBelow;
        if (preferBelow == undefined) {
            preferBelow = false;
        }
        var additionalShiftHorizontal = argObj.additionalShiftHorizontal;
        if (additionalShiftHorizontal == undefined) {
            additionalShiftHorizontal = 10;
        }
        var additionalShiftVertical = argObj.additionalShiftVertical;
        if (additionalShiftVertical == undefined) {
            additionalShiftVertical = 4;
        }
        var event = BAL.getEvent(argObj.event);
        var eventPos = this._getEventPositionRelativeToOutermostWindow(event);
        
        var outermostWindow = services.ajax.topWindow;
        var placeDialogObject = this;

        var mouseMove = function(event) {
        	var balEvent = BAL.getEvent(event);
        	eventPos = placeDialogObject._getEventPositionRelativeToOutermostWindow(balEvent);
        };
        var cancelTooltip = function() {
        	outermostWindow.clearTimeout(tooltipTimeoutId);
        	removeTooltipTriggerListener();
        };
        var delayedFunction = function() {
        	removeTooltipTriggerListener();
            placeDialogObject._makeTooltip(element, content, tooltipClasses, preferBelow, additionalShiftHorizontal, additionalShiftVertical, eventPos);
        };
        var removeTooltipTriggerListener = function() {
        	BAL.removeEventListener(document, 'mousemove', mouseMove);
        	BAL.removeEventListener(element, 'mousedown', cancelTooltip);
        	BAL.removeEventListener(element, 'mouseout', cancelTooltip);
        	BAL.removeEventListener(element, 'DOMNodeRemovedFromDocument', cancelTooltip); 
        };
        
        BAL.addEventListener(document, 'mousemove', mouseMove);
        BAL.addEventListener(element, 'mouseout', cancelTooltip);
        BAL.addEventListener(element, 'mousedown', cancelTooltip);
        BAL.addEventListener(element, 'DOMNodeRemovedFromDocument', cancelTooltip); 
        tooltipTimeoutId = outermostWindow.setTimeout(delayedFunction, delay);
    },
    
    removeTooltip: function() {
        var outermostDocument = services.ajax.topWindow.document;
        var oldTooltip = outermostDocument.getElementById(PlaceDialog.tooltipId);
        if (oldTooltip != undefined) {
            oldTooltip.parentNode.removeChild(oldTooltip);
        }
        
    },
    
    _makeTooltip: function (element, content, tooltipClasses, preferBelow, additionalShiftHorizontal, additionalShiftVertical, eventPos) {
        PlaceDialog.removeTooltip();
        var outermostDocument = services.ajax.topWindow.document;
        var tooltipDiv = outermostDocument.createElement('div');
        tooltipDiv.id = PlaceDialog.tooltipId;
        tooltipDiv.style.padding = '3px';
        tooltipDiv.style.zIndex = 9999999;
        tooltipDiv.innerHTML = content;
        BAL.DOM.setClassesArray(tooltipDiv, tooltipClasses);

        var onmouseout = function() {
            BAL.removeEventListener(element, 'mouseout', onmouseout);
            BAL.removeEventListener(element, 'mousedown', onmouseout);
            BAL.removeEventListener(element, 'DOMNodeRemovedFromDocument', onmouseout);
            PlaceDialog.removeTooltip();
            
            // Don't suppress standard action (e.g. checkboxes would not react to clicks and
            // therefore cannot become enabled or disabled). If any element needs to suppress the
            // default action, it has to be done by a separate event listener.
            // Also don't return true explicitly, because there is a different handling of event
            // bubbling between return values true and undefined. Despite both
            // return values don't prevent event bubbling, the former one undermines the above
            // mentioned possibility of event cancellation.
        };
        BAL.DOM.getFirstElementChild(outermostDocument.body).appendChild(tooltipDiv);
        BAL.addEventListener(element, 'mouseout', onmouseout);
        BAL.addEventListener(element, 'mousedown', onmouseout);
        BAL.addEventListener(element, 'DOMNodeRemovedFromDocument', onmouseout);
        
        if (!eventPos) {
	        var elementPos = this._getElementPositionRelativeToOutermostWindow(element);
	        this._placeTooltip(tooltipDiv, elementPos, BAL.getElementWidth(element), BAL.getElementHeight(element), preferBelow, additionalShiftHorizontal, additionalShiftVertical);
        } 
        else {
	        this._placeTooltip(tooltipDiv, eventPos, 0, 0, preferBelow, additionalShiftHorizontal, additionalShiftVertical);
        }
        
    },
    
    _placeTooltip: function(tooltip, anchorPosition, anchorWidth, anchorHeight, preferBelow, additionalShiftHorizontal, additionalShiftVertical) {
        var leftPosition, topPosition = 0;
        
    	tooltip.style.position = 'absolute';
    	tooltip.style.overflow = 'auto';

            // if possible place the tooltip right, otherwise left 
        var tooltipWidth = BAL.getElementWidth(tooltip);
        var viewportWidth = BAL.getViewportWidthOfWindowObject(services.ajax.topWindow);
        if ((anchorPosition.left + anchorWidth + tooltipWidth + additionalShiftHorizontal) < viewportWidth){
            leftPosition = anchorPosition.left + anchorWidth + additionalShiftHorizontal;
        } else{
            leftPosition = anchorPosition.left - tooltipWidth - additionalShiftHorizontal;
        }

            // if possible place the tooltip above, otherwise below 
        var tooltipHeight = BAL.getElementHeight(tooltip);
        var viewportHeight = BAL.getViewportHeightOfWindowObject(services.ajax.topWindow);
        if (preferBelow) {
            if ((anchorPosition.top + anchorHeight + tooltipHeight + additionalShiftVertical) < viewportHeight) {
                topPosition = anchorPosition.top + anchorHeight + additionalShiftVertical;
            } else{
                topPosition = anchorPosition.top - tooltipHeight - additionalShiftVertical;
            }
        }
        else {
            if ((anchorPosition.top - tooltipHeight - additionalShiftVertical) >= 0) {
                topPosition = anchorPosition.top - tooltipHeight - additionalShiftVertical;
            } else {
                var lowestPoint = anchorPosition.top + anchorHeight + tooltipHeight + additionalShiftVertical;
                if (lowestPoint < viewportHeight) {
                    topPosition = anchorPosition.top + anchorHeight + additionalShiftVertical;
                } else {
                    topPosition = anchorPosition.top - tooltipHeight - additionalShiftVertical;
                }
            }
        }
        
        if(leftPosition < 0 || topPosition < 0) {
        	var additionalSpace = 5;
        	if(leftPosition < 0 && topPosition < 0) {
        		var tooltipLocation;
        		if((tooltipWidth < viewportWidth && tooltipHeight < viewportHeight) ||
        			(tooltipWidth > viewportWidth && tooltipHeight > viewportHeight)) {
        			var horizontalOverlap = Math.abs(viewportWidth - tooltipWidth);
        			var verticalOverlap = Math.abs(viewportHeight - tooltipHeight);
        			if(horizontalOverlap < verticalOverlap) {
        				tooltipLocation = this._expandHorizontally(tooltip, tooltipWidth, tooltipHeight, anchorPosition, anchorHeight, viewportWidth, viewportHeight, additionalShiftVertical, additionalSpace);
        			} else {
        				tooltipLocation = this._expandVertically(tooltip, tooltipWidth, tooltipHeight, anchorPosition, anchorWidth, viewportWidth, viewportHeight, additionalShiftHorizontal, additionalSpace);
        			}
    			} else if(tooltipWidth < viewportWidth) {
    				tooltipLocation = this._expandHorizontally(tooltip, tooltipWidth, tooltipHeight, anchorPosition, anchorHeight, viewportWidth, viewportHeight, additionalShiftVertical, additionalSpace);
    			} else {
    				tooltipLocation = this._expandVertically(tooltip, tooltipWidth, tooltipHeight, anchorPosition, anchorWidth, viewportWidth, viewportHeight, additionalShiftHorizontal, additionalSpace);
    			}
        		leftPosition = tooltipLocation.leftPosition;
        		topPosition = tooltipLocation.topPosition;
        	} else if(leftPosition < 0) {
        		if(tooltipWidth > viewportWidth - 2 * additionalSpace) {
        			var constant = 2; /* arbitrary constant to avoid endless recursion, due to BAL.setElementWidth() and BAL.getElementWidth() are not strictly synchronous*/
        			BAL.setElementWidth(tooltip, viewportWidth - 2 * additionalSpace - constant);
        			this._placeTooltip(tooltip, anchorPosition, anchorWidth, anchorHeight, preferBelow, additionalShiftHorizontal, additionalShiftVertical)
        			return;
        		}
        		leftPosition = additionalSpace;
        	} else {
        		if(tooltipHeight > viewportHeight - 2 * additionalSpace) {
        			var constant = 2; /* arbitrary constant to avoid endless recursion, due to BAL.setElementWidth() and BAL.getElementWidth() are not strictly synchronous*/
        			BAL.setElementHeight(tooltip, viewportHeight - 2 * additionalSpace - constant);
        			this._placeTooltip(tooltip, anchorPosition, anchorWidth, anchorHeight, preferBelow, additionalShiftHorizontal, additionalShiftVertical)
        			return;
        		}
        		topPosition = additionalSpace;
        	}
        }
        
        tooltip.style.left = leftPosition + 'px';
        tooltip.style.top = topPosition + 'px';
    },
    
    _expandHorizontally: function(tooltip, tooltipWidth, tooltipHeight, anchorPosition, anchorHeight, viewportWidth, viewportHeight, additionalShiftVertical, additionalSpace) {
    	var leftPosition = additionalSpace;
    	var topPosition;
		if(tooltipWidth > viewportWidth - 2 * additionalSpace) {
			BAL.setElementWidth(tooltip, viewportWidth - 2 * additionalSpace);
		}
		var topSpace = anchorPosition.top - additionalShiftVertical;
		var bottomSpace = viewportHeight - anchorPosition.top - anchorHeight - additionalShiftVertical;
		if(topSpace > bottomSpace) {
			topPosition = additionalSpace;
			BAL.setElementHeight(tooltip, topSpace - additionalSpace);
		} else {
			topPosition = anchorPosition.top + additionalShiftVertical;
			BAL.setElementHeight(tooltip, bottomSpace - additionalSpace);
		}
    	return {leftPosition: leftPosition, topPosition: topPosition};
    },
    
    _expandVertically: function(tooltip, tooltipWidth, tooltipHeight, anchorPosition, anchorWidth, viewportWidth, viewportHeight, additionalShiftHorizontal, additionalSpace) {
    	var topPosition = additionalSpace;
    	var leftPosition;
		if(tooltipHeight > viewportHeight - 2 * additionalSpace) {
			BAL.setElementHeight(tooltip, viewportHeight - 2 * additionalSpace);
		}
		var leftSpace = anchorPosition.left - additionalShiftHorizontal;
		var rightSpace = viewportWidth - anchorPosition.left - anchorWidth - additionalShiftHorizontal;
		if(leftSpace > rightSpace) {
			leftPosition = additionalSpace;
			BAL.setElementWidth(tooltip, leftSpace - additionalSpace);
		} else {
			leftPosition = anchorPosition.left + additionalShiftHorizontal;
			BAL.setElementWidth(tooltip, rightSpace - additionalSpace);
		}
    	return {leftPosition: leftPosition, topPosition: topPosition};
    },
    
    showMenu: function(elementID, contentID, argObj) {
        var element = document.getElementById(elementID);
        var cancelClosing = function() {
	        if (element.timeoutID != null) {
	            window.clearTimeout(element.timeoutID);
	            element.timeoutID = null;
	        }
	    };
        if (element.timeoutID != null) {
            cancelClosing();
            return;
        }
        if (argObj == null) {
            argObj = new Object();
        }
        argObj.element = element;
        var content = document.getElementById(contentID);
        var oldParent = content.parentNode;
        var nextElementSibling = BAL.DOM.getNextElementSibling(content);

        var remove = function() {
        	
        	// hide the content before remove it (see #6369)
        	content.style.display = 'none';
        	
            document.body.removeChild(content);
            if (nextElementSibling == null) {
                oldParent.appendChild(content);
            } else {
                oldParent.insertBefore(content, nextElementSibling);
            }
            element.timeoutID = null;
            BAL.removeEventListener(element, 'mouseout', delayedHideFunction);
            BAL.removeEventListener(element, 'click', delayedHideFunction);
            BAL.removeEventListener(element, 'DOMNodeRemovedFromDocument', delayedHideFunction);
            
            BAL.removeEventListener(content, 'mouseout', delayedHideFunction);
            BAL.removeEventListener(content, 'click', delayedHideFunction);
            BAL.removeEventListener(content, 'mouseover', delayedHideFunction);
        };
        oldParent.removeChild(content);
        document.body.appendChild(content);
        argObj.content = content;
        PlaceDialog.placeDialog(argObj);
        var delayedHideFunction = function() {
			if (element.timeoutID == null) {
				element.timeoutID = window.setTimeout(remove, 100);
			}
        };
        BAL.addEventListener(element, 'mouseout', delayedHideFunction);
        BAL.addEventListener(element, 'click', delayedHideFunction);
        BAL.addEventListener(element, 'DOMNodeRemovedFromDocument', delayedHideFunction);
        
        BAL.addEventListener(content, 'mouseout', delayedHideFunction);
        BAL.addEventListener(content, 'click', delayedHideFunction);
        BAL.addEventListener(content, 'mouseover', cancelClosing);
    },
    
    openAtElement: function(elementId, controlId, hPos, vPos) {
		var popupRequestor = document.getElementById(elementId);
		var popupContent = services.ajax.topWindow.document.getElementById(controlId);
		
		return this.placeDialog({ element: popupRequestor, content: popupContent, hPos: hPos, vPos: vPos});
	},
	
	openAtPosition: function(controlId, anchorX, anchorY) {
		var popupContent = services.ajax.topWindow.document.getElementById(controlId);

		return this.placeDialog({ content: popupContent, preferBelow: true, anchorX: anchorX, anchorY: anchorY});
	},
    
    setSize: function(contentId, hasTitleBar, popupTitleHeight) {
		var contentArea = document.getElementById(contentId);
		
		var viewportWidth = BAL.getViewportWidthOfWindowObject(services.ajax.topWindow);
		var viewportHeight = BAL.getViewportHeightOfWindowObject(services.ajax.topWindow);
	
		var contentAreaWidth = BAL.getElementWidth(contentArea);
		if (contentAreaWidth < viewportWidth) {
			BAL.setElementWidth(contentArea, contentAreaWidth);
		} else {
			BAL.setElementWidth(contentArea, viewPortWidth);
		}
	
		if (hasTitleBar) {
			var availableHeight = viewportHeight - popupTitleHeight;
		} else {
			var availableHeight = viewportHeight;
		}
		
		var contentAreaHeight = BAL.getElementHeight(contentArea);
		if (contentAreaHeight > availableHeight) {
			BAL.setElementHeight(contentArea, availableHeight);
		}
    }
};

class PositionStrategy {
  constructor(viewportWidth, viewportHeight, anchorX, anchorY, anchorWidth, anchorHeight, additionalShiftHorizontal, additionalShiftVertical, dialogWidth, dialogHeight) {
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
    this.anchorX = anchorX;
    this.anchorY = anchorY;
    this.anchorWidth = anchorWidth;
    this.anchorHeight = anchorHeight;
    this.additionalShiftHorizontal = additionalShiftHorizontal;
    this.additionalShiftVertical = additionalShiftVertical;
    this.dialogWidth = dialogWidth;
    this.dialogHeight = dialogHeight;
  }
  
  initHPos(hPos) {
  	this.setHPos(hPos);
  	if (this.horizontalClash()) {
	  	this.setHPos(this.switchHPos(hPos));
	  	if (this.horizontalClash()) {
    		this.leftPosition = this.viewportWidth / 2 - this.dialogWidth / 2;
	    	this.shiftBelow = true;
	  	}
  	}
  }
  
  initVPos(vPos) {
  	this.setVPos(vPos);
  	if (this.verticalClash()) {
	  	this.setVPos(this.switchVPos(vPos));
	  	if (this.verticalClash()) {
        	this.topPosition = this.viewportHeight / 2 - this.dialogHeight / 2;
	  	}
  	}
  }
  
  switchHPos(hPos) {
    if (hPos == "LEFT_ALIGNED") {
    	return "RIGHT_ALIGNED";
    }
    else if (hPos == "RIGHT_ALIGNED") {
    	return "LEFT_ALIGNED";
    }
    else if (hPos == "TO_THE_RIGHT") {
    	return "TO_THE_LEFT";
    }
    else if (hPos == "TO_THE_LEFT") {
    	return "TO_THE_RIGHT";
    }
    else {
    	throw "Invalid hPos: " + hPos;
    }
  }
  
  switchVPos(vPos) {
    if (vPos == "BELOW") {
    	return "ABOVE";
    }
    else if (vPos == "ABOVE") {
    	return "BELOW";
    }
    else {
    	throw "Invalid hPos: " + hPos;
    }
  }
  
  setHPos(hPos) {
    if (hPos == "LEFT_ALIGNED") {
    	this.leftPosition = this.anchorX + this.additionalShiftHorizontal;
    	this.shiftBelow = true;
    }
    else if (hPos == "RIGHT_ALIGNED") {
    	this.leftPosition = this.anchorX + this.anchorWidth - this.dialogWidth - this.additionalShiftHorizontal;
    	this.shiftBelow = true;
    }
    else if (hPos == "TO_THE_RIGHT") {
    	this.leftPosition = this.anchorX + this.anchorWidth + this.additionalShiftHorizontal;
    	this.shiftBelow = false;
    }
    else if (hPos == "TO_THE_LEFT") {
    	this.leftPosition = this.anchorX - this.dialogWidth - this.additionalShiftHorizontal;
    	this.shiftBelow = false;
    }
    else {
    	throw "Invalid hPos: " + hPos;
    }
  }
  
  setVPos(vPos) {
    if (vPos == "BELOW") {
        this.topPosition = this.anchorY + this.additionalShiftVertical + (this.shiftBelow ? this.anchorHeight : 0);
    }
    else if (vPos == "ABOVE") {
        this.topPosition = this.anchorY - this.dialogHeight - this.additionalShiftVertical + (this.shiftBelow ? 0 : this.anchorHeight);
    }
    else {
    	throw "Invalid vPos: " + vPos;
    }
  }
  
  horizontalClash() {
  	return this.leftPosition < 0 || this.leftPosition + this.dialogWidth > this.viewportWidth;
  }
  
  verticalClash() {
  	return this.topPosition < 0 || this.topPosition + this.dialogHeight > this.viewportHeight;
  }
}
