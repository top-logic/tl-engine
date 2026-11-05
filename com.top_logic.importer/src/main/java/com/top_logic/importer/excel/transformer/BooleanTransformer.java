/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Transform a cell value into a boolean object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class BooleanTransformer implements Transformer<Boolean> {

	/** Configuration options for {@link BooleanTransformer}. */
	public static interface Config extends Transformer.Config {

		/** Default value of {@link #getNull()}. */
		String NULL_DEFAULT_VALUE = "?,null,<EMPTY>";

		/** Default value of {@link #getTrue()}. */
		String TRUE_DEFAULT_VALUE = "X,JA,TRUE,YES,WAHR,Y,J";

		/** Default value of {@link #getFalse()}. */
		String FALSE_DEFAULT_VALUE = "O,NEIN,FALSE,NO,FALSCH,N,-";

		/** Default value of {@link #getEmpty()}. */
		boolean EMPTY_DEFAULT_VALUE = false;

		/** Default value of {@link #isTrim()}. */
		boolean TRIM_DEFAULT_VALUE = true;

		/** Configuration name of {@link #getNull()}. */
		String NULL_NAME = "null";

		/** Configuration name of {@link #getTrue()}. */
		String TRUE_NAME = "true";

		/** Configuration name of {@link #getFalse()}. */
		String FALSE_NAME = "false";

		/** Configuration name of {@link #getEmpty()}. */
		String EMPTY_NAME = "empty";

		/** Configuration name of {@link #isTrim()}. */
		String TRIM_NAME = "trim";

		/** Cell values that are interpreted as "no value". */
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(NULL_DEFAULT_VALUE)
		@Name(NULL_NAME)
		List<String> getNull();

		/** Cell values that are interpreted as "true". */
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(TRUE_DEFAULT_VALUE)
		@Name(TRUE_NAME)
		List<String> getTrue();

		/** Cell values that are interpreted as "false". */
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(FALSE_DEFAULT_VALUE)
		@Name(FALSE_NAME)
		List<String> getFalse();

		/** Value to use for an empty cell. */
		@BooleanDefault(EMPTY_DEFAULT_VALUE)
		@Name(EMPTY_NAME)
		Boolean getEmpty();

		/**
		 * Whether boolean values must be trimmed, i.e. whether e.g. " false " must be treated as
		 * "false".
		 */
		@BooleanDefault(TRIM_DEFAULT_VALUE)
		@Name(TRIM_NAME)
		boolean isTrim();
	}

	private Boolean              emptyValue;
	private Map<String, Boolean> valueMap;
	private boolean mandatory;

	private final boolean _trim;

    /** 
     * Creates a {@link BooleanTransformer}.
     */
	public BooleanTransformer(InstantiationContext context, Config config) {
		this(config.isMandatory(), config.getNull(), config.getTrue(), config.getFalse(), config.getEmpty(),
			config.isTrim());
	}

	public BooleanTransformer(boolean aMandatory) {
		this(aMandatory,
			toList(Config.NULL_NAME, Config.NULL_DEFAULT_VALUE),
			toList(Config.TRUE_NAME, Config.TRUE_DEFAULT_VALUE),
			toList(Config.FALSE_NAME, Config.FALSE_DEFAULT_VALUE),
			Config.EMPTY_DEFAULT_VALUE, Config.TRIM_DEFAULT_VALUE);
	}

	private static List<String> toList(String property, String commaSeparatedValue) {
		try {
			return CommaSeparatedStrings.INSTANCE.getValue(property, commaSeparatedValue);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	public BooleanTransformer(boolean aMandatory, List<String> aNull, List<String> aTrue, List<String> aFalse,
			Boolean anEmpty, boolean trim) {
		this.valueMap = new HashMap<>();
		for (String theString : aNull) {
			this.valueMap.put(theString, null);
		}
		for (String theString : aTrue) {
			this.valueMap.put(theString, true);
		}
		for (String theString : aFalse) {
			this.valueMap.put(theString, false);
		}
		
		this.emptyValue = anEmpty;
		
		this.mandatory = aMandatory; 
		_trim = trim;
	}

    @Override
    public Boolean transform(ExcelContext aContext, String aColumnName, AbstractExcelFileImportParser<?> aHandler, ImportLogger aLogger) {
    	String theVal = aContext.getString(aColumnName);
    	
    	if (StringServices.isEmpty(theVal)) {
    	    if (this.mandatory) {
                throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, aColumnName, theVal);
            }
    	    else { 
    	        return this.emptyValue;
    	    }
    	}
    	
    	theVal = theVal.toUpperCase();
		if (_trim) {
			theVal = theVal.trim();
		}
    	if (!this.valueMap.containsKey(theVal)) {
    		throw new TransformException(I18NConstants.BOOLEAN_PARSE_ERROR, aContext.row() + 1, aColumnName, theVal);
    	}
    	
    	Boolean theBool = this.valueMap.get(theVal);
    	
    	if (theBool == null && this.mandatory) {
    		throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, aColumnName, theVal);
    	}
    	
    	return theBool;
    }
}

