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

import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.ui.CodeCompletion;
import com.top_logic.model.search.ui.TLScriptCompletionService;

/**
 * Test for the variable-completion helpers of {@link TLScriptCompletionService}.
 */
public class TestTLScriptAutoCompletionCommand extends BasicTestCase {

	/**
	 * Variable-completion mode triggers on a trailing <code>$</code> (optionally followed by
	 * identifier characters), but not after a completed reference or outside a variable position.
	 */
	public void testInVariableCompletionMode() {
		assertTrue(TLScriptCompletionService.inVariableCompletionMode("x -> $"));
		assertTrue(TLScriptCompletionService.inVariableCompletionMode("x -> $fo"));
		assertFalse(TLScriptCompletionService.inVariableCompletionMode("x -> $x."));
		assertFalse(TLScriptCompletionService.inVariableCompletionMode("foo("));
		assertFalse(TLScriptCompletionService.inVariableCompletionMode(""));
	}

	/**
	 * Matched variables are returned with the leading <code>$</code> (all enclosing lambda parameters
	 * offered).
	 */
	public void testMatchingVariablesReturnsDollarPrefixed() {
		Set<String> result =
			new HashSet<>(TLScriptCompletionService.matchingVariables("x -> foo(y -> $", "$", false));
		assertEquals(new HashSet<>(List.of("$x", "$y")), result);
	}

	/**
	 * The typed prefix narrows the offered variables.
	 */
	public void testMatchingVariablesFiltersByPrefix() {
		List<String> result =
			TLScriptCompletionService.matchingVariables("element -> foo(other -> $el", "$el", false);
		assertEquals(List.of("$element"), result);
	}

	/**
	 * Prefix matching is case insensitive when requested.
	 */
	public void testMatchingVariablesCaseInsensitive() {
		List<String> result =
			TLScriptCompletionService.matchingVariables("Element -> $e", "$e", false);
		assertEquals(List.of("$Element"), result);
	}

	/**
	 * Prefix matching respects case when case-sensitive matching is requested.
	 */
	public void testMatchingVariablesCaseSensitive() {
		List<String> result =
			TLScriptCompletionService.matchingVariables("Element -> $e", "$e", true);
		assertEquals(List.of(), result);
	}

	/**
	 * Context variables are offered at the top level, where no lambda parameter is in scope.
	 */
	public void testContextVariablesOfferedTopLevel() {
		java.util.Set<String> result = new HashSet<>(
			TLScriptCompletionService.matchingVariables("$", List.of("path", "id"), "$", false));
		assertEquals(new HashSet<>(List.of("$path", "$id")), result);
	}

	/**
	 * Context variables are offered alongside the in-scope lambda parameters.
	 */
	public void testContextVariablesUnionWithLambda() {
		java.util.Set<String> result = new HashSet<>(
			TLScriptCompletionService.matchingVariables("x -> $", List.of("path"), "$", false));
		assertEquals(new HashSet<>(List.of("$x", "$path")), result);
	}

	/**
	 * A context variable with the same name as an in-scope lambda parameter is offered only once.
	 */
	public void testContextVariablesDeduplicated() {
		java.util.Set<String> result = new HashSet<>(
			TLScriptCompletionService.matchingVariables("x -> $", List.of("x"), "$", false));
		assertEquals(new HashSet<>(List.of("$x")), result);
	}

	/**
	 * Context variables are subject to the same prefix filtering as text-derived variables.
	 */
	public void testContextVariablesFilteredByPrefix() {
		assertEquals(List.of("$path"),
			TLScriptCompletionService.matchingVariables("$pa", List.of("path", "id"), "$pa", false));
	}

	/**
	 * A <code>$</code> typed inside an open string literal is recognized as text mode, so it does not
	 * trigger variable completion; a <code>$</code> outside any string is not text mode.
	 */
	public void testInTextModeRecognizesDollarInString() {
		assertTrue(TLScriptCompletionService.inTextMode("\"$"));
		assertTrue(TLScriptCompletionService.inTextMode("\"abc $"));
		assertTrue(TLScriptCompletionService.inTextMode("'x $"));

		assertFalse(TLScriptCompletionService.inTextMode("x -> $"));
	}

	/**
	 * The overload the React editor's completion handler uses: a null {@link DisplayContext} is
	 * acceptable in variable mode, the text up to the cursor drives the in-scope variables, and the
	 * declared context variables are included — all returned with a leading <code>$</code>.
	 */
	public void testComputeCompletionsVariableModeNullContext() {
		List<CodeCompletion> completions = TLScriptCompletionService.computeCompletions(
			null, "x -> $", "$", "x -> $", List.of("ctx"), false);

		java.util.Set<String> names = new HashSet<>();
		for (CodeCompletion completion : completions) {
			names.add(completion.getName());
		}
		assertEquals(new HashSet<>(List.of("$x", "$ctx")), names);
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTLScriptAutoCompletionCommand.class));
	}
}
