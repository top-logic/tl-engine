/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.List;

import com.top_logic.basic.CollectionUtil;

/**
 * Operations on a list of configuration-related things.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ListHandler {

	private final AbstractListStorage<Object, ConfigurationItem> _storage;

	public ListHandler(AbstractListStorage<Object, ConfigurationItem> storage) {
		_storage = storage;
	}

	public List<ConfigurationItem> toList() {
		return _storage.toList();
	}

	public void remove(Object reference) {
		_storage.remove(reference);
	}

	public ConfigurationItem resolveReferenceOrNull(Object reference) {
		ConfigurationItem value = _storage.resolveReferenceOrNull(reference);
		if (value == null) {
			return null;
		}
		return value;
	}

	public ConfigurationItem resolveReference(Object reference) {
		return _storage.resolveReferenceOrNull(reference);
	}

	public void update(ConfigurationItem newConfig) {
		if (newConfig != null) {
			_storage.update(newConfig);
		}
	}

	public void moveToStart(ConfigurationItem newConfig) {
		if (newConfig != null) {
			_storage.moveToStart(newConfig);
		}
	}

	public void moveToEnd(ConfigurationItem newConfig) {
		if (newConfig != null) {
			_storage.moveToEnd(newConfig);
		}
	}

	public void moveBefore(ConfigurationItem newConfig, Object reference) {
		if (newConfig != null) {
			_storage.moveBefore(newConfig, reference);
		}
	}

	public void moveAfter(ConfigurationItem newConfig, Object reference) {
		if (newConfig != null) {
			_storage.moveAfter(newConfig, reference);
		}
	}

	public void prepend(ConfigurationItem newConfig) {
		if (newConfig != null) {
			_storage.prepend(newConfig);
		}
	}

	public void append(ConfigurationItem newConfig) {
		if (newConfig != null) {
			_storage.append(newConfig);
		}
	}

	public void insertBefore(Object reference, ConfigurationItem newConfig) {
		if (newConfig != null) {
			_storage.insertBefore(reference, newConfig);
		}
	}

	public void insertAfter(Object reference, ConfigurationItem newConfig) {
		if (newConfig != null) {
			_storage.insertAfter(reference, newConfig);
		}
	}

	static ListHandler createListHandler(InstantiationContext context, PropertyDescriptor property, List<?> baseList) {
		/* Here, only values from default values could be ConfiguredInstances and not ConfigBuilder,
		 * as the ConfigReader copies its fallback item and by that transforms it into a
		 * ConfigBuilder tree. But properties of kind List and Map are not allowed to have default
		 * values. This cast is therefore safe. */
		List<? extends ConfigurationItem> baseConfigs =
			(baseList == null) ? null : CollectionUtil.dynamicCastView(ConfigurationItem.class, baseList);

		final PropertyDescriptor keyProperty = property.getKeyProperty();
		AbstractListStorage<Object, ConfigurationItem> storage;
		if (keyProperty == null) {
			storage = new SimpleListStorage<>(context, property, baseConfigs);
		} else {
			storage = new IndexedListStorage<>(context, keyProperty, baseConfigs);
		}

		return new ListHandler(storage);
	}

}