/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;

/**
 * Tests {@link ToolMethod#commitMessage()}: the message a call of an annotated tool is logged with.
 */
public class TestToolCommitMessage extends TestCase {

	/** A writing tool that describes its calls. */
	public static class Describing {

		int _runs;

		@ToolMethod(description = "Rename", mode = ToolMode.DATA_WRITE, commitMessage = "renameMessage",
			marking = AnswerMarking.NONE)
		public String rename(@P("from") String from, @P("to") String to,
				@P(value = "how often", required = false) Integer times) {
			_runs++;
			return "renamed";
		}

		ResKey renameMessage(String from, String to, Integer times) {
			return ResKey.text(from + " -> " + to + (times == null ? "" : " x" + times));
		}
	}

	/** A writing tool that says nothing about its calls. */
	public static class Silent {

		@ToolMethod(description = "Write", mode = ToolMode.DATA_WRITE, marking = AnswerMarking.NONE)
		public String write(@P("what") String what) {
			return "";
		}
	}

	/** Names a commit message method whose parameters differ from the tool method's. */
	public static class OtherParameters {

		@ToolMethod(description = "Write", mode = ToolMode.DATA_WRITE, commitMessage = "message")
		public String write(@P("what") String what) {
			return "";
		}

		ResKey message(Object what) {
			return null;
		}
	}

	/** Names a commit message method that answers a text instead of a key. */
	public static class NotAKey {

		@ToolMethod(description = "Write", mode = ToolMode.DATA_WRITE, commitMessage = "message")
		public String write(@P("what") String what) {
			return "";
		}

		String message(String what) {
			return what;
		}
	}

	/** The named method receives the arguments the tool method would, coerced the same way. */
	public void testTheNamedMethodDescribesTheCall() {
		Describing describing = new Describing();
		Tool tool = tool(describing);

		assertEquals(ResKey.text("a -> b x3"), tool.commitMessage(args("from", "a", "to", "b", "times", "3"), null));
		assertEquals(ResKey.text("a -> b"), tool.commitMessage(args("from", "a", "to", "b"), null));
		assertEquals("Describing a call does not run it.", 0, describing._runs);
	}

	/** Arguments that do not coerce get no message of the tool's, so the caller uses the generic one. */
	public void testArgumentsThatDoNotCoerceHaveNoMessage() {
		assertNull(tool(new Describing()).commitMessage(args("from", "a", "to", "b", "times", "many"), null));
	}

	/** Without a named method a call is logged with the generic message naming the tool. */
	public void testWithoutAMethodTheGenericMessage() {
		assertEquals(I18NConstants.TOOL_CALL__TOOL.fill("write"), tool(new Silent()).commitMessage(args(), null));
	}

	/** A declaration that cannot be called fails when the tool is created, not in a run. */
	public void testABrokenDeclarationFailsWhenTheToolIsCreated() {
		assertCreationFails(new OtherParameters(), "same parameters");
		assertCreationFails(new NotAKey(), "ResKey");
	}

	private static void assertCreationFails(Object provider, String expected) {
		try {
			AnnotatedTools.from(provider);
			fail("Expected the tool creation to fail.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(expected));
		}
	}

	private static Tool tool(Object provider) {
		return AnnotatedTools.from(provider).get(0);
	}

	private static Map<String, Object> args(Object... keyValues) {
		Map<String, Object> result = new HashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			result.put((String) keyValues[i], keyValues[i + 1]);
		}
		return result;
	}
}
