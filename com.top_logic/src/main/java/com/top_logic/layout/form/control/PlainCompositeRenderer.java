/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;

/**
 * Renderer that displays the {@link View}s of a {@link CompositeControl} in a
 * constant layout given as format string with placeholder strings, where the
 * {@link View}s are inserted.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PlainCompositeRenderer extends AbstractSimpleCompositeControlRenderer {

	/**
	 * The place-holder string that marks the locations in the {@link #format},
	 * where the {@link View}s are inserted.
	 */
	public static final String PLACEHOLDER = "<%+%>";
	
	/**
	 * @see #getFormat()
	 */
	private final String format;

	/**
	 * Creates a new renderer with a given format. 
	 * 
	 * @param localName see {@link AbstractSimpleCompositeControlRenderer#getLocalName()}
	 * @param format see {@link #getFormat()}.
	 */
	public PlainCompositeRenderer(String localName, Map<String, String> attributes, String format) {
		super(localName, attributes);
		this.format = format;
	}
	
	/**
	 * The format consisting of a (well-formed) HTML fragment with interleaving
	 * {@link #PLACEHOLDER} strings, where the {@link CompositeControl}s views
	 * are inserted.
	 */
	public String getFormat() {
		return format;
	}

	@Override
	public void writeChildren(DisplayContext context, TagWriter out, List<? extends HTMLFragment> views) throws IOException {
		int contentStart = 0;
		for (int viewIndex = 0, cnt = views.size(); viewIndex < cnt; viewIndex++) {
			int nextSeparatorIndex = format.indexOf(PLACEHOLDER, contentStart);
			if (nextSeparatorIndex >= 0) {
				out.writeContent(format.substring(contentStart, nextSeparatorIndex));
				contentStart = nextSeparatorIndex + PLACEHOLDER.length();
			} else {
				contentStart = format.length();
			}
			views.get(viewIndex).write(context, out);
		}
		out.writeContent(format.substring(contentStart));
	}

}
