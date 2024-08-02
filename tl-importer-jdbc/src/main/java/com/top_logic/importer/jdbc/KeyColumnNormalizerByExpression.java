/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.importer.jdbc;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.model.jdbcBinding.api.KeyColumnNormalizer;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Normalizes the key with a TL-Script expression.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class KeyColumnNormalizerByExpression extends AbstractConfiguredInstance<KeyColumnNormalizerByExpression.Config>
		implements KeyColumnNormalizer {

	/** {@link ConfigurationItem} for the {@link ColumnParserByExpression}. */
	public interface Config extends PolymorphicConfiguration<KeyColumnNormalizerByExpression> {

		/**
		 * The expression normalizing the raw database value.
		 * <p>
		 * The expression expects a single value (the raw database value) and returns the normalized
		 * value.
		 * </p>
		 */
		@Mandatory
		Expr getExpression();

	}

	private QueryExecutor _expression;

	/** {@link TypedConfiguration} constructor for {@link ColumnParserByExpression}. */
	public KeyColumnNormalizerByExpression(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object normalize(Object value) {
		return getExpression().execute(value);
	}

	private QueryExecutor getExpression() {
		if (_expression == null) {
			_expression = QueryExecutor.compile(getConfig().getExpression());
		}
		return _expression;
	}

}
