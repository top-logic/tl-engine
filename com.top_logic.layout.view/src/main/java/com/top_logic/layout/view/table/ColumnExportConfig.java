/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.tool.export.ExcelCellRenderer;

/**
 * How a declared column takes part in an export of its table.
 *
 * @see com.top_logic.layout.view.element.TableElement.Config#getExport()
 */
@Abstract
public interface ColumnExportConfig extends ConfigurationItem {

	/** @see #getExport() */
	String EXPORT = "export";

	/** @see #getExportRenderer() */
	String EXPORT_RENDERER = "export-renderer";

	/**
	 * Whether the column is part of an export of the table.
	 *
	 * <p>
	 * An export holds the columns the user sees. Switching this off keeps a displayed column out of
	 * it - a column holding something not meant to leave the application, or a value that means
	 * nothing outside it.
	 * </p>
	 */
	@Name(EXPORT)
	@BooleanDefault(true)
	boolean getExport();

	/**
	 * How a value of the column is written into a cell of an Excel export.
	 *
	 * <p>
	 * Without a renderer, the one the type of the column's values is configured with is used - a
	 * &lt;column-info&gt; annotation of the type or attribute, a format pattern annotated to a
	 * number or date, the renderer registered for the type in the label provider service - and
	 * otherwise the default, which writes a number as number, a date as date and everything else as
	 * its label.
	 * </p>
	 */
	@Name(EXPORT_RENDERER)
	PolymorphicConfiguration<? extends ExcelCellRenderer> getExportRenderer();

}
