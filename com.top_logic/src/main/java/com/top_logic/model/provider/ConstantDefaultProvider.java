/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MODefaultProvider;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link DefaultProvider}, {@link MODefaultProvider} and {@link DefaultValueProvider} returning
 * constantly a given value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Default value computation")
public class ConstantDefaultProvider extends DefaultValueProviderShared implements DefaultProvider, MODefaultProvider {

	private Object _defaultValue;

	/**
	 * Creates a new {@link ConstantDefaultProvider}.
	 */
	public ConstantDefaultProvider(Object defaultValue) {
		_defaultValue = defaultValue;
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		return _defaultValue;
	}

	@Override
	public Object createDefault(MOAttribute attribute) {
		return _defaultValue;
	}

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		return _defaultValue;
	}

}

