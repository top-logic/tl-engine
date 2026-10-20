/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;
import com.top_logic.tl.ai.core.security.layout.PolicyMatrix.RowState;

/**
 * Undo and redo within one editing of the policy (#404).
 */
public class TestPolicyEditSession extends TestCase {

	private static final String ROLE = "ticketReader";

	private static final String MODEL = "claude-haiku";

	/** The draft copies configuration items, which resolve their types through the type index. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestPolicyEditSession.class)));
	}

	/** Every step goes back, and forward again, in order. */
	public void testUndoAndRedo() {
		PolicyEditSession session = session();
		session.edit(() -> matrix(session).setRowState(ROLE, RowState.NAMED));
		session.edit(() -> matrix(session).setCell(ROLE, MODEL, CellState.GRANTED));
		assertEquals(CellState.GRANTED, matrix(session).cell(ROLE, MODEL));

		session.undo();
		assertEquals(CellState.ABSENT, matrix(session).cell(ROLE, MODEL));
		assertEquals(RowState.NAMED, matrix(session).rowState(ROLE));

		session.undo();
		assertEquals(RowState.NOT_NAMED, matrix(session).rowState(ROLE));
		assertFalse("Back where it started.", session.draft().isModified());
		assertFalse(session.canUndo());

		session.redo();
		session.redo();
		assertEquals(CellState.GRANTED, matrix(session).cell(ROLE, MODEL));
		assertFalse(session.canRedo());
	}

	/** An edit that changes nothing is not a step. */
	public void testANoOpIsNotAStep() {
		PolicyEditSession session = session();
		int revision = session.revision();
		session.edit(() -> matrix(session).setRowState(ROLE, RowState.NOT_NAMED));

		assertFalse(session.canUndo());
		assertEquals(revision, session.revision());
	}

	/** A new edit after an undo forgets what could have been redone. */
	public void testANewEditDropsTheRedo() {
		PolicyEditSession session = session();
		session.edit(() -> matrix(session).setRowState(ROLE, RowState.NAMED));
		session.undo();
		assertTrue(session.canRedo());

		session.edit(() -> session.draft().setRunRestricted(Dimension.AGENT, ROLE, true));
		assertFalse(session.canRedo());
		assertEquals(RowState.NOT_NAMED, matrix(session).rowState(ROLE));
	}

	/** An undo step is a snapshot: editing after an undo leaves the snapshots alone. */
	public void testStepsAreSnapshots() {
		PolicyEditSession session = session();
		session.edit(() -> matrix(session).setRowState(ROLE, RowState.NAMED));
		session.edit(() -> matrix(session).setCell(ROLE, MODEL, CellState.DENIED));
		session.undo();
		session.edit(() -> matrix(session).setCell(ROLE, MODEL, CellState.GRANTED));
		session.undo();

		assertEquals(CellState.ABSENT, matrix(session).cell(ROLE, MODEL));
		assertEquals(List.of(), session.draft().runNames(Dimension.MODEL, ROLE, true));
	}

	/** Starting over forgets every step and every refusal. */
	public void testResetForgetsTheHistory() {
		PolicyEditSession session = session();
		session.edit(() -> matrix(session).setRowState(ROLE, RowState.NAMED));
		session.setRefusals(List.of(I18NConstants.REFUSE_BACKGROUND_ALLOWLIST_UNDECLARED));

		session.reset(draft());

		assertFalse(session.canUndo());
		assertTrue(session.refusals().isEmpty());
		assertEquals(RowState.NOT_NAMED, matrix(session).rowState(ROLE));
	}

	/** The history is bounded; the oldest steps are forgotten. */
	public void testTheHistoryIsBounded() {
		PolicyEditSession session = session();
		for (int n = 0; n < PolicyEditSession.MAX_STEPS + 5; n++) {
			boolean named = n % 2 == 0;
			session.edit(() -> matrix(session).setRowState(ROLE, named ? RowState.NAMED : RowState.NOT_NAMED));
		}
		int steps = 0;
		while (session.canUndo()) {
			session.undo();
			steps++;
		}
		assertEquals(PolicyEditSession.MAX_STEPS, steps);
	}

	private static PolicyMatrix matrix(PolicyEditSession session) {
		return new PolicyMatrix(session.draft(), MatrixDimension.MODELS);
	}

	private static PolicyEditSession session() {
		return new PolicyEditSession(draft());
	}

	private static PolicyDraft draft() {
		return new PolicyDraft(TypedConfiguration.newConfigItem(AiToolAccessService.Config.class));
	}
}
