/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.Collection;

/**
 * General definition for a layer of items. Wrapper over {@link Collection}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class AbstractLayer<I> {
	private Collection<I> _items;

	/**
	 * Creates an {@link AbstractLayer} for the given items.
	 */
	public AbstractLayer(Collection<I> items) {
		_items = items;
	}

	/**
	 * All items.
	 */
	public Collection<I> getAll() {
		return _items;
	}

	/**
	 * Adds the given item.
	 */
	public void add(I item) {
		_items.add(item);
	}

	/**
	 * Adds the given items.
	 */
	public void add(Collection<I> items) {
		_items.addAll(items);
	}

	/**
	 * Removes the given item from the layer.
	 */
	public void remove(I item) {
		_items.remove(item);
	}

	/**
	 * Number of contained items.
	 */
	public int size() {
		return _items.size();
	}
}
