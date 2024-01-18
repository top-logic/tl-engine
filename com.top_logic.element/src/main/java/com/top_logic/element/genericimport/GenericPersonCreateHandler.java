/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.base.user.UserInterface;
import com.top_logic.element.genericimport.interfaces.GenericCreateHandler;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.knowledge.gui.layout.person.NewPersonCommandHandler;
import com.top_logic.knowledge.objects.CreateException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericPersonCreateHandler extends WrapperUpdateHandler implements GenericCreateHandler {
    
    private final NewPersonCommandHandler factory;
    
    public GenericPersonCreateHandler(Properties props) {
        super(props);
        factory = (NewPersonCommandHandler) CommandHandlerFactory.getInstance().getHandler(NewPersonCommandHandler.COMMAND_ID);
    }
    
    @Override
	public Object createBusinessObject(GenericValueMap aDO, String aFKeyAttr) throws CreateException {
		{
            String login     = (String) aDO.getAttributeValue(Person.NAME_ATTRIBUTE);
			String firstName = (String) aDO.getAttributeValue(UserInterface.FIRST_NAME);
			String lastName = (String) aDO.getAttributeValue(UserInterface.NAME);
            String title     = (String) aDO.getAttributeValue(UserInterface.TITLE);
			Boolean isRestricted = (Boolean) aDO.getAttributeValue(Person.RESTRICTED_USER);
            
			Person person =
				factory.createPerson(login, firstName, lastName, title, "dbSecurity", isRestricted);
            super.updateBusinessObject(person, aDO, aFKeyAttr);
            return person;
        }
    }
}
