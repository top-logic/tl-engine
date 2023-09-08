/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.top_logic.basic.CollectionUtil;

/**
 * Reports all descendant nodes of a {@link TreeView} node in
 * <b>d</b>epth <b>f</b>irst <b>s</b>earch (pre-)order.
 * 
 * <p>This iterator cannot iterate over colletction containung null-objects,
 * for a better solution see {@link com.top_logic.basic.col.FilteredIterator}.</p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DescendantDFSIterator<T> implements Iterator<T> {
    
    /** View of a tree. Used for lookup of the children of a given node. */ 
	private TreeView<T> view;

    /** An {@link Iterator} that reports elements from descendant nodes. */ 
    private DescendantDFSIterator<T> childSource;

    /** Iterator of the children for the root node passed to the constructor. */
	private List<? extends T> source;
	
	private int _sourceIndex;

	private Comparator<T> _treeLayerComparator;

    /**
	 * The next object that will be returned by a call to {@link #next()}, or
	 * <code>null</code>, if there is no next object.
	 * 
	 * <p>
	 * The next result of this {@link Iterator} is provided either by
	 * {@link #source} or {@link #childSource}.
	 * </p>
	 */
	private T nextResult;


	/**
	 * Constructs a new {@link Iterator} that reports all descendant nodes in
	 * DFS (pre-)order.
     * 
     * @param node 
     *     The root node of the tree, of which this iterator reports descendant 
     *     nodes. 
	 */
	public DescendantDFSIterator(TreeView<T> view, T node) {
		this(view, node, false);
	}

	/**
	 * Constructs a new {@link Iterator} that reports all nodes from a given tree in DFS
	 * (pre-)order.
	 * 
	 * @param node
	 *        The root node of the tree, of which this iterator reports descendant nodes.
	 * @param isRootNodeIncluded
	 *        Whether root node shall be included in iterator output, or not.
	 */
	@SuppressWarnings("unchecked")
	public DescendantDFSIterator(TreeView<T> view, T node, boolean isRootNodeIncluded) {
		this(view, node, isRootNodeIncluded, (Comparator<T>) Equality.INSTANCE);
	}

	/**
	 * Create a new {@link DescendantDFSIterator}, that reports all nodes in DFS order, whereby node
	 * order of a tree layer will be defined by given comparator.
	 */
	public DescendantDFSIterator(TreeView<T> view, T node, boolean isRootNodeIncluded,
			Comparator<T> treeLayerComparator) {
		this.view = view;
		_treeLayerComparator = treeLayerComparator;
		source = CollectionUtil.toList(view.getChildIterator(node));
		Collections.sort(source, _treeLayerComparator);
		_sourceIndex = 0;
		if (isRootNodeIncluded) {
			nextResult = node;
		} else {
			findNext();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		return nextResult != null;
	}

	@Override
	public T next() {
		T result = nextResult;

		if (result == null) 
			throw new NoSuchElementException();
		
		findNext();
		
		return result;
	}
	
    /**
     * Initializes {@link #nextResult}.
     */
	private void findNext() {
		while (true) {
			if (childSource != null) {
				// Delegate to the child iterator.
				if (childSource.hasNext()) {
					this.nextResult = childSource.next();
					return;
				} else {
					childSource = null;
					// The child iterator is exhausted, fall through.
				}
			}
			
			if (_sourceIndex < source.size()) {
				T nextChild = source.get(_sourceIndex++);
				if (! view.isLeaf(nextChild)) {
					// Remember to descend into the sub-tree rooted at nextChild.
					this.childSource = 
						new DescendantDFSIterator<>(view, nextChild, false, _treeLayerComparator);
				}

				// Next result element found.
				this.nextResult = nextChild;
				return;
			} else {
				// No more elements in this iterator.
				this.nextResult = null;
				return;
			}
		}
	}
}