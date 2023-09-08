/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tools.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Test case for {@link ResourceFile}.
 */
@SuppressWarnings("javadoc")
public class TestResourceFile extends TestCase {

	public void testReadWrite() throws IOException {
		Properties orig = new Properties();
		orig.setProperty("foo", "bar \u20AC");
		orig.setProperty("bar", "   b a  z   z   ");
		orig.setProperty("xxx", "yyy\naaa");

		File javaFile = File.createTempFile("resources-standard", ".properties");
		orig.store(new FileOutputStream(javaFile), "Java standard format");

		ResourceFile loadedResourceFile = new ResourceFile(javaFile);
		assertEquals(orig, loadedResourceFile.toProperties());

		ResourceFile normalizedResourceFile = new ResourceFile(orig);

		File normalizedFile = File.createTempFile("resources-normalized", ".properties");
		normalizedResourceFile.saveAs(normalizedFile);

		Properties loaded = new Properties();
		loaded.load(new FileInputStream(normalizedFile));

		assertEquals(orig, loaded);

		ResourceFile loadedAgain = new ResourceFile(normalizedFile);

		assertEquals(orig, loadedAgain.toProperties());
	}

}
