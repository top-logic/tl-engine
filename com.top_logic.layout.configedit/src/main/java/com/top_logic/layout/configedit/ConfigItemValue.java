/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;

/**
 * {@link ConfigCollection} over an {@link PropertyKind#ITEM ITEM} property, holding the item as
 * its only element, or nothing while the property has no value.
 *
 * <p>
 * An item is thereby edited by a {@link ConfigListEditorControl} like a list of at most one entry:
 * it is created with the add button, which is not offered while the item exists, and removed by
 * the remove action of its entry - unless the property is mandatory, or may not be
 * <code>null</code>.
 * </p>
 */
public final class ConfigItemValue implements ConfigCollection {

	private final ConfigurationItem _parentConfig;

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigItemValue}.
	 *
	 * @param parentConfig
	 *        The configuration owning the property.
	 * @param property
	 *        The ITEM property edited.
	 */
	public ConfigItemValue(ConfigurationItem parentConfig, PropertyDescriptor property) {
		_parentConfig = parentConfig;
		_property = property;
	}

	private ConfigurationItem item() {
		return _property.getConfigurationAccess().getConfig(_parentConfig.value(_property));
	}

	@Override
	public List<ConfigurationItem> elements() {
		List<ConfigurationItem> result = new ArrayList<>(1);
		ConfigurationItem item = item();
		if (item != null) {
			result.add(item);
		}
		return result;
	}

	@Override
	public int indexOf(ConfigurationItem item) {
		return item != null && item == item() ? 0 : -1;
	}

	@Override
	public boolean isReorderable() {
		return false;
	}

	@Override
	public boolean isKeyed() {
		return false;
	}

	@Override
	public PropertyDescriptor keyProperty(ConfigurationItem entry) {
		return null;
	}

	@Override
	public boolean hasEntryWithKey(Object key) {
		return false;
	}

	@Override
	public boolean isFull() {
		return item() != null;
	}

	/**
	 * Whether the item may be removed: not for a mandatory property, and not for one that may not be
	 * <code>null</code>, since removing the item sets the property to <code>null</code>.
	 */
	@Override
	public boolean isRemovable() {
		return !_property.isMandatory() && _property.isNullable();
	}

	@Override
	public void add(ConfigurationItem entry) {
		_parentConfig.update(_property, entry);
	}

	@Override
	public void remove(int index) {
		if (index == 0) {
			_parentConfig.update(_property, null);
		}
	}

	@Override
	public void move(int index, int delta) {
		// A single item has no order.
	}

	@Override
	public void replace(int index, ConfigurationItem replacement) {
		if (index == 0) {
			_parentConfig.update(_property, replacement);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ConfigurationItem newElement() {
		return TypedConfiguration.newConfigItem((Class<? extends ConfigurationItem>) _property.getType());
	}

	@Override
	public String label() {
		return Labels.propertyLabel(_property, false);
	}

	/**
	 * @return The label of the property: the item is the value of the property, so it is called
	 *         like it, rather than by the technical name of its type.
	 */
	@Override
	public String entryTitle(ConfigurationItem entry) {
		return label();
	}

	@Override
	public PropertyDescriptor titleProperty(ConfigurationItem entry) {
		TitleProperty declared = _property.getAnnotation(TitleProperty.class);
		if (declared == null || declared.name().isEmpty()) {
			return null;
		}
		return entry.descriptor().getProperty(declared.name());
	}

}
