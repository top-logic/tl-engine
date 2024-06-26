/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;



/**
 * This Extension of TreeTableRenderer takes care that the DefaultColumn is not wrapped
 * and allows the use of CSSColumnDeclaration.
 * 
 * @deprecated TODO #21888: Replace with tree table rendering.
 * 
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
@Deprecated
public class ExtendedTreeTableRenderer extends TreeTableRenderer {

	public ExtendedTreeTableRenderer(TreeImageProvider treeImages, TableDeclaration tableDeclaration) {
        super(tableDeclaration, new MyTableRowRenderer(treeImages, tableDeclaration));
    }

    public ExtendedTreeTableRenderer(TableDeclaration tableDeclaration, TreeContentRenderer aContentRenderer) {
        super(tableDeclaration, aContentRenderer);
    }

	@Override
    protected void writeHeader(DisplayContext context, TagWriter out) throws IOException {
    List columnNames = getTableDeclaration().getColumnNames();
    
    out.beginTag(TR);
    Resources res = Resources.getInstance();
    for (int n = 0, cnt = columnNames.size(); n < cnt; n++) {
        
        
        String            columnName = (String) columnNames.get(n);
        ColumnDeclaration column     = getTableDeclaration().getColumnDeclaration(columnName);
    
        String headerCssClass;
        if (column instanceof CSSColumnDeclaration) {
            headerCssClass = ((CSSColumnDeclaration)column).getHeaderCssClass();
        } else {
        	headerCssClass = null;
        }
        
		out.beginBeginTag(TH);
		CssUtil.writeCombinedStyle(out, column.getWidthStyle(), column.getHeaderStyle());
		out.writeAttribute(CLASS_ATTR, headerCssClass);
        out.endBeginTag();
    
        switch (column.getHeaderType()) {
            case ColumnDeclaration.NO_HEADER:
                break;
    
            case ColumnDeclaration.DEFAULT_HEADER:
                out.writeText(res.getString(getTableDeclaration().getResourcePrefix().key(columnName)));
                break;
    
            case ColumnDeclaration.STRING_HEADER:
                out.writeText(res.getString(column.getHeaderKey()));
                break;
    
            case ColumnDeclaration.HTML_HEADER:
                out.writeContent(res.getString(column.getHeaderKey()));
                break;
    
            case ColumnDeclaration.RENDERED_HEADER:
                column.getHeaderRenderer().write(context, out, columnName);
                break;
    
            // TODO: Implement template headers.
    
            default:
                throw new UnsupportedOperationException("Header type " + column.getHeaderType() + " not supported.");
        }
    
        out.endTag(TH);
    }
    
    out.endTag(TR);
    }

    public static class MyTableRowRenderer extends AbstractTreeContentRenderer {

        private final TableDeclaration tableDeclaration;
        private final TreeImageProvider treeImages;

        public MyTableRowRenderer(TreeImageProvider treeImages, TableDeclaration tableDeclaration) {
            this.treeImages = treeImages;
            this.tableDeclaration = tableDeclaration;
        }

    	@Override
		public ResourceProvider getResourceProvider() {
            return tableDeclaration.getResourceProvider();
        }

    	@Override
        protected TreeImageProvider getTreeImages() {
            return treeImages;
        }

        protected final TableDeclaration getTableDeclaration() {
            return this.tableDeclaration;
        }

		/**
		 * Return a css class applied on each TD in a table row
		 */
        protected String getCssClass(ColumnDeclaration aDeclaration, NodeContext nodeContext) {
            if (aDeclaration instanceof CSSColumnDeclaration) {
                return ((CSSColumnDeclaration)aDeclaration).getCssClass();
            }
            return null;
        }
        
    	@Override
        public void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException {
            List columnNames = tableDeclaration.getColumnNames();
            Accessor accessor = tableDeclaration.getAccessor();

            Object node = nodeContext.currentNode();

            TreeControl tree = nodeContext.getTree();
            Object userObject = tree.getModel().getUserObject(node);


            for (int n = 0, cnt = columnNames.size(); n < cnt; n++) {
                String            columnName = (String) columnNames.get(n);
                ColumnDeclaration column     = tableDeclaration.getColumnDeclaration(columnName);

                writer.beginBeginTag(TD);
				CssUtil.writeCombinedStyle(writer, column.getWidthStyle(), column.getStyle());
				writer.writeAttribute(CLASS_ATTR, this.getCssClass(column, nodeContext));
				writer.endBeginTag();
                
                {

                    switch (column.getColumnType()) {
                        case ColumnDeclaration.DEFAULT_COLUMN: {
							Renderer<Object> renderer = column.getRenderer();
                            writer.beginTag("nobr");
                            writeNodeDecoration(context, writer, nodeContext);
                            if (renderer != null) {
                                renderer.write(context, writer, nodeContext.currentNode());
                            }
                            else {
                                writeTypeImage(context, writer, nodeContext);
								writeNodeText(context, writer, nodeContext);
                            }
                            writer.endTag("nobr");
                            break;
                        }
                        case ColumnDeclaration.TEMPLATE_COLUMN: {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        }
                        case ColumnDeclaration.RENDERED_COLUMN: {
							Renderer<Object> renderer = column.getRenderer();
                            Object columnValue = accessor.getValue(userObject, columnName);
                            renderer.write(context, writer, columnValue);
                            break;
                        }
                        case ColumnDeclaration.CONTROL_COLUMN: {
                            ControlProvider controlProvider = column.getControlProvider();
                            Object columnValue = accessor.getValue(userObject, columnName);

//                            ControlScope renderingScope = context.getRenderingScope();
							Control columnControl = controlProvider.createControl(columnValue);
                            if (columnControl != null) {
                                columnControl.write(context, writer);
                            } else {
                                // The ControlProvider may have valid reasons suppress a control ...
                                // Logger.warn("Failed to createControl for Column '" + columnName + "'", this);
                            }
                            break;
                        }
                    }
                }
                writer.endTag(TD);
            }
        }
    }

	@Override
    protected String getNodeTag() {
        return TR;
    }

}
