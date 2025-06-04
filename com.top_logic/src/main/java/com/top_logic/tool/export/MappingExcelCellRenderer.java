/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.Collection;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link ExcelCellRenderer} that applies an {@link ExcelMapping} to values before export.
 */
public class MappingExcelCellRenderer extends AbstractExcelCellRenderer
		implements ConfiguredInstance<MappingExcelCellRenderer.Config<?>> {

	private final Config<?> _config;

	private final ExcelMapping _mapping;

	/**
	 * Configuration options for {@link MappingExcelCellRenderer}.
	 */
	public interface Config<I extends MappingExcelCellRenderer> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getExcelMapping()
		 */
		String EXCEL_MAPPING = "excel-mapping";

		/**
		 * The export mapping to apply.
		 */
		@Name(EXCEL_MAPPING)
		@Mandatory
		PolymorphicConfiguration<? extends ExcelMapping> getExcelMapping();

	}

	/**
	 * Creates a {@link MappingExcelCellRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MappingExcelCellRenderer(InstantiationContext context, Config<?> config) {
		_config = config;
		_mapping = context.getInstance(config.getExcelMapping());
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		if (cellValue instanceof Collection<?> collection) {
			return new ExcelValue(excelRow, excelColumn,
				DefaultExcelCellRenderer.getExportValue(context,
					collection.stream().map(e -> _mapping.apply(context, e)).toList()));
		} else {
			return new ExcelValue(excelRow, excelColumn, _mapping.apply(context, cellValue));
		}
	}
}
