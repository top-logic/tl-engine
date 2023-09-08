/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.util.css.CssUtil;

/**
 * {@link TreeTableRenderer} using custom CSS classes.
 * 
 * @deprecated TODO #21888: Replace with tree table rendering.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@Deprecated
public class ThemeTreeTableRenderer extends TreeTableRenderer {

    public ThemeTreeTableRenderer(TreeImageProvider aTreeImages, TableDeclaration aTableDeclaration) {
        super(aTreeImages, aTableDeclaration);
    }

    /** 
     * Creates a {@link ThemeTreeTableRenderer}.
     */
    public ThemeTreeTableRenderer(TableDeclaration aTableDeclaration, TreeContentRenderer aContentRenderer) {
        super(aTableDeclaration, aContentRenderer);
    }

	@Override
	protected void writeHeaderAttributes(TagWriter out, ColumnDeclaration column) throws IOException {
		writeHeaderClasses(out);
		CssUtil.writeCombinedStyle(out, column.getWidthStyle(), column.getHeaderStyle());
	}

	/**
     * Returns the classes which are used as css class for the {@link HTMLConstants#TH} element of the header of the table
     */
	protected final void writeHeaderClasses(TagWriter out) throws IOException {
		out.beginAttribute(CLASS_ATTR);
		writeHeaderClassesContent(out);
		out.endAttribute();
	}

	protected void writeHeaderClassesContent(TagWriter out) throws IOException {
		out.append("treeTableTh");
	}

	@Deprecated
    public static class ThemeTableRowRenderer extends TableRowRenderer {

        public ThemeTableRowRenderer(TreeImageProvider treeImages, TableDeclaration tableDeclaration) {
            super(treeImages, tableDeclaration);
        }

		@Override
		protected void writeColumnAttributes(TagWriter out, NodeContext nodeContext, ColumnDeclaration column) throws IOException {
			super.writeColumnAttributes(out, nodeContext, column);
			writeColumnClassAttribute(out, nodeContext, column);
		}

		/**
		 * Writes the {@link HTMLConstants#CLASS_ATTR} attribute for the given column.
		 */
		protected void writeColumnClassAttribute(TagWriter out, NodeContext nodeContext, ColumnDeclaration column)
				throws IOException {
			out.beginCssClasses();
			if (column.getColumnType() == ColumnDeclaration.DEFAULT_COLUMN) {
				out.append("treeTableTdFirst");
			} else {
				out.append("treeTableTd");
			}

			Object node = nodeContext.currentNode();
			SelectionModel selectionModel = nodeContext.getTree().getSelectionModel();
			if (selectionModel.isSelected(node)) {
				out.append(DefaultTableRenderer.TABLE_CELL_SELECTED_CSS_CLASS);
			}

			if (selectionModel.isSelectable(node) && column.isSelectable()) {
				out.append(TreeControl.SELECTABLE_ROW_NODE_CSS);
			} else {
				out.append(TreeControl.UNSELECTABLE_NODE_CSS);
			}
			out.endCssClasses();
		}

    }

}

