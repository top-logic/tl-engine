/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.ColumnAttributeMapping;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.UpdateException;
import com.top_logic.element.genericimport.interfaces.GenericUpdateHandler;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * The MetaElementBasedUpdateHandler is used to update a business object with a
 * map of values during import.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MetaElementBasedUpdateHandler extends MetaElementBasedImportBase implements
        GenericUpdateHandler {

    /** 
     * Creates a {@link MetaElementBasedUpdateHandler}.
     */
    public MetaElementBasedUpdateHandler(Properties aSomeProps) {
        super(aSomeProps);
    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericUpdateHandler#updateBusinessObject(java.lang.Object, GenericValueMap, String)
     */
    @Override
	public Object updateBusinessObject(Object anObject, GenericValueMap aDO, String aFKeyAttr)
            throws UpdateException {
        
		if (!(anObject instanceof TLObject)) {
			throw new UpdateException("Object " + anObject.toString() + " not instance of TLObject");
        }
        
		TLObject theAttributed = (TLObject) anObject;
		if (!this.checkMetaElement(theAttributed.tType())) {
            throw new UpdateException("the meta element of " + theAttributed + " does not match " + this.getMetaElement());
        }
        
        return updateAttributes(theAttributed, aDO, aFKeyAttr);
    }
    
	public Object updateAttributes(TLObject anAttribted, GenericValueMap aDO, String aForeignKey)
			throws UpdateException {
        try {
			TLStructuredType type = anAttribted.tType();
            String[]    theColumns = aDO.getAttributeNames();
            GenericDataImportConfiguration configuration = getImportConfiguration();
            
            for (int i=0; i<theColumns.length; i++) {
                String theColumn = theColumns[i];
                String theAttr   = theColumn;
				ColumnAttributeMapping theMapping = configuration.getMappingForAttribute(type.getName(), theColumn);
                
                if (theMapping != null) {
                    theAttr = theMapping.getAttributeName();
                }
				TLClassPart attribute = (TLClassPart) type.getPart(theAttr);
				if (attribute != null) {
					if (!attribute.isDerived()) {
						Object theValue = aDO.getAttributeValue(theColumn);
						if (theColumn.equals(aForeignKey)) {
							theValue = removeTypePrefix(theValue);
						}
						this.setValue(anAttribted, theAttr, theValue);
					}
                }
            }
            return anAttribted;
        } catch (NoSuchAttributeException nsax) {
            throw new UpdateException(nsax);
        }
    }
    
	protected void setValue(TLObject attributed, String attribute, Object newValue) {
		attributed.tUpdateByName(attribute, newValue);
    }

    /** 
     * This method removes the element type from the key before it is set
     * to the wrapper. This avoids key values being written out as "ref_ID".
     */
    private static Object removeTypePrefix(Object aValue) {
        if (aValue instanceof String) {
            String theStrValue = (String) aValue;
            int theIdx = theStrValue.indexOf('_');
            if (theIdx > 0) {
                theStrValue = theStrValue.substring(theIdx + 1);
            }
            aValue = theStrValue;
        }
        return aValue;
    }

	private boolean checkMetaElement(TLStructuredType type) {
		return MetaElementUtil.hasGeneralization(type, this.getMetaElement());
    }
}

