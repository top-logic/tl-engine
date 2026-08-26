/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.IdentityHashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Looks up the {@link ConfigFieldModel} that displays a given {@link PropertyDescriptor} of a
 * given {@link ConfigurationItem}.
 *
 * <p>
 * A validation failure names an item and a property (that is what
 * {@code ConstraintFailure} carries), but the field that shows it is a {@link ConfigFieldModel}
 * buried in the control tree. This index is filled while the editor builds and consulted when
 * Apply refuses.
 * </p>
 *
 * <p>
 * Both maps are {@link IdentityHashMap}: a {@link ConfigurationItem} is identity-equal anyway, but
 * {@link PropertyDescriptor} overrides neither {@link Object#equals(Object)} nor
 * {@link Object#hashCode()} - and a subtype's descriptor hands out its own instance even for an
 * inherited property. A plain {@link java.util.HashMap} would already behave this way by
 * accident; using {@link IdentityHashMap} explicitly says so and keeps a later change from
 * silently drifting to equality-based lookup.
 * </p>
 */
public final class ConfigFieldIndex {

	private final Map<ConfigurationItem, Map<PropertyDescriptor, ConfigFieldModel>> _fields =
		new IdentityHashMap<>();

	/**
	 * Registers the {@link ConfigFieldModel} that displays the given property of the given
	 * configuration item.
	 *
	 * @param item
	 *        The configuration item the property belongs to.
	 * @param property
	 *        The property the model displays.
	 * @param model
	 *        The field model to register.
	 */
	public void register(ConfigurationItem item, PropertyDescriptor property, ConfigFieldModel model) {
		_fields.computeIfAbsent(item, any -> new IdentityHashMap<>()).put(property, model);
	}

	/**
	 * The {@link ConfigFieldModel} previously {@link #register(ConfigurationItem, PropertyDescriptor, ConfigFieldModel)
	 * registered} for the given item and property, or {@code null} if none was.
	 *
	 * @param item
	 *        The configuration item the property belongs to.
	 * @param property
	 *        The property to look up.
	 */
	public ConfigFieldModel lookup(ConfigurationItem item, PropertyDescriptor property) {
		Map<PropertyDescriptor, ConfigFieldModel> byProperty = _fields.get(item);
		return byProperty == null ? null : byProperty.get(property);
	}

	/**
	 * Forgets all registrations.
	 *
	 * <p>
	 * Every render cycle refills the index, so what an earlier cycle put there must go.
	 * </p>
	 */
	public void clear() {
		_fields.clear();
	}
}
