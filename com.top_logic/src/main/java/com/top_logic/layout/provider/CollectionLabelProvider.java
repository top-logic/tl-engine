/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import java.util.Collection;
import java.util.Iterator;

import com.top_logic.layout.LabelProvider;

/**
 * Display the content of a collection using the {@link LabelProvider inner provider}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CollectionLabelProvider implements LabelProvider {

    /** The label provice for the objects in the collection. */
    private final LabelProvider inner;

    /** The separator to be displayed between two objects. */
    private final String separator;

    /** 
     * This constructor creates a simple collection provider.
     * 
     * It'll use the {@link MetaResourceProvider} to get the inner label
     * provider and is using ", " as a separator.
     */
    public CollectionLabelProvider() {
        this(MetaResourceProvider.INSTANCE, ", ");
    }
    
    /** 
     * Creates a {@link CollectionLabelProvider}.
     * 
     * @param    anInner       The inner label provider for writing the object 
     *                         in the collection, must not be <code>null</code>.
     * @param    aSeparator    The separator to divide the objects in the 
     *                         collection, must not be <code>null</code>.
     */
    public CollectionLabelProvider(LabelProvider anInner, String aSeparator) {
        this.inner     = anInner;
        this.separator = aSeparator;
    }

    /**
     * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object anObject) {
        Collection   theColl        = (Collection) anObject;
        boolean      writeSeparator = false;
        StringBuffer theResult      = new StringBuffer();

        if (theColl != null) {
            for (Iterator theIt = theColl.iterator(); theIt.hasNext();) {
                Object theValue = theIt.next();
    
                if (writeSeparator) {
                    theResult.append(this.separator);
                }
                else {
                    writeSeparator = true;
                }
    
                theResult.append(this.inner.getLabel(theValue));
            }
        }

        return (theResult.toString());
    }
}

