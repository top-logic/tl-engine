/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.struct;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.internal.ItemFactory;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Transforms a JSON object to a configuration type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToConfig extends GenericMethod implements WithFlatMapSemantics<ItemFactory> {

	/**
	 * Creates a {@link ToConfig}.
	 */
	protected ToConfig(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ToConfig(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String intfName = asString(arguments[2], null);

		Class<?> intf;
		if (intfName == null) {
			String className = (String) arguments[1];
			if (className == null) {
				// Error.
			}

			Class<?> implType = lookupClass(className);
			Factory factory = getFactory(implType);
			intf = factory.getConfigurationInterface();
		} else {
			intf = lookupClass(intfName);
		}

		ItemFactory factory = TypedConfiguration.getConfigurationDescriptor(intf).factory();

		return evalPotentialFlatMap(definitions, arguments[0], factory);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, ItemFactory factory) {
		Map<?, ?> json = asMap(singletonValue);
		return toItem(definitions, factory, json);
	}

	private Object toItem(EvalContext definitions, ItemFactory factory, Map<?, ?> json) {
		ConfigurationItem item = factory.createNew();
		for (Entry<?, ?> entry : json.entrySet()) {
			String propertyName = entry.getKey().toString();
			PropertyDescriptor property = item.descriptor().getProperty(propertyName);
			if (property == null) {
				if (!propertyName.startsWith("$")) {
					Logger.warn("No such property '" + propertyName + "' in "
						+ item.descriptor().getConfigurationInterface().getName() + ".", ToConfig.class);
				}
				continue;
			}
			switch (property.kind()) {
				case PLAIN: {
					Object value = entry.getValue();
					Object parsedValue;
					if (value == null) {
						parsedValue = null;
					} else {
						parsedValue = parse(property, value.toString());
					}
					item.update(property, parsedValue);
					break;
				}

				case COMPLEX:
					Object value = entry.getValue();
					item.update(property, value);
					break;

				case ITEM: {
					Object jsonValue = entry.getValue();
					Object itemValue = toInnerItem(definitions, property, jsonValue);
					item.update(property, itemValue);
					break;
				}

				case ARRAY:
				case LIST:
				case MAP: {
					List<?> jsonList = asList(entry.getValue());
					List<?> itemList =
						jsonList.stream().map(x -> toInnerItem(definitions, property, x))
							.collect(Collectors.toList());
					item.update(property, itemList);
					break;
				}

				case DERIVED:
				case REF:
					// Skip
			}
		}
		return item;
	}

	private Object parse(PropertyDescriptor property, String value) {
		try {
			return property.getValueProvider().getValue(property.getPropertyName(), value);
		} catch (ConfigurationException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_PARSING_CONFIGURATION_VALUE__PROP_VAL_MSG.fill(property, value, ex.getMessage()),
				ex);
		}
	}

	private Object toInnerItem(EvalContext definitions, PropertyDescriptor property, Object jsonValue) {
		Object rawValue = asSingleElement(jsonValue);
		if (rawValue instanceof CharSequence) {
			ConfigurationValueProvider<?> format = property.getValueProvider();
			if (format != null) {
				return parse(property, rawValue.toString());
			}
		}

		Map<?, ?> mapValue = (Map<?, ?>) rawValue;
		Object itemValue;
		if (mapValue == null) {
			itemValue = null;
		} else {
			String tagName = (String) mapValue.get("$tag");
			ConfigurationDescriptor elementDescriptor;
			if (tagName != null) {
				elementDescriptor = property.getElementDescriptor(tagName);
			} else {
				String intfName = (String) mapValue.get("$intf");
				if (intfName == null) {
					String className = (String) mapValue.get("class");
					if (className == null) {
						elementDescriptor = property.getValueDescriptor();
					} else {
						Class<?> implType = lookupClass(className);
						Factory elementFactory = getFactory(implType);
						elementDescriptor = TypedConfiguration
							.getConfigurationDescriptor(elementFactory.getConfigurationInterface());
					}
				} else {
					elementDescriptor =
						TypedConfiguration.getConfigurationDescriptor(lookupClass(intfName));
				}
			}
			itemValue = evalDirect(definitions, mapValue, elementDescriptor.factory());
		}
		return itemValue;
	}

	private Factory getFactory(Class<?> implType) {
		try {
			return DefaultConfigConstructorScheme.getFactory(implType);
		} catch (ConfigurationException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_CREATING_CONFIGURATION__TYPE_MSG.fill(implType.getName(), ex.getMessage()), ex);
		}
	}

	private Class<?> lookupClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException ex) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SUCH_CLASS__NAME.fill(name));
		}
	}

	/**
	 * {@link MethodBuilder} creating {@link ToConfig}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ToConfig> {
		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("json")
			.optional("class")
			.optional("intf")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public ToConfig build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new ToConfig(getConfig().getName(), args);
		}

	}

}
