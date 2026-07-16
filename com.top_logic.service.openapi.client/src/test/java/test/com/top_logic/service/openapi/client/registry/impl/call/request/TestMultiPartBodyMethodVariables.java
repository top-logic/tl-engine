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
import com.top_logic.service.openapi.client.registry.impl.call.request.MethodParameterVariables;
import com.top_logic.service.openapi.client.registry.impl.call.request.MultiPartRequestBody;
import com.top_logic.service.openapi.client.registry.impl.call.request.MultiPartRequestBody.MultiPartContent;

/**
 * Test for {@link MethodParameterVariables} applied to a {@link MultiPartContent} part of a
 * {@link MultiPartRequestBody}.
 */
public class TestMultiPartBodyMethodVariables extends BasicTestCase {

	/**
	 * The provider returns the parameter names of the {@link MethodDefinition} that (indirectly)
	 * owns a {@link MultiPartContent} part (reached by walking up the {@code container()} chain
	 * from the part to the
	 * {@link com.top_logic.service.openapi.client.registry.impl.call.request.MultiPartRequestBody.Config}
	 * that holds it in its parts list, and further up to the {@link MethodDefinition} that holds
	 * that config as an entry of {@link MethodDefinition#getCallBuilders()}).
	 */
	public void testReturnsMethodParameterNames() {
		MethodDefinition method = TypedConfiguration.newConfigItem(MethodDefinition.class);
		method.getParameters().add(parameter("a"));
		method.getParameters().add(parameter("b"));

		MultiPartRequestBody.Config bodyConfig = TypedConfiguration.newConfigItem(MultiPartRequestBody.Config.class);
		MultiPartContent part = TypedConfiguration.newConfigItem(MultiPartContent.class);
		part.setName("part1");
		// Attaching the part to the body config sets its container to the body config.
		bodyConfig.getParts().add(part);

		// Attaching the body config to the method sets its container to the method.
		method.getCallBuilders().add(bodyConfig);

		List<String> variables = new MethodParameterVariables().getVariablesFromModel(part);

		assertEquals(List.of("a", "b"), variables);
	}

	/**
	 * When the part is not (yet) attached below a {@link MethodDefinition}, no variables are
	 * found.
	 */
	public void testReturnsEmptyListWithoutEnclosingMethod() {
		MultiPartContent part = TypedConfiguration.newConfigItem(MultiPartContent.class);
		part.setName("part1");

		List<String> variables = new MethodParameterVariables().getVariablesFromModel(part);

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
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestMultiPartBodyMethodVariables.class));
	}
}
