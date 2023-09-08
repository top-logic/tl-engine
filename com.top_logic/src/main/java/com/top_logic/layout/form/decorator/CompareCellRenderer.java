/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CellRenderer} for columns in a compare table.
 * 
 * <p>
 * Depending on the {@link CompareInfo#getChangeInfo() change} it renders the old value or the new
 * value, and eventually a decoration object.
 * </p>
 * 
 * @see SimpleCompareCellRenderer
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareCellRenderer<CI extends CompareInfo> extends AbstractCellRenderer {

	private final CellRenderer _finalRenderer;

	private final CompareService<CI> _compareService;

	private final boolean _treeTable;

	/**
	 * Creates a new {@link CompareCellRenderer}.
	 * 
	 * @param finalCellRenderer
	 *        {@link CellRenderer} to display one of the compared objects in {@link CompareInfo}.
	 * @param compareService
	 *        {@link CompareService} to render decoration of the displayed {@link CompareInfo}.
	 * @param treeTable
	 *        Whether the displayed table is a tree table.
	 */
	public CompareCellRenderer(CellRenderer finalCellRenderer, CompareService<CI> compareService, boolean treeTable) {
		_finalRenderer = finalCellRenderer;
		_compareService = compareService;
		_treeTable = treeTable;
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, final Cell cell) throws IOException {
		CompareCell compareCell = createCompareCell(cell);
		CompareRowObject rowObject = compareCell.compareRow();
		boolean sameObject = rowObject.changeValue() != null && rowObject.baseValue() != null;
		CI compareInfo = (CI) compareCell.compareValue();
		switch (compareInfo.getChangeInfo()) {
			case NO_CHANGE:
				_finalRenderer.writeCell(context, out, compareCell);
				break;
			case CREATED:
			case REMOVED:
				/* ChangeInfo.REMOVED and CREATED and same object is a little bit strange, but the
				 * CompareInfo has type CREATED, e.g. when a String attribute has switched from
				 * empty string to something else, or a collection attribute has switched from empty
				 * list to non empty list. */
				if (sameObject) {
					_compareService.start(context, out, compareInfo);
				}
				_finalRenderer.writeCell(context, out, compareCell);
				if (sameObject) {
					_compareService.end(context, out, compareInfo);
				}
				break;
			case CHANGED:
			case DEEP_CHANGED:
				_compareService.start(context, out, compareInfo);
				_finalRenderer.writeCell(context, out, compareCell);
				_compareService.end(context, out, compareInfo);
				break;
			default:
				break;
		}
	}

	/**
	 * true, if a tree table shall be displayed, false otherwise.
	 */
	protected boolean isTreeTable() {
		return _treeTable;
	}

	/**
	 * {@link CompareCell} to use for value retrieval.
	 */
	protected CompareCell createCompareCell(final Cell innerCell) {
		return new CompareCell(innerCell, isTreeTable());
	}

}

