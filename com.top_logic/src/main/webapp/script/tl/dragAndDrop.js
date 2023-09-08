services.DnD = {

	DropTarget: function(targetElement, targetType) {
		this.element = targetElement;
		this.type = targetType;
		this.handle= null;
	},
	
	Draggable: {
		/**
		 * Called on a potential drag source element on which a 
		 * mousedown-event has happened.
		 * 
		 * @param element
		 *          The concrete DOM element on which the drag'n drop
		 *          process is started.
		 * @returns Information about the dragged object, or null 
		 *          if the given event should be ignored.
		 *          
		 * @see DnD.dragOperation
		 */
		createDrag : function(event, element) {
			return new services.DnD.Drag(element.outerHTML);
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
			return new services.DnD.Drop();
		}
		
	},
	
	/**
	 * A drag operation.
	 */
	Drag: function(dragShapeHtml) {
		this.dragShapeHtml = dragShapeHtml;
	},
	
	/**
	 * A drop target prepared to receive a drop.
	 */
	Drop: function() {
		// No additional state by default.
	},
	
	/**
	 * Information about a drag'n drop operation consisting of:
	 * 
	 * dragShapeHtml
	 *    A HTML fragment string of the moved content to show as mouse marker.
	 *    
	 * serverDragInfo
	 *    The ids of the source elements which are moved before the drag has started.
	 */
	dragOperation: null,
	
	/**
	 * The HTML element representing the drag content following the mouse pointer during Drag&Drop.
	 */
	mouseMarker : null,

	handleMouseDown : function(element, targets, evt) {
			/*
			 * Must compute this for each execution. Setting this once will not
			 * work, since when some target has repaint the DOM elements for the
			 * id's are new
			 */
			var dropTargets = new Array();
			for ( var i = 0; i < targets.length; i++) {
				var targetElement = document.getElementById(targets[i]);
				if (targetElement == null) {
					services.log.error("No element found with id '" + targets[i] + "' for drag and drop");
					continue;
				}
				var targetType = this.getType(targetElement);
				dropTargets.push(new services.DnD.DropTarget(targetElement, targetType));
				targetElement = null;
				targetType = null;
			}
			if (dropTargets.length == 0) {
				return false;
			}

			/*
			 * change onselect to prevent selection
			 */
			services.DnD.onselectstart = document.onselectstart;
			document.onselectstart = function() {
				return false;
			};

			var executeDragNDrop = function() {
				var type = services.DnD.getType(element);
				var dragOperation = type.createDrag(evt, element);
				if (dragOperation == null) {
					document.onselectstart = services.DnD.onselectstart;
					delete services.DnD.onselectStart;
					return;
				}
				services.DnD.dragOperation = dragOperation;
				
				for ( var i = 0; i < dropTargets.length; i++) {
					dropTargets[i].setup(dragOperation, element, evt);
				}
	
				/*
				 * Installs the HTMLElement which will follow the mouse on move and
				 * corresponding functions
				 */
				services.DnD._addMouseMarker();
	
				var mouseUp = function(event) {
					BAL.removeEventListener(document, 'mouseover', mouseOver, false);
					BAL.removeEventListener(element, 'DOMNodeRemovedFromDocument', services.DnD._removeMouseMarker, true);
					services.DnD._removeMouseMarker();

					var srcElement = BAL.getEventTarget(BAL.getEvent(event));

					var serverDropInfo = null;
					var droppedElement = null;
					var accepted = false;
					for ( var i = 0; i < dropTargets.length; i++) {
						var dropTarget = dropTargets[i];
						
						// Determine whether the event happened in the current target.
						var parent = srcElement;
						while (parent != null && parent != dropTarget.element) {
							parent = parent.parentNode;
						}
						if (parent == null) {
							// Don't care. drop happened within a different element
							dropTarget.dispose();
							continue;
						}
						
						var clientDragInfo = dragOperation.getClientDragInfo(element, dropTarget.element);
						var dropInfo = dropTarget.offerDrop(clientDragInfo, srcElement);
						if (dropInfo != null) {
							droppedElement = dropTarget.element;
							accepted = true;
							serverDropInfo = dropInfo;
						}
						
						dropTarget.dispose();
					}
					dragOperation.dispose(accepted, droppedElement);
	
					services.DnD._reinstallOnSelectStart();
	
					if (accepted) {
						var serverDragInfo = dragOperation.getServerDragInfo(element, droppedElement)
						services.DnD.notifyServer(element.id, droppedElement.id, serverDragInfo, serverDropInfo);
					}
	
					services.DnD.dragOperation = null
					services.DnD.mouseMarker = null;
				}
				var addedMouseUp = BAL.onceEventListener(document, 'mouseup', mouseUp, this, false);
	
				var mouseOver = function(event) {
					if (BAL.getEventTarget(BAL.getEvent(event)) == document.body) {
						addedMouseUp.call();
					}
				};
				BAL.addEventListener(document, 'mouseover', mouseOver, false);
				BAL.addEventListener(element, 'DOMNodeRemovedFromDocument', services.DnD._removeMouseMarker, true);
			}
			
			var startDnDMouseOut = function() {
				BAL.removeEventListener(element, 'mouseout', startDnDMouseOut);
				BAL.removeEventListener(element, 'mouseup', cancelDnD);
				executeDragNDrop.call();
			}
			
			var cancelDnD = function() {
				BAL.removeEventListener(element, 'mouseout', startDnDMouseOut);
				BAL.removeEventListener(element, 'mouseup', cancelDnD);
				services.DnD._reinstallOnSelectStart();
			}
			BAL.addEventListener(element, 'mouseout', startDnDMouseOut);
			BAL.addEventListener(element, 'mouseup', cancelDnD);
			return false;
	},
	
	getType: function(targetElement) {
		var targetType = TL.getType(targetElement)
		if (targetType == null || targetType == "") {
			targetType = services.DnD.Draggable;
		}
		return targetType;
	},
	
	_reinstallOnSelectStart: function() {
		/*
		 * Reinstall onselectstart
		 */
		document.onselectstart = services.DnD.onselectstart;
		delete services.DnD.onselectStart;
	},

	onDragging: function(event) {
		event = BAL.getEvent(event);
		
		var dragShapeHtml = services.DnD.dragOperation.dragShapeHtml;
		
		var mouseMarker = services.DnD.mouseMarker;
		if (mouseMarker == null) {
			if (dragShapeHtml == null) {
				return;
			}
			
			mouseMarker = document.createElement('div');
			BAL.DOM.addClass(mouseMarker, 'DNDmouseMarker');
			mouseMarker.innerHTML = dragShapeHtml;
			document.body.appendChild(mouseMarker);
			BAL.addEventListener(mouseMarker, 'mousemove', services.DnD.clearDragTimeout, false);
			
			services.DnD.mouseMarker = mouseMarker;
		}
		BAL.setElementX(mouseMarker, BAL.getEventX(event) + 10);
		BAL.setElementY(mouseMarker, BAL.getEventY(event) - 25);
	},
	
	_addMouseMarker : function() {
		BAL.addEventListener(document, 'mousemove', services.DnD.onDragging, false);
	},

	clearDragTimeout : function() {
		if (services.DnD.timer) {
			window.clearTimeout(services.DnD.timer);
			delete services.DnD.timer;
		}
	},

	_removeMouseMarker : function() {
		BAL.removeEventListener(document, 'mousemove', services.DnD.onDragging, false);
		
		var mouseMarker = services.DnD.mouseMarker;
		if (mouseMarker != null) {
			BAL.removeEventListener(mouseMarker, 'mousemove', services.DnD.clearDragTimeout, false);
			mouseMarker.parentNode.removeChild(mouseMarker);
			
			services.DnD.mouseMarker = null;
		}
	},

	notifyServer : function(sourceID, dropID, serverDragInfo, serverDropInfo) {
		services.ajax.execute('dispatchControlCommand', {
			controlCommand : 'dragNdrop',
			controlID : sourceID,
			serverDragInfo : serverDragInfo,
			dropID : dropID,
			serverDropInfo : serverDropInfo

		}, /*useWaitPane*/ false);
	}

};

services.DnD.DropTarget.prototype = {
	setup: function(dragOperation, dragSourceElement, eventMouseDown) {
		var element = this.element;
		var dropHandle = this.type.createDrop(eventMouseDown, element);
		if (dropHandle == null) {
			return;
		}
		
		var clientDragInfo = dragOperation.getClientDragInfo(dragSourceElement, element);
		this.onMouseMove = function(event) {
			var eventSrc = BAL.getEventTarget(BAL.getEvent(event));
			services.DnD.clearDragTimeout();
			
			dropHandle.dragOver(eventSrc, clientDragInfo);
		}
		BAL.addEventListener(element, 'mousemove', this.onMouseMove, false);

		this.onMouseOut = function(event) {
			if (BAL.getEventTarget(BAL.getEvent(event)) != element) {
				return;
			}
			services.DnD.timer = window.setTimeout(function() {
				dropHandle.dragOut();
			}, 100);
		}
		BAL.addEventListener(element, 'mouseout', this.onMouseOut, false);
		
		this.handle = dropHandle;
	},
	
	offerDrop: function(clientDragInfo, srcElement) {
		if (this.handle == null) {
			return null;
		}

		return this.handle.offerDrop(clientDragInfo, srcElement, this.element);
	},

	dispose: function() {
		if (this.handle == null) {
			return;
		}
		
		var element = this.element;
		BAL.removeEventListener(element, 'mousemove', this.onMouseMove, false);
		BAL.removeEventListener(element, 'mouseout', this.onMouseOut, false);
		
		this.handle.dragOut();
	}
	
};

services.DnD.Drag.prototype = {
	/**
	 * Called after the drag operation has finished. 
	 * 
	 * Can be used to clean up state and styles modified while staring the drag operation.
	 */
	dispose: function(successful, dropTargetElement) {
		return;
	},

	/**
	 * Provides information of the dragged objects, that must be handled by drop targets at client side.
	 * Called, whether when mouse hovers a drop target during drag operation, or drop shall be
	 * performed on drop target.
	 * 
	 * @param dragSourceElement
	 * 			DOM element, that is the source of a drag'n'drop operation
	 * @param dropTargetElement
	 * 			DOM element, that is the target of a drag'n'drop operation
	 * 			
	 */
	getClientDragInfo : function(dragSourceElement, dropTargetElement) {
		return null;
	},
	
	/**
	 * Provides information of the dragged objects, that must be handled by drop targets.
	 * Called, when drop target has been successfully accepted the drop and drag'n'drop update
	 * is send to server.
	 * 
	 * @param dragSourceElement
	 * 			DOM element, that is the source of a drag'n'drop operation
	 * @param dropTargetElement
	 * 			DOM element, that is the target of a drag'n'drop operation
	 * 			
	 */
	getServerDragInfo : function(dragSourceElement, dropTargetElement) {
		return null;
	}
};

services.DnD.Drop.prototype = {
	/**
	 * Notifies the drop target that the mouse moves currently over it.
	 * 
	 * This callback can be used to highlight the drop target to give 
	 * visual feedback what would happen, if the drop would be completed 
	 * at the current location.
	 * 
	 * @param eventSrc The DOM element within the drop target that is 
	 *          hit by the mouse cursor.
	 * @param clientDragInfo
	 *        The drag information created by the drag source in getClientDragInfo().
	 */
	dragOver: function(eventSrc, clientDragInfo) {
		// Ignore.
	},
	
	/**
	 * Notifies the drop target that the mouse cursor has left its area.
	 * 
	 * @see dragOver()
	 */
	dragOut: function() {
		// Ignore.
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
	 * @param clientDragInfo
	 *        The drag information created by the drag source in getClientDragInfo().
	 * @param element
	 *            The concrete DOM instance which is a potential drop
	 *            target.
	 * @param event
	 *            The mouse-up event which occurs during drop. This is used to
	 *            determine whether the element should accepts the drop.
	 * @return Custom information to be sent to the server as arguments of 
	 *         the drag'n drop operation.
	 */
	offerDrop: function(clientDragInfo, srcElement, element) {
		return {};
	}
};
