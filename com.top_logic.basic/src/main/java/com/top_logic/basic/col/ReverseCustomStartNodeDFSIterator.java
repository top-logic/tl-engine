/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * Depth first iterator of a {@link StructureView} with custom root node and custom traversal start
 * node, which produces reverse order to {@link CustomStartNodeDFSIterator}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ReverseCustomStartNodeDFSIterator<T> implements Iterator<T> {

	/** Error message for illegal traversal start node */
	public static final String ILLEGAL_START_NODE_MESSAGE = "Start node must be in subtree of root node!";

	private StructureView<T> _structureView;
	private Iterator<T> _childIterator;
	private List<T> _neighbourList;
	private int _neighbourIndex;
	private T _rootNode;
	private boolean _isRootNodeIncluded;
	private Comparator<T> _treeLayerComparator;

	private T _nextResult;


	/**
	 * Create a new {@link ReverseCustomStartNodeDFSIterator}, that returns nodes in reverse order
	 * to {@link CustomStartNodeDFSIterator}.
	 */
	@SuppressWarnings("unchecked")
	public ReverseCustomStartNodeDFSIterator(StructureView<T> structureView, T rootNode, T traversalStartNode) {
		this(structureView, rootNode, traversalStartNode, true, (Comparator<T>) Equality.INSTANCE);
	}

	/**
	 * Create a new {@link ReverseCustomStartNodeDFSIterator}, that returns nodes in reverse order
	 * to {@link CustomStartNodeDFSIterator} , whereby natural order of a tree layer will be defined
	 * by given comparator.
	 */
	public ReverseCustomStartNodeDFSIterator(StructureView<T> structureView, T rootNode, T traversalStartNode,
			boolean isRootNodeIncluded, Comparator<T> treeLayerComparator) {
		assertStartNodeUnderRootNode(structureView, rootNode, traversalStartNode);

		_structureView = structureView;
		_rootNode = rootNode;
		_isRootNodeIncluded = isRootNodeIncluded;
		_treeLayerComparator = treeLayerComparator;
		_childIterator = Collections.emptyIterator();
		_neighbourList = createNeighbourList(traversalStartNode);
		if (_rootNode != traversalStartNode || _isRootNodeIncluded) {
			_nextResult = traversalStartNode;
			_neighbourIndex--;
		} else {
			_nextResult = null;
			_neighbourIndex = -1;
		}
	}

	private void assertStartNodeUnderRootNode(StructureView<T> structureView, T rootNode, T startNode) {
		if (rootNode != startNode) {
			T parentNode = structureView.getParent(startNode);
			while (parentNode != null) {
				if (parentNode == rootNode) {
					return;
				}
				parentNode = structureView.getParent(parentNode);
			}
			throw new IllegalArgumentException(ILLEGAL_START_NODE_MESSAGE);
		}
	}

	@Override
	public boolean hasNext() {
		return _nextResult != null;
	}

	@Override
	public T next() {
		T result = _nextResult;

		if (result == null)
			throw new NoSuchElementException();

		findPrevious();

		return result;
	}

	private void findPrevious() {
		if (_childIterator.hasNext()) {
			_nextResult = _childIterator.next();
		} else {
			if (_neighbourIndex >= 0) {
				_nextResult = neighbourChildNode();
			} else {
				retrieveParentNeighbourList();
				if (_neighbourIndex >= 0) {
					_nextResult = _neighbourList.get(_neighbourIndex--);
				} else {
					_nextResult = null;
				}
			}
		}
	}

	private void retrieveParentNeighbourList() {
		if (_nextResult != _rootNode) {
			T parentNode = _structureView.getParent(_nextResult);
			if (parentNode != _rootNode || _isRootNodeIncluded) {
				_neighbourList = createNeighbourList(parentNode);
			} else {
				_neighbourList = Collections.emptyList();
				_neighbourIndex = -1;
			}
		} else {
			_neighbourList = Collections.emptyList();
			_neighbourIndex = -1;
		}
	}

	private T neighbourChildNode() {
		T neighbourNode = _neighbourList.get(_neighbourIndex--);
		_childIterator =
			new ReverseDescendantDFSIterator<>(_structureView, neighbourNode, true,
				_treeLayerComparator);
		return _childIterator.next();
	}

	@SuppressWarnings("unchecked")
	private List<T> createNeighbourList(T currentNode) {
		if (currentNode == _rootNode) {
			_neighbourIndex = 0;
			return Collections.singletonList(_rootNode);
		}

		T parentNode = _structureView.getParent(currentNode);
		if (parentNode != null) {
			Iterator<T> neighbourIterator = (Iterator<T>) _structureView.getChildIterator(parentNode);
			List<T> neighbourList;
			neighbourList = CollectionUtil.toList(neighbourIterator);
			Collections.sort(neighbourList, _treeLayerComparator);
			for (_neighbourIndex = neighbourList.size() - 1; _neighbourIndex >= 0; _neighbourIndex--) {
				T neighbour = neighbourList.get(_neighbourIndex);
				if (neighbour == currentNode) {
					break;
				}
			}
			return neighbourList;
		} else {
			_neighbourIndex = -1;
			return Collections.<T> emptyList();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
