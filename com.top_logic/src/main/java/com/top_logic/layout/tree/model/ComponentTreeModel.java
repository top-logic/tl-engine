/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.layout.tabbar.TabBarModel.TabBarListener;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.SimpleCard;

/**
 * The class {@link ComponentTreeModel} creates a {@link #getTree() tree} that
 * represents the tab structure below a given root component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentTreeModel {

	private final SingleSelectionModel _selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);

	private final Map<DefaultMutableTLTreeNode, InternalTabBarListener> _listener =
		new HashMap<>();

	private final MutableTLTreeModel _treeModel = new DefaultMutableTLTreeModel();

	private final Map<DefaultMutableTLTreeNode, TabBarModel> _tabBarByNode =
		new HashMap<>();

	/**
	 * Creates a new {@link ComponentTreeModel} based on the given component.
	 * 
	 * @param rootComponent
	 *        {@link LayoutComponent} that serves as root of th component tree.
	 */
	public ComponentTreeModel(LayoutComponent rootComponent) {
		DefaultMutableTLTreeNode rootNode = _treeModel.getRoot();
		SimpleCard rootUserObject = new SimpleCard(rootComponent.getName().qualifiedName(), rootComponent);
		rootNode.setBusinessObject(rootUserObject);

		createSubTree(rootNode, rootUserObject);
		initSubtree(rootNode);
	}

	/**
	 * Returns a {@link SingleSelectionModel} which selects the node in the
	 * returned tree.
	 * 
	 * The selected node is the one which represents the currently visible
	 * {@link LayoutComponent business component}.
	 */
	public final SingleSelectionModel getSelectionModel() {
		return _selectionModel;
	}

	/**
	 * Returns the tree which represents the tab structure. I.e. each leaf of
	 * the tree represents a business component. The parent of such a node
	 * represents the direct tab parent of that node, so if components are
	 * siblings in the same {@link TabComponent tab bar} they are also siblings
	 * in the tree.
	 * 
	 * <p>
	 * The nodes of the returned tree are {@link DefaultMutableTLTreeNode}s. The
	 * {@link DefaultMutableTLTreeNode#getBusinessObject() business object} of the node is the
	 * card of {@link LayoutComponent business component} in the tab bar model
	 * that component belongs to.
	 * </p>
	 */
	public final TLTreeModel<? extends TLTreeNode<?>> getTree() {
		return _treeModel;
	}

	/**
	 * This method creates the subtree of the given {@link DefaultMutableTLTreeNode}
	 * with the {@link Card}.
	 */
	private void createSubTree(DefaultMutableTLTreeNode node, Card card) {
		TabBarModel tabBarModel = getTabBarModel(card.getContent());
		if (tabBarModel == null) {
			return;
		}
		_tabBarByNode.put(node, tabBarModel);
		updateSubTree(node, tabBarModel);
	}

	void updateSubTree(DefaultMutableTLTreeNode root, TabBarModel tabBarModel) {
		List<Card> cards = tabBarModel.getAllCards();
		for (int index = 0, size = cards.size(); index < size; index++) {
			Card currentCard = cards.get(index);
			createSubTree(root.createChild(currentCard), currentCard);
		}
	}

	/**
	 * This method returns a {@link TabBarModel} which lies &quot;under&quot;
	 * the given <code>baseObject</code>
	 * 
	 * @param baseObject
	 *        the object for which a {@link TabBarModel} is searched
	 * @return is <code>null</code> if no {@link TabBarModel} is found.
	 */
	private static TabBarModel getTabBarModel(Object baseObject) {
		if (baseObject instanceof TabBarModel) {
			return (TabBarModel) baseObject;
		}
		if (baseObject instanceof TabComponent) {
			return ((TabComponent) baseObject).getTabBarModel();
		}
		if (baseObject instanceof LayoutContainer) {
			Collection<LayoutComponent> children = ((LayoutContainer) baseObject).getChildList();
			if (children != null) {
				for (Object child : children) {
					TabBarModel tabBarModel = getTabBarModel(child);
					if (tabBarModel != null) {
						return tabBarModel;
					}
				}
			}
		}
		return null;
	}

	void initSubtree(DefaultMutableTLTreeNode node) {
		initSubtree(node, nodeVisible(node));
	}

	/**
	 * Whether the given node is visible, i.e. whether on the way to root the represented
	 * {@link TabBarModel} have the index of the node as selected index.
	 */
	private boolean nodeVisible(DefaultMutableTLTreeNode node) {
		List<DefaultMutableTLTreeNode> pathToRoot = _treeModel.createPathToRoot(node);
		for (int index = pathToRoot.size() - 2; index >= 0; index--) {
			DefaultMutableTLTreeNode parent = pathToRoot.get(index + 1);
			DefaultMutableTLTreeNode child = pathToRoot.get(index);
			TabBarModel tabBar = _tabBarByNode.get(parent);
			if (tabBar == null) {
				return false;
			}
			if (tabBar.getSelectedIndex() != parent.getIndex(child)) {
				return false;
			}
		}
		return true;
	}

	private void initSubtree(DefaultMutableTLTreeNode node, boolean nodeVisible) {
		final TabBarModel tabBarModel = _tabBarByNode.get(node);
		if (tabBarModel != null) {
			final int selectedIndex = tabBarModel.getSelectedIndex();

			if (_listener.get(node) == null) {
				InternalTabBarListener internalTabBarListener = new InternalTabBarListener(node, tabBarModel);
				_listener.put(node, internalTabBarListener);
			}
			List<DefaultMutableTLTreeNode> children = node.getChildren();
			for (int index = 0, size = children.size(); index < size; index++) {
				DefaultMutableTLTreeNode childNode = children.get(index);
				if (nodeVisible && selectedIndex == index) {
					getSelectionModel().setSingleSelection(childNode);
					initSubtree(childNode, true);
				} else {
					initSubtree(childNode, false);
				}
			}
		}
	}

	/**
	 * removes the subtree starting with the given node from the tree
	 * 
	 * @param node
	 *        the root node of the subtree to remove
	 */
	void removeSubtree(DefaultMutableTLTreeNode node) {
		InternalTabBarListener listener = _listener.remove(node);
		if (listener != null) {
			listener.detach();
		}
		_tabBarByNode.remove(node);
		removeChildren(node);
	}

	void removeChildren(DefaultMutableTLTreeNode node) {
		List<DefaultMutableTLTreeNode> children = node.getChildren();
		for (int index = 0, size = children.size(); index < size; index++) {
			DefaultMutableTLTreeNode childNode = children.get(index);
			removeSubtree(childNode);
		}
		// remove direct children
		node.clearChildren();
	}

	/**
	 * Changes the current selection to the node of the subtree starting with
	 * the given node which represents the currently visible business component.
	 * 
	 * @param node
	 *        the node to in whose subtree the node exists which represents the
	 *        currently visible business component
	 */
	void changeSelection(DefaultMutableTLTreeNode node) {
		TabBarModel tabBar = _tabBarByNode.get(node);
		if (tabBar == null) {
			getSelectionModel().setSingleSelection(node);
		} else {
			int selectedIndex = tabBar.getSelectedIndex();
			if (selectedIndex != -1) {
				changeSelection(node.getChildAt(selectedIndex));
			} else {
				getSelectionModel().setSingleSelection(node);
			}
		}

	}

	/**
	 * The class {@link InternalTabBarListener} will be informs some node about
	 * the change of a {@link TabBarModel} to update its children.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private class InternalTabBarListener implements TabBarListener {

		private final DefaultMutableTLTreeNode _node;

		private final TabBarModel _tabBarModel;

		public InternalTabBarListener(DefaultMutableTLTreeNode node, TabBarModel tabBarModel) {
			_node = node;
			_tabBarModel = tabBarModel;
			_tabBarModel.addTabBarListener(this);
		}

		void detach() {
			_tabBarModel.removeTabBarListener(this);
		}

		@Override
		public void inactiveCardChanged(TabBarModel sender, Card card) {
			// currently nothing to do
		}

		@Override
		public void notifyCardsChanged(TabBarModel sender, List<Card> oldAllCards) {
			if (sender != _tabBarModel) {
				return;
			}
			removeChildren(_node);
			updateSubTree(_node, sender);
			initSubtree(_node);
		}

		@Override
		public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
			if (model != _tabBarModel) {
				return;
			}
			int selectedIndex = _tabBarModel.getSelectedIndex();
			if (selectedIndex != -1) {
				changeSelection(_node.getChildAt(selectedIndex));
			}
		}
	}

}
