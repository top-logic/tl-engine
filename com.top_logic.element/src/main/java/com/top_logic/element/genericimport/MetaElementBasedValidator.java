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
import com.top_logic.element.genericimport.GenericDataImportConfiguration.ColumnAttributeMapping;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericValidator;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MetaElementBasedValidator extends MetaElementBasedImportBase implements
        GenericValidator {

    private boolean allowEmptyValues;
    
    /** 
     * Creates a {@link MetaElementBasedValidator}.
     */
    public MetaElementBasedValidator(Properties aSomeProps) {
        super(aSomeProps);
        
        this.allowEmptyValues = Boolean.valueOf(aSomeProps.getProperty("allowNullValues", "false")).booleanValue();
    }

    public ValidationResult validate(GenericValueMap aDO, GenericCache aCache, boolean validateReferences) {
        ValidationResult theResult = new ValidationResult(aDO);
        
        String[]    theCols = aDO.getAttributeNames();
        TLClass theMeta  = this.getMetaElement();

        GenericDataImportConfiguration theConfig  = this.getImportConfiguration();                
        String                         theType    = theMeta.getName();
        
        for (int i=0; i<theCols.length; i++) {
            String theCol  = theCols[i];
            String theAttr = null; 
            ColumnAttributeMapping theMapping = theConfig.getMappingForAttribute(theType, theCol);
            
            if (theMapping != null) {
                theAttr = theMapping.getAttributeName();
            }
            if (theAttr == null) {
                theAttr = theCol;
            }
            
            try {
                if (MetaElementUtil.hasMetaAttribute(theMeta, theCol)) {
                    if (theMapping == null || ! theMapping.isIgnoreExistance() ) {
						TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theMeta, theCol);
                        Object        theVal = aDO.getAttributeValue(theCol);
                        
                        // only perform the validation if the current attribute is of the kind
                        // we're checking, i.e. references or a simple type
                        if ((validateReferences && this.isReference(theMA)) ||
                            (!validateReferences && !this.isReference(theMA))) {
                            if (AttributeOperations.isReadOnly(theMA)) {
                                theResult.addError(new AttributeError(theCol, I18NConstants.ERROR_READ_ONLY));
                            }
                            else if (theMA.isMandatory() || ! allowEmptyValues) {
                                if (theVal == null || (theVal instanceof Collection && ((Collection) theVal).isEmpty())) {
                                    theResult.addError(new AttributeError(theCol, I18NConstants.ERROR_NOT_NULL));
                                }
                            }
                        }
                    }
                }
            } catch (NoSuchAttributeException nsax) {
				theResult.addError(new AttributeError(theCol, ResKey.text(nsax.getMessage())));
            }
        }
        return theResult;
    }

    
    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericValidator#validateSimpleTypes(com.top_logic.element.genericimport.interfaces.GenericValueMap, com.top_logic.element.genericimport.interfaces.GenericCache)
     */
    @Override
	public ValidationResult validateSimpleTypes(GenericValueMap aDO, GenericCache aCache) {
        return this.validate(aDO, aCache, false);
    }
    
    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericValidator#validateReferenceTypes(com.top_logic.element.genericimport.interfaces.GenericValueMap, com.top_logic.element.genericimport.interfaces.GenericCache)
     */
    @Override
	public ValidationResult validateReferenceTypes(GenericValueMap aDO, GenericCache aCache) {
        return this.validate(aDO, aCache, true);
    }
    
    private boolean isReference(TLStructuredTypePart aMA) {
        int theType = AttributeOperations.getMetaAttributeType(aMA);
        return theType == LegacyTypeCodes.TYPE_WRAPPER ||
               theType == LegacyTypeCodes.TYPE_TYPEDSET ||
               theType == LegacyTypeCodes.TYPE_SINGLE_REFERENCE ||
               theType == LegacyTypeCodes.TYPE_SINGLEWRAPPER ||
               theType == LegacyTypeCodes.TYPE_COLLECTION ||
               theType == LegacyTypeCodes.TYPE_LIST;
    }
}

