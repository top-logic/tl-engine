/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link KeyValueAttributeBinding} using configured key extractor and key resolver functions in
 * TL-Script.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class AttributeValueBindingByExpression<C extends AttributeValueBindingByExpression.Config<?>>
		extends KeyValueAttributeBinding<C> {

	/**
	 * Configuration options for {@link AttributeValueBindingByExpression}.
	 */
	public interface Config<I extends AttributeValueBindingByExpression<?>> extends KeyValueAttributeBinding.Config<I> {

		/**
		 * @see #getKeyFun()
		 */
		String KEY_FUN = "key-fun";

		/**
		 * @see #getResolveFun()
		 */
		String RESOLVER_FUN = "resolver-fun";

		/**
		 * Function computing a key from a given object.
		 * 
		 * <p>
		 * The function must retrieve as many values as {@link #getKeyAttributes()} contains names.
		 * </p>
		 * 
		 * <p>
		 * If multiple key values are returned, they must be wrapped in a list. The result may
		 * contain key values of any type (including <code>null</code>), but only their canonical
		 * string-representation is stored and passed to {@link #getResolveFun()} later on for
		 * retrieval.
		 * </p>
		 */
		@Name(KEY_FUN)
		@Mandatory
		Expr getKeyFun();

		/**
		 * Function resolving an object given as many values as {@link #getKeyAttributes()} contains
		 * names.
		 * 
		 * <p>
		 * If {@link #getKeyAttributes()} contains a single name, a one-argument function is
		 * expected. If {@link #getKeyAttributes()} contains <code>n</code> names, a
		 * <code>n</code>-argument function is expected. All key arguments passed in are either of
		 * type {@link String} or <code>null</code>.
		 * </p>
		 */
		@Name(RESOLVER_FUN)
		@Mandatory
		Expr getResolveFun();

	}

	private final QueryExecutor _keyFun;

	private final QueryExecutor _resolveFun;

	/**
	 * Creates a {@link AttributeValueBindingByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeValueBindingByExpression(InstantiationContext context, C config) {
		super(context, config);

		_keyFun = QueryExecutor.compile(config.getKeyFun());
		_resolveFun = QueryExecutor.compile(config.getResolveFun());
	}

	@Override
	protected Object createKey(Object value) {
		return _keyFun.execute(value);
	}

	@Override
	protected Object resolveKey(Object key) {
		return _resolveFun.execute(key);
	}

}
