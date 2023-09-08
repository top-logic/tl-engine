/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;

/**
 * {@link ConfigurationValueProvider} that parses a query parameter specification.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class QueryParameterFormat extends AbstractConfigurationValueProvider<Map<String, AttributeValueLocator>> {

	private static final String PARAMS_SEP_PARAM  = ",";
	private static final String PARAMS_SEP_TYPE   = "#";

	/**
	 * Singleton {@link QueryParameterFormat} instance.
	 */
	public static final QueryParameterFormat INSTANCE = new QueryParameterFormat();

	private QueryParameterFormat() {
		super(Map.class);
	}

	@Override
	protected Map<String, AttributeValueLocator> getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	public Map<String, AttributeValueLocator> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		
		return parseParameters(propertyValue.toString());
	}

	private Map<String, AttributeValueLocator> parseParameters(String parameterConfig) {
		String[] parameterConfigs = StringServices.toArray(parameterConfig, PARAMS_SEP_PARAM);
		Map<String, AttributeValueLocator> result = new HashMap<>();
		for (int i=0; i<parameterConfigs.length; i++) {
			String[] nameLocatorPair = StringServices.toArray(parameterConfigs[i], PARAMS_SEP_TYPE);
			if (nameLocatorPair.length != 2) {
				throw new RuntimeException("Invalid parameter definition: " + parameterConfigs[i]);
			}
			
			String name = nameLocatorPair[0];
			if (StringServices.isEmpty(name)) {
				throw new RuntimeException("Empty parameter name: " + parameterConfigs[i]);
			}
			if (result.containsKey(name)) {
				throw new RuntimeException("Duplicate parameter name: " + parameterConfigs[i]);
			}
			
			String locatorName = nameLocatorPair[1];
			AttributeValueLocator locator = AttributeValueLocatorFactory.getExpressionLocator(locatorName);
			result.put(name, locator);
		}
		return result;
	}

	@Override
	public Map<String, AttributeValueLocator> defaultValue() {
		return new HashMap<>();
	}
	
	@Override
	public String getSpecificationNonNull(Map<String, AttributeValueLocator> configValue) {
		StringBuilder result = new StringBuilder();
		for (Entry<String, AttributeValueLocator> entry : configValue.entrySet()) {
			if (result.length() > 0) {
				result.append(PARAMS_SEP_PARAM);
			}
			result.append(entry.getKey());
			result.append(PARAMS_SEP_TYPE);
			result.append(AttributeValueLocatorFactory.getLocatorExpression(entry.getValue()));
		}
		return result.toString();
	}

}
