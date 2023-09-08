/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.journal;

import java.util.Date;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.journal.JournalResultEntry;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * @author    <a href=mailto:kha@top-logic.com>tHEO sATTLER</a>
 */
public class JournalRenderHelper {

    public JournalRenderHelper() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public static String encodeAttributeValue(JournalResultEntry aJRE, String anAttributeName, Object aValue) {
        if (aJRE == null) {
            return aValue.toString();
        }
        if (aValue == null) {
            return "";
        }
        if (aValue instanceof Date) {
            return HTMLFormatter.getInstance().formatShortDateTime((Date)aValue);
        }
        if (aValue instanceof String) {
			String theResult = Resources.getInstance().getString(ResKey.legacy((String) aValue), (String) aValue);
            return theResult;
        }
        return aValue.toString();
    }
    
}
