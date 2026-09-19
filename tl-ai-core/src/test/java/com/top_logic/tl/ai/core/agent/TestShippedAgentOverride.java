/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * That an installation renames an agent it was shipped by overriding one field of it.
 *
 * <p>
 * The shipped agents live in {@code tl-ai-core.conf.config.xml}; an installation overrides them
 * through the ordinary configuration overlay, which is what the service editor of
 * <em>TopLogic-Agents and Skills</em> writes into the application's autoconf folder. The test
 * application carries such an overlay
 * ({@code src/test/webapp/WEB-INF/autoconf/com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions.config.xml}),
 * and it sets exactly one scalar: the display name of {@link #AGENT}.
 * </p>
 *
 * <p>
 * What is under test is therefore not that a configuration value arrives - it is that the overlay
 * replaces the <em>one</em> field it names and leaves the rest of the shipped definition alone. An
 * overlay that replaced the whole entry would take the agent's summary, prompt, tools and skill with
 * it, and an installation that renamed an agent would stop receiving every later release's work on
 * it.
 * </p>
 *
 * @see TopLogicAgentDefinitions.AgentConfig#getDisplayName()
 */
public class TestShippedAgentOverride extends BasicTestCase {

	/** The shipped agent the test application overrides. */
	private static final String AGENT = "TL_ModelExplorer";

	/** What the overlay renames it to, in English. */
	private static final String OVERRIDDEN_NAME_EN = "Model Scout";

	/** What the overlay renames it to, in German. */
	private static final String OVERRIDDEN_NAME_DE = "Modell-Späher";

	/** The summary the overlay does not touch, in English, as shipped. */
	private static final String SHIPPED_SUMMARY_EN = "Explores model and data, changes nothing.";

	/** The summary the overlay does not touch, in German, as shipped. */
	private static final String SHIPPED_SUMMARY_DE = "Erkundet Modell und Daten, ändert nichts.";

	/** The agent's display name is the installation's, in the language of whoever reads it. */
	public void testTheOverrideReplacesTheDisplayName() {
		assertEquals(OVERRIDDEN_NAME_EN, english(agent().displayName()));
		assertEquals(OVERRIDDEN_NAME_DE, german(agent().displayName()));
	}

	/**
	 * The summary the overlay does not name is still the shipped one - in both languages.
	 *
	 * <p>
	 * The two texts are separate properties on purpose: an installation that renames an agent and
	 * says nothing about its summary keeps receiving the summary, and its translations, from every
	 * later release.
	 * </p>
	 */
	public void testTheOverrideLeavesTheSummaryAlone() {
		assertEquals(SHIPPED_SUMMARY_EN, english(agent().summary()));
		assertEquals(SHIPPED_SUMMARY_DE, german(agent().summary()));
	}

	/** The identifier is not what was renamed; it is what everything else refers to. */
	public void testTheIdentifierIsUnchanged() {
		assertEquals(AGENT, agent().name());
	}

	/** The capabilities the overlay says nothing about are still the shipped ones. */
	public void testTheOverrideLeavesToolsSkillsAndPromptAlone() {
		AgentDefinition agent = agent();

		assertEquals("The overlay names no skill, so the shipped one is still there.",
			List.of("TL_annotationDiscovery"), agent.skills());
		assertTrue("The overlay names no tool, so the shipped ones are still there: " + agent.tools(),
			agent.tools().contains("model-browser.listModules"));
		assertEquals("The shipped tool list flows through entry by entry.", 11, agent.tools().size());
		assertTrue("The shipped system prompt flows through: " + agent.systemPrompt(),
			agent.systemPrompt() != null && agent.systemPrompt().contains("TopLogic Model Explorer"));
		assertTrue("The model-facing description flows through: " + agent.description(),
			agent.description() != null && agent.description().startsWith("Delegate to the Model Explorer"));
	}

	/** An agent the overlay does not mention keeps the name it was shipped with. */
	public void testAnAgentWithNoOverrideIsUnchanged() {
		AgentDefinition albert = TopLogicAgentDefinitions.getInstance().getConfigAgent("TL_SingleAgent");

		assertNotNull("The test application ships TL_SingleAgent.", albert);
		assertEquals("Albert", english(albert.displayName()));
	}

	private static AgentDefinition agent() {
		AgentDefinition agent = TopLogicAgentDefinitions.getInstance().getConfigAgent(AGENT);
		assertNotNull("The test application ships " + AGENT + ".", agent);
		return agent;
	}

	private static String english(ResKey key) {
		return Resources.getInstance(Locale.ENGLISH).getString(key);
	}

	private static String german(ResKey key) {
		return Resources.getInstance(Locale.GERMAN).getString(key);
	}

	/** Suite of this test. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(
			new TestSuite(TestShippedAgentOverride.class), TopLogicAgentDefinitions.Module.INSTANCE));
	}
}
