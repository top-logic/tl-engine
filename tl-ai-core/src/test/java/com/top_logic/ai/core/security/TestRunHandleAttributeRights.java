/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.security;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.tools.script.TLScriptExecutor;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A grant on one attribute of a run decides for the run handle function that returns this
 * attribute, as it decides for {@code $run.get(...)} (Ticket #30482).
 *
 * <p>
 * The configuration next to this class lets only a role that nobody holds read the attribute
 * {@code transcript} of a run. The owner keeps every other part of the run through the shipped
 * policy, which {@link TestRunHandleAccessPolicy} asserts without such a grant.
 * </p>
 */
public class TestRunHandleAttributeRights extends BasicTestCase {

	private static final String ANSWER = "The answer of the run.";

	/** The text of a transcript is not checked; it is only the value that must not leak. */
	private static final String LOG = "{\"turns\":[]}";

	private static Person owner;

	private static AgentExecution run;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner != null) {
			return;
		}
		owner = TestPerson.createPerson("run-handle-part-owner");
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.BACKGROUND, owner, "run-handle-part");
		run = store.beginExecution(session, "run-handle-part", "agent", null, null, null);
		store.finishExecution(run, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, null, LOG, ANSWER);
		HistoryUtils.updateSessionRevision();
	}

	/** Runs each test with the security checks on, see {@link TestRunHandleAccessPolicy}. */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> withSecurityChecks(true, () -> {
			super.runTest();
			return null;
		});
	}

	/** The owner reads the transcript neither through the handle nor through the attribute. */
	public void testOwnerReadsNoTranscript() {
		assertNull("The attribute itself is hidden: the grant is in effect.",
			evalAs("r -> $r.get(`tl.ai.execution:AgentExecution#transcript`)", run));
		assertNull(evalAs("h -> aiAgentRunTranscript($h)", run));
		assertNull(evalAs("h -> aiAgentRunTranscript($h)", sessionId()));
	}

	/** The grant on the transcript does not hide the rest of the run from its owner. */
	public void testOwnerReadsTheRestOfTheRun() {
		assertEquals(ExecutionStatus.COMPLETE.name(), evalAs("h -> aiAgentRunStatus($h)", sessionId()));
		assertEquals(ANSWER, evalAs("h -> aiAgentRunResult($h)", sessionId()));
	}

	private static String sessionId() {
		return KBUtils.getWrappedObjectName(run.getSession()).toString();
	}

	/** Evaluates the given TL-Script for the owner, with the security checks on. */
	private static Object evalAs(String script, Object handle) {
		return TLContextManager.<Object, RuntimeException, RuntimeException> inPersonContext(owner,
			() -> TLScriptExecutor.compile(script).executeWith(Args.some(handle)));
	}

	/**
	 * Creates the test suite with the services of {@link TestRunHandleAccessPolicy} and the
	 * configuration next to this class.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(
			new TestSuite(TestRunHandleAttributeRights.class),
			ModelService.Module.INSTANCE,
			AccessManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE,
			AgentExecutionStore.Module.INSTANCE);
		String config = CustomPropertiesDecorator.createFileName(TestRunHandleAttributeRights.class);
		return PersonManagerSetup.createPersonManagerSetup(new CustomPropertiesSetup(test, config, true));
	}
}
