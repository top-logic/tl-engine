/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link TableDragSourceByExpression} for tree tables.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { "treetable" })
public class TreeTableDragSourceByExpression extends TableDragSourceByExpression {

	/**
	 * Configuration options for {@link TreeTableDragSourceByExpression}.
	 */
	public interface Config<I extends TreeTableDragSourceByExpression> extends TableDragSourceByExpression.Config<I> {
		/**
		 * Whether paths to objects are transfered to drag targets instead of simply tree node user
		 * objects.
		 * 
		 * <p>
		 * In a tree where the same object may occur multiple times in different nodes, this option
		 * allows to exactly specify the dragged nodes.
		 * </p>
		 * 
		 * <p>
		 * When enabled, the drag data consists of collections of lists of business objects. Where
		 * each list of business objects represents a path in the tree to a selected object. In each
		 * path, the first element is the tree's root node and the last element is the dragged
		 * object itself.
		 * </p>
		 */
		boolean transferPaths();
	}

	private boolean _transferPaths;

	/**
	 * Creates a {@link TreeTableDragSourceByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TreeTableDragSourceByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);
		_transferPaths = config.transferPaths();
	}

	@Override
	protected Object unwrap(Object rowObject) {
		if (_transferPaths) {
			List<Object> path = new ArrayList<>();
			buildPath(path, ((TLTreeNode<?>) rowObject));
			return path;
		} else {
			return ((TLTreeNode<?>) rowObject).getBusinessObject();
		}
	}

	private static <N extends TLTreeNode<N>> void buildPath(List<Object> path, TLTreeNode<N> node) {
		TLTreeNode<N> parent = node.getParent();
		if (parent != null) {
			buildPath(path, parent);
		}
		path.add(node.getBusinessObject());
	}

}
