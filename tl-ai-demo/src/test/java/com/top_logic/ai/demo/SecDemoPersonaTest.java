/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.tools.ToolExecutions;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Shared fixture access for the tests of the agent security test bed.
 *
 * <p>
 * Every subclass runs against a real application boot of the opt-in aspect
 * {@code WEB-INF/conf/secdemo.config.xml}: real roles from {@code InitialRolesManager}, real group
 * memberships and accounts from {@link SecDemoSetupService}, the shipped {@code <tool-access>}
 * policy, and the running {@code ToolRegistryService} with the {@code @ToolMethod(group = ...)}
 * declarations of every registered tool. Nothing is stubbed - a test that passes here is a statement
 * about the deployed configuration, not about a hand-built policy object.
 * </p>
 *
 * <p>
 * The persona names are deliberately written out in each test rather than looked up, so that a
 * failure message names the persona whose behaviour changed. What is looked up is the <em>set</em> of
 * personas ({@link SecDemoSetupService#personaLogins()}), for the tests that must cover all of them:
 * a test that hard-coded the list would silently stop covering a persona added to the aspect.
 * </p>
 *
 * @see doc/security/testbed.md
 */
public abstract class SecDemoPersonaTest extends BasicTestCase {

	/** Holds nothing beyond the implicitly granted {@code interaction} capability. */
	public static final String BARE = "secbare1";

	/** Holds {@code model.read} and nothing else. */
	public static final String VIEWER = "secviewer1";

	/** Holds {@code model.read}, {@code data.read}, {@code script}. */
	public static final String READER = "secreader1";

	/** {@link #READER} plus {@code agent.meta} and {@code data.write}. */
	public static final String EDITOR = "seceditor1";

	/** Holds {@code model.read}, {@code data.read}, {@code data.write}, {@code workspace}. */
	public static final String WRITER = "secwriter1";

	/** Holds {@code agent.meta} and {@code agent.authoring}, and no data rights. */
	public static final String AUTHOR = "secauthor1";

	/** In {@code model.read}, granted to every persona but {@link #BARE}. */
	public static final String MODEL_READ_TOOL = "model-browser.listModules";

	/** In {@code data.read}. */
	public static final String DATA_READ_TOOL = "model-instances.listInstances";

	/** In {@code agent.meta}, granted to {@link #EDITOR} and {@link #AUTHOR}. */
	public static final String AGENT_META_TOOL = "framework-wiring.listFrameworkAgents";

	/** In {@code agent.authoring}, a privileged group, granted to {@link #AUTHOR} only. */
	public static final String AGENT_AUTHORING_TOOL = "agent-authoring.createAgent";

	/** In {@code script}, a privileged group, granted to {@link #READER} and {@link #EDITOR}. */
	public static final String SCRIPT_TOOL = "script-exec.executeTLScript";

	/** In {@code model.structure}, a privileged group, granted to nobody. */
	public static final String STRUCTURE_TOOL = "model-modification.createClass";

	/** In {@code workspace}, granted to {@link #WRITER} only. */
	public static final String WORKSPACE_TOOL = "task.createTask";

	/** In {@code egress}, granted to nobody. */
	public static final String EGRESS_TOOL = "web-fetch.webFetch";

	/**
	 * A TL-Script tool the policy names in its {@code data.read} {@code <tool-groups>} entry.
	 *
	 * @see #UNCLASSIFIED_SCRIPT_TOOL
	 */
	public static final String CLASSIFIED_READ_SCRIPT_TOOL = "secdemo-reporting.countNotes";

	/** A TL-Script tool the policy names in its {@code data.write} {@code <tool-groups>} entry. */
	public static final String CLASSIFIED_WRITE_SCRIPT_TOOL = "secdemo-editing.addNote";

	/**
	 * The same TL-Script tool as {@link #CLASSIFIED_READ_SCRIPT_TOOL} in every respect except that
	 * no {@code <tool-groups>} entry names it.
	 */
	public static final String UNCLASSIFIED_SCRIPT_TOOL = "secdemo-unclassified.countNotesUnclassified";

	/**
	 * A TL-Script that persists something, for the tests of the commit path.
	 *
	 * @see #executeScriptAs(String, String, boolean)
	 */
	public static final String CREATE_NOTE =
		"new(`secdemo:Note`)..set(`secdemo:Note#title`, \"Commit gate test\")";

	private Person _previousUser;

	@Override
	protected void tearDown() throws Exception {
		if (_previousUser != null) {
			TLContext.getContext().setCurrentPerson(_previousUser);
			_previousUser = null;
		}
		super.tearDown();
	}

	/**
	 * The security context of a foreground run acting as the given persona, built exactly as the chat
	 * and TL-Script entry points build it.
	 */
	protected TopLogicSecurityContext securityFor(String login) {
		return securityFor(login, false);
	}

	/**
	 * The security context of a run acting as the given persona.
	 *
	 * @param background
	 *        Whether the run has no human to answer an approval prompt, which additionally subjects
	 *        it to the policy's {@code <background-allowlist>}.
	 */
	protected TopLogicSecurityContext securityFor(String login, boolean background) {
		return TopLogicSecurityContext.forRun(actAs(login), background);
	}

	/**
	 * Binds the given persona to the calling thread and returns it.
	 */
	protected Person actAs(String login) {
		TLContext context = TLContext.getContext();
		if (_previousUser == null) {
			_previousUser = context.getPerson();
		}
		Person account = account(login);
		context.setCurrentPerson(account);
		return account;
	}

	/** Asserts that the given persona may invoke the given tool, and says why not if it may not. */
	protected void assertAllowed(String login, String qualifiedToolName) {
		Tool tool = tool(qualifiedToolName);
		TopLogicSecurityContext security = securityFor(login);

		String reason = security.toolDenialReason(tool);
		assertNull(login + " must be allowed to use '" + qualifiedToolName + "', but: " + reason, reason);
		assertTrue(security.hasToolPermission(tool));
	}

	/**
	 * Asserts that the given persona is refused the given tool, with a reason naming the tool and the
	 * capability it needs and nothing about withheld data.
	 */
	protected void assertDenied(String login, String qualifiedToolName, String expectedCapability) {
		Tool tool = tool(qualifiedToolName);
		TopLogicSecurityContext security = securityFor(login);

		assertFalse(login + " must not be allowed to use '" + qualifiedToolName + "'.",
			security.hasToolPermission(tool));

		String reason = security.toolDenialReason(tool);
		assertNotNull("A denial must state a reason.", reason);
		assertTrue("The reason must name the tool: " + reason,
			reason.contains(qualifiedToolName) || reason.contains(tool.name()));
		assertTrue("The reason must name the missing capability: " + reason,
			reason.contains(expectedCapability));
	}

	/**
	 * Runs the registered TL-Script tool as the given persona, the way a tool call in a run does.
	 *
	 * <p>
	 * Goes through {@link ToolExecutions} and the tool's own argument dispatch rather than calling the
	 * Java method, because what is under test on the commit path is a check the tool performs on an
	 * <em>argument</em> - and the injected execution context that carries the answer arrives through
	 * exactly that dispatch.
	 * </p>
	 *
	 * @param login
	 *        The persona to act as.
	 * @param script
	 *        The TL-Script to execute.
	 * @param withCommit
	 *        Whether to ask for the changes to be persisted.
	 * @return The tool result text, a JSON object either way - a refusal is a tool error, not an
	 *         exception.
	 */
	protected String executeScriptAs(String login, String script, boolean withCommit) {
		return executeScript(securityFor(login), script, withCommit);
	}

	/**
	 * Runs the registered TL-Script tool under the given security context.
	 *
	 * <p>
	 * For the runs no persona login describes: an administrator, or a background run.
	 * </p>
	 *
	 * @see #executeScriptAs(String, String, boolean)
	 */
	protected static String executeScript(SecurityContext security, String script, boolean withCommit) {
		Map<String, Object> args = new HashMap<>();
		args.put("script", script);
		args.put("withCommit", withCommit);

		ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
		return ToolExecutions.execute(tool(SCRIPT_TOOL), args, context).text();
	}

	/**
	 * Every {@code secdemo:Note} instance, for a test that checks whether a commit landed.
	 */
	protected static List<TLObject> notes() {
		return MetaElementUtil.getAllInstancesOf((TLClass) TLModelUtil.findType("secdemo:Note"),
			TLObject.class);
	}

	/**
	 * Deletes every note that is not one of {@code keep}, so a test that committed one leaves the
	 * fixture as it found it.
	 */
	protected static void deleteNotesBeyond(Collection<TLObject> keep) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			for (TLObject note : notes()) {
				if (!keep.contains(note)) {
					note.tDelete();
				}
			}
			tx.commit();
		}
	}

	/**
	 * A persona account, asserted to exist and not to be an administrator.
	 */
	protected static Person account(String login) {
		Person account = Person.byName(login);
		assertNotNull("The persona '" + login + "' must have been seeded by "
			+ SecDemoSetupService.class.getSimpleName() + "; check the <personas> list in"
			+ " 'secdemo.config.xml'.", account);
		assertFalse("The persona '" + login + "' must not be an administrator, otherwise it would"
			+ " bypass the checks under test.", account.isAdmin());
		return account;
	}

	/**
	 * A registered tool, asserted to exist.
	 */
	protected static Tool tool(String qualifiedName) {
		Tool tool = ToolRegistryService.getInstance().getTool(qualifiedName);
		assertNotNull("The tool '" + qualifiedName + "' must be registered.", tool);
		return tool;
	}

	/**
	 * The first administrator account of the application.
	 */
	protected static Person anyAdministrator() {
		for (Person person : Person.all()) {
			if (person.isAdmin()) {
				return person;
			}
		}
		return null;
	}

	/**
	 * A suite booting the test application with the services the test bed needs.
	 *
	 * @param testClass
	 *        The test class to run.
	 * @param additionalModules
	 *        Services beyond the fixture's own that the test needs.
	 */
	protected static Test suite(Class<? extends Test> testClass,
			BasicRuntimeModule<?>... additionalModules) {
		List<BasicRuntimeModule<?>> modules = new ArrayList<>();
		modules.add(SearchBuilder.Module.INSTANCE);
		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);
		modules.add(PersonManager.Module.INSTANCE);
		modules.add(SecDemoSetupService.Module.INSTANCE);
		modules.add(ToolRegistryService.Module.INSTANCE);
		modules.add(AiToolAccessService.Module.INSTANCE);
		for (BasicRuntimeModule<?> module : additionalModules) {
			modules.add(module);
		}

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(testClass),
				modules.toArray(new BasicRuntimeModule<?>[modules.size()])));
	}

}
