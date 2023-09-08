/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model.internal;

import javax.xml.XMLConstants;

import com.top_logic.basic.xml.TagWriter;

/**
 * Utilities for simplified usage of {@link TagWriter}.
 * 
 * <ul>
 * <li>There is no need to explicitly call {@link TagWriter#endBeginTag()}. This is automatically
 * called, some content is written to the tag, or it is closed.</li>
 * <li>The tag name is not required for writing the end tag.</li>
 * </ul>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TagOutput {

	/**
	 * Writes a tag with a namespace declaration.
	 * 
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param namespace
	 *        the namespace URI.
	 * @param prefix
	 *        The namespace prefix to use.
	 * @param name
	 *        The tag name to write.
	 */
	public static final void beginTagNS(TagWriter out, String namespace, String prefix, String name) {
		out.beginBeginTag(prefix + ":" + name);
		out.writeAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix, namespace);
	}

	/**
	 * @see TagWriter#endTag(String)
	 */
	public static final void endTag(TagWriter out) {
		out.endTag(out.getStack().peek());
	}

}
