/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.mig.html.HTMLConstants;

/**
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderAttributeRenderer extends AbstractCellRenderer {

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		Object theValue = cell.getValue();
		Integer theSize = WebFolderTableUtil.getWebFolderCount(theValue);
		TagWriter theOut = out;

		theOut.beginBeginTag(HTMLConstants.DIV);
		theOut.writeAttribute(HTMLConstants.ALIGN_ATTR, HTMLConstants.CENTER_VALUE);
		theOut.endBeginTag();
		theOut.writeText(Integer.toString(theSize));
		theOut.endTag(HTMLConstants.DIV);
	}
}