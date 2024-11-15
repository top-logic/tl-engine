/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.iterators.TransformIterator;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;

/**
 * {@link TLTreeModel} for business objects that uses {@link TLTreeNode}s for internal operations.
 * 
 * It is assumed that each business object is assigned to exactly one tree node and vice versa.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class BusinessObjectTreeModel<T> implements TLTreeModel<T> {

	DefaultIndexedTreeModel _treeModel;
	
	Function<T, Collection<?>> _parents;

	/**
	 * Creates a finite {@link TLTreeModel} for business objects.
	 * 
	 * @param root
	 *        Business object of the root node.
	 * @param childrenByObject
	 *        Children objects of a given business object.
	 * @param parentByObject
	 *        Parent objects of a given business object.
	 */
	public BusinessObjectTreeModel(T root, Function<T, Collection<?>> childrenByObject, Function<T, Collection<?>> parentByObject) {
		this(root, childrenByObject, parentByObject, true);
	}

	/**
	 * Creates a {@link TLTreeModel} for business objects.
	 * 
	 * @param root
	 *        Business object of the root node.
	 * @param childrenByObject
	 *        Children objects of a given business object.
	 * @param parentByObject
	 *        Parent objects of a given business object.
	 * @param finite
	 *        Whether the created tree model is of limited depth.
	 */
	public BusinessObjectTreeModel(T root, Function<T, Collection<?>> childrenByObject,
			Function<T, Collection<?>> parentByObject, boolean finite) {
		_treeModel = new DefaultIndexedTreeModel(createTreeBuilder(childrenByObject, finite), root);
		_parents = parentByObject;
	}

	private TreeBuilder<DefaultTreeUINode> createTreeBuilder(Function<T, Collection<?>> childrenByObject,
			boolean finite) {
		return new TreeBuilder<>() {

			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				Collection<?> children = childrenByObject.apply(getBusinessObject(node));
				List<DefaultTreeUINode> childrenNodes = new ArrayList<>();
				AbstractMutableTLTreeModel<DefaultTreeUINode> model = node.getModel();
				for (Object child : children) {
					childrenNodes.add(createNode(model, node, child));
				}
				return childrenNodes;
			}

			@Override
			public boolean isFinite() {
				return finite;
			}

		};
	}

	@Override
	public T getRoot() {
		return getBusinessObject(_treeModel.getRoot());
	}

	@Override
	public List<? extends T> getChildren(T object) {
		List<T> childrenObjects = new ArrayList<>();

		for (DefaultTreeUINode child : _treeModel.getChildren(internalNode(object))) {
			childrenObjects.add(getBusinessObject(child));
		}

		return childrenObjects;
	}

	@Override
	public boolean childrenInitialized(T object) {
		return _treeModel.childrenInitialized(internalNode(object));
	}

	private DefaultTreeUINode internalNode(T object) {
		List<DefaultTreeUINode> nodes = _treeModel.getIndex().getNodes(object);

		if (!CollectionUtil.isEmpty(nodes)) {
			return nodes.get(0);
		}

		validateIndex(object);

		List<DefaultTreeUINode> updatedNodes = _treeModel.getIndex().getNodes(object);
		if (CollectionUtil.isEmpty(updatedNodes)) {
			return null;
		}

		return updatedNodes.get(0);
	}

	private void validateIndex(T object) {
		DefaultTreeUINode parentNode = getParentNode(object);

		_treeModel.getChildren(parentNode);
	}

	private DefaultTreeUINode getParentNode(T object) {
		Object parent = CollectionUtilShared.getSingleValueFrom(_parents.apply(object));

		return parent == null ? _treeModel.getRoot() : internalNode((T) parent);
	}

	@Override
	public void resetChildren(T object) {
		_treeModel.resetChildren(internalNode(object));
	}

	@Override
	public boolean containsNode(T object) {
		return _treeModel.containsNode(internalNode(object));
	}

	@Override
	public boolean hasChild(T object, Object node) {
		return _treeModel.hasChild(internalNode(object), node);
	}

	@Override
	public boolean hasChildren(T object) {
		return _treeModel.hasChildren(internalNode(object));
	}

	@Override
	public boolean addTreeModelListener(TreeModelListener listener) {
		return _treeModel.addTreeModelListener(listener);
	}

	@Override
	public boolean removeTreeModelListener(TreeModelListener listener) {
		return _treeModel.removeTreeModelListener(listener);
	}

	@Override
	public boolean isLeaf(T object) {
		return _treeModel.isLeaf(internalNode(object));
	}

	@Override
	public Iterator<? extends T> getChildIterator(T object) {
		Iterator<? extends DefaultTreeUINode> childNodeIterator = _treeModel.getChildIterator(internalNode(object));

		return new TransformIterator<DefaultTreeUINode, T>(childNodeIterator, value -> getBusinessObject(value));
	}

	@Override
	public boolean isFinite() {
		return _treeModel.isFinite();
	}

	@Override
	public T getParent(T object) {
		DefaultTreeUINode node = internalNode(object);
		if (node == null) {
			return null;
		}

		DefaultTreeUINode parent = _treeModel.getParent(node);
		if (parent != null) {
			return getBusinessObject(parent);
		}

		return null;
	}

	@Override
	public List<T> createPathToRoot(T object) {
		List<T> businessObjectPath = new ArrayList<>();

		for (DefaultTreeUINode node : _treeModel.createPathToRoot(internalNode(object))) {
			businessObjectPath.add(getBusinessObject(node));
		}

		return businessObjectPath;
	}

	@SuppressWarnings("unchecked")
	T getBusinessObject(DefaultTreeUINode node) {
		return (T) node.getBusinessObject();
	}

	@Override
	public Object getBusinessObject(T object) {
		return object;
	}

}
