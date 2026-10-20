/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
 */
public abstract class SecDemoPersonaTest extends BasicTestCase {

	/**
	 * {@code projectPartner} on the security root (the tier) and on project {@link #ALPHA} (the
	 * scope): write tools globally, and one project to write them on.
	 */
	public static final String ANNA = "anna";

	/** {@code ticketReader} on the security root: reads every ticket, writes none. */
	public static final String BEN = "ben";

	/** Both roles: {@code projectPartner} scoped to {@link #BETA}, and {@code ticketReader}. */
	public static final String CARA = "cara";

	/**
	 * {@code projectPartner} on the security root and on nothing else.
	 *
	 * <p>
	 * The failure mode the fixture is built to make visible: write tools and nothing to write. Every
	 * attempt refused, and reads come back <em>empty</em> rather than denied.
	 * </p>
	 */
	public static final String DORA = "dora";

	/** The project {@link #ANNA} partners on. */
	public static final String ALPHA = "Alpha";

	/** The project {@link #CARA} partners on. */
	public static final String BETA = "Beta";

	/** In {@code model.read}, granted to both roles. */
	public static final String MODEL_READ_TOOL = "model-browser.listModules";

	/** In {@code data.read}. */
	public static final String DATA_READ_TOOL = "model-instances.listInstances";

	/** In {@code agent.meta}, granted to nobody. */
	public static final String AGENT_META_TOOL = "framework-wiring.listFrameworkAgents";

	/** In {@code agent.authoring}, a privileged group, granted to nobody. */
	public static final String AGENT_AUTHORING_TOOL = "agent-authoring.createAgent";

	/** In {@code script}, a privileged group, granted to both roles: changing data with a script. */
	public static final String SCRIPT_TOOL = "script-exec.changeData";

	/** In {@code data.read}: answering a question with one read-only expression. */
	public static final String QUERY_TOOL = "model-query.query";

	/** In {@code model.structure}, a privileged group, granted to nobody. */
	public static final String STRUCTURE_TOOL = "model-modification.createClass";

	/** In {@code workspace}, granted to nobody. */
	public static final String WORKSPACE_TOOL = "task.createTask";

	/** In {@code egress}, granted to nobody. */
	public static final String EGRESS_TOOL = "web-fetch.webFetch";

	/** In {@code data.read}: the paginated instance listing. */
	public static final String LIST_TOOL = "model-instances.listInstances";

	/** In {@code data.read}: the single-instance description. */
	public static final String GET_TOOL = "model-instances.getInstance";

	/**
	 * A TL-Script tool the policy names in its {@code data.read} {@code <tool-groups>} entry, whose
	 * count is deliberately <em>unfiltered</em>.
	 *
	 * @see #UNCLASSIFIED_SCRIPT_TOOL
	 * @see #FILTERED_COUNT_SCRIPT_TOOL
	 */
	public static final String CLASSIFIED_READ_SCRIPT_TOOL = "secdemo-reporting.countTickets";

	/**
	 * The honest counterpart of {@link #CLASSIFIED_READ_SCRIPT_TOOL}: it filters inside the script
	 * before counting, which is the only way to report a count the acting account may believe.
	 */
	public static final String FILTERED_COUNT_SCRIPT_TOOL = "secdemo-reporting.countVisibleTickets";

	/** A TL-Script tool the policy names in its {@code data.write} {@code <tool-groups>} entry. */
	public static final String CLASSIFIED_WRITE_SCRIPT_TOOL = "secdemo-editing.addTicket";

	/**
	 * The same TL-Script tool as {@link #CLASSIFIED_READ_SCRIPT_TOOL} in every respect except that
	 * no {@code <tool-groups>} entry names it.
	 */
	public static final String UNCLASSIFIED_SCRIPT_TOOL = "secdemo-unclassified.countTicketsUnclassified";

	/**
	 * A TL-Script that persists something, for the tests of the commit path.
	 *
	 * <p>
	 * Creates a top-level ticket rather than one inside a project, so that what is under test is the
	 * {@code Create} grant on {@code pm:Ticket} and not the {@code Write} grant on the composition.
	 * </p>
	 *
	 * @see #changeDataAs(String, String, boolean)
	 */
	public static final String CREATE_TICKET =
		"new(`pm:Ticket`)..set(`pm:Ticket#title`, \"Commit gate test\")";

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
	 * Runs the registered tool that changes data with a script as the given persona, the way a tool
	 * call in a run does.
	 *
	 * <p>
	 * Goes through {@link ToolExecutions} and the tool's own argument dispatch rather than calling the
	 * Java method, because what is under test on the change path is a check the tool performs on an
	 * <em>argument</em> - and the injected execution context that carries the answer arrives through
	 * exactly that dispatch.
	 * </p>
	 *
	 * @param login
	 *        The persona to act as.
	 * @param script
	 *        The script that changes data.
	 * @param dryRun
	 *        Whether to run it without saving.
	 * @return The tool result text - a refusal is a failed result, not an exception.
	 */
	protected String changeDataAs(String login, String script, boolean dryRun) {
		return changeData(securityFor(login), script, dryRun);
	}

	/**
	 * Runs the registered tool that changes data with a script under the given security context.
	 *
	 * <p>
	 * For the runs no persona login describes: an administrator, or a background run.
	 * </p>
	 *
	 * @see #changeDataAs(String, String, boolean)
	 */
	protected static String changeData(SecurityContext security, String script, boolean dryRun) {
		Map<String, Object> args = new HashMap<>();
		args.put("script", script);
		args.put("dryRun", dryRun);

		ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
		return ToolExecutions.execute(tool(SCRIPT_TOOL), args, context).text();
	}

	/**
	 * Asks the registered query tool as the given persona: one read-only expression, the way a read
	 * in a run is made.
	 *
	 * @return The tool result text.
	 */
	protected String queryAs(String login, String expression) {
		return query(securityFor(login), expression);
	}

	/**
	 * Asks the registered query tool under the given security context.
	 *
	 * @see #queryAs(String, String)
	 */
	protected static String query(SecurityContext security, String expression) {
		ToolContext context = new ToolContext(security, ApprovalCallback.AUTO_APPROVE);
		return ToolExecutions.execute(tool(QUERY_TOOL), Map.of("expression", expression), context).text();
	}

	/**
	 * Every {@code pm:Ticket} instance, for a test that checks whether a commit landed.
	 */
	protected static List<TLObject> tickets() {
		return instancesOf("pm:Ticket");
	}

	/**
	 * Every {@code pm:Project} instance.
	 */
	protected static List<TLObject> projects() {
		return instancesOf("pm:Project");
	}

	/**
	 * Every instance of the given qualified type name.
	 */
	protected static List<TLObject> instancesOf(String qualifiedTypeName) {
		return MetaElementUtil.getAllInstancesOf((TLClass) TLModelUtil.findType(qualifiedTypeName),
			TLObject.class);
	}

	/**
	 * The seeded project of the given name, asserted to exist.
	 */
	protected static TLObject project(String name) {
		TLObject project = SecDemoSetupService.projectByName(name);
		assertNotNull("The project '" + name + "' must have been seeded by "
			+ SecDemoSetupService.class.getSimpleName() + "; check <projects> in 'secdemo.config.xml'.",
			project);
		return project;
	}

	/**
	 * A ticket of the given project, asserted to exist.
	 */
	@SuppressWarnings("unchecked")
	protected static TLObject ticketOf(String projectName) {
		Collection<TLObject> tickets =
			(Collection<TLObject>) project(projectName).tValueByName("tickets");
		assertFalse("Project '" + projectName + "' must contain at least one ticket.", tickets.isEmpty());
		return tickets.iterator().next();
	}

	/**
	 * Deletes every ticket that is not one of {@code keep}, so a test that committed one leaves the
	 * fixture as it found it.
	 */
	protected static void deleteTicketsBeyond(Collection<TLObject> keep) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			for (TLObject ticket : tickets()) {
				if (!keep.contains(ticket)) {
					ticket.tDelete();
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
