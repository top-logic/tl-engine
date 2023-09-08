/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.Collection;

/**
 * {@link TraversalListener} that collects all reported nodes in a buffer.
 * 
 * @param <T> The node type of the graph, from which are nodes collected.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TraversalCollector<T> implements TraversalListener<T> {

	private final Collection<T> buffer;

	/**
	 * Creates a {@link TraversalCollector}.
	 * 
	 * @param buffer
	 *        The buffer to add all nodes reported in a {@link Traversal}.
	 */
	public TraversalCollector(Collection<T> buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public boolean onVisit(T node, int depth) {
		this.buffer.add(node);
		return true;
	}
	
	/**
	 * The buffer passed to the constructor.
	 */
	public Collection<T> getNodes() {
		return buffer;
	}

}
