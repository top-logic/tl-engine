/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * One administrator's editing of the tool access policy: the draft, the steps that can be undone and
 * redone, and why the last apply was refused.
 *
 * <p>
 * Shared by the four matrices of the policy editor, so that a change on one sub-tab is part of the
 * same draft, the same undo history and the same apply as a change on another.
 * </p>
 */
/*
 * Issue #404. The engine's edit session knows discard and apply and nothing in between, so the steps
 * are kept here: a snapshot of the whole draft before every change. A draft is a copy of one service
 * configuration of a few dozen entries, so a snapshot is cheap, and a snapshot cannot get out of step
 * with the draft the way a list of inverse operations can.
 */
public class PolicyEditSession {

	/** How many steps can be undone; older ones are forgotten. */
	static final int MAX_STEPS = 100;

	private static final Property<PolicyEditSession> SESSION =
		TypedAnnotatable.property(PolicyEditSession.class, "aiToolAccessPolicyEditSession");

	private PolicyDraft _draft;

	private final Deque<PolicyDraft> _undo = new ArrayDeque<>();

	private final Deque<PolicyDraft> _redo = new ArrayDeque<>();

	private List<ResKey> _refusals = List.of();

	private int _revision;

	/**
	 * Creates a {@link PolicyEditSession}.
	 *
	 * @param draft
	 *        The policy to edit, or <code>null</code> if the saved one cannot be read.
	 */
	public PolicyEditSession(PolicyDraft draft) {
		_draft = draft;
	}

	/**
	 * The editing session of the user the given component is shown to.
	 *
	 * <p>
	 * Kept on the main layout, which exists once per user session, so that the four sub-tabs find the
	 * same one.
	 * </p>
	 */
	public static PolicyEditSession of(LayoutComponent component) {
		MainLayout mainLayout = component.getMainLayout();
		if (mainLayout == null) {
			// Not placed in an application window, e.g. built by a test from a bare configuration.
			PolicyEditSession own = component.get(SESSION);
			if (own == null) {
				own = new PolicyEditSession(PolicyDraft.ofSavedPolicy());
				component.set(SESSION, own);
			}
			return own;
		}
		PolicyEditSession session = mainLayout.get(SESSION);
		if (session == null) {
			session = new PolicyEditSession(PolicyDraft.ofSavedPolicy());
			mainLayout.set(SESSION, session);
		}
		return session;
	}

	/** The policy being edited, or <code>null</code> if the saved one cannot be read. */
	public PolicyDraft draft() {
		return _draft;
	}

	/**
	 * Counts every change of the draft, so that a view can tell whether what it shows is still current.
	 */
	public int revision() {
		return _revision;
	}

	/**
	 * Makes one change that can be undone.
	 *
	 * @param change
	 *        Changes the {@link #draft()}.
	 */
	public void edit(Runnable change) {
		PolicyDraft before = _draft.copy();
		change.run();
		if (_draft.sameAs(before)) {
			return;
		}
		_undo.push(before);
		if (_undo.size() > MAX_STEPS) {
			_undo.removeLast();
		}
		_redo.clear();
		changed();
	}

	/** Whether there is a step to undo. */
	public boolean canUndo() {
		return !_undo.isEmpty();
	}

	/** Whether there is an undone step to redo. */
	public boolean canRedo() {
		return !_redo.isEmpty();
	}

	/** Goes back one step, if there is one. */
	public void undo() {
		if (_undo.isEmpty()) {
			return;
		}
		_redo.push(_draft);
		_draft = _undo.pop();
		changed();
	}

	/** Goes forward one undone step, if there is one. */
	public void redo() {
		if (_redo.isEmpty()) {
			return;
		}
		_undo.push(_draft);
		_draft = _redo.pop();
		changed();
	}

	/**
	 * Starts over from the given draft, forgetting every step.
	 *
	 * <p>
	 * After an apply, a discard or a restart: what could be undone was a change of a policy that is no
	 * longer the one being edited.
	 * </p>
	 */
	public void reset(PolicyDraft draft) {
		_draft = draft;
		_undo.clear();
		_redo.clear();
		_refusals = List.of();
		changed();
	}

	/** Why the last apply was refused, empty when the last one succeeded or none has run. */
	public List<ResKey> refusals() {
		return _refusals;
	}

	/** Records why an apply was refused. */
	public void setRefusals(List<ResKey> refusals) {
		_refusals = List.copyOf(refusals);
		_revision++;
	}

	private void changed() {
		_revision++;
	}

}
