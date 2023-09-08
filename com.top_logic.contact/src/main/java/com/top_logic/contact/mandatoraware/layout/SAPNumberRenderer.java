/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import com.top_logic.contact.mandatoraware.SAPFormatHelper;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.table.renderer.ObjectRightAlignedRenderer;

/**
 * Render a SAP number to the writer.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SAPNumberRenderer extends ObjectRightAlignedRenderer {

    /** 
     * Creates a {@link SAPNumberRenderer}.
     */
    public SAPNumberRenderer() {
        super(DefaultLabelProvider.INSTANCE);
    }

    /**
     * @see com.top_logic.layout.table.renderer.ObjectRightAlignedRenderer#getText(java.lang.Object)
     */
    @Override
	protected String getText(Object aValue) {
        return (SAPFormatHelper.stripSAPNo((String) aValue));
    }
}

