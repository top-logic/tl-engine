/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tests for {@link UserInteractionStore}, in particular that a pending approval is discoverable
 * through {@link UserInteractionStore#getPendingApprovals()} and answerable through
 * {@link UserInteractionStore#resolve(String, String)} without the caller having observed the
 * event that announced it.
 */
public class TestUserInteractionStore extends TestCase {

	private static final long TIMEOUT_MS = 5000;

	private UserInteractionStore _store;

	@Override
	protected void setUp() {
		_store = new UserInteractionStore();
	}

	/**
	 * The reported defect: a registered approval must be discoverable by a UI that only has the
	 * store, and resolving it must release the thread parked on its future.
	 */
	public void testApprovalIsDiscoverableAndResolvable() throws Exception {
		ApprovalRequest request = approvalRequest("approval-1", "deleteObject");

		assertFalse(_store.hasPendingApprovals());
		CompletableFuture<String> future = _store.registerApproval(request);
		assertTrue(_store.hasPendingApprovals());

		List<PendingApproval> pending = _store.getPendingApprovals();
		assertEquals(1, pending.size());
		PendingApproval approval = pending.get(0);
		assertEquals("approval-1", approval.id());
		assertSame("the full request must be handed out, not just its ID",
			request, approval.request());
		assertEquals("deleteObject", approval.request().toolName());
		assertEquals(Map.of("id", "42"), approval.request().arguments());
		assertEquals(ToolMode.STRUCTURAL, approval.request().mode());
		assertTrue(approval.createdAt() > 0);

		// A UI that discovered the approval this way can answer it.
		_store.resolve(approval.id(), "APPROVED");

		assertEquals("APPROVED", future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS));
		assertFalse(_store.hasPendingApprovals());
		assertTrue(_store.getPendingApprovals().isEmpty());
	}

	/** Resolving one approval must leave the others pending. */
	public void testApprovalsAreIndependent() throws Exception {
		CompletableFuture<String> first = _store.registerApproval(approvalRequest("a-1", "one"));
		CompletableFuture<String> second = _store.registerApproval(approvalRequest("a-2", "two"));

		assertEquals(2, _store.getPendingApprovals().size());

		_store.resolve("a-2", "REJECTED:not now");

		assertEquals("REJECTED:not now", second.get(TIMEOUT_MS, TimeUnit.MILLISECONDS));
		assertFalse(first.isDone());
		assertEquals(List.of("a-1"), _store.getPendingApprovals().stream().map(PendingApproval::id).toList());
	}

	/**
	 * A caller that gives up on its own drops the registration, so a timed-out approval does not
	 * stay visible as a phantom.
	 */
	public void testDiscardedApprovalDisappears() {
		CompletableFuture<String> future = _store.registerApproval(approvalRequest("a-1", "one"));

		_store.discardApproval("a-1");

		assertFalse(_store.hasPendingApprovals());
		assertFalse("discarding must not pretend the user answered", future.isDone());

		// Discarding twice, or discarding an unknown ID, is a no-op.
		_store.discardApproval("a-1");
		_store.discardApproval("unknown");
	}

	/** A question must behave exactly as before: same accessor, same resolution, unaffected by approvals. */
	public void testQuestionIsUnaffected() throws Exception {
		UserInteractionRequest question =
			_store.createRequest("Which one?", "Choose", "text", null, false);

		assertTrue(_store.hasPending());
		assertEquals(List.of(question), _store.getPendingRequests());

		// Registering an approval must not show up among the interaction requests.
		_store.registerApproval(approvalRequest("a-1", "one"));
		assertEquals("an approval must not leak into getPendingRequests()",
			List.of(question), _store.getPendingRequests());

		_store.resolve(question.getId(), "the answer");

		assertEquals("the answer", question.getFuture().get(TIMEOUT_MS, TimeUnit.MILLISECONDS));
		assertFalse(_store.hasPending());
		assertTrue(_store.getPendingRequests().isEmpty());
		assertEquals("resolving a question must not touch the approvals",
			1, _store.getPendingApprovals().size());
	}

	/** {@code hasPending()} keeps its meaning: it answers for questions only. */
	public void testHasPendingCoversQuestionsOnly() {
		_store.registerApproval(approvalRequest("a-1", "one"));

		assertFalse("hasPending() must keep reporting on interaction requests only",
			_store.hasPending());
		assertTrue(_store.hasPendingApprovals());
	}

	/** Resolving an unknown ID is a silent no-op, as before. */
	public void testResolveUnknownId() {
		_store.resolve("nothing-registered-under-this-id", "APPROVED");
	}

	/**
	 * The whole point, end to end: an agent thread parked in an {@link ApprovalCallback} is
	 * released by a UI that knows nothing but the store - it discovers the approval through
	 * {@link UserInteractionStore#getPendingApprovals()} and answers it there.
	 */
	public void testBlockedAgentThreadIsReleasedByStoreOnlyUi() throws Exception {
		ApprovalRequest request = approvalRequest("a-1", "deleteObject");

		// The bridge the chat UI installs: register, then block until the user responds.
		ApprovalCallback callback = req -> {
			CompletableFuture<String> future = _store.registerApproval(req);
			try {
				return "APPROVED".equals(future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS))
					? new ApprovalResult.Approved()
					: new ApprovalResult.Rejected("rejected");
			} catch (Exception ex) {
				return new ApprovalResult.TimedOut();
			} finally {
				_store.discardApproval(req.approvalRequestId());
			}
		};

		AtomicReference<ApprovalResult> result = new AtomicReference<>();
		Thread agentThread = new Thread(() -> result.set(callback.requestApproval(request)), "agent");
		agentThread.start();

		// The UI side: no event, no log, just the store.
		PendingApproval approval = awaitApproval();
		assertEquals("deleteObject", approval.request().toolName());
		_store.resolve(approval.id(), "APPROVED");

		agentThread.join(TIMEOUT_MS);
		assertFalse("the agent thread must not still be parked on the approval",
			agentThread.isAlive());
		assertTrue("the approval must have been granted, not timed out: " + result.get(),
			result.get() instanceof ApprovalResult.Approved);
		assertTrue(_store.getPendingApprovals().isEmpty());
	}

	private PendingApproval awaitApproval() throws InterruptedException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			List<PendingApproval> pending = _store.getPendingApprovals();
			if (!pending.isEmpty()) {
				return pending.get(0);
			}
			Thread.sleep(10);
		}
		throw new AssertionError("the approval never became visible in getPendingApprovals()");
	}

	private static ApprovalRequest approvalRequest(String approvalRequestId, String toolName) {
		return new ApprovalRequest(approvalRequestId, toolName, "call-" + approvalRequestId,
			Map.of("id", "42"), ToolMode.STRUCTURAL, "Tool '" + toolName + "' changes the model structure",
			Duration.ofSeconds(120));
	}
}
