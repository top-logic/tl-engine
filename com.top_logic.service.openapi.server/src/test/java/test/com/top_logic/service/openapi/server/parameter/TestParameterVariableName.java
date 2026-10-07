/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.parameter;

import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.conf.PathItem;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter.ParameterConfiguration;
import com.top_logic.service.openapi.server.parameter.HeaderParameter;
import com.top_logic.service.openapi.server.parameter.MultiPartBodyParameter;
import com.top_logic.service.openapi.server.parameter.PathParameter;
import com.top_logic.service.openapi.server.parameter.QueryParameter;

/**
 * Test for the {@link ParameterConfiguration#getVariableName() variable name} of request
 * parameters.
 */
public class TestParameterVariableName extends BasicTestCase {

	/**
	 * A parameter name that is a TL-Script variable name needs no variable name.
	 */
	public void testPlainNameWithoutVariableName() throws ConfigurationException {
		assertEquals(List.of(), failures(header("event", null)));
	}

	/**
	 * A parameter name that is no TL-Script variable name is accepted with a variable name.
	 */
	public void testVariableNameForInvalidName() throws ConfigurationException {
		assertEquals(List.of(), failures(header("X-Gitea-Event", "event")));
	}

	/**
	 * A parameter name that is no TL-Script variable name requires a variable name.
	 */
	public void testMissingVariableName() throws ConfigurationException {
		assertEquals(
			List.of(com.top_logic.service.openapi.server.parameter.I18NConstants.ERROR_VARIABLE_NAME_REQUIRED__NAME
				.getKey()),
			failures(header("X-Gitea-Event", null)));
	}

	/**
	 * An explicit variable name must be a TL-Script variable name.
	 */
	public void testInvalidVariableName() throws ConfigurationException {
		assertEquals(
			List.of(com.top_logic.service.openapi.server.parameter.I18NConstants.ERROR_INVALID_VARIABLE_NAME__NAME
				.getKey()),
			failures(header("X-Gitea-Event", "a-b")));
	}

	/**
	 * The variable name of a multipart body part is checked like the one of a parameter.
	 */
	public void testMissingVariableNameOfBodyPart() throws ConfigurationException {
		MultiPartBodyParameter.Config body = TypedConfiguration.newConfigItem(MultiPartBodyParameter.Config.class);
		MultiPartBodyParameter.BodyPart part = TypedConfiguration.newConfigItem(MultiPartBodyParameter.BodyPart.class);
		part.setName("file-name");
		body.getParts().put(part.getName(), part);

		assertEquals(
			List.of(com.top_logic.service.openapi.server.parameter.I18NConstants.ERROR_VARIABLE_NAME_REQUIRED__NAME
				.getKey()),
			failures(body));
	}

	/**
	 * Two parameters of one operation must not bind the same script variable.
	 */
	public void testDuplicateVariableInOperation() throws ConfigurationException {
		OperationByMethod operation = operation(server(), "/hook");
		operation.getParameters().add(header("X-Gitea-Event", "event"));
		operation.getParameters().add(query("event"));

		assertEquals(List.of(com.top_logic.service.openapi.server.conf.I18NConstants.ERROR_DUPLICATE_PARAMETER_VARIABLES__NAMES
			.getKey()), failures(operation.getEnclosingPathItem().getServerConfiguration()));
	}

	/**
	 * A parameter of the operation must not bind the script variable of a parameter of the
	 * enclosing {@link PathItem}.
	 */
	public void testDuplicateVariableWithPathItem() throws ConfigurationException {
		OperationByMethod operation = operation(server(), "/hook/{id}");
		PathParameter.Config id = TypedConfiguration.newConfigItem(PathParameter.Config.class);
		id.setName("id");
		operation.getEnclosingPathItem().getParameters().add(id);
		operation.getParameters().add(header("X-Id", "id"));

		assertEquals(List.of(com.top_logic.service.openapi.server.conf.I18NConstants.ERROR_DUPLICATE_PARAMETER_VARIABLES__NAMES
			.getKey()), failures(operation.getEnclosingPathItem().getServerConfiguration()));
	}

	/**
	 * Different variable names for parameters of one operation are accepted.
	 */
	public void testUniqueVariablesInOperation() throws ConfigurationException {
		OperationByMethod operation = operation(server(), "/hook");
		operation.getParameters().add(header("X-Gitea-Event", "event"));
		operation.getParameters().add(query("delivery"));

		assertEquals(List.of(), failures(operation.getEnclosingPathItem().getServerConfiguration()));
	}

	private static OpenApiServer.Config<?> server() {
		return TypedConfiguration.newConfigItem(OpenApiServer.Config.class);
	}

	private static OperationByMethod operation(OpenApiServer.Config<?> server, String path) {
		PathItem pathItem = TypedConfiguration.newConfigItem(PathItem.class);
		pathItem.setPath(path);
		server.getPaths().add(pathItem);

		OperationByMethod operation = TypedConfiguration.newConfigItem(OperationByMethod.class);
		operation.setMethod(HttpMethod.POST);
		pathItem.getOperations().put(HttpMethod.POST, operation);
		return operation;
	}

	private static HeaderParameter.Config header(String name, String variableName) {
		HeaderParameter.Config config = TypedConfiguration.newConfigItem(HeaderParameter.Config.class);
		config.setName(name);
		config.setVariableName(variableName);
		return config;
	}

	private static QueryParameter.Config query(String name) {
		QueryParameter.Config config = TypedConfiguration.newConfigItem(QueryParameter.Config.class);
		config.setName(name);
		return config;
	}

	/**
	 * The keys of the variable name related constraint failures in the given configuration.
	 */
	private static List<String> failures(ConfigurationItem config) throws ConfigurationException {
		List<String> relevant = List.of(
			com.top_logic.service.openapi.server.parameter.I18NConstants.ERROR_VARIABLE_NAME_REQUIRED__NAME.getKey(),
			com.top_logic.service.openapi.server.parameter.I18NConstants.ERROR_INVALID_VARIABLE_NAME__NAME.getKey(),
			com.top_logic.service.openapi.server.conf.I18NConstants.ERROR_DUPLICATE_PARAMETER_VARIABLES__NAMES.getKey());

		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);
		return checker.getFailures()
			.stream()
			.filter(failure -> !failure.isWarning())
			.map(ConstraintFailure::getConstraintName)
			.map(ResKey::getKey)
			.filter(relevant::contains)
			.collect(Collectors.toList());
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestParameterVariableName.class));
	}
}
