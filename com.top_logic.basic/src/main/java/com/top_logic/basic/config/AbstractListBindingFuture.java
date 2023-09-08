/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.format.BuiltInFormats;

/**
 * {@link ConfigurationValueBindingFuture} for {@link List} or {@link Map}
 * values properties.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractListBindingFuture implements ConfigurationValueBindingFuture {

	public static final String STRING_DEFAULT = "";
	public static final Class<?> FORMAT_CLASS_DEFAULT = VoidValueProvider.class;

	public static final String DEFAULT_VAUE_ATTRIBUTE_NAME = "value";

	public AbstractListBindingFuture() {
		super();
	}

	protected String resolveValueAttributeName(Protocol protocol, PropertyDescriptorImpl property, String valueAttributeName) {
		if (!STRING_DEFAULT.equals(valueAttributeName)) {
			return valueAttributeName;
		}

		protocol.info(property + ": No value attribute specified: use " + DEFAULT_VAUE_ATTRIBUTE_NAME, Protocol.DEBUG);
		return DEFAULT_VAUE_ATTRIBUTE_NAME;
	}

	protected String resolveTagName(Protocol protocol, PropertyDescriptorImpl property, String tagName) {
		if (!STRING_DEFAULT.equals(tagName)) {
			return tagName;
		}
		final String fallbackName = property.getFallbackElementName();
		protocol.info(property + ": No element tag specified: use " + fallbackName, Protocol.DEBUG);
		return fallbackName;
	}

	protected ConfigurationValueProvider<?> resolveProvider(Protocol protocol, PropertyDescriptorImpl property,
			Class<? extends ConfigurationValueProvider<?>> formatClass) {
		ConfigurationValueProvider<?> valueProvider;
		if (formatClass == FORMAT_CLASS_DEFAULT) {
			// try generic
			final Class<?> type = property.getType();
			if (!(List.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type) || type.isArray())) {
				throw protocol.fatal(property + " must have at least collection return type but has '" + type.getName()
						+ "'.");
			}
			final Class<?> elementType = property.getElementType();
			if (elementType == null) {
				throw protocol.fatal(property + " must have definition of element type.");
			}
			valueProvider = BuiltInFormats.getPrimitiveValueProvider(elementType);
			if (valueProvider == null) {
				Format classFormatAnnotation = elementType.getAnnotation(Format.class);
				if (classFormatAnnotation != null) {
					valueProvider = createValueProvider(protocol, classFormatAnnotation.value());
				}
			}
			if (valueProvider == null) {
				throw protocol.fatal("Unable to resolve element type '" + elementType.getName() + "' of '" + property
						+ "' to a ConfigurationValueProvider");
			}
		} else {
			valueProvider = createValueProvider(protocol, formatClass);
		}
		return valueProvider;
	}

	/**
	 * Instantiates the given formt class.
	 */
	protected ConfigurationValueProvider<?> createValueProvider(Protocol protocol,
			Class<? extends ConfigurationValueProvider<?>> formatClass) {
		try {
			return ConfigUtil.getInstance(formatClass);
		} catch (ConfigurationException ex) {
			throw protocol.fatal("Unable to resolve " + ConfigurationValueProvider.class.getName(), ex);
		}
	}

}
