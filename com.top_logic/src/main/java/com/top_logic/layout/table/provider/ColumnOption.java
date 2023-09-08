/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.layout.editor.config.ColumnsTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Type of the produced options of {@link Options#fun()} when configuring columns for an in app layout.
 * 
 * @see ColumnsTemplateParameters
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface ColumnOption {

	/**
	 * Column label.
	 */
	String getLabel();

	/**
	 * Technical column name.
	 */
	String getTechnicalName();

}
