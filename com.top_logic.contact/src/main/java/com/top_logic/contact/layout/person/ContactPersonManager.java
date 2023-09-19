/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.base.security.device.SecurityDeviceFactory;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.person.related.NewRelatedPersonCommandHandler;
import com.top_logic.element.knowledge.wrap.person.ElementPersonManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Extend the TLPersonManager with knowledge about PersonContacts.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@ServiceDependencies(ModelService.Module.class)
public class ContactPersonManager extends ElementPersonManager {

	/**
	 * <p>
	 * <b>The {@link PersonManager} is build via reflection so no other constructor is allowed.</b>
	 * </p>
	 */
	@CalledByReflection
	public ContactPersonManager(InstantiationContext context, Config config) {
		super(context, config);
	}

	/** Key in TLContext to store related Person temporary for {@link NewRelatedPersonCommandHandler}. */
	public static final Property<PersonContact> RELATED_PERSON_CONTACT =
		TypedAnnotatable.property(PersonContact.class, "relatedPersonContact");
    
    @Override
	protected void handleNewPerson(Person aPerson) {
        super.handleNewPerson(aPerson);

        try{
            // handle RELATED_PERSON_CONTACT
			PersonContact personContact = TLContext.getContext().get(RELATED_PERSON_CONTACT);
            if (personContact != null) {
                this.copyContactAttributesToPerson(personContact, aPerson); 
                this.connectRelatedContactToPerson(aPerson, personContact);
            } 
            else {
                // handle normal case
                personContact = ContactFactory.getInstance().getContactForPerson(aPerson);
                if (personContact == null){
                    personContact = this.createContactForPerson(aPerson);
                    this.connectContactToPerson(aPerson, personContact);
                } else {
                    personContact.copyPersonAttributesToContact(aPerson);
                }
            }

        } catch (Exception e) {
            Logger.error("Failed to create Contact for newly created Person", e, ContactPersonManager.class);
        }
    }

    /**
     * Creates a PersonContact for the given person.
     *
     * @param aPerson
     *        the person to create a person contact for
     */
    protected PersonContact createContactForPerson(Person aPerson) {
        return ContactFactory.getInstance().createNewPersonContact(aPerson.getLastName(), aPerson.getFirstName());
    }

    @Override
	public void handleRefreshPerson(Person aPerson) {
        super.handleRefreshPerson(aPerson);
        
        try{
            String theName = aPerson.getName();
            PersonContact theContact = ContactFactory.getInstance().getContactForPerson(aPerson);
            if (theContact == null) {
                try{
                    theContact = this.createContactForPerson(aPerson);
                    this.connectContactToPerson(aPerson, theContact);
					Logger.info("Created missing contact for account '" + theName + "'.", ContactPersonManager.class);
				} catch (Exception ex) {
					Logger.error("Unable to create missing contact for account '" + theName + "'.", ex,
						ContactPersonManager.class);
                }
            }
            if (theContact != null) {
                theContact.copyPersonAttributesToContact(aPerson);
            }
		} catch (Exception ex) {
			Logger.error("Unable to transfer account update to contact", ex, ContactPersonManager.class);
        }
    }

    /**
     * This method is a hook for sub-classes.
     * 
     * @param aPerson
     *        The new Person. Never <code>null</code>.
     * @param aPersonContact
     *        The person contact maybe a new created. Never <code>null</code>.
     */
	public void connectContactToPerson(Person aPerson, PersonContact aPersonContact) {
        aPersonContact.connectToPerson(aPerson);
    }

    /**
     * This method is a hook for sub-classes for RELATED_PERSON_CONTACT.
     * 
     * @param aPerson
     *        The new Person. Never <code>null</code>.
     * @param aPersonContact
     *        The existing person contact. Never <code>null</code>.
     */
	public void connectRelatedContactToPerson(Person aPerson, PersonContact aPersonContact) {
        aPersonContact.connectToPerson(aPerson);
    }

    public void copyContactAttributesToPerson(PersonContact aPersonContact, Person aPerson){
        PersonDataAccessDevice theProxy;
        boolean               deviceReadonly;
		{
            theProxy       = SecurityDeviceFactory.getPersonAccessDevice(aPerson.getDataAccessDeviceID());
            deviceReadonly = (theProxy != null) && theProxy.isReadOnly();
        }
        
        try {
            UserInterface user = Person.getUser(aPerson);
            if (!deviceReadonly && user != null) {
                user.setAttributeValue(PersonAttributes.SUR_NAME,        aPersonContact.getValue(PersonContact.NAME_ATTRIBUTE));
                user.setAttributeValue(PersonAttributes.GIVEN_NAME,      aPersonContact.getValue(PersonContact.ATT_FIRSTNAME));
                user.setAttributeValue(PersonAttributes.TITLE,           aPersonContact.getValue(PersonContact.ATT_TITLE));
				user.setAttributeValue(PersonAttributes.MAIL_NAME, aPersonContact.getValue(PersonContact.ATT_MAIL));
				user.setAttributeValue(PersonAttributes.PRIVATE_NR,
					aPersonContact.getValue(PersonContact.ATT_PHONE_PRIVATE));
                user.setAttributeValue(PersonAttributes.MOBILE_NR,       aPersonContact.getValue(PersonContact.ATT_PHONE_MOBILE));
                user.setAttributeValue(PersonAttributes.INTERNAL_NR,     aPersonContact.getValue(PersonContact.ATT_PHONE_OFFICE));
            }
        }
        catch (Exception ex) {
            Logger.error("Failed to copyContactAttributesToPerson ('" + aPersonContact.getName() + "') and ('" + aPerson.getName() + "')", ex, ContactPersonManager.class);
        }
    }
    
}
