/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Map;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.format.BuiltInFormats;
import com.top_logic.basic.config.xml.MapAttributeBinding;

/**
 * {@link ConfigurationValueBindingFuture} resolving a {@link MapBinding}.
 * 
 * @see SimpleListValueBindingFuture
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleMapValueBindingFuture extends AbstractListBindingFuture {

	public static final String DEFAULT_KEY_ATTRIBUTE_NAME = "key";
	public static final Class<?> KEY_FORMAT_CLASS_DEFAULT = VoidValueProvider.class;

	private final MapBinding _binding;

	public SimpleMapValueBindingFuture(MapBinding binding) {
		this._binding = binding;
	}

	@Override
	public ConfigurationValueBinding<?> resolveFor(Protocol protocol, PropertyDescriptorImpl property) {
		ConfigurationValueProvider<?> valueProvider = resolveProvider(protocol, property, _binding.valueFormat());
		ConfigurationValueProvider<?> keyProvider = resolveKeyProvider(protocol, property, _binding.keyFormat());
		String elementName = resolveTagName(protocol, property, _binding.tag());
		String attributeName = resolveValueAttributeName(protocol, property, _binding.attribute());
		String keyAttribute = resolveKeyAttributeName(protocol, property, _binding.key());
		return MapAttributeBinding.createMapAttributeBinding(elementName, keyAttribute, keyProvider, attributeName, valueProvider);
	}

	private String resolveKeyAttributeName(Protocol protocol, PropertyDescriptor property, String keyAttributeName) {
		if (!STRING_DEFAULT.equals(keyAttributeName)) {
			return keyAttributeName;
		}
		protocol.info(property + ": No key attribute specified: use " + DEFAULT_KEY_ATTRIBUTE_NAME, Protocol.DEBUG);
		return DEFAULT_KEY_ATTRIBUTE_NAME;
	}

	protected ConfigurationValueProvider<?> resolveKeyProvider(Protocol protocol, PropertyDescriptorImpl property,
			Class<? extends ConfigurationValueProvider<?>> formatClass) {
		ConfigurationValueProvider<?> valueProvider;
		if (formatClass == FORMAT_CLASS_DEFAULT) {
			// try generic
			final Class<?> type = property.getType();
			if (Map.class != type) {
				throw protocol.fatal(property + " must have at least collection return type but has '" + type.getName()
						+ "'.");
			}
			final Class<?> keyType = property.getKeyType();
			if (keyType == null) {
				throw protocol.fatal(property + " must have definition of element type.");
			}
			valueProvider = BuiltInFormats.getPrimitiveValueProvider(keyType);
			if (valueProvider == null) {
				Format classFormatAnnotation = keyType.getAnnotation(Format.class);
				if (classFormatAnnotation != null) {
					valueProvider = createValueProvider(protocol, classFormatAnnotation.value());
				}
			}
			if (valueProvider == null) {
				throw protocol.fatal("Unable to resolve key type '" + keyType.getName() + "' of '" + property
						+ "' to a ConfigurationValueProvider");
			}
		} else {
			valueProvider = createValueProvider(protocol, formatClass);
		}
		return valueProvider;
	}


}
