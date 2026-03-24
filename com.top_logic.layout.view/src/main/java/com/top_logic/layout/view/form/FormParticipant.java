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
	 * Applies this participant's changes within the given transaction.
	 *
	 * <p>
	 * Called within an open transaction before the overlay is applied to the base object.
	 * Participants may persist new objects and update the overlay's reference lists here.
	 * </p>
	 *
	 * @param tx
	 *        The current transaction.
	 */
	void apply(Transaction tx);

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
