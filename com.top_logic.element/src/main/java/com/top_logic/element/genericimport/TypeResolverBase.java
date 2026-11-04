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
 * Base class for {@link GenericTypeResolver}s mapping external type names to internal types.
 *
 * <p>
 * The mapping is configured by properties of the form {@code externalType:<external>} with the
 * internal type as value. The mapping for {@link #DEFAULT_TYPE} is mandatory and used for all
 * external types without explicit mapping.
 * </p>
 *
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class TypeResolverBase implements GenericTypeResolver {

    /**
     * External type name whose mapping is used for all external types without explicit mapping.
     */
    public static final String DEFAULT_TYPE         = "default";

    /**
     * Prefix of the properties defining the mapping of an external type.
     */
    public static final String PREFIX_EXTERNAL_TYPE = "externalType:";

    /**
     * Length of {@link #PREFIX_EXTERNAL_TYPE}.
     */
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

    /**
     * Checks the configured internal type for an external type.
     *
     * @param externalType
     *        The external type name.
     * @param internalType
     *        The configured internal type.
     * @return The internal type to use, or {@code null} if the configured type is invalid.
     */
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
