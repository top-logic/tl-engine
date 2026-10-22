/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A background subagent's approval waits in its job's store, not in the store of the run that started
 * it (issue #695).
 *
 * <p>
 * The subagent inherits the chat run's approval callback. That callback used to close over the run's
 * own store, so an approval the subagent needed after the run's turn ended was parked in a store the
 * chat had already let go of: shown, perhaps, but answered by nobody.
 * </p>
 */
public class TestSubagentApprovalStore extends BasicTestCase {

	private static Person account;

	private Person _previous;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (account == null) {
			account = TestPerson.createPerson("subagent-approval-account");
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

	/** The approval waits where the subagent's thread says, and the answer there unparks it. */
	public void testASubagentsApprovalWaitsInItsJobsStore() throws Exception {
		UserInteractionStore runStore = new UserInteractionStore();
		TaskProgressTracker tracker = new TaskProgressTracker();
		tracker.setApprovalMode(ApprovalMode.MANUAL);
		ToolContext context =
			ChatProcessor.buildToolContext(runStore, tracker, tracker.activeChat(), new CancellationToken());
		SubagentJob job = new SubagentJob("sa-1", "worker", "do x", new CancellationToken());

		CompletableFuture<ApprovalResult> decided = CompletableFuture.supplyAsync(() -> {
			try (UserInteractionStoreBinding.Binding bound =
				UserInteractionStoreBinding.bind(job.interactionStore())) {
				return context.approval().requestApproval(new ApprovalRequest("ap-1", "createObject",
					"call-1", Map.of(), ToolMode.DATA_WRITE, "Creates a contract.", Duration.ofMinutes(1)));
			}
		});

		long deadline = System.currentTimeMillis() + 10_000;
		while (!job.interactionStore().isPending("ap-1") && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertTrue("The approval must wait in the subagent's own store.",
			job.interactionStore().isPending("ap-1"));
		assertFalse("The run's store is let go of when its turn ends; nothing may wait there.",
			runStore.isPending("ap-1"));

		job.interactionStore().resolve("ap-1", "APPROVED");

		assertTrue(decided.get(5, TimeUnit.SECONDS) instanceof ApprovalResult.Approved);
	}

	/** On the run's own thread nothing changes: the approval waits in the run's store. */
	public void testTheRunsOwnApprovalWaitsInTheRunsStore() throws Exception {
		UserInteractionStore runStore = new UserInteractionStore();
		TaskProgressTracker tracker = new TaskProgressTracker();
		tracker.setApprovalMode(ApprovalMode.MANUAL);
		ToolContext context =
			ChatProcessor.buildToolContext(runStore, tracker, tracker.activeChat(), new CancellationToken());

		CompletableFuture<ApprovalResult> decided = CompletableFuture.supplyAsync(() -> {
			try (UserInteractionStoreBinding.Binding bound = UserInteractionStoreBinding.bind(runStore)) {
				return context.approval().requestApproval(new ApprovalRequest("ap-2", "createObject",
					"call-2", Map.of(), ToolMode.DATA_WRITE, "Creates a contract.", Duration.ofMinutes(1)));
			}
		});

		long deadline = System.currentTimeMillis() + 10_000;
		while (!runStore.isPending("ap-2") && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		runStore.resolve("ap-2", "REJECTED");

		assertTrue(decided.get(5, TimeUnit.SECONDS) instanceof ApprovalResult.Rejected);
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSubagentApprovalStore.class),
				ModelService.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE));
	}
}
