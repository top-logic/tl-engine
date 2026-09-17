/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * The {@code <columns>} of a table: what it displays, in display order.
 *
 * <p>
 * An entry declares a column - {@code <column attribute="..."/>} for one over a model attribute,
 * {@code <computed-column .../>} for one over a computed value - or a whole set of them, as
 * {@code <embedded-columns reference="..."/>} does for the columns of an object the row points to.
 * Every table describing its columns uses this one configuration, so a kind of column added here is
 * offered by all of them.
 * </p>
 */
public interface ColumnsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getColumns()}. */
	String COLUMNS = "columns";

	/**
	 * The declarations contributing the columns to display, in display order.
	 */
	@Name(COLUMNS)
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	List<PolymorphicConfiguration<? extends ColumnDeclaration>> getColumns();

}
