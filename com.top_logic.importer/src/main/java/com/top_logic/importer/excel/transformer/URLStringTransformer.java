/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Special string transformer for URLs.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class URLStringTransformer<C extends URLStringTransformer.Config> extends StringTransformer<C> {

    public interface Config extends StringTransformer.Config {
        // Nothing in here....
    }

    /** 
     * Creates a {@link URLStringTransformer}.
     */
    public URLStringTransformer(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
    }

    @Override
    public String transform(ExcelContext aContext, String aColumnName, AbstractExcelFileImportParser<?> aHandler, ImportLogger aLogger) {
        String theURL = super.transform(aContext, aColumnName, aHandler, aLogger);

        if (!StringServices.isEmpty(theURL)) {
            int idx = theURL.indexOf("://");
            int length = theURL.length();

            if (idx < 1 || idx > length - 4) {
                throw new TransformException(I18NConstants.STRING_URL_INVALID, aContext.row() + 1, aColumnName, theURL);
            }
        }

        return theURL;
    }
}

