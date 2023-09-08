/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import static com.top_logic.basic.shared.collection.iterator.IteratorUtilShared.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode;
import com.top_logic.util.Utils;

/**
 * Utilities for {@link TLTreeModel} and associated interfaces.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLTreeModelUtil {

	/**
	 * Wrapper used in method {@link TLTreeModelUtil#findBestMatch(TLTreeModel, Object, Object)} to
	 * indicate that a perfect match could not be found.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class ApproximatedMatch {

		/**
		 * best match found in {@link TLTreeModelUtil#findBestMatch(TLTreeModel, Object, Object)}
		 */
		final Object _bestMatch;

		public ApproximatedMatch(Object bestMatch) {
			_bestMatch = bestMatch;
		}

	}

	/**
	 * Utility method to get the common ancestor (or self) node of two nodes in the given tree
	 * model.
	 * 
	 * <p>
	 * That means <code>getCommonRootNode(node X, child_of(node X)) = node X</code>.
	 * </p>
	 * 
	 * @param treeModel
	 *        The {@link TLTreeModel} in which the common ancestor is searched.
	 * @param node1
	 *        The first node.
	 * @param node2
	 *        The second node.
	 * @return The common ancestor node or one of the given nodes, if a node is (transitive) child
	 *         of the other node; this must be at least the root node, if the given nodes are within
	 *         the same tree model. If one of the given nodes is not in the given tree model,
	 *         <code>null</code> is returned.
	 */
	public static <T> T getCommonAncestor(TLTreeModel<T> treeModel, T node1, T node2) {
		if (node1.equals(node2)) {
			return treeModel.containsNode(node1) ? node1 : null;
		}
		List<T> path1 = treeModel.createPathToRoot(node1);
		if (path1 == null) {
			// Node not in the given model.
			return null;
		}
		List<T> path2 = treeModel.createPathToRoot(node2);
		if (path2 == null) {
			// Node not in the given model.
			return null;
		}
		return getLastCommonNode(path1, path2);
	}

	/**
	 * Utility method to get the common ancestor (or self) node of two nodes.
	 * 
	 * <p>
	 * That means <code>getCommonRootNode(node X, child_of(node X)) = node X</code>.
	 * </p>
	 * 
	 * @param node1
	 *        The first node.
	 * @param node2
	 *        The second node.
	 * @return The common ancestor node or one of the given nodes, if a node is (transitive) child
	 *         of the other node; this must be at least the root node, if the given nodes are within
	 *         the same tree model. If the nodes are not within the same tree model,
	 *         <code>null</code> is returned.
	 */
	public static <T extends TLTreeNode<T>> T getCommonAncestor(T node1, T node2) {
		List<T> path1 = createPathToRoot(node1);
		List<T> path2 = createPathToRoot(node2);
		return getLastCommonNode(path1, path2);
	}

	/**
	 * Creates a path from the given node to the node's model root node.
	 * 
	 * @see TLTreeModel#createPathToRoot(Object)
	 */
	public static <T extends TLTreeNode<T>> List<T> createPathToRoot(T node) {
		ArrayList<T> result = new ArrayList<>();
		while (node != null) {
			result.add(node);
			node = node.getParent();
		}
		return result;
	}

	/**
	 * Find a node from a given root node by a path of user objects.
	 * 
	 * @param startNode
	 *        The node from which to start the search.
	 * @param userObjectPath
	 *        List of user objects.
	 * @param bestMatch
	 *        Whether the node on the longest sub-path should be returned, if the exact node cannot
	 *        be found.
	 * @return The node that is reachable by a path of nodes with the given list of user objects, or
	 *         <code>null</code>, if there is no such path (if best match is <code>false</code>). If
	 *         the result is non- <code>null</code> (and best match is <code>false</code>), the
	 *         {@link TLTreeNode#getBusinessObject() user object} of the result is the last object of
	 *         the given list. If the given list is empty, the given start node is returned.
	 */
	public static <T extends TLTreeNode<T>> T findNode(T startNode, List<Object> userObjectPath, boolean bestMatch) {
		T node = startNode;

		path: for (Object userObject : userObjectPath) {
			for (T child : node.getChildren()) {
				if (Utils.equals(userObject, child.getBusinessObject())) {
					node = child;

					// Node was found, continue with next step in path.
					continue path;
				}
			}

			// Not found.
			if (bestMatch) {
				return node;
			} else {
				return null;
			}
		}

		return node;
	}

	/**
	 * Find a node from the root node of the given {@link TLTreeModel} by a path of user objects.
	 * 
	 * @param tree
	 *        The {@link TLTreeModel} to search.
	 * @param userObjectPath
	 *        List of user objects.
	 * @param bestMatch
	 *        Whether the node on the longest sub-path should be returned, if the exact node cannot
	 *        be found.
	 * @return The node that is reachable by a path of nodes with the given list of user objects, or
	 *         <code>null</code>, if there is no such path (if best match is <code>false</code>). If
	 *         the result is non- <code>null</code> (and best match is <code>false</code>), the
	 *         {@link TLTreeNode#getBusinessObject() user object} of the result is the last object
	 *         of the given list. If the given list is empty, the given start node is returned.
	 */
	public static <N> N findNode(TLTreeModel<N> tree, List<Object> userObjectPath, boolean bestMatch) {
		return findNode(tree, tree.getRoot(), userObjectPath, bestMatch);
	}

	/**
	 * Find a node from a given root node by a path of user objects.
	 * 
	 * @param tree
	 *        The {@link TLTreeModel} of the given node.
	 * @param startNode
	 *        The node from which to start the search.
	 * @param userObjectPath
	 *        List of user objects.
	 * @param bestMatch
	 *        Whether the node on the longest sub-path should be returned, if the exact node cannot
	 *        be found.
	 * @return The node that is reachable by a path of nodes with the given list of user objects, or
	 *         <code>null</code>, if there is no such path (if best match is <code>false</code>). If
	 *         the result is non- <code>null</code> (and best match is <code>false</code>), the
	 *         {@link TLTreeNode#getBusinessObject() user object} of the result is the last object
	 *         of the given list. If the given list is empty, the given start node is returned.
	 */
	public static <N> N findNode(TLTreeModel<N> tree, N startNode, List<Object> userObjectPath, boolean bestMatch) {
		N node = startNode;

		path:
		for (Object userObject : userObjectPath) {
			for (N child : tree.getChildren(node)) {
				if (Utils.equals(userObject, tree.getBusinessObject(child))) {
					node = child;

					// Node was found, continue with next step in path.
					continue path;
				}
			}

			// Not found.
			if (bestMatch) {
				return node;
			} else {
				return null;
			}
		}

		return node;
	}

	/**
	 * Creates a path from the given node to the node's model root node in
	 * {@link TLTreeNode#getBusinessObject() user objects}.
	 * 
	 * @param node
	 *        The node to which the path should be created.
	 * @return List of user objects on the path to root. The first object in the resulting list is
	 *         the {@link TLTreeNode#getBusinessObject()} of the given node. The last object is the
	 *         user object of the root node.
	 * 
	 * @see #createPathToRootUserObject(TLTreeNode)
	 * @see TLTreeModel#createPathToRoot(Object)
	 * @see TLTreeNode#getBusinessObject()
	 */
	public static <T> List<T> createPathToRootUserObject(TLTreeNode<?> node, Class<T> type) {
		List<Object> untypedPath = TLTreeModelUtil.createPathToRootUserObject(node);
		return CollectionUtil.dynamicCastView(type, untypedPath);
	}

	/**
	 * Creates a path from the given node to the node's model root node in
	 * {@link TLTreeNode#getBusinessObject() user objects}.
	 * 
	 * @param node
	 *        The node to which the path should be created.
	 * @return List of user objects on the path to root. The first object in the resulting list is
	 *         the {@link TLTreeNode#getBusinessObject()} of the given node. The last object is the
	 *         user object of the root node.
	 * 
	 * @see #createPathToRootUserObject(TLTreeNode, Class)
	 * @see TLTreeModel#createPathToRoot(Object)
	 * @see TLTreeNode#getBusinessObject()
	 */
	public static List<Object> createPathToRootUserObject(TLTreeNode<?> node) {
		ArrayList<Object> result = new ArrayList<>();
		while (node != null) {
			result.add(node.getBusinessObject());
			node = node.getParent();
		}
		return result;
	}

	private static <T> T getLastCommonNode(List<? extends T> path1, List<? extends T> path2) {
		T commonAncestor = null;
		for (int i1 = path1.size() - 1, i2 = path2.size() - 1; i1 >= 0 && i2 >= 0; i1--, i2--) {
			T ancestor1 = path1.get(i1);
			T ancestor2 = path2.get(i2);

			if (ancestor1 != ancestor2) {
				break;
			}
			commonAncestor = ancestor1;
		}

		return commonAncestor;
	}

	/**
	 * Checks, whether the given node is part of the given {@link TLTreeModel}.
	 */
	public static <T> boolean containsNode(TLTreeModel<T> treeModel, T node) {
		Object root = treeModel.getRoot();
		while (true) {
			if (node == root) {
				return true;
			}

			T parent = treeModel.getParent(node);
			if (parent == null) {
				return false;
			}

			node = parent;
		}
	}

	/**
	 * Checks whether the given <code>potentialAncestor</code> is an ancestor of the given
	 * <code>node</code> in the given <code>treeModel</code>, i. e. whether the subtree of the given
	 * {@link TLTreeModel} with the given <code>potentialAncestor</code> as root contains the given
	 * <code>node</code>.
	 * 
	 * @param treeModel
	 *        must not be <code>null</code>.
	 * @param potentialAncestor
	 *        root of the subtree of the given <code>treeModel</code>.
	 * @param node
	 *        the node potentially contained in the subtree.
	 * 
	 * @return <code>true</code> if the given <code>potentialAncestor</code> is an ancestor of the
	 *         given node in the given <code>treeModel</code>.
	 */
	public static <T> boolean isAncestor(TLTreeModel<T> treeModel, T potentialAncestor, T node) {
		if (!treeModel.containsNode(node)) {
			return false;
		}

		T parent = node;
		do {
			if (potentialAncestor.equals(parent)) {
				return true;
			}
			parent = treeModel.getParent(parent);
		} while (parent != null);

		return false;
	}

	/**
	 * Tries to find a node in a {@link TLTreeModel} which is most similar to a formerly removed
	 * node, i.e. it tries to find a node with an equal business object and the parent structure is
	 * the same as the old parent structure.
	 * 
	 * If no such node is found the best match is returned, i.e. the a deepest descendant node of
	 * the given <code>aliveSubtreeRoot</code>, such that the list of business objects corresponding
	 * to the path from the given <code>aliveSubtreeRoot</code> to the best match is an initial
	 * segment of the list of business objects corresponding to the path from the given
	 * <code>aliveSubtreeRoot</code> to the <code>formerNode</code> before removal.
	 * 
	 * @param model
	 *        the model of the tree.
	 * @param formerNode
	 *        a node (either still in the tree or not) which was formerly a descendant of the given
	 *        <code>aliveSubtreeRoot</code>
	 * @param aliveSubtreeRoot
	 *        the parent of the removed subtree in which the given <code>formerNode</code> lived.
	 * 
	 * @return a descendant node of <code>aliveSubtreeRoot</code> in the given <code>model</code>
	 *         whose parent structure is most similar to the given <code>formerNode</code>.
	 */
	public static <T> T findBestMatch(TLTreeModel<T> model, T formerNode, T aliveSubtreeRoot) {
		if (model.containsNode(formerNode)) {
			return formerNode;
		} else {
			Object internalFindBestMatch = internalFindBestMatch(model, formerNode, aliveSubtreeRoot);
			if (internalFindBestMatch instanceof ApproximatedMatch) {
				return (T) ((ApproximatedMatch) internalFindBestMatch)._bestMatch;
			} else {
				return (T) internalFindBestMatch;
			}
		}
	}

	private static <T> Object internalFindBestMatch(TLTreeModel<T> model, T formerNode, T aliveSubtreeRoot) {
		T directParent = model.getParent(formerNode);
		Object userObject = model.getBusinessObject(formerNode);
		T bestMatchForDirectParent;
		if (directParent == null) {
			bestMatchForDirectParent = aliveSubtreeRoot;
		} else {
			Object match = internalFindBestMatch(model, directParent, aliveSubtreeRoot);
			if (match instanceof ApproximatedMatch) {
				return match;
			}
			bestMatchForDirectParent = (T) match;
		}
		Iterator<? extends T> childIterator = model.getChildIterator(bestMatchForDirectParent);
		while (childIterator.hasNext()) {
			T aliveChild = childIterator.next();
			if (Utils.equals(model.getBusinessObject(aliveChild), userObject)) {
				return aliveChild;
			}
		}
		return new ApproximatedMatch(bestMatchForDirectParent);
	}

	/**
	 * Updates the children of the given node, such that the children have the given business
	 * objects and have the same order as the business objects.
	 * 
	 * <p>
	 * This method tries to save as much as existing children as possible.
	 * <ul>
	 * <li>If there is a node which has a business objects which does not occur in the given
	 * sequence of business objects, the child is removed.</li>
	 * <li>If there is a business object for which no node exists, a new child is created at the
	 * correct position.</li>
	 * <li>If for a business object a node exists, but the node is placed at the wrong position it
	 * is moved to that position.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param node
	 *        The node to update the children.
	 * @param businessObjects
	 *        The new business objects of the children.
	 */
	public static <N extends MutableTLTreeNode<N>> void updateChildren(N node, Iterator<?> businessObjects) {
		int treeChildIndex = 0;
		while (true) {
			if (!businessObjects.hasNext()) {
				// The children at end of list were removed.
				for (int index = node.getChildCount() - 1; index >= treeChildIndex; index--) {
					node.removeChild(index);
				}
				break;
			}
			if (treeChildIndex == node.getChildCount()) {
				// New business children at the end of the children are inserted.
				Object newBusinessChild = businessObjects.next();
				if (!businessObjects.hasNext()) {
					node.createChild(newBusinessChild);
				} else {
					ArrayList<Object> newChildren = new ArrayList<>();
					newChildren.add(newBusinessChild);
					do {
						newChildren.add(businessObjects.next());
					} while (businessObjects.hasNext());
					node.createChildren(newChildren);
				}
				break;
			}

			Object businessChild = businessObjects.next();
			N treeChild = node.getChildAt(treeChildIndex);
			if (Utils.equals(businessChild, treeChild.getBusinessObject())) {
				// Lists agree up to current index. Check next.
			} else {
				// Business child is new or has got new position.
				int oldIndexOfChild = treeChildIndex + 1;
				while (oldIndexOfChild < node.getChildCount()) {
					if (Utils.equals(businessChild, node.getChildAt(oldIndexOfChild).getBusinessObject())) {
						break;
					}
					oldIndexOfChild++;
				}

				N child;
				if (oldIndexOfChild == node.getChildCount()) {
					// No corresponding node found, business object is new.
					child = node.createChild(businessChild);
				} else {
					// Node for business object has moved.
					child = node.getChildAt(oldIndexOfChild);
				}

				if (child != null) {
					child.moveTo(node, treeChildIndex);
				}
			}
			treeChildIndex++;
		}
	}

	/**
	 * Writes a {@link String} representation of the given {@link TreeView} to the given
	 * {@link Appendable}.
	 * <p>
	 * The nodes are indented with tabs.
	 * </p>
	 * 
	 * @param <T>
	 *        The type of the tree nodes.
	 * @param out
	 *        The {@link Appendable} to write into.
	 * @param tree
	 *        Is not allowed to be null.
	 * @param root
	 *        Is allowed to be null, if null is a node in the tree.
	 */
	public static <T> void toString(Appendable out, TreeView<T> tree, T root) {
		try {
			toString(out, tree, root, 0);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private static <T> void toString(Appendable out, TreeView<T> tree, T node, int indentationSize)
			throws IOException {
		out.append(StringServices.repeadString("\t", indentationSize));
		out.append(MetaLabelProvider.INSTANCE.getLabel(node));
		out.append("\n");
		int childIndentation = indentationSize + 1;
		for (T child : getChildren(tree, node)) {
			toString(out, tree, child, childIndentation);
		}
	}

	/**
	 * @param <T>
	 *        The type of the tree nodes.
	 * @param tree
	 *        Is not allowed to be null.
	 * @param node
	 *        Is allowed to be null, if null is a node in the tree.
	 * @return The {@link List} of the children of the given {@link TreeView}.
	 */
	public static <R, T extends R> List<R> getChildren(TreeView<T> tree, T node) {
		return IteratorUtil.toList(tree.getChildIterator(node));
	}

	/**
	 * All nodes in the given {@link TLTreeModel}, recursively.
	 * <p>
	 * The root node itself is the first one in the result.
	 * </p>
	 */
	public static <N> List<N> getChildrenRecursively(TLTreeModel<N> tree) {
		return getChildrenRecursively(tree, tree.getRoot());
	}

	/**
	 * All children of the given node, recursively.
	 * <p>
	 * The given node itself is the first one in the result.
	 * </p>
	 */
	public static <N> List<N> getChildrenRecursively(TreeView<N> tree, N node) {
		return toList(new DescendantDFSIterator<>(tree, node, true));
	}

	/**
	 * Expands all parents of the given {@link TreeUINode}.
	 */
	public static void expandParents(TreeUINode<?> node) {
		TreeUINode<?> currentNode = node.getParent();

		while (currentNode != null) {
			currentNode.setExpanded(true);

			currentNode = currentNode.getParent();
		}
	}

	/**
	 * Returns the innermost business object of the given objects by unwrapping {@link TLTreeNode}s.
	 * 
	 * @see #getInnerBusinessObject(Object)
	 */
	public static Collection<Object> getInnerBusinessObjects(Collection<Object> objects) {
		Collection<Object> businessObjects = new ArrayList<>();

		for (Object object : objects) {
			businessObjects.add(getInnerBusinessObject(object));
		}

		return businessObjects;
	}

	/**
	 * Returns the innermost business object of the given object by unwrapping {@link TLTreeNode}s.
	 */
	public static Object getInnerBusinessObject(Object object) {
		Object currentObject = object;

		while (currentObject instanceof TLTreeNode<?>) {
			currentObject = ((TLTreeNode<?>) currentObject).getBusinessObject();
		}

		return currentObject;
	}

}
