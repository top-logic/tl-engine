/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tools;

import java.io.File;

import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleLayoutTestConstants;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.tools.JSPRewrite;

/**
 * Test case for {@link JSPRewrite}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestJSPRewrite extends TestCase {

	public void testCharacterReference() throws Exception {
		new File("tmp").mkdirs();
		FileUtilities.copyFile(
			new File(ModuleLayoutTestConstants.SRC_TEST_WEBAPP_PATH + "/jsp/TestCharacterReference.jsp"),
			new File("tmp/TestCharacterReference.jsp"));

		JSPRewrite.main(new String[] { "tmp/TestCharacterReference.jsp" });
		
		assertTrue(FileUtilities.equalsFile(
			new File(ModuleLayoutTestConstants.SRC_TEST_WEBAPP_PATH + "/jsp/TestCharacterReference.jsp"),
			new File("tmp/TestCharacterReference.jsp")));
	}

}
