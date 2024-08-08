/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.CellClassProvider;

/**
 * Mix-in interface for specifying column CSS classes.
 */
@Abstract
public interface WithColumnCssClasses extends ConfigurationItem {

	/**
	 * @see #getCssClass()
	 */
	String CSS_CLASS = "cssClass";

	/**
	 * @see #getCssHeaderClass()
	 */
	String CSS_HEADER_CLASS = "cssHeaderClass";

	/**
	 * @see #getCssClassGroupFirst()
	 */
	String CSS_CLASS_GROUP_FIRST = "cssClassGroupFirst";

	/**
	 * @see #getCssClassGroupFirst()
	 */
	String CSS_CLASS_GROUP_LAST = "cssClassGroupLast";

	/**
	 * @see #getCssClassProvider()
	 */
	String CSS_CLASS_PROVIDER = "cssClassProvider";


	/**
	 * Static CSS class to apply to all content cells of a column.
	 * 
	 * <p>
	 * There are a number of pre-defined CSS classes for highlighting:
	 * </p>
	 * 
	 * <ul>
	 * <li><code>tl-info</code></li>
	 * <li><code>tl-success</code></li>
	 * <li><code>tl-warning</code></li>
	 * <li><code>tl-danger</code></li>
	 * <li><code>tl-debug</code></li>
	 * <li><code>tl-accent</code></li>
	 * </ul>
	 * 
	 * <p>
	 * All these classes can be combined with the class <code>tl-lighter</code> to make the
	 * highlighting less prominent.
	 * </p>
	 * 
	 * @see #getCssClassProvider()
	 * @see ColumnConfiguration#getCssClass()
	 */
	@Name(CSS_CLASS)
	@Nullable
	String getCssClass();

	/**
	 * @see #getCssClass()
	 */
	void setCssClass(String value);

	/**
	 * The CSS class to add to the column's header.
	 * 
	 * <p>
	 * There are a number of pre-defined CSS classes for highlighting:
	 * </p>
	 * 
	 * <ul>
	 * <li><code>tl-info</code></li>
	 * <li><code>tl-success</code></li>
	 * <li><code>tl-warning</code></li>
	 * <li><code>tl-danger</code></li>
	 * <li><code>tl-debug</code></li>
	 * <li><code>tl-accent</code></li>
	 * </ul>
	 * 
	 * <p>
	 * All these classes can be combined with the class <code>tl-lighter</code> to make the
	 * highlighting less prominent.
	 * </p>
	 * 
	 * @see ColumnConfiguration#getCssHeaderClass()
	 */
	@Name(CSS_HEADER_CLASS)
	@Nullable
	String getCssHeaderClass();

	/**
	 * @see #getCssHeaderClass()
	 */
	void setCssHeaderClass(String value);

	/**
	 * CSS class for this column if it is the first column in a column group.
	 * 
	 * @see ColumnConfiguration#getCssClassGroupFirst()
	 */
	@Name(ColumnConfig.CSS_CLASS_GROUP_FIRST)
	@Nullable
	String getCssClassGroupFirst();

	/**
	 * @see #getCssClassGroupFirst()
	 */
	void setCssClassGroupFirst(String value);

	/**
	 * CSS class for this column if it is the last column in a column group.
	 * 
	 * @see ColumnConfiguration#getCssClassGroupLast()
	 */
	@Name(ColumnConfig.CSS_CLASS_GROUP_LAST)
	@Nullable
	String getCssClassGroupLast();

	/**
	 * @see #getCssClassGroupLast()
	 */
	void setCssClassGroupLast(String value);

	/**
	 * Algorithm to dynamically compute CSS classes for certain cells of a column.
	 * 
	 * @see #getCssClass()
	 */
	@Options(fun = AllInAppImplementations.class)
	@DefaultContainer
	@Name(CSS_CLASS_PROVIDER)
	PolymorphicConfiguration<? extends CellClassProvider> getCssClassProvider();

	/**
	 * @see #getCssClassProvider()
	 */
	void setCssClassProvider(PolymorphicConfiguration<? extends CellClassProvider> value);

}
