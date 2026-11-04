/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.extractor;

import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Extracts values from the current row of an Excel import.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface Extractor {

    /** Configuration options for an {@link Extractor}. */
    public interface Config extends PolymorphicConfiguration<Transformer<?>> {
	}

	/**
	 * Extracts values from the current row of the given context.
	 *
	 * @param aContext
	 *        The context positioned at the current row.
	 * @param aParser
	 *        The parser performing the import.
	 * @param aLogger
	 *        The logger for reporting problems.
	 * @return The extracted values by their attribute name, may be {@code null}.
	 */
	public Map<String, Object> extract(ExcelContext aContext, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger);

}
