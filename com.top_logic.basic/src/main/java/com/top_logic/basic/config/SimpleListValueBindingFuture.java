/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.xml.ArrayValueBinding;
import com.top_logic.basic.config.xml.ListValueBinding;

/**
 * {@link ConfigurationValueBindingFuture} resolving a {@link ListBinding} annotation to a
 * {@link ConfigurationValueBinding}.
 * 
 * @see SimpleMapValueBindingFuture
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleListValueBindingFuture extends AbstractListBindingFuture {

	private final ListBinding _binding;

	/**
	 * Creates a {@link SimpleListValueBindingFuture}.
	 *
	 * @param binding
	 *        The {@link ListBinding} annotation to interpret.
	 */
	public SimpleListValueBindingFuture(ListBinding binding) {
		this._binding = binding;
	}

	@Override
	public ConfigurationValueBinding<?> resolveFor(Protocol protocol, PropertyDescriptorImpl property) {
		ConfigurationValueProvider<?> valueProvider = resolveProvider(protocol, property, _binding.format());
		String elementName = resolveTagName(protocol, property, _binding.tag());
		final String attributeName = resolveValueAttributeName(protocol, property, _binding.attribute());
		
		Class<?> type = property.getType();
		if (type.isArray()) {
			Class<?> elementType = type.getComponentType();
			return new ArrayValueBinding(elementType, elementName, attributeName, valueProvider);
		} else {
			return new ListValueBinding(elementName, attributeName, valueProvider);
		}
	}

}
