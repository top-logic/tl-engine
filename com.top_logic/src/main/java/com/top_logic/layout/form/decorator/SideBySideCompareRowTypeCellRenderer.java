/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CompareRowTypeCellRenderer} for side by side display of compare table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SideBySideCompareRowTypeCellRenderer extends CompareRowTypeCellRenderer {

	private final ChangeInfo _changeInfoSide;

	/**
	 * Creates a {@link SideBySideCompareRowTypeCellRenderer}.
	 * 
	 * <p>
	 * Note: Don't forget to call {@link #init(CompareService)} before using.
	 * </p>
	 */
	public SideBySideCompareRowTypeCellRenderer(ResourceProvider resourceProvider, CellRenderer contentRenderer,
			ChangeInfo changeInfoSide) {
		super(resourceProvider, contentRenderer);
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
	protected void writeValueContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		if (shallDisplay((CompareInfo) cell.getValue())) {
			super.writeValueContent(context, out, cell);
		}
	}

	private boolean shallDisplay(CompareInfo compareInfo) {
		return compareInfo.getChangeInfo() == ChangeInfo.NO_CHANGE
			|| compareInfo.getChangeInfo() == ChangeInfo.CHANGED
			|| compareInfo.getChangeInfo() == ChangeInfo.DEEP_CHANGED
			|| compareInfo.getChangeInfo() == _changeInfoSide;
	}

}
