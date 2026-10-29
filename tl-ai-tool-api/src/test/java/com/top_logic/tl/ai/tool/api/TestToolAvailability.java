/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests {@link ToolMethod#unavailable()}: what a tool says about the run it would be offered to.
 *
 * <p>
 * The declaration decides whether the model is offered the tool at all, so the property that matters
 * is that the method it names is found - also when a subclass answers for its base class, which is how
 * one tool is available on a page and always in an eval - and that a broken declaration fails when the
 * tool is created rather than offering the tool everywhere.
 * </p>
 */
public class TestToolAvailability extends TestCase {

	/** A tool that can only act where something is there to act on. */
	public static class Shower {

		boolean _place;

		int _runs;

		@ToolMethod(description = "Show", mode = ToolMode.READ, unavailable = "noPlace", marking = AnswerMarking.NONE)
		public String show(@P("what") String what) {
			_runs++;
			return "shown " + what;
		}

		String noPlace(ToolExecutionContext context) {
			return _place ? null : "There is no place to show anything on.";
		}
	}

	/** Answers for the base class's declaration, as a delivering subclass does. */
	public static class EverywhereShower extends Shower {

		@Override
		String noPlace(ToolExecutionContext context) {
			return null;
		}
	}

	/** Names a method it does not have. */
	public static class Misspelled {

		@ToolMethod(description = "Show", unavailable = "noplace")
		public String show() {
			return "";
		}

		String noPlace(ToolExecutionContext context) {
			return null;
		}
	}

	/** Names a method that does not take the run's context. */
	public static class WithoutContext {

		@ToolMethod(description = "Show", unavailable = "noPlace")
		public String show() {
			return "";
		}

		String noPlace() {
			return null;
		}
	}

	/** Names a method that does not answer with a reason. */
	public static class NotAReason {

		@ToolMethod(description = "Show", unavailable = "noPlace")
		public String show() {
			return "";
		}

		boolean noPlace(ToolExecutionContext context) {
			return false;
		}
	}

	/** The named method answers for the tool, and the tool method does not run for it. */
	public void testTheNamedMethodSaysWhetherTheToolCanAct() {
		Shower shower = new Shower();
		Tool tool = tool(shower);

		assertEquals("There is no place to show anything on.", tool.unavailableReason(null));
		shower._place = true;
		assertNull(tool.unavailableReason(null));
		assertEquals(0, shower._runs);
	}

	/** A subclass answers for the declaration of its base class. */
	public void testASubclassAnswersForTheBaseClass() {
		assertNull(tool(new EverywhereShower()).unavailableReason(null));
	}

	/**
	 * A declaration naming no fitting method fails when the tool is created: at run time it would
	 * offer the tool in every run.
	 */
	public void testABrokenDeclarationFailsWhenTheToolIsCreated() {
		assertCreationFails(new Misspelled(), "'noplace'");
		assertCreationFails(new WithoutContext(), "'noPlace'");
		assertCreationFails(new NotAReason(), "must return the reason");
	}

	/** A tool that declares nothing can act in every run. */
	public void testWithoutDeclarationTheToolCanAlwaysAct() {
		Tool tool = AnnotatedTools.from(new AnnotatedToolsTest.GroupedTools()).stream()
			.filter(t -> t.name().equals("unclassified"))
			.findFirst()
			.orElseThrow();

		assertNull(tool.unavailableReason(null));
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
}
