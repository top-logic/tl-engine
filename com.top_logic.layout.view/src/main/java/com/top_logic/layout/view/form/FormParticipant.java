/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.error.TopLogicException;

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
	 * Whether this participant's content is free of validation errors visible to the user.
	 *
	 * <p>
	 * A pure query, called whenever the form re-evaluates what the user may do. Errors are made
	 * visible by {@link #revealAll()}, which the form runs before validating on a save attempt.
	 * </p>
	 *
	 * @return {@code true} if valid, {@code false} if validation errors exist.
	 */
	boolean validate();

	/**
	 * Ensures that the current user may write all changes {@link #applyState()} would write.
	 *
	 * <p>
	 * Called by {@link FormControl} for all participants before any of them persists or applies its
	 * state, so a refused save leaves the buffered changes of every participant in place.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the current user is not allowed to write one of the changes.
	 *
	 * @see TLObjectOverlay#checkApply()
	 */
	default void checkApplyState() {
		// Default no-op for participants whose state is fully managed by the main overlay.
	}

	/**
	 * Applies buffered overlay changes to the underlying base objects.
	 *
	 * <p>
	 * Called by {@link FormControl#executeStoreState()} for all participants after validation, the
	 * write-rights check and, for a persistent form object, {@link #persist(Transaction)}, and before
	 * the main overlay is applied. For a persistent form object, this happens within the KB
	 * transaction opened for {@link #persist(Transaction)}. For example, a composition table applies
	 * its row overlays here.
	 * </p>
	 */
	default void applyState() {
		// Default no-op for participants whose state is fully managed by the main overlay.
	}

	/**
	 * Performs KB-specific operations within the given transaction.
	 *
	 * <p>
	 * Called by {@link FormControl#executeStoreState()} for all participants if the form object is
	 * persistent, after validation and the write-rights check, within an open transaction, and before
	 * {@link #applyState()} and the main overlay application. Participants persist new transient
	 * objects, update overlay reference lists, and delete orphaned objects here. For a transient form
	 * object, this method is not called.
	 * </p>
	 *
	 * @param tx
	 *        The current transaction. It may be nested into a transaction of the caller, which then
	 *        decides whether the changes are committed.
	 */
	void persist(Transaction tx);

	/**
	 * Called in edit mode after a change to the edited object was stored by someone else than the
	 * form, e.g. by a command committing a change to the displayed object.
	 *
	 * <p>
	 * A participant shows the stored values where the user has not changed anything, and keeps the
	 * user's changes everywhere else, so that a save still writes them.
	 * </p>
	 */
	default void onObjectChanged() {
		// Default no-op for participants keeping their buffered state until save or cancel.
	}

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
