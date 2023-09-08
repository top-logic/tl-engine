/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.component.model.NoSelectionModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.TreeDataEvent;
import com.top_logic.layout.tree.TreeDataListener;
import com.top_logic.layout.tree.TreeDataOwner;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.dnd.DefaultTreeDrag;
import com.top_logic.layout.tree.dnd.NoTreeDrop;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TreeModelBase;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelAdapter;
import com.top_logic.layout.tree.renderer.DefaultTreeRenderer;
import com.top_logic.mig.html.SelectionModel;

/**
 * Tree-structured {@link FormContainer} that associates input elements with
 * tree model nodes.
 * 
 * <p>
 * Implements {@link TreeUIModel} providing {@link FormGroup}s as
 * {@link TreeUIModel#getUserObject(Object) user objects} for tree nodes. All
 * other aspects of a {@link TreeUIModel} are delegated to an underlying model.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormTree extends AbstractFormContainer implements TreeData, TreeModelListener {
	
	// TreeData state.
	
	@Inspectable
	private FormTreeModel treeModel;
	
	@Inspectable
    private SelectionModel selectionModel;
	
	@Inspectable
	private TreeRenderer renderer = DefaultTreeRenderer.INSTANCE;
	
	
	// FormContainer state.

	/** Called to populate the {@link FormGroup}s with the desired Fields */
	private final NodeGroupInitializer nodeGroupProvider;
	
	/** Bidirectional mapping between tree-nodes and inner {@link FormGroup}s */
    /* <Object treeNode, FormGroup group> */
	private final BidiMap groupByNode = new BidiHashMap();

	private final Map<String, FormGroup> membersByName = new HashMap<>();

	private final Collection<FormGroup> membersView = Collections.unmodifiableCollection(membersByName.values());
	
	/** Identifiers of inner {@link FormGroup}s a derived from this counter. */
	private int nextNodeGroupId = 1;

	/**
	 * Nodes that are potentially removed in a structure change.
	 */
	private ArrayList<Object> pendingChanges;

	private TreeDragSource _dragSource = DefaultTreeDrag.INSTANCE;

	private TreeDropTarget _dropTarget = NoTreeDrop.INSTANCE;

	public FormTree(String name, ResourceView resources, TreeUIModel treeModel, NodeGroupInitializer nodeGroupProvider) {
		super(name, resources);
		
		this.treeModel = new FormTreeModel(treeModel);
		this.treeModel.addTreeModelListener(this);
		
		this.selectionModel = NoSelectionModel.INSTANCE;
		
		this.nodeGroupProvider = nodeGroupProvider;
	}

	@Override
	public TreeUIModel getTreeModel() {
		return this.treeModel;
	}
	
	public TreeUIModel getTreeApplicationModel() {
		return this.treeModel.getImpl();
	}
	
	public void setTreeModel(TreeUIModel newBaseModel) {
		FormTreeModel oldModel = this.treeModel;
		TreeUIModel oldBaseModel = oldModel.getImpl();
		
		if (newBaseModel == oldBaseModel) {
			return;
		}
		
		FormTreeModel newModel = new FormTreeModel(newBaseModel);
		
		oldModel.removeTreeModelListener(this);
		this.treeModel = newModel;
		newModel.addTreeModelListener(this);
		
		fireTreeModelChange(newModel, oldModel);
		
		// Clear cached node groups.
		internalClearNodeGroups();
	}

    @Override
	public SelectionModel getSelectionModel() {
        return this.selectionModel;
    }
    
    public void setSelectionModel(SelectionModel newSelectionModel) {
    	SelectionModel oldSelectionModel = this.selectionModel;
    	
		if (newSelectionModel == oldSelectionModel) {
    		return;
    	}

		this.selectionModel = newSelectionModel;
		fireSelectionChange(newSelectionModel, oldSelectionModel);
	}
	
	@Override
	public TreeRenderer getTreeRenderer() {
		return renderer;
	}
	
	public void setTreeRenderer(TreeRenderer newRenderer) {
		TreeRenderer oldRenderer = this.renderer;
		
		this.renderer = newRenderer;
		
		fireRendererChange(newRenderer, oldRenderer);
	}
	
	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		// Keep node groups in sync with tree model changes. 
		switch (evt.getType()) {
			case TreeModelEvent.BEFORE_NODE_REMOVE: {
				Object node = evt.getNode();
				FormGroup nodeGroup = findNodeGroup(node);
				
				// Node group might not have been created yet.
				if (nodeGroup != null) {
					internalRemoveNodeGroup(node, nodeGroup);
				}
				
				break;
			}
			case TreeModelEvent.BEFORE_STRUCTURE_CHANGE: {
				pendingChanges = getCachedNodes(evt.getModel(), evt.getNode());
				break;
			}
			case TreeModelEvent.AFTER_STRUCTURE_CHANGE: {
                // removing all removed nodes - removed nodes are nodes without parent 
				Object root = getTreeModel().getRoot();
			    for (Object node : pendingChanges) {
			    	if (TLTreeModelUtil.containsNode(treeModel, node)) {
						/*
						 * Node is still in the tree, build parent groups to root.
						 * This ensures that the invariant still holds, that all
						 * nodes with groups are reachable through a path, where
						 * every element has a group. This invariant is required for
						 * finding pending changes in the before structure change
						 * event.
						 */
			    		Object ancestor = node;
			    		while (true) {
			    			if (ancestor == root) {
			    				break;
			    			}

			    			ancestor = treeModel.getParent(ancestor);
			    			assert ancestor != null;
			    			
			    			if (groupByNode.get(ancestor) != null) {
			    				break;
			    			}
			    			
			    			createNodeGroup(ancestor);
			    		}
			    	} else {
			            FormGroup nodeGroup = (FormGroup) groupByNode.get(node);
			            internalRemoveNodeGroupLocally(node, nodeGroup);
			    	}
			    }
			    pendingChanges = null;

			    break;
			}
		}
	}
	
	private ArrayList<Object> getCachedNodes(TreeModelBase model, Object node) {
		ArrayList<Object> result = new ArrayList<>();
		enterCachedNodes(result, model, node);
		return result;
	}

	private void enterCachedNodes(ArrayList<Object> result, TreeModelBase model, Object node) {
		if (! groupByNode.containsKey(node)) {
			return;
		}
		
		result.add(node);
		if (!model.childrenInitialized(node)) {
			// Children not initialized, therefore no cached nodes.
			return;
		}
		for (Object child : model.getChildren(node)) {
			enterCachedNodes(result, model, child);
		}
	}

	@Override
	public boolean addTreeDataListener(TreeDataListener listener) {
	    return addListener(TreeDataListener.class, listener);
	}

	@Override
	public boolean removeTreeDataListener(TreeDataListener listener) {
	    return removeListener(TreeDataListener.class, listener);
	}
		
	protected final void fireTreeModelChange(TreeUIModel newModel, TreeUIModel oldModel) {
		List<TreeDataListener> listeners = getListeners(TreeDataListener.class);
		int cnt = listeners.size();
		if (cnt > 0) {
			TreeDataEvent evt = new TreeDataEvent.TreeUIModelChange(this, oldModel, newModel);
			for (int n = 0; n < cnt; n++) {
				listeners.get(n).handleTreeDataChange(evt);
			}
		}
	}

	protected final void fireSelectionChange(SelectionModel newSelection, SelectionModel oldSelection) {
		List<TreeDataListener> listeners = getListeners(TreeDataListener.class);
		int cnt = listeners.size();
		if (cnt > 0) {
			TreeDataEvent evt = new TreeDataEvent.SelectionModelChange(this, oldSelection, newSelection);
			for (int n = 0; n < cnt; n++) {
				listeners.get(n).handleTreeDataChange(evt);
			}
		}
	}

	protected final void fireRendererChange(TreeRenderer newRenderer, TreeRenderer oldRenderer) {
		List<TreeDataListener> listeners = getListeners(TreeDataListener.class);
		int cnt = listeners.size();
		if (cnt > 0) {
			TreeDataEvent evt = new TreeDataEvent.RendererChange(this, oldRenderer, newRenderer);
			for (int n = 0; n < cnt; n++) {
				listeners.get(n).handleTreeDataChange(evt);
			}
		}
	}
	
	
	// FormTree API.
	
	public NodeGroupInitializer getNodeGroupProvider() {
		return nodeGroupProvider;
	}
	
	public Object getNode(FormMember nodeGroup) {
		return groupByNode.getKey(nodeGroup);
	}

	public final FormGroup findNodeGroup(Object node) {
		return (FormGroup) groupByNode.get(node);
	}

	/**
	 * Create cached {@link FormGroup} per node. 
	 */
	/*package protected*/ final FormContainer createNodeGroup(Object node) {
		FormGroup nodeGroup = findNodeGroup(node);
		
		if (nodeGroup == null) {
			if (installParentGroups(node)) {
				nodeGroup = locallyInstallNodeGroup(node);
			}
		}
		
		return nodeGroup;
	}

	/**
	 * Install node groups for all parent nodes of the given node, if the given
	 * node is in the tree.
	 * 
	 * @param node
	 *        The node to start the search.
	 * @return Whether the given node is in the tree.
	 */
	private boolean installParentGroups(Object node) {
		TreeUIModel treeModel2 = getTreeModel();

		Object root = treeModel2.getRoot();
		if (node == root) {
			return true;
		}

		Object parent = treeModel2.getParent(node);
		if (parent == null) {
			// A node without a parent is in the tree, if it is the root node.
			return false;
		}

		if (parent == root && !treeModel2.isRootVisible()) {
			return true;
		}

		{
			boolean parentHasGoup = findNodeGroup(parent) != null;
			if (parentHasGoup) {
				return true;
			}
			
			boolean nodeInTree = installParentGroups(parent);
			if (nodeInTree) {
				locallyInstallNodeGroup(parent);
			}
			return nodeInTree;
		}
	}

	/**
	 * Unconditionally create and install a new node group for the given node.
	 * 
	 * @param node
	 *        The node to install a new node group for.
	 * @return The new node group.
	 */
	private FormGroup locallyInstallNodeGroup(Object node) {
		FormGroup nodeGroup = new FormGroup(Integer.toString(nextNodeGroupId++), getResources());
		nodeGroupProvider.createNodeGroup(this, nodeGroup, node);

		internalAddNodeGroup(node, nodeGroup);
		return nodeGroup;
	}

	/**
	 * Register the given node group for the given node.
	 * 
	 * @param node
	 *        The node to register the given node group for.
	 * @param nodeGroup
	 *        The node group to register.
	 */
	private void internalAddNodeGroup(Object node, FormGroup nodeGroup) {
		membersByName.put(nodeGroup.getName(), nodeGroup);
		if (nodeGroup.isChanged()) {
			increaseChangedMembers();
		}
		groupByNode.put(node, nodeGroup);
		nodeGroup.setParent(this);
	}

	/**
	 * Recursively remove all nodes and their FormGroups.
	 */
	private void internalRemoveNodeGroup(Object node, FormGroup nodeGroup) {
		internalRemoveNodeGroupLocally(node, nodeGroup);
		
		TreeUIModel treeModel = getTreeModel();
		if (!treeModel.childrenInitialized(node)) {
			// / Children not initialized, therefore no cached node groups.
			return;
		}
		List<?> children = treeModel.getChildren(node);
		for (int n=children.size(), i=0; i < n; i++ ) {
		    Object    childNode  = children.get(i);
		    FormGroup childGroup = findNodeGroup(childNode);
		    if (childGroup != null) {
		        internalRemoveNodeGroup(childNode, childGroup);
		    }
		}
	}
	
	private void internalRemoveNodeGroupLocally(Object node, FormGroup nodeGroup) {
		nodeGroup.setParent(null);
		FormMember removedMember = membersByName.remove(nodeGroup.getName());
		if (removedMember != null && removedMember.isChanged()) {
			decreaseChangedMembers();
		}
		groupByNode.remove(node);
	}

	private void internalClearNodeGroups() {
		for (Iterator<FormGroup> it = membersByName.values().iterator(); it.hasNext(); ) {
			AbstractFormMember nodeGroup = it.next();
			nodeGroup.setParent(null);
		}
		membersByName.clear();
		while (changedMembersCount() > 0) {
			decreaseChangedMembers();
		}
		groupByNode.clear();
	}
	
	
	// TreeUIModel implementation.
	
	private final class FormTreeModel extends TreeUIModelAdapter {
		
		public FormTreeModel(TreeUIModel impl) {
			super(impl);
		}

		@Override
		public final Object getUserObject(Object node) {
			return createNodeGroup(node);
		}
	}
	
	
	// FormContainer implementation.
	
	@Override
	public int size() {
		return membersByName.size();
	}
	
	@Override
	protected FormMember internalGetMember(String name) {
		return membersByName.get(name);
	}

	@Override
	public Iterator<? extends FormMember> getMembers() {
		return membersView.iterator();
	}

	@Override
	protected void internalAddMember(FormMember member) {
		throw new UnsupportedOperationException("Form members cannot be explicitly added to a form tree.");
	}

	@Override
	protected boolean internalRemoveMember(FormMember member) {
		throw new UnsupportedOperationException("Form members cannot be explicitly removed from a form tree.");
	}

	/** Support FormMemberVisitor by calling {@link FormMemberVisitor#visitFormTree(FormTree, Object)} */
	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitFormTree(this, arg);
	}

	@Override
	public Maybe<TreeDataOwner> getOwner() {
		/* Not supported, as the FormTree does not need an owner. The owner is only needed to build
		 * a stable reference for the scripting framework. But the FormTree can already be
		 * referenced as a FormMember. */
		return Maybe.none();
	}

	@Override
	public TreeDragSource getDragSource() {
		return _dragSource;
	}

	public void setDragSource(TreeDragSource dragSource) {
		_dragSource = dragSource;
	}

	@Override
	public TreeDropTarget getDropTarget() {
		return _dropTarget;
	}

	public void setDropTarget(TreeDropTarget dropTarget) {
		_dropTarget = dropTarget;
	}

	@Override
	public boolean focus() {
		// Ignore, cannot have focus.
		return false;
	}

}
