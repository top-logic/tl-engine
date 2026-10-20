/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentFact;
import com.top_logic.tl.ai.core.agent.environment.TurnContext;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.tl.ai.core.tools.present.PresentAs;
import com.top_logic.tl.ai.core.tools.present.Presentation;
import com.top_logic.util.model.ModelService;

/**
 * What a page's selection says to the agent (Gitea #494).
 *
 * <p>
 * Real persisted objects, because the point of the line is the identifier: a label the model cannot
 * feed back into a tool call is the state the ticket complains about, so the test reads the value
 * back with the very parser the tools use.
 * </p>
 */
public class TestChatSelection extends BasicTestCase {

	private static Person user;

	private static TLObject first;

	private static TLObject second;

	/**
	 * What the fixture's objects are called.
	 *
	 * <p>
	 * A page's own {@code <label>} expression, standing in for the one every application has: these
	 * are chat sessions, and what a chat session is called in a list is not what this test is about.
	 * The default - the application's own label provider - has a case of its own below.
	 * </p>
	 */
	private static String title(Object object) {
		return object == first ? "Hosting August" : "Domain renewal";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("selector");
			// Any persisted object will do: what is under test is how one is stated, which knows
			// nothing about what an invoice is.
			first = ChatHistoryService.createSession("Hosting August", user);
			second = ChatHistoryService.createSession("Domain renewal", user);
		}
	}

	/** Nothing selected is a statement, not a gap: the agent is told, rather than left guessing. */
	public void testNothingSelectedSaysSo() {
		assertEquals(ChatSelection.NOTHING, ChatSelection.describe(null, null));
		assertEquals(ChatSelection.NOTHING, ChatSelection.describe(List.of(), null));
		assertEquals("A channel holding the empty string holds nothing.",
			ChatSelection.NOTHING, ChatSelection.describe("", null));
	}

	/** One object: the label the user sees, the type the model reasons with, the id it acts on. */
	public void testOneObjectIsNamedThreeWays() {
		String value = ChatSelection.describe(first, TestChatSelection::title);

		assertContains("Hosting August", value);
		assertContains("[type:tl.ai.execution:AgentSession]", value);
		assertContains("[id:" + ReadFilter.idOf(first) + "]", value);
	}

	/** A page that names no expression gets the label the user sees everywhere else in the app. */
	public void testTheApplicationsOwnLabelIsTheDefault() {
		String value = ChatSelection.describe(first, null);

		assertTrue("The application's own label leads the line: " + value,
			value.startsWith(MetaLabelProvider.INSTANCE.getLabel(first)));
		assertContains("[id:" + ReadFilter.idOf(first) + "]", value);
	}

	/**
	 * The point of stating the identifier: a model that copies the line back into a tool call
	 * reaches the object the user selected.
	 */
	public void testTheIdentifierIsTheOneTheToolsReadBack() {
		String value = ChatSelection.describe(first, TestChatSelection::title);

		assertEquals(ReadFilter.idOf(first), TidResolver.objectId(value));
	}

	/** Several objects are one line, in the order the page selected them. */
	public void testSeveralObjectsAreOneLine() {
		String value = ChatSelection.describe(List.of(first, second), TestChatSelection::title);

		assertContains("Hosting August", value);
		assertContains("Domain renewal", value);
		assertTrue("The first selected object is named first: " + value,
			value.indexOf("Hosting August") < value.indexOf("Domain renewal"));
	}

	/** "Select all" does not put a table into the prompt: the rest is counted, not named. */
	public void testALargeSelectionIsBounded() {
		List<Object> many = new ArrayList<>();
		for (int n = 0; n < ChatSelection.MAX_NAMED + 5; n++) {
			many.add(n % 2 == 0 ? first : second);
		}

		String value = ChatSelection.describe(many, TestChatSelection::title);

		assertContains("and 5 more", value);
		// MAX_NAMED objects joined by MAX_NAMED - 1 separators, plus the one in front of the count.
		assertEquals("Exactly MAX_NAMED objects are named, and the rest is one phrase.",
			ChatSelection.MAX_NAMED, countOccurrences(value, "; "));
	}

	/**
	 * What the agent presented is still what the user looks at: a table is stated as a table, with
	 * the objects in it (#616).
	 */
	public void testAPresentedTableSaysItIsOne() {
		Presentation table = Presentation.of(PresentAs.TABLE, List.of(first, second), "Rechnungen", null);

		String value = ChatSelection.describe(table, TestChatSelection::title);

		assertTrue(value, value.startsWith("a table of 2 objects: Hosting August"));
		assertContains("[id:" + ReadFilter.idOf(second) + "]", value);
	}

	/** A page that knows a better word for its objects says it, and nothing else changes. */
	public void testAPageMayNameTheObjectItself() {
		String value = ChatSelection.describe(first, object -> "the August hosting bill");

		assertContains("the August hosting bill", value);
		assertContains("[id:" + ReadFilter.idOf(first) + "]", value);
	}

	/** A value that is no model object has no type and no id to state, and says what it is. */
	public void testAPlainValueIsStatedAsItIs() {
		assertEquals("Ready", ChatSelection.describe("Ready", object -> String.valueOf(object)));
	}

	/**
	 * The whole path this ticket is about: what a page states arrives in the per-turn block, which
	 * is where a model looking for the current selection reads it.
	 */
	public void testTheSelectionReachesTheTurnContextBlock() {
		EnvironmentFact fact = new EnvironmentFact("Selected invoice",
			ChatSelection.describe(first, TestChatSelection::title), false);

		String block = TurnContext.compose(List.of(fact), 2);

		assertContains(TurnContext.OPEN_TAG, block);
		assertContains("Selected invoice: Hosting August", block);
		assertContains("[id:" + ReadFilter.idOf(first) + "]", block);
		assertContains(TurnContext.CLOSE_TAG, block);
	}

	private static int countOccurrences(String text, String part) {
		int count = 0;
		int at = text.indexOf(part);
		while (at >= 0) {
			count++;
			at = text.indexOf(part, at + part.length());
		}
		return count;
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSelection.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
