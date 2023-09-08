/**
 * Methods useful for inspecting the GUI.
 * Above all: openGuiInspectorAfterTargetClicked()
 * 
 * Author:  <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */

INSPECT = {
	
	HIDE_ERRORS: !true,
	
	controlIdFormat: /^c[0-9]+$/,
	
	/* 
	 * In some tables (for example table trees like the budget tree in Top-S) the rows have a normal id.
	 * For example 'c12' instead of 'c5.12'.
	 * The "fixed" at the end of the id appears in the fixed columns of "frozen tables".
	 */
	tableRowControlIdFormat: /^c([0-9]+)\.([0-9]+)(fixed)?$/,
	
	isInspectable: function(element) {
		return INSPECT.isWithinControl(element) || INSPECT.isTableCellWithSingleControl(element) || INSPECT.isTableCellContent(element);
	},
	
	/**
	 * Returns the element at given location, recursing down Frames and IFrames.
	 */
	elementFromPointRecursive: function(x, y) {
		
		var contentDocument = function(element) {
			return element.contentWindow.document;
		};
		
		var scrollLeft = function(element) {
			return contentDocument(element).documentElement.scrollLeft;
		};
		
		var scrollTop = function(element) {
			return contentDocument(element).documentElement.scrollTop;
		};
		
		var hasChildren = function(element) {
			return element && element.contentWindow && element.contentWindow.document;
		};
		
		var searchRecursivelyWithinElement = function(result, x, y) {
			var element = result[result.length - 1];
			
			var elementPosition = BAL.getAbsoluteElementPosition(element);
			var xPositionWithinVisiblePartOfElement = x - elementPosition.x;
			var yPositionWithinVisiblePartOfElement = y - elementPosition.y;
			var child = contentDocument(element).elementFromPoint(xPositionWithinVisiblePartOfElement, yPositionWithinVisiblePartOfElement);
			result.push(child);
			
			if (hasChildren(child)) {
				var xPositionWithinWholeElement = xPositionWithinVisiblePartOfElement + scrollLeft(element);
				var yPositionWithinWholeElement = yPositionWithinVisiblePartOfElement + scrollTop(element);
				searchRecursivelyWithinElement(result, xPositionWithinWholeElement, yPositionWithinWholeElement);
			}
		};
		
		var topLevelElement = services.ajax.topWindow.document.elementFromPoint(x, y);
		var result = [topLevelElement];
		if (hasChildren(topLevelElement)) {
			searchRecursivelyWithinElement(result, x, y);
		}
		return result;
	},
	
	isControl: function(element) {
		var hasControlMarker = function(element) {
			return BAL.DOM.containsClass(element, "can-inspect");
		};
		
		return element && hasControlMarker(element);
	},
	
	isDivOrSpanTag: function(element) {
		var tagName = BAL.DOM.getTagName(element);
		return tagName == "span" || tagName == "div";
	},
	
	/**
	 * Returns the nearest parent that has a valid control id.
	 * Even if it is not marked as a control.
	 */
	findControlIdEvenIfHidden: function(element) {
		var controlIdHolder = INSPECT.firstAncestor(element, 
			function(object) { return INSPECT.isControl(object) && INSPECT.controlIdFormat.test(object.id); }
		);
		return controlIdHolder ? controlIdHolder.id : null;
	},
	
	/**
	 * Returns the nearest parent that is marked as a control and has a valid control id.
	 */
	findControlId: function(element) {
		var controlIdHolder = INSPECT.firstAncestor(element, 
			function(object) { return INSPECT.isControl(object); }
		);

		if ( ! controlIdHolder) {
			return null;
		}
		if ( ! INSPECT.controlIdFormat.test(controlIdHolder.id)) {
			return null;
		}
		return controlIdHolder.id;
	},
	
	isWithinControl: function(element) {
		return INSPECT.findControlId(element);
	},
	
	getWaitPane: function() {
		return services.ajax.topWindow.document.getElementById('waitPane');
	},
	
	showInspectWaitPane: function() {
		var waitpane = INSPECT.getWaitPane();
		services.ajax.showWaitPane();
		waitpane.originalCursorStyle = waitpane.style.cursor;
		waitpane.style.cursor = "help";
	},
	
	hideInspectWaitPane: function() {
		var waitpane = INSPECT.getWaitPane();
		services.ajax.hideWaitPane();
		waitpane.style.cursor = waitpane.originalCursorStyle;
		
		// Do not use "delete waitpane.originalCursorStyle", because IE7
		// throws an "Not supported" exception on that.
		waitpane.originalCursorStyle = null;
	},
	
	waitPaneUntilCalled: function(wrappee, thisOfWrappee) {
		INSPECT.showInspectWaitPane();
		return function() {
			INSPECT.hideInspectWaitPane();
			wrappee.apply(thisOfWrappee, arguments);
		};
	},
	
	openGuiInspectorForClickedControl: function(mouseEvent) {
		var path = INSPECT.elementFromPointRecursive(mouseEvent.clientX, mouseEvent.clientY);
		for (var n = path.length - 1; n >= 0; n--) {
			var element = path[n];
			if (INSPECT.isInspectable(element)) {
				INSPECT.openGuiInspector(element);
				return;
			}
		}
	},
	
	openGuiInspector: function(element) {
		var addCoreInformation = function(commandArguments) {
			commandArguments.controlCommand = "openGuiInspector";
			commandArguments.controlID = INSPECT.findControlIdEvenIfHidden(element);
		};
		
		var addTableCellInformation = function(commandArguments) {
			commandArguments.isTableCell = true;
			commandArguments.rowIndex = INSPECT.getIndexOfTableRow(element);
			commandArguments.columnIndex = INSPECT.getIndexOfTableColumn(element);
			commandArguments.isFixedColumn = INSPECT.isWithinFixedColumn(element);
			if (INSPECT.isTreeTableNodeContent(element)){
				commandArguments.isTreeNode = true;
			}
		};
		
		var addTreeNodeInformation = function(commandArguments) {
			commandArguments.controlID = INSPECT.findControlId(element);
			commandArguments.isTreeNode = true;
			commandArguments.treeNodeId = INSPECT.findSurroundingTreeNode(element).id
		};
		
		var createCommandArguments = function() {
			try {
				var commandArguments = {};
				addCoreInformation(commandArguments);
				if (INSPECT.isTableCellWithSingleControl(element)) {
					commandArguments.controlID = INSPECT.getSingleControlOfTableCell(element).id;
				} else if (INSPECT.isTableCellContent(element)) {
					addTableCellInformation(commandArguments);
				} else if (INSPECT.isTreeNodeContent(element)) {
					addTreeNodeInformation(commandArguments);
				}
				return commandArguments;
			} catch (error) {
				if (INSPECT.HIDE_ERRORS) {
					return null;
				} else {
					throw error;
				}
			}
		};
		
		var windowOfElement = BAL.getCurrentWindow(element);
		var commandArguments = createCommandArguments();
		if (commandArguments) {
			windowOfElement.services.ajax.execute("dispatchControlCommand", commandArguments);
		}
	},
	
	findSurroundingTableCell: function(element) {
		var anchestor = element;
		while (anchestor != null) {
			var parentTd = INSPECT.findParentTag(anchestor, 'td');
			if (parentTd == null) {
				return null;
			}
			if (BAL.DOM.containsClass(parentTd, "tl-table__cell")) {
				return parentTd;
			}
			anchestor = parentTd.parentNode;
		}
		return null;
	},
	
	findSurroundingTreeNode: function(element) {
		return INSPECT.firstAncestor(element, 
			function(object) { return BAL.DOM.containsClass(object, 'treeNode'); }
		);
	},
	
	findSurroundingTreeTableNode: function(element) {
		return INSPECT.firstAncestor(element, 
			function(object) {
				if (!BAL.DOM.hasAttributes(object)){
					return false;
				}
				return TL.getTLAttribute(object, 'treenode') == "true"; 
			}
		);
	},
	
	/**
	 * Is the clicked element within a table cell that contains exactly one descendant which is a control?
	 * (Ignoring if the control contains further controls or not.)
	 * 
	 * Intention: Detect if the user clicked on a table cell that contains a single control, but missed that control.
	 */
	isTableCellWithSingleControl: function(element) {
		if ( ! INSPECT.isTableCellContent(element)) {
			return false;
		}
		return INSPECT.getSingleControlOfTableCell(INSPECT.findSurroundingTableCell(element)) != null;
	},
	
	getSingleControlOfTableCell: function(element) {
		var getRelevantOnlyChild = function(node) {
			var relevantChildren = filter(node.childNodes, INSPECT.isDivOrSpanTag);
			if (relevantChildren.length == 1) {
				return relevantChildren[0];
			}
			return null;
		};
		
		var relevantOnlyChildDescendants = createList(element, getRelevantOnlyChild);
		return findFirst(relevantOnlyChildDescendants, INSPECT.isControl);
	},
	
	isTableCellContent: function(element) {
		var surroundingTableCell = INSPECT.findSurroundingTableCell(element);
		if (! surroundingTableCell) {
			return false;
		}
		return INSPECT.findControlIdEvenIfHidden(element) == INSPECT.findControlIdEvenIfHidden(surroundingTableCell);
	},
	
	isTreeNodeContent: function(element) {
		var surroundingTreeNode = INSPECT.findSurroundingTreeNode(element);
		if (!surroundingTreeNode) {
			return false;
		}
		return INSPECT.findControlIdEvenIfHidden(element) == INSPECT.findControlIdEvenIfHidden(surroundingTreeNode);
	},
	
	isTreeTableNodeContent: function(element) {
		var surroundingTreeNode = INSPECT.findSurroundingTreeTableNode(element);
		if (!surroundingTreeNode) {
			return false;
		}
		return INSPECT.findControlIdEvenIfHidden(element) == INSPECT.findControlIdEvenIfHidden(surroundingTreeNode);
	},
	
	/**
	 * Waits for the user to click an ui element and tries to open the gui inspector for it.
	 */
	openGuiInspectorAfterTargetClicked: function() {
		BAL.onceEventListener(
			INSPECT.getWaitPane(),
			'mousedown',
			INSPECT.waitPaneUntilCalled(INSPECT.openGuiInspectorForClickedControl, this),
			this,
			true
		);
	},
	
	getIndexOfTableRow: function(element) {
		var extractTableRowIndex = function(controlId) {
			var tableRowControlIdMatcher = INSPECT.tableRowControlIdFormat.exec(controlId);
			var tableRowIndexAsString = tableRowControlIdMatcher[2];
			return parseInt(tableRowIndexAsString, 10);
		};
		
		var controlId = INSPECT.findControlIdOfSurroundingRow(element);
		if (!controlId) {
			throw new Error("The table row containing the inspected element has no or no valid control id!");
		}
		return extractTableRowIndex(controlId);
	},
	
	getIndexOfTableColumn: function(element) {
		return INSPECT.findParentTag(element, 'td').cellIndex;
	},
	
	/**
	 * Is that element within fixed column of a "frozen table"?
	 */
	isWithinFixedColumn: function(element) {
		var controlIdOfSurroundingRow = INSPECT.findControlIdOfSurroundingRow(element);
		return controlIdOfSurroundingRow.indexOf('fixed') != -1;
	},
	
	findControlIdOfSurroundingRow: function(element) {
		var searchTableRowId = function() {
			var surroundingTrTag = INSPECT.findParentTag(element, 'tr');
			return surroundingTrTag.id;
		};
		
		var isValidTableRowControlId = function(id) {
			var hasTableRowControlIdFormat = function() {
				return INSPECT.tableRowControlIdFormat.test(id);
			};
			
			return id && hasTableRowControlIdFormat(id);
		};
		var tableRowId = searchTableRowId();
		return isValidTableRowControlId(tableRowId) ? tableRowId : null;
	},
	
	findParentTag: function(element, tagName) {
		return INSPECT.firstAncestor(element,
			function(object) { return BAL.DOM.getTagName(object) == tagName; }
		);
	},
	
	firstAncestor: function(element, test) {
		var anchestor = element;
		while (anchestor != null) {
			if (test(anchestor)) {
				return anchestor;
			}
			var parent = anchestor.parentNode;
			if (parent == null) {
				anchestor = null;
			} else {
				anchestor = parent;
			}
		}
		return null;
	}
	
};