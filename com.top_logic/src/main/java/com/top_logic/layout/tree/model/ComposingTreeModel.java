/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.NamedConstant;

/**
 * {@link AbstractTreeModel} holding multiple other {@link AbstractTreeModel}. The branches in this
 * tree model are the sum of all branches in the held models enlarged by a synthetic root node.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComposingTreeModel extends AbstractTreeModel<Object> {

	private List<? extends TLTreeModel<?>> _models;

	private Object _root;

	/**
	 * Creates a new {@link ComposingTreeModel}.
	 */
	public ComposingTreeModel(TLTreeModel<?>... models) {
		this(Arrays.asList(models));
	}

	/**
	 * Creates a new {@link ComposingTreeModel}.
	 */
	public ComposingTreeModel(List<? extends TLTreeModel<?>> models) {
		this(new NamedConstant("root"), models);
	}

	/**
	 * Creates a new {@link ComposingTreeModel}.
	 */
	public ComposingTreeModel(Object root, TLTreeModel<?>... models) {
		this(root, Arrays.asList(models));
	}

	/**
	 * Creates a new {@link ComposingTreeModel}.
	 */
	public ComposingTreeModel(Object root, List<? extends TLTreeModel<?>> models) {
		_models = models;
		_root = root;
	}

	@Override
	public Object getBusinessObject(Object node) {
		TLTreeModel<Object> model = getModel(node);
		if (model == null) {
			throw notPartOfTheModel(node);
		}
		return model.getBusinessObject(node);
	}

	@Override
	public Object getRoot() {
		return _root;
	}

	@Override
	public List<? extends Object> getChildren(Object parent) {
		if (parent == getRoot()) {
			return _models.stream().map(TLTreeModel::getRoot).collect(Collectors.toList());
		}
		TLTreeModel<Object> model = getModel(parent);
		if (model == null) {
			return Collections.emptyList();
		}
		return model.getChildren(parent);
	}

	@Override
	public boolean childrenInitialized(Object parent) {
		if (parent == getRoot()) {
			return false;
		}
		TLTreeModel<Object> model = getModel(parent);
		if (model == null) {
			throw notPartOfTheModel(parent);
		}
		return model.childrenInitialized(parent);
	}

	@Override
	public void resetChildren(Object parent) {
		if (parent == getRoot()) {
			return;
		}
		TLTreeModel<Object> model = getModel(parent);
		if (model != null) {
			model.resetChildren(parent);
		}
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == getRoot()) {
			return false;
		}
		TLTreeModel<Object> model = getModel(node);
		if (model == null) {
			throw notPartOfTheModel(node);
		}
		return model.isLeaf(node);
	}

	@Override
	public boolean isFinite() {
		return !_models.stream().filter(ComposingTreeModel::isInfinite).findAny().isPresent();
	}

	private static boolean isInfinite(TLTreeModel<?> model) {
		return !model.isFinite();
	}

	@Override
	public Object getParent(Object node) {
		if (node == getRoot()) {
			return null;
		}
		TLTreeModel<Object> model = getModel(node);
		if (model == null) {
			return null;
		}
		if (node == model.getRoot()) {
			return getRoot();
		}
		return model.getParent(node);
	}

	@Override
	public List<Object> createPathToRoot(Object aNode) {
		if (aNode == null) {
			return Collections.emptyList();
		}
		if (aNode == getRoot()) {
			return Collections.singletonList(aNode);
		}

		TLTreeModel<Object> model = getModel(aNode);
		if (model == null) {
			return Collections.emptyList();
		}

		List<Object> subPath = model.createPathToRoot(aNode);
		if (subPath.isEmpty()) {
			StringBuilder error = new StringBuilder();
			error.append("Model ");
			error.append(model);
			error.append(" contains node ");
			error.append(aNode);
			error.append(" but creates an empty path to root.");
			throw new IllegalStateException(error.toString());
		}
		ArrayList<Object> result = new ArrayList<>(subPath);
		result.add(getRoot());
		return result;
	}

	@Override
	public boolean containsNode(Object aNode) {
		if (aNode == getRoot()) {
			return true;
		}
		return getModel(aNode) != null;
	}

	private RuntimeException notPartOfTheModel(Object node) {
		return new IllegalArgumentException(node + " is not part of the model " + this);
	}

	/**
	 * Returns the {@link TLTreeModel} to which the given node belongs. MAy be <code>null</code> in
	 * case the node is not part of any model.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private TLTreeModel<Object> getModel(Object node) {
		for (TLTreeModel model : _models) {
			try {
				if (model.containsNode(node)) {
					return model;
				}
			} catch (RuntimeException ex) {
				// ignore
				// This may happen because the node may be incompatible with some other model.
			}
		}
		return null;
	}

}

