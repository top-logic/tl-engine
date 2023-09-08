/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * Computes a set of non-overlapping updates from a number of
 * {@link TLTreeModel} changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeUpdateAccumulator<M extends TLTreeModel> {
	/**
	 * The accumulated updates indexed by node.
	 */
	private final HashMap<Object, NodeUpdate> updateByNode = new HashMap<>();

	private final HashSet<Object> newNodes = new HashSet<>();

	public TreeUpdateAccumulator() {
		// Default constructor.
	}
	
	/**
	 * Whether any updates are recorded.
	 */
	public boolean hasUpdates() {
		return ! updateByNode.isEmpty();
	}

	/**
	 * All nodes that have updates assigned.
	 */
	protected final Set<Object> getNodesWithUpdates() {
		return updateByNode.keySet();
	}

	/**
	 * All node updates recorded.
	 */
	public Collection<NodeUpdate> getUpdates() {
		return updateByNode.values();
	}

	/**
	 * The update for the given node, or <code>null</code> if the given node is not updated, or a
	 * parent node has structurally changed.
	 */
	protected final NodeUpdate getUpdateForNode(Object node) {
		return updateByNode.get(node);
	}

	/**
	 * Records the given update.
	 */
	protected final void addUpdate(NodeUpdate update) {
		updateByNode.put(update.getNode(), update);
	}
	
	/**
	 * Drops the update for the given node.
	 * 
	 * @param node
	 *        The node to drop the update for.
	 * @return The update that was previously recorded for the given node.
	 */
	protected final NodeUpdate removeUpdateForNode(Object node) {
		return updateByNode.remove(node);
	}

	/**
	 * Drops all node updates.
	 */
	protected final void clearNodeUpdates() {
		updateByNode.clear();
	}

	public void clear() {
		clearNodeUpdates();
		newNodes.clear();
	}
	
	/**
	 * Visitor interface for {@link TreeUIUpdateAccumulator.NodeUpdate}s.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static interface UpdateVisitor<R, A1, A2> {

		/**
		 * Visit single node updates.
		 */
		R visitNodeUpdate(NodeUpdate nodeUpdate, A1 arg1, A2 arg2);

		/**
		 * Visit updates of whole subtrees.
		 */
		R visitSubtreeUpdate(SubtreeUpdate subtreeUpdate, A1 arg1, A2 arg2);
		
	}
	
	/**
	 * A local node update, that does not affect any children of the node.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class NodeUpdate {
		private Object node;
		private String nodeId;
		
		public NodeUpdate(Object node, String nodeId) {
			super();
			this.node = node;
			this.nodeId = nodeId;
		}
		
		/**
		 * The updated node. 
		 */
		public Object getNode() {
			return node;
		}

		/**
		 * The ID of the updated node. 
		 */
		public String getNodeId() {
			return nodeId;
		}
		
		public <R, A1, A2> R visit(UpdateVisitor<R, A1, A2> visitor, A1 arg1, A2 arg2) {
			return visitor.visitNodeUpdate(this, arg1, arg2);
		}
	}
	
	/**
	 * An update of a whole subtree.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SubtreeUpdate extends TreeUIUpdateAccumulator.NodeUpdate {
		private String rightmostVisibleDescendantId;
		
		public SubtreeUpdate(Object subroot, String subrootId, String rightmostVisibleDescendantId) {
			super(subroot, subrootId);
			
//			assert rightmostVisibleDescendantId != null : "Missing rightmost visible descendant ID.";
			this.rightmostVisibleDescendantId = rightmostVisibleDescendantId;
		}
		
		/**
		 * The ID of the rightmost visible node of the root of this update that
		 * is currently displayed.
		 */
		public String getRightmostVisibleDescendantId() {
			return rightmostVisibleDescendantId;
		}

		@Override
		public <R, A1, A2> R visit(UpdateVisitor<R, A1, A2> visitor, A1 arg1, A2 arg2) {
			return visitor.visitSubtreeUpdate(this, arg1, arg2);
		}
	}

	/**
	 * Mark the given node as locally invalid.
	 */
	public void invalidateNode(M model, Object node) {
		if (! isRelevantUpdate(model, node)) return;

		addUpdate(new NodeUpdate(node, getNodeId(node)));
	}

	/**
	 * Marks the given node as new.
	 */
	public void notifyAdd(Object newNode) {
	    newNodes.add(newNode);
	}

	/**
	 * Whether the given node is not yet displayed.
	 */
	protected boolean isNewNode(Object node) {
		return newNodes.contains(node);
	}

	/**
     * Mark the subtree rooted at the given node as invalid.
     */
	public void invalidateSubtree(M model, Object subroot) {
		if (! isRelevantUpdate(model, subroot)) return;

		String subrootId = getNodeId(subroot);
		
		// Compute the ID of the rightmost displayed descendant of
		// the changed node at the time of the last reset.
		String rightmostVisibleDescendantId = 
		    computeRightmostVisibleDescenantId(model, subroot);
		
		// Remove all updates that are descendants of the currently changed node.
		for (Iterator<Object> it = getNodesWithUpdates().iterator(); it.hasNext(); ) {
			Object updatedNode = it.next();
			
			Object updateAncestor = updatedNode;
			while (true) {
				updateAncestor = model.getParent(updateAncestor);
				if (updateAncestor == null) {
					break;
				}

				if (subroot.equals(updateAncestor)) {
					it.remove();
					break;
				}
			}
		}
		
		addUpdate(new SubtreeUpdate(subroot, subrootId, rightmostVisibleDescendantId));
	}

	protected String computeRightmostVisibleDescenantId(M model, Object subroot) {
		return null;
    }

	protected String getNodeId(Object node) {
		return null;
	}

	protected boolean isRelevantUpdate(M model, Object subroot) {
		return true;
	}

}
