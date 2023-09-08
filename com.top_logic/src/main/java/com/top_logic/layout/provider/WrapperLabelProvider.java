/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.LabelProvider;

/**
 * Provide lables from Wrappers via theire name.
 * 
 * @deprecated Use {@link WrapperResourceProvider}. Besides that this class is
 *             in the wrong package (should be somewhere below
 *             <code>com.top_logic.knowledge</code>.
 * 
 * @author <a href=mailto:kha@top-logic.com>kha</a>
 */
@Deprecated
public class WrapperLabelProvider implements LabelProvider {

    public static WrapperLabelProvider INSTANCE = new WrapperLabelProvider();

    protected WrapperLabelProvider() {
        /* must be protected for singleton */
    }

    /** 
     * Fetch Name of Wrapper.
     * 
     * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object anObject) {
        if (anObject != null){
            return ((Wrapper) anObject).getName();
        }
        return "";
    }
}
