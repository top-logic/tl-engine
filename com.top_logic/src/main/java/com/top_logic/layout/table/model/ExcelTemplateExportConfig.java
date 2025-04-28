/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Configuration of an Excel export.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExcelTemplateExportConfig extends ExportConfig {

	/** Configuration name of {@link #getTemplateName()}. */
	String TEMPLATE_NAME = "templateName";

	/** Configuration name of {@link #getAutofitColumns()}. */
	String AUTOFIT_COLUMNS = "autofitColumns";

	/**
	 * Name of the Excel template that is used to export table.
	 */
	@Name(TEMPLATE_NAME)
	@StringDefault("defaultTemplate.xlsx")
	String getTemplateName();

	/**
	 * Setter for {@link #getTemplateName()}.
	 */
	void setTemplateName(String value);

	/**
	 * Flag, if columns have to be auto fitted to matching width.
	 * 
	 * @see ExcelAccess#setValuesDirect(java.io.InputStream, java.io.File,
	 *      com.top_logic.base.office.excel.ExcelValue[], boolean)
	 */
	@Name(AUTOFIT_COLUMNS)
	@BooleanDefault(true)
	boolean getAutofitColumns();

	/**
	 * Setter for {@link #getAutofitColumns()}.
	 */
	void setAutofitColumns(boolean value);

}

