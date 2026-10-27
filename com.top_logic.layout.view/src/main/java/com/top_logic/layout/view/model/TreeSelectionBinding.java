/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeNodeComparator;
import com.top_logic.layout.view.channel.DeletedObjects;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;

/**
 * Two-way binding between the selection of a tree and a {@link ViewChannel}.
 *
 * <p>
 * The tree is shown by a {@link TreeSelectionDisplay} - a tree control, or a table displaying the
 * tree as its rows -, which the binding tells the selection to show and which reports the selection
 * the user makes.
 * </p>
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
 * more, so the surviving selection is written in its place. Where nothing of the selection is left,
 * the node the vanished node hung in becomes the selection and its object is written - the nearest
 * node above it the tree still holds, where its parent is gone too, and of several vanished nodes
 * the first in display order that still has one. The root counts only where the tree
 * {@link DefaultTreeUINodeModel#isRootVisible() displays it}; without such a node, the channel is
 * cleared.</li>
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
 * A channel drops an object it holds once that object is deleted (see
 * {@link com.top_logic.layout.view.channel.DefaultViewChannel}): the value then is the displayed
 * selection without the deleted objects - <code>null</code> where nothing else was selected. That
 * value announces the deletion, it asks for no selection: the tree leaves its selection and its
 * memory of it alone (see {@link DeletedObjects#dropsDeletedOnly(Collection, Object)}), and answers
 * with its next selection once its own structure change removes the nodes - the survivors, or the
 * node the deleted one hung in. A deleted object the channel still holds - one nobody attached -
 * has no node, like any foreign value, and is replaced the same way.
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

	private final TreeSelectionDisplay _display;

	private final Supplier<DefaultTreeUINodeModel> _treeModel;

	private final NodeLocator _locator;

	private final ViewChannel _channel;

	private final Registration _channelRegistration;

	private final Registration _selectionRegistration;

	/** Whether a channel value is currently being applied, so the tree's echo is not written back. */
	private boolean _applyingFromChannel;

	/**
	 * The business objects of the nodes the tree last displayed as selected, in display order, each
	 * with the business objects of the nodes above its node at that time, from its parent up to the
	 * root.
	 *
	 * <p>
	 * The nodes above are remembered because a vanished node is already taken out of the tree when
	 * {@link #structureChanged()} learns about it, so the node it hung in can no longer be asked
	 * for.
	 * </p>
	 */
	private Map<Object, List<Object>> _displayed = Map.of();

	/**
	 * Creates a {@link TreeSelectionBinding} and applies the channel's current value to the tree.
	 *
	 * @param display
	 *        The display of the tree, showing its selection.
	 * @param treeModel
	 *        The tree displayed now, which is another one after the tree was built anew.
	 * @param locator
	 *        Finds the node standing for an object of the channel.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public TreeSelectionBinding(TreeSelectionDisplay display, Supplier<DefaultTreeUINodeModel> treeModel,
			NodeLocator locator, ViewChannel channel) {
		_display = display;
		_treeModel = treeModel;
		_locator = locator;
		_channel = channel;

		_selectionRegistration = display.addSelectionListener(this::handleSelectionChanged);
		_channelRegistration = channel.addListener((sender, oldValue, newValue) -> applyChannelValue());

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
	 * selected is gone, the surviving selection is written to the channel. Where nothing of the
	 * selection survives, the node the first vanished node hung in is selected, revealed and written
	 * instead - or the nearest node above it the tree still holds and displays -, and
	 * <code>null</code> where there is no such node.
	 * </p>
	 */
	public void structureChanged() {
		// A value that only announced the deletion of displayed objects is answered here, not
		// applied: applying it would clear the selection this change chooses in their place.
		boolean deletionAnnounced = DeletedObjects.dropsDeletedOnly(_displayed.keySet(), _channel.get());

		Set<DefaultTreeUINode> survivors = new LinkedHashSet<>();
		List<List<Object>> vanishedAncestors = new ArrayList<>();
		for (Map.Entry<Object, List<Object>> entry : _displayed.entrySet()) {
			DefaultTreeUINode node = locate(entry.getKey());
			if (node != null) {
				survivors.add(node);
			} else {
				vanishedAncestors.add(entry.getValue());
			}
		}
		boolean displayedNodeGone = !vanishedAncestors.isEmpty();

		Set<DefaultTreeUINode> selection;
		boolean reveal;
		if (survivors.isEmpty() && displayedNodeGone) {
			// The user's place in the tree is the node the selection hung in, not its root.
			selection = nearestDisplayedAncestor(vanishedAncestors);
			reveal = true;
		} else {
			selection = survivors;
			reveal = false;
		}

		// A tree built anew has other nodes, so what survived is selected on the nodes of the tree
		// displayed now - the selection holding nodes of a tree nobody displays any more would be
		// shown nowhere.
		showSelection(selection, reveal);
		recordDisplayed(selection);

		if (!deletionAnnounced) {
			applyChannelValue();
		}

		if (displayedNodeGone) {
			// The value names nodes nobody can see any more, so it is replaced by what the tree
			// selects now - which is no selection at all when no node is left to select.
			writeSelection(_displayed.keySet());
		}
	}

	/**
	 * The node the tree still holds and displays that is nearest above a vanished node.
	 *
	 * @param vanishedAncestors
	 *        For each vanished node in display order, the business objects of the nodes above it,
	 *        from its parent up to the root.
	 * @return The node above the first vanished node that has one, empty where none has.
	 */
	private Set<DefaultTreeUINode> nearestDisplayedAncestor(List<List<Object>> vanishedAncestors) {
		DefaultTreeUINodeModel treeModel = _treeModel.get();
		if (treeModel == null) {
			return Set.of();
		}
		for (List<Object> ancestors : vanishedAncestors) {
			for (Object ancestor : ancestors) {
				DefaultTreeUINode node = locate(ancestor);
				if (node != null && (node != treeModel.getRoot() || treeModel.isRootVisible())) {
					return Set.of(node);
				}
			}
		}
		return Set.of();
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
		_channelRegistration.dispose();
		_selectionRegistration.dispose();
	}

	/**
	 * Writes a selection made in the tree to the channel.
	 */
	private void handleSelectionChanged(Collection<DefaultTreeUINode> newSelection) {
		if (!_applyingFromChannel) {
			writeSelection(TreeSelectionDisplay.businessObjects(newSelection));
		}
		recordDisplayed(newSelection);
	}

	/**
	 * Remembers the given selected nodes as {@link #_displayed}.
	 */
	private void recordDisplayed(Collection<DefaultTreeUINode> selectedNodes) {
		List<DefaultTreeUINode> nodes = new ArrayList<>(selectedNodes);
		if (nodes.size() > 1) {
			nodes.sort(displayOrder(nodes.get(0)));
		}

		Map<Object, List<Object>> displayed = new LinkedHashMap<>();
		for (DefaultTreeUINode node : nodes) {
			List<Object> path = TLTreeModelUtil.createPathToRootUserObject(node);
			List<Object> ancestors = new ArrayList<>(path.subList(1, path.size()));
			displayed.put(TLTreeModelUtil.getInnerBusinessObject(node), ancestors);
		}
		_displayed = displayed;
	}

	/**
	 * The order in which the tree of the given node displays its nodes.
	 */
	@SuppressWarnings("unchecked")
	private static Comparator<DefaultTreeUINode> displayOrder(DefaultTreeUINode node) {
		Comparator<DefaultTreeUINode> siblingOrder = Comparator.comparingInt(n -> n.getParent().getIndex(n));
		return new TreeNodeComparator(node.getModel(), siblingOrder);
	}

	/**
	 * Writes the given business objects to the channel: one as that object, several as their
	 * {@link Set}, none as <code>null</code>.
	 */
	private void writeSelection(Collection<Object> objects) {
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
		if (DeletedObjects.dropsDeletedOnly(_displayed.keySet(), value)) {
			// The channel announces the deletion of displayed objects. The tree answers with its
			// next selection when its structure change removes their nodes; until then, it keeps
			// the selection and the memory of what it displayed, which that answer is computed from.
			return;
		}
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
			if (!_display.canDisplaySeveral()) {
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
	 * Makes the given nodes the tree's selection, see
	 * {@link TreeSelectionDisplay#showSelection(Set, boolean)}.
	 *
	 * @param nodes
	 *        The nodes to select, empty to select nothing.
	 * @param reveal
	 *        Whether to make the given nodes visible.
	 */
	private void showSelection(Set<DefaultTreeUINode> nodes, boolean reveal) {
		_applyingFromChannel = true;
		try {
			_display.showSelection(nodes, reveal);
		} finally {
			_applyingFromChannel = false;
		}
	}

	/**
	 * Whether the tree's current selection is exactly the given channel value.
	 */
	private boolean isDisplayed(Object value) {
		Set<Object> selected = _display.getSelectedObjects();
		if (value == null) {
			return selected.isEmpty();
		}
		if (value instanceof Collection<?> objects) {
			return selected.equals(new LinkedHashSet<>(objects));
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
	 *
	 * <p>
	 * A deleted object is nowhere in the tree, and it is not handed to the {@link NodeLocator}:
	 * nothing can be computed over an object that is gone, so a locator walking up from it - asking
	 * what holds it - fails on the spot. It is therefore no survivor of a change, and the selection
	 * that is left, or the node above it, takes its place on the channel.
	 * </p>
	 */
	private DefaultTreeUINode locate(Object businessObject) {
		if (businessObject == null) {
			return null;
		}
		if (businessObject instanceof TLObject model && !model.tValid()) {
			return null;
		}
		DefaultTreeUINodeModel treeModel = _treeModel.get();
		return treeModel == null ? null : _locator.locate(treeModel.getRoot(), businessObject);
	}

}
