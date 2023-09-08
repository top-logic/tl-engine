/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Transform a cell value into a float object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DoubleTransformer implements Transformer<Double> {

    private boolean mandatory;

    /** 
     * Creates a {@link DoubleTransformer}.
     */
    public DoubleTransformer(boolean isMandatory) {
        this.mandatory = isMandatory;
    }
    
	public DoubleTransformer(InstantiationContext aContext, Config aConfig) {
		this(aConfig.isMandatory());
	}

    @Override
    public Double transform(ExcelContext aContext, String aColumnName, AbstractExcelFileImportParser<?> aHandler, ImportLogger aLogger) {
    	try {
    	    Double theLong = aContext.getDouble(aColumnName);
    		
    		if (mandatory && theLong == null) {
    			throw new TransformException(I18NConstants.DOUBLE_PARSE_ERROR, aContext.row() + 1, aColumnName, aContext.getString(aColumnName));
    		}
    		
    		return theLong;
    	}
    	catch (NumberFormatException ex) {
			throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, aColumnName, aContext.getString(aColumnName));
    	}
    }
}

