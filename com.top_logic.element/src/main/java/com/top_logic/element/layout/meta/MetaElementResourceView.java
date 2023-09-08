/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Resources;

/**
 * Create resource names out of a defined meta element.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MetaElementResourceView implements ResourceView {

    /** The meta element to be used for generating string resources. */
    private TLClass me;

    /** Fall back for columns, which are no meta attributes (to be used for generating string resources). */
	private final ResPrefix resPrefix;

    /** Internal cache for attribute name to qualified resource name mapping. */
	private final Map<String, ResKey> cache;

    /** 
     * Creates a {@link MetaElementResourceView}.
     * 
     * @param    aME    The meta element to generate the resources for, must not be <code>null</code>.
     */
    public MetaElementResourceView(TLClass aME) {
        this(aME, null);
    }

    /** 
     * Creates a {@link MetaElementResourceView}.
     * 
     * @param    aME           The meta element to generate the resources for, must not be <code>null</code>.
     * @param    aResPrefix    A fall back for columns, which are no meta attribute of the held meta element, may be <code>null</code>.
     */
	public MetaElementResourceView(TLClass aME, ResPrefix aResPrefix) {
		assert aME != null : "Type must not be null";
    	
        this.me        = aME;
        this.resPrefix = aResPrefix;
        this.cache     = new HashMap();
    }

    /**
     * @see com.top_logic.layout.ResourceView#getStringResource(java.lang.String)
     */
    @Override
	public String getStringResource(String aKey) {
        return Resources.getInstance().getString(this.getQualifiedResourceName(aKey));
    }

    /**
     * @see com.top_logic.layout.ResourceView#getStringResource(java.lang.String, java.lang.String)
     */
    @Override
	public String getStringResource(String aKey, String aDefault) {
        return Resources.getInstance().getString(this.getQualifiedResourceName(aKey), aDefault);
    }

    /**
     * @see com.top_logic.layout.ResourceView#hasStringResource(java.lang.String)
     */
    @Override
	public boolean hasStringResource(String aKey) {
        return Resources.getInstance().getString(this.getQualifiedResourceName(aKey), null) != null;
    }

    /** 
     * Create the resource name out of the meta element and the given attribute name.
     * 
     * @param    aKey    The requested meta attribute name, may be <code>null</code>.
     * @return   The requested resource name.
     */
	private ResKey getQualifiedResourceName(String aKey) {
		ResKey theResult = this.cache.get(aKey);

        if (theResult == null) {
            try {
				TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(this.me, aKey);
                TLClass   theME = AttributeOperations.getMetaElement(theMA);

				theResult = ResPrefix.legacyString(theME.getName()).key(theMA.getName());
            }
            catch (NoSuchAttributeException ex) {
				theResult = (this.resPrefix != null) ? this.resPrefix.key(aKey) : ResPrefix.GLOBAL.key(aKey);
            }
            
            this.cache.put(aKey, theResult);
        }

        return theResult;
    }
}

