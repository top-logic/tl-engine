/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.converterfunction;

import com.top_logic.basic.StringServices;
import com.top_logic.element.genericimport.GenericDataImportConfiguration;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.ColumnAttributeMapping;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericConverterFunction;

/**
 * The ResolveReferenceMapping is a mapping function to resolve single references.
 *
 * It will use the referenceType of {@link ColumnAttributeMapping} to resolve types, or if null,
 * assumes the string value to map to be of the following format:<br />
 * type + "_" + an integer.
 *
 * @author <a href=mailto:TEH@top-logic.com>TEH</a>
 */
public class ResolveReferenceMapping implements GenericConverterFunction {

    private final GenericDataImportConfiguration config;
    private final ColumnAttributeMapping mapping;

    public ResolveReferenceMapping(GenericDataImportConfiguration config, ColumnAttributeMapping aMapping) {
        this.config  = config;
        this.mapping = aMapping;
    }

    protected GenericDataImportConfiguration getConfig() {
        return (this.config);
    }

    @Override
	public Object map(Object aInput, GenericCache aCache) {
        if (! StringServices.isEmpty(aInput)) {
            String theRef = (String) aInput;

            return aCache.get(extractType(theRef), extractId(theRef));
        }
        return null;
    }

    protected String extractType(String aReference) {
        String theElementName = null;

        // first check for a configured reference target
        if (this.mapping != null) {
            theElementName = this.mapping.getReferenceTarget();
            return theElementName;
        }

        // try to derive the type of the reference from the key itself
        if (theElementName == null) {
            int theIdx = aReference.indexOf('_');
            if (theIdx > 0) {
                theElementName = aReference.substring(0, theIdx);
            }
            else {
                throw new IllegalArgumentException("Reference value in column is expected to be of format: ReferenceType_ID");
            }
        }
        return config.getTypeResolver().resolveType(theElementName);

    }

    protected String extractId(String aReference) {
        return aReference;
    }
}
