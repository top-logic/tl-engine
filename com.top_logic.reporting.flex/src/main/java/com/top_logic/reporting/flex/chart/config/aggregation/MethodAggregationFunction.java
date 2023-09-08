/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.aggregation;

import java.lang.reflect.Method;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.MethodCriterion;
import com.top_logic.util.Resources;

/**
 * A {@link MethodAggregationFunction} calls a configured method to get a number-value from a given object.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class MethodAggregationFunction extends AbstractAggregationFunction {

	/**
	 * Config-interface for {@link MethodAggregationFunction}.
	 */
	@TagName("method")
	public interface Config extends AbstractAggregationFunction.Config {

		@Override
		@ClassDefault(MethodAggregationFunction.class)
		public Class<? extends MethodAggregationFunction> getImplementationClass();

		/**
		 * a resource-key for the label of the function. Must not be set, default is
		 *         class-name + operation-name + method-name.
		 */
		@InstanceFormat
		@Mandatory
		ResKey getLabelKey();

		/**
		 * the name of the method to be called on the target. The method must not have any
		 *         paramters and return a Number.
		 */
		@Mandatory
		String getName();

		/**
		 * see {@link #getName()}
		 */
		void setName(String method);

	}

	/**
	 * Creates a {@link MethodAggregationFunction} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MethodAggregationFunction(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public String getLabel() {
		ResKey key = getConfig().getLabelKey();
		return Resources.getInstance().getString(key);
	}

	@Override
	protected Object getObjectValue(Object obj) {
		try {
			Method method = obj.getClass().getMethod(getConfig().getName());
			return method.invoke(obj);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected Criterion initCriterion() {
		return new MethodCriterion(this);
	}

}