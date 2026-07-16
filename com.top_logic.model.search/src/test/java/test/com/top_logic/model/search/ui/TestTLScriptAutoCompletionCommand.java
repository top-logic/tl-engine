/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.model.search.ui.TLScriptCompletionService;

/**
 * Test for the variable-completion helpers of {@link TLScriptCompletionService}.
 */
public class TestTLScriptAutoCompletionCommand extends BasicTestCase {

	public void testInVariableCompletionMode() {
		assertTrue(TLScriptCompletionService.inVariableCompletionMode("x -> $"));
		assertTrue(TLScriptCompletionService.inVariableCompletionMode("x -> $fo"));
		assertFalse(TLScriptCompletionService.inVariableCompletionMode("x -> $x."));
		assertFalse(TLScriptCompletionService.inVariableCompletionMode("foo("));
		assertFalse(TLScriptCompletionService.inVariableCompletionMode(""));
	}

	public void testMatchingVariablesReturnsDollarPrefixed() {
		Set<String> result =
			new HashSet<>(TLScriptCompletionService.matchingVariables("x -> foo(y -> $", "$", false));
		assertEquals(new HashSet<>(List.of("$x", "$y")), result);
	}

	public void testMatchingVariablesFiltersByPrefix() {
		List<String> result =
			TLScriptCompletionService.matchingVariables("element -> foo(other -> $el", "$el", false);
		assertEquals(List.of("$element"), result);
	}

	public void testMatchingVariablesCaseInsensitive() {
		List<String> result =
			TLScriptCompletionService.matchingVariables("Element -> $e", "$e", false);
		assertEquals(List.of("$Element"), result);
	}

	public void testMatchingVariablesCaseSensitive() {
		List<String> result =
			TLScriptCompletionService.matchingVariables("Element -> $e", "$e", true);
		assertEquals(List.of(), result);
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTLScriptAutoCompletionCommand.class));
	}
}
