/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.Column;

/**
 * Proxy for {@link ExcelCellRenderer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ExcelCellRendererProxy implements ExcelCellRenderer {

	@Override
	public ExcelValue renderCell(RenderContext context) {
		return impl().renderCell(context);
	}

	@Override
	public int colSpan(TableModel model, Column modelColumn) {
		return impl().colSpan(model, modelColumn);
	}

	@Override
	public Object newCustomContext(TableModel model, Column modelColumn) {
		return impl().newCustomContext(model, modelColumn);
	}

	/**
	 * {@link ExcelCellRenderer} to delegate calls to.
	 */
	protected abstract ExcelCellRenderer impl();

}

