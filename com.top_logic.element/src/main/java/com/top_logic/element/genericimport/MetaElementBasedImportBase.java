/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Iterator;
import java.util.Properties;

import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.model.TLClass;

/**
 * The MetaElementBasedImportBase is a basic 
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MetaElementBasedImportBase extends AbstractGenericDataImportBase {

    private TLClass metaelement;

    /** 
     * Creates a {@link MetaElementBasedImportBase}.
     */
    public MetaElementBasedImportBase(Properties aSomeProps) {
        super(aSomeProps);
        
        String theType  = aSomeProps.getProperty(GenericDataImportConfiguration.PROP_TYPE);

        TLClass theMeta  = getUniqueMetaElement(theType);
        
        if (theMeta == null) {
            throw new IllegalArgumentException("No meta element found for " + theType);
        }
        
        this.setMetaElement(theMeta);
    }

    protected TLClass getMetaElement() {
        return this.metaelement;
    }
    
    public void setMetaElement(TLClass aMetaElement) {
        this.metaelement = aMetaElement;
    }
    
    public static final TLClass getUniqueMetaElement(String aType) {
        
        if (aType == null) {
            throw new IllegalArgumentException("Meta element type must not be null!");
        }
        
        Iterator    theMetas = MetaElementFactory.getInstance().getAllMetaElements().iterator();
        TLClass theMeta  = null;
        
        while (theMetas.hasNext()) {
            TLClass theM = (TLClass) theMetas.next();
            if (aType.equals(theM.getName())) {
                if (theMeta != null) {
                    throw new IllegalArgumentException("Given meta element type "+ aType  +" is not unique!");
                }
                theMeta = theM;
                break;
            }
        }
        return theMeta;
    }
}

