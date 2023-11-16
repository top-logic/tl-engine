/**
 * Table Object Modell
 * 
 * Includes functions for tables and identifies the position of an element in a browser.
 *
 * Author:  <a href=mailto:bhu@top-logic.com>Bernhard Haumacher</a>
 * FIXME : ColDetection center mid rework and border with color
 * TODO : Persistenz for users
 * 
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
TABLE = {
	
	doLog: false,

	UNDEFINED_DISPLAY_VERSION: -1,
	MINIMUM_COLUMN_WIDTH: 12,
	BROWSER_MAXIMUM_WIDTH: 900000,
	MINIMUM_FLEX_SPACE: 150, // 150 is randomely choosen for the space for flexible part
	COLUMN_MOVE_INIT_DELTA: 3, //px - amount of pixels mouse must have been moved, before column movement is recognized
	DEFAULT_TABLE_SEPARATOR_WIDTH: 5, //px
	tableSliceManagers: null,
	
	setColumnWidth: function(element, columnWidth) {
		BAL.setEffectiveWidth(element, columnWidth);
	},

	setColumnCellWidth: function(td, columnWidth) {
		this.setColumnWidth(td, columnWidth);
		TL.setTLAttribute(td, "column-width", columnWidth);
	},
	
	/**
	 * function called during moving the mouse when column drag & drop is
	 * activated, i.e. if the user wants to change the order of columns this
	 * function is triggered when he moves the mouse.
	 * 
	 * @param {Object}
	 *            event the event occured when moving the mouse
	 * @param {TABLE.MouseEventData}
	 *            mouseEvtData data holder object which holds information about
	 *            the mouse position when activating drag & drop
	 * @return {Boolean} always false
	 */
	mouseMove: function(event, mouseEvtData) {
		mouseEvtData.dragOrSort = true;
		if (mouseEvtData.isFirstMove) {
			TABLE.createNewDivElement(mouseEvtData);
			mouseEvtData.isFirstMove = false;
		}
		event = event || window.event;

		var mousePosition = BAL.mouseCoordinates(event);

		BAL.setElementX(mouseEvtData.dragObject, mousePosition.x - mouseEvtData.relativeMouse.x);
		BAL.setElementY(mouseEvtData.dragObject, mousePosition.y - mouseEvtData.relativeMouse.y);
		TABLE.colDetection(mouseEvtData.dragObject, mouseEvtData.limits);
		TABLE.checkForInsert(mouseEvtData, mousePosition);

		if (mouseEvtData.scrollLeftAreaUpperBoundX) {
			if (   (mousePosition.x >= mouseEvtData.scrollLeftAreaLowerBoundX)
                && (mousePosition.x <= mouseEvtData.scrollLeftAreaUpperBoundX)) {

				TABLE.scrollLeftFunction(mouseEvtData);
				
			} else if ((mousePosition.x >= mouseEvtData.scrollRightAreaLowerBoundX)
					&& (mousePosition.x <= mouseEvtData.scrollRightAreaUpperBoundX)) {

				TABLE.scrollRightFunction(mouseEvtData);
				
			} else {
        		if (mouseEvtData.timerID != 0) {
        			window.clearInterval(mouseEvtData.timerID);
        			mouseEvtData.timerID = 0;
        		}
			}
		}

		return false;
	},
	
	/**
	 * Starts a delyed function which scrolls the scroll pane to the left all
	 * 10ms.
	 * 
	 * @param {TABLE.MouseEventData}
	 *            mouseEvtData data holder object which holds information about
	 *            the mouse position when activating drag & drop
	 * @return {Boolean} always false
	 */
	scrollLeftFunction: function(mouseEvtData) {
		if (mouseEvtData.timerID == 0) {
			var scrollLeft = function() {
				mouseEvtData.horizontalScrollBar.scrollLeft -= mouseEvtData.scrollStepWidth;
				return false;
			};

			// Delay start, because of the user possibly wants to drag a flex
			// column to fix header or vice versa.
			var delayFunction = function() {
				mouseEvtData.timerID = window.setInterval(scrollLeft, 10);
			};
			mouseEvtData.timerID = window.setTimeout(delayFunction, 500);
			return false;
		}
	},

    /**
	 * Starts a function which scrolls the scroll pane to the right all 10ms.
	 * 
	 * @param {TABLE.MouseEventData}
	 *            mouseEvtData data holder object which holds information about
	 *            the mouse position when activating drag & drop
	 * @return {Boolean} always false
	 */
	scrollRightFunction: function(mouseEvtData) {
		if (mouseEvtData.timerID == 0) {
			var scrollRight = function() {
				mouseEvtData.horizontalScrollBar.scrollLeft += mouseEvtData.scrollStepWidth;
				return false;
			};
			
			mouseEvtData.timerID = window.setInterval(scrollRight, 10);

			return false;
		}
	},

	/**
	 * Change color of the elements by changing the class.
	 * 
	 * @param {TABLE.MouseEventData}
	 *            mouseEvtData holder object for the event data during start of
	 *            drag and drop
	 * @param {TABLE.Coordinates}
	 *            mousePosition current position of the mouse to retrieve object
	 *            to change color from
	 * @return {Boolean} always false
	 */
	checkForInsert: function(mouseEvtData, mousePosition) {
		var tableheader = mouseEvtData.headerList;
		var lookupFirstMatchingColumn = true;
		
		for (var i = 0; i < tableheader.length; i++) {
			var currentTableHeader = tableheader[i];
			var elementPos = TABLE.getElementPosition(currentTableHeader);
			var left = elementPos.topX;
			var right = elementPos.belowX;
			if (left < mousePosition.x && mousePosition.x < right && lookupFirstMatchingColumn) {
				var middle = (right + left) / 2 ;
				if (mousePosition.x <= middle) {
					TABLE.markLeftBorderForInsert(mouseEvtData, tableheader, i);
				} else {
					TABLE.markRightBorderForInsert(mouseEvtData, tableheader, i);
					if (i + 1 < tableheader.length) {
						//Must increase index as otherwise next loop run would remove added classes
        				i++;
					}
				}
				lookupFirstMatchingColumn = false;
			} else {
				BAL.DOM.removeClass(currentTableHeader, "insertLeft");
				BAL.DOM.removeClass(currentTableHeader, "insertRight");
			}
		}
		
		if(tableheader.length > 0) {
			var mostLeftPosition = TABLE.getElementPosition(tableheader[0]).topX;
			var mostRightPosition = TABLE.getElementPosition(tableheader[tableheader.length - 1]).belowX;
			if(mousePosition.x < mostLeftPosition) {
				TABLE.markLeftBorderForInsert(mouseEvtData, tableheader, 0);
			} else if(mousePosition.x > mostRightPosition) {
				TABLE.markRightBorderForInsert(mouseEvtData, tableheader, tableheader.length - 1);
			}
		}
		
		return false;
	},
	
	markLeftBorderForInsert: function(mouseEvtData, tableHeader, elementIndex) {
		var currentTableHeader = tableHeader[elementIndex];
		mouseEvtData.changeElement = currentTableHeader.columnIndex;
		BAL.DOM.addClass(currentTableHeader, "insertLeft");
		BAL.DOM.removeClass(currentTableHeader, "insertRight");
		if (!currentTableHeader.isFirstFlexNode && elementIndex > 0) {
			BAL.DOM.addClass(tableHeader[elementIndex - 1], "insertRight");
		}
		if(currentTableHeader.isFirstFlexNode) {
			if(mouseEvtData.nodePosition == "lastFixNode") {
				mouseEvtData.isFixFlexColumnExchange = !mouseEvtData.isFixFlexColumnExchange;
			}
			mouseEvtData.nodePosition = "firstFlexNode";
		} else {
			mouseEvtData.nodePosition = "commonNode";
		}
	},
	
	markRightBorderForInsert: function(mouseEvtData, tableHeader, elementIndex) {
		var currentTableHeader = tableHeader[elementIndex];
		mouseEvtData.changeElement = currentTableHeader.columnIndex + 1;
		BAL.DOM.addClass(currentTableHeader, "insertRight");
		BAL.DOM.removeClass(currentTableHeader, "insertLeft");
		if (elementIndex + 1 < tableHeader.length) {
			// update also the next cell.
			var nextTableHeader = tableHeader[elementIndex + 1];
			if(!nextTableHeader.isFirstFlexNode) {
				BAL.DOM.addClass(nextTableHeader, "insertLeft");
			}
			BAL.DOM.removeClass(nextTableHeader, "insertRight");
			if(nextTableHeader.isFirstFlexNode) {
				if(mouseEvtData.nodePosition == "firstFlexNode") {
					mouseEvtData.isFixFlexColumnExchange = !mouseEvtData.isFixFlexColumnExchange;
				}
				mouseEvtData.nodePosition = "lastFixNode";
			} else {
				mouseEvtData.nodePosition = "commonNode";
			}
		}
	},
	
	/**
	 * Retrieves the element position relative to a specified ancestor node.
	 * 
	 * @param {Node}
	 *            child the child to determine relative position from
	 * @param {Node}
	 *            ancestor an ancestor of the given child
	 * @return {Object} contains the X and Y coordinate of the child
	 *         relative to the parent
	 */
	relativeElementPosition: function(child, ancestor) {

		var relativeCurrentNodePosition = BAL.getAbsoluteElementPosition(child);
		var parentNodePosition = BAL.getAbsoluteElementPosition(ancestor);
		relativeCurrentNodePosition.x -= parentNodePosition.x;
		relativeCurrentNodePosition.y -= parentNodePosition.y;

		return relativeCurrentNodePosition;
	},

	/**
	 * Returns an JSON Object which contains position informations.
	 * 
	 * @param {Node}
	 *            element an DOM element from which you want to know the
	 *            absolute Position, not the Viewport position.
	 * @return {TABLE.ElementPosition} containing information about the
	 *         coordinates (the four corners), width, and height.
	 */
	getElementPosition: function(element) {

		var width = BAL.getElementWidth(element);
		var height = BAL.getElementHeight(element);

		var absolutePos = BAL.getAbsoluteElementPosition(element);

		return new TABLE.ElementPosition(absolutePos.x, absolutePos.y, width, height);
	},

	/**
	 * Removes the drag & drop insert marker set by {@link TABLE.checkForInsert}
	 * 
	 * @param {TABLE.MouseEventData}
	 *            mouseEvtData data used to set the insert marker
	 */
	removeInsertMarker: function(mouseEvtData) {
		var tableheader = mouseEvtData.headerList;
		for (var i = 0; i < tableheader.length; i++) {
			BAL.DOM.removeClass(tableheader[i], "insertRight");
			BAL.DOM.removeClass(tableheader[i], "insertLeft");
		}

	},

	/**
	 * Adopts the X and Y coordinates of the given DOM element that it fits in
	 * the given limits (as good as it can be done).
	 * 
	 * @param {Node}
	 *            element the element which must fit into the given limits
	 * @param {TABLE.ElementPosition}
	 *            limits the limits to ensure that the element fits into
	 */
	colDetection: function(domElement, limits) {
		var domPosition = TABLE.getElementPosition(domElement);

		// test x position out of range
		if (domPosition.topX < limits.topX) {
			BAL.setElementX(domElement, limits.topX);
		} else {
			if (domPosition.belowX > limits.belowX) {
				BAL.setElementX(domElement, limits.belowX - domPosition.width);
			}
		}

		// test y position out of range
		if (domPosition.topY < limits.topY) {
			BAL.setElementY(domElement, limits.topY);
		} else {
			if (domPosition.belowY > limits.belowY) {
				BAL.setElementY(domElement, limits.belowY - domPosition.height);
			}
		}
	},

	/**
	 * Finds the leas ancestor of the given element with the given tagname (may
	 * be the element self or null if there is none)
	 * 
	 * @param {Node}
	 *            elem the element to get ancestor from
	 * @param {String}
	 *            tagName the tag name of the desired ancestor. must not be null
	 * @return {Node} the "least" ancestor of the given element or null if there
	 *         is none.
	 */
	findAncestorWithTagName: function(elem, tagName) {
		var lowerTagName = tagName.toLowerCase();
		while (elem != null && BAL.DOM.getTagName(elem) != lowerTagName) {
			elem = elem.parentNode;
		}
		return elem;
	},

	/**
	 * Creates the element moved during the drag & drop operation
	 * 
	 * @param {TABLE.MouseEventData}
	 *            mouseEvtData
	 */
	createNewDivElement: function(mouseEvtData) {
		// mouseEvtData.limits = this.getElementPosition(this.dragElement.parentNode);
		mouseEvtData.oldclass = mouseEvtData.dragElement.className;
		// document.body.appendChild(this.dragObject);
		// this.dragObject = document.createElement("div");
		// alert(this.dragObject);

		// Give the right CSS class
		BAL.DOM.addClass(mouseEvtData.dragObject, "movingheader");
		BAL.addEventListener(mouseEvtData.dragObject, 'mouseover', function(event) {
			BAL.eventStopPropagation(event);
		}, true);

		// Get the correct Name
		mouseEvtData.dragObject.innerHTML = mouseEvtData.dragElement.innerHTML;

		// FF
		mouseEvtData.dragObject.style.opacity = '.50';

		// IE
		mouseEvtData.dragObject.style.filter = 'alpha(opacity=40)';

		var pos = TABLE.getElementPosition(mouseEvtData.dragElement);

		// alert("Width: " + pos.width + " Height " + pos.height + " topX: " + pos.topX + " topY:" + pos.topY);
		var x = mouseEvtData.mousepos.x - pos.topX;
		var y = mouseEvtData.mousepos.y - pos.topY;

		mouseEvtData.relativeMouse = new BAL.Coordinates(x, y);

		BAL.setElementX(mouseEvtData.dragObject, pos.topX);
		BAL.setElementY(mouseEvtData.dragObject, pos.topY);
		BAL.setElementWidth(mouseEvtData.dragObject, pos.width);
		BAL.setElementHeight(mouseEvtData.dragObject, pos.height);
	},

	/**
	 * Determines whether the table rendered by the control with the given ID is
	 * frozen.
	 * 
	 * @param {String}
	 *            ctrlID
	 * @return {Boolean} whether the table rendered by the control with the
	 *         given ID is frozen.
	 */
	isFrozenTable: function(ctrlID) {
		if (document.getElementById(ctrlID + "_headerFix") != null 
		      || document.getElementById(ctrlID + "_headerFlex") != null) {
			return true;
		} else {
			return false;
		}
	},

	openFilterDialog: function(filterButton, ctrlID, columnIndex) {
		return services.ajax.execute("dispatchControlCommand", {
					controlCommand: "openTableFilterDialog",
					controlID: ctrlID,
					column: columnIndex,
					sortFilterButtonID: filterButton
				 });
	},

	openSortDialog: function(sortButton, ctrlID, columnIndex) {
		return services.ajax.execute("dispatchControlCommand", {
					controlCommand: "openTableSortDialog",
					controlID: ctrlID,
					column: columnIndex,
					sortFilterButtonID: sortButton
				});
	},

	resetElement: function(element) {
		// see Ticket #644:
		element.onmousedown = null;
		element.className = "";
	},

	handleColumnWidthAdjustment: function(currentMousePosition, colWidthAdj, actionType) {

		// Adjust column border line position
		if (actionType == "adjust") {
			var horizontalScreenPosition = 0;
			if (colWidthAdj.tablePart != "fixed") {
				currentMousePosition.x = Math.max(currentMousePosition.x, colWidthAdj.minMouseX);
				horizontalScreenPosition = currentMousePosition.x
						- colWidthAdj.tableStructure.headerFlex.scrollLeft
						+ colWidthAdj.tableStructure.headerWidth
						+ colWidthAdj.tableStructure.tableSeparatorWidth;
			} else {
				currentMousePosition.x = Math.min(currentMousePosition.x, colWidthAdj.rawMaximumMousePositionX);
				horizontalScreenPosition = Math.max(currentMousePosition.x, colWidthAdj.minMouseX);
			}

			BAL.setElementX(colWidthAdj.columnBorderElement, horizontalScreenPosition);
		}

		// Scroll flex table to right or left
		else if (colWidthAdj.tablePart != "fixed") {

			var scrollStepWidth = 5/* px */;

			// Scroll right
			if (actionType == "scrollRight") {
				colWidthAdj.horizontalScrollBar.parentNode.scrollLeft += scrollStepWidth;
			}

			// Scroll left
			else if (currentMousePosition.x > colWidthAdj.minMouseX) {
				colWidthAdj.horizontalScrollBar.parentNode.scrollLeft -= scrollStepWidth;
			}
		}

		return false;
	},

	setHeaderColumnWidth: function(tableID, numberHeaderRows, columnNumber, newColumnWidth) {
		var columnDefinitionId = tableID + "_basicColumnDefinition_" + columnNumber;
		var columnDefinition = document.getElementById(columnDefinitionId);
		TABLE.setColumnWidth(columnDefinition, newColumnWidth);
		for (var i = 0; i < numberHeaderRows; i++) {
			var columnHeaderId = tableID + "_header_" + i + "_" + columnNumber;
			var div = document.getElementById(columnHeaderId);
			TABLE.setColumnWidth(div, newColumnWidth);
			// Quirks, to force IE6 and IE7 to set the new column width directly at th-tag,
			// due to these IE versions do not perform column adjustment according to the
			// width of child elements
			TABLE.setColumnCellWidth(div.parentNode, newColumnWidth);
		}
	},
	
		manager: null,
	
	/**
	 * Returns the tables top spacer row.
	 *
	 * The top spacer row has no content, it has only a height equal to the size of all rows that are theoretical above it,
	 * even if they are not rendered or displayed.
	 *
	 * This is necessary to make the scrollbar large enough and the user think that all rows are already loaded, 
	 * although this is not the case.
	 *
	 * Loading all rows directly leads to performance problems with large tables, therefore only a small number of rows is rendered and 
	 * if necessary (e.g. when the user scrolls) rows are reloaded.
	 *
	 * @param {tableContainer} Container holding the table.
	 */
	getTopSpacerRow: function(tableContainer) {
		return tableContainer.querySelector("table tr#" + tableContainer.id + "_topSpacer");
	},
	
	/**
	 * Returns the tables top spacer row.
	 *
	 * The bottom spacer row has no content, it has only a height equal to the size of all rows that are theoretical below it,
	 * even if they are not rendered or displayed.
	 *
	 * This is necessary to make the scrollbar large enough and the user think that all rows are already loaded, 
	 * although this is not the case.
	 *
	 * Loading all rows directly leads to performance problems with large tables, therefore only a small number of rows is rendered and 
	 * if necessary (e.g. when the user scrolls) rows are reloaded.
	 *
	 * @param {tableContainer} Container holding the table.
	 */
	getBottomSpacerRow: function(tableContainer) {
		return tableContainer.querySelector("table tr#" + tableContainer.id + "_bottomSpacer");
	},
	
	/**
	 * Returns the row height of the tables body on the client in pixels.
	 *
	 * If no rows are rendered then 0 is returned.
	 *
	 * @param {tableContainer} Container holding the table.
	 */
	getTableBodyRowHeight: function(tableContainer) {
		var table =  tableContainer.querySelector("table");
		var tableRow = table.querySelector(":scope > tbody tr:not(:empty)");
		
		if(tableRow) {
			return TABLE.getHeight(tableRow);
		} else {
			return 0.;
		}		
	},
	
	/**
	 * Returns the number of rows that should be rendered.
	 *
	 * This number should at least as large as the number of rows that fit in the users tables viewport (if the table has enough rows)
	 * so that no free space is displayed and unused.
	 *
	 * @param{ctrlID} Table control identifier.
	 * @param {tableInformer} Holds the tables metadata.
	 */
	getNumberOfRowsToRender: function(ctrlID, tableInformer) {
		return Math.ceil(Math.max(50, TABLE.getNumberOfRowsFitInViewport(ctrlID, tableInformer) * 1.5));
	},
	
	/**
	 * Return the number of rows to add when the table is scrolled. 
	 *
	 * It is only relevant when an incremental row update is executed. The set of rows that are reloaded is bounded by this number. 
 	 *
	 * @param{ctrlID} Table control identifier.
	 * @param {tableInformer} Holds the tables metadata.
	 */
	getNumberOfRowsToAddWhenScrolling: function(ctrlID, tableInformer) {
		return Math.ceil(TABLE.getNumberOfRowsToRender(ctrlID, tableInformer) * 1/4);
	},
	
	/**
	 * Returns the number of rows that can fit in the tables viewport.
	 *
	 * @param{ctrlID} Table control identifier.
	 * @param {tableInformer} Holds the tables metadata.
	 */
	getNumberOfRowsFitInViewport: function(ctrlID, tableInformer) {
	 	return Math.ceil(TABLE.getTableBodyHeight(ctrlID) / tableInformer.rowHeight)
	},
	
	/**
	 *	Saves the holder of the tables metadata into the tables manager.
	 *
	 * @param {ctrlID} Table control identifier.
 	 * @param {tableInformer} Holds the tables metadata (line height, rendered rows, how many rows can be rendered 
	 * on the current table page if pagination is enabled, etc.).
	 */
	saveTableInformer: function(ctrlID, tableInformer) {
		if (TABLE.manager == null) {
			TABLE.manager = new Object();
		}
			
		TABLE.manager[ctrlID] = tableInformer;
	},
	
	/**
	 * Extends the holder of the tables metadata with data that are dependant of the client: 
	 * 
	 * - height of the row
	 * - tables top scroll position
	 * - placeholder template which is displayed when more rows are loaded
	 * - the range of row indices to render (could override the initial range because 
	 * additional rows can be reloaded to fill the empty tables viewport space
	 *
 	 * @param {tableContainer} Container holding the table.
	 * @param {tableInformer} Holds the tables metadata.
	 */
	extendsTableInformerWithClientMetadata: function(tableContainer, tableInformer) {
		var rowHeight = TABLE.getTableBodyRowHeight(tableContainer);
		
		Object.assign(tableInformer, { 
			scrollTop: 0,
			rowHeight: rowHeight,
			rowPlaceholderTemplate: TABLE.createTableRowPlaceholderTemplate(tableContainer, tableInformer),
			scrollPosition: {
				scrollTop: 0,
				scrollLeft: 0
			},
			rowsToRemove: new Set()
		});
		
		return tableInformer;
	},
	
	/**
	 * Creates a template for a table row placeholder.
	 * 
	 * It is a glowing row placeholder which is used to show the user that this row is beeing processed
	 * and replaced later by the server.
	 * 
	 * The template is only a node, which will be cloned afterwards, if a new placeholder is needed.
	 *
	 * @param {tableContainer} Container holding the table.
	 * @param {tableInformer} Holds the tables metadata.
	 */
	createTableRowPlaceholderTemplate: function(tableContainer, tableInformer) {
		var row = TABLE.createElement('tr', 'tl-table__row', 'tl-table__row--placeholder');
		var cellPlaceholder = TABLE.createTableCellPlaceholder();
		var leftOffsetByColumn = TABLE.getLeftOffsetByColumn(tableContainer);
		
		for(var i = 0; i < tableInformer.numberOfColumns; i++) {
			var cell = row.appendChild(cellPlaceholder.cloneNode(true));
			
			if(i <= tableInformer.numberOfFixedColumns) {
				TABLE.changeElementToFixed(cell);
				TABLE.setLeft(cell, leftOffsetByColumn.get(i));
				
				if(i == tableInformer.numberOfFixedColumns) {
					cell.classList.add('tl-table__fix-flex-separator');
				}
			}
		}
		
		return row;
	},
	
	/**
	 * Creates a glowing table cell placeholder to indicate the user that this cell is
	 * beeing processed and replaced later.
	 */
	createTableCellPlaceholder: function() {
		var cell = TABLE.createElement('td', 'tl-table__cell', 'tl-table__cell--placeholder');
		var placeholder = TABLE.createElement('div', 'tl-placeholder', 'tl-placeholder--glow');
		
		cell.appendChild(placeholder);
		
		return cell;
	},
	
	/**
	 * Initialize the table.
	 *
	 * Loads additional rows to fill the remaining empty space in viewport and set the tables scroll position.
	 * 
	 * @param {ctrlID} Table control identifier.
	 * @param {tableInformer} Holds the tables metadata (line height, rendered rows, how many rows can be rendered 
	 * on the current table page if pagination is enabled, etc.).
	 * @param{clientDisplayData} Holding data on the current display state (among others the scroll position).
	 *
	 * @see {@link renderRows}
	 * @see {@link updateScrollPosition}
	 */
	initTable: function(ctrlID, tableInformer, clientDisplayData) {
		var tableContainer = document.getElementById(ctrlID);
		var tableInformer = TABLE.extendsTableInformerWithClientMetadata(tableContainer, tableInformer);
		
		TABLE.saveTableInformer(ctrlID, tableInformer);	
		
		var oldRangeOfRows = tableInformer.rangeOfRenderedRowIndices;
		var newRangeOfRows = TABLE.getRangeOfRowsToRender(ctrlID, tableInformer, clientDisplayData);
		
		TABLE.renderRows(tableContainer, tableInformer, oldRangeOfRows, newRangeOfRows, () => {
			TABLE.updateScrollPosition(ctrlID, clientDisplayData);
			TABLE.addListenerToReloadRowsOnScroll(tableContainer);
			TABLE.ensureAllFixedColumnsInViewport(tableInformer, tableContainer);
			TABLE.addObserverToUpdateTableOnResize(tableContainer, tableInformer);
			TABLE.addObserverToRemoveRowsOnLoad(tableContainer, tableInformer);
		});
	},
	
	/**
	 * Ensures that all table fixed columns are displayed in the users viewport.
	 * 
	 * If the sum of the width of all fixed columns exceed the users viewport
	 * then the amount of fixed column is reduced accordingly.
	 * 
	 * @param {tableInformer} Holds the tables metadata.
	 * @param {tableContainer} Container holding the table.
	 */
	ensureAllFixedColumnsInViewport: function(tableInformer, tableContainer) {
		var columns = TABLE.getTerminalTableHeaderCells(tableContainer);
		var maxWidth = TABLE.getMaxWidthForFixedColumns(columns, tableContainer);
		var maxNumberOfFixedColumns = TABLE.getNumberOfColumnsFitInWidth(columns, maxWidth);
		
		if(maxNumberOfFixedColumns < tableInformer.numberOfFixedColumns) {
			TABLE.updateFixedColumnAmount(tableContainer.id, maxNumberOfFixedColumns);
		}
	},
	
	/**
	 * Returns all table cells of the last header row.
	 * 
	 * @param {tableContainer} Container holding the table.
	 */
	getTerminalTableHeaderCells: function(tableContainer) {
		return TABLE.getArrayOf(tableContainer.querySelectorAll('thead tr:last-child th'));
	},
	
	/**
	 * Returns the maximum width in pixels that the sum of all fixed columns 
	 * may have in order to be all displayed in the user's viewport.
	 * 
	 * @param {columns} Array of table columns.
	 * @param {tableContainer} Container holding the table.
	 */
	getMaxWidthForFixedColumns: function(columns, tableContainer) {
		var maxWidth = TABLE.getScrollContainer(tableContainer.id).clientWidth;
		var tableSeparator = TABLE.getTableSeparator(columns);
		
		if(tableSeparator) {
			maxWidth -= tableSeparator.offsetWidth;
		}
		
		return maxWidth;
	},
	
	/**
	 * Returns the number of table columns whose summed width is lower than the given width.
	 * 
	 * @param {columns} Array of table columns.
	 * @param {width} Width that should not be exceeded by the sum of all column width.
	 */
	getNumberOfColumnsFitInWidth: function(columns, width) {
		var numberOfColumns = 0;
		
		for(var i = 0; i < columns.length; i++) {
			var column = columns[i];
			
			width -= column.offsetWidth;
			
			if(width >= 0) {
				numberOfColumns++;
			} else {
				break;
			}
		}
		
		return numberOfColumns;
	},
	
	/**
	 * Server request to update its table number of fixed columns.
	 * 
	 * @param{ctrlID} Table containers identifier.
	 * @param {newFixedColumnAmount} New number of fixed columns.
	 */
	updateFixedColumnAmount: function(ctrlID, newFixedColumnAmount) {
		services.ajax.execute("dispatchControlCommand", {
			controlCommand: "updateFixedColumnAmount",
			controlID: ctrlID,
			fixedColumnAmount: newFixedColumnAmount
		}, false);
	},
	
	/**
	 * Updates the table when the its container has been resized.
	 * 
	 * - Load additional lines if necessary
	 * - Update the row height and the row placeholder template if the user has for instance zoomed
	 * 
	 * @param {tableContainer} Container holding the table.
	 * @param {tableInformer} Holds the tables metadata (line height, rendered rows, how many rows can be rendered 
	 * on the current table page if pagination is enabled, etc.).
	 */
	addObserverToUpdateTableOnResize: function(tableContainer, tableInformer) {
		new ResizeObserver((entries, observer) => {
			if(!TABLE.hasTableOverlay(tableContainer)) {
				if(document.body.contains(tableContainer)) {
					TABLE.ensureAllFixedColumnsInViewport(tableInformer, tableContainer);
				
					var rowHeight = TABLE.getTableBodyRowHeight(tableContainer);
					
					tableInformer.rowHeight = rowHeight;
					tableInformer.rowPlaceholderTemplate = TABLE.createTableRowPlaceholderTemplate(tableContainer, tableInformer);
					
					TABLE.debounce(TABLE.handleTableScroll, 50, tableContainer);
				} else {
					observer.disconnect();
				}
			}
		}).observe(tableContainer);
	},
	
	/**
	 * Adds an observer to the given table container that waits till placeholder
	 * of rows, that should be removed, are replaced with their content, to delete them.
	 * 
	 * If placeholder rows are deleted directly by the client instead of lazy, 
	 * the servers ajax fragment for replacing the placeholders with their content does not find 
	 * the corresponding IDs of the elements to replace them and fails.
	 * 
	 * @param {tableContainer} Container holding the table.
	 * @param {tableInformer} Holds the tables metadata.
	 */
	addObserverToRemoveRowsOnLoad: function(tableContainer, tableInformer) {
		var tbody = tableContainer.querySelector('tbody');
		
		new MutationObserver((mutations, observer) => {
			if(!TABLE.hasTableOverlay(tableContainer)) {
				if(document.body.contains(tableContainer)) {
					for(let mutation of mutations) {
						for(let node of mutation.addedNodes) {
							if(!TABLE.isTableRowPlaceholder(node) && tableInformer.rowsToRemove.delete(node.id)) {
								node.remove();
							}
						}
					}
				} else {
					observer.disconnect();
				}	
			}
		}).observe(tbody, { childList: true });
	},
	
	/**
	 * Executes the given callback when the given element is removed from DOM.
	 * 
	 * The parent of the given element may not be deleted itself.
	 * 
	 * @param {element} Element that is being observed.
	 * @param {callback} Function that is executed when the given element is removed from DOM.
	 */
	onRemove: function(element, callback) {
		var observer = new MutationObserver((mutations, observer) => {
		   var isRemoved = mutations.some(mutation => mutation.removedNodes.indexOf(element) !== -1);
		   
		   if(isRemoved) {
			  callback();
			  
			  observer.disconnect();
		   }
		});
		
		observer.observe(element.parentNode, {
		  childList: true,
		  subtree: true
		});
	},
	
	/**
	 * Adds an onscroll event listener to the table.
	 *
	 * - Vertical scroll: Loads additional rows and sends a request to the server that he should store 
	 * the new vertical scroll position of this table on the client.
	 * - Horizontal scroll: Sends a request to the server that he should store 
	 * the new horizontal scroll position of this table on the client.
	 *
	 * When the table displays an overlay to change the order of the columns, only the scroll position is cached. 
	 * The scroll position, if the change order action is completed, is sent to the server so that it stores it for the corresponding table.
	 *
	 * @param {tableContainer} Container holding the table.
	 *
	 * @see {@link debounce}
	 */
	addListenerToReloadRowsOnScroll: function(tableContainer) {
		TABLE.getScrollContainer(tableContainer.id).addEventListener('scroll', function(event) {
			TABLE.getTableInformer(tableContainer.id).scrollPosition = TABLE.getScrollPosition(event);
			
			if(!TABLE.hasTableOverlay(tableContainer)) {
				TABLE.debounce(TABLE.handleTableScroll, 50, tableContainer);
				TABLE.storeScrollPositionOnServer(tableContainer);
			}
		});
	},
	
	/**
	 * Returns an object containing the top and left scroll position of the event target.
	 *
	 * @param {scrollEvent} Event fired by a change of the scroll position.
	 */
	getScrollPosition: function(scrollEvent) {
		return {
			scrollTop: scrollEvent.target.scrollTop,
			scrollLeft: scrollEvent.target.scrollLeft
		};
	},
	
	/**
	 * Returns the table overlay for reordering columns of the given container.
	 * 
	 * If no table overlay exist then null is returned.
	 * 
	 * @param {tableContainer}  Container holding the table.
	 */
	getTableOverlay: function(tableContainer) {
		return tableContainer.querySelector('.tl-table-overlay');
	},
	
	/**
	 * Returns true if the given table container displays a table overlay for reordering columns.
	 *
	 * @param {tableContainer}  Container holding the table.
	 */
	hasTableOverlay: function(tableContainer) {
		return TABLE.getTableOverlay(tableContainer) != null;
	},
	
	/**
	 * Returns true if the given element is a table overlay
	 * to reorder columns.
	 * 
	 * @param {element} Element to check if it is a table overlay.
	 */
	isTableOverlay: function(element) {
		return element.classList.contains('tl-table-overlay');
	},
	
	/**
	 * Renders the given range of row indices. 
	 *
	 * The old currently rendered set of rows are no longer be displayed.
	 *
	 * Because loading of additional rows requires a call to the server and could be probably time consuming, 
	 * placeholders (gray glowing box) are displayed for the table rows to be loaded to inform the user that content is 
	 * still being processed and could not yet be loaded.
	 *
	 * @param {tableContainer} Container holding the table.
	 * @param {tableInformer} Holds the tables metadata.
	 * @param{oldRangeOfRows} Range of row indidces that are rendered.
 	 * @param{newRangeOfRows} Range of row indidces that should be rendered.
	 * @param {callbackBeforeRequest} Callback executed before the rendering request for further rows has sent to the server.
	 */
	renderRows: function(tableContainer, tableInformer, oldRangeOfRows, newRangeOfRows, callbackBeforeRequest) {
		var rangeTransformations = TABLE.getAddAndRemoveRangeTransformations(oldRangeOfRows, newRangeOfRows);
		var rowsHasChanged = rangeTransformations.addRanges.length > 0 || rangeTransformations.removeRanges.length > 0;
		
		if(rowsHasChanged) {
			tableInformer.rangeOfRenderedRowIndices = newRangeOfRows;
			
			TABLE.replaceRenderedRangeOfRows(tableContainer, tableInformer, rangeTransformations);
			
			callbackBeforeRequest();
			
			TABLE.requestRowsToUpdate(tableContainer, rangeTransformations.addRanges, rangeTransformations.removeRanges, () => {});
		}
	},
	
	/**
	 * Replaces the tables body content.
	 *
	 * Adjusts the tables body space row heights w. r. t. the new set of rendered rows.
	 *
	 * Rows that are no longer needed are removed and placeholders for the new rows are added. These placeholder will be 
	 * replaced later by the server. All remaining rendered old rows are reused.
	 *
 	 * @param{tableContainer} Container holds the table.
 	 * @param{tableInformer} Holds the tables metadata.
 	 * @param{rangeTransformations} Contains the information which rows should be added to or removed from the current table.
	 *
	 * @see {@link requestRowsToUpdate}
	 */
	replaceRenderedRangeOfRows: function(tableContainer, tableInformer, rangeTransformations) {
		TABLE.adjustTableSpacerRowHeights(tableContainer, tableInformer);
		
		for(var range of rangeTransformations.removeRanges) {
			TABLE.removeTableRows(tableContainer.id, tableInformer, range);
		}
		
		for(var range of rangeTransformations.addRanges) {
			TABLE.addTableRows(tableContainer.id, tableInformer, TABLE.getTableBody(tableContainer), range);
		}
	},
	
	/**
	 * Adjust the tables body spacer row heights.
	 * 
	 * @param{tableContainer} Container holds the table.
 	 * @param{tableInformer} Holds the tables metadata.
	 */
	adjustTableSpacerRowHeights: function(tableContainer, tableInformer) {
		TABLE.getTopSpacerRow(tableContainer)?.style.setProperty('height', TABLE.getTableTopSpacerRowHeight(tableInformer) + 'px');
		TABLE.getBottomSpacerRow(tableContainer)?.style.setProperty('height', TABLE.getTableBottomSpacerRowHeight(tableInformer) + 'px');
	},
	
	/**
	 * Returns the top spacer row height in pixels i. e. the number of rows above the rendered rows 
	 * multiplied by the row height.
	 * 
	 * @param{tableInformer} Holds the tables metadata. 
	 */
	getTableTopSpacerRowHeight: function(tableInformer) {
		var firstRowOnPage = tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex;
		var firstRenderedRowOnPage= tableInformer.rangeOfRenderedRowIndices.firstRowIndex;
		
		return (firstRenderedRowOnPage - firstRowOnPage) * tableInformer.rowHeight;
	},
	
	/**
	 * Returns the bottom spacer row height in pixels i. e. the number of rows below the rendered rows 
	 * multiplied by the row height.
	 * 
	 * @param{tableInformer} Holds the tables metadata. 
	 */
	getTableBottomSpacerRowHeight: function(tableInformer) {
		var lastRowOnPage = tableInformer.rangeOfRowIndicesFitOnPage.lastRowIndex;
		var lastRenderedRowOnPage = tableInformer.rangeOfRenderedRowIndices.lastRowIndex;
		
		return (lastRowOnPage - lastRenderedRowOnPage) * tableInformer.rowHeight;
	},
	
	/**
	 * Removes the given range of rows from the given table body.
	 * 
	 * Loading rows, i. e. rows that have not yet been rendered, are not removed, 
	 * otherwise the server executes a snippet in which it searches for row nodes with an id that
	 * not exist.
	 * 
	 * @param{ctrlID} Table containers identifier.
 	 * @param{tableInformer} Holds the tables metadata.
 	 * @param{rangeOfRows} Range of row indices that should be removed.
	 */
	removeTableRows: function(ctrlID, tableInformer, rangeOfRows) {
		var firstRow = document.getElementById(`${ctrlID}.${rangeOfRows.firstRowIndex}`);
		var lastRow = document.getElementById(`${ctrlID}.${rangeOfRows.lastRowIndex}`);
		
		TABLE.executeOnElementsBetween(firstRow, lastRow, (row) => {
			if(TABLE.isTableRowPlaceholder(row)) {
				tableInformer.rowsToRemove.add(row.id);
				
				row.classList.add('tl-hide');
			} else {
				row.remove();
			}
		});
	},
	
	/**
	 * Executes the given method on all elements between the given start and end element.
	 * The start and end element are included.
	 * 
	 * It is assumed that that the given start and end elements are siblings.
	 * 
	 * @param{start} Start element.
	 * @param{end} End element.
	 * @param{method} Function to execute on the elements inbetween.
	 */
	executeOnElementsBetween: function(start, end, method) {
		var siblings = TABLE.getArrayOf(start.parentNode.children);
		
		siblings.slice(siblings.indexOf(start), siblings.indexOf(end) + 1).forEach(element => method(element));
	},
	
	/**
	 * Adds the given range of rows to the given table body.
	 * 
	 * For this purpose, row placeholders are created, 
	 * which are later replaced by the server with their content.
	 * 
	 * @param{ctrlID} Table containers identifier.
 	 * @param{tableInformer} Holds the tables metadata.
 	 * @param{tbody} Table body node.
 	 * @param{rangeOfRows} Range of row indices that should be added.
	 */
	addTableRows: function(ctrlID, tableInformer, tbody, rangeOfRows) {
		var insertBefore = TABLE.getArrayOf(tbody.children).slice(1, -1).find(row => {
			return rangeOfRows.lastRowIndex < TABLE.getRowIndex(row.id);
		}) || tbody.lastChild;
		
		for(var i = rangeOfRows.firstRowIndex; i <= rangeOfRows.lastRowIndex; i++) {
			var row = TABLE.createTableRowPlaceholder(tableInformer, `${ctrlID}.${i}`);
			
			if(tableInformer.rowsToRemove.delete(row.id)) {
				row = document.getElementById(`${ctrlID}.${i}`);
				
				row.classList.remove('tl-hide');
			}
			
			tbody.insertBefore(row, insertBefore);
		}
	},
	
	/**
	 * Returns the row index from a row node id.
	 * 
	 * It is assumed that the id's are in the following format: <code>ctrlID.index</code>.
	 * 
	 * @param{rowID} Identifier of a row node.
	 */
	getRowIndex: function(rowID) {
		return parseInt(rowID.slice(rowID.indexOf('.') + 1));
	},
	
	/**
	 * Creates a table row placeholder node from a given template and
	 * sets its id to the given identifier and its height to the
	 * current table row height.
	 * 
	 * @param{tableInformer} Holds the tables metadata.
	 * @param{id} The table row placeholder node identifier.
	 */
	createTableRowPlaceholder: function(tableInformer, id) {
		var rowPlaceholder = tableInformer.rowPlaceholderTemplate.cloneNode(true);
	
		rowPlaceholder.id = id;
		rowPlaceholder.style.height = tableInformer.rowHeight + 'px';
		
		return rowPlaceholder;
	},
	
	/**
	 * Returns true if the given element is a table row placeholder which is replaced
	 * later by the server.
	 * 
	 * @param{element} DOM node whose checked if it is a table row placeholder.
	 */
	isTableRowPlaceholder: function(element) {
		return element.classList.contains('tl-table__row--placeholder');
	},
	
	/**
	 * Returns the intersection of the two given ranges.
	 *
	 * If they do not intersect then undefined is returned.
	 *
 	 * @param{range1} First range of row indices.
 	 * @param{range2} Second range of row indices.
	 */
	getIntersection: function(range1, range2) {
		if(range1.firstRowIndex > range2.lastRowIndex || range2.firstRowIndex > range1.lastRowIndex) {
			return undefined;
		} else {
			return {
				firstRowIndex: Math.max(range1.firstRowIndex, range2.firstRowIndex),
				lastRowIndex: Math.min(range1.lastRowIndex, range2.lastRowIndex)
			};
		}
	},
	
	/**
	 * Returns the tables tbody element.
	 *
	 * @param{tableContainer} Container holding the table.
	 */
	getTableBody: function(tableContainer) {
		return tableContainer.querySelector('#' + tableContainer.id + '_scrollContainer' + ' > table > tbody');
	},
	
	/**
	 * Return the number of rows that the range of indices represents.
	 *
	 * @param{rangeOfRowIndices} Range of row indices.
	 *
	 * @see {@link getNumberOfRowsOrNull}
	 */
	getNumberOfRows: function(rangeOfRowIndices) {
		return rangeOfRowIndices.lastRowIndex - rangeOfRowIndices.firstRowIndex + 1;
	},
	
	/**
	 * Return the number of rows that the range of indices represents. 
	 *
	 * If the given range of row indices is falsy (for instance undefined), then 0 is returned.
	 *
	 * @param{rangeOfRowIndices} Range of row indices.
	 *
	 * @see {@link getNumberOfRows}
	 */
	getNumberOfRowsOrNull: function(rangeOfRowIndices) {
		if(rangeOfRowIndices) {
			return TABLE.getNumberOfRows(rangeOfRowIndices);
		} else {
			return 0;
		}
	},
	
	/**
	 * Returns the tables top spacer row height.
	 *
	 * @param{tableInformer} Holds the tables metadata.
	 *
	 * @see {@link TABLE.getTopSpacerRow}
	 */
	getTopSpacerRowHeight: function(tableInformer) {
		return (tableInformer.rangeOfRenderedRowIndices.firstRowIndex - tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex) * tableInformer.rowHeight;
	},
	
	/**
	 * Returns the tables bottom spacer row height.
	 *
	 * @param{tableInformer} Holds the tables metadata.
	 *
	 * @see {@link TABLE.getBottomSpacerRow}
	 */
	getBottomSpacerRowHeight: function(tableInformer) {
		return (tableInformer.rangeOfRowIndicesFitOnPage.lastRowIndex - tableInformer.rangeOfRenderedRowIndices.lastRowIndex) * tableInformer.rowHeight;
	},
	
	/**
	 * Returns the range of row indices that should be rendered w. r. t. the current client display state.
	 *
	 * This range is not adjusted to the tables page offset and bounds.
	 *
	 * @param{ctrlID} Table control identifier.
	 * @param{tableInformer} Holds the tables metadata (row height, body height, header height, etc.).
	 * @param {clientDisplayData} Holding data on the current display state (among others scroll position).
	 *
	 * @see {@link adjustRangeOfRowsToBounds}
	 */
	createRangeOfRowsToRender: function(ctrlID, tableInformer, clientDisplayData) {
		var numberOfRowsToRenderOnClient = TABLE.getNumberOfRowsToRender(ctrlID, tableInformer);
		
		var requestedRangeOfRows = clientDisplayData.visiblePane.rowRange;
		var currentFirstRowIndex = clientDisplayData.viewportState.rowAnchor.index;
		
		var rowsFitInViewport = TABLE.getNumberOfRowsFitInViewport(ctrlID, tableInformer);
		
		if(TABLE.hasRowViewportRequest(clientDisplayData) && TABLE.hasRowViewportState(clientDisplayData) && currentFirstRowIndex <= requestedRangeOfRows.forcedVisibleIndexInRange && requestedRangeOfRows.forcedVisibleIndexInRange < currentFirstRowIndex + rowsFitInViewport) {
			if(requestedRangeOfRows.lastIndex < currentFirstRowIndex + numberOfRowsToRenderOnClient) {
				return TABLE.createRangeOfRows(currentFirstRowIndex, requestedRangeOfRows.lastIndex);
			} else {
				return TABLE.createRangeOfRows(currentFirstRowIndex, currentFirstRowIndex + numberOfRowsToRenderOnClient - 1);
			}
		} else if(TABLE.hasRowViewportRequest(clientDisplayData)) {
			return TABLE.createRangeOfRowsFromVisiblePaneRequest(requestedRangeOfRows, numberOfRowsToRenderOnClient);
		} else if(TABLE.hasRowViewportState(clientDisplayData)) {
			return TABLE.createRangeOfRows(currentFirstRowIndex ,TABLE.getNumberOfRowsFitInViewport(ctrlID, tableInformer));
		} else {
			return TABLE.createRangeOfRows(tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex, numberOfRowsToRenderOnClient);
		}
	},
	
	/**
	 * Returns true if the client display data, holding among others the tables scroll position, has 
	 * a valid row anchor for the request to display this row into the users viewport. 
	 *
	 * @param {clientDisplayData} Holding data on the current display state (among others scroll position).
	 */
	hasRowViewportRequest: function(clientDisplayData) {
		return clientDisplayData.visiblePane.rowRange.firstIndex > -1;
	},
	
	/**
	 * Returns true if the client display data, holding among others the tables scroll position, has 
	 * a valid row anchor for the viewport state. 
	 *
	 * @param {clientDisplayData} Holding data on the current display state (among others scroll position).
	 */
	hasRowViewportState: function(clientDisplayData) {
		return clientDisplayData.viewportState.rowAnchor.index > -1;
	},
	
	/**
	 * Returns a range of rows computed from a visible pane request.
	 *
	 * If the size of the given requested range is greater than the number of rows that should be rendered then
	 * a range around the row index that have to be visible is created.
	 *
	 * @param{requestedRangeOfRows} Range of rows that are requested to be displayed.
	 * @param{numberOfRowsToRenderOnClient} Number of rows that should be rendered.
	 */
	createRangeOfRowsFromVisiblePaneRequest: function(requestedRangeOfRows, numberOfRowsToRenderOnClient) {
		var requestFirstRowIndex = requestedRangeOfRows.firstIndex;
		var requestLastRowIndex = requestedRangeOfRows.lastIndex;
		
		if(numberOfRowsToRenderOnClient - (requestLastRowIndex - requestFirstRowIndex + 1) > 0) {
			return TABLE.createRangeOfRowIndices(requestFirstRowIndex, requestLastRowIndex);
		} else {
			return TABLE.createRangeOfRowIndices(requestedRangeOfRows.forcedVisibleIndexInRange, requestedRangeOfRows.forcedVisibleIndexInRange);
		}
	},
	
	/**
	 * Returns a range of rows beginning by the given row index and of the given size.
	 *
	 * @param{firstRowIndex} Index of the first row.
	 * @param{size} Size of the range.
	 */
	createRangeOfRows: function(firstRowIndex, size) {
		return TABLE.createRangeOfRowIndices(firstRowIndex, firstRowIndex + (size - 1));
	},
	
	/**
	 * Returns a json object representing a range of row indices.
	 *
	 * @param{firstRowIndex} Index of the first row.
	 * @param{lastRowIndex} Index of the last row.
	 */
	createRangeOfRowIndices: function(firstRowIndex, lastRowIndex) {
		return {
			firstRowIndex: firstRowIndex,
			lastRowIndex: lastRowIndex
		};
	},
	
	/**
	 * Returns the number of elements the given range covers.
	 *
	 * @param{rangeOfRowIndices} Range of indices.
	 */
	getSizeOfRange: function(range) {
		return range.lastRowIndex - range.firstRowIndex + 1;
	},
	
	/**
	 * Returns true if the given range of row indices is valid
	 * i. e. the row indices has to be greater or equals than 0.
	 * 
	 * @param{rangeOfRowIndices} Range of row indices.
	 */
	isValidRangeOfRowIndices: function(rangeOfRowIndices) {
		return rangeOfRowIndices.firstRowIndex >= 0 && rangeOfRowIndices.lastRowIndex >= 0;
	},
	
	/**
	 * Returns a range of row indices that fit in (i. e. is a part of) the given range of row indices.
	 *
	 * @param{rangeOfRows} Range of row indices.
	 * @param{rangeBounds} Range of row indices that represents bounds which should not be exceeded.
	 */
	adjustRangeOfRowsToBounds: function(rangeOfRows, rangeBounds) {
		var numberOfTopOverflowedRows = rangeBounds.firstRowIndex - rangeOfRows.firstRowIndex;
		
		if(numberOfTopOverflowedRows > 0) {
			rangeOfRows.firstRowIndex += numberOfTopOverflowedRows;
			rangeOfRows.lastRowIndex += numberOfTopOverflowedRows;
		}
		
		var numberOfBottomOverflowedRows = rangeOfRows.lastRowIndex - rangeBounds.lastRowIndex;
		
		if(numberOfBottomOverflowedRows > 0) {
			rangeOfRows.firstRowIndex = Math.max(rangeBounds.firstRowIndex, rangeOfRows.firstRowIndex - numberOfBottomOverflowedRows);
			rangeOfRows.lastRowIndex -= numberOfBottomOverflowedRows;
		}
		
		return rangeOfRows;
	},
	
	/**
	 * Computes out of the client display data the range of row indices that should be rendered.
	 * If no client display state is given then the rows are determined that fit into the
	 * users viewport.
	 *
	 * A returned range of row indices from -1 to -1 means that no rows should be rendered.
	 *
	 * @param {ctrlID} Table control identifier.
	 * @param{tableInformer} Holds the tables metadata.
	 * @param {clientDisplayData} Holding data on the current display state.
	 */
	getRangeOfRowsToRender: function(ctrlID, tableInformer, clientDisplayData) {
		if(TABLE.isEmptyTablePage(tableInformer)) {
			return TABLE.createRangeOfRowIndices(-1, -1);				
		} else {
			var rangeOfRowsToRender = {};
			
			if(clientDisplayData) {
				rangeOfRowsToRender = TABLE.createRangeOfRowsToRender(ctrlID, tableInformer, clientDisplayData);
			} else {
				rangeOfRowsToRender = TABLE.getRangeOfRowsInViewport(ctrlID, tableInformer, tableInformer.scrollPosition.scrollTop);
			}
			
			rangeOfRowsToRender = TABLE.extendEvenlyRangeOfRowsToSize(tableInformer, rangeOfRowsToRender, TABLE.getNumberOfRowsToRender(ctrlID, tableInformer));
			
			if(rangeOfRowsToRender.firstRowIndex != tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex) {
				TABLE.ensureEvenFirstRowIndexInRange(rangeOfRowsToRender);
			}
			
			return rangeOfRowsToRender;
		}
	},
	
	/**
	 * Returns true if the current table page is empty (contains no rows).
	 *
	 * @param{tableInformer} Holds the tables metadata.
	 */
	isEmptyTablePage: function(tableInformer) {
		return tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex < 0;
	},
	
	/**
	 * Extends the given range of rows to the given size.
	 *
	 * The range of rows is extended evenly from above and below with respect to the lower and upper bounds of the tables page.
	 *
	 * @param{tableInformer} Holds the tables metadata.
	 * @param{rangeOfRows} Range of row indices.
	 * @param{size} Size to which the range of rows should be extended.
	 */
	extendEvenlyRangeOfRowsToSize: function(tableInformer, rangeOfRows, size) {
		var extendedRangeOfRows = rangeOfRows;
	
		extendedRangeOfRows = TABLE._extendEvenlyRangeOfRowsToSize(extendedRangeOfRows, size);
		extendedRangeOfRows = TABLE.adjustRangeOfRowsToBounds(extendedRangeOfRows, tableInformer.rangeOfRowIndicesFitOnPage);
		
		return extendedRangeOfRows;
	},
	
	/**
	 * Extends the given range of rows to the given size.
	 *
	 * The range of rows is extended evenly from above and below. Attention, it does not respect the lower and upper bound of the tables page.
	 *
	 * @param{rangeOfRows} Range of row indices.
	 * @param{size} Size to which the range of rows should be extended.
	 */
	_extendEvenlyRangeOfRowsToSize: function(rangeOfRows, size) {
		var currentRangeSize = rangeOfRows.lastRowIndex - rangeOfRows.firstRowIndex + 1;
		var numberOfRowsToAdd = size - currentRangeSize;
		
		var numberOfRowsAddingToTop = Math.floor(numberOfRowsToAdd / 2); 
		var numberOfRowsAddingToBottom = numberOfRowsToAdd - numberOfRowsAddingToTop; 
		
		return {
			firstRowIndex: rangeOfRows.firstRowIndex - numberOfRowsAddingToTop,
			lastRowIndex: rangeOfRows.lastRowIndex + numberOfRowsAddingToBottom
		}
	},
	
	/**
	 * Returns the height of the tables body (tbody) in pixels. This height equals to the table height minus the header height.
	 *
 	 * @param {tableContainer} ID of the container holding the table.
	 */
	getTableBodyHeight: function(tableContainerID) {
		var scrollContainer = TABLE.getScrollContainer(tableContainerID);

		return scrollContainer.clientHeight - TABLE.getTableHeaderHeight(scrollContainer);
	},
	
	/**
	 * Returns the height of the tables header on the client in pixels.
	 *
	 * @param {tableContainer} Container holding the table.
	 */
	getTableHeaderHeight: function(tableContainer) {
		var table =  tableContainer.querySelector("table");
		var tableHeader = table.querySelector(":scope > thead");
		
		return TABLE.getHeight(tableHeader);
	},
	
	/**
	 * Updates the vertical and horizontal scroll position of the the scroll container.
	 *
	 * @param{ctrlID} Table control identifier.
	 * @param{clientDisplayData} Holding data on the current display state.
	 *
	 * @see {@link getScrollContainer}
	 */
	updateScrollPosition: function(ctrlID, clientDisplayData) {
		if(clientDisplayData != undefined || clientDisplayData != null) {
			var scrollContainer = TABLE.getScrollContainer(ctrlID);
			
			TABLE.setVerticalScrollPosition(ctrlID, clientDisplayData, scrollContainer);
			TABLE.setHorizontalScrollPosition(clientDisplayData.viewportState.columnAnchor, scrollContainer);
			
			TABLE.getTableInformer(ctrlID).scrollPosition = {
				scrollTop: scrollContainer.scrollTop,
				scrollLeft: scrollContainer.scrollLeft
			};
		} 
	},
	
	/**
	 * Sets the vertical scroll position of the given scroll container.
	 *
	 * If a pane request exists, then the row index that is forced to be displayed is centered in the tables viewport,
	 * otherwise the current row index is scrolled to the top of the tables viewport.
	 *
	 * If the table has no pane request or viewport state, then the container holds his initial scroll position.
 	 *
	 * @param{ctrlID} Table control identifier.
	 * @param{clientDisplayData} Holding data on the current display state.
	 * @param{scrollContainer} Scrollable container element.
	 */
	setVerticalScrollPosition: function(ctrlID, clientDisplayData, scrollContainer) {
		var tableInformer = TABLE.getTableInformer(ctrlID);
		var firstRowIndexOnPage = tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex;
		var rowsFitInViewport = TABLE.getNumberOfRowsFitInViewport(ctrlID, tableInformer);
		
		var clientViewportRows = TABLE.getRangeOfRowsInViewport(ctrlID, tableInformer, scrollContainer.scrollTop);
		var serverViewportRows = TABLE.createRangeOfRows(clientDisplayData.viewportState.rowAnchor.index - firstRowIndexOnPage, rowsFitInViewport); 
		var requestedRows = TABLE.createRangeOfRowIndices(clientDisplayData.visiblePane.rowRange.firstIndex, clientDisplayData.visiblePane.rowRange.lastIndex);
		var forcedRowIndex = clientDisplayData.visiblePane.rowRange.forcedVisibleIndexInRange - firstRowIndexOnPage;
		
		if(TABLE.hasRowViewportRequest(clientDisplayData)) {
			if(TABLE.hasRowViewportState(clientDisplayData) && TABLE.isRowIndexInsideRange(forcedRowIndex, serverViewportRows)) {
				if(TABLE.containsRange(serverViewportRows, requestedRows)) {
					scrollContainer.scrollTop = serverViewportRows.firstRowIndex * tableInformer.rowHeight + clientDisplayData.viewportState.rowAnchor.indexPixelOffset;
				} else {
					if(TABLE.getNumberOfRows(requestedRows) > TABLE.getNumberOfRows(serverViewportRows)) {
						scrollContainer.scrollTop = TABLE.getScrollTopToCenterRow(ctrlID, tableInformer, serverViewportRows, forcedRowIndex);
					} else {
						if(serverViewportRows.firstRowIndex - requestedRows.firstRowIndex > 0) {
							scrollContainer.scrollTop = requestedRows.firstRowIndex * tableInformer.rowHeight;
						} else {
							scrollContainer.scrollTop = (requestedRows.lastRowIndex - (rowsFitInViewport - 1)) * tableInformer.rowHeight + (tableInformer.rowHeight - TABLE.getTableBodyHeight(ctrlID) % tableInformer.rowHeight);
						}
					}
				}
			} else if(!TABLE.isRowIndexInsideRange(forcedRowIndex, clientViewportRows)) {
				scrollContainer.scrollTop = TABLE.getScrollTopToCenterRow(ctrlID, tableInformer, clientViewportRows, forcedRowIndex);
			}
		} else if(TABLE.hasRowViewportState(clientDisplayData) && !TABLE.isRowIndexInsideRange(serverViewportRows.firstRowIndex, clientViewportRows)) {
			scrollContainer.scrollTop = serverViewportRows.firstRowIndex * tableInformer.rowHeight + clientDisplayData.viewportState.rowAnchor.indexPixelOffset;
		}
	},
	
	/**
	 * Returns the scroll top position in pixels to center the row of the given index in the users table viewport.
	 *
	 * @param{ctrlID} Table control identifier.
	 * @param{tableInformer} Holds the tables metadata.
	 * @param{rangeOfRowsInViewport} Range of row indices that fit in the users current viewport.
	 * @param{rowIndexToCenter} Index of the row to center in the users viewport.
	 */
	getScrollTopToCenterRow: function(ctrlID, tableInformer, rangeOfRowsInViewport, rowIndexToCenter) {
		var firstRowOnPageIndex = tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex;
		var numberOfRowsInViewport = TABLE.getNumberOfRows(rangeOfRowsInViewport);
		var newFirstRowIndexInViewport = rowIndexToCenter - Math.ceil((numberOfRowsInViewport - 1) / 2.0);
		var firstRowOffset = (numberOfRowsInViewport * tableInformer.rowHeight - TABLE.getTableBodyHeight(ctrlID)) / 2.0;
		
		if(newFirstRowIndexInViewport >= firstRowOnPageIndex) {
			return (newFirstRowIndexInViewport + 1/2) * tableInformer.rowHeight + firstRowOffset;
		} else {
			return firstRowOnPageIndex * tableInformer.rowHeight;
		}
	},
	
	/**
	 * Return true if the given row index is inside the given range of row indices, otherwise false;
	 *
	 * @param{rowIndex} Row index.
	 * @param{range} Range of row indices.
	 */
	isRowIndexInsideRange: function(rowIndex, range) {
		return range.firstRowIndex <= rowIndex && rowIndex <= range.lastRowIndex;
	},
	
	/**
	 * Returns true if the given first range contains the second given range.
	 *
 	 * @param{range1} Range of row indices.
	 * @param{range2} Range of row indices.
	 */
	containsRange: function(range1, range2) {
		return range1.firstRowIndex <= range2.firstRowIndex && range1.lastRowIndex >= range2.lastRowIndex;
	},
	
	/**
	 * Returns the range of row indices that are displayed in the tables viewport.
	 * 
	 * These indices are absolute and not relative to the page that is currently displayed.
	 *
 	 * @param {ctrlID} Table control identifier.
	 * @param{tableInformer} Holds the tables metadata.
	 * @param{scrollTop} Top scroll position.
	 */
	getRangeOfRowsInViewport: function(ctrlID, tableInformer, scrollTop) {
		var numberOfRowsInViewport = TABLE.getNumberOfRowsFitInViewport(ctrlID, tableInformer);
		
		var firstPageRelativeRowIndex = Math.floor(scrollTop / tableInformer.rowHeight);
		var firstRowIndex = tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex + firstPageRelativeRowIndex;
		
		return TABLE.createRangeOfRows(firstRowIndex, numberOfRowsInViewport);
	},
	
	/**
	 * Sets the horizontal scroll position of the given scroll container to the column specified by the given column anchor.
 	 *
	 * @param{columnAnchor} Holds column to scroll to.
	 * @param{scrollContainer} Scrollable container element.
	 */
	setHorizontalScrollPosition: function(columnAnchor, scrollContainer) {
		var tableHeaderCells = TABLE.getTerminalTableHeaderCells(scrollContainer);
		
		var columnAnchorOffsetLeft = 0;
		
		for(var i = 0; i < columnAnchor.index; i++) {
			var headerCell = tableHeaderCells[i];
			
			if(!TABLE.isFixed(headerCell)) {
				columnAnchorOffsetLeft += headerCell.offsetWidth;
			}
		}
		
		scrollContainer.scrollLeft = columnAnchorOffsetLeft + columnAnchor.indexPixelOffset;
	},
	
	/**
	 * Returns the scrollcontainer of element of the given control identifier.
	 *
	 * @param {ctrlID} Control identifier.
	 */
	getScrollContainer: function(ctrlID) {
		return document.getElementById(ctrlID + '_scrollContainer');
	},
	
	/**
	 * Returns the table informer i. e. an element that holds the metadata of a table, for the given control identifier.
	 *
	 * @param {ctrlID} Control identifier.
	 */
	getTableInformer: function(ctrlID) {
		return TABLE.manager[ctrlID];
	},
	
	/**
	 * Updates and stores the tables scroll position by a request on the server.
	 *
	 * @param {tableContainer} Container holding the table.
  	 *
  	 * @see {@link _storeScrollPositionOnServer}
	 */
	storeScrollPositionOnServer: function(tableContainer) {
		var scrollPosition = TABLE.getTableInformer(tableContainer.id).scrollPosition;
		
		TABLE._storeScrollPositionOnServer(tableContainer, scrollPosition.scrollTop, scrollPosition.scrollLeft);
	},
	
	/**
	 * Updates and stores the tables scroll position by a request on the server.
	 *
	 * @param {tableContainer} Container holding the table.
  	 * @param {scrollTop} Scroll top position.
  	 * @param {scrollLeft} Scroll left position.
  	 *
  	 * @see {@link getAllOverlayTerminalColumns}
  	 * @see {@link getRowAnchorFromScrollPosition}
  	 * @see {@link getColumnAnchorFromScrollPosition}
	 */
	_storeScrollPositionOnServer: function(tableContainer, scrollTop, scrollLeft) {
		var tableInformer = TABLE.getTableInformer(tableContainer.id);
		
		var rowAnchor = TABLE.getRowAnchorFromScrollPosition(tableInformer, scrollTop);
		var columnAnchor = TABLE.getColumnAnchorFromScrollPosition(tableContainer, scrollLeft);
		
		services.ajax.executeOrUpdateLazy(services.ajax.createLazyRequestID(), "dispatchControlCommand", Object.assign({
			controlCommand: "updateScrollPosition",
			controlID: tableContainer.id,
			displayVersion: tableInformer.displayVersion
		}, rowAnchor, columnAnchor));
	},
	
	/**
	 * Return the row anchor object out of the current vertical scroll position of the scroll container.
	 *
	 * I. e. return the index and offset (because the row could be partially scrolled away) of the first rendered 
	 * row in the tables viewport.
	 *
	 * @param{tableInformer} Holds the tables metadata.
	 * @param{scrollTop} Scroll containers scroll top position.
	 */
	getRowAnchorFromScrollPosition: function(tableInformer, scrollTop) {
		var rowAnchor = tableInformer.rangeOfRowIndicesFitOnPage.firstRowIndex + Math.floor(scrollTop / tableInformer.rowHeight);
	
		return {
			rowAnchor: rowAnchor,
			rowAnchorOffset: scrollTop - (rowAnchor * tableInformer.rowHeight)
		};
	},
	
	/**
	 * Returns the column anchor object out of the current horizontal scroll position of the scroll container.
	 *
	 * I. e. return the index and offset (because the column could be partially scrolled away) of the first rendered 
	 * flexible column in the tables viewport.
	 *
	 * @param {tableContainer} Container holding the table.
  	 * @param {scrollLeft} Scroll left position.
  	 */
	getColumnAnchorFromScrollPosition: function(tableContainer, scrollLeft) {
		var columns;
	
		if(TABLE.hasTableOverlay(tableContainer)) {
			columns = TABLE.getAllOverlayTerminalColumns(TABLE.getColumnOrdererContext(tableContainer), TABLE.getTableOverlay(tableContainer));
		} else {
			columns = TABLE.getTerminalTableHeaderCells(TABLE.getScrollContainer(tableContainer.id));
		}
		
		return TABLE.getColumnAnchorFromScrollPositionInternal(columns, scrollLeft);
	},
	
	/**
	 * Returns a collection of all tables overlay terminal columns.
	 *
	 * Columns could be grouped into each other. Consequently, the overlay contains a set of "column trees". 
	 * If a column is not grouped and thus part of another column, then it is a terminal column.
	 *
	 * @param {context} Table context holding among others the dragging overlay column.
	 * @param {overlayContainer} Container holding the overlay columns.
	 */
	getAllOverlayTerminalColumns: function(context, overlayContainer) {
		return TABLE.getArrayOf(overlayContainer.querySelectorAll('.tl-table-overlay__columns-container :is(div.tl-table-overlay__column:not(.tl-table-overlay__column-group), .tl-table-overlay__column-placeholder)'))
	},
	
	/**
	 * Returns true if the given column is the placeholder column in the tables overlay (this overlay is used to reorder the tables columns), otherwise false.
	 *
	 * @param {column} Column to check if it is an overlay placeholder column.
	 */
	isOverlayPlaceholderColumn: function(column) {
		return column.classList.contains('tl-table-overlay__column-placeholder');
	},
	
	/**
	 * Returns true if the given column is a column that group other columns, otherwise false.
	 *
	 * @param {column} Column to check if it is an overlay group column.
	 */
	isOverlayGroupColumn: function(column) {
		return column.classList.contains('tl-table-overlay__column-group');
	},
	
	/**
	 * Returns all overlay terminal columns that are contained in the given overlay column.
	 *
	 * @param {overlayColumn} Column for which all terminal columns are searched.
	 */
	getOverlayTerminalColumns: function(overlayColumn) {
		var overlayTerminalColumns = TABLE.getArrayOf(overlayColumn.querySelectorAll('div.tl-table-overlay__column:not(.tl-table-overlay__column-group)'));
		
		overlayTerminalColumns.forEach(overlayTerminalColumn => overlayTerminalColumn.dataset.isFixed = overlayColumn.dataset.isFixed);
		
		return overlayTerminalColumns;
	},
	
	/**
	 * Return the column anchor object out of the current horizontal scroll position of the scroll container.
	 *
	 * I. e. return the index and offset (because the column could be partially scrolled away) of the first rendered 
	 * flexible column in the tables viewport.
	 *
	 * @param{columns} Collection of columns.
	 * @param{scrollLeft} Scroll containers scroll left position.
	 */
	getColumnAnchorFromScrollPositionInternal: function(columns, scrollLeft) {
		var columnAnchor = 0;
		var columnAnchorOffset = 0;
		
		for(var i = 0; i < columns.length; i++) {
			var column = columns[i];
			
			if(TABLE.isFlexibleColumn(column)) {
				if(columnAnchorOffset + column.offsetWidth <= scrollLeft) {
					columnAnchorOffset += column.offsetWidth;
				} else {
					columnAnchorOffset = scrollLeft - columnAnchorOffset;
					columnAnchor =  parseInt(column.dataset.firstColumnIndex);
				
					break;
				}
			}
		}
		
		return {
			columnAnchor: columnAnchor,
			columnAnchorOffset: columnAnchorOffset
		};
	},
	
	/**
	 * Returns the number of columns the given column spans over.
	 * 
	 * @param {column} Column to compute the column span from.
	 */
	getColumnSpan: function(column) {
		if(TABLE.isColumnOverlay(column)) {
			return TABLE.getColumnOverlaySpan(column);
		} else {
			return TABLE.getTableCellSpan(column);
		}
	},
	
	/**
	 * Returns the number of column overlays the given column overlay spans over.
	 * 
	 * @param {columnOverlay} Table overlay column.
	 */
	getColumnOverlaySpan: function(columnOverlay) {
		var lastColumnIndex = parseInt(columnOverlay.dataset.lastColumnIndex);
		var firstColumnIndex = parseInt(columnOverlay.dataset.firstColumnIndex);
		
		return lastColumnIndex - firstColumnIndex + 1;
	},
	
	/**
	 * Debounces the given method by the given delay.
	 *
	 * It ensures that the method is called only once during a period. I. e. 
	 * before the method is executed, a timer is set which executes the method after a certain time. 
	 * If the method is executed again during this time, the timer is reset.
	 *
	 * @param{method} Method to execute.
	 * @param{delay} Time after the method is executed if the method is not called again.
 	 * @param {tableContainer} Container holding the table.
	 * @param{args} Arguments of the method to execute.
	 */
	debounce: function(method, delay, tableContainer, args) {
		clearTimeout(method._tId);
		
	    method._tId = setTimeout(function() {
			method(tableContainer, args);
	    }, delay);
	},
	
	/**
	 * Return true if the given number is even (modulo 2 equals 0), otherwise false.
	 *
	 * @param{x} Number.
	 */
	isEven: function(x) {
	    if(x % 2 == 0) {
	        return true;
	    } else {
	        return false;
	    }
	},
	
	/**
	 * Ensures that first row index of the given range of row indices is even.
	 *
	 * @param{rangeOfRows} Range of row indices.
	 */
	 ensureEvenFirstRowIndexInRange: function(rangeOfRows) {
		if(!TABLE.isEven(rangeOfRows.firstRowIndex)) {
			rangeOfRows.firstRowIndex--;
		}
	 },
	
	/**
	 * Handles a scrolling of the table.
	 *
	 * Loads additional rows to the direction to which the user has scrolled.
	 *
	 * If only a small piece in the table is scrolled, only the necessary rows are reloaded. 
	 * For example, if the user scrolls up a small part so that 5 more rows would need to be reloaded, 
	 * then only those 5 rows are added at the top and 5 rows are removed from the bottom of the table so 
	 * that the same number of rows are always rendered on the client.
	 *
	 * But if the user uses the scrollbar and jumps from the top part of a large table to the bottom part of the table, then
	 * the set of rendered rows have to be reloaded (i. e. all old rendered rows are removed).
	 *
	 * If all rows of a large table are rendered, this can lead to severe performance problems.
	 *
	 * @param {tableContainer} Container holding the table.
	 */
	handleTableScroll: function(tableContainer) {
		var tableInformer = TABLE.getTableInformer(tableContainer.id);
		
		var oldRangeOfRows = tableInformer.rangeOfRenderedRowIndices;
		var newRangeOfRows = TABLE.getRangeOfRowsToRender(tableContainer.id, tableInformer, null);
		
		TABLE.renderRows(tableContainer, tableInformer, oldRangeOfRows, newRangeOfRows, () => {});
	},
	
	/**
	 * Return true if the given two range of row indices intersect, otherwise false.
	 *
 	 * @param{range1} First range of row indices.
	 * @param{range2} Second range of row indices.
	 */
	hasRowIntersection: function(range1, range2) {
		return (range1.lastRowIndex >= range2.firstRowIndex && range2.lastRowIndex >= range1.firstRowIndex);
	},
	
	/**
	 * Returns the add and remove range transformation steps to transform the given source range of row indices
	 * to the given target range of row indices.
	 *
  	 * @param{sourceRange} Range of row indices that should be transformed to the targetRange.
	 * @param{targetRange} Range of row indices to which the sourceRange should transformed to.
	 */
	getAddAndRemoveRangeTransformations: function(sourceRange, targetRange) {
		var addRanges = [];
		var removeRanges = [];
		
		if(TABLE.hasRowIntersection(sourceRange, targetRange)) {
			if(sourceRange.firstRowIndex < targetRange.firstRowIndex) {
				removeRanges.push(TABLE.createRangeOfRowIndices(sourceRange.firstRowIndex, targetRange.firstRowIndex - 1));
			} else if(sourceRange.firstRowIndex > targetRange.firstRowIndex) {
				addRanges.push(TABLE.createRangeOfRowIndices(targetRange.firstRowIndex, sourceRange.firstRowIndex - 1));
			}
				
			if(targetRange.lastRowIndex < sourceRange.lastRowIndex) {
				removeRanges.push(TABLE.createRangeOfRowIndices(targetRange.lastRowIndex + 1, sourceRange.lastRowIndex));
			} else if(targetRange.lastRowIndex > sourceRange.lastRowIndex) {
				addRanges.push(TABLE.createRangeOfRowIndices(sourceRange.lastRowIndex + 1, targetRange.lastRowIndex));
			}
		} else {
			addRanges.push(targetRange);
			removeRanges.push(sourceRange);
		}
		
		return {
			addRanges: addRanges,
			removeRanges: removeRanges
		}
	},
	
	/**
	 * Sends a request to the server to add e. g. remove the given rows.
	 *
	 * For rows that should be added are already placeholder created.
	 *
	 * @param{tableContainer} Container holds the table.
	 * @param{rangesOfRowsToAdd} Ranges of row indices that should be added to the current table by replacing its row placeholders.
	 * @param{rangesOfRowsToRemove} Ranges of row indices that should be removed.
 	 * @param {serverResponseCallback} Callback executed after the server has responded to the row update request.
	 *
	 * @see {@link replaceRenderedRangeOfRows}
	 */
	requestRowsToUpdate: function(tableContainer, rangesOfRowsToAdd, rangesOfRowsToRemove, serverResponseCallback) {
		commandArguments = {
			controlCommand: "updateRows",
			controlID: tableContainer.id
		};
		
		if(rangesOfRowsToAdd.length > 0) {
			commandArguments.added = TABLE.reduceRangesOfRowsForRequest(rangesOfRowsToAdd);
		}
		
		if(rangesOfRowsToRemove.length > 0) {
			commandArguments.removed = TABLE.reduceRangesOfRowsForRequest(rangesOfRowsToRemove);
		}
		
		services.ajax.execute("dispatchControlCommand",	services.ajax.addSystemCommandProperty(commandArguments), true, serverResponseCallback);
	},
	
	/**
	 * Reduces an array of ranges of row indices to use it as an argument for a server request.
	 *
	 * The set of ranges are separated by a comma and the ranges itself are enclosed by squared 
	 * brackets and separated by a dash. 
	 *
	 * For instance: [0-3],[20-30] is the output for the collection of ranges that concerns the rows 0-3 and 20-30.
	 *
	 * @param{rangesOfRows} An array of ranges of row indices.
	 */
	reduceRangesOfRowsForRequest: function(rangesOfRows) {
		var formattedRanges = '';
	
		for(var i = 0; i < rangesOfRows.length; i++) {
			if(i > 0) {
				formattedRanges += ',';
			}
			
			var rangeOfRows = rangesOfRows[i];
			
			formattedRanges += `[${rangeOfRows.firstRowIndex}-${rangeOfRows.lastRowIndex}]`;
		}
	
		return formattedRanges;
	},	
	
	/**
	 * Initializing the column resizing on mousedown.
	 *
	 * - A mousemove handler is registered to update live the column width when the user move the cursor.
	 * - A mouseup handler is registered to clean up and sending the new column width to the server.
	 *
	 * @param{mousedownEvent} Mouse down event.
	 * @param{ctrlID} Table control identifier.
	 */
	initColumnResizing: function(mousedownEvent, ctrlID) {
		var columnResizer = mousedownEvent.currentTarget;
		
		TABLE.addColumnResizingStyles(columnResizer);
		
		var columnWidthOnClientUpdater = TABLE.createColumnWidthOnClientUpdater(ctrlID, columnResizer);
		var columnWidthOnServerUpdater = TABLE.createColumnWidthOnServerUpdater(ctrlID, columnResizer, columnWidthOnClientUpdater);
		
		window.addEventListener('mousemove', columnWidthOnClientUpdater);
 		window.addEventListener('mouseup', columnWidthOnServerUpdater, { once: true });
 		
 		mousedownEvent.stopPropagation();
	},
	
	/**
	 * Returns the column resizer for the given table header cell.
	 *
	 * @param{headerCell} Table column header cell.
	 */
	getColumnResizer: function(headerCell) {
		return headerCell.querySelector('.tl-table__cell-resizer');
	},
	
	/**
	 * Append styles to indicate the user that he is resizing the table columns width.
	 *
	 * - Cursor is changed to a resizing cursor.
	 * - Column resizer and its enclosing table row obtain a CSS class to flag it as currently resized.
	 */
	addColumnResizingStyles: function(columnResizer) {
		document.body.style.cursor = 'col-resize';
		
		columnResizer.classList.add('tl-table__cell-resizer--active');
	},
	
	/**
	 * Updates the width of the column of the given column resizer and adjust the left offset of the sticky (fixed) columns
	 * right of the resized column.
	 *
 	 * @param{ctrlID} Table control identifier.
	 * @param{columnResizer} Element to resize the column width.
	 *
	 * @see {@link createFixedColumnLeftOffsetUpater}.
	 */
	createColumnWidthOnClientUpdater: function(ctrlID, columnResizer) {
		var fixedColumnLeftOffsetUpdater = TABLE.createFixedColumnLeftOffsetUpater(ctrlID, columnResizer);
	
		return (event) => {
			TABLE.updateColumnWidth(event, ctrlID, columnResizer, fixedColumnLeftOffsetUpdater);
		};
	},
	
	/**
	 * Creates a function that moves all cells in fixed columns right of that column that has been changed
	 * by the function parameter from the left.
	 *
	 * I. e. all left offsets of sticky positioned table cells right of that column that has been changed are adjusted.
	 *
	 * If for instance the 4th column has its width increased by 100px and the tables 5th and 6th
	 * columns are fixed too, then the sticky left position of the cells of those two columns 
	 * are increased by 100px too.
	 *
	 * The flexible (non sticky/fixed) cells are positioned relative, so that nothing has to be done.
	 *
 	 * @param{ctrlID} Table control identifier.
	 * @param{columnResizer} Resize element of the column whose width has been changed.
	 *
	 * @see {@link moveFromLeft}.
	 * @see {@link isFixed}.
	 */
	createFixedColumnLeftOffsetUpater: function(ctrlID, columnResizer) {
		var enclosingCell = columnResizer.parentNode;
	
		if(TABLE.isFixed(enclosingCell)) {
			var fixedCells = TABLE.getFixedCellsRightOfResizedCell(ctrlID, enclosingCell);

			return (x) => fixedCells.forEach(cell => TABLE.moveFromLeft(cell, x));
		} else {
			return (x) => {};
		}
	},
	
	/**
	 * Returns all fixed table cells right of the given cell that is being resized.
	 *
 	 * @param{ctrlID} Table control identifier.
	 * @param{resizedCell} Table cell that is being resized.
	 */
	getFixedCellsRightOfResizedCell: function(ctrlID, resizedCell) {
		var tableContainer = document.getElementById(ctrlID);
		
		var lastFixedCell = TABLE.getLastFixedElement(TABLE.getArrayOf(resizedCell.parentNode.children));
		var rows = TABLE.getArrayOf(tableContainer.querySelectorAll('tr'));
		
		return TABLE.getColumnCells(rows, TABLE.getLastColumnIndex(resizedCell) + 1, TABLE.getLastColumnIndex(lastFixedCell));
	},
	
	/**
	 * Returns the zero based index of the element in the elements parent child list.
	 *
	 * @param{element} Element to find the index in his parent children list.
	 */
	getChildIndex: function(element) {
		return TABLE.getArrayOf(element.parentNode.children).indexOf(element);
	},
	
	/**
	 * Returns the child of the given index in the parents children list.
	 *
	 * @param{parent} Node whose children are searched for.
	 * @param{index} Zero based index of the searched child.
	 */
	getChildAtIndex: function(parent, index) {
		return TABLE.getArrayOf(parent.children)[index];
	},
	
	/**
	 * Returns an array of the given HTMLCollection or NodeList.
	 *
	 * @param{elements} List of elements to transform into an array.
	 */
	getArrayOf: function(elements) {
		return [].slice.call(elements);
	},
	
	/**
	 * Creates a selector to find all fixed table cells right of a cell (column).
	 *
	 * Attention. The nth-child CSS selector use one based indices. 
	 *
	 * @param{cellIndex} One based index specifying the cells position in its enclosing table row.
	 */
	createFixedCellsRightOfCellSelector: function(cellIndex) {
		var nextOneBasedCellIndex = cellIndex + 1;
	
		var selector = '';
	
		selector += 'th.tl-position--sticky:nth-child(n + ' + nextOneBasedCellIndex + ')';
		selector += ', td.tl-position--sticky:nth-child(n + ' + nextOneBasedCellIndex + ')';
		
		return selector;
	},
	
	/**
	 * Moves the element by the given offset from the containers left border away.
	 *
	 * @param{element} Element to move.
	 * @param{x} Offset in pixels to move the element.
	 */
	moveFromLeft: function(element, x) {
		TABLE.setLeft(element, parseInt(element.style.left, 10) + x);
	},
	
	/**
	 * Sets the given positioned element by the given offset from the containers left border away.
	 *
	 * @param{element} Element to set the left position of.
	 * @param{x} Offset in pixels.
	 */
	setLeft: function(element, x) {
		element.style.left = x + 'px';
	},
	
	/**
	 * Adds the given CSS class to the existing classes of the given element.
	 * 
	 * @param{element} Element to add an CSS class to.
	 * @param{className} CSS class to add.
	 */
	addClass: function(element, className) {
		element.classList.add(className);
	},
	
	/**
	 * Returns true if the given element has the given CSS class, otherwise false.
	 * 
	 * @param{element} CSS classes of this element are checked.
	 * @param{className} CSS class.
	 */
	hasClass: function(element, className) {
		return element.classList.contains(className);
	},
	
	/**
	 * Sets the new columns width.
	 *
	 * @param{event} Mouse move event.
	 * @param{ctrlID} Table control identifier.
	 * @param{columnResizer} Table column resizer element.
	 * @param{callback} Function executed after the column is resized.
	 */
	updateColumnWidth: function(event, ctrlID, columnResizer, callback) {
		var distanceMouseToColumnResizer = BAL.relativeMouseCoordinates(BAL.getEvent(event), columnResizer);
		
		var changedColgroupColumn = TABLE.getLastColgroupColumn(document.getElementById(ctrlID), columnResizer.parentNode);
		var newColumnWidth = parseInt(changedColgroupColumn.style.width, 10) + distanceMouseToColumnResizer.x;
		
		if(newColumnWidth > 0) {
			changedColgroupColumn.style.width = newColumnWidth + 'px';
			
			callback(distanceMouseToColumnResizer.x);
		}
	},
	
	/**
	 * Returns the first tables colgroup column containing the given cell if it spans over multiple columns.
	 *
	 * @param{tableContainer} Container holds the table.
	 * @param{cell} Table column cell.
	 */
	getFirstColgroupColumn: function(tableContainer, cell) {
		return tableContainer.querySelector(TABLE.createColgroupColumnSelector(TABLE.getFirstColumnIndex(cell)));
	},
	
	/**
	 * Returns the last tables colgroup column containing the given cell if it spans over multiple columns.
	 *
	 * @param{tableContainer} Container holds the table.
	 * @param{cell} Table column cell.
	 */
	getLastColgroupColumn: function(tableContainer, cell) {
		return tableContainer.querySelector(TABLE.createColgroupColumnSelector(TABLE.getLastColumnIndex(cell)));
	},
	
	/**
	 *	Returns the CSS selector to find the column of the given index in the tables colgroups.
	 *
	 * Attention. The nth-child CSS selector use one based indices. 
	 *
	 * @param{columnIndex} Zero based column index.
	 */
	createColgroupColumnSelector: function(columnIndex) {
		return 'colgroup col:nth-child(' + (columnIndex + 1) + ')';
	},
	
	/**
	 * Returns the index of the first column of which the cell is a part.
	 * If the cell has no parent and can therefore not be found in its parent children list, then
	 * -1 is returned.
	 *
	 * The returned index is zero based.
	 *
	 * @param{cell} Table column cell.
	 */
	getFirstColumnIndex: function(cell) {
		var index = 0;
	
		for(const child of cell.parentNode.children) {
			if(child == cell) {
				return index;
			}	
			
			index += TABLE.getTableCellSpan(child);
		}
		
		return -1;
	},
	
	/**
	 * Returns the row cell that spans at least over the column specified by the given index.
	 *
	 * If no cell for the given column in this row is found, then null is returned.
 	 *
	 * @param{row} Table row to find the column cell of.
	 * @param{columnIndex} Zero based column index.
	 *
	 * @see {@link getColumnCells}.
	 */
	getEnclosingColumnCell: function(row, columnIndex) {
		var index = 0;
	
		for(const child of row.children) {
			var colSpan = TABLE.getTableCellSpan(child);
		
			if(columnIndex <= index && index <= columnIndex + (colSpan - 1)) {
				return child;
			}	
			
			index += colSpan;
		}
		
		return null;
	},
	
	/**
	 * Returns the cells of the column range specified by the given indices
	 * for all given rows.
 	 *
	 * @param{rows} Table rows to find the column cells of.
	 * @param{columnFirstIndex} Zero based index of the first column.
	 * @param{columnLastIndex} Zero based index of the last column.
	 * 
	 * @see {@link getColumnCellsInternal}
	 */
	getColumnCells: function(rows, columnFirstIndex, columnLastIndex) {
		var cells = [];

		for (var i = 0; i < rows.length; i++) {
			cells = cells.concat(TABLE.getColumnCellsInternal(rows[i].children, columnFirstIndex, columnLastIndex));
		}
		
		return cells;
	},
	
	/**
	 * Returns the cells of the column range specified by the given indices.
 	 *
	 * @param{cells} Array of cells to be sliced i. e. to compute a portion of.
	 * @param{columnFirstIndex} Zero based index of the first column.
	 * @param{columnLastIndex} Zero based index of the last column.
	 */
	getColumnCellsInternal: function(cells, columnFirstIndex, columnLastIndex) {
		var newCells = [];
	
		var index = 0;
	
		for(const cell of cells) {
			var colSpan = TABLE.getTableCellSpan(cell);
		
			if(columnFirstIndex <= index && index + (colSpan - 1) <= columnLastIndex) {
				newCells.push(cell);
			}	
			
			index += colSpan;
		}
		
		return newCells;
	},
	
	/**
	 * Returns the cell of the given column index from the given cells.
	 * 
	 * @param{cells} Array of cells to be sliced i. e. to compute a portion of.
	 * @param{columnIndex} Zero based index of searched column.
	 */
	getColumnCell: function(cells, columnIndex) {
		return TABLE.getColumnCellsInternal(cells, columnIndex, columnIndex).pop();
	},
	
	/**
	 * Returns the number of columns that cell spans over.
	 *
	 * If no colspan is set then 1 is returned as default value.
	 *
	 * @param{cell} Table column cell.
	 */
	getTableCellSpan: function(cell) {
		 return cell.colSpan ? cell.colSpan : 1;
	},
	
	/**
	 * Returns true if the given cell spanning over at least two columns, otherwise false.
	 *
	 * @param{cell} Table column cell.
	 */
	isSpanningMultipleColumns: function(cell) {
		return TABLE.getTableCellSpan(cell) > 1;
	},
	
	/**
	 * Returns the index of the last column of which the cell is a part.
 	 * If the cell has no parent and can therefore not be found in its parent children list, then
	 * -1 is returned.
	 *
	 * The returned index is zero based.
	 *
	 * @param{cell} Table column cell.
	 */
	getLastColumnIndex: function(cell) {
		var firstColumnIndex = TABLE.getFirstColumnIndex(cell);
		
		if(firstColumnIndex > -1) {
			return firstColumnIndex + TABLE.getTableCellSpan(cell) - 1;
		} else {
			return -1;
		}
	},
	
	/**
	 * Sends a request to the server to update the column width and cleanup listener and styles.
	 *
  	 * @param{ctrlID} Table control identifier.
	 * @param{columnResizer} Element to resize the column width.
	 * @param{columnWidthOnClientUpdater} Updater adjusting the columns width on the client.
	 */
	createColumnWidthOnServerUpdater: function(ctrlID, columnResizer, columnWidthOnClientUpdater) {
		return (event) => {
 			window.removeEventListener('mousemove', columnWidthOnClientUpdater);
			
 			TABLE.removeColumnResizingStyles(columnResizer);
 			
			var columnIndex = TABLE.getLastColumnIndex(columnResizer.parentNode);
 			var terminalHeaderCells = TABLE.getTerminalTableHeaderCells(document.getElementById(ctrlID));
 			
			services.ajax.execute("dispatchControlCommand", {
				controlCommand: "updateColumnWidth",
				controlID: ctrlID,
				columnID: columnIndex,
				newColumnWidth: TABLE.getColumnCell(terminalHeaderCells, columnIndex).offsetWidth
			}, false);
		};
	},
	
	/**
	 * Removes styles that indicated the user that he is resizing the table columns width.
	 *
	 * @see {@link addColumnResizingStyles}
	 */
	removeColumnResizingStyles: function(columnResizer) {
		document.body.style.cursor = 'default';
		
		columnResizer.classList.remove('tl-table__cell-resizer--active');
	},
	
	/**
	 * Initializing the column reordering.
	 *
	 * For this purpose, a fake table is created by creating a table for each column that represents that column. 
	 * Then the created tables are put into a container that visually corresponds to the appearance of the 
	 * original table. 
	 * 
	 * This container is de facto a live preview of the original table and is displayed 
	 * instead of it. This live preview offers a great advantage over using the original table. 
	 * Swapping columns respectively change the column ordering is very efficient, 
	 * because only the position of a child of the container of all tables representing original columns
	 * has to be changed.
	 *
	 * If the user does not move the mouse, this corresponds to a normal click. A normal click
	 * on the columns header cell corresponds to a sorting of the table by this column.
	 *
	 * - A mouseover handler is registered to stop its propagation in order to, among other things, 
	 * disable the creation and placement of tooltips.
	 * - A mousemove handler is registered to update the overlay (live preview) by possibly changing the column ordering.
	 * - A mouseup handler is registered to inform the server about the new sorting of the table or its column order.
	 *
	 * @param{onMouseDownEvent} Mouse down event.
	 * @param{ctrlID} Table control identifier.
	 */
	initColumnReordering: function(onMouseDownEvent, ctrlID) {
		var tableContainer = document.getElementById(ctrlID);
		
		TABLE.initColumnOrdererContext(onMouseDownEvent, tableContainer);
		TABLE.createTableOverlayForReordering(onMouseDownEvent, tableContainer);
		
		var overlayUpdater = TABLE.createColumnOverlayUpdater(tableContainer);
		
		document.addEventListener('mouseover', BAL.eventStopPropagation, true);
		document.addEventListener('mousemove', overlayUpdater);
		document.addEventListener('mouseup', TABLE.createColumnReorderingFinisher(tableContainer, overlayUpdater), { once: true });
	},
	
	/**
	 * Initializes the context used while ordering the table columns, for instance the original table 
	 * and the column index on which the user clicked.
	 *
 	 * @param{onMouseDownEvent} Mouse down event.
	 * @param{tableContainer} Container holds the table.
	 */
	initColumnOrdererContext: function(onMouseDownEvent, tableContainer) {
		var headerCell = onMouseDownEvent.currentTarget.closest('th');
		
		TABLE.addColumnOrdererContext(tableContainer, {
			originalTable: tableContainer.querySelector('table'),
			scrollContainer: TABLE.getScrollContainer(tableContainer.id),
			draggedColumnIndex: TABLE.getFirstColumnIndex(headerCell),
			headerCell: headerCell
		});
	},
	
	/**
	 * Adds the given context part to the current column orderer context.
	 *
	 * @param{tableContainer} Container holds the table.
	 * @param{context} Context object used while ordering the table columns.
	 */
	addColumnOrdererContext: function(tableContainer, context) {
		if(tableContainer.columnReordering) {
			Object.assign(tableContainer.columnReordering, context);
		} else {
			tableContainer.columnReordering = context;
		}
	},
	
	/**
	 * Creates the callback that is executed when the user finishes his column reordering operation
	 * by releasing the mouse.
	 *
	 * This finisher cleans the dom by removing remaining overlay elements and send a request
	 * to inform the server about the new sorting of the table or its column order.
	 *
 	 * @param{tableContainer} Container holds the table.
	 * @param{overlayUpdater} Updates the tables column overlay when the user moves the dragged column to reorder columns.
	 */
	createColumnReorderingFinisher: function(tableContainer, overlayUpdater) {
		return (event) => {
			TABLE.enableScrolling();
			
			var context = TABLE.getColumnOrdererContext(tableContainer);
		
			if(TABLE.hasColumnMoved(tableContainer)) {
				TABLE._storeScrollPositionOnServer(tableContainer, context.columnsContainer.scrollTop, context.columnsContainer.scrollLeft);
			
				TABLE.updateColumnOrder(tableContainer.id, TABLE.applyColumnOrderToOriginalTable(context.originalTable, context));
			} else {
				if(!TABLE.isSpanningMultipleColumns(context.headerCell)) {
					TABLE.requestSortOfTableByColumn(tableContainer, TABLE.getFirstColumnIndex(context.headerCell));
				}
			}
			
			TABLE.cleanupColumnReordering(context.columnsContainer, tableContainer);
			
			document.removeEventListener('mouseover', BAL.eventStopPropagation, true);
			document.removeEventListener('mousemove', overlayUpdater);
		};
	},
	
	/**
	 * Enables scrolling for the user.
	 */
	enableScrolling: function() {
		window.removeEventListener('keydown', TABLE.preventDefaultForScrollKeys, true);
		window.removeEventListener('wheel', TABLE.preventDefault, {
			passive: false
		});
	},
	
	/**
	 * Disables scrolling for the user.
	 */
	disableScrolling: function() {
		window.addEventListener('keydown', TABLE.preventDefaultForScrollKeys, true);
		window.addEventListener('wheel', TABLE.preventDefault, {
			passive: false
		});
	},
	
	/**
	 * Scroll keys:
	 *
	 * - 37: left arrow
	 * - 38: up arrow
	 * - 39: right arrow
	 * - 40: down arrow
	 */
	scrollKeys: {37: 1, 38: 1, 39:1, 40: 1},
	
	/**
	 * Prevent scrolling through the keyboard.
	 *
	 * @param{event} Keydown event.
	 */
	preventDefaultForScrollKeys: function(event) {
		if(TABLE.scrollKeys[event.keyCode]) {
			TABLE.preventDefault(event);
			
			return false;
		}
	},
	
	/**
	 * Prevent (default) handling and propagation of the given event.
	 *
	 * @param{event} Document event.
	 */
	preventDefault: function(event) {
		event.preventDefault();
	    event.stopPropagation();

	    return false;
	},
	
	/**
	 * Returns the column orderer context.
	 *
	 * @param{tableContainer} Container holds the table.
	 */
	getColumnOrdererContext: function(tableContainer) {
		return tableContainer.columnReordering;
	},
	
	/**
	 * Deletes the column orderer context.
	 *
	 * @param{tableContainer} Container holds the table.
	 */
	deleteColumnOrdererContext: function(tableContainer) {
		delete tableContainer.columnReordering;
	},
	
	/**
 	 * Creates a fake table by creating a table for each column that represents that column. 
	 * Then the created tables are put into a container that visually corresponds to the appearance of the 
	 * original table. This container is also called "table overlay".
	 *
  	 * @param{onMouseDownEvent} Mouse down event.
	 * @param{tableContainer} Container holds the table.
	 */
	createTableOverlayForReordering: function(onMouseDownEvent, tableContainer) {
		var context = TABLE.getColumnOrdererContext(tableContainer);
	
	    var table = context.scrollContainer;
	    
	    TABLE.createAndTransferScrollPosition(table, () => {
			var tableOverlay = table.parentNode.appendChild(TABLE.createElement('div', TABLE.getTableOverlayClassName()));
			
			context.columnsContainer = tableOverlay.appendChild(TABLE.createColumnsOverlayContainer(context));
			
		    table.style.display = 'none';
			
			context.leftScroller = tableOverlay.appendChild(TABLE.createTableOverlayLeftScroller(context.columnsContainer));
			context.rightScroller = tableOverlay.appendChild(TABLE.createTableOverlayRightScroller(context.columnsContainer));
		    
		    return context.columnsContainer;
	    });
	},
	
	/**
	 * Returns the table overlay CSS class name.
	 */
	getTableOverlayClassName: function() {
		return "tl-table-overlay";
	},
	
	/**
	 * Executes the given method while maintaining the scroll position of the given container.
	 *
	 * The method is executed with the scrollTop as first and scrollLeft as second as parameter.
	 *
	 * @param{scrollContainer} Scrollable container element.
	 * @param{method} Function to execute.
	 */
	executeButKeepScrollPosition: function(scrollContainer, method) {
	    var scrollTop = scrollContainer.scrollTop;
	    var scrollLeft = scrollContainer.scrollLeft;
	    
	    method(scrollTop, scrollLeft);
	    
	    BAL.setScrollTopElement(scrollContainer, scrollTop);
	    BAL.setScrollLeftElement(scrollContainer, scrollLeft);
	},
	
	/**
	 * Executes the given method to create a new scroll container while maintaining the scroll position of the given container by
	 * transfering it to the new created scroll container.
	 *
	 * @param{scrollContainer} Scrollable container element.
	 * @param{method} Function to create a new scroll container.
	 */
	createAndTransferScrollPosition: function(scrollContainer, createMethod) {
	    var scrollTop = scrollContainer.scrollTop;
	    var scrollLeft = scrollContainer.scrollLeft;
	    
	    var newScrollContainer = createMethod();
	    
	    BAL.setScrollTopElement(newScrollContainer, scrollTop);
	    BAL.setScrollLeftElement(newScrollContainer, scrollLeft);
	},
	
	/**
	 * Creates the columns overlay container that visually corresponds to the appearance of 
	 * the original table.
	 *
	 * This container has as many children as the original table has columns. Each child corresponds
	 * to one column of the original table.
	 *
	 * @param{context} Metadata object.
	 *
	 * @see {@link appendColumnOverlays}
	 */
	createColumnsOverlayContainer: function(context) {
	    var columnsOverlayContainer = TABLE.createElement('div', TABLE.getColumnsOverlayName());
	    
		TABLE.appendColumnOverlays(columnsOverlayContainer, context);
		
	    return columnsOverlayContainer;
	},
	
	/**
	 * Returns the CSS class name which is used by the columns overlay container.
	 */
	getColumnsOverlayName: function() {
		return 'tl-table-overlay__columns-container';
	},
	
	/**
	 * Creates an element for the given tag with the given CSS classes added.
	 *
	 * @param{tagName} Name of tag to create.
	 * @param{cssClass} Set of CSS classes to add to the created element.
	 */
	createElement: function(tagName, ...cssClasses) {
	    var element = document.createElement(tagName);
	    
	    element.classList.add(...cssClasses);
	    
	    return element;
	},
	
	/**
	 * Creates for each column of the original table a column overlay 
	 * and append it to the given element.
	 *
	 * Each column overlay consists of a table element that visually corresponds to the appearance of 
	 * one of the original table columns.
	 *
	 * @param{parent} Element to append the created column overlay to.
	 * @param{context} Metadata object.
	 */
	appendColumnOverlays: function(parent, context) {
		var tableHeight = context.originalTable.offsetHeight;
		var rowHeaderCellsSet = TABLE.createArrayOfChildren(context.originalTable.querySelector('thead').children);
		
		TABLE.createColumnOverlays(rowHeaderCellsSet, 0, TABLE.getNumberOfColumns(context.originalTable) - 1).forEach((columnOverlay) => {
			columnOverlay.style.height = tableHeight + 'px';
			
		   	TABLE.copyLeftStickyPosition(columnOverlay.overlayedHeaderCells[0], columnOverlay);
			
			parent.appendChild(columnOverlay)
		});
	},
	
	/**
	 * Copy the left sticky position of the given source to the given target element.
	 * 
	 * It is a noop if the source element is not left sticky positioned.
	 * 
	 * @param{source} Element to get the left sticky position from.
	 * @param{target} Element to copy the sources left sticky position to.
	 */
	copyLeftStickyPosition: function(source, target) {
   		if(TABLE.hasClass(source, 'tl-position--sticky')) {
	   		TABLE.addClass(target, 'tl-position--sticky');
	   		TABLE.setLeft(target, parseInt(source.style.left, 10));
	   	}
	},
	
	/**
	 * Creates an array of the elements children that satify the given predicate.
	 *
	 * @param{elements} For each element the children are computed and pushed to the new array.
	 */
	createArrayOfChildren: function(elements) {
		var childrenSet = [];
		
		for(var i = 0; i < elements.length; i++) {
			childrenSet.push(TABLE.getArrayOf(elements[i].children));
		}
		
		return childrenSet;
	}, 
	
	/**
	 * Creates the column overlays for the given range of columns.
	 *
	 * For each table header cell from the given cells set an overlay is created.
	 *
	 * @param{rowHeaderCellsSet} Collection of unvisited table row header cells.
	 */
	createColumnOverlays: function(rowHeaderCellsSet, firstColumnIndex, lastColumnIndex) {
		var columnOverlays = [];
		
		var currentColumnIndex = firstColumnIndex;
		
		do {
			var headerCell = rowHeaderCellsSet[0].shift();
			
			columnOverlays.push(TABLE.createColumnOverlay(rowHeaderCellsSet.slice(1), headerCell, currentColumnIndex));
			
			currentColumnIndex += TABLE.getTableCellSpan(headerCell);
		} while(currentColumnIndex <= lastColumnIndex);
		
		return columnOverlays;
	},
	
	/**
	 * Create the column overlay for the the given header cell.
	 *
	 * @param{rowHeaderCellsSet} Collection of unvisited table row header cells.
	 * @param{headerCell} The overlay is created for the enclosing column of this cell.
	 * @param{firstColumnIndex} The first column over that this created overlay spans (can group other columns).
	 */
	createColumnOverlay: function(rowHeaderCellsSet, headerCell, firstColumnIndex) {
		var columnOverlay;
	
		var colSpan = TABLE.getTableCellSpan(headerCell);
		
		if(colSpan > 1) {
			columnOverlay = TABLE.createGroupColumnOverlay(rowHeaderCellsSet, headerCell, firstColumnIndex, firstColumnIndex + colSpan - 1);
		} else {
			columnOverlay = TABLE.createSingleColumnOverlay(rowHeaderCellsSet, headerCell);
		}
		
		columnOverlay.dataset.firstColumnIndex = firstColumnIndex;
		columnOverlay.dataset.lastColumnIndex = firstColumnIndex + colSpan - 1;
		columnOverlay.dataset.isFixed = TABLE.isFixed(headerCell);
		
		return columnOverlay;
	},
	
	/**
	 * Creates a group column overlay i. e. a column in the overlay that spans over at least two other columns.
	 *
 	 * @param{rowHeaderCellsSet} Collection of unvisited table row header cells.
	 * @param{headerCell} The overlay is created for the enclosing column of this cell.
	 * @param{firstColumnIndex} The first column over that this created overlay spans.
	 * @param{firstColumnIndex} The last column over that this created overlay spans.
	 */
	createGroupColumnOverlay: function(rowHeaderCellsSet, headerCell, firstColumnIndex, lastColumnIndex) {
		var groupColumnOverlay = TABLE.createRawColumnOverlay(headerCell);
		
		groupColumnOverlay.classList.add('tl-table-overlay__column-group');
		
		groupColumnOverlay.appendChild(TABLE.createGroupColumnOverlayHeader(headerCell));
		groupColumnOverlay.appendChild(TABLE.createGroupColumnOverlayColumns(rowHeaderCellsSet, firstColumnIndex, lastColumnIndex));
		
		return groupColumnOverlay;
	},
	
	/**
	 * Creates the header of a group column.
	 *
	 * The header itself is a table that has the same styles as the original table.
	 *
	 * @param{headerCell} Header cell to clone from. The overlay is created for the enclosing column of this cell.
	 */
	createGroupColumnOverlayHeader: function(headerCell) {
		var groupColumnHeader = TABLE.createColumnOverlayTable(headerCell.closest('table'));
		
		groupColumnHeader.classList.add('tl-table-overlay__column-group-header');
		groupColumnHeader.style.top = TABLE.getChildIndex(headerCell.parentNode) * headerCell.offsetHeight + 'px';
		
		groupColumnHeader.appendChild(TABLE.createColumnOverlayHeader(headerCell));
		
		return groupColumnHeader;
	},
	
	/**
	 * Creates a table for the column overlay by cloning the enclosing
	 * table of the given header cell and expand it to the full width.
	 *
	 * @param{table} Table to create the column overlay table from.
	 */
	createColumnOverlayTable: function(table) {
		var newTable = table.cloneNode(false);
		
		newTable.style.width = '100%';
		newTable.style.margin = 0 + 'px';
		
		return newTable;
	},
	
	/**
	 * Creates a table header for the column overlay by cloning the enclosing
	 * table header of the given header cell.
	 *
	 * @param{headerCell} Header cell to clone his enclosing table header from. The overlay is created for the enclosing column of this cell.
	 */
	createColumnOverlayHeader: function(headerCell) {
		var newTableHeader = TABLE.createStickyTableHeader();
		
		newTableHeader.appendChild(TABLE.createColumnOverlayHeaderRow(headerCell));
		
		return newTableHeader;
	},
	
	/**
	 * Creates a table row for the column overlay by cloning the enclosing
	 * table row of the given header cell.
	 *
	 * @param{headerCell} Header cell to clone his enclosing table row from. The overlay is created for the enclosing column of this cell.
	 */
	createColumnOverlayHeaderRow: function(headerCell) {
		var newHeaderRow = headerCell.closest('tr').cloneNode(false);
		
		newHeaderRow.appendChild(TABLE.createColumnOverlayCell(headerCell));
		
		return newHeaderRow;
	},
	
	/**
	 * Creates from the given cell a copy by removing styles that are not necessary in a table overlay.
	 *
	 * @param{cell} Table cell to clone. The overlay is created for the enclosing column of this cell.
	 */
	createColumnOverlayCell: function(cell) {
		return cell.cloneNode(true);
	},
	
	/**
	 * Creates the column overlays that are grouped into another column overlay.
	 *
	 * Only the last column of this flex container can shrink. This column could only
	 * shrink when it is the last fixed column and have some additional styling (for instance
	 * a right thick border to indicate the user that the area left of it is fixed).
	 *
  	 * @param{rowHeaderCellsSet} Collection of unvisited table row header cells.
	 * @param{firstColumnIndex} The first column over that this created overlay spans.
	 * @param{firstColumnIndex} The last column over that this created overlay spans.
	 *
	 * @see {@link createGroupColumnOverlay}
	 * @see {@link createSingleColumnOverlay}
	 * @see {@link createColumnOverlays}
	 */
	createGroupColumnOverlayColumns: function(rowHeaderCellsSet, firstColumnIndex, lastColumnIndex) {
		var columnsContainer = TABLE.createElement('div', 'tl-table-overlay__column-group-body');
		var columnOverlays = TABLE.createColumnOverlays(rowHeaderCellsSet, firstColumnIndex, lastColumnIndex);
		
		for(var i = 0; i < columnOverlays.length; i++) {
			columnsContainer.appendChild(columnOverlays[i]);
		}
		
		return columnsContainer;
	},
	
	/**
	 * Creates a column overlay for one single column.
	 *
  	 * @param{rowHeaderCellsSet} Collection of unvisited table row header cells.
	 * @param{headerCell} The overlay is created for the enclosing column of this cell.
	 *
	 * @see {@link createGroupColumnOverlay}
	 */
	createSingleColumnOverlay: function(rowHeaderCellsSet, headerCell) {
		var columnOverlay = TABLE.createRawColumnOverlay(headerCell);
        
		if(TABLE.isTableSeparator(headerCell)) {
			columnOverlay.classList.add('tl-table__fix-flex-separator');
		} else {
	        columnOverlay.appendChild(TABLE.createSingleColumnOverlayContent(headerCell));
		}
        
        rowHeaderCellsSet.forEach(rowCells => columnOverlay.overlayedHeaderCells.push(rowCells.shift()));
		
		return columnOverlay;
	},
	
	/**
	 * Returns true if the given element is the first child of its parent.
	 *
	 * @param{element} Element to check if its the first child of his parent.
	 */
	isFirstChild: function(element) {
		return element === element.parentNode.firstElementChild;
	},
	
	/**
	 * Creates a raw overlay for a table column out of the given cell.
	 *
	 * Attention, this container has no content.
	 *
	 * @param{headerCell} An overlay for the enclosing columns of this cell is created.
	 */
	createRawColumnOverlay: function(headerCell) {
		var columnOverlay = document.createElement('div');
		
		var width = TABLE.getWidth(headerCell);
		
   		columnOverlay.style.flex = '0 0 ' + width + 'px';
   		columnOverlay.style.width = width + 'px';
   		
   		columnOverlay.classList.add('tl-table-overlay__column');
   		
   		columnOverlay.overlayedHeaderCells = [headerCell];
   		
   		return columnOverlay;
	},
	
	/**
	 * Returns the elements height in pixels by computing its bounding client rectangle.
	 * 
	 * @param{element} Element to compute its height from.
	 */
	getHeight: function(element) {
		return element.getBoundingClientRect().height;
	},
	
		/**
	 * Returns the elements width in pixels by computing its bounding client rectangle.
	 * 
	 * @param{element} Element to compute its width from.
	 */
	getWidth: function(element) {
		return element.getBoundingClientRect().width;
	},
	
	/**
	 * Creates the column overlay content.
	 *
	 * I. e. a HTML table with exactly one column is created by extracting all
	 * relevant body cells from the original table for which this overlay is created.
	 *
	 * @param{headerCell} For the enclosing column of this cell the overlay content is created.
	 */
	createSingleColumnOverlayContent: function(headerCell) {
		var originalTable = headerCell.closest('table');
		
        var columnTable = TABLE.createColumnOverlayTable(originalTable);
        var columnIndex = TABLE.getFirstColumnIndex(headerCell);
        
        TABLE.appendColumnOverlayBody(columnTable, originalTable.querySelector('tbody'), columnIndex, columnIndex);
        TABLE.appendColumnOverlayHeader(columnTable, headerCell, columnIndex, columnIndex);
        
        return columnTable;
	},
	
	/**
	 * Returns the number of table columns by counting the colgroup children.
	 *
	 * @param{table} Table to count the number of columns from.
	 */
	getNumberOfColumns: function(table) {
		return table.querySelector('colgroup').children.length;
	},
	
	/**
	 * Returns the computed style text of the given element.
	 *
	 * There exist in firefox a long open known bug: https://bugzilla.mozilla.org/show_bug.cgi?id=137687
	 *
	 * @param{element} Element to compute the styles from.
	 */
	getStyleText: function(element) {
		const styles = window.getComputedStyle(element);
		
		if (styles.cssText !== '') {
		    return styles.cssText;
		} else {
		    return Object.values(styles).reduce((css, propertyName) => `${css}${propertyName}:${styles.getPropertyValue(propertyName)};`);
		}
	},
	
	/**
	 * Creates and then appends the table header to the given column overlay element.
	 *
 	 * @param{parent} Element to append the table header to.
 	 * @param{headerCell} For the enclosing column of this cell the overlay content is created.
 	 * @param{firstColumnIndex} Index of the first column for which a copy is added to this table.
	 * @param{lastColumnIndex} Index of the last column for which a copy is added to this table.
	 */
	appendColumnOverlayHeader: function(parent, headerCell, firstColumnIndex, lastColumnIndex) {
        var newTableHeader = parent.appendChild(TABLE.createStickyTableHeader());
        
		newTableHeader.style.top = TABLE.getChildIndex(headerCell.parentNode) * headerCell.offsetHeight + 'px';
        
        var headerRow = headerCell.parentNode;
        
        while(headerRow) {
        	var newHeaderRow = newTableHeader.appendChild(headerRow.cloneNode(false));
        	
        	TABLE.getColumnCells([headerRow], firstColumnIndex, lastColumnIndex).forEach((cell) => {
        		newHeaderRow.appendChild(TABLE.createColumnOverlayCell(cell));
        	});
        
        	headerRow = headerRow.nextSibling;
        }
	},
	
	/**
	 * Creates a sticky table header to the top.
	 */
	createStickyTableHeader: function() {
    	var tableHeader = document.createElement('thead');
    	
    	tableHeader.style.position = 'sticky';
    	tableHeader.style.top = 0;
    	
    	return tableHeader;
	},
	
	/**
	 * Creates and then appends the table body with rows containing the cloned cells of the original column 
	 * specified by the given index to the given element.
	 *
 	 * @param{parent} Element to append the table body to.
 	 * @param{tbody} Body element of the original table.
	 * @param{firstColumnIndex} Index of the first column for which a copy is added to this table.
	 * @param{lastColumnIndex} Index of the last column for which a copy is added to this table.
	 */
	appendColumnOverlayBody: function(parent, tbody, firstColumnIndex, lastColumnIndex) {
        var newTableBody = parent.appendChild(tbody.cloneNode(false));
        
        newTableBody.appendChild(tbody.firstChild.cloneNode(false));
        
        TABLE.getArrayOf(tbody.children).slice(1, -1).forEach((row) =>  {
            var newRow = newTableBody.appendChild(row.cloneNode(false));
        
        	TABLE.getArrayOf(row.children).slice(firstColumnIndex, lastColumnIndex + 1).forEach((cell) => {
        		var newCell = newRow.appendChild(cell.cloneNode(true));
        	
	            newCell.style.removeProperty('position');
        	});
        });
        
        newTableBody.appendChild(tbody.lastChild.cloneNode(false));
	},
	
	/**
	 * Returns all cell of the table column specified by the given index.
	 *
	 * @param{tbody} Tabe body element.
	 * @param{targetColumnIndex} Index of column for which the cells are returned.
	 * @param{numberOfColumns} Number of table columns.
	 */
	getTableColumnCells: function(tbody, targetColumnIndex, numberOfColumns) {
        return TABLE.getArrayOf(tbody.querySelectorAll('td')).filter(function(columnCell, columnIndex) {
            return (columnIndex - targetColumnIndex) % numberOfColumns === 0;
        });
	},
	
	/**
	 * Creates a left scroller on the table overlay container placed right after the last fixed column
	 * and which is displayed when the columns exceed the tables viewport width and the user has already
	 * scrolled to the right.
	 *
	 * @param{scrollContainer} Scrollable container of the original table.
	 */
	createTableOverlayLeftScroller: function(scrollContainer) {
		var leftScroller = document.createElement('div');
		
		leftScroller.classList.add('tl-table-overlay__left-scroller');
		leftScroller.style.zIndex = 2;
		leftScroller.isScrollable = () => TABLE.isScrollableToLeft(scrollContainer);
		leftScroller.isActivatable = true;
		leftScroller.appendChild(TABLE.createElement('i', 'fas', 'fa-solid', 'fa-chevron-left', 'align-middle', 'tl-table-overlay__scroller-icon'));
		
		TABLE.positionAfterLastFixedColumn(leftScroller, scrollContainer);
		
		return leftScroller;
	},
	
	/**
	 * Position the given element after the last fixed column.
	 *
	 * @param{element} Element to position.
	 * @param{scrollContainer} Scrollable container of the original table.
	 */
	positionAfterLastFixedColumn: function(element, scrollContainer) {
		var tableOverlayContainer = scrollContainer.parentNode.querySelector('.' + TABLE.getColumnsOverlayName());
	
		element.style.position = 'absolute';
		element.style.top = 0;
		element.style.left = TABLE.computeWidthOfAllFixedColumns(tableOverlayContainer.children) + 'px';
		element.style.height = scrollContainer.clientHeight + 'px';
	},
	
	/**
	 * Creates a right scroller on the table overlay container placed before the right border of the tables viewport 
	 * and which is displayed when the columns exceed the tables viewport width.
	 *
	 * @param{scrollContainer} Scrollable container of the original table.
	 */
	createTableOverlayRightScroller: function(scrollContainer) {
		var rightScroller = document.createElement('div');
		
		rightScroller.classList.add('tl-table-overlay__right-scroller');
		rightScroller.style.zIndex = 2;
		rightScroller.isScrollable = () => TABLE.isScrollableToRight(scrollContainer);
		rightScroller.isActivatable = true;
		rightScroller.appendChild(TABLE.createElement('i', 'fas', 'fa-solid', 'fa-chevron-right', 'align-middle', 'tl-table-overlay__scroller-icon'));
		
		TABLE.positionBeforeRightViewportBorder(rightScroller, scrollContainer);
		
		return rightScroller;
	},
	
	/**
	 * Position the given element at the right viewport border.
	 *
	 * @param{element} Element to position.
	 * @param{scrollContainer} Scrollable container of the original table.
	 */
	positionBeforeRightViewportBorder: function(element, scrollContainer) {
		element.style.position = 'absolute';
		element.style.top = 0;
		element.style.right = (scrollContainer.offsetWidth - scrollContainer.clientWidth) + 'px';
		element.style.height = scrollContainer.clientHeight + 'px';
	},
	
	/**
	 * Creates an updater that adjust the table columns overlay when the user has changed the column ordering.
	 *
	 * If the user has not yet moved the column, then a placeholder is created and replaces the dragged
	 * column which itself is positioned absolute at the point where the user has moved the cursor.
	 *
	 * @param{tableContainer} Container holds the table.
	 */
	createColumnOverlayUpdater: function(tableContainer) {
	   	var context = TABLE.getColumnOrdererContext(tableContainer);
	   
		return (event) => {
		    if(!context.hasColumnMoved) {
				context.hasColumnMoved = true;
				context.draggingElement = TABLE.getDraggingElement(context);
				
				TABLE.createPlaceholderAndPositionDraggedColumn(context);
				TABLE.updateTableOverlayScrollersVisibility(context.leftScroller, context.rightScroller);
				TABLE.disableScrolling();
		    }
		    
			TABLE.moveDraggedColumnAndPlaceholder(event, tableContainer);
		}
	},
	
	/**
	 * Returns the column overlay for the table header cell the user has moved/dragged.
	 *
	 * @param{context} Holds table metadata.
	 */
	getDraggingElement: function(context) {
		return Array.from(context.columnsContainer.querySelectorAll('.tl-table-overlay__column')).find(columnOverlay => {
			return columnOverlay.overlayedHeaderCells.includes(context.headerCell);
		});
	},
	
	/**
	 * Creates a placeholder which is displayed at the position of the dragged column.
	 *
	 * The dragged column itself is positioned absolute at the point where the user has moved the cursor.
	 *
	 * @param{context} Holds table metadata.
	 */
	createPlaceholderAndPositionDraggedColumn: function(context) {
		var draggingElement = context.draggingElement;
		
	    context.placeholder = draggingElement.parentNode.insertBefore(TABLE.createPlaceholderForDraggedColumn(draggingElement), draggingElement.nextSibling);
	    context.columnsContainer.parentNode.appendChild(draggingElement);
	    
		TABLE.positionElementAtMouse(event, draggingElement);
		TABLE.applyStylesToDraggingColumnInPreview(draggingElement);
		TABLE.positionColumnOverlayContentToScrollTop(draggingElement, context.columnsContainer);
	},
	
	/**
	 * Creates a placeholder which is displayed at the position of the dragged column.
	 *
	 * @param{draggedColumn} The column which is moved by the user.
	 */
	createPlaceholderForDraggedColumn: function(draggedColumn) {
		var placeholder = document.createElement('div');
		
   		if(TABLE.isFixed(draggedColumn)) {
   			placeholder.style = draggedColumn.getAttribute('style');
   		}
   		
   		var width = TABLE.getWidth(draggedColumn);
   		
   		placeholder.style.flex = '0 0 ' + width + 'px';
        placeholder.style.width = width + 'px';
   		placeholder.style.height = TABLE.getHeight(draggedColumn) + 'px';
   		placeholder.style.removeProperty('left');
   		placeholder.className = draggedColumn.className;
   		placeholder.classList.add('tl-table-overlay__column-placeholder');
   		
		placeholder.dataset.firstColumnIndex = draggedColumn.dataset.firstColumnIndex;
		placeholder.dataset.lastColumnIndex = draggedColumn.dataset.lastColumnIndex;
   		placeholder.dataset.isFixed = draggedColumn.dataset.isFixed;
   		
   		placeholder.appendChild(TABLE.createElement('div', 'tl-table-overlay__column-placeholder-content'));
        
        return placeholder;
	},
	
	/**
	 * Position the given element at the point where the user has moved the cursor.
	 *
	 * @param{event} Mouse move event.
	 * @param{element} Element to position.
	 */
	positionElementAtMouse: function(event, element) {
		var relativeToHisParentChangedCoordinates = BAL.relativeMouseCoordinates(event, element.offsetParent);
		
		element.style.position = 'absolute';
		element.style.left = relativeToHisParentChangedCoordinates.x + 'px';
		element.style.top = relativeToHisParentChangedCoordinates.y + 'px';
	},
	
	/**
	 * Apply custom stlyes for the dragged column in the table overlay.
	 *
	 * @param{draggedColumn} Column that the user is dragging.
	 */
	applyStylesToDraggingColumnInPreview: function(draggedColumn) {
		draggedColumn.classList.add('tl-table-overlay__column--dragged');
		
		var cellHeight = draggedColumn.querySelector('tr').offsetHeight;
		
		TABLE.adjustColumnOverlayPositionOffsets(draggedColumn, cellHeight, 0);
	},
	
	/**
	 * Adjust the offsets of the absolute positioned given column overlay.
	 *
	 * @param{columnOverlay} Column overlay whose content must be positioned.
	 * @param{cellHeight} Cell height of the content table of this column.
	 * @param{stickyTop} Top offset.
	 */
	adjustColumnOverlayPositionOffsets: function(columnOverlay, cellHeight, stickyTop) {
		if(columnOverlay.children.length > 1) {
			columnOverlay.querySelector('table').style.top = stickyTop + 'px';
		
			TABLE.getColumnOverlays(columnOverlay.lastChild.children).forEach((column) => {
				TABLE.adjustColumnOverlayPositionOffsets(column, cellHeight, stickyTop + cellHeight);
			});
		} else {
			columnOverlay.querySelector('thead').style.top = stickyTop + 'px';
		}
	},
	
	/**
	 * Returns true if the given column overlay is grouped into other
	 * column overlays.
	 *
	 * @param{columnOverlay} Column in the table overlay.
	 */
	isGroupedColumnOverlay: function(columnOverlay) {
		return columnOverlay.parentNode.closest('.tl-table-overlay__column-group') !== null;
	},
	
	/**
	 * Returns the outermost column overlay that contains the given column overlay.
	 * 
	 * If the given column overlay is not nested (grouped) into other columns then
	 * this overlay is returned.
	 * 
	 * @param{columnOverlay} Column in the table overlay.
	 */
	getOutermostColumnOverlay: function(columnOverlay) {
		var currentColumnOverlay = columnOverlay;
		
		var element = columnOverlay.parentNode;
		
		while(!TABLE.isTableOverlay(element)) {
			if(TABLE.isColumnOverlay(element)) {
				currentColumnOverlay = element;
			}
			
			element = element.parentNode;
		}
		
		return currentColumnOverlay;
	},
	
	/**
	 * Returns the enclosing group column overlay if it exists, otherwise null.
	 *
	 * @param{columnOverlay} Column in the table overlay.
	 */
	getEnclosingGroupColumn: function(columnOverlay) {
		return columnOverlay.parentNode.closest('.tl-table-overlay__column-group');
	},
	
	/**
	 * Fake scrolling of the columns overlay content  by positioning the table body to the scroll position.
	 *
	 * Since the column is positioned absolutely, it is not kept synchronous with the scroll position 
	 * of the container like the other columns. 
	 *
	 * @param{columnOverlay} Column in the table overlay. 
	 * @param{scrollContainer} Scrollable container of the original table.
	 */
	positionColumnOverlayContentToScrollTop: function(columnsOverlay, scrollContainer) {
		columnsOverlay.querySelectorAll('table tbody').forEach((tableBody) => {
			tableBody.style.position = 'relative';
			tableBody.style.top = -scrollContainer.scrollTop + 'px';
		});
	},
	
	/**
	 * Returns true if the user has moved the column, otherwise false.
	 *
	 * @param{tableContainer} Container holds the table.
	 */
	hasColumnMoved: function(tableContainer) {
		return TABLE.getColumnOrdererContext(tableContainer).hasColumnMoved;
	},
	
	/**
	 * Move the dragged column to the mouse cursors position and change the placeholders position
	 * in the table overlay container to the index induced by the mouse position.
	 *
	 * If the user has moved above a scroller then the table is scroller after a timer runs out. I. e.
	 * if the user moves fast over the scroller then the table is not scrolled.
	 *
	 * @param{event} Mouse move event.
	 * @param{tableContainer} Container holds the table.
	 */
	moveDraggedColumnAndPlaceholder: function(event, tableContainer) {
		var context = TABLE.getColumnOrdererContext(tableContainer);
		
		var draggingElement = context.draggingElement;
		
		TABLE.positionElementAtMouse(BAL.getEvent(event), draggingElement);

		TABLE.startScrollOnHover(context.leftScroller, context.columnsContainer, draggingElement, () => TABLE.scrollTable(context.leftScroller, -10, context));
		TABLE.startScrollOnHover(context.rightScroller, context.columnsContainer, draggingElement, () => TABLE.scrollTable(context.rightScroller, 10, context));
		
		if(!context.columnsContainer.isScrolling) {
			TABLE.movePlaceholderColumnOverlay(draggingElement, context.placeholder, context.leftScroller);
		}
	},
	
	/**
	 * Checks if the user has moved on a scroller. If this is the case, a timer is started.
	 * If the timer runs out and the cursor is still staying on that scroller, then the table scrolling starts.
	 *
	 * If the user moves fast over the scroller, then nothing happens.
	 *
	 * @param{scroller} Preview scroller.
	 * @param{scrollContainer} Scrollable container.
	 * @param{draggingElement} Element that has been moved.
	 * @param{scrollTableFunction} Function to execute the table scroll.
	 */
	startScrollOnHover: function(scroller, scrollContainer, draggingElement, scrollTableFunction) {
		if(TABLE.isVisibleAndActivatableScroller(scroller, scrollContainer, draggingElement)) {
			scroller.isActivatable = false;
			
			setTimeout(function() {
				if(TABLE.elementsOverlap(draggingElement, scroller)) {
					scrollTableFunction();
				} else {
					scroller.isActivatable = true;
				}
			}, 500);
		}
	},
	
	/**
	 * Returns true if in the given scroll container could be scrolled at least 
	 * by a given offset to the right.
	 *
	 * @param{scrollContainer} Scrollable container.
	 */
	isScrollableToRight: function(scrollContainer) {
		return scrollContainer.scrollLeft + scrollContainer.clientWidth < scrollContainer.scrollWidth;
	},
	
	/**
	 *	Returns true if in the given scroll container the user can still scroll to the left.
	 *
	 * @param{scrollContainer} Scrollable container.
	 */
	isScrollableToLeft: function(scrollContainer) {
		return scrollContainer.scrollLeft > 0;
	},
	
	/**
	 * Use the given scroller to scroll the table overlay container.
 	 *
	 * @param{scroller} Table overlay scroller.
	 * @param{scrollChange} Number of pixel that are added to the scroll containers scroll left.
	 * @param{context} Holds table metadata.
	 */
	scrollTable: function(scroller, scrollChange, context) {
		var scrollFunction = setInterval(function() {
			if(scroller.isScrollable() && TABLE.elementsOverlap(context.draggingElement, scroller)) {
				context.columnsContainer.isScrolling = true;
				context.columnsContainer.scrollLeft += scrollChange;
				
				TABLE.updateTableOverlayScrollersVisibility(context.leftScroller, context.rightScroller);
			} else {
				TABLE.stopTableOverlayScrollingAndPlaceColumn(scroller, scrollFunction, context);
			}
		}, 10);
	},
	
	/**
	 * Stops scrolling of the table overlay container and place then the dragged column to the
	 * current scroll position.
	 *
	 * @param{scroller} Table overlay scroller.
	 * @param{scrollFunction} Interval function updating the scroll position.
	 * @param{context} Holds table metadata.
	 */
	stopTableOverlayScrollingAndPlaceColumn: function(scroller, scrollFunction, context) {
		clearInterval(scrollFunction);
		
		scroller.isActivatable = true;
		context.columnsContainer.isScrolling = false;
		
		TABLE.movePlaceholderColumnOverlay(context.draggingElement, context.placeholder, context.leftScroller);
	},
	
	/**
	 * Updates the visbility of the table overlay scrollers (left and right).
	 *
	 * It is necessary to distinguish 4 cases:
	 *
	 * - The scroll bar is on the left edge and the user scrolls to the right: Left scroller must be visible
	 * - The scroll bar is on the right edge and the user scrolls to the left: Right scroller must be visible
	 * - The scroll bar is not on the left edge, but the user scrolls to the left edge: Left scroller must be hidden
	 * - The scroll bar is not on the right edge, but the user scrolls to the right edge: Right scroller must be hidden
	 *
	 * @param{leftScroller} Scroller to scroll to the left.
	 * @param{rightScroller} Scroller to scroll to the right.
	 *
	 * @see {@link updateTableOverlayScrollersVisibilityInternal}
	 */
	updateTableOverlayScrollersVisibility: function(leftScroller, rightScroller) {
		TABLE.updateTableOverlayScrollersVisibilityInternal(leftScroller);
		TABLE.updateTableOverlayScrollersVisibilityInternal(rightScroller);
	},
	
	/**
	 * Updates the visbility of the table overlay scroller.
	 *
	 * If the scroller is visible but the user cant scroll or the scroller is hidden and the user can scroll, then
	 * the scrollers visibility is toggled.
	 *
	 * @param{scroller} Preview container scroller.
	 */
	updateTableOverlayScrollersVisibilityInternal: function(scroller) {
		if(TABLE.getStyle(scroller, 'visibility') == 'hidden') {
			if(scroller.isScrollable()) {
				scroller.style.visibility = 'visible';
			}
		} else {
			if(!scroller.isScrollable()) {
				scroller.style.visibility = 'hidden';
			}
		}
	},
	
	/**
	 * Returns the computed style property of the given element.
	 *
	 * @param{element} Element to get the computed style from.
	 * @param{property} Style property to access.
	 */
	getStyle: function(element, property) {
		return window.getComputedStyle(element).getPropertyValue(property);
	},
	
	/**
	 * Return true if scroller is visible and activatable.
	 *
	 * @param{scroller} Preview scroller.
	 * @param{scrollContainer} Scrollable container.
	 * @param{draggingElement} Element that has been moved.
	 *
	 * @see {@link isVisibleScroller}
	 * @see {@link isActivatableScroller}
	 */
	isVisibleAndActivatableScroller: function(scroller, scrollContainer, draggingElement) {
		var isActivatableScroller = TABLE.isActivatableScroller(scroller, scrollContainer, draggingElement);
		var isVisibleScroller = TABLE.isVisibleScroller(scroller);
		
		return isVisibleScroller && isActivatableScroller;
	},
	
	/**
	 * Returns true if the scroller is visible i. e. the scroll container can be scrolled to the to 
	 * the corresponding direction.
	 *
	 * @param{scroller} Table overlay scroller.
	 */
	isVisibleScroller: function(scroller) {
		return !TABLE.isHidden(scroller);
	},
	
	/**
	 * Return true if the given element has set the visbility style to hidden.
	 *
	 * @param{element} Element node.
	 */
	isHidden: function(element) {
		return window.getComputedStyle(element).visibility === "hidden"
	},
	
	/**
	 * Return true if the given scroller is activatable i. e. the scroller must not already been waiting
	 * of the timeout to begin scrolling and the given reference point have to be inside the scrollers 
	 * horizontal viewbox.
	 *
	 * @param{scroller} Preview scroller.
	 * @param{scrollContainer} Scrollable container.
	 * @param{draggingElement} Element that has been moved.
	 */
	isActivatableScroller: function(scroller, scrollContainer, draggingElement) {
		var isInsideScroller = TABLE.elementsOverlap(scroller, draggingElement);
		
		return scroller.isActivatable && isInsideScroller;
	},
	
	/**
	 * Returns true if the given point is located inside the given range (one dimensional), otherwise false.
	 *
	 * @param{point} Point to check if it is inside the given range.
	 * @param{range} One dimensional intervall to check if it contains the given point.
	 */
	isInRange: function(point, range) {
		return range.left < point && point < range.right;
	},
	
	/**
	 * Returns true if the given two elements having an overlapping viewbox.
	 *
 	 * @param{element1} First element.
 	 * @param{element2} Second element.
	 */
	elementsOverlap: function(element1, element2) {
		var elementBox1 = element1.getBoundingClientRect();
		var elementBox2 = element2.getBoundingClientRect();
		
		return elementBox1.right >= elementBox2.left && elementBox1.left <= elementBox2.right;
	},
	
	/**
	 * Sends a request to the server to update his column ordering.
	 *
	 * @param{ctrlID} Table control identifier.
	 * @param{moveContext} Holds information about the column reordering.
	 */
	updateColumnOrder: function(ctrlID, moveContext) {
		return dispatchControlCommand(Object.assign({
			controlCommand: "tableReplace",
			controlID: ctrlID
		}, moveContext));
	},
	
	/**
	 * Do a cleanup. 
	 *
	 * Removes the created table overlay container that helped to reorder columns more easily, 
	 * display the original table and removes the created context object.
	 *
	 * @param{scrollContainer} Scrollable container.
	 * @param{tableContainer} Container holds the table.
	 */
	cleanupColumnReordering: function(scrollContainer, tableContainer) {
    	TABLE.createAndTransferScrollPosition(scrollContainer, () => {
    		var context = TABLE.getColumnOrdererContext(tableContainer);
    	
		    context.scrollContainer.style.display = 'block';
			context.columnsContainer.parentNode.remove();
			
			TABLE.deleteColumnOrdererContext(tableContainer);
			
			return context.scrollContainer;
    	});
	},
	
	/**
	 * Sends a request to sort the table by the column the user clicked on.
	 *
	 * @param{tableContainer} Container holds the table.
	 * @param{columnIndex} Index of the column to sort by.
	 */
	requestSortOfTableByColumn: function(tableContainer, columnIndex) {
		return dispatchControlCommand({
			controlCommand: "tableSort",
			controlID: tableContainer.id,
			column: columnIndex
		});
	},
	
	/**
	 * Apply the column order of the table overlay container back to
	 * the original table w. r. t. a possible movement of cells 
	 * from the area of fixed columns to the area of flexible columns
	 * or vice vera.
	 *
	 * @param{originalTable} The original table.
	 * @param{context} Context object used while ordering the table columns.
	 */
	applyColumnOrderToOriginalTable: function(originalTable, context) {
	    var tableReplaceCommandArguments = TABLE.createTableReplaceCommandArguments(context);
	    
	    TABLE.reorderTableColgroup(originalTable, tableReplaceCommandArguments);
	    
	    var newLeftOffsetByColumn = TABLE.getLeftOffsetByColumn(originalTable);
	    
	    TABLE.getAllTableRowsToUpdate(originalTable, context).forEach(row => {
	        TABLE.applyColumnOrderToOriginalTableRow(row, tableReplaceCommandArguments, newLeftOffsetByColumn);
	    });
	    
		return tableReplaceCommandArguments;
	},
	
	/**
	 * Returns all table rows that should be updated.
	 *
	 * This update operation consists of moving table cells according to the user's drag operation.
	 * If the user has dragged column 5 before column 2, then the cells of column 5 of each row
	 * should be moved before the cells of column 2.
	 *
	 * Since grouped columns can be reordered only within their column grouping, 
	 * it is sufficient to update only those rows of the header that affect the columns of this column group.
	 * The cell containing the group label does not need to be updated for example.
	 *
	 * @param{originalTable} The original table.
	 * @param{context} Context object used while ordering the table columns.
	 */
	getAllTableRowsToUpdate: function(table, context) {
		var firstMovedHeaderCell = context.draggingElement.overlayedHeaderCells[0];
		var firstMovedHeaderRowIndex = TABLE.getChildIndex(firstMovedHeaderCell.parentNode);
		
	 	var rowsToUpdateSelector = 'thead tr:nth-child(n + ' + (firstMovedHeaderRowIndex + 1) +'), tbody tr';
	 	
	 	return table.querySelectorAll(rowsToUpdateSelector);
	},
	
	/**
	 * Adjusts the given table row to the new order of the table overlay.
	 *
	 * @param{row} Table row that should be adjusted according to the new column order.
	 * @param{tableReplaceArguments} Request arguments of the command to replace or reorder columns.
	 * @param{newLeftOffsetByColumn} (New) Left offsets of the table columns after reordering.
	 */
	applyColumnOrderToOriginalTableRow: function(row, tableReplaceArguments, newLeftOffsetByColumn) {
    	var cellsToMove = TABLE.getColumnCells([row], tableReplaceArguments.firstMovedColumn, tableReplaceArguments.lastMovedColumn);
    	
        if(cellsToMove.length > 0) {
	        TABLE.adjustCellsToFlixFlexMovement(cellsToMove, tableReplaceArguments);
	        
       		TABLE.insertElementsBefore(cellsToMove, TABLE.getEnclosingColumnCell(row, tableReplaceArguments.insertIndex));
       		
        	TABLE.setReorderingAffectedCellsLeftOffset(row, tableReplaceArguments, newLeftOffsetByColumn);
        }
	},
	
	
	
	/**
	 * Adjust table cells to a possible movement from the area of flexible columns
	 * to the area of fixed columns (sticky positioned) or vice vera.
	 *
	 * @param{cellsToMove} Cells of columns that the user has been dragged to a new position.
	 * @param{tableReplaceArguments} Request arguments of the command to replace or reorder columns.
	 */
	adjustCellsToFlixFlexMovement: function(cellsToMove, tableReplaceArguments) {
    	if(tableReplaceArguments.fixFlexMovement) {
        	if(tableReplaceArguments.draggedColumnIsFixed) {
        		TABLE.changeElementsToFixed(cellsToMove);
        	} else {
        		TABLE.changeElementsToFlexible(cellsToMove);
        	}
    	}
	},
	
	/**
	 * Creates the command arguments to replace the table.
	 *
	 * @param{context} Column reordering context.
	 */
	createTableReplaceCommandArguments: function(context) {
		var placeholder = context.placeholder;
		
		var tableReplaceArguments = {};
		
		tableReplaceArguments.draggedColumnIsFixed = TABLE.isTrue(placeholder.dataset.isFixed);
		tableReplaceArguments.fixFlexMovement = TABLE.isFixed(context.headerCell) != TABLE.isTrue(placeholder.dataset.isFixed);
		tableReplaceArguments.firstMovedColumn = parseInt(placeholder.dataset.firstColumnIndex);
		tableReplaceArguments.lastMovedColumn = parseInt(placeholder.dataset.lastColumnIndex);
		tableReplaceArguments.insertIndex = TABLE.getNextOverlayColumnIndex(placeholder, TABLE.getNumberOfColumns(context.originalTable));
		
		return tableReplaceArguments;
	},
	
	/**
	 * Sets the left offset of all table cells that are affected by the reordering of columns.
	 *
	 * param{row} Table row.
	 * param{tableReplaceArguments} Request arguments of the command to replace or reorder columns.
	 * param{leftOffsetByColumn} Left offsets of all columns.
	 */
	setReorderingAffectedCellsLeftOffset: function(row, tableReplaceArguments, leftOffsetByColumn) {
		var firstColumn = Math.min(tableReplaceArguments.firstMovedColumn, tableReplaceArguments.insertIndex);
		var lastColumn = Math.max(tableReplaceArguments.lastMovedColumn, tableReplaceArguments.insertIndex - 1);
		
		var currentColumnIndex = firstColumn;
		
		TABLE.getColumnCells([row], firstColumn, lastColumn).forEach(cell => {
			TABLE.setLeftOffset(cell, leftOffsetByColumn.get(currentColumnIndex));
			
			currentColumnIndex += TABLE.getTableCellSpan(cell);
		});
	},
	
	/**
	 * Sets the left offset of the given element.
	 *
	 * @param{element} Element to set the left offset on.
	 * @param{offset} Offset in pixels.
	 */
	setLeftOffset: function(element, offset) {
		if(TABLE.isFixed(element)) {
			TABLE.setLeft(element, offset);
		}
	},
	
	/**
	 * Returns a map that have for all of columns its left offset.
	 *
	 * It does not use the offsetLeft API, because elements which are set by CSS to display none have
	 * an offset of 0. Instead the colgroup of the table is used.
	 *
	 * param{table} Table to create a map with column left offsets from.
	 */
	getLeftOffsetByColumn: function(table) {
	    var currentLeftOffset = 0;
	    var columnLeftOffsets = new Map();
	    
	    var colgroupColumns = TABLE.getColgroupColumns(table);
	    
	    for(var i = 0; i < colgroupColumns.length; i++) {
	    	columnLeftOffsets.set(i, currentLeftOffset);
	    	
	    	currentLeftOffset += parseInt(colgroupColumns[i].style.width, 10);
	    }
	    
	    return columnLeftOffsets;
	},
	
	/**
	 * Returns the colgroup columns of the given table as array.
	 * 
	 * If the given table contains no colgroup definition then undefined is returned.
	 * 
	 * @param{table} Table to compute the colgroup columns of.
	 */
	getColgroupColumns: function(table) {
		var colgroup = table.querySelector('colgroup');
		
		if(colgroup) {
			return TABLE.getArrayOf(colgroup.children);
		} else {
			return undefined;
		}
	},
	
	
	/**
	 * Reorders the colgroup of the given table by moving the colgroup columns specified by
	 * the given range of columns, before the column of the given index.
	 *
	 * @param{table} Columns of the colgroup of this table is reordered.
	 * @param{tableReplaceArguments} Arguments of the tableReplace command to replace table columns.
	 */
	reorderTableColgroup: function(table, tableReplaceArguments) {
		var firstMovedColumn = tableReplaceArguments.firstMovedColumn;
		var lastMovedColumn = tableReplaceArguments.lastMovedColumn;
	
	    var colgroupColumns = TABLE.getColgroupColumns(table);
	    var movedColgroupColumns = colgroupColumns.slice(firstMovedColumn, lastMovedColumn + 1);
	    
	    TABLE.insertElementsBefore(movedColgroupColumns, colgroupColumns[tableReplaceArguments.insertIndex] || null);
	},
	
	/**
	 * Returns the index of the column whose overlay is next to the given column overlay.
	 *
	 * @param{columnOverlay} Column in the table overlay.
	 * @param{numberOfColumns} Number of terminal (group column overlays are excluded) column overlays.
	 */
	getNextOverlayColumnIndex: function(columnOverlay, numberOfColumns) {
	    var nextColumnOverlay = columnOverlay.nextSibling;
	    
	    if(nextColumnOverlay) {
		    return parseInt(nextColumnOverlay.dataset.firstColumnIndex);
	    } else {
	    	if(TABLE.isGroupedColumnOverlay(columnOverlay)) {
			    return parseInt(TABLE.getEnclosingGroupColumn(columnOverlay).dataset.lastColumnIndex) + 1;
	    	} else {
	    		return numberOfColumns;
	    	}
	    }
	},
	
	getNextOverlayColumnSibling: function(columnOverlay) {
		for(var sibling = columnOverlay.nextSibling; sibling; sibling = sibling.nextSibling) {
			if(TABLE.isTableOverlayColumn(sibling)) {
				return sibling;
			} 
		}
		
		return null;
	},
	
	isTableOverlayColumn: function(overlayElement) {
		return !overlayElement.classList.contains('tl-table__fix-flex-separator');
	},
	
	/**
	 * Returns true if the given boolean string is truthy.
	 *
	 * @param{booleanString} Boolean string to evaluate.
	 */
	isTrue: function(booleanString) {
		return booleanString?.toLowerCase() === "true";
	},
	
	/**
	 * Place the given sticky elements next each other with respect to the given offset.
	 *
	 * It is assumed that the given elements are already positioned sticky.
	 *
	 * @param{elements} Sticky elements to position next to each other.
	 * @param{offset} Left offset for element positioning.
	 */
	placeStickyElementsNextEachOther: function(elements, offset) {
		var currentOffset = offset;
	
		for(var i = 0; i < elements.length; i++) {
			var element = elements[i];
		
			TABLE.setLeftOffset(element, currentOffset);
			
			currentOffset += element.offsetWidth;
		}
	},
	
	/**
	 * Inserts the given elements in ascending index order before the given referenced element.
	 *
	 * @param{elements} Elements to insert in ascending index order before the referenced element.
	 * @param{referencedElement} Element to insert before.
	 */
	insertElementsBefore: function(elements, referencedElement) {
		elements.forEach((element) => element.parentNode.insertBefore(element, referencedElement));
	},
	
	/**
	 * Moves the placeholder column overlay to the position derived by the position
	 * the user has moved his dragged column to.
	 *
	 * @param{draggingColumn} Column overlay the user drags.
	 * @param{placeholder} Column overlay of the dragged column placeholder.
	 * @param{leftScroller} Scroller to scroll the flexible columns in the table overlay to the left.
	 */
	movePlaceholderColumnOverlay: function(draggingColumn, placeholder, leftScroller) {
		var columnOverlays = TABLE.getColumnOverlays(placeholder.parentNode.children);
		var isGroupedColumn = TABLE.isGroupedColumnOverlay(placeholder);
		
		placeholder.dataset.isFixed = TABLE.isDraggedIntoFixedArea(draggingColumn, placeholder, TABLE.getTableSeparator(columnOverlays));
		
		var insertBefore = TABLE.getDraggedColumnInsertBefore(draggingColumn, placeholder, columnOverlays);
		var insertBeforeIndex = insertBefore ? columnOverlays.indexOf(insertBefore) : columnOverlays.length;
		
		if(!isGroupedColumn) {
			TABLE.adjustFixedColumnsOffset(placeholder, columnOverlays, insertBeforeIndex);
		}
		
		TABLE.insertBeforeSibling(placeholder, insertBefore);
		
		if(!isGroupedColumn) {
			TABLE.adjustPlaceholderAndScrollerPosition(placeholder, leftScroller);
		}
	},
	
	/**
	 * Returns true if the given element has a table separator in its children, otherwise false.
	 * 
	 * @see {@link isTableSeparator}
	 */
	hasTableSeparator: function(element) {
		return TABLE.getArrayOf(element.children).find(child => {
			return TABLE.isTableSeparator(child);
		}) !== undefined;
	},
	
	/**
	 * Returns the table separator element from the given elements.
	 * 
	 * If no separator exists then undefined is returned.
	 * 
	 * @param{elements} Elements to find the table separator from.
	 */
	getTableSeparator: function(elements) {
		return elements.find(element => TABLE.isTableSeparator(element));
	},
	
	/**
	 * Returns true if the given element separates the tables fix and flexibale part, otherwise false.
	 * 
	 * @param{element} Element to check if it is the tables separator.
	 */
	isTableSeparator: function(element) {
		return element.classList.contains('tl-table__fix-flex-separator');
	},
	
	/**
	 * Sets the property on the given element if the value is defined.
	 *
	 * @param{element} Element to set the property on.
	 * @param{property} Elements property to set the value on.
	 * @param{value} Value to set.
	 */
	setProperty: function(element, property, value) {
		if(value !== undefined) {
			element.dataset[property] = value;
		}
	},
	
	/**
	 * Returns true if the given column overlay is dragged into the area of 
	 * fixed column overlays, otherwise false.
	 *
	 * @param{draggedColumn} Overlay of the column the user drags.
 	 * @param{placeholder} Column overlay of the dragged column placeholder.
	 * @param{tableSeparator} Element separates the tables fixed and flex columns.
	 */
	isDraggedIntoFixedArea: function(draggedColumn, placeholder, tableSeparator) {
		if(tableSeparator) {
			if(TABLE.isRightOfElement(draggedColumn, placeholder)) {
				return !TABLE.isRightOfElement(draggedColumn, tableSeparator);
			} else {
				return TABLE.isLeftOfElement(draggedColumn, tableSeparator);
			}
		} else {
			return placeholder.dataset.isFixed;
		}
	},
	
	/**
	 * Returns an array filtered by only column overlays.
	 *
	 * @param{elements} HTMLCollection of items.
	 */
	getColumnOverlays: function(elements) {
		return TABLE.getArrayOf(elements).filter((element) => TABLE.isColumnOverlay(element));
	},
	
	/**
	 * Returns true if the given element is a column inside a table overlay
	 * to reorder columns.
	 * 
	 * @param{element} Element to check if it is a table column overlay.
	 */
	isColumnOverlay: function(element) {
		return element.classList.contains('tl-table-overlay__column');
	},
	
	/**
	 * True if the first given element is left of the second given element, otherwise false.
	 *
	 * This method use Element.getBoundingClientRect to compare the elements positions.
	 */
	isLeftOfElement: function(element1, element2) {
		return element1.getBoundingClientRect().left <= element2.getBoundingClientRect().left;
	},
	
	/**
	 * Returns true if the given element is the last fixed element of his siblings, otherwise false.
	 * 
	 * @param {element} Element to check if it is the last fixed among his siblings.
	 */
	isLastFixedElement: function(element) {
		return TABLE.getLastFixedElement(TABLE.getArrayOf(element.parentNode.children)) === element;
	},
	
	/**
	 * True if the first given element is right of the second given element, otherwise false.
	 *
	 * This method use Element.getBoundingClientRect to compare the elements positions.
	 */
	isRightOfElement: function(element1, element2) {
		return element2.getBoundingClientRect().right <= element1.getBoundingClientRect().right;
	},
	
	/**
	 * Returns the element where the placeholder column overlay should moved before.
	 *
	 * @param{draggedColumn} Overlay of the column the user drags.
	 * @param{placeholder}  Column overlay of the dragged column placeholder.
	 * @param{columnOverlays} Collection of column overlays in the same group as the placeholder.
	 */
	getDraggedColumnInsertBefore: function(draggedColumn, placeholder, columnOverlays) {
		if(TABLE.isRightOfElement(draggedColumn, placeholder)) {
			return TABLE.getInsertBeforeForDraggingRight(draggedColumn, placeholder, columnOverlays);
		} else {
			return TABLE.getInsertBeforeForDraggingLeft(draggedColumn, placeholder, columnOverlays);
		}
	},
	
	/**
	 * Returns the element where the placeholder column overlay should moved before.
	 *
	 * @param{draggedColumn} Overlay of the column the user drags.
	 * @param{placeholder}  Column overlay of the dragged column placeholder.
	 * @param{columnOverlays} Collection of column overlays in the same group as the placeholder.
	 */
	getInsertBeforeForDraggingRight: function(draggedColumn, placeholder, columnOverlays) {
		var placeholderIndex = columnOverlays.indexOf(placeholder);
		var isRightOfColumn = (column) => !TABLE.isRightOfElement(draggedColumn, column);
		
		if(TABLE.isTrue(placeholder.dataset.isFixed) && !TABLE.isGroupedColumnOverlay(placeholder)) {
			var tableSeparator = TABLE.getTableSeparator(columnOverlays);
			
			return TABLE.find(columnOverlays, placeholderIndex + 1, columnOverlays.indexOf(tableSeparator) + 1, isRightOfColumn);
		} else {
			var firstFlexibleColumnIndex = placeholderIndex + TABLE.firstFlexibleColumnIndex(columnOverlays.slice(placeholderIndex));
			
			return TABLE.find(columnOverlays, Math.max(placeholderIndex + 1, firstFlexibleColumnIndex), columnOverlays.length, isRightOfColumn);
		}
	},
	
	/**
	 * Returns the element where the placeholder column overlay should moved before.
	 *
	 * @param{draggedColumn} Overlay of the column the user drags.
	 * @param{placeholder}  Column overlay of the dragged column placeholder.
	 * @param{columnOverlays} Collection of column overlays in the same group as the placeholder.
	 */
	getInsertBeforeForDraggingLeft: function(draggedColumn, placeholder, columnOverlays) {
		var insertBeforeIndex = columnOverlays.indexOf(placeholder);
		var isLeftOfColumn = (column) => TABLE.isLeftOfElement(draggedColumn, column);
		var tableSeparatorIndex = columnOverlays.indexOf( TABLE.getTableSeparator(columnOverlays));
	
		if(TABLE.isTrue(placeholder.dataset.isFixed) && !TABLE.isGroupedColumnOverlay(placeholder)) {
			insertBeforeIndex = Math.min(insertBeforeIndex, tableSeparatorIndex);
			
			var startIndex = 0;
			var stopIndex = insertBeforeIndex + 1;
		} else {
			startIndex = tableSeparatorIndex !== -1 ? tableSeparatorIndex : 0;
			stopIndex = insertBeforeIndex + 1;
		}
		
		return TABLE.find(columnOverlays, startIndex, stopIndex, isLeftOfColumn);
	},
	
	/**
	 * Find last index of array element that satifies the given predicate starting from the given end position.
	 *
	 * Returns -1 when the predicate is always falsy.
	 *
	 * @param{array} Array to traverse to find an element that statifies the given predicate.
	 * @param{startIndex} Index of array to end the search.
	 * @param{endIndex} Index of array to start the search.
	 * @param{predicate} Array elements are searched that satify this predicate.
	 *
	 * @see {@link findIndex}
	 */
	findLastIndex: function(array, startIndex, endIndex, predicate) {
		var index = array.slice(startIndex, endIndex).findLastIndex(predicate);
		
		return index === -1 ? -1 : index + startIndex;
	},
	
	/**
	 * Returns the last array element that satisfies the given predicate starting from the given start index
	 * to the given end index.
	 * 
	 * Returns undefined if not element satisfy the given predicate.
	 * 
	 * @param{array} Array to traverse to find an element that statifies the given predicate.
	 * @param{startIndex} Index of array to end the search.
	 * @param{endIndex} Index of array to start the search.
	 * @param{predicate} Array elements are searched that satify this predicate.
	 */
	findLast: function(array, startIndex, endIndex, predicate) {
		return array.slice(startIndex, endIndex).findLast(predicate);
	},
	
	/**
	 * Find first index of array element that satifies the given predicate starting from the given start position.
	 *
	 * Returns -1 when the predicate is always falsy.
	 *
	 * @param{array} Array to traverse to find an element that statifies the given predicate.
	 * @param{startIndex} Index of array to start the search.
	 * @param{endIndex} Index of array to end the search.
	 * @param{predicate} Array elements are searched that satify this predicate.
	 *
	 * @see {@link findLastIndex}
	 */
	findIndex: function(array, startIndex, endIndex, predicate) {
		var index = array.slice(startIndex, endIndex).findIndex(predicate);
		
		return index === -1 ? -1 : index + startIndex;
	},
	
	/**
	 * Returns the first array element that satisfies the given predicate starting from the given start index
	 * to the given end index.
	 * 
	 * Returns undefined if not element satisfy the given predicate.
	 * 
	 * @param{array} Array to traverse to find an element that statifies the given predicate.
	 * @param{startIndex} Index of array to end the search.
	 * @param{endIndex} Index of array to start the search.
	 * @param{predicate} Array elements are searched that satify this predicate.
	 */
	find: function(array, startIndex, endIndex, predicate) {
		return array.slice(startIndex, endIndex).find(predicate);
	},
	
	/**
	 * If the placeholder column overlays fixed property has changed i. e. 
	 * the old column was fixed and is now flexible or 
	 * vice vera, then the placeholder styling is adjusted accordingly.
	 *
	 * @param{placeholder} Column overlay of the dragged column placeholder.
	 * @param{leftScroller} Scroller to scroll the flexible columns in the table overlay to the left.
	 *
	 * @see {@link changeElementToFixed}
	 * @see {@link changeElementToFlexible}
	 */
	adjustPlaceholderAndScrollerPosition: function(placeholder, leftScroller) {
		var isDraggedInFixedColumnOverlays = TABLE.isTrue(placeholder.dataset.isFixed);
	
		if(isDraggedInFixedColumnOverlays != TABLE.isFixed(placeholder)) {
			if(isDraggedInFixedColumnOverlays) {
				TABLE.changeElementToFixed(placeholder);
				
				TABLE.moveFromLeft(leftScroller, placeholder.offsetWidth);
			} else {
	        	TABLE.changeElementToFlexible(placeholder);
			        	
	   		    TABLE.moveFromLeft(leftScroller, -placeholder.offsetWidth);
			}
		}
		
		TABLE.setLeftOffset(placeholder, TABLE.getElementsWidth(TABLE.getAllPreviousSiblings(placeholder)));
	},
	
	/**
	 * Returns all previous siblings of the given element.
	 *
	 * @param{element} Element to compute the previous siblings of.
	 */
	getAllPreviousSiblings: function(element) {
		var siblings = TABLE.getArrayOf(element.parentNode.children);
		
		return siblings.slice(0, siblings.indexOf(element));
	},
	
	/**
	 * Add styles to elements so that the elements become fixed.
	 *
	 * @param{elements} Array of elements that become fixed.
	 *
	 * @see {@link changeElementToFixed}
	 */
	changeElementsToFixed: function(elements) {
		elements.forEach((element) => TABLE.changeElementToFixed(element));
	},
	
	/**
	 * Add styles to element so that the element becomes fixed.
	 *
	 * @param{element} Element that becomes fixed.
	 */
	changeElementToFixed: function(element) {
		element.classList.add('tl-position--sticky');
	},
	
	/**
	 * Add styles to elements so that the elements become flexible.
	 *
	 * @param{elements} Array of elements that become flexible.
	 *
	 * @see {@link changeElementToFlexible}
	 */
	changeElementsToFlexible: function(elements) {
		elements.forEach((element) => TABLE.changeElementToFlexible(element));
	},
	
	/**
	 * Removes styles so that the element becomes flexible.
	 * 
	 * A fixed table cell resp. column is an sticky positioned element on the left border.
	 *
	 * @param{element} Element that becomes flexible.
	 */
	changeElementToFlexible: function(element) {
		element.classList.remove('tl-position--sticky');
		element.style.removeProperty('left');
	},
	
	/**
	 * Adjusts the left offset of those fixed columns, that are affected by the
	 * reordering of the placeholder column, by the placeholder columns width.
	 *
	 * If the user moves for instance the column to the right, then all fixed columns are affect between 
	 * the old and new position of this column.
	 *
	 * @param{placeholder}  Column overlay of the dragged column placeholder.
	 * @param{columnOverlays} Collection of column overlays in the same group as the placeholder.
	 * @param{insertBeforeIndex} Index of the column to insert the given placeholder before.
	 */
	adjustFixedColumnsOffset: function(placeholder, columnOverlays, insertBeforeIndex) {
		var placeholderIndex = columnOverlays.indexOf(placeholder);
		
		if(placeholderIndex !== insertBeforeIndex) {
			var placeholderWidth = placeholder.offsetWidth;
			
			if(placeholderIndex < insertBeforeIndex) {
				TABLE.moveFixedElementsToRight(columnOverlays.slice(placeholderIndex + 1, insertBeforeIndex), -placeholderWidth);
			} else {
				TABLE.moveFixedElementsToRight(columnOverlays.slice(insertBeforeIndex, placeholderIndex), placeholderWidth);
			}
		}
	},
	
	/**
	 * Inserts the given element before the given sibling.
	 *
	 * If the given sibling is null, then it is appended at the end.
	 *
	 * @param{element} Element to insert before one of his siblings.
	 * @param{sibling} Sibling of the given element.
	 */
	insertBeforeSibling: function(element, sibling) {
		if(sibling) {
	    	element.parentNode.insertBefore(element, sibling);
		} else {
	    	element.parentNode.appendChild(element);
		}
	},
	
	/**
	 * Returns the value of the given array index.
	 *
	 * @param{array} Array object.
	 * @param{index} Array position.
	 */
	getArrayValueOrNull: function(array, index) {
		if(index < array.length) {
	    	return array[index];
		} else {
	    	return null;
		}
	},
	
	/**
	 * Returns the sum of all the given elements offsetWidths. 
	 * If the collection of elements is empty then 0 is returned.
	 *
	 * @param{elements} Collection of elements.
	 */
	getElementsWidth: function(elements) {
		var summedWidth = 0;
		
		for (var i = 0; i < elements.length; i++) { 
			summedWidth += elements[i].offsetWidth; 
		}
		
		return summedWidth;
	},
	
	/**
	 * Moves the all fixed elements of the given set until the first flexible element appears by the 
	 * given offset to the right.
	 *
	 * @param{elements} Collection of elements.
	 * @param{offset} Amount of pixel to move the elements to the right.
	 */
	moveFixedElementsToRight: function(elements, offset) {
		for(var i = 0; i < elements.length; i++) {
			var element = elements[i];
			
            if(TABLE.isFixed(element)) {
				TABLE.moveFromLeft(element, offset);
            } else {
            	break;
            }
		}
	},
	
	/**
	 * Returns the sum of the width of all fixed columns.
	 *
	 * @param{columns} Preview container columns.
	 */
	computeWidthOfAllFixedColumns: function(columns) {
		var lastFixedColumn;
	
        for(var i=0; i<columns.length; i++){
            var column = columns[i];

            if(TABLE.isFixed(column)) {
            	lastFixedColumn = column;
            } else {
            	break;
            }
        }
        
        return lastFixedColumn ? lastFixedColumn.offsetLeft + lastFixedColumn.offsetWidth : 0;
	},
	
	/**
	 * Return true if the given element has the sticky position styling.
	 *
	 * @param{element} Element node.
	 */
	isFixed: function(element) {
		return window.getComputedStyle(element).position === 'sticky';
	},
	
	/**
	 * Return true if the given column is not fixed.
	 * 
	 * @param{column} Column element.
	 * 
	 * @see {@link isFixed}
	 */
	isFlexibleColumn: function(column) {
		if(TABLE.isColumnOverlay(column)) {
			return !TABLE.isFixed(TABLE.getOutermostColumnOverlay(column));
		} else {
			return !TABLE.isFixed(column);
		}
	},
	
	/**
	 * Returns the index of the first flexible (non fixed) column in the given columns container.
	 *
	 * If no flexible column exists then Number.MAX_SAFE_INTEGER is returned.
	 *
	 * @param{columns} Collection of columns.
	 */
	firstFlexibleColumnIndex: function(columns) {
		for(var i = 0; i < columns.length; i++) {
            if(!TABLE.isFixed(columns[i])) {
            	return i;
            }
		}
		
		return Number.MAX_SAFE_INTEGER;
	},
	
	/**
	 * Returns the index of the last fixed element of the given elements.
	 *
	 * If no fixed element exists then -1 is returned.
	 *
	 * @param{elements} Array of elements.
	 */
	getLastFixedElementIndex: function(elements) {
		return elements.findLastIndex(element => {
			return TABLE.isFixed(element);
		});
	},
	
	/**
	 * Returns the last fixed element of the given elements.
	 *
	 * If no fixed element exists then undefined is returned.
	 *
	 * @param{elements} Array of elements.
	 */
	getLastFixedElement: function(elements) {
		return elements.findLast(element => {
			return TABLE.isFixed(element);
		});
	},
	
	/**
	 * Returns the first flexibale element of the given elements.
	 *
	 * If no flexibale element exists then undefined is returned.
	 *
	 * @param{elements} Array of elements.
	 */
	getFirstFlexibaleElement: function(elements) {
		return elements.find(element => {
			return !TABLE.isFixed(element);
		});
	},
	
	/**
	 * Resize the elemens height specified by the given id relative to the mouse movement.
	 *
	 * @param{onMouseDownEvent} Mouse down event.
	 * @param{ctrlID} Control identifier.
	 */
	resizeHeight: function(onMouseDownEvent, ctrlID) {
		document.body.style.cursor = 'ns-resize';
		document.body.style.userSelect = 'none';
	
		var elementToResize = document.getElementById(ctrlID);
		
		var resizeContainerOnMove = TABLE.createHeightAdjusterOnMove(elementToResize);
	
		window.addEventListener('mousemove', resizeContainerOnMove);
 		window.addEventListener('mouseup', (event) => {
 			window.removeEventListener('mousemove', resizeContainerOnMove);
			
			document.body.style.cursor = 'default';
			document.body.style.userSelect = 'auto';
			
			TABLE.saveHeightOnServer(elementToResize);
 		}, { once: true });
	},
	
	/**
	 * Creates the handler that adjust the maximal height of the given element
	 * when the user moves the cursor.
	 *
	 * The maximum height of the element is limited by the maximum height of its content.
	 *
	 * @param{element} Element thats height being resized.
	 */
	createHeightAdjusterOnMove: function(element) {
		return (event) => {
			var oldHeight = TABLE.getHeight(element);
			
			var oldMaxHeight = parseFloat(element.style.maxHeight);
			var newMaxHeight = BAL.relativeMouseCoordinates(BAL.getEvent(event), element).y;
	
			element.style.maxHeight = newMaxHeight + 'px';
	
			var newHeight = TABLE.getHeight(element);
			
			if(newHeight == oldHeight) {
				element.style.maxHeight = oldMaxHeight + 'px';
			}
		};
	},
	
	/**
	 * Request the server to save the new height for the given element into the users personal configuration.
	 *
	 * @param{elementToResize} Element is being resized.
	 */
	saveHeightOnServer: function(elementToResize) {
		services.ajax.execute("dispatchControlCommand", {
			controlCommand: "saveVerticalSize",
			controlID: elementToResize.id,
			size: parseFloat(elementToResize.style.maxHeight)
		}, false);
	},
	
	initFrozing: function(ctrlID, displayVersion, firstPageRow, lastPageRow, firstSliceRow,
			lastSliceRow, numberHeaderRows, fixedColumnCountAdjustable, tableSeparatorTooltip,
			clientDisplayData) {

		// Create table slice manager registry on demand
		if (TABLE.tableSliceManagers == null) {
			TABLE.tableSliceManagers = new Object();
		}

		var sliceManager = TABLE.tableSliceManagers[ctrlID];
		var tableBodyData = null;

		// Initial frozing
		if (sliceManager == null) {
			tableBodyData = TABLE.frozenTableAdjustment(ctrlID,	displayVersion,
														fixedColumnCountAdjustable, true,
														tableSeparatorTooltip);
			if (!tableBodyData.forcedFixedColumnCountAdjustment) {
				sliceManager = new TABLE.SliceManager(ctrlID, displayVersion,
						tableBodyData.bodyHeight, firstPageRow, lastPageRow,
						tableBodyData.viewports, firstSliceRow, lastSliceRow);

				TABLE.tableSliceManagers[ctrlID] = sliceManager;
				
				services.layout.addLayoutFunction(function(){
					TABLE.adjustScrollBars(ctrlID);
					var adapted = TABLE.checkMinimumSize(sliceManager, numberHeaderRows, ctrlID);
					if (adapted) {
						tableBodyData = TABLE.frozenTableAdjustment(ctrlID,	displayVersion,
														fixedColumnCountAdjustable, false,
														tableSeparatorTooltip);
					}
				});
				
				services.layout.addPostLayoutFunction(function(){
					TABLE.initPreviousScrollPosition(sliceManager, clientDisplayData);
					TABLE.activateScrollPositionPropagationInViewports(tableBodyData.viewports);
					TABLE.scrollToRequestedPosition(ctrlID, clientDisplayData);
					
				});
			}
		}
		// Resizing
		else {
			tableBodyData = TABLE.frozenTableAdjustment(ctrlID, TABLE.UNDEFINED_DISPLAY_VERSION,
														fixedColumnCountAdjustable, false,
														tableSeparatorTooltip);
			if (!tableBodyData.forcedFixedColumnCountAdjustment) {
				services.layout.addLayoutFunction(function() {
					// Adjusting visible viewport height, and updates all slices (especially
					// slice insert positions and sizes)
					sliceManager.setVisibleViewportHeight(tableBodyData.bodyHeight);
				});
			}
		}
	},
	
	frozenTableAdjustment: function(ctrlID, displayVersion, fixedColumnCountAdjustable,	isInitialLayout, tableSeparatorTooltip) {
		var horizontalFlexPartInsertPosition;
		var outermostDiv = TABLE.getFrozenTableDiv(ctrlID);
		var outermostDivWidth = services.layout.parentLayoutInformation.effectiveWidth;
		var outermostDivHeight = services.layout.parentLayoutInformation.effectiveHeight;
		var effectiveOutermostDivWidth = outermostDivWidth;
		var effectiveOutermostDivHeight = outermostDivHeight;
		
//		********************* Setter ********************
//		*************************************************
		var outermostDivLayoutFunction = services.layout.createPositionAndSizeApplianceFunction(outermostDiv, 0, 0, effectiveOutermostDivWidth, effectiveOutermostDivHeight);
		services.layout.addLayoutFunction(outermostDivLayoutFunction);
//		***************************************************
//		***************************************************

		var availableWidth = Math.max(0, effectiveOutermostDivWidth - BAL.getVerticalScrollBarWidth());
		var availableHeight = Math.max(0, effectiveOutermostDivHeight - BAL.getHorizontalScrollBarHeight());
		var tableSeparatorWidth = TABLE.DEFAULT_TABLE_SEPARATOR_WIDTH; /* px */;
		
//		********************* Setter ********************
//		*************************************************
		TABLE.setTableSeparatorWidth(ctrlID, tableSeparatorWidth);
//		***************************************************
//		***************************************************

		// Title row

		var title = document.getElementById(ctrlID + "_title");
		var titleHeight;
		if (title != null) {
			titleHeight = Math.min(parseInt(TL.getTLAttribute(title, "title-height")), availableHeight);
			
//		********************* Setter ********************
//		*************************************************
			var titleLayoutFunction = services.layout.createPositionAndSizeApplianceFunction(title, 0, 0, availableWidth, titleHeight);
			services.layout.addLayoutFunction(titleLayoutFunction);
//		***************************************************
//		***************************************************
			
		} else {
			titleHeight = 0;
		}

		// Fix Header part of the table

		var tableBodyData = new Object();
		var fixHeader = document.getElementById(ctrlID + "_headerFix");
		var hasFixPart = fixHeader != null;
		var fixBodyID = TABLE.getFixBodyID(ctrlID);
		var fixBody = document.getElementById(fixBodyID);
		if (hasFixPart && fixBody == null) {
			throw new Error("Table for Control '" + ctrlID+ "' has fixed header part but no fixed body part!");
		}
		if (!hasFixPart && fixBody != null) {
			throw new Error("Table for Control '" + ctrlID + "' has fixed body part but no fixed header part!");
		}
		var fixWidth;
		var headerHeight;
		var borderWidth;
		var fixColumnWidths = new Array();
		if (hasFixPart) {
			borderWidth = parseInt(TL.getTLAttribute(fixHeader, "border-width")); 
			var fixHeaderTable = fixHeader.getElementsByTagName("table")[0];
			var fixTableHeader = fixHeaderTable.getElementsByTagName("thead")[0];
			var fixTableRows = BAL.DOM.getChildElements(fixTableHeader);
			var fixTableRow = fixTableRows[fixTableRows.length - 1];
			var fixTableColumns = BAL.DOM.getChildElements(fixTableRow);
			
			// Check if fix table does fit screen width
			var fixTableWidth = 0;
			for (var j = 0; j < fixTableColumns.length; j++) {
				var currentColumnWidth = parseInt(TL.getTLAttribute(fixTableColumns[j], "column-width"));
				if (currentColumnWidth > 0) { // TODO: WHY no check at flex table part?
					fixColumnWidths.push(currentColumnWidth);
					fixTableWidth += currentColumnWidth;
				}
			}
			fixTableWidth += fixTableColumns.length * borderWidth + borderWidth;

			var maximumWidth = availableWidth - TABLE.MINIMUM_FLEX_SPACE;
			if (fixTableWidth <= maximumWidth) {
				fixWidth = fixTableWidth;
			}

			// Force adjustment of fixed columns to fit maximum fix table width
			else if (fixedColumnCountAdjustable) {
				// Determine fixed column count
				var newFixedColumns = fixTableColumns.length;
				for (; newFixedColumns > 0; newFixedColumns--) {
					if (fixTableWidth <= maximumWidth) {
						break;
					}
					fixTableWidth -= fixColumnWidths[newFixedColumns - 1] + borderWidth;
				}
				
				var reloadTimerID = BAL.DOM.getNonStandardAttribute(outermostDiv, "reloadTimerID");
				if(reloadTimerID != undefined && reloadTimerID != null) {
					BAL.getTopLevelWindow().clearTimeout(reloadTimerID);
				}
				
				// Notify server to reload page
				var repaintFunction = function() {
					BAL.DOM.removeNonStandardAttribute(outermostDiv, "reloadTimerID");
					services.ajax.execute("dispatchControlCommand", {
						controlCommand: "updateFixedColumnAmount",
						controlID: ctrlID,
						fixedColumnAmount: newFixedColumns
					}, /* useWaitPane */true);
				};
				
				reloadTimerID = BAL.getTopLevelWindow().setTimeout(repaintFunction, 100);
//		********************* Setter ********************
//		*************************************************
				services.layout.addLayoutFunction(function() {
					BAL.DOM.setNonStandardAttribute(outermostDiv, "reloadTimerID", reloadTimerID);
				});
//		***************************************************
//		***************************************************

				// Stop frozing function, due to page reload request
				tableBodyData.forcedFixedColumnCountAdjustment = true;

				return tableBodyData;
			} else {
				fixWidth = maximumWidth;
			}
			
			headerHeight = 0;
			for (var j = 0; j < fixTableRows.length; j++) {
				headerHeight += parseInt(TL.getTLAttribute(fixTableRows[j], "header-row-height"));
			}
//		********************* Setter ********************
//		*************************************************
			var fixHeaderLayoutFunction = services.layout.createPositionAndSizeApplianceFunction(fixHeader, 0, titleHeight, fixWidth, headerHeight);
			services.layout.addLayoutFunction(fixHeaderLayoutFunction);
			services.layout.addLayoutFunction(function() {
				fixHeader.style.overflow = "hidden";
			});
			
//		***************************************************
//		***************************************************

		} else {
			fixWidth = 0;
			headerHeight = 0;
		}

		// Flex Header part of the table

		var flexHeader = document.getElementById(ctrlID + "_headerFlex");
		var hasFlexPart = flexHeader != null;
		var flexHeaderTable = null;
		var flexWidth = 0;
		var flexColumnWidths = new Array();
		var horizontalScrollManagerInstance = new TABLE.HorizontalScrollManager(availableWidth, flexWidth);
		var verticalScrollManagerInstance = new TABLE.VerticalScrollManager(ctrlID);
		if (hasFlexPart) {
			flexHeaderTable = flexHeader.getElementsByTagName("table")[0];
			var flexTableHeader = flexHeaderTable.getElementsByTagName("thead")[0];
			var flexTableRows = BAL.DOM.getChildElements(flexTableHeader);
			var flexTableRow = flexTableRows[flexTableRows.length - 1];
			var flexTableColumns = BAL.DOM.getChildElements(flexTableRow);
			if(!hasFixPart) {
				borderWidth = parseInt(TL.getTLAttribute(flexHeader, "border-width"));
			} 
			if (headerHeight == 0) {
				for (var j = 0; j < flexTableRows.length; j++) {
					headerHeight += parseInt(TL.getTLAttribute(flexTableRows[j], "header-row-height"));
				}
			}
			horizontalFlexPartInsertPosition = fixWidth + tableSeparatorWidth;
			if(!hasFixPart) {
				horizontalFlexPartInsertPosition++; // Add table separator overlap from fixed part
			}
			
			flexWidth = Math.max(availableWidth - fixWidth - tableSeparatorWidth, 0);
//			********************* Setter ********************
//			*************************************************
			var flexHeaderLayoutFunction = services.layout.createPositionAndSizeApplianceFunction(flexHeader, horizontalFlexPartInsertPosition, titleHeight, flexWidth, headerHeight);
			services.layout.addLayoutFunction(flexHeaderLayoutFunction);
			services.layout.addLayoutFunction(function() {
				flexHeader.style.overflow = "hidden";
			});
//			***************************************************
//			***************************************************

			for (var j = 0; j < flexTableColumns.length; j++) {
				flexColumnWidths.push(parseInt(TL.getTLAttribute(flexTableColumns[j], "column-width")));
			}
			
			horizontalScrollManagerInstance.setFlexTableWidth(flexWidth);
			if(isInitialLayout) {
				horizontalScrollManagerInstance.setFlexHeader(flexHeader);
			}
		}
		// Footer part of the table

		var footer = document.getElementById(ctrlID + "_footer");
		var footerHeight;
		if (footer != null) {
			footerHeight = Math.min(parseInt(TL.getTLAttribute(footer, "footer-height")), availableHeight);
//			********************* Setter ********************
//			*************************************************
			var footerLayoutFunction = services.layout.createPositionAndSizeApplianceFunction(footer, 0, availableHeight - footerHeight, availableWidth, footerHeight);
			services.layout.addLayoutFunction(footerLayoutFunction);
//			***************************************************
//			***************************************************
		} else {
			footerHeight = 0;
		}

		// Fix Body part of the table

		var viewports = new Array();
		var verticalFixAndFlexBodyPosition = headerHeight + titleHeight;
		
		// Ensure, that viewport has technically at least height of 1 px
		var fixAndFlexBodyHeight = Math.max(1, availableHeight - footerHeight - headerHeight - titleHeight);
		if (hasFixPart) {
//			********************* Setter ********************
//			*************************************************
			var fixBodyLayoutFunction = services.layout.createPositionAndSizeApplianceFunction(fixBody, 0, verticalFixAndFlexBodyPosition, fixWidth, fixAndFlexBodyHeight);
			services.layout.addLayoutFunction(fixBodyLayoutFunction);
			services.layout.addLayoutFunction(function() {
				fixBody.style.overflow = "hidden";
			});
//			***************************************************
//			***************************************************
			var fixBodyViewportID = TABLE.getViewportIDForTablePartWithID(fixBodyID);
			var fixBodyViewport = null;
			if (isInitialLayout) {
				if(displayVersion == TABLE.UNDEFINED_DISPLAY_VERSION) {
					throw new Error("Initial table state at client side cannot be created with undefined display version!");
				}
				fixBodyViewport = new TABLE.Viewport(ctrlID, displayVersion, fixBodyViewportID,
						fixWidth, 
						fixColumnWidths, 
						0);
				verticalScrollManagerInstance.setFixBody(fixBody);
				verticalScrollManagerInstance.setFixBodyViewport(fixBodyViewport);
			} else {
				var presentSliceManager = TABLE.tableSliceManagers[ctrlID];
				fixBodyViewport = presentSliceManager.getViewport(fixBodyViewportID);
				if (fixBodyViewport != null) {
					fixBodyViewport.setColumnWidths(fixColumnWidths);
					// TODO: why no adjustment of width as in flex part
				} else {
					throw new Error("Client is not in sync with server. Table is not rendered initial, but there is no sliceManager. Reload page!");
				}
			}
			viewports.push(fixBodyViewport);
		}

		// Flex Body part of the table

		var flexBodyID = TABLE.getFlexBodyID(ctrlID);
		var flexBody = document.getElementById(flexBodyID);
		if (hasFlexPart && flexBody == null) {
			throw new Error("Table for Control '" + ctrlID + "' has flex header part but no flex body part!");
		}
		if (!hasFlexPart && flexBody != null) {
			throw new Error("Table for Control '" + ctrlID + "' has flex body part but no flex header part!");
		}

		var flexTableWidth = 0;
		if (hasFlexPart) {
			for (var j = 0; j < flexColumnWidths.length; j++) {
				flexTableWidth += flexColumnWidths[j];
			}
			flexTableWidth += flexColumnWidths.length * borderWidth + borderWidth;
//			********************* Setter ********************
//			*************************************************
			var flexBodyLayoutFunction = services.layout.createPositionAndSizeApplianceFunction(flexBody, horizontalFlexPartInsertPosition, verticalFixAndFlexBodyPosition, flexWidth, fixAndFlexBodyHeight);
			services.layout.addLayoutFunction(flexBodyLayoutFunction);
			services.layout.addLayoutFunction(function() {
				flexBody.style.overflow = "hidden";
			});
//			***************************************************
//			***************************************************
			var flexBodyViewportID = TABLE.getViewportIDForTablePartWithID(flexBodyID);
			var flexBodyViewport = null;
			if (isInitialLayout) {
				if(displayVersion == TABLE.UNDEFINED_DISPLAY_VERSION) {
					throw new Error("Initial table state at client side cannot be created with undefined display version!");
				}
				flexBodyViewport = new TABLE.Viewport(ctrlID, displayVersion, flexBodyViewportID,
						flexTableWidth, 
						flexColumnWidths,
						fixColumnWidths.length);
				horizontalScrollManagerInstance.setFlexBody(flexBody);
				horizontalScrollManagerInstance.setFlexBodyViewport(flexBodyViewport);
				verticalScrollManagerInstance.setFlexBody(flexBody);
				verticalScrollManagerInstance.setFlexBodyViewport(flexBodyViewport);
			} else {
				var presentSliceManager = TABLE.tableSliceManagers[ctrlID];
				flexBodyViewport = presentSliceManager.getViewport(flexBodyViewportID);
				if (flexBodyViewport == null) {
					throw new Error("Client is not in sync with server. Table is not rendered initial, but there is no sliceManager. Reload page!");
				}

				// Adjust viewport width
				flexBodyViewport.initWidth(flexTableWidth);
				
				// Adjust column widths
				flexBodyViewport.setColumnWidths(flexColumnWidths);
			}
			viewports.push(flexBodyViewport);
		}
		
		// Horizontal scroll bar

		var horizontalScrollOuter = document.getElementById(ctrlID + "_horizontalScroll");
		var horizontalScrollInner;
		if (horizontalScrollOuter != null) {
			horizontalScrollInner = horizontalScrollOuter.getElementsByTagName("div")[0];
		}
		
		var horizontalScrollInnerWidth = 0;
		if (hasFlexPart) {
			// needed: innerScrollbarDivSize/outerScrollbarDivSize == neededFlexSize/availableFlexSize
			horizontalScrollInnerWidth = (flexTableWidth * availableWidth) / flexWidth;
		}
		
		if (horizontalScrollOuter == null) {
//			********************* Setter ********************
//			*************************************************
			services.layout.addLayoutFunction(function() {
				
				horizontalScrollOuter = document.createElement("div");
				horizontalScrollOuter.id = ctrlID + "_horizontalScroll";
				horizontalScrollOuter.style.position = "absolute";
				horizontalScrollOuter.style.overflowX = "scroll";
				horizontalScrollOuter.style.overflowY = "hidden";
				
				horizontalScrollInner = document.createElement("div");
				horizontalScrollInner.style.visibility = "hidden";
				
				if(hasFlexPart && isInitialLayout) {
					horizontalScrollOuter.horizontalScrollManager = horizontalScrollManagerInstance;
					horizontalScrollManagerInstance.setScrollbar(horizontalScrollOuter);
					
					var cancelHorizontalScrollManager = function() {
						BAL.removeEventListener(horizontalScrollOuter, 'DOMNodeRemovedFromDocument', cancelHorizontalScrollManager);
						horizontalScrollManagerInstance.cancelAllAnimations();
					};
					BAL.addEventListener(horizontalScrollOuter, 'DOMNodeRemovedFromDocument', cancelHorizontalScrollManager);
				}
				
				BAL.setEffectiveWidth(horizontalScrollInner, horizontalScrollInnerWidth);
				BAL.setEffectiveHeight(horizontalScrollInner, Math.min(BAL.getHorizontalScrollBarHeight(), effectiveOutermostDivHeight));
				BAL.setEffectiveWidth(horizontalScrollOuter, availableWidth);
				BAL.setEffectiveHeight(horizontalScrollOuter, Math.min(BAL.getHorizontalScrollBarHeight(), effectiveOutermostDivHeight));
				BAL.setElementY(horizontalScrollOuter, availableHeight);
				
				outermostDiv.appendChild(horizontalScrollOuter);
				horizontalScrollOuter.appendChild(horizontalScrollInner);
			});
//			***************************************************
//			***************************************************
		} else {
//			********************* Setter ********************
//			*************************************************
			services.layout.addLayoutFunction(function() {
				BAL.setEffectiveWidth(horizontalScrollInner, horizontalScrollInnerWidth);
				BAL.setEffectiveHeight(horizontalScrollInner, Math.min(BAL.getHorizontalScrollBarHeight(), effectiveOutermostDivHeight));
				BAL.setEffectiveWidth(horizontalScrollOuter, availableWidth);
				BAL.setEffectiveHeight(horizontalScrollOuter, Math.min(BAL.getHorizontalScrollBarHeight(), effectiveOutermostDivHeight));
				BAL.setElementY(horizontalScrollOuter, availableHeight);
			});
//			***************************************************
//			***************************************************
		}
		
		if (hasFlexPart && !isInitialLayout) {
//			********************* Setter ********************
//			*************************************************
			// Adjust scrolling bar dimensions and scrolling position
			services.layout.addLayoutFunction(function() {
				horizontalScrollOuter.horizontalScrollManager.setAvailableWidth(availableWidth);
				horizontalScrollOuter.horizontalScrollManager.setFlexTableWidth(flexWidth);
				horizontalScrollOuter.horizontalScrollManager.scrollbarScrolled();
			});
//			***************************************************
//			***************************************************
		}
		

		// Vertical scroll bar

		var constantSummand = headerHeight + footerHeight + titleHeight;
		var verticalScrollOuter = document.getElementById(ctrlID + "_verticalScroll");
		if (verticalScrollOuter == null) {
//		********************* Setter ********************
//		*************************************************
			services.layout.addLayoutFunction(function() {
				
				verticalScrollOuter = document.createElement("div");
				verticalScrollOuter.id = ctrlID + "_verticalScroll";
				verticalScrollOuter.style.position = "absolute";
				verticalScrollOuter.style.overflowY = "scroll";
				verticalScrollOuter.style.overflowX = "hidden";
				if((hasFixPart || hasFlexPart) && isInitialLayout) {
					verticalScrollManagerInstance.setScrollbar(verticalScrollOuter);
					verticalScrollOuter.scrolledVertical = verticalScrollManagerInstance.scrollbarScrolled;
					verticalScrollOuter.verticalScrollManager = verticalScrollManagerInstance;
					
					var cancelVerticalScrollManager = function() {
						BAL.removeEventListener(verticalScrollOuter, 'DOMNodeRemovedFromDocument', cancelVerticalScrollManager);
						verticalScrollManagerInstance.cancelAllAnimations();
					};
					BAL.addEventListener(verticalScrollOuter, 'DOMNodeRemovedFromDocument', cancelVerticalScrollManager);
				}
				var mouseWheel = function(event) {
					var scrollingDelta = BAL.getEventMouseScrollDelta(event);
					verticalScrollOuter.scrollTop += scrollingDelta;
					verticalScrollManagerInstance.scrollbarScrolled();
				};
				
				if (hasFixPart) {
					BAL.addMouseScrollListener(fixBody, mouseWheel);
				}
				if (hasFlexPart) {
					BAL.addMouseScrollListener(flexBody, mouseWheel);
				}
				var verticalScrollInner = document.createElement("div");
				verticalScrollInner.style.visibility = "hidden";
				BAL.setEffectiveWidth(verticalScrollInner, Math.min(BAL.getVerticalScrollBarWidth(), effectiveOutermostDivWidth));
				
				
				// store informations about height of inner scroll for adjusting
				// when adding new rows to the table
				verticalScrollInner.constantSummand = constantSummand;
				if (hasFixPart) {
					verticalScrollInner.bodyTable = fixBody.getElementsByTagName("table")[0];
				} else {
					verticalScrollInner.bodyTable = flexBody.getElementsByTagName("table")[0];
				}
				BAL.setEffectiveHeight(verticalScrollOuter, availableHeight);
				BAL.setEffectiveWidth(verticalScrollOuter, Math.min(BAL.getVerticalScrollBarWidth(), effectiveOutermostDivWidth));
				BAL.setElementX(verticalScrollOuter, availableWidth);
				
				outermostDiv.appendChild(verticalScrollOuter);
				verticalScrollOuter.appendChild(verticalScrollInner);
			});
//		***************************************************
//		***************************************************
		} else {
//		********************* Setter ********************
//		*************************************************
			services.layout.addLayoutFunction(function() {
				
				BAL.setEffectiveHeight(verticalScrollOuter, availableHeight);
				BAL.setEffectiveWidth(verticalScrollOuter, Math.min(BAL.getVerticalScrollBarWidth(), effectiveOutermostDivWidth));
				BAL.setElementX(verticalScrollOuter, availableWidth);
			});
//		***************************************************
//		***************************************************
		}

		// Create table separator

		var tableSeparator = document.getElementById(ctrlID + "_tableSeperator");
		var tableSeparatorHeight = Math.max(availableHeight - titleHeight - footerHeight, 0);
		if (tableSeparator == null) {
//				********************* Setter ********************
//				*************************************************
			services.layout.addLayoutFunction(function() {
				
				tableSeparator = document.createElement("div");
				if (tableSeparatorTooltip != null) {
					BAL.DOM.setNonStandardAttribute(tableSeparator, "data-tooltip", tableSeparatorTooltip);
				}
				tableSeparator.id = ctrlID + "_tableSeperator";
				tableSeparator.style.position = "absolute";
				BAL.DOM.addClass(tableSeparator, "tblSeperator");
				if(fixedColumnCountAdjustable) {
					var TableSeperatorAdjustment = function(tableSeparatorHeight) {
						
						var self = this;
						this._tableSeparatorHeight = tableSeparatorHeight;
						
						this.initAdjustment = function() {
							
							if (hasFlexPart) {
								// Reset horizontal scrolling
								BAL.setScrollLeftElement(horizontalScrollOuter, 0);
							}
							
							// Delay frozen column amount adjustment, to ensure that the
							// adjustment function retrieves correct DOM values (e.g. positions)
							// after reset of the horizontal scrolling. Thereby the delay length
							// is not important, just the control flow has to be interrupted.
							var delayedFunction = function() {
								// Init frozen column adjustment
								return TABLE.initFrozenColumnAmountAdjustment(ctrlID,
										fixHeader, 
										flexHeader,
										fixColumnWidths.length,
										tableSeparatorWidth,
										self._tableSeparatorHeight);
							};
							
							BAL.getTopLevelWindow().setTimeout(delayedFunction, 10);
							return false;
						};
						
						this.setTableSeparatorHeight = function(tableSeperatorHeight) {
							this._tableSeparatorHeight = tableSeperatorHeight;
						};
					};
					
					tableSeparator.adjustmentFunction = new TableSeperatorAdjustment(tableSeparatorHeight);
					BAL.addEventListener(tableSeparator, "mousedown", tableSeparator.adjustmentFunction.initAdjustment);
				} else {
					tableSeparator.style.cursor = "default";
				}
				
				// Increase width of table seperator by 2px to build overlap with
				// table borders
				BAL.setEffectiveWidth(tableSeparator, Math.min(tableSeparatorWidth + 2, availableWidth));
				BAL.setEffectiveHeight(tableSeparator, tableSeparatorHeight);
				BAL.setElementX(tableSeparator, Math.max(0, fixWidth - 1));
				BAL.setElementY(tableSeparator, titleHeight);
				
				outermostDiv.appendChild(tableSeparator);
			});
//				***************************************************
//				***************************************************
		} else {
//				********************* Setter ********************
//				*************************************************
			services.layout.addLayoutFunction(function() {
				if(fixedColumnCountAdjustable) {
					tableSeparator.adjustmentFunction.setTableSeparatorHeight(tableSeparatorHeight);
				}
				
				// Increase width of table seperator by 2px to build overlap with
				// table borders
				BAL.setEffectiveWidth(tableSeparator, Math.min(tableSeparatorWidth + 2, availableWidth));
				BAL.setEffectiveHeight(tableSeparator, tableSeparatorHeight);
				BAL.setElementX(tableSeparator, Math.max(0, fixWidth - 1));
				BAL.setElementY(tableSeparator, titleHeight);
			});
//				***************************************************
//				***************************************************
		}

		// Update current scrolling positions in viewport containers
		if(!isInitialLayout) {
			if (hasFixPart) {
				fixBodyViewport.setVerticalScrollingPosition(BAL.getScrollTopElement(verticalScrollOuter));
			}
			if (hasFlexPart) {
				flexBodyViewport.setHorizontalScrollingPosition(BAL.getScrollLeftElement(horizontalScrollOuter));
				flexBodyViewport.setVerticalScrollingPosition(BAL.getScrollTopElement(verticalScrollOuter));
			}
		}

		// Deregister table slice manager, when the table becomes removed
		if (isInitialLayout) {
//			********************* Setter ********************
//			*************************************************
			services.layout.addLayoutFunction(function() {
				
				var deregisterSliceManager = function() {
					BAL.removeEventListener(outermostDiv, 'DOMNodeRemovedFromDocument', deregisterSliceManager);
					delete TABLE.tableSliceManagers[ctrlID];
				};
				BAL.addEventListener(outermostDiv, 'DOMNodeRemovedFromDocument', deregisterSliceManager);
			});
//				***************************************************
//				***************************************************
		}

		if (hasFlexPart || hasFixPart) {
			tableBodyData.bodyHeight = fixAndFlexBodyHeight;
		} else {
			tableBodyData.bodyHeight = 1;
		}
		tableBodyData.viewports = viewports;
		tableBodyData.forcedFixedColumnCountAdjustment = false;
		
		return tableBodyData;
	},
	
	checkMinimumSize: function(sliceManager, numberHeaderRows, ctrlID) {
		if (numberHeaderRows < 1) {
			return false;
		}
		var viewportContentID = ctrlID + "_bodyFix" + TABLE.Viewport.CONTAINER_ID_SUFFIX;
		var fixViewport = sliceManager.getViewport(viewportContentID);
		var numberFixedColumns;
		if (fixViewport != null) {
			numberFixedColumns = fixViewport.getColumnCount();
		} else {
			numberFixedColumns = 0;
		}
		
		var viewportContentID = ctrlID + "_bodyFlex" + TABLE.Viewport.CONTAINER_ID_SUFFIX;
		var flexViewport = sliceManager.getViewport(viewportContentID);
		var numberFlexColumns;
		if (flexViewport != null) {
			numberFlexColumns = flexViewport.getColumnCount();
		} else {
			numberFlexColumns = 0;
		}
		
		var columnCount = numberFixedColumns + numberFlexColumns;
		
		var adaptedColumnWidth = false;
		for (var columnNumber = 0; columnNumber < columnCount; columnNumber++) {
			var	columnHeaderId = sliceManager.getTableID() + "_header_" + 0 + "_" + columnNumber;
			var headerColumnDiv = document.getElementById(columnHeaderId);
			var headerColumnDivWidth = BAL.getEffectiveWidth(headerColumnDiv);
			var headerColumnThWidth = BAL.getEffectiveWidth(headerColumnDiv.parentNode);
			if (headerColumnDivWidth < headerColumnThWidth) {
				adaptedColumnWidth = true;
				var tableID = sliceManager.getTableID();
				TABLE.setHeaderColumnWidth(tableID, numberHeaderRows, columnNumber, headerColumnThWidth)
				if (columnNumber < numberFixedColumns) {
					sliceManager.updateColumnWidth(
		                fixViewport.getContainerID(),
						columnNumber, 
						headerColumnThWidth,
						/* update server column width */true);
				} else {
					sliceManager.updateColumnWidth(
		                flexViewport.getContainerID(),
						columnNumber - numberFixedColumns, 
						headerColumnThWidth,
						/* update server column width */true);
					
				}
			}
		}
		return adaptedColumnWidth;
	},
	
	/**
	 * Determines the right sibling of the given node. This method searches in
	 * both the fix header and the flex header (if not null): If the current
	 * node is the last one in the fixHeader, the first of the flexHeader is
	 * returned.
	 * 
	 * @param {Node}
	 *            currentNode must not be null
	 * @param {Element}
	 *            fixHeader may be null
	 * @param {Element}
	 *            flexHeader may be null
	 * @return {Node} my be null if the given node does not have a right
	 *         sibling.
	 */
	determineNextTableSeperatorNode: function(currentNode, fixHeader, flexHeader) {

		// Determine next node at current dom level
		var nextNode = currentNode;
		while (true) {
			nextNode = nextNode.nextSibling;
			if (nextNode != null) {
				if (BAL.DOM.getTagName(nextNode) == "th") {
					break;
				}
			} else {
				break;
			}
		}

		/*
		 * If there was no next node at current dom level, determine if the
		 * current node belongs to fix header table. If there is no fix header
		 * the element is already in the flex part and loop above must find
		 * sibling; if no flex header exists no node can be found there
		 */
		if (nextNode == null && flexHeader != null && fixHeader != null) {
			var isFixNode = false;
			var tempNode = currentNode;
			while (tempNode != null) {
				tempNode = tempNode.parentNode;
				if (tempNode == fixHeader) {
					isFixNode = true;
					break;
				} else if (tempNode == flexHeader) {
					break;
				}
			}

			if (isFixNode) {
				// Determine row in fix table
				var fixTableRow = currentNode.parentNode;
				var rowNumber = 0;
				while(fixTableRow != null) {
					fixTableRow = BAL.DOM.getPreviousElementSibling(fixTableRow);
					if(fixTableRow != null) {
						rowNumber++;
					}
				}
				
				var flexHeaderTable = flexHeader.getElementsByTagName("table")[0];
				var flexTableHeader = flexHeaderTable.getElementsByTagName("thead")[0];
				var flexTableRows = BAL.DOM.getChildElements(flexTableHeader);
				var flexTableRow = flexTableRows[rowNumber];
				var flexTableColumns = BAL.DOM.getChildElements(flexTableRow);
				nextNode = flexTableColumns[0];
				nextNode.isFirstFlexNode = true;
			}
		}

		return nextNode;
	},

    /**
	 * Determines the left sibling of the given node. This method searches in
	 * both the fix header and the flex header (if not null): If the current
	 * node is the first one in the flexHeader, the last one of the fixHeader is
	 * returned.
	 * 
	 * @param {Node}
	 *            currentNode must not be null
	 * @param {Element}
	 *            fixHeader may be null
	 * @param {Element}
	 *            flexHeader may be null
	 * @return {Node} my be null if the given node does not have a left sibling.
	 */
	determinePreviousTableSeperatorNode: function(currentNode, fixHeader, flexHeader) {


		// Determine previous node at current dom level
		var previousNode = currentNode;
		while (true) {
			previousNode = previousNode.previousSibling;
			if (previousNode != null) {
				if (BAL.DOM.getTagName(previousNode) == "th") {
					break;
				}
			} else {
				break;
			}
		}

		// If there was no previous node at current dom level, determine if the
		// current node belongs to flex header table
		if (previousNode == null && flexHeader != null && fixHeader != null) {
			var isFlexNode = false;
			var tempNode = currentNode;
			while (tempNode != null) {
				tempNode = tempNode.parentNode;
				if (tempNode == flexHeader) {
					isFlexNode = true;
					break;
				} else if (tempNode == fixHeader) {
					break;
				}
			}

			if (isFlexNode) {
				// Determine row in flex table
				var flexTableRow = currentNode.parentNode;
				var rowNumber = 0;
				while(flexTableRow != null) {
					flexTableRow = BAL.DOM.getPreviousElementSibling(flexTableRow);
					if(flexTableRow != null) {
						rowNumber++;
					}
				}
				
				var fixHeaderTable = fixHeader.getElementsByTagName("table")[0];
				var fixTableHeader = fixHeaderTable.getElementsByTagName("thead")[0];
				var fixTableRows = BAL.DOM.getChildElements(fixTableHeader);
				var fixTableRow = fixTableRows[rowNumber];
				var fixTableColumns = BAL.DOM.getChildElements(fixTableRow);

				for (var i = fixTableColumns.length - 1; i > -1; i--) {
					if (BAL.DOM.getTagName(fixTableColumns[i]) == "th") {
						previousNode = fixTableColumns[i];
						break;
					}
				}
				currentNode.isFirstFlexNode = true;
			}
		}

		return previousNode;
	},

	/**
	 * Initializes the adjustment of the number of fixed columns, i.e. installs
	 * event handlers which allows the user to select the number of fixed
	 * columns via D&D of the separator.
	 * 
	 * @param {String}
	 *            ctrlID the id of the control which rendered the table
	 * @param {Node}
	 *            fixHeader the fixed header element. May be null in case no
	 *            columns are frozen.
	 * @param {Node}
	 *            flexHeader the flexible header of the table. May be null in
	 *            case all columns are frozen.
	 * @param {Number}
	 *            fixColumnCount the current number of fixed columnns
	 * @param {Number}
	 *            adjustmentLineWidth the width of the transparent line moved by
	 *            the user
	 * @param {Number}
	 *            adjustmentLineHeight the height of the transparent line moved
	 *            by the user
	 * @return {Boolean} always false
	 */
	initFrozenColumnAmountAdjustment: function(ctrlID, fixHeader, flexHeader,
	       fixColumnCount, adjustmentLineWidth, adjustmentLineHeight) {

		var outermostDiv = TABLE.getFrozenTableDiv(ctrlID);

		var currentNode, nextNode, previousNode;
		var currentNodePosition, nextNodePosition, previousNodePosition;
		if (flexHeader != null) {
			// Determine current horizontal position data
			var flexHeaderTable = flexHeader.getElementsByTagName("table")[0];
			var flexTableHeader = flexHeaderTable.getElementsByTagName("thead")[0];
			var flexTableRows = BAL.DOM.getChildElements(flexTableHeader);
			var flexTableRow = flexTableRows[flexTableRows.length - 1];
			var flexTableColumns = BAL.DOM.getChildElements(flexTableRow);
			currentNode = flexTableColumns[0];
			currentNodePosition = TABLE.relativeElementPosition(currentNode, outermostDiv).x;

			nextNode = TABLE.determineNextTableSeperatorNode(currentNode, fixHeader, flexHeader);
			if(nextNode != null) {
				nextNodePosition = TABLE.relativeElementPosition(nextNode, outermostDiv).x;
			} else {
				nextNodePosition = currentNodePosition + BAL.getElementWidth(currentNode);
			}
    		
    		if (fixHeader != null) {
    			previousNode = TABLE.determinePreviousTableSeperatorNode(currentNode, fixHeader, flexHeader);
    			previousNodePosition = TABLE.relativeElementPosition(previousNode, outermostDiv).x;
    		} else {
    			// no fixed columns, separator is at the front of the table
    			previousNode = null;
    			previousNodePosition = null;
    		}
			
		} else {
            var fixHeaderTable = fixHeader.getElementsByTagName("table")[0];
            var fixTableHeader = fixHeaderTable.getElementsByTagName("thead")[0];
            var fixTableRows =  BAL.DOM.getChildElements(fixTableHeader);
			var fixTableRow = fixTableRows[fixTableRows.length - 1];
            var fixTableColumns = BAL.DOM.getChildElements(fixTableRow);
            previousNode = fixTableColumns[fixTableColumns.length - 1];
            previousNodePosition = TABLE.relativeElementPosition(previousNode, outermostDiv).x;

			// currently no flex part, all columns fixed, separator is at the end of the table
			currentNode = nextNode = null;
			currentNodePosition = nextNodePosition = previousNodePosition + BAL.getElementWidth(previousNode);
		}


		var currentFixedColumns = fixColumnCount;

		// Create background pane
		var mainLayout = services.ajax.topWindow.document.getElementsByTagName("div")[0];
		var backgroundPane = document.createElement("div");
		mainLayout.appendChild(backgroundPane);
		BAL.DOM.addClass(backgroundPane, "tblColumnAdjustmentPane");

		// Create table seperator adjustment div
		var tableSeperator = document.createElement("div");
		outermostDiv.appendChild(tableSeperator);
		BAL.DOM.addClass(tableSeperator, "tblSeperatorLine");
		BAL.setElementWidth(tableSeperator, adjustmentLineWidth);
		BAL.setElementHeight(tableSeperator, adjustmentLineHeight);
		var horizontalPosition = fixHeader == null ? 0 : BAL.getElementWidth(fixHeader);
		var verticalPosition;
		if(fixHeader != null) {
			verticalPosition = BAL.getElementY(fixHeader);
		} else {
			verticalPosition = BAL.getElementY(flexHeader);
		}
		BAL.setElementX(tableSeperator, horizontalPosition);
		BAL.setElementY(tableSeperator, verticalPosition);

		// Create function for adjustment of table seperator position
		var adjustTableSeperatorPosition = function(event) {

			// Retrieve mouse position
			var ev = BAL.getEvent(event);
			var currentMousePosition = BAL.relativeMouseCoordinates(ev, outermostDiv).x;

			// Determine horizontal position change of the table seperator
			if (currentMousePosition > currentNodePosition) {
				
				var maxTableSeparatorPosition = BAL.getElementX(TABLE.getVerticalScrollbarOuter(ctrlID)) - TABLE.MINIMUM_FLEX_SPACE;
				
				// In case the current mouse position is near to the next node position
				if (nextNode != currentNode 
				        && (nextNodePosition - currentMousePosition) < (currentMousePosition - currentNodePosition)
						&& (nextNodePosition <= maxTableSeparatorPosition)) {

					previousNode = currentNode;
					previousNodePosition = currentNodePosition;
					currentNode = nextNode;
					currentNodePosition = nextNodePosition;
					if (currentNode != null) {
    					nextNode = TABLE.determineNextTableSeperatorNode(currentNode, fixHeader, flexHeader);
        				if(nextNode != null) {
        					nextNodePosition = TABLE.relativeElementPosition(nextNode, outermostDiv).x;
        				} else {
        					nextNodePosition = currentNodePosition + BAL.getElementWidth(currentNode);
        				}
					}
						

					BAL.setElementX(tableSeperator, currentNodePosition - TABLE.DEFAULT_TABLE_SEPARATOR_WIDTH);

					if (previousNode != null && previousNode.colSpan) {
						currentFixedColumns += previousNode.colSpan;
					} else {
						currentFixedColumns++;
					}
				}
			} else {

				// In case the current mouse position is near to the previous
				// node position
				if (previousNode != null 
				        && (currentMousePosition - previousNodePosition) < (currentNodePosition - currentMousePosition)
						&& previousNodePosition >= 0) {

					nextNode = currentNode;
					nextNodePosition = currentNodePosition;
					currentNode = previousNode;
					currentNodePosition = previousNodePosition;
					previousNode = TABLE.determinePreviousTableSeperatorNode(currentNode, fixHeader, flexHeader);
					if (previousNode != null) {
    					previousNodePosition = TABLE.relativeElementPosition(previousNode, outermostDiv).x;
					} else {
						previousNodePosition = null;
					}

					BAL.setElementX(tableSeperator, currentNodePosition - TABLE.DEFAULT_TABLE_SEPARATOR_WIDTH);

					if (nextNode != null && nextNode.colSpan) {
						currentFixedColumns -= nextNode.colSpan;
					} else {
						currentFixedColumns--;
					}
				}
			}
			BAL.removeDocumentSelection();
			return false;
		};

		// Create function for appliance of table seperator position
		var applyTableSeperatorPosition = function(event) {

			// Cleanup layout
			backgroundPane.parentNode.removeChild(backgroundPane);
			outermostDiv.removeChild(tableSeperator);
			BAL.removeDocumentSelection();

			// Remove event listeners
			BAL.removeEventListener(document, "mousemove", adjustTableSeperatorPosition);
			BAL.removeEventListener(document, "mouseup", applyTableSeperatorPosition);

			// Notify server
			if (currentFixedColumns != fixColumnCount) {
				services.ajax.execute("dispatchControlCommand", {
							controlCommand: "updateFixedColumnAmount",
							controlID: ctrlID,
							fixedColumnAmount: currentFixedColumns
						}, /* useWaitPane */true);
			}
			
			return false;
		};

		// Register table seperator adjustment and appliance functions
		BAL.addEventListener(document, "mousemove", adjustTableSeperatorPosition);
		BAL.addEventListener(document, "mouseup", applyTableSeperatorPosition);

		return false;
	},

	initPreviousScrollPosition: function(sliceManager, clientDisplayData) {
		var bodyScrollPositions = TABLE.ScrollPositionProvider.getServerScrollPositions(sliceManager, clientDisplayData);
		var tableID = sliceManager.getTableID();
		
		// Set initial scroll positions to table (including synchronization between table parts)
		if(TABLE.hasVerticalScollbarOuter(tableID)) {
			var verticalScrollOuter = TABLE.getVerticalScrollbarOuter(tableID);
			if(verticalScrollOuter.scrolledVertical != undefined &&
					verticalScrollOuter.scrolledVertical != null) {
				verticalScrollOuter.verticalScrollManager.initScrollPosition(bodyScrollPositions.vertical);
			}
		}
		
		if(TABLE.hasHorizontalScollbarOuter(tableID)) {
			var horizontalScrollOuter = TABLE.getHorizontalScrollbarOuter(tableID);
			if(horizontalScrollOuter.horizontalScrollManager != undefined &&
					horizontalScrollOuter.horizontalScrollManager != null) {
				horizontalScrollOuter.horizontalScrollManager.initScrollPosition(bodyScrollPositions.horizontal);
			}
		}
	},
	
	scrollToRequestedPosition: function(ctrlID, clientDisplayData) {
		var sliceManager = TABLE.tableSliceManagers[ctrlID];
		
		if(sliceManager != null) {
			var scrollbarScrollPositions = TABLE.ScrollPositionProvider.getComputedScrollPositions(sliceManager, clientDisplayData);
			if(TABLE.hasHorizontalScollbarOuter(ctrlID)) {
				var horizontalScrollOuter = TABLE.getHorizontalScrollbarOuter(ctrlID);
				if(horizontalScrollOuter.horizontalScrollManager != undefined &&
				   horizontalScrollOuter.horizontalScrollManager != null) {
					BAL.setScrollLeftElement(horizontalScrollOuter, scrollbarScrollPositions.horizontal);
					horizontalScrollOuter.horizontalScrollManager.scrollbarScrolled.call();
				}
			}
			
			if(TABLE.hasVerticalScollbarOuter(ctrlID)) {
				var verticalScrollOuter = TABLE.getVerticalScrollbarOuter(ctrlID);
				if(verticalScrollOuter.scrolledVertical != undefined &&
				   verticalScrollOuter.scrolledVertical != null) {
					BAL.setScrollTopElement(verticalScrollOuter, scrollbarScrollPositions.vertical);
					verticalScrollOuter.scrolledVertical.call();
				}
			}
			
			// Because the requested scroll position may not differ from the previous scroll
			// position, the check for additional slice loading must be triggered manually.
			sliceManager.positionUpdate(scrollbarScrollPositions.vertical);
		} else {
			services.log.error("Could not scroll to requested position, " +
					"due to slice manager for control with id '" + ctrlID + "' was not found!");
		}
	},
	
	activateScrollPositionPropagationInViewports: function(viewports) {
		viewports[viewports.length - 1].setUpdateServerState(true);
	},

	insertTableSlice: function(ctrlID, viewportID, sliceID, sliceRowCount) {
		var sliceManager = TABLE.tableSliceManagers[ctrlID];
		if ((sliceManager != undefined) && (sliceManager != null)) {
			sliceManager.insertSlice(viewportID, sliceID, sliceRowCount);
		} else {
			throw new Error("Client is not in sync with server. Reload page!");
		}
	},

	updateViewportHeight: function(ctrlID, sliceIDPart, pageRowCount) {
		var sliceManager = TABLE.tableSliceManagers[ctrlID];
		if ((sliceManager != undefined) && (sliceManager != null)) {
			sliceManager.setPageRange(0, pageRowCount - 1);
			
			// Update fix body container
			var fixViewportID = ctrlID + "_bodyFix" + TABLE.Viewport.CONTAINER_ID_SUFFIX;
			var fixSliceID = fixViewportID + "_slice-" + sliceIDPart;
			sliceManager.updateViewportHeight(fixViewportID, fixSliceID, pageRowCount);

			// Update flex body container
			var flexViewportID = ctrlID + "_bodyFlex" + TABLE.Viewport.CONTAINER_ID_SUFFIX;
			var flexSliceID = flexViewportID + "_slice-" + sliceIDPart;
			sliceManager.updateViewportHeight(flexViewportID, flexSliceID, pageRowCount);
		} else {
			throw new Error("Client is not in sync with server. Reload page!");
		}
	},

	getEffectiveElementSize: function(element, elementWidth, elementHeight) {
		var horizontalPaddingAndBorderSizes = BAL.getHorizontalPaddingAndBorderSizes(element);
		var verticalPaddingAndBorderSizes = BAL.getVerticalPaddingAndBorderSizes(element);
		var effectiveWidth = calculateEffectiveSize(elementWidth, horizontalPaddingAndBorderSizes);
		var effectiveHeight = calculateEffectiveSize(elementHeight, verticalPaddingAndBorderSizes);
		
		return {effectiveWidth: effectiveWidth, effectiveHeight: effectiveHeight};
	},
	
	getAvailableWidth: function(ctrlID) {
		var outermostDiv = TABLE.getFrozenTableDiv(ctrlID);
		return Math.max(0, BAL.getElementWidth(outermostDiv) - BAL.getVerticalScrollBarWidth());
	},
	
	getFixBodyID: function(ctrlID) {
		return ctrlID + "_bodyFix";
	},
	
	getFlexBodyID: function(ctrlID) {
		return ctrlID + "_bodyFlex";
	},
	
	getViewportIDForTablePartWithID: function(tablePartID) {
		return tablePartID + TABLE.Viewport.CONTAINER_ID_SUFFIX;
	},
	
	getViewportFromSliceManager: function(ctrlID, tablePartID) {
		var sliceManager = TABLE.tableSliceManagers[ctrlID];
		var viewportID = TABLE.getViewportIDForTablePartWithID(tablePartID);
		return sliceManager.getViewport(viewportID);
	},

	adjustScrollBars: function(ctrlID) {
		if(TABLE.hasVerticalScollbarOuter(ctrlID)) {
			var verticalScrollOuter = TABLE.getVerticalScrollbarOuter(ctrlID);
			var verticalScrollInner = verticalScrollOuter.children[0];
			var bodyTable = verticalScrollInner.bodyTable;
			if (bodyTable == null) {
				// no table so nothing to adjust
				return;
			};
			var sliceManager = TABLE.tableSliceManagers[ctrlID];
			var constantSummand = verticalScrollInner.constantSummand;
			var verticalScrollInnerHeight = constantSummand + sliceManager.getViewportContentHeight();
			BAL.setEffectiveHeight(verticalScrollInner, verticalScrollInnerHeight);
		}
	},
	
	getVerticalScrollbarOuter: function(ctrlID) {
		return document.getElementById(ctrlID + "_verticalScroll");
	},
	
	hasVerticalScollbarOuter: function(ctrlID) {
		return TABLE.getVerticalScrollbarOuter(ctrlID) != null;
	},

	getHorizontalScrollbarOuter: function(ctrlID) {
		return document.getElementById(ctrlID + "_horizontalScroll");
	},
	
	hasHorizontalScollbarOuter: function(ctrlID) {
		return TABLE.getHorizontalScrollbarOuter(ctrlID) != null;
	},
	
	getTableSeparatorWidth: function(ctrlID) {
		return TABLE.getFrozenTableDiv(ctrlID).tableSeparatorWidth;
	},

	setTableSeparatorWidth: function(ctrlID, width) {
		TABLE.getFrozenTableDiv(ctrlID).tableSeparatorWidth = width;
	},

	getFrozenTableDiv: function(ctrlID) {
		return document.getElementById(ctrlID + "_table");
	},
	
	focus: function(ctrlElement) {
		var offsetParent = ctrlElement.offsetParent;
		var topX = BAL.getElementX(ctrlElement);
		var topY = BAL.getElementY(ctrlElement);
		BAL.setScrollLeftElement(offsetParent, topX);
		/* Add a little margin to the scroll position. */
		BAL.setScrollTopElement(offsetParent, topY - 25);
	}
};

TABLE.VerticalScrollManager = function(tableControlID) {
	
	var self = this;
	
	this._timerID = null;
	this._ctrlID = tableControlID;
	this._scrollbar = null;
	this._fixBody = null;
	this._fixBodyViewport = null;
	this._flexBody = null;
	this._flexBodyViewport = null;
	
	this.initScrollPosition = function(initialBodyScrollPosition) {
		if (self._scrollbar != null) {
			BAL.setScrollTopElement(self._scrollbar, initialBodyScrollPosition);
		}
		if (self._fixBodyViewport != null) {
			BAL.setScrollTopElement(self._fixBody, initialBodyScrollPosition);
			self._fixBodyViewport.setVerticalScrollingPosition(initialBodyScrollPosition);
		}
		if (self._flexBodyViewport != null) {
			BAL.setScrollTopElement(self._flexBody, initialBodyScrollPosition);
			self._flexBodyViewport.setVerticalScrollingPosition(initialBodyScrollPosition);
		}
	};
	
	this.scrollbarScrolled = function() {
		self.cancelScrollbarAnimation();
		self.scrollbarAnimationId = BAL.requestAnimationFrame(function() {
			TABLE.executeInstantly(removeFixBodyListener, removeFlexBodyListener);
			var scrollbarScrollPosition = BAL.getScrollTopElement(self._scrollbar);
			if(hasScrollPositionChanged(scrollbarScrollPosition)) {
				if (self._fixBodyViewport != null) {
					BAL.setScrollTopElement(self._fixBody, scrollbarScrollPosition);
					self._fixBodyViewport.setVerticalScrollingPosition(scrollbarScrollPosition);
				}
				if (self._flexBodyViewport != null) {
					BAL.setScrollTopElement(self._flexBody, scrollbarScrollPosition);
					self._flexBodyViewport.setVerticalScrollingPosition(scrollbarScrollPosition);
				}
				performSliceManagerUpdate(scrollbarScrollPosition);
				self.scrollbarAnimationId = null;
			}
			TABLE.executeAfterRepaint(addFixBodyListener, addFlexBodyListener);
		});
	};
	
	this.cancelAllAnimations = function() {
		self.cancelScrollbarAnimation();
		self.cancelFixBodyAnimation();
		self.cancelFlexBodyAnimation();
	};
	
	this.cancelScrollbarAnimation = function() {
		if (self.scrollbarAnimationId != null) {
			BAL.cancelAnimationFrame(self.scrollbarAnimationId);
		}
	};
	
	this.fixBodyScrolled = function() {
		self.cancelFixBodyAnimation();
		self.fixBodyAnimationId = BAL.requestAnimationFrame(function() {
			TABLE.executeInstantly(removeScrollbarListener, removeFlexBodyListener);
			tableScrolled(self._fixBody, self._fixBodyViewport, self._flexBody, self._flexBodyViewport);
			self.fixBodyAnimationId = null;
			TABLE.executeAfterRepaint(addScrollbarListener, addFlexBodyListener);
		});
	};
	
	this.cancelFixBodyAnimation = function() {
		if (self.fixBodyAnimationId != null) {
			BAL.cancelAnimationFrame(self.fixBodyAnimationId);
		}
	};
	
	function tableScrolled(tableTriggerPart, tableTriggerPartViewport, tableAppliancePart, tableAppliancePartViewport) {
		var tableScrollPosition = BAL.getScrollTopElement(tableTriggerPart);
		if(hasScrollPositionChanged(tableScrollPosition)) {
			BAL.setScrollTopElement(self._scrollbar, tableScrollPosition);
			if(tableAppliancePartViewport != null) {
				BAL.setScrollTopElement(tableAppliancePart, tableScrollPosition);
				tableTriggerPartViewport.setVerticalScrollingPosition(tableScrollPosition);
				tableAppliancePartViewport.setVerticalScrollingPosition(tableScrollPosition);
			}
			performSliceManagerUpdate(tableScrollPosition);
		}
	}
	
	this.flexBodyScrolled = function() {
		self.cancelFlexBodyAnimation();
		self.flexBodyAnimationId = BAL.requestAnimationFrame(function() {
			TABLE.executeInstantly(removeScrollbarListener, removeFixBodyListener);
			tableScrolled(self._flexBody, self._flexBodyViewport, self._fixBody, self._fixBodyViewport);
			self.flexBodyAnimationId = null;
			TABLE.executeAfterRepaint(addScrollbarListener, addFixBodyListener);
		});
	};
	
	this.cancelFlexBodyAnimation = function() {
		if (self.flexBodyAnimationId != null) {
			BAL.cancelAnimationFrame(self.flexBodyAnimationId);
		}
	};
	
	function hasScrollPositionChanged(currentScrollPosition) {
		var referenceViewport = getReferenceViewport();
		return referenceViewport != null && 
		currentScrollPosition != referenceViewport.getVerticalScrollingPosition();
	}
	
	function getReferenceViewport() {
		return self._fixBodyViewport != null ? self._fixBodyViewport : self._flexBodyViewport;
	}
	
	function performSliceManagerUpdate(currentScrollPosition) {
		var sliceManager = TABLE.tableSliceManagers[self._ctrlID];
		
		// Informs slice manager about position update
		if (self._timerID != null) {
			BAL.getTopLevelWindow().clearTimeout(self._timerID);
		}
		self._timerID = BAL.getTopLevelWindow().setTimeout(function() {
			sliceManager.positionUpdate(currentScrollPosition);
		}, 100);
	}
	
	function removeScrollbarListener() {
		TABLE.removeScrollListener(self._scrollbar, self.scrollbarScrolled);
	}
	
	function removeFlexBodyListener() {
		TABLE.removeScrollListener(self._flexBody, self.flexBodyScrolled);
	}
	
	function removeFixBodyListener() {
		TABLE.removeScrollListener(self._fixBody, self.fixBodyScrolled);
	}
	
	function addScrollbarListener() {
		TABLE.addScrollListener(self._scrollbar, self.scrollbarScrolled);
	}
	
	function addFlexBodyListener() {
		TABLE.addScrollListener(self._flexBody, self.flexBodyScrolled);
	}
	
	function addFixBodyListener() {
		TABLE.addScrollListener(self._fixBody, self.fixBodyScrolled);
	}
	
	this.setScrollbar = function(scrollbar) {
		removeScrollbarListener();
		self._scrollbar = scrollbar;
		addScrollbarListener();
	};
	
	this.setFixBody = function(fixBody) {
		removeFixBodyListener();
		self._fixBody = fixBody;
		addFixBodyListener();
	};
	
	this.setFixBodyViewport = function(fixBodyViewport) {
		self._fixBodyViewport = fixBodyViewport;
	};
	
	this.setFlexBody = function(flexBody) {
		removeFlexBodyListener();
		self._flexBody = flexBody;
		addFlexBodyListener();
	};
	
	this.setFlexBodyViewport = function(flexBodyViewport) {
		self._flexBodyViewport = flexBodyViewport;
	};
};

TABLE.HorizontalScrollManager = function(availableWidth, flexWidth) {

	var self = this;
	
	this._availableWidth = availableWidth;
	this._flexWidth = flexWidth;
	this._scrollbar = null;
	this._flexHeader = null;
	this._flexBody = null;
	this._flexBodyViewport = null;
	
	this.initScrollPosition = function(initialBodyScrollPosition) {
		if(self._scrollbar != null && self._flexHeader != null && self._flexBody != null) {
			var scrollbarScrollPosition = Math.round(initialBodyScrollPosition * (self._availableWidth / self._flexWidth));
			BAL.setScrollLeftElement(self._scrollbar, scrollbarScrollPosition);
			BAL.setScrollLeftElement(self._flexHeader, initialBodyScrollPosition);
			BAL.setScrollLeftElement(self._flexBody, initialBodyScrollPosition);
			self._flexBodyViewport.setHorizontalScrollingPosition(initialBodyScrollPosition);
		}
	};

	this.cancelAllAnimations = function() {
		self.cancelScrollbarAnimation();
		self.cancelBodyScrolledAnimation();
		self.cancelHeaderScrolledAnimation();
	};

	this.scrollbarScrolled = function() {
		self.cancelScrollbarAnimation();
		self.scrollbarAnimationId = BAL.requestAnimationFrame(function() {
			TABLE.executeInstantly(removeFlexHeaderListener, removeFlexBodyListener);
			var scrollbarScrollPosition = BAL.getScrollLeftElement(self._scrollbar);
			var tableScrollPosition = Math.round(scrollbarScrollPosition / (self._availableWidth / self._flexWidth));
			if(hasScrollPositionChanged(tableScrollPosition)) {
				// needed: availableFlexScroll/outerScrollbarDivScroll == availableFlexSize/outerScrollbarDivSize
				BAL.setScrollLeftElement(self._flexHeader, tableScrollPosition);
				BAL.setScrollLeftElement(self._flexBody, tableScrollPosition);
				self._flexBodyViewport.setHorizontalScrollingPosition(tableScrollPosition);
				self.scrollbarAnimationId = null;
			}
			TABLE.executeAfterRepaint(addFlexHeaderListener, addFlexBodyListener);
		});
	};
	
	this.cancelScrollbarAnimation = function() {
		if (self.scrollbarAnimationId != null) {
			BAL.cancelAnimationFrame(self.scrollbarAnimationId);
		}
	};

	this.headerScrolled = function() {
		self.cancelHeaderScrolledAnimation();
		self.headerAnimationId = BAL.requestAnimationFrame(function() {
			TABLE.executeInstantly(removeScrollbarListener, removeFlexBodyListener);
			tableScrolled(self._flexHeader, self._flexBody);
			self.headerAnimationId = null;
			TABLE.executeAfterRepaint(addScrollbarListener, addFlexBodyListener);
		});
	};
	
	this.cancelHeaderScrolledAnimation = function() {
		if (self.headerAnimationId != null) {
			BAL.cancelAnimationFrame(self.headerAnimationId);
		}
	};

	function tableScrolled(scrollTriggerTablePart, scrollApplianceTablePart) {
		var tableScrollPosition = BAL.getScrollLeftElement(scrollTriggerTablePart);
		var scrollbarScrollPosition = Math.round(tableScrollPosition * (self._availableWidth / self._flexWidth));
		if(hasScrollPositionChanged(tableScrollPosition)) {
			// needed: availableFlexScroll/outerScrollbarDivScroll == availableFlexSize/outerScrollbarDivSize
			BAL.setScrollLeftElement(self._scrollbar, scrollbarScrollPosition);
			BAL.setScrollLeftElement(scrollApplianceTablePart, tableScrollPosition);
			self._flexBodyViewport.setHorizontalScrollingPosition(tableScrollPosition);
		}
	}
	
	function hasScrollPositionChanged(currentScrollPosition) {
		return self._flexBodyViewport != null && currentScrollPosition != self._flexBodyViewport.getHorizontalScrollingPosition();
	}
	
	this.bodyScrolled = function() {
		self.cancelBodyScrolledAnimation();
		self.bodyAnimationId = BAL.requestAnimationFrame(function() {
			TABLE.executeInstantly(removeScrollbarListener, removeFlexHeaderListener);
			tableScrolled(self._flexBody, self._flexHeader);
			self.bodyAnimationId = null;
			TABLE.executeAfterRepaint(addScrollbarListener, addFlexHeaderListener);
		});
	};
	
	this.cancelBodyScrolledAnimation = function() {
		if (self.bodyAnimationId != null) {
			BAL.cancelAnimationFrame(self.bodyAnimationId);
		}
	};

	this.setFlexTableWidth = function(flexWidth) {
		self._flexWidth = flexWidth;
	};

	this.setAvailableWidth = function(availableWidth) {
		self._availableWidth = availableWidth;
	};
	
	function removeScrollbarListener() {
		TABLE.removeScrollListener(self._scrollbar, self.scrollbarScrolled);
	}
	
	function removeFlexBodyListener() {
		TABLE.removeScrollListener(self._flexBody, self.bodyScrolled);
	}
	
	function removeFlexHeaderListener() {
		TABLE.removeScrollListener(self._flexHeader, self.headerScrolled);
	}
	
	function addScrollbarListener(element, listenerFunction) {
		TABLE.addScrollListener(self._scrollbar, self.scrollbarScrolled);
	}
	
	function addFlexBodyListener() {
		TABLE.addScrollListener(self._flexBody, self.bodyScrolled);
	}
	
	function addFlexHeaderListener() {
		TABLE.addScrollListener(self._flexHeader, self.headerScrolled);
	}
	
	this.setScrollbar = function(scrollbar) {
		removeScrollbarListener()
		self._scrollbar = scrollbar;
		addScrollbarListener()
	};
	
	this.setFlexHeader = function(flexHeader) {
		removeFlexHeaderListener();
		self._flexHeader = flexHeader;
		addFlexHeaderListener();
	};
	
	this.setFlexBody = function(flexBody) {
		removeFlexBodyListener();
		self._flexBody = flexBody;
		addFlexBodyListener();
	};
	
	this.setFlexBodyViewport = function(flexBodyViewport) {
		self._flexBodyViewport = flexBodyViewport;
	};
};

/**
 * A container class for slice informations
 */
TABLE.Slice = function(sliceID, insertPosition, insertStrategy, firstRow,
	lastRow, rowHeight) {

	/*
	 * Initialization of member variables
	 */
	this._sliceID = sliceID;
	this._insertPosition = insertPosition;
	this._insertStrategy = insertStrategy;
	this._firstRow = firstRow;
	this._lastRow = lastRow;
	
	this._displayed = false;
	this._rowHeight = rowHeight;
};

/**
 * The slice id (the div/@id attribute of the slice element).
 */
TABLE.Slice.prototype.getID = function() {
	return this._sliceID;
};

/**
 * Vertical position in pixels fo the slice.
 */
TABLE.Slice.prototype.getInsertPosition = function() {
	return this._insertPosition;
};

TABLE.Slice.prototype.noIntersection = function(start, stop) {
	return this.getUpperBound() >= stop || this.getLowerBound() <= start;
};

/**
 * Retrieves the slice bound with higher pixel offset value
 * 
 */
TABLE.Slice.prototype.getLowerBound = function() {
	if (this.getInsertStrategy() == TABLE.BELOW_STRATEGY) {
		return this.getInsertPosition() + this.getHeight();
	} else {
		return this.getInsertPosition();
	}
};

/**
 * Retrieves the slice bound with lower pixel offset value
 */
TABLE.Slice.prototype.getUpperBound = function() {
	if (this.getInsertStrategy() == TABLE.BELOW_STRATEGY) {
		return this.getInsertPosition();
	} else {
		return this.getInsertPosition() - this.getHeight();
	}
};

/**
 * Where to insert a requested slice relative to the {_insertPosition}.
 * 
 * <p>
 * Possible values are {TABLE.ABOVE_STRATEGY} and {TABLE.BELOW_STRATEGY}.
 * </p>
 */
TABLE.Slice.prototype.getInsertStrategy = function() {
	return this._insertStrategy;
};

/**
 * DOM element of the slice.
 */
TABLE.Slice.prototype.getDOMElement = function() {
	return document.getElementById(this._sliceID);
};

/**
 * Getter for the slice height
 */
TABLE.Slice.prototype.getHeight = function() {
	return this._rowHeight * this.getRowCount();
};

/**
 * Sever-side row number of the first row in this slice
 */
TABLE.Slice.prototype.getFirstRow = function() {
	return this._firstRow;
};

TABLE.Slice.prototype.setFirstRow = function(firstRow) {
	this._firstRow = firstRow;
};

/**
 * Sever-side row number of the last row in this slice
 */
TABLE.Slice.prototype.getLastRow = function() {
	return this._lastRow;
};

TABLE.Slice.prototype.setLastRow = function(lastRow) {
	this._lastRow = lastRow;
};

/**
 * Numer of rows in this slice.
 */
TABLE.Slice.prototype.getRowCount = function() {
	return this._lastRow - this._firstRow + 1;
};

TABLE.Slice.prototype.setWidth = function(viewportWidth) {
	BAL.setEffectiveWidth(this.getDOMElement(), viewportWidth);
};

TABLE.Slice.prototype.remove = function() {
	var sliceElement = this.getDOMElement();
	sliceElement.parentNode.removeChild(sliceElement);
};

/**
 * A container structure for viewport informations.
 * 
 * <p>
 * Ein Viewport ist ein scorllbarer Inhaltsbereich in der Tablelle. 
 * I.d.R. gibt es davon zwei, einen im fixierten Tabellenteil der nur vertikal 
 * gescrollt werden kann und einen im flexiblen Teil, der sowohl horizontal als 
 * auch vertikal gescrollt werden kann.
 * </p>
 * 
 * <p>
 * Ein Viewport-Element enth�lt eine zusammenh�ngende Menge von Slices. Ein Slice 
 * ist repr�sentiert durch Strukturen {TABLE.Slice} im {TABLE.SliceManager}. 
 * </p>
 * 
 * @param containerID {String} 
 *			  div/@id of element holding the slice divs.
 * @param columnWidths {Array}
 *            the widths of the columns in the view port
 * @param columnIDOffset {Number}
 *            the number of columns to add to compute the servcer
 *            side id of a column, e.g. if the Viewport is a flex one then the
 *            number of fixed columns must be added to get the "logical" column
 *            number.
 */
TABLE.Viewport = function(tableID, tableDisplayVersion, containerID, viewportWidth, columnWidths, columnIDOffset) {

	/*
	 * Initialization of member variables
	 */
	this._tableID = tableID;
	this._tableDisplayVersion = tableDisplayVersion;
	this._containerID = containerID;
	this.initWidth(viewportWidth);
	this._horizontalScrollingPosition = 0;
	this._verticalScrollingPosition = 0;
	this.setColumnWidths(columnWidths);
	this._columnIDOffset = columnIDOffset;
	this._shallUpdateServerState = false;
	this._lazyRequestID = services.ajax.createLazyRequestID();
	this._slices = new Array();
};

TABLE.Viewport.CONTAINER_ID_SUFFIX = "_viewport";

TABLE.Viewport.prototype.appendSlice = function(slice) {
	this._slices.push(slice);
};

TABLE.Viewport.prototype.prependSlice = function(slice) {
	this._slices.splice(0,0, slice);
};

TABLE.Viewport.prototype.setUpdateServerState = function(shallUpdateServerState) {
	this._shallUpdateServerState = shallUpdateServerState;
};

/**
 * Getter for the viewport container id
 */
TABLE.Viewport.prototype.getContainerID = function() {
	return this._containerID;
};

/**
 * Getter for the viewport width (sum width of all columns)
 */
TABLE.Viewport.prototype.getWidth = function() {
	return this._width;
};

/**
 * Called at frozen table creation / lazifying setting
 */
TABLE.Viewport.prototype.initWidth = function(viewportWidth) {
	this._width = viewportWidth;
	var effectiveWidth = viewportWidth;
	var viewport = document.getElementById(this.getContainerID());
	var layoutFunction = function(viewport, effectiveWidth) {
		return function() {
			BAL.setEffectiveWidth(viewport, effectiveWidth);
			viewport = null;
			effectiveWidth = null;
		};
	}(viewport, effectiveWidth);
	services.layout.addLayoutFunction(layoutFunction);
};

/**
 * Setter for the viewport width (sum width of all columns)
 */
TABLE.Viewport.prototype.setWidth = function(viewportWidth) {
	this._width = viewportWidth;
	var viewport = document.getElementById(this.getContainerID());
	BAL.setElementWidth(viewport, viewportWidth);
};

/**
 * @returns viewport width, which can be seen on screen
 */
TABLE.Viewport.prototype.getVisibleWidth = function() {
	return BAL.getElementWidth(this.getContentElement().parentNode);
};

/**
 * Called at frozen table creation / lazifying setting
 */
TABLE.Viewport.prototype.initHeight = function(viewportHeight) {
	var effectiveHeight = viewportHeight;
	var viewport = document.getElementById(this.getContainerID());
	var layoutFunction = function(viewport, effectiveHeight) {
		return function() {
			BAL.setEffectiveHeight(viewport, effectiveHeight);
			viewport = null;
			effectiveHeight = null;
		};
	}(viewport, effectiveHeight);
	services.layout.addLayoutFunction(layoutFunction);
};

/**
 * Setter for the viewport height
 */
TABLE.Viewport.prototype.setHeight = function(viewportHeight) {
	var viewport = document.getElementById(this.getContainerID());
	BAL.setElementHeight(viewport, viewportHeight);
};

/**
 * Getter for the offset of the column ids
 */
TABLE.Viewport.prototype.getColumnIDOffset = function() {
	return this._columnIDOffset;
};

/**
 * Getter for column count
 */
TABLE.Viewport.prototype.getColumnCount = function() {
	return this._columnWidths.length;
};

/**
 * Getter for a column width
 */
TABLE.Viewport.prototype.getColumnWidth = function(column) {
	if (typeof(this._columnWidths[column]) != "undefined") {
		return this._columnWidths[column];
	} else {
		throw new Error("Could not retrieve column width due to invalid column index!");
	}
};

/**
 * Setter for a column width
 */
TABLE.Viewport.prototype.setColumnWidth = function(column, columnWidth) {
	if (typeof(this._columnWidths[column]) != "undefined") {
		this._columnWidths[column] = columnWidth;
	} else {
		throw new Error("Could not set column width due to invalid column index!");
	}
};

/**
 * Setter for the column widths
 */
TABLE.Viewport.prototype.setColumnWidths = function(/* Array */columnWidths) {
	this._columnWidths = columnWidths;
};

/**
 * Getter for the viewport html reference
 */
TABLE.Viewport.prototype.getContentElement = function() {
	return document.getElementById(this._containerID);
};

/**
 * Getter for the viewport horizontal scrolling position
 */
TABLE.Viewport.prototype.getHorizontalScrollingPosition = function() {
	return this._horizontalScrollingPosition;
};

/**
 * Setter for the viewport horizontal scrolling position
 */
TABLE.Viewport.prototype.setHorizontalScrollingPosition = function(
		horizontalScrollingPosition) {
	if (this._horizontalScrollingPosition != horizontalScrollingPosition) {
		this._horizontalScrollingPosition = horizontalScrollingPosition;
		if(this._shallUpdateServerState) {
			this.propagateScrollPositionToServer();
		}
	}
};

/**
 * Getter for the viewport vertical scrolling position
 */
TABLE.Viewport.prototype.getVerticalScrollingPosition = function() {
	return this._verticalScrollingPosition;
};

/**
 * Setter for the viewport vertical scrolling position
 */
TABLE.Viewport.prototype.setVerticalScrollingPosition = function(
		verticalScrollingPosition) {
	if (this._verticalScrollingPosition != verticalScrollingPosition) {
		this._verticalScrollingPosition = verticalScrollingPosition;
		if(this._shallUpdateServerState) {
			this.propagateScrollPositionToServer();
		}
	}
};

TABLE.Viewport.prototype.getSliceManager = function() {
	return TABLE.tableSliceManagers[this.getTableID()];
};

TABLE.Viewport.prototype.propagateScrollPositionToServer = function() {
	var sliceManager = this.getSliceManager();
	if (sliceManager == null) {
		/* There is no SliceManager any more. This can happen in rare cases when the the table has already been 
		 * removed from the GUI and a function with timeout was executed too late. */
		return;
	}
	var position = TABLE.VisiblePositionCalculator.getPosition(this, sliceManager);
	services.ajax.executeOrUpdateLazy(this._lazyRequestID, "dispatchControlCommand", {
		controlCommand: "updateScrollPosition",
		controlID: this._tableID,
		displayVersion: this._tableDisplayVersion,
		columnAnchor: position.horizontal.columnAnchor,
		columnAnchorOffset: position.horizontal.columnAnchorOffset,
		rowAnchor: position.vertical.rowAnchor,
		rowAnchorOffset: position.vertical.rowAnchorOffset
	});
};

TABLE.Viewport.prototype.getTableID = function() {
	return this._tableID;
};

/**
 * A class to manage table slices
 * 
 * @param {Array}
 *            viewports a list of managed viewports {TABLE.Viewport}
 */
TABLE.SliceManager = function(tableControlID, tableDisplayVersion, viewportHeight, firstRow, 
        lastRow, viewports, firstSliceRow, lastSliceRow) {

	/*
	 * Initialization of member variables
	 */
	this._tableControlID = tableControlID;
	this._tableDisplayVersion = tableDisplayVersion;
	this._visibleViewportHeight = viewportHeight;
	this._pageStart = firstRow;
	this._pageEnd = lastRow;
	this._viewports = viewports;
	this._viewportsById = new Object();
	
	for (var n = 0; n < viewports.length; n++) {
		var viewport = viewports[n];
		this._viewportsById[viewport.getContainerID()] = viewport;
	}
	
	this._rowHeight = 0; // adjusted in init function
	
	this._viewportContentHeight = 0; // adjusted in init function
	
	/* Note: The initial slice gets its ID by the server. */
	this._nextId = this.INITIAL_SLICE_ID + 1;

	this._init(firstSliceRow, lastSliceRow);
};

TABLE.SliceManager.prototype.getTableID = function() {
	return this._tableControlID;
};

TABLE.SliceManager.prototype.getFirstPageRow = function() {
	return this._pageStart;
};

TABLE.SliceManager.prototype.getLastPageRow = function() {
	return this._pageEnd;
};

/** Vertical start position of the current page. */
TABLE.SliceManager.prototype.getPageStartPosition = function() {
	return 0;
};

/** Vertical end position of the current page. */
TABLE.SliceManager.prototype.getPageEndPosition = function() {
	return this.getPageRowCount() * this.getRowHeight();
};

/** Vertical start position of the first slice of the requested range. */
TABLE.SliceManager.prototype.getRangeStartPosition = function() {
	var sliceCount = this._viewports[0]._slices.length;
	if (sliceCount > 0) {
		return this._viewports[0]._slices[0].getUpperBound();
	} else {
		return 0;
	}
};

/** Vertical end position of the last slice of the requested range. */
TABLE.SliceManager.prototype.getRangeStopPosition = function() {
	var sliceCount = this._viewports[0]._slices.length;
	if (sliceCount > 0) {
		return this._viewports[0]._slices[sliceCount - 1].getLowerBound();
	} else {
		return -1;
	}
};

TABLE.SliceManager.prototype.getRangeStartRow = function() {
	var sliceCount = this._viewports[0]._slices.length;
	if (sliceCount > 0) {
		return this._viewports[0]._slices[0].getFirstRow();
	} else {
		return 0;
	}
};

TABLE.SliceManager.prototype.getRangeLastRow = function() {
	var sliceCount = this._viewports[0]._slices.length;
	if (sliceCount > 0) {
		return this._viewports[0]._slices[sliceCount - 1].getLastRow();
	} else {
		return -1;
	}
};


TABLE.ABOVE_STRATEGY = "above";
TABLE.BELOW_STRATEGY = "below";

/*
 * Declaration of privileged methods
 */

/**
 * Getter for the height of the viewport container
 */
TABLE.SliceManager.prototype.getViewportContentHeight = function() {
	return this._viewportContentHeight;
};

TABLE.SliceManager.prototype.newId = function() {
	return this._nextId++;
};

/**
 * Getter for a viewport container
 */
TABLE.SliceManager.prototype.getViewport = function(containerID) {
	return this._viewportsById[containerID];
};

/**
 * Setter for the visible viewport height
 */
TABLE.SliceManager.prototype.setVisibleViewportHeight = function(viewportHeight) {
	// Viewport height must be at least 1 pixel
	this._visibleViewportHeight = Math.max(viewportHeight, 1);

	// Check, if slice updates are necessary
	for (var id in this._viewports) {
        var verticalScrollPosition = this._viewports[id].getVerticalScrollingPosition();
        break;
	}
	if (typeof(verticalScrollPosition) == undefined) {
		throw new Error("No viewPorts found");
	}
	
	this.positionUpdate(verticalScrollPosition);
};

/**
 * Getter for the visible viewport height
 */
TABLE.SliceManager.prototype.getVisibleViewportHeight = function() {
	return this._visibleViewportHeight;
};

/**
 * Update the viewport container height
 */
TABLE.SliceManager.prototype.updateViewportHeight = function(
		viewportID, sliceID, pageRowCount) {
	// Adjusting viewport container height
    var viewport = this.getViewport(viewportID);
	if (viewport == null) {
		return;
	}
	
	if(this.hasSlices()) {
		viewport._slices[0].setFirstRow(0);
		viewport._slices[0].setLastRow(pageRowCount - 1);
	}
	this._adjustViewportContentHeight(viewport, sliceID, pageRowCount, false);
	TABLE.adjustScrollBars(this._tableControlID);
};

TABLE.SliceManager.prototype.getFixedSlices = function() {
	return this._viewports[0]._slices;
};

/**
 * Update the width of a table column
 */
TABLE.SliceManager.prototype.updateColumnWidth = function(viewportID,
		columnID, columnWidth, updateServerColumn) {

	// Update viewport container data
	var viewport = this.getViewport(viewportID);
	var deltaWidth = columnWidth - viewport.getColumnWidth(columnID);
	viewport.setColumnWidth(columnID, columnWidth);
	viewport.setWidth(viewport.getWidth() + deltaWidth);

	// Update table columns
	var slices = viewport._slices;
	for (var i = 0; i < slices.length; i++) {
		var currentSlice = slices[i];
		if (!currentSlice._displayed) {
			continue;
		}
		
		// Adjust slice width
		var sliceFragment = currentSlice.getDOMElement();
		var sliceTableFragment = sliceFragment.getElementsByTagName("table")[0];
		currentSlice.setWidth(viewport.getWidth());
		
		// Adjust column width
		if (sliceTableFragment != undefined) {
			var tableBody = sliceTableFragment.getElementsByTagName("tbody")[0];
			var tableRows = tableBody.getElementsByTagName("tr");
			var columnDIV = null;
			for (var rowIndex = 0; (rowIndex < tableRows.length); rowIndex++) {
				var tableColumns = tableRows[rowIndex].getElementsByTagName("td");
				columnDIV = tableColumns[columnID].getElementsByTagName("div")[0];
				TABLE.setColumnWidth(columnDIV, viewport.getColumnWidth(columnID));
				
				// Quirks, to force IE6 and IE7 to set the new column width
				// directly at td-tag, due to these IE versions do not
				// perform column adjustment according to the width of
				// child elements
				TABLE.setColumnWidth(tableColumns[columnID], viewport.getColumnWidth(columnID));
			}
		}
	}
	
	// Update server-side column width
	if (updateServerColumn) {
		var serverColumnID = viewport.getColumnIDOffset() + columnID;
		
		services.ajax.execute("dispatchControlCommand", {
			controlCommand: "updateColumnWidth",
			controlID: this._tableControlID,
			columnID: serverColumnID,
			newColumnWidth: columnWidth
		}, /* useWaitPane */false);
	}
};

/**
 * Inserts a slice into a viewport container
 */
TABLE.SliceManager.prototype.insertSlice = function(viewportID, sliceID, sliceRowCount) {
	if(TABLE.doLog) {
		this._startProfiling("insertSlice");
	}
	
	var viewport = this.getViewport(viewportID);
	
	// Retrieve slice from request array
	var slice = null;
	var slices = viewport._slices;
	for (var i = 0; i < slices.length; i++) {
		if (slices[i].getID() == sliceID) {
			slice = slices[i];
			break;
		}
	}

	var sliceElement = document.getElementById(sliceID);
	if (slice == null) {
		// This slice is already requested to be deleted. 
		// Drop it from DOM.
		sliceElement.parentNode.removeChild(sliceElement);
		
		if(TABLE.doLog) {
			this._reportState("Received slice '" + sliceID + "' that is already marked as deleted.");
		}
		return;
	}


	if(TABLE.doLog) {
		this._startProfiling("slicePositionSynchronization");
	}

	slice._displayed = true;
	
	var sliceHeight = slice.getHeight();
	
	if(TABLE.doLog) {
		this._endProfiling("slicePositionSynchronization");
		this._startProfiling("slicePositioning");
	}

	if (slice.getInsertStrategy() == TABLE.BELOW_STRATEGY) {

		// Insert new slice after last visible slice
		BAL.setElementY(sliceElement, slice.getInsertPosition());
		BAL.setElementX(sliceElement, 0);
	} else {

		// Insert new slice before first visible slice
		BAL.setElementY(sliceElement, slice.getInsertPosition() - sliceHeight);
		BAL.setElementX(sliceElement, 0);
	}
	
	if(TABLE.doLog) {
		this._endProfiling("slicePositioning");
	}
	
	var viewport = this.getViewport(viewportID);
	// Set slice width in every viewport
	if (viewport != null) {
		
		// Workaround for IE erasing the scroll information of empty divs:
		// Set horizontal scrolling for flex viewport again.
		var contentElement = viewport.getContentElement().parentNode;
		contentElement.scrollLeft = viewport.getHorizontalScrollingPosition();
	} else {
		throw new Error("Received table slice was not found in request queue!");
	}
	
	if(TABLE.doLog) {
		this._endProfiling("insertSlice");
		this._reportState("Insert slice!", ["insertSlice", "slicePositionSynchronization",
		                                    "slicePositioning"]);
	}
};

/**
 * Informs the slice manager about scroll position updates, to request new
 * slices from server on demand, or to drop unused slices.
 */
TABLE.SliceManager.prototype.positionUpdate = function(scrollPosition) {
	if(TABLE.doLog) {
		this._startProfiling("positionUpdate");
	}
	
	// Check if table is empty
	if(this.getPageEndPosition() > 0) {
		
		var viewportStart = scrollPosition;
		var viewportStop = scrollPosition + this._visibleViewportHeight;
		
		var keepRangeStart = Math.max(this.getPageStartPosition(), viewportStart - this.getKeepLimit());
		var keepRangeStop = Math.min(this.getPageEndPosition(), viewportStop + this.getKeepLimit());
		
		/* Determine slices, which can be dismissed */
		var dismissedSlices = new Array();
		var dismissedBoundaries = new Array();
		for (var n = 0; n < this._viewports.length; n++) {
			var viewport = this._viewports[n];
			
			var slices = viewport._slices;
			for (var i = slices.length - 1; i >= 0; i--) {
				var slice = slices[i];
				
				if (slice.noIntersection(keepRangeStart, keepRangeStop)) {
					// Drop slice.
					slices.splice(i, 1);
					dismissedSlices.push(slice);
					
					if (n == 0) {
						dismissedBoundaries.push(slice.getLastRow());
					}
				}
			}
		}
		
		// Update current range after dropping outdated slices.
		var rangeStart = this.getRangeStartPosition();
		var rangeStop = this.getRangeStopPosition();
		
		var requestRangeStart = Math.max(this.getPageStartPosition(), viewportStart - this.getRequestOffset());
		var requestRangeStop = Math.min(this.getPageEndPosition(), viewportStop + this.getRequestOffset());
		
		if(TABLE.doLog) {
			this._startProfiling("_requestSlice");
		}
		var requestSent = false;
		if (this.hasSlices()) {
			var triggerRangeStart = Math.max(this.getPageStartPosition(), viewportStart - this.getTriggerLimit());
			var triggerRangeStop = Math.min(this.getPageEndPosition(), viewportStop + this.getTriggerLimit());
			
			if (rangeStart > triggerRangeStart) {
				// Request before current range.
				requestSent |= this._requestSlice(requestRangeStart, rangeStart, TABLE.ABOVE_STRATEGY, dismissedSlices, dismissedBoundaries);
			}
			if (rangeStop < triggerRangeStop) {
				// Request after current range.
				requestSent |= this._requestSlice(rangeStop, requestRangeStop, TABLE.BELOW_STRATEGY, dismissedSlices, dismissedBoundaries);
			}
		} else {
			// Request new range.
			requestSent |= this._requestSlice(requestRangeStart, requestRangeStop, TABLE.BELOW_STRATEGY, dismissedSlices, dismissedBoundaries);
		}
		
		if(TABLE.doLog) {
			this._endProfiling("_requestSlice");
		}
		
		if ((dismissedSlices.length > 0) && !requestSent) {
			var tableControlID = this._tableControlID;
			var tableDisplayVersion = this._tableDisplayVersion;
			var rangeStartRow = this.getRangeStartRow();
			var rangeLastRow = this.getRangeLastRow();
			
			this.blurCellFocus();
			
			// Propagate slice dismissal to server, even if there is no request for further slices
			var callback = function() {
				TABLE.dismissSlices(dismissedSlices);
			};
			var commandArguments = {
					controlCommand: "dismissSlice",
					controlID: tableControlID,
					displayVersion: tableDisplayVersion,
					dismissedBoundaries: dismissedBoundaries,
					displayFirstRow: rangeStartRow,
					displayLastRow: rangeLastRow
				};
			services.ajax.execute("dispatchControlCommand",
					services.ajax.addSystemCommandProperty(commandArguments),
					/* useWaitPane */false, callback);
		}
	} 
	
	
	if(TABLE.doLog) {
		this._endProfiling("positionUpdate");
		if(this.getRowHeight() > 0) {
			this._reportState("Slice request operation", ["positionUpdate", "_requestSlice"]);
		} else {
			this._reportState("Slice request operation", ["positionUpdate"]);
		}
	}
};

TABLE.SliceManager.prototype.blurCellFocus = function() {
	var activeElement = BAL.DOM.getCurrentActiveSimpleElement(BAL.getTopLevelDocument());
	if(activeElement != null) {
		var tableControlId = this._tableControlID;
		var fixBodyNode = document.getElementById(TABLE.getFixBodyID(tableControlId));
		var flexBodyNode = document.getElementById(TABLE.getFlexBodyID(tableControlId));
		var currentNode = activeElement;
		var isFocusInCell = false;
		while(currentNode != null) {
			if(currentNode == fixBodyNode || currentNode == flexBodyNode) {
				isFocusInCell = true;
				break;
			}
			currentNode = currentNode.parentNode;
		}
		if(isFocusInCell) {
			blurFocus(activeElement);
		}
	}
};

TABLE.dismissSlices = function(slicesToDismiss) {
	for (var n = 0; n < slicesToDismiss.length; n++) {
		var slice = slicesToDismiss[n];
		if (slice._displayed) {
			// Drop slice from DOM.
			slice.remove();
		} else {
			// Slice was not yet delivered by the server and therefore cannot be hidden. 
			// This slice will be disabled directly after it arrives.
		}
	}
};

/*
 * Declaration of private methods
 */

TABLE.SliceManager.prototype.INITIAL_SLICE_ID = 0;
TABLE.SliceManager.prototype.MAX_KEEP_PIXELS = 700;
TABLE.SliceManager.prototype.MIN_TRIGGER_PIXELS = 400;
TABLE.SliceManager.prototype.MAX_REQUEST_PIXELS = 700;

/**
 * Keep limit is two times the visible viewport height, but at most
 * TABLE.SliceManager.prototype.MAX_KEEP_PIXELS 
 */
TABLE.SliceManager.prototype.getKeepLimit = function() {
	return Math.min(this._visibleViewportHeight * 2, this.MAX_KEEP_PIXELS);
};

/**
 * Request trigger limit is at half of visible viewport height, but at least
 * TABLE.SliceManager.prototype.MIN_TRIGGER_PIXELS
 */
TABLE.SliceManager.prototype.getTriggerLimit = function() {
	return Math.max(this._visibleViewportHeight / 2, this.MIN_TRIGGER_PIXELS);
};

/**
 * Request offset is two times the visible viewport height, but at most
 * TABLE.SliceManager.prototype.MAX_REQUEST_PIXELS 
 */
TABLE.SliceManager.prototype.getRequestOffset = function() {
	return Math.min(this._visibleViewportHeight * 2, this.MAX_REQUEST_PIXELS);
};

TABLE.SliceManager.prototype.hasSlices = function() {
	return this._viewports[0]._slices.length > 0;
};

/**
 * Inits the slice manager at object creation
 */
TABLE.SliceManager.prototype._init = function(firstSliceRow, lastSliceRow) {
	for (var id in this._viewports) {
		var viewport = this._viewports[id];
		
		var sliceID = viewport.getContainerID() + "_slice-" + this.INITIAL_SLICE_ID;
		var sliceDomElement = document.getElementById(sliceID);
		var sliceRowCount = firstSliceRow > -1 ? (lastSliceRow - firstSliceRow + 1) : 0;
		this._adjustViewportContentHeight(viewport,	sliceID, sliceRowCount, true);
		var insertPosition = this._calculateInsertPosition(TABLE.BELOW_STRATEGY, firstSliceRow, lastSliceRow);
		var newSlice = new TABLE.Slice(sliceID, 
		          insertPosition, 
		          TABLE.BELOW_STRATEGY, 
		          firstSliceRow, 
		          lastSliceRow, 
		          this.getRowHeight());
		viewport.appendSlice(newSlice);
		newSlice._displayed = true;
		
//		********************* Setter ********************
//		*************************************************
		var layoutFunction = function(sliceDOMElement, position, currentViewport) {
			return function() {
				BAL.setElementY(sliceDOMElement, position);
				newSlice.setWidth(currentViewport.getWidth());
				sliceDOMElement = null;
				position = null;
				currentViewport = null;
			};
		}(sliceDomElement, insertPosition, viewport);
		services.layout.addLayoutFunction(layoutFunction);
//		***************************************************
//		***************************************************
	}
};

/**
 * Computes and adjusts the average row height
 */
TABLE.SliceManager.prototype._adjustRowHeight = function(sliceID, sliceRowCount) {
	var slice = document.getElementById(sliceID);
	var sliceRowHeight;
	if(sliceRowCount > 0) {
		var sliceTable = slice.getElementsByTagName("table")[0];
		var sliceTableBody = sliceTable.getElementsByTagName("tbody")[0];
		var sliceTableRows = BAL.DOM.getChildElements(sliceTableBody);
		var sliceTableRow = sliceTableRows[sliceTableRows.length - 1];
		sliceRowHeight = parseInt(TL.getTLAttribute(sliceTableRow, "row-height"));
	} else {
		sliceRowHeight = 0;
	}
	this._rowHeight = sliceRowHeight;
};

/**
 * @returns {Number} height of a single row within viewport
 */
TABLE.SliceManager.prototype.getRowHeight = function() {
	return this._rowHeight;
};

/**
 * Computes and adjusts the height of the viewport container
 * 
 * @param {TABLE.Viewport} viewPort the view port to adjust 
 */
TABLE.SliceManager.prototype._adjustViewportContentHeight = function(viewPort, sliceID, sliceRowCount, isInitPhase) {
	if(isInitPhase) {
		this._adjustRowHeight(sliceID, sliceRowCount);
	}
	this._viewportContentHeight = this.getRowHeight() * this.getPageRowCount();
	if(!isInitPhase) {
		viewPort.setHeight(this._viewportContentHeight);
	} else {
		viewPort.initHeight(this._viewportContentHeight);
	}
};

TABLE.SliceManager.prototype.setPageRange = function(firstPageRow, lastPageRow) {
	this._pageStart = firstPageRow;
	this._pageEnd = lastPageRow;
};

TABLE.SliceManager.prototype.getPageRowCount = function() {
	return this._pageEnd - this._pageStart + 1;
};

TABLE.SliceManager.prototype.getRowStartingBefore = function(position) {
	return this._pageStart + Math.floor(position / this.getRowHeight());
};

TABLE.SliceManager.prototype.getRowEndingAfter = function(position) {
	return Math.min(this._pageEnd, this._pageStart + Math.ceil(position / this.getRowHeight()));
};

/**
 * Requests a new slice from server
 */
TABLE.SliceManager.prototype._requestSlice = function(startPosition, stopPosition, 
		insertStrategy, /* Array */ dismissedSlices, /* Array */dismissedBoundaries) {
	
	var firstRow = this.getRangeStartRow();
	var lastRow = this.getRangeLastRow();

	var sliceFirstRow = this.getRowStartingBefore(startPosition);
	var sliceLastRow = this.getRowEndingAfter(stopPosition);
	
	var insertBelow = insertStrategy == TABLE.BELOW_STRATEGY;
	
	// Make sure, no overlapping rows are requested after rounding from pixels to rows.
	if (lastRow >= firstRow) {
		// Currently at least one row is displayed.
		if (insertBelow) {
			sliceFirstRow = Math.max(lastRow + 1, sliceFirstRow);
		} else {
			sliceLastRow = Math.min(firstRow - 1, sliceLastRow);
		}
	}

	if (sliceFirstRow > sliceLastRow) {
		return false;
	}
	
	var insertPosition = this._calculateInsertPosition(insertStrategy, sliceFirstRow, sliceLastRow);
	
	var sliceIDPart = this.newId();
	for (var id in this._viewports) {
		var viewport = this._viewports[id];
		var sliceID = viewport.getContainerID() + "_slice-" + sliceIDPart;
		var newSlice = new TABLE.Slice(sliceID, 
			insertPosition, 
			insertStrategy, 
			sliceFirstRow, 
			sliceLastRow, 
			this.getRowHeight());
		
		if (insertBelow) {
			viewport.appendSlice(newSlice);
		} else {
			viewport.prependSlice(newSlice);
		}
	}
	
	var tableControlID = this._tableControlID;
	var tableDisplayVersion = this._tableDisplayVersion;
	var rangeStartRow = this.getRangeStartRow();
	var rangeLastRow = this.getRangeLastRow();
	var callback = function() {
		TABLE.dismissSlices(dismissedSlices);
	};
	
	this.blurCellFocus();
	
	var commandArguments = {
			controlCommand: "requestSlice",
			controlID: tableControlID,
			displayVersion: tableDisplayVersion,
			sliceIDPart: sliceIDPart,
			sliceFirstRow: sliceFirstRow,
			sliceLastRow: sliceLastRow,
			dismissedBoundaries: dismissedBoundaries,
			displayFirstRow: rangeStartRow,
			displayLastRow: rangeLastRow
		};
	services.ajax.execute("dispatchControlCommand",
			services.ajax.addSystemCommandProperty(commandArguments),
			/* useWaitPane */false, callback);
			
	return true;
};

TABLE.SliceManager.prototype._calculateInsertPosition = function(insertStrategy, sliceFirstRow, sliceLastRow) {
	if(insertStrategy == TABLE.BELOW_STRATEGY) {
		return this._getRowOnCurrentPage(sliceFirstRow) * this.getRowHeight();
	} else {
		return (this._getRowOnCurrentPage(sliceLastRow) + 1) * this.getRowHeight();
	}
};

TABLE.SliceManager.prototype._getRowOnCurrentPage = function(tableRow) {
	return tableRow - this._pageStart;
};

TABLE.SliceManager.prototype._timeMeasures = new Object();

TABLE.SliceManager.prototype._startProfiling = function(id) {
	this._timeMeasures[id] = new Object();
	this._timeMeasures[id].startTime = new Date();
};

TABLE.SliceManager.prototype._endProfiling = function(id) {
	this._timeMeasures[id].stopTime = new Date();
};

TABLE.SliceManager.prototype._getProfilingTime = function(id) {
	return this._timeMeasures[id].stopTime.getTime() - this._timeMeasures[id].startTime.getTime();
};

/**
 * Method to report the current state to the logs 
 * @param reportMessage - an additional report message
 */
TABLE.SliceManager.prototype._reportState = function(reportMessage, /*Array*/ profilingIDs) {
//	this._tableControlID = ctrlID;
//	this._pageStart = firstRow;
//	this._pageEnd = lastRow;
//	this._visibleViewportHeight = viewportHeight;
//	this._viewports = viewports;
//	this._viewportContentHeight = 0; // adjusted in init function
//	this._init(firstSliceRow, lastSliceRow);
	var report = reportMessage + "\n";
	if((profilingIDs != undefined) && (profilingIDs != null)) {
		for(var i = 0; i < profilingIDs.length; i++) {
			report += "Execution time of profiled code section '" + profilingIDs[i] + "': " +
					  this._getProfilingTime(profilingIDs[i]) + "ms\n";
		}
	}
	report += "First visible row: " + this.getRangeStartRow() + "\n";
	report += "Last visible row: " + this.getRangeLastRow() + "\n\n";
	
	// Slices info section begin
	report += "------\n\n";
	
	// Add infos about open slices (only fix part
	report += "Slices:\n";
	var slices = this.getFixedSlices();
	for(var i = 0; i < slices.length; i++) {
		var slice = slices[i];
		report += this._getSliceInfo(slice);
	}
	
	// Slices info section end
	report += "------\n\n";
	
	report += "First page row: " + this._pageStart + "\n";
	report += "Last page row: " + this._pageEnd + "\n";
	report += "Page row count: " + this.getPageRowCount() + "\n";
	
	// Send report to server
	services.log.info(report);
};

/**
 * Method to report information about a slice 
 * @param slice {TABLE.Slice} - the slice, which info shall be added to the report
 * 
 * @returns the info of the slice
 */
TABLE.SliceManager.prototype._getSliceInfo = function(slice) {
	var spacer = "    ";
	var report = spacer + "*********\n";
	report += spacer + "ID: " + slice.getID() + "\n";
	report += spacer + "displayed: " + slice._displayed + "\n";
	report += spacer + "first row: " + slice.getFirstRow() + "\n";
	report += spacer + "last row: " + slice.getLastRow() + "\n";
	report += spacer + "insert strategy: " + slice.getInsertStrategy() + "\n";
	report += spacer + "*********\n\n";
	
	return report;
};

TABLE.MouseEventData = function() {
	this.oldclass = null;
	this.changeElement = null;
	this.isFirstMove = true;
	this.dragOrSort = false;
	this.origMouseMove = null;
	this.isFixFlexColumnExchange = false;
};

TABLE.ColumnWidthAdjustment = function(event, ctrlID) {
	this.tableStructure = new TABLE.TableStructure(ctrlID);
};

TABLE.TableStructure = function(ctrlID) {
	this.headerFix = document.getElementById(ctrlID + "_headerFix");
	this.bodyFix = document.getElementById(ctrlID + "_bodyFix");
	this.hasFixPart = this.headerFix != null;
	this.headerFlex = document.getElementById(ctrlID + "_headerFlex");
	this.bodyFlex = document.getElementById(ctrlID + "_bodyFlex");
	this.hasFlexPart = this.headerFlex != null;
	this.tableSeperator = document.getElementById(ctrlID + "_tableSeperator");
	this.tableSeparatorWidth = TABLE.getTableSeparatorWidth(ctrlID);
	if (this.hasFixPart) {
		var fixHeaderTable = this.headerFix.getElementsByTagName("table")[0];
		this.headerHeight = BAL.getElementHeight(fixHeaderTable);
		this.headerWidth = BAL.getElementWidth(fixHeaderTable);
	} else {
		var flexHeaderTable = this.headerFlex.getElementsByTagName("table")[0];
		this.headerHeight = BAL.getElementHeight(flexHeaderTable);
		this.headerWidth = 0;
	}
};

TABLE.TableStructure.prototype.updateFlexWidth = function(availableWidth) {
	if (!this.hasFlexPart) {
		return;
	}
	var width = availableWidth - this.headerWidth - this.tableSeparatorWidth;
	BAL.setElementWidth(this.bodyFlex, width);
	BAL.setElementWidth(this.headerFlex, width);
	return width;
};

TABLE.TableStructure.prototype.updateFixWidth = function() {
	if (!this.hasFixPart) {
		return;
	}
	var width = BAL.getElementWidth(this.headerFix.getElementsByTagName("table")[0]);
	if (this.hasFlexPart) {
		BAL.setElementX(this.bodyFlex, width + this.tableSeparatorWidth);
		BAL.setElementX(this.headerFlex, width + this.tableSeparatorWidth);
	}
	BAL.setElementWidth(this.bodyFix, width);
	BAL.setElementWidth(this.headerFix, width);
	this.headerWidth = width;
	return width;
};

TABLE.ElementPosition = function(x, y, width, height) {
	this.topX = x;
	this.topY = y;
	this.belowX = x + width;
	this.belowY = y + height;
	this.width = width;
	this.height = height;
};

/******************************************************************************************
 *                                                                                        *
 *                                                                                        *
 *                      ScrollPositionProvider                                            *
 *                                                                                        *
 *                                                                                        *
 *                                                                                        *
 ******************************************************************************************/

TABLE.ScrollPositionProvider = {
		/**
		 * Return scroll positions of table body
		 */
		getServerScrollPositions:function(sliceManager, clientDisplayData) {
			if(TABLE.ScrollPositionProvider.isClientDisplayDataExistent(clientDisplayData)) {
				var storedVerticalPosition = TABLE.ScrollPositionProvider.getStoredVerticalScrollPosition(sliceManager, clientDisplayData);
				var storedHorizontalPosition = TABLE.ScrollPositionProvider.getStoredHorizontalScrollPosition(sliceManager, clientDisplayData);
				return TABLE.ScrollPositionProvider.getFormattedPositions(storedHorizontalPosition, storedVerticalPosition);
			} else {
				return TABLE.ScrollPositionProvider.getTopLeftPosition(sliceManager);
			}
		},
		
		/**
		 * Return scroll positions of scrollbar
		 */
		getComputedScrollPositions:function(sliceManager, clientDisplayData) {
			if(TABLE.ScrollPositionProvider.isClientDisplayDataExistent(clientDisplayData)) {
				return TABLE.ScrollPositionProvider.getCalculatedScrollPositions(sliceManager, clientDisplayData);
			} else {
				return TABLE.ScrollPositionProvider.getTopLeftPosition(sliceManager);
			}
		},
		
		isClientDisplayDataExistent:function(clientDisplayData) {
			return clientDisplayData != undefined || clientDisplayData != null;
		},
		
		getCalculatedScrollPositions:function(sliceManager, clientDisplayData) {
			var verticalScrollPosition = TABLE.ScrollPositionProvider.getVerticalScrollPosition(sliceManager, clientDisplayData);
			var horizontalScrollPosition = TABLE.ScrollPositionProvider.getHorizontalScrollPosition(sliceManager, clientDisplayData);
			return TABLE.ScrollPositionProvider.getFormattedPositions(horizontalScrollPosition, verticalScrollPosition);
		},
		
		getVerticalScrollPosition:function(sliceManager, clientDisplayData) {
			if(TABLE.ScrollPositionProvider.isVerticalRangeDefined(sliceManager, clientDisplayData)) {
				return TABLE.ScrollPositionProvider.getVerticalScrollPositionForDefinedRange(sliceManager, clientDisplayData);
			} else {
				return TABLE.ScrollPositionProvider.getVerticalScrollPositionForUndefinedRange(sliceManager, clientDisplayData);
			}
		},
		
		isVerticalRangeDefined:function(sliceManager, clientDisplayData) {
			var pageRowRange = TABLE.ScrollPositionProvider.getPageRowRange(sliceManager, clientDisplayData);
			return pageRowRange.firstIndex > -1;
		},
		
		getPageRowRange:function(sliceManager, clientDisplayData) {
			var rowRange = clientDisplayData.visiblePane.rowRange;
			var pageRangeFirstRow = rowRange.firstIndex - sliceManager.getFirstPageRow();
			var pageRangeLastRow = rowRange.lastIndex - sliceManager.getFirstPageRow();
			var pageForcedVisibleRowInRange = rowRange.forcedVisibleIndexInRange - sliceManager.getFirstPageRow();
			return {firstIndex:pageRangeFirstRow, lastIndex:pageRangeLastRow,
					forcedVisibleIndexInRange:pageForcedVisibleRowInRange};
		},
		
		getVerticalScrollPositionForUndefinedRange:function(sliceManager, clientDisplayData) {
			if(TABLE.ScrollPositionProvider.isStoredVerticalScrollPositionDefined(sliceManager, clientDisplayData)) {
				return TABLE.ScrollPositionProvider.getStoredVerticalScrollPosition(sliceManager, clientDisplayData);
			} else {
				return sliceManager.getRangeStartPosition();
			}
		},
		
		isStoredVerticalScrollPositionDefined:function(sliceManager, clientDisplayData) {
			var pageRowAnchor = TABLE.ScrollPositionProvider.getPageRowAnchor(sliceManager, clientDisplayData); 
			return pageRowAnchor.index > -1;
		},
		
		getPageRowAnchor:function(sliceManager, clientDisplayData) {
			var pageRowAnchorIndex = clientDisplayData.viewportState.rowAnchor.index - sliceManager.getFirstPageRow();
			return {index:pageRowAnchorIndex, indexPixelOffset:clientDisplayData.viewportState.rowAnchor.indexPixelOffset };
		},
		
		getStoredVerticalScrollPosition:function(sliceManager, clientDisplayData) {
			var pageRowAnchor = TABLE.ScrollPositionProvider.getPageRowAnchor(sliceManager, clientDisplayData);
			var visibleRowPosition = pageRowAnchor.index * sliceManager.getRowHeight();
			var visibleRowOffset = pageRowAnchor.indexPixelOffset;
			return TABLE.ScrollPositionProvider.getStoredScrollPosition(visibleRowPosition, visibleRowOffset);
		},
		
		getStoredScrollPosition:function(indexScrollPosition, indexPixelOffset) {
			return indexScrollPosition + indexPixelOffset;
		},
		
		getVerticalScrollPositionForDefinedRange:function(sliceManager, clientDisplayData) {
			if(TABLE.ScrollPositionProvider.isPaneSmallerThanViewportHeight(sliceManager, clientDisplayData)) {
				return TABLE.ScrollPositionProvider.getVerticalScrollPositionForFullyDisplayablePane(sliceManager, clientDisplayData);
			} else {
				return TABLE.ScrollPositionProvider.getVerticalScrollPositionForForcedVisibleRow(sliceManager, clientDisplayData);
			}
		},
		
		isPaneSmallerThanViewportHeight:function(sliceManager, clientDisplayData) {
			return sliceManager.getVisibleViewportHeight() > TABLE.ScrollPositionProvider.getVerticalPaneHeight(sliceManager, clientDisplayData);
		},
		
		getVerticalPaneHeight:function(sliceManager, clientDisplayData) {
			var pageRowRange = TABLE.ScrollPositionProvider.getPageRowRange(sliceManager, clientDisplayData);
			return (pageRowRange.lastIndex - pageRowRange.firstIndex + 1) * sliceManager.getRowHeight();
		},
		
		getVerticalScrollPositionForFullyDisplayablePane:function(sliceManager, clientDisplayData) {
			var pageRowRange = TABLE.ScrollPositionProvider.getPageRowRange(sliceManager, clientDisplayData);
			var paneAnchor = pageRowRange.firstIndex;
			var paneHeight = TABLE.ScrollPositionProvider.getVerticalPaneHeight(sliceManager, clientDisplayData);
			return TABLE.ScrollPositionProvider.getPaneAndPreviousScrollingBasedVerticalPosition(sliceManager, clientDisplayData, paneAnchor, paneHeight);
		},
		
		getPaneAndPreviousScrollingBasedVerticalPosition:function(sliceManager, clientDisplayData, visibleRangeAnchor, visibleRangeHeight) {
			var rangeOffsetFromTop = visibleRangeAnchor * sliceManager.getRowHeight();
			var visibleViewportHeight = sliceManager.getVisibleViewportHeight();
			if(TABLE.ScrollPositionProvider.isStoredVerticalScrollPositionDefined(sliceManager, clientDisplayData)) {
				var verticalScrollOffset = TABLE.ScrollPositionProvider.getStoredVerticalScrollPosition(sliceManager, clientDisplayData);
				return TABLE.ScrollPositionProvider.getPaneAndPreviousScrollingBasedPosition(visibleRangeHeight, rangeOffsetFromTop, visibleViewportHeight, verticalScrollOffset);
			} else {
				return TABLE.ScrollPositionProvider.getCenteredScrollPosition(visibleRangeHeight, rangeOffsetFromTop, visibleViewportHeight);
			}
		},
		
		getPaneAndPreviousScrollingBasedPosition:function(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize, viewportScrollOffset) {
			if(TABLE.ScrollPositionProvider.hasPaneIntersectionWithPreviousScrollPosition(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize, viewportScrollOffset)) {
				return TABLE.ScrollPositionProvider.getDeltaBasedScrollPosition(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize, viewportScrollOffset);
			} else {
				return TABLE.ScrollPositionProvider.getCenteredScrollPosition(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize);
			}
		},
		
		hasPaneIntersectionWithPreviousScrollPosition:function(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize, viewportScrollOffset) {
			var boundaries = TABLE.ScrollPositionProvider.getRangeAndViewportBoundaries(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize, viewportScrollOffset);
			return boundaries.rangeBegin < boundaries.visibleViewportEnd
					&& boundaries.rangeEnd > boundaries.visibleViewportBegin;
		},
		
		getRangeAndViewportBoundaries:function(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize, viewportScrollOffset) {
			var rangeBegin = rangeOffsetFromTableBegin;
			var rangeEnd = rangeBegin + visibleRangeSize;
			var visibleViewportBegin = viewportScrollOffset;
			var visibleViewportEnd = visibleViewportBegin + visibleViewportSize;
			return {rangeBegin:rangeBegin, rangeEnd:rangeEnd,
					visibleViewportBegin:visibleViewportBegin,
					visibleViewportEnd:visibleViewportEnd};
			
		},
		
		getDeltaBasedScrollPosition:function(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize, viewportScrollOffset) {
			var boundaries = TABLE.ScrollPositionProvider.getRangeAndViewportBoundaries(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize, viewportScrollOffset);
			var endDelta;
			if(boundaries.rangeBegin < boundaries.visibleViewportBegin) {
				endDelta = Math.min(0, boundaries.rangeBegin - boundaries.visibleViewportBegin);
			} else {
				endDelta = Math.max(0, boundaries.rangeEnd - boundaries.visibleViewportEnd);
			}
			return viewportScrollOffset + endDelta;
		},
		
		getCenteredScrollPosition:function(visibleRangeSize, rangeOffsetFromTableBegin, visibleViewportSize) {
			var rangeOffsetFromViewportBegin = (visibleViewportSize - visibleRangeSize) / 2;
			return rangeOffsetFromTableBegin - rangeOffsetFromViewportBegin;
		},
		
		getVerticalScrollPositionForForcedVisibleRow:function(sliceManager, clientDisplayData) {
			var pageRowRange = TABLE.ScrollPositionProvider.getPageRowRange(sliceManager, clientDisplayData);
			var rangeTopBound = pageRowRange.firstIndex * sliceManager.getRowHeight();
			var rangeBottomBound = (pageRowRange.lastIndex + 1) * sliceManager.getRowHeight() - sliceManager.getVisibleViewportHeight();
			var rangeAnchor = pageRowRange.forcedVisibleIndexInRange;
			var forcedVisibleRowHeight = sliceManager.getRowHeight();
			var verticalScrollPosition = TABLE.ScrollPositionProvider.getPaneAndPreviousScrollingBasedVerticalPosition(sliceManager, clientDisplayData, rangeAnchor, forcedVisibleRowHeight);
			return TABLE.ScrollPositionProvider.getScrollPositionInRange(rangeTopBound, rangeBottomBound, verticalScrollPosition);
		},
		
		getScrollPositionInRange:function(rangeLowerBound, rangeUpperBound, centeredScrollPosition){
			return Math.min(Math.max(centeredScrollPosition, rangeLowerBound), rangeUpperBound);
		},
		
		getHorizontalScrollPosition:function(sliceManager, clientDisplayData) {
			var flexViewportScrollPosition;
			if(TABLE.ScrollPositionProvider.existsFlexViewport(sliceManager)) {
				if(TABLE.ScrollPositionProvider.shallScrollHorizontalPaneBased(sliceManager, clientDisplayData)) {
					flexViewportScrollPosition = TABLE.ScrollPositionProvider.getPaneBasedFlexPartHorizontalScrollPosition(sliceManager, clientDisplayData);
				} else {
					flexViewportScrollPosition = TABLE.ScrollPositionProvider.getNonPaneBasedFlexPartHorizontalScrollPosition(sliceManager, clientDisplayData);
				}
				return TABLE.ScrollPositionProvider.convertToScrollbarScrollPosition(sliceManager, flexViewportScrollPosition);
			} else {
				return 0;
			}
		},
		
		shallScrollHorizontalPaneBased:function(sliceManager, clientDisplayData) {
			return TABLE.ScrollPositionProvider.isHorizontalRangeDefined(clientDisplayData) && TABLE.ScrollPositionProvider.isRangeFullyInFlexPart(sliceManager, clientDisplayData);
		},
		
		isStoredHorizontalScrollPositionDefined:function(clientDisplayData) {
			return clientDisplayData.viewportState.columnAnchor.index > -1;
		},
		
		getNonPaneBasedFlexPartHorizontalScrollPosition:function(sliceManager, clientDisplayData) {
			if(TABLE.ScrollPositionProvider.isStoredHorizontalScrollPositionDefined(clientDisplayData)) {
				return TABLE.ScrollPositionProvider.getStoredHorizontalScrollPosition(sliceManager, clientDisplayData);
			} else {
				return 0;
			}
		},
		
		getStoredHorizontalScrollPosition:function(sliceManager, clientDisplayData) {
			var flexColumnAnchor = TABLE.ScrollPositionProvider.getColumnAnchorInFlexPart(sliceManager, clientDisplayData);
			var visibleColumnPosition = TABLE.ScrollPositionProvider.getOffsetFromLeft(sliceManager, 0, flexColumnAnchor.index);
			var visibleColumnOffset = flexColumnAnchor.indexPixelOffset;
			return TABLE.ScrollPositionProvider.getStoredScrollPosition(visibleColumnPosition, visibleColumnOffset);
		},
		
		convertToScrollbarScrollPosition:function(sliceManager, flexViewportScrollPosition) {
			var flexViewport = TABLE.ScrollPositionProvider.getFlexViewport(sliceManager);
			var flexWidth = flexViewport.getVisibleWidth();
			var availableWidth = TABLE.getAvailableWidth(sliceManager.getTableID());
			return flexViewportScrollPosition * (availableWidth / flexWidth);
		},
		
		isHorizontalRangeDefined:function(clientDisplayData) {
			return clientDisplayData.visiblePane.columnRange.firstIndex > -1;
		},
		
		getPaneBasedFlexPartHorizontalScrollPosition:function(sliceManager, clientDisplayData) {
			if(TABLE.ScrollPositionProvider.isPaneSmallerThanViewportWidth(sliceManager, clientDisplayData)) {
				return TABLE.ScrollPositionProvider.getHorizontalScrollPositionForFullyDisplayablePane(sliceManager, clientDisplayData);
			} else {
				return TABLE.ScrollPositionProvider.getHorizontalScrollPositionForForcedVisibleColumn(sliceManager, clientDisplayData);
			}
		},
		
		isPaneSmallerThanViewportWidth:function(sliceManager, clientDisplayData) {
			var flexViewport = TABLE.ScrollPositionProvider.getFlexViewport(sliceManager);
			return flexViewport.getVisibleWidth() > TABLE.ScrollPositionProvider.getFlexPaneWidth(sliceManager, clientDisplayData);
		},
		
		getHorizontalScrollPositionForFullyDisplayablePane:function(sliceManager, clientDisplayData) {
			var paneOffsetFromLeft = TABLE.ScrollPositionProvider.getPaneOffsetFromFlexLeft(sliceManager, clientDisplayData);
			var paneWidth = TABLE.ScrollPositionProvider.getFlexPaneWidth(sliceManager, clientDisplayData);
			return TABLE.ScrollPositionProvider.getPaneAndPreviousScrollingBasedHorizontalPosition(sliceManager, clientDisplayData, paneOffsetFromLeft, paneWidth);
		},
		
		getPaneAndPreviousScrollingBasedHorizontalPosition:function(sliceManager, clientDisplayData, visibleRangeOffset, visibleRangeWidth) {
			var flexViewport = TABLE.ScrollPositionProvider.getFlexViewport(sliceManager);
			var visibleViewportWidth = flexViewport.getVisibleWidth();
			if(TABLE.ScrollPositionProvider.isStoredHorizontalScrollPositionDefined(clientDisplayData)) {
				var horizontalScrollOffset = TABLE.ScrollPositionProvider.getStoredHorizontalScrollPosition(sliceManager, clientDisplayData);
				return TABLE.ScrollPositionProvider.getPaneAndPreviousScrollingBasedPosition(visibleRangeWidth, visibleRangeOffset, visibleViewportWidth, horizontalScrollOffset);
			} else {
				return TABLE.ScrollPositionProvider.getCenteredScrollPosition(visibleRangeWidth, visibleRangeOffset, visibleViewportWidth);
			}
		},
		
		getHorizontalScrollPositionForForcedVisibleColumn:function(sliceManager, clientDisplayData) {
			var flexViewport = TABLE.ScrollPositionProvider.getFlexViewport(sliceManager);
			var leftBound = TABLE.ScrollPositionProvider.getPaneOffsetFromFlexLeft(sliceManager, clientDisplayData);
			var rightBound = TABLE.ScrollPositionProvider.getPaneEndOffsetFromFlexLeft(sliceManager, clientDisplayData) - flexViewport.getVisibleWidth();
			var offsetFromLeft = TABLE.ScrollPositionProvider.getForcedVisibleColumnOffsetFromFlexLeft(sliceManager, clientDisplayData);
			var width = TABLE.ScrollPositionProvider.getForcedVisibleColumnWidth(sliceManager, clientDisplayData);
			var horizontalScrollPosition = TABLE.ScrollPositionProvider.getPaneAndPreviousScrollingBasedHorizontalPosition(sliceManager, clientDisplayData, offsetFromLeft, width);
			return TABLE.ScrollPositionProvider.getScrollPositionInRange(leftBound, rightBound, horizontalScrollPosition);
		},
		
		getPaneOffsetFromFlexLeft:function(sliceManager, clientDisplayData) {
			var flexRange = TABLE.ScrollPositionProvider.getRangeInFlexPart(sliceManager, clientDisplayData);
			return TABLE.ScrollPositionProvider.getOffsetFromLeft(sliceManager, 0, flexRange.firstIndex);
		},
		
		getOffsetFromLeft:function(sliceManager, startIndex, stopIndex) {
			var flexViewport = TABLE.ScrollPositionProvider.getFlexViewport(sliceManager);
			var offset = 0;
			if(flexViewport != null) {
				for(var i = startIndex; i < stopIndex && i < flexViewport.getColumnCount(); i++) {
					offset += flexViewport.getColumnWidth(i);
				}
			}
			
			return offset;
		},
		
		getForcedVisibleColumnOffsetFromFlexLeft:function(sliceManager, clientDisplayData) {
			var flexRange = TABLE.ScrollPositionProvider.getRangeInFlexPart(sliceManager, clientDisplayData);
			return TABLE.ScrollPositionProvider.getOffsetFromLeft(sliceManager, 0, flexRange.forcedVisibleIndexInRange);
		},
		
		getPaneEndOffsetFromFlexLeft:function(sliceManager, clientDisplayData) {
			var flexRange = TABLE.ScrollPositionProvider.getRangeInFlexPart(sliceManager, clientDisplayData);
			return TABLE.ScrollPositionProvider.getOffsetFromLeft(sliceManager, 0, flexRange.lastIndex + 1);
		},
		
		getRangeInFlexPart:function(sliceManager, clientDisplayData) {
			var columnRange = clientDisplayData.visiblePane.columnRange;
			var fixViewportColumnCount = TABLE.ScrollPositionProvider.getFixViewportColumnCount(sliceManager);
			var firstFlexIndex = columnRange.firstIndex - fixViewportColumnCount;
			var lastFlexIndex = columnRange.lastIndex - fixViewportColumnCount;
			var forcedVisibleFlexIndexInRange = columnRange.forcedVisibleIndexInRange - fixViewportColumnCount;
			
			return { firstIndex:firstFlexIndex, lastIndex:lastFlexIndex,
				forcedVisibleIndexInRange:forcedVisibleFlexIndexInRange };
		},
		
		getColumnAnchorInFlexPart:function(sliceManager, clientDisplayData) {
			var columnAnchor = clientDisplayData.viewportState.columnAnchor;
			var fixViewportColumnCount = TABLE.ScrollPositionProvider.getFixViewportColumnCount(sliceManager);
			var flexColumnIndex = columnAnchor.index - fixViewportColumnCount;
			
			return { index:flexColumnIndex, indexPixelOffset:columnAnchor.indexPixelOffset };
		},
		
		getFixViewportColumnCount:function(sliceManager) {
			if(TABLE.ScrollPositionProvider.existsFixViewport(sliceManager)) {
				return TABLE.ScrollPositionProvider.getFixViewport(sliceManager).getColumnCount();
			} else {
				return 0;
			}
		},
		
		existsFixViewport:function(sliceManager) {
			var fixViewport = TABLE.ScrollPositionProvider.getFixViewport(sliceManager);
			return fixViewport != undefined && fixViewport != null;
		},
		
		existsFlexViewport:function(sliceManager) {
			var flexViewport = TABLE.ScrollPositionProvider.getFlexViewport(sliceManager);
			return flexViewport != undefined && flexViewport != null;
		},
		
		getFlexPaneWidth:function(sliceManager, clientDisplayData) {
			var flexRange = TABLE.ScrollPositionProvider.getRangeInFlexPart(sliceManager, clientDisplayData);
			return TABLE.ScrollPositionProvider.getOffsetFromLeft(sliceManager, flexRange.firstIndex, flexRange.lastIndex + 1);
		},
		
		getForcedVisibleColumnWidth:function(sliceManager, clientDisplayData) {
			var flexViewport = TABLE.ScrollPositionProvider.getFlexViewport(sliceManager);
			var flexRange = TABLE.ScrollPositionProvider.getRangeInFlexPart(sliceManager, clientDisplayData);
			return flexViewport.getColumnWidth(flexRange.forcedVisibleIndexInRange);
		},
		
		getFlexViewport:function(sliceManager) {
			var flexViewportID = TABLE.getViewportIDForTablePartWithID(TABLE.getFlexBodyID(sliceManager.getTableID()));
			return sliceManager.getViewport(flexViewportID);
		},
		
		getFixViewport:function(sliceManager) {
			var fixViewportID = TABLE.getViewportIDForTablePartWithID(TABLE.getFixBodyID(sliceManager.getTableID()));
			return sliceManager.getViewport(fixViewportID);
		},
		
		isRangeFullyInFlexPart:function(sliceManager, clientDisplayData) {
			var columnRange = clientDisplayData.visiblePane.columnRange;
			var fixViewport = TABLE.ScrollPositionProvider.getFixViewport(sliceManager);
			return !TABLE.ScrollPositionProvider.existsFixViewport(sliceManager)
			       || columnRange.firstIndex >= fixViewport.getColumnCount();
		},
		
		getTopLeftPosition:function(sliceManager) {
			var horizontalScrollPosition = 0;
			var verticalScrollPosition = sliceManager.getRangeStartPosition();
			return TABLE.ScrollPositionProvider.getFormattedPositions(horizontalScrollPosition, verticalScrollPosition);
		},
		
		getFormattedPositions:function(horizontalScrollPosition, verticalScrollPosition) {
			return { horizontal:horizontalScrollPosition, vertical:verticalScrollPosition };
		}
};

/**************************************************************************************
 * 																					  *
 * 						VisibleAnchorCalculator										  *
 * 																					  *
 * 																					  *
 **************************************************************************************/
TABLE.VisiblePositionCalculator = {
	getPosition:function(viewport, sliceManager) {
		return {horizontal: TABLE.VisiblePositionCalculator.getHorizontalPosition(viewport),
				vertical: TABLE.VisiblePositionCalculator.getVerticalPosition(viewport, sliceManager)};
	},
	
	getVerticalPosition:function(viewport, sliceManager) {
		return {rowAnchor:TABLE.VisiblePositionCalculator.getRowAnchor(viewport, sliceManager),
			    rowAnchorOffset:TABLE.VisiblePositionCalculator.getRowAnchorOffset(viewport, sliceManager)};
	},
	
	getRowAnchor:function(viewport, sliceManager) {
		if(sliceManager.getRowHeight() > 0) {
			return Math.floor(viewport.getVerticalScrollingPosition() / sliceManager.getRowHeight()) + sliceManager.getFirstPageRow();
		} else {
			return sliceManager.getFirstPageRow();
		}
	},
	
	getRowAnchorOffset:function(viewport, sliceManager) {
		if(sliceManager.getRowHeight() > 0) {
			return Math.round(viewport.getVerticalScrollingPosition() % sliceManager.getRowHeight());
		} else {
			return 0;
		}
	},
	
	getHorizontalPosition:function(viewport) {
		var columnAnchorData = TABLE.VisiblePositionCalculator.getColumnAnchorAndOffsetFromLeft(viewport);
		var columnAnchorOffset = TABLE.VisiblePositionCalculator.getColumnAnchorOffset(viewport, columnAnchorData.offsetFromLeft);
		return {columnAnchor:columnAnchorData.columnAnchor,
				columnAnchorOffset:columnAnchorOffset};
	},
	
	getColumnAnchorAndOffsetFromLeft:function(viewport) {
		var columnAnchor = 0;
		var offsetFromLeft = 0;
		for(; columnAnchor < viewport.getColumnCount(); columnAnchor++) {
			if(offsetFromLeft + viewport.getColumnWidth(columnAnchor) < viewport.getHorizontalScrollingPosition()) {
				offsetFromLeft += viewport.getColumnWidth(columnAnchor);
			} else {
				break;
			}
		}
		
		return {columnAnchor:columnAnchor + viewport.getColumnIDOffset(),
			    offsetFromLeft:offsetFromLeft};
	},
	
	getColumnAnchorOffset:function(viewport, offsetFromLeft) {
		return Math.round(viewport.getHorizontalScrollingPosition() - offsetFromLeft);
	}
};

/** Utilities **/

TABLE.addScrollListener = function(element, listenerFunction) {
	TABLE.removeScrollListener(element, listenerFunction);
	if(element != null) {
		BAL.addEventListener(element, "scroll", listenerFunction);
	}
};

TABLE.removeScrollListener = function(element, listenerFunction) {
	if(element != null) {
		BAL.removeEventListener(element, "scroll", listenerFunction);
	}
};

TABLE.executeInstantly = function(firstListenerFunction, secondListenerFunction) {
	firstListenerFunction();
	secondListenerFunction();
};
	
TABLE.executeAfterRepaint = function(firstListenerFunction, secondListenerFunction) {
	// Disrupt control flow, to ensure browser has made all GUI updates,
	// before listeners will be added.
	// Otherwise listeners would be triggered not by intention.
	// As long as control flow has not finished, no GUI updates
	// will be performed. So if the listeners would be added right away,
	// they would be triggered instantly after GUI updates have been made.
	BAL.requestAnimationFrame(function() {
		firstListenerFunction();
		secondListenerFunction();
	});
};