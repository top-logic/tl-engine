/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Named MBean with a configured expression.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MBeanByExpression extends AbstractDynamicMBean {

	private final QueryExecutor _impl;

	/** {@link ConfigurationItem} for the {@link MBeanByExpression}. */
	public interface Config extends AbstractDynamicMBean.Config {

		@ValueInitializer(MBeanByExpressionValueInitializer.class)
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

	/** {@link TypedConfiguration} constructor for {@link MBeanByExpression}. */
	public MBeanByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_impl = QueryExecutor.compile(config.getImpl());

		if (getParameters() == null || getParameters().size() == 0) {
			execute();
		}
	}

	@Override
	protected MBeanAttributeInfo[] createAttributeInfo() {
		MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[1];

		attributes[0] = new MBeanAttributeInfo(
			"Result", // name
			"java.lang.Object", // type
			"The result of the query.", // description
			true, // readable
			false, // writable
			false); // isIs

		return attributes;
	}

	@Override
	protected MBeanOperationInfo[] createOperationInfo() {
		MBeanOperationInfo[] operations = new MBeanOperationInfo[1];

		List<MBeanParameterInfo> parameters = new ArrayList<>();
		for (OperationParameter param : getParameters()) {
			OperationParameter.Config paramConfig = param.getConfig();
			parameters.add(new MBeanParameterInfo(
				paramConfig.getName(),
				paramConfig.getType().getCanonicalName(),
				paramConfig.getDescription()));
		}

		operations[0] = new MBeanOperationInfo(
			"execute", // name
			"Executes the configured script with its given parameters.", // description
			parameters.toArray(MBeanParameterInfo[]::new), // parameter types
			"java.lang.Object", // return type
			MBeanOperationInfo.ACTION); // impact

		return operations;
	}

	/** Returns the result of the query. */
	@CalledByReflection
	public Object getResult() {
		return ThreadContextManager.inSystemInteraction(MBeanByExpression.class,
			() -> CollectionUtil.isEmpty(getParameters()) ? getImpl().execute() : getImpl().execute(getParameters()));
	}

	/** Executes the configured script with its given parameters. Maybe called by reflection. */
	@CalledByReflection
	public Object execute(Object... args) {
		return ThreadContextManager.inSystemInteraction(MBeanByExpression.class, () -> getImpl().execute(args));
	}

	private List<OperationParameter> getParameters() {
		return ((MBeanByExpression.Config) getConfig()).getParameters();
	}

	private QueryExecutor getImpl() {
		return _impl;
	}

}