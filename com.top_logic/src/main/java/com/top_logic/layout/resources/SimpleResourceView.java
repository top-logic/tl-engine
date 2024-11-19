/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.resources;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResourceView;

/**
 * Simply return the values got from parameters.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G�nsler</a>
 */
public class SimpleResourceView implements ResourceView {

    public static final ResourceView INSTANCE = new SimpleResourceView();

    /** 
     * Creates a {@link SimpleResourceView}.
     */
    private SimpleResourceView() {
    }

    @Override
	public ResKey getStringResource(String aKey) {
		return ResKey.text(aKey);
    }

    @Override
	public ResKey getStringResource(String aKey, ResKey aDefault) {
        return aDefault;
    }

    @Override
	public boolean hasStringResource(String aKey) {
        return true;
    }
}

