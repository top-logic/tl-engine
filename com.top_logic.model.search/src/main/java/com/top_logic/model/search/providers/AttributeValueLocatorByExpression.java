/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link AttributeValueLocator} that can be implemented with a TL-Script expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class AttributeValueLocatorByExpression extends
		AbstractConfiguredInstance<AttributeValueLocatorByExpression.Config<?>> implements AttributeValueLocator {

	/**
	 * Configuration options for {@link AttributeValueLocatorByExpression}.
	 */
	public interface Config<I extends AttributeValueLocatorByExpression> extends PolymorphicConfiguration<I> {
		/**
		 * Function resolving an object.
		 * 
		 * <p>
		 * The function expects the base object as single argument.
		 * </p>
		 */
		Expr getExpr();
	}

	private QueryExecutor _fun;

	/**
	 * Creates a {@link AttributeValueLocatorByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeValueLocatorByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_fun = QueryExecutor.compile(config.getExpr());
	}

	@Override
	public Object locateAttributeValue(Object anObject) {
		return _fun.execute(anObject);
	}

}
