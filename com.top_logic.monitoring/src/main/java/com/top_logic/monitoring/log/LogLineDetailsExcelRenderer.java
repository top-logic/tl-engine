/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import org.apache.poi.ss.usermodel.VerticalAlignment;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.tool.export.AbstractExcelCellRenderer;
import com.top_logic.tool.export.ExcelCellRenderer;

/**
 * {@link ExcelCellRenderer} that exports the {@link LogLine#PROPERTY_DETAILS details property} of a
 * {@link LogLine}.
 */
public class LogLineDetailsExcelRenderer extends AbstractExcelCellRenderer
		implements ConfiguredInstance<LogLineDetailsExcelRenderer.Config> {

	/** {@link ConfigurationItem} for the {@link LogLineDetailsExcelRenderer}. */
	public interface Config extends PolymorphicConfiguration<LogLineDetailsExcelRenderer> {

		/** The default value for {@link #getColumnWidth()}. */
		int DEFAULT_COLUMN_WIDTH = 150;

		/**
		 * The width of this column.
		 * <p>
		 * This is also the width of the comment on the cell.
		 * </p>
		 */
		@IntDefault(DEFAULT_COLUMN_WIDTH)
		int getColumnWidth();

	}

	private final Config _config;

	/** {@link TypedConfiguration} constructor for {@link LogLineDetailsExcelRenderer}. */
	public LogLineDetailsExcelRenderer(@SuppressWarnings("unused") InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		String details = (String) cellValue;
		if (StringServices.isEmpty(details)) {
			return emptyValue(excelRow, excelColumn);
		}
		ExcelValue excelValue = new ExcelValue(excelRow, excelColumn, details);
		excelValue.setVerticalAlignment(VerticalAlignment.TOP);
		return excelValue;
	}

	private ExcelValue emptyValue(int excelRow, int excelColumn) {
		return new ExcelValue(excelRow, excelColumn, null);
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}
