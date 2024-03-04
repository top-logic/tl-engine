/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Named MBean with a configured expression.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MonitorByExpression extends DynamicMBeanElement {

	private QueryExecutor _impl;

	private List<OperationParameter> _parameters;

	/** {@link ConfigurationItem} for the {@link MonitorByExpression}. */
	public interface Config extends DynamicMBeanElement.Config {

		@ValueInitializer(MonitorByExpressionValueInitializer.class)
		@Override
		String getName();

		/**
		 * A configured expression which will be executed when this MBean is asked for the result of
		 * the query.
		 */
		@Label("Implementation")
		@Mandatory
		Expr getImpl();

		/**
		 * Parameters to fill for the configured script.
		 */
		List<OperationParameter> getParameters();
	}

	/** {@link TypedConfiguration} constructor for {@link MonitorByExpression}. */
	public MonitorByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_impl = QueryExecutor.compile(config.getImpl());
		_parameters = config.getParameters();

		buildDynamicMBeanInfo(config);
	}

	@Override
	protected MBeanConstructorInfo[] createConstructorInfo() {
		return null;
	}

	@Override
	protected MBeanAttributeInfo[] createAttributeInfo() {
		return null;
	}

	@Override
	protected MBeanOperationInfo[] createOperationInfo() {
		MBeanOperationInfo[] dOperations = new MBeanOperationInfo[1];

		List<MBeanParameterInfo> parameters = new ArrayList<>();
		for (OperationParameter param : _parameters) {
			com.top_logic.monitoring.data.OperationParameter.Config paramConfig = param.getConfig();
			parameters.add(new MBeanParameterInfo(
				paramConfig.getName(), // name
				paramConfig.getType().getCanonicalName(), // type
				paramConfig.getDescription())); // description
		}

		dOperations[0] = new MBeanOperationInfo(
			"execute", // name
			"Executes the configured script with its given parameters.", // description
			parameters.toArray(MBeanParameterInfo[]::new), // parameter types
			"java.lang.Object", // return type
			MBeanOperationInfo.ACTION); // impact

		return dOperations;
	}

	/** Executes the configured script with its given parameters. */
	public Object execute(Object... args) {
		Object[] arguments;
		if (args.length == 1 && args[0] instanceof Object[]) {
			// if called by reflection, the arguments are wrapped inside an array at the first
			// position to fit to the number of parameters.
			arguments = (Object[]) args[0];
		} else {
			arguments = args;
		}

		return _impl.execute(arguments);
	}

}
