/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;

import junit.framework.Test;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.ToolPermissionFilter;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * What the model is <em>told</em> about: the per-turn schema filter, and the message a name the
 * catalog cannot resolve produces.
 *
 * <p>
 * The filter is user experience rather than enforcement - the gate that stops a call is
 * {@link SecDemoToolAccessTest} - but it is the reason a well-behaved model does not propose a tool
 * it cannot use and does not narrate a denial. {@code AgentLoop.callProviderOnce} builds the provider
 * request as {@code registry.toolSchemas(new ToolPermissionFilter(security))}, which is exactly the
 * call below, so an absent schema here is an absent schema in the request.
 * </p>
 *
 * <p>
 * The two questions are deliberately in one class, because they are two halves of the same promise:
 * the model is shown exactly the tools it may call, and a name outside that set is answered with
 * "there is no such tool, here is what there is" rather than with a permission message. Following the
 * hint can therefore never walk into a denial, and a typo cannot disclose the name of a capability
 * the user does not hold.
 * </p>
 */
public class SecDemoSchemaFilterTest extends SecDemoPersonaTest {

	/**
	 * A denied tool's schema is absent from the request, and a granted one's is present.
	 *
	 * <p>
	 * Both halves matter: a filter that removed everything would pass the first assertion alone.
	 * </p>
	 */
	public void testDeniedToolSchemaIsAbsentFromTheRequest() {
		List<String> viewerSees = schemaNamesFor(VIEWER);

		assertTrue("A tool the persona may use must be offered: " + viewerSees,
			viewerSees.contains(tool(MODEL_READ_TOOL).name()));
		assertFalse("A denied tool must not be offered to the model: " + viewerSees,
			viewerSees.contains(tool(DATA_READ_TOOL).name()));
	}

	/**
	 * The filter follows the policy per persona, on the same catalog.
	 *
	 * <p>
	 * The comparison is what makes it a statement about the policy rather than about the catalog: one
	 * catalog, two personas, and {@code agent.meta} is the only capability between them.
	 * </p>
	 */
	public void testTheSameCatalogIsFilteredDifferentlyPerPersona() {
		String agentMeta = tool(AGENT_META_TOOL).name();

		assertFalse("secreader1 holds no agent.meta.", schemaNamesFor(READER).contains(agentMeta));
		assertTrue("seceditor1 does.", schemaNamesFor(EDITOR).contains(agentMeta));
	}

	/**
	 * The filter's other half, told apart from authorization: a {@code STRUCTURAL} tool this persona
	 * <em>is</em> authorized for is still withheld, by the execution mode.
	 *
	 * <p>
	 * {@link ToolPermissionFilter} answers two independent questions - may this user use the tool, and
	 * may <em>anyone</em> use it in the current {@link ExecutionMode}. The second withholds
	 * {@code STRUCTURAL} tools in {@code PRODUCTION}, which is what the test application reports, so a
	 * schema missing for a {@code STRUCTURAL} tool proves nothing about the policy on its own.
	 * </p>
	 *
	 * <p>
	 * This is worth pinning rather than working around, because it is also what a tester sees: the
	 * model editor and the agent-authoring tools stay out of the request in a deployed application
	 * whatever the policy says, and the denial reason names the mode rather than a capability - so
	 * nobody goes looking for a {@code <tool-access>} entry that is already correct.
	 * </p>
	 */
	public void testTheExecutionModeHalfWithholdsStructuralToolsIndependently() {
		Tool structural = tool(AGENT_AUTHORING_TOOL);
		SecurityContext security = securityFor(AUTHOR);

		assertEquals("Precondition: an agent-authoring tool is STRUCTURAL.",
			ToolMode.STRUCTURAL, structural.mode());
		assertEquals("Precondition: the test application reports PRODUCTION.",
			ExecutionMode.PRODUCTION, security.executionMode());

		assertTrue("secauthor1 is authorized for it - the policy grants agent.authoring.",
			security.hasToolPermission(structural));
		assertFalse("...and the execution-mode half withholds it anyway: " + schemaNamesFor(AUTHOR),
			schemaNamesFor(AUTHOR).contains(structural.name()));

		String reason = new ToolPermissionFilter(security).denialReason(structural);
		assertTrue("The reason must name the mode rather than a capability: " + reason,
			reason.contains("production mode"));
		assertFalse("Blaming the policy would send an operator to a configuration that is correct: "
			+ reason, reason.contains("capability"));
	}

	/**
	 * Nothing the policy grants nobody reaches any persona's request.
	 *
	 * <p>
	 * All three tools named here are {@code READ}-classified, so the execution-mode half of the filter
	 * has no say and their absence is attributable to authorization alone - see
	 * {@link #testTheExecutionModeHalfWithholdsStructuralToolsIndependently()} for why that matters.
	 * </p>
	 */
	public void testUngrantedCapabilitiesReachNoRequest() {
		for (String login : List.of(BARE, VIEWER, READER, EDITOR, AUTHOR)) {
			List<String> offered = schemaNamesFor(login);

			assertFalse(login + " must not be offered a workspace tool: " + offered,
				offered.contains(tool(WORKSPACE_TOOL).name()));
			assertFalse(login + " must not be offered an egress tool: " + offered,
				offered.contains(tool(EGRESS_TOOL).name()));
			assertFalse(login + " must not be offered an unclassified script tool: " + offered,
				offered.contains(tool(UNCLASSIFIED_SCRIPT_TOOL).name()));
		}

		// secwriter1 is the one persona holding workspace, so it is excluded above and asserted here -
		// a filter that withheld everything would otherwise pass the loop vacuously.
		assertTrue(schemaNamesFor(WRITER).contains(tool(WORKSPACE_TOOL).name()));
		assertFalse(schemaNamesFor(WRITER).contains(tool(EGRESS_TOOL).name()));
	}

	/**
	 * The bare persona is offered nothing but the implicitly granted {@code interaction} tool.
	 *
	 * <p>
	 * The deployed policy declares no {@code <baseline>}, so the one tool that survives the filter is
	 * the one no configuration had to mention.
	 * </p>
	 */
	public void testBarePersonaIsOfferedOnlyTheBaseline() {
		List<String> offered = schemaNamesFor(BARE);

		assertEquals("Only the interaction tool of the catalog survives the filter: " + offered,
			List.of("askUser"), offered);
	}

	/**
	 * A name the catalog cannot resolve is answered with "Unknown tool" and the available list, never
	 * with a permission message.
	 *
	 * <p>
	 * "No such tool" and "you may not use that" are different answers and a model acts on them
	 * differently: the first is corrected by picking a name from the list, the second is final. Naming
	 * a group for a hallucinated name would take away the hint the model corrects itself with, which
	 * is why {@code AgentLoop} deliberately does not put an unresolvable name through the permission
	 * check at all.
	 * </p>
	 */
	public void testUnknownToolNameIsNotAPermissionProblem() {
		String text = resultTextOf("call-1", "listEverythingEverywhere", READER);

		assertTrue("Expected an unknown-tool message, got: " + text,
			text.contains("Unknown tool: 'listEverythingEverywhere'"));
		assertTrue("The message must list what is available instead: " + text,
			text.contains("Available:"));
		assertFalse("An unresolvable name must not be reported as a permission problem: " + text,
			text.contains("Permission denied"));
		assertFalse("...nor name a capability the user does not hold: " + text,
			text.contains("capability"));
	}

	/**
	 * The {@code Available:} list of an unknown-tool message holds exactly what the persona may call.
	 *
	 * <p>
	 * Following the hint must not walk into a denial, and the list must not disclose the names of
	 * capabilities the persona does not have.
	 * </p>
	 */
	public void testUnknownToolMessageListsOnlyPermittedTools() {
		String text = resultTextOf("call-2", "nope", VIEWER);

		assertTrue("A tool the viewer may call belongs in the list: " + text,
			text.contains(tool(MODEL_READ_TOOL).name()));
		assertFalse("A tool it may not call does not: " + text,
			text.contains(tool(DATA_READ_TOOL).name()));
		assertFalse(text, text.contains(tool(STRUCTURE_TOOL).name()));
	}

	/**
	 * The catalog every case in this class filters: one tool per capability the fixture cares about.
	 *
	 * <p>
	 * Built here rather than taken from an agent definition on purpose - what is under test is the
	 * filter, and a fixed catalog makes "absent" mean "the filter removed it" rather than "the agent
	 * never carried it".
	 * </p>
	 */
	private ToolCatalog catalog() {
		ToolCatalog catalog = new ToolCatalog();
		for (String name : List.of("user-interaction.askUser", MODEL_READ_TOOL, DATA_READ_TOOL,
			AGENT_META_TOOL, AGENT_AUTHORING_TOOL, STRUCTURE_TOOL, WORKSPACE_TOOL, EGRESS_TOOL,
			CLASSIFIED_READ_SCRIPT_TOOL, UNCLASSIFIED_SCRIPT_TOOL)) {
			catalog.register(tool(name));
		}
		return catalog.freeze();
	}

	private List<String> schemaNamesFor(String login) {
		SecurityContext security = securityFor(login);
		return catalog().toolSchemas(new ToolPermissionFilter(security)).stream()
			.map(ToolSchema::name)
			.toList();
	}

	/**
	 * The text a tool call against the fixture catalog produces for the given persona.
	 *
	 * <p>
	 * Runs with no transaction scope, because every case here is a call the catalog refuses before it
	 * reaches a tool.
	 * </p>
	 */
	private String resultTextOf(String callId, String toolName, String login) {
		ToolContext context =
			new ToolContext(securityFor(login), ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
		ToolResultMessage result =
			catalog().execute(new ToolCall(callId, toolName, "{}"), context);

		assertTrue("Expected a refused call, got: " + result.state(),
			result.state() instanceof ToolState.Failed);
		return ((ToolState.Failed) result.state()).error();
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		// The filter consults the ExecutionMode for STRUCTURAL tools, which reads the maintenance
		// window - see testTheExecutionModeHalfWithholdsStructuralToolsIndependently.
		return suite(SecDemoSchemaFilterTest.class, MaintenanceWindowManager.Module.INSTANCE);
	}

}
