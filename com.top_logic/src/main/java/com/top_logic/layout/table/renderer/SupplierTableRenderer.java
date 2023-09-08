/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.css.CssUtil;

/**
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class SupplierTableRenderer extends DefaultTableRenderer {

	public SupplierTableRenderer(InstantiationContext context, Config atts) {
		super(context, atts);
	}

	// TODO tbe: make it configurable
    @Override
	public void writeColumn(TagWriter out, DisplayContext context, RenderState state, boolean isSelected, int column,
			int displayedRow, int row, int leftOffset) throws IOException {
		String theColName = state.getView().getApplicationModel().getColumnName(column);
        if (!"volume".equals(theColName)) {
			super.writeColumn(out, context, state, isSelected, column, displayedRow, row, leftOffset);
            return;
        }
        else {
			String theTDClass = state.getTDClass(column, isSelected);
			boolean isSelectable = state.getView().isRowSelectable(row);


            out.beginBeginTag(TD);
			CssUtil.writeCombinedCssClasses(out, theTDClass, "tblRight");
            if (!isSelected && isSelectable) {
                writeOnClick(out, state, row, column);
				out.writeAttribute(STYLE_ATTR, "cursor: pointer; white-space: nowrap; "
					+ HTMLConstants.TEXT_ALIGN_RIGHT);
            }
            else {
				out.writeAttribute(STYLE_ATTR, "white-space: nowrap; " + HTMLConstants.TEXT_ALIGN_RIGHT);
            }
            out.endBeginTag();

			state.getRenderer().writeCellContent(context, out, state, isSelected, column, row);

            out.endTag(TD);
        }
    }

	private void writeOnClick(TagWriter out, RenderState state, int row, int column) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		state.getView().appendSelectAction(out, row, column);
		out.endAttribute();
	}
}

