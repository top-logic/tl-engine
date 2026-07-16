/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests for {@link SearchToolsTool}.
 */
public class TestSearchToolsTool extends TestCase {

	private SearchToolsTool _tool;

	@Override
	protected void setUp() {
		// The registry keys tools in provider-safe notation ({factory}__{tool}), which is what
		// AgentFactory.buildSearchTool passes here. Skills, however, reference their tools in
		// config notation ({factory}.{tool}). SearchToolsTool must bridge the two.
		Map<String, ToolSchema> allTools = new LinkedHashMap<>();
		allTools.put("model-browser__listModules",
			new ToolSchema("model-browser__listModules", "List all modules in the data model",
				"{\"type\":\"object\",\"properties\":{}}"));
		allTools.put("model-browser__getType",
			new ToolSchema("model-browser__getType", "Get details of a specific type",
				"{\"type\":\"object\",\"properties\":{}}"));
		allTools.put("script-exec__executeTLScript",
			new ToolSchema("script-exec__executeTLScript", "Execute a TL-Script expression",
				"{\"type\":\"object\",\"properties\":{}}"));
		allTools.put("web-search__webSearch",
			new ToolSchema("web-search__webSearch", "Search the web",
				"{\"type\":\"object\",\"properties\":{}}"));

		Map<String, Skill> skills = new LinkedHashMap<>();
		skills.put("TL_modelExploration", new Skill("TL_modelExploration",
			"Explore the TopLogic data model",
			"skill content",
			List.of("model-browser.listModules", "model-browser.getType")));
		skills.put("TL_scriptExecution", new Skill("TL_scriptExecution",
			"Execute TL-Script expressions",
			"skill content",
			List.of("script-exec.executeTLScript")));

		_tool = new SearchToolsTool(allTools, skills);
	}

	/** Searching for "model" finds the modelExploration skill. */
	public void testSearchByKeyword() {
		String result = _tool.searchTools("model");
		assertTrue("Should mention TL_modelExploration", result.contains("TL_modelExploration"));
	}

	/** Results include the loadSkill hint. */
	public void testResultIncludesLoadSkillHint() {
		String result = _tool.searchTools("model");
		assertTrue("Should include loadSkill hint",
			result.contains("loadSkill(\"TL_modelExploration\")"));
	}

	/** Searching for "script" finds the scriptExecution skill. */
	public void testSearchFindsScriptSkill() {
		String result = _tool.searchTools("script");
		assertTrue("Should mention TL_scriptExecution", result.contains("TL_scriptExecution"));
	}

	/**
	 * Tools not assigned to any skill are unreachable for agents, so they must not appear
	 * in the result — a query matching only such tools reports no matches.
	 */
	public void testUngroupedToolsHidden() {
		String result = _tool.searchTools("web search");
		assertFalse("Must not show tool assigned to no skill", result.contains("web-search__webSearch"));
		assertTrue("Should indicate no matches", result.contains("No tools found"));
	}

	/** No matches returns a helpful message. */
	public void testNoMatches() {
		String result = _tool.searchTools("zzzznonexistent");
		assertTrue("Should indicate no matches", result.contains("No tools found"));
	}

	/** Search is case-insensitive. */
	public void testCaseInsensitive() {
		String result = _tool.searchTools("MODEL");
		assertTrue("Case insensitive match", result.contains("TL_modelExploration"));
	}

	/**
	 * Regression: skills reference tools in config notation ({factory}.{tool}) while the registry
	 * keys them in provider notation ({factory}__{tool}). The search must bridge the two and list
	 * the matched tool under its registry name.
	 */
	public void testBridgesConfigAndProviderNotation() {
		String result = _tool.searchTools("modules");
		assertTrue("Should surface the matched tool by its registry name: " + result,
			result.contains("model-browser__listModules"));
	}
}
