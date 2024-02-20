/**
 * Access manager to invisible container elements at GUI.
 */

services.viewport = {
	
	scrollSpeed : 300, /* px/s */
	
	inactiveScrollButtonClass : "inactive",
	
	scrollEventListenerAdded : false,

	currentScrollOffset : new Object(),
	
	initScrollingDisplay : function(viewportReferences) {
		services.layout._applyLayout();
		var self = services.viewport;
		if (self.isViewportTooSmall(viewportReferences)) {
			if (self.areScrollButtonsInvisible(viewportReferences)) {
				services.layout.addLayoutFunction(self
						.getFadeScrollButtonsFunction(viewportReferences, true));
			}
			services.layout.addPostLayoutFunction(self
					.getScrollOffsetAdjustmentFunction(viewportReferences));
		} else if (self.areScrollButtonsVisible(viewportReferences)) {
			services.layout.addLayoutFunction(self
					.getFadeScrollButtonsFunction(viewportReferences, false));
			services.layout.addPostLayoutFunction(self
					.getResetScrollPositionFunction(viewportReferences));
		}
	},
	
	initMenuDisplay: function(viewportReferences) {
		var self = services.viewport;
		var viewportWidth = self.getParentWidth(viewportReferences);
		var buttonsHidden = self.isViewportTooSmall(viewportReferences, viewportWidth);
		if (buttonsHidden) {
			if(self.isMenuDialogOpenerButtonInvisible(viewportReferences)) {
				var layoutFunction = self.getFadeMenuDialogOpenerFunction(viewportReferences, true);
				services.layout.addLayoutFunction(layoutFunction);
				layoutFunction.call();
			}
		} else if (self.isMenuDialogOpenerButtonVisible(viewportReferences)) {
			var layoutFunction = self.getFadeMenuDialogOpenerFunction(viewportReferences, false);
			services.layout.addLayoutFunction(layoutFunction);
			layoutFunction.call();
		}
		
		var scrollContainerWidth = viewportWidth;
		
		services.layout.addLayoutFunction(function() {
			var buttonScrollContainer = self.getScrollContainer(viewportReferences);
			BAL.setElementWidth(buttonScrollContainer, scrollContainerWidth);
		});
		
		var elementContainerLeft = BAL.getElementX(self.getElementContainer(viewportReferences));
		var elementContainerWidth = self.getElementContainerWidth(viewportReferences);
		var spaceHolderContainer = self.getSpaceHolderContainer(viewportReferences);
		var scrollPosition = elementContainerLeft + elementContainerWidth + parseInt(BAL.getComputedStyle(spaceHolderContainer, "paddingRight")) - scrollContainerWidth;
		if (buttonsHidden) {
			var hiddenButtonCount = self.calculateHiddenButtonCount(viewportReferences, scrollPosition);
			var firstVisibleButton = self.getFirstVisibleElement(viewportReferences, hiddenButtonCount);
			var buttonX;
			if(firstVisibleButton != null) {
				buttonX = BAL.getElementX(firstVisibleButton);
			} else {
				buttonX = 0;
			}
			
			services.layout.addLayoutFunction(function() {
				self.updateHiddenButtonState(viewportReferences, hiddenButtonCount);
				
				var menuDialogOpener = self.getMenuDialogOpenerButton(viewportReferences);
				if(firstVisibleButton != null) {
					var buttonPositionToParent = buttonX - scrollPosition;
					var menuDialogOpenerPositionX = viewportWidth - buttonPositionToParent; /*5 is randomely chosen adjustment*/;
					menuDialogOpener.style.right = menuDialogOpenerPositionX + "px";
				} else {
					menuDialogOpener.style.right = buttonX + "px";
				}
			});
			
			services.layout.addPostLayoutFunction(function() {
				self.scrollTo(viewportReferences, scrollPosition);
			});
			
			
			services.viewport.updateServerState(hiddenButtonCount, viewportReferences);
		} else {
			var firstElement = self.getFirstElement(viewportReferences);
			services.layout.addPostLayoutFunction(function() {
				self.scrollTo(viewportReferences, scrollPosition);
				self.showAllAfter(viewportReferences, firstElement);
			});
			services.viewport.updateServerState(0, viewportReferences);
		}
		
		if(services.layout.isIE9) {
			// Quirks to force GUI update in IE9, #10215
			services.layout.addPostLayoutFunction(function() {
				var elementContainer = self.getElementContainer(viewportReferences);
				BAL.getElementWidth(elementContainer);
				BAL.getElementHeight(elementContainer);
			});
		}
	},
	
	getParentWidth: function(viewportReferences) {
		if (services.layout.parentLayoutInformation != null) {
			return services.layout.parentLayoutInformation.effectiveWidth;
		} else {
			var barControl = document.getElementById(viewportReferences.controlID);
			return BAL.getEffectiveWidth(barControl);
		}
	},
	
	adjustContainerScrollPosition: function(viewportReferences, newSelection) {
		var element = document.getElementById(viewportReferences.controlID + "-" + newSelection);
		var elementOffsetX = BAL.getElementX(element);
		var elementWidth = BAL.getFullElementWidth(element);
		var scrollContainer = services.viewport.getScrollContainer(viewportReferences);
		var scrollContainerWidth = BAL.getElementWidth(scrollContainer);
		var scrollContainerScrollOffset = BAL
				.getScrollLeftElement(scrollContainer);
		var singleStep = true;

		if (elementOffsetX < scrollContainerScrollOffset) {
			this.scrollLeft(viewportReferences, singleStep);
		} else {
			var elementRightBorder = elementOffsetX + elementWidth;
			var scrollContainerRightBorder = scrollContainerScrollOffset
					+ scrollContainerWidth;
			if (elementRightBorder > scrollContainerRightBorder) {
				this.scrollRight(viewportReferences, singleStep);
			} else {
				this.currentScrollOffset[viewportReferences.controlID] = scrollContainerScrollOffset;
			}
		}
	},

	isViewportTooSmall: function(viewportReferences, precomputedViewportWidth) {
		var self = services.viewport;
		var viewport = self.getViewport(viewportReferences);
		var viewportWidth = precomputedViewportWidth != null ? precomputedViewportWidth : BAL.getElementWidth(viewport);
		var rightContent = self.getAdditionalRightContent(viewportReferences);
		var rightContentSize = rightContent != null ? BAL.getFullElementWidth(rightContent) : 0;
		return viewportWidth - rightContentSize  < self.getElementContainerWidth(viewportReferences);
	},
	
	getElementContainerWidth: function(viewportReferences) {
		var self = services.viewport;
		var elementSizeContainer = self.getElementSizeContainer(viewportReferences);
		return BAL.getElementWidth(elementSizeContainer);
	},

	areScrollButtonsInvisible : function(viewportReferences) {
		var leftScroller = services.viewport.getLeftScrollButton(viewportReferences);
		return services.viewport.isMarkedAsInvisible(leftScroller);
	},
	
	isMarkedAsInvisible: function(element) {
		return !this.isMarkedAsVisible(element);
	},

	areScrollButtonsVisible : function(viewportReferences) {
		var leftScroller = services.viewport
				.getLeftScrollButton(viewportReferences);
		return services.viewport.isMarkedAsVisible(leftScroller);
	},
	
	isMarkedAsVisible: function(element) {
		return BAL.DOM.containsClass(element, "visible");
	},
	
	markAsVisible: function(element) {
		BAL.DOM.addClass(element, "visible");
	},
	
	markAsInvisible: function(element) {
		BAL.DOM.removeClass(element, "visible");
	},
	
	setVisibilityPropertyTo: function(element, isVisible) {
		element.isVisible = isVisible;
	},
	
	isMenuDialogOpenerButtonInvisible: function(viewportReferences) {
		var menuDialogOpenerButton = services.viewport.getMenuDialogOpenerButton(viewportReferences);
		return services.viewport.isMarkedAsInvisible(menuDialogOpenerButton);
	},
	
	isMenuDialogOpenerButtonVisible: function(viewportReferences) {
		var menuDialogOpenerButton = services.viewport.getMenuDialogOpenerButton(viewportReferences);
		return services.viewport.isMarkedAsVisible(menuDialogOpenerButton);
	},

	getScrollOffsetAdjustmentFunction : function(viewportReferences) {
		var self = services.viewport;

		if (self.isScrollContainerOverScrolled(viewportReferences)) {
			return self.getRemoveOverScrollingFunction(viewportReferences);
		} else if (self.isPreviousScrollPositionRequested(viewportReferences)) {
			return self.getRestorePreviousScrollPositionFunction(viewportReferences);
		} else {
			return self.getMakeSelectedElementVisibleScrollingFunction(viewportReferences);
		}
	},
	
	isPreviousScrollPositionRequested : function(viewportReferences) {
		var self = services.viewport;
		return self.hasNoSelectedElement(viewportReferences) ||
			   self.isSelectedElementVisibleAtScrollOffset(viewportReferences);
	},
	
	hasNoSelectedElement : function (viewportReferences) {
		var self = services.viewport;
		return self.getSelectedElement(viewportReferences) == null;
	},

	isSelectedElementVisibleAtScrollOffset : function(viewportReferences) {
		var self = services.viewport;
		var scrollContainer = self.getScrollContainer(viewportReferences);
		var selectedElement = self.getSelectedElement(viewportReferences);
		var leftElementBorder = BAL.getElementX(selectedElement);
		var rightElementBorder = leftElementBorder + BAL.getFullElementWidth(selectedElement);
		var leftVisibleBorder = self.getCurrentScrollOffset(viewportReferences);
		var rightVisibleBorder = leftVisibleBorder
				+ BAL.getElementWidth(scrollContainer);
		return leftVisibleBorder <= leftElementBorder
				&& rightElementBorder <= rightVisibleBorder;
	},

	getCurrentScrollOffset : function(viewportReferences) {
		var offset = this.currentScrollOffset[viewportReferences.controlID];
		if (offset != null) {
			return offset;
		} else {
			return 0;
		}
	},

	isScrollContainerOverScrolled : function(viewportReferences) {
		var self = services.viewport;
		var scrollContainer = self.getScrollContainer(viewportReferences);
		var elementContainerWidth = self.getElementContainerWidth(viewportReferences);
		var scrollContainerScrollOffset = BAL
				.getScrollLeftElement(scrollContainer);
		var scrollContainerWidth = BAL.getElementWidth(scrollContainer);
		return (elementContainerWidth - scrollContainerScrollOffset) < scrollContainerWidth;
	},

	getRemoveOverScrollingFunction : function(viewportReferences) {
		return function() {
			var self = services.viewport;
			var scrollContainer = self.getScrollContainer(viewportReferences);
			var elementContainerWidth = self.getElementContainerWidth(viewportReferences);
			var scrollContainerWidth = BAL.getElementWidth(scrollContainer);
			var scrollOffset = elementContainerWidth - scrollContainerWidth;
			self.currentScrollOffset[viewportReferences.controlID] = scrollOffset;
			BAL.setScrollLeftElement(scrollContainer, scrollOffset);
		};
	},

	getResetScrollPositionFunction : function(viewportReferences) {
		return function() {
			var scrollContainer = services.viewport
					.getScrollContainer(viewportReferences);
			BAL.setScrollLeftElement(scrollContainer, 0);
		};
	},

	getRestorePreviousScrollPositionFunction : function(viewportReferences) {
		var self = this;
		return function() {
			var scrollContainer = services.viewport
					.getScrollContainer(viewportReferences);
			var lastScrollOffset = services.viewport.getCurrentScrollOffset
					.call(self, viewportReferences);
			BAL.setScrollLeftElement(scrollContainer, lastScrollOffset);
		};
	},

	getMakeSelectedElementVisibleScrollingFunction : function(viewportReferences) {
		var self = this;
		return function() {
			var context = services.viewport;
			var scrollContainer = context.getScrollContainer(viewportReferences);
			var selectedElement = context.getSelectedElement(viewportReferences);
			var leftElementBorder = BAL.getElementX(selectedElement);
			var rightElementBorder = leftElementBorder
					+ BAL.getFullElementWidth(selectedElement);
			var leftVisibleBorder = context.getCurrentScrollOffset.call(
					self, viewportReferences);
			var scrollContainerWidth = BAL.getElementWidth(scrollContainer);
			var scrollOffset;
			if (leftElementBorder < leftVisibleBorder) {
				scrollOffset = leftElementBorder;
			} else {
				scrollOffset = rightElementBorder - scrollContainerWidth;
			}
			self.currentScrollOffset[viewportReferences.controlID] = scrollOffset;
			BAL.setScrollLeftElement(scrollContainer, scrollOffset);
		};
	},

	getFadeScrollButtonsFunction : function(viewportReferences, showButtons) {
		return function() {
			var self = services.viewport;
			var leftScroller = self.getLeftScrollButton(viewportReferences);
			var rightScroller = self.getRightScrollButton(viewportReferences);
			if (showButtons) {
				self.markAsVisible(leftScroller);
				leftScroller.style.display = "block";
				rightScroller.style.display = "block";
			} else {
				leftScroller.style.display = "none";
				rightScroller.style.display = "none";
				self.markAsInvisible(leftScroller);
			}
		};
	},

	getFadeMenuDialogOpenerFunction : function(viewportReferences, visible) {
		return function() {
			var self = services.viewport;
			var menuDialogOpener = self.getMenuDialogOpenerButton(viewportReferences);
			if (visible) {
				self.markAsVisible(menuDialogOpener);
			} else {
				self.markAsInvisible(menuDialogOpener);
			}
		};
	},
	
	scrollTo : function(viewportReferences, buttonContainerPosition) {
		var self = services.viewport;
		var buttonScrollContainer = self.getScrollContainer(viewportReferences);
		BAL.setScrollLeftElement(buttonScrollContainer, buttonContainerPosition);
	},
	
	initScrollButtonChanger : function(viewportReferences) {
		var self = services.viewport;
		if(self._hasTabs(viewportReferences)) {
			var leftButton = self.getLeftScrollButtonImage(viewportReferences);
			var rightButton = self.getRightScrollButtonImage(viewportReferences);
			BAL.DOM.removeClass(leftButton, self.inactiveScrollButtonClass);
			BAL.DOM.removeClass(rightButton, self.inactiveScrollButtonClass);
			leftButton.showButton = true;
			rightButton.showButton = true;
			var scrollContainer = self.getScrollContainer(viewportReferences);
			BAL.addEventListener(scrollContainer, "scroll", function() {
				self._updateButtonVisibility(viewportReferences);
			});	
			self._updateButtonVisibility(viewportReferences);
		}
	},
	
	_hasTabs : function(viewportReferences) {
		var self = services.viewport;
		var firstElement = self.getFirstElement(viewportReferences);
		return firstElement != null;
	},
	
	_updateButtonVisibility : function(viewportReferences) {
		var self = services.viewport;
		var scrollContainer = self.getScrollContainer(viewportReferences);
		var scrollContainerWidth = BAL.getElementWidth(scrollContainer);
		var firstElement = self.getFirstElement(viewportReferences);
		var lastElement = self.getLastElement(viewportReferences);
		var firstElementPosition = BAL.getElementX(firstElement);
		var scrollContainerRightBorderScrollOffset = BAL.getElementX(lastElement) + BAL.getFullElementWidth(lastElement);
		var scrollContainerLeftBorderScrollOffset = Math.max(scrollContainerRightBorderScrollOffset
				- scrollContainerWidth, 1);
		var scrollPosition = self.getCurrentScrollOffset(viewportReferences);
		var leftButton = self.getLeftScrollButtonImage(viewportReferences);
		var rightButton = self.getRightScrollButtonImage(viewportReferences);
		if(scrollPosition <= firstElementPosition) {
			if(leftButton.showButton == true) {
				BAL.DOM.addClass(leftButton, self.inactiveScrollButtonClass);
				leftButton.showButton = false;
			}
		} else {
			if(leftButton.showButton == false) {
				BAL.DOM.removeClass(leftButton, self.inactiveScrollButtonClass);
				leftButton.showButton = true;
			}
		}
		if(scrollPosition >= scrollContainerLeftBorderScrollOffset) {
			if(rightButton.showButton == true) {
				BAL.DOM.addClass(rightButton, self.inactiveScrollButtonClass);
				rightButton.showButton = false;
			}
		} else {
			if(rightButton.showButton == false) {
				BAL.DOM.removeClass(rightButton, self.inactiveScrollButtonClass);
				rightButton.showButton = true;
			}
		} 
	},
	
	_scrollInit : function(viewportReferences, targetScrollPosition, scrollDirection, loopingAnimation) {
		var self = services.viewport;
		var scrollContainer = self.getScrollContainer(viewportReferences);
		if (scrollContainer.animations == null) {
			scrollContainer.animations = new Array();
		}
		scrollContainer.animations.push(loopingAnimation);
		var animationEnd = function() {
			var continueAnimation = scrollContainer.animations[0];
			scrollContainer.animations.shift();
			if(continueAnimation == true) {
				if(scrollDirection == "left") {
					self.scrollLeft(viewportReferences, false);
				} else {
					self.scrollRight(viewportReferences, false);
				}
			} 
		};
		var scrollDistance = Math.abs(targetScrollPosition - self.getCurrentScrollOffset(viewportReferences));
		var scrollTime = scrollDistance / self.scrollSpeed * 1000;
		self.currentScrollOffset[viewportReferences.controlID] = targetScrollPosition;
		BAL.setScrollLeftElementAnimated(scrollContainer, targetScrollPosition, scrollTime, animationEnd);
	},
	
	scrollFinish : function(viewportReferences, scrollElement) {
		var self = services.viewport;
		var scrollContainer = self.getScrollContainer(viewportReferences);
		var animations = scrollContainer.animations;
		if(animations != null) {
			for(var i = 0; i < animations.length; i++) {
				if(animations[i] == true) {
					animations[i] = false;
					break;
				}
			}
		}
		/* Removes the EventListeners which were added either in scrollLeft and scrollRight */
		self.scrollEventListenerAdded = false;
		scrollElement.removeEventListener("mouseup", self.scrollFinishHandler(viewportReferences, this));
		scrollElement.removeEventListener("mouseout", self.scrollFinishHandler(viewportReferences, this));
		
		return false;
	},
	
	scrollLeft : function(viewportReferences, singleStep) {
		var self = services.viewport;
		var scrollContainerScrollOffset = self.getCurrentScrollOffset(viewportReferences);
		var element = self.getFirstElement(viewportReferences);
		var previousElement = null;
		var scrollLeftID = viewportReferences.scrollLeftButton;
		var scrollLeftElement = document.getElementById(scrollLeftID);

		if (!self.scrollEventListenerAdded) {
			self.scrollEventListenerAdded = true;
		  	scrollLeftElement.addEventListener("mouseup", self.scrollFinishHandler(viewportReferences, scrollLeftElement));
		  	scrollLeftElement.addEventListener("mouseout", self.scrollFinishHandler(viewportReferences, scrollLeftElement));
		}
		    
		while (element != null
				&& BAL.getElementX(element) < scrollContainerScrollOffset) {
			previousElement = element;
			element = self.getNextElement(element, viewportReferences);
		}

		element = previousElement;
		if (element != null) {
			var scrollOffset = BAL.getElementX(element);
			self._scrollInit(viewportReferences, scrollOffset, "left", !singleStep);
		}

		return false;
	},
	
	scrollRight : function(viewportReferences, singleStep) {
		var self = services.viewport;
		var scrollContainer = self.getScrollContainer(viewportReferences);
		var scrollContainerWidth = BAL.getElementWidth(scrollContainer);
		var scrollContainerLeftBorderScrollOffset = self.getCurrentScrollOffset(viewportReferences);
		var scrollContainerRightBorderScrollOffset = scrollContainerLeftBorderScrollOffset
				+ scrollContainerWidth;
		var element = self.getFirstElement(viewportReferences);
		var scrollRightID = viewportReferences.scrollRightButton;
		var scrollRightElement = document.getElementById(scrollRightID);
		
		if (!self.scrollEventListenerAdded) {
			self.scrollEventListenerAdded = true;
		  	scrollRightElement.addEventListener("mouseup", self.scrollFinishHandler(viewportReferences, scrollRightElement));
		  	scrollRightElement.addEventListener("mouseout", self.scrollFinishHandler(viewportReferences, scrollRightElement));
		}

		while ((element != null)
				&& (BAL.getElementX(element) + BAL.getFullElementWidth(element)) <= scrollContainerRightBorderScrollOffset) {
			element = self.getNextElement(element, viewportReferences);
		}

		if ((element != null)
				&& (BAL.getElementX(element) == scrollContainerLeftBorderScrollOffset)) {
			element = self.getNextElement(element, viewportReferences);
		}

		if (element != null) {
			var elementWidth = BAL.getFullElementWidth(element);
			var elementPosition = BAL.getElementX(element);
			var scrollOffset;
			if (elementWidth < scrollContainerWidth) {
				scrollOffset = elementPosition + elementWidth
						- scrollContainerWidth;
			} else {
				scrollOffset = elementPosition;
			}
			self._scrollInit(viewportReferences, scrollOffset, "right", !singleStep);
		}
		return false;
	},
	
	scrollFinishHandler: function(viewportReferences, scrollElement) {
	  return function() {
	    services.viewport.scrollFinish(viewportReferences, scrollElement);
	  };
	},
	
	calculateHiddenButtonCount: function(viewportReferences, scrollOffset) {
		var self = services.viewport;
		var hiddenButtonCount = 0;
		var firstElement = self.getFirstElement(viewportReferences);
		var currentElement = firstElement;
		while(currentElement != null && BAL.getElementX(currentElement) < scrollOffset + 20) {
			hiddenButtonCount++;
			currentElement = self.getNextElement(currentElement, viewportReferences);
		}
		
		return hiddenButtonCount;
	},
	
	updateHiddenButtonState: function(viewportReferences, hiddenButtonCount) {
		var self = services.viewport;
		var currentElement = self.getFirstElement(viewportReferences);
		for(var i = 0; i < hiddenButtonCount; i++) {
			currentElement.style.visibility = "hidden";
			currentElement = self.getNextElement(currentElement, viewportReferences);
		}
		self.showAllAfter(viewportReferences, currentElement);
	},
	
	getFirstVisibleElement: function(viewportReferences, hiddenButtonCount) {
		var self = services.viewport;
		var currentElement = self.getFirstElement(viewportReferences);
		for(var i = 0; i < hiddenButtonCount; i++) {
			currentElement = self.getNextElement(currentElement, viewportReferences);
		}
		
		return currentElement;
	},
	
	updateServerState: function(hiddenButtonCount, viewportReferences) {
		var self = services.viewport;
		var barControl = document.getElementById(viewportReferences.controlID);
		var currentHiddenButtonCount = BAL.DOM.getNonStandardAttribute(barControl, "data-hidden-button-count");
		if (currentHiddenButtonCount == ("" + hiddenButtonCount)) {
			return;
		}
		BAL.DOM.setNonStandardAttribute(barControl, "data-hidden-button-count", ("" + hiddenButtonCount));
		if(self.hasNoRequestId(barControl)) {
			barControl.requestId = services.ajax.createLazyRequestID();
		}
		services.ajax.executeOrUpdateLazy(barControl.requestId, "dispatchControlCommand", {
			controlCommand : viewportReferences.visibleButtonCountCommand,
			controlID : viewportReferences.controlID,
			hiddenElements : hiddenButtonCount
		});
	},
	
	hasNoRequestId: function(barControl) {
		return barControl.requestId == null;
	},
	
	showAllAfter: function(viewportReferences, currentElement) {
		var self = services.viewport;
		while(currentElement != null) {
			currentElement.style.visibility = "visible";
			currentElement = self.getNextElement(currentElement, viewportReferences);
		}
	},

	getSelectedElement : function(viewportReferences) {
		var self = services.viewport;
		var firstElement = self.getFirstElement(viewportReferences);
		if (self.isSelectedElement(firstElement, viewportReferences)) {
			return firstElement;
		} else {
			return self.getFollowingElementInOrder(firstElement,
					BAL.DOM.getNextElementSibling, self.isSelectedElement,
					viewportReferences);
		}
	},

	getFirstElement : function(viewportReferences) {
		var self = services.viewport;
		var elementContainer = self.getElementContainer(viewportReferences);
		var firstElementCandidate = BAL.DOM.getFirstElementChild(elementContainer);
		if (firstElementCandidate == null) {
			return null;
		} else if (self.isVisibleElement(firstElementCandidate, viewportReferences)) {
			return firstElementCandidate;
		} else {
			return self.getNextElement(firstElementCandidate, viewportReferences);
		}
	},
	
	getLastElement : function(viewportReferences) {
		var self = services.viewport;
		var elementContainer = self.getElementContainer(viewportReferences);
		var lastElementCandidate = BAL.DOM.getLastElementChild(elementContainer);
		if (lastElementCandidate == null) {
			return null;
		} else if (self.isVisibleElement(lastElementCandidate, viewportReferences)) {
			return lastElementCandidate;
		} else {
			return self.getPreviousElement(lastElementCandidate, viewportReferences);
		}
	},

	getNextElement : function(currentElement, viewportReferences) {
		return services.viewport.getFollowingElementInOrder(currentElement,
				BAL.DOM.getNextElementSibling,
				services.viewport.isVisibleElement,
				viewportReferences);
	},

	getPreviousElement : function(currentElement, viewportReferences) {
		return services.viewport.getFollowingElementInOrder(currentElement,
				BAL.DOM.getPreviousElementSibling,
				services.viewport.isVisibleElement,
				viewportReferences);
	},

	getFollowingElementInOrder : function(currentElement, followingElementFunction,
			elementAcceptanceFilter, viewportReferences) {
		var followingElement = followingElementFunction.call(BAL.DOM, currentElement);
		while (followingElement != null && !elementAcceptanceFilter(followingElement, viewportReferences)) {
			followingElement = followingElementFunction.call(BAL.DOM, followingElement);
		}
		return followingElement;
	},

	isVisibleElement : function(element, viewportReferences) {
		var self = services.viewport;
		return BAL.DOM.containsClass(element,viewportReferences.visibleElement)
				|| self.isSelectedElement(element, viewportReferences);
	},

	isSelectedElement : function(element, viewportReferences) {
		return BAL.DOM.containsClass(element, viewportReferences.activeElement);
	},

	getLeftScrollButton : function(viewportReferences) {
		return document.getElementById(viewportReferences.scrollLeftButton);
	},

	getLeftScrollButtonImage : function(viewportReferences) {
		return document.getElementById(viewportReferences.scrollLeftButtonImage);
	},

	getRightScrollButton : function(viewportReferences) {
		return document.getElementById(viewportReferences.scrollRightButton);
	},

	getRightScrollButtonImage : function(viewportReferences) {
		return document.getElementById(viewportReferences.scrollRightButtonImage);
	},

	getAdditionalRightContent : function(viewportReferences) {
		return document.getElementById(viewportReferences.additionalRightContent);
	},
	
	getMenuDialogOpenerButton : function(viewportReferences) {
		return document.getElementById(viewportReferences.buttonDialogOpener);
	},
	
	getMenuDialogOpenerButtonImage : function(viewportReferences) {
		return document.getElementById(viewportReferences.buttonDialogOpenerImage);
	},

	getViewport : function(viewportReferences) {
		return document.getElementById(viewportReferences.viewport);
	},

	getScrollContainer : function(viewportReferences) {
		return document.getElementById(viewportReferences.scrollContainer);
	},

	getElementContainer : function(viewportReferences) {
		return document.getElementById(viewportReferences.elementContainer);
	},

	getElementSizeContainer : function(viewportReferences) {
		var elementSizeContainer = document.getElementById(viewportReferences.elementSizeContainer);
		if (elementSizeContainer != null) {
			return elementSizeContainer;
		} else {
			return services.viewport.getElementContainer(viewportReferences);
		}
	},
	
	getSpaceHolderContainer: function(viewportReferences) {
		return document.getElementById(viewportReferences.buttonSpaceHolderContainer);
	},
	
	/* 
	*	Makes Tabbar scrollable with mouse drag and drop, 
	*	gui-arrows on each end of the tabbar and mouse wheel.
	*	Parameter: TabBarControl.
	*/
	tabScrollBehaviour: function(element) {
		services.layout._applyLayout();
		const elementID = element.id;
		const tabLayout = document.getElementById(elementID);
		const scrollContainer = tabLayout.querySelector('.tlTabScrollContainer');
		const tabBar = scrollContainer.parentElement;
		const scrollLeftButton = tabLayout.querySelector('.tlTabScrollLeft');
		const scrollRightButton = tabLayout.querySelector('.tlTabScrollRight');
		const rightContent = tabLayout.querySelector('.tlRightContent');
		let isScrolling = false;
		const scrollAmount = 70;
		const totalContentWidth = scrollContainer.scrollWidth + rightContent.offsetWidth;
		
		
		if (tabBar.clientWidth >= totalContentWidth) {
	        scrollLeftButton.style.display = 'none';
	        scrollRightButton.style.display = 'none';
	    } else {
	        scrollLeftButton.style.display = 'flex';
	        scrollRightButton.style.display = 'flex';
	    }
	    
		function scrollLeft() {
			scrollContainer.scrollBy({
				left: -scrollAmount,
				behavior: 'smooth'
			});
			if (isScrolling) {
				setTimeout(scrollLeft, 100);
			}
		}
		
		function scrollRight() {
			scrollContainer.scrollBy({
				left: scrollAmount,
				behavior: 'smooth'
			});
			if (isScrolling) {
				setTimeout(scrollRight, 100);
			}
		}
	    	    
	    scrollLeftButton.addEventListener('mousedown', function() {
	    	isScrolling = true;
	    	scrollLeft();
		});
		
		scrollRightButton.addEventListener('mousedown', function() {
			isScrolling = true;
			scrollRight();
		});
		
		scrollLeftButton.addEventListener('mouseup', function() {
			isScrolling = false;
		});
		
		scrollRightButton.addEventListener('mouseup', function() {
			isScrolling = false;
		});
		
		scrollContainer.addEventListener("mousedown", function(e) {
			isScrolling = true;
	        const initialX = e.clientX;
	        
	        const moveHandler = function(e) {
	        	if (!isScrolling) return;
	        	if (Math.abs(initialX - e.clientX) > 5) {
	        		scrollContainer.classList.add("scrolling");
	        		scrollContainer.scrollLeft -= e.movementX;
	        	}
	        };
	
	        const upHandler = function() {
	        	isScrolling = false;
	        	scrollContainer.classList.remove("scrolling");
	            document.removeEventListener("mousemove", moveHandler);
	            document.removeEventListener("mouseup", upHandler);
	        };
	
	        document.addEventListener("mousemove", moveHandler);
	        document.addEventListener("mouseup", upHandler);
	    });
		
		// Makes scolling with CTRL or SHIFT + mouse wheel possible.
		scrollContainer.addEventListener('wheel', function(event) {
            scrollContainer.scrollLeft += event.deltaY;
        });
        
	},
	
	/* Gets a value when a tab is clicked. See services.form.TabBarControl.handleClick */
	savedTabScrollOffset: null,
	
	/* 
	*	Ensures that the selected tab is being displayed in the
	*	tab container and not hidden outside the tab bar scope.
	*	Especially important for when the page is being refreshed.
	*	Parameter: TabBarControl.
	*/
	ensureTabVisible: function(element) {
		const elementID = element.id;
		const tabLayout = document.getElementById(elementID);
		const tabContainer = tabLayout.querySelector('.tlTabScrollContainer');
		const activeTab = tabContainer.querySelector('.activeTab');
		const scrollLeftButton = tabLayout.querySelector('.tlTabScrollLeft');
		const scrollRightButton = tabLayout.querySelector('.tlTabScrollRight');
		/* 
		*	Code has to be skipped when the element
		*	has no tabbar, for example a mega menu.
		*/
		if (activeTab) {
			if(services.viewport.savedTabScrollOffset !== null) {
	        	tabContainer.scrollLeft = services.viewport.savedTabScrollOffset;			
			}
			const containerScrollLeft = tabContainer.scrollLeft;
	        const containerWidth = tabContainer.clientWidth;
	        const tabLeft = activeTab.offsetLeft;
	        const tabWidth = activeTab.offsetWidth;
	        const tabRight = tabLeft + tabWidth;
	        const scrollLeftButtonWidth = scrollLeftButton ? scrollLeftButton.offsetWidth : 0;
	        const scrollRightButtonWidth = scrollRightButton ? scrollRightButton.offsetWidth : 0;
	        const visibleLeft = containerScrollLeft + scrollLeftButtonWidth;
	        const visibleRight = visibleLeft + containerWidth - scrollRightButtonWidth;
	
	        if (tabLeft < visibleLeft) {
	            tabContainer.scrollLeft = tabLeft - scrollLeftButtonWidth - 60;
	        }
	        else if (tabRight > visibleRight) {
	            tabContainer.scrollLeft = tabRight - containerWidth;
	        }
		}
	},
	
	/* 
	*	Hides buttons that are no longer visible in the browser or exceeds the width 
	*	of the container, and makes them visible again if the container gets wider again.
	*	Hidden buttons will be displayed in a dropdown button.
	*	Parameter: Button component element.
	*/
	buttonComponentResizing: function(element) {
		const elementID = element.id;
	  	const buttonComponentParent = document.getElementById(elementID);
		const buttonScrollContainer = buttonComponentParent.querySelector('.tlButtonScrollContainer');
		const buttons = Array.from(buttonScrollContainer.querySelectorAll('.containerButton:not(.tlDropdownContent .containerButton)'));
		const buttonsSize = buttons.length;
		let buttonsClone = [];
	  	let currButton;
	  	let dropdown = buttonScrollContainer.querySelector('.tlDropdown');
	  	let dropdownButton = buttonScrollContainer.querySelector('.tlDropdownButton');
	  	let dropdownContent = buttonScrollContainer.querySelector('.tlDropdownContent');
	  	let dropdownHtml = '';
	  	dropdown.style.display = 'none';
	  	dropdownContent.style.display = 'none';
	  	for (let i = 0; i < buttonsSize; i++) {
	    	buttonsClone[i] = buttons[i].cloneNode(true);
	    	buttons[i].style.display = 'inline-block';
	  	}
	  	
	  	// Migrates the hidden buttons into the dropdown content.
	  	for (let i = 0; i < buttonsSize; i++) {
	    	if(buttons[i].offsetLeft < 1) {
	    		dropdown.style.display = 'inline-block';
	      		currButton = buttonsClone[i].firstElementChild;
	      		currButton.classList = "tlInDropdown"
	      		dropdownHtml += currButton.outerHTML;
	      		buttons[i].style.display = 'none';
	    	}
	  	}
	  	dropdownContent.innerHTML = dropdownHtml;
	},
	
	/* 
	*	Opens the dropdown with all the hidden buttons.
	*
	*	Parameter: Dropdown button.
	*/
	openButtonComponentsDropdown: function(dropdownButton) {
		const dropdownContent = dropdownButton.nextElementSibling;
	  	if (dropdownContent.style.display === 'none') {
	    	dropdownContent.style.display = 'block';
	    	// Finds the button with the biggest width. Dropdown content's width depends on that,
	    	// because normally an element grows in the right direction but this dropdown menu has 
	    	// to grow in the left direction.
	    	let maxWidth = 0;
	    	const labels = dropdownContent.querySelectorAll('a .tlButtonLabel');
			for (let i = 0; i < labels.length; i++) {
				const label = labels[i];
			  	let width = label.getBoundingClientRect().width;
			  	const nextSibling = label.nextElementSibling;
  			  	if (nextSibling) {
					width += nextSibling.getBoundingClientRect().width;
				  	width += parseFloat(getComputedStyle(nextSibling).getPropertyValue('padding-right'));
  			  	}
			  	maxWidth = Math.max(maxWidth, width);
			}
			const anchors = dropdownContent.querySelectorAll('a');
			const buttonPadding = getComputedStyle(anchors[0]);
			const paddingLeft = parseFloat(buttonPadding.getPropertyValue('padding-left'));
			const paddingRight = parseFloat(buttonPadding.getPropertyValue('padding-right'));
			dropdownContent.style.width = `${maxWidth + paddingLeft + paddingRight}px`;
			const ancestorWithLcHidden = this.findAncestorWithClass(dropdownButton, 'lcHidden', 5);
	        if (ancestorWithLcHidden) {
	            ancestorWithLcHidden.style.overflow = 'visible';
	        }
	  	} else {
	    	dropdownContent.style.display = 'none';
	    	const ancestorWithLcHidden = this.findAncestorWithClass(dropdownButton, 'lcHidden', 5);
	        if (ancestorWithLcHidden) {
	            ancestorWithLcHidden.style.overflow = 'hidden';
	        }
	  	}
	  	// Closes dropdown when user clicks somewhere else outside the dropdown.
	  	document.addEventListener('click', function(event) {
	    	if (!dropdownButton.contains(event.target) && !dropdownContent.contains(event.target)) {
	      		dropdownContent.style.display = 'none';
	    	}
	  	});
	},
	
	findAncestorWithClass: function(element, cssClass, maxHops) {
		let hops = 0;
	    while ((element = element.parentElement) && !element.classList.contains(cssClass) && hops < maxHops) {
	        hops++;
	    }
	    return element.classList.contains(cssClass) ? element : null;
	}
};