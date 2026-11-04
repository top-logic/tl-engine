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
 * Utilities for rendering values of journal entries.
 * 
 * @author    <a href=mailto:kha@top-logic.com>tHEO sATTLER</a>
 */
public class JournalRenderHelper {

    public JournalRenderHelper() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Converts the given attribute value of a journal entry to a display string.
     * 
     * @param aJRE
     *        The journal entry the value belongs to.
     * @param anAttributeName
     *        The name of the attribute (currently not used).
     * @param aValue
     *        The value to convert.
     * @return The display string for the value. Dates are formatted, strings are interpreted as
     *         resource keys.
     */
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
