/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.NodeLocator;
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * The functions of a {@link TreeStructureConfig} compiled once for an element, and what an element
 * displaying the tree builds from them for a session: the {@link DefaultTreeUINodeModel} holding
 * the tree, and the function telling what holds an object.
 *
 * <p>
 * Every element displaying a scripted tree - a tree, a table whose rows form a tree - computes its
 * tree with these functions, so it is the same tree whatever displays it, and it follows the model
 * through the same {@link ObservableTreeModel}.
 * </p>
 */
public final class TreeFunctions {

	private final TreeStructureConfig _config;

	private final QueryExecutor _rootExecutor;

	private final QueryExecutor _childrenExecutor;

	/** The compiled {@link TreeStructureConfig#getParents()} function, {@code null} without one. */
	private final QueryExecutor _parentsExecutor;

	/**
	 * Compiles the functions of the given configuration.
	 *
	 * <p>
	 * If services like {@code PersistencyLayer} are not yet active,
	 * {@link QueryExecutor#compile(com.top_logic.model.search.expr.config.dom.Expr)} returns an
	 * executor that compiles on first execution.
	 * </p>
	 */
	public TreeFunctions(TreeStructureConfig config) {
		_config = config;
		_rootExecutor = QueryExecutor.compile(config.getRoot());
		_childrenExecutor = QueryExecutor.compile(config.getChildren());
		_parentsExecutor = QueryExecutor.compileOptional(config.getParents());
	}

	/**
	 * The object the tree is built from for the given input values.
	 *
	 * @param inputValues
	 *        The values of the input channels, in declaration order.
	 */
	public Object root(Object[] inputValues) {
		return _rootExecutor.execute(inputValues);
	}

	/**
	 * A tree model built from the object the given channels name now, with its root not
	 * displayed.
	 *
	 * @param builder
	 *        The builder computing the children, see {@link #builder(List)}.
	 * @param inputChannels
	 *        The channels whose values the root function is called with.
	 */
	public DefaultTreeUINodeModel treeModel(TreeBuilder<DefaultTreeUINode> builder, List<ViewChannel> inputChannels) {
		return new DefaultTreeUINodeModel(builder, root(ChannelInputs.arguments(inputChannels)));
	}

	/**
	 * The builder computing the children of a node with the children function.
	 *
	 * @param inputChannels
	 *        The channels whose values the function is called with, followed by the node's
	 *        business object.
	 */
	public TreeBuilder<DefaultTreeUINode> builder(List<ViewChannel> inputChannels) {
		return new TreeBuilder<>() {

			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				Object[] args = appendArg(ChannelInputs.arguments(inputChannels), node.getBusinessObject());
				Collection<?> children = toCollection(_childrenExecutor.execute(args));

				List<DefaultTreeUINode> childNodes = new ArrayList<>(children.size());
				for (Object childObj : children) {
					DefaultTreeUINode childNode = createNode(node.getModel(), node, childObj);
					if (childNode != null) {
						childNodes.add(childNode);
					}
				}
				return childNodes;
			}

			@Override
			public boolean isFinite() {
				return _config.getCanExpandAll();
			}
		};
	}

	/**
	 * What holds a business object in the tree, {@code null} without a
	 * {@link TreeStructureConfig#getParents()} function.
	 *
	 * @param inputChannels
	 *        The channels whose values the function is called with, followed by the object.
	 */
	public Function<Object, Object> parentFunction(List<ViewChannel> inputChannels) {
		if (_parentsExecutor == null) {
			return null;
		}
		return businessObject -> singleObject(
			_parentsExecutor.execute(appendArg(ChannelInputs.arguments(inputChannels), businessObject)));
	}

	/**
	 * How the node of a business object is found in the tree.
	 *
	 * @param parentFunction
	 *        What holds an object in the tree, {@code null} where the tree does not say.
	 */
	public static NodeLocator nodeLocator(Function<Object, Object> parentFunction) {
		return parentFunction == null ? NodeLocator.SEARCHING : NodeLocator.byParents(parentFunction);
	}

	/**
	 * The business object a tree node stands for, the node itself when it is no
	 * {@link DefaultTreeUINode}.
	 */
	public static Object businessObject(Object node) {
		return node instanceof DefaultTreeUINode uiNode ? uiNode.getBusinessObject() : node;
	}

	/**
	 * The object a function returning a single object yielded, taking the first element of a
	 * collection the script produced instead and {@code null} from an empty one.
	 */
	private static Object singleObject(Object result) {
		if (result instanceof Collection<?> collection) {
			return collection.isEmpty() ? null : collection.iterator().next();
		}
		return result;
	}

	private static Object[] appendArg(Object[] base, Object extra) {
		Object[] result = new Object[base.length + 1];
		System.arraycopy(base, 0, result, 0, base.length);
		result[base.length] = extra;
		return result;
	}

	private static Collection<?> toCollection(Object result) {
		if (result instanceof Collection<?>) {
			return (Collection<?>) result;
		}
		if (result == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(result);
	}

}
