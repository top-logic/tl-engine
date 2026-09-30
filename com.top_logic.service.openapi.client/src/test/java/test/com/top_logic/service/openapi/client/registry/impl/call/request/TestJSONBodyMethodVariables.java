/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.client.registry.impl.call.request;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.conf.ParameterDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.request.JSONRequestBody;
import com.top_logic.service.openapi.client.registry.impl.call.request.MethodParameterVariables;

/**
 * Test for {@link MethodParameterVariables}.
 */
public class TestJSONBodyMethodVariables extends BasicTestCase {

	/**
	 * The provider returns the parameter names of the {@link MethodDefinition} that (indirectly)
	 * owns the
	 * {@link com.top_logic.service.openapi.client.registry.impl.call.request.JSONRequestBody.Config}
	 * (reached via the config's {@code container()}, since the JSON body is an entry of
	 * {@link MethodDefinition#getCallBuilders()}, not an immediate property of the
	 * {@link MethodDefinition}).
	 */
	public void testReturnsMethodParameterNames() {
		MethodDefinition method = TypedConfiguration.newConfigItem(MethodDefinition.class);
		method.getParameters().add(parameter("a"));
		method.getParameters().add(parameter("b"));

		JSONRequestBody.Config<?> jsonConfig = TypedConfiguration.newConfigItem(JSONRequestBody.Config.class);
		// Attaching the call builder to the method sets its container to the method.
		method.getCallBuilders().add(jsonConfig);

		List<String> variables = new MethodParameterVariables().getVariablesFromModel(jsonConfig);

		assertEquals(List.of("a", "b"), variables);
	}

	/**
	 * When the configuration item is not (yet) attached below a {@link MethodDefinition}, no
	 * variables are found.
	 */
	public void testReturnsEmptyListWithoutEnclosingMethod() {
		JSONRequestBody.Config<?> jsonConfig = TypedConfiguration.newConfigItem(JSONRequestBody.Config.class);

		List<String> variables = new MethodParameterVariables().getVariablesFromModel(jsonConfig);

		assertTrue(variables.isEmpty());
	}

	private static ParameterDefinition parameter(String name) {
		ParameterDefinition result = TypedConfiguration.newConfigItem(ParameterDefinition.class);
		result.setName(name);
		return result;
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJSONBodyMethodVariables.class));
	}
}
