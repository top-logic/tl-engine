/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.structure;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.importer.AbstractTestImporter;
import test.com.top_logic.importer.data.struct.TestStructAll;
import test.com.top_logic.importer.data.struct.TestStructFactory;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler.ImportMessage;

/**
 * Test import of {@link TestStructAll test structure} via XML file.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestXMLTestStructureImporter extends AbstractTestImporter {

    @Override
    protected String getFileName() {
        return "/data/TestStructure.xml";
    }

    @Override
    protected String getWrongFileName() {
        return "/data/PersonContacts.txt";
    }

    @Override
    protected String getImporterName() {
		return "structureXML";
    }

    @SuppressWarnings("unchecked")
    @Override
	protected List<Map<String, Object>> loadData(BinaryData aFile, AbstractImportParser aHandler)
			throws FileNotFoundException {
    	AssistentComponent  theComp   = AbstractTestImporter.createAssistentComponent();
    	List<ImportMessage> theResult = aHandler.validateFile(theComp, aFile);
    	
    	this.logErrorMessages(theResult);
    	
    	return (List<Map<String, Object>>) theComp.getData(AbstractImportPerformer.VALUE_MAP);
    }

    @Override
    protected Map<String, Object> getDataMap(List<Map<String, Object>> someData) {
        return new MapBuilder<String, Object>()
                .put(AbstractImportPerformer.VALUE_MAP, someData)
                .toMap();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        TestStructFactory.getInstance();
    }

	@SuppressWarnings("javadoc")
	public static Test suite() {
		return AbstractTestImporter.suite(TestXMLTestStructureImporter.class);
    }
}

