/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DelegatingColumn;

/**
 * The resolved descriptor of one table column, passed to its {@link ColumnBinding} to build the
 * runtime column and contribute any per-session UI.
 *
 * @param attribute
 *        The column (attribute) name.
 * @param label
 *        The resolved column header label.
 * @param part
 *        The model attribute, or {@code null} if the row type could not be resolved.
 * @param viewContext
 *        The per-session context, e.g. for resolving channel references.
 * @param binding
 *        The strategy turning this descriptor into a column (and optional UI).
 * @param width
 *        The configured default display width in pixels, or {@code 0} to keep the width the column
 *        brings itself.
 */
public record ColumnSetup(
		String attribute,
		ResKey label,
		TLStructuredTypePart part,
		ViewContext viewContext,
		ColumnBinding binding,
		int width) {

	/**
	 * The runtime column for this descriptor: the column its {@link #binding()} builds, displayed
	 * in the {@link #width() configured width} when there is one.
	 */
	public Column<Object, ?> buildColumn() {
		Column<Object, ?> column = binding().createColumn(this);
		return width() > 0 ? DelegatingColumn.withDefaultWidth(column, width()) : column;
	}

}
