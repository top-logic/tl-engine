/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.impl;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.impl.OperationParameterVariables;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilderByExpression;
import com.top_logic.service.openapi.server.parameter.PathParameter;

/**
 * Test for {@link OperationParameterVariables}.
 */
public class TestOperationParameterVariables extends BasicTestCase {

	/**
	 * The provider returns the parameter names of the {@link OperationByMethod operation} that owns
	 * the implementation config (reached via the config's {@code container()}).
	 */
	public void testReturnsOperationParameterNames() {
		OperationByMethod operation = TypedConfiguration.newConfigItem(OperationByMethod.class);
		operation.getParameters().add(pathParam("id"));
		operation.getParameters().add(pathParam("name"));

		// Attaching the implementation sets its container to the operation.
		ServiceMethodBuilderByExpression.Config impl =
			TypedConfiguration.newConfigItem(ServiceMethodBuilderByExpression.Config.class);
		operation.setImplementation(impl);

		List<String> variables =
			new OperationParameterVariables().getVariablesFromModel(impl);

		assertTrue(variables.contains("id"));
		assertTrue(variables.contains("name"));
	}

	/**
	 * A path parameter configuration with the given name.
	 */
	private static PathParameter.Config pathParam(String name) {
		PathParameter.Config config = TypedConfiguration.newConfigItem(PathParameter.Config.class);
		config.setName(name);
		return config;
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestOperationParameterVariables.class));
	}
}
