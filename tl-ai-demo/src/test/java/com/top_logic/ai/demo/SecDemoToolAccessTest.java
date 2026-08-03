/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * End-to-end test of the tool layer (L1) of the security scaffold in
 * {@code WEB-INF/conf/secdemo.config.xml}: the {@code <tool-access>} policy is evaluated against the
 * roles the acting account actually holds on the security root, with the real tool registry.
 *
 * <p>
 * The counterpart of {@link SecDemoModelSecurityTest}, which covers the model layer (L2). The two are
 * independent: a role can be granted a tool and still be denied every object that tool touches, and
 * the other way round.
 * </p>
 *
 * <p>
 * Nothing here is stubbed. The roles come from {@code InitialRolesManager}, the group memberships from
 * {@code SecDemoSetupService}, the accounts from the same, the policy from the shipped aspect file, and
 * the tools from the running {@code ToolRegistryService} with their {@code @ToolMethod(group = ...)}
 * declarations. What is not covered is the agent loop calling the gate, because that needs a model
 * provider; see section 5 of {@code doc/security/e2e-security-test.md}.
 * </p>
 *
 * @see com.top_logic.ai.demo.secdemo.SecDemoSetupService
 */
public class SecDemoToolAccessTest extends BasicTestCase {

	/** In {@code agent.meta}, granted to secEditor only. Read-only, so calling it is harmless. */
	private static final String AGENT_META_TOOL = "framework-wiring.listFrameworkAgents";

	/** In {@code script}, a privileged group, granted to both demo roles. */
	private static final String SCRIPT_TOOL = "script-exec.executeTLScript";

	/** In {@code model.structure}, a privileged group, granted to nobody. */
	private static final String STRUCTURE_TOOL = "model-modification.createClass";

	/** In {@code data.read}, granted to both demo roles. */
	private static final String DATA_READ_TOOL = "model-instances.listInstances";

	private Person _previousUser;

	@Override
	protected void tearDown() throws Exception {
		if (_previousUser != null) {
			TLContext.getContext().setCurrentPerson(_previousUser);
		}
		super.tearDown();
	}

	/** Precondition: writing {@code <tool-access>} is what turned enforcement on. */
	public void testPolicyIsEnforced() {
		assertTrue("The secdemo aspect must be enabled for this test.",
			AiToolAccessService.Module.INSTANCE.isActive());
		assertEquals("A non-empty <tool-access> must resolve to ENFORCED.",
			AiSecurityMode.ENFORCED, AiToolAccessService.ceiling());
	}

	/** Precondition: the group taxonomy actually reached the registered tools. */
	public void testToolsDeclareTheirGroups() {
		assertEquals(ToolGroups.AGENT_META, tool(AGENT_META_TOOL).group());
		assertEquals(ToolGroups.SCRIPT, tool(SCRIPT_TOOL).group());
		assertEquals(ToolGroups.MODEL_STRUCTURE, tool(STRUCTURE_TOOL).group());
		assertEquals(ToolGroups.DATA_READ, tool(DATA_READ_TOOL).group());
	}

	/**
	 * Precondition: the accounts hold the roles the policy grants to.
	 *
	 * <p>
	 * Note what else they hold: the engine gives every account {@code SecurityStructure.user} on the
	 * security root. It is not a defect and not something this layer adds - it is simply another role
	 * name a {@code <tool-access>} entry may be written against, and the closest thing to an "every
	 * authenticated account" row short of the policy's own {@code <baseline>}. An administrator who
	 * grants it is granting every account.
	 * </p>
	 */
	public void testAccountsHoldTheDemoRoles() {
		Set<String> readerRoles = AiToolAccessService.heldRoleNames(account("secreader1"));
		Set<String> editorRoles = AiToolAccessService.heldRoleNames(account("seceditor1"));

		assertTrue("secreader1 holds secReader, got " + readerRoles, readerRoles.contains("secReader"));
		assertFalse("secreader1 must not hold secEditor, got " + readerRoles,
			readerRoles.contains("secEditor"));
		assertTrue("seceditor1 holds secEditor, got " + editorRoles, editorRoles.contains("secEditor"));
		assertFalse("seceditor1 must not hold secReader, got " + editorRoles,
			editorRoles.contains("secReader"));
	}

	/**
	 * The denial: {@code secReader} is granted no {@code agent.meta}, so the tool is refused.
	 */
	public void testReaderDeniedAgentMetaTool() {
		assertDenied("secreader1", AGENT_META_TOOL, ToolGroups.AGENT_META);
	}

	/**
	 * The allow, on the same tool for the other role - the pair that makes the denial meaningful.
	 */
	public void testEditorAllowedAgentMetaTool() {
		assertAllowed("seceditor1", AGENT_META_TOOL);
	}

	/** A privileged group named explicitly in the policy is granted, to both roles here. */
	public void testExplicitlyGrantedPrivilegedGroupAllowed() {
		assertAllowed("secreader1", SCRIPT_TOOL);
		assertAllowed("seceditor1", SCRIPT_TOOL);
	}

	/** Deny-by-default: a group no role is granted needs no {@code <deny>} to be unreachable. */
	public void testUngrantedStructureToolDeniedToBoth() {
		assertDenied("secreader1", STRUCTURE_TOOL, ToolGroups.MODEL_STRUCTURE);
		assertDenied("seceditor1", STRUCTURE_TOOL, ToolGroups.MODEL_STRUCTURE);
	}

	/** A group both roles hold is reachable by both. */
	public void testDataReadAllowedToBoth() {
		assertAllowed("secreader1", DATA_READ_TOOL);
		assertAllowed("seceditor1", DATA_READ_TOOL);
	}

	/**
	 * A remote MCP tool has no annotation to carry a group, so it is ungrouped and denied - the
	 * deny-by-default answer to "what happens when a tool arrives that nobody classified".
	 */
	public void testUnclassifiedToolDeniedToBoth() {
		for (String login : List.of("secreader1", "seceditor1")) {
			String reason = securityFor(login, false).toolDenialReason("mcp__unknown__doThing");

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

		assertTrue("An administrator reaches even an ungranted privileged tool.",
			security.hasToolPermission(tool(STRUCTURE_TOOL)));
		assertNull(security.toolDenialReason(tool(STRUCTURE_TOOL)));
	}

	/**
	 * A background run is bounded by the allowlist even for a capability its role holds.
	 *
	 * <p>
	 * The demo allowlist covers everything the harness needs, so what is asserted here is the
	 * mechanism rather than a denial: a tool outside the allowlist is refused to an account that
	 * otherwise holds it. {@code model.structure} is not on the allowlist and not granted, so the
	 * message must name the allowlist rather than the missing capability.
	 * </p>
	 */
	public void testBackgroundRunIsBoundedByTheAllowlist() {
		String reason = securityFor("seceditor1", true).toolDenialReason(tool(STRUCTURE_TOOL));

		assertNotNull(reason);
		assertTrue("A background denial must say it is the allowlist, not the role: " + reason,
			reason.contains("background allowlist"));
	}

	/** A denial names the tool and the missing capability, and nothing about withheld data. */
	private void assertDenied(String login, String qualifiedToolName, String expectedGroup) {
		Tool tool = tool(qualifiedToolName);
		TopLogicSecurityContext security = securityFor(login, false);

		assertFalse(login + " must not be allowed to use '" + qualifiedToolName + "'.",
			security.hasToolPermission(tool));

		String reason = security.toolDenialReason(tool);
		assertNotNull("A denial must state a reason.", reason);
		assertTrue("The reason must name the tool: " + reason,
			reason.contains(qualifiedToolName) || reason.contains(tool.name()));
		assertTrue("The reason must name the missing capability: " + reason,
			reason.contains(expectedGroup));
	}

	private void assertAllowed(String login, String qualifiedToolName) {
		Tool tool = tool(qualifiedToolName);
		TopLogicSecurityContext security = securityFor(login, false);

		String reason = security.toolDenialReason(tool);
		assertNull(login + " must be allowed to use '" + qualifiedToolName + "', but: " + reason,
			reason);
		assertTrue(security.hasToolPermission(tool));
	}

	/**
	 * The security context of a run acting as the given account, built exactly as the chat and
	 * TL-Script entry points build it.
	 */
	private TopLogicSecurityContext securityFor(String login, boolean background) {
		return TopLogicSecurityContext.forRun(actAs(login), background);
	}

	private Person actAs(String login) {
		TLContext context = TLContext.getContext();
		if (_previousUser == null) {
			_previousUser = context.getPerson();
		}
		Person account = account(login);
		context.setCurrentPerson(account);
		return account;
	}

	private static Person anyAdministrator() {
		for (Person person : Person.all()) {
			if (person.isAdmin()) {
				return person;
			}
		}
		return null;
	}

	private static Person account(String login) {
		Person account = Person.byName(login);
		assertNotNull("The demo account '" + login + "' must have been seeded by "
			+ SecDemoSetupService.class.getSimpleName() + ".", account);
		assertFalse("The demo account '" + login + "' must not be an administrator, "
			+ "otherwise it would bypass the checks under test.", account.isAdmin());
		return account;
	}

	private static Tool tool(String qualifiedName) {
		Tool tool = ToolRegistryService.getInstance().getTool(qualifiedName);
		assertNotNull("The tool '" + qualifiedName + "' must be registered.", tool);
		return tool;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		List<BasicRuntimeModule<?>> modules = new ArrayList<>();
		modules.add(SearchBuilder.Module.INSTANCE);
		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);
		modules.add(PersonManager.Module.INSTANCE);
		modules.add(SecDemoSetupService.Module.INSTANCE);
		modules.add(ToolRegistryService.Module.INSTANCE);
		modules.add(AiToolAccessService.Module.INSTANCE);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(SecDemoToolAccessTest.class),
				modules.toArray(new BasicRuntimeModule<?>[modules.size()])));
	}

}
