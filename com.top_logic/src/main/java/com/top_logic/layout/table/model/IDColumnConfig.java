/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;
import com.top_logic.layout.table.renderer.IDColumnTableCellRenderer;
import com.top_logic.model.annotate.ui.TLIDColumn;

/**
 * Configuration to set the id column of a table.
 * 
 * <p>
 * The id column is the column for which an icon of the row objects type is added before its value
 * and which is rendered as a link to its row object.
 * </p>
 * 
 * @see IDColumnTableCellRenderer
 * @see TLIDColumn
 * @see IDColumnTableConfigurationProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface IDColumnConfig extends ConfigurationItem {

	/**
	 * Configuration name.
	 */
	public static final String ID_COLUMN = "id-column";

	/**
	 * @see #getValue()
	 */
	String VALUE = "value";

	/**
	 * The technical column name for which an icon of the row objects type is added before its value
	 * and which is rendered as a link to its row object.
	 * 
	 * <p>
	 * If no id column is annotated then the <code>name</code> column is used by default, if it
	 * exists.
	 * </p>
	 */
	@Name(VALUE)
	@Options(fun = AllColumnsForConfiguredTypes.class, mapping = ColumnOptionMapping.class)
	@OptionLabels(value = ColumnOptionLabelProvider.class)
	@Nullable
	String getValue();

}
