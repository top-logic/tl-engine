/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.resources;

import com.top_logic.layout.ResourceView;

/**
 * Simply return the values got from parameters.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SimpleResourceView implements ResourceView {

    public static final ResourceView INSTANCE = new SimpleResourceView();

    /** 
     * Creates a {@link SimpleResourceView}.
     */
    private SimpleResourceView() {
    }

    /**
     * @see com.top_logic.layout.ResourceView#getStringResource(java.lang.String)
     */
    @Override
	public String getStringResource(String aKey) {
        return aKey;
    }

    /**
     * @see com.top_logic.layout.ResourceView#getStringResource(java.lang.String, java.lang.String)
     */
    @Override
	public String getStringResource(String aKey, String aDefault) {
        return aDefault;
    }

    /**
     * @see com.top_logic.layout.ResourceView#hasStringResource(java.lang.String)
     */
    @Override
	public boolean hasStringResource(String aKey) {
        return true;
    }
}

