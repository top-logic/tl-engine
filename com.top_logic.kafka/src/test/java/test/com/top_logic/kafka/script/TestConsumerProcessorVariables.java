/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.script;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.kafka.script.ConsumerProcessorVariables;

/**
 * Test for {@link ConsumerProcessorVariables}.
 */
public class TestConsumerProcessorVariables extends BasicTestCase {

	/**
	 * The provider must return exactly the four implicit parameters that
	 * {@link com.top_logic.kafka.script.ConsumerProcessorByExpression} binds around the processor
	 * script: <code>message</code>, <code>key</code>, <code>headers</code>, and <code>topic</code>.
	 * The provider ignores the given {@link com.top_logic.layout.form.values.edit.ValueModel} since
	 * the variables are constant, so <code>null</code> can be passed.
	 */
	public void testReturnsImplicitParameterNames() {
		List<String> variables = new ConsumerProcessorVariables().getVariables(null);

		assertEquals(Set.of("message", "key", "headers", "topic"), Set.copyOf(variables));
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestConsumerProcessorVariables.class));
	}

}
