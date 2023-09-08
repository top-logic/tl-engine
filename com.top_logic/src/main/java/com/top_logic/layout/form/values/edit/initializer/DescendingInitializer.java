/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.Utils;

/**
 * An {@link Initializer} that descends into inner {@link ConfigurationItem}s recursively, to call
 * its inner {@link Initializer} not only on the original property but also for every inner property
 * of the given value, recursively.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class DescendingInitializer implements Initializer, TypedAnnotatable {

	private final DispatchingInitializer _inner;

	private final TypedAnnotatable _settings;

	/**
	 * Creates an inner {@link DescendingInitializer}.
	 * 
	 * @param inner
	 *        Is not allowed to be null.
	 * @param settings
	 *        {@link TypedAnnotatable} with custom settings.
	 */
	public DescendingInitializer(DispatchingInitializer inner, TypedAnnotatable settings) {
		_settings = settings;
		_inner = Utils.requireNonNull(inner);
	}

	/**
	 * The inner {@link Initializer} to apply.
	 */
	public DispatchingInitializer getInitializer() {
		return _inner;
	}

	@Override
	public void init(ConfigurationItem model, PropertyDescriptor property, Object value) {
		internalInit(model, property, value);
	}

	private void internalInit(ConfigurationItem model, PropertyDescriptor property, Object value) {
		_inner.init(model, property, value);
		if (value instanceof ConfigurationItem) {
			initAllProperties((ConfigurationItem) value);
		}
	}

	private void initAllProperties(ConfigurationItem config) {
		if (config == null) {
			return;
		}
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			initAllConfigs(config, property, ConfigUtil.getChildConfigs(config, property));
		}
	}

	private void initAllConfigs(ConfigurationItem parent, PropertyDescriptor property,
			Iterable<ConfigurationItem> configs) {
		for (ConfigurationItem child : configs) {
			internalInit(parent, property, child);
		}
	}

	@Override
	public <T> T get(Property<T> property) {
		return _settings.get(property);
	}

	@Override
	public <T> T set(Property<T> property, T value) {
		return _settings.set(property, value);
	}

	@Override
	public boolean isSet(Property<?> property) {
		return _settings.isSet(property);
	}

	@Override
	public <T> T reset(Property<T> property) {
		return _settings.reset(property);
	}

}
