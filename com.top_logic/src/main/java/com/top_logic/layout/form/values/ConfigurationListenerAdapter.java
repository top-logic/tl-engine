/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationListener;

/**
 * An adapter from {@link Listener} to {@link ConfigurationListener}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class ConfigurationListenerAdapter implements ConfigurationListener {

	private final Value<?> _self;

	private final Listener _listener;

	ConfigurationListenerAdapter(Value<?> self, Listener listener) {
		_self = self;
		_listener = listener;
	}

	@Override
	public void onChange(ConfigurationChange change) {
		_listener.handleChange(_self);
	}

}
