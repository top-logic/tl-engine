/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.base.locking.strategy.ConfiguredLockStrategy;
import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link LockStrategy} that selects its scope with a search expression.
 * 
 * @see Config#getObjects()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LockStrategyByExpression<C extends LockStrategyByExpression.Config<?>>
		extends ConfiguredLockStrategy<C, Object> {

	private QueryExecutor _objectsFun;

	/**
	 * Configuration options for {@link LockStrategyByExpression}.
	 */
	@TagName("tokens")
	public interface Config<I extends LockStrategyByExpression<?>> extends ConfiguredLockStrategy.Config<I> {

		/**
		 * Function that produces a set of objects for which tokens of kind {@link #getKind()}
		 * should be allocated.
		 */
		@Name("objects")
		Expr getObjects();
	}

	/**
	 * Creates a {@link LockStrategyByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LockStrategyByExpression(InstantiationContext context, C config) {
		super(context, config);

		_objectsFun = QueryExecutor.compile(config.getObjects());
	}

	@Override
	public List<Token> createTokens(Object model) {
		Object objectResult = _objectsFun.execute(model);
		List<Token> result = new ArrayList<>();
		Kind kind = getConfig().getKind();
		String aspect = getConfig().getAspect();
		if (objectResult instanceof Collection<?>) {
			for (Object element : unique((Collection<?>) objectResult)) {
				if (element == null) {
					continue;
				}
				result.add(Token.newToken(kind, asTLObject(element), aspect));
			}
		} else if (objectResult != null) {
			result.add(Token.newToken(kind, asTLObject(objectResult), aspect));
		}
		return result;
	}

	private TLObject asTLObject(Object element) {
		return SearchExpression.asTLObject(_objectsFun.getSearch(), element);
	}

	private Collection<?> unique(Collection<?> objects) {
		return objects instanceof Set<?> ? objects : new HashSet<>(objects);
	}

}
