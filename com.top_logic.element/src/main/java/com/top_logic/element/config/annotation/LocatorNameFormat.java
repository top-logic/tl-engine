/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;

/**
 * {@link ConfigurationValueProvider} that finds a {@link AttributeValueLocator} by its name in the
 * {@link AttributeValueLocatorFactory}.
 * 
 * @see com.top_logic.element.meta.expr.ExpressionEvaluationAlgorithm.Config.LocatorFormat
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocatorNameFormat extends AbstractConfigurationValueProvider<AttributeValueLocator> {

	/**
	 * Singleton {@link LocatorNameFormat} instance.
	 */
	public static final LocatorNameFormat INSTANCE = new LocatorNameFormat();

	private LocatorNameFormat() {
		super(AttributeValueLocator.class);
	}

	@Override
	public AttributeValueLocator getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		
		return AttributeValueLocatorFactory.getExpressionLocator(propertyValue.toString());
	}

	@Override
	public String getSpecificationNonNull(AttributeValueLocator configValue) {
		return AttributeValueLocatorFactory.getLocatorExpression(configValue);
	}

}
