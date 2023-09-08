/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.strategy;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.locking.token.Token;

/**
 * {@link LockStrategy} wrapping multiple {@link LockStrategy strategies}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StrategyCollection<M> implements LockStrategy<M> {

	private List<LockStrategy<? super M>> _strategies;

	/**
	 * Creates a {@link StrategyCollection}.
	 */
	public StrategyCollection(List<LockStrategy<? super M>> strategies) {
		_strategies = strategies;
	}

	@Override
	public List<Token> createTokens(M model) {
		ArrayList<Token> result = new ArrayList<>();
		for (LockStrategy<? super M> strategy : _strategies) {
			result.addAll(strategy.createTokens(model));
		}
		return result;
	}

}
