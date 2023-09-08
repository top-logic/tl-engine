/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.UpdateException;
import com.top_logic.element.genericimport.interfaces.GenericUpdateHandler;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class WrapperUpdateHandler extends AbstractGenericDataImportBase implements
        GenericUpdateHandler {

    /**
     * Creates a {@link WrapperUpdateHandler}.
     */
    public WrapperUpdateHandler(Properties aSomeProps) {
        super(aSomeProps);

    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericUpdateHandler#updateBusinessObject(java.lang.Object, GenericValueMap, String)
     */
    @Override
	public Object updateBusinessObject(Object anObject, GenericValueMap aDO, String aFKeyAttr)
            throws UpdateException {
        if (anObject instanceof Wrapper) {
//            GenericDataImportConfiguration theConfig = this.getImportConfiguration();
            try {
                Wrapper theWrap = (Wrapper) anObject;
                String[] theAttrs = aDO.getAttributeNames();
                MetaObject theMeta = theWrap.tTable();
                for (int i=0; i<theAttrs.length; i++) {
                    String theAttr  = theAttrs[i];
                    
                    if (MetaObjectUtils.hasAttribute(theMeta, theAttr)) {
                        Object theValue = aDO.getAttributeValue(theAttr);
                        if (theValue != null) {
                            theWrap.setValue(theAttr, theValue);
                        }
                    }
                    
//                    String theType = theWrap.getMetaObject().getName();
//                    ColumnAttributeMapping theMapping = theConfig.getMappingForAttribute(theType, theAttr);
//                    if (theMapping == null || ! theMapping.isIgnoreExistance()) {
//                    }
                }
                return theWrap;
            } catch (NoSuchAttributeException nsax) {
                throw new UpdateException(nsax);
            }
        }

        throw new UpdateException("Object is not a wrapper!" + aDO);
    }

}

