/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Log;

/**
 * Expects that the given {@link PropertyDescriptor} has no
 * {@link PropertyDescriptor#getKeyProperty() key property} so each method which
 * needs the key property results in an error.
 * 
 * <p>
 * Items are addressed by their position or, for items already in the list, by identity.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleListStorage<K, V> extends AbstractListStorage<K, V> {

	private ArrayList<V> _list;

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link SimpleListStorage}.
	 * 
	 * @param protocol
	 *        See {@link AbstractListStorage#AbstractListStorage(Log)}.
	 * @param property
	 *        The configuration property this storage builds its value.
	 * @param baseList
	 *        The original list that is being updated.
	 */
	public SimpleListStorage(Log protocol, PropertyDescriptor property, List<? extends V> baseList) {
		super(protocol);
		_property = property;
		if (baseList == null) {
			_list = new ArrayList<>();
		} else {
			_list = new ArrayList<>(baseList);
		}
	}

	@Override
	void append(V item) {
		_list.add(item);
	}

	@Override
	void prepend(V item) {
		_list.add(0, item);
	}

	@Override
	void insertBefore(K reference, V item) {
		noReference(reference);
	}

	@Override
	void insertAfter(K reference, V item) {
		noReference(reference);
	}

	@Override
	void remove(K reference) {
		noReference(reference);
	}

	@Override
	void moveToStart(V item) {
		int index = indexOf(item);
		if (index >= 0) {
			_list.remove(index);
			_list.add(0, item);
		}
	}

	@Override
	void moveToEnd(V item) {
		int index = indexOf(item);
		if (index >= 0) {
			_list.remove(index);
			_list.add(item);
		}
	}

	@Override
	void moveBefore(V item, K reference) {
		noReference(reference);
	}

	@Override
	void moveAfter(V item, K reference) {
		noReference(reference);
	}

	@Override
	void replace(V oldItem, V newItem) {
		int index = indexOf(oldItem);
		if (index >= 0) {
			_list.set(index, newItem);
		}
	}

	/**
	 * The position of the given item (compared by identity) in the list, or <code>-1</code> after
	 * reporting an error, if the item is not contained.
	 */
	private int indexOf(V item) {
		for (int n = 0, cnt = _list.size(); n < cnt; n++) {
			if (_list.get(n) == item) {
				return n;
			}
		}
		cannotIdentify(item);
		return -1;
	}

	@Override
	V resolveReference(K reference) {
		noReference(reference);
		return null;
	}
	
	@Override
	V resolveReferenceOrNull(K reference) {
		return null;
	}

	@Override
	V resolveIndexOrNull(int index) {
		if (index < 0 || index >= _list.size()) {
			return null;
		}
		return _list.get(index);
	}

	@Override
	int size() {
		return _list.size();
	}

	@Override
	List<V> toList() {
		return _list;
	}

	private void cannotIdentify(V item) {
		StringBuilder message = new StringBuilder();
		message.append("Property '");
		message.append(_property);
		message.append("' does not contain item '");
		message.append(item);
		message.append('\'');
		_protocol.error(message.toString());
	}

	private void noReference(Object reference) {
		StringBuilder message = new StringBuilder();
		message.append("Property '");
		message.append(_property);
		message.append("' has no key property. It is not possible to find a corresponding element for reference '");
		message.append(reference);
		message.append('\'');
		_protocol.error(message.toString());
	}

}
