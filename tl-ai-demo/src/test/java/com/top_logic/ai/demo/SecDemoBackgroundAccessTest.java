/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;

import junit.framework.Test;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * The ceiling on a run that has nobody to answer an approval prompt, and the two other things that
 * follow from a run having no attending human: the boot rule that makes the ceiling mandatory, and
 * the MCP endpoint's refusal.
 *
 * <p>
 * An {@code aiAgentChatAsync} run, a scheduler job, an MCP call and a detached (fire-and-forget)
 * subagent all auto-approve every tool call, because there is no human on the other end. Approval is
 * the compensating control that makes a write-classified tool safe, so a run that has lost it must not
 * reach one without an explicit administrative decision - and {@code <background-allowlist>} is that
 * decision.
 * </p>
 *
 * <p>
 * The fixture is set up so this is observable rather than vacuous: {@code Anna} holds
 * {@code data.write} and {@code data.write} is deliberately <em>off</em> the demo's allowlist. The same
 * account, the same tool, two run kinds, two answers.
 * </p>
 */
public class SecDemoBackgroundAccessTest extends SecDemoPersonaTest {

	/**
	 * The whole mechanism in one pair: a capability the role holds is out of reach unattended.
	 *
	 * <p>
	 * Nothing about {@code Anna} changes between the two calls. Only the absence of a human to
	 * approve does.
	 * </p>
	 */
	public void testWriteToolIsReachableInTheForegroundAndNotInTheBackground() {
		assertAllowed(ANNA, CLASSIFIED_WRITE_SCRIPT_TOOL);

		String reason = securityFor(ANNA, true).toolDenialReason(tool(CLASSIFIED_WRITE_SCRIPT_TOOL));

		assertNotNull("An unattended run must not reach a data.write tool under this policy.", reason);
		assertTrue("The denial must blame the allowlist, not the role - the role does hold the"
			+ " capability: " + reason, reason.contains("background allowlist"));
		assertFalse("Blaming the role would send an operator to the wrong configuration: " + reason,
			reason.contains("none of your roles grants"));
	}

	/**
	 * The allowlist is asked first, so when both would refuse it is the allowlist that is named.
	 *
	 * <p>
	 * Worth pinning because the message decides where an operator goes looking. A capability that
	 * neither the role holds nor the allowlist admits is refused by the ceiling, and the ceiling says
	 * so - which is the right answer for an unattended run, since granting the role more would not help.
	 * </p>
	 *
	 * <p>
	 * The complementary row - an <em>allowlisted</em> capability the role does not hold, where the
	 * denial must name the role rather than the allowlist - needs a persona short of an allowlisted
	 * capability, and this fixture has none: every persona holds all three. {@code TestToolAuthorization}
	 * in {@code tl-ai-core} is the deterministic form, against a hand-built policy.
	 * </p>
	 */
	public void testTheAllowlistIsAskedFirst() {
		String reason = securityFor(BEN, true).toolDenialReason(tool(WORKSPACE_TOOL));

		assertNotNull("workspace is granted to no role and is off the allowlist.", reason);
		assertTrue("The ceiling is what an unattended run meets first: " + reason,
			reason.contains("background allowlist"));

		assertTrue("...and in the foreground the same tool is refused by the role instead.",
			securityFor(BEN).toolDenialReason(tool(WORKSPACE_TOOL)).contains(ToolGroups.WORKSPACE));
	}

	/** What the allowlist does admit still works unattended, so the ceiling is not a blanket refusal. */
	public void testAllowlistedCapabilitiesSurviveABackgroundRun() {
		for (String toolName : List.of(MODEL_READ_TOOL, LIST_TOOL, SCRIPT_TOOL)) {
			assertNull("Ben must reach '" + toolName + "' in a background run: the harness of"
				+ " doc/security/testbed.md depends on it.",
				securityFor(BEN, true).toolDenialReason(tool(toolName)));
		}
	}

	/**
	 * The ceiling applies to administrators too, because the missing approval prompt is its
	 * justification rather than the principal's authority.
	 *
	 * <p>
	 * An unattended administrator run has nobody to ask either. An administrator who needs a write
	 * tool in a scheduled run says so by putting its group on the allowlist.
	 * </p>
	 */
	public void testAllowlistBoundsAdministratorsToo() {
		Person administrator = anyAdministrator();
		assertNotNull("The application must have at least one administrator account.", administrator);

		TopLogicSecurityContext background = TopLogicSecurityContext.forRun(administrator, true);

		assertFalse("An unattended administrator run is bounded by the allowlist.",
			background.hasToolPermission(tool(CLASSIFIED_WRITE_SCRIPT_TOOL)));
		assertTrue("The foreground administrator run is not.",
			TopLogicSecurityContext.forRun(administrator, false)
				.hasToolPermission(tool(CLASSIFIED_WRITE_SCRIPT_TOOL)));
	}

	/**
	 * An unattended run cannot commit through TL-Script, not even as an administrator.
	 *
	 * <p>
	 * The bypass this closes, end to end: {@code script} is on the demo's allowlist because the harness
	 * needs it, {@code data.write} is deliberately off it, and {@code executeTLScript} used to be gated
	 * by {@code script} alone. An unattended run - which auto-approves every call because no human is
	 * there to ask - could therefore read a write tool's implementation, re-run it through
	 * {@code executeTLScript} with {@code withCommit=true} and persist it, in a policy whose whole
	 * intent is "no {@code data.write} unattended". Administrators are the sharpest case: the allowlist
	 * binds them too, so not even the account that bypasses every role grant gets the commit.
	 * </p>
	 */
	public void testBackgroundRunCannotCommitThroughTLScript() {
		Person administrator = anyAdministrator();
		assertNotNull("The application must have at least one administrator account.", administrator);
		assertNull("Precondition: 'script' is on the allowlist, so the tool itself is reachable.",
			securityFor(BEN, true).toolDenialReason(tool(SCRIPT_TOOL)));

		List<TLObject> seeded = tickets();
		try {
			String refused =
				executeScript(TopLogicSecurityContext.forRun(administrator, true), CREATE_TICKET, true);

			assertTrue("The refusal must name the capability that is missing from the allowlist: "
				+ refused, refused.contains(ToolGroups.DATA_WRITE));
			assertTrue("...and blame the allowlist rather than the roles: " + refused,
				refused.contains("background allowlist"));
			assertEquals("Nothing may have been written.", seeded.size(), tickets().size());
		} finally {
			deleteTicketsBeyond(seeded);
		}
	}

	/**
	 * A read-only script is unaffected: the harness of {@code doc/security/testbed.md} runs unattended.
	 */
	public void testBackgroundRunStillReadsThroughTLScript() {
		String result = executeScript(securityFor(BEN, true), "all(`pm:Ticket`).size()", false);

		assertFalse("'script' is allowlisted, so a read-only script must go through: " + result,
			result.contains("Permission denied"));
		assertTrue("The script must actually have run: " + result,
			result.contains("\"success\":true") || result.contains("\"success\": true"));
	}

	/**
	 * A fire-and-forget subagent is an unattended run, so the ceiling reaches it too - issue #266.
	 *
	 * <p>
	 * {@code spawn_subagent} returns a handle immediately and the parent turn ends while the child keeps
	 * working on a pool thread, so from that moment there is no human to answer an approval prompt.
	 * Before this was classified, the detached child ran with {@code Anna}'s full interactive
	 * grant set - and in an {@code AUTO_APPROVE} session it self-approved every write tool it reached.
	 * The parent context here carries exactly that callback, which is what makes the refusal below a
	 * statement about the authorization gate rather than about approval.
	 * </p>
	 *
	 * @see com.top_logic.tl.ai.core.agent.ToolContext#forDetachedSubagent
	 */
	public void testDetachedSubagentIsRefusedAWriteToolItsParentTurnMayUse() {
		ToolContext parentTurn = new ToolContext(securityFor(ANNA), ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP);
		Tool write = tool(CLASSIFIED_WRITE_SCRIPT_TOOL);

		assertTrue("Precondition: Anna reaches the write tool in the foreground chat.",
			parentTurn.security().hasToolPermission(write));

		ToolContext detached =
			parentTurn.forDetachedSubagent(TransactionScope.NOOP, new CancellationToken());

		assertTrue("A detached subagent has nobody to approve its calls.",
			detached.security().isBackgroundRun());
		assertFalse("...so 'data.write' - which this policy keeps off the allowlist - is out of reach.",
			detached.security().hasToolPermission(write));

		String reason = detached.security().toolDenialReason(write);
		assertTrue("The denial must blame the allowlist, not the role - Anna does hold"
			+ " data.write: " + reason, reason.contains("background allowlist"));
		assertFalse("Blaming the role would send an operator to the wrong configuration: " + reason,
			reason.contains("none of your roles grants"));
		assertTrue("The loss must be nameable, so it can be logged and put in the child's prompt: "
			+ detached.security().capabilitiesWithheldInBackground(),
			detached.security().capabilitiesWithheldInBackground().contains(ToolGroups.DATA_WRITE));
	}

	/**
	 * A synchronous delegation is untouched: it completes inside the parent turn, where the human who
	 * could answer an approval prompt still is.
	 */
	public void testSynchronousDelegationKeepsTheParentTurnsReach() {
		ToolContext parentTurn = new ToolContext(securityFor(ANNA), ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP);

		ToolContext delegate = parentTurn.forDelegation(TransactionScope.NOOP);

		assertFalse(delegate.security().isBackgroundRun());
		assertTrue("A synchronous delegate of Anna keeps data.write.",
			delegate.security().hasToolPermission(tool(CLASSIFIED_WRITE_SCRIPT_TOOL)));
	}

	/** What the allowlist admits still works in a detached subagent, so the ceiling is not a wall. */
	public void testDetachedSubagentKeepsTheAllowlistedCapabilities() {
		ToolContext parentTurn = new ToolContext(securityFor(BEN), ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP);

		ToolContext detached =
			parentTurn.forDetachedSubagent(TransactionScope.NOOP, new CancellationToken());

		for (String toolName : List.of(MODEL_READ_TOOL, LIST_TOOL, SCRIPT_TOOL)) {
			assertNull("Ben's detached subagent must reach '" + toolName + "'.",
				detached.security().toolDenialReason(tool(toolName)));
		}
	}

	/**
	 * A policy that grants roles anything and declares no allowlist is under-specified, and the deployed
	 * policy is not.
	 *
	 * <p>
	 * {@code AiToolAccessService} rejects such a policy at boot with {@code context.error(...)}: the
	 * allowlist is the only control standing in for the approval prompt, so its absent state must not
	 * be the permissive one. That an application <em>can</em> be rejected is pinned by
	 * {@code TestAiToolAccessService.testPolicyWithRolesMustDeclareBackgroundAllowlist}; what this
	 * asserts is the other half - that the shipped fixture, which does grant roles, satisfies the rule.
	 * An application whose boot fails on this shows the message quoted in
	 * doc/security/tool-access-configuration.md.
	 * </p>
	 */
	public void testTheDeployedPolicyDeclaresAnAllowlist() {
		assertFalse("The precondition: the fixture grants roles something.",
			AiToolAccessService.policy().getToolAccess().isEmpty());
		assertNotNull("A granting policy with no <background-allowlist> is a boot error, so a booted"
			+ " application with a grant table always has one.",
			AiToolAccessService.policy().getBackgroundAllowlist());
		assertFalse("The demo allowlist is neither the opt-out nor empty.",
			AiToolAccessService.policy().getBackgroundAllowlist().isAll());
		assertFalse(AiToolAccessService.policy().getBackgroundAllowlist().getGrants().isEmpty());
	}

	/**
	 * The MCP endpoint cannot execute a tool in this deployment, because it names no user.
	 *
	 * <p>
	 * {@code /mcp/*} authenticates nobody, so an MCP tool call has no principal to authorize and
	 * {@code McpToolAdapter} refuses it outright. What this pins in a booted application is the reason
	 * it has to: a run with no principal cannot even build a security context, whatever the policy
	 * says. The refusal <em>message</em> is pinned by {@code TestMcpToolContexts} in
	 * tl-ai-mcp-server.
	 * </p>
	 */
	public void testARunWithoutAPrincipalCannotBeBuilt() {
		try {
			// The shape an MCP tool call would have: no principal, unattended.
			TopLogicSecurityContext.forRun((Person) null, true);
			fail("A run with no principal must be refused up front, not at the first tool call.");
		} catch (IllegalStateException expected) {
			assertTrue("The refusal must say what is missing: " + expected.getMessage(),
				expected.getMessage().contains("authenticated user"));
		}
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoBackgroundAccessTest.class);
	}

}
