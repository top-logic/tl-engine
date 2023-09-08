/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;


/**
 * Observable {@link List} implementation that sends notifications through its owning
 * {@link ItemChangeHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class ItemList<T> extends ArrayList<T> {

	private final ItemChangeHandler _handler;

	private final PropertyDescriptor _property;

	private boolean _modified;

	/**
	 * Creates a {@link ItemList}.
	 * 
	 * @param handler
	 *        The container that handles notification.
	 * @param property
	 *        The property for which this {@link ItemList} holds elements.
	 */
	public ItemList(ItemChangeHandler handler, PropertyDescriptor property) {
		_handler = handler;
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

		ItemChangeHandler receiver = receiver();

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

		ItemChangeHandler receiver = receiver();
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

		ItemChangeHandler receiver = receiver();
		T oldValue = internalRemove(receiver, index);
		internalAdd(receiver, index, element);
		// ArrayList expects no modCount change on set.
		modCount = oldModCount;
		return oldValue;
	}

	@Override
	public void clear() {
		checkLegalValue(this, Collections.<T> emptyList());

		ItemChangeHandler receiver = receiver();
		for (int n = size() - 1; n >= 0; n--) {
			internalRemove(receiver, n);
		}
	}

	private void internalAdd(ItemChangeHandler receiver, int index, T element) {
		super.add(index, element);
	
		receiver.__notifyAdd(_property, index, element);
		markModified();
	}

	/**
	 * Whether this list has been modified after construction (either creating the containing
	 * configuration item or reading it from XML).
	 */
	public boolean isModified() {
		return _modified;
	}

	@FrameworkInternal
	private void markModified() {
		setModified(true);
	}

	/**
	 * Initializes the list's modified state.
	 * 
	 * @see #isModified()
	 */
	@FrameworkInternal
	public final void setModified(boolean value) {
		_modified = value;
	}

	/**
	 * Updates this list to the new state.
	 *
	 * @param value
	 *        The new contents.
	 * @param isSet
	 *        The new {@link #isModified()} marker.
	 */
	@FrameworkInternal
	public final void set(Collection<T> value, boolean isSet) {
		clear();
		addAll(value);
		setModified(isSet);
	}

	/**
	 * Resets the modification marker.
	 * 
	 * @see #isModified()
	 */
	@FrameworkInternal
	public void markUnmodified() {
		_modified = false;
	}

	private boolean internalRemove(ItemChangeHandler receiver, Object o) {
		int index = indexOf(o);
		if (index >= 0) {
			internalRemove(receiver, index);
			return true;
		} else {
			return false;
		}
	}

	private T internalRemove(ItemChangeHandler receiver, int index) {
		T oldValue = super.remove(index);
		receiver.__notifyRemove(_property, index, oldValue);
		markModified();
		return oldValue;
	}

	private ItemChangeHandler receiver() {
		return _handler;
	}

	private void checkLegalValue(Collection<?> removeValues, Collection<? extends T> addValues) {
		PropertyDescriptorImpl.checkCorrectListValues(_property, this, removeValues, addValues);
	}

}
