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
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.react.MetaResourceControlProvider;
import com.top_logic.layout.react.ReactControlProvider;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Declarative {@link UIElement} that wraps a {@link ReactTreeControl}.
 *
 * <p>
 * Input data is provided via {@link ViewChannel}s. The root object and child lists are computed
 * using TL-Script expressions, and the tree structure is built lazily via a custom
 * {@link TreeBuilder}.
 * </p>
 *
 * <p>
 * Note: The optional config properties {@code isLeaf}, {@code supportsNode},
 * {@code modelForNode}, {@code parents}, and {@code nodesToUpdate} are declared for future use
 * and currently not wired into the runtime. They parse correctly but have no effect. Leaf status
 * is determined by whether {@code children} returns an empty list. Incremental update support
 * will be added when the view system gains model event integration.
 * </p>
 */
public class TreeElement implements UIElement {

	/**
	 * Configuration for {@link TreeElement}.
	 */
	@TagName("tree")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TreeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getRoot()}. */
		String ROOT = "root";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/** Configuration name for {@link #getIsLeaf()}. */
		String IS_LEAF = "isLeaf";

		/** Configuration name for {@link #getSupportsNode()}. */
		String SUPPORTS_NODE = "supportsNode";

		/** Configuration name for {@link #getModelForNode()}. */
		String MODEL_FOR_NODE = "modelForNode";

		/** Configuration name for {@link #getParents()}. */
		String PARENTS = "parents";

		/** Configuration name for {@link #getNodesToUpdate()}. */
		String NODES_TO_UPDATE = "nodesToUpdate";

		/** Configuration name for {@link #getCanExpandAll()}. */
		String CAN_EXPAND_ALL = "canExpandAll";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getNodeContent()}. */
		String NODE_CONTENT = "nodeContent";

		/**
		 * References to {@link ViewChannel}s whose current values become positional arguments to
		 * the expression properties.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script function computing the root object of the tree.
		 *
		 * <p>
		 * Takes the input channel values as positional arguments and returns a single object to be
		 * used as the tree root.
		 * </p>
		 */
		@Name(ROOT)
		@Mandatory
		@NonNullable
		Expr getRoot();

		/**
		 * TL-Script function computing the children of a node.
		 *
		 * <p>
		 * Takes the input channel values followed by the parent business object as last argument.
		 * Returns a {@link Collection} of child business objects.
		 * </p>
		 */
		@Name(CHILDREN)
		@Mandatory
		@NonNullable
		Expr getChildren();

		/**
		 * Optional TL-Script function determining whether a node is a leaf.
		 *
		 * <p>
		 * Takes the input channel values followed by the node business object as last argument.
		 * Returns a boolean. If not set, leaf status is determined by whether
		 * {@link #getChildren()} returns an empty list.
		 * </p>
		 */
		@Name(IS_LEAF)
		Expr getIsLeaf();

		/**
		 * Optional TL-Script function for incremental update decisions.
		 *
		 * <p>
		 * Takes the input channel values followed by a candidate object as last argument.
		 * </p>
		 */
		@Name(SUPPORTS_NODE)
		Expr getSupportsNode();

		/**
		 * Optional TL-Script function for reverse model lookup.
		 *
		 * <p>
		 * Takes the input channel values followed by a candidate object as last argument.
		 * </p>
		 */
		@Name(MODEL_FOR_NODE)
		Expr getModelForNode();

		/**
		 * Optional TL-Script function computing the parent chain.
		 *
		 * <p>
		 * Takes the input channel values followed by a node object as last argument.
		 * </p>
		 */
		@Name(PARENTS)
		Expr getParents();

		/**
		 * Optional TL-Script function computing nodes to update on a change.
		 *
		 * <p>
		 * Takes the input channel values followed by a changed object as last argument.
		 * </p>
		 */
		@Name(NODES_TO_UPDATE)
		Expr getNodesToUpdate();

		/**
		 * Whether the tree supports expand-all.
		 */
		@Name(CAN_EXPAND_ALL)
		@BooleanDefault(true)
		boolean getCanExpandAll();

		/**
		 * Optional reference to a {@link ViewChannel} to write the selected node's business
		 * object to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Optional provider for custom node content controls. If not set, nodes are rendered
		 * using a simple text label.
		 */
		@Name(NODE_CONTENT)
		PolymorphicConfiguration<ReactControlProvider> getNodeContent();
	}

	private final Config _config;

	private final QueryExecutor _rootExecutor;

	private final QueryExecutor _childrenExecutor;

	private final ReactControlProvider _nodeContentProvider;

	/**
	 * Creates a new {@link TreeElement} from configuration.
	 *
	 * <p>
	 * Expressions are compiled once here and shared across all sessions. If services like
	 * {@code PersistencyLayer} are not yet active, {@link QueryExecutor#compile(Expr)} returns a
	 * {@code DeferredQueryExecutor} that lazily compiles on first execution.
	 * </p>
	 */
	@CalledByReflection
	public TreeElement(InstantiationContext context, Config config) {
		_config = config;

		_rootExecutor = QueryExecutor.compile(config.getRoot());
		_childrenExecutor = QueryExecutor.compile(config.getChildren());

		ReactControlProvider configuredProvider = context.getInstance(config.getNodeContent());
		_nodeContentProvider = configuredProvider != null ? configuredProvider : MetaResourceControlProvider.INSTANCE;
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		// 1. Resolve input channels.
		List<ChannelRef> inputRefs = _config.getInputs();
		List<ViewChannel> inputChannels = new ArrayList<>(inputRefs.size());
		for (ChannelRef ref : inputRefs) {
			inputChannels.add(context.resolveChannel(ref));
		}

		// 2. Execute initial root query.
		Object[] channelValues = readChannelValues(inputChannels);
		Object rootObject = _rootExecutor.execute(channelValues);

		// 3. Build tree model with custom TreeBuilder.
		TreeBuilder<DefaultTreeUINode> builder = createTreeBuilder(inputChannels);
		DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(builder, rootObject);

		// 4. Create selection model.
		DefaultSingleSelectionModel<Object> selectionModel =
			new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);

		// 5. Create ReactTreeControl.
		ReactTreeControl treeControl = new ReactTreeControl(treeModel, selectionModel, _nodeContentProvider);

		// 6. Wire selection channel.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			selectionModel.addSelectionListener(new SelectionListener<Object>() {
				@Override
				public void notifySelectionChanged(SelectionModel<Object> model, SelectionEvent<Object> event) {
					Set<?> newSelection = event.getNewSelection();
					if (newSelection.size() == 1) {
						Object selectedNode = newSelection.iterator().next();
						// Extract business object if the selected object is a tree node.
						if (selectedNode instanceof DefaultTreeUINode) {
							selectionChannel.set(((DefaultTreeUINode) selectedNode).getBusinessObject());
						} else {
							selectionChannel.set(selectedNode);
						}
					} else if (newSelection.isEmpty()) {
						selectionChannel.set(null);
					} else {
						selectionChannel.set(newSelection);
					}
				}
			});
		}

		// 7. Wire input channel listeners for re-query on change.
		ViewChannel.ChannelListener refreshListener = (sender, oldValue, newValue) -> {
			Object[] newValues = readChannelValues(inputChannels);
			Object newRoot = _rootExecutor.execute(newValues);
			DefaultTreeUINodeModel newTreeModel = new DefaultTreeUINodeModel(builder, newRoot);
			selectionModel.clear();
			treeControl.setTreeModel(newTreeModel);
		};
		for (ViewChannel channel : inputChannels) {
			channel.addListener(refreshListener);
		}

		return treeControl;
	}

	private TreeBuilder<DefaultTreeUINode> createTreeBuilder(List<ViewChannel> inputChannels) {
		return new TreeBuilder<DefaultTreeUINode>() {

			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				Object[] channelValues = readChannelValues(inputChannels);
				Object[] args = appendArg(channelValues, node.getBusinessObject());
				Object result = _childrenExecutor.execute(args);
				Collection<?> children = toCollection(result);

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

	private static Object[] readChannelValues(List<ViewChannel> channels) {
		Object[] values = new Object[channels.size()];
		for (int i = 0; i < channels.size(); i++) {
			values[i] = channels.get(i).get();
		}
		return values;
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
