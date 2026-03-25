/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.knowledge.service.Transaction;

/**
 * Participant in a form's editing lifecycle.
 *
 * <p>
 * {@link FormControl} delegates save/cancel/validate/dirty/reveal operations to all registered
 * participants uniformly, without knowing what kind of content they manage (primitive fields,
 * composition tables, sub-forms, etc.).
 * </p>
 */
public interface FormParticipant {

	/**
	 * Validates this participant's content.
	 *
	 * @return {@code true} if valid, {@code false} if validation errors exist.
	 */
	boolean validate();

	/**
	 * Applies buffered overlay changes to the underlying base objects.
	 *
	 * <p>
	 * Called by {@link FormControl#executeStoreState()} for all participants. Does not require a KB
	 * transaction. For example, a composition table applies its row overlays here.
	 * </p>
	 */
	default void applyState() {
		// Default no-op for participants whose state is fully managed by the main overlay.
	}

	/**
	 * Performs KB-specific operations within the given transaction.
	 *
	 * <p>
	 * Called within an open transaction before {@link #applyState()} and the main overlay
	 * application. Participants persist new transient objects, update overlay reference lists, and
	 * delete orphaned objects here.
	 * </p>
	 *
	 * @param tx
	 *        The current transaction.
	 */
	void persist(Transaction tx);

	/**
	 * Cancels this participant's editing state, discarding any uncommitted changes.
	 */
	void cancel();

	/**
	 * Reveals all hidden validation errors managed by this participant.
	 */
	void revealAll();

	/**
	 * Whether this participant has uncommitted changes.
	 *
	 * @return {@code true} if dirty.
	 */
	boolean isDirty();
}
