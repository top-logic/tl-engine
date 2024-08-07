/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.model.WithConfigurationProviders;
import com.top_logic.layout.table.provider.AllColumnOptions;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;
import com.top_logic.layout.table.provider.ColumnProviderConfig;

/**
 * Template configuration parameter to provide in app edit of viewable columns.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Abstract
public interface ColumnsTemplateParameters extends TypesTemplateParameters, WithConfigurationProviders {

	/**
	 * Configuration option name for {@link #getDefaultColumns()}.
	 */
	public static final String DEFAULT_COLUMNS = "defaultColumns";

	/**
	 * Columns that shall be displayed by default in the defined order.
	 */
	@Name(DEFAULT_COLUMNS)
	@Format(CommaSeparatedStrings.class)
	@Options(fun = AllColumnOptions.class, mapping = ColumnOptionMapping.class, args = {
		@Ref(TypesTemplateParameters.TYPE),
		@Ref(CONFIGURATION_PROVIDERS),  
		@Ref(steps = {
			@Step(CONFIGURATION_PROVIDERS), 
			@Step(type = ColumnProviderConfig.class, value = ColumnProviderConfig.COLUMN_LABEL) })
	})
	@OptionLabels(value = ColumnOptionLabelProvider.class)
	List<String> getDefaultColumns();

}
