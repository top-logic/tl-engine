/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.model.search.ui.ModelReferenceChecker;

/**
 * Test for {@link ModelReferenceChecker}, which reports a script that parses but cannot be
 * compiled against the application.
 */
@SuppressWarnings("javadoc")
public class TestModelReferenceChecker extends AbstractSearchExpressionTest {

	public void testWellFormedScriptPasses() throws Exception {
		ModelReferenceChecker.checkModelElements(parse("p -> null"));
	}

	public void testUnboundVariableIsReported() throws Exception {
		assertRejected("p -> $f");
	}

	public void testUnknownFunctionIsReported() throws Exception {
		assertRejected("p -> noSuchFunction($p)");
	}

	public void testUnknownTypeIsReported() throws Exception {
		assertRejected("p -> all(`no.such:Type`)");
	}

	private static void assertRejected(String script) throws Exception {
		try {
			ModelReferenceChecker.checkModelElements(parse(script));
			fail("The script must be rejected: " + script);
		} catch (I18NRuntimeException ex) {
			assertNotNull(ex.getErrorKey());
		}
	}

	public static Test suite() {
		return suite(TestModelReferenceChecker.class);
	}

}
