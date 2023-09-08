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
 * node.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CustomStartNodeDFSIterator<T> implements Iterator<T> {

	/** Error message for illegal traversal start node */
	public static final String ILLEGAL_START_NODE_MESSAGE = "Start node must be in subtree of root node!";

	private StructureView<T> _structureView;
	private DescendantDFSIterator<T> _childIterator;
	private List<T> _neighbourList;
	private int _neighbourIndex;
	private T _rootNode;
	private Comparator<T> _treeLayerComparator;

	private T _nextResult;


	/**
	 * Create a new {@link CustomStartNodeDFSIterator}, that returns nodes in natural DFS order.
	 */
	@SuppressWarnings("unchecked")
	public CustomStartNodeDFSIterator(StructureView<T> structureView, T rootNode, T traversalStartNode) {
		this(structureView, rootNode, traversalStartNode, (Comparator<T>) Equality.INSTANCE);
	}

	/**
	 * Create a new {@link CustomStartNodeDFSIterator}, that returns nodes in DFS order, whereby
	 * order of a tree layer will be defined by given comparator.
	 */
	public CustomStartNodeDFSIterator(StructureView<T> structureView, T rootNode, T traversalStartNode,
			Comparator<T> treeLayerComparator) {
		assertStartNodeUnderRootNode(structureView, rootNode, traversalStartNode);

		_structureView = structureView;
		_rootNode = rootNode;
		_treeLayerComparator = treeLayerComparator;
		_childIterator = new DescendantDFSIterator<>(structureView, traversalStartNode, true, _treeLayerComparator);
		_nextResult = _childIterator.next();
		_neighbourList = createNeighbourList(traversalStartNode);
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

		findNext();

		return result;
	}

	private void findNext() {
		if (_childIterator.hasNext()) {
			_nextResult = _childIterator.next();
		} else {
			if (_neighbourIndex < _neighbourList.size()) {
				_nextResult = nextNeighbourNode();
			} else {
				retrieveNextParentNeighbourList();
				if (_neighbourIndex < _neighbourList.size()) {
					_nextResult = nextNeighbourNode();
				} else {
					_nextResult = null;
				}
			}
		}
	}

	private void retrieveNextParentNeighbourList() {
		T parentNode = _structureView.getParent(_nextResult);
		_neighbourList = createNeighbourList(parentNode);
		while (parentNode != _rootNode && _neighbourIndex >= _neighbourList.size()) {
			_neighbourList = createNeighbourList(parentNode);
			parentNode = _structureView.getParent(parentNode);
		}
	}

	private T nextNeighbourNode() {
		T neighbourNode = _neighbourList.get(_neighbourIndex++);
		_childIterator =
			new DescendantDFSIterator<>(_structureView, neighbourNode, false,
				_treeLayerComparator);
		return neighbourNode;
	}

	@SuppressWarnings("unchecked")
	private List<T> createNeighbourList(T currentNode) {
		_neighbourIndex = 0;
		T parentNode = _structureView.getParent(currentNode);
		if (parentNode != null) {
			Iterator<T> neighbourIterator = (Iterator<T>) _structureView.getChildIterator(parentNode);
			List<T> neighbourList = CollectionUtil.toList(neighbourIterator);
			Collections.sort(neighbourList, _treeLayerComparator);
			for (T neighbour : neighbourList) {
				_neighbourIndex++;
				if (neighbour == currentNode) {
					break;
				}
			}
			return neighbourList;
		} else {
			return Collections.<T> emptyList();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
