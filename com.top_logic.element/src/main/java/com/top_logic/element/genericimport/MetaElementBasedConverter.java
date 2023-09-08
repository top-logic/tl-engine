/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.ParseBooleanMapping;
import com.top_logic.basic.col.ParseDateMapping;
import com.top_logic.basic.col.ParseStringMapping;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.ColumnAttributeMapping;
import com.top_logic.element.genericimport.converterfunction.ResolveReferenceListMapping;
import com.top_logic.element.genericimport.converterfunction.ResolveReferenceMapping;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericConverterFunction;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;


/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MetaElementBasedConverter extends AbstractGenericConverter {

    private Mapping dateMapping;

    public MetaElementBasedConverter(Properties someProps){
        super(someProps);
        
        // default is like Date#toString() format
        String format = someProps.getProperty("dateFormat", "EEE MMM dd HH:mm:ss z yyyy");
        
		this.dateMapping = new ParseDateMapping(CalendarUtil.newSimpleDateFormat(format, new Locale("en")));
    }
    
    @Override
	public boolean checkImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) throws Exception {
        TLClass theMeta = MetaElementBasedImportBase.getUniqueMetaElement(anInternalType);
        if (theMeta == null) {
            throw new ConfigurationError("No meta element found for type " +anInternalType);
        }
        
        Set theAttrs = aConfig.getAttributes(anInternalType);
        for (Iterator theAttrIter = theAttrs.iterator(); theAttrIter.hasNext(); ) {
            String theAttr = (String) theAttrIter.next();
            ColumnAttributeMapping theMapping = aConfig.getMappingForAttribute(anInternalType, theAttr);
            if (theMapping == null || ! theMapping.isIgnoreExistance()) {
				MetaElementUtil.getMetaAttribute(theMeta, theAttr); // just check if meta attribute exists
            }
        }
        
        return true;
    }

    @Override
	public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) {
        try {

            boolean theResult = super.setImportConfiguration(aConfig, anInternalType);
            
            if (theResult) {
                this.adjustConfig(this.getImportConfiguration(), anInternalType);
            }
            
            return theResult;
        } catch (Exception ex) {
            Logger.error("Invalid configuration!", ex, this.getClass());
        }
        return false;
    }
    
    /**
     * For MetaElements this will add the foreign key to the mapped result, since
     * it is needed for references as well as simple types.
     * 
     * @see com.top_logic.element.genericimport.AbstractGenericConverter#postConvert(com.top_logic.element.genericimport.interfaces.GenericValueMap, com.top_logic.element.genericimport.interfaces.GenericValueMap, com.top_logic.element.genericimport.interfaces.GenericCache)
     */
    @Override
	protected void postConvert(GenericValueMap aSource, GenericValueMap aResult,
            GenericCache aCache) throws DataObjectException {
     // no matter whether we're mapping references or simple types - we need the ID value
        GenericDataImportConfiguration theConf = this.getImportConfiguration();
        String fKey = theConf.getForeignKey(this.getType());
        
        if (!aResult.hasAttribute(fKey)) {
            ColumnAttributeMapping theMap = theConf.getMappingForColumn(this.getType(), fKey);
            Object theValue = aSource.getAttributeValue(fKey);
            if (theMap != null) {
                String theAttr = theMap.getAttributeName();
                // if the key has already been mapped we can save time by just
                // adding the mapped value again under the foreign key name.
                if (aResult.hasAttribute(theAttr)) {
                    aResult.setAttributeValue(fKey, aResult.getAttributeValue(theAttr));
                }
                else {
                    GenericConverterFunction theFunc = theMap.getFunction();
                    if (theFunc != null) {
                        aResult.setAttributeValue(fKey, this.map(theValue, theFunc, aCache));
                    }
                }
            }
            else {
                aResult.setAttributeValue(fKey, theValue);
            }
        }
    }
    
    /** 
     * This method adds default mapping functions for all supported meta attributes
     * if no other function is set.
     * 
     * @param aConf      The importer configuration instance.
     */
    private void adjustConfig(GenericDataImportConfiguration aConf, String anInternalType) throws Exception {
        TLClass theMeta = MetaElementBasedImportBase.getUniqueMetaElement(anInternalType);
        
        // Check all meta attributes for mappings 
		Collection<TLStructuredTypePart> theAttrs = TLModelUtil.getMetaAttributes(theMeta);
        for (TLStructuredTypePart metaAttribute : theAttrs) {
            String theAttr = metaAttribute.getName();
            
            if (AttributeOperations.isReadOnly(metaAttribute)) {
                continue;
            }
            
            boolean isReference = this.isReference(metaAttribute);
            
            ColumnAttributeMapping theMapping = aConf.getMappingForAttribute(anInternalType, theAttr);
            GenericConverterFunction  theFunc = this.getMappingFunction(metaAttribute, theMapping);
            
            // this will cause an automatic mapping
            if (theFunc != null && theMapping == null) { // create new mapping for supported attributes only
                theMapping = new ColumnAttributeMapping(theAttr, theAttr, isReference, null, theFunc, true, false);
            }
            
            if (theMapping != null) { 
                if (theMapping.getFunction() == null && theFunc != null) { // set own function for mappings without function
                    theMapping.setFunction(theFunc);
                }
                if (theMapping.getColumn() == null) {
                    theMapping.setColumn(theAttr);
                }
                
                theMapping.setIsReference(isReference);
                
                aConf.setMapping(anInternalType, theMapping);
            }
        }
    }
    
    /** 
     * This method assigns a default mapping function based on the meta attribute's
     * type.
     */
    private GenericConverterFunction getMappingFunction(TLStructuredTypePart aMA, ColumnAttributeMapping aMapping) {
        switch (AttributeOperations.getMetaAttributeType(aMA)) {
            case LegacyTypeCodes.TYPE_BOOLEAN:
                return new WrappedMapping(ParseBooleanMapping.INSTANCE);
            case LegacyTypeCodes.TYPE_DATE:
                return new WrappedMapping(dateMapping);
            case LegacyTypeCodes.TYPE_LONG:
                return LongConverterFunction.INSTANCE;
            case LegacyTypeCodes.TYPE_FLOAT:
                return FloatConverterFunction.INSTANCE;
            case LegacyTypeCodes.TYPE_STRING:
                return new WrappedMapping(ParseStringMapping.INSTANCE);
            case LegacyTypeCodes.TYPE_CLASSIFICATION:
                return new WrappedMapping(ParseFastlistElementMapping.INSTANCE);
            case LegacyTypeCodes.TYPE_WRAPPER:
            case LegacyTypeCodes.TYPE_SINGLEWRAPPER:
            case LegacyTypeCodes.TYPE_SINGLE_REFERENCE:
            case LegacyTypeCodes.TYPE_SINGLE_STRUCTURE:
                return new ResolveReferenceMapping(this.getImportConfiguration(), aMapping);
            case LegacyTypeCodes.TYPE_COLLECTION:
            case LegacyTypeCodes.TYPE_TYPEDSET:
            case LegacyTypeCodes.TYPE_LIST:
            case LegacyTypeCodes.TYPE_STRUCTURE:
                return new ResolveReferenceListMapping(this.getImportConfiguration(), aMapping);
        }
        return null;
    }
    
    /** 
     * Returns true if the given meta attribute is a key (a reference)
     * to another table entry, or a list of such references.
     */
    private boolean isReference(TLStructuredTypePart aMA) {
        int theType = AttributeOperations.getMetaAttributeType(aMA);
        return theType == LegacyTypeCodes.TYPE_WRAPPER ||
               theType == LegacyTypeCodes.TYPE_TYPEDSET ||
               theType == LegacyTypeCodes.TYPE_SINGLE_REFERENCE ||
               theType == LegacyTypeCodes.TYPE_SINGLEWRAPPER ||
               theType == LegacyTypeCodes.TYPE_SINGLE_STRUCTURE||
               theType == LegacyTypeCodes.TYPE_STRUCTURE||
               theType == LegacyTypeCodes.TYPE_COLLECTION ||
               theType == LegacyTypeCodes.TYPE_LIST;
    }
    
    static class NumberConverterFunction implements GenericConverterFunction {
        @Override
		public Object map(Object aValue, GenericCache aCache) {
			return Double.valueOf((String) aValue);
        }
    }
    
    static class LongConverterFunction extends NumberConverterFunction {
        
        public static final GenericConverterFunction INSTANCE = new LongConverterFunction();
        
        @Override
		public Object map(Object aValue, GenericCache aCache) {
            Number theNum = (Number) super.map(aValue, aCache);
			return Long.valueOf(theNum.longValue());
        }
    }
    
    static class FloatConverterFunction extends NumberConverterFunction {
        
        public static final GenericConverterFunction INSTANCE = new FloatConverterFunction();
        
        @Override
		public Object map(Object aValue, GenericCache aCache) {
            Number theNum = (Number) super.map(aValue, aCache);
			return Float.valueOf(theNum.floatValue());
        }
    }
}

