/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.tree.model.AbstractTreeModel;
import com.top_logic.reporting.report.model.ReportTree.ReportNode;
import com.top_logic.reporting.report.model.aggregation.AggregationFunction;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.Partition;
import com.top_logic.reporting.report.model.partition.criteria.Criteria;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;

/**
 * A ReportTree represents the result of a {@link RevisedReport} after its initialization. Its nodes
 * are of type {@link ReportNode}. The root node holds all BusinessObjects. An inner node represents
 * a {@link Partition}, each leaf node holds a {@link ReportNode#value} that represents the result
 * of an {@link AggregationFunction} on the BusinessObjects of its parent node.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
@Deprecated
public class ReportTree extends AbstractTreeModel<ReportNode> {

	private ReportNode root;

	public ReportTree(ReportNode aNode) {
		this.root = aNode;
	}
	
	@Override
	public ReportNode getParent(ReportNode aNode) {
		return aNode.getParent();
	}

	@Override
	public List<ReportNode> getChildren(ReportNode aParent) {
		return aParent.getChildren();
	}

	@Override
	public boolean childrenInitialized(ReportNode parent) {
		return parent.isInitialized();
	}

	@Override
	public void resetChildren(ReportNode parent) {
		// Ignore, not lazily initialized.
	}

	@Override
	public ReportNode getRoot() {
		return this.root;
	}

	@Override
	public boolean isLeaf(ReportNode aNode) {
		return aNode.isLeaf();
	}
	
	/**
	 * @param node
	 *        must be a non <code>null</code> {@link ReportNode}.
	 *        
	 * @return {@link ReportNode#getObjects()}
	 * 
	 * @see com.top_logic.layout.tree.model.TLTreeModel#getBusinessObject(java.lang.Object)
	 */
	@Override
	public Object getBusinessObject(ReportNode node) {
		return node.getObjects();
	}

	@Override
	public boolean hasChild(ReportNode parent, Object node) {
		if (!(node instanceof ReportNode)) {
			return false;
		}
		ReportNode parentOfNode = ((ReportNode) node).getParent();
		return parent == parentOfNode;
	}

	@Override
	public boolean isFinite() {
		return false;
	}

	public void printTree() {
		printNode(this.root, this.root.getChildren(), "");
	}
	
    /** 
	 * Debugging only.
	 */
	private void printNode(ReportNode aNode, List someChildren, String indent) {
		System.out.println(indent + aNode.toString());
		for (int i = 0; i < someChildren.size(); i++ ) {
			ReportNode theChild = (ReportNode) someChildren.get(i);
			printNode(theChild, theChild.getChildren(), indent + "\t");
		}
	}

	/**
	 * A {@link ReportNode} represents either the result of a {@link PartitionFunction} or of an
	 * {@link AggregationFunction}. Inner Nodes always result from {@link PartitionFunction}s
	 * whereas leaf nodes result from {@link AggregationFunction}s.
	 */
	public static class ReportNode { 
		private AggregationFunctionConfiguration config;
        private ReportNode                       parent;
        private List<ReportNode>                 children;
        private List<?>                          objects;
        private String                           name;
        private Number                           value;
        private Criteria                         criteria;
        
        public ReportNode(String aName, List<?> someObjects) {
        	this(aName, someObjects, null, null, null);
        }
        
        public ReportNode(String aName, List<?> someObjects, Number aValue, Criteria aCriteria, AggregationFunctionConfiguration aConfig) {
        	this.name     = aName;
        	this.objects  = someObjects;
        	this.value    = aValue;
        	this.criteria = aCriteria;
        	this.config   = aConfig;
        }
        
        public boolean isLeaf() {
        	return this.children == null;
        }

		/**
		 * Adds the given node as a child. Beware that inner nodes do not have a {@link #value} or a
		 * {@link #config}. Therefore they will be set to <code>null</code>
		 * 
		 * @param aNode a {@link ReportNode} to add as a child.
		 */
        public void addChild(ReportNode aNode) {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            this.children.add(aNode);
            
            //Inner nodes do not have values or configurations.
            this.value  = null;
            this.config = null;
            
            aNode.setParent(this);
        }
        
        public boolean hasChildren() {
        	return !CollectionUtil.isEmptyOrNull(this.children);
        }
        
        /** 
         * A {@link List} of this nodes children, never <code>null</code>.
         */
        public List<ReportNode> getChildren() {
            if (this.children == null) {
                return Collections.emptyList();
            }
            return (this.children);
        }
        
		boolean isInitialized() {
			// Children are not created lazy.
			return true;
		}

		/**
		 * The {@link AggregationFunctionConfiguration} that was used to calculate this node
		 *         if {@link #isLeaf()} returns <code>true</code>, <code>null</code> otherwise.
		 */
        public AggregationFunctionConfiguration getConfig() {
        	return this.config;
        }
        
        private void setParent(ReportNode aParent) {
        	this.parent = aParent;
        }
        
        /** 
         * The parent of this node or <code>null</code> if this node is the root node.
         */
        public ReportNode getParent() {
        	return (this.parent);
        }

		/**
		 * Returns the business objects.
		 */
		public List<?> getObjects() {
			return objects;
		}

		/**
		 * Returns the name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the value.
		 */
		public Number getValue() {
			return value;
		}

		/**
		 * Returns the criteria.
		 */
		public Criteria getCriteria() {
			return criteria;
		}

		@Override
		public String toString() {
			String theString = "ReportNode id: " + this.name + "; value: " + this.value + "; objects: " + this.objects.size();
			return this.criteria != null ? theString +  "; criteria: " + this.criteria.getCriteriaTyp() : theString;
		}
    }
}
