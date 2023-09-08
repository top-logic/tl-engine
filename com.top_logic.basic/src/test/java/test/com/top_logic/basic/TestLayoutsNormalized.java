/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static com.top_logic.basic.io.FileUtilities.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;

import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;

/**
 * Test that checks that the layout files are normalized.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestLayoutsNormalized extends TestCase {

	public void testLayouts() {
		BufferingProtocol log = new BufferingProtocol();

		/* The file needs to be canonicalized to be able to reliably access its parent without
		 * getting an NPE. The parent is not accessed in this method but in some of the called
		 * methods. When this file is canonicalized, all files derived from it are okay, too. Making
		 * it unnecessary to canonicalize each of them individually. */
		File layoutsDirectory = canonicalize(AbstractBasicTestAll.potentiallyNotExistingLayoutDir());
		List<File> layoutFiles;
		try {
			layoutFiles = getLayoutFiles(layoutsDirectory);
		} catch (IOException ex) {
			log.error("Unable to get layout files from "
				+ relativeToWorkspace(layoutsDirectory), ex);
			return;
		}

		DocumentBuilder builder = DOMUtil.getDocumentBuilder();
		for (File layoutFile: layoutFiles) {
			try {
				checkNormalization(log, builder, layoutFile);
			} catch (Throwable ex) {
				log.error("Unable to get test for layout file " + layoutFile, ex);
			}
		}

		if (log.hasErrors()) {
			fail(log.getErrors().stream().collect(Collectors.joining("\n")));
		}
	}

	private String relativeToWorkspace(File layoutsDirectory) {
		String absolutePath = layoutsDirectory.getAbsolutePath();
		int relevantIndex = canonicalize(new File(".")).getParentFile().getAbsolutePath().length() + 1;
		return absolutePath.substring(relevantIndex);
	}

	private void checkNormalization(Log log, DocumentBuilder builder, File layoutFile) throws Exception {
		byte[] actualContent = FileUtilities.getBytesFromFile(layoutFile);
		Document document = builder.parse(new ByteArrayInputStream(actualContent));

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		XMLPrettyPrinter.dump(buffer, document);

		byte[] expectedContent = buffer.toByteArray();
		if (!Arrays.equals(expectedContent, actualContent)) {
			checkNormalization(log, layoutFile, expectedContent, actualContent);
		}
	}

	private void checkNormalization(Log log, File layoutFile, byte[] expectedContent, byte[] actualContent) {
		if (!new String(expectedContent).equals(new String(actualContent))) {
			log.error(getRelativeName(layoutFile) + ": Not normalized.");
		}
	}

	private static String getRelativeName(File layoutFile) {
		String layoutDirPath = AbstractBasicTestAll.MODULE_LAYOUT.getLayoutDir().getAbsolutePath();
		String canonicalPath = layoutFile.getAbsolutePath();

		return canonicalPath.substring(layoutDirPath.length() + 1);
	}

	private List<File> getLayoutFiles(File layoutsDirectory) throws IOException {
		if (!layoutsDirectory.exists()) {
			return Collections.emptyList();
		}
		ArrayList<File> allFiles = new ArrayList<>();
		addLayoutFiles(layoutsDirectory, allFiles);
		return allFiles;
	}

	private void addLayoutFiles(File file, List<File> files) throws IOException {
		if (file.isDirectory()) {
			for (File content : FileUtilities.listFiles(file)) {
				if (content.getName().startsWith(".")) {
					continue;
				}
				if (content.isFile() && !content.getName().endsWith(".xml")) {
					continue;
				}

				addLayoutFiles(content, files);
			}
		} else {
			files.add(file);
		}

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestLayoutsNormalized}.
	 */
	public static Test suite() {
		return new TestSuite(TestLayoutsNormalized.class);
	}

}

