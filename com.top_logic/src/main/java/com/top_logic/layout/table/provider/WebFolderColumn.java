/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link ColumnInfo} configuring a column displaying {@link WebFolder} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebFolderColumn extends ColumnInfo {

	/**
	 * Creates a {@link WebFolderColumn}.
	 * @param headerI18NKey
	 *        See {@link #getHeaderI18NKey()}.
	 * @param accessor
	 *        See {@link #getAccessor()}
	 */
	public WebFolderColumn(TLTypeContext contentType, ResKey headerI18NKey, DisplayMode visibility,
			Accessor accessor) {
		super(contentType, headerI18NKey, visibility, accessor);
	}

	@Override
	protected
	void setComparators(ColumnConfiguration column) {
		column.setComparator(new WebFolderAttributeComparator(1));
		column.setDescendingComparator(null);
	}

	@Override
	protected
	void setFilterProvider(ColumnConfiguration column) {
		column.setFilterProvider(WebFolderAttributeFilterProvider.INSTANCE);
	}
	
	@Override
	protected void setCellExistenceTester(ColumnConfiguration column) {
		if (getTypeContext().getTypePart() != null) {
			column.setCellExistenceTester(AllCellsExist.INSTANCE);
		}
	}

	@Override
	protected void setExcelRenderer(ColumnConfiguration column) {
		// No information.
	}

	@Override
	protected void setRenderer(ColumnConfiguration column) {
		column.setCellRenderer(new WebFolderAttributeRenderer());
	}

	@Override
	protected ColumnInfo internalJoin(ColumnInfo other) {
		if (!(other instanceof WebFolderColumn)) {
			return GenericColumn.joinColumns(this, other);
		} else {
			return this;
		}
	}

	@Override
	protected ControlProvider getControlProvider() {
		// Cannot be edited.
		return null;
	}
}