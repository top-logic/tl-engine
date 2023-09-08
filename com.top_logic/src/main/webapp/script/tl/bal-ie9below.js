/**
 * Browser abstraction layer. 
 * 
 * Specific adjustments for Internet Explorer.
 *
 * Author:  <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 */
BAL.extend({
	
	requestDownload: function(downloadId, url) {
		// Note: The download must be taken out of the rendering flow of the AJAX response,
		// because the rendering engine von IE immediately stops after opening the "Save as" 
		// popup dialog. The effect is that not all updates made to the client DOM become 
		// displayed.
		window.setTimeout(
			function() {
				BAL._enterDownloadFrame(downloadId, url);
			}, 100);
	},
	
	getCurrentWindow: function(element) {
		var currentWindow = null;
		var currentDocument = BAL.getCurrentDocument(element);
		if(currentDocument != null) {
			currentWindow = currentDocument.parentWindow;
		}
		return currentWindow;
	},
	
	getFrameWindow: function(frame) {
		return frame.contentWindow;
	},
	
	hasAltModifier : function(event) {
		var source = BAL.getEventSource(event);
		if(source != null) {
			return BAL.getCurrentWindow(source).event.altLeft;
		} else {
			return event.altKey;
		}
	},
	
	disableComponent: function(windowRef) {
        var dynamicWaitPane = windowRef.document.createElement("div");
        windowRef.document.body.appendChild(dynamicWaitPane);
        dynamicWaitPane.style.position = "absolute";
        dynamicWaitPane.style.top = "0";
        dynamicWaitPane.style.left = "0";
        dynamicWaitPane.style.height = "100%";
        dynamicWaitPane.style.width = "100%";
        dynamicWaitPane.style.cursor = "wait";
        dynamicWaitPane.style.backgroundColor = "#FFEE00";
        dynamicWaitPane.style.filter = "alpha(opacity=0)";
    },

    installKeyBindingHandler: function(handler) {
        if (document.onkeydown == null) {
            document.onkeydown=handler;
        }
    },

	setOuterHTML: function(targetNode, fragment) {
		var updateFunction = IE9SetOuterHTMLFunctions[targetNode.nodeName];
		if (updateFunction != undefined) {
			updateFunction(targetNode, fragment);
		} else {
			targetNode.outerHTML = fragment;
		}
	},
    
	setInnerHTML: function(targetNode, fragment) {
		var updateFunction = IE9SetInnerHTMLFunctions[targetNode.nodeName];
		if (updateFunction != undefined) {
			updateFunction(targetNode, fragment);
		} else {
			targetNode.innerHTML = fragment;
		}
	},
	
	insertAdjacentHTML: function(targetNode, position, fragment) {
		var updateFunction = IE9InsertAdjacentHTMLFunctions[targetNode.nodeName];
		if (updateFunction != undefined) {
			updateFunction(targetNode, position, fragment);
		} else {
			targetNode.insertAdjacentHTML(position, fragment);
		}
	},

  	getComputedStyle: function(element, property) {
		return element.currentStyle[property];
  	},
  	
  	getComputedStyleInt: function(element, property) {
  		var styleValue = this.getComputedStyle(element, property);
  		if (styleValue != null && styleValue != "" && styleValue != "auto") {
  			return parseInt(styleValue);
  		} else {
  			return 0;
  		}
  	},
  	
  	requestAnimationFrame: function(animationFunction) {
  		return window.setTimeout(animationFunction, 0);
	},
	
	cancelAnimationFrame: function(id) {
		window.clearTimeout(id);
	}
});

IE9InsertAdjacentHTMLFunctions = {};
IE9InsertAdjacentHTMLFunctions["tbody"] = function(targetNode, position, fragment) {
	var sourceParent = IE9ParseFragment(targetNode, position, fragment, IE9ParseHTMLFunctions.tbody, IE9ParseHTMLFunctions.tr);
	IE9InsertAdjacentNodes(targetNode, position, sourceParent);
};
IE9InsertAdjacentHTMLFunctions["tr"] = function(targetNode, position, fragment) {
	var sourceParent = IE9ParseFragment(targetNode, position, fragment, IE9ParseHTMLFunctions.tr, IE9ParseHTMLFunctions.td);
	IE9InsertAdjacentNodes(targetNode, position, sourceParent);
};
IE9InsertAdjacentHTMLFunctions["td"] = function(targetNode, position, fragment) {
	var sourceParent = IE9ParseFragment(targetNode, position, fragment, IE9ParseHTMLFunctions.td, IE9ParseHTMLFunctions.div);
	IE9InsertAdjacentNodes(targetNode, position, sourceParent);
};
IE9InsertAdjacentHTMLFunctions["TBODY"] = IE9InsertAdjacentHTMLFunctions["tbody"];
IE9InsertAdjacentHTMLFunctions["TR"] = IE9InsertAdjacentHTMLFunctions["tr"];
IE9InsertAdjacentHTMLFunctions["TD"] = IE9InsertAdjacentHTMLFunctions["td"];

IE9ParseFragment = function(targetNode, position, fragment, outerParser, innerParser) {
	var parser;
	if (position == "beforebegin") {
		parser = outerParser;
	}
	else if (position == "afterbegin") {
		parser = innerParser;
	}
	else if (position == "beforeend") {
		parser = innerParser;
	}
	else if (position == "afterend") {
		parser = outerParser;
	}
	else {
		throw "Invalid position '" + position + "'.";
	}
	
	return parser(targetNode, fragment);
};

IE9InsertAdjacentNodes = function(targetNode, position, sourceParent) {
	var parent;
	var before;
	if (position == "beforebegin") {
		parent = targetNode.parentNode;
		before = targetNode;
	}
	else if (position == "afterbegin") {
		parent = targetNode;
		before = targetNode.firstChild;
	}
	else if (position == "beforeend") {
		parent = targetNode;
		before = null;
	}
	else if (position == "afterend") {
		parent = targetNode.parentNode;
		before = targetNode.nextSibling;
	}
	else {
		throw "Invalid position '" + position + "'.";
	}
	while (sourceParent.firstChild != null) {
		var newNode = sourceParent.firstChild;
		sourceParent.removeChild(newNode);
		
		parent.insertBefore(newNode, before);
	}
};

IE9SetOuterHTMLFunctions = {};
IE9SetOuterHTMLFunctions["tbody"] = function(targetNode, fragment) {
	var newNode = IE9ParseHTMLFunctions.tbody(targetNode, fragment).firstChild;
	newNode.parentNode.removeChild(newNode);
	targetNode.parentNode.replaceChild(newNode, targetNode);
};
IE9SetOuterHTMLFunctions["tr"] = function(targetNode, fragment) {
	var newNode = IE9ParseHTMLFunctions.tr(targetNode, fragment).firstChild;
	newNode.parentNode.removeChild(newNode);
	targetNode.parentNode.replaceChild(newNode, targetNode);
};
IE9SetOuterHTMLFunctions["td"] = function(targetNode, fragment) {
	var newNode = IE9ParseHTMLFunctions.td(targetNode, fragment).firstChild;
	newNode.parentNode.removeChild(newNode);
	targetNode.parentNode.replaceChild(newNode, targetNode);
};
IE9SetOuterHTMLFunctions["TBODY"] = IE9SetOuterHTMLFunctions["tbody"];
IE9SetOuterHTMLFunctions["TR"] = IE9SetOuterHTMLFunctions["tr"];
IE9SetOuterHTMLFunctions["TD"] = IE9SetOuterHTMLFunctions["td"];

IE9SetInnerHTMLFunctions = {};
IE9SetInnerHTMLFunctions["tbody"] = function(targetNode, fragment) {
	IE9SetInnerHTMLTemplate(targetNode, fragment, IE9InsertAdjacentHTMLFunctions.tbody);
};
IE9SetInnerHTMLFunctions["tr"] = function(targetNode, fragment) {
	IE9SetInnerHTMLTemplate(targetNode, fragment, IE9InsertAdjacentHTMLFunctions.tr);
};
IE9SetInnerHTMLFunctions["td"] = function(targetNode, fragment) {
	IE9SetInnerHTMLTemplate(targetNode, fragment, IE9InsertAdjacentHTMLFunctions.td);
};
IE9SetInnerHTMLFunctions["TBODY"] = IE9SetInnerHTMLFunctions["tbody"];
IE9SetInnerHTMLFunctions["TR"] = IE9SetInnerHTMLFunctions["tr"];
IE9SetInnerHTMLFunctions["TD"] = IE9SetInnerHTMLFunctions["td"];

IE9SetInnerHTMLTemplate = function(targetNode, fragment, insertAdjacentHTMLFunction) {
	while (targetNode.firstChild != null) {
		targetNode.removeChild(targetNode.firstChild);
	}
	insertAdjacentHTMLFunction(targetNode, "afterBegin", fragment);
};

IE9ParseHTMLFunctions = {};
IE9ParseHTMLFunctions.div = function(targetNode, fragment) {
	var container = targetNode.ownerDocument.createElement("div");
	container.innerHTML = fragment;
	return container;
};
IE9ParseHTMLFunctions.tbody = function(targetNode, fragment) {
	var container = targetNode.ownerDocument.createElement("div");
	container.innerHTML = "<table>" + fragment + "</table>";
	return container.firstChild;
};
IE9ParseHTMLFunctions.tr = function(targetNode, fragment) {
	var container = targetNode.ownerDocument.createElement("div");
	container.innerHTML = "<table><tbody>" + fragment + "</tbody></table>";
	return container.firstChild.firstChild;
};
IE9ParseHTMLFunctions.td = function(targetNode, fragment) {
	var container = targetNode.ownerDocument.createElement("div");
	container.innerHTML = "<table><tbody><tr>" + fragment + "</tr></tbody></table>";
	return container.firstChild.firstChild.firstChild;
};
 
BAL.DOM.extend({
	/**
	 * Hook for transparently accessing node objects that are build in 
	 * plain JavaScript.
	 */
	getFirstChild: function(node) {
		if ("getFirstChild" in node) {
			return node.getFirstChild();
		} else {
			return node.firstChild;
		}
	},
		
	/**
	 * Hook for transparently accessing node objects that are build in 
	 * plain JavaScript.
	 */
	getNextSibling: function(node) {
		if ("getNextSibling" in node) {
			return node.getNextSibling();
		} else {
			return node.nextSibling;
		}
	},
	
	getTextContent: function(node) {
	   if (node.text != undefined){
	       return node.text;
	   }
		return node.data;
	},
	
	getLocalName: function(node) {
		return node.baseName;
	},
	
	createRange: function(doc) {
		return new BAL.IE.Range(doc);
	},

	importNode: function(doc, node) {
		var result;
		switch (node.nodeType) {
			case Node.ELEMENT_NODE:
				var tagName = BAL.DOM.getTagName(node);
				var container = doc.createElement("div");
				var content;
				if ("xml" in node) {
					content = node.xml;
				} else {
					if ("outerHTML" in node) {
						content = node.outerHTML;
					} else {
						throw new Error("could not import node '" + node + "' to document '" + doc +"'.");
					}
				}
				var innerHtml;
				if (tagName == "td" || tagName == "th") {
					innerHtml = "<table><tbody><tr>" + content + "</tr></tbody></table>"; 
				} else if (tagName == "tr") {
					innerHtml = "<table><tbody>" + content + "</tbody></table>"; 
				} else if (tagName == "tbody" || tagName == "thead") {
					innerHtml = "<table>" + content + "</table>";
				} else {
					innerHtml = content;
				}
				container.innerHTML = innerHtml;
				content = null;
				innerHtml = null;
				if (tagName == "td" || tagName == "th") {
					var table = BAL.DOM.getFirstChild(container);
					var tbody = BAL.DOM.getFirstChild(table);
					var tr = BAL.DOM.getFirstChild(tbody);
					result = BAL.DOM.getFirstChild(tr);
					tr.removeChild(result);
					tbody.removeChild(tr);
					tr = null;
					table.removeChild(tbody);
					tbody = null;
					container.removeChild(table);
					table = null;
				} else if (tagName == "tr") {
					var table = BAL.DOM.getFirstChild(container);
					var tbody = BAL.DOM.getFirstChild(table);
					result = BAL.DOM.getFirstChild(tbody);
					tbody.removeChild(result);
					table.removeChild(tbody);
					tbody = null;
					container.removeChild(table);
					table = null;
				} else if (tagName == "tbody" || tagName == "thead") {
					var table = BAL.DOM.getFirstChild(container);
					result = BAL.DOM.getFirstChild(table);
					table.removeChild(result);
					table = null;
				} else {
					result = BAL.DOM.getFirstChild(container);
					container.removeChild(result);
				}
				container = null;
				break;
				
			case Node.TEXT_NODE:
				result = doc.createTextNode(node.data);
				break;
				
			case Node.DOCUMENT_FRAGMENT_NODE:
				result = new BAL.IE.DocumentFragment(doc);
				var currentChild = node.getFirstChild();
				while (currentChild != null) {
					var importedChild = BAL.DOM.importNode(doc, currentChild);
					result.appendChild(importedChild);
					importedChild = null;
					currentChild = BAL.DOM.getNextSibling(currentChild);
				}
				break;
			
			default:
				throw new Error("Unsupported node type: " + node.nodeType);
		}
		
		return result;
	},
	
	_EVENT_HANDLERS: {
		onclick: true,
		onchange: true,
		onmousedown: true,
		onmouseup: true
	},
	
	_isEventHandler: function(attrName) {
		return attrName in this._EVENT_HANDLERS;
	},
	
    deleteContents: function(range) {
        if (range.ownerDocument.xml != undefined) {
            range.deleteContents();
        }
        var reference = BAL.DOM.getNodeAtOffset(range.startContainer, range.startOffset);
        while (range.startOffset < range.endOffset) {
            var deletedNode = reference;
            reference = BAL.DOM.getNextSibling(reference);
            // Using this function also triggers the DOMNodeRemovedFromDocument event
            BAL.DOM.removeChild(range.startContainer, deletedNode);
            range.endOffset--;
        }
    },
    
    getNonStandardAttribute: function(element, attribute) {
        var attr = element.getAttributeNode(attribute);
        if (attr == null) {
            return null;
        } else {
            return attr.value; 
        }
    },
    
    hasAttributes: function(element) {
    	if (element == null) {
    		return false;
    	}
    	return element.attributes != null;
    },
    
    createXMLDocument: function(aDOMString) {
    	var parser = new ActiveXObject("Microsoft.XMLDOM");
		parser.loadXML(aDOMString);
		parser.async = "false"; 
    	return parser;
    }
	
});

/**
 * Container for support classes. These classes are used for creating 
 * functionality not present in Internet Explorer. 
 */
BAL.IE = new Object();

/**
 * A document fragment class in pure JavaScript.
 */
BAL.IE.DocumentFragment = function(doc) {
	this.nodeType = Node.DOCUMENT_FRAGMENT_NODE;
	this.ownerDocument = doc;
	this.container = doc.createElement('fragment');
};

BAL.IE.DocumentFragment.prototype = {
	appendChild: function(newChild) {
		return this.container.appendChild(newChild);
	},
	
	getFirstChild: function() {
		return this.container.firstChild;
	},
	
	removeChild: function(node) {
		return this.container.removeChild(node);
	}
};

/**
 * A range class in pure JavaScript. It is supposed that the startContainer is the same as the endContainer.
 */
BAL.IE.Range = function(doc) {
	this.ownerDocument = doc;
	   // according to W3C Range there is a startContainer and an endContainer
	this.startContainer = null; 
	this.endContainter = null;
};

BAL.IE.Range.prototype = {
	setStart: function(node, index) {
        this._internalSetEnd(node, index);
		this.startContainer = node;
		this.startOffset = index;
	},

    _internalSetEnd: function(newEndContainer, newEndOffset) {
        if (this.endContainer != null) {
            if (this.endContainer != newEndContainer) {
                throw new Error("this range supposes that the startContainer and the endContainer are equal");
            }
            if (this.endOffset < newEndOffset) {
                this.endOffset = newEndOffset;
            }
        } else {
            this.endContainer = newEndContainer;
            this.endOffset = newEndOffset;
        }
    },
    	
	setEnd: function(node, index) {
        this._internalSetStart(node, index);
        this.endContainer = node;
        this.endOffset = index;
	},
	
	_internalSetStart: function(newStartContainer, newStartIndex) {
        if (this.startContainer != null) {
            if (this.startContainer != newStartContainer) {
                throw new Error("this range supposes that the startContainer and the endContainer are equal");
            }
            if (this.startOffset > newStartIndex) {
                this.startOffset = newStartIndex;
            }
        } else {
            this.startContainer = newStartContainer;
            this.startOffset = newStartIndex;
        } 	
	},

	selectNode: function(node) {
		this.startContainer = node.parentNode;
		this.endContainer = node.parentNode;
		this.startOffset = BAL.DOM.getNodeOffset(node);
		this.endOffset = this.startOffset + 1;
	},

	selectNodeContents: function(parent) {
		this.startContainer = parent;
		this.endContainer = parent;
		this.startOffset = 0;
		if (parent.lastChild != null) {
			this.endOffset = BAL.DOM.getNodeOffset(parent.lastChild) + 1;
		} else {
			this.endOffset = 0;
		}
	},
	
	setStartBefore: function(node) {
        var offset = BAL.DOM.getNodeOffset(node);
        this._internalSetEnd(node.parentNode, offset);
        this.startContainer = node.parentNode;
		this.startOffset = offset;
	},
	
	setEndAfter: function(node) {
        var offset = BAL.DOM.getNodeOffset(node) + 1;
        this._internalSetStart(node.parentNode, offset);
        this.endContainer = node.parentNode;
		this.endOffset = offset;
	},

	insertNode: function(node) {
		var reference = BAL.DOM.getNodeAtOffset(this.startContainer, this.startOffset);
		
		var importRequired = this.startContainer.ownerDocument != node.ownerDocument;
		if (node.nodeType == Node.DOCUMENT_FRAGMENT_NODE) {
			for (var child = BAL.DOM.getFirstChild(node); child != null; ) {
				var insertedChild = child;
				child = BAL.DOM.getNextSibling(child);
				
				node.removeChild(insertedChild);

				if (insertedChild.nodeType != Node.ELEMENT_NODE) {
					continue;
				}

				this.startContainer.insertBefore(insertedChild, reference);
				this.endOffset++;
			}
		} else {
			if (importRequired) {
				node = BAL.DOM.importNode(this.startContainer.ownerDocument, node);
			}
			this.startContainer.insertBefore(node, reference);
			this.endOffset++;
		}
	},
	
	deleteContents: function() {
		var reference = BAL.DOM.getNodeAtOffset(this.startContainer, this.startOffset);
		while (this.startOffset < this.endOffset) {
			var deletedNode = reference;
			reference = BAL.DOM.getNextSibling(reference);
            this.startContainer.removeChild(deletedNode);
			this.endOffset--;
		}
	},
	
	extractContents: function() {
		var result = new BAL.IE.DocumentFragment(this.ownerDocument);

		var reference = BAL.DOM.getNodeAtOffset(this.startContainer, this.startOffset);
		while (this.startOffset < this.endOffset) {
			var deletedNode = reference;
			reference = reference.nextSibling;
            this.startContainer.removeChild(deletedNode);
			this.endOffset--;
			
			result.appendChild(deletedNode);
		}
		
		return result;
	}
	
};

/**
 * Node object with constants for values of the nodeType property. This 
 * object is specified for DOM level 2 but not present in Internet Explorer.
 */
Node = {
	/** This constant is of type Number and its value is 1. */
	ELEMENT_NODE: 1,

	/** This constant is of type Number and its value is 2. */
	ATTRIBUTE_NODE: 2,
	
	/** This constant is of type Number and its value is 3. */
	TEXT_NODE: 3,

	/** This constant is of type Number and its value is 4. */
	CDATA_SECTION_NODE: 4,

	/** This constant is of type Number and its value is 5. */
	ENTITY_REFERENCE_NODE: 5,

	/** This constant is of type Number and its value is 6. */
	ENTITY_NODE: 6,

	/** This constant is of type Number and its value is 7. */
	PROCESSING_INSTRUCTION_NODE: 7,

	/** This constant is of type Number and its value is 8. */
	COMMENT_NODE: 8,

	/** This constant is of type Number and its value is 9. */
	DOCUMENT_NODE: 9,

	/** This constant is of type Number and its value is 10. */
	DOCUMENT_TYPE_NODE: 10,

	/** This constant is of type Number and its value is 11. */
	DOCUMENT_FRAGMENT_NODE: 11,

	/** This constant is of type Number and its value is 12. */
	NOTATION_NODE: 12
};
