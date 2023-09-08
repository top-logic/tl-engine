/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.decorator.CompareCell.ValueType;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CompareCellRenderer}, that will be used for side by side display of compare table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SideBySideCompareCellRenderer<CI extends CompareInfo> extends CompareCellRenderer<CI> {

	private ChangeInfo _changeInfoSide;

	/**
	 * Create a new {@link SideBySideCompareCellRenderer}.
	 */
	public SideBySideCompareCellRenderer(CellRenderer finalCellRenderer, CompareService<CI> compareService,
			boolean treeTable, ChangeInfo changeInfoSide) {
		super(finalCellRenderer, compareService, treeTable);
		_changeInfoSide = changeInfoSide;
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		if (shallDisplay((CompareInfo) cell.getValue())) {
			super.writeCell(context, out, cell);
		}
	}

	@Override
	protected CompareCell createCompareCell(Cell innerCell) {
		CompareCell compareCell = new CompareCell(innerCell, isTreeTable());
		if (_changeInfoSide == ChangeInfo.REMOVED) {
			compareCell.setValueType(ValueType.BASE);
		} else if (_changeInfoSide == ChangeInfo.CREATED) {
			compareCell.setValueType(ValueType.CHANGE);
		}
		return compareCell;
	}

	private boolean shallDisplay(CompareInfo compareInfo) {
		return compareInfo.getChangeInfo() == ChangeInfo.NO_CHANGE
			|| compareInfo.getChangeInfo() == ChangeInfo.CHANGED
			|| compareInfo.getChangeInfo() == ChangeInfo.DEEP_CHANGED
			|| compareInfo.getChangeInfo() == _changeInfoSide;
	}

}
