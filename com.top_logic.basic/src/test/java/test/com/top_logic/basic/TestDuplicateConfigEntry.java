/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * Tests that an {@link Exception} is thrown when the (untyped) XML configuration contains a
 * duplicate section.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestDuplicateConfigEntry extends TestCase {

	private static final String CONFIG_FILE_NAME_SUFFIX = ".configWithDuplicates.xml";

	private static final String KEY_DUPLICATE_SECTION = "duplicate";

	private static final String X_PATH_DUPLICATE_CUSTOM_ENTRY = "/root/custom";

	public void testDuplicateSection() {
		try {
			xmlProperties().getProperties(TestDuplicateConfigEntry.class).getProperty(KEY_DUPLICATE_SECTION);
		} catch (Exception ex) {
			// Good
			BasicTestCase.assertContains("duplicate", ex.getMessage());
			return;
		}
		fail("Expected the access to a duplicate section to fail, but it did not fail.");
	}

	public void testDuplicateSectionWithDefault() {
		try {
			xmlProperties().getProperties(TestDuplicateConfigEntry.class).getProperty(KEY_DUPLICATE_SECTION,
				"SomeDefaulValue");
		} catch (Exception ex) {
			// Good
			BasicTestCase.assertContains("duplicate", ex.getMessage());
			return;
		}
		fail("Expected the access to a duplicate section to fail, but it did not fail.");
	}

	private static XMLProperties xmlProperties() throws IOException {
		return XMLProperties.createXMLProperties(getSpecialConfig());
	}

	private static BinaryData getSpecialConfig() {
		String fileName = TestDuplicateConfigEntry.class.getSimpleName() + CONFIG_FILE_NAME_SUFFIX;
		return BinaryDataFactory
			.createBinaryData(FileUtilities.urlToFile(TestDuplicateConfigEntry.class.getResource(fileName)));
	}

}

