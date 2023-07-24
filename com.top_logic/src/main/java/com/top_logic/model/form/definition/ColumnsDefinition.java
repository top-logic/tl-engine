/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.model.form.implementation.ColumnsDefinitionTemplateProvider;

/**
 * Definition of a columns layout. It can contain other {@link FormElement}s.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("columnsLayout")
public interface ColumnsDefinition extends ContainerDefinition<ColumnsDefinitionTemplateProvider> {

	/** Configuration name for the value of the {@link #getColumns()}. */
	String COLUMNS = "columns";

	/** Configuration name for the value of the {@link #getLineBreak()}. */
	String LINE_BREAK = "lineBreak";

	/**
	 * Sets whether the elements are rendered over multiple rows or the number of columns is kept
	 * instead of adjust to the viewport size.
	 */
	void setLineBreak(Boolean lineBreak);

	/**
	 * Returns whether the elements are rendered over multiple rows or the number of columns is kept
	 * instead of adjust to the viewport size.
	 */
	@Name(LINE_BREAK)
	@BooleanDefault(true)
	Boolean getLineBreak();
}