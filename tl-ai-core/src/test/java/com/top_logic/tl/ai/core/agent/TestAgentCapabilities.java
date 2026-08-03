/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests {@link AgentCapabilities}: which skills an agent offers a given principal, and what its
 * prompt tells that principal's agent about the capabilities they lack.
 *
 * <p>
 * The two methods exercised here are the whole difference an enforced run makes to a system
 * prompt, so pinning both to "no effect" under {@link AiSecurityMode#LEGACY} is what keeps an
 * unmigrated application's prompt byte-identical to what it was before this existed.
 * </p>
 */
public class TestAgentCapabilities extends TestCase {

	private static final String READ_TOOL = "data.readInstances";

	private static final String WRITE_TOOL = "data.createInstance";

	private static final String SCRIPT_TOOL = "script.evaluate";

	private static final String UNGROUPED_TOOL = "custom.mystery";

	/** An unenforced run adds nothing to the prompt, whatever the principal may do. */
	public void testLegacyNoticeIsEmpty() {
		AgentCapabilities capabilities = capabilities(AiSecurityMode.LEGACY, Set.of());
		assertFalse(capabilities.enforced());
		assertEquals("", capabilities.notice(List.of(READ_TOOL), List.of(skill("s", WRITE_TOOL))));
	}

	/** An unenforced run drops no skill, not even one whose every tool would be denied. */
	public void testLegacyKeepsEverySkill() {
		AgentCapabilities capabilities = capabilities(AiSecurityMode.LEGACY, Set.of());
		assertNull(capabilities.unusableReason(skill("writing", WRITE_TOOL, SCRIPT_TOOL)));
	}

	/** A run with no user identity is not an enforced run either. */
	public void testSystemModeIsNotEnforced() {
		assertFalse(AgentCapabilities.forRun(SecurityContext.UNRESTRICTED).enforced());
	}

	/** The notice names exactly the capabilities the principal lacks, and no others. */
	public void testNoticeNamesLackingCapabilities() {
		AgentCapabilities capabilities = capabilities(AiSecurityMode.ENFORCED, Set.of(READ_TOOL));
		String notice = capabilities.notice(List.of(READ_TOOL),
			List.of(skill("writing", WRITE_TOOL), skill("scripting", SCRIPT_TOOL)));

		assertTrue(notice, notice.contains("`" + ToolGroups.DATA_WRITE + "`"));
		assertTrue(notice, notice.contains("`" + ToolGroups.SCRIPT + "`"));
		assertFalse("A capability the principal holds is not reported as missing: " + notice,
			notice.contains("`" + ToolGroups.DATA_READ + "`"));
	}

	/** The wording matches what the model actually receives on a denial. */
	public void testNoticeUsesTheRealDenialWording() {
		String notice = capabilities(AiSecurityMode.ENFORCED, Set.of())
			.notice(List.of(WRITE_TOOL), List.of());
		assertTrue(notice, notice.contains("Permission denied"));
		assertFalse("'unauthorized' appears in no tool-facing message: " + notice,
			notice.toLowerCase().contains("unauthorized"));
	}

	/** A principal who may use everything is told so rather than shown an empty list. */
	public void testNoticeWithoutLackingCapabilities() {
		AgentCapabilities capabilities =
			capabilities(AiSecurityMode.ENFORCED, Set.of(READ_TOOL, WRITE_TOOL));
		String notice = capabilities.notice(List.of(READ_TOOL), List.of(skill("writing", WRITE_TOOL)));
		assertTrue(notice, notice.contains("holds every capability"));
	}

	/**
	 * An ungrouped tool contributes no capability name - there is none to act on - but its denial
	 * must not be reported as "this user holds everything" either.
	 */
	public void testUngroupedToolIsNotNamedAsACapability() {
		String notice = capabilities(AiSecurityMode.ENFORCED, Set.of())
			.notice(List.of(UNGROUPED_TOOL), List.of());
		assertFalse(notice, notice.contains("holds every capability"));
		assertFalse(notice, notice.contains("does **not** hold"));
	}

	/** A skill that is pure instruction text survives - "all zero denied" must not delete it. */
	public void testZeroToolSkillSurvives() {
		AgentCapabilities capabilities = capabilities(AiSecurityMode.ENFORCED, Set.of());
		assertNull(capabilities.unusableReason(skill("promptOnly")));
	}

	/** A skill none of whose tools the principal may use is dropped, with a reason worth logging. */
	public void testFullyDeniedSkillIsDropped() {
		AgentCapabilities capabilities = capabilities(AiSecurityMode.ENFORCED, Set.of(READ_TOOL));
		String reason = capabilities.unusableReason(skill("writing", WRITE_TOOL, SCRIPT_TOOL));
		assertNotNull(reason);
		assertTrue(reason, reason.contains(ToolGroups.DATA_WRITE));
		assertTrue(reason, reason.contains(ToolGroups.SCRIPT));
	}

	/** One usable tool keeps the skill: its instructions still apply to that tool. */
	public void testPartiallyDeniedSkillSurvives() {
		AgentCapabilities capabilities = capabilities(AiSecurityMode.ENFORCED, Set.of(READ_TOOL));
		assertNull(capabilities.unusableReason(skill("mixed", READ_TOOL, WRITE_TOOL)));
	}

	/** A typo in a skill's tool list is not a denial and must not remove the skill. */
	public void testSkillOfOnlyUnresolvableToolsSurvives() {
		AgentCapabilities capabilities = capabilities(AiSecurityMode.ENFORCED, Set.of());
		assertNull(capabilities.unusableReason(skill("typo", "no.suchTool", "also.missing")));
	}

	/** An unresolvable name does not count towards "all of them are denied" either. */
	public void testUnresolvableToolDoesNotSaveADeniedSkill() {
		AgentCapabilities capabilities = capabilities(AiSecurityMode.ENFORCED, Set.of(READ_TOOL));
		assertNotNull(capabilities.unusableReason(skill("mostlyGone", "no.suchTool", WRITE_TOOL)));
	}

	/**
	 * An administrator sees every skill. No {@code isAdmin()} check exists here: the tool
	 * authorization already returns "allowed" for one, which is what this stands in for.
	 */
	public void testPrincipalWhoMayUseEverythingSeesEverySkill() {
		AgentCapabilities capabilities =
			capabilities(AiSecurityMode.ENFORCED, Set.of(READ_TOOL, WRITE_TOOL, SCRIPT_TOOL, UNGROUPED_TOOL));
		assertNull(capabilities.unusableReason(skill("writing", WRITE_TOOL, SCRIPT_TOOL)));
		assertTrue(capabilities.notice(List.of(READ_TOOL), List.of(skill("writing", WRITE_TOOL)))
			.contains("holds every capability"));
	}

	// ===== The two call sites that apply the decision =====

	/**
	 * An unenforced run produces the very skill catalog it produced before this existed: the same
	 * skills, in the same order, and nothing appended.
	 */
	public void testLegacyCatalogIsUnchanged() {
		String catalog = catalogOf(AiSecurityMode.LEGACY, Set.of());
		assertEquals("### Available Skills\n\n"
			+ "You have specialized skills. "
			+ "Load a skill BEFORE performing the related action by calling `loadSkill(\"skill-id\")`.\n\n"
			+ "- `promptOnly`: promptOnly\n"
			+ "- `mixed`: mixed\n"
			+ "- `writing`: writing\n"
			+ "- `typo`: typo\n", catalog);
	}

	/** An agent with no skills gets no section at all, enforced or not. */
	public void testNoSkillsProducesNoSection() {
		assertEquals("", AgentFactory.skillCatalog("agent", List.of(), List.of(READ_TOOL),
			name -> null, capabilities(AiSecurityMode.ENFORCED, Set.of())));
	}

	/** Under enforcement the catalog keeps what is usable, drops what is not, and names the gap. */
	public void testEnforcedCatalogDropsTheUnusableSkill() {
		String catalog = catalogOf(AiSecurityMode.ENFORCED, Set.of(READ_TOOL));

		assertTrue(catalog, catalog.contains("- `promptOnly`"));
		assertTrue(catalog, catalog.contains("- `mixed`"));
		assertTrue(catalog, catalog.contains("- `typo`"));
		assertFalse("The fully denied skill is not offered: " + catalog,
			catalog.contains("- `writing`"));
		assertTrue("The capability behind the dropped skill is named: " + catalog,
			catalog.contains("`" + ToolGroups.DATA_WRITE + "`"));
	}

	/** {@code loadSkill} refuses exactly what the catalog no longer offers. */
	public void testEnforcedSkillMapDropsTheUnusableSkill() {
		Map<String, ?> loadable = AgentFactory.buildSkillMap(skillNames(), skillLookup(),
			capabilities(AiSecurityMode.ENFORCED, Set.of(READ_TOOL)));

		assertEquals(Set.of("promptOnly", "mixed", "typo"), loadable.keySet());
	}

	/** Without enforcement every declared skill stays loadable. */
	public void testLegacySkillMapKeepsEverySkill() {
		Map<String, ?> loadable = AgentFactory.buildSkillMap(skillNames(), skillLookup(),
			capabilities(AiSecurityMode.LEGACY, Set.of()));

		assertEquals(Set.of("promptOnly", "mixed", "writing", "typo"), loadable.keySet());
	}

	private static String catalogOf(AiSecurityMode mode, Set<String> permittedTools) {
		return AgentFactory.skillCatalog("agent", skillNames(), List.of(READ_TOOL), skillLookup(),
			capabilities(mode, permittedTools));
	}

	private static List<String> skillNames() {
		return List.of("promptOnly", "mixed", "writing", "typo");
	}

	private static Function<String, SkillDefinition> skillLookup() {
		Map<String, SkillDefinition> skills = new LinkedHashMap<>();
		skills.put("promptOnly", skill("promptOnly"));
		skills.put("mixed", skill("mixed", READ_TOOL, WRITE_TOOL));
		skills.put("writing", skill("writing", WRITE_TOOL, SCRIPT_TOOL));
		skills.put("typo", skill("typo", "no.suchTool"));
		return skills::get;
	}

	// ===== Helpers =====

	private static AgentCapabilities capabilities(AiSecurityMode mode, Set<String> permittedTools) {
		Map<String, Tool> registry = new HashMap<>();
		registry.put(READ_TOOL, tool(READ_TOOL, ToolGroups.DATA_READ));
		registry.put(WRITE_TOOL, tool(WRITE_TOOL, ToolGroups.DATA_WRITE));
		registry.put(SCRIPT_TOOL, tool(SCRIPT_TOOL, ToolGroups.SCRIPT));
		registry.put(UNGROUPED_TOOL, tool(UNGROUPED_TOOL, ""));

		SecurityContext security = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return permittedTools.contains(tool.name());
			}

			@Override
			public AiSecurityMode securityMode() {
				return mode;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
		return new AgentCapabilities(registry::get, security);
	}

	private static SkillDefinition skill(String name, String... tools) {
		return new SkillDefinition(name, name, "content of " + name, List.of(tools), AgentSource.CONFIG);
	}

	private static Tool tool(String name, String group) {
		return new Tool() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return "Stub: " + name;
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(name, description(), "{\"type\":\"object\",\"properties\":{}}");
			}

			@Override
			public String group() {
				return group;
			}

			@Override
			public ToolMode mode() {
				return ToolMode.READ;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("ok");
			}
		};
	}
}
