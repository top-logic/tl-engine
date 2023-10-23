/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link CellRenderer} that displays a {@link WebFolder} just by the
 * {@link WebFolderTableUtil#getWebFolderCount(Object) number} of entries.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderAttributeRenderer extends AbstractCellRenderer {

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		Object value = cell.getValue();
		int size = WebFolderTableUtil.getWebFolderCount(value);
		writeCount(size, out);
	}

	/** Writes the size centered in a new HTML tag. */
	public static void writeCount(int size, TagWriter out) {
		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.ALIGN_ATTR, HTMLConstants.CENTER_VALUE);
		out.endBeginTag();
		out.writeText(Integer.toString(size));
		out.endTag(HTMLConstants.DIV);
	}

}
