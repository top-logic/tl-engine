/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.template.Eval.EvalException;
import com.top_logic.basic.config.template.TemplateExpression.PropertyAccess;

/**
 * {@link ModelAccess} for {@link ConfigurationItem} models.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigModelAccess implements ModelAccess {

	/**
	 * Singleton {@link ConfigModelAccess} instance.
	 */
	public static final ConfigModelAccess INSTANCE = new ConfigModelAccess();

	private ConfigModelAccess() {
		// Singleton constructor.
	}

	@Override
	public Object getProperty(Object targetObject, PropertyAccess expr) {
		if (targetObject instanceof ConfigurationItem) {
			ConfigurationItem config = (ConfigurationItem) targetObject;
			String propertyName = expr.getPropertyName();
			PropertyDescriptor property = config.descriptor().getProperty(propertyName);
			if (property == null) {
				throw new EvalException("No property '" + propertyName + "' in configuration '"
					+ config.descriptor().getConfigurationInterface().getName() + "'" + expr.location() + ".");
			}
			if (property.isMandatory() && !config.valueSet(property)) {
				return null;
			}
			return config.value(property);
		}

		throw new EvalException(
			"Cannot access properties of target value '" + targetObject + "'" + expr.location() + ".");
	}

}
