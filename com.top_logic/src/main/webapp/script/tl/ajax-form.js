/**
 * <<package>>
 * 
 * 
 */

services.form = {

	Constants : {
		IS_INPUT_CSS_CLASS : "is-input",
		IS_ACTION_CSS_CLASS : "is-action",

		MANDATORY_CLASS : "mandatory",
		ERROR_CLASS : "error",
		NO_ERROR_CLASS : "no-error",
		INVISIBLE_CLASS : "invisible",
		CAN_EDIT_CLASS : "can-edit",
		CANNOT_EDIT_CLASS : "cannot-edit",
		DISABLED_CLASS : "disabled"
	},

	
	sendDroppedFiles: function(ctrlId, command, message){
		services.ajax.execute("dispatchControlCommand", {
			controlCommand : command,
			controlID : ctrlId, 
			message : message
		});
	},
	
	installKeyEventHandler : function(windowId) {
		BAL.installKeyBindingHandler(
			function(event) {
				event = BAL.getEvent(event);
				
				var shift = BAL.hasShiftModifier(event);
				var ctrl = BAL.hasCtrlModifier(event);
				var alt = BAL.hasAltModifier(event);
				var keycode = BAL.getKeyCode(event);
				
				var key = 
					"K" + 
					(shift ? "S" : "") + 
					(ctrl ? "C" : "") + 
					(alt ? "A" : "") + 
					(keycode < 100 ? "0" : "") +  
					(keycode < 10 ? "0" : "") +  
					keycode;
				
				var source = BAL.getEventSource(event);
				var isEscapeKey = BAL.isEscapeKey(keycode);
				var isReturnKey = BAL.isReturnKey(keycode);
				var handleNative = (isEscapeKey || isReturnKey) && !shift && !ctrl && !alt;
				while (source != null && BAL.DOM.isElementNode(source)) {
					var selectors = BAL.DOM.getNonStandardAttribute(source, "data-keys");
					if (selectors != null) {
						if (selectors.indexOf(key) >= 0) {
							services.form.transmitTransientState(event);
							
							var controlID = BAL.DOM.getAttribute(source, "id");
							
							var showWait;
							if (isReturnKey || isEscapeKey) {
								// show wait pane in case of escape (27) and enter (13)
								showWait = true;
							} else {
								showWait = services.ajax.USE_WAIT_PANE_IN_FORMULA;
							}
							
							BAL.eventStopPropagation(event);
							services.ajax.execute("dispatchControlCommand", {
								controlCommand : "keypress",
								controlID : controlID,
								scancode : keycode,
								shift : shift,
								ctrl : ctrl,
								alt : alt
							}, showWait);
							return BAL.cancelEvent(event);
						}
					}
					
					if (handleNative) {
						var tagName = BAL.DOM.getTagName(source);
						if (tagName == "select") {
							return true;
						}
						if(isReturnKey) {
							if (tagName == "textarea" || tagName == "a") {
								// Do not capture return from the context of elements that natively handle return.
								return true;
							}
							if (tagName == "input") {
								var inputType = source.type;
								// special check for text because this occurs mostly.
								if (inputType != "text") {
									if (inputType == "radio" || inputType == "checkbox" || inputType == "image" || inputType == "button") {
										source.click();
										return BAL.cancelEvent(event);
									}
								}
							}
							if (tagName == "button") {
								source.click();
								return BAL.cancelEvent(event);
							}
						}
					}
					source = source.parentNode;
				}
				
				var topWindow = services.ajax.topWindow;
				return topWindow.services.form.BrowserWindowControl.handleKeyDown(event, windowId);
			});
		
		document.addEventListener('contextmenu', services.form.openContextMenu, false);
		
	},
	
	/**
	 * Triggers the command to open the context menu, when a parent of the event source has a context menu.
	 */
	openContextMenu: function(event) {
		var source = BAL.getEventSource(event);
		bubbleUp:
		while (source != null && BAL.DOM.isElementNode(source)) {
			var openBrowserContextMenu = BAL.DOM.getNonStandardAttribute(source, "data-browser-menu");
			if(openBrowserContextMenu != null && openBrowserContextMenu){
				break;
			}
			var contextMenuValue = BAL.DOM.getNonStandardAttribute(source, "data-context-menu");
			if (contextMenuValue != null) {
				var controlID;
				var controlNode = source;
				while (true) {
					if (controlNode == null || !BAL.DOM.isElementNode(controlNode)) {
						break bubbleUp;
					}
					if (BAL.DOM.containsClass(controlNode, "is-control")) {
						controlID = BAL.DOM.getAttribute(controlNode, "id");
						break;
					}
					controlNode = controlNode.parentNode;
				}
				
				BAL.eventStopPropagation(event);
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "open-context-menu",
					controlID : controlID,
					eventPosition : BAL.getAbsoluteEventPosition(event),
					contextMenuValue : contextMenuValue,
					});
				return BAL.cancelEvent(event);
			}
			
			var id = BAL.DOM.getAttribute(source, "id");
			if (id == "pdlgPopupDialogPane") {
				BAL.eventStopPropagation(event);
				var topWindow = services.ajax.topWindow;
				topWindow.services.form.BrowserWindowControl.closeAllPopupDialogs();
				return BAL.cancelEvent(event);
			}
			
			source = source.parentNode;
		}
	},
	
	/**
	 * Triggers all onchange listeners from the source of the given event to the document root.
	 */
	transmitTransientState: function(event) {
		// Give controls a change to save their state before executing the command. Otherwise, 
		// input is not available on the server at the time, the handler is invoked.
		var source = BAL.getEventTarget(event);
		while (source != null) {
			if (source.onchange != null) {
				source.onchange();
			}
			source = source.parentNode;
		}
	},

	disableReturn : function(formName, defaultCommand) {
		var it = new BAL.DescendantIterator(document.forms[formName], function(elem) {
			// All inputs other than TEXTAREA!
				return BAL.DOM.getTagName(elem) == "input" || BAL.DOM.getTagName(elem) == "button";
			});

		var returnHandler = function(event) {
			return services.form.captureReturn(event, defaultCommand);
		};

		if (document.onkeypress == null) {
			document.onkeypress = returnHandler;
		}

		while (it.hasNext()) {
			var element = it.next();
			if (element.onkeypress == null) {
				element.onkeypress = returnHandler;
			}
		}
	},

	captureReturn : function(event, defaultCommand) {
		event = BAL.getEvent(event);

		var keycode = BAL.getKeyCode(event);
		if (keycode != 13) {
			// Only capture return key presses.
			return true;
		}

		var source = BAL.getEventSource(event);

		if (source != null && BAL.DOM.getTagName(source) == "textarea") {
			// Do not capture return in textarea elements.
			return true;
		}

		if (defaultCommand != null) {
			eval(defaultCommand);
		}

		return false;
	},
	
	/**
	 * Send the current value of the given input element, if it has not changed 
	 * from its original value.
	 * 
	 * == Background ==  
	 * Before sending keycode events, the `onchange` handler of the originating 
	 * input element is triggered explicitly. But explicitly triggering the 
	 * `onchange` handler, does not consistently prevent the native change event 
	 * from occurring. This means that at least in Chrome and Opera the `onchange` 
	 * handler is triggered twice, once before the keycode event and once afterwards.
	 *
	 * If the keycode handler updates the UI state, the second event causes havoc 
	 * on the server, since the input control may no longer be there at this time 
	 * (this at happens least in grids when the next row is activated upon RETURN). 
	 *
	 * To prevent the second execution of `onchange` to actually transmit the value 
	 * to the server a second time, all handlers explicitly check, whether the current
	 * value is different from its default value. This enables dropping the second 
	 * request by updating the default value property.
	 */
	sendValueUpdateOnce : function(inputElement, controlID, showWait, serverResponseCallback) {
		var value = inputElement.value;
		var defaultValue = inputElement.defaultValue;
		if (value != defaultValue) {
			services.form.sendValueUpdate(inputElement, controlID,
				value, showWait, serverResponseCallback);
			inputElement.defaultValue = value;
		}
	},

	/**
	 * @param {} inputElement
	 * @param {String} controlID the id of the responsible control
	 * @param {Object} fieldValue the new value of the field
	 * @param {Boolean} showWait whether the waitPane must be shown.
	 */
	sendValueUpdate : function(inputElement, controlID, fieldValue, showWait, serverResponseCallback) {
		// Accumulate multiple events to a single change event. Otherwise, using
		// e.g. the scroll wheel
		// on a select input would produce an event flood. Reducing the amount
		// of events reduces the
		// probability of events that arrive out of order. See Ticket #205.
		var pendingUpdate = inputElement.updateTimer;
		if (pendingUpdate != null) {
			window.clearTimeout(pendingUpdate);
			inputElement.updateTimer = null;
		}
		if (typeof(showWait) === "undefined" || showWait == null) {
			showWait = services.ajax.USE_WAIT_PANE_IN_FORMULA;
		}
		
		var updateFunction = function() {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "valueChanged",
				controlID : controlID,
				value : fieldValue
			}, showWait, serverResponseCallback);
		};

		updateFunction();
	},
	
	_putToDnDCache: function(controlElement, sourceID, targetID, dropability) {
		if(window.tlDnD.cache === undefined) {
			window.tlDnD.cache = new services.util.TwoKeyMap();
		}
		
		window.tlDnD.cache.set(sourceID, targetID, dropability);
	},
	
	_createDragImageElement: function(draggedObjects) {
		var dragImageElement = document.createElement("div");
		dragImageElement.classList.add("dragImage");
		var draggedObjectList = document.createElement("ul");
		dragImageElement.appendChild(draggedObjectList);
		
		for(var i = 0; i < draggedObjects.length; i++) {
			var draggedObjectListItem = document.createElement("li");
			draggedObjectList.appendChild(draggedObjectListItem);
			var draggedObjectImage = BAL.DOM.getAttribute(draggedObjects[i], "data-drag-image");
			draggedObjectListItem.insertAdjacentHTML("beforebegin", draggedObjectImage);
		}
		
		dragImageElement.style.position = "absolute";
		/*
		 * The element from which the image for the drag operation is created should not be visible
		 * to the user. The height is unknown and depends on the number and size of the elements to be dragged. 
		 * Therefore, the element is moved down by the maximal height (i.e. height of the viewport).
		 */
		dragImageElement.style.bottom = "-100vh";
	
		document.body.appendChild(dragImageElement);
		
		return dragImageElement;
	},
	
	ScrollContainerControl : {

		scrollRatio : 0.382 // ca. golden ratio.
		,

		scrollTo : function(controlId, elementId, minId, maxId) {
			var scrollContainer = document.getElementById(controlId);

			var mainElement = document.getElementById(elementId);
			var mainElementPosition = BAL.getElementPositionRelativeTo(mainElement, scrollContainer);

			var maxElementPosition;
			var maxElement;
			if (maxId) {
				maxElement = document.getElementById(maxId);
				maxElementPosition = BAL.getElementPositionRelativeTo(maxElement, scrollContainer);
			} else {
				maxElement = mainElement;
				maxElementPosition = mainElementPosition;
			}

			var minElementPosition;
			var minElement;
			if (minId) {
				minElement = document.getElementById(minId);
				minElementPosition = BAL.getElementPositionRelativeTo(minElement, scrollContainer);
			} else {
				minElement = mainElement;
				minElementPosition = mainElementPosition;
			}

			this.scrollHorizontal(scrollContainer, minElement, minElementPosition, mainElement, mainElementPosition,
					maxElement, maxElementPosition);
			this.scrollVertical(scrollContainer, minElement, minElementPosition, mainElement, mainElementPosition,
					maxElement, maxElementPosition);
		},

		scrollHorizontal : function(scrollContainer, minElement, minElementPosition, mainElement, mainElementPosition,
				maxElement, maxElementPosition) {
			var main = mainElementPosition.x;
			var min = Math.min(minElementPosition.x, main, maxElementPosition.x);
			var max = Math.max(minElementPosition.x	+ BAL.getElementWidth(minElement), 
							main + BAL.getElementWidth(mainElement),
							maxElementPosition.x + BAL.getElementWidth(maxElement));
			// let not hide element by scrollbar.
			max += BAL.getVerticalScrollBarWidth();
			var currentScroll = BAL.getScrollLeftElement(scrollContainer);
			var viewPortSize = BAL.getElementWidth(scrollContainer);
			var newScrollPosition = this.getScrollPosition(currentScroll, viewPortSize, min, main, max);
			if (newScrollPosition != -1) {
				BAL.setScrollLeftElement(scrollContainer, newScrollPosition);
			}
		},

		scrollVertical : function(scrollContainer, minElement, minElementPosition, mainElement, mainElementPosition,
				maxElement, maxElementPosition) {
			var main = mainElementPosition.y;
			var min = Math.min(minElementPosition.y, main, maxElementPosition.y);
			var max = Math.max(minElementPosition.y + BAL.getElementHeight(minElement), 
							main + BAL.getElementHeight(mainElement),
							maxElementPosition.y + BAL.getElementHeight(maxElement));
			// let not hide element by scrollbar.
			max += BAL.getHorizontalScrollBarHeight();
			var currentScroll = BAL.getScrollTopElement(scrollContainer);
			var viewPortSize = BAL.getElementHeight(scrollContainer);
			var newScrollPosition = this.getScrollPosition(currentScroll, viewPortSize, min, main, max);
			if (newScrollPosition != -1) {
				BAL.setScrollTopElement(scrollContainer, newScrollPosition);
			}
		},

		getScrollPosition : function(currentScrollValue, scrollContainerSize,
				min, main, max) {
			if (currentScrollValue <= min && max <= currentScrollValue + scrollContainerSize) {
				// whole range is displayed no scroll needed
				return -1;
			} else {
				var optimalScrollPosition = main - this.scrollRatio * scrollContainerSize;
				if (main < currentScrollValue || currentScrollValue + scrollContainerSize < main) {
					// main point is not visible. position it optimal
					return optimalScrollPosition;
				} else {
					if (currentScrollValue < optimalScrollPosition) {
						// main point lies below optimal point
						return Math.min(optimalScrollPosition, max - scrollContainerSize);
					} else {
						return -1;
					}
				}
			}

		}

	}, 

	/**
	 * <<class>>
	 */
	SelectOptionControl : {
		handleOnChange : function(inputElement, showWait) {
			services.form.sendValueUpdate(inputElement, inputElement.parentNode.id,
                    inputElement.checked, showWait);

			return false;
		}
	},

	/**
	 * <<class>>
	 */
	SelectionPartControl : {
		handleOnChange : function(inputElement, showWait) {
			services.form.sendValueUpdate(inputElement, inputElement.parentNode.id,
				inputElement.checked, showWait);
			
			return false;
		}
	},
	
    /**
	 * checks whether there is an active element on the path from the
	 * startElement (inclusive) to the stopElement (exclusive). It is expected
	 * that the stopElement is an ancestor of the startElement
	 * 
	 * @param {Node}
	 *            startElement the first element to check
	 * @param {Node}
	 *            stopElement the parent of the last element to check
	 * @return {Boolean}
	 */
    checkActiveElement : function(startElement, stopElement) {
        while (startElement != null && startElement != stopElement) {
        	if (BAL.DOM.canBeActive(startElement)){
        		return true;
        	}
            startElement = startElement.parentNode;
        }
        return false;
    },

    TableControl : {
    	currentInsertionMarker : null,
    	
        selectRow : function(evt, element, controlId) {
            evt = BAL.getEvent(evt);
            var eventSource = BAL.getEventSource(evt);
            
            var tableCell = eventSource.closest('td');
            var tableRowId = tableCell.parentNode.id;
            
            var index = tableRowId.lastIndexOf('.');
			var rowId = parseInt(tableRowId.substring(index + 1));

            this.select(evt, element, controlId, rowId, TABLE.getChildIndex(tableCell));
        },
        
        select : function(evt, element, controlId, rowId, columnId) {
            evt = BAL.getEvent(evt);

            var eventSource = BAL.getEventSource(evt);
            if (services.form.checkActiveElement(eventSource, element)) {
                // Do not select table row, if a input element
                // or an anchor was clicked.
                return true;
            }
            this._select(evt, controlId, rowId, columnId);
        },
		
		_select : function(evt, controlId, rowId, columnId) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "tableSelect",
				controlID : controlId,
				row : rowId, 
				column : columnId,
				shiftKey : BAL.hasShiftModifier(evt),
				ctrlKey : BAL.hasCtrlModifier(evt)
			}, services.ajax.USE_WAIT_PANE_IN_FORMULA);

			try {
				BAL.removeDocumentSelection();
			} catch(ex) {
				// Sometimes IE throws an exception if no document selection is present.
			}
			BAL.eventStopPropagation(evt);
			return false;
		},
		
		handleOnDragStart: function(event, controlElement) {
			var event = BAL.getEvent(event);
			var rowElement = this.getRow(controlElement, event.target);

			var draggedRows = [];
			
			if(BAL.DOM.containsClass(rowElement, "tblSelected")) {
				/*
				 * When one of the selected rows is dragged, the whole selection is dragged.
				 */
				for (const selectedRow of controlElement.querySelectorAll('tr.tblSelected')) {
					if(!draggedRows.includes(selectedRow)) {
						draggedRows.push(selectedRow);
					}
				}
			} else {
				draggedRows.push(rowElement);
			}

			var scope = services.ajax.COMPONENT_ID;
			var dragImageElement = services.form._createDragImageElement(draggedRows);

			window.tlDnD = {
				/**
				 * For Chrome and IE the dataTransfer data is only available during the drop event handling. 
				 */
				data: "dnd://" + scope + "/" + controlElement.id + "/" + draggedRows.map(row => row.id).join(),
				image: dragImageElement
			};

			event.dataTransfer.setData("text", window.tlDnD.data);
			event.dataTransfer.setDragImage(window.tlDnD.image, 0, 0);
			event.dataTransfer.effectAllowed = "all";
			
			return true;
		},
		
		handleOnDrop: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();

			var dropType = BAL.DOM.getNonStandardAttribute(controlElement, "data-droptype");
			var targetId;
			var pos;
			var success;
			{
				var rowElement = this.currentInsertionMarker;
				success = rowElement != null;
				if (success) {
					targetId = rowElement.id;
					if (dropType == "ORDERED") {
						if (BAL.DOM.containsClass(rowElement, "dndInsertAbove")) {
							pos = "above";
						} else if (BAL.DOM.containsClass(rowElement, "dndInsertBelow")) {
							pos = "below";
						} else {
							success = false;
						}
					} else {
						pos = "onto";
					}
				}
			}
			
			if (success) {
				var data = window.tlDnD.data;
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "dndDrop",
					controlID : controlElement.id,
					data : data,
					id: targetId,
					pos: pos
				}, services.ajax.USE_WAIT_PANE_IN_FORMULA);
			}
			
			this.resetMarker();
			return false;
		},
		
		handleOnDragOver: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			if(!window.tlDnD || !window.tlDnD.data){
				event.dataTransfer.dropEffect = 'none';
				return;
			}
			
			if(!controlElement.isDragOverHandled) {
				var row = this.getRow(controlElement, event.target);
				
				if(row != null) {
					var position = services.form.TableControl._getDropPosition(event, controlElement, row);
					
					if(window.tlDnD.cache !== undefined) {
						var isDropable = window.tlDnD.cache.get(window.tlDnD.data.split("/").pop(), row.id);
						
						if(isDropable !== undefined) {
							if(isDropable) {
								this.displayDropMarkerInternal(controlElement, row, position);
								event.dataTransfer.dropEffect = "move";
							} else {
								this.resetMarker();
								event.dataTransfer.dropEffect = 'none';
							}
							
							return;
						} 
					}
					
					controlElement.isDragOverHandled = true;
					
					services.ajax.execute("dispatchControlCommand", {
						controlCommand : "dragOver",
						controlID : controlElement.id,
						data: window.tlDnD.data,
						id: row.id,
						pos: position
					}, true);
					
					setTimeout(function() {
						controlElement.isDragOverHandled = false;
					}, 50);
				} else {
					this.resetMarker();
					event.dataTransfer.dropEffect = 'none';
				}
			}
		},
		
		_getDropPosition: function(event, controlElement, row) {
			var dropType = BAL.DOM.getNonStandardAttribute(controlElement, "data-droptype");

			if (dropType == "ORDERED") {
				var coordinates = BAL.relativeMouseCoordinates(event, row);
				var height = BAL.getElementHeight(row);
				
				return coordinates.y > (height / 2) ? "below" : "above";
			} else {
				return "onto";
			}
		},
		
		changeToNoDropCursor: function(controlElement, target) {
			this.resetMarker();
			
			services.form._putToDnDCache(controlElement, window.tlDnD.data.split("/").pop(), target, false);
		},
		
		displayDropMarker: function(controlElement, target, position) {
			this.displayDropMarkerInternal(controlElement, target, position);
			services.form._putToDnDCache(controlElement, window.tlDnD.data.split("/").pop(), target, true);

			return false;
		},
		
		displayDropMarkerInternal: function(controlElement, rowElement, position) {
			this.resetMarker();
			
			if (position == "below") {
				this.addMarkerClass(rowElement, "dndInsertBelow");
			} else if(position == "above"){
				this.addMarkerClass(rowElement, "dndInsertAbove");
			} else {
				this.addMarkerClass(rowElement, "dndInsertInto");
			}
		},
		
		handleOnDragEnter: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			return false;
		},
		
		handleOnDragLeave: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			this.resetMarker();
			
			return false;
		},
		
		handleOnDragEnd: function(event, controlElement) {
			window.tlDnD.image.remove();
			
			delete window.tlDnD;
		},
		
		getRow: function(controlElement, targetElement) {
			var element = targetElement;
            while (element != null && element != controlElement) {
            	if (BAL.DOM.containsClass(element, "tl-table__row")) {
            		if (BAL.DOM.containsClass(element, "header") || BAL.DOM.containsClass(element, "footer")) {
            			return null;
            		}
            		return element;
            	}

            	element = element.parentNode;
            }
            return null;
		},
		
		addMarkerClass: function(markerElement, hoverClass) {
			BAL.DOM.addClass(markerElement, hoverClass);
			this.currentInsertionMarker = markerElement;
		},
		
		resetMarker: function() {
			var markerElement = this.currentInsertionMarker;
			if (markerElement != null) {
				this.resetMarkerOn(markerElement);
			}
		},
		
		resetMarkerOn: function(element) {
			BAL.DOM.removeClass(element, "dndInsertAbove");
			BAL.DOM.removeClass(element, "dndInsertBelow");
			BAL.DOM.removeClass(element, "dndInsertInto");
		}
		
	},

	/**
	 * <<class>>
	 */
	EditableTableControl : {
		/** @static */
		getRow : function(buttonElement) {
			// Note: tagName.toLowerCase() is important: For compatibility with
		// HTML 4.0, browsers report tag names of XHTML elements in upper
		// case.
		// If such element is replaced with the result of an XML response to
		// an AJAX command, it gets the "correct" (lowercase) XHTML tagName
		// (at least in FireFox). To match both versions, the case of the
		// tagName property must be normalized.
		var ancestorOrSelf = buttonElement;
		while (!(BAL.DOM.getTagName(ancestorOrSelf) == "tr")) {
			ancestorOrSelf = ancestorOrSelf.parentNode;
		}
		return ancestorOrSelf;
	},

	getControlDiv : function(element) {
		// Note: tagName.toLowerCase() is important: For compatibility with
		// HTML 4.0, browsers report tag names of XHTML elements in upper
		// case.
		// If such element is replaced with the result of an XML response to
		// an AJAX command, it gets the "correct" (lowercase) XHTML tagName
		// (at least in FireFox). To match both versions, the case of the
		// tagName property must be normalized.
		var ancestorOrSelf = element;
		while (!(BAL.DOM.getTagName(ancestorOrSelf) == "table")) {
			ancestorOrSelf = ancestorOrSelf.parentNode;
		}

		// The control div surrounds the table element.
		return ancestorOrSelf.parentNode;
	},

	handleChooseClick : function(element) {
		var cid = this.getControlDiv(element).id;
		services.ajax.execute("dispatchControlCommand", {
			controlCommand : "openRowSelector",
			controlID : cid
		}, services.ajax.USE_WAIT_PANE_IN_FORMULA);

		return false;
	},

	handleRemoveClick : function(element) {
		var rowID = this.getRow(element).id;
		var cid = this.getControlDiv(element).id;
		services.ajax.execute("dispatchControlCommand", {
			controlCommand : "removeRow",
			controlID : cid,
			rowID : rowID
		}, services.ajax.USE_WAIT_PANE_IN_FORMULA);

		return false;
	},

	handleMoveUpClick : function(element) {
		var rowID = this.getRow(element).id;
		var cid = this.getControlDiv(element).id;
		services.ajax.execute("dispatchControlCommand", {
			controlCommand : "moveRowUp",
			controlID : cid,
			rowID : rowID
		}, services.ajax.USE_WAIT_PANE_IN_FORMULA);

		return false;
	},

	handleMoveDownClick : function(element) {
		var rowID = this.getRow(element).id;
		var cid = this.getControlDiv(element).id;
		services.ajax.execute("dispatchControlCommand", {
			controlCommand : "moveRowDown",
			controlID : cid,
			rowID : rowID
		}, services.ajax.USE_WAIT_PANE_IN_FORMULA);

		return false;
	},

	handleSelectClick : function(element) {
		var rowID = this.getRow(element).id;
		var cid = this.getControlDiv(element).id;
		services.ajax.execute("dispatchControlCommand", {
			controlCommand : "tableSelect",
			controlID : cid,
			rowID : rowID
		}, services.ajax.USE_WAIT_PANE_IN_FORMULA);

		return false;
	}
	},
	
	ContentControl: {
		handleOnDragOver: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			if(!window.tlDnD || !window.tlDnD.data){
				event.dataTransfer.dropEffect = 'none';
				return;
			}
			
			return false;
		},
		
		handleOnDragEnter: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();

			event.dataTransfer.dropEffect = "move";
			BAL.DOM.addClass(controlElement, "dndInsertInto");

			return false;
		},
		
		handleOnDragLeave: function(event, controlElement) {
			var event = BAL.getEvent(event);
			var source = BAL.getEventSource(event);
			if (!BAL.DOM.containsClass(source, "dropPane")) {
				return true;
			}
			event.preventDefault();
			
			this.resetMarker(controlElement);
			
			return false;
		},
		
		handleOnDrop: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();

			var data = window.tlDnD.data;
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "dndDrop",
				controlID : controlElement.id,
				data : data
			}, services.ajax.USE_WAIT_PANE_IN_FORMULA);
			
			this.resetMarker(controlElement);
			return false;
		},
		
		resetMarker: function(controlElement) {
			BAL.DOM.removeClass(controlElement, "dndInsertInto");
		}
	},
	
	TreeControl: {
		
		/**
		 * The node element that is currently highlighted for a pending drop operation.
		 */
		currentInsertionMarker: null,
		
		selectTreeTableRow: function(event, controlId, nodeId) {
			var controlElement = document.getElementById(controlId);
			var nodeElement = document.getElementById(nodeId);
			var nodeHandle = {nodeElement: nodeElement, toggleElement: null, stopProcessing: false};
			
			services.form.TreeControl._handleSelection(event, controlElement, nodeHandle);
		},
		
		handleOnClick: function(event, controlElement) {
            var nodeHandle = services.form.TreeControl._getClickedNodeHandle(event, controlElement);
            if(nodeHandle.stopProcessing != null) {
            	return nodeHandle.stopProcessing;
            }
            services.form.TreeControl._handleSelection(event, controlElement, nodeHandle);
            return BAL.cancelEvent(event);
		},
		
		handleOnMouseDown: function(event, controlElement) {
			var nodeHandle = services.form.TreeControl._getClickedNodeHandle(event, controlElement);
			if(nodeHandle.stopProcessing != null) {
				return nodeHandle.stopProcessing;
			}
			
			var nodeElement = nodeHandle.nodeElement;
			if(services.form.TreeControl._isSelected(nodeHandle.nodeElement)) {
				var removeListener = function() {
					BAL.removeEventListener(nodeElement, 'mouseout', removeListener);
					BAL.removeEventListener(nodeElement, 'mouseup', doSelect);
				};
				var doSelect = function(mouseUpEvent) {
					removeListener();
					services.form.TreeControl._handleSelection(mouseUpEvent, controlElement, nodeHandle);
				};
				BAL.addEventListener(nodeElement, 'mouseup', doSelect);
				BAL.addEventListener(nodeElement, 'mouseout', removeListener);
			} else {
				services.form.TreeControl._handleSelection(event, controlElement, nodeHandle);
			}
			
			return BAL.cancelEvent(event);
		},
		
		_handleSelection: function(event, controlElement, nodeHandle) {
			var normalizedEvent = BAL.getEvent(event);
			
			var nodeElement = nodeHandle.nodeElement;
			var toggleElement = nodeHandle.toggleElement;
			var canSelect = services.form.TreeControl._isSelectable(nodeElement);
			
			var ctrlId = controlElement.getAttribute("id");
			var nodeId = nodeElement.getAttribute("id");
			
			if (toggleElement != null || canSelect) {
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : toggleElement != null ? "toggleExpand" : "selectTree",
							controlID : ctrlId,
							id : nodeId,
							ctrlModifier : BAL.hasCtrlModifier(normalizedEvent),
							shiftModifier : BAL.hasShiftModifier(normalizedEvent)
				}, true);
			}
		},
		
		_getClickedNodeHandle: function(event, controlElement) {
			var event = BAL.getEvent(event);
            var eventSource = BAL.getEventSource(event);

            var nodeElement = null;
            var toggleElement = null;
            var stopProcessing = null;
            
            var element = eventSource;
            while (element != null && element != controlElement) {
        		if (element.disabled) {
        			stopProcessing = false;
        			break;
        		}
        		
        		else if (BAL.DOM.containsClass(element, "tcrLabel")) {
            		// Node element found.
            		nodeElement = element;
            		break;
            	}
            	
            	else if (BAL.DOM.containsClass(element, "tcrToggle")) {
            		// Node element found.
            		toggleElement = element;
            	}
        		
            	else if (BAL.DOM.canBeActive(element)) {
            		// Do not treat the node anchor as other "active" element.
            		if (!BAL.DOM.containsClass(element, "tcrLink")) {
            			// Do not select, when clicking on other active elements such as buttons and input elements.
            			stopProcessing = true;
            			break;
                	}
	        	}

                element = element.parentNode;
            }
            
            if (nodeElement == null) {
            	// No node was clicked.
            	stopProcessing = true;
            }
            
            return {nodeElement: nodeElement, toggleElement: toggleElement, stopProcessing: stopProcessing};
		},
		
		_isSelectable: function(node) {
			return !BAL.DOM.containsClass(node, "tcrNoSelect");
		},
		
		_isSelected: function(node) {
			return BAL.DOM.containsClass(node, "treeSelected");
		},
		
		dblClick: function(event, ctrlId, useWaitPane) {
			var event = BAL.getEvent(event);
			var controlElement = document.getElementById(ctrlId);
			var nodeElement = this.getNode(controlElement, BAL.getEventTarget(event));
			
			var canSelect = !BAL.DOM.containsClass(nodeElement, "tcrNoSelect");
			if(canSelect) {
				var nodeId = nodeElement.getAttribute("id");
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "dblClick",
					controlID : ctrlId,
					id : nodeId
				}, useWaitPane);
			}
			BAL.eventStopPropagation(event);
			return BAL.cancelEvent(event);
		},
		
		handleFixed: function(event, element) {
			var nodeHandle = services.form.TreeControl._getClickedNodeHandle(event, element);
			if(nodeHandle.stopProcessing != null) {
				return nodeHandle.stopProcessing;
			}
			
			if(nodeHandle.toggleElement != null) {
				services.form.TreeControl._handleSelection(event, element, nodeHandle);
			} 
			return BAL.cancelEvent(event);
		},
		
		handleOnDragStart: function(event, controlElement) {
			var event = BAL.getEvent(event);
			var nodeElement = this.getNode(controlElement, event.target);

			var draggedNodeIDs;
			var draggedNodes;
			
			if(services.form.TreeControl._isSelected(nodeElement)) {
				draggedNodes = services.form.TreeControl.getSelectedNodes(controlElement);
				draggedNodeIDs = services.form.TreeControl.getSelectedNodes(controlElement).map(node => node.id).join();
			} else {
				draggedNodes = [nodeElement];
				draggedNodeIDs = nodeElement.id;
			}

			var scope = services.ajax.COMPONENT_ID;

			var dragImageElement = services.form._createDragImageElement(draggedNodes);
			
			window.tlDnD = {
				/**
				 * For Chrome and IE the dataTransfer data is only available during the drop event handling. 
				 */
				data: "dnd://" + scope + "/" + controlElement.id + "/" + draggedNodeIDs,
				image: dragImageElement
			};
			
			event.dataTransfer.setData("text", window.tlDnD.data);
			event.dataTransfer.setDragImage(window.tlDnD.image, 0, 0);
			event.dataTransfer.effectAllowed = "all";
			
			return true;
		},
		
		handleOnDragOver: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			if(!window.tlDnD || !window.tlDnD.data){
				event.dataTransfer.dropEffect = 'none';
				return;
			}
			
			if(!controlElement.isDragOverHandled) {
				var dropTarget = this.getDropTarget(controlElement, event);
				
				if(dropTarget !== undefined) {
					if(window.tlDnD.cache !== undefined) {
						var isDropable = window.tlDnD.cache.get(window.tlDnD.data.split("/").pop(), dropTarget.node.id);
						
						if(isDropable !== undefined) {
							var isDropableAtPosition = isDropable[dropTarget.position];
							
							if(isDropableAtPosition !== undefined) {
								if(isDropableAtPosition) {
									this.displayDropMarkerInternal(controlElement, dropTarget.node, dropTarget.position);
									event.dataTransfer.dropEffect = "move";
								} else {
									this.resetMarker();
									event.dataTransfer.dropEffect = 'none';
								}
								
								return;
							} 
						}
					}
					
					controlElement.isDragOverHandled = true;
					
					services.ajax.execute("dispatchControlCommand", {
						controlCommand : "dragOver",
						controlID : controlElement.id,
						data: window.tlDnD.data,
						id: dropTarget.node.id,
						pos: dropTarget.position
					}, true);
					
					setTimeout(function() {
						controlElement.isDragOverHandled = false;
					}, 50);
				}
			}
		},
		
		changeToNoDropCursor: function(controlElement, target, pos) {
			this.resetMarker();
			
			var sourceID = window.tlDnD.data.split("/").pop();
			this.addToDnDCache(controlElement, sourceID, target.id, pos, false);
		},
		
		addToDnDCache: function (controlElement, sourceID, targetID, pos, isDropable) {
			if(window.tlDnD.cache !== undefined) {
				var cacheValue = window.tlDnD.cache.get(sourceID, targetID);
				if(cacheValue !== undefined) {
					cacheValue[pos] = isDropable;
					return;
				}
			}
			
			var cacheValue = {};
			cacheValue[pos] = isDropable;
			
			services.form._putToDnDCache(controlElement, sourceID, targetID, cacheValue);
		},
		
		displayDropMarker: function(controlElement, target, pos) {
			this.displayDropMarkerInternal(controlElement, target, pos);
			var sourceID = window.tlDnD.data.split("/").pop();
			this.addToDnDCache(controlElement, sourceID, target.id, pos, true);

			return false;
		},
		
		getDropTarget: function(controlElement, event) {
			var dropType = BAL.DOM.getNonStandardAttribute(controlElement, "data-droptype");
			var node =  this.getNode(controlElement, event.target);
			
			if(node != null) {
				return this._getDropTargetInternal(event, controlElement, node, dropType);
			} else {
				if (dropType == "ORDERED") {
					// Below the last visible node.
					var container = BAL.DOM.getLastElementChild(controlElement);
					if (container != null) {
						var lastNode = this.lastNode(container);
						if (lastNode != null) {
							return {
								node: lastNode,
								position: "below"
							};
						} 
					}
				}
				
				return undefined;
			}
		},
		
		_getDropTargetInternal: function(event, controlElement, nodeElement, dropType) {
			if (dropType == "ORDERED") {
				var coordinates = BAL.relativeMouseCoordinates(event, nodeElement);
			 	height = BAL.getElementHeight(nodeElement);
			 	var within = coordinates.y > (height / 2);
				
				if (within) {
					var expanded = BAL.DOM.containsClass(nodeElement, "tcrExpanded");
					
					var childrenContainer = BAL.DOM.getNextElementSibling(nodeElement);
					if (childrenContainer != null) {
						var firstChild = BAL.DOM.getFirstElementChild(childrenContainer);
						if (firstChild != null) {
							if (BAL.DOM.containsClass(firstChild, "tcrNode")) {
								var firstChildLabel = BAL.DOM.getFirstElementChild(firstChild);
								if (firstChildLabel != null) {
									return {
										node: firstChildLabel,
										position: "above"
									};
								}
							}
						}
					}
					
					return {
						node: nodeElement,
						position: "within"
					};
				} else {
					var nodeParent = nodeElement.parentNode;
					if (nodeParent != null) {
						var preceeding = BAL.DOM.getPreviousSibling(nodeParent);
						if (preceeding != null) {
							var node = this.lastNode(preceeding);
							if (node != null) {
								return {
									node: node,
									position: "below"
								};
							}
						}
					}
					
					return {
						node: nodeElement,
						position: "above"
					};
				}
			} else {
				return {
					node: nodeElement,
					position: "within"
				};
			}
		},
		
		displayDropMarkerInternal: function(controlElement, nodeElement, position) {
			this.resetMarker();
			
			this.currentInsertionMarker = nodeElement;
			
			if (BAL.DOM.getNonStandardAttribute(controlElement, "data-droptype") == "ORDERED") {
				if(position == "above") {
					BAL.DOM.addClass(nodeElement, "dndInsertAbove");
				} else if(position == "below") {
					BAL.DOM.addClass(nodeElement, "dndInsertBelow");
				} else {
					BAL.DOM.addClass(nodeElement, "dndInsertWithin");
				}
			} else {
				BAL.DOM.addClass(nodeElement, "dndInsertInto");
			}
		},
		
		lastNode: function(container) {
			while (true) {
				var inner = BAL.DOM.getLastElementChild(container);
				if (inner == null) {
					return null;
				}
				if (BAL.DOM.containsClass(inner, "tcrLabel")) {
					// Node found.
					return inner;
				}
				container = inner;
			}
		},
		
		handleOnDragEnter: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			return false;
		},
		
		handleOnDragLeave: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			this.resetMarker();
			return false;
		},
		
		handleOnDragEnd: function(event, controlElement) {
			window.tlDnD.image.remove();
			
			delete window.tlDnD;
		},
		
		getDropPositionFromElement: function(controlElement, nodeElement) {
			var dropType = BAL.DOM.getNonStandardAttribute(controlElement, "data-droptype");
			
			if (dropType == "ORDERED") {
				if (BAL.DOM.containsClass(nodeElement, "dndInsertAbove")) {
					return "above";
				} else if (BAL.DOM.containsClass(nodeElement, "dndInsertBelow")) {
					return "below";
				} else {
					return "within";
				}
			} else {
				return "within";
			}
		},
		
		handleOnDrop: function(event, controlElement) {
			var event = BAL.getEvent(event);
			event.preventDefault();

			var node = this.currentInsertionMarker;
			if(node != null) {
				var position = this.getDropPositionFromElement(controlElement, node);
				var data = window.tlDnD.data;
				
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "dndTreeDrop",
					controlID : controlElement.id,
					data : data,
					id: node.id,
					pos: position
				}, services.ajax.USE_WAIT_PANE_IN_FORMULA);
			}
			
			this.resetMarker();
			return false;
		},
		
		resetMarker: function() {
			if (this.currentInsertionMarker != null) {
				BAL.DOM.removeClass(this.currentInsertionMarker, "dndInsertAbove");
				BAL.DOM.removeClass(this.currentInsertionMarker, "dndInsertWithin");
				BAL.DOM.removeClass(this.currentInsertionMarker, "dndInsertBelow");
				BAL.DOM.removeClass(this.currentInsertionMarker, "dndInsertInto");
			}
		},
		
		getNode: function(controlElement, element) {
            while (element != null && element != controlElement) {
            	if (BAL.DOM.containsClass(element, "tcrLabel")) {
            		return element;
            	}

            	element = element.parentNode;
            }
            return null;
		},
		
		getSelectedNodes: function(treeRoot) {
			var selectedNodes = new Array();
			services.form.TreeControl._collectSelectedNodes(treeRoot, selectedNodes);
			return selectedNodes;
		},
		
		_collectSelectedNodes: function(node, selectedNodes) {
			if(services.form.TreeControl._isSelected(node)) {
				selectedNodes.push(node);
			}
			var childNodes = BAL.DOM.getChildElements(node);
			for(var i = 0; i < childNodes.length; i++) {
				services.form.TreeControl._collectSelectedNodes(childNodes[i], selectedNodes);
			}
		},
		
		/* Legacy Drag and Drop implementation */

		/**
		 * Called on the element of a drag'n drop operation on which a 
		 * mousedown-event has happened.
		 * 
		 * @param element
		 *            The concrete DOM element on which the drag'n drop
		 *            process is started.
		 * @returns The IDs of the moved list entries.
		 */
		createDrag: function(evt, rootElement) {
			var eventSource;
			try {
				// Quirks for IE, due to event target element strangely is not part of DOM,
				// and therefore cannot be used as traversal base to find DOM node representation
				// of tree node.
				eventSource = event.relatedTarget;
			} catch(ex) {
				eventSource = BAL.getEventTarget(evt);
			}
			var clickedNode = services.form.TreeControl.getNode(rootElement, eventSource);
			if (clickedNode == null) {
				return null;
			}
			var movedNodes = services.form.TreeControl.getSelectedNodes(rootElement);
			
			var clientDragInfoAll = document.createElement("ul");
			var serverDragInfoAll = new Array();
			var clientDragInfoNonFixed = null;
			var serverDragInfoNonFixed = new Array();
			
			var maxDisplayed = 4;
			var dragShapeHtml = null;
			if(movedNodes.length > 0 ) {
				dragShapeHtml = "<div class='selectbox dndSelection'><ul>";
				for (var i = 0, size = movedNodes.length; i < size; i++) {
					var movedNode = movedNodes[i];
					var copiedLi = document.createElement("li");
					var copiedLiContent = BAL.DOM.importNode(document, movedNode);
					BAL.DOM.removeClass(copiedLiContent, "treeSelected");
					copiedLi.appendChild(copiedLiContent);
					serverDragInfoAll.push(movedNode.id);
					copiedLi.id = services.ajax.nextID();
					clientDragInfoAll.appendChild(copiedLi);
					if(TL.getTLAttribute(copiedLi, "fixed") == null) {
						serverDragInfoNonFixed.push(movedNode.id);
						if(clientDragInfoNonFixed == null) {
							clientDragInfoNonFixed = document.createElement("ul");
						}
						var copiedLiNonFixed = BAL.DOM.importNode(document, movedNode);
						copiedLiNonFixed.id = services.ajax.nextID();
						clientDragInfoNonFixed.appendChild(copiedLiNonFixed);
					}
					
					if (i < maxDisplayed || i == size - 1) {
						var previewLi = BAL.DOM.importNode(document, movedNode);
						BAL.DOM.removeNonStandardAttribute(previewLi, "id");
						dragShapeHtml += previewLi.outerHTML;
					} else {
						if (i == maxDisplayed) {
							dragShapeHtml += "<li>...</li>";
						}
					}
				}
				dragShapeHtml += "</ul></div>";
			}
			
			return {
				_element: rootElement,
				
				getClientDragInfo: function(dragSourceElement, dropTargetElement) {
					if(dragSourceElement === dropTargetElement) {
						return clientDragInfoAll;
					} else {
						return clientDragInfoNonFixed;
					}
				},
				
				dragShapeHtml: dragShapeHtml,
				
				/**
				 * List of IDs of moved nodes.
				 */
				getServerDragInfo: function(dragSourceElement, dropTargetElement) {
					if(dragSourceElement === dropTargetElement) {
						return serverDragInfoAll;
					} else {
						return serverDragInfoNonFixed;
					}
				},
				
				/**
				 * Informs the source about the completion of the drag'n drop operation.
				 * 
				 * @param successful
				 *        If the the dragged object was accepted by any target.
				 */
				dispose: function(successful, dropTargetElement) {
					// Do nothing
				}

			};
		},
		
		/**
		 * Called on each potential target element of a drag'n drop operation 
		 * to inform that a drag'n drop operation is about to start.
		 * 
		 * A potential drop target must be enabled to provide visual feedback
		 * about a pending drop within its area. A pending drop will be is 
		 * announced by calling a "marker function" installed on the potential 
		 * drop target.
		 * 
		 * @param element
		 *            The concrete DOM element which is a potential drag'n 
		 *            drop target.
		 * @returns A handle object that is informed about drag positions and 
		 *          the end of the drag'n drop operation.
		 */
		createDrop: function(event, element) {
			return {
				element: element,
				
				marker: null,
				
				lastEventSrc: null,
				
				dragOver: function(eventSrc, clientDragInfo) {
					// Do nothing
				},
				
				dragOut: function() {
					// Do nothing
				},
				
				offerDrop: function(clientDragInfo, srcElement, element) {
					var result = {};
					if (clientDragInfo == null || BAL.DOM.getTagName(clientDragInfo) != "ul") {
						result = null;
					} 
					return result;
				}
				
			};
		},
	},

	FormEditorControl : {
		moveMember : function(controlID, elementID, siblingID, parentID, elementName, inEditor, showWait, serverResponseCallback) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "moveMember",
				controlID : controlID,
				elementID : elementID,
				siblingID : siblingID,
				parentID : parentID,
				elementName : elementName,
				inEditor : inEditor
			}, showWait, serverResponseCallback);
		},
					
		editElement : function(evt, controlID, elementID, showWait, serverResponseCallback) {
			evt = BAL.getEvent(evt);

			if(controlID != null) {
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "editElement",
					controlID : controlID,
					elementID : elementID
				}, showWait, serverResponseCallback);
			}
			
			BAL.eventStopPropagation(evt);
		},
				
		updateToolbox : function(controlID, showWait, serverResponseCallback) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "updateToolbox",
				controlID : controlID
			}, showWait, serverResponseCallback);
		}
	},
	
	TextInputControl : {
		handleOnChange : function(inputElement, controlId, showWait, serverResponseCallback) {
			services.form.sendValueUpdateOnce(inputElement, controlId, showWait, serverResponseCallback);

			// Continue processing this event.
			return true;
		},

		handleOnFocus : function(inputElement, maxLength, minLength) {
			// Save localError in the control element.
			inputElement.parentNode.hasLocalError = this.checkError(inputElement, maxLength, minLength);

			return true;
		},

		/**
		 * 
		 * Checks whether the size of the value of the given input element is to
		 * large or to small
		 * 
		 * @param {}
		 *            inputElement the input element whose value is to check
		 * @param {Number}
		 *            maxLength the maximal allowed size or -1 for no
		 *            restriction
		 * @param {Number}
		 *            minLength the minimal allowed size or -1 for no
		 *            restriction
		 * @return {Boolean} whether the field has an error
		 */
		checkError : function(inputElement, maxLength, minLength) {
			var hasMaxLength = (maxLength != -1);
			if (hasMaxLength) {
				window.status = this.getMessage(inputElement, maxLength);
			}
			var inputLength = this.getInputLength(inputElement);
			var hasError = inputLength < minLength;
			if (hasMaxLength) {
				hasError |= inputLength > maxLength;
			}
			return hasError;

		},

		/**
		 * Determines the length of the value of the given input element.
		 * 
		 * @param {}
		 *            inputElement the input element whose values size is needed
		 * @return {} the normalized size of the value ie a linebreak is
		 *         exactely one character
		 */
		getInputLength : function(inputElement) {
			/*
			 * ignore \r on windows in size computation as on server they are
			 * also ignored
			 */
			return new String(inputElement.value).replace(/\r\n/g, "\n").length;
		},

		handleOnInput : function(inputElement, controlId, maxLength, minLength, showWait) {
			var hasError = this.checkError(inputElement, maxLength, minLength);
			var hasErrorBefore = inputElement.parentNode.hasLocalError;

			if (hasError || (hasError != hasErrorBefore)) {
				inputElement.parentNode.hasLocalError = hasError;

				// Submit current value to the server to revalidate constraints.
				// Since the error message may change with the number of characters entered, 
				// the value must be submitted with each change until the error is cleared.
				// Sending error events locally does not work for two reasons:
				// First, this control does not locally listen for property
				// updates
				// (because it has a server-side control). Second, resetting the
				// error condition may be wrong, because another constraint may
				// fail on the server.
				this.handleOnChange(inputElement, controlId, showWait);
			}

			return true;
		},

		handleOnBlur : function(inputElement, maxLength, minLength) {
			window.status = "";
			return true;
		},

		getMessage : function(inputElement, maxLength) {
			var inputLength = this.getInputLength(inputElement);
			var hasError = inputLength > maxLength;

			if (hasError) {
				return services.i18n.FIELD_OVERFULL__DELETE.replace("{0}",
						inputLength - maxLength);
			} else if (inputLength == maxLength) {
				return services.i18n.FIELD_FULL;
			} else {
				return services.i18n.FIELD_CHARACTERS_LEFT__SPACE.replace(
						"{0}", maxLength - inputLength);
			}
		}
	},

	IntegerInputControl : {

		handleOnChange : function(inputElement, controlId, showWait) {
			services.form.sendValueUpdateOnce(inputElement, controlId, showWait);
			// Continue processing this event.
			return true;
		},
		
		clearTimer : function(intControl) {
			// clear any previously set update timer
			var pendingUpdate = intControl.intTimer;
			if (pendingUpdate != null) {
				window.clearTimeout(pendingUpdate);
				intControl.intTimer = null;
			}
		},

		handleIncrement : function(ctrlID, inputID, minValue, maxValue, showWait) {
			var inputControl = document.getElementById(inputID);
			var intControl = document.getElementById(ctrlID);
			var oldValue = inputControl.value;

			// if the previous value is empty and there is a minimum configured
			// that is greater than the implicit value zero set the value to
			// that minimum.
			if (oldValue == "" && minValue != null && minValue > 0) {
				inputControl.value = minValue;
				services.form.IntegerInputControl.clearTimer(intControl);
			}
			// if the previous value is less than any minimum then set the value
			// to the minimum
			else if (oldValue != "" && minValue != null && minValue > oldValue) {
				inputControl.value = minValue;
				services.form.IntegerInputControl.clearTimer(intControl);
			}
			// if we're still within the legal range just increment the value
			else if (maxValue == null || oldValue < maxValue) {
				inputControl.value++;
				services.form.IntegerInputControl.clearTimer(intControl);
			}

			var updateFunction = function() {
				services.form.IntegerInputControl.handleOnChange(inputControl,
						ctrlID, showWait);
			};
			// if the value has changed send set a timer for an update event
			if (oldValue != inputControl.value) {
				intControl.intTimer = window.setTimeout(updateFunction, 250);
			}

			return false;
		},

		handleDecrement : function(ctrlID, inputID, minValue, maxValue, showWait) {
			var inputControl = document.getElementById(inputID);
			var intControl = document.getElementById(ctrlID);
			var oldValue = inputControl.value;

			// if the previous value is empty and there is a maximum configured
			// that is greater than the implicit value zero than set the value
			// to that maximum
			if (inputControl.value == "" && maxValue != null && maxValue < 0) {
				inputControl.value = maxValue;
				services.form.IntegerInputControl.clearTimer(intControl);
			}
			// if the previous value is less than any maximum then set the value
			// to the maximum
			else if (inputControl.value != "" && maxValue != null
					&& maxValue < inputControl.value) {
				inputControl.value = maxValue;
				services.form.IntegerInputControl.clearTimer(intControl);
			}
			// if we're still within the legal range just decrement the value
			else if (minValue == null || inputControl.value > minValue) {
				inputControl.value--;
				services.form.IntegerInputControl.clearTimer(intControl);
			}

			var updateFunction = function() {
				services.form.IntegerInputControl.handleOnChange(inputControl,
						ctrlID, showWait);
			};
			// if the value has changed send set a timer for an update event
			if (oldValue != inputControl.value) {
				intControl.intTimer = window.setTimeout(updateFunction, 250);
			}

			return false;
		}
	},

	SelectControl : {
		setValue : function(select, selection) {
			var options = select.options;
			for ( var n = 0; n < options.length; n++) {
				options[n].selected = false;
			}

			if (selection.length == 0) {
				options.selectedIndex = -1;
			} else {
				var optionsByName = new Object();
				for ( var n = 0; n < options.length; n++) {
					optionsByName[options[n].value] = options[n];
				}

				for ( var n = 0; n < selection.length; n++) {
					var optionName = selection[n];
					var option = optionsByName[optionName];
					if (option == null) {
						// A selection was programatically set that is no in the
						// list of options. This is a programming error.
						services.log.error("Selection id '" + optionName
								+ "' not in the list of options in field '" + select.name
								+ "'.");
					} else {
						option.selected = true;
					}
				}
			}
		},

		handleOnChangeSingleSelect : function(select, controlId, showWait) {
			var oldSelection = BAL.DOM.getNonStandardAttribute(select, "data-currentselection");
			var selection = -1;
			
			var options = select.options;
			for (var n = 0; n < options.length; n++) {
				var option = options[n];
				if (option.selected) {
					if (n == oldSelection) {
						if (selection < 0) {
							selection = n;
						}
						
						// searching the new selected option, so don't care about old selection
						continue;
					} else {
						// expects that a whole interval without gaps is selected
						selection = n;
						if (n > oldSelection) {
							// search selection with most distance to old selection
							for (var m = n + 1; m < options.length; m++) {
								if (options[m].selected) {
									// only one selection so deselect other element
									options[m-1].selected = false;
									selection = m;
								} else {
									// no options are selected
									break;									
								}
							}
						} else {
							// n is new selection. Deselect larger
							for (var m = n + 1; m <= oldSelection; m++) {
								if (options[m].selected) {
									options[m].selected = false;
								}
							}
							
						}
						break;
					}
				}
			}
			
			if (selection == oldSelection) {
				return;
			}
			
			// delete old selection
			if (oldSelection >= 0) {
				options[oldSelection].selected = false;
			}
			if (selection >= 0) {
				// to ensure selection is highlighted
				options[selection].selected = true;
			} else {
				var canReset = BAL.DOM.getNonStandardAttribute(select, "data-canreset") == "true";
				if (!canReset) {
					if (oldSelection >= 0) {
						options[oldSelection].selected = true;
					}
					
					// No server update necessary.
					return;
				}
			}

			BAL.DOM.setNonStandardAttribute(select, "data-currentselection", selection);
			var selectionArray = new Array();
			if (selection >= 0) {
				selectionArray.push(options[selection].value);
			}
			services.form.sendValueUpdate(select, controlId, selectionArray, showWait);
		},
		
		handleOnChange : function(select, controlId, showWait) {
			var selection = new Array();
			
			var options = select.options;
			for ( var n = 0; n < options.length; n++) {
				var option = options[n];
				if (option.selected) {
					selection.push(option.value);
				}
			}

			// Prevent transmitting null-changes to the server. This minimizes the risk of
			// events arriving
			// out of order. See Ticket #205.
			var oldValue = select.oldValue;
			if (oldValue != null) {
				if (selection.length == oldValue.length) {
					var equals = true;
					for ( var n = 0; n < selection.length; n++) {
						if (selection[n] != oldValue[n]) {
							equals = false;
							break;
						}
					}
					if (equals) {
						// Prevent events that effectively have not changed the
						// value. This is necessary,
						// because this handler is called from onclick and onchange
						// for compatibility reasons.
						return;
					}
				}
			}
			select.oldValue = selection;
	
			services.form.sendValueUpdate(select, controlId, selection, showWait);
		}
	},
	
	GalleryControl : {
		init : function(imageContainerId, images) {
			var jqueryId = "#" + imageContainerId;
			$(jqueryId).fotorama({data: images});
			var fotoramaContainer = document.getElementById(imageContainerId);
			var deregisterFotorama = function() {
				BAL.removeEventListener(fotoramaContainer, 'DOMNodeRemovedFromDocument', deregisterFotorama);
				if($(jqueryId).data('fotorama')) {
					$(jqueryId).data('fotorama').destroy();
				}
			};
			BAL.addEventListener(fotoramaContainer, 'DOMNodeRemovedFromDocument', deregisterFotorama);
		},
		
		reload : function(imageContainerId, images) {
			var jqueryId = "#" + imageContainerId;
			$(jqueryId).data('fotorama').load(images);
		},
		
		showCaptions : function(imageContainerId, showCaptions) {
			var jqueryId = "#" + imageContainerId;
			var fotorama = $(jqueryId).data('fotorama');
			var options = fotorama.options;
			options.captions = showCaptions;
			fotorama.setOptions(options);
		}
	},

	ButtonControl : {
		debugMouseDown : function(controlElement, onclick, onactivate) {
			controlElement.enableTimer = setTimeout(function() {
				controlElement.onclick = onclick;
				onactivate(controlElement);
			}, 2000);
		},

		debugMouseUp : function(controlElement, onclick) {
			clearTimeout(controlElement.enableTimer);
			controlElement.enableTimer = null;
		},

		handleClick : function(event, controlID, progressDivID) {
			BAL.eventStopPropagation(BAL.getEvent(event));
			var delay = services.ajax.progressBarDelay;
			var progDivTimeout = null;
			if (progressDivID != null) {
				var progressDiv = document.getElementById(progressDivID);
				if (progressDiv != undefined) {
					if (delay > 0) {
						progDivTimeout = window.setTimeout(function() {
							progressDiv.style.display = "block";
							progDivTimeout = null;
						}, delay);
					} else {
						progressDiv.style.display = "block";
					}
				}
			}
			var callback;
			if (progressDivID != null) {
				callback = function() {
					try {
						if (progDivTimeout != null) {
							window.clearTimeout(progDivTimeout);
						} else {
							document.getElementById(progressDivID).style.display = "none";
						}
					} catch (e) {
						// maybe document is no longer visible
						// so this must fail.
					}
				};
			} else {
				callback = null;
			}
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "activate",
				controlID : controlID
			}, true, callback);
		}
	},

	ChoiceControl : {
		handleClick : function(optionInput, controlId, showWait) {
			// Find control parent element.
			var controlElement = document.getElementById(controlId);

			// Compute new client-side value.
			var allCheckedOptions = new BAL.DescendantIterator(controlElement,
					function(node) {
						return (node.name == optionInput.name) && node.checked;
					});

			var newValue = [];
			while (allCheckedOptions.hasNext()) {
				var optionID = allCheckedOptions.next().value;
				newValue.push(optionID);
			}

			// Notify server.
			services.form.sendValueUpdate(controlElement, controlId, newValue, showWait);
		}
	},

	BeaconControl : {
		handleClick : function(element, controlId, asArray, showWait) {
			var controlElement = document.getElementById(controlId);

			// Update the current selection.
			var span = BAL.DOM.getFirstElementChild(controlElement);
			while (span != null) {
				var input = BAL.DOM.getFirstElementChild(span);

				if (input == element) {
					BAL.DOM.removeClass(input, "option");
					input.src = BAL.DOM.getNonStandardAttribute(input, "data-selected-image");
				} else {
					BAL.DOM.addClass(input, "option");
					input.src = BAL.DOM.getNonStandardAttribute(input, "data-option-image");
				}

				span = BAL.DOM.getNextElementSibling(span);
			}

			// Notify server.
			var elementName = asArray ? [ element.name ] : element.name;
			services.form.sendValueUpdate(controlElement, controlId, elementName, showWait);
		}
	},
	
	Checkbox : {
		handleClick : function(controlElement, controlId, showWait) {
			services.form.sendValueUpdate(controlElement, controlId,
					controlElement.checked ? "true" : "false", showWait);
		}
	},

	BooleanChoiceControl : {
		handleClick : function(controlElement, controlId, checked, showWait) {
			services.form.sendValueUpdate(controlElement, controlId, checked, showWait);
		}
	},
	
	IconSelectControl : {
		handleClick : function(element, controlId, showWait) {
			var controlElement = element.parentNode;

			var currentIndex = -1;
			var images = {};
			var img = BAL.DOM.getFirstElementChild(controlElement);
			var imageCnt = 0;
			while (img != null) {
				if (img.className.indexOf("input-image") > -1) {
					images[imageCnt] = img;
					if (img == element) {
						currentIndex = imageCnt;
					}
					imageCnt++;
				}
				img = BAL.DOM.getNextElementSibling(img);
			}
			
			var positive = BAL.DOM.getNonStandardAttribute(controlElement, "data-positive") == "true";
			var resetable = BAL.DOM.getNonStandardAttribute(controlElement, "data-resetable") == "true";
			var increment = positive ? 1 : imageCnt - 1;
			var newIndex = (currentIndex + increment) % imageCnt;
			if ((! resetable) && (newIndex == 0)) {
				newIndex = (newIndex + increment) % imageCnt;
			}
			var newValueExpr = BAL.DOM.getNonStandardAttribute(images[newIndex], "data-value");
			var newValue = eval(newValueExpr);
			
			// Update UI.
			if (currentIndex >= 0) {
				images[currentIndex].style.display = "none";
			}
			images[newIndex].style.display = "inline";
			images[newIndex].focus();
			
			// Notify server.
			services.form.sendValueUpdate(controlElement, controlId, newValue, showWait);
		}
	},	

    /*
     * DEPRECATED 
     */
	TristateControl : {
		VALUES : [ "true", "false", "none" ],

		handleOnClick : function(element, controlId) {
			var controlElement = element.parentNode;

			var currentIndex;
			var images = {};
			var img = BAL.DOM.getFirstElementChild(controlElement);
			var index = 0;
			var config;
			while (img != null) {
				if (img.type == "image") {
					if (img == element) {
						currentIndex = index;
					}
					images[index] = img;
					index++;
				} else if (img.className == "config") {
					// Surround the JSON expression with parentheses to switch
					// the interpreter to expression context. Without parentheses, the
					// starting '{' of the JSON string is interpreted as the
					// beginning of a block.
					config = eval("(" + img.firstChild.nodeValue + ")");
				}
				img = BAL.DOM.getNextElementSibling(img);
			}
			
			var currentValue = this.VALUES[currentIndex];
			
			var newValue = this.toggleValue(config, currentValue);
			var newIndex = arrayIndexOf(this.VALUES, newValue, 0);
			
			// Update UI.
			images[currentIndex].style.display = "none";
			images[newIndex].style.display = "inline";
			
			// Notify server.
			services.form.sendValueUpdate(controlElement, controlId, newValue);
		},
		
		toggleValue : function(config, currentValue) {
			if (currentValue == "true") {
				if ((!config.resetable) || config.positive) {
					return "false";
				} else {
					return "none";
				}
			} else if (currentValue == "false") {
				if (config.resetable && config.positive) {
					return "none";
				} else {
					return "true";
				}
			} else {
				if (config.positive) {
					return "true";
				} else {
					return "false";
				}
			}
		}
	},

	/**
	 * <<class>>
	 */
	GroupControl : {
		handleToggle : function(element) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "toggleCollapsed",
				controlID : element.parentNode.parentNode.parentNode.id
			});
			return false;
		}
	},

	FormGroupControl : {
		handleToggle : function(id) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "toggleCollapsed",
				controlID : id
			});
			return false;
		}
	},

	CalendarControl : {

		switchCalendarView : function(id, date, navigation, step) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "switchCalendarView",
				controlID : id,
				date : date,
				navigation : navigation,
				step: step
			});
			return false;
		},

		setCalendarDate : function(id, date) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "setCalendarDate",
				controlID : id,
				selectedDate : date
			});
			return false;
		},
		
	    changeRule: function(name, step, titleHeight, x, y) {
	        var keyframes = BAL.findKeyframesRule(name);
	        
	        if (keyframes != null) {
	        	BAL.changeRule(keyframes, step, 
	        		step + " { transform-origin: 0% 0%; transform: translate(" + x + "px," + (titleHeight + y) + "px) scaleX(0.4) scaleY(0.25); opacity: 0.2; }");
	        }
	    },
	    
		animate: function(newFragmentId, oldFragmentId, direction, animOld, animationClass, x, y) {
			var newFragment = document.getElementById(newFragmentId);
			var oldFragment = document.getElementById(oldFragmentId);

			var container = newFragment.parentNode;
			
			// Drop container.
			container.parentNode.removeChild(container);
			
			// Rip new fragment from its transport container.
			newFragment.parentNode.removeChild(newFragment);
			
			// Position fragment.
			newFragment.style.position = "absolute";
			
			var oldWidth = BAL.getElementWidth(oldFragment);
			var oldHeight = BAL.getElementHeight(oldFragment);
			BAL.setElementWidth(newFragment, oldWidth);
			BAL.setElementHeight(newFragment, oldHeight);
			
			if (!BAL.supportsAnimations()) {
				// No animations supported.
				BAL.setElementY(newFragment, BAL.getElementY(oldFragment));
				BAL.setElementX(newFragment, BAL.getElementX(oldFragment));
				
				oldFragment.parentNode.replaceChild(newFragment, oldFragment);
				return;
			}
			
			BAL.setElementX(newFragment, BAL.getElementX(oldFragment) + direction * oldWidth);
			BAL.setElementY(newFragment, BAL.getElementY(oldFragment));
			
			var titleHeight = BAL.getElementHeight(oldFragment.firstChild.firstChild);
			var contentHeight = oldHeight - titleHeight;
			
			if (animationClass == "cal_zoom_outAnimation") {
				this.changeRule(animationClass + "Frames", "100%", titleHeight, x * oldWidth / 3, y * contentHeight / 4);
			}
			else if (animationClass == "cal_zoom_inAnimation") {
				this.changeRule(animationClass + "Frames", "0%", titleHeight, x * oldWidth / 3, y * contentHeight / 4);
			}
			
			if (animOld) {
				var listener = function() {
					// Remove old fragment from DOM.
					oldFragment.parentNode.removeChild(oldFragment);
				};
				BAL.addAnimationEndListener(oldFragment, listener);

				// Insert below old fragment.
				oldFragment.parentNode.insertBefore(newFragment, oldFragment);
				
				// Start animation.
				oldFragment.classList.add(animationClass);
			} else  {
				var listener = function() {
					// Remove old fragment from DOM.
					oldFragment.parentNode.removeChild(oldFragment);
					
					BAL.removeAnimationEndListener(newFragment, listener);
					BAL.setElementY(newFragment, 0);
					BAL.setElementX(newFragment, 0);
					newFragment.classList.remove(animationClass);
				};
				
				BAL.addAnimationEndListener(newFragment, listener);
				
				// Insert above old fragment.
				oldFragment.parentNode.appendChild(newFragment);
				
				// Start animation.
				newFragment.classList.add(animationClass);
			}
		}
	},

	DateInputControl : {

		toggleCalendarVisibility : function(id) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "toggleCalendarVisibility",
				controlID : id
			});
			return false;
		}
	},
	
	TimeInputControl : {

		initializeAndOpenTimepicker : function(element, format, inputID, controlID, initialTime) {
			var timepicker = $(element).mdtimepicker({format: format, inputID: inputID, controlID: controlID, initialTime: initialTime});
			timepicker.getValue();
			timepicker.show();
			
		},
				
		handleOnChange : function(inputElement, controlId) {		
			services.form.sendValueUpdateOnce(inputElement, controlId);
					
			// Continue processing this event.
			return true;
		},
		
		sendValueParseUpdate : function(inputElement, controlID, fieldValue, format, showWait, serverResponseCallback) {
			var updateFunction = function() {
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "valueChangedParseToFormat",
					controlID : controlID,
					value : fieldValue,
					format : format
				}, showWait, serverResponseCallback);
			};
		
			updateFunction();		
		}
	},

	ColorChooserControl : {
		click : function(element, controlId) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "openSelectionDialog",
				controlID : controlId,
				color : element.style.backgroundColor
			});
		},

		setColor : function(controlId) {
			services.ajax
					.execute(
							"dispatchControlCommand",
							{
								controlCommand : "setSelectedColor",
								controlID : controlId,
								color : services.ajax.topWindow.colorChooser.colorOfElement.HEX
							});

			return false;
		}
	},
	
	ColorDisplay: {
		selectColor : function(controlId) {
			services.ajax.execute("dispatchControlCommand",
				{
					controlCommand : "selectColor",
					controlID : controlId
				}
			);
		}
	},

	IconChooserControl : {

		setThemeImageCommand : function(controlID, event) {
			var themeImage = services.form.IconChooserControl.findIcon(event.srcElement);
			if(themeImage != null) {
				services.ajax.execute("dispatchControlCommand",
					{
						controlCommand : "setThemeImageCommand",
						controlID : controlID,
						themeImage: themeImage
					}
				);	
			}
		},
		
		themeImageIntoStringFieldCommand : function(controlID, event) {
			var themeImage = services.form.IconChooserControl.findIcon(event.srcElement);
			if(themeImage != null) {
				services.ajax.execute("dispatchControlCommand",
					{
						controlCommand : "themeImageIntoStringFieldCommand",
						controlID : controlID,
						themeImage: themeImage
					}
				);	
			}
		},
		
		findIcon : function(node) {
			var themeImage = BAL.DOM.getNonStandardAttribute(node, "data-icon");
			if(themeImage != null) {
				return themeImage;
			} else {
				var parent = node.parentNode;
				if (parent != null && BAL.DOM.isElementNode(parent)) {
					return services.form.IconChooserControl.findIcon(parent);
				} else {
					return null;
				}
			}
		}, 

		setThemeImageExpertCommand : function(controlID) {
			services.ajax.execute("dispatchControlCommand",
				{
					controlCommand : "setThemeImageCommand",
					controlID : controlID
				}
			);	
		},		

		resetThemeImageCommand : function(controlID) {
			services.ajax.execute("dispatchControlCommand",
				{
					controlCommand : "setThemeImageCommand",
					controlID : controlID,
					themeImage: null
				}
			);	
		},
		
		switchModeCommand : function(controlID) {
			services.ajax.execute("dispatchControlCommand",
				{
					controlCommand : "switchModeCommand",
					controlID : controlID
				}
			);	
		}		
	},
	
	BreadcrumbControl : {
		showMenu : function(element, menuID, controlID, controlCommand) {
			var outermostDocument = BAL.getTopLevelDocument();
			var menu = BAL.DOM.importNode(outermostDocument, document.getElementById(menuID));
			BAL.DOM.removeClass(menu, "breadcrumbMenu");
			BAL.DOM.addClass(menu, "breadcrumbMenuVisible");
			var remove = function() {
				outermostDocument.body.removeChild(menu);
			};
			outermostDocument.body.appendChild(menu);
			var clickFunction = createHideWaitpaneOnClickFunction(remove);

			PlaceDialog.placeDialog( {
				element : element,
				content : menu
			});
			showClickResponsiveWaitpane(clickFunction);

			var iterator = new BAL.DescendantIterator(menu, function(node) {
				return node.nodeName.toUpperCase() == "DIV";
			});

			while (iterator.hasNext()) {
				var divNode = iterator.next();
				(function(nodeId) {
					divNode.onclick = function() {
						hideClickResponsiveWaitpane(clickFunction);
						services.ajax.execute('dispatchControlCommand', {
							controlCommand : controlCommand,
							controlID : controlID,
							id : nodeId
						});
						return false;
					};
				})(divNode.id.substr(menuID.length));
			}
		}
	},

	TabBarControl : {
		
		handleClick : function(controlID, newSelection) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "tabSwitch",
				newSelection : newSelection,
				controlID : controlID
			});
		}	   
	},

	ImageControl : {
		
		/* called from ImageControl*/
		init: function(ctrlID, sizeContainerId, doRespectVerticalScrollbar, doRespectHorizontalScrollbar, updateDynamically) {
			var imageContainer = document.getElementById(ctrlID);
			services.form.ImageControl.updateImage(ctrlID, sizeContainerId, doRespectVerticalScrollbar, doRespectHorizontalScrollbar);
			if (updateDynamically) {
				var updateImageFunction = function() {
					imageContainer.updateTimer = null;
					services.form.ImageControl.updateImage(ctrlID, sizeContainerId, doRespectVerticalScrollbar, doRespectHorizontalScrollbar);
				}
				var deferredUpdateImageFunction = function() {
					if (imageContainer.updateTimer != null) {
						window.clearTimeout(imageContainer.updateTimer);
					}
					imageContainer.updateTimer = window.setTimeout(updateImageFunction, 100);
				}
				var removeResizeListener = function() {
					BAL.removeEventListener(window, 'resize', deferredUpdateImageFunction);
					BAL.removeEventListener(imageContainer, 'DOMNodeRemovedFromDocument', removeResizeListener);
				}
				BAL.addEventListener(window, 'resize', deferredUpdateImageFunction);
				BAL.addEventListener(imageContainer, 'DOMNodeRemovedFromDocument', removeResizeListener);
				
				// Reference used in GanttChartDisplay to trigger updates from layouting.
				imageContainer.resizeFunction = deferredUpdateImageFunction;
			}
		},
		
		updateImage : function(ctrlID, sizeContainerId, doRespectVerticalScrollbar, doRespectHorizontalScrollbar) {
			var self = services.form.ImageControl;
			var element = document.getElementById(sizeContainerId);
			if(element.runningImageUpdate != true) {
				var width = BAL.getElementWidth(element);
				var height = BAL.getElementHeight(element);
				if (doRespectVerticalScrollbar) {
					width = width - BAL.getVerticalScrollBarWidth();
				}
				if (doRespectHorizontalScrollbar) {
					height = height - BAL.getHorizontalScrollBarHeight();
				}
				if(self.hasChangedDimensions(element, width, height)) {
					element.oldWidth = width;
					element.oldHeight = height;
					element.runningImageUpdate = true;
					services.ajax.execute("dispatchControlCommand", {
						controlCommand : "requestImage",
						controlID : ctrlID,
						width : width,
						height: height,
						isSystemCommand: true
					}, false);
				}
			} else {
				element.requestImageUpdate = true;
			} 
		},
	
		hasChangedDimensions: function(element, currentWidth, currentHeight) {
			return element.oldWidth == null || element.oldWidth != currentWidth ||
			element.oldHeight != currentHeight;
		},
		
		/* called from ImageControl*/
		checkForBufferedUpdateRequests: function(ctrlID, sizeContainerId, doRespectVerticalScrollbar, doRespectHorizontalScrollbar) {
			var self = services.form.ImageControl;
			var element = document.getElementById(sizeContainerId);
			element.runningImageUpdate = false;
			if(element.requestImageUpdate == true) {
				element.requestImageUpdate = false;
				self.updateImage(ctrlID, sizeContainerId, doRespectVerticalScrollbar, doRespectHorizontalScrollbar);
			}
		}
	},

	SelectionControl : {
		outermostWindow : services.ajax.topWindow,
		selectedEntry : 0,
		matchingEntries : 0,
		visible : false,
		hideFloater : undefined,
		image : undefined,
		timerID : null,
		delay : 500,

		handleOnChange : function(inputElement, ctrlID, showWait) {
			services.form.SelectionControl._closeAndUpdate(inputElement, ctrlID, showWait);
			return true;
		},
		
		_closeAndUpdate: function(inputElement, ctrlId, showWait) {
			services.form.sendValueUpdateOnce(inputElement, ctrlId, showWait);
			services.form.SelectionControl.hideAutoCompletionFloater();
			if (this.mouseout != null) {
				this.mouseout.call();
			}
		},

		/**
		 * Handling of mouse over events for the displayed autocompletion
		 * options. This method calls the function that was attached to the
		 * floater when it was created. This is necessary to keep the events in
		 * the same context (means document). Otherwise the element would not be
		 * accessible via document.getElementById.
		 * 
		 * @param {String}
		 *            ctrlID
		 * @param {Event}
		 *            e
		 * @param {String}
		 *            divID
		 */
		handleOnMouseOver : function(ctrlID, e, divID) {
			var sifID = ctrlID + "-sif";
			var theFloater = document.getElementById(sifID);
			var theHandler = theFloater.onMouseoverFunction;
			theHandler.call(this, ctrlID, e, divID);
			return true;
		},

		/**
		 * The actual function that is called for mouse over.
		 * 
		 * @param {String}
		 *            ctrlID
		 * @param {Event}
		 *            e
		 * @param {String}
		 *            divID
		 */
		mouseOver : function(ctrlID, e, divID) {
			var lastSelection = services.form.SelectionControl.selectedEntry;
			services.form.SelectionControl.selectedEntry = divID;

			services.form.SelectionControl
					.replaceClasses(ctrlID, lastSelection,
							services.form.SelectionControl.selectedEntry);
		},

		/**
		 * Handling of mouse click events. This method calls the click function
		 * that was registered when the floater was created (@see
		 * handleOnMouseOver).
		 * 
		 * @param {String}
		 *            ctrlID
		 * @param {Event}
		 *            e
		 * @param {String}
		 *            divID
		 * @param {String}
		 *            inputFieldID
		 * @param {String}
		 *            selectionSeparator
		 * @param {Boolean}
		 *            isMultiSelect
		 */
		handleOnClick : function(ctrlID, e, divID, inputFieldID, selectionSeparator, selectionSeparatorFormat, isMultiSelect) {
			var sifID = ctrlID + "-sif";
			var theFloater = document.getElementById(sifID);
			theFloater.inputElement.onchange = theFloater.inputElement.outerOnChange;
			theFloater.inputElement.onblur = theFloater.inputElement.outerOnBlur;
			var theHandler = theFloater.onClickFunction;
			theHandler.call(this, ctrlID, e, divID, inputFieldID, selectionSeparator, selectionSeparatorFormat, isMultiSelect);
			return true;
		},
		
		handleNOOPClick : function(ctrlId) {
			var autoCompletion = services.form.AutoCompletion;
			var floaterID = ctrlId + "-sif";
			var floater = document.getElementById(floaterID);
			var inputElement = floater.inputElement;
			var cursorPosition = autoCompletion.getPosition(inputElement);
			autoCompletion.setPosition(cursorPosition, inputElement);
			
			return false;
		},

		/**
		 * The actual function that is called for mouse clicks. The value of the
		 * underlying inputfield is updated and the floater is removed.
		 * 
		 * @param {String}
		 *            ctrlID
		 * @param {Event}
		 *            e
		 * @param {String}
		 *            divID
		 * @param {String}
		 *            inputFieldID
		 * @param {String}
		 *            selectionSeparator
		 * @param {String}
		 *            selectionSeparatorFormat
 		 * @param {Boolean}
		 *            isMultiSelect
		 */
		divClick : function(ctrlID, e, divID, inputFieldID, selectionSeparator, selectionSeparatorFormat, isMultiSelect) {
			var self = services.form.SelectionControl;
			var inputField = document.getElementById(inputFieldID);
			services.form.AutoCompletion.setPosition(self.cursorPosition, inputField);
			self.selectedEntry = divID;
			self.insertSelectedEntry(inputField,
					ctrlID, isMultiSelect, selectionSeparator, selectionSeparatorFormat);
			self.hideAutoCompletionFloater();
			delete self.cursorPosition;
		},

		handleOnBlur : function(ctrlID, inputFieldID,  showWait) {
			var inputElement = document.getElementById(inputFieldID);
			services.form.SelectionControl._closeAndUpdate(inputElement, ctrlID, showWait);
			return true;
		},

		/**
		 * Handling of keyDown events. For some reasons there were problems if
		 * this was done in keyUp so the pressed key has to be added manually if
		 * needed. 1. up and down arrow cannot be caught if keyPress is captured
		 * 2. event bubbling order in IE makes it impossible to stop propagation
		 * in this element (BAL.eventStopPropagation()).
		 * 
		 * @param {Element}
		 *            inputElement
		 * @param {String}
		 *            ctrlID
		 * @param {Event}
		 *            e
		 * @param {String}
		 *            selectionSeparator - separator between selected options
		 * @param {String}
		 *            selectionSeparatorFormat - separator format
		 * @param {Boolean}
		 * 			  isMultiSelect - whether this field can contain multiple selected options or not.
		 */
		handleOnKeyDown : function(inputElement, ctrlID, e, selectionSeparator, selectionSeparatorFormat, isMultiSelect) {
			var theEvent = BAL.getEvent(e);
			var keyCode = BAL.getKeyCode(theEvent);
			
			switch (keyCode) {
			case 16: // Shift
			case 17: // Ctrl
				return false;
			case 33: // Page up 
			case 34: // Page down
				if(!BAL.hasCtrlModifier(theEvent)) {
					break;
				}
				
				// Complex Tab, do the same as Tab
				
			case 9: // Tab
				this.hideAutoCompletionFloater();
				return true;
			case 13: // Enter
				if (this.autoCompletionEntriesDisplayed()) {
					this.insertSelectedEntry(inputElement, ctrlID, isMultiSelect, selectionSeparator, selectionSeparatorFormat);
				}
				if(this.visible) {
					this.hideAutoCompletionFloater();
					BAL.eventStopPropagation(theEvent);
					return false;
				} else {
					return true;
				}
			case 27: // Esc
				if (this.visible) {
					this.hideAutoCompletionFloater();
					BAL.eventStopPropagation(theEvent);
					return false;
				} else {
					return true;
				}
			case 38: // up arrow
				if (this.autoCompletionEntriesDisplayed()) {
					this.selectPrevious(ctrlID);
					search = true;
				}
				if (this.visible) {
					BAL.eventStopPropagation(theEvent);
					return false;
				} else {
					return true;
				}
			case 40: // down arrow
				if (this.autoCompletionEntriesDisplayed()) {
					this.selectNext(ctrlID);
					search = true;
				}
				if (this.visible) {
					BAL.eventStopPropagation(theEvent);
					return false;
				} else {
					return true;
				}
			case 46: // Entf
			case 8: // Backspace
			case 37: // left arrow
			case 39: // right arrow
			default:
				this.adjustAutoCompletion(ctrlID, inputElement, selectionSeparator);
				this.saveCursorPosition(inputElement);
				break;
			}
			
			return true;
		},
		
		autoCompletionEntriesDisplayed: function() {
			return this.visible && this.matchingEntries > 0;
		},
		
		adjustAutoCompletion: function(ctrlId, inputElement, selectionSeparator) {
			window.setTimeout(function() {
				var self = services.form.SelectionControl;
				
				// 1. the floater is only shown, if the input is at least 3
				// chars long.
				// 2. instead of updating every char a timeout is used to reduce
				// communication
				// with the server. An update is only send, if no new char was
				// entered for 'delay' ms.
				
				// Determine caret position
				var cursorPosition = services.form.AutoCompletion.getPosition(inputElement);
				
				var start = services.form.AutoCompletion.getStartPosition(inputElement.value, cursorPosition, selectionSeparator);
				var length = cursorPosition - start;
				if (length >= 3) {
					self.updateServerState(ctrlId, inputElement, selectionSeparator);
				} else {
					self.hideAutoCompletionFloater();
				}
			}, 0);
			
		},
		
		saveCursorPosition: function(inputElement) {
			window.setTimeout(function() {
				var self = services.form.SelectionControl;
				self.cursorPosition = services.form.AutoCompletion.getPosition(inputElement); 				
			}, 0);
		},
		
		hideAutoCompletionFloater: function() {
			var self = services.form.SelectionControl;
			if (self.hideFloater != null) {
				self.hideFloater.call();
			}
			// remove pending timers, floater shall not be shown
			if (self.timerID != null) {
				window.clearTimeout(self.timerID);
				self.timerID = null;
			}
			self.selectedEntry = 0;
			self.visible = false;
		},
		
		updateServerState: function(ctrlID, inputField, selectionSeparator) {
			var self = services.form.SelectionControl;
			var delayedCall = function() {
				var cursorPosition = services.form.AutoCompletion.getPosition(inputField);
				var valueStartPosition = services.form.AutoCompletion.getStartPosition(inputField.value, cursorPosition, selectionSeparator);
				
				var inputFieldValue = inputField.value.substring(valueStartPosition, cursorPosition);
				services.form.SelectionControl.handleInput(
						inputField, ctrlID, inputFieldValue);
			};
			
			if (self.timerID == null) {
				self.timerID = setTimeout(delayedCall, self.delay);
			} else {
				window.clearTimeout(self.timerID);
				self.timerID = setTimeout(delayedCall, self.delay);
			}
		},
		
		/**
		 * Handling of input. If no floater is visible, a new one is created an
		 * copied to the outer document.
		 * 
		 * @param {Element}
		 *            inputElement
		 * @param {String}
		 *            ctrlID
		 * @param {String}
		 *            elemVal
		 */
		handleInput : function(inputElement, ctrlID, elemVal) {
			if (!this.visible) {
				this.hideFloater = this.showFloater(inputElement, ctrlID);
			}
			this.updateFloater(elemVal, ctrlID);
			this.timerID = null;
		},

		/**
		 * Copies the value of the selected option into the underlying input
		 * field. Afterwards a handleOnChange is fired, to inform the server of
		 * a changed value.
		 * 
		 * @param {Element}
		 *            inputElement
		 * @param {String}
		 *            ctrlID
		 * @param {Boolean}
		 * 			  isMultiSelect
		 * @param {String}
		 * 			  selectionSeparator
		 * @param {String}
		 * 			  selectionSeparatorFormat
		 */
		insertSelectedEntry : function(inputElement, ctrlID, isMultiSelect, selectionSeparator, selectionSeparatorFormat) {
			var autoCompletion = services.form.AutoCompletion;
			
			// Determine caret position
			var cursorPosition = autoCompletion.getPosition(inputElement);
			
			// Determine search word indices around caret position
			var insertStartPosition = autoCompletion.getStartPosition(inputElement.value, cursorPosition, selectionSeparator);
			var insertStopPosition = cursorPosition;
			
			if(inputElement.value.indexOf(selectionSeparatorFormat, insertStopPosition) == insertStopPosition) {
				insertStopPosition += selectionSeparatorFormat.length;
			} else if(inputElement.value.indexOf(selectionSeparator, insertStopPosition) == insertStopPosition) {
				insertStopPosition += selectionSeparator.length;
			}
			
			var sifID = ctrlID + "-sif";
			var outermostDocument = this.outermostWindow.document;
			var theDiv = outermostDocument.getElementById(sifID + "_"
					+ this.selectedEntry);
			var insertedValue = theDiv.firstChild.nodeValue;
			if(isMultiSelect) {
				inputElement.value = inputElement.value.substring(0, insertStartPosition) + insertedValue + selectionSeparatorFormat + inputElement.value.substring(insertStopPosition, inputElement.value.length);
				autoCompletion.setPosition(insertStartPosition + insertedValue.length + selectionSeparatorFormat.length, inputElement);
			} else {
				inputElement.value = insertedValue;
				autoCompletion.setPosition(insertedValue.length, inputElement);
			}
		},

		/**
		 * Updates a displayed floater. First the theme based Slider.gif is
		 * displayed, to show server activity. Then an ajax request is send to
		 * the server to update the floater with the matching entries.
		 * 
		 * @param {String}
		 *            elemVal
		 * @param {String}
		 *            ctrlID
		 */
		updateFloater : function(elemVal, ctrlID) {
			var sifID = ctrlID + "-sif";
			var sifDiv = this.outermostWindow.document.getElementById(sifID);
			sifDiv.innerHTML = "<img src='" + this.image + "' alt='loading' />";
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "autoCompletion",
				controlID : ctrlID,
				elementID : sifID,
				enteredValue : elemVal
			}, services.ajax.USE_WAIT_PANE_IN_FORMULA);
		},

		/**
		 * Creates a new floater and copies it to the mainlayout.
		 * 
		 * @param {Element}
		 *            inputElement
		 * @param {String}
		 *            ctrlID
		 */
		showFloater : function(inputElement, ctrlID) {
			var sifID = ctrlID + "-sif";

			var outermostDocument = this.outermostWindow.document;
			var sifDiv = outermostDocument.createElement('div');
			sifDiv.onClickFunction = this.divClick;
			sifDiv.onMouseoverFunction = this.mouseOver;
			sifDiv.id = sifID;
			sifDiv.innerHTML = "<img src='" + this.image + "' alt='loading' />";
			BAL.DOM.addClass(sifDiv, "sifFloater");
			BAL.setStyle(sifDiv, "min-width: " + BAL.getElementWidth(inputElement) + "px");
			outermostDocument.body.appendChild(sifDiv);

			sifDiv.inputElement = inputElement;
			inputElement.outerOnChange = inputElement.onchange;
			inputElement.outerOnBlur = inputElement.onblur;

			// if a selection is done via mouse, the inputField would normally
			// fire two events: onchange and onblur.
			// This is not wanted in this case, because it would trigger
			// unwanted servercommunication. So the event handling
			// has to be removed, when the mouse enters the floater and restored
			// when either the mouse leaves the floater
			// or when a selection was made. To keep the old eventhandlig
			// functions accessible they are stored as attributes
			// of the inputElement.
			var onmouseover = function() {
				inputElement.onchange = null;
				inputElement.onblur = null;
			};

			var onmouseout = function() {
				inputElement.onchange = inputElement.outerOnChange;
				inputElement.onblur = inputElement.outerOnBlur;
			};

			var doHideFloater = function(event) {
				var autoCompletion = services.form.AutoCompletion;
				var cursorPosition = autoCompletion.getPosition(inputElement);
				autoCompletion.setPosition(cursorPosition, inputElement);
				inputElement.onchange = inputElement.outerOnChange;
				inputElement.onblur = inputElement.outerOnBlur;
				outermostDocument.body.removeChild(sifDiv);
				services.form.SelectionControl.visible = false;
				services.form.SelectionControl.hideFloater = null;
				if(event != null) {
					var balEvent = BAL.getEvent(event);
					BAL.eventStopPropagation(balEvent);
					BAL.cancelEvent(balEvent);
				}
				return false;
			};
			var clickFunction = createHideWaitpaneOnClickFunction(doHideFloater);

			BAL.addEventListener(sifDiv, 'mouseout', onmouseout);
			BAL.addEventListener(sifDiv, 'mouseover', onmouseover);

			this.visible = true;
			this.input = inputElement;

			PlaceDialog.placeDialog( {
				element : inputElement,
				content : sifDiv
			});
			
			showClickResponsiveWaitpane(clickFunction);
			
			return clickFunction;
		},

		/***********************************************************************
		 * Navigating through the smart input suggestions
		 */
		selectPrevious : function(ctrlID) {
			var lastSelection = services.form.SelectionControl.selectedEntry;
			if ((services.form.SelectionControl.selectedEntry - 1) >= 0) {
				services.form.SelectionControl.selectedEntry -= 1;
			} else if (this.matchingEntries == 0) {
				services.form.SelectionControl.selectedEntry = 0;
			} else {
				services.form.SelectionControl.selectedEntry = this.matchingEntries - 1;
			}
			this.replaceClasses(ctrlID, lastSelection,
					services.form.SelectionControl.selectedEntry);
		},

		selectNext : function(ctrlID) {
			var lastSelection = services.form.SelectionControl.selectedEntry;
			if ((services.form.SelectionControl.selectedEntry + 1) < this.matchingEntries) {
				services.form.SelectionControl.selectedEntry += 1;
			} else {
				services.form.SelectionControl.selectedEntry = 0;
			}
			this.replaceClasses(ctrlID, lastSelection,
					services.form.SelectionControl.selectedEntry);
		},

		/**
		 * Gets the two divs from the floater that are represented by the two
		 * passed in ids oldID and newID and changes the classes.
		 * 
		 * The oldID-Div changes from selected to normal, the newID-Div from
		 * normal to selected.
		 * 
		 * @param {String}
		 *            ctrlID
		 * @param {int}
		 *            oldID
		 * @param {int}
		 *            newID
		 */
		replaceClasses : function(ctrlID, oldID, newID) {
			var outermostDocument = services.form.SelectionControl.outermostWindow.document;
			var sifID = ctrlID + "-sif";

			var theOldDiv = outermostDocument.getElementById(sifID + "_"
					+ oldID);
			var theDiv = outermostDocument.getElementById(sifID + "_" + newID);
			BAL.DOM.replaceClass(theDiv, "sifSearchElement",
					"sifSearchElementSelected");
			BAL.DOM.replaceClass(theOldDiv, "sifSearchElementSelected",
					"sifSearchElement");
		},

		PatternField : {

			onInputTimer : null,

			/**
			 * this method is used on the opened dialog. its the 'oninput'
			 * function of the pattern field
			 * 
			 * @param {Event}
			 *            on key up event
			 * @param {String}
			 *            id of the control representing the pattern field
			 * @param {String}
			 *            id of the HTML input element of the pattern field
			 * @param {String}
			 *            control id of the control representing the list of
			 *            options to select from
			 * @param {String}
			 *            control id of the control representing the list of
			 *            current selected options. may be null if no such list
			 *            is displayed.
			 * @param {String}
			 *            id of the input element used to change current
			 *            displayed page. may be null if no such element is
			 *            displayed.
			 */
			handleInput : function(event, controlID, inputElementID,
					optionElementID, selectionElementID, pageElementID) {
				event = BAL.getEvent(event);
				var keyCode = BAL.getKeyCode(event);
				var self = this;

				if (BAL.isModifierKey(keyCode)) {
					return true;
				}
				if (BAL.isCursorKey(keyCode)) {
					return true;
				}
				if (BAL.isTabKey(keyCode)) {
					return true;
				}
				if (BAL.hasAltModifier(event)) {
					return true;
				}
				if (BAL.isEscapeKey(keyCode)) {
					return true;
				}
				if (BAL.isReturnKey(keyCode)) {
					return true;
				}
				
				if (! self.clearUpdateTimer()) {
					self.disableListBoxes(optionElementID, selectionElementID, pageElementID);
				}
				
				self.onInputTimer = window.setTimeout(function() {
					self.onInputTimer = null;
					self.sendPatternUpdate(controlID, inputElementID, optionElementID, selectionElementID, pageElementID);
				}, services.form.SelectionControl.delay);

				return true;
			},
			
			disableListBoxes : function(optionElementID, selectionElementID, pageElementID) {
				if (optionElementID != null) {
					services.form.disableGUI(optionElementID);
				}
				if (selectionElementID != null) {
					services.form.disableGUI(selectionElementID);
				}
				if (pageElementID != null) {
					// cannot disable the same way as select control has span as control tag with height = 0
					var selectbox = document.getElementById(pageElementID);
					// may be null as select box is currently build, also if not displayed 
					if (selectbox != null) {
						services.form.addOverlay(selectbox);
					}
				}
			},
			
			enableListBoxes : function(optionElementID, selectionElementID, pageElementID) {
				if (optionElementID != null) {
					services.form.enableGUI(optionElementID);
				}
				if (selectionElementID != null) {
					services.form.enableGUI(selectionElementID);
				}
				if (pageElementID != null) {
					// cannot disable the same way as select control has span as control tag with height = 0
					var selectbox = document.getElementById(pageElementID);
					// may be null as select box is currently build, also if not displayed 
					if (selectbox != null) {
						services.form.removeOverlay(selectbox);
					}
				}
			},
			
			sendPatternUpdate : function(controlID, inputElementID, optionElementID, selectionElementID, pageElementID) {
				var self = this;
				
				// Fake change event to enforce option list update.
				var patternInput = document.getElementById(inputElementID);
				if (patternInput != null) {
					// Timer executed after dialog was closed. This can happen, because ESC closes 
					// the dialog during keydown, but the pattern field handler runs only on key up. 
					// Therefore, there is no chance to stop the timer if ESC or ENTER is pressed.
					var callback = function() {
						// Enable list boxes at the time the result of the pattern change arrives.
						self.enableListBoxes(optionElementID, selectionElementID, pageElementID);
					};
					services.form.TextInputControl.handleOnChange(patternInput, controlID, true, callback);
				}
			},
			
			clearUpdateTimer : function() {
				var self = this;
				var hasTimer = self.onInputTimer != null;
				if (hasTimer) {
					window.clearTimeout(self.onInputTimer);
					self.onInputTimer = null;
				}
				return hasTimer;
			}
		},
		
		handleOnDragOver: function(event, input) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			if(!window.tlDnD || !window.tlDnD.data){
				event.dataTransfer.dropEffect = 'none';
				return;
			}
			
			return false;
		},
		
		handleOnDragEnter: function(event, input) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			return false;
		},
		
		handleOnDragLeave: function(event, input) {
			var event = BAL.getEvent(event);
			event.preventDefault();
			
			return false;
		},
		
		handleOnDrop: function(event, input) {
			var event = BAL.getEvent(event);
			event.preventDefault();

			var data = window.tlDnD.data;
			
        	services.ajax.execute("dispatchControlCommand", {
        		controlCommand : "dndFieldDrop",
    			controlID : this.controlElement(input).id,
    			data : data
        	}, services.ajax.USE_WAIT_PANE_IN_FORMULA);
			
			return false;
		},
		
		controlElement: function(element) {
            while (element != null) {
            	if (BAL.DOM.containsClass(element, "cPopupSelect")) {
            		return element;
            	}

            	element = element.parentNode;
            }
            return null;
		}
	},
	
	DropDownControl: {
		// CSS classes
		containerCl: "ddwttContainer",
		buttonCl: "ddwttDropBtn",
		activeCl: "ddwttDropBtnActive",
		searchCl: "ddwttSearch",
		hideCl: "ddwttHide",
		boxCl: "ddwttDDBox",
		listCl: "ddwttDDList",
		itemCl: "ddwttItem",
		selItemCl: "ddwttSelectedItem",
		actItemCl: "ddwttActiveItem",
		mutObserver: null,

		buttonDrop: function(button) {
			const ddBoxOriginal = button.nextElementSibling;
			let ddBox = this.getDDBox();

			const onGlobalChange = function() {
				if (ddBox.contains(document.activeElement)) {
					services.form.DropDownControl.buttonDrop(button);
				}
			};

			let prevActive = button.parentElement.classList.contains(this.activeCl);
			button.parentElement.classList.toggle(this.activeCl);
			

			if (prevActive) {
				this.closeDD(button, ddBox);
				if (this.mutObserver) {
					this.mutObserver.disconnect();
					this.mutObserver = null;
				}
			} else {
				const outerDocument = document.body.firstElementChild;
				ddBox = ddBoxOriginal.cloneNode(true);
				outerDocument.append(ddBox);
				const ddList = ddBox.querySelector("." + this.listCl);
				let activeItem = this.getActiveItem(button, ddList);

				this.positionDD(button, ddBox);
				if (activeItem) {
					this.setItemActive(activeItem, true);
					this.addScrollEvents(button, onGlobalChange);
				}
				
				let dialog = button.closest(".dlgWindow");
				if (dialog) {
					this.setMutationObserver(dialog, button);
				}
			}
		},
		
		getDDBox: function() {
			return document.body.firstElementChild.querySelector(":scope > ." + this.boxCl);
		},
		
		getButton: function(ddBox) {
			return document.body.firstElementChild.querySelector("#" + ddBox.dataset.ctrlid + " ." + this.buttonCl);
		},

		closeDD: function(button, ddBox) {
			// reset chevron to default (right)
			button.classList.remove("down");
			button.classList.remove("up");

			// reset & hide search
			const search = ddBox.querySelector("." + this.searchCl);
			search.value = "";
			search.classList.add(this.hideCl);
			
			PlaceDialog.closeCurrentTooltip(document.body.firstElementChild);

			this.cancelScrollEvents(button);

			if (ddBox.contains(document.activeElement)) {
				button.focus();
			}
			
			// hide DropDown
			ddBox.remove();
		},
		
		setMutationObserver: function(dialog, button) {
			// Options for the observer (which mutations to observe)
			const config = {attributeFilter: ["style"], attributeOldValue: true};
			
			// Callback function to execute when mutations are observed
			const callback = (mutationList) => {
				mutationList.forEach((mutation) => {
					switch (mutation.type) {
						case "attributes":
							this.buttonDrop(button);
							break;
					}
				});
			};
			this.mutObserver = new MutationObserver(callback);
			this.mutObserver.observe(dialog, config);
		},

		cancelScrollEvents: function(button) {
			if (button.controller) {
				button.controller.abort();
				delete button.controller;
			}
		},

		getActiveItem: function(button, ddList) {
			// search for previous active item
			let activeItem = ddList.querySelector(":scope > ." + this.actItemCl);
			// if there was no previous active item
			if (!activeItem) {
				for (item of ddList.children) {
					if (this.isDisplayedItem(item)) {
						if (!activeItem) {
							// get first item that is displayed
							activeItem = item;
						}
						// if there is a selected item in single mode
						if (item.firstElementChild.textContent == button.textContent) {
							// return this instead
							return activeItem = item;
						}
					}
				}
			}
			return activeItem;
		},

		addScrollEvents: function(button, onGlobalChange) {
			button.controller = new AbortController();
			const signal = button.controller.signal;
			window.addEventListener("resize", onGlobalChange, { once: true, signal });
			let scrollParent = button;
			while (scrollParent.parentElement) {
				scrollParent = this.getClosestParent(scrollParent, "overflow", ["auto", "scroll"]);
				if (scrollParent) {
					scrollParent.addEventListener("scroll", onGlobalChange, { once: true, signal });
				} else {
					break;
				}
			}
		},

		getClosestParent: function(element, property, values) {
			let parent = element.parentElement,
				propVal = window.getComputedStyle(parent).getPropertyValue(property);

			if (values.some((value) => propVal.includes(value))) {
				return parent;
			} else {
				if (parent.parentElement) {
					return this.getClosestParent(parent, property, values);
				} else {
					return null;
				}
			}
		},
		
		positionDD: function(button, ddBox) {
			ddBox.style.left = 0;
			ddBox.style.top = 0;

			let btnPos = button.getBoundingClientRect(),
				ddBoxPos = ddBox.getBoundingClientRect(),
				hWindow = window.innerHeight;

			let bottomSpace = hWindow - btnPos.bottom,
				ddMaxHeight = bottomSpace;

			if ((bottomSpace >= ddBoxPos.height) || (bottomSpace >= btnPos.top)) {
				this.openDown(button, ddBox);
			} else {
				ddMaxHeight = btnPos.top;
				this.openUp(button, ddBox);
			}
			this.setDimensions(btnPos, ddBox, ddMaxHeight);
		},

		openDown: function(button, ddBox) {
			let btnPos = button.getBoundingClientRect();
			button.classList.add("down");
			ddBox.style.removeProperty("bottom");
			ddBox.style.setProperty("top", btnPos.bottom + "px");
			ddBox.style.setProperty("flex-direction", "column");
		},

		openUp: function(button, ddBox) {
			let btnPos = button.getBoundingClientRect();
			button.classList.add("up");
			ddBox.style.removeProperty("top");
			ddBox.style.setProperty("bottom", (window.innerHeight - btnPos.top) + "px");
			ddBox.style.setProperty("flex-direction", "column-reverse");
		},

		setDimensions: function(btnPos, ddBox, ddMaxHeight) {
			ddBox.style.setProperty("left", btnPos.left + "px");
			ddBox.style.setProperty("min-width", btnPos.width + "px");
			ddBox.style.setProperty("max-height", ddMaxHeight + "px");
			let search = ddBox.querySelector(":scope > ." + this.searchCl);
			search.style.setProperty("width", window.getComputedStyle(ddBox).getPropertyValue("width"));
			search.focus();
		},

		setItemActive: function(item, scroll) {
			let ddList = item.parentElement;
			let previousActive = ddList.querySelector(":scope > ." + this.actItemCl);
			if (previousActive) {
				if (previousActive == item) return;
				this.setItemInactive(previousActive);
			}
			item.classList.add(this.actItemCl);

			if (scroll) {
				item.scrollIntoView({ behavior: "smooth", block: "nearest", inline: "nearest" });
			}
			
			const mouseoverEvent = new Event('mouseover', { 'bubbles': true });
			item.dispatchEvent(mouseoverEvent);
		},
		
		setItemInactive: function(item) {
			let tooltip = item.lastElementChild;
			if (tooltip && tooltip.childElementCount > 0) {
				tooltip.firstElementChild.scrollTop = 0;
			}
			item.classList.remove(this.actItemCl);
			
			const mouseleaveEvent = new Event('mouseleave', { 'bubbles': true });
			item.dispatchEvent(mouseleaveEvent);
		},

		lostFocus: function() {
			const ddBox = this.getDDBox();
			const button = this.getButton(ddBox);
			const ddList = ddBox.querySelector("." + this.listCl);
			let itemList = ddList.children;

			setTimeout(() => {
				if (!ddBox.contains(document.activeElement)) {
					for (item of itemList) {
						if (item.classList.contains(this.actItemCl)) {
							this.setItemInactive(item);
							let tooltip = item.lastElementChild;
							tooltip.style.display = "";
							tooltip.style.width = "";
						}
					}
					if (button.parentElement.classList.contains(this.activeCl)) {
						this.buttonDrop(button);
					}
				}
			}, 150);
		},

		keyPressed: function(event, multi) {
			let ddBox = this.getDDBox();
			let button = event.target;
			if (ddBox) {
				button = this.getButton(ddBox);
			} else {
				if (multi) {
					this.buttonDrop(button);
					ddBox = this.getDDBox();
					button = this.getButton(ddBox);
				} else {
					ddBox = button.nextElementSibling;
				}
			}
			let ddList = ddBox.querySelector("." + this.listCl);
			let sourceBtn = (event.target == button);

			let activeItem = this.getActiveItem(button, ddList);

			switch (event.key) {
				// UP ARROW was pressed
				case "ArrowUp":
					// get previous navigation item for this element
					let previous = activeItem.previousElementSibling;

					// previous is not an item or is a selected item
					while (!previous || !this.isDisplayedItem(previous)) {
						// a previous element is not defined
						if (!previous) {
							// wrap to last element
							previous = ddList.lastElementChild;
						} else {
							// go one step up the list
							previous = previous.previousElementSibling;
						}
					}
					// if event source is the button
					if (sourceBtn && !multi) {
						// set previous item as selected
						this.selectItem(previous);
						event.preventDefault();
						return;
					} else {
						// set previous item active
						this.setItemActive(previous, true);
						break;
					}

				// DOWN ARROW was pressed
				case "ArrowDown":
					// get next navigation item for this element
					let next = activeItem.nextElementSibling;

					// next is not an item or is a selected item
					while (!next || !this.isDisplayedItem(next)) {
						// a next element is not defined
						if (!next) {
							// wrap to first element
							next = ddList.querySelector(":scope > ." + this.itemCl + ":not(." + this.selItemCl + ")");
						} else {
							// go one step down the list
							next = next.nextElementSibling;
						}
					}
					// if event source is the button
					if (sourceBtn && !multi) {
						// set next item as selected
						this.selectItem(next);
						event.preventDefault();
						return;
					} else {
						// set next item active
						this.setItemActive(next, true);
						break;
					}

				// [HOME|POS1] key was pressed
				case "Home":
					// get first displayed item
					let first = ddList.querySelector(":scope > ." + this.itemCl + ":not(." + this.selItemCl + ")");
					// if event source is the button
					if (sourceBtn && !multi) {
						// set first item as selected
						this.selectItem(first);
						event.preventDefault();
						return;
					} else {
						// set first item active
						this.setItemActive(first, true);
						break;
					}

				// [PageUp|PgUp|Bild ^] was pressed
				case "PageUp":
				// [PageDown|PgDn|Bild v] was pressed
				case "PageDown":
					let tempList = ddList,
						tempActive = activeItem;
						
					if (sourceBtn && !multi) {
						this.buttonDrop(button);
						ddList = this.getDDBox().querySelector("." + this.listCl);
						activeItem = this.getActiveItem(button, ddList);
					}
					
					let pageH = ddList.getBoundingClientRect().height,
						itemH = activeItem.getBoundingClientRect().height;
						
					if (sourceBtn && !multi) {
						this.buttonDrop(button);
						ddList = tempList;
						activeItem = tempActive;
					}
					
					let itemCount = Math.trunc(pageH / itemH),
						scrollH = itemCount * itemH;

					let i = 0,
						pageItem = activeItem;
					while (i < itemCount) {
						if (event.key == "PageUp") {
							pageItem = pageItem.previousElementSibling;
						} else {
							pageItem = pageItem.nextElementSibling;
						}

						if (!pageItem) {
							break;
						}

						if (this.isDisplayedItem(pageItem)) {
							activeItem = pageItem;
							i++;
						}
					}

					if (event.key == "PageUp") {
						scrollH = - scrollH;
					}

					// if event source is the button
					if (sourceBtn && !multi) {
						// set page up/down item as selected
						this.selectItem(activeItem);
						event.preventDefault();
						return;
					} else {
						ddList.scrollBy({ top: scrollH, behavior: "smooth" });
						this.setItemActive(activeItem, false);
						return;
					}

				// [END|ENDE] was Pressed
				case "End":
					// get last item
					let last = ddList.lastElementChild;
					// while last is no displayed item
					while (!this.isDisplayedItem(last)) {
						// go one step up the list
						last = last.previousElementSibling;
					}
					// if event source is the button
					if (sourceBtn && !multi) {
						// set last item as selected
						this.selectItem(last);
						event.preventDefault();
						return;
					} else {
						// set last item active
						this.setItemActive(last, true);
						break;
					}

				// [ENTER] was pressed
				case "Enter":
					if (!sourceBtn) {
						// set current item as selected
						this.selectItem(activeItem);
						return;
					}
					event.stopImmediatePropagation();
					break;

				// [ESC] was pressed
				case "Escape":
					if (sourceBtn && !multi) {
						return;
					}
					this.buttonDrop(button);
					event.stopImmediatePropagation();
					return;

				// [Control|Ctrl|Ctl|STRG] + [C] was pressed
				case "c":
					if (event.ctrlKey && !sourceBtn) {
						return;
					}
					break;

				// any other key was pressed
				default:
					// the key was a printable character
					if (event.key.length == 1) {
						break;
					}
					// the key was no printable character
					return;
			}

			if (sourceBtn && !multi) {
				this.buttonDrop(button);
			}

			let search = this.getDDBox().querySelector(":scope > ." + this.searchCl);
			if (event.target != search) {
				search.focus();
			}
			
		},

		isDisplayedItem: function(item) {
			return (item.classList.contains(this.itemCl) && !(item.classList.contains(this.selItemCl))
				&& !(item.style.display == "none"));
		},

		search: function(search) {
			let ddList = search.parentElement.querySelector("." + this.listCl),
				itemList = ddList.children,
				inputStr = search.value.toLowerCase(),
				firstItem = false;

			search.classList.remove(this.hideCl);

			for (item of itemList) {
				this.setItemInactive(item);
				item.style.display = "";
				if (this.isDisplayedItem(item)) {
					let label = item.firstElementChild.textContent.toLowerCase();
					if (label.includes(inputStr)) {
						if (!firstItem) {
							firstItem = item;
						}
					} else {
						item.style.display = "none";
					}
				}
			}
			if (firstItem) {
				this.setItemActive(firstItem, true);
			}
		},

		selectItem: function(item) {
			const ddBox = item.parentElement.parentElement;
			const button = this.getButton(ddBox);
			let ctrlID = ddBox.dataset.ctrlid;
			
			if (button.parentElement.classList.contains(this.activeCl)) {
				this.buttonDrop(button);
			}
	
			services.ajax.execute("dispatchControlCommand", {
				controlCommand: "ddItemSelected",
				controlID: ctrlID,
				itemID: item.id
			});
		},

		changeSelectedState: function(item) {
			item.classList.toggle(this.selItemCl);
		},

		setSelectedLabel: function(button, selection) {
			button.firstElementChild.textContent = selection;
		},

		removeTag: function(tag, itemID) {
			let item = document.getElementById(itemID);
			if (!item) {
				item = tag;
				item.id = itemID;
			}
			this.selectItem(item);
		}
	},

	AutoCompletion : {
		
		/**
		 * Gibt die Position des vorhergehenden Trennzeichens (Whitespace, Separator, Eingabefeldanfang)
		 * ausgehend von der aktuellen Suchposition zur�ck.
		 * 
		 * @param {Object}
		 *            text der Text des Formelfeldes
		 * @param {Object}
		 *            pos die aktuelle Cursorposition
		 * @param {Object}
		 *            separator Trennzeichen zwischen den selektierten Optionen
		 */
		getStartPosition : function(/* String */text, /* Int */pos, separator) {
			var nonWhiteSpaceRegEx = /\S/;
			var start = pos;
			for (var i = start; i >= 0; i--) {
				var character = text.charAt(i);
				if (character == separator && i != pos) {
					break;
				}
				if(character.match(nonWhiteSpaceRegEx)) {
					start = i;
				}
			}
			return start;
		},
		
		/**
		 * Gibt die Position des nachfolgenden Trennzeichens (Whitespace, Separator, Eingabefeldanfang)
		 * ausgehend von der aktuellen Suchposition zur�ck.
		 * 
		 * @param {Object}
		 *            text der Text des Formelfeldes
		 * @param {Object}
		 *            pos die aktuelle Cursorposition
		 * @param {Object}
		 *            separator Trennzeichen zwischen den selektierten Optionen
		 */
		getStopPosition : function(/* String */text, /* Int */pos, separator) {
			var stop = pos;
			var nonWhiteSpaceRegEx = /\S/;
			for (var i = stop; i < text.length; i++) {
				var character = text.charAt(i);
				if (character == separator) {
					break;
				}
				if(character.match(nonWhiteSpaceRegEx)) {
					stop = i;
				}
			}
			return stop;
		},
		
		/**
		 * Gibt den Text vom letzten Trennzeichen (Whitespace, ; , Eingabefeldanfang) bis
		 * zur aktuellen Cusorposition zur�ck
		 * 
		 * @param {Object}
		 *            text der Text des Formelfeldes
		 * @param {Object}
		 *            pos die aktuelle Cursorposition
		 */
		getLeadingPart : function(/* String */text, /* Int */pos) {
			var start = 0;
			var regEx = /[\s(;]/;
			text = text.substr(0, pos);
			for ( var i = text.length - 1; i >= 0; i--) {
				var c = text.charAt(i);
				if (c.match(regEx)) {
					break;
				}
				start = i;
			}
			text = text.substr(start, pos);
			return text;
		},
		
		/**
		 * Gibt den Text von der aktuellen Cursorposition bis zum n�chsten Trennzeichen
		 * (Whitespace, ; , Eingabefeldende) zur�ck.
		 * 
		 * @param {Object}
		 *            text der Text des Formelfeldes
		 * @param {Object}
		 *            pos die aktuelle Cursorposition
		 */
		getTrailingPart : function(/* String */text, /* Int */pos) {
			var stop = 0;
			var regEx = /[\s(;]/;
			text = text.substr(pos, text.length - 1);
			for ( var i = 1; i < text.length; i++) {
				var c = text.charAt(i);
				if (c.match(regEx)) {
					break;
				}
				stop = i;
			}
			text = text.substr(1, stop);
			return text;
		},

		/**
		 * Returns the caret position of the InputField.
		 * 
		 * @param {Object}
		 *            input field
		 */
		getPosition : function(elem) {
			var start = 0;
			var input = elem;
			if (document.selection) { // IE
				/*
				 * Schritt 1: erweitern der Selection bis zum Ende des Strings
				 * Schritt 2a: Ist Text der Selection leer, ist Anfang der Selection
				 * Ende des Strings 2b: Suche das letzte (einzige) Vorkommen der
				 * Selection um den "Anfangsindex" zu bekommen
				 */
				var temp = document.selection.createRange().duplicate();
				temp.moveEnd('character', input.value.length);
				if (temp.text == '') {
					start = input.value.length;
				} else {
					start = input.value.lastIndexOf(temp.text);
				}
			} else { // Firefox
				start = input.selectionStart;
			}
			
			return start;
	},

	/**
	 * Takes an Inputfield and sets the caret to the position <code>pos</code>
	 * 
	 * @param {Object}
	 *            Int pos the position to move the caret
	 * @param {Object}
	 *            String id the id of the InputField
	 */
	setPosition : function(/* Int */pos, /* Element */elem) {
		var input = elem;
		input.focus();
		// IE
		if (document.selection) {
			/*
			 * Schritt 1 und 2: Erweitern der Selection auf den ganzen String,
			 * um Indexprobleme zu vermeiden Schritt 3: verschiebe Anfang auf
			 * gew�nschte Position Schritt 4: verschiebe Ende auf gew�nschte
			 * Position Schritt 5: "aktiviere" Selection Ergebnis: Cursor an
			 * richtiger Stelle positioniert
			 */
			var temp = document.selection.createRange().duplicate();
			temp.moveStart('character', -input.value.length);
			temp.moveEnd('character', input.value.length);
			temp.moveStart('character', pos);
			temp.moveEnd('character', -(input.value.length - pos));
			temp.select();
		} // Firefox
		else {
			input.selectionStart = pos;
			input.selectionEnd = pos;
		}
	}
	},

	DataItemControl: {
		
		fileNameUpdate: function(controlID, newValue) {
			var fileNames = [];
			var fileSizes = [];
			for (var i = 0; i < newValue.length; i++) {
				fileNames.push(newValue[i].name);
				fileSizes.push(newValue[i].size);
			}
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "fileNameUpdate",
				controlID : controlID,
				value: fileNames,
				size: fileSizes
			});
		},
		
		submit: function(controlID, uploadFieldID, uploadUrl, validFileIds, waitImage, waitMessage) {
			var fileupload = document.getElementById(uploadFieldID);
			
			services.ajax.showWaitPane();
			
			var formData = new FormData();
			for (var i = 0; i < validFileIds.length; i++) {
				formData.append("file", fileupload.files[validFileIds[i]]);
			}
			
			var self = this;
			fetch(uploadUrl, {
			  method: "POST", 
			  body: formData
			}).then((response) => self.uploadPerformed(controlID));
		},
		
		uploadPerformed: function(controlID) {
			services.ajax.hideWaitPane();
			
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "uploadPerformed",
				controlID : controlID
			});
		},

		showWaitDiv: function(placeElement, waitImage, waitMessage) {
			var splashDiv = document.createElement("div");
			splashDiv.id = "splashImage";
			BAL.DOM.addClass(splashDiv, "uploadSplash");
			document.body.appendChild(splashDiv);

			var innerHTML = "<img src='" + waitImage + "' alt='wait image'><span style='margin-left:2px;font-weight:bold'>" + waitMessage + "</span></img>";
			splashDiv.innerHTML = innerHTML;
			
			splashDiv.style.visibility = 'visible';
			var removeFunction = function() {
				document.body.removeChild(splashDiv);
			};
			placeElement.removeWaitFunction = createHideWaitpaneOnClickFunction(removeFunction);
			PlaceDialog.placeDialog({
				element: placeElement, 
				content: splashDiv
			});
			showClickResponsiveWaitpane(placeElement.removeWaitFunction);
		}

	},

	ListControl : {
		
		_getParentLi : function(event) {
			var target = BAL.getEventTarget(BAL.getEvent(event));
			var li = target;
			while (li != null && BAL.DOM.getTagName(li) != "li") {
				li = li.parentNode;
			}
			return li;
		},
		
		clickSelect : function(event, ctrlID) {
			event = BAL.getEvent(event);
			if (!BAL.isReturnKey(BAL.getKeyCode(event))) {
				return;
			}
			this.mouseDownSelectImpl(event, ctrlID);
			return BAL.cancelEvent(event);
		},

		mouseDownSelect : function(event, ctrlID) {
			event = BAL.getEvent(event);
			this.mouseDownSelectImpl(event, ctrlID);
			return BAL.cancelEvent(event);
		},

		mouseDownSelectImpl : function(event, ctrlID) {
			var target = BAL.getEventTarget(event);
			var focusable = null;
			var elem = target;
			while (elem != null && BAL.DOM.getTagName(elem) != "li") {
				if (BAL.DOM.canBeActive(elem)) {
					focusable = elem;
				}
				elem = elem.parentNode;
			}
			if (elem == null) {
				return false;
			}
			if (focusable != null) {
				BAL.focus(focusable);
			}
			var ctrlElem = document.getElementById(ctrlID);
			var multiSelection = BAL.DOM.getNonStandardAttribute(ctrlElem,
					"data-multiselection");
			multiSelection = multiSelection != null && multiSelection == "true";

			var selectionCache = ctrlElem.selectionCache;
			if (selectionCache == null) {
				selectionCache = services.form.ListControl
						._setSelectionCache(ctrlElem);
			}
			var ul = services.form.ListControl._getULChild(ctrlElem);
			var oldFocus = selectionCache.focus;
			var ctrlKey = BAL.hasCtrlModifier(event);
			var shiftKey = BAL.hasShiftModifier(event);

            var formerlySelected = BAL.DOM.containsClass(elem, "selected");

			var selectAction = function() {
				if (shiftKey && multiSelection) {
					// intervall must be selected
					var interval = false;
					for (var li = BAL.DOM.getFirstElementChild(ul); li != null; li = BAL.DOM
							.getNextElementSibling(li)) {
						var isInInterval = interval;
						if (li == elem || li == oldFocus) {
							interval = !interval;
							isInInterval = true;
						}
						if (isInInterval) {
							if (formerlySelected && ctrlKey) {
								BAL.DOM.removeClass(li, "selected");
								delete selectionCache.selection[li.id];
							} else {
								services.form.ListControl._setSelection(li,
										selectionCache);
							}
						} else {
							if (!ctrlKey) {
								BAL.DOM.removeClass(li, "selected");
								delete selectionCache.selection[li.id];
							}
						}
					}
				} else {
                    if (ctrlKey) {
                        // invert selection
                        if (BAL.DOM.containsClass(elem, "selected")) {
                            BAL.DOM.removeClass(elem, "selected");
                            delete selectionCache.selection[elem.id];
                        } else {
                            if (multiSelection) {
                                services.form.ListControl._setSelection(elem,
                                        selectionCache);
                            } else {
                                services.form.ListControl._setSingleSelection(
                                        elem, selectionCache);
                            }
                        }
                    } else {
                        services.form.ListControl._setSingleSelection(elem,
                                selectionCache);
                    }
                }

				if (oldFocus != null) {
					BAL.DOM.removeClass(oldFocus, "focus");
				}
				BAL.DOM.addClass(elem, "focus");
				selectionCache.focus = elem;
			};

			if (formerlySelected) {
				// run select action in mouse up as drag&drop is more important
				var removeListener = function() {
					BAL.removeEventListener(elem, 'mouseout', removeListener);
					BAL.removeEventListener(elem, 'mouseup', doSelect);
				};
				var doSelect = function() {
					removeListener();
					selectAction();
				};
				BAL.addEventListener(elem, 'mouseup', doSelect);
				BAL.addEventListener(elem, 'mouseout', removeListener);
			} else {
				selectAction();
			}

			services.ajax.execute('dispatchControlCommand', {
						controlCommand : 'mouseDownSelect',
						controlID : ctrlID,
						item : elem.id,
						shift : shiftKey,
						ctrl : ctrlKey
					}, services.ajax.USE_WAIT_PANE_IN_FORMULA);
			return false;
		},

        _setSingleSelection : function(elem, selectionCache) {
            // select all but the clicked element
            for (var i in selectionCache.selection) {
                BAL.DOM.removeClass(document.getElementById(i), "selected");
                delete selectionCache.selection[i];
            }
            services.form.ListControl._setSelection(elem, selectionCache);
        },
        
		_setSelection : function(elem, selectionCache) {
			if (!services.form.ListControl._fixedFilter(elem)) {
				BAL.DOM.addClass(elem, "selected");
				selectionCache.selection[elem.id] = true;
			}
		},

		/**
		 * @param {Node}
		 *            elem
		 * @return {Node}
		 */
		_getControlElement : function(elem) {
			while (elem != null && TL.getType(elem) == null) {
				elem = elem.parentNode;
			}
			return elem;
		},

		_setSelectionCache : function(ctrlElem) {
			var selectionCache = new Object();
			selectionCache.selection = new Object();
			var ul = services.form.ListControl._getULChild(ctrlElem);
			for (var li = BAL.DOM.getFirstElementChild(ul); li != null; li = BAL.DOM
					.getNextElementSibling(li)) {
				if (services.form.ListControl._focusFilter(li)) {
					selectionCache.focus = li;
				}
				if (BAL.DOM.containsClass(li, "selected")) {
					selectionCache.selection[li.id] = true;
				}
			}
			ctrlElem.selectionCache = selectionCache;
			return selectionCache;
		},

		ctrlsListSelected : function(event, ctrlID) {
			event = BAL.getEvent(event);
			var elem = BAL.getEventTarget(event);
			if (BAL.DOM.getTagName(elem) != "li") {
				return false;
			}
			services.ajax.execute('dispatchControlCommand', {
				controlCommand : 'listSelect',
				controlID : ctrlID,
				item : elem.id,
				shift : event.shiftKey,
				ctrl : event.ctrlKey
			}, services.ajax.USE_WAIT_PANE_IN_FORMULA);
			return false;
		},

		dblClick : function(event, ctrlID, isWaitPaneRequested) {
			var useWaitPane;
			if(isWaitPaneRequested) {
				useWaitPane = isWaitPaneRequested;
			} else {
				useWaitPane = services.ajax.USE_WAIT_PANE_IN_FORMULA;
			}
			var elem = services.form.ListControl._getParentLi(event);
			if (elem == null) {
				return false;
			}
			services.ajax.execute('dispatchControlCommand', {
				controlCommand : 'listDblClick',
				controlID : ctrlID,
				item : elem.id
			}, useWaitPane);
			return false;
		},

		handleFixed : function(event, element) {
			BAL.eventStopPropagation(BAL.getEvent(event));
		},

		scrollToFocus : function(controlID) {
			var controlElem = document.getElementById(controlID);
			var iter = new BAL.DescendantIterator(controlElem,
					this._focusFilter);
			if (iter.hasNext()) {
				controlElem.scrollTop = BAL.getElementY(iter.next());
			}
		},

		_focusFilter : function(node) {
			return BAL.DOM.containsClass(node, "focus");
		},

		_fixedFilter : function(node) {
			return BAL.DOM.containsClass(node, "fixed");
		},

		disableGUI : function(element) {
			services.form.addOverlay(element);
		},

		enableGUI : function(element) {
			services.form.removeOverlay(element);
		},

		/* Drag and Drop implementation */

		/**
		 * Called on the element of a drag'n drop operation on which a 
		 * mousedown-event has happened.
		 * 
		 * @param element
		 *            The concrete DOM element on which the drag'n drop
		 *            process is started.
		 * @returns The IDs of the moved list entries.
		 */
		createDrag: function(event, element) {
			var clickedLi = services.form.ListControl._getParentLi(event);
			if (clickedLi == null) {
				return null;
			}
			var srcUl = clickedLi.parentNode;
			var movedLis = new Array();
			for (var sel = BAL.DOM.getFirstElementChild(srcUl); sel != null; sel = BAL.DOM.getNextElementSibling(sel)) {
				if (BAL.DOM.getTagName(sel) == "li" && BAL.DOM.containsClass(sel, "selected")) {
					movedLis.push(sel);
				}
			}
			if (movedLis.length == 0) {
				movedLis.push(clickedLi);
			}
			
			var clientDragInfoAll = document.createElement("ul");
			var serverDragInfoAll = new Array();
			var clientDragInfoNonFixed = null
			var serverDragInfoNonFixed = new Array();
			
			var maxDisplayed = 4;
			var dragShapeHtml = "<div class='selectbox dndSelection'><ul>";
			for (var i = 0, size = movedLis.length; i < size; i++) {
				var movedLI = movedLis[i];
				var copiedLi = BAL.DOM.importNode(document, movedLI);
				BAL.DOM.removeClass(copiedLi, "focus");
				BAL.DOM.removeClass(copiedLi, "selected");
				// add after copy to ensure new elements are not invisible
				BAL.DOM.addClass(movedLI, "invisible");
				serverDragInfoAll.push(movedLI.id);
				copiedLi.id = services.ajax.nextID();
				clientDragInfoAll.appendChild(copiedLi);
				if(TL.getTLAttribute(copiedLi, "fixed") == null) {
					serverDragInfoNonFixed.push(movedLI.id);
					if(clientDragInfoNonFixed == null) {
						clientDragInfoNonFixed = document.createElement("ul");
					}
					var copiedLiNonFixed = BAL.DOM.importNode(document, movedLI);
					copiedLiNonFixed.id = services.ajax.nextID();
					clientDragInfoNonFixed.appendChild(copiedLiNonFixed);
				}

				if (i < maxDisplayed || i == size - 1) {
					var previewLi = BAL.DOM.importNode(document, movedLI);
					BAL.DOM.removeNonStandardAttribute(previewLi, "id");
					dragShapeHtml += previewLi.outerHTML;
				} else {
					if (i == maxDisplayed) {
						dragShapeHtml += "<li>...</li>";
					}
				}
			}
			dragShapeHtml += "</ul></div>";
			
			return {
				_element: element,
				
				getClientDragInfo: function(dragSourceElement, dropTargetElement) {
					if(dragSourceElement === dropTargetElement) {
						return clientDragInfoAll;
					} else {
						return clientDragInfoNonFixed;
					}
				},
				
				dragShapeHtml: dragShapeHtml,
				
				/**
				 * List of IDs of moved list elements.
				 */
				getServerDragInfo: function(dragSourceElement, dropTargetElement) {
					if(dragSourceElement === dropTargetElement) {
						return serverDragInfoAll;
					} else {
						return serverDragInfoNonFixed;
					}
				},
				
				/**
				 * Informs the source about the completion of the drag'n drop operation.
				 * 
				 * @param successful
				 *        If the the dragged object was accepted by any target.
				 */
				dispose: function(successful, dropTargetElement) {
					var element = this._element;
					
					var ul = services.form.ListControl._getULChild(element);
					if (successful) {
						var serverDragInfo = this.getServerDragInfo(element, dropTargetElement);
						for (var i = 0; i < serverDragInfo.length; i++) {
							var moved = document.getElementById(serverDragInfo[i]);
							ul.removeChild(moved);
						}
					} else {
						for (var sel = BAL.DOM.getFirstElementChild(ul); sel != null; sel = BAL.DOM.getNextElementSibling(sel)) {
							BAL.DOM.removeClass(sel, "invisible");
						}
						
					}
					services.form.ListControl.resetCache(element.id);
				}

			};
		},

		/**
		 * Called on each potential target element of a drag'n drop operation 
		 * to inform that a drag'n drop operation is about to start.
		 * 
		 * A potential drop target must be enabled to provide visual feedback
		 * about a pending drop within its area. A pending drop will be is 
		 * announced by calling a "marker function" installed on the potential 
		 * drop target.
		 * 
		 * @param element
		 *            The concrete DOM element which is a potential drag'n 
		 *            drop target.
		 * @returns A handle object that is informed about drag positions and 
		 *          the end of the drag'n drop operation.
		 */
		createDrop: function(event, element) {
			var li = document.createElement("li");
			BAL.DOM.addClass(li, "DNDinsertMarker");
			li.innerHTML = "&nbsp;";
			li.id = services.form.ListControl._markerID;
			
			return {
				element: element,
				
				marker: li,
				
				lastEventSrc: null,
				
				dragOver: function(eventSrc, clientDragInfo) {
					if (clientDragInfo == null || BAL.DOM.getTagName(clientDragInfo) != "ul") {
						return
					}
					
					var marker = this.marker;
					if (eventSrc == marker) {
						return;
					}
					
					if (this.lastEventSrc == eventSrc) {
						return;
					}
					
					this.lastEventSrc = eventSrc;
					this._removeMarker();
					
					if (BAL.DOM.getTagName(eventSrc) == "ul") {
						eventSrc.appendChild(marker);
					} else if (BAL.DOM.getTagName(eventSrc) == "li") {
						eventSrc.parentNode.insertBefore(marker, eventSrc);
					} else if (BAL.DOM.getTagName(eventSrc) == "span" || BAL.DOM.getTagName(eventSrc) == "a") {
						eventSrc.parentNode.parentNode.insertBefore(marker, eventSrc.parentNode);
					} else {
						services.form.ListControl._getULChild(this.element).appendChild(marker);
					}
				},
				
				dragOut: function() {
					this._removeMarker();
				},
				
				/**
				 * Informs a drop target about a drop. 
				 * 
				 * Each drop target will be informed about a drop (no matter, where the 
				 * drop event occurred). This function must decide, whether the given 
				 * element accepts the drop. 
				 * 
				 * In this function, the drop targets must also reset state installed 
				 * during createDrop().
				 * 
				 * @param element
				 *            The concrete DOM instance which is a potential drop
				 *            target.
				 * @param event
				 *            The mouse-up event which occurs during drop. This is used to
				 *            determine whether the element should accepts the drop.
				 * @return Position and the IDs of the newly inserted elements.
				 */
				offerDrop: function(clientDragInfo, srcElement, element) {
					var result;
					if (clientDragInfo == null || BAL.DOM.getTagName(clientDragInfo) != "ul") {
						result = null;
					} else {
						if (BAL.DOM.getTagName(srcElement) == "span" || BAL.DOM.getTagName(srcElement) == "a") {
							srcElement = srcElement.parentNode;
						}
						{
							var ul = services.form.ListControl._getULChild(element);

							var insertedLIs = new Array();
							var insertedLiIds = new Array();
							for (var movedLi = BAL.DOM.getFirstElementChild(clientDragInfo); movedLi != null; movedLi = BAL.DOM.getNextElementSibling(movedLi)) {
								var li = BAL.DOM.importNode(document, movedLi);
								BAL.DOM.addClass(li, "selected");
								insertedLIs.push(li);
								insertedLiIds.push(li.id);
							}
							
							var dropInfo = {
								newElementIDs: insertedLiIds
							}
							if (services.form.ListControl._isOnPath(srcElement, ul, element)) {
								for (var i = 0; i < insertedLIs.length; i++) {
									ul.appendChild(insertedLIs[i]);
								}
								dropInfo.position = "end";
								result = dropInfo;
							} else {
								/*
								 * Drop on marker element
								 */
								if (srcElement.id == services.form.ListControl._markerID) {
									var sibling = BAL.DOM.getNextElementSibling(srcElement);
									if (sibling == null) {
										// inserted after the last child
										for (var i = 0; i < insertedLIs.length; i++) {
											ul.appendChild(insertedLIs[i]);
										}
										dropInfo.position = "end";
										result = dropInfo;
									} else {
										services.form.ListControl._insertBefore(insertedLIs, ul, sibling);
										dropInfo.position = sibling.id;
										result = dropInfo;
									}
								} else {
									services.form.ListControl._insertBefore(insertedLIs, ul, srcElement);
									dropInfo.position = srcElement.id;
									result = dropInfo;
								}
							}
						}
					}
					return result;
				},
				
				_removeMarker: function() {
					var marker = this.marker;
					if (marker.parentNode != null) {
						marker.parentNode.removeChild(marker);
					}
				}
			};
		},
		
		/**
		 * Checks whether the given elelemt is on the path from the source node to the target node.
		 * 
		 * @param {Node}
		 *            element It is checked whether that element is on the path from source to target.
		 * @param {Node}
		 *            source The first node on the path. Must be a descendant of given target.
		 * @param {Node}
		 *            target The last node on the path. Must be an ancestor of given source.
		 * 
		 * @return {Boolean}
		 */
		_isOnPath : function(element, source, target) {
			var parent = source;
			while (parent != target && parent != null) {
				if (parent == element) {
					return true;
				}
				parent = parent.parentNode;
			}
			return element == target;
		},
		
		/**
		 * inserts the given elements into the node before the sibling
		 * 
		 * @param {Array}
		 *            elementsToInsert elements to insert
		 * @param {Node}
		 *            node the DOM element to insert
		 * @param {Node}
		 *            sibling the DOM element to insert before
		 * 
		 * @return The ID of the 
		 */
		_insertBefore : function(elementsToInsert, node, sibling) {
			for (var i = 0; i < elementsToInsert.length; i++) {
				node.insertBefore(elementsToInsert[i], sibling);
			}
		},

		resetCache : function(ctrlID) {
			document.getElementById(ctrlID).selectionCache = null;
		},

		_markerID : "listControl0815",

		_getULChild : function(element) {
			if (BAL.DOM.getTagName(element) == "ul") {
				return element;
			}
			var iter = new BAL.DescendantIterator(element, function(node) {
				return BAL.DOM.getTagName(node) == "ul";
			});
			if (iter.hasNext()) {
				return iter.next();
			} else {
				throw new Error(
						"No unordered list as child of element with tagName '"
								+ BAL.DOM.getTagName(element) + "' and id '" + element.id
								+ "'");
			}
		}
	},
	
	FlexibleFlowLayout: {
		MINIMUM_SIZE: 20, //px, randomely choosen
		
		initLayoutAdjustment: function(event, ctrlID, separatorID, adjustmentBar, resizeMode, isArrangedHorizontal) {
			if(!services.layout.isCollapsed(adjustmentBar)) {
				var adjustmentDataContainer = new Object();
				
				var directBefore = BAL.DOM.getPreviousElementSibling(adjustmentBar);
				var previousNode = directBefore;
				while (services.layout.isCollapsed(previousNode)) {
					previousNode = BAL.DOM.getPreviousElementSibling(previousNode);
				}
				var nextNode = BAL.DOM.getNextElementSibling(adjustmentBar);
				while (services.layout.isCollapsed(nextNode)) {
					nextNode = BAL.DOM.getNextElementSibling(nextNode);
				}
				
				adjustmentDataContainer.previousNode = previousNode;
				adjustmentDataContainer.nextNode = nextNode;
				
				adjustmentDataContainer.ctrlID = ctrlID;
				adjustmentDataContainer.separatorID = separatorID;
				adjustmentDataContainer.resizeMode = resizeMode;
				
				var previousNodeMinSize = this.getNodeMinSizeInPx(previousNode);
				var nextNodeMinSize = this.getNodeMinSizeInPx(nextNode);
				if(isArrangedHorizontal) {
					var startPosition = BAL.getElementX(adjustmentBar);
					adjustmentDataContainer.startPosition = startPosition;
					
					adjustmentDataContainer.minPosition = 
						startPosition - (BAL.getElementWidth(previousNode) - previousNodeMinSize);
					adjustmentDataContainer.maxPosition = 
						startPosition + (BAL.getElementWidth(nextNode) - nextNodeMinSize);
					
					adjustmentDataContainer.effectiveSizeDelta = BAL.getElementWidth(previousNode) - BAL.getEffectiveWidth(previousNode);
				} else {
					var startPosition = BAL.getElementY(adjustmentBar);
					adjustmentDataContainer.startPosition = startPosition;
					
					adjustmentDataContainer.minPosition = 
						startPosition - (BAL.getElementHeight(previousNode) - previousNodeMinSize);
					adjustmentDataContainer.maxPosition = 
						startPosition + (BAL.getElementHeight(nextNode) - nextNodeMinSize);
					
					adjustmentDataContainer.effectiveSizeDelta = BAL.getElementHeight(previousNode) - BAL.getEffectiveHeight(previousNode);
				}
				adjustmentDataContainer.isArrangedHorizontal = isArrangedHorizontal;
				adjustmentDataContainer.adjustmentBar = adjustmentBar;
				
				adjustmentDataContainer.originalZIndex = adjustmentBar.style.zIndex;
				adjustmentBar.style.zIndex = "1000";
				adjustmentDataContainer.backgroundPane = this.createBackgroundPane(isArrangedHorizontal);
				
				adjustmentDataContainer.adjustmentFunction = function(closureEvent) {
					services.form.FlexibleFlowLayout.adjustmentFunction(BAL.getEvent(closureEvent), adjustmentDataContainer);
					return false;
				};
				
				adjustmentDataContainer.tearDown = function(closureEvent) {
					services.form.FlexibleFlowLayout.tearDown(BAL.getEvent(closureEvent), adjustmentDataContainer);
					return false;
				};
				
				this.convertPercentToPixelSizes(adjustmentDataContainer);
				
				BAL.addEventListener(document, "mousemove",	adjustmentDataContainer.adjustmentFunction);
				BAL.addEventListener(document, "mouseup", adjustmentDataContainer.tearDown);
			}
			return BAL.cancelEvent(BAL.getEvent(event));
		},
		
		convertPercentToPixelSizes: function(adjustmentDataContainer) {
			var layoutParent = adjustmentDataContainer.adjustmentBar.parentNode;
			var layoutChild = BAL.DOM.getFirstElementChild(layoutParent);
			while(layoutChild != null) {
				var layoutConstraint = layoutChild.layoutConstraint;
				if(layoutConstraint.unit != "px") {
					if(adjustmentDataContainer.isArrangedHorizontal) {
						layoutConstraint.size = layoutChild.layoutResult._width;
					} else {
						layoutConstraint.size = layoutChild.layoutResult._height;
					}
					layoutChild.layoutConstraint.unit = "px";
				}
				layoutChild = BAL.DOM.getNextElementSibling(layoutChild);
			}
		},
		
		saveNewSizes: function(adjustmentDataContainer) {
			var sumPixelOfPercentLayouts = 0;
			var layoutSizes = adjustmentDataContainer.layoutSizes = new Object();
			var layoutParent = adjustmentDataContainer.adjustmentBar.parentNode;
			var layoutChild = BAL.DOM.getFirstElementChild(layoutParent);
			while(layoutChild != null) {
				var domLayoutConstraint = services.layout.getParsedLayoutConstraint(layoutChild);
				if(domLayoutConstraint.unit != "px") {
					if(adjustmentDataContainer.isArrangedHorizontal) {
						sumPixelOfPercentLayouts += layoutChild.layoutResult._width;
					} else {
						sumPixelOfPercentLayouts += layoutChild.layoutResult._height;
					}
				}
				layoutChild = BAL.DOM.getNextElementSibling(layoutChild);
			}
			
			layoutChild = BAL.DOM.getFirstElementChild(layoutParent);
			while(layoutChild != null) {
				var layoutConstraint = layoutChild.layoutConstraint;
				var domLayoutConstraint = services.layout.getParsedLayoutConstraint(layoutChild);
				if(domLayoutConstraint.unit != "px") {
					layoutConstraint.unit = domLayoutConstraint.unit;
					
					var pixelSize;
					if(adjustmentDataContainer.isArrangedHorizontal) {
						pixelSize = layoutChild.layoutResult._width;
					} else {
						pixelSize = layoutChild.layoutResult._height;
					}
					// Only space, that exceeds minimum size, will be distributed in percent (see layouting in layout.js)
					layoutConstraint.size = Math.round((pixelSize - services.form.FlexibleFlowLayout.getNodeMinSizeInPx(layoutChild)) / sumPixelOfPercentLayouts * 100);
				}
				layoutSizes[layoutChild.id] = layoutConstraint.size;
				services.layout.setSizeAnnotation(layoutChild, layoutConstraint);
				layoutChild = BAL.DOM.getNextElementSibling(layoutChild);
			}
		},
		
		getNodeMinSizeInPx: function(node) {
			var layoutConstraint = node.layoutConstraint;
			var minSizeInPx = layoutConstraint.minSize;
			return Math.max(minSizeInPx, services.form.FlexibleFlowLayout.MINIMUM_SIZE);
		},
		
		createBackgroundPane: function(isArrangedHorizontal) {
			var topWindow = services.ajax.topWindow.document.getElementsByTagName("div")[0];
			var backgroundPane = document.createElement("div");
			topWindow.appendChild(backgroundPane);
			BAL.DOM.addClass(backgroundPane, "layoutAdjustmentPane");
			if(isArrangedHorizontal) {
				BAL.setStyle(backgroundPane, "cursor:e-resize");
			} else {
				BAL.setStyle(backgroundPane, "cursor:n-resize");
			}
			return backgroundPane;
		},
		
		adjustmentFunction: function(event, adjustmentDataContainer) {

			var currentPosition = BAL.relativeMouseCoordinates(event, adjustmentDataContainer.adjustmentBar.offsetParent);
			var insertPosition;
			if(adjustmentDataContainer.isArrangedHorizontal) {
				adjustmentDataContainer.currentPosition = currentPosition.x;
				insertPosition = this.getInsertPosition(adjustmentDataContainer);
				BAL.setElementX(adjustmentDataContainer.adjustmentBar, insertPosition);			
			} else {
				adjustmentDataContainer.currentPosition = currentPosition.y;
				insertPosition = this.getInsertPosition(adjustmentDataContainer);
				BAL.setElementY(adjustmentDataContainer.adjustmentBar, insertPosition);			
			}
			
			if(adjustmentDataContainer.resizeMode == "instant") {
				this.adjustLayout(adjustmentDataContainer);
			}
		},
		
		tearDown: function(event, adjustmentDataContainer) {
			BAL.removeEventListener(document, "mousemove", adjustmentDataContainer.adjustmentFunction);
			BAL.removeEventListener(document, "mouseup", adjustmentDataContainer.tearDown);
			
			if(adjustmentDataContainer.resizeMode == "preview") {
				this.adjustLayout(adjustmentDataContainer);
			}
			
			this.saveNewSizes(adjustmentDataContainer);
			this.updateServer(adjustmentDataContainer);
			
			adjustmentDataContainer.backgroundPane.parentNode.removeChild(adjustmentDataContainer.backgroundPane);
			adjustmentDataContainer.adjustmentBar.style.zIndex = adjustmentDataContainer.originalZIndex;
		},
		
		updateServer: function(adjustmentDataContainer) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand: "updateLayoutSizes",
				controlID: adjustmentDataContainer.ctrlID,
				layoutSizes: adjustmentDataContainer.layoutSizes
			});
		},
		
		getInsertPosition: function(adjustmentDataContainer) {
			return Math.min(adjustmentDataContainer.maxPosition,
							Math.max(adjustmentDataContainer.minPosition,
									 adjustmentDataContainer.currentPosition));
		},
		
		adjustLayout: function(adjustmentDataContainer) {
			this.adjustPreviousSibling(adjustmentDataContainer);
			this.adjustNextSibling(adjustmentDataContainer);
			this.renderLayout(adjustmentDataContainer.adjustmentBar);
			adjustmentDataContainer.startPosition = this.getCurrentSeparatorPosition(adjustmentDataContainer);
		},
		
		adjustPreviousSibling: function(adjustmentDataContainer) {
			var previousNode = adjustmentDataContainer.previousNode;
			var oldPreviousNodeSize = this.getOldPreviousSiblingSize(previousNode, adjustmentDataContainer);
			var newPreviousNodeSize = this.getNewPreviousSiblingSize(oldPreviousNodeSize, adjustmentDataContainer);
			this.adjustNodeSize(previousNode, oldPreviousNodeSize, newPreviousNodeSize, adjustmentDataContainer);
		},
		
		adjustNextSibling: function(adjustmentDataContainer) {
			var nextNode = adjustmentDataContainer.nextNode;
			var oldNextNodeSize = this.getOldNextSiblingSize(nextNode, adjustmentDataContainer);
			var newNextNodeSize = this.getNewNextSiblingSize(oldNextNodeSize, adjustmentDataContainer);
			this.adjustNodeSize(nextNode, oldNextNodeSize, newNextNodeSize, adjustmentDataContainer);
		},
		
		getOldPreviousSiblingSize: function(previousNode, adjustmentDataContainer) {
			return this.getCurrentNodeSizeInPixel(previousNode, adjustmentDataContainer);
		},
		
		getNewPreviousSiblingSize: function(previousNodeSizeBefore, adjustmentDataContainer) {
			var currentSeparatorPosition = this.getCurrentSeparatorPosition(adjustmentDataContainer);
			
			return previousNodeSizeBefore + (currentSeparatorPosition - adjustmentDataContainer.startPosition);
		},
		
		getCurrentNodeSizeInPixel: function(node, adjustmentDataContainer) {
			if(adjustmentDataContainer.isArrangedHorizontal) {
				return BAL.getElementWidth(node);
			} else {
				return BAL.getElementHeight(node);
			}
		},
		
		getCurrentSeparatorPosition: function(adjustmentDataContainer) {
			if(adjustmentDataContainer.isArrangedHorizontal) {
				return BAL.getElementX(adjustmentDataContainer.adjustmentBar);
			} else {
				return BAL.getElementY(adjustmentDataContainer.adjustmentBar);
			}
		},
		
		getOldNextSiblingSize: function(nextNode, adjustmentDataContainer) {
			return this.getCurrentNodeSizeInPixel(nextNode, adjustmentDataContainer);
		},
		
		getNewNextSiblingSize: function(nextNodeSizeBefore, adjustmentDataContainer) {
			var currentSeparatorPosition = this.getCurrentSeparatorPosition(adjustmentDataContainer);
			
			return  nextNodeSizeBefore - (currentSeparatorPosition - adjustmentDataContainer.startPosition);
		},
		
		adjustNodeSize: function(node, oldSize, newSize, adjustmentDataContainer) {
			var layoutConstraint = node.layoutConstraint;
			var effectiveNewSize = newSize - adjustmentDataContainer.effectiveSizeDelta;
			layoutConstraint.size = effectiveNewSize;
		},
		
		renderLayout: function (node) {
			var layoutParent = node.parentNode;
			while (typeof (layoutParent.layout) == "undefined") {
				layoutParent = layoutParent.parentNode;
			}
			layoutParent.layout.render();
		}
	}, 
	
	TimerControl: {
		schedule : function(controlID, nextDelay) {
			var control = document.getElementById(controlID);
			if (control == null) {
				return;
			}
			var lastSchedule = control.nextSchedule;
			if (lastSchedule != null) {
				window.clearTimeout(lastSchedule);
			}
			var nextSchedule = 
				window.setTimeout(function() {services.form.TimerControl.update(controlID); }, nextDelay);
			control.nextSchedule = nextSchedule;
		},
		
		update : function(controlID) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "update",
				controlID : controlID
			}, /*showWait*/false, /*serverResponseCallback*/null);
		}
	},
	
	AccordionControl: {
		select: function(controlID, nodeID) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "select",
				controlID : controlID,
				nodeID : nodeID
			}, /*showWait*/true);
			return false;
		}
	},

	MinimizableControl: {
		toggle: function(controlID) {
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "toggle",
				controlID : controlID
			}, false);
			return false;
		}
	},
	
	LogoutTimerControl: {
		init: function(controlID, timeoutSeconds, countingSeconds, logoutUrl) {
			var element = document.getElementById(controlID);
			element.timeoutSeconds = timeoutSeconds;
			element.countingSeconds = countingSeconds;
			element.logoutUrl = logoutUrl;
			
			this.resetTimer(controlID);
			
			BAL.addEventListener(element, "click", function() {
				services.ajax.execute("dispatchControlCommand", {
					controlCommand : "refresh",
					controlID : controlID,
				}, false);
			});
		},
		
		update: function(controlID) {
			this.resetTimer(controlID);
		},
		
		resetTimer: function(controlID) {
			var element = document.getElementById(controlID);
			
			if(element != null) {
				BAL.DOM.removeClass(element, "ltcCounting");
				
				element.lastUpdate = new Date().getTime();
				
				if (element.timer != null) {
					window.clearTimeout(element.timer);
					element.timer = null;
				}
				
				var countingSeconds = element.countingSeconds;
				var showDelay;
				if (countingSeconds < 0) {
					showDelay = (-countingSeconds) * 1000;
				} else {
					showDelay = (element.timeoutSeconds - countingSeconds) * 1000;
				}
				if (showDelay > 0) {
					var self = this;
					element.timer = window.setTimeout(function() {self.showTimeout(controlID); }, showDelay);
				} else {
					this.showTimeout(controlID);
				}
			}
			
		},
		
		showTimeout: function(controlID) {
			var element = document.getElementById(controlID);
			
			if (element != null) {
				BAL.DOM.addClass(element, "ltcCounting");
				
				this.updateDisplay(controlID);
			}
		},
		
		updateDisplay: function(controlID) {
			var element = document.getElementById(controlID);
			
			if(element != null) {
				var timeout = element.timeoutSeconds * 1000;
				var millisLeft = element.lastUpdate + timeout - new Date().getTime();
				var secondsLeft = Math.floor(millisLeft / 1000);
				
				if (secondsLeft < 0) {
					services.ajax.showSessionTimeout(element.logoutUrl);
				} else {
					var minutesLeft = Math.floor(secondsLeft / 60);
					var secondsRest = secondsLeft % 60;
					var remaining = minutesLeft + ":" + (secondsRest < 10 ? "0" : "") + secondsRest;
					
					var display = document.getElementById(controlID + "-timer");
					display.innerHTML = remaining;
					
					var self = this;
					element.timer = window.setTimeout(function() {self.updateDisplay(controlID); }, 1000);
				}
			}
		}
		
	},
	
	MediaQueryControl: {
		parentLayoutInformation: new Map(),

		defaultSizes: [550, 1100, 1650, 2200, 2750],
		
		onResize: function(controlID, classPrefix, maxColumns, sizes) {
			if (sizes == null) {
				sizes = this.defaultSizes;
			}
			
			if (maxColumns > sizes.length) {
				maxColumns = sizes.length;
			}
			
			var control = document.getElementById(controlID);
			
			if(services.layout.parentLayoutInformation != null) {
				this.parentLayoutInformation.set(controlID, services.layout.parentLayoutInformation);
			}
			
			var width = (this.parentLayoutInformation.get(controlID)).effectiveWidth;
			var height = (this.parentLayoutInformation.get(controlID)).effectiveHeight;

			var classes = BAL.DOM.getClassesArray(control)
			for (var n = classes.length - 1; n >= 0 ; n--) {
				var css = classes[n];
				if (BAL.startsWith(css, classPrefix) && css.length > classPrefix.length) {
					classes.splice(n, 1);
				}
			}
			
			assign: {
				for (var n = 0; n < maxColumns; n++) {
					if (width < sizes[n]) {
						classes.push(classPrefix + n);
						break assign;
					}
				}
				classes.push(classPrefix + maxColumns);
			}
			
			
			BAL.DOM.setClassesArray(control, classes);
			
			var defaultLayout = control.defaultLayout;
			if (defaultLayout != null) {
				defaultLayout.compute(width, height);
			}
		}
	},
	
	BrowserWindowControl : {

		handleKeyDown : function(event, controlID) {
			event = BAL.getEvent(event);
			BAL.eventStopPropagation(event);
			if (event != null) {
				var keycode = BAL.getKeyCode(event);

				// alert(keycode);
				if (BAL.isEscapeKey(keycode) || BAL.isReturnKey(keycode)) {
					services.form.transmitTransientState(event);

					var shift = BAL.hasShiftModifier(event);
					var ctrl = BAL.hasCtrlModifier(event);
					var alt = BAL.hasAltModifier(event);

					services.ajax.execute("dispatchControlCommand", {
						controlCommand : "keypress",
						controlID : controlID,
						scancode : keycode,
						shift : shift,
						ctrl : ctrl,
						alt : alt
					}, /*useWaitPane*/ true);
					return BAL.cancelEvent(event);
				}
				
				// Dispatch to tab navigation event handling
				return KEYBOARD_NAVIGATION.keyDownHandler(event);
			}
			return true;
		},
		
		handlesActivation: function(source) {
			var tagName = BAL.DOM.getTagName(source);
			if (tagName == "textarea") return true;
			if (tagName == "a" && source.href) return true;
			if (tagName == "button") return true;
			if (tagName == "input") {
				var type = source.type;
				if (type == "image") return true;
				if (type == "button") return true;
				if (type == "reset") return true;
				if (type == "submit") return true;
			}
			return false;
		},
		
		getPopupDialogAnchor: function() {
			return services.ajax.topWindow.document.getElementById('pdlgPopupDialogs');
		},

		hidePopupPane: function(popupDialogAnchor) {
			while (popupDialogAnchor.hasChildNodes()) {
				popupDialogAnchor.removeChild(popupDialogAnchor.firstChild);
			}

			document.getElementById('pdlgPopupDialogPane').className = 'pdlgPopupDialogPane_invisible';
		},
		
		showPopupPane: function() {
			document.getElementById('pdlgPopupDialogPane').className = 'pdlgPopupDialogPane_visible';
		},
		
		hasPopups: function(popupDialogAnchor) {
			return BAL.DOM.getFirstElementChild(popupDialogAnchor) != null;
		},
		
		// Closes all open popup dialogs
		closeAllPopupDialogs : function() {

			var popupDialogAnchor = this.getPopupDialogAnchor();
			this.hidePopupPane(popupDialogAnchor);
			
			browserWindowControlID = popupDialogAnchor.parentNode.getAttribute('id');
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "unregisterAllPopupDialogs",
				controlID : browserWindowControlID
			}, /*useWaitPane*/ false);
		},
		
		// Close a specific popup dialog
		closePopupDialog : function(popupDialogID) {
			
			var popupDialogAnchor = this.getPopupDialogAnchor();
			var popupDialog = services.ajax.topWindow.document.getElementById(popupDialogID);
			popupDialogAnchor.removeChild(popupDialog);
			
			if (! this.hasPopups(popupDialogAnchor)) {
				this.hidePopupPane(popupDialogAnchor);
			}
			
			browserWindowControlID = popupDialogAnchor.parentNode.getAttribute('id');
			services.ajax.execute("dispatchControlCommand", {
				controlCommand : "unregisterPopupDialog",
				controlID : browserWindowControlID,
				popupID: popupDialogID 
			}, /*useWaitPane*/ false);
		}
	},

	disableGUI : function(controlElementID) {
		var element = document.getElementById(controlElementID);
		if (element != null) {
			var type = TL.getType(element);
			if (type != null && type.disableGUI) {
				try {
					type.disableGUI(element);
				} catch (e) {
					// no disable function defined for that type
				}
			} else {
				services.form.addOverlay(element);
			}
		}
	},

	enableGUI : function(controlElementID) {
		var element = document.getElementById(controlElementID);
		if (element != null) {
			var type = TL.getType(element);
			if (type != null && type.enableGUI) {
				try {
					type.enableGUI(element);
				} catch (e) {
					// no disable function defined for that type
				}
			} else {
				services.form.removeOverlay(element);
			}
		}
	},

	addOverlay : function(element) {
		var id = services.form._getOverlayId(element);
		if (document.getElementById(id) != null) {
			return;
		}
		var overlay = document.createElement("div");
		document.body.appendChild(overlay);

		overlay.id = id;
		BAL.DOM.addClass(overlay, "updating");
		overlay.style.position = "absolute";
		// can not set via class since IE will not show until moving cursor
		overlay.style.cursor = "wait";
		BAL.addEventListener(element, 'DOMNodeRemovedFromDocument', function() {
			services.form.removeOverlay(element);
		}, true);

		BAL.setElementWidth(overlay, BAL.getElementWidth(element));
		BAL.setElementHeight(overlay, BAL.getElementHeight(element));
		var elementPos = BAL.getAbsoluteElementPosition(element);
		BAL.setElementX(overlay, elementPos.x);
		BAL.setElementY(overlay, elementPos.y);
	},

	removeOverlay : function(element) {
		var overlay = document.getElementById(services.form
				._getOverlayId(element));
		if (overlay != null) {
			// must reset cursor since IE will not remove "wait" until moving
			// cursor
			overlay.style.cursor = "pointer";
			overlay.parentNode.removeChild(overlay);
		}
	},

	_getOverlayId : function(element) {
		if (element == null) {
			services.log.error("can not get overlay id for 'null'");
		}
		if (element.id == null) {
			services.log
					.error("can not get overlay id for element without id.");
		}
		return element.id + "_overlay";
	},
	
	/**
	 * Establishes a sync-scroll relation from the element identified by contentId to 
	 * an element to the left identified by leftId and to an element to the top 
	 * identified by topId.
	 * 
	 * @param topHeight
	 *        The height of the fixed header area.
	 * @param leftWidth
	 *        The width of the fixed side bar area.
	 * @param topId
	 *        Element id of an element to be scrolled horizontally together with the contentId element.
	 * @param leftId
	 *        Element id of an element to be scrolled horizontally together with the contentId element.
	 * @param contentId
	 *        Scroll master element. 
	 * 
	 * @see ImageControl
	 */
	syncScroll: function(topHeight, leftWidth, topId, leftId, contentId) {
		var topElement = document.getElementById(topId);
		var leftElement = document.getElementById(leftId);
		var contentElement = document.getElementById(contentId);
		
		var active = true;
		
		contentElement.onscroll = function(evt) {
			if (!active) {
				return;
			}
			active = false;
			try {
				evt = BAL.getEvent(evt);
				
				topElement.style.left = -(leftWidth + BAL.getScrollLeftElement(contentElement)) + "px";
				leftElement.style.top = -(topHeight + BAL.getScrollTopElement(contentElement)) + "px";
				
				BAL.eventStopPropagation(evt);
				BAL.cancelEvent(evt);
			} finally {
				active = true;
			}
		}
		
		var onWheelY = function(evt) {
			if (!active) {
				return;
			}
			active = false;
			try {
				evt = BAL.getEvent(evt);

				var delta = BAL.getEventMouseScrollDelta(evt);
				var currentY = BAL.getScrollTopElement(contentElement);
				var nextScroll = currentY + delta;
				
				BAL.setScrollTopElement(contentElement, nextScroll);
				leftElement.style.top = -(topHeight + BAL.getScrollTopElement(contentElement)) + "px";
				
				BAL.eventStopPropagation(evt);
				BAL.cancelEvent(evt);
			} finally {
				active = true;
			}
		}
		
		var onWheelX = function(evt) {
			if (!active) {
				return;
			}
			active = false;
			try {
				evt = BAL.getEvent(evt);
				
				var delta = BAL.getEventMouseScrollDelta(evt);
				var currentX = BAL.getScrollLeftElement(contentElement);
				var nextScroll = currentX + delta;
				
				BAL.setScrollLeftElement(contentElement, nextScroll);
				topElement.style.left = -(leftWidth + BAL.getScrollLeftElement(contentElement)) + "px";
				
				BAL.eventStopPropagation(evt);
				BAL.cancelEvent(evt);
			} finally {
				active = true;
			}
		}
		
		BAL.addMouseScrollListener(leftElement, onWheelY);
		BAL.addMouseScrollListener(topElement, onWheelX);
		BAL.addMouseScrollListener(contentElement, onWheelY);
	}
};

function invokeCommand(command, arguments, contextInformation) {
	services.ajax.execute(command, arguments, /*useWaitPane*/ false, null, null, contextInformation);
}
