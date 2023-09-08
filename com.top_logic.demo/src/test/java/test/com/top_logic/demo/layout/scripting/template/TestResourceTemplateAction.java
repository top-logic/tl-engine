/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.scripting.template;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

import com.top_logic.layout.scripting.template.action.ScriptTemplateAction;
import com.top_logic.layout.scripting.template.action.op.ScriptTemplateActionOp;

/**
 * Test for {@link ScriptTemplateAction} and it's implementation {@link ScriptTemplateActionOp}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestResourceTemplateAction {

	/** Creates the {@link Test} suite containing all the actual {@link Test}s of this class. */
	public static Test suite() {
		TestSuite test =
			XmlScriptedTestUtil.suite(TestResourceTemplateAction.class,
				"DefaultProtocol", "FilesystemProtocol", "ResourceProtocol", "ScriptProtocol");
		return DemoSetup.createDemoSetup(test);
	}

}
