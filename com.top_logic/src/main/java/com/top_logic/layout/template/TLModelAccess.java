/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.template.ConfigModelAccess;
import com.top_logic.basic.config.template.ModelAccess;
import com.top_logic.basic.config.template.TemplateExpression.PropertyAccess;
import com.top_logic.model.TLObject;

/**
 * {@link ModelAccess} for {@link ConfigurationItem}s, {@link TLObject}s and
 * {@link WithProperties}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLModelAccess implements ModelAccess {

	/**
	 * Singleton {@link TLModelAccess} instance.
	 */
	public static final TLModelAccess INSTANCE = new TLModelAccess();

	private TLModelAccess() {
		// Singleton constructor.
	}

	@Override
	public Object getProperty(Object targetObject, PropertyAccess expr) {
		String propertyName = expr.getPropertyName();
		if (targetObject instanceof WithProperties) {
			WithProperties properties = (WithProperties) targetObject;
			try {
				return properties.getPropertyValue(propertyName);
			} catch (NoSuchPropertyException ex) {
				throw WithProperties.reportError(properties, propertyName);
			}
		} else if (targetObject instanceof TLObject) {
			return ((TLObject) targetObject).tValueByName(propertyName);
		} else {
			return ConfigModelAccess.INSTANCE.getProperty(targetObject, expr);
		}
	}

}
