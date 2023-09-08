/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.excel.AbstractExcelFileImportParser.MappingConfig;
import com.top_logic.importer.excel.ExcelImportUtils;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class PatternTransformer implements Transformer<String> {

	public interface Config extends Transformer.Config {
		List<MappingConfig> getMappings();
		String getKey();
	}

	private Config config;

	public PatternTransformer(InstantiationContext context, Config config){
		this.config = config;
	}

	@SuppressWarnings("deprecation")
    @Override
	public String transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> uploadHandler, ImportLogger logger) {
	    Map<String, Object> theMap = ExcelImportUtils.INSTANCE.evaluateMappings(aContext, logger, config.getMappings(), uploadHandler);

		String       theKey     = config.getKey();
		int          theSize    = this.config.getMappings().size();
		List<Object> theObjects = new ArrayList<>(theSize);

        for (int thePos = 0; thePos < theSize; thePos++) {
			Object theValue = theMap.get(Integer.toString(thePos));

			if (theValue != null){
				theKey = theKey + thePos;
			}

			theObjects.add(theValue);
		}

		return Resources.getInstance().getMessage(ResKey.legacy(theKey), theObjects.toArray());
	}
}