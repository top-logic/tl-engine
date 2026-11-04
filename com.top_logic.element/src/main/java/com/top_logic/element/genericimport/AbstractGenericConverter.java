/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.DataObjectException;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.ColumnAttributeMapping;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericConverter;
import com.top_logic.element.genericimport.interfaces.GenericConverterFunction;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;

/**
 * Base class for {@link GenericConverter}s that map the imported columns to attributes
 * according to the column-attribute mappings of the {@link GenericDataImportConfiguration}.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class AbstractGenericConverter extends AbstractGenericDataImportBase implements GenericConverter {

    private static final GenericConverterFunction DEFAULT_MAPPING      = new WrappedMapping(new Mapping() {
                                                            @Override
															public Object map(Object input) { return input; }
                                                        });

    private String type;
                                                        
    /** 
     * Creates a {@link AbstractGenericConverter}.
     * 
     */
    public AbstractGenericConverter(Properties someProps) {
        super(someProps);
        this.type = someProps.getProperty(GenericDataImportConfiguration.PROP_TYPE);
    }

    /** 
     * This method maps values read from file to values that are meaningful in
     * the context of the knowledge base. 
     * 
     * @param aDO               The source values as they were read from file.
     * @param aCache            The cache of imported objects.
     * @return Returns a new {@link GenericValueMap} with values converted according
     * to the configured mapping. Values of reference attributes are converted by the
     * mapping functions, e.g. to the objects the foreign keys point to.
     */
    @Override
	public GenericValueMap convert(GenericValueMap aDO, GenericCache aCache) throws DataObjectException {
        GenericDataImportConfiguration theConf = this.getImportConfiguration();
        String     theType = this.type;
        
        String[]   theCols = aDO.getAttributeNames();
        GenericValueMap theDO   = new GenericValueMapImpl(theType, aDO.getIdentifier(), theCols.length);

        for (String theCol : theCols) {
            ColumnAttributeMapping theMapping  = theConf.getMappingForColumn(theType, theCol); // must not be null
            
            String  theAttr = null;
            GenericConverterFunction theFunc = getDefaultMappingFuncion();
            
            if (theMapping != null) {
                theAttr     = theMapping.getAttributeName();
                theFunc     = theMapping.getFunction();
            }
            else {
                theAttr = theCol;
            }
            
            if (theAttr == null) {
                throw new IllegalStateException("Misconfiguration! Mapped attribute must not be null for type " + theType + " / column " + theCol);
            }
            
                Object theValue = aDO.getAttributeValue(theCol);
                theDO.setAttributeValue(theAttr, this.map(theValue, theFunc, aCache));
        }
        
        this.postConvert(aDO, theDO, aCache);
        
        return theDO;
    }
    
    /** 
     * This method delegates to the actual provided {@link GenericConverterFunction} object.
     * 
     * @param anObject  The object to map.
     * @param aFuntion  The mapping function to use.
     * @param aCache    The cache of imported wrappers.
     * @return          Returns the mapped object.
     * 
     * @see GenericConverterFunction
     */
    protected Object map(Object anObject, GenericConverterFunction aFuntion, GenericCache aCache) {
        if (StringServices.isEmpty(anObject)) {
            return null;
        }
        return aFuntion.map(String.valueOf(anObject), aCache);
    }
    
    /** 
     * This method is called after the main loop for mapping imported values.
     * It should be overridden to perform further processing.
     * 
     * @param aSource   The source value map.
     * @param aResult   The converted value map.
     * @param aCache    The cache of imported objects.
     */
    protected abstract void postConvert(GenericValueMap aSource, GenericValueMap aResult, GenericCache aCache) throws DataObjectException;
    
    /**
     * The internal type of the objects this converter is responsible for.
     */
    protected final String getType() {
        return this.type;
    }
    
    /**
     * The function used for columns without a configured mapping function.
     */
    protected GenericConverterFunction getDefaultMappingFuncion() {
        return DEFAULT_MAPPING;
    }
    
    /**
     * The {@link WrappedMapping} wraps a single mapping instance and delegates the mapping
     * function to that instance.
     * 
     * @author    <a href=mailto:TEH@top-logic.com>TEH</a>
     */
    public static class WrappedMapping implements GenericConverterFunction {
        final Mapping inner;
        public WrappedMapping(Mapping aMapping) {
            this.inner = aMapping;
        }
        
        @Override
		public Object map(Object aValue, GenericCache aCache) {
            return inner.map(aValue);
        }
    }
}

