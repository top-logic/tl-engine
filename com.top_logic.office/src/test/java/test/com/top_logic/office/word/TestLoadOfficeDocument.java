/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.office.word;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Test case for loading a word document.
 */
@SuppressWarnings("javadoc")
public class TestLoadOfficeDocument extends TestCase {

	public void testLoad() throws Docx4JException, IOException {
		WordprocessingMLPackage document =
			WordprocessingMLPackage.load(getClass().getResourceAsStream("document.docx"));
		assertNotNull(document);

		new Save(document).save(new FileOutputStream(File.createTempFile("test-word", ".docx")));
	}

}
