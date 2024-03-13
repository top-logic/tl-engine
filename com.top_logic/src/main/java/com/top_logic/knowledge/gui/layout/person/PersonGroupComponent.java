/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;

/**
 * {@link LayoutComponent} allowing to edit the groups, a {@link Person} is member of.
 * 
 * @author <a href="mailto:kbu@top-logic.com></a>
 */
public class PersonGroupComponent extends EditComponent {

    public static final String FORM_MEMBER_GROUP = "roles";


	/**
	 * Configuration of the {@link PersonGroupComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditComponent.Config {

		@Override
		@StringDefault(ApplyPersonGroupCommand.COMMAND_ID)
		String getApplyCommand();

		@Override
		@StringDefault(SavePersonGroupCommand.COMMAND_ID)
		String getSaveCommand();

	}

	/**
	 * Creates a new {@link PersonGroupComponent}.
	 */
    public PersonGroupComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			PersonManager r = PersonManager.getManager();
			setModel((TLContext.currentUser()));
		}
		return super.validateModel(context);
    }

    /**
     * We can do something for Users or Person.
     */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
		if (anObject == null) {
			BoundObject defaultObject = BoundHelper.getInstance().getDefaultObject();
			if (defaultObject != null) {
				return allowInHierarchy(getDefaultCommandGroup(), defaultObject, TLContext.getContext());
			}

			return false;
		}
        return (anObject instanceof Person);
    }


	/**
	 * Overridden to forward the resPrefic to the form.
	 */
	@Override
	public FormContext createFormContext() {

		Wrapper model = (Wrapper) this.getModel();
		if (model != null) {
			SelectField theField;
			{
				List theGroups = new ArrayList(Group.getGroups(model, false, true));
				List theAllGroups = Group.getAll();
				List theDisabledGroups = this.getDisabledGroups(theAllGroups);

//          person might be member of representative groups, that are not going to be displayed
// 			and therefore are not returned by getAllGroups. This call will scrub those from list
				theGroups.retainAll(theAllGroups);

				theField = FormFactory.newSelectField(FORM_MEMBER_GROUP, theAllGroups, true, theGroups, false);
				theField.setFixedOptions(theDisabledGroups);
				theField.setOptionComparator(LabelComparator.newCachingInstance());
			}
			return new FormContext("context", getResPrefix(), new FormMember[] { theField });
		} else {
			return new FormContext("context", getResPrefix(), FormContext.NO_FORM_MEMBERS);
		}
	}

    /**
     * Get the Groups that cannot be assigned by the current user
     *
     * @param allGroups all possible groups
     * @return the list of disabled groups
     */
    protected List getDisabledGroups(List allGroups) {
        if (allGroups == null) {
            return null;
        }

        List theDisabledGroups = new ArrayList();
        Iterator theGroups = allGroups.iterator();
        while (theGroups.hasNext()) {
            Group       theGroup = (Group) theGroups.next();
            BoundObject theBO    = theGroup.getBoundObject();

            // Check allowance on the boundobject of the group if available
            boolean allowed = true;
            if (theBO == null) {
                allowed = this.allow(SimpleBoundCommandGroup.WRITE);
            }
            else {
                allowed = this.allow(SimpleBoundCommandGroup.WRITE, theBO);
            }

            if (!allowed) {
                theDisabledGroups.add(theGroup);
            }
        }

        return theDisabledGroups;
    }

    /**
     * Check if aCmdGroup is allowed in the BoundObject hierarchy of aRoot
     *
     * @param aCmdGroup the command group
     * @param aRoot     the root object
     * @param aContext  the TLContext
     * @return true if aCmdGroup is allowed in the BoundObject hierarchy of aRoot
     */
    protected boolean allowInHierarchy(BoundCommandGroup aCmdGroup, BoundObject aRoot, TLContext aContext) {
        if (this.checkAccess(aContext, aRoot, aCmdGroup)) {
            return true;
        }

        boolean allowed = false;
        Collection theChildren = aRoot.getSecurityChildren();
        if (theChildren != null) {
            Iterator theChildrenIt = theChildren.iterator();
            while (theChildrenIt.hasNext() && !allowed) {
                BoundObject theBO = (BoundObject) theChildrenIt.next();
                allowed = this.checkAccess(aContext, theBO, aCmdGroup);
            }
        }

        return allowed;
    }

    /**
     * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
     */
    public static class ApplyPersonGroupCommand extends AbstractApplyCommandHandler {

        public static final String COMMAND_ID = "applyPersonGroup";

        public ApplyPersonGroupCommand(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
            SelectField theField = (SelectField) formContext.getMember(FORM_MEMBER_GROUP);

            if (theField.isChanged()) {
                List theList = theField.getSelection();

                return (this.storeChanges(theList, (BoundObject) model));
            }
            else {
                return (false);
            }
        }
        
        /**
         * @param    aList     The list of groups the model should be applied to.
         * @param    aModel    The bound object to be used for storage.
         * @return   Flag, if something has changed.
         */
		public boolean storeChanges(List aList, BoundObject aModel) {
            Collection theGroups = Group.getGroups((Wrapper)aModel);
            Group      theGroup;
            Object     theObject;

            if (aList != null) {
                for (Iterator theIt = aList.iterator(); theIt.hasNext(); ) {
                   theObject = theIt.next();

					if (theObject instanceof Group) {
                       theGroup = (Group) theObject;
                   }
                   else {
                       theGroup = null;
                   }

                   if (theGroup != null) {
                       if (!theGroups.contains(theGroup)) {
                           theGroup.addMember((Wrapper)aModel);
                       }

                       theGroups.remove(theGroup);
                   }
                }
            }

			// Remove old groups that are not set any more
			for (Iterator theIt = theGroups.iterator(); theIt.hasNext();) {
				theGroup = (Group) theIt.next();

				theGroup.removeMember((Wrapper) aModel);
            }

            return true;
        }
    }

    /**
     * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
     */
    public static class SavePersonGroupCommand extends ApplyPersonGroupCommand {

        // Constants

        /** ID of this handler. */
        public static final String COMMAND_ID = "savePersonGroup";

        

        public SavePersonGroupCommand(InstantiationContext context, Config config) {
        	super(context, config);
        }
        // Overidden methods from ApplyPersonCommandHandler

        /**
         * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            HandlerResult theResult = super.handleCommand(aContext, aComponent, model, someArguments);

            if (theResult.isSuccess() && (aComponent instanceof EditComponent)) {
                ((EditComponent) aComponent).setViewMode();
            }

            return (theResult);
        }
    }
}
