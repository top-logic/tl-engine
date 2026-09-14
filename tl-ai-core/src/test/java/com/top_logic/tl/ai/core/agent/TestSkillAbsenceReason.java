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

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.AgentCapabilities.AgentGraph;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * What {@code loadSkill} answers when it cannot hand over the skill that was asked for.
 *
 * <p>
 * The bug this pins: the answer used to be "not available for this agent" for every reason a skill
 * can be missing, and the model - given a refusal it could not attribute - supplied a cause of its
 * own. Asked to use a tool no agent carries, it reported that the acting user lacked the
 * {@code script} capability and told them to have an administrator grant it. The user held
 * {@code data.write}, lacked {@code script} for nothing that mattered here, and the skill it had
 * actually been refused ({@code TL_agenticAuthoring}) needed {@code agent.meta} /
 * {@code agent.authoring}.
 * </p>
 *
 * <p>
 * Four answers, because there are four different things to do about them: ask an administrator for a
 * capability, accept that an unattended run cannot have it, ask an administrator to put the skill on
 * this agent, or pick a name that exists. The last must stay free of capability names - a typo may not
 * disclose what the user lacks - which is why it is asserted verbatim.
 * </p>
 *
 * <p>
 * The wiring is the real one: {@link AgentFactory#buildSkillMap} decides what is loadable and
 * {@link AgentFactory#absenceReason} explains what it dropped, so a message that stops matching the
 * decision fails here.
 * </p>
 *
 * @see AgentCapabilities#skillAbsenceReason(String, java.util.Collection)
 * @see com.top_logic.tl.ai.core.tools.ToolCatalog
 */
public class TestSkillAbsenceReason extends TestCase {

	private static final String READ_TOOL = "data.readInstances";

	private static final String WRITE_TOOL = "data.createInstance";

	private static final String SCRIPT_TOOL = "script.evaluate";

	private static final String UNGROUPED_TOOL = "custom.mystery";

	/** Declared by the agent, one {@code data.read} tool - the one it can always load. */
	private static final String READABLE_SKILL = "reading";

	/** Declared by the agent, every tool of it write- or script-classified. */
	private static final String WITHHELD_SKILL = "writing";

	/** Declared by the agent, its only tool ungrouped - denied with no capability to name. */
	private static final String UNGROUPED_SKILL = "mystery";

	/** A skill of the application that this agent does not declare. */
	private static final String FOREIGN_SKILL = "authoring";

	/** The agent under test. */
	private static final String AGENT = "agent";

	// ===== The skill is there and the acting user may use nothing in it =====

	/** The refusal names the capabilities an administrator would have to grant. */
	public void testWithheldSkillNamesItsCapabilities() {
		String message = loadSkill(readOnlyPrincipal(), WITHHELD_SKILL);

		assertTrue(message, message.contains("carried by this agent but withheld"));
		assertTrue("The capability an administrator has to grant must be named: " + message,
			message.contains("`" + ToolGroups.DATA_WRITE + "`"));
		assertTrue(message, message.contains("`" + ToolGroups.SCRIPT + "`"));
		assertTrue("The refusal must be final, not an invitation to retry: " + message,
			message.contains("that is final"));
	}

	/** ...and forbids the model to add a cause of its own. */
	public void testWithheldSkillForbidsTheGuess() {
		assertTrue(loadSkill(readOnlyPrincipal(), WITHHELD_SKILL).contains(
			AgentCapabilities.NO_GUESSING));
	}

	/**
	 * ...and scopes those capabilities to the skill, which is the whole of the fix.
	 *
	 * <p>
	 * The wording is pinned literally, not read from the constant: the first version said only "do not
	 * attribute this to a capability unless this message names one", and the message it closes
	 * <em>does</em> name capabilities - correctly, for the skill it refused. That was enough for the
	 * model, routed to {@code loadSkill("TL_agenticAuthoring")} by its prompt, to relay them as the
	 * reason a tool the user had named was unreachable. See {@code tl-ai-demo/doc/secdemo/testbed.md} rows
	 * C3/C4.
	 * </p>
	 */
	public void testTheGuessIsForbiddenForTheOtherSubjectsToo() {
		assertEquals("The scoping wording must not drift.",
			"These capabilities explain only this skill. Do not present them as the reason any other"
				+ " tool or skill is unreachable, and do not name a capability this message does not"
				+ " name.",
			AgentCapabilities.NO_GUESSING);
	}

	/** An attended run must not be explained as an unattended one. */
	public void testWithheldSkillDoesNotBlameTheRun() {
		assertFalse(loadSkill(readOnlyPrincipal(), WITHHELD_SKILL).contains("unattended"));
	}

	/**
	 * The list is what this agent can still load, so following it cannot walk into a second refusal.
	 */
	public void testWithheldSkillPointsAtWhatIsLoadable() {
		String message = loadSkill(readOnlyPrincipal(), WITHHELD_SKILL);

		assertTrue(message, message.contains("Available: "));
		assertTrue(message, message.contains(READABLE_SKILL));
		assertFalse("A skill this agent cannot load must not be offered as an alternative: " + message,
			message.contains("Available: [" + WITHHELD_SKILL));
	}

	/**
	 * A denied tool with no group names no capability - and the model is still forbidden to invent one.
	 *
	 * <p>
	 * "You lack the capability of a tool that has none" is not actionable: the deployment's answer to an
	 * unclassified tool is a {@code <tool-groups>} entry, not a role grant.
	 * </p>
	 */
	public void testUngroupedToolNamesNoCapability() {
		String message = loadSkill(readOnlyPrincipal(), UNGROUPED_SKILL);

		assertTrue(message, message.contains("carried by this agent but withheld"));
		assertFalse("Nothing may be named where there is no group: " + message,
			message.contains("`"));
		assertTrue(message, message.contains(AgentCapabilities.NO_GUESSING));
	}

	// ===== The background ceiling took it, not the user's roles =====

	/**
	 * An unattended run is told that it is unattended.
	 *
	 * <p>
	 * The same user in the chat has this skill, so blaming their roles would send them to an
	 * administrator with nothing to grant. The fix is a {@code <background-allowlist>} entry.
	 * </p>
	 */
	public void testBackgroundCeilingIsNotBlamedOnTheUser() {
		String message = loadSkill(unattendedPrincipal(), WITHHELD_SKILL);

		assertTrue(message, message.contains("this run is unattended"));
		assertTrue(message, message.contains("background allowlist"));
		assertTrue("What would change it must be named: " + message,
			message.contains("<background-allowlist>"));
		assertFalse("The acting user's roles are not the reason: " + message,
			message.contains("this user does not hold"));
	}

	/** It still names the capabilities, and still forbids the guess. */
	public void testBackgroundCeilingNamesTheCapabilities() {
		String message = loadSkill(unattendedPrincipal(), WITHHELD_SKILL);

		assertTrue(message, message.contains("`" + ToolGroups.DATA_WRITE + "`"));
		assertTrue(message, message.contains(AgentCapabilities.NO_GUESSING));
	}

	/**
	 * A skill the roles do not grant either keeps the role answer, unattended or not.
	 *
	 * <p>
	 * The ceiling narrows, it never widens: with the roles short of the capability as well, the
	 * allowlist is not what decided and saying so would be the wrong instruction.
	 * </p>
	 */
	public void testCeilingDoesNotHideARoleDenial() {
		AgentCapabilities capabilities = capabilities(Set.of(READ_TOOL),
			Set.of(SCRIPT_TOOL));
		String message = loadSkill(capabilities, UNGROUPED_SKILL);

		assertFalse("Only a tool the roles would have allowed makes it the run's fault: " + message,
			message.contains("unattended"));
	}

	// ===== This agent never declared it =====

	/** A skill of the application that this agent does not carry is not a permission problem. */
	public void testSkillThisAgentDoesNotCarry() {
		String message = loadSkill(readOnlyPrincipal(), FOREIGN_SKILL);

		assertTrue(message, message.contains("exists in this application"));
		assertTrue(message, message.contains("not carried by this agent"));
		assertTrue("Who can change it must be named: " + message, message.contains("administrator"));
		assertTrue(message, message.contains("not a permission problem"));
		assertFalse("Nothing here is about a capability: " + message,
			message.contains("capability"));
	}

	/**
	 * The same reason for a principal who may use everything: it is not about permissions.
	 *
	 * <p>
	 * The list of alternatives that follows it does differ, and must - it is the skills that principal
	 * can load.
	 * </p>
	 */
	public void testSkillThisAgentDoesNotCarryIsPrincipalIndependent() {
		AgentCapabilities unrestricted = capabilities(
			Set.of(READ_TOOL, WRITE_TOOL, SCRIPT_TOOL, UNGROUPED_TOOL), Set.of());

		assertEquals(reason(loadSkill(readOnlyPrincipal(), FOREIGN_SKILL)),
			reason(loadSkill(unrestricted, FOREIGN_SKILL)));
	}

	/** The explanation without the list of alternatives that follows it. */
	private static String reason(String message) {
		return message.substring(0, message.indexOf(" Available: "));
	}

	// ===== No such skill =====

	/**
	 * An id nothing resolves keeps the message it always had, to the byte.
	 *
	 * <p>
	 * A misspelled name never goes through a permission check, so it cannot disclose which capabilities
	 * the user lacks - the property that made the tool-call path safe to explain and that this asserts
	 * verbatim rather than by keyword.
	 * </p>
	 */
	public void testUnknownSkillIdIsUnchanged() {
		assertEquals("Skill 'no-such-skill' is not available for this agent. Available: "
			+ "[" + READABLE_SKILL + "]", loadSkill(readOnlyPrincipal(), "no-such-skill"));
	}

	/** An unknown id says nothing about capabilities, whoever asks. */
	public void testUnknownSkillIdNamesNoCapability() {
		String message = loadSkill(unattendedPrincipal(), "TL_agenticAuthoringg");

		assertFalse(message, message.contains("capability"));
		assertFalse(message, message.contains("unattended"));
	}

	// ===== The skill that is there =====

	/**
	 * A skill the agent can load is unaffected: the same bytes as before the explanation existed.
	 *
	 * <p>
	 * The load result is a tool result the model works from, so a change to it is a change to every
	 * agent's behaviour. Only the refusal was supposed to change.
	 * </p>
	 */
	public void testAvailableSkillLoadsByteIdentically() {
		AgentCapabilities capabilities = readOnlyPrincipal();
		Map<String, Skill> loadable =
			AgentFactory.buildSkillMap(agent().skills(), graph()::skill, capabilities);

		String withExplanation = new SkillTool(loadable, freshCatalog(), registry()::get,
			capabilities.permitted(), null, AgentFactory.absenceReason(agent(), capabilities))
				.loadSkill(READABLE_SKILL);
		String withoutExplanation = new SkillTool(loadable, freshCatalog(), registry()::get,
			capabilities.permitted(), null).loadSkill(READABLE_SKILL);

		assertEquals(withoutExplanation, withExplanation);
		assertTrue(withExplanation, withExplanation.contains("content of " + READABLE_SKILL));
	}

	/** A principal who may use every tool has nothing withheld: no message of it names a capability. */
	public void testPrincipalWhoMayUseEverythingNamesNoCapability() {
		AgentCapabilities capabilities =
			capabilities(Set.of(READ_TOOL, WRITE_TOOL, SCRIPT_TOOL, UNGROUPED_TOOL), Set.of());

		assertTrue(loadSkill(capabilities, WITHHELD_SKILL).contains("content of " + WITHHELD_SKILL));
		assertFalse(loadSkill(capabilities, FOREIGN_SKILL).contains("capability"));
		assertFalse(loadSkill(capabilities, "no-such-skill").contains("capability"));
	}

	// ===== Helpers =====

	/** Holds {@link #READ_TOOL} and nothing else, in an attended run. */
	private static AgentCapabilities readOnlyPrincipal() {
		return capabilities(Set.of(READ_TOOL), Set.of());
	}

	/**
	 * Holds {@link #READ_TOOL} in its roles and would hold the rest, but the run has no one to approve
	 * anything.
	 */
	private static AgentCapabilities unattendedPrincipal() {
		return capabilities(Set.of(READ_TOOL),
			Set.of(WRITE_TOOL, SCRIPT_TOOL, UNGROUPED_TOOL));
	}

	/**
	 * The message {@code loadSkill} hands the model, through the wiring {@link AgentFactory} builds.
	 */
	private static String loadSkill(AgentCapabilities capabilities, String skillId) {
		AgentDefinition agent = agent();
		Map<String, Skill> loadable =
			AgentFactory.buildSkillMap(agent.skills(), graph()::skill, capabilities);
		SkillTool skillTool = new SkillTool(loadable, freshCatalog(), registry()::get,
			capabilities.permitted(), null, AgentFactory.absenceReason(agent, capabilities));

		return skillTool.loadSkill(skillId);
	}

	private static DynamicToolCatalog freshCatalog() {
		return new DynamicToolCatalog(new ToolCatalog().freeze());
	}

	/**
	 * The capability view of one run.
	 *
	 * @param permittedTools
	 *        The tools the run's principal may use.
	 * @param ceilingTools
	 *        The tools an attended run with the same principal would have been allowed, and this one is
	 *        not - the background allowlist is what removed them.
	 */
	private static AgentCapabilities capabilities(Set<String> permittedTools,
			Set<String> ceilingTools) {
		SecurityContext security = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return permittedTools.contains(tool.name());
			}

			@Override
			public boolean withheldByBackgroundCeiling(Tool tool) {
				return ceilingTools.contains(tool.name());
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
		return new AgentCapabilities(registry()::get, security, graph());
	}

	/** Declares three of the four skills; {@link #FOREIGN_SKILL} is deliberately not one of them. */
	private static AgentDefinition agent() {
		return new AgentDefinition(AGENT, AGENT, null, "", List.of(READ_TOOL), List.of(),
			List.of(READABLE_SKILL, WITHHELD_SKILL, UNGROUPED_SKILL), List.of(), AgentSource.CONFIG);
	}

	/** Every skill the application has, whether this agent declares it or not. */
	private static AgentGraph graph() {
		Map<String, SkillDefinition> skills = new LinkedHashMap<>();
		skills.put(READABLE_SKILL, skill(READABLE_SKILL, READ_TOOL));
		skills.put(WITHHELD_SKILL, skill(WITHHELD_SKILL, WRITE_TOOL, SCRIPT_TOOL));
		skills.put(UNGROUPED_SKILL, skill(UNGROUPED_SKILL, UNGROUPED_TOOL));
		skills.put(FOREIGN_SKILL, skill(FOREIGN_SKILL, WRITE_TOOL));

		return new AgentGraph() {
			@Override
			public AgentDefinition agent(String name) {
				return AGENT.equals(name) ? TestSkillAbsenceReason.agent() : null;
			}

			@Override
			public SkillDefinition skill(String name) {
				return skills.get(name);
			}

			@Override
			public List<Tool> mcpTools(String serverName) {
				return null;
			}
		};
	}

	private static SkillDefinition skill(String name, String... tools) {
		return new SkillDefinition(name, name, "content of " + name, List.of(tools),
			AgentSource.CONFIG);
	}

	private static Map<String, Tool> registry() {
		Map<String, Tool> registry = new HashMap<>();
		registry.put(READ_TOOL, tool(READ_TOOL, ToolGroups.DATA_READ));
		registry.put(WRITE_TOOL, tool(WRITE_TOOL, ToolGroups.DATA_WRITE));
		registry.put(SCRIPT_TOOL, tool(SCRIPT_TOOL, ToolGroups.SCRIPT));
		registry.put(UNGROUPED_TOOL, tool(UNGROUPED_TOOL, ""));
		return registry;
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
