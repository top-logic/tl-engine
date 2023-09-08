/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.scripting.template;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.ScriptedTest;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;
import test.com.top_logic.layout.scripting.template.ScriptedTestTemplateFactory;

import com.top_logic.basic.io.FileLocation;

/**
 * Tests if the features combination of the xml template language and scripted tests work properly.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestXmlTemplates {

	/** Creates the {@link Test} suite containing all the actual {@link Test}s of this class. */
	public static Test suite() {
		String testname = "01_MinimalTemplateTest";
		String filename = XmlScriptedTestUtil.createFileName(TestXmlTemplates.class, testname);
		FileLocation file = FileLocation.fileByContextClass(filename, TestXmlTemplates.class);
		Map<String, ?> templateParameters = Collections.singletonMap("Example_Parameter", "Example Value");
		ScriptedTest test = ScriptedTestTemplateFactory.SINGLETON
			.buildTestFromTemplateFile(file, testname, templateParameters);
		return DemoSetup.createDemoSetup(test);
	}

}
