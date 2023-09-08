/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.webfolder;

import junit.framework.Test;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * {@link TestSimilarDocuments} tests the "similar documents" function of webfolder.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSimilarDocuments {

	public static Test suite() {
		String[] testCases = {
			"01_SimpleTest",
		};
		return DemoSetup.createDemoSetup(XmlScriptedTestUtil.suite(TestSimilarDocuments.class, testCases));
	}

}
