/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr;


import java.io.StringReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.element.config.algorithm.GenericAlgorithmConfig;
import com.top_logic.element.config.annotation.LocatorNameFormat;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.MetaAttributeAlgorithm;
import com.top_logic.element.meta.expr.internal.Chain;
import com.top_logic.element.meta.expr.internal.OperationFactory;
import com.top_logic.element.meta.expr.parser.ExpressionParser;
import com.top_logic.element.meta.expr.parser.ParseException;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.model.TLObject;

/**
 * Default {@link MetaAttributeAlgorithm} that evaluates a configurable generic expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpressionEvaluationAlgorithm extends MetaAttributeAlgorithm {

	/**
	 * Configuration options for {@link ExpressionEvaluationAlgorithm}.
	 */
	@TagName("expression-evaluation")
	public interface Config extends GenericAlgorithmConfig {

		@Override
		@ClassDefault(ExpressionEvaluationAlgorithm.class)
		public Class<? extends MetaAttributeAlgorithm> getImplementationClass();

		/**
		 * The expression configuration.
		 * 
		 * @see ExpressionParser
		 */
		@Format(LocatorFormat.class)
		@ItemDefault(Chain.Config.class)
		@ImplementationClassDefault(Chain.class)
		@DefaultContainer
		PolymorphicConfiguration<? extends AttributeValueLocator> getExpr();

		/**
		 * Compact format for an {@link AttributeValueLocator} configuration.
		 * 
		 * @see ExpressionParser
		 * @see LocatorNameFormat
		 */
		class LocatorFormat extends
				AbstractConfigurationValueProvider<PolymorphicConfiguration<? extends AttributeValueLocator>> {

			/**
			 * Singleton {@link LocatorFormat} instance.
			 */
			public static final LocatorFormat INSTANCE = new LocatorFormat();

			private LocatorFormat() {
				super(PolymorphicConfiguration.class);
			}

			@Override
			protected PolymorphicConfiguration<? extends AttributeValueLocator> getValueNonEmpty(
					String propertyName, CharSequence propertyValue) throws ConfigurationException {
				String value = propertyValue.toString();
				ExpressionParser parser = new ExpressionParser(new StringReader(value));
				OperationFactory operations = new OperationFactory();
				parser.setOperationFactory(operations);
				PolymorphicConfiguration<? extends AttributeValueLocator> config;
				try {
					config = parser.expr();
				} catch (ParseException ex) {
					throw new ConfigurationException(I18NConstants.ERROR_INVALID_LOCATOR_FORMAT__VALUE.fill(value),
						propertyName, propertyValue, ex);
				}
				return config;
			}

			@Override
			protected String getSpecificationNonNull(
					PolymorphicConfiguration<? extends AttributeValueLocator> configValue) {
				throw new UnsupportedOperationException(
					"There is no normative way to serialize a locator configuration.");
			}

			@Override
			public boolean isLegalValue(Object value) {
				// Parsing only format.
				return false;
			}

		}

	}

	private final AttributeValueLocator _locator;

	/**
	 * Creates a {@link ExpressionEvaluationAlgorithm} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExpressionEvaluationAlgorithm(InstantiationContext context, Config config) {
		super(context, config);

		_locator = context.getInstance(config.getExpr());
	}

	@Override
	public Object calculate(TLObject obj) throws AttributeException {
		return _locator.locateAttributeValue(obj);
	}

}
