/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.security;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.core.agent.SecurityContext;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.scripting.AIScriptFunctions;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContextManager;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * {@link RunSecurity#contextFor(RunPrincipal)} keeps the subject of a run only if the run may read
 * it, decided by the model access rights of the account the run acts as.
 *
 * <p>
 * The subject is a conversation of one account. Under the access policy that this module ships for
 * conversations, only its owner may read it. No policy is written for the test: the default factory
 * and the shipped rights decide.
 * </p>
 */
public class TestRunSubjectCheck extends BasicTestCase {

	/** May read {@link #subject}. */
	private static Person owner;

	/** May not read {@link #subject}. */
	private static Person stranger;

	private static AgentSession subject;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner != null) {
			return;
		}
		owner = TestPerson.createPerson("run-subject-owner");
		stranger = TestPerson.createPerson("run-subject-stranger");
		subject = AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, owner, "Website relaunch");
		HistoryUtils.updateSessionRevision();
	}

	/** An account that may read the subject keeps it, and everything else the entry point stated. */
	public void testReadableSubjectIsKept() {
		RunPrincipal stated = new RunPrincipal(owner, RunEntryPoint.SCRIPT, true, "conv-1", subject);

		assertEquals(stated, RunSecurity.contextFor(stated).principal());
	}

	/**
	 * An account that may not read the subject gets a run without one, and everything else the entry
	 * point stated.
	 */
	public void testUnreadableSubjectIsDropped() {
		RunPrincipal stated = new RunPrincipal(stranger, RunEntryPoint.SCRIPT, true, "conv-2", subject);

		assertEquals(new RunPrincipal(stranger, RunEntryPoint.SCRIPT, true, "conv-2", null),
			RunSecurity.contextFor(stated).principal());
	}

	/** A run that states no subject has none. */
	public void testRunWithoutSubjectHasNone() {
		RunPrincipal stated = new RunPrincipal(owner, RunEntryPoint.CHAT, false);

		assertEquals(stated, RunSecurity.contextFor(stated).principal());
	}

	/**
	 * The rights of the context the factory returns decide, not the rights of the account: a factory
	 * that narrows the model rights drops a subject the account itself may read.
	 */
	public void testTheRightsOfTheFactorysContextDecide() {
		RunPrincipal stated = new RunPrincipal(owner, RunEntryPoint.SCRIPT, false, null, subject);

		SecurityContext context = RunSecurity.contextFor(stated,
			run -> withReadAnswer(DefaultSecurityContextFactory.defaultContext(run), () -> Boolean.FALSE));

		assertNull(context.principal().subject());
	}

	/** A factory whose context names a principal of its own: the subject of that principal is checked. */
	public void testTheSubjectOfTheFactorysPrincipalIsChecked() {
		assertEquals("A subject the account may read is kept.", subject,
			RunSecurity.contextFor(new RunPrincipal(owner, RunEntryPoint.SCRIPT, false),
				namingItsOwnSubject(owner)).principal().subject());

		SecurityContext context = RunSecurity.contextFor(new RunPrincipal(stranger, RunEntryPoint.SCRIPT, false),
			namingItsOwnSubject(stranger));

		assertNull("A subject the account may not read is dropped.", context.principal().subject());
		assertEquals(stranger, context.principal().account());
	}

	/**
	 * A check that fails drops the subject, and the log line names neither the label of the subject
	 * nor the message of the failure, which can contain it.
	 */
	public void testAFailedCheckLogsNoLabel() {
		String label = "Label of the subject";
		RunPrincipal stated = new RunPrincipal(owner, RunEntryPoint.SCRIPT, false, null, subject);
		CollectingLogListener log = new CollectingLogListener(Set.of(Level.INFO), true);
		SecurityContext context;
		try {
			context = RunSecurity.contextFor(stated,
				run -> withReadAnswer(DefaultSecurityContextFactory.defaultContext(run), () -> {
					throw new IllegalStateException("Cannot check " + label + ".");
				}));
		} finally {
			log.deactivate();
		}

		assertNull(context.principal().subject());
		List<String> lines = log.getLogEntries().stream()
			.filter(entry -> entry.getCaller() == RunSecurity.class)
			.map(LogEntry::getMessage)
			.toList();
		assertEquals(lines.toString(), 1, lines.size());
		assertFalse(lines.get(0), lines.get(0).contains(label));
	}

	/**
	 * {@code aiAgentChatAsync} states its subject for the run: the session of the run is about it.
	 *
	 * <p>
	 * The agent does not exist, so the run fails, but only after it wrote its subject onto the
	 * session.
	 * </p>
	 */
	public void testAsyncScriptRunStatesItsSubject() {
		TLContextManager.inPersonContext(owner, () -> {
			String handle = AIScriptFunctions.agentChatAsync("__no_such_agent__", "hello", subject);
			String status = null;
			for (int n = 0; n < 150 && (status == null || ExecutionStatus.RUNNING.name().equals(status)); n++) {
				HistoryUtils.updateSessionRevision();
				status = AIScriptFunctions.agentRunStatus(handle, false);
				if (status == null || ExecutionStatus.RUNNING.name().equals(status)) {
					pause();
				}
			}
			assertEquals(ExecutionStatus.ERROR.name(), status);
			assertEquals(subject, AgentExecutionStore.getInstance().findSessionById(handle).getSubject());
		});
	}

	private static void pause() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/** A factory whose context names the account of the run and {@link #subject}, whatever was stated. */
	private static SecurityContextFactory namingItsOwnSubject(Person account) {
		RunPrincipal named = new RunPrincipal(account, RunEntryPoint.SCRIPT, false, null, subject);
		return run -> new SecurityContextProxy(DefaultSecurityContextFactory.defaultContext(run)) {
			@Override
			public RunPrincipal principal() {
				return named;
			}
		};
	}

	/** The given context, with model rights that answer Read with the given answer. */
	private static SecurityContext withReadAnswer(SecurityContext context, Supplier<Boolean> read) {
		ModelSecurity rights = context.modelSecurity();
		ModelSecurity answering = (ModelSecurity) Proxy.newProxyInstance(ModelSecurity.class.getClassLoader(),
			new Class<?>[] { ModelSecurity.class },
			(proxy, method, args) -> method.getName().equals("canRead") && args.length == 1
				? read.get()
				: method.invoke(rights, args));
		return new SecurityContextProxy(context) {
			@Override
			public ModelSecurity modelSecurity() {
				return answering;
			}
		};
	}

	/**
	 * The services of the conversations and of their access policy, the tool access the default
	 * context of a run is built with, and the scheduler of an asynchronous script run.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestRunSubjectCheck.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}
}
