/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class Integer2StringTransformer implements Transformer<String> {

    public static final String EMPTY = "<empty>";

    public interface Config extends Transformer.Config {
        @BooleanDefault(true)
        boolean isMissingColumnAllowed();
        @IntDefault(100000000)
        int getMaxLength();
    }

    private boolean missingColumnAllowed;
    private int     maxLength;
    private boolean mandatory;

    public Integer2StringTransformer(InstantiationContext context, Config config) {
        missingColumnAllowed = config.isMissingColumnAllowed();
        maxLength = config.getMaxLength();
        mandatory = config.isMandatory();

    }

    @Override
    public String transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> handler, ImportLogger logger) {
        final String value = getString(aContext, columnName, missingColumnAllowed, maxLength, mandatory, logger);
        if (mandatory && isEmpty(value)) {
            throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, columnName, aContext.getString(columnName));
        }
        return value;
    }

    public static String getString(ExcelContext aContext, String columnName, boolean missingColumnAllowed, boolean mandatory, ImportLogger logger) {
        return getString(aContext, columnName, missingColumnAllowed, 100000000, mandatory, logger);
    }

    public static String getString(ExcelContext aContext, String columnName, boolean missingColumnAllowed, int maxLength, boolean mandatory, ImportLogger logger) {
        if (!aContext.hasColumn(columnName)) {
            if (missingColumnAllowed) {
                return null;
            }
            else {
                // FIXME MGA/KBU error handling: really throw ex?
                throw new RuntimeException("Column " + columnName + " is missing in " + aContext);
            }
        }

        Object theValue = aContext.column(columnName).value();
        String result;

        if (theValue instanceof Number) {
            result = Integer.toString(((Number) theValue).intValue());
        }
        else {
            result = (theValue instanceof String) ? (String) theValue : aContext.getString(columnName);
        }

        if (result != null) {
            int length = result.length();

            if (length > maxLength) {
                throw new TransformException(I18NConstants.STRING_SHORTENED, aContext.row() + 1, columnName, result, length, maxLength);
            }
        }

        return result;
    }

    public static boolean isEmpty(Object aString) {
        return StringServices.isEmpty(aString) || EMPTY.equals(aString);
    }
}
