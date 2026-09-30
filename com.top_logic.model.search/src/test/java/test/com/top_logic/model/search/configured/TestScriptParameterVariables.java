/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.configured;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.search.configured.ScriptConfiguration;
import com.top_logic.model.search.configured.ScriptParameter;
import com.top_logic.model.search.configured.ScriptParameterVariables;

/**
 * Test for {@link ScriptParameterVariables}.
 */
public class TestScriptParameterVariables extends BasicTestCase {

	/**
	 * The declared {@link ScriptConfiguration#getParameters()} are found as variables.
	 */
	public void testVariablesFromParameters() {
		ScriptConfiguration config = TypedConfiguration.newConfigItem(ScriptConfiguration.class);

		ScriptParameter a = TypedConfiguration.newConfigItem(ScriptParameter.class);
		a.setName("a");
		ScriptParameter b = TypedConfiguration.newConfigItem(ScriptParameter.class);
		b.setName("b");
		config.getParameters().add(a);
		config.getParameters().add(b);

		assertEquals(List.of("a", "b"),
			new ScriptParameterVariables().getVariablesFromModel(config));
	}

	/**
	 * A {@link ScriptConfiguration} without parameters yields no variables.
	 */
	public void testEmptyWhenNoParameters() {
		ScriptConfiguration config = TypedConfiguration.newConfigItem(ScriptConfiguration.class);

		assertEquals(List.of(), new ScriptParameterVariables().getVariablesFromModel(config));
	}

	/**
	 * A configuration item that is not a {@link ScriptConfiguration} yields no variables.
	 */
	public void testEmptyWhenNotScriptConfiguration() {
		ScriptParameter notAScript = TypedConfiguration.newConfigItem(ScriptParameter.class);

		assertEquals(List.of(), new ScriptParameterVariables().getVariablesFromModel(notAScript));
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestScriptParameterVariables.class));
	}

}
