/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.jdbc;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.model.jdbcBinding.api.ColumnParser;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Parses the value with a TL-Script expression.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@InApp
public class ColumnParserByExpression extends AbstractConfiguredInstance<ColumnParserByExpression.Config>
		implements ColumnParser {

	/** {@link ConfigurationItem} for the {@link ColumnParserByExpression}. */
	public interface Config extends PolymorphicConfiguration<ColumnParserByExpression> {

		/**
		 * The expression parsing the raw database value.
		 * <p>
		 * The expression expects a single value (the raw database value) and returns the
		 * application value.
		 * </p>
		 */
		@Mandatory
		Expr getExpression();

	}

	private final QueryExecutor _expression;

	/** {@link TypedConfiguration} constructor for {@link ColumnParserByExpression}. */
	public ColumnParserByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_expression = QueryExecutor.compile(config.getExpression());
	}

	@Override
	public Object getApplicationValue(Object columnValue) {
		return _expression.execute(columnValue);
	}

}
