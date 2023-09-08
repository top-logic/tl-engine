/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.extractor;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.importer.excel.ExcelStructureImportParser;

/**
 * Create the unique
 * {@link com.top_logic.importer.excel.extractor.AbstractExcelStructureExtractor.NodeInfo} for
 * building up a structure in {@link ExcelStructureImportParser}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExcelStructureExtractor<C extends ExcelStructureExtractor.ColumnConfig>
		extends AbstractExcelStructureExtractor<C> {

	/**
	 * Configuration of am {@link ExcelStructureExtractor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<C extends ExcelStructureExtractor.ColumnConfig>
			extends AbstractExcelStructureExtractor.Config<C> {

		/**
		 * Configuration of the columns, indexed by the name of the column.
		 */
		@Key(C.NAME_ATTRIBUTE)
		Map<Object, C> getColumns();
	}

	private final Map<Object, C> _columns;

	/**
	 * Creates a new {@link ExcelStructureExtractor}.
	 */
	public ExcelStructureExtractor(InstantiationContext aContext, Config<C> aConfig) {
		super(aContext, aConfig);
		_columns = aConfig.getColumns();
	}

	@Override
	public Map<Object, C> getColumns() {
		return _columns;
	}

}

