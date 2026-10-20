/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.skills;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/** */
public class TestSkillTool extends TestCase {

	private SkillTool _tool;

	private DynamicToolCatalog _dynamicCatalog;

	@Override
	protected void setUp() {
		ToolCatalog base = new ToolCatalog();
		base.freeze();
		_dynamicCatalog = new DynamicToolCatalog(base);

		Map<String, Skill> skills = new LinkedHashMap<>();
		skills.put("test/frontmatter-skill",
			new Skill("test/frontmatter-skill", "A test skill with frontmatter",
				"FRONTMATTER RULE content", List.of("test.toolA", "test.toolB")));
		skills.put("test/sample-skill",
			new Skill("test/sample-skill", "test/sample-skill",
				"SAMPLE RULE content", List.of()));
		skills.put("test/unresolvable-skill",
			new Skill("test/unresolvable-skill", "A skill with bad tools",
				"UNRESOLVABLE content", List.of("nonexistent.tool")));

		_tool = new SkillTool(skills, _dynamicCatalog, TestSkillTool::resolveTool);
	}

	/** */
	public void testLoadSkillReturnsContent() {
		String result = _tool.loadSkill("test/frontmatter-skill");
		assertTrue("Should contain skill content", result.contains("FRONTMATTER RULE"));
	}

	/**
	 * <b>A loaded skill is not untrusted content, and must not be marked as any.</b>
	 *
	 * <p>
	 * {@code loadSkill} is documented as returning "specialized instructions you MUST follow", so it is
	 * the one tool result that is <em>meant</em> to be an instruction. The standing rule the system
	 * prompt carries (see {@code UntrustedEnvelope.standingRule}) is scoped to spans marked with
	 * {@code <untrusted_*>} tags precisely so that it leaves this alone: a rule that covered tool
	 * results in general would switch the skills mechanism off, and a skill that arrived inside an
	 * envelope would be told, by the framework, to be ignored.
	 * </p>
	 *
	 * <p>
	 * Pinned as its own test because the failure is silent. Nothing throws; the agent simply stops
	 * doing what its skills say, and the reason is a sentence in a prompt half the codebase away.
	 * </p>
	 */
	public void testLoadedSkillContentIsNotMarkedAsUntrusted() {
		String result = _tool.loadSkill("test/frontmatter-skill");

		assertTrue("Precondition: the skill content has to be in the result.",
			result.contains("FRONTMATTER RULE"));
		assertFalse("A loaded skill must carry no untrusted-content envelope - the standing rule would"
			+ " then tell the model to treat its own instructions as data. Got:\n" + result,
			result.contains("untrusted_"));
	}

	/** */
	public void testLoadSkillRejectsUnregisteredSkill() {
		String result = _tool.loadSkill("nonexistent/skill");
		assertTrue("Should return error message", result.contains("not available"));
	}

	/** */
	public void testLoadSkillListsAvailableOnError() {
		String result = _tool.loadSkill("nonexistent/skill");
		assertTrue("Should list available skills", result.contains("test/frontmatter-skill"));
	}

	/** Loading a skill with tools adds them to the dynamic catalog. */
	public void testLoadSkillAddsToolsToCatalog() {
		assertNull("toolA not in catalog before load", _dynamicCatalog.getTool("test__toolA"));

		_tool.loadSkill("test/frontmatter-skill");

		assertNotNull("toolA in catalog after load", _dynamicCatalog.getTool("test__toolA"));
		assertNotNull("toolB in catalog after load", _dynamicCatalog.getTool("test__toolB"));
	}

	/** Loading a skill with no tools still works (no overlay added). */
	public void testLoadSkillWithNoToolsAddsNoOverlay() {
		String result = _tool.loadSkill("test/sample-skill");
		assertTrue("Should still return content", result.contains("SAMPLE RULE"));
	}

	/** Loading the same skill twice doesn't duplicate tools. */
	public void testIdempotentLoad() {
		_tool.loadSkill("test/frontmatter-skill");
		String secondResult = _tool.loadSkill("test/frontmatter-skill");

		// Should have 2 tool schemas (toolA, toolB), not 4
		assertEquals(2, _dynamicCatalog.toolSchemas(t -> true).size());
		// Second load should return short message, not full content
		assertTrue("Should say already loaded", secondResult.contains("already loaded"));
		assertFalse("Should NOT contain full skill content",
			secondResult.contains("FRONTMATTER RULE"));
	}

	/** Result includes list of added tools. */
	public void testResultListsAddedTools() {
		String result = _tool.loadSkill("test/frontmatter-skill");
		assertTrue("Should list added tools", result.contains("test__toolA"));
		assertTrue("Should list added tools", result.contains("test__toolB"));
	}

	/** Unresolvable tool names are skipped with a warning in the result. */
	public void testUnresolvableToolSkipped() {
		String result = _tool.loadSkill("test/unresolvable-skill");
		assertTrue("Should contain content", result.contains("UNRESOLVABLE"));
	}

	/** Preloaded skill IDs restore tool overlays at construction time. */
	public void testPreloadedSkillsRestoreTools() {
		ToolCatalog base = new ToolCatalog();
		base.freeze();
		DynamicToolCatalog catalog = new DynamicToolCatalog(base);

		Map<String, Skill> skills = new LinkedHashMap<>();
		skills.put("test/frontmatter-skill",
			new Skill("test/frontmatter-skill", "A test skill with frontmatter",
				"FRONTMATTER RULE content", List.of("test.toolA", "test.toolB")));

		Set<String> preloaded = Set.of("test/frontmatter-skill");
		SkillTool tool = new SkillTool(skills, catalog, TestSkillTool::resolveTool, null, preloaded);

		// Tools should be available immediately without calling loadSkill
		assertNotNull("toolA available after preload", catalog.getTool("test__toolA"));
		assertNotNull("toolB available after preload", catalog.getTool("test__toolB"));

		// loadSkill should return "already loaded"
		String result = tool.loadSkill("test/frontmatter-skill");
		assertTrue("Should say already loaded", result.contains("already loaded"));
	}

	/** Preloading a skill that was removed is silently skipped. */
	public void testPreloadedSkillRemovedSilently() {
		ToolCatalog base = new ToolCatalog();
		base.freeze();
		DynamicToolCatalog catalog = new DynamicToolCatalog(base);

		Map<String, Skill> skills = new LinkedHashMap<>(); // no skills registered

		Set<String> preloaded = Set.of("removed/skill");
		// Should not throw
		new SkillTool(skills, catalog, TestSkillTool::resolveTool, null, preloaded);
	}

	/**
	 * A tool the principal may not use is neither registered nor announced as available.
	 *
	 * <p>
	 * The bug this pins: announcing it puts an authoritative tool result in the conversation that
	 * the next turn's schema filter contradicts, and a model that believes it earns a denial.
	 * </p>
	 */
	public void testDeniedToolIsNeitherRegisteredNorAnnounced() {
		ToolCatalog base = new ToolCatalog();
		base.freeze();
		DynamicToolCatalog catalog = new DynamicToolCatalog(base);

		Map<String, Skill> skills = new LinkedHashMap<>();
		skills.put("test/mixed-skill",
			new Skill("test/mixed-skill", "Partly permitted", "MIXED content",
				List.of("test.toolA", "test.toolB")));

		SkillTool tool = new SkillTool(skills, catalog, TestSkillTool::resolveTool,
			t -> "test__toolA".equals(t.name()), null);

		String result = tool.loadSkill("test/mixed-skill");

		assertNotNull("The permitted tool is registered", catalog.getTool("test__toolA"));
		assertNull("The denied tool is not registered", catalog.getTool("test__toolB"));
		assertTrue("The permitted tool is announced: " + result, result.contains("test__toolA"));
		assertFalse("The denied tool is not announced: " + result, result.contains("test__toolB"));
		assertTrue("The skill's instructions still arrive", result.contains("MIXED content"));
	}

	/** Restoring a session's skills applies the same filter as loading them. */
	public void testPreloadedSkillWithholdsDeniedTools() {
		ToolCatalog base = new ToolCatalog();
		base.freeze();
		DynamicToolCatalog catalog = new DynamicToolCatalog(base);

		Map<String, Skill> skills = new LinkedHashMap<>();
		skills.put("test/mixed-skill",
			new Skill("test/mixed-skill", "Partly permitted", "MIXED content",
				List.of("test.toolA", "test.toolB")));

		new SkillTool(skills, catalog, TestSkillTool::resolveTool,
			t -> "test__toolA".equals(t.name()), Set.of("test/mixed-skill"));

		assertNotNull("The permitted tool is restored", catalog.getTool("test__toolA"));
		assertNull("The denied tool is not restored", catalog.getTool("test__toolB"));
	}

	/**
	 * Past the ceiling, a load says which skill it cost - and the lost skill can be loaded again.
	 *
	 * <p>
	 * The contract of Gitea #346: overlays are bounded, and the model is not left calling a tool
	 * that quietly went away. It read the evicted skill's instructions earlier in this very
	 * conversation.
	 * </p>
	 */
	public void testLoadingPastTheCeilingSaysWhatItCost() {
		ToolCatalog base = new ToolCatalog();
		base.freeze();
		DynamicToolCatalog catalog = new DynamicToolCatalog(base, 1);

		Map<String, Skill> skills = new LinkedHashMap<>();
		skills.put("skill/a", new Skill("skill/a", "A", "A content", List.of("test.toolA")));
		skills.put("skill/b", new Skill("skill/b", "B", "B content", List.of("test.toolB")));
		SkillTool tool = new SkillTool(skills, catalog, TestSkillTool::resolveTool);

		tool.loadSkill("skill/a");
		String second = tool.loadSkill("skill/b");

		assertTrue("The eviction must be stated: " + second, second.contains("skill/a"));
		assertTrue("The eviction must be stated: " + second, second.contains("unloaded"));
		assertNull("The evicted skill's tool is gone", catalog.getTool("test__toolA"));
		assertEquals(Set.of("skill/b"), tool.loadedSkills());

		String again = tool.loadSkill("skill/a");
		assertFalse("An evicted skill must load again, not answer 'already loaded': " + again,
			again.contains("already loaded"));
		assertNotNull("Its tool must come back", catalog.getTool("test__toolA"));
	}

	/** A skill without tools counts as loaded, so its content is not dumped twice. */
	public void testASkillWithoutToolsIsLoadedToo() {
		assertTrue(_tool.loadSkill("test/sample-skill").contains("SAMPLE RULE"));
		assertEquals(Set.of("test/sample-skill"), _tool.loadedSkills());

		String again = _tool.loadSkill("test/sample-skill");
		assertTrue("The second load must be the short answer: " + again,
			again.contains("already loaded"));
		assertFalse("The content must not be repeated: " + again, again.contains("SAMPLE RULE"));
	}

	/**
	 * After a compaction the instructions are delivered again, and the tools never left.
	 *
	 * <p>
	 * A summarizing compaction can replace the turn that carried a skill's text. The tools stay -
	 * taking a capability away for a reason the model cannot see is worse - but the short "already
	 * loaded" answer would leave the model following a skill it can no longer read.
	 * </p>
	 */
	public void testACompactionMakesTheInstructionsLoadableAgain() {
		assertTrue(_tool.loadSkill("test/frontmatter-skill").contains("FRONTMATTER RULE"));
		assertTrue(_tool.loadSkill("test/frontmatter-skill").contains("already loaded"));

		_dynamicCatalog.historyCompacted();

		String reloaded = _tool.loadSkill("test/frontmatter-skill");
		assertTrue("The instructions must arrive again: " + reloaded,
			reloaded.contains("FRONTMATTER RULE"));
		assertNotNull("The tools must not have moved", _dynamicCatalog.getTool("test__toolA"));
		assertEquals(List.of("test/frontmatter-skill"), _dynamicCatalog.loadedSkills());
	}

	/** Restored skills keep the order the session loaded them in. */
	public void testRestoreKeepsTheLoadOrder() {
		ToolCatalog base = new ToolCatalog();
		base.freeze();
		DynamicToolCatalog catalog = new DynamicToolCatalog(base);

		Map<String, Skill> skills = new LinkedHashMap<>();
		skills.put("skill/a", new Skill("skill/a", "A", "A content", List.of("test.toolA")));
		skills.put("skill/b", new Skill("skill/b", "B", "B content", List.of("test.toolB")));

		new SkillTool(skills, catalog, TestSkillTool::resolveTool, null,
			new LinkedHashSet<>(List.of("skill/b", "skill/a")));

		assertEquals(List.of("skill/b", "skill/a"), catalog.loadedSkills());
	}

	// ===== Helpers =====

	private static Tool resolveTool(String qualifiedName) {
		if (qualifiedName.startsWith("nonexistent.")) {
			return null;
		}
		// Simulate ToolRegistryService sanitization: config name → provider name
		String providerName = ToolRegistryService.toProviderName(qualifiedName);
		return ToolStubs.builder(providerName).build();
	}
}
