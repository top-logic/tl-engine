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
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * The ceiling on a run that has nobody to answer an approval prompt, and the two other things that
 * follow from a run having no attending human: the boot rule that makes the ceiling mandatory, and
 * the MCP endpoint's refusal.
 *
 * <p>
 * An {@code aiAgentChatAsync} run, a scheduler job and an MCP call all auto-approve every tool call,
 * because there is no human on the other end. Approval is the compensating control that makes a
 * write-classified tool safe, so a run that has lost it must not reach one without an explicit
 * administrative decision - and {@code <background-allowlist>} is that decision.
 * </p>
 *
 * <p>
 * The fixture is set up so this is observable rather than vacuous: {@code secwriter1} holds
 * {@code data.write} and {@code data.write} is deliberately <em>off</em> the demo's allowlist. The same
 * account, the same tool, two run kinds, two answers.
 * </p>
 */
public class SecDemoBackgroundAccessTest extends SecDemoPersonaTest {

	/**
	 * The whole mechanism in one pair: a capability the role holds is out of reach unattended.
	 *
	 * <p>
	 * Nothing about {@code secwriter1} changes between the two calls. Only the absence of a human to
	 * approve does.
	 * </p>
	 */
	public void testWriteToolIsReachableInTheForegroundAndNotInTheBackground() {
		assertAllowed(WRITER, CLASSIFIED_WRITE_SCRIPT_TOOL);

		String reason = securityFor(WRITER, true).toolDenialReason(tool(CLASSIFIED_WRITE_SCRIPT_TOOL));

		assertNotNull("An unattended run must not reach a data.write tool under this policy.", reason);
		assertTrue("The denial must blame the allowlist, not the role - the role does hold the"
			+ " capability: " + reason, reason.contains("background allowlist"));
		assertFalse("Blaming the role would send an operator to the wrong configuration: " + reason,
			reason.contains("none of your roles grants"));
	}

	/** The same for {@code workspace}, the fixture's other capability left off the allowlist. */
	public void testWorkspaceToolIsAlsoOffTheAllowlist() {
		assertAllowed(WRITER, WORKSPACE_TOOL);

		String reason = securityFor(WRITER, true).toolDenialReason(tool(WORKSPACE_TOOL));

		assertNotNull(reason);
		assertTrue(reason, reason.contains("background allowlist"));
	}

	/**
	 * The allowlist narrows and never widens: an allowlisted capability the role does not hold stays
	 * denied, and the denial names the role.
	 */
	public void testAllowlistIsAnIntersectionNotAGrant() {
		String reason = securityFor(VIEWER, true).toolDenialReason(tool(DATA_READ_TOOL));

		assertNotNull("data.read is allowlisted, but secviewer1 does not hold it.", reason);
		assertTrue("A run denied by the role must say so rather than blaming the allowlist: " + reason,
			reason.contains(ToolGroups.DATA_READ));
		assertFalse(reason, reason.contains("background allowlist"));
	}

	/** What the allowlist does admit still works unattended, so the ceiling is not a blanket refusal. */
	public void testAllowlistedCapabilitiesSurviveABackgroundRun() {
		for (String toolName : List.of(MODEL_READ_TOOL, DATA_READ_TOOL, SCRIPT_TOOL)) {
			assertNull("secreader1 must reach '" + toolName + "' in a background run: the harness of"
				+ " doc/security/testbed.md depends on it.",
				securityFor(READER, true).toolDenialReason(tool(toolName)));
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
			securityFor(READER, true).toolDenialReason(tool(SCRIPT_TOOL)));

		List<TLObject> seeded = notes();
		try {
			String refused =
				executeScript(TopLogicSecurityContext.forRun(administrator, true), CREATE_NOTE, true);

			assertTrue("The refusal must name the capability that is missing from the allowlist: "
				+ refused, refused.contains(ToolGroups.DATA_WRITE));
			assertTrue("...and blame the allowlist rather than the roles: " + refused,
				refused.contains("background allowlist"));
			assertEquals("Nothing may have been written.", seeded.size(), notes().size());
		} finally {
			deleteNotesBeyond(seeded);
		}
	}

	/**
	 * A read-only script is unaffected: the harness of {@code doc/security/testbed.md} runs unattended.
	 */
	public void testBackgroundRunStillReadsThroughTLScript() {
		String result = executeScript(securityFor(READER, true), "all(`secdemo:Note`).size()", false);

		assertFalse("'script' is allowlisted, so a read-only script must go through: " + result,
			result.contains("Permission denied"));
		assertTrue("The script must actually have run: " + result,
			result.contains("\"success\":true") || result.contains("\"success\": true"));
	}

	/**
	 * An enforcing policy that declares no allowlist at all is under-specified, and the deployed policy
	 * is not.
	 *
	 * <p>
	 * {@code AiToolAccessService} rejects such a policy at boot with {@code context.error(...)}: the
	 * allowlist is the only control standing in for the approval prompt, so its absent state must not
	 * be the permissive one. That an application <em>can</em> be rejected is pinned by
	 * {@code TestAiToolAccessService.testEnforcingPolicyMustDeclareBackgroundAllowlist}; what this
	 * asserts is the other half - that the shipped fixture, which does enforce, satisfies the rule. An
	 * application whose boot fails on this shows the message quoted in
	 * doc/security/tool-access-configuration.md.
	 * </p>
	 */
	public void testTheDeployedEnforcingPolicyDeclaresAnAllowlist() {
		assertEquals(AiSecurityMode.ENFORCED, AiToolAccessService.ceiling());
		assertNotNull("An enforcing policy with no <background-allowlist> is a boot error, so a booted"
			+ " enforcing application always has one.",
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
	 * {@code McpToolAdapter} refuses it rather than running it as the system. The refusal is decided
	 * from the deployment's mode, and this is where the demo's own mode is checked against that rule:
	 * {@code ENFORCED.requiresUser()} is what closes the endpoint, and a principal-less run cannot even
	 * build a security context. The refusal <em>message</em> is pinned by
	 * {@code TestMcpToolContexts} in tl-ai-mcp-server.
	 * </p>
	 */
	public void testMcpRunHasNoPrincipalAndIsRefusedUnderThisPolicy() {
		assertTrue("An enforcing deployment requires a user, which is what closes /mcp/*.",
			AiToolAccessService.ceiling().requiresUser());

		try {
			// Exactly what McpToolContexts.forCall builds: no principal, unattended.
			TopLogicSecurityContext.forRun((Person) null, true);
			fail("A run with no principal must be refused up front under an enforcing policy,"
				+ " not at the first tool call.");
		} catch (IllegalStateException expected) {
			assertTrue("The refusal must name the mode: " + expected.getMessage(),
				expected.getMessage().contains(AiSecurityMode.ENFORCED.name()));
		}
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoBackgroundAccessTest.class);
	}

}
