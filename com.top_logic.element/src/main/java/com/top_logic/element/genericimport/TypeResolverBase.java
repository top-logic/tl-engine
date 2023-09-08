/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.element.genericimport.interfaces.GenericTypeResolver;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class TypeResolverBase implements GenericTypeResolver {

    public static final String DEFAULT_TYPE         = "default";
    public static final String PREFIX_EXTERNAL_TYPE = "externalType:";
    public static final int    PREFIX_EXTERNAL_TYPE_LENGTH = "externalType:".length();
    
    private final Map<String, String> typeMapping = new HashMap<>();
    
    public TypeResolverBase(Properties someProps) {
        for (Iterator theIter = someProps.keySet().iterator(); theIter.hasNext();) {
            String theExternalType = (String) theIter.next();
            String theInternalType = someProps.getProperty(theExternalType);
         
            // or use a prefixed key as definition of external type
            if(theExternalType.startsWith(PREFIX_EXTERNAL_TYPE)) {
                theExternalType = theExternalType.substring(PREFIX_EXTERNAL_TYPE_LENGTH);
            }
            // ignore the rest
            else {
                continue;
            }
            
            String checkedType = this.checkTypeConfiguration(theExternalType, theInternalType);
            if (checkedType == null) {
                throw new ConfigurationError("Configured value '" + theInternalType + "' for value '" + theExternalType + "' is not a unique meta element type");
            }
            this.typeMapping.put(theExternalType, checkedType);
        }
        
        if (! this.typeMapping.containsKey(DEFAULT_TYPE)) {
            throw new ConfigurationError("At least the property 'externalType:default' must be specified for " + MetaElementBasedTypeResolver.class.getSimpleName());
        }
    }

    protected abstract String checkTypeConfiguration(String externalType, String internalType);
    
    @Override
	public final String resolveType(GenericValueMap aDO) {
        return this.resolveType(aDO.getType());
    }
    
    @Override
	public String resolveType(String anExternalType) {
        if (this.typeMapping.containsKey(anExternalType)) {
            return this.typeMapping.get(anExternalType);
        }
        else {
            return this.typeMapping.get(DEFAULT_TYPE);
        }
    }

}
