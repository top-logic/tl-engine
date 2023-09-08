/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.Collection;
import java.util.Date;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.FormField;

/**
 * {@link ExcelCellRenderer} that creates a {@link ExcelValue} with a given.
 * 
 * <p>
 * It is expected that the value of the table model is a simple value, e.g. {@link Double},
 * {@link String}, or {@link Date}. Complex values like {@link Collection} or {@link FormField} are
 * not supported.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormattedValueExcelRenderer extends AbstractExcelCellRenderer {

	/**
	 * Configuration of a {@link FormattedValueExcelRenderer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<FormattedValueExcelRenderer> {

		/** Name for configuration of {@link #getFormat()}. */
		String FORMAT_ATTRIBUTE = "format";

		/**
		 * The data format for the excel cell.
		 */
		@Mandatory
		@Name(FORMAT_ATTRIBUTE)
		String getFormat();
	}

	private final String _format;

	/**
	 * Creates a new {@link FormattedValueExcelRenderer} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link FormattedValueExcelRenderer}.
	 */
	public FormattedValueExcelRenderer(InstantiationContext context, Config config) {
		this(config.getFormat());
	}

	/**
	 * Creates a new {@link FormattedValueExcelRenderer}.
	 * 
	 * @param format
	 *        format to set in {@link ExcelValue#setDataFormat(String)}
	 */
	public FormattedValueExcelRenderer(String format) {
		_format = format;
	}

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		ExcelValue excelValue = new ExcelValue(excelRow, excelColumn, cellValue);
		excelValue.setDataFormat(_format);
		return excelValue;
	}
}

