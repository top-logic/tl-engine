/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.excel;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.importer.AbstractTestImporter;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.base.ListDataImportPerformer;
import com.top_logic.importer.excel.ExcelListImportParser;
import com.top_logic.importer.excel.ExcelListImportParser.Config;

/**
 * Test import of {@link PersonContact}s via excel file.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestPersonContactExcelImporter extends AbstractTestImporter {

    @Override
    protected String getFileName() {
        return "/data/PersonContacts.xlsx";
    }

    @Override
    protected String getWrongFileName() {
        return "/data/PersonContacts.txt";
    }

    @Override
    protected String getImporterName() {
		return "personsExcel";
    }

    @Override
    protected Map<String, Object> getDataMap(List<Map<String, Object>> someData) {
        Config theConfig = ((ExcelListImportParser<?>) this.getParser()).getConfig();

        return new MapBuilder<String, Object>()
                .put(AbstractImportPerformer.VALUE_MAP, someData)
                .put(ListDataImportPerformer.PROP_KEY_MAPPING, ListDataImportPerformer.getIDsFromMapping(theConfig.getMappings(), theConfig.getUID()))
                .toMap();
    }

	@SuppressWarnings("javadoc")
    public static Test suite() {
        return AbstractTestImporter.suite(TestPersonContactExcelImporter.class);
    }
}

