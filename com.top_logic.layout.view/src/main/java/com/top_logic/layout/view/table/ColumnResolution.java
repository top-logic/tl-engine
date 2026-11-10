/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;

import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.model.ModelService;

/**
 * What a {@link ColumnDeclaration} resolves its columns against: the rows of the table, and the
 * session displaying it.
 *
 * <p>
 * A table may display rows of several types. A column showing an attribute resolves it at the first
 * of these types holding an attribute of that name, see {@link #part(String)}. Should two of the
 * types hold different attributes of the same name, the one of the type listed first wins.
 * </p>
 *
 * @param rowTypes
 *        The model types of the table's rows, in their order. Empty when the type is unknown - the
 *        table declares none and its rows say nothing about their type.
 * @param context
 *        The per-session context, which the columns resolve their channel references through.
 */
public record ColumnResolution(List<TLStructuredType> rowTypes, ViewContext context) {

	/**
	 * Creates a {@link ColumnResolution}.
	 *
	 * @param rowTypes
	 *        See {@link #rowTypes()}.
	 * @param context
	 *        See {@link #context()}.
	 */
	public ColumnResolution {
		rowTypes = List.copyOf(rowTypes);
	}

	/**
	 * Creates a {@link ColumnResolution} for a table whose rows are of a single type.
	 *
	 * @param rowType
	 *        The model type of the table's rows, or {@code null} when it is unknown.
	 * @param context
	 *        See {@link #context()}.
	 */
	public ColumnResolution(TLStructuredType rowType, ViewContext context) {
		this(rowType == null ? List.of() : List.of(rowType), context);
	}

	/**
	 * The first of the {@link #rowTypes()}, or {@code null} when the row type is unknown.
	 *
	 * <p>
	 * A column asking for the type holding a certain attribute uses {@link #typeHolding(String)}
	 * instead.
	 * </p>
	 */
	public TLStructuredType rowType() {
		return rowTypes.isEmpty() ? null : rowTypes.get(0);
	}

	/**
	 * The attribute of the given name held by the first of the {@link #rowTypes()} holding one, or
	 * {@code null} when none does - or the row type is unknown.
	 */
	public TLStructuredTypePart part(String attribute) {
		TLStructuredType type = typeHolding(attribute);
		return type == null ? null : type.getPart(attribute);
	}

	/**
	 * The first of the {@link #rowTypes()} holding an attribute of the given name, or {@code null}
	 * when none does - or the row type is unknown.
	 */
	public TLStructuredType typeHolding(String attribute) {
		for (TLStructuredType type : rowTypes) {
			if (type.getPart(attribute) != null) {
				return type;
			}
		}
		return null;
	}

	/**
	 * The model a column resolves the types it names in, see {@link #model(TLStructuredType)} of
	 * the {@link #rowType()}.
	 */
	public TLModel model() {
		return model(rowType());
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
