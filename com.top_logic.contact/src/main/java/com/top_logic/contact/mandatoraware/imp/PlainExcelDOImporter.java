/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.util.Properties;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class PlainExcelDOImporter extends AbstractExcelDOImporter {

    /** 
     * Creates a {@link PlainExcelDOImporter}.
     */
    public PlainExcelDOImporter(Properties aProp) {
        super(aProp);
    }

    /** 
     * Creates a {@link PlainExcelDOImporter}.
     */
	public PlainExcelDOImporter(BinaryData aImportSource, String aSheetName, boolean aDoDeleteWhenDone) {
        super(aImportSource, aSheetName, aDoDeleteWhenDone);
    }

	@Override
	protected String[] getExpectedColumnNames() {
		return null;
	}
	
	@Override
	protected boolean checkColumnFormat(String[] excelColumnNames) {
		return true;
	}
}

