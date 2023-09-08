/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.converterfunction;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.element.genericimport.GenericDataImportConfiguration;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.ColumnAttributeMapping;
import com.top_logic.element.genericimport.interfaces.GenericCache;

/**
 * The ResolveReferenceListMapping performs the foreign key resolution for
 * lists of references.
 * 
 * @author    <a href=mailto:TEH@top-logic.com>TEH</a>
 */
public class ResolveReferenceListMapping extends ResolveReferenceMapping {
    
    private static final String LIST_SEPARATOR = ",";

    public ResolveReferenceListMapping(GenericDataImportConfiguration config, ColumnAttributeMapping aMapping) {
        super(config, aMapping);
    }
    
    /**
	 * Returns a List of resolved references. Note that since types must for now be encoded
	 *         in the reference this will also work for untyped references.
	 * @see ResolveReferenceMapping#map(java.lang.Object,
	 *      com.top_logic.element.genericimport.interfaces.GenericCache)
	 */
    @Override
	public Object map(Object aInput, GenericCache aCache) {
        if (! StringServices.isEmpty(aInput)) {
            String[] theRefs = this.extractList((String) aInput);
            List theResult = new ArrayList(theRefs.length);
            for (int i=0; i<theRefs.length; i++) {
                theResult.add(super.map(theRefs[i], aCache));
            }
            return theResult;
        }
        return null;
    }
    
    /** 
     * This method converts a String with zero or more references separated by
     * {@link ResolveReferenceListMapping#LIST_SEPARATOR} into an
     * array of such references.
     */
    private String[] extractList(String someReferences) {
        return someReferences.split(LIST_SEPARATOR);
    }
}
