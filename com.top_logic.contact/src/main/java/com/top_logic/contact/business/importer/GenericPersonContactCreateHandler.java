/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business.importer;

import java.util.Properties;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.ParseBooleanMapping;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.mandatoraware.COSPersonContact;
import com.top_logic.element.genericimport.MetaElementBasedUpdateHandler;
import com.top_logic.element.genericimport.interfaces.GenericCreateHandler;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.gui.layout.person.NewPersonCommandHandler;
import com.top_logic.knowledge.objects.CreateException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class GenericPersonContactCreateHandler extends MetaElementBasedUpdateHandler implements
        GenericCreateHandler {

    private static final String IS_USER_COLUMN = "isUser";
    private static final String USER_ID_COLUMN = "userID";
    
    private final ContactFactory          contactFactory;
    private final NewPersonCommandHandler userFactory;
    private final boolean createUser;

    /**
	 * Creates a {@link GenericPersonContactCreateHandler}.
	 */
    public GenericPersonContactCreateHandler(Properties aSomeProps) {
        super(aSomeProps);
        
        try {
            this.createUser = ConfigUtil.getBooleanValue("createUser", aSomeProps.getProperty("createUser"), Boolean.FALSE);
        }
        catch (ConfigurationException e) {
            throw new ConfigurationError("Invalid configuration for " + GenericPersonContactCreateHandler.class, e);
        }
        
        this.contactFactory = ContactFactory.getInstance();
        this.userFactory    = (NewPersonCommandHandler) CommandHandlerFactory.getInstance().getHandler(NewPersonCommandHandler.COMMAND_ID);
    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericCreateHandler#createBusinessObject(com.top_logic.element.genericimport.interfaces.GenericValueMap, String)
     */
    @Override
	public Object createBusinessObject(GenericValueMap aDO, String aFKeyAttr) throws CreateException {
        
		PersonContact theContact = (PersonContact) this.contactFactory.createNewWrapper(ContactFactory.PERSON_TYPE);
        theContact.setValue(COSPersonContact.ATTRIBUTE_MANDATOR, Mandator.getRootMandator());
		updateBusinessObject(theContact, aDO, aFKeyAttr);
        
        if (this.createUser) {
			{
                boolean isUser = ParseBooleanMapping.INSTANCE.map((String) aDO.getAttributeValue(IS_USER_COLUMN));
                String  userID = (String) aDO.getAttributeValue(USER_ID_COLUMN);
                if (isUser) {
					userFactory.createPerson(userID, theContact.getString(PersonContact.FIRST_NAME),
						theContact.getString(PersonContact.NAME_ATTRIBUTE),
						theContact.getString(PersonContact.TITLE), "dbSecurity", (Boolean) aDO.getAttributeValue(Person.RESTRICTED_USER));
                }
            }
        }
        
        return theContact;
    }
}

