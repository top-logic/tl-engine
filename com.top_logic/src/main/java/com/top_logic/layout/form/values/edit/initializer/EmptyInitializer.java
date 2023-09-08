/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Guard {@link Initializer} that does nothing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EmptyInitializer implements Initializer {

	/**
	 * Singleton {@link EmptyInitializer} instance.
	 */
	public static final EmptyInitializer INSTANCE = new EmptyInitializer();

	private EmptyInitializer() {
		// Singleton constructor.
	}

	@Override
	public void init(ConfigurationItem model, PropertyDescriptor property, Object value) {
		// Ignore.
	}
}