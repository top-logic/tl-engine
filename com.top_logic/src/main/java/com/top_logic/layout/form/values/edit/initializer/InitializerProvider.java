/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Provider for {@link Initializer}s for certain configuration item properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InitializerProvider extends TypedAnnotatable {

	/**
	 * The {@link Initializer} to be called whenever the given property on the given type is set.
	 * 
	 * @param descriptor
	 *        The type an initializer is requested for.
	 * @param property
	 *        The property being set.
	 * @return The {@link Initializer} to call. Is not allowed to be null.
	 */
	Initializer getInitializer(ConfigurationDescriptor descriptor, PropertyDescriptor property);

}
