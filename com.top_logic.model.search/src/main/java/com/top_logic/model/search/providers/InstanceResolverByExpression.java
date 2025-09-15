/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link InstanceResolver} implemented in TL-Script.
 */
@InApp
@Label("TL-Script instance resolver")
public class InstanceResolverByExpression extends AbstractConfiguredInstance<InstanceResolverByExpression.Config<?>>
		implements InstanceResolver {

	private QueryExecutor _identity;

	private QueryExecutor _resolver;

	/**
	 * Configuration options for {@link InstanceResolverByExpression}.
	 */
	@TagName("resolver-by-expression")
	public interface Config<I extends InstanceResolverByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * Function computing a stable an ID for an object.
		 * 
		 * <p>
		 * The function expects the object to identify as argument and must return the object's ID
		 * as result.
		 * </p>
		 */
		@Mandatory
		Expr getIdentity();

		/**
		 * Function resolving an object from its ID.
		 * 
		 * <p>
		 * The function expects the object's ID as single argument and returns the resolved object.
		 * </p>
		 */
		@Mandatory
		Expr getResolver();

	}

	/**
	 * Creates a {@link InstanceResolverByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InstanceResolverByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_identity = QueryExecutor.compile(config.getIdentity());
		_resolver = QueryExecutor.compile(config.getResolver());
	}

	@Override
	public TLObject resolve(Log log, String kind, String id) {
		return SearchExpression.asTLObject(_resolver.getSearch(), _resolver.execute(id));
	}

	@Override
	public String buildId(TLObject obj) {
		return ToString.toString(_identity.execute(obj));
	}

}
