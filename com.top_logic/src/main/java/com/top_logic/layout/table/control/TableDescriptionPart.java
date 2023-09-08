/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.util.Comparator;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.tool.export.ExcelCellRenderer;

/**
 * Implementation aspect of configuring a {@link ColumnConfiguration} or
 * {@link TableConfiguration} through inner elements.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TableDescriptionPart extends ColumnConfiguration {

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setAccessor(Object value) {
		setAccessor((Accessor) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setFieldProvider(Object value) {
		setFieldProvider((FieldProvider) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setLabelProvider(Object value) {
		setLabelProvider((LabelProvider) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setFullTextProvider(Object value) {
		setFullTextProvider((LabelProvider) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setResourceProvider(Object value) {
		setResourceProvider((ResourceProvider) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setRenderer(Object value) {
		setRenderer(((Renderer<?>) value).generic());
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setCellRenderer(Object value) {
		this.setCellRenderer((CellRenderer) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setControlProvider(Object value) {
		setControlProvider((ControlProvider) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setFilterProvider(Object value) {
		setFilterProvider((TableFilterProvider) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setExcelRenderer(Object value) {
		setExcelRenderer((ExcelCellRenderer) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setHeadControlProvider(Object value) {
		setHeadControlProvider((ControlProvider) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setComparator(Object value) {
		setComparator((Comparator<?>) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setSortKeyProvider(Object value) {
		setSortKeyProvider((Mapping) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setEditControlProvider(Object value) {
		setEditControlProvider((ControlProvider) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setPreloadContribution(Object value) {
		setPreloadContribution((PreloadContribution) value);
	}

	/**
	 * Internal configuration parsing method. Must only be called by reflection during component
	 * loading.
	 */
	@Deprecated
	public void setDescendingComparator(Object value) {
		setDescendingComparator((Comparator<?>) value);
	}

}
