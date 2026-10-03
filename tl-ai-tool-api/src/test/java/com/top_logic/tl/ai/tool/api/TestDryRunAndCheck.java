/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests {@link ToolMethod#dryRun()} and {@link ToolMethod#check()}: what a call is classified as, and
 * what is refused before it runs.
 *
 * <p>
 * Both decide something before the tool method is invoked - whether anybody is asked, whether the
 * call runs at all - so the one property that matters is that they agree with what the method itself
 * receives. A dry run the classification missed costs an approval; a dry run the method missed would
 * change data without one.
 * </p>
 */
public class TestDryRunAndCheck extends TestCase {

	/** A writing tool with a dry run and an early check. */
	public static class Writer {

		int _runs;

		@ToolMethod(description = "Write", mode = ToolMode.DATA_WRITE, dryRun = "dryRun", check = "refuse",
			marking = AnswerMarking.NONE)
		public String write(@P("what") String what, @P(value = "dry run", required = false) Boolean dryRun,
				@P(value = "how often", required = false) Integer times) {
			_runs++;
			return Boolean.TRUE.equals(dryRun) ? "would write " + what : "wrote " + what;
		}

		String refuse(String what, Boolean dryRun, Integer times) {
			return "nothing".equals(what) ? "There is nothing to write." : null;
		}
	}

	/** Names a dry-run parameter it does not have. */
	public static class Misspelled {

		@ToolMethod(description = "Write", mode = ToolMode.DATA_WRITE, dryRun = "dryrun")
		public String write(@P("dry run") Boolean dryRun) {
			return "";
		}
	}

	/** Names a dry-run parameter that is no boolean. */
	public static class NotABoolean {

		@ToolMethod(description = "Write", mode = ToolMode.DATA_WRITE, dryRun = "dryRun")
		public String write(@P("dry run") String dryRun) {
			return "";
		}
	}

	/** Names a check whose parameters differ from the tool method's. */
	public static class OtherParameters {

		@ToolMethod(description = "Write", mode = ToolMode.DATA_WRITE, check = "refuse")
		public String write(@P("what") String what) {
			return "";
		}

		String refuse(Object what) {
			return null;
		}
	}

	/** A call with the dry-run parameter set is a read; any other call keeps the declared mode. */
	public void testADryRunCallIsARead() {
		Tool tool = tool(new Writer());

		assertEquals(ToolMode.READ, tool.mode(args("what", "x", "dryRun", true)));
		assertEquals(ToolMode.DATA_WRITE, tool.mode(args("what", "x", "dryRun", false)));
		assertEquals(ToolMode.DATA_WRITE, tool.mode(args("what", "x")));
		assertEquals(ToolMode.DATA_WRITE, tool.mode(null));
		assertEquals(ToolMode.DATA_WRITE, tool.mode());
	}

	/**
	 * The classification reads the flag the way the method receives it: a "true" the model sent as a
	 * text is a dry run for both, anything else is a real call for both.
	 */
	public void testTheClassificationAgreesWithTheMethod() {
		Writer writer = new Writer();
		Tool tool = tool(writer);

		for (Object flag : List.of("true", "TRUE", Boolean.TRUE, "yes", "", Boolean.FALSE)) {
			Map<String, Object> call = args("what", "x", "dryRun", flag);
			boolean classifiedDry = tool.mode(call) == ToolMode.READ;
			boolean ranDry = tool.execute(call, null).text().startsWith("would");
			assertEquals("Flag '" + flag + "'", classifiedDry, ranDry);
		}
	}

	/**
	 * A misspelled or mistyped dry-run parameter fails when the tool is created: at call time it
	 * would make every dry run a real call.
	 */
	public void testABrokenDryRunDeclarationFailsWhenTheToolIsCreated() {
		assertCreationFails(new Misspelled(), "'dryrun'");
		assertCreationFails(new NotABoolean(), "not a boolean");
	}

	/** The check answers before the method runs, and the method does not run for it. */
	public void testTheCheckRefusesBeforeTheMethodRuns() {
		Writer writer = new Writer();
		Tool tool = tool(writer);

		assertEquals("There is nothing to write.", tool.refusal(args("what", "nothing"), null));
		assertNull(tool.refusal(args("what", "x"), null));
		assertEquals(0, writer._runs);
	}

	/** An argument that does not coerce is refused with the message the call would fail with. */
	public void testAnArgumentThatDoesNotCoerceIsRefused() {
		Tool tool = tool(new Writer());

		String refusal = tool.refusal(args("what", "x", "times", "often"), null);

		assertNotNull(refusal);
		assertTrue(refusal, refusal.contains("'times'"));
	}

	/** A check that does not take the tool method's parameters fails when the tool is created. */
	public void testACheckWithOtherParametersFailsWhenTheToolIsCreated() {
		assertCreationFails(new OtherParameters(), "'refuse'");
	}

	/** A tool that declares neither keeps its mode for every call and refuses nothing early. */
	public void testWithoutDeclarationsEveryCallIsTheTool() {
		Tool tool = AnnotatedTools.from(new AnnotatedToolsTest.GroupedTools()).stream()
			.filter(t -> t.name().equals("unclassified"))
			.findFirst()
			.orElseThrow();

		assertEquals(ToolMode.STRUCTURAL, tool.mode(args("dryRun", true)));
		assertNull(tool.refusal(args(), null));
	}

	private static Tool tool(Object provider) {
		List<Tool> tools = AnnotatedTools.from(provider);
		assertEquals(1, tools.size());
		return tools.get(0);
	}

	private static void assertCreationFails(Object provider, String expected) {
		try {
			AnnotatedTools.from(provider);
			fail("Expected the tool of " + provider.getClass().getSimpleName() + " to be refused.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(expected));
		}
	}

	private static Map<String, Object> args(Object... keysAndValues) {
		Map<String, Object> result = new HashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			result.put((String) keysAndValues[i], keysAndValues[i + 1]);
		}
		return result;
	}
}
