/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for the {@link ComponentName}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentNameFormat extends AbstractConfigurationValueProvider<ComponentName> {

	/** Singleton {@link ComponentNameFormat} instance. */
	public static final ComponentNameFormat INSTANCE = new ComponentNameFormat();

	private ComponentNameFormat() {
		super(ComponentName.class);
	}

	@Override
	protected ComponentName getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String componentName = propertyValue.toString();
		int scopeSeparator = componentName.indexOf(ComponentName.SCOPE_SEPARATOR);
		if (scopeSeparator == -1) {
			return ComponentName.newName(componentName);
		}
		if (componentName.indexOf(ComponentName.SCOPE_SEPARATOR, scopeSeparator + 1) != -1) {
			throw new ConfigurationException(I18NConstants.ERROR_DUPLICATE_SEPARATOR__SEPARATOR
				.fill(String.valueOf(ComponentName.SCOPE_SEPARATOR)), propertyName, propertyValue);
		}
		return ComponentName.newName(componentName.substring(0, scopeSeparator),
			componentName.substring(scopeSeparator + 1));
	}

	@Override
	protected String getSpecificationNonNull(ComponentName configValue) {
		return configValue.qualifiedName();
	}

}

