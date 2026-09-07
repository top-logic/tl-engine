/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.model.FieldModel;

/**
 * A {@link ConfigCollection} whose elements are the value of a form field - a multi-valued model
 * attribute holding {@link ConfigurationItem}s.
 *
 * <p>
 * The counterpart of {@link ConfigCollectionValue}: there is no surrounding configuration and no
 * property, so nothing here is decided by a property's shape or its annotations. What the collection
 * is called and what its elements are typed as have to be told to it, since a field cannot say
 * either.
 * </p>
 *
 * <p>
 * Not keyed. A key comes from a {@code @Key} annotation on a configuration property, and there is no
 * such property here - so nothing indexes the elements, a new entry is taken straight away instead
 * of waiting to be given a key, and two elements carrying the same name are the caller's business
 * rather than this collection's.
 * </p>
 *
 * <p>
 * Every change hands the field a <em>new</em> list. A field decides whether anything happened by
 * comparing what it is given with what it holds, so mutating its own list in place would leave the
 * two the same object and the surrounding form would never learn of the change - no dirty state, and
 * nothing to save. For the same reason {@link #elements()} answers a copy: what a caller does to
 * that must not reach the field except through the operations here.
 * </p>
 */
public final class FieldCollectionValue implements ConfigCollection {

	private final FieldModel _field;

	private final Class<? extends ConfigurationItem> _elementType;

	private final String _label;

	/**
	 * Creates a {@link FieldCollectionValue}.
	 *
	 * @param field
	 *        The field holding the elements. Its value is a {@link Collection} of
	 *        {@link ConfigurationItem}s, or {@code null} for none.
	 * @param elementType
	 *        The configuration interface {@link #newElement()} creates an instance of.
	 * @param label
	 *        What the collection is called, for the add button.
	 */
	public FieldCollectionValue(FieldModel field, Class<? extends ConfigurationItem> elementType, String label) {
		_field = field;
		_elementType = elementType;
		_label = label;
	}

	@Override
	public List<ConfigurationItem> elements() {
		Object value = _field.getValue();
		if (!(value instanceof Collection<?> collection)) {
			// An unset field is an empty collection: a multi-valued attribute that was never filled
			// in is not an error, it is a collection with nothing in it yet.
			return new ArrayList<>();
		}
		List<ConfigurationItem> result = new ArrayList<>(collection.size());
		for (Object each : collection) {
			result.add((ConfigurationItem) each);
		}
		return result;
	}

	/**
	 * Hands the field the given elements as a new list.
	 */
	private void store(List<ConfigurationItem> elements) {
		_field.setValue(new ArrayList<>(elements));
	}

	@Override
	public int indexOf(ConfigurationItem item) {
		return elements().indexOf(item);
	}

	@Override
	public boolean isReorderable() {
		return true;
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
	public void add(ConfigurationItem entry) {
		List<ConfigurationItem> elements = elements();
		elements.add(entry);
		store(elements);
	}

	@Override
	public void remove(int index) {
		List<ConfigurationItem> elements = elements();
		elements.remove(index);
		store(elements);
	}

	@Override
	public void move(int index, int delta) {
		List<ConfigurationItem> elements = elements();
		int target = index + delta;
		if (target < 0 || target >= elements.size()) {
			return;
		}
		Collections.swap(elements, index, target);
		store(elements);
	}

	@Override
	public void replace(int index, ConfigurationItem replacement) {
		List<ConfigurationItem> elements = elements();
		elements.set(index, replacement);
		store(elements);
	}

	@Override
	public ConfigurationItem newElement() {
		return TypedConfiguration.newConfigItem(_elementType);
	}

	@Override
	public String label() {
		return _label;
	}

	@Override
	public PropertyDescriptor titleProperty(ConfigurationItem entry) {
		// No property, so nothing can carry a @TitleProperty here. The editor falls back to what
		// the entry's own type says.
		return null;
	}

}
