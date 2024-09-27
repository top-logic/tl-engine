/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.renderer.DefaultRowClassProvider;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;


/**
 * {@link TreeRenderer} for displaying a {@link TreeControl} as table.
 *
 * @deprecated TODO #21888: Replace with tree table rendering.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class TreeTableRenderer extends LegacyTreeRenderer {

	private final TableDeclaration tableDeclaration;

	private final TreeContentRenderer contentRenderer;


    public TreeTableRenderer(TreeImageProvider treeImages, TableDeclaration tableDeclaration) {
        this(tableDeclaration, new TableRowRenderer(treeImages, tableDeclaration));
    }

    public TreeTableRenderer(TableDeclaration tableDeclaration, TreeContentRenderer aContentRenderer) {
        this.tableDeclaration = tableDeclaration;
        this.contentRenderer  = aContentRenderer;
    }

	@Override
	protected String getControlTag(TreeControl control) {
		return TABLE;
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, TreeControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);
		writeOnClick(out, control);
		writeIECompatibilityAttributesIfNeeded(context, out);
	}

	/**
	 * Has been introduced with #5663. When using border collapsing style "separate" IE6 and IE7
	 * apply cell spacing and and cell padding to table cells. This cannot be configured via CSS,
	 * because these IE-Versions do not know CSS-attribute "border-spacing". Therefore using
	 * xhtml-attributes. If support of IE6 and IE7 runs out, this code can be removed.
	 */
	public void writeIECompatibilityAttributesIfNeeded(DisplayContext context, TagWriter out) {
		if (isIE6orIE7(context)) {
			out.writeAttribute(CELLSPACING_ATTR, "0");
			out.writeAttribute(CELLPADDING_ATTR, "0");
		}
	}

	/**
	 * Returns true if the underlying user agent is the Internet Explorer 6 or 7.
	 */
	public boolean isIE6orIE7(DisplayContext context) {
		return context.getUserAgent().is_ie() && !context.getUserAgent().is_ie8up();
	}

	private void writeOnClick(TagWriter out, TreeControl treeControl) throws IOException {
		if (treeControl.useLegacyDrag()) {
			out.beginAttribute(ONMOUSEDOWN_ATTR);
			out.append("return services.form.TreeControl.handleOnMouseDown(event, this);");
			out.endAttribute();
		} else {
			out.beginAttribute(ONCLICK_ATTR);
			out.append("return services.form.TreeControl.handleOnClick(event, this);");
			out.endAttribute();
		}
	}

	public final TableDeclaration getTableDeclaration() {
	    return this.tableDeclaration;
	}

	/**
	 * Overridden to care about Table header.
	 */
	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, TreeControl control) throws IOException {
		final TreeControl tree = control;
		writeTitle(context, out, tree);
		if (tableDeclaration.hasHeader()) {
			writeHeader(context, out);
		}
		writeTreeContents(context, out, new NodeContext(tree));
	}

	protected void writeHeader(DisplayContext context, TagWriter out) throws IOException {
        List<String> columnNames = getHeaderNames(tableDeclaration);

        out.beginBeginTag(TR);
        writeHeaderRowAttributes(context, out);
        out.endBeginTag();
        Resources res = Resources.getInstance();
        final int numberColumns = columnNames.size();
		for (int n = 0, cnt = numberColumns; n < cnt; n++) {
        	String            columnName = columnNames.get(n);
        	ColumnDeclaration column     = tableDeclaration.getColumnDeclaration(columnName);

        	out.beginBeginTag(TH);
        	writeHeaderAttributes(out, column);
        	out.endBeginTag();

        	switch (column.getHeaderType()) {
        		case ColumnDeclaration.NO_HEADER:
        			break;

        		case ColumnDeclaration.DEFAULT_HEADER:
					out.writeText(res.getString(tableDeclaration.getResourcePrefix().key(columnName)));
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
		if (numberColumns == 0) {
			out.beginTag(TH);
			out.endTag(TH);
		}

        out.endTag(TR);
    }

	protected void writeHeaderAttributes(TagWriter out, ColumnDeclaration column) throws IOException {
		CssUtil.writeCombinedStyle(out, column.getWidthStyle(), column.getHeaderStyle());
	}

	/**
	 * Writes the attributes for the {@link HTMLConstants#TR} element of the
	 * header of the table.
	 * 
	 * @param context
	 *        the {@link DisplayContext} in which rendering occurs
	 * @param out
	 *        the writer to write attributes to
	 */
    protected void writeHeaderRowAttributes(DisplayContext context, TagWriter out) throws IOException {
    	// no special attributes here
	}

	protected List<String> getHeaderNames(TableDeclaration declaration) {
		return declaration.getColumnNames();
	}

	@Override
	protected void writeNodeClassesContent(TagWriter out, NodeContext nodeContext) throws IOException {
		super.writeNodeClassesContent(out, nodeContext);

		final boolean isSelected = nodeContext.getTree().getSelectionModel().isSelected(nodeContext.currentNode());
		if (isSelected) {
			out.append(DefaultRowClassProvider.TR_SELECTED_CSS_CLASS);
		}
	}

	@Deprecated
	public static class TableRowRenderer extends LegacyTreeContentRenderer {

	    private final TableDeclaration tableDeclaration;
	    private final TreeImageProvider treeImages;

		public TableRowRenderer(TreeImageProvider treeImages, TableDeclaration tableDeclaration) {
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

		@Override
		public void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException {
			List<String> columnNames = getColumnNames(tableDeclaration, nodeContext);
			Accessor accessor = tableDeclaration.getAccessor();

			Object node = nodeContext.currentNode();

			TreeControl tree = nodeContext.getTree();
			Object userObject = tree.getModel().getUserObject(node);


			final int numberColumns = columnNames.size();
			for (int n = 0, cnt = numberColumns; n < cnt; n++) {
			    String            columnName = columnNames.get(n);
			    ColumnDeclaration column     = tableDeclaration.getColumnDeclaration(columnName);
			    writer.beginBeginTag(TD);
	            writeColumnAttributes(writer, nodeContext, column);
				writeCellStyle(writer, column);

                writer.endBeginTag();
				{
		            // IE seems to ignore inherited style attributes if they are set for a surrounding TD. As a fix
		            // we add an additional DIV inside every TD to ensure that defined styles for a TD are actually 
		            // used.
				    writer.beginBeginTag(DIV);
					writeCellStyle(writer, column);
				    writer.endBeginTag();
		            {
		                writer.beginBeginTag(DIV);
		                writer.endBeginTag();
		                {
		                    switch (column.getColumnType()) {
		                        case ColumnDeclaration.DEFAULT_COLUMN: {
		                            writeTreeColumn(context, writer, nodeContext, column);
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

//		                          ControlScope renderingScope = context.getRenderingScope();
		                            Control columnControl = controlProvider.createControl(columnValue, column.getStyle());
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
		                writer.endTag(DIV);
		            }
		            writer.endTag(DIV);
				}
				writer.endTag(TD);
			}
			if (numberColumns == 0) {
				writer.beginTag(TD);
				writer.endTag(TD);
			}

		}

		/**
		 * Writes the &quot;tree column&quot;, i.e. the column which looks like a tree.
		 * 
		 * @param context
		 *            {@link DisplayContext} in which rendering occurs
		 * @param out
		 *            writer to write content to
		 * @param nodeContext
		 *            context to get information of the node currently written (actually given by
		 *            {@link NodeContext#currentNode()})
		 * @param column
		 *            declaration of the column. used to get configured informations about the cell,
		 *            e.g. {@link ColumnDeclaration#getRenderer() the renderer} for the content
		 */
		protected void writeTreeColumn(DisplayContext context, TagWriter out, NodeContext nodeContext, ColumnDeclaration column)
				throws IOException {
			Renderer<Object> renderer = column.getRenderer();
			writeNodeDecoration(context, out, nodeContext);
			writeTextSeparator(out);
			if (renderer != null) {
			    renderer.write(context, out, nodeContext.currentNode());
			}
			else {
			    writeBusinessNodeContent(context, out, nodeContext);
			}
		}

		protected void writeBusinessNodeContent(DisplayContext context, TagWriter out, NodeContext nodeContext) throws IOException {
		    writeTypeImage(context, out, nodeContext);
			writeNodeText(context, out, nodeContext);
		}

		@Override
		protected void writeNodeText(DisplayContext context, TagWriter writer, NodeContext nodeContext)
				throws IOException {
			Object node = nodeContext.currentNode();
			TreeControl tree = nodeContext.getTree();

			boolean canSelect = tree.getSelectionModel().isSelectable(node);

			/* The width of columns of tree tables can not be manipulated by the user. If the text
			 * of a node is very large it will be cut by the browser. If the node is rendered with
			 * an anchor text, the browser scrolls the cell such that the <b>end</b> of the text is
			 * visible. This is unexpected. As the user can not wide the column, he has no chance to
			 * made the start of the text, or the expansion image visible. Therefore in tree tables
			 * no anchor should be written (See #5857, #14874). */
			String selectableNodeTag = SPAN;
			ResKey tooltip = ResKey.text(getResourceProvider().getTooltip(node));
			writeTextLinkStart(context, writer, selectableNodeTag, canSelect, OnClickWriter.NONE, node, tooltip);
			renderNodeText(writer, nodeContext);
			writeTextLinkStop(writer, selectableNodeTag, canSelect);

		}

		protected void writeColumnAttributes(TagWriter out, NodeContext nodeContext, ColumnDeclaration column) throws IOException {
			out.writeAttribute(ID_ATTR, getCurrentNodeId(nodeContext));
		}

		/**
		 * Returns the id of the node currently written
		 * 
		 * @param nodeContext
		 *        context to get current node and additional informations.
		 */
		protected String getCurrentNodeId(NodeContext nodeContext) {
			Object node = nodeContext.currentNode();
			TreeControl tree = nodeContext.getTree();
			return tree.getNodeId(node);
		}

		protected void writeCellStyle(TagWriter out, ColumnDeclaration column) throws IOException {
			out.beginAttribute(STYLE_ATTR);
			CssUtil.appendStyleOptional(out, column.getWidthStyle());
			CssUtil.appendStyleOptional(out, column.getStyle());
			out.append("white-space: nowrap; overflow: hidden;");
			out.endAttribute();
		}

		/**
		 * Returns the list of column names which have to be written
		 * 
		 * @param declaration
		 *        the tableDeclaration of the written table
		 * @param nodeContext
		 *        the context of the currently written node to get additional
		 *        informations
		 */
		protected List<String> getColumnNames(TableDeclaration declaration, NodeContext nodeContext) {
			return declaration.getColumnNames();
		}

		/**
		 * Decides whether the column with the given column name is the first
		 * visible column or not
		 * 
		 * @param columnName
		 *        the column in question
		 * @param declaration
		 *        the table declaration to get information from
		 */
		protected boolean isFirstColumn(String columnName, TableDeclaration declaration) {
			return declaration.getColumnNames().get(0).equals(columnName);
		}
	}

	@Override
	protected String getNodeTag() {
		return TR;
	}

	@Override
	public TreeContentRenderer getTreeContentRenderer() {
		return contentRenderer;
	}

}
