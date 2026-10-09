/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.docs.DevDoc;
import com.top_logic.basic.docs.DevDocs;

/**
 * Test that checks the developer documentation of the module ({@link DevDocs}).
 *
 * <p>
 * Each chapter and article the module ships must have a description, and each of its links must
 * lead somewhere on the class path of the module: an article may link only to articles of the
 * module itself and of the modules it depends on, never to a file outside the documentation. The
 * folder {@value #LEGACY_FOLDER} of the workspace must not hold articles any more: an article there
 * ships with no module.
 * </p>
 *
 * @see DevDocs#check(java.util.Collection, DevDoc)
 */
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestDocumentation extends TestCase {

	/** The folder of the workspace the developer articles were kept in before they shipped with the modules. */
	public static final String LEGACY_FOLDER = "docs/faq";

	private static final String DOC_SUFFIX = ".md";

	/**
	 * Checks the chapters and articles of the module.
	 */
	public void testDocumentation() {
		DevDoc root = DevDocs.load(TestDocumentation.class.getClassLoader());
		String module = AbstractBasicTestAll.MODULE_LAYOUT.getModuleDir().getName();
		List<DevDoc> own = new ArrayList<>();
		for (DevDoc doc : DevDocs.entries(root)) {
			if (module.equals(doc.getModule())) {
				own.add(doc);
			}
		}
		assertEquals("Problems in the developer documentation of " + module + ".", List.of(),
			DevDocs.check(own, root));
	}

	/**
	 * Checks that no article is left in the {@link #LEGACY_FOLDER}.
	 */
	public void testNoLegacyArticles() {
		File legacy = new File(AbstractBasicTestAll.MODULE_LAYOUT.getWorkspaceDir(), LEGACY_FOLDER);
		List<String> left = new ArrayList<>();
		File[] files = legacy.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().endsWith(DOC_SUFFIX)) {
					left.add(file.getName());
				}
			}
		}
		assertEquals("Articles in " + LEGACY_FOLDER + " ship with no module; move them to "
			+ "src/main/java/" + DevDocs.DOCS_DIR + " of the module owning the topic.", List.of(), left);
	}

	/**
	 * The suite of this test.
	 */
	public static Test suite() {
		return new TestSuite(TestDocumentation.class);
	}

}
