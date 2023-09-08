/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.gui.I18NConstants;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.Document;

/**
 * Providing the correct type definitions and tool tips for documents.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DocumentResourceProvider extends WrapperResourceProvider {

    @Override
    public String getType(Object anObject) {
        return MimeTypes.getInstance().getMimeType(((Document) anObject).getName());
    }

    @Override
	protected ResKey getTooltipNonNull(Object object) {
		String theName = ((Document) object).getName();
        MimeTypes theTypes = MimeTypes.getInstance();
        String    theType  = theTypes.getMimeType(theName);

		return I18NConstants.WRAPPER_TOOLTIP.fill(
			quote(theName),
			quote(theTypes.getDescription(theType)));
    }
}

