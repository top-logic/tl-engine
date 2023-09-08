/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Iterator;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.TLContext;

/**
 * This is the Persistent copy of the {@link TransientPersonalConfiguration}.
 * 
 * @author    <a href=mailto:fma@top-logic.com>fma</a>
 */
public class PersonalConfigurationWrapper extends AbstractBoundWrapper implements PersonalConfiguration {

    /**
     * the name of the KnowledgeAssociation between a person and a PersonalConfiguration
     */
    private static final String HAS_CONFIGURATION="hasPersonalConfiguration";
    
	/**
	 * Outgoing {@link AssociationSetQuery} for the association holding the
	 * {@link PersonalConfiguration} of a {@link Person}.
	 */
	public static final AssociationSetQuery<KnowledgeAssociation> PERSONAL_CONFIG_ASSOCIATION_QUERY =
		AssociationQuery.createOutgoingQuery(HAS_CONFIGURATION, HAS_CONFIGURATION);

    /**
     * KO wrapper by this class
     */
    public static final String OBJECT_NAME = "PersonalConfiguration";
    

    /**
     * Only needed for WrapperFactory.
     */
    public PersonalConfigurationWrapper(KnowledgeObject ko) {
        super(ko);
    }

    /**
     *  NOTE: Use the {@link TLContext#getPersonalConfiguration()} to get/set individual information 
     *        of the current user! For futher information ask KHA.
     *  
     * returns the personal configuration belonging to the given person. If none exists, null is returned
     * 
     * @param aPerson the Person to get the configuartion object for
     * @return the configuration object of the given person or null.
     */
    public static PersonalConfigurationWrapper getPersonalConfiguration(Person aPerson) {
		try {
			Iterator<PersonalConfigurationWrapper> iter = AbstractWrapper
				.resolveWrappersTyped(aPerson, PERSONAL_CONFIG_ASSOCIATION_QUERY, PersonalConfigurationWrapper.class)
				.iterator();
			if (iter.hasNext()) {
				PersonalConfigurationWrapper persConf = iter.next();
                if(iter.hasNext()) {
                    Logger.warn("There are at least two PersonalConfigurations for "+aPerson+" in the KnowledgeBase.",PersonalConfigurationWrapper.class);
                }
				return persConf;
            }
        }
        catch(Exception e){
            Logger.warn("Problem getting peronal configuration for "+aPerson, e, PersonalConfigurationWrapper.class);
        }
        return null;
    }

	/**
	 * Creates a new {@link PersonalConfigurationWrapper} for the given person.
	 * 
	 * Take care to {@link Transaction#commit() commit} this correctly. Call only when
	 * {@link #getPersonalConfiguration(Person)} returns <code>null</code>.
	 */
    public static PersonalConfigurationWrapper createPersonalConfiguration(Person aPerson){
    	PersonalConfigurationWrapper pc = getPersonalConfiguration(aPerson);
    	if(pc!=null){
    		return pc; //already exists for given person
    	}
    	else{ // create a new Config object
    		try{
	            KnowledgeBase   kb = aPerson.getKnowledgeBase();  
				KnowledgeObject ko = kb.createKnowledgeObject(OBJECT_NAME);
				kb.createAssociation(aPerson.tHandle(), ko, HAS_CONFIGURATION);
				return (PersonalConfigurationWrapper) WrapperFactory.getWrapper(ko);
    		}catch(Exception e){
                Logger.warn("Problem getting peronal configuration for "+aPerson, e, PersonalConfigurationWrapper.class);
            }
            return null;
        }
    }
}
