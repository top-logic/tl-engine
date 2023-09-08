/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link ColumnInfo} for a column with arbitrary/undefined types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class GenericColumn extends DefaultColumn {

	/**
	 * Creates a new GenericColumn.
	 * @param headerI18NKey
	 *        See {@link #getAccessor()}.
	 * @param accessor
	 *        See {@link #getHeaderI18NKey()}.
	 */
	public GenericColumn(TLTypeContext contentType, ResKey headerI18NKey, DisplayMode visibility, Accessor accessor) {
		super(contentType, headerI18NKey, visibility, accessor);
	}

	@Override
	protected ColumnInfo internalJoin(ColumnInfo other) {
		return this;
	}

	/**
	 * Joins the given {@link ColumnInfo}.
	 * 
	 * @param base
	 *        {@link ColumnInfo} to use most informations from.
	 * @param other
	 *        Additional {@link ColumnInfo} to join into the base info.
	 * @return A {@link GenericColumn} for given {@link ColumnInfo}s.
	 */
	public static GenericColumn joinColumns(ColumnInfo base, ColumnInfo other) {
		TLTypeContext baseType = base.getTypeContext();
		TLTypeContext otherType = other.getTypeContext();
		return new GenericColumn(baseType.join(otherType), base.getHeaderI18NKey(), base.getVisibility(),
			base.getAccessor());

	}

}