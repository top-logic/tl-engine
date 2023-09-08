/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;
import java.util.Optional;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.decorator.CompareCell.ValueType;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CompareTreeCellRenderer} for side by side display of tree tables.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SideBySideCompareTreeCellRenderer extends CompareTreeCellRenderer {

	private ChangeInfo _changeInfoSide;

	/**
	 * Creates a {@link SideBySideCompareTreeCellRenderer}.
	 * 
	 * <p>
	 * Note: Don't forget to call {@link #init(CompareService)} before using.
	 * </p>
	 */
	public SideBySideCompareTreeCellRenderer(ResourceProvider resourceProvider, CellRenderer contentRenderer,
			int indentChars, ChangeInfo changeInfoSide) {
		super(resourceProvider, contentRenderer, indentChars);
		_changeInfoSide = changeInfoSide;
	}

	@Override
	protected CompareCellRenderer<? extends CompareInfo> createCompareCellRenderer(
			CompareService<? extends CompareInfo> compareService) {
		return new SideBySideCompareCellRenderer<>(newImageRenderer(), compareService, true, _changeInfoSide);
	}

	@Override
	protected void writeDecorationContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		if (shallDisplay((CompareInfo) cell.getValue())) {
			super.writeDecorationContent(context, out, cell);
		}
	}

	@Override
	protected Optional<CompareCell> createCompareCell(Cell cell) {
		if (shallDisplay((CompareInfo) cell.getValue())) {
			CompareCell compareCell = new CompareCell(cell, true);
			if (_changeInfoSide == ChangeInfo.REMOVED) {
				compareCell.setValueType(ValueType.BASE);
			} else if (_changeInfoSide == ChangeInfo.CREATED) {
				compareCell.setValueType(ValueType.CHANGE);
			}
			return Optional.of(compareCell);
		} else {
			return Optional.empty();
		}
	}

	private boolean shallDisplay(CompareInfo compareInfo) {
		return compareInfo.getChangeInfo() == ChangeInfo.NO_CHANGE
			|| compareInfo.getChangeInfo() == ChangeInfo.CHANGED
			|| compareInfo.getChangeInfo() == ChangeInfo.DEEP_CHANGED
			|| compareInfo.getChangeInfo() == _changeInfoSide;
	}

}
