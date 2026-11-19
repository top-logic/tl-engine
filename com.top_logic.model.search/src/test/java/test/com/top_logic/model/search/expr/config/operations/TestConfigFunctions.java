/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.model.search.expr.config.operations.ConfigFunctions;

/**
 * Test of {@link ConfigFunctions}.
 *
 * <p>
 * The configuration variables used here are defined in the test configuration
 * <code>modelSearchConf.test.xml</code>.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestConfigFunctions extends AbstractSearchExpressionTest {

	private static final String PLAIN = "TEST.COM.TOP_LOGIC.MODEL.SEARCH.EXPR.CONFIG.OPERATIONS.TESTCONFIGFUNCTIONS__PLAIN";

	private static final String REFERENCE =
		"TEST.COM.TOP_LOGIC.MODEL.SEARCH.EXPR.CONFIG.OPERATIONS.TESTCONFIGFUNCTIONS__REFERENCE";

	private static final String UNDEFINED =
		"TEST.COM.TOP_LOGIC.MODEL.SEARCH.EXPR.CONFIG.OPERATIONS.TESTCONFIGFUNCTIONS__UNDEFINED";

	private static final String PLAIN_VALUE = "plain value";

	public void testNameWithoutMarkers() throws Exception {
		assertEquals(PLAIN_VALUE, configVariable(PLAIN));
	}

	public void testNameWithMarkers() throws Exception {
		assertEquals(PLAIN_VALUE, configVariable(alias(PLAIN)));
	}

	public void testUndefined() throws Exception {
		assertNull(configVariable(UNDEFINED));
		assertNull(configVariable(alias(UNDEFINED)));
	}

	public void testInvalidName() throws Exception {
		assertNull(configVariable("A%B"));
	}

	public void testReferenceIsResolved() throws Exception {
		assertEquals("before " + PLAIN_VALUE + " after", configVariable(REFERENCE));
	}

	public void testResolveAliasReplacesAllVariables() throws Exception {
		assertEquals(PLAIN_VALUE + ":before " + PLAIN_VALUE + " after",
			evalWithoutErrors("resolveAlias('" + alias(PLAIN) + ":" + alias(REFERENCE) + "')"));
	}

	private Object configVariable(String name) throws Exception {
		return evalWithoutErrors("configVariable('" + name + "')");
	}

	private static String alias(String name) {
		return "%" + name + "%";
	}

	public static Test suite() {
		return suite(TestConfigFunctions.class);
	}

}
