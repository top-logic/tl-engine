/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.tool.export.ExcelCellRenderer.RenderContext;

/**
 * Proxy class for {@link RenderContext}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RenderContextProxy implements RenderContext {

	@Override
	public TableModel model() {
		return impl().model();
	}

	@Override
	public Column modelColumn() {
		return impl().modelColumn();
	}

	@Override
	public int excelColumn() {
		return impl().excelColumn();
	}

	@Override
	public int modelRow() {
		return impl().modelRow();
	}

	@Override
	public int excelRow() {
		return impl().excelRow();
	}

	@Override
	public Object getCustomContext() {
		return impl().getCustomContext();
	}

	@Override
	public Object getCellValue() {
		return impl().getCellValue();
	}

	/**
	 * {@link RenderContext} to delegate calls to.
	 */
	protected abstract RenderContext impl();

}

