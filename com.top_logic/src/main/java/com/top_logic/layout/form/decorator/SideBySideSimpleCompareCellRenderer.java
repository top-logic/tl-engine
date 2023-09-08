/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.layout.form.decorator.CompareCell.ValueType;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link SimpleCompareCellRenderer} for side by side display of compare table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SideBySideSimpleCompareCellRenderer extends SimpleCompareCellRenderer {

	private ChangeInfo _changeInfoSide;

	/**
	 * Create a new {@link SideBySideSimpleCompareCellRenderer}.
	 */
	public SideBySideSimpleCompareCellRenderer(CellRenderer cellRenderer, boolean treeTable,
			ChangeInfo changeInfoSide) {
		super(cellRenderer, treeTable);
		_changeInfoSide = changeInfoSide;
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

}
