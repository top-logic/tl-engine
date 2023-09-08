/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.aggregation;

import java.util.Date;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;
import com.top_logic.reporting.flex.chart.config.partition.Criterion;
import com.top_logic.reporting.flex.chart.config.partition.Criterion.FunctionCriterion;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;

/**
 * A {@link AttributeAggregationFunction} applies a configured
 * {@link com.top_logic.reporting.flex.chart.config.aggregation.AbstractAggregationFunction.Operation}
 * to a configured {@link TLStructuredTypePart}. E.g. calculate the sum of the impact for given risks.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class AttributeAggregationFunction extends AbstractAggregationFunction {

	/**
	 * Config-interface for {@link AttributeAggregationFunction}.
	 */
	@TagName("attribute")
	public interface Config extends AbstractAggregationFunction.Config {

		@Override
		@ClassDefault(AttributeAggregationFunction.class)
		public Class<? extends AttributeAggregationFunction> getImplementationClass();

		/**
		 * a {@link MetaAttributeProvider} providing the attribute this function works with.
		 */
		@Hidden
		@Name("name")
		public MetaAttributeProvider getMetaAttribute();

		/**
		 * see {@link #getMetaAttribute()}
		 */
		public void setMetaAttribute(MetaAttributeProvider ma);

	}

	/**
	 * Creates a {@link AttributeAggregationFunction} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeAggregationFunction(InstantiationContext context, Config config) {
		super(context, config);
	}

	private TLStructuredTypePart getMetaAttribute() {
		MetaAttributeProvider maProvider = getConfig().getMetaAttribute();
		return maProvider == null ? null : maProvider.get();
	}

	@Override
	public String getLabel() {
		return new ConfigLabelProvider().getLabel(getConfig());
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * the name of the attribute containing the values for calculation.
	 */
	protected String getAttributeName() {
		MetaAttributeProvider maProvider = getConfig().getMetaAttribute();
		return maProvider == null ? null : maProvider.getMetaAttributeName();
	}

	private Object getAttribute(Object anObject) {
		return ((Wrapper) anObject).getValue(getAttributeName());
	}

	@Override
	protected Object getObjectValue(Object wrapper) {
		Object value = getAttribute(wrapper);
		if (value instanceof Date) {
			return ((Date) value).getTime();
		}
		return value;
	}

	@Override
	protected Criterion initCriterion() {
		return new FunctionCriterion(getLabel(), getMetaAttribute());
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @param ma
	 *        the attribute containing the values to be calculated
	 * @param op
	 *        the operation for the calculation
	 * @return a new ConfigItem initialized with the given values.
	 */
	public static Config item(TLStructuredTypePart ma, Operation op) {
		return item(new MetaAttributeProvider(ma), op);
	}

	/**
	 * see {@link #item(TLStructuredTypePart, Operation)} but with {@link MetaAttributeProvider} instead of
	 * {@link TLStructuredTypePart} directly.
	 */
	public static Config item(MetaAttributeProvider ma, Operation op) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setMetaAttribute(ma);
		item.setOperation(op);
		return item;
	}

}