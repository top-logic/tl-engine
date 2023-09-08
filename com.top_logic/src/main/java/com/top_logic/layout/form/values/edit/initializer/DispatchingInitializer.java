/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.Utils;

/**
 * An {@link Initializer} that dispatches its call with an {@link InitializerProvider}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DispatchingInitializer implements Initializer {

	private final InitializerProvider _provider;

	/**
	 * Creates a {@link DispatchingInitializer}.
	 * 
	 * @param initializerProvider
	 *        See {@link #getInitializerProvider()}.
	 */
	public DispatchingInitializer(InitializerProvider initializerProvider) {
		_provider = Utils.requireNonNull(initializerProvider);
	}

	/**
	 * The {@link InitializerProvider} providing {@link Initializer}s to call when setting new
	 * values.
	 */
	public InitializerProvider getInitializerProvider() {
		return _provider;
	}

	@Override
	public void init(ConfigurationItem model, PropertyDescriptor property, Object value) {
		_provider.getInitializer(model.descriptor(), property).init(model, property, value);
	}

}
