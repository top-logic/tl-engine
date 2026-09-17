/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.model.ModelService;

/**
 * What a {@link ColumnDeclaration} resolves its columns against: the rows of the table, and the
 * session displaying it.
 *
 * @param rowType
 *        The model type of the table's rows, or {@code null} when it is unknown - the table
 *        declares none and its rows say nothing about their type.
 * @param context
 *        The per-session context, which the columns resolve their channel references through.
 */
public record ColumnResolution(TLStructuredType rowType, ViewContext context) {

	/**
	 * The attribute of the given name held by the {@link #rowType()}, or {@code null} when the row
	 * type has none - or is unknown.
	 */
	public TLStructuredTypePart part(String attribute) {
		return rowType == null ? null : rowType.getPart(attribute);
	}

	/**
	 * The model a column resolves the types it names in, see {@link #model(TLStructuredType)}.
	 */
	public TLModel model() {
		return model(rowType);
	}

	/**
	 * The model a column of a table over rows of the given type resolves the types it names in:
	 * the model those rows live in, and the application model for a table whose row type is
	 * unknown.
	 *
	 * @param rowType
	 *        The model type of the table's rows, or {@code null} when it is unknown.
	 */
	public static TLModel model(TLStructuredType rowType) {
		return rowType == null ? ModelService.getApplicationModel() : rowType.getModel();
	}

}
