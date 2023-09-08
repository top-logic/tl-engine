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
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SubStringTransformer<C extends SubStringTransformer.Config> extends StringTransformer<C> {
    
    public interface Config extends StringTransformer.Config {
        public String getEndChar();
    }

    private String endChar;

    public SubStringTransformer(InstantiationContext context, C config){
        super(context, config);

        this.endChar = config.getEndChar();
    }

    @Override
    public String transform(ExcelContext aContext, String aColumnName, AbstractExcelFileImportParser<?> aHandler, ImportLogger aLogger) {
        String theString = super.transform(aContext, aColumnName, aHandler, aLogger);

        if (theString != null) {
            int thePos = theString.indexOf(this.endChar);

            if (thePos > 0) {
                theString = theString.substring(0, thePos);
            }
        }
        return theString;
    }
}

