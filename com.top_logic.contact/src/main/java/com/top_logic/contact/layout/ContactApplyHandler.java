/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import java.util.Iterator;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.person.EditPersonContactComponent;
import com.top_logic.element.meta.form.component.AbstractApplyAttributedCommandHandler;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.wrap.Group;


/**
 * Apply handling for personContacts.
 *
 * @author <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ContactApplyHandler extends AbstractApplyAttributedCommandHandler {

    /** The ID of this command. */
    public static final String COMMAND_ID = "contactApply";

    

    /**
     * Creates a new instance of this class.
     */
    public ContactApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
    }
    
    /**
     * Everything is a meta attribute so use the 
     * {@link AbstractApplyAttributedCommandHandler#saveMetaAttributes(FormContext)} method.
     */
    @Override
	protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
    	boolean flag = false;
		if (model instanceof AttributedWrapper) {
			flag = saveMetaAttributes(formContext);
        }
//    	formfield for "representative" is automatically added for personcontacts that belong to a person
//		so if its present we take it into account    	
		if (flag && model instanceof PersonContact
			&& formContext.hasMember(EditPersonContactComponent.PARAM_REPRESENTATIVES)) {
			PersonContact theContact = (PersonContact) model;
        	Person theContactsPerson = theContact.getPerson();
        	if(theContactsPerson!=null){
        		Group theRepresentativesGroup = theContactsPerson.getRepresentativeGroup();
        		if(theRepresentativesGroup!=null){
            		//remove all members
            		Iterator theMembers = theRepresentativesGroup.getMembers().iterator();
            		while(theMembers.hasNext()){
            			theRepresentativesGroup.removeMember((Wrapper)theMembers.next());
            		}
            		//set selected members
					SelectField theRepresentatives =
						(SelectField) formContext.getField(EditPersonContactComponent.PARAM_REPRESENTATIVES);
        			Iterator theRepresentativesIt = theRepresentatives.getSelection().iterator();
        			while(theRepresentativesIt.hasNext()){
        				Wrapper aRepresentative = (Wrapper)theRepresentativesIt.next();
        				theRepresentativesGroup.addMember(aRepresentative);
        			}
        		}else{
        			Logger.error("Attempt to set representatives for a person without representative group! Will be ignored.",this);
        		}
        	}else{
        		Logger.error("Attempt to set representatives for a person that is null! Will be ignored.",this);
        	}
        }
        return flag;
    }

}
