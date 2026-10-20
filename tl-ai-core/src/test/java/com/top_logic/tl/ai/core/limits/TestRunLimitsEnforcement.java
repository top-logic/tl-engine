/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.Date;
import java.util.Locale;
import java.util.OptionalDouble;
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
import com.top_logic.tl.ai.core.agent.environment.RunEnvironment;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.limits.BudgetConfig.RoleBudget;
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
 * this thread cannot see - which is the whole reason {@code spentSince} runs there too.
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
		record(mine, new TokenUsage(300, 150, 50, 20, 30));
		record(other, new TokenUsage(9000, 0, 0, 0, 0));

		try (RunLimitSettings.Overridden config = RunLimitSettings.override(budget(1000))) {
			try (RunLimits.Admission admission = RunLimits.admit(runOf(mine))) {
				assertTrue(admission.allowance().isLimited());
				assertEquals("1000 budget minus this account's own 500; the other's spend is not its.",
					500, admission.allowance().tokenHeadroom());
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
					1000, admission.allowance().tokenHeadroom());
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
					2500, admission.allowance().tokenHeadroom());
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
					500, admission.allowance().tokenHeadroom());
			}
		}
	}

	/**
	 * The money headroom is the money budget minus what the account's recorded runs cost, converted
	 * from the US dollars they record into the system currency.
	 */
	public void testTheMoneyHeadroomComesFromTheRecordedCost() {
		Person mine = freshAccount();
		record(mine, TokenUsage.EMPTY, usd(8.50));
		record(mine, TokenUsage.EMPTY, usd(3.50));
		record(freshAccount(), TokenUsage.EMPTY, usd(200));

		try (RunLimitSettings.Overridden rates = euro(0.5);
				RunLimitSettings.Overridden config = RunLimitSettings.override(money(15))) {
			try (RunLimits.Admission admission = RunLimits.admit(runOf(mine))) {
				BudgetAllowance allowance = admission.allowance();
				assertTrue(allowance.isMoneyLimited());
				assertFalse("No token budget is configured.", allowance.isTokenLimited());
				assertEquals("The budget is stated in the system currency.", "EUR",
					allowance.moneyBudget().currency());
				assertEquals("15 EUR minus this account's own 12 USD at 0.5.", 9.0,
					allowance.moneyBudget().headroom(), 1e-9);
				assertEquals("The run's own costs count at today's rate.", 0.5,
					allowance.moneyBudget().usdRate(), 1e-9);
			}
		}
	}

	/**
	 * An account whose runs cost its money budget is refused, and the refusal names what it spent and
	 * what it may spend, as amounts of the system currency in the account's own language.
	 */
	public void testASpentMoneyBudgetRefusesTheRunWithTheAmounts() {
		Person spent = freshAccount();
		record(spent, TokenUsage.EMPTY, usd(24.80));
		record(spent, TokenUsage.EMPTY, usd(6));

		try (RunLimitSettings.Overridden rates = euro(0.5);
				RunLimitSettings.Overridden config = RunLimitSettings.override(money(15))) {
			try {
				RunLimits.admit(runOf(spent));
				fail("A run of an account whose runs cost 15.40 EUR of 15 EUR must be refused.");
			} catch (RunLimitExceededException expected) {
				assertFalse(expected.isTemporary());
				assertEquals(I18NConstants.MONEY_BUDGET_SPENT__SPENT_BUDGET_HOURS.fill("", "", 0).getKey(),
					expected.reason().getKey());
				Locale locale = RunEnvironment.localeOf(spent);
				Object[] arguments = expected.reason().arguments();
				assertEquals(new Money(15.40, "EUR").format(locale), arguments[0]);
				assertEquals(new Money(15, "EUR").format(locale), arguments[1]);
				assertEquals(Integer.valueOf(24), arguments[2]);
			}
			assertEquals("A refused run holds no slot.", 0, RunLimits.runningFor(spent));
		}
	}

	/**
	 * Every run counts at the rate of the day it started: a rate entered later does not change what
	 * an earlier run cost.
	 */
	public void testEachRunCountsAtTheRateOfItsDay() {
		Person account = freshAccount();
		backdate(record(account, TokenUsage.EMPTY, usd(10)), 48);
		record(account, TokenUsage.EMPTY, usd(10));
		Date yesterday = new Date(System.currentTimeMillis() - 24 * 3600_000L);

		RunLimitsConfig config = money(20);
		config.update(config.descriptor().getProperty(RunLimitsConfig.BUDGET_WINDOW_HOURS), Integer.valueOf(72));
		try (RunLimitSettings.Overridden rates = CostConversion.override("EUR",
			day -> OptionalDouble.of(day.before(yesterday) ? 0.5 : 0.9));
				RunLimitSettings.Overridden overridden = RunLimitSettings.override(config)) {
			try (RunLimits.Admission admission = RunLimits.admit(runOf(account))) {
				assertEquals("20 EUR minus 10 USD at the old 0.5 and 10 USD at today's 0.9.", 6.0,
					admission.allowance().moneyBudget().headroom(), 1e-9);
			}
		}
	}

	/**
	 * The sum reads only what can lie in the window - not the runs of a conversation nobody touched
	 * since, nor those of an active conversation that started before it - and comes out as the sum
	 * over every run that started in it, each at the rate of its day.
	 */
	public void testTheSpendCountsOnlyRunsThatStartedInTheWindow() {
		Person account = freshAccount();
		AgentExecution quiet = record(account, new TokenUsage(1000, 0, 0, 0, 0), usd(1));
		backdate(quiet, 48);
		backdateSession(quiet.getSession(), 48);
		AgentSession active = _store.createSession(SessionKind.CHAT, account, "budget-test");
		AgentExecution before = finished(active, "test-agent", null, new TokenUsage(2000, 0, 0, 0, 0), usd(2));
		advance();
		backdate(before, 48);
		finished(active, "test-agent", null, new TokenUsage(300, 0, 0, 0, 0), usd(3));
		advance();

		Date yesterday = new Date(System.currentTimeMillis() - 24 * 3600_000L);
		try (RunLimitSettings.Overridden rates = CostConversion.override("EUR",
			day -> OptionalDouble.of(day.before(yesterday) ? 0.5 : 0.9))) {
			AgentExecutionStore.AccountSpend window = _store.spentSince(account, yesterday);
			assertEquals("Only the run of today counts.", 300, window.tokens());
			assertEquals("3 USD at today's 0.9.", 2.7, window.cost(), 1e-9);
			assertTrue(window.complete());

			AgentExecutionStore.AccountSpend all = _store.spentSince(account, null);
			assertEquals("Without a window every run counts, the quiet conversation's too.", 3300, all.tokens());
			assertEquals("1 and 2 USD at the old 0.5, 3 USD at today's 0.9.", 4.2, all.cost(), 1e-9);
			assertTrue(all.complete());
		}
	}

	/**
	 * Without a rate nobody is refused for a sum nobody could convert: the money budget is not
	 * checked, the log is told, and the token budget still applies.
	 */
	public void testWithoutARateTheMoneyBudgetIsNotChecked() {
		Person account = freshAccount();
		record(account, new TokenUsage(2000, 0, 0, 0, 0), usd(100));

		try (RunLimitSettings.Overridden rates = CostConversion.override("EUR", day -> OptionalDouble.empty());
				RunLimitSettings.Overridden config = RunLimitSettings.override(money(15))) {
			try (RunLimits.Admission admission = RunLimits.admit(runOf(account))) {
				assertFalse("100 USD cannot be told in euros, so the money budget is not checked.",
					admission.allowance().isMoneyLimited());
			}
			assertFalse("The run start told the log already; the next warning waits for the interval.",
				CostConversion.warnNoRate("a probe of the test"));
			assertNull(RunLimits.budgetRefusal(account, Set.of()));
		}

		RunLimitsConfig both = money(15);
		both.update(both.descriptor().getProperty(RunLimitsConfig.TOKEN_BUDGET), Long.valueOf(1000));
		try (RunLimitSettings.Overridden rates = CostConversion.override("EUR", day -> OptionalDouble.empty());
				RunLimitSettings.Overridden config = RunLimitSettings.override(both)) {
			try {
				RunLimits.admit(runOf(account));
				fail("The token budget is checked without a rate.");
			} catch (RunLimitExceededException expected) {
				assertEquals(I18NConstants.TOKEN_BUDGET_SPENT__HOURS.fill(0).getKey(), expected.reason().getKey());
			}
		}
	}

	/** A system currency that is US dollars needs no rate: the recorded dollars count as they are. */
	public void testAUsdSystemCurrencyNeedsNoRate() {
		Person account = freshAccount();
		record(account, TokenUsage.EMPTY, usd(16));

		try (RunLimitSettings.Overridden rates = CostConversion.override("USD", day -> OptionalDouble.empty());
				RunLimitSettings.Overridden config = RunLimitSettings.override(money(15))) {
			try {
				RunLimits.admit(runOf(account));
				fail("16 USD of a 15 USD budget are spent.");
			} catch (RunLimitExceededException expected) {
				assertEquals(I18NConstants.MONEY_BUDGET_SPENT__SPENT_BUDGET_HOURS.fill("", "", 0).getKey(),
					expected.reason().getKey());
			}
		}
	}

	/** A role names a money figure of its own, and a role that names none leaves the section's. */
	public void testARoleMoneyBudgetApplies() {
		Person account = freshAccount();
		record(account, TokenUsage.EMPTY, usd(40));

		RunLimitsConfig config = money(15);
		config.getRoleBudgets().put("power", roleBudget("power", null, Double.valueOf(50)));
		config.getRoleBudgets().put("tokens-only", roleBudget("tokens-only", Long.valueOf(1_000_000), null));

		try (RunLimitSettings.Overridden rates = euro(0.5);
				RunLimitSettings.Overridden overridden = RunLimitSettings.override(config)) {
			try {
				RunLimits.admit(runOf(account, Set.of("tokens-only")));
				fail("A role that names only tokens says nothing about money: 20 EUR is over the 15 budget.");
			} catch (RunLimitExceededException expected) {
				// Expected.
			}
			try (RunLimits.Admission admission = RunLimits.admit(runOf(account, Set.of("power")))) {
				assertEquals("50 for the role, minus the 20 already spent.", 30.0,
					admission.allowance().moneyBudget().headroom(), 1e-9);
			}
		}
	}

	/**
	 * The chat's pre-check names the spent money budget before anything is written down, and says
	 * nothing while there is headroom or no budget at all.
	 */
	public void testThePreCheckNamesASpentMoneyBudget() {
		Person account = freshAccount();
		record(account, TokenUsage.EMPTY, usd(32));

		try (RunLimitSettings.Overridden rates = euro(0.5)) {
			assertNull("No budget configured, nothing to refuse.", RunLimits.budgetRefusal(account, Set.of()));
			try (RunLimitSettings.Overridden config = RunLimitSettings.override(money(15))) {
				assertEquals(I18NConstants.MONEY_BUDGET_SPENT__SPENT_BUDGET_HOURS.fill("", "", 0).getKey(),
					RunLimits.budgetRefusal(account, Set.of()).getKey());
				assertEquals("The pre-check takes no slot.", 0, RunLimits.runningFor(account));
			}
			try (RunLimitSettings.Overridden config = RunLimitSettings.override(money(20))) {
				assertNull("4 EUR are left.", RunLimits.budgetRefusal(account, Set.of()));
			}
		}
	}

	/** The token and the money budget apply together: either one refuses. */
	public void testTokenAndMoneyBudgetsApplyTogether() {
		Person account = freshAccount();
		record(account, new TokenUsage(2000, 0, 0, 0, 0), usd(1));

		RunLimitsConfig config = money(15);
		config.update(config.descriptor().getProperty(RunLimitsConfig.TOKEN_BUDGET), Long.valueOf(1000));
		try (RunLimitSettings.Overridden rates = euro(0.5);
				RunLimitSettings.Overridden overridden = RunLimitSettings.override(config)) {
			try {
				RunLimits.admit(runOf(account));
				fail("The token budget is spent, whatever money is left.");
			} catch (RunLimitExceededException expected) {
				assertEquals(I18NConstants.TOKEN_BUDGET_SPENT__HOURS.fill(0).getKey(), expected.reason().getKey());
			}
		}
	}

	/** After a run stopped on the money budget, the notice names the amounts; otherwise the token notice. */
	public void testTheStopNoticeNamesTheBudgetThatRanOut() {
		Person account = freshAccount();
		record(account, TokenUsage.EMPTY, usd(32));

		try (RunLimitSettings.Overridden rates = euro(0.5)) {
			try (RunLimitSettings.Overridden config = RunLimitSettings.override(money(15))) {
				assertEquals(I18NConstants.RUN_STOPPED_MONEY_BUDGET_SPENT__SPENT_BUDGET_HOURS.fill("", "", 0).getKey(),
					RunLimits.budgetStopNotice(actingAs(account, Set.of())).getKey());
			}
			try (RunLimitSettings.Overridden config = RunLimitSettings.override(budget(10))) {
				assertEquals(I18NConstants.RUN_STOPPED_BUDGET_SPENT__HOURS.fill(0).getKey(),
					RunLimits.budgetStopNotice(actingAs(account, Set.of())).getKey());
			}
		}
	}

	/**
	 * The checks of one send share one reading of the spend (#835): the run start takes over what the
	 * pre-check read rather than reading the account's runs again, and only once.
	 */
	public void testTheRunStartTakesOverTheSendsReading() {
		Person account = freshAccount();
		record(account, new TokenUsage(500, 0, 0, 0, 0));

		try (RunLimitSettings.Overridden config = RunLimitSettings.override(budget(1000))) {
			RunLimits.SpendReading reading = RunLimits.spendReading(account);
			// What the send's pre-check reads.
			reading.spent(RunLimitSettings.budgetWindowHours());
			record(account, new TokenUsage(300, 0, 0, 0, 0));

			try (RunLimits.Reuse reuse = RunLimits.reuse(reading)) {
				try (RunLimits.Admission admission = RunLimits.admit(runOf(account))) {
					assertEquals("The run start uses the send's reading, not a second pass.",
						500, admission.allowance().tokenHeadroom());
				}
				try (RunLimits.Admission next = RunLimits.admit(runOf(account))) {
					assertEquals("A reading serves one run start; the next one reads again.",
						200, next.allowance().tokenHeadroom());
				}
			}
		}
	}

	/** A reading taken for one account is not used for a run of another. */
	public void testAReadingOfAnotherAccountIsNotUsed() {
		Person reader = freshAccount();
		Person runner = freshAccount();
		record(runner, new TokenUsage(400, 0, 0, 0, 0));

		try (RunLimitSettings.Overridden config = RunLimitSettings.override(budget(1000))) {
			RunLimits.SpendReading reading = RunLimits.spendReading(reader);
			reading.spent(RunLimitSettings.budgetWindowHours());

			try (RunLimits.Reuse reuse = RunLimits.reuse(reading);
					RunLimits.Admission admission = RunLimits.admit(runOf(runner))) {
				assertEquals(600, admission.allowance().tokenHeadroom());
			}
		}
	}

	// ---- helpers ----

	/** An account no other test of this suite has recorded a run for. */
	private static Person freshAccount() {
		return TestPerson.createPerson("run-limits-" + (++_accounts));
	}

	private AgentExecution record(Person owner, TokenUsage usage) {
		return record(owner, usage, null);
	}

	private AgentExecution record(Person owner, TokenUsage usage, Money cost) {
		AgentSession session = _store.createSession(SessionKind.CHAT, owner, "budget-test");
		AgentExecution execution = finished(session, "test-agent", null, usage, cost);
		advance();
		return execution;
	}

	private AgentExecution finished(AgentSession session, String agent, AgentExecution parent,
			TokenUsage usage) {
		return finished(session, agent, parent, usage, null);
	}

	private AgentExecution finished(AgentSession session, String agent, AgentExecution parent,
			TokenUsage usage, Money cost) {
		AgentExecution execution = _store.beginExecution(session,
			"run-" + agent + "-" + usage.totalTokens() + "-" + (++_runs),
			agent, "test-model", "test-provider", parent);
		_store.finishExecutionWithLatestTranscript(execution, ExecutionStatus.COMPLETE, null, null, usage, cost,
			() -> null, null);
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

	/** Moves a conversation's last activity out of the window, as if nobody had touched it since. */
	private void backdateSession(AgentSession session, int hours) {
		Date then = new Date(System.currentTimeMillis() - hours * 3600_000L);
		try (com.top_logic.knowledge.service.Transaction tx =
			session.tKnowledgeBase().beginTransaction()) {
			session.setLastUpdated(then);
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

	private static RunLimitsConfig money(double amount) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.MONEY_BUDGET), Double.valueOf(amount));
		return config;
	}

	/** The system currency euro, with the given rate on every day. */
	private static RunLimitSettings.Overridden euro(double rate) {
		return CostConversion.override("EUR", day -> OptionalDouble.of(rate));
	}

	private static Money usd(double amount) {
		return new Money(amount, Money.USD);
	}

	private static RoleBudget roleBudget(String role, Long tokens, Double money) {
		RoleBudget budget = com.top_logic.basic.config.TypedConfiguration.newConfigItem(RoleBudget.class);
		budget.update(budget.descriptor().getProperty(RoleBudget.NAME_ATTRIBUTE), role);
		budget.update(budget.descriptor().getProperty(RoleBudget.TOKENS), tokens);
		budget.update(budget.descriptor().getProperty(RoleBudget.MONEY), money);
		return budget;
	}

	private static RunLimitsConfig budget(long tokens) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.TOKEN_BUDGET), Long.valueOf(tokens));
		return config;
	}
}
