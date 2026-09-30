/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.event;

import java.util.Objects;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link Modification} modifies some content in the {@link KnowledgeBase}, e.g. it deletes or
 * creates objects in reaction to other changes.
 * 
 * @see ModificationListener
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Modification {

	/**
	 * No operations to perform.
	 * 
	 * <p>
	 * Return this instance no modifications must be done.
	 * </p>
	 */
	Modification NONE = new Modification() {
	
		@Override
		public void execute() {
			// does nothing
		}

		@Override
		public Modification compose(Modification before) {
			Objects.requireNonNull(before);
			return before;
		}

		@Override
		public Modification andThen(Modification after) {
			Objects.requireNonNull(after);
			return after;
		}
	
	};

	/**
	 * Performs the changes in the {@link KnowledgeBase}.
	 * 
	 * @throws DataObjectException
	 *         If some data object operation fails.
	 */
	void execute() throws DataObjectException;

	/**
	 * Returns a composed {@link Modification} that first executes the {@code before}
	 * {@link Modification} and then this modifications. If evaluation of either
	 * {@link Modification} throws an exception, it is relayed to the caller of the composed
	 * {@link Modification}.
	 *
	 * @param before
	 *        The {@link Modification} to execute before this {@link Modification} is executed.
	 * @return A composed {@link Modification} that first executes the {@code before}
	 *         {@link Modification} and then executes this {@link Modification}.
	 * @throws NullPointerException
	 *         if before is null
	 *
	 * @see #andThen(Modification)
	 */
	default Modification compose(Modification before) {
		return before.andThen(this);
	}

	/**
	 * Returns a composed {@link Modification} that first executes this {@link Modification} and
	 * then the {@code after} {@link Modification}. If evaluation of either {@link Modification}
	 * throws an exception, it is relayed to the caller of the composed {@link Modification}.
	 *
	 * @param after
	 *        The {@link Modification} to execute after this {@link Modification} is executed.
	 * @return A composed {@link Modification} that first executes this {@link Modification} and
	 *         then the {@code after} {@link Modification}.
	 * @throws NullPointerException
	 *         if after is null
	 *
	 * @see #compose(Modification)
	 */
	default Modification andThen(Modification after) {
		if (after == NONE) {
			return this;
		}
		Objects.requireNonNull(after);
		return new ModificationPair(this, after);
	}
}

