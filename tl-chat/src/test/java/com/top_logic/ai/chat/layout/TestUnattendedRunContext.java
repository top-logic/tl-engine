/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * What a run gets when its tracker says there is nobody to ask (issue #384).
 *
 * <p>
 * The chat builds one {@code ToolContext} for every run it starts, whoever started it, so this is
 * the one place where "a scheduled run is unattended" turns into something the agent loop can act
 * on. Two things follow from it, and they are deliberately two: the run loses the approval prompt
 * as its bound and gains the policy's background ceiling instead, and {@code askUser} stops being a
 * thing it can do.
 * </p>
 *
 * <p>
 * The other direction matters as much: a user who switches <em>their own</em> chat to auto-approval
 * is still sitting in front of it. The flag is therefore set by the entry point that knows there is
 * no window, never derived from the approval mode here.
 * </p>
 */
public class TestUnattendedRunContext extends BasicTestCase {

	private static Person account;

	private Person _previous;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (account == null) {
			account = TestPerson.createPerson("unattended-run-account");
		}
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previous = subSession.getPerson();
		subSession.setPerson(account);
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previous);
		super.tearDown();
	}

	/** A chat window's run may ask its user, and is bounded by the prompt it can show. */
	public void testAWindowsRunIsAttended() {
		ToolContext context = contextOf(newTracker(ApprovalMode.MANUAL, false));

		assertTrue("There is a person in front of this chat.", context.attended());
		assertFalse("An attended run is bounded by its approval prompt, not by the allowlist.",
			context.security().isBackgroundRun());
	}

	/**
	 * Auto-approval alone does not make a run unattended.
	 *
	 * <p>
	 * The user chose not to confirm each tool call; they did not leave. Deriving "nobody is here"
	 * from that setting would silently take {@code askUser} away from every chat that uses it.
	 * </p>
	 */
	public void testAutoApprovalInAWindowStaysAttended() {
		ToolContext context = contextOf(newTracker(ApprovalMode.AUTO_APPROVE, false));

		assertTrue(context.attended());
		assertFalse(context.security().isBackgroundRun());
	}

	/** A run whose tracker has no window gets both consequences of having nobody. */
	public void testScheduledRunIsUnattendedAndBounded() {
		ToolContext context = contextOf(newTracker(ApprovalMode.AUTO_APPROVE, true));

		assertFalse("askUser has to fail rather than park the scheduler's thread.",
			context.attended());
		assertTrue("Losing the approval prompt is what the background allowlist compensates for.",
			context.security().isBackgroundRun());
	}

	private static ToolContext contextOf(TaskProgressTracker tracker) {
		return ChatProcessor.buildToolContext(new UserInteractionStore(), tracker, tracker.activeChat(),
			new CancellationToken());
	}

	private static TaskProgressTracker newTracker(ApprovalMode mode, boolean unattended) {
		TaskProgressTracker tracker = new TaskProgressTracker();
		tracker.setApprovalMode(mode);
		tracker.setUnattended(unattended);
		return tracker;
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestUnattendedRunContext.class),
				ModelService.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE));
	}
}
