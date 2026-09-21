/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Two-way binding between the selection of a {@link ReactTreeControl} and a {@link ViewChannel}.
 *
 * <p>
 * What lands on the channel are the {@link TLTreeNode#getBusinessObject() business objects} of the
 * selected nodes, not the nodes themselves: the channel is read by displays and commands that work
 * with the application's objects and know nothing about the tree showing them. The channel is the
 * shared selection - several displays, a detail panel and a command's executability can all be
 * bound to the same one - so the tree is only one of its writers, and it writes it in exactly two
 * situations:
 * </p>
 *
 * <ul>
 * <li>The selection changed in the tree itself (the user clicked a node): one selected node is
 * written as its object, several as the {@link Set} of their objects, none as <code>null</code> -
 * so a display bound to the channel works with a tree selecting a single node as well as with one
 * selecting any number, and only one that is to show several objects at once has to expect a
 * set.</li>
 * <li>A node this tree displayed as selected is gone after a change of the tree - its object was
 * deleted, or taken out of the child list it hung in. The value names something nobody can see any
 * more, so the surviving selection is written in its place, <code>null</code> where nothing of it
 * is left.</li>
 * </ul>
 *
 * <p>
 * A value another writer puts on the channel is displayed as the tree's selection: the node of the
 * object is looked for by the {@link NodeLocator}, the subtrees above it are opened so that it is
 * visible, and it becomes the selection. That is what a command creating an object and writing it
 * to the channel needs - and since the tree may hold no node for it yet, the value is applied again
 * after every change of the tree, so the object is selected as soon as its node exists.
 * </p>
 *
 * <p>
 * A value the tree has no node for means no more than "no node selected here": the tree leaves the
 * channel alone, and leaves its own selection where it is. Clearing the channel would destroy what
 * another writer put there - the object a second display over other objects selected, or the object
 * a create command wrote before this tree caught up with it.
 * </p>
 *
 * <p>
 * A value that is a {@link Collection} is the selection of several nodes. A tree selecting any
 * number of nodes displays the nodes it has for those objects and ignores the rest. A tree
 * selecting a single node cannot display such a value at all: it shows no selection, and leaves the
 * channel alone for the same reason.
 * </p>
 *
 * <p>
 * The tree's own echo of an applied channel value (its selection listener fires from every
 * selection change, including a programmatic one) never writes the channel back, and a value the
 * tree already displays as its selection is not applied again - re-applying it would open subtrees
 * the user closed since.
 * </p>
 */
public class TreeSelectionBinding {

	private final ReactTreeControl _tree;

	private final SelectionModel<Object> _selectionModel;

	private final Supplier<DefaultTreeUINodeModel> _treeModel;

	private final NodeLocator _locator;

	private final ViewChannel _channel;

	private final ViewChannel.ChannelListener _channelListener;

	private final SelectionListener<Object> _selectionListener = this::handleSelectionChanged;

	/** Whether a channel value is currently being applied, so the tree's echo is not written back. */
	private boolean _applyingFromChannel;

	/** The business objects of the nodes the tree last displayed as selected. */
	private Set<Object> _displayedObjects = Set.of();

	/**
	 * Creates a {@link TreeSelectionBinding} and applies the channel's current value to the tree.
	 *
	 * @param tree
	 *        The control displaying the tree, told to push a selection made here to the client.
	 * @param selectionModel
	 *        The selection of that tree, holding its nodes.
	 * @param treeModel
	 *        The tree displayed now, which is another one after the tree was built anew.
	 * @param locator
	 *        Finds the node standing for an object of the channel.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public TreeSelectionBinding(ReactTreeControl tree, SelectionModel<Object> selectionModel,
			Supplier<DefaultTreeUINodeModel> treeModel, NodeLocator locator, ViewChannel channel) {
		_tree = tree;
		_selectionModel = selectionModel;
		_treeModel = treeModel;
		_locator = locator;
		_channel = channel;

		selectionModel.addSelectionListener(_selectionListener);
		_channelListener = (sender, oldValue, newValue) -> applyChannelValue();
		channel.addListener(_channelListener);

		applyChannelValue();
	}

	/**
	 * Re-establishes the binding after the tree changed.
	 *
	 * <p>
	 * To be called by the owner of the tree whenever its structure changed, which is what
	 * {@link ObservableTreeModel#addStructureListener(Runnable)} announces: the selection is
	 * expressed on the nodes the tree has now, the channel value is applied again - so an object
	 * that gets a node only now is selected and revealed - and when a node this tree displayed as
	 * selected is gone, the surviving selection is written to the channel, <code>null</code> where
	 * nothing of it is left.
	 * </p>
	 */
	public void structureChanged() {
		Map<Object, DefaultTreeUINode> survivors = new LinkedHashMap<>();
		for (Object businessObject : _displayedObjects) {
			DefaultTreeUINode node = locate(businessObject);
			if (node != null) {
				survivors.put(businessObject, node);
			}
		}
		boolean displayedNodeGone = survivors.size() < _displayedObjects.size();

		// A tree built anew has other nodes, so what survived is selected on the nodes of the tree
		// displayed now - the selection holding nodes of a tree nobody displays any more would be
		// shown nowhere.
		showSelection(new LinkedHashSet<>(survivors.values()), false);
		_displayedObjects = new LinkedHashSet<>(survivors.keySet());

		applyChannelValue();

		if (displayedNodeGone) {
			// The value names nodes nobody can see any more, so it is replaced by what is left of
			// the selection - which is no selection at all when every displayed node vanished.
			writeSelection(_displayedObjects);
		}
	}

	/**
	 * Detaches from the tree's selection and from the channel.
	 *
	 * <p>
	 * To be called by the owner when the tree goes away. The selection and the channel outlive the
	 * control displaying them, so a binding that kept listening would express a selection on a tree
	 * nobody shows.
	 * </p>
	 */
	public void dispose() {
		_channel.removeListener(_channelListener);
		_selectionModel.removeSelectionListener(_selectionListener);
	}

	/**
	 * Writes a selection made in the tree to the channel.
	 */
	private void handleSelectionChanged(SelectionModel<Object> model, SelectionEvent<Object> event) {
		Set<Object> objects = businessObjects(event.getNewSelection());
		if (!_applyingFromChannel) {
			writeSelection(objects);
		}
		_displayedObjects = objects;
	}

	/**
	 * Writes the given business objects to the channel: one as that object, several as their
	 * {@link Set}, none as <code>null</code>.
	 */
	private void writeSelection(Set<Object> objects) {
		if (objects.size() == 1) {
			_channel.set(objects.iterator().next());
		} else if (objects.isEmpty()) {
			_channel.set(null);
		} else {
			_channel.set(new LinkedHashSet<>(objects));
		}
	}

	/**
	 * Displays the channel's current value as the tree's selection, leaving the selection where it
	 * is where the tree has no node for the value.
	 */
	private void applyChannelValue() {
		Object value = _channel.get();
		if (isDisplayed(value)) {
			// The tree already shows this selection: it is the tree's own, echoed back through the
			// channel.
			return;
		}
		if (value == null) {
			showSelection(Set.of(), false);
			return;
		}
		if (value instanceof Collection<?> objects) {
			if (!_selectionModel.isMultiSelectionSupported()) {
				// A tree selecting one node at a time has no way of showing a selection of several.
				showSelection(Set.of(), false);
				return;
			}
			Set<DefaultTreeUINode> nodes = locateAll(objects);
			if (!nodes.isEmpty()) {
				showSelection(nodes, true);
			}
			return;
		}
		DefaultTreeUINode node = locate(value);
		if (node != null) {
			showSelection(Set.of(node), true);
		}
	}

	/**
	 * Makes the given nodes the tree's selection, and pushes that to the client.
	 *
	 * @param nodes
	 *        The nodes to select, empty to select nothing.
	 * @param reveal
	 *        Whether to open the subtrees above the given nodes, so that they are displayed.
	 */
	private void showSelection(Set<DefaultTreeUINode> nodes, boolean reveal) {
		_applyingFromChannel = true;
		try {
			if (reveal) {
				for (DefaultTreeUINode node : nodes) {
					TreeNodes.revealNode(node);
				}
			}
			_selectionModel.setSelection(nodes);
		} finally {
			_applyingFromChannel = false;
		}

		// Unlike a selection or an expansion the user made, which flows through the control's own
		// command handlers, this change is made on the models directly, so the control's visible
		// node state has to be rebuilt explicitly.
		_tree.updateVisibleState();
	}

	/**
	 * Whether the tree's current selection is exactly the given channel value.
	 */
	private boolean isDisplayed(Object value) {
		Set<Object> selected = businessObjects(_selectionModel.getSelection());
		if (value == null) {
			return selected.isEmpty();
		}
		if (value instanceof Collection<?> objects) {
			return selected.equals(new LinkedHashSet<Object>(objects));
		}
		return selected.size() == 1 && selected.contains(value);
	}

	/**
	 * The nodes standing for the given objects, leaving out the objects the tree has no node for.
	 */
	private Set<DefaultTreeUINode> locateAll(Collection<?> objects) {
		Set<DefaultTreeUINode> nodes = new LinkedHashSet<>();
		for (Object businessObject : objects) {
			DefaultTreeUINode node = locate(businessObject);
			if (node != null) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	/**
	 * The node standing for the given object, {@code null} where the tree holds none.
	 */
	private DefaultTreeUINode locate(Object businessObject) {
		if (businessObject == null) {
			return null;
		}
		DefaultTreeUINodeModel treeModel = _treeModel.get();
		return treeModel == null ? null : _locator.locate(treeModel.getRoot(), businessObject);
	}

	/**
	 * The business objects the given nodes stand for.
	 */
	private static Set<Object> businessObjects(Collection<?> nodes) {
		Set<Object> objects = new LinkedHashSet<>();
		for (Object node : nodes) {
			objects.add(TLTreeModelUtil.getInnerBusinessObject(node));
		}
		return objects;
	}

}
