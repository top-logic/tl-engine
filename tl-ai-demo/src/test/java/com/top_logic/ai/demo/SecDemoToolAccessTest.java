/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * The invocation gate of the tool layer (L1), one persona at a time.
 *
 * <p>
 * This is the enforcement point: {@code AgentLoop.executeWithSecurityCheck} asks exactly the
 * {@link TopLogicSecurityContext#hasToolPermission} this test asks, so an allow or a denial here is
 * an allow or a denial of a real tool call. The per-turn schema filter is a separate question and
 * lives in {@link SecDemoSchemaFilterTest}; what a denial does to the agent's <em>prompt</em> lives
 * in {@link SecDemoCapabilityNoticeTest}.
 * </p>
 *
 * <p>
 * The counterpart of {@link SecDemoModelSecurityTest}, which covers the model layer (L2). The two are
 * independent: a role can be granted a tool and still be denied every object that tool touches, and
 * the other way round - {@link #testAuthorHoldsToolsAndNoDataRights()} is that contrast made
 * explicit.
 * </p>
 *
 * @see SecDemoPersonaTest
 */
public class SecDemoToolAccessTest extends SecDemoPersonaTest {

	/** Precondition: writing {@code <tool-access>} is what turned enforcement on. */
	public void testPolicyIsEnforced() {
		assertTrue("The secdemo aspect must be enabled for this test.",
			AiToolAccessService.Module.INSTANCE.isActive());
		assertEquals("A non-empty <tool-access> must resolve to ENFORCED. If this reports LEGACY,"
			+ " 'secdemo-legacy.config.xml' is on the metaConf and has to come off for this test.",
			AiSecurityMode.ENFORCED, AiToolAccessService.ceiling());
	}

	/** Precondition: the group taxonomy actually reached the registered tools. */
	public void testToolsDeclareTheirGroups() {
		assertEquals(ToolGroups.MODEL_READ, tool(MODEL_READ_TOOL).group());
		assertEquals(ToolGroups.DATA_READ, tool(DATA_READ_TOOL).group());
		assertEquals(ToolGroups.AGENT_META, tool(AGENT_META_TOOL).group());
		assertEquals(ToolGroups.AGENT_AUTHORING, tool(AGENT_AUTHORING_TOOL).group());
		assertEquals(ToolGroups.SCRIPT, tool(SCRIPT_TOOL).group());
		assertEquals(ToolGroups.MODEL_STRUCTURE, tool(STRUCTURE_TOOL).group());
		assertEquals(ToolGroups.WORKSPACE, tool(WORKSPACE_TOOL).group());
		assertEquals(ToolGroups.EGRESS, tool(EGRESS_TOOL).group());
	}

	/**
	 * Precondition: every configured persona exists, holds exactly its own role, and is not an
	 * administrator.
	 *
	 * <p>
	 * Note what else each of them holds: the engine gives every account
	 * {@code SecurityStructure.user} on the security root. It is not a defect and not something this
	 * layer adds - it is simply another role name a {@code <tool-access>} entry may be written
	 * against, and the closest thing to an "every authenticated account" row short of the policy's
	 * own {@code <baseline>}. An administrator who grants it is granting every account.
	 * </p>
	 */
	public void testEveryPersonaHoldsExactlyItsOwnRole() {
		Map<String, String> expected = SecDemoSetupService.personaRoles();
		assertEquals("The aspect must configure six personas; adjust this test bed if it changed.",
			6, expected.size());

		for (Map.Entry<String, String> persona : expected.entrySet()) {
			String login = persona.getKey();
			Set<String> held = AiToolAccessService.heldRoleNames(account(login));

			assertTrue(login + " must hold '" + persona.getValue() + "', got " + held,
				held.contains(persona.getValue()));
			for (String otherRole : expected.values()) {
				if (!otherRole.equals(persona.getValue())) {
					assertFalse(login + " must not hold '" + otherRole + "', got " + held,
						held.contains(otherRole));
				}
			}
		}
	}

	/** The bare persona reaches nothing but the baseline, not even a read-only model tool. */
	public void testBarePersonaIsDeniedEvenModelRead() {
		assertDenied(BARE, MODEL_READ_TOOL, ToolGroups.MODEL_READ);
		assertDenied(BARE, DATA_READ_TOOL, ToolGroups.DATA_READ);
		assertDenied(BARE, AGENT_META_TOOL, ToolGroups.AGENT_META);
	}

	/**
	 * The baseline applies to everyone, including the persona whose role grants nothing.
	 *
	 * <p>
	 * Without it an agent cannot even ask the user a question, which is why the baseline exists and
	 * why it is worth pinning that the narrowest persona still has it.
	 * </p>
	 */
	public void testBaselineReachesEvenTheBarePersona() {
		for (String login : SecDemoSetupService.personaLogins()) {
			assertAllowed(login, "user-interaction.askUser");
		}
	}

	/** {@code model.read} without {@code data.read}: the narrowest granted persona. */
	public void testViewerReadsTheModelButNotTheData() {
		assertAllowed(VIEWER, MODEL_READ_TOOL);
		assertDenied(VIEWER, DATA_READ_TOOL, ToolGroups.DATA_READ);
	}

	/** The read-only persona reaches both read groups and no write capability. */
	public void testReaderReadsModelAndData() {
		assertAllowed(READER, MODEL_READ_TOOL);
		assertAllowed(READER, DATA_READ_TOOL);
		assertDenied(READER, WORKSPACE_TOOL, ToolGroups.WORKSPACE);
		assertDenied(READER, EGRESS_TOOL, ToolGroups.EGRESS);
	}

	/**
	 * The one-capability pair: the same tool, denied to {@link #READER} and allowed to
	 * {@link #EDITOR}.
	 *
	 * <p>
	 * The sharpest single assertion in the tool layer, because the two personas differ in
	 * {@code agent.meta} and in nothing else. Anything that broke enforcement in general would break
	 * one half of this; anything that broke role resolution would break the other.
	 * </p>
	 */
	public void testAgentMetaSeparatesReaderFromEditor() {
		assertDenied(READER, AGENT_META_TOOL, ToolGroups.AGENT_META);
		assertAllowed(EDITOR, AGENT_META_TOOL);
	}

	/** A privileged group named explicitly in the policy is granted; a wildcard could not reach it. */
	public void testExplicitlyGrantedPrivilegedGroupsAreReachable() {
		assertAllowed(READER, SCRIPT_TOOL);
		assertAllowed(EDITOR, SCRIPT_TOOL);
		assertAllowed(AUTHOR, AGENT_AUTHORING_TOOL);
	}

	/** A privileged group granted to nobody needs no {@code <deny>} to be unreachable. */
	public void testUngrantedStructureToolDeniedToEveryPersona() {
		for (String login : SecDemoSetupService.personaLogins()) {
			assertDenied(login, STRUCTURE_TOOL, ToolGroups.MODEL_STRUCTURE);
		}
	}

	/** {@code egress} is granted to nobody: no persona can send data out of the application. */
	public void testEgressDeniedToEveryPersona() {
		for (String login : SecDemoSetupService.personaLogins()) {
			assertDenied(login, EGRESS_TOOL, ToolGroups.EGRESS);
		}
	}

	/** {@code workspace} separates the writer from every other persona. */
	public void testWorkspaceIsTheWritersAlone() {
		assertAllowed(WRITER, WORKSPACE_TOOL);
		for (String login : List.of(BARE, VIEWER, READER, EDITOR, AUTHOR)) {
			assertDenied(login, WORKSPACE_TOOL, ToolGroups.WORKSPACE);
		}
	}

	/**
	 * The two layers are independent: the agent author holds privileged tool grants and no data
	 * rights at all.
	 *
	 * <p>
	 * It may create an agent and may not list an instance. That combination is not a
	 * misconfiguration, it is the point of separating "which tool" from "which data": a tool grant is
	 * not a data grant, and the {@code <security-config>} that would give this persona data rights
	 * deliberately does not mention its role.
	 * </p>
	 */
	public void testAuthorHoldsToolsAndNoDataRights() {
		assertAllowed(AUTHOR, AGENT_AUTHORING_TOOL);
		assertAllowed(AUTHOR, AGENT_META_TOOL);
		assertDenied(AUTHOR, DATA_READ_TOOL, ToolGroups.DATA_READ);
		assertDenied(AUTHOR, MODEL_READ_TOOL, ToolGroups.MODEL_READ);
	}

	/**
	 * A remote MCP tool has no annotation to carry a group, so it is ungrouped and denied - the
	 * deny-by-default answer to "what happens when a tool arrives that nobody classified".
	 */
	public void testUnclassifiedToolDeniedToEveryPersona() {
		for (String login : SecDemoSetupService.personaLogins()) {
			String reason = securityFor(login).toolDenialReason("mcp__unknown__doThing");

			assertNotNull("An unclassified tool must be denied to " + login + ".", reason);
			assertTrue(reason, reason.contains("no tool group"));
		}
	}

	/**
	 * An administrator bypasses the tool layer, mirroring the engine: {@code root} already bypasses
	 * {@code ModelAccessRights}, and a tool layer that disagreed would produce a half-denial nobody
	 * could explain.
	 */
	public void testAdministratorBypassesThePolicy() {
		Person administrator = anyAdministrator();
		assertNotNull("The application must have at least one administrator account.", administrator);
		assertTrue("Precondition: " + administrator.getName() + " is an administrator.",
			administrator.isAdmin());

		TopLogicSecurityContext security = TopLogicSecurityContext.forRun(administrator, false);

		for (String toolName : List.of(STRUCTURE_TOOL, EGRESS_TOOL, AGENT_AUTHORING_TOOL,
			WORKSPACE_TOOL, UNCLASSIFIED_SCRIPT_TOOL)) {
			assertTrue("An administrator reaches '" + toolName + "', which the policy grants nobody.",
				security.hasToolPermission(tool(toolName)));
			assertNull(security.toolDenialReason(tool(toolName)));
		}
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoToolAccessTest.class);
	}

}
