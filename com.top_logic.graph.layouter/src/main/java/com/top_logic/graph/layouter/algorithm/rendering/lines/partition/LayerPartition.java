/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.partition;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Layered partition for objects.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayerPartition<T> {

	private List<Collection<T>> _partition;

	/**
	 * Creates a layer partition.
	 */
	public LayerPartition(List<Collection<T>> partition) {
		_partition = partition;
	}

	/**
	 * {@link LayerPartition}
	 */
	public List<Collection<T>> getPartition() {
		return _partition;
	}

	/**
	 * @see #getPartition()
	 */
	public void setPartition(List<Collection<T>> partition) {
		_partition = partition;
	}

	/**
	 * Size of the partition.
	 */
	public int size() {
		return _partition.size();
	}

	/**
	 * Return the given element in the ordered partition.
	 */
	public Collection<T> get(int index) {
		return _partition.get(index);
	}

	/**
	 * Adds a new item to the ordered partition.
	 */
	public void add(Collection<T> item) {
		_partition.add(item);
	}

	/**
	 * Reverse the given ordered layer partition.
	 */
	public void reverse() {
		Collections.reverse(_partition);
	}

}
