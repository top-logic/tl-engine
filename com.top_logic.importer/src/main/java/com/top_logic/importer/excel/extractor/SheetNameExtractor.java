/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.extractor;

import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.excel.WorkbookExcelContext;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Extract the sheet name (when the used context is a {@link WorkbookExcelContext}).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SheetNameExtractor<C extends SheetNameExtractor.Config> implements Extractor {

	@SuppressWarnings("javadoc")
	public interface Config extends Extractor.Config {

		/** Attribute name to store the information to. */
		String getAttribute();
	}

	private final C config;

	/**
	 * Creates a {@link SheetNameExtractor}.
	 */
	public SheetNameExtractor(InstantiationContext aContext, C aConfig) {
		this.config = aConfig;
	}

	@Override
	public Map<String, Object> extract(ExcelContext aContext, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
		if (aContext instanceof WorkbookExcelContext) {
			String theSheetName = ((WorkbookExcelContext) aContext).getCurrentSheetName();

			return new MapBuilder<String, Object>()
					.put(this.config.getAttribute(), theSheetName)
					.toMap();
		}
		else {
			return null;
		}
	}

	protected C getConfig() {
		return (config);
	}
}

