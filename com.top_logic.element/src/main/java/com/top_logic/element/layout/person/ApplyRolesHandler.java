/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * {@link AbstractApplyCommandHandler} storing roles.
 * 
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class ApplyRolesHandler extends AbstractApplyCommandHandler {

    public static final String COMMAND_ID = "applyPersonOrGroupRoles";

    public ApplyRolesHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	protected boolean storeChanges(LayoutComponent aComponent, FormContext aContext, Object aModel) {
		EditPersonOrGroupRolesMultiComponent editComponent = (EditPersonOrGroupRolesMultiComponent) aComponent;
    	FormTree theTree = (FormTree) aContext.getMember(EditPersonOrGroupRolesMultiComponent.TREE_FIELD);

        if (theTree == null) {
            return false; // Just to be sure ...
        }
        
		TLModule securityModule = editComponent.getSecurityModule();
		for (TLModuleSingleton link : securityModule.getSingletons()) {
			saveTree(editComponent, theTree, (BoundObject) link.getSingleton());
		}
        
        // force rebuild of form context to reflect changes due to inherit flags
		((FormComponent) aComponent).removeFormContext();
        aComponent.invalidate();

        return true;
    }
    
    private void saveTree(EditPersonOrGroupRolesMultiComponent aComp, FormTree aTree, BoundObject anElement) {
    	
    	FormGroup   theFG       = aTree.findNodeGroup(anElement);
    	Collection  theChildren = anElement.getSecurityChildren();
    	boolean     inherit     = false;

    	if (theFG != null) {
    		SelectField  theSelection = (SelectField)  theFG.getMember(EditPersonOrGroupRolesMultiComponent.COL_SELECTION);
    		BooleanField theChkBox    = (BooleanField) theFG.getMember(EditPersonOrGroupRolesMultiComponent.COL_CHECKBOX);
    		List         theRoles     = theSelection.getSelection();

			inherit = theChkBox.getAsBoolean();
			saveRoles(aComp, anElement, theRoles, inherit);
    	}
    	
    	if (!inherit) {
    		for (Iterator theIt = theChildren.iterator(); theIt.hasNext();) {
    			BoundObject theChild = (BoundObject) theIt.next();
    			saveTree(aComp, aTree, theChild);
    		}
    	}
    }
    
    /**
     * Make the given BoundObject have the Roles given.
     * @param anObject      the BoundObject
     * @param roles         the new roles, may be empty but not <code>null</code>.
     * @param inherit       Make all subelements inherit the same roles.
     */
    protected void saveRoles(EditPersonOrGroupRolesMultiComponent aComponent, BoundObject anObject, Collection roles, boolean inherit) {        
        if (!(anObject instanceof AbstractBoundWrapper)) {
            Logger.error("This component only works on AbstractBoundWrappers!", this);
            return;
        }
        Object theModel = aComponent.getModel();

		Group group;
		if (theModel instanceof Person) {
			group = ((Person) theModel).getRepresentativeGroup();
		} else {
			group = (Group) theModel;
		}

		Collection currRoles = BoundedRole.getRoles(anObject, group);
        
        // Handle add first (may get security problem otherwise
        // if we remove roles first and the current user
        // is about to change his own access rights...
        Collection theRolesToAdd = new ArrayList();
        theRolesToAdd.addAll(roles);
        theRolesToAdd.removeAll(currRoles);
        Iterator toAddIt = theRolesToAdd.iterator();
        while (toAddIt.hasNext()) {
            Object next = toAddIt.next();
			BoundedRole theRole = (BoundedRole) next;
            
            // this may be the case in the single select roles view when selecting no role
            if (theRole == null) {
                continue;
            }
            
			BoundedRole.assignRole(anObject, group, theRole);
        }   
        
        // Handle remove
        Collection theRolesToRemove = new ArrayList();
        theRolesToRemove.addAll(currRoles);
        theRolesToRemove.removeAll(roles);
        Iterator toRemoveIt = theRolesToRemove.iterator();
        while (toRemoveIt.hasNext()) {
            BoundedRole theRole = (BoundedRole) toRemoveIt.next();
			BoundedRole.removeRoleAssignments(anObject, group, theRole);
        }
        
        // Handle recursion
        if (inherit) {
			Collection theChildren = anObject.getSecurityChildren();
            if (theChildren != null) {
                Iterator theChildrenIt = theChildren.iterator();
                while (theChildrenIt.hasNext()) {
                    BoundObject theChildBO = (BoundObject) theChildrenIt.next();
                    saveRoles(aComponent, theChildBO, roles, inherit);
                }
            }
        }
    }
}
