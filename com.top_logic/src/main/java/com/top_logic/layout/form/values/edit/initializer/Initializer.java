/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Algorithm that is called, whenever a certain property on a {@link ConfigurationItem} is set (or
 * added to).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Initializer {

	/**
	 * Hook being called after the given property got a new value.
	 * 
	 * @param model
	 *        The modified model.
	 * @param property
	 *        The property being set or added to.
	 * @param value
	 *        The set or added value.
	 */
	void init(ConfigurationItem model, PropertyDescriptor property, Object value);

}