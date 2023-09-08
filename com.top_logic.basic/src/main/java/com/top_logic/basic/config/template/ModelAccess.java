/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import com.top_logic.basic.config.template.TemplateExpression.PropertyAccess;

/**
 * Algorithm for accessing properties of model values in {@link TemplateExpression}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelAccess {

	/**
	 * Resolves the property with the given name from the given target object.
	 *
	 * @param targetObject
	 *        The object to access.
	 * @param expr
	 *        The property to retrieve its value from in the context of the given object.
	 * @return The resolved property value.
	 */
	Object getProperty(Object targetObject, PropertyAccess expr);

}
