/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.model.v5.transform.ModelLayout;
import com.top_logic.util.Resources;

/**
 * I18N for element states (eg for project states).
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ElementStateResourceProvider extends DefaultResourceProvider {

    public static final ElementStateResourceProvider I18N_INSTANCE = new ElementStateResourceProvider();

    /** 
     * Return the I18N name of an element state.
     * 
     * If there is no I18N, the normal name of the element state will be returned.
     * 
     * @param    anObject    The element state to get the I18N for.
     * @return   The I18N version of the given element state.
     * @see      com.top_logic.mig.html.DefaultResourceProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object anObject) {
        if (anObject instanceof ElementState) {
            ElementState theState = (ElementState) anObject;

			// Note: What follows is a hack for compatibility with legacy quirks-design (storing
			// local classification names in .
			String idioticString = theState.getKey();
			String absurdString = "model." + ModelLayout.TL5_ENUM_MODULE + '.'
				+ idioticString.substring(0, idioticString.lastIndexOf('.')) + '.' + idioticString;
			return (Resources.getInstance().getString(ResKey.legacy(absurdString), idioticString));
        }
        else {
            return super.getLabel(anObject);
        }
    }
}
