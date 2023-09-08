/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.util.Resources;

/**
 * A Renderer that will show translates Strings (with encode parameters).
 * 
 * @see  Resources#decodeMessageFromKeyWithEncodedArguments(String)
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MessageRenderer implements Renderer<String> {

    public static final MessageRenderer INSTANCE = new MessageRenderer();

    @Override
	public void write(DisplayContext aContext, TagWriter anOut, String aValue) throws IOException {
		if (aValue != null) { // includes check for null
            String theMessage = Resources.getInstance().decodeMessageFromKeyWithEncodedArguments((String) aValue);
            // Encoding is required again because of changes in AttributeConverter.java on Ticket #4195
            anOut.writeContent(TagUtil.encodeXML(theMessage));
        }
    }
}
