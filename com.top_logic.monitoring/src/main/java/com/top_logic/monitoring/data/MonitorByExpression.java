/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Named MBean with a configured expression.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MonitorByExpression extends AbstractConfiguredInstance<MonitorByExpression.Config>
		implements MonitorByExpressionMBean, NamedMonitor {

	private QueryExecutor _compile;

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

		_compile = QueryExecutor.compile(config.getImpl());
	}

	@Override
	public Object getResult() {
		return _compile.execute();
	}

}
