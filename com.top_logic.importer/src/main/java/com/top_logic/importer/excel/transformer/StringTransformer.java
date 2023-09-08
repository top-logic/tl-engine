/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.util.List;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class StringTransformer<C extends StringTransformer.Config> implements Transformer<String> {
	
	private int maxLength;
    private List<String> emptyValues;
    private boolean trim;
    private boolean lengthError;
    private boolean mandatory;

    public interface Config extends Transformer.Config {
		@IntDefault(100000000)
		int getMaxLength();
		@BooleanDefault(false)
		public boolean isLengthError();
		@BooleanDefault(true)
		public boolean isTrim();
		@Format(CommaSeparatedStrings.class)
		public List<String> getEmptyValues();
	}

//	protected C config;

	public StringTransformer(InstantiationContext context, C config){
	    this(config.isMandatory(), config.getMaxLength(), config.getEmptyValues(), config.isTrim(), config.isLengthError());
	}

    public StringTransformer(boolean aMandatory, int aMaxLength, List<String> aEmptyValues, boolean aTrim, boolean aLengthError) {
        this.maxLength = aMaxLength;
        this.emptyValues = aEmptyValues;
        this.trim = aTrim;
        this.lengthError = aLengthError;
        this.mandatory = aMandatory;
    }

    @Override
	public String transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> handler, ImportLogger logger) {
		return getString(aContext, columnName, this.mandatory, this.maxLength, this.emptyValues, this.trim, this.lengthError, logger);
	}

	public static String getString(ExcelContext aContext, String columnName, boolean mandatory, ImportLogger logger){
		return getString(aContext,  columnName,  mandatory, 100000000, null, true, false, logger);
	}
	
	public static String getString(ExcelContext aContext, String columnName, boolean mandatory, int maxLength, List<String> emptyStrings, boolean trim, boolean isLengthError, ImportLogger logger){
		Object theValue = aContext.hasColumn(columnName) ? aContext.column(columnName).value() : null;
		String result   = theValue == null ? null : (theValue instanceof String) ? (String) theValue: aContext.getString(columnName);

		if (isEmpty(result, emptyStrings)) {
			if (mandatory) {
				throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, columnName, theValue);
			}
			
			return null;
		}
		
		result = trim ? result.trim() : result;
		int length = result.length();
		if (length > maxLength) {
			if (isLengthError) {
				throw new TransformException(I18NConstants.STRING_SHORTENED, aContext.row() + 1, columnName, result, length, maxLength);
			}
			else if (logger != null) {
				// TODO KBU think about log level (config)
				logger.info(StringTransformer.class, I18NConstants.STRING_SHORTENED, aContext.row() + 1, columnName, result, length, maxLength);
			}
			return result.substring(0,maxLength);
		}

		return result;
	}

	public static boolean isEmpty(String aString, List<String> emptyStrings) {
        return StringServices.isEmpty(aString) || emptyStrings != null && emptyStrings.contains(aString);
    }
}
