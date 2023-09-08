/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.layout.table.model.Column;

/**
 * Adapter for a {@link TableRenderer.Cell} based on a {@link RenderInfoAdapter}.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CellAdapter extends RenderInfoAdapter implements TableRenderer.Cell {

	@Override
	public int getRowIndex() {
		return impl().getRowIndex();
	}

	@Override
	public int getColumnIndex() {
		return impl().getColumnIndex();
	}

	@Override
	public Column getColumn() {
		return impl().getColumn();
	}

	@Override
	public String getColumnName() {
		return impl().getColumnName();
	}

	@Override
	public Object getValue() {
		return impl().getValue();

	}

	@Override
	public Object getRowObject() {
		return impl().getRowObject();
	}

	@Override
	public TableRenderer.RenderState getRenderState() {
		return impl().getRenderState();
	}

	@Override
	public boolean cellExists() {
		return impl().cellExists();
	}

	@Override
	protected abstract TableRenderer.Cell impl();

}

