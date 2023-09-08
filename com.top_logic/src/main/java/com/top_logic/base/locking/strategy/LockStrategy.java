/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.strategy;

import java.util.List;

import com.top_logic.base.locking.token.Token;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Algorithm creating lock {@link Token}s for a given model element.
 * 
 * @param <M>
 *        The type of the model object for which {@link Token}s are created.
 * 
 * @see #createTokens(Object)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LockStrategy<M> {

	/**
	 * Configuration options for {@link LockStrategy}.
	 */
	public interface Config<I extends LockStrategy<?>> extends PolymorphicConfiguration<I> {
		// Pure marker interface.
	}

	/**
	 * Builds a set of {@link Token}s that must be acquired for a certain operation on the given
	 * model element.
	 *
	 * @param model
	 *        The model element to lock.
	 * @return The {@link Token}s that are necessary for the abstract operation that should be
	 *         performed.
	 */
	List<Token> createTokens(M model);

}
