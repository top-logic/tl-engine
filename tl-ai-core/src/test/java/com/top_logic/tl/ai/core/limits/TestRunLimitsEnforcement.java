/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.Date;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.PermissionDecision;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig.RoleBudget;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.util.model.ModelService;

/**
 * What {@link RunLimits} lets through, and what it refuses.
 *
 * <p>
 * Against a real {@link com.top_logic.knowledge.service.KnowledgeBase} with a booted
 * {@link AgentExecutionStore}, because half of this is about rows: the token budget is computed from
 * the account's recorded runs, and the store writes those on its own writer thread at a revision
 * this thread cannot see - which is the whole reason {@code tokensSpentSince} runs there too.
 * </p>
 *
 * <p>
 * {@link RunLimits#admit} is the gate itself; {@code AgentFactory.runAgent} calls it in a
 * try-with-resources around every run and does nothing else with it.
 * </p>
 */
public class TestRunLimitsEnforcement extends BasicTestCase {

	/**
	 * Two accounts for the concurrency tests, which leave nothing behind.
	 *
	 * <p>
	 * The budget tests cannot share them: the suite boots one {@code KnowledgeBase} and the rows a
	 * test records stay there, so each of those asks {@link #freshAccount()} for an account whose
	 * spend is only its own.
	 * </p>
	 */
	private static Person anna;

	private static Person ben;

	/** Makes every {@link #freshAccount()} name unique within the suite. */
	private static int _accounts;

	/** Makes every recorded run's id unique within the suite. */
	private static int _runs;

	private AgentExecutionStore _store;

	/** */
	public static Test suite() {
		Test test = new TestSuite(TestRunLimitsEnforcement.class);
		test = ServiceTestSetup.createSetup(test,
			ModelService.Module.INSTANCE,
			AgentExecutionStore.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_store = AgentExecutionStore.getInstance();
		if (anna == null) {
			anna = TestPerson.createPerson("run-limits-anna");
			ben = TestPerson.createPerson("run-limits-ben");
		}
		RunLimits.resetForTest();
	}

	@Override
	protected void tearDown() throws Exception {
		RunLimits.resetForTest();
		super.tearDown();
	}

	/** Nothing is refused and nothing is counted while no ceiling is configured. */
	public void testNothingIsLimitedByDefault() {
		try (RunLimits.Admission first = RunLimits.admit(runOf(anna));
				RunLimits.Admission second = RunLimits.admit(runOf(anna));
				RunLimits.Admission third = RunLimits.admit(runOf(anna))) {
			assertEquals(3, RunLimits.runningFor(anna));
			assertFalse("With no budget configured, no run is bounded.",
				first.allowance().isLimited());
		}
		assertEquals("Closing an admission frees the slot.", 0, RunLimits.runningFor(anna));
		assertNull("Nothing to refuse without a cap.", RunLimits.concurrencyRefusal(anna));
	}

	/** With a cap of two, the third run of the same account is refused. */
	public void testTheCapRefusesTheRunOverIt() {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(cap(2))) {
			try (RunLimits.Admission first = RunLimits.admit(runOf(anna));
					RunLimits.Admission second = RunLimits.admit(runOf(anna))) {
				assertNotNull("Two are permitted.", second);
				assertNotNull("The pre-check says so before anything is started.",
					RunLimits.concurrencyRefusal(anna));
				try {
					RunLimits.admit(runOf(anna));
					fail("The third run of this account must be refused.");
				} catch (RunLimitExceededException expected) {
					assertTrue("Trying again after a run finishes can work.", expected.isTemporary());
					assertNotNull("The user is told why, in their own language.", expected.reason());
				}
				assertEquals("A refused run holds no slot.", 2, RunLimits.runningFor(anna));
			}
			assertNull("Both finished, so there is room again.", RunLimits.concurrencyRefusal(anna));
		}
	}

	/** The cap is per account: one account's runs never refuse another's. */
	public void testTheCapIsPerAccount() {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(cap(1))) {
			try (RunLimits.Admission annas = RunLimits.admit(runOf(anna))) {
				assertNotNull("Anna is at her cap.", RunLimits.concurrencyRefusal(anna));
				assertNull("Ben has room of his own.", RunLimits.concurrencyRefusal(ben));

				try (RunLimits.Admission bens = RunLimits.admit(runOf(ben))) {
					assertEquals(1, RunLimits.runningFor(anna));
					assertEquals(1, RunLimits.runningFor(ben));
				}
			}
		}
	}

	/**
	 * A delegated run is part of the run that delegated it, so it is not counted again.
	 *
	 * <p>
	 * The rule the ticket asks for. Without it a cap of 3 would refuse the first agent that
	 * delegates three times, which would make a user's ceiling depend on how an agent happens to be
	 * built rather than on what they asked for.
	 * </p>
	 */
	public void testADelegateIsNotASecondRun() {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(cap(1))) {
			ToolContext parent = runOf(anna);
			try (RunLimits.Admission admission = RunLimits.admit(parent)) {
				assertEquals(1, RunLimits.runningFor(anna));

				ToolContext child = parent.forDelegation(TransactionScope.NOOP);
				try (RunLimits.Admission delegated = RunLimits.admit(child)) {
					assertSame("A delegate holds no slot of its own.",
						RunLimits.Admission.NONE, delegated);
					assertEquals("Still one run of this account.", 1, RunLimits.runningFor(anna));

					ToolContext grandchild = child.forDelegation(TransactionScope.NOOP);
					try (RunLimits.Admission deeper = RunLimits.admit(grandchild)) {
						assertSame(RunLimits.Admission.NONE, deeper);
						assertEquals(1, RunLimits.runningFor(anna));
					}
				}
			}
			assertEquals("The one slot the parent held is freed once.", 0, RunLimits.runningFor(anna));
		}
	}

	/** A run that cannot name an account is not attributed to one. */
	public void testARunWithoutAPrincipalIsNotCounted() {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(cap(1))) {
			try (RunLimits.Admission first = RunLimits.admit(ToolContext.unrestricted());
					RunLimits.Admission second = RunLimits.admit(ToolContext.unrestricted())) {
				assertSame(RunLimits.Admission.NONE, first);
				assertSame(RunLimits.Admission.NONE, second);
			}
		}
	}

	/** Closing an admission twice frees one slot, not two. */
	public void testClosingTwiceFreesOneSlot() {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(cap(2))) {
			RunLimits.Admission admission = RunLimits.admit(runOf(anna));
			assertEquals(1, RunLimits.runningFor(anna));
			admission.close();
			admission.close();
			assertEquals(0, RunLimits.runningFor(anna));
		}
	}

	/** The headroom a run gets is its budget minus what the account's recorded runs already cost. */
	public void testTheHeadroomComesFromTheRecordedRuns() {
		Person mine = freshAccount();
		Person other = freshAccount();
		record(mine, new TokenUsage(300, 100, 50, 20, 30));
		record(other, new TokenUsage(9000, 0, 0, 0, 0));

		try (RunLimitSettings.Overridden config = RunLimitSettings.override(budget(1000))) {
			try (RunLimits.Admission admission = RunLimits.admit(runOf(mine))) {
				assertTrue(admission.allowance().isLimited());
				assertEquals("1000 budget minus this account's own 500; the other's spend is not its.",
					500, admission.allowance().headroom());
			}
		}
	}

	/** An account that has spent its budget does not get a run at all. */
	public void testASpentBudgetRefusesTheRun() {
		Person spent = freshAccount();
		record(spent, new TokenUsage(1500, 0, 0, 0, 0));

		try (RunLimitSettings.Overridden config = RunLimitSettings.override(budget(1000))) {
			try {
				RunLimits.admit(runOf(spent));
				fail("A run of an account with nothing left must be refused.");
			} catch (RunLimitExceededException expected) {
				assertFalse("Trying again shortly does not help; the window has to pass.",
					expected.isTemporary());
			}
			assertEquals("A refused run holds no slot.", 0, RunLimits.runningFor(spent));
		}
	}

	/** Runs older than the window do not count against it. */
	public void testOnlyTheWindowCounts() {
		Person account = freshAccount();
		AgentExecution old = record(account, new TokenUsage(5000, 0, 0, 0, 0));
		backdate(old, 48);

		try (RunLimitSettings.Overridden config = RunLimitSettings.override(budget(1000))) {
			try (RunLimits.Admission admission = RunLimits.admit(runOf(account))) {
				assertEquals("Two days ago is outside a 24h window.",
					1000, admission.allowance().headroom());
			}
		}
	}

	/** The role's figure is what the holder is measured against. */
	public void testARoleBudgetApplies() {
		Person account = freshAccount();
		record(account, new TokenUsage(1500, 0, 0, 0, 0));

		RunLimitsConfig config = budget(1000);
		RoleBudget generous =
			com.top_logic.basic.config.TypedConfiguration.newConfigItem(RoleBudget.class);
		generous.update(generous.descriptor().getProperty(RoleBudget.NAME_ATTRIBUTE), "power");
		generous.update(generous.descriptor().getProperty(RoleBudget.TOKENS), Long.valueOf(4000));
		config.getRoleBudgets().put("power", generous);

		try (RunLimitSettings.Overridden overridden = RunLimitSettings.override(config)) {
			try {
				RunLimits.admit(runOf(account, Set.of("tl.user")));
				fail("Without the role, 1500 spent is over the 1000 budget.");
			} catch (RunLimitExceededException expected) {
				// Expected.
			}
			try (RunLimits.Admission admission = RunLimits.admit(runOf(account, Set.of("power")))) {
				assertEquals("4000 for the role, minus the 1500 already spent.",
					2500, admission.allowance().headroom());
			}
		}
	}

	/** What a child run cost counts against the account too - its row is the account's as well. */
	public void testAChildRunsSpendCountsForTheAccount() {
		Person account = freshAccount();
		AgentSession session = _store.createSession(SessionKind.CHAT, account, "child-spend");
		AgentExecution parent = finished(session, "parent", null, new TokenUsage(200, 0, 0, 0, 0));
		finished(session, "child", parent, new TokenUsage(300, 0, 0, 0, 0));
		advance();

		try (RunLimitSettings.Overridden config = RunLimitSettings.override(budget(1000))) {
			try (RunLimits.Admission admission = RunLimits.admit(runOf(account))) {
				assertEquals("Parent and child are two rows and count once each.",
					500, admission.allowance().headroom());
			}
		}
	}

	// ---- helpers ----

	/** An account no other test of this suite has recorded a run for. */
	private static Person freshAccount() {
		return TestPerson.createPerson("run-limits-" + (++_accounts));
	}

	private AgentExecution record(Person owner, TokenUsage usage) {
		AgentSession session = _store.createSession(SessionKind.CHAT, owner, "budget-test");
		AgentExecution execution = finished(session, "test-agent", null, usage);
		advance();
		return execution;
	}

	private AgentExecution finished(AgentSession session, String agent, AgentExecution parent,
			TokenUsage usage) {
		AgentExecution execution = _store.beginExecution(session,
			"run-" + agent + "-" + usage.totalTokens() + "-" + (++_runs),
			agent, "test-model", "test-provider", parent);
		_store.finishExecution(execution, ExecutionStatus.COMPLETE, null, null, usage, null, null);
		return execution;
	}

	/** Moves a recorded run's start out of the window. */
	private void backdate(AgentExecution execution, int hours) {
		Date then = new Date(System.currentTimeMillis() - hours * 3600_000L);
		try (com.top_logic.knowledge.service.Transaction tx =
			execution.tKnowledgeBase().beginTransaction()) {
			execution.setStartedAt(then);
			tx.commit();
		}
		advance();
	}

	private static void advance() {
		HistoryUtils.updateSessionRevision();
	}

	private static ToolContext runOf(Person principal) {
		return runOf(principal, Set.of());
	}

	private static ToolContext runOf(Person principal, Set<String> roles) {
		CancellationToken token = new CancellationToken();
		return new ToolContext(actingAs(principal, roles), ApprovalCallback.AUTO_APPROVE,
			new TransactionScope(token), token);
	}

	/**
	 * A security context that names an account, which is all {@link RunLimits} asks of one.
	 *
	 * <p>
	 * Built here rather than through {@code TopLogicSecurityContext.forRun}, which additionally
	 * resolves the tool policy - a question this test has nothing to say about.
	 * </p>
	 */
	private static SecurityContext actingAs(Person principal, Set<String> roles) {
		com.top_logic.tl.ai.core.security.RunAuthorization authorization =
			new com.top_logic.tl.ai.core.security.RunAuthorization(null, null, null, principal, roles);
		return new SecurityContext() {

			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}

			@Override
			public PermissionDecision checkApproval(Tool tool, ToolCall toolCall) {
				return new PermissionDecision.Allowed();
			}

			@Override
			public com.top_logic.tl.ai.core.security.RunAuthorization runAuthorization() {
				return authorization;
			}
		};
	}

	private static RunLimitsConfig cap(int runs) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.CONCURRENT_RUNS_PER_ACCOUNT),
			Integer.valueOf(runs));
		return config;
	}

	private static RunLimitsConfig budget(long tokens) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.TOKEN_BUDGET), Long.valueOf(tokens));
		return config;
	}
}
