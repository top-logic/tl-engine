/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Store changes made to a tree used in the {@link StructureEditComponent}.
 * 
 * Implementing classes will only need to define the real storing of a value from
 * a {@link FormField} to the representing {@link Wrapper}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractApplyTreeEditCommandHandler extends AbstractApplyCommandHandler {

    /**
	 * Creates a {@link AbstractApplyTreeEditCommandHandler}.
	 */
    public AbstractApplyTreeEditCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /** 
     * Store the values defined in the given (changed) field to the given wrapper.
     * 
     * <p>This method will only be called, when the {@link FormField} has really changed
     * its value.</p>
     * 
     * @param    aWrapper    The wrapper to set the value, must not be <code>null</code>.
     * @param    aField      The changed field, must not be <code>null</code>..
     * @return   <code>true</code> if calling this method changed the given wrapper.
     * @see      #storeNode(TreeUIModel, DefaultMutableTLTreeNode)
     */
    protected abstract boolean storeValue(Wrapper aWrapper, FormField aField);

    @Override
	protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
        if (!formContext.isChanged()) {
            return false;
        }
        else {
            FormTree          theTree = (FormTree) formContext.getMember(StructureEditComponentConstants.FIELD_NAME_TREE);
            DefaultMutableTLTreeNode theRoot = (DefaultMutableTLTreeNode) theTree.getTreeApplicationModel().getRoot();

            this.storeChanges(theTree.getTreeModel(), theRoot);

            return true;
        }
    }

    /** 
     * Store the changes made to the given node and all of its children.
     * 
     * @param    aTree    The tree holding the {@link FormGroup}s for the given nodes, must not be <code>null</code>.
     * @param    aNode    The node to be stored (with his sub structure), must not be <code>null</code>.
     * @return   <code>true</code> when at least one value has been changed.
     * @see      #storeNode(TreeUIModel, DefaultMutableTLTreeNode)
     */
    public boolean storeChanges(TreeUIModel aTree, DefaultMutableTLTreeNode aNode) {
        boolean theResult = this.storeNode(aTree, aNode);

        if (aNode.isInitialized()) {
            for (DefaultMutableTLTreeNode theNode : aNode.getChildren()) {
                theResult |= this.storeChanges(aTree, theNode);
            }
        }

        return theResult;
    }

    /** 
     * Store the changes made to the single node.
     * 
     * @param    aTree    The tree holding the {@link FormGroup}s for the given nodes, must not be <code>null</code>.
     * @param    aNode    The node to be stored, must not be <code>null</code>.
     * @return   <code>true</code> when at least one value has been changed.
     * @see      #storeChanges(TreeUIModel, DefaultMutableTLTreeNode)
     * @see      #storeValue(Wrapper, FormField)
     */
    protected boolean storeNode(TreeUIModel aTree, DefaultMutableTLTreeNode aNode) {
        FormGroup theGroup  = (FormGroup) aTree.getUserObject(aNode);
        boolean   theResult = false;

        if (theGroup.isChanged()) {
            Wrapper theElement = (Wrapper) aNode.getBusinessObject();

            for (Object theObject : theGroup.getChangedMembers()) {
                if (theObject instanceof FormField) { 
                    theResult |= this.storeValue(theElement, (FormField) theObject);
                }
            }
        }

        return theResult;
    }
}

