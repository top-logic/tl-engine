/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * @author     <a href="mailto:kbu@top-logic.com">fma</a>
 */
public class StringToNumberByListTransformer implements Transformer<Long> {
	private final Config _config;
    private boolean _mandatory;

	public interface Config extends Transformer.Config {
		@MapBinding(key="value",attribute="element")
		Map<String, String> getMappings();
        @BooleanDefault(true)
        @Override
        boolean isMandatory();
	}
	
	public StringToNumberByListTransformer(InstantiationContext context, Config config){
		this._config = config;
		this._mandatory = config.isMandatory();
	}

	@Override
	public Long transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> handler, ImportLogger logger) {
		String key = StringTransformer.getString(aContext, columnName, false, logger);
		Long   res = null;
		
		if (key != null){
			res = getNumber(key);

			if (res == null) {
			    throw new TransformException(I18NConstants.NO_NUMBER, aContext.row() + 1, columnName, key);
			}
		}
		else if (this._mandatory) {
		    throw new TransformException(I18NConstants.NO_NUMBER, aContext.row() + 1, columnName, key);
		}

		return res;
	}
	
	protected Long getNumber(String key) {
		String numberAsString = _config.getMappings().get(key);

		return (numberAsString != null) ? Long.parseLong(numberAsString) : null;
	}
}
