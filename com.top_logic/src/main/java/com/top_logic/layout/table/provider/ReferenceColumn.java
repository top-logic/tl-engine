/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.table.filter.CollectionLabelFilterProvider;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.filter.WrapperValueExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link ColumnInfo} configuring a column displaying object values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReferenceColumn extends ColumnInfo {

	/**
	 * Creates a {@link ReferenceColumn}.
	 * 
	 * @param contentType
	 *        See {@link #getTypeContext()}.
	 */
	public ReferenceColumn(TLTypeContext contentType, ResKey headerI18NKey, DisplayMode visibility,
			Accessor accessor) {
		super(contentType, headerI18NKey, visibility, accessor);
	}

	/**
	 * Whether multiple elements are expected in the content.
	 */
	public final boolean isMultiple() {
		return getTypeContext().isMultiple();
	}

	/**
	 * Whether at least one element is expected.
	 */
	public final boolean isMandatory() {
		return getTypeContext().isMandatory();
	}

	@Override
	protected
	void setComparators(ColumnConfiguration column) {
		if (isMultiple()) {
			column.setComparator(ComparableComparator.INSTANCE);
			column.setDescendingComparator(null);
		} else {
			column.setComparator(WrapperNameComparator.getInstanceNullsafe());
			column.setDescendingComparator(null);
		}
	}

	@Override
	protected
	void setFilterProvider(ColumnConfiguration column) {
		if (isMultiple()) {
			column.setFilterProvider(new CollectionLabelFilterProvider(isMandatory()));
		} else {
			column.setFilterProvider(new LabelFilterProvider(isMandatory()));
		}
	}

	@Override
	protected void setCellExistenceTester(ColumnConfiguration column) {
		if (getTypeContext().getTypePart() != null) {
			column.setCellExistenceTester(WrapperValueExistenceTester.INSTANCE);
		}
	}

	@Override
	protected ColumnInfo internalJoin(ColumnInfo other) {
		if (!(other instanceof ReferenceColumn)) {
			return GenericColumn.joinColumns(this, other);
		}

		return joinReference((ReferenceColumn) other);
	}

	ColumnInfo joinReference(ReferenceColumn other) {
		if (isMultiple() != other.isMultiple()) {
			return GenericColumn.joinColumns(this, other);
		}

		return createColumn(getTypeContext().join(other.getTypeContext()));
	}

	/**
	 * Creates a copy of this {@link ReferenceColumn}.
	 * 
	 * @param type
	 *        New value of {@link #getTypeContext()}.
	 * @return A new {@link ReferenceColumn} with the same values (and the given ones) as this
	 *         column.
	 */
	protected ReferenceColumn createColumn(TLTypeContext type) {
		return new ReferenceColumn(type, getHeaderI18NKey(), getVisibility(), getAccessor());
	}

	@Override
	protected ControlProvider getControlProvider() {
		return SelectionControlProvider.SELECTION_INSTANCE;
	}

	@Override
	protected void setExcelRenderer(ColumnConfiguration column) {
		// No information here
	}

	@Override
	protected void setRenderer(ColumnConfiguration column) {
		// No information here
	}

}