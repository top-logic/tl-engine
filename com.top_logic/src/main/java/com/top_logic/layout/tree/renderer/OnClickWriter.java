/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * Callback for writing the per-node <code>onclick</code> JavaScript.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface OnClickWriter {

	/**
	 * {@link OnClickWriter} that writes no action.
	 */
	OnClickWriter NONE = new OnClickWriter() {
		@Override
		public void writeOnClickSelect(TagWriter out, Object node) throws IOException {
			// No action.
		}
	};

	/**
	 * Create the <code>onclick</code> attribute with appropriate contents.
	 * 
	 * @param node
	 *        The current node object.
	 */
	void writeOnClickSelect(TagWriter out, Object node) throws IOException;

}
