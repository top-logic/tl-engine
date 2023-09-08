/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * Common base class for {@link XMLTag} implementations providing convenience methods.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractXMLTag implements XMLTag {

	@Override
	public void endEmptyTag(DisplayContext context, TagWriter out) throws IOException {
		endBeginTag(context, out);
		endTag(context, out);
	}

}
