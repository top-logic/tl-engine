/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentDownloadRenderer;
import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.filter.WrapperValueExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link ColumnInfo} configuring a column displaying a {@link Document}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocumentColumn extends ColumnInfo {

	/**
	 * Creates a new {@link DocumentColumn}.
	 * @param headerI18NKey
	 *        see {@link #getHeaderI18NKey()}
	 * @param accessor
	 *        see {@link #getAccessor()}
	 */
	public DocumentColumn(TLTypeContext contentType, ResKey headerI18NKey, DisplayMode visibility,
			Accessor<?> accessor) {
		super(contentType, headerI18NKey, visibility, accessor);
	}

	@Override
	protected void setComparators(ColumnConfiguration column) {
		column.setComparator(WrapperNameComparator.getInstanceNullsafe());
	}

	@Override
	protected void setFilterProvider(ColumnConfiguration column) {
		column.setFilterProvider(LabelFilterProvider.INSTANCE);
	}
	
	@Override
	protected void setCellExistenceTester(ColumnConfiguration column) {
		if (getTypeContext().getTypePart() != null) {
			column.setCellExistenceTester(WrapperValueExistenceTester.INSTANCE);
		}
	}

	@Override
	protected void setExcelRenderer(ColumnConfiguration column) {
		// No information.
	}

	@Override
	protected void setRenderer(ColumnConfiguration column) {
		column.setRenderer(DocumentDownloadRenderer.INSTANCE);
	}

	@Override
	protected ControlProvider getControlProvider() {
		return null;
	}

	@Override
	protected ColumnInfo internalJoin(ColumnInfo other) {
		if (!(other instanceof DocumentColumn)) {
			return GenericColumn.joinColumns(this, other);
		} else {
			return this;
		}
	}
}