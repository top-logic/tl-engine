/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ObservableTreeModel;
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.layout.view.model.TreeSelectionDisplay;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.table.Column;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.impl.TreeRowSource;

/**
 * The tree a table displays as its rows, computed by {@link TreeFunctions} for the input channels
 * of one display, together with what keeps the rows in sync with it.
 *
 * <p>
 * The tree is held in a {@link DefaultTreeUINodeModel}; the rows see it through a
 * {@link TreeStructure} whose top-level rows are the children of the root node and whose rows are
 * keyed by their business objects, so that the expansion and the selection of a row survive a
 * rebuild of the tree. A table built over {@link #createSource(List) its row source} follows the
 * model through an {@link ObservableTreeModel} once it {@link #observe(ObservableTreeModel.Display,
 * Set, Collection, Collection) observes} it, and selects its rows through a
 * {@link TreeSelectionBinding} {@link #bindSelection(TableViewControl, ViewChannel) bound} to it.
 * </p>
 *
 * <p>
 * The observation runs between {@link #attach(ModelScope)} and {@link #detach()}: a detached tree
 * is not changed by the model, and takes up what happened meanwhile when it is attached again.
 * </p>
 */
public final class RowTree {

	private final TreeFunctions _functions;

	private final List<ViewChannel> _inputChannels;

	private final TreeBuilder<DefaultTreeUINode> _builder;

	private final NodeStructure _structure;

	/**
	 * What holds an object in the tree, {@code null} where the tree does not say.
	 *
	 * <p>
	 * It serves the observation (where an object that moved went, where an object of an observed
	 * type belongs) and the selection (which row an object of the channel has).
	 * </p>
	 */
	private final Function<Object, Object> _parentFunction;

	/** The selection bound to the rows, {@code null} without one. */
	private TreeSelectionBinding _selectionBinding;

	/** The observation of the model, {@code null} before {@link #observe}. */
	private ObservableTreeModel _observation;

	/** How the children of a node are computed, see {@link #setChildList(TreeFunctions.ChildList)}. */
	private TreeFunctions.ChildList _childList = TreeFunctions.ChildList.DIRECT;

	/**
	 * Builds the tree from the object the given channels name now.
	 *
	 * @param functions
	 *        The functions computing the tree.
	 * @param inputChannels
	 *        The channels whose values the functions see.
	 * @param finite
	 *        Whether the tree may be expanded completely, see {@link TreeStructure#isFinite()}.
	 */
	public RowTree(TreeFunctions functions, List<ViewChannel> inputChannels, boolean finite) {
		_functions = functions;
		_inputChannels = inputChannels;
		_builder = functions.builder(inputChannels, (node, function) -> _childList.children(node, function));
		_structure = new NodeStructure(functions.treeModel(_builder, inputChannels), finite);
		_parentFunction = functions.parentFunction(inputChannels);
	}

	/**
	 * The business objects of the top-level rows.
	 */
	public List<Object> topLevelObjects() {
		List<DefaultTreeUINode> roots = _structure.roots();
		List<Object> result = new ArrayList<>(roots.size());
		for (DefaultTreeUINode root : roots) {
			result.add(root.getBusinessObject());
		}
		return result;
	}

	/**
	 * The object the tree is built from, whose children are the top-level rows.
	 */
	public Object rootObject() {
		return _structure.rootParent();
	}

	/**
	 * Sets how the children of a node are computed from now on: by applying the children function
	 * to the business object of the node, or otherwise, see {@link TreeFunctions.ChildList}.
	 *
	 * <p>
	 * The children computed already stay as they are until they are {@link #reconcile(Collection)
	 * reconciled}.
	 * </p>
	 *
	 * @param childList
	 *        How the children are computed, {@code null} for {@link TreeFunctions.ChildList#DIRECT}.
	 */
	public void setChildList(TreeFunctions.ChildList childList) {
		_childList = childList == null ? TreeFunctions.ChildList.DIRECT : childList;
	}

	/**
	 * The nodes whose children were computed, the root node first and every node before the nodes
	 * below it.
	 */
	public List<DefaultTreeUINode> computedNodes() {
		List<DefaultTreeUINode> result = new ArrayList<>();
		collectComputed(_structure.model().getRoot(), result);
		return result;
	}

	private static void collectComputed(DefaultTreeUINode node, List<DefaultTreeUINode> result) {
		if (!node.isInitialized()) {
			return;
		}
		result.add(node);
		for (DefaultTreeUINode child : node.getChildren()) {
			collectComputed(child, result);
		}
	}

	/**
	 * The node of the given business object among the {@link #computedNodes() computed nodes} and
	 * their children, the root node for the {@link #rootObject() root object}.
	 *
	 * @return The node, {@code null} where the tree has none computed for the object.
	 */
	public DefaultTreeUINode findNode(Object businessObject) {
		DefaultTreeUINode root = _structure.model().getRoot();
		if (businessObject == root.getBusinessObject()) {
			return root;
		}
		for (DefaultTreeUINode node : computedNodes()) {
			for (DefaultTreeUINode child : node.getChildren()) {
				if (child.getBusinessObject() == businessObject) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * Computes the children of the given nodes again, keeping the nodes of the objects still among
	 * them, see {@link ObservableTreeModel#reconcileChildren(DefaultTreeUINode, TreeBuilder)}.
	 *
	 * <p>
	 * A change of the tree makes the {@link #bindSelection(TableViewControl, ViewChannel) bound
	 * selection} express itself on the rows, see {@link TreeSelectionBinding#structureChanged()}.
	 * </p>
	 *
	 * @return Whether the tree changed.
	 */
	public boolean reconcile(Collection<DefaultTreeUINode> nodes) {
		boolean changed = false;
		for (DefaultTreeUINode node : nodes) {
			changed |= ObservableTreeModel.reconcileChildren(node, _builder);
		}
		if (changed) {
			structureChanged();
		}
		return changed;
	}

	/**
	 * Computes the children of every {@link #computedNodes() computed node} again, keeping the nodes
	 * of the objects still among them.
	 *
	 * <p>
	 * Unlike {@link #reconcile(Collection)}, the {@link #bindSelection(TableViewControl, ViewChannel)
	 * bound selection} is not told: this is for a tree whose table is built anew, with a selection
	 * bound anew, which applies the selection channel to the tree as it is then.
	 * </p>
	 *
	 * @return Whether the tree changed.
	 */
	public boolean reconcileAll() {
		return ObservableTreeModel.reconcileSubtree(_structure.model().getRoot(), _builder);
	}

	private void structureChanged() {
		if (_selectionBinding != null) {
			_selectionBinding.structureChanged();
		}
	}

	/**
	 * Creates the source of the rows of a table displaying this tree with the given columns.
	 *
	 * <p>
	 * Opening a row computes the children of its node, whose objects the {@link #observe
	 * observation} follows from then on.
	 * </p>
	 */
	public TreeRowSource<DefaultTreeUINode, Object> createSource(List<Column<Object, ?>> columns) {
		return createSource(columns, Function.identity(), Set.of());
	}

	/**
	 * Creates the source of the rows of a table displaying this tree with the given columns, whose
	 * row objects are computed from the business objects of the nodes.
	 *
	 * <p>
	 * The rows stay keyed by the business objects of their nodes, whatever their row objects are.
	 * A row object is computed whenever the source hands out the row, sorts or filters it, so the
	 * function sees exactly the nodes that are displayed or searched. Opening a row computes the
	 * children of its node, whose objects the {@link #observe observation} follows from then on.
	 * </p>
	 *
	 * @param columns
	 *        The columns of the table.
	 * @param rowObject
	 *        Computes the row object of a node from its business object.
	 * @param expandedKeys
	 *        The keys of the rows that are expanded from the start: the
	 *        {@link TreeRowSource#expandedKeys() expanded rows} of a source this one replaces.
	 */
	public <R> TreeRowSource<DefaultTreeUINode, R> createSource(List<Column<R, ?>> columns,
			Function<Object, ? extends R> rowObject, Collection<?> expandedKeys) {
		TreeRowSource<DefaultTreeUINode, R> source =
			new TreeRowSource<>(new RowStructure<>(_structure, rowObject), columns, expandedKeys);
		source.addListener((from, to) -> {
			if (_observation != null) {
				_observation.nodesComputed();
			}
		});
		return source;
	}

	/**
	 * Binds the selection of the given table displaying this tree to the given channel through a
	 * {@link TreeSelectionBinding}.
	 *
	 * <p>
	 * The binding is dropped with the table: the channel and the selection outlive it. It survives
	 * an {@link #attach(ModelScope) attach}/{@link #detach() detach} cycle, which only suspends the
	 * observation of the model, so a selection written while the table is off screen is displayed
	 * when it returns. Every change of the tree the observation makes is reported to the binding,
	 * see {@link TreeSelectionBinding#structureChanged()}.
	 * </p>
	 */
	public TreeSelectionBinding bindSelection(TableViewControl<?> table, ViewChannel channel) {
		return bindSelection(table, channel, TreeSelectionBinding.ChannelObjects.IDENTITY);
	}

	/**
	 * Binds the selection of the given table displaying this tree to the given channel, which
	 * represents the business objects of the nodes by other objects.
	 *
	 * @see #bindSelection(TableViewControl, ViewChannel)
	 * @see TreeSelectionBinding.ChannelObjects
	 */
	public TreeSelectionBinding bindSelection(TableViewControl<?> table, ViewChannel channel,
			TreeSelectionBinding.ChannelObjects channelObjects) {
		// The node of an object read from the channel is looked for in the tree displayed now,
		// which is another one after a rebuild.
		TreeSelectionBinding binding = new TreeSelectionBinding(
			TreeSelectionDisplay.of(table, _structure::model), _structure::model,
			TreeFunctions.nodeLocator(_parentFunction), channel, channelObjects);
		table.addCleanupAction(binding::dispose);
		_selectionBinding = binding;
		return binding;
	}

	/**
	 * Drops the {@link #bindSelection(TableViewControl, ViewChannel) bound selection} before the
	 * table it was bound to goes away.
	 */
	public void releaseSelection() {
		if (_selectionBinding != null) {
			_selectionBinding.dispose();
			_selectionBinding = null;
		}
	}

	/**
	 * Keeps the tree in sync with the model and the input channels, telling the given display about
	 * every change.
	 *
	 * <p>
	 * A tree built anew is the one the rows see before the display is told
	 * about it. Every change of the tree makes the {@link #bindSelection(TableViewControl,
	 * ViewChannel) bound selection} express itself on the rows, after the display was told, so a
	 * selected object that lost its row is gone from them. The observation begins with
	 * {@link #attach(ModelScope)}.
	 * </p>
	 *
	 * @param display
	 *        What displays the tree.
	 * @param observedTypes
	 *        Types whose created or changed objects are placed in the tree even while it has no node
	 *        for them, see {@link ObservableTreeModel}.
	 * @param rowIndependentInputs
	 *        The input channels whose changes cannot change the tree, see
	 *        {@link ObservableTreeModel#withRowIndependentInputs(Collection)}.
	 * @param cellInputs
	 *        Channels the cells read that are no input channels, see
	 *        {@link ObservableTreeModel#withCellInputs(Collection)}.
	 */
	public void observe(ObservableTreeModel.Display display, Set<TLStructuredType> observedTypes,
			Collection<? extends ViewChannel> rowIndependentInputs, Collection<? extends ViewChannel> cellInputs) {
		_observation = new ObservableTreeModel(new ObservableTreeModel.Display() {
			@Override
			public void invalidateNode(DefaultTreeUINode node) {
				display.invalidateNode(node);
			}

			@Override
			public void nodesChanged() {
				display.nodesChanged();
			}

			@Override
			public void treeReplaced(DefaultTreeUINodeModel newModel) {
				_structure.setModel(newModel);
				display.treeReplaced(newModel);
			}

			@Override
			public void inputsChanged() {
				display.inputsChanged();
			}
		}, _structure.model(), _functions::root, _builder, _parentFunction, observedTypes, _inputChannels)
			.withRowIndependentInputs(rowIndependentInputs)
			.withCellInputs(cellInputs);
		_observation.addStructureListener(() -> {
			if (_selectionBinding != null) {
				_selectionBinding.structureChanged();
			}
		});
	}

	/**
	 * Begins or resumes {@link #observe observing} the model on the given scope.
	 */
	public void attach(ModelScope scope) {
		if (_observation != null) {
			_observation.attach(scope);
		}
	}

	/**
	 * Suspends the {@link #observe observation}: the tree stays as it is until it is
	 * {@link #attach(ModelScope) attached} again.
	 */
	public void detach() {
		if (_observation != null) {
			_observation.detach();
		}
	}

	/**
	 * The tree a {@link DefaultTreeUINodeModel} holds, as the rows of a table see it: the children
	 * of the root node are the top-level rows, and a row is keyed by its business object, so that
	 * its expansion and its selection survive a rebuild of the tree.
	 */
	private static final class NodeStructure implements TreeStructure<DefaultTreeUINode, Object> {

		private DefaultTreeUINodeModel _model;

		private final boolean _finite;

		NodeStructure(DefaultTreeUINodeModel model, boolean finite) {
			_model = model;
			_finite = finite;
		}

		/**
		 * The model whose tree this structure is.
		 */
		DefaultTreeUINodeModel model() {
			return _model;
		}

		/**
		 * Makes the structure the one of the given model, which replaces the one held so far.
		 */
		void setModel(DefaultTreeUINodeModel model) {
			_model = model;
		}

		@Override
		public List<DefaultTreeUINode> roots() {
			return _model.getRoot().getChildren();
		}

		@Override
		public List<DefaultTreeUINode> children(DefaultTreeUINode node) {
			return node.getChildren();
		}

		@Override
		public boolean isLeaf(DefaultTreeUINode node) {
			return node.getChildren().isEmpty();
		}

		@Override
		public boolean isFinite() {
			return _finite;
		}

		@Override
		public Object businessObject(DefaultTreeUINode node) {
			return node.getBusinessObject();
		}

		@Override
		public Object key(DefaultTreeUINode node) {
			return node.getBusinessObject();
		}

		@Override
		public Object rootParent() {
			return _model.getRoot().getBusinessObject();
		}

	}

	/**
	 * The {@link NodeStructure} with row objects computed from the business objects of its nodes,
	 * see {@link RowTree#createSource(List, Function, Collection)}.
	 */
	private static final class RowStructure<R> implements TreeStructure<DefaultTreeUINode, R> {

		private final NodeStructure _nodes;

		private final Function<Object, ? extends R> _rowObject;

		RowStructure(NodeStructure nodes, Function<Object, ? extends R> rowObject) {
			_nodes = nodes;
			_rowObject = rowObject;
		}

		@Override
		public List<DefaultTreeUINode> roots() {
			return _nodes.roots();
		}

		@Override
		public List<DefaultTreeUINode> children(DefaultTreeUINode node) {
			return _nodes.children(node);
		}

		@Override
		public boolean isLeaf(DefaultTreeUINode node) {
			return _nodes.isLeaf(node);
		}

		@Override
		public boolean isFinite() {
			return _nodes.isFinite();
		}

		@Override
		public R businessObject(DefaultTreeUINode node) {
			return _rowObject.apply(_nodes.businessObject(node));
		}

		@Override
		public Object key(DefaultTreeUINode node) {
			return _nodes.key(node);
		}

		@Override
		public Object rootParent() {
			return _nodes.rootParent();
		}

	}

}
