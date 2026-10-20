/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.environment.PromptPlaceholders;
import com.top_logic.tl.ai.core.agent.environment.RunEnvironment;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.util.Resources;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Tests for {@link TextPromptContributor}: a host gives its agents a text without Java code, in the
 * language of the acting account, to the agents it names (Gitea #648).
 */
public class TestTextPromptContributor extends TestCase {

	/**
	 * Boots the module the typed configuration and the resources need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestTextPromptContributor.class));
	}

	private static final AgentModelAssignment ASSIGNMENT = new AgentModelAssignment("test-model",
		null, null, null, null, null, null, null, null, null, null, null, null, null, null);

	private static final AgentDefinition ALBERT = agent("TL_SingleAgent");

	private static final AgentDefinition IMPROVER = agent("TL_PromptImprover");

	/** What a host writes into {@code <prompt-contributors>}, for one agent. */
	private static final String ALBERT_ONLY = """
		<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor"
			agents="TL_SingleAgent"
		>
			<text>
				<de>## Fachwissen
		Ein Auftrag ist 'offen', bis er abgerechnet ist.</de>
				<en>## Domain knowledge
		An order is 'open' until it is invoiced.</en>
			</text>
		</contributor>
		""";

	/**
	 * The account's language picks the text; a language with no text gets the one the engine falls
	 * back to.
	 */
	public void testLanguageOfTheAccount() throws ConfigurationException {
		TextPromptContributor contributor = contributor(ALBERT_ONLY);

		assertEquals("## Fachwissen\nEin Auftrag ist 'offen', bis er abgerechnet ist.",
			contributor.text(Locale.GERMAN));
		assertEquals("## Domain knowledge\nAn order is 'open' until it is invoiced.",
			contributor.text(Locale.ENGLISH));
		String fallback = contributor.text(Locale.FRENCH);
		assertTrue("A language without a text gets one of the configured texts: " + fallback,
			contributor.text(Locale.GERMAN).equals(fallback) || contributor.text(Locale.ENGLISH).equals(fallback));
	}

	/**
	 * The named agent gets the text once, after its own prompt; another agent does not get it.
	 */
	public void testOnlyTheNamedAgentGetsTheText() throws ConfigurationException {
		List<SystemPromptContributor> contributors = List.of(contributor(ALBERT_ONLY));

		String albert = AgentFactory.appendContributions(ALBERT, ASSIGNMENT, "You are Albert.", contributors);
		String improver = AgentFactory.appendContributions(IMPROVER, ASSIGNMENT, "Improve the draft.",
			contributors);

		assertTrue(albert, albert.startsWith("You are Albert.\n\n"));
		assertEquals("Exactly once: " + albert, 1, occurrences(albert, "invoiced") + occurrences(albert, "abgerechnet"));
		assertEquals("Improve the draft.", improver);
	}

	/**
	 * Without {@code agents}, every agent gets the text.
	 */
	public void testNoAgentsMeansEveryAgent() throws ConfigurationException {
		TextPromptContributor contributor = contributor("""
			<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor">
				<text>
					<en>House rule.</en>
				</text>
			</contributor>
			""");

		assertTrue(contributor.appliesTo(ALBERT.name()));
		assertTrue(contributor.appliesTo(IMPROVER.name()));
		assertEquals("Improve the draft.\n\nHouse rule.",
			AgentFactory.appendContributions(IMPROVER, ASSIGNMENT, "Improve the draft.", List.of(contributor)));
	}

	/**
	 * A placeholder in the text is filled like one in the agent's own prompt, because the text is
	 * appended before the environment block is composed.
	 */
	public void testPlaceholdersAreFilled() throws ConfigurationException {
		TextPromptContributor contributor = contributor("""
			<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor">
				<text>
					<en>You work for {{user}} in {{application}}.</en>
				</text>
			</contributor>
			""");

		String prompt = AgentFactory.appendContributions(ALBERT, ASSIGNMENT, "You are Albert.", List.of(contributor));
		String filled = RunEnvironment.of("", Map.of(
			PromptPlaceholders.USER, "Ada Lovelace",
			PromptPlaceholders.APPLICATION, "Order Desk")).apply(prompt);

		assertEquals("You are Albert.\n\nYou work for Ada Lovelace in Order Desk.", filled);
	}

	/**
	 * An unknown placeholder, in any of the languages, is a constraint violation - the check the
	 * engine runs on reading the application configuration and on saving a service in the service
	 * editor.
	 */
	public void testUnknownPlaceholderIsRefused() throws ConfigurationException {
		PolymorphicConfiguration<?> config = read("""
			<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor">
				<text>
					<de>Für {{user}}.</de>
					<en>For {{usr}} on {{today}}.</en>
				</text>
			</contributor>
			""");

		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);

		assertEquals(1, checker.getFailures().size());
		ResKey message = checker.getFailures().get(0).getMessage();
		String text = Resources.getInstance(Locale.ENGLISH).getString(message);
		assertTrue(text, text.contains("{{usr}}, {{today}}"));
	}

	/**
	 * A text whose placeholders are all known passes the check.
	 */
	public void testKnownPlaceholdersPass() throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(read(ALBERT_ONLY.replace("## Domain knowledge", "## Domain knowledge for {{user}}")));

		assertEquals(List.of(), checker.getFailures());
	}

	/**
	 * The text is fixed when the contributor is built - which is when the service starts - so every
	 * run asks the same text and an edited configuration reaches no run before the service restarts.
	 */
	public void testTextIsConstantUntilRestart() throws ConfigurationException {
		@SuppressWarnings("unchecked")
		TextPromptContributor.Config<TextPromptContributor> config =
			(TextPromptContributor.Config<TextPromptContributor>) read(ALBERT_ONLY);
		TextPromptContributor running = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		String first = running.contribute(ALBERT, ASSIGNMENT);

		// What saving in the service editor does: a changed copy of the configuration.
		TextPromptContributor.Config<?> edited = TypedConfiguration.copy(config);
		edited.update(edited.descriptor().getProperty(TextPromptContributor.Config.TEXT),
			ResKey.builder().add(Locale.ENGLISH, "New text.").add(Locale.GERMAN, "Neuer Text.").build());

		assertEquals(first, running.contribute(ALBERT, ASSIGNMENT));
		assertEquals("New text.",
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(edited).text(Locale.ENGLISH));
	}

	/**
	 * A text the service editor wrote back reaches the prompt without the indentation the
	 * pretty-printer gave its lines.
	 */
	public void testIndentationOfASavedTextIsRemoved() throws ConfigurationException {
		TextPromptContributor contributor = contributor("""
			<contributor class="com.top_logic.tl.ai.core.agent.TextPromptContributor">
				<text>
					<en>
						## Domain knowledge
						An order is 'open' until it is invoiced.

						- One item
						  - A nested item
					</en>
				</text>
			</contributor>
			""");

		assertEquals("## Domain knowledge\nAn order is 'open' until it is invoiced.\n\n- One item\n  - A nested item",
			contributor.text(Locale.ENGLISH));
	}

	/**
	 * Saving in the service editor and reading the file back gives the text exactly as it was
	 * entered - also a nested list, whose indentation the editor's pretty-printer would strip from
	 * plain element text.
	 */
	public void testServiceEditorRoundTripKeepsTheText() throws ConfigurationException {
		String entered = "## Domain knowledge\n\n- One item\n  - A nested item\n\nYou work for {{user}}.";
		TextPromptContributor.Config<?> config =
			TypedConfiguration.newConfigItem(TextPromptContributor.Config.class);
		config.update(config.descriptor().getProperty(TextPromptContributor.Config.TEXT),
			ResKey.builder().add(Locale.ENGLISH, entered).build());

		String written = InAppServiceConfigStore.toXml("contributor", config, Set.of());
		TextPromptContributor reread =
			(TextPromptContributor) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(read(written));

		assertTrue(written, written.contains("<![CDATA["));
		assertEquals(entered, reread.text(Locale.ENGLISH));
	}

	/**
	 * A text written without indentation stays as it is.
	 */
	public void testUnindentedTextIsKept() {
		assertEquals("A\n  B\nC", TextPromptContributor.unindent("A\n  B\nC\n"));
		assertEquals("One line", TextPromptContributor.unindent("  One line  "));
		assertNull(TextPromptContributor.unindent(null));
	}

	private static TextPromptContributor contributor(String xml) throws ConfigurationException {
		return (TextPromptContributor) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(read(xml));
	}

	private static PolymorphicConfiguration<?> read(String xml) throws ConfigurationException {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(PolymorphicConfiguration.class);
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("contributor", descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		return (PolymorphicConfiguration<?>) reader.read();
	}

	private static AgentDefinition agent(String name) {
		return new AgentDefinition(name, null, "", null, "", List.of(), List.of(), List.of(), List.of(),
			AgentSource.CONFIG);
	}

	private static int occurrences(String text, String part) {
		int count = 0;
		for (int index = text.indexOf(part); index >= 0; index = text.indexOf(part, index + 1)) {
			count++;
		}
		return count;
	}

}
