/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.text;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.importer.AbstractTestImporter;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.base.ListDataImportPerformer;
import com.top_logic.importer.text.TextFileImportParser;
import com.top_logic.importer.text.TextFileImportParser.Config;

/**
 * Test import of {@link PersonContact}s via text file.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestPersonContactTextImporter extends AbstractTestImporter {

    @Override
    protected String getFileName() {
        return "/data/PersonContacts.txt";
    }

    @Override
    protected String getWrongFileName() {
        return "/data/PersonContactsInvalid.txt";
    }

    @Override
    protected String getImporterName() {
		return "personsText";
    }

    @Override
    protected Map<String, Object> getDataMap(List<Map<String, Object>> someData) {
        Config theConfig = ((TextFileImportParser) this.getParser()).getConfig();

        return new MapBuilder<String, Object>()
                .put(AbstractImportPerformer.VALUE_MAP, someData)
                .put(ListDataImportPerformer.PROP_KEY_MAPPING, ListDataImportPerformer.getIDsFromMapping(theConfig.getMappings().values(), theConfig.getUID()))
                .toMap();
    }

    public static Test suite() {
        return AbstractTestImporter.suite(TestPersonContactTextImporter.class);
    }
}

