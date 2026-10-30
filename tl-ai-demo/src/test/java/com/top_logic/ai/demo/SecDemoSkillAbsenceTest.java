/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.SkillDefinition;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * What a persona is told when {@code loadSkill} cannot hand over the skill it asked for.
 *
 * <p>
 * The session this pins: as {@code Anna}, asked to use {@code secdemo-editing.addTicket}, the
 * agent followed its own prompt - "when the user names a specific tool, load
 * {@code TL_agenticAuthoring} and confirm before saying it does not exist" - and got
 * <em>"Skill 'TL_agenticAuthoring' is not available for this agent"</em>. It then told the user that
 * the root cause was "the missing {@code script} capability for this user" and that an administrator
 * should grant it. Every part of that was wrong: the skill was dropped because its tools need
 * {@code agent.meta} / {@code agent.authoring}, {@code script} was the reason for nothing here, and
 * the tool the user actually asked about is carried by no agent at all.
 * </p>
 *
 * <p>
 * The decision was correct and is not what changed - see {@link SecDemoCapabilityNoticeTest} for which
 * skills a persona keeps. What is asserted here is the <em>explanation</em>: one message per way a
 * skill can be absent, each attributable to the thing that actually caused it.
 * </p>
 *
 * <p>
 * Everything runs against the catalog {@link AgentFactory#buildToolCatalog} builds for the shipped
 * {@code TL_SingleAgent} and the acting persona, and every message is read out of a real tool call
 * through it. So both routes the model can take - naming the tool, or hunting for a skill - are
 * answered by the same object the chat answers them with.
 * </p>
 *
 */
public class SecDemoSkillAbsenceTest extends SecDemoPersonaTest {

	/** The chat's default entry point, and the agent of the reported session. */
	private static final String ENTRY_AGENT = "TL_SingleAgent";

	/** Every tool of it needs {@code agent.meta} or {@code agent.authoring}. */
	private static final String AUTHORING_SKILL = "TL_agenticAuthoring";

	/** Four tools, all {@code script}. */
	private static final String SCRIPT_SKILL = "TL_writingDataChanges";

	/** A framework skill {@code TL_SingleAgent} does not declare - {@code TL_Orchestrator} does. */
	private static final String FOREIGN_SKILL = "TL_promptModelEditor";

	/**
	 * The sentence that closes every message naming a capability, or deliberately naming none.
	 *
	 * <p>
	 * Spelled out here rather than read from {@code AgentCapabilities.NO_GUESSING}, so that changing
	 * the constant fails this test: the wording is the fix, not an implementation detail. It has to
	 * name the message's own <em>subject</em> - the first version said only "do not attribute this to a
	 * capability unless this message names one", and the withheld-skill message does name capabilities,
	 * correctly, for the skill it refused. That was enough for the model to relay them as the reason a
	 * different tool was unreachable. See the developer article {@code ai/security/test-bed}, rows C3/C4.
	 * </p>
	 */
	private static final String NO_GUESSING =
		"These capabilities explain only this skill. Do not present them as the reason any other tool"
			+ " or skill is unreachable, and do not name a capability this message does not name.";

	/** Every capability the fixture's policy knows, for the "names nothing else" assertions. */
	private static final List<String> FIXTURE_CAPABILITIES = List.of(ToolGroups.MODEL_READ,
		ToolGroups.DATA_READ, ToolGroups.DATA_WRITE, ToolGroups.SCRIPT, ToolGroups.MODEL_STRUCTURE,
		ToolGroups.AGENT_META, ToolGroups.AGENT_AUTHORING, ToolGroups.WORKSPACE, ToolGroups.EGRESS);

	/** Precondition: the agent and the skills this test reasons about have the assumed shape. */
	public void testFixtureHasTheAssumedShape() {
		AgentDefinition agent = agent();

		assertTrue(ENTRY_AGENT + " must declare the skills of the reported session: " + agent.skills(),
			agent.skills().containsAll(List.of(AUTHORING_SKILL, SCRIPT_SKILL)));
		assertFalse(ENTRY_AGENT + " must not declare " + FOREIGN_SKILL + ", or the not-carried case"
			+ " has no example: " + agent.skills(), agent.skills().contains(FOREIGN_SKILL));
		assertNotNull("The not-carried case needs a skill the application really has.",
			AgentDefinitionQuery.getInstance().findSkillByName(FOREIGN_SKILL));

		assertEquals("Anna must lack exactly the capabilities of the authoring skill.",
			Set.of(ToolGroups.AGENT_META, ToolGroups.AGENT_AUTHORING),
			capabilitiesOf(ANNA).lacking(skill(AUTHORING_SKILL).tools()));
		assertAllowed(ANNA, CLASSIFIED_WRITE_SCRIPT_TOOL);
	}

	/**
	 * The refusal of the reported session names the capabilities of the skill it refused, and no
	 * others.
	 */
	public void testAuthoringSkillRefusalNamesItsOwnCapabilities() {
		String message = loadSkillAs(ANNA, AUTHORING_SKILL);

		assertTrue(message, message.contains("carried by this agent but withheld"));
		assertTrue("The capability an administrator has to grant must be named: " + message,
			message.contains("`" + ToolGroups.AGENT_META + "`"));
		assertTrue(message, message.contains("`" + ToolGroups.AGENT_AUTHORING + "`"));
		assertFalse("The wrong answer of the reported session, now forbidden by the message"
			+ " itself: " + message, message.contains("`" + ToolGroups.SCRIPT + "`"));
		assertFalse("Anna holds data.write, so naming it would be provably false: " + message,
			message.contains("`" + ToolGroups.DATA_WRITE + "`"));
		assertTrue(message, message.contains(NO_GUESSING));
	}

	/**
	 * ...and says which subject those capabilities belong to.
	 *
	 * <p>
	 * The sentence C3/C4 were missing. The model was told "do not attribute this to a capability unless
	 * this message names one", relayed a message that <em>did</em> name two, and the user read them as
	 * the reason the tool they had asked about was unreachable. Naming the subject is what makes the
	 * relay honest.
	 * </p>
	 */
	public void testTheRefusalScopesItsCapabilitiesToTheSkill() {
		String message = loadSkillAs(ANNA, AUTHORING_SKILL);

		assertTrue("The names must be scoped to the skill they explain: " + message,
			message.contains("explain only this skill"));
		assertTrue("...and must be forbidden as the reason for anything else: " + message,
			message.contains("any other tool or skill"));
		assertTrue("The wording is the fix and must not drift: " + message,
			message.contains(NO_GUESSING));
	}

	/** An attended chat run must not be explained as an unattended one. */
	public void testForegroundRefusalDoesNotBlameTheRun() {
		assertFalse(loadSkillAs(ANNA, AUTHORING_SKILL).contains("unattended"));
	}

	/**
	 * One persona over, the same two skills swap places - which is what makes the message a statement
	 * about the policy rather than about the skill.
	 */
	public void testTheScriptPersonaLoadsTheScriptSkillAndNotTheAuthoringOne() {
		String script = loadSkillAs(BEN, SCRIPT_SKILL);
		assertTrue("Ben holds script, so the skill must load: " + script,
			script.contains("Skill \"" + SCRIPT_SKILL + "\" loaded."));
		assertTrue("...and hand over its tool: " + script,
			script.contains("- " + tool(SCRIPT_TOOL).name() + ":"));

		String authoring = loadSkillAs(BEN, AUTHORING_SKILL);
		assertTrue("Ben holds no agent.meta: " + authoring,
			authoring.contains("carried by this agent but withheld"));
		assertTrue(authoring, authoring.contains("`" + ToolGroups.AGENT_META + "`"));
		assertFalse("A capability it holds must not be named: " + authoring,
			authoring.contains("`" + ToolGroups.SCRIPT + "`"));
	}

	/**
	 * A skill withheld for a reason the run cannot change still names the capability and forbids the
	 * guess, in a background run as in the chat.
	 *
	 * <p>
	 * The sharper form - a skill the acting user's roles <em>do</em> reach and the
	 * {@code <background-allowlist>} withholds, where blaming the roles would send the user to an
	 * administrator who has already granted everything there is to grant - needs a skill whose
	 * capability is granted to a role and off the allowlist. This fixture's one such capability is
	 * {@code data.write}, and no shipped skill carries a {@code data.write} tool, so the row is not
	 * expressible here. {@code TestSkillAbsenceReason} in {@code tl-ai-core} is the deterministic form,
	 * and {@code SecDemoBackgroundAccessTest} pins the same distinction at the tool level, where the
	 * fixture <em>can</em> show it.
	 * </p>
	 */
	public void testUnattendedRefusalStillNamesTheCapability() {
		String message = loadSkillInBackground(ANNA, "TL_taskManagement");

		assertTrue("The capability nobody holds must be named: " + message,
			message.contains("`" + ToolGroups.WORKSPACE + "`"));
		assertTrue(message, message.contains(NO_GUESSING));
		assertTrue("An administrator is who could change it: " + message,
			message.contains("administrator"));
	}

	/** A skill of the application that this agent does not declare is not a permission problem. */
	public void testSkillThisAgentDoesNotCarryIsNotAPermissionProblem() {
		String message = loadSkillAs(ANNA, FOREIGN_SKILL);

		assertTrue(message, message.contains("exists in this application"));
		assertTrue(message, message.contains("not carried by this agent"));
		assertTrue("Who could change it must be named: " + message, message.contains("administrator"));
		assertNamesNoCapability(message);
	}

	/** A misspelled skill id discloses nothing about capabilities. */
	public void testUnknownSkillIdDisclosesNothing() {
		String message = loadSkillAs(ANNA, AUTHORING_SKILL + "g");

		assertTrue(message, message.contains("is not available for this agent"));
		assertNamesNoCapability(message);
		assertFalse(message, message.contains("capability"));
	}

	/**
	 * The question that started all of this, answered honestly whichever route the model takes.
	 *
	 * <p>
	 * {@code secdemo-editing.addTicket} is in no agent's {@code <tools>} and in no skill, so the chat
	 * cannot reach it however the policy is configured, and {@code Anna} is granted it - a
	 * capability answer here is provably false. Two routes lead there: naming the tool, and hunting for
	 * a skill that might carry it. Both are asserted, because the model picks one and the user reads
	 * whichever it picked.
	 * </p>
	 */
	public void testAddNoteIsAnsweredHonestlyOnBothRoutes() {
		String named = callAs(ANNA, CLASSIFIED_WRITE_SCRIPT_TOOL);
		assertTrue("The tool exists, so this is not an unknown name: " + named,
			named.contains("exists in this application"));
		assertTrue("...and it is the agent that does not carry it: " + named,
			named.contains("not carried by this agent"));
		assertFalse("Anna is granted this tool: " + named, named.contains("Permission denied"));
		assertNamesNoCapability(named);

		for (String skillName : agent().skills()) {
			String message = loadSkillAs(ANNA, skillName);
			if (message.contains("loaded.")) {
				continue;
			}
			assertTrue("A withheld skill must say so and forbid the guess: " + skillName + "\n"
				+ message, message.contains(NO_GUESSING));
			assertNamesOnly(message, capabilitiesOf(ANNA).lacking(skill(skillName).tools()));
		}
	}

	// ===== Helpers =====

	/**
	 * Asserts that the message names no capability at all.
	 *
	 * <p>
	 * A capability is always named in backticks, which is what the agent quotes to the user. The bare
	 * word can appear inside a capability's own description without being a claim about this refusal.
	 * </p>
	 */
	private static void assertNamesNoCapability(String message) {
		assertNamesOnly(message, Set.of());
	}

	/** Asserts that the message names these capabilities and no others. */
	private static void assertNamesOnly(String message, Set<String> expected) {
		List<String> unexpected = new ArrayList<>();
		for (String capability : FIXTURE_CAPABILITIES) {
			if (message.contains("`" + capability + "`") && !expected.contains(capability)) {
				unexpected.add(capability);
			}
		}
		assertTrue("Named as the reason without being one: " + unexpected + "\n" + message,
			unexpected.isEmpty());
		for (String capability : expected) {
			assertTrue("The capability that decided must be named: " + capability + "\n" + message,
				message.contains("`" + capability + "`"));
		}
	}

	/** The message {@code loadSkill} hands the model, out of a real call through the agent's catalog. */
	private String loadSkillAs(String login, String skillId) {
		return loadSkill(securityFor(login), skillId);
	}

	/** {@link #loadSkillAs(String, String)} for a run with no human to answer an approval prompt. */
	private String loadSkillInBackground(String login, String skillId) {
		return loadSkill(securityFor(login, true), skillId);
	}

	private String loadSkill(SecurityContext security, String skillId) {
		return call(security, "loadSkill", "{\"skillId\": \"" + skillId + "\"}");
	}

	/** The message a call for the named tool produces - a refusal, since none of them can run. */
	private String callAs(String login, String toolName) {
		return call(securityFor(login), toolName, "{}");
	}

	/**
	 * Executes one tool call against the catalog the given run would have, and returns the text the
	 * model receives.
	 */
	private static String call(SecurityContext security, String toolName, String arguments) {
		ToolRegistry catalog = catalogFor(security);
		ToolContext context =
			new ToolContext(security, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);

		ToolResultMessage result = catalog.execute(new ToolCall("call-1", toolName, arguments), context);
		if (result.state() instanceof ToolState.Completed completed) {
			return completed.result();
		}
		return ((ToolState.Failed) result.state()).error();
	}

	/**
	 * The tool catalog of a run of {@link #ENTRY_AGENT} under the given security context, built exactly
	 * as the chat builds it - including the {@code loadSkill} tool and the skills it can hand over.
	 */
	private static ToolRegistry catalogFor(SecurityContext security) {
		return AgentFactory.getInstance()
			.buildToolCatalog(agent(), AgentCapabilities.forRun(security));
	}

	private AgentCapabilities capabilitiesOf(String login) {
		return AgentCapabilities.forRun(securityFor(login));
	}

	private static AgentDefinition agent() {
		AgentDefinition agent = AgentDefinitionQuery.getInstance().findAgentByName(ENTRY_AGENT);
		assertNotNull("The framework agent '" + ENTRY_AGENT + "' must exist; this test bed assumes"
			+ " the agents shipped in 'tl-ai-core.conf.config.xml'.", agent);
		return agent;
	}

	private static SkillDefinition skill(String name) {
		SkillDefinition definition = AgentDefinitionQuery.getInstance().findSkillByName(name);
		assertNotNull("The framework skill '" + name + "' must exist.", definition);
		return definition;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoSkillAbsenceTest.class, TopLogicAgentDefinitions.Module.INSTANCE);
	}

}
