/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Properties;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * TODO FMA this class colects function to bre (re-)moved.
 * 
 * The should be either
 *   - moved to the concerned classes.
 *   - not be used at all.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TLUtil {
    
    /**
     * Creates a new association of given type between source and target and
     * puts it in the KnowledgeBase.
     * 
     * @param metaObject string representing the type of KnowledgeAssociation to
     *            create.
     * @param sourceObject source KnowledgeObject of assocication.
     * @param destinationObject dest KnowledgeObject of association.
     * @return created KnowledgeAssociation if successful, null otherwise.
     */
    public static KnowledgeAssociation createKnowledgeAssociation(
        String metaObject,
        KnowledgeObject sourceObject,
        KnowledgeObject destinationObject) {
        try {
            KnowledgeBase theBase = sourceObject.getKnowledgeBase();
			KnowledgeAssociation theKA = theBase.createAssociation(sourceObject, destinationObject, metaObject);

            return theKA;
        } catch (Exception ex) {
            Logger.error(
                "Couldn't create KnowledgeAssociation of type "
                    + metaObject
                    + " between "
                    + sourceObject
                    + " and "
                    + destinationObject
                    + ".",
                ex,
                TLUtil.class);
            // TODO KBU check exception type
            throw new RuntimeException("Could not create KnowledgeAssociation of type " + metaObject + ".", null);
        }
    }

    /**
     * Returns the property aName of section aSection from the property-XML
     * 
     * @param aSection  the section to get the property from
     * @param aName     the name of the property
     * @param aDefault  the value to be used if the property is not found or not a valid int
     * @return          the property aName from aSection
     */
    public static int getIntProperty(String aSection, String aName, int aDefault) {
        int theResult = aDefault;
        
        try {
            String hlp = TLUtil.getProperties(aSection).getProperty(aName);
            if (hlp == null) {
                Logger.warn("No property for section " + aSection + " and name " + aName + " found. Using default: "+aDefault, TLUtil.class);
            }
            theResult = Integer.parseInt(hlp);
        } catch (Exception e) {
            Logger.error("Problem getIntProperty(" + aSection + "," + aName + ")", e, TLUtil.class);
        }

        return theResult;
    }

    /**
     * Returns the property aName of section aSection from the property-XML
     * If no property or no section exists and no default is given, the String
     * "missingProperty:aSection:aName" is returned
     * 
     * @param aSection  the section to get the property from
     * @param aName     the name of the property
     * @param aDefault  the value to be used if the property is not found
     * @return          the property aName from aSection
     */
    public static String getProperty(String aSection, String aName, String aDefault) {
        String theResult = null;
    
        try {
            theResult = TLUtil.getProperties(aSection).getProperty(aName);
        } catch (Exception e) {
            Logger.error("Problem getProperty(" + aSection + "," + aName + ")", e, TLUtil.class);
        }
    
        if (theResult == null) {
            Logger.warn("No property for section " + aSection + " and name " + aName + " found.", TLUtil.class);
            if (aDefault != null) {
                theResult = aDefault;
            } else {
                theResult = "missingProperty: " + aSection + ":" + aName;
            }
        }
    
        return theResult;
    }
    
    /**
     * Get the Properties with the given key.
     * This is quite expensive, as the Properties are not cached!
     * 
     * @return the properties with the given key
     */
    public static Properties getProperties(String aKey) {
        return (Configuration.getConfigurationByName(aKey).getProperties());
    }
    
}
