/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayoutCommandGroupDistributor;
import com.top_logic.tool.boundsec.gui.profile.EditRolesProfileComponent.ColumnProvider;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class RolesProfileColumnProvider extends ColumnProvider {
	
	/** Singleton {@link RolesProfileColumnProvider} instance. */
	@SuppressWarnings("hiding")
	public static final RolesProfileColumnProvider INSTANCE = new RolesProfileColumnProvider();

	/**
	 * Creates a new {@link RolesProfileColumnProvider}.
	 * 
	 */
	protected RolesProfileColumnProvider() {
		// singleton instance
	}

    @Override
	public boolean applyRolesProfile(BoundedRole aModel, FormTree aField) {
        final TreeUIModel theTreeModel = aField.getTreeModel();

        Object theRoot = theTreeModel.getRoot();

        this.applyProfile(aField, theRoot, aModel);

//        if (theRoot instanceof LayoutComponent) {
//            ((LayoutComponent) theRoot)
//                    .acceptVisitorRecursively(new CompoundSecurityLayoutCommandGroupDistributor());
//        }

        return super.applyRolesProfile(aModel, aField);
    }

    @Override
	public Collection getCommandGroups(LayoutComponent aNode) {
        return aNode instanceof CompoundSecurityLayout ? ((CompoundSecurityLayout) aNode).getCommandGroups() : Collections.EMPTY_SET;
    }

    protected void applyProfile(FormTree aTree, Object aNode, BoundedRole aModel) {
        TreeUIModel theModel = aTree.getTreeModel();

        if (!theModel.isLeaf(aNode)) {
            for (Iterator theIt = theModel.getChildIterator(aNode); theIt.hasNext();) {
                CompoundSecurityLayout theLayout = (CompoundSecurityLayout) theIt.next();
                FormGroup theFC = aTree.findNodeGroup(theLayout);
                if (theFC != null) {
                    for (Iterator theFieldIt = theFC.getFields(); theFieldIt.hasNext(); ) {
                        BooleanField theField = (BooleanField) theFieldIt.next();
                        String theCommandGroupName = theField.getName();
                        if (theField.getAsBoolean()) {
                            theLayout.addAccess(CommandGroupRegistry.resolve(theCommandGroupName), aModel);
                        } else {
                            theLayout.removeAccess(CommandGroupRegistry.resolve(theCommandGroupName), aModel);
                        }
                    }
                }
                
                theLayout.acceptVisitorRecursively(new CompoundSecurityLayoutCommandGroupDistributor());

                this.applyProfile(aTree, theLayout, aModel);
            }
        }
    }
}

