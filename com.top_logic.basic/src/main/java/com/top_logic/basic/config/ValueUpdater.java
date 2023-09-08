/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An {@link Updater} which is also an {@link ConfigurationListener} and calls its inner
 * {@link Updater}s whenever the value on the property changes for which it is responsible.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class ValueUpdater extends Updater implements ConfigurationListener {

	private final PropertyDescriptor _property;

	private final List<Updater> _valueUpdaters;

	private ConfigurationListener _updateListener;

	/**
	 * Creates a {@link ValueUpdater}.
	 * 
	 * @param property
	 *        Is not allowed to be <code>null</code>.
	 * @param valueUpdaters
	 *        Must not be changed afterwards. Is not allowed to be null. Is not allowed to contain
	 *        null.
	 */
	public ValueUpdater(PropertyDescriptor property, List<Updater> valueUpdaters) {
		_property = property;
		_valueUpdaters = valueUpdaters;
	}

	@Override
	public void onChange(ConfigurationChange change) {
		switch (change.getKind()) {
			case ADD:
				installValue((ConfigurationItem) change.getNewValue());
				break;
			case REMOVE:
				uninstallValue((ConfigurationItem) change.getOldValue());
				break;
			case SET:
				forValue(change.getProperty(), change.getOldValue(), this::uninstallValue);
				forValue(change.getProperty(), change.getNewValue(), this::installValue);
				break;
			default:
				break;

		}

		_updateListener.onChange(change);
	}

	@Override
	public final void install(ConfigurationItem item, ConfigurationListener updateListener) {
		if (_updateListener != null && updateListener != _updateListener) {
			throw new IllegalArgumentException("Must not install different UpdateListener.");
		}
		_updateListener = updateListener;

		PropertyDescriptor runtimeProperty = getRuntimeProperty(item);
		item.addConfigurationListener(runtimeProperty, this);

		if (item.valueSet(runtimeProperty) || !runtimeProperty.isMandatory()) {
			forValue(runtimeProperty, item.value(runtimeProperty), this::installValue);
		}
	}

	private void forValue(PropertyDescriptor runtimeProperty, Object value, Consumer<ConfigurationItem> callback) {
		if (value == null) {
			return;
		}
		switch (runtimeProperty.kind()) {
			case ITEM:
				callback.accept((ConfigurationItem) value);
				break;
			case LIST:
				forCollectionValue(callback, (Collection<?>) value);
				break;
			case MAP:
				forCollectionValue(callback, ((Map<?, ?>) value).values());
				break;
			case ARRAY:
				for (int i = 0; i < Array.getLength(value); i++) {
					callback.accept((ConfigurationItem) Array.get(value, i));
				}
				break;
			case DERIVED:
				if (value instanceof Collection) {
					forCollectionValue(callback, (Collection<?>) value);
				} else if (value instanceof Map) {
					forCollectionValue(callback, ((Map<?, ?>) value).values());
				} else if (value.getClass().isArray()) {
					for (int i = 0; i < Array.getLength(value); i++) {
						callback.accept((ConfigurationItem) Array.get(value, i));
					}
				} else {
					callback.accept((ConfigurationItem) value);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected Kind " + runtimeProperty.kind());
		}
	}

	private void forCollectionValue(Consumer<ConfigurationItem> callback, Collection<?> value) {
		for (Object t : value) {
			callback.accept((ConfigurationItem) t);
		}
	}

	private void installValue(ConfigurationItem valueModel) {
		for (Updater updater : _valueUpdaters) {
			updater.install(valueModel, _updateListener);
		}
	}

	@Override
	public final void uninstall(ConfigurationItem item, ConfigurationListener updateListener) {
		PropertyDescriptor runtimeProperty = getRuntimeProperty(item);
		item.removeConfigurationListener(runtimeProperty, this);

		if (item.valueSet(runtimeProperty) || !runtimeProperty.isMandatory()) {
			forValue(runtimeProperty, item.value(runtimeProperty), this::uninstallValue);
		}
	}

	private void uninstallValue(ConfigurationItem valueModel) {
		for (Updater updater : _valueUpdaters) {
			updater.uninstall(valueModel, _updateListener);
		}
	}

	private PropertyDescriptor getRuntimeProperty(ConfigurationItem item) {
		return item.descriptor().getProperty(_property.getPropertyName());
	}

}
