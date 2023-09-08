/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.model.AllColumnsForConfiguredTypes;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;

/**
 * Template configuration parameters for a {@link FormContextModificator}.
 * 
 * @see TemplateFormContextModificator
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface FormContextModificatorTemplateParameters {

	/**
	 * Configuration option name for {@link #getReadOnlyColumns()}.
	 */
	public static final String READ_ONLY_COLUMNS = "readOnlyColumns";

	/**
	 * Name of columns that are read only.
	 */
	@Name(READ_ONLY_COLUMNS)
	@Format(CommaSeparatedStrings.class)
	@Options(fun = AllColumnsForConfiguredTypes.class, mapping = ColumnOptionMapping.class)
	@OptionLabels(value = ColumnOptionLabelProvider.class)
	List<String> getReadOnlyColumns();

}
