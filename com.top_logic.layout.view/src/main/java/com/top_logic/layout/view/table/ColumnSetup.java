/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.TLStructuredTypePart;

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
 */
public record ColumnSetup(
		String attribute,
		ResKey label,
		TLStructuredTypePart part,
		ViewContext viewContext,
		ColumnBinding binding) {
	// Pure data carrier.
}
