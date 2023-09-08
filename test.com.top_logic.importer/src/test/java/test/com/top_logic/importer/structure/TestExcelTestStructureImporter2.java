/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.structure;

import junit.framework.Test;

import test.com.top_logic.importer.AbstractTestImporter;
import test.com.top_logic.importer.data.struct.TestStructAll;

/**
 * Test import of {@link TestStructAll test structure} via excel file.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestExcelTestStructureImporter2 extends TestExcelTestStructureImporter {

    @Override
    protected String getImporterName() {
		return "structureExcel2";
    }

    public static Test suite() {
		return AbstractTestImporter.suite(TestExcelTestStructureImporter2.class);
    }
}

