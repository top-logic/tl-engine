/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link ColumnInfo} providing no additional information.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DefaultColumn extends ColumnInfo {

	/**
	 * Creates a {@link DefaultColumn}.
	 * @param headerI18NKey
	 *        See {@link #getHeaderI18NKey()}.
	 * @param accessor
	 *        See {@link #getAccessor()}.
	 */
	public DefaultColumn(TLTypeContext contentType, ResKey headerI18NKey, DisplayMode visibility,
			Accessor accessor) {
		super(contentType, headerI18NKey, visibility, accessor);
	}

	@Override
	protected void setComparators(ColumnConfiguration column) {
		// No information.
	}

	@Override
	protected void setFilterProvider(ColumnConfiguration column) {
		// No information.
	}
	
	@Override
	protected void setCellExistenceTester(ColumnConfiguration column) {
		// No information.
	}

	@Override
	protected void setExcelRenderer(ColumnConfiguration column) {
		// No information.
	}

	@Override
	protected void setRenderer(ColumnConfiguration column) {
		// No information.
	}

	@Override
	protected ControlProvider getControlProvider() {
		// No information.
		return null;
	}

	@Override
	protected void setPDFRenderer(ColumnConfiguration column) {
		// No information.
	}

}