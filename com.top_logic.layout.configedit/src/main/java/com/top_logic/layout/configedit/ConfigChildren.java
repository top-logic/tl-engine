/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * The child {@link ConfigurationItem}s stored in a single structural property of a
 * {@link ConfigurationItem}.
 *
 * <p>
 * Addresses a property of {@link PropertyKind#LIST} or {@link PropertyKind#ITEM} kind and provides
 * the structural edit operations on it: {@link #add(ConfigurationItem)},
 * {@link #remove(ConfigurationItem)}, {@link #move(ConfigurationItem, int)} and
 * {@link #replace(ConfigurationItem, ConfigurationItem)}. An {@link PropertyKind#ITEM} property is
 * treated as a list of at most one element, so both kinds are edited through the same API.
 * </p>
 *
 * <p>
 * {@link #allowedTypes()} reports the element types the property accepts, and
 * {@link #newElement(Object)} instantiates one of them. Both delegate to
 * {@link PolymorphicOptions}, so a property with an
 * {@link com.top_logic.layout.form.values.edit.annotation.Options Options} annotation offers
 * exactly the options the declarative form editor would offer.
 * </p>
 */
public final class ConfigChildren {

	private final ConfigurationItem _owner;

	private final PropertyDescriptor _property;

	private PolymorphicOptions.Choices _choices;

	private ConfigChildren(ConfigurationItem owner, PropertyDescriptor property) {
		_owner = owner;
		_property = property;
	}

	/**
	 * Creates a {@link ConfigChildren} for the given structural property.
	 *
	 * @param owner
	 *        The configuration holding the property.
	 * @param property
	 *        The property to edit, of {@link PropertyKind#LIST} or {@link PropertyKind#ITEM} kind.
	 * @return The {@link ConfigChildren} accessing that property, or {@code null} if the property is
	 *         of another kind.
	 */
	public static ConfigChildren create(ConfigurationItem owner, PropertyDescriptor property) {
		if (owner == null || property == null || !isStructural(property)) {
			return null;
		}
		return new ConfigChildren(owner, property);
	}

	/**
	 * Whether the given property can be edited through a {@link ConfigChildren}.
	 */
	public static boolean isStructural(PropertyDescriptor property) {
		PropertyKind kind = property.kind();
		return kind == PropertyKind.LIST || kind == PropertyKind.ITEM;
	}

	/**
	 * The configuration holding the edited property.
	 */
	public ConfigurationItem getOwner() {
		return _owner;
	}

	/**
	 * The edited property.
	 */
	public PropertyDescriptor getProperty() {
		return _property;
	}

	/**
	 * Whether the edited property holds a list of elements rather than a single one.
	 *
	 * <p>
	 * Only a list can be reordered, see {@link #move(ConfigurationItem, int)}.
	 * </p>
	 */
	public boolean isList() {
		return _property.kind() == PropertyKind.LIST;
	}

	/**
	 * The current child elements, in order.
	 *
	 * <p>
	 * The result is unmodifiable; use the operations of this class to change the property.
	 * </p>
	 */
	public List<ConfigurationItem> elements() {
		if (isList()) {
			List<ConfigurationItem> list = list();
			return list == null ? List.of() : Collections.unmodifiableList(list);
		}
		ConfigurationItem value = item();
		return value == null ? List.of() : List.of(value);
	}

	/**
	 * The index of the given child, or {@code -1} if it is not an element of the edited property.
	 */
	public int indexOf(ConfigurationItem child) {
		if (child == null) {
			return -1;
		}
		if (isList()) {
			List<ConfigurationItem> list = list();
			return list == null ? -1 : list.indexOf(child);
		}
		return item() == child ? 0 : -1;
	}

	/**
	 * Whether another element can be added.
	 *
	 * <p>
	 * Always {@code true} for a list property; for a single-valued property only while it is unset.
	 * </p>
	 */
	public boolean canAdd() {
		return isList() || item() == null;
	}

	/**
	 * Appends the given child to the edited property.
	 *
	 * <p>
	 * When the property is keyed, the child is given a unique key first: elements of a keyed list are
	 * identified by that key when the configuration is read again, so two elements added without one
	 * would collapse into a single element.
	 * </p>
	 *
	 * @param child
	 *        The child to add.
	 * @return Whether the child was added.
	 */
	public boolean add(ConfigurationItem child) {
		if (child == null || !canAdd()) {
			return false;
		}
		assignUniqueKey(child);
		if (isList()) {
			list().add(child);
		} else {
			_owner.update(_property, child);
		}
		return true;
	}

	/**
	 * Gives the child a key that no current element uses, if the property is keyed by a string
	 * property that the child has not set.
	 */
	private void assignUniqueKey(ConfigurationItem child) {
		PropertyDescriptor keyProperty = _property.getKeyProperty();
		if (keyProperty == null || keyProperty.getType() != String.class) {
			return;
		}
		PropertyDescriptor childKey = child.descriptor().getProperty(keyProperty.getPropertyName());
		if (childKey == null) {
			return;
		}
		Object current = child.value(childKey);
		if (current != null && !current.toString().isEmpty()) {
			return;
		}

		String base = ConfigTagName.of(child).toLowerCase();
		String candidate = base;
		for (int i = 2; isKeyUsed(childKey, candidate); i++) {
			candidate = base + i;
		}
		child.update(childKey, candidate);
	}

	private boolean isKeyUsed(PropertyDescriptor keyProperty, String key) {
		for (ConfigurationItem element : elements()) {
			PropertyDescriptor elementKey = element.descriptor().getProperty(keyProperty.getPropertyName());
			if (elementKey != null && key.equals(element.value(elementKey))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the given child from the edited property.
	 *
	 * @param child
	 *        The child to remove.
	 * @return Whether the child was an element of the property and has been removed.
	 */
	public boolean remove(ConfigurationItem child) {
		if (indexOf(child) < 0) {
			return false;
		}
		if (isList()) {
			list().remove(child);
		} else {
			// Reset instead of setting null, so that the property becomes unset and is not
			// serialized at all.
			_owner.reset(_property);
		}
		return true;
	}

	/**
	 * Moves the given child by the given offset within the edited property.
	 *
	 * @param child
	 *        The child to move.
	 * @param delta
	 *        The number of positions to move the child; negative moves it towards the start.
	 * @return Whether the child has been moved. {@code false} if the property is not a list, the
	 *         child is not an element of it, or the target position is outside the list.
	 */
	public boolean move(ConfigurationItem child, int delta) {
		if (!isList()) {
			return false;
		}
		int index = indexOf(child);
		if (index < 0) {
			return false;
		}
		int target = index + delta;
		List<ConfigurationItem> list = list();
		if (target < 0 || target >= list.size()) {
			return false;
		}
		list.remove(index);
		list.add(target, child);
		return true;
	}

	/**
	 * Replaces the given child with another one at the same position.
	 *
	 * @param child
	 *        The child to replace.
	 * @param replacement
	 *        The child to put in its place.
	 * @return Whether the child was an element of the property and has been replaced.
	 */
	public boolean replace(ConfigurationItem child, ConfigurationItem replacement) {
		int index = indexOf(child);
		if (index < 0 || replacement == null) {
			return false;
		}
		if (isList()) {
			list().set(index, replacement);
		} else {
			_owner.update(_property, replacement);
		}
		return true;
	}

	/**
	 * The element types the edited property accepts.
	 *
	 * <p>
	 * {@link PolymorphicOptions.Choices#hasOptions()} is {@code false} for a property with a single
	 * fixed element type; {@link #newElement(Object)} then ignores its argument.
	 * </p>
	 *
	 * @implNote Resolved on first access and cached. Resolving the options of a polymorphic property
	 *           loads every candidate implementation class, which is too expensive to do for every
	 *           property of a configuration that is merely displayed.
	 */
	public PolymorphicOptions.Choices allowedTypes() {
		if (_choices == null) {
			_choices = PolymorphicOptions.compute(_owner, _property);
		}
		return _choices;
	}

	/**
	 * Creates a new element for the edited property.
	 *
	 * <p>
	 * The element is not added; pass it to {@link #add(ConfigurationItem)} or
	 * {@link #replace(ConfigurationItem, ConfigurationItem)}.
	 * </p>
	 *
	 * @param typeOption
	 *        One of the options of {@link #allowedTypes()}, or {@code null} to create an element of
	 *        the property's default type.
	 * @return The new element.
	 */
	@SuppressWarnings("unchecked")
	public ConfigurationItem newElement(Object typeOption) {
		if (typeOption != null && allowedTypes().hasOptions()) {
			return (ConfigurationItem) allowedTypes().mapping().toSelection(typeOption);
		}
		return TypedConfiguration.newConfigItem(
			(Class<? extends ConfigurationItem>) _property.getDefaultDescriptor().getConfigurationInterface());
	}

	@SuppressWarnings("unchecked")
	private List<ConfigurationItem> list() {
		return (List<ConfigurationItem>) _owner.value(_property);
	}

	private ConfigurationItem item() {
		Object value = _owner.value(_property);
		return value instanceof ConfigurationItem item ? item : null;
	}

	@Override
	public String toString() {
		return ConfigChildren.class.getSimpleName() + "(" + _property.getPropertyName() + ")";
	}
}
