/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * Base class for composing {@link ColumnInfo}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColumnProxy extends ColumnInfo {

	private final ColumnInfo _base;

	/**
	 * Creates a {@link ColumnProxy}.
	 * 
	 * @param base
	 *        The delegate.
	 */
	public ColumnProxy(ColumnInfo base) {
		super(base.getTypeContext(), base.getHeaderI18NKey(), base.getVisibility(), base.getAccessor());

		_base = base;
	}

	@Override
	protected void setComparators(ColumnConfiguration column) {
		_base.setComparators(column);
	}

	@Override
	protected void setFilterProvider(ColumnConfiguration column) {
		_base.setFilterProvider(column);
	}

	@Override
	protected void setExcelRenderer(ColumnConfiguration column) {
		_base.setExcelRenderer(column);
	}

	@Override
	protected void setRenderer(ColumnConfiguration column) {
		_base.setRenderer(column);
	}

	@Override
	protected void setPDFRenderer(ColumnConfiguration column) {
		_base.setPDFRenderer(column);
	}

	@Override
	protected ControlProvider getControlProvider() {
		return _base.getControlProvider();
	}

	@Override
	protected ColumnInfo internalJoin(ColumnInfo other) {
		return _base.internalJoin(other);
	}

	@Override
	protected void setCellExistenceTester(ColumnConfiguration column) {
		_base.setCellExistenceTester(column);
	}
}