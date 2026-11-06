/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.attribute;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.security.AccessChecker;
import com.top_logic.layout.security.DispatchingSecurityProvider;
import com.top_logic.layout.security.SecurityProvider;
import com.top_logic.layout.table.component.TableComponent;

/**
 * The {@link MetaAttributeBasedSecurityProvider} can be used in {@link TableComponent}s (to
 * protect table columns) and in other scenarios to protect the access to model attributes and to
 * other properties that are mapped to model attributes.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MetaAttributeBasedSecurityProvider extends DispatchingSecurityProvider {
	
	/** Configuration options for {@link MetaAttributeBasedSecurityProvider}. */
	public interface Config extends PolymorphicConfiguration<SecurityProvider> {
		/** Whether all columns are protected, even those not listed in {@link #getProtected()}. */
		@Name(TableComponent.XML_CONF_KEY_PROTECT_ALL)
		@BooleanDefault(false)
		boolean getProtectAll();

		/**
		 * Comma separated list of protected properties.
		 *
		 * <p>
		 * Each entry is either an attribute name or a mapping {@code property:attribute}, where the
		 * access to the property is checked against the access to the given attribute.
		 * </p>
		 */
		@Name(TableComponent.XML_CONF_KEY_PROTECTED)
		@Mandatory
		String getProtected();
	}

	/** The value of {@link Config#getProtectAll()}. */
	protected boolean protectAll;

    /** 
     * Creates a {@link MetaAttributeBasedSecurityProvider}.
     */
    public MetaAttributeBasedSecurityProvider(Map<String, String> someMappedProperties, boolean aProtectAll) {
        super(generateCheckerMap(someMappedProperties));
        
        this.protectAll = aProtectAll;
    }
    
    public MetaAttributeBasedSecurityProvider(InstantiationContext context, Config someAttributes) throws ConfigurationException {
		super(generateCheckerMap(context, someAttributes));
        
        this.protectAll = someAttributes.getProtectAll();
    }
    
    private static Map<String, AccessChecker> generateCheckerMap(InstantiationContext context, Config someAttributes) throws ConfigurationException {
        String theProtectedString = someAttributes.getProtected();
        Map<String, String> theMap = new HashMap<>();
        for (Iterator<String> theIt = StringServices.toList(theProtectedString, ',').iterator(); theIt.hasNext(); ) {
            String theProtected = theIt.next();
            String[] theParts = StringServices.split(theProtected, ':');
            if (theParts.length == 1) {
                String theKey = theParts[0].trim();
                theMap.put(theKey, theKey);
            } else {
                String theKey   = theParts[0].trim();
                String theValue = theParts[1].trim();
                theMap.put(theKey, theValue);
            }
        }
        return generateCheckerMap(theMap);
    }

    
    private static Map<String, AccessChecker> generateCheckerMap(Map<String, String> someMappedProperties) {
        Map<String, AccessChecker> theResult = new HashMap<>();
        if (someMappedProperties != null) {
            for (Iterator<Map.Entry<String, String>> theIt = someMappedProperties.entrySet().iterator(); theIt.hasNext();) {
                Map.Entry<String, String> theEntry = theIt.next();
                String        thePropertie     = theEntry.getKey();
                String        theAttributeName = theEntry.getValue();
                AccessChecker theChecker       = new MetaAttributeAccessChecker(theAttributeName);
                theResult.put(thePropertie, theChecker);
            }
        }
        return theResult;
    }
    
    @Override
    protected AccessChecker getAccessChecker(String aProperty) {
    	AccessChecker theChecker = super.getAccessChecker(aProperty);
    	if (theChecker == null) {
    		theChecker = new MetaAttributeAccessChecker(aProperty);
    	}
    	
    	return theChecker;
    }
}

