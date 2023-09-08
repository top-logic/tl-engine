/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import java.util.Collection;
import java.util.Collections;

/**
 * Base class for atomic boxes that directly render content.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractContentBox extends AbstractBox implements ContentBox {

	private String _style = null;

	private int _initialColumns = 1;

	private int _initialRows = 1;

	@Override
	public Collection<Box> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public String getStyle() {
		return _style;
	}

	@Override
	public void setStyle(String style) {
		_style = style;
	}

	@Override
	public void setInitialColumns(int columns) {
		_initialColumns = columns;
	}

	@Override
	public void setInitialRows(int rows) {
		_initialRows = rows;
	}

	@Override
	protected void localLayout() {
		setDimension(_initialColumns, _initialRows);
	}

	@Override
	protected void enterContent(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table) {
		table.set(x, y, this);
	}

}
