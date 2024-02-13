/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.io.StringReader;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Named MBean with a configured expression.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MonitorByExpression extends AbstractConfiguredInstance<MonitorByExpression.Config>
		implements MonitorByExpressionMBean, MBeanElement {

	private QueryExecutor _internalImplemenation;

	private QueryExecutor _externalImplementation;

	/** {@link ConfigurationItem} for the {@link MonitorByExpression}. */
	public interface Config extends MBeanConfiguration<MonitorByExpression> {

		@ValueInitializer(MonitorByExpressionValueInitializer.class)
		@Constraint(MBeanNameConstraint.class)
		@Override
		public String getName();

		/**
		 * A configured expression which will be executed when this MBean is asked for the result of
		 * the query.
		 */
		@Label("Implementation")
		@Mandatory
		Expr getImpl();
	}

	/** {@link TypedConfiguration} constructor for {@link MonitorByExpression}. */
	public MonitorByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_internalImplemenation = QueryExecutor.compile(config.getImpl());
	}

	@Override
	public Object getResult() {
		return _internalImplemenation.execute();
	}

	@Override
	public void compileExternalQuery(String script) {
		Expr stringToExpr = stringToExpr(script);

		if (stringToExpr != null) {
			ThreadContextManager.inSystemInteraction(MonitorUser.class, () -> {
				_externalImplementation = QueryExecutor.compile(stringToExpr);
			});
		}
	}

	private Expr stringToExpr(String script) {
		if (StringServices.isEmpty(script)) {
			return null;
		}

		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(script));
		try {
			return parser.expr();
		} catch (ParseException ex) {
			Logger.error("Failed to create an Expr of: '" + script + "'.", ex, MonitorByExpression.class);
		}
		return null;
	}

	@Override
	public Object getExternalResult() {
		if (_externalImplementation != null) {
			return _externalImplementation.execute();
		}

		return null;
	}

}
