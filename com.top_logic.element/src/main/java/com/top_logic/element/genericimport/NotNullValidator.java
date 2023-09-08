/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Collection;
import java.util.Properties;

import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericValidator;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;

/**
 * The NotNullValidator validates that all provided values are not null and collections
 * are not empty.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class NotNullValidator extends AbstractGenericDataImportBase implements
        GenericValidator {

    private boolean allowEmptyValues;

    /** 
     * Creates a {@link NotNullValidator}.
     */
    public NotNullValidator(Properties aSomeProps) {
        super(aSomeProps);
        
        this.allowEmptyValues = Boolean.valueOf(aSomeProps.getProperty("allowNullValues", "false")).booleanValue();
    }

    public ValidationResult validate(GenericValueMap aDO, GenericCache aCache) {
        ValidationResult theResult = new ValidationResult(aDO);
        String[] theAttrs = aDO.getAttributeNames();
        for (int i=0; i<theAttrs.length; i++) {
            String theAttr = theAttrs[i];
            try {
                Object theVal  = aDO.getAttributeValue(theAttr);
                if (! allowEmptyValues) {
                    if (theVal == null || (theVal instanceof Collection && ((Collection) theVal).isEmpty())) {
                        theResult.addError(new AttributeError(theAttr, I18NConstants.ERROR_NOT_NULL));
                    }
                }
            } catch (NoSuchAttributeException nsax) {
                theResult.addError(new AttributeError(theAttr, ResKey.text(nsax.getMessage())));
            }
        }
        return theResult;
    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericValidator#validateSimpleTypes(com.top_logic.element.genericimport.interfaces.GenericValueMap, com.top_logic.element.genericimport.interfaces.GenericCache)
     */
    @Override
	public ValidationResult validateSimpleTypes(GenericValueMap aDO, GenericCache aCache) {
        return validate(aDO, aCache);
    }
    
    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericValidator#validateReferenceTypes(com.top_logic.element.genericimport.interfaces.GenericValueMap, com.top_logic.element.genericimport.interfaces.GenericCache)
     */
    @Override
	public ValidationResult validateReferenceTypes(GenericValueMap aDO, GenericCache aCache) {
        return validate(aDO, aCache);
    }
}

