/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.tree.ReactTreeControl;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.table.Row;
import com.top_logic.table.SelectionMode;

/**
 * The display of a tree whose selection a {@link TreeSelectionBinding} binds to a channel: it
 * shows the selection the binding chooses, and reports the selection the user makes.
 *
 * <p>
 * The selection is expressed on the nodes of a {@link DefaultTreeUINodeModel}, whatever the display
 * draws them as - the nodes of a {@link #of(ReactTreeControl, SelectionModel) tree} or the rows of a
 * {@link #of(TableViewControl, Supplier) table displaying a tree}.
 * </p>
 */
public interface TreeSelectionDisplay {

	/**
	 * Notified when the selection of a {@link TreeSelectionDisplay} changed.
	 */
	@FunctionalInterface
	interface Listener {

		/**
		 * Called after the selection changed.
		 *
		 * @param selection
		 *        The nodes selected now.
		 */
		void selectionChanged(Collection<DefaultTreeUINode> selection);
	}

	/**
	 * The {@link TLTreeModelUtil#getInnerBusinessObject(Object) business objects} of the nodes
	 * displayed as selected.
	 */
	Set<Object> getSelectedObjects();

	/**
	 * Whether the display shows several selected nodes at once.
	 */
	boolean canDisplaySeveral();

	/**
	 * Makes the given nodes the selection, and pushes it to the client.
	 *
	 * <p>
	 * The {@link #addSelectionListener(Listener) listeners} are told about the change like about
	 * one the user made.
	 * </p>
	 *
	 * @param nodes
	 *        The nodes to select, empty to select nothing.
	 * @param reveal
	 *        Whether to make the given nodes visible: the nodes above them are opened, and a
	 *        display scrolling its nodes scrolls to them.
	 */
	void showSelection(Set<DefaultTreeUINode> nodes, boolean reveal);

	/**
	 * Registers a listener for every change of the selection.
	 *
	 * @return The registration that drops the listener again.
	 */
	Registration addSelectionListener(Listener listener);

	/**
	 * The {@link TreeSelectionDisplay} of a tree control.
	 *
	 * @param tree
	 *        The control displaying the tree, told to push a selection made here to the client.
	 * @param selectionModel
	 *        The selection of that tree, holding its nodes.
	 */
	static TreeSelectionDisplay of(ReactTreeControl tree, SelectionModel<Object> selectionModel) {
		return new TreeSelectionDisplay() {
			@Override
			public Set<Object> getSelectedObjects() {
				return businessObjects(selectionModel.getSelection());
			}

			@Override
			public boolean canDisplaySeveral() {
				return selectionModel.isMultiSelectionSupported();
			}

			@Override
			public void showSelection(Set<DefaultTreeUINode> nodes, boolean reveal) {
				if (reveal) {
					for (DefaultTreeUINode node : nodes) {
						TreeNodes.revealNode(node);
					}
				}
				selectionModel.setSelection(nodes);

				// Unlike a selection or an expansion the user made, which flows through the
				// control's own command handlers, this change is made on the models directly, so
				// the control's visible node state has to be rebuilt explicitly.
				tree.updateVisibleState();
			}

			@Override
			public Registration addSelectionListener(Listener listener) {
				SelectionListener<Object> selectionListener =
					(model, event) -> listener.selectionChanged(nodes(event.getNewSelection()));
				selectionModel.addSelectionListener(selectionListener);
				return Registration.onDispose(() -> selectionModel.removeSelectionListener(selectionListener));
			}
		};
	}

	/**
	 * The {@link TreeSelectionDisplay} of a table displaying a tree as its rows.
	 *
	 * <p>
	 * The {@link Row#key() key} of a row is the business object of its node. Revealing a node opens
	 * the rows above it, and selecting a row scrolls it into view.
	 * </p>
	 *
	 * @param table
	 *        The table displaying the tree.
	 * @param treeModel
	 *        The tree the table displays now, which is another one after the tree was built anew.
	 */
	static TreeSelectionDisplay of(TableViewControl<?> table, Supplier<DefaultTreeUINodeModel> treeModel) {
		return new TreeSelectionDisplay() {
			@Override
			public Set<Object> getSelectedObjects() {
				return new LinkedHashSet<>(table.getSelectedKeys());
			}

			@Override
			public boolean canDisplaySeveral() {
				return table.getSelectionMode() == SelectionMode.MULTI;
			}

			@Override
			public void showSelection(Set<DefaultTreeUINode> nodes, boolean reveal) {
				Set<Object> keys = businessObjects(nodes);
				if (reveal) {
					table.expandRows(ancestorKeys(nodes));
				} else if (keys.equals(table.getSelectedKeys())) {
					// Selecting the rows again would scroll the table back to them.
					return;
				}
				table.selectRows(keys);
			}

			@Override
			public Registration addSelectionListener(Listener listener) {
				TableViewControl.SelectionListener selectionListener =
					keys -> listener.selectionChanged(nodesOf(keys));
				table.addSelectionListener(selectionListener);
				return Registration.onDispose(() -> table.removeSelectionListener(selectionListener));
			}

			/**
			 * The nodes of the rows with the given keys, leaving out the keys the tree has no node
			 * for.
			 */
			private List<DefaultTreeUINode> nodesOf(Set<Object> keys) {
				DefaultTreeUINodeModel model = treeModel.get();
				List<DefaultTreeUINode> result = new ArrayList<>(keys.size());
				if (model == null) {
					return result;
				}
				for (Object key : keys) {
					// A row is displayed, so the child list holding its node is computed.
					DefaultTreeUINode node = TreeNodes.findComputedNode(model.getRoot(), key);
					if (node != null) {
						result.add(node);
					}
				}
				return result;
			}

			/**
			 * The row keys of the nodes above the given ones, the root - which has no row - left
			 * out.
			 */
			private Set<Object> ancestorKeys(Set<DefaultTreeUINode> nodes) {
				Set<Object> result = new LinkedHashSet<>();
				for (DefaultTreeUINode node : nodes) {
					List<Object> ancestors = new ArrayList<>();
					for (DefaultTreeUINode parent = node.getParent(); parent != null
						&& parent.getParent() != null; parent = parent.getParent()) {
						ancestors.add(parent.getBusinessObject());
					}
					// Top-down, so every row is opened below an open one.
					for (int n = ancestors.size() - 1; n >= 0; n--) {
						result.add(ancestors.get(n));
					}
				}
				return result;
			}
		};
	}

	/**
	 * The given selected nodes as tree nodes.
	 */
	private static List<DefaultTreeUINode> nodes(Collection<?> selection) {
		List<DefaultTreeUINode> result = new ArrayList<>(selection.size());
		for (Object node : selection) {
			result.add((DefaultTreeUINode) node);
		}
		return result;
	}

	/**
	 * The business objects the given nodes stand for.
	 */
	static Set<Object> businessObjects(Collection<?> nodes) {
		Set<Object> objects = new LinkedHashSet<>();
		for (Object node : nodes) {
			objects.add(TLTreeModelUtil.getInnerBusinessObject(node));
		}
		return objects;
	}

}
