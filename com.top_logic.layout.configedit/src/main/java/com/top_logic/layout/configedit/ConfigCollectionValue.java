/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;

/**
 * The collection a {@link ConfigListEditorControl} edits: its elements as a sequence, addressed by
 * index, however the edited property actually holds them.
 *
 * <p>
 * A LIST, an ARRAY and a MAP property are one editor's worth of the same thing - a sequence of
 * configuration items the user adds to, removes from and rearranges - but three different shapes
 * underneath. This class is where that difference lives, and the only place it does: everything
 * above it works in element indices and knows nothing of {@link PropertyKind}.
 * </p>
 *
 * <p>
 * Nothing here draws anything. That is what makes the three shapes' behaviour - what an index
 * means, what reordering does to a map, which key a polymorphic entry is filed under - testable
 * without a rendering context.
 * </p>
 */
public final class ConfigCollectionValue implements ConfigCollection {

	private final ConfigurationItem _parentConfig;

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigCollectionValue}.
	 *
	 * @param parentConfig
	 *        The configuration owning the edited property.
	 * @param property
	 *        The edited LIST, ARRAY or MAP property.
	 */
	public ConfigCollectionValue(ConfigurationItem parentConfig, PropertyDescriptor property) {
		_parentConfig = parentConfig;
		_property = property;
	}

	/** The edited property. */
	public PropertyDescriptor property() {
		return _property;
	}

	/** The configuration owning the edited property. */
	public ConfigurationItem parentConfig() {
		return _parentConfig;
	}

	/**
	 * The elements currently held by the edited property.
	 *
	 * <p>
	 * For a {@link PropertyKind#LIST} property this is the configuration's own live list, so a
	 * mutation of it takes effect directly - which is what lets {@link TypedConfiguration} reject a
	 * duplicate key at the moment of the change. For a {@link PropertyKind#ARRAY} property it is a
	 * detached copy that only reaches the configuration through {@link #store(List)}.
	 * </p>
	 *
	 * <p>
	 * A {@link PropertyKind#MAP} property's own value is, under the hood, exactly as directly
	 * mutable as a LIST's - {@link Map#put(Object, Object)}/{@link Map#remove(Object)} on it take
	 * effect immediately, the same as {@link List#add(Object)}/{@link List#remove(Object)} do on a
	 * LIST's live list. But there is no live list to hand out here: what this method returns is
	 * rows to render and address by index, and a {@link Map} has no index of its own. So, like
	 * ARRAY, this is a detached copy - the map's values, in the map's current iteration order -
	 * that reaches the configuration only through {@link #store(List)} rebuilding the map from
	 * scratch.
	 * </p>
	 *
	 * <p>
	 * Callers that change the collection should go through {@link #add(ConfigurationItem)},
	 * {@link #remove(int)}, {@link #move(int, int)} or {@link #replace(int, ConfigurationItem)}
	 * rather than mutating what this returns: only those pair the mutation with the write-back the
	 * shape needs, and only a LIST would happen to survive without it.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ConfigurationItem> elements() {
		Object value = _parentConfig.value(_property);
		if (_property.kind() == PropertyKind.ARRAY) {
			return value == null ? new ArrayList<>()
				: new ArrayList<>((List<ConfigurationItem>) PropertyDescriptorImpl.arrayAsList(value));
		}
		if (_property.kind() == PropertyKind.MAP) {
			Map<?, ?> map = (Map<?, ?>) value;
			return map == null ? new ArrayList<>() : new ArrayList<>((Collection<ConfigurationItem>) map.values());
		}
		return (List<ConfigurationItem>) value;
	}

	/**
	 * Writes back the elements obtained from {@link #elements()}, for a property shape that cannot
	 * be mutated in place.
	 *
	 * <p>
	 * A {@link PropertyKind#MAP} property is rebuilt wholesale in the order it is handed, as
	 * {@code MapFormGroupBuilder} (the classic form's equivalent) does, so the order the user sees
	 * stays stable - keyed by each entry's own key property value, resolved by
	 * {@link #keyProperty(ConfigurationItem)} rather than {@link #property()}'s declared one, for
	 * the same polymorphism reason that method exists for. A duplicate key cannot reach this
	 * method: a pending entry is refused one before it is committed, which matters here in a way it
	 * does not for a keyed LIST - {@link Map#put(Object, Object)} on an existing key silently
	 * overwrites it, unlike inserting a duplicate key into a keyed LIST, which {@link TypedConfiguration}
	 * rejects with an {@link IllegalArgumentException}.
	 * </p>
	 *
	 * <p>
	 * That rebuild happens on the property's own live map, not by handing
	 * {@link ConfigurationItem#update(PropertyDescriptor, Object)} a replacement. {@code update}
	 * compares the incoming map against the current one with plain {@link Map#equals(Object)},
	 * which ignores iteration order, and skips the write when they match - so a pure reordering,
	 * where exactly the same keys map to exactly the same entries, would be discarded as "no
	 * change". Clearing the live map and refilling it in the wanted order is what actually moves an
	 * entry.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private void store(List<ConfigurationItem> elements) {
		if (_property.kind() == PropertyKind.ARRAY) {
			_parentConfig.update(_property, PropertyDescriptorImpl.listAsArray(_property, elements));
			return;
		}
		if (_property.kind() == PropertyKind.MAP) {
			Map<Object, ConfigurationItem> newMap = new LinkedHashMap<>();
			for (ConfigurationItem element : elements) {
				newMap.put(element.value(keyProperty(element)), element);
			}
			Map<Object, ConfigurationItem> liveMap = (Map<Object, ConfigurationItem>) _parentConfig.value(_property);
			if (liveMap == null) {
				_parentConfig.update(_property, newMap);
			} else {
				liveMap.clear();
				liveMap.putAll(newMap);
			}
			return;
		}
		// A LIST property was mutated in place and needs no write-back.
	}

	/** The position of the given element, or {@code -1} if it is not in the collection. */
	@Override
	public int indexOf(ConfigurationItem item) {
		List<ConfigurationItem> items = elements();
		return items != null ? items.indexOf(item) : -1;
	}

	/**
	 * Whether the collection has an order the user can rearrange.
	 *
	 * <p>
	 * A {@link PropertyKind#MAP} property counts, even though {@code setKindMap} sets its
	 * {@link PropertyDescriptor#isOrdered() ordered} flag to {@code false}: a {@link Map} has no
	 * <em>positional</em> index for {@link PropertyDescriptor#isOrdered()} to promise, but the value
	 * a MAP property actually holds is a {@code PropertyMap}, backed by a {@link LinkedHashMap} - so
	 * it does have a stable iteration order, that order is what {@link #elements()} returns and what
	 * a configuration is written out in, and {@link #store(List)} rebuilds the map in exactly the
	 * order it is handed. Rearranging that order is therefore both meaningful and persistent.
	 * </p>
	 */
	@Override
	public boolean isReorderable() {
		return _property.isOrdered() || _property.kind() == PropertyKind.MAP;
	}

	/** Whether the collection is indexed by a property of its entries. */
	@Override
	public boolean isKeyed() {
		return _property.getKeyProperty() != null;
	}

	/**
	 * The key property of the given entry, resolved against the entry's own
	 * {@link ConfigurationItem#descriptor() descriptor} rather than {@link #property()}'s declared
	 * element type.
	 *
	 * <p>
	 * For a polymorphic keyed collection an entry's actual type is typically a genuine subtype of
	 * the collection's declared element type, and its own {@link ConfigurationDescriptor} creates a
	 * fresh {@link PropertyDescriptor} instance for every property, including one inherited
	 * unchanged from a super interface. {@link PropertyDescriptor} has no
	 * {@link Object#equals(Object) equals}/{@link Object#hashCode() hashCode} override, so hiding
	 * the key from the nested editor via {@link java.util.Set#contains(Object) Set.contains} only
	 * works if both call sites use the very same instance - the one the entry's own descriptor hands
	 * out, not {@link PropertyDescriptor#getKeyProperty()}'s declared-type instance.
	 * </p>
	 *
	 * @param entry
	 *        The entry whose key property is resolved.
	 * @return {@code null} if the collection is not {@link #isKeyed() keyed}.
	 */
	@Override
	public PropertyDescriptor keyProperty(ConfigurationItem entry) {
		PropertyDescriptor declaredKeyProperty = _property.getKeyProperty();
		if (declaredKeyProperty == null) {
			return null;
		}
		return entry.descriptor().getProperty(declaredKeyProperty.getPropertyName());
	}

	/**
	 * Whether some element of the collection carries the given key.
	 *
	 * <p>
	 * Resolves each existing entry's key via {@link #keyProperty(ConfigurationItem)}, not
	 * {@link #property()}'s declared key property, for the same reason every other key lookup here
	 * does - see that method's own documentation. This is also what makes a duplicate key impossible
	 * for a {@link PropertyKind#MAP} property: unlike a keyed LIST, where {@link TypedConfiguration}
	 * itself rejects a colliding insert, {@link Map#put(Object, Object)} on an already-used key would
	 * silently overwrite the existing entry - so this check, not the underlying structure, is the
	 * only thing standing in the way.
	 * </p>
	 *
	 * @param key
	 *        The candidate key. Never {@code null} or empty.
	 */
	@Override
	public boolean hasEntryWithKey(Object key) {
		List<ConfigurationItem> items = elements();
		if (items == null) {
			return false;
		}
		for (ConfigurationItem existing : items) {
			if (key.equals(existing.value(keyProperty(existing)))) {
				return true;
			}
		}
		return false;
	}

	/** Appends the given element. */
	@Override
	public void add(ConfigurationItem entry) {
		List<ConfigurationItem> items = elements();
		items.add(entry);
		store(items);
	}

	/** Removes the element at the given index, if there is one. */
	@Override
	public void remove(int index) {
		List<ConfigurationItem> items = elements();
		if (items != null && index >= 0 && index < items.size()) {
			items.remove(index);
			store(items);
		}
	}

	/**
	 * Moves the element at the given index by the given number of positions.
	 *
	 * <p>
	 * A move that would leave the collection is ignored rather than clamped: the buttons that
	 * trigger it are disabled at the ends, so a move past them is not something the user asked for.
	 * </p>
	 *
	 * @param index
	 *        The position of the element to move.
	 * @param delta
	 *        How far to move it, negative towards the front.
	 */
	@Override
	public void move(int index, int delta) {
		List<ConfigurationItem> items = elements();
		if (items == null) {
			return;
		}
		int target = index + delta;
		if (index < 0 || index >= items.size() || target < 0 || target >= items.size()) {
			return;
		}
		ConfigurationItem item = items.remove(index);
		items.add(target, item);
		store(items);
	}

	/** Puts the given element in the place of the one at the given index. */
	@Override
	public void replace(int index, ConfigurationItem replacement) {
		List<ConfigurationItem> items = elements();
		if (items != null && index >= 0 && index < items.size()) {
			items.set(index, replacement);
			store(items);
		}
	}

	/**
	 * Creates an element of the collection's own element type - the fallback for a collection that
	 * offers no polymorphic options to pick a type from.
	 */
	@Override
	public String label() {
		return Labels.propertyLabel(_property, false);
	}

	@Override
	public PropertyDescriptor titleProperty(ConfigurationItem entry) {
		TitleProperty declared = _property.getAnnotation(TitleProperty.class);
		if (declared == null || declared.name().isEmpty()) {
			return null;
		}
		return entry.descriptor().getProperty(declared.name());
	}

	@Override
	public ConfigurationItem newElement() {
		return TypedConfiguration.newConfigItem(elementType());
	}

	/** Resolves the configuration interface to instantiate for new elements. */
	@SuppressWarnings("unchecked")
	private Class<? extends ConfigurationItem> elementType() {
		return (Class<? extends ConfigurationItem>) _property.getDefaultDescriptor().getConfigurationInterface();
	}

}
