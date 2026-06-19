/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.table.Column;

/**
 * Strategy that builds a green-field {@link Column} (accessor, renderer, comparator and filter) for
 * a model attribute.
 *
 * <p>
 * Resolved per attribute type by {@link ColumnProviderService}, so the mapping from a datatype to
 * its table affordances is an extensible registry rather than a hard-coded type switch: an
 * application can register a provider for its own type without touching the table layer.
 * </p>
 */
public interface ColumnProvider {

	/**
	 * Builds the column for the given attribute.
	 *
	 * @param attribute
	 *        The attribute (column) name, used as the cell value accessor key.
	 * @param label
	 *        The resolved column header label.
	 * @param part
	 *        The model attribute, or {@code null} when the row type could not be resolved.
	 * @return The column definition.
	 */
	Column<Object, ?> createColumn(String attribute, ResKey label, TLStructuredTypePart part);

}
