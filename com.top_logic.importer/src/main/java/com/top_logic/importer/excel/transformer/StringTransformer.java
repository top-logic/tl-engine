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
 * Transforms a cell value into a string.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class StringTransformer<C extends StringTransformer.Config> implements Transformer<String> {
	
	private int maxLength;
    private List<String> emptyValues;
    private boolean trim;
    private boolean lengthError;
    private boolean mandatory;

    /** Configuration options for {@link StringTransformer}. */
    public interface Config extends Transformer.Config {
		/** Maximum length of the text, longer texts are cut. */
		@IntDefault(100000000)
		int getMaxLength();
		/** Whether a text exceeding the maximum length is an error. */
		@BooleanDefault(false)
		public boolean isLengthError();
		/** Whether white space at the beginning and the end is removed. */
		@BooleanDefault(true)
		public boolean isTrim();
		/** Values that are interpreted as empty. */
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

	/**
	 * Reads the given column as text without length restriction.
	 *
	 * @see #getString(ExcelContext, String, boolean, int, List, boolean, boolean, ImportLogger)
	 */
	public static String getString(ExcelContext aContext, String columnName, boolean mandatory, ImportLogger logger){
		return getString(aContext,  columnName,  mandatory, 100000000, null, true, false, logger);
	}
	
	/**
	 * Reads the given column as text.
	 *
	 * @param aContext
	 *        The context positioned at the row to read.
	 * @param columnName
	 *        The column to read.
	 * @param mandatory
	 *        Whether an empty value is an error.
	 * @param maxLength
	 *        The maximum length of the text, longer texts are cut.
	 * @param emptyStrings
	 *        Values that are interpreted as empty, may be {@code null}.
	 * @param trim
	 *        Whether white space at the beginning and the end is removed.
	 * @param isLengthError
	 *        Whether a text exceeding the maximum length is an error.
	 * @param logger
	 *        The logger for reporting cut texts, may be {@code null}.
	 * @return The read text, or {@code null}, if the cell is empty.
	 */
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

	/** Whether the given text is empty or one of the given empty values. */
	public static boolean isEmpty(String aString, List<String> emptyStrings) {
        return StringServices.isEmpty(aString) || emptyStrings != null && emptyStrings.contains(aString);
    }
}
