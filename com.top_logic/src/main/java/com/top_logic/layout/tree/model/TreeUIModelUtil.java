/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode;
import com.top_logic.util.Utils;


/**
 * Some helper-methods to use with a TreeUIModel
 *
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class TreeUIModelUtil {

	/**
	 * Expands all ancestors of the given node in the given {@link TreeUIModel}.
	 */
	public static <N> void expandParents(TreeUIModel<N> model, N node) {
		N parent = model.getParent(node);
		if (parent != null) {
			expandSelfAndParents(model, parent);
		}
	}

	/**
	 * Expands all ancestors and the given node in the given {@link TreeUIModel}.
	 */
	public static <N> void expandSelfAndParents(TreeUIModel<N> model, N node) {
		do {
			model.setExpanded(node, true);
			node = model.getParent(node);
		} while (node != null);
	}

    /**
	 * Convenience method to recover a expansion-state of a TreeUIModel from a Set. The Set contains
	 * the expanded nodes. This assumes that the Tree is initially collapsed! You can use
	 * {@link TreeUIModelUtil#getExpansionModel(TreeUIModel)} to get such a Set.
	 * 
	 * @param expansionModel
	 *        A collection containing the expanded nodes.
	 * @param aTreeUIModel
	 *        the Model to apply the map to
	 */
	public static void setExpansionModel(Collection<? extends Object> expansionModel, TreeUIModel aTreeUIModel) {
        Iterator iter = expansionModel.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            aTreeUIModel.setExpanded(obj, true);
        }
    }

    /**
     * Convenience method to extract a expansion-model from a TreeUIModel. A Set is used
     * to store the expansion-state of the TreeUIModel.
     * The tree-structure is only analyzed up to not-expanded nodes. This means sub-nodes
     * from not-expanded nodes are not stored in this Set. (Information about not visible
     * expanded nodes is lost!)
     */
	public static Collection<Object> getExpansionModel(TreeUIModel aTreeUIModel) {
        Set result = new HashSet();
        Object root = aTreeUIModel.getRoot();
        getExpansionModelRecursively(result, aTreeUIModel, root);
        return result;
    }

    private static void getExpansionModelRecursively(Set expansionModel, TreeUIModel aTreeUIModel, Object node) {
        if (aTreeUIModel.isExpanded(node) || aTreeUIModel.getRoot().equals(node)) {
            Iterator iter = aTreeUIModel.getChildIterator(node);
            expansionModel.add(node);
            while (iter.hasNext()) {
                getExpansionModelRecursively(expansionModel, aTreeUIModel, iter.next());
            }
        }
    }

	/**
	 * Expands or collapses all visible nodes in the given {@link TreeUIModel}.
	 */
	public static <N> void setExpandedAll(TreeUIModel<N> treeModel, boolean expand) {
		N root = treeModel.getRoot();
		if (!expand && treeModel.isRootVisible()) {
			// Only collapse the children of the root node, because an invisible root node must stay
			// expanded.
			setExpandedAllChildren(treeModel, root, false);
		} else {
			setExpandedAll(treeModel, root, expand);
		}
	}

	/**
	 * Sets the expansion state of all children of the given node. 
	 */
	public static <N> void setExpandedAllChildren(TreeUIModel<N> treeModel, N node, boolean expand) {
		List<? extends N> rootChildren = treeModel.getChildren(node);
		rootChildren.forEach(child -> setExpandedAll(treeModel, child, expand));
	}

    /**
     * Set the expanded state of the given node and all its children to the given expanded state.
     */
    public static void setExpandedAll(TreeUIModel aModel, Object aNode, boolean expanded) {
        if (!aModel.isLeaf(aNode)) {
            aModel.setExpanded(aNode, expanded);
            for (Iterator theIt = aModel.getChildIterator(aNode); theIt.hasNext();) {
                setExpandedAll(aModel, theIt.next(), expanded);
            }
        }
    }

	/**
	 * Set the expanded state of the given node and its children up to the given depth to the given
	 * expanded state.
	 */
	public static void setExpanded(TreeUIModel aModel, Object aNode, boolean expanded, int expansionDepth) {
		if (expansionDepth > 0) {
			if (!aModel.isLeaf(aNode)) {
				aModel.setExpanded(aNode, expanded);
				for (Iterator theIt = aModel.getChildIterator(aNode); theIt.hasNext();) {
					setExpanded(aModel, theIt.next(), expanded, expansionDepth - 1);
				}
			}
		}
	}

	/**
	 * Same as {@link #getExpansionModel(TreeUIModel)} with {@link BusinessObjectMapping}.
	 */
	public static Collection<Object> getExpansionUserModel(TreeUIModel<?> aTreeUIModel) {
		return getExpansionUserModel(aTreeUIModel, BusinessObjectMapping.INSTANCE);
    }

	/**
	 * Convenience method to extract a expansion model from a TreeUIModel. A Set is used to store
	 * the expansion state of the TreeUIModel. In opposition to the
	 * {@link #getExpansionModel(TreeUIModel)} method, the expansion states will be computed not by
	 * node but by the user object of the nodes, retrieved by given mapping, and completely, not
	 * only the visible nodes.
	 *
	 * The given TreeUIModel must use TLTreeNodes as nodes.
	 *
	 * @param aTreeUIModel
	 *        the TreeUIModel to get the expansion states
	 * @return a Set containing the information about the expansion states
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Object> getExpansionUserModel(TreeUIModel<?> aTreeUIModel,
			Mapping<TLTreeNode<?>, Object> userObjectMapping) {
		Set<Object> result = new HashSet<>();
		TLTreeNode<?> root = (TLTreeNode<?>) aTreeUIModel.getRoot();
		getExpansionUserModelRecursively(result, (TreeUIModel<TLTreeNode<?>>) aTreeUIModel, root, userObjectMapping);
		return result;
	}

	private static void getExpansionUserModelRecursively(Set<Object> aExpansionModel,
			TreeUIModel<TLTreeNode<?>> aTreeUIModel, TLTreeNode<?> aNode,
			Mapping<TLTreeNode<?>, Object> userObjectMapping) {
        if (aTreeUIModel.isExpanded(aNode)) {
			aExpansionModel.add(userObjectMapping.map(aNode));
        }
		for (Iterator<?> it = aNode.getChildren().iterator(); it.hasNext();) {
			getExpansionUserModelRecursively(aExpansionModel, aTreeUIModel, (TLTreeNode<?>) it.next(),
				userObjectMapping);
        }
    }

    /**
	 * Same as {@link #setExpansionUserModel(Collection, TreeUIModel, Mapping)} with
	 * {@link BusinessObjectMapping}.
	 */
	public static void setExpansionUserModel(Collection<? extends Object> aExpansionModel,
			TreeUIModel<?> aTreeUIModel) {
		setExpansionUserModel(aExpansionModel, aTreeUIModel, BusinessObjectMapping.INSTANCE);
	}

	/**
	 * Convenience method to recover a expansion state of a TreeUIModel from a set, which was
	 * created by the {@link TreeUIModelUtil#getExpansionUserModel(TreeUIModel)} method. All nodes
	 * with an user object, retrieved by given mapping, which is contained in the set gets expanded,
	 * all others gets collapsed.
	 * 
	 * The given TreeUIModel must use TLTreeNodes as nodes.
	 * 
	 * @param aExpansionModel
	 *        a Set containing the information about the expansion states
	 * @param aTreeUIModel
	 *        the TreeUIModel to set the expansion states
	 * @param userObjectMapping
	 *        the mapping to retrieve user object from node
	 */
	@SuppressWarnings("unchecked")
	public static void setExpansionUserModel(Collection<? extends Object> aExpansionModel, TreeUIModel<?> aTreeUIModel,
			Mapping<TLTreeNode<?>, Object> userObjectMapping) {
		TLTreeNode<?> root = (TLTreeNode<?>) aTreeUIModel.getRoot();
		setExpansionUserModelRecursively(aExpansionModel, (TreeUIModel<TLTreeNode<?>>) aTreeUIModel, root,
			userObjectMapping);
    }

	private static void setExpansionUserModelRecursively(Collection<? extends Object> expansionModel,
			TreeUIModel<TLTreeNode<?>> aTreeUIModel,
			TLTreeNode<?> node,
			Mapping<TLTreeNode<?>, Object> userObjectMapping) {
		aTreeUIModel.setExpanded(node, expansionModel.contains(userObjectMapping.map(node)));
		for (Iterator<?> it = node.getChildren().iterator(); it.hasNext();) {
			setExpansionUserModelRecursively(expansionModel, aTreeUIModel, (TLTreeNode<?>) it.next(),
				userObjectMapping);
        }
    }

	/**
	 * node {@link Iterator} of the visible subtree of the given {@link TreeUIModel} in DFS
	 *         order.
	 */
	public static <N> Iterator<N> createVisibleNodeDFSIterator(TreeUIModel<N> treeModel) {
		FilterTreeModel<N> filteredModel = new FilterTreeModel<>(treeModel, node -> {
			N parentNode = treeModel.getParent(node);
			return parentNode == null || treeModel.isExpanded(parentNode);
		});
		return new DescendantDFSIterator<>(filteredModel, filteredModel.getRoot(), treeModel.isRootVisible());
	}

	/**
	 * Returns <code>true</code> if the business objects of the given two tree nodes sets are the
	 * same, otherwise <code>false</code>.
	 * 
	 * @param nodes1
	 *        Collection of tree nodes to compute the business object from to compare it with the
	 *        business objects resulting from the second given set of tree nodes.
	 * @param nodes2
	 *        Collection of tree nodes to compute the business object from to compare it with the
	 *        business objects resulting from the first given set of tree nodes.
	 * 
	 * @see #nodesHasSameBusinessObject(Collection, Collection)
	 */
	public static boolean hasSameBusinessObject(Collection<? extends TreeUINode<?>> nodes1,
			Collection<? extends TreeUINode<?>> nodes2) {
		return nodesHasSameBusinessObject(nodes1, getBusinessObjects(nodes2));
	}

	/**
	 * Returns <code>true</code> if the business objects of the given tree nodes are the same as the
	 * given objects, otherwise <code>false</code>.
	 * 
	 * @param nodes
	 *        Collection of tree nodes to compute the business object from to compare it with the
	 *        other business objects.
	 * @param businessObjects
	 *        Business objects which are compared with the business objects resulting from the given
	 *        set of tree nodes.
	 * 
	 * @see #getBusinessObjects(Collection)
	 */
	public static boolean nodesHasSameBusinessObject(Collection<? extends TreeUINode<?>> nodes,
			Collection<?> businessObjects) {
		return CollectionUtilShared.containsSame(getBusinessObjects(nodes), businessObjects);
	}

	/**
	 * Returns the business objects from the given tree nodes.
	 * 
	 * @param nodes
	 *        Tree nodes to compute the business object from.
	 * 
	 * @see #getBusinessObjects(Collection)
	 */
	public static Set<Object> getBusinessObjects(Collection<? extends TreeUINode<?>> nodes) {
		Set<Object> nodeBusinessObjects = new HashSet<>();

		for (TreeUINode<?> node : nodes) {
			nodeBusinessObjects.add(getBusinessObject(node));
		}

		return nodeBusinessObjects;
	}

	/**
	 * Returns <code>true</code> if the first set of nodes contains one node which has the same
	 * business object as the second node, otherwise <code>false</code>.
	 * 
	 * @param nodes
	 *        Collection of tree nodes to compute the business object from to compare it with the
	 *        other node business object.
	 * @param node
	 *        Tree node to compute the business object from to compare it with the others node
	 *        business objects.
	 * 
	 * @see #getBusinessObjects(Collection)
	 */
	public static boolean containsNodeWithSameBusinessObject(Collection<? extends TreeUINode<?>> nodes,
			TLTreeNode<?> node) {
		return getBusinessObjects(nodes).contains(getBusinessObject(node));
	}

	/**
	 * Returns <code>true</code> if the given nodes has the same business object, otherwise
	 * <code>false</code>.
	 * 
	 * @param node1
	 *        First tree node to compute the business object from to compare it with the others node
	 *        business object.
	 * @param node2
	 *        Second tree node to compute the business object from to compare it with the other node
	 *        business object.
	 * 
	 * @see #getBusinessObject(TLTreeNode)
	 */
	public static boolean hasSameBusinessObject(TreeUINode<?> node1, TreeUINode<?> node2) {
		return Utils.equals(getBusinessObject(node1), getBusinessObject(node2));
	}

	/**
	 * Returns the nodes business object.
	 * 
	 * <p>
	 * If the node is <code>null</code> then <code>null</code> is returned.
	 * </p>
	 * 
	 * @param node
	 *        Tree node to compute the business object from.
	 */
	public static Object getBusinessObject(TLTreeNode<?> node) {
		if (node != null) {
			return node.getBusinessObject();
		} else {
			return null;
		}
	}

}
