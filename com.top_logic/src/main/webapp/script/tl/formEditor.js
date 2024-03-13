function FormEditor() {}

FormEditor.init = function(controlId, putElementBackText) {
	var controlId = controlId;
	var putElementBackText = putElementBackText;
	
	var lastTarget;
	var insertBefore;
	var lastDropTarget;
	var displayedInForm = null;
	var controlContainer = document.getElementById(controlId);
	
	BAL.addEventListener(controlContainer, 'DOMNodeRemoved', deregisterElement);
	
	BAL.addEventListener(controlContainer, 'dragstart', dragstart);
	BAL.addEventListener(controlContainer, 'dragover', dragover);
	BAL.addEventListener(controlContainer, 'dragenter', dragenter);
	BAL.addEventListener(controlContainer, 'dragleave', dragleave);
	BAL.addEventListener(controlContainer, 'dragend', dragend);
	BAL.addEventListener(controlContainer, 'drop', drop);

	BAL.addEventListener(controlContainer, 'updateToolbox', updateToolbox);
	BAL.addEventListener(controlContainer, 'showDropArea', showDropArea);
	BAL.addEventListener(controlContainer, 'removeDropArea', removeDropArea);
	
	/* informs all registered controls for the given listener */
	function triggerEvent(eventName) {
		var controls = window.controls.split(",");
		
		for(var i = 0; i < controls.length; i++) {
			if(document.getElementById(controls[i])) {
				var event;
				   if (typeof(Event) === 'function') {
				        event = new Event(eventName);
				    } else {
				        event = document.createEvent('Event');
				        event.initEvent(eventName, true, true);
				    }
				document.getElementById(controls[i]).dispatchEvent(event);
			}
		}		
	}	
	
	/* updates the toolbox if an element is missing */
	var deregisterElement = function() {
		BAL.removeEventListener(controlContainer, 'DOMNodeRemoved', deregisterElement);

		var dragged = getDragged(); 
		if(dragged) {
			var isTool = dragged.getAttribute("data-tool");
			
			if(isTool === "true") {
				updateToolbox(event);
			}
		}
	};
	
	/* control writes its ID once to the DOM so it will be informed about changes */
	function register() {
		var controls = window.controls;

		if(!controls) {
			controls = controlId;
		} else {
			if(controls.indexOf(controlId) < 0) {
				controls += "," + controlId;
			}
		}

		window.controls = controls;
	}
	register();
	
	/* sends a request to the server to update the toolbox */
	function updateToolbox(event) {	
		if(!inEditor(controlContainer)) {
			services.form.FormEditorControl.updateToolbox(controlId);
		}
	}
	
	/* listener which listens to if the drop area should be rendered */
	function showDropArea(event) {	
		var className = document.getElementById(controlId).className;
		/*
		var dragged = getDragged();
		var type = dragged.getAttribute("data-type"); */
				
		if(className) {				
			if(className.indexOf("cFormEditorToolbar") >= 0) {
				createDropArea(putElementBackText);
			}
		}
	}
	
	/* listener which listens to if the drop area should be removed */
	function removeDropArea(event) {	
		var element = document.getElementById(controlId + "-dropArea");

		if(controlContainer && element) {
			controlContainer.removeChild(element);
		}
	}
	
	/*listener which listens to the start of the drag */
	function dragstart(event) { 
		window.dragged = event.target.id;
		window.draggedDisplay = event.target.style.display;
		event.dataTransfer.setData("Text", event.target.id);
	
		event.target.className += " rf_dragged";
		
		// change opacity if it is not a tool
		var isTool = event.target.getAttribute("data-tool");		
		if(isTool === "false") {
			event.target.style.opacity = "0.4";
		}
		
		// remove preview at the cursor
		if (typeof event.dataTransfer.setDragImage === "function") {
			// create transparent image
			var dragIcon = document.createElement("img");
			dragIcon.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7'
			event.dataTransfer.setDragImage(dragIcon, -10, -10);
		}
		
		// show drop area if the element is inside the editor
		if(inEditor(event.target)) {
			triggerEvent("showDropArea");
		}
	}

	/* listener which listens to the end of a drag */
	function dragend(event) {
		event.preventDefault();
		
		// get information of the position of the element
		if(event.target.className.indexOf("rf_dragged") >= 0) {
			var dragged = getDragged();
		  	var siblingID = dragged.nextSibling ? dragged.nextSibling.getAttribute("data-id") : "";
		  	var parent = findDropTarget(event.target); // findDrop , "rf_innerTarget"
		  	var parentID = "";
		  	if(parent) {
		  		var parentControl = findcFormEditorElement(parent);		  	
		  		if(parentControl) {
		  			parentID = parentControl.getAttribute("data-id");
		  		}
		  	}
		  	moveMember(controlId, event.target, siblingID, parentID);
		}

		removeClass("rf_parent");
	  	highlightEditor(false);
		triggerEvent("removeDropArea");
	}
	
	function removeClass(cssClass) {
		var parentList = document.getElementsByClassName(cssClass);
		for(var i = 0; i < parentList.length; i++) {
			parentList[i].classList.remove(cssClass);
		}		
	}

	/* send request to the server that an element is moved */
	function moveMember(controlId, node, siblingID, parentID) {
		// restore default
		node.style.opacity = '1';
		node.className = (node.className.replace(/ rf_dragged/gi, ""));
	  	lastTarget = null;
	  	window.displayedInForm = null;
	  	displayedInForm = null;
		
	  	// send move request to server
		var elementID = node.getAttribute("data-id");
	  	services.form.FormEditorControl.moveMember(controlId, elementID, siblingID, parentID, inEditor(node));
	}
	
	/* get dragged element */
	function getDragged() {
		return document.getElementById(window.dragged);
	}
	
	/* listener which listens to the movements of the dragged element */
	function dragover(event) {
		event.preventDefault();
		var dragged = getDragged();
		var displayedInForm = window.displayedInForm;
		var last = window.lastTarget;
		
		if(mouseMoved(event) && dragged) {
						
			highlightEditor(true);
			var isInEditor = inEditor(event.target);
			var type = dragged.getAttribute("data-type");
					 
			if(isInEditor) { 
				// make element visible (can be invisible when not in editor)
				dragged.style.display = window.draggedDisplay;
				
				if(isDropTarget(event.target)) {
					if(!isDescendant(dragged, event.target)) {
						if(!findCssClassUp(event.target, "rf_locked")) {
							// append to an empty dropTarget
							var dropTarget = findInnerTarget(event.target);
							dropTarget.appendChild(dragged);
							switchAppearance(dragged);
						}
					}
				} else {
					// get the container of the element of this event
					var dragElement = findDragElement(event.target);
				  	
					if(dragElement) {
								
						// calculate the mouse position relative to the element of this event
						var rect = dragElement.getBoundingClientRect();
						var insertBeforeNew = (((event.clientX - rect.left) ) <= 160);
									
						
						// check if the position has changed to the last call of the listener
						if(lastTarget != dragElement.id || insertBefore != insertBeforeNew) {
							var parent = dragElement.parentNode;

							if(parent && !findCssClassUp(parent, "rf_locked")) {			
								// check if the dragged is parent of the element which should be its parent
								if(!isDescendant(dragged, parent)) {
									var sibling = insertBeforeNew ? dragElement : dragElement.nextSibling;								
									
									// check if the dragged element is entered before the other one and if it has a sibling
									if(insertBeforeNew || dragElement.nextSibling) {
										// insert next to an other element
										parent.insertBefore(dragged, sibling);
									} else {
										// append the dragged element
										parent.appendChild(dragged);
									}
									
									// switch appearance depending on the position of the dragged element (in editor or not)
									switchAppearance(dragged);
								} 
							
								lastTarget = dragElement.id;
								insertBefore = insertBeforeNew;
							} 
						}
					}
				}
			} else { 
				// make element invisible in editor
				dragged.style.display = "none"; 
			}
		}
	}	

	/* Creates an overlaying div to show that an element can be dropped here */
	function createDropArea(text) {
		var control = document.getElementById(controlId);
		
		if(!control.contains(document.getElementById(controlId + "-dropArea"))) {		
			// Background
			var dropArea = document.createElement("div");
			dropArea.id = controlId + "-dropArea";
			dropArea.classList.add("rf_dropArea");
			dropArea.addEventListener("drop", drop);
			control.appendChild(dropArea);
			
			// Text
			var dropText = document.createElement("div");
			dropText.classList.add("rf_dropText");
			dropText.innerHTML = text; 
			dropArea.appendChild(dropText);	
		}
	}
	
	/* show dropArea for removing an element and highlight hovered dropTarget */
	function dragenter(event) {
		if(inEditor(event.target)) {
			triggerEvent("showDropArea");
			var dropTarget = findDropTarget(event.target);
			if(dropTarget) {
				dropTarget.classList.add("rf_parent");
				window.dropTarget = dropTarget.id ? dropTarget.id : dropTarget.parentNode.id;
			}
		} 
	}
	
	/* Resets the highlighting of the hovered dropTarget */
	function dragleave(event) {
		if(inEditor(event.target)) {
			var dropTarget = findDropTarget(event.target);
			dropTargetID = dropTarget.id ? dropTarget.id : dropTarget.parentNode.id;
			
			if(window.dropTarget != dropTargetID) {
				removeClass("rf_parent");
				window.dropTarget = null;
			}
		} 
	}

	/* Send a request to the server if an element was moved to outside of the editor */		
	function drop(event) {
		event.preventDefault();
		
		var dragged = getDragged();	
		
		if(!inEditor(event.target)) {
			moveMember(controlId, dragged, null, null);
		}
	}
	
	/* controls whether a dragged element is displayed as form element or element of the attribute list */
	function switchAppearance(dragged) {
		var displayedInForm = window.displayedInForm;
		
		// switch from cell to list-element and vice versa
		if(inEditor(dragged)) {
			if(displayedInForm != 'true') {
				toFormElement(dragged);
			} 
		} else {	  
			if(displayedInForm == 'true') {
				toListElement(dragged); 
			}
		}
	}
	
	/* Change appearance from list element to form element */
	function toFormElement(inputElement) {			
		if(inputElement) {
			// if a tool is attached to an editor, the toolbox has to be updated to guarantee that there are still all tools
			var isTool = inputElement.getAttribute("data-tool");
			if(isTool === "true") {
				triggerEvent("updateToolbox");
			}
			
			var children = inputElement.childNodes;
			
			for(var i = 0; i < children.length; i++) {
				if(children[i].className) {
					// remove css class which hides form elements
					if(children[i].className.indexOf("field") >= 0) {
						children[i].className = children[i].className.replace(/ hidden/gi, "");
					}
					// add css class which hides list elements once
					if(children[i].className.indexOf("attribute") >= 0 && children[i].className.indexOf("hidden") < 0) {
						children[i].className += " hidden";
					}
				}
			}	
			
			// store that the element is changed already to avoid unnecessary calls of this method
			window.displayedInForm = true; 
		}
	}
	
	/* Change appearance from list element to form element */
	function toListElement(inputElement) {
		if(inputElement) {
			var children = inputElement.childNodes;
			
			for(var i = 0; i < children.length; i++) {
				if(children[i].className) {
					// remove css class which hides list elements
					if(children[i].className.indexOf("attribute") >= 0) {
						children[i].className = children[i].className.replace(/ hidden/gi, "");
					}
					// add css class which hides form elements once
					if(children[i].className.indexOf("field") >= 0 && children[i].className.indexOf("hidden") < 0) {
						children[i].className += " hidden";
					}
				}
			}
			
			// store that the element is changed already to avoid unnecessary calls of this method
			window.displayedInForm = false; 
		}
	}	
	
	/* Find container of the given node which is drag-able */
	function findDragElement(node) {
		var result;

		if(node.draggable) {
			result = node;
		} else {
			if(node.parentNode) { 
				result = findDragElement(node.parentNode);
			} else { 
				result = null; 
			}
		}

		return result;
	}

	/* Check if a given node is a drop target */
	function isDropTarget(node) { 
		var className = "";
		if(node.className) {
			className = node.className;
			if(className.indexOf("cFormEditorElement") >= 0) {
				return false;
			} else if(className.indexOf("dropTarget") >= 0 || className.indexOf("rf_innerTarget") >= 0) {
				return true;
			} else {
				return isDropTarget(node.parentNode);
			}
		} else {
			return isDropTarget(node.parentNode);
		}
	}

	function findcFormEditorElement(node) {
		var result;
		var className = "";

		if(node.className) {
			className = node.className;
		}
		
		if(className.indexOf("cFormEditorElement") >= 0) {
			result = node;
		} else {
			if(node.parentNode) { 
				result = findcFormEditorElement(node.parentNode); 
			} else { 
				result = null; 
			}
		}

		return result;
	}
	
	
	/* Find the drop target where the node lays in */
	function findDropTarget(node) {
		var result;
		var className = "";

		if(node.className) {
			className = node.className;
		}
		
		if(className.indexOf("rf_dropTarget") >= 0) {
			result = node;
		} else {
			if(node.parentNode) { 
				result = findDropTarget(node.parentNode); 
			} else { 
				result = null; 
			}
		}

		return result;
	}
	
	function findInnerTarget(node) {
		var result = findCssClass(node, "rf_innerTarget");
		if(result == null) {
			result = findDropTarget(node);
		}
				
		return result;
	}
		
	function findCssClass(parent, cssClass) {
		var result;
		var className = "";

		if(parent.className) {
			className = parent.className;
		}
		
		if(className.indexOf(cssClass) >= 0) {
			result = parent;
		} else if(className.indexOf("cFormEditorPreview") >= 0) {
			result = null;
		} else {
			if(parent.childNodes) { 
				for(var i = 0; i < parent.childNodes.length; i++) {
					result = findCssClass(parent.childNodes[i], cssClass); 
				}				
			} else { 
				result = null; 
			}
		}

		return result;
	}
	
	function findCssClassUp(node, cssClass) {
		var result;
		var className = "";

		if(node.className) {
			className = node.className;
		}
		
		if(className.indexOf(cssClass) >= 0) {
			result = node;
		} else if(className.indexOf("cFormEditorDisplay") >= 0) {
			result = null;
		} else {
			if(node.parentNode) { 
				result = findCssClassUp(node.parentNode, cssClass); 			
			}
		}

		return result;
	}	

	/* Check if the a given node is inside of the editor */
	function inEditor(node) {
		var editors = document.getElementsByClassName("rf_editor");
		var result = false;
		for(var i=0; i < editors.length; i++) {
			result = (result || editors[i].contains(node));
		}
		
		return result;
	}
	
	function findFrame(node) {
		var result;
		var className = "";

		if(node.className) {
			className = node.className;
		}
		
		if(className.indexOf("rf_frameBorder") >= 0 || className.indexOf("rf_wrapper") >= 0) {
			result = node;
		} else {
			if(node.parentNode) { 
				result = findFrame(node.parentNode); 
			} else { 
				result = null; 
			}
		}

		return result;
	}
	
	/* Checks if a given node is child of a given parent */
	function isDescendant(parent, child) {
	     var node = child.parentNode;
	     while (node != null) {
	         if (node == parent) {
	             return true;
	         }
	         node = node.parentNode;
	     }
	     return false;
	}

	/* Highlight elements in editor */
	function highlightEditor(highlight) {
		node = document.getElementById(controlId);

		if(node) {
			var className = ""; 
			if(node.className) {
				className = node.className;
			}
			
			if(highlight) {
				if(className.indexOf(" rf_highlighted") < 0) {
					node.className = className + " rf_highlighted";
				}
			} else {
				node.className = (className.replace(/ rf_highlighted/gi, ""));
			}
		}
	}
	
	function mouseMoved(event) {
		var mousePos = BAL.mouseCoordinates(event);
		var offset = 5;
		if(window.mouseX >= (mousePos.x - offset) && window.mouseX <= (mousePos.x + offset)
				&& window.mouseY >= (mousePos.y - offset) && window.mouseY <= (mousePos.y + offset)) {
			return false;
		} else {
			window.mouseX = mousePos.x;
			window.mouseY = mousePos.y;
			return true;
		}
		
		return true;
	}
};