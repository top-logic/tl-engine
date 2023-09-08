/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Observable {@link List} implementation that sends notifications through its owning
 * {@link AbstractConfigItem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class PropertyList<T> extends ArrayList<T> {

	private final AbstractConfigItem _container;

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link PropertyList}.
	 * 
	 * @param container
	 *        The container that handles notification.
	 * @param property
	 *        The property for which this {@link PropertyList} holds elements.
	 * @param initialValue
	 *        The initial contents.
	 */
	public PropertyList(AbstractConfigItem container, PropertyDescriptor property,
			Collection<? extends T> initialValue) {
		super(initialValue);
		_container = container;
		_property = property;
	}

	@Override
	public boolean add(T e) {
		add(size(), e);
		return true;
	}

	@Override
	public void add(int index, T element) {
		checkLegalValue(Collections.emptyList(), Collections.singletonList(element));
		internalAdd(receiver(), index, element);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return addAll(size(), c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		if (c.isEmpty()) {
			return false;
		}
		checkLegalValue(Collections.emptyList(), c);

		Change receiver = receiver();

		int n = index;
		for (T element : c) {
			internalAdd(receiver, n++, element);
		}

		return true;
	}

	@Override
	public T remove(int index) {
		checkLegalValue(Collections.singletonList(get(index)), Collections.<T> emptyList());

		return internalRemove(receiver(), index);
	}

	@Override
	public boolean remove(Object o) {
		checkLegalValue(Collections.singletonList(o), Collections.<T> emptyList());

		return internalRemove(receiver(), o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		checkLegalValue(c, Collections.<T> emptyList());

		Change receiver = receiver();
		boolean changed = false;
		for (Object element : c) {
			changed |= internalRemove(receiver, element);
		}
		return changed;
	}

	@Override
	public T set(int index, T element) {
		int oldModCount = modCount;
		checkLegalValue(Collections.singletonList(get(index)), Collections.singletonList(element));

		Change receiver = receiver();
		T oldValue = internalRemove(receiver, index);
		internalAdd(receiver, index, element);
		// ArrayList expects no modCount change on set.
		modCount = oldModCount;
		return oldValue;
	}

	@Override
	public void clear() {
		checkLegalValue(this, Collections.<T> emptyList());

		Change receiver = receiver();
		for (int n = size() - 1; n >= 0; n--) {
			internalRemove(receiver, n);
		}
	}

	private void internalAdd(Change receiver, int index, T element) {
		super.add(index, element);
	
		receiver.add(_property, index, element);
	}

	private boolean internalRemove(Change receiver, Object o) {
		int index = indexOf(o);
		if (index >= 0) {
			internalRemove(receiver, index);
			return true;
		} else {
			return false;
		}
	}

	private T internalRemove(Change receiver, int index) {
		T oldValue = super.remove(index);
		receiver.remove(_property, index, oldValue);
		return oldValue;
	}

	private Change receiver() {
		return _container.onChange(_property);
	}

	private void checkLegalValue(Collection<?> removeValues, Collection<? extends T> addValues) {
		PropertyDescriptorImpl.checkCorrectListValues(_property, this, removeValues, addValues);
	}

}
