/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.table.control.IndexViewportState;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.table.renderer.Icons;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.ColumnDeclaration;
import com.top_logic.layout.tree.renderer.TableDeclaration;
import com.top_logic.layout.tree.renderer.ThemeTreeTableRenderer;
import com.top_logic.layout.tree.renderer.TreeImageProvider;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;

/**
 * The class {@link FixedTreeTableRenderer} renders a {@link TreeControl} with
 * some fixed columns.
 * 
 * @deprecated TODO #21888: Replace with tree table rendering.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Deprecated
public class FixedTreeTableRenderer extends ThemeTreeTableRenderer {

	private static final String TREE_FROZEN_TITLE_CSS_CLASS = "tblTreeFrozenTitle";

	public static final Pattern regexp = Pattern.compile("width:\\s*(\\d+)");
	
	private static final String FROZEN_CSS_CLASS = "tblFrozen";
	private static final String FIXED_ID_SUFFIX = "_fixed";
	private static final String FLEX_ID_SUFFIX = "_flex";

	private final int numberFixedColumns;
	private boolean fixed = true;

    public FixedTreeTableRenderer(TreeImageProvider treeImages, TableDeclaration tableDeclaration, int fixedColumns) {
        this(new FixedContentRenderer(treeImages, tableDeclaration, fixedColumns), tableDeclaration, fixedColumns);
    }
    
    protected FixedTreeTableRenderer(FixedContentRenderer contentRenderer, TableDeclaration tableDeclaration, int fixedColumns) {
        super(tableDeclaration, contentRenderer);
        this.numberFixedColumns = fixedColumns;
    }

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, TreeControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		StructureEditTreeControl tree = (StructureEditTreeControl) control;
		String resizeFunction = getJSInitFunction(tree, getVisibleNodeCount(tree), tree.getTableViewportState());

		LayoutControlRenderer.writeLayoutConstraintInformation(100, DisplayUnit.PERCENT, out);
		LayoutControlRenderer.writeLayoutResizeFunction(out, resizeFunction);
	}

	/**
	 * This method returns the JS function for positioning the divs on client side, whereby the
	 * viewport mode is disabled and the count of fixed columns is immutable.
	 * 
	 * @param view
	 *        the control which represents the table.
	 * @param writtenRows
	 *        - count of written rows
	 * @param tableViewportState
	 *        - scroll positions of table
	 */
	public String getJSInitFunction(Control view, int writtenRows, IndexViewportState tableViewportState) {
		int firstRow, lastRow;
		if (writtenRows > 0) {
			firstRow = 0;
			lastRow = writtenRows - 1;
		} else {
			firstRow = lastRow = -1;
		}
		return "TABLE.initFrozing('" + view.getID() + "', 0, " + firstRow + "," + lastRow + ", " + firstRow + ", "
			+ lastRow
			+ ", 0, false, null, "
			+ getClientDisplayData(tableViewportState)
			+ " );";
	}

	private String getClientDisplayData(IndexViewportState tableViewportState) {
		return getClientDisplayData(new VisiblePaneRequest(IndexRange.undefined(), IndexRange.undefined()),
			tableViewportState.getRowAnchor(), tableViewportState.getRowOffset(), tableViewportState.getColumnAnchor(),
			tableViewportState.getColumnOffset());
	}

	private String getClientDisplayData(VisiblePaneRequest visiblePagePane, int rowIndex, int rowOffset,
			int columnIndex, int columnOffset) {
		IndexRange rowRange = visiblePagePane.getRowRange();
		IndexRange columnRange = visiblePagePane.getColumnRange();
		StringBuilder paneCreationFunction = new StringBuilder();
		paneCreationFunction.append("function getDisplayData() {");
		paneCreationFunction.append("var visiblePane = new Object();");
		paneCreationFunction.append("visiblePane.rowRange = new Object();");
		paneCreationFunction.append("visiblePane.rowRange.firstIndex = " + rowRange.getFirstIndex() + ";");
		paneCreationFunction.append("visiblePane.rowRange.lastIndex = " + rowRange.getLastIndex() + ";");
		paneCreationFunction.append("visiblePane.rowRange.forcedVisibleIndexInRange = "
			+ rowRange.getForcedVisibleIndexInRange() + ";");

		paneCreationFunction.append("visiblePane.columnRange = new Object();");
		paneCreationFunction.append("visiblePane.columnRange.firstIndex = " + columnRange.getFirstIndex() + ";");
		paneCreationFunction.append("visiblePane.columnRange.lastIndex = " + columnRange.getLastIndex() + ";");
		paneCreationFunction.append("visiblePane.columnRange.forcedVisibleIndexInRange = "
			+ columnRange.getForcedVisibleIndexInRange() + ";");

		paneCreationFunction.append("var viewportState = new Object();");
		paneCreationFunction.append("viewportState.rowAnchor = new Object();");
		paneCreationFunction.append("viewportState.rowAnchor.index = " + rowIndex + ";");
		paneCreationFunction.append("viewportState.rowAnchor.indexPixelOffset = " + rowOffset + ";");

		paneCreationFunction.append("viewportState.columnAnchor = new Object();");
		paneCreationFunction.append("viewportState.columnAnchor.index = " + columnIndex + ";");
		paneCreationFunction.append("viewportState.columnAnchor.indexPixelOffset = " + columnOffset + ";");
		paneCreationFunction.append("return { visiblePane:visiblePane, viewportState:viewportState }");
		paneCreationFunction.append("}()");

		return paneCreationFunction.toString();
	}

	private int getVisibleNodeCount(TreeControl tree) {
		TreeUIModel treeModel = tree.getModel();
		TLTreeNode rootNode = (TLTreeNode) treeModel.getRoot();
		int subtreeNodeCount = getSubtreeNodeCount(treeModel, rootNode);
		if (!treeModel.isRootVisible()) {
			subtreeNodeCount--;
		}
		return subtreeNodeCount;
	}

	private int getSubtreeNodeCount(TreeUIModel treeModel,
			TLTreeNode node) {
		if (node == null) {
			return 0;
		}

		int visibleChildrenCount = 1;
		if (treeModel.isExpanded(node)) {
			List<TLTreeNode> childNodes = (List<TLTreeNode>) node.getChildren();
			for (TLTreeNode childNode : childNodes) {
				visibleChildrenCount += getSubtreeNodeCount(treeModel, childNode);
			}
		}
		return visibleChildrenCount;
	}

	@Override
	protected void appendControlClasses(TagWriter out) throws IOException {
		HTMLUtil.appendCSSClass(out, AbstractControlBase.IS_CONTROL_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, AbstractControlBase.CAN_INSPECT_CSS_CLASS);
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, TreeControl control) throws IOException {
		TreeControl tree = (TreeControl) control;

		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, tree.getID() + "_table");
		out.writeAttribute(STYLE_ATTR, "overflow:hidden; position: absolute;");
		out.writeAttribute(CLASS_ATTR, FROZEN_CSS_CLASS);
		out.endBeginTag();
		{

			writeTitle(context, out, tree);

			int numberColumns = getTableDeclaration().getColumnNames().size();
			this.fixed = true;
			if (0 < numberFixedColumns) {
				writeTablePart(context, out, tree, true);
			}
			this.fixed = false;
			if (numberFixedColumns < numberColumns) {
				writeTablePart(context, out, tree, false);
			}
		}
		out.endTag(DIV);
	}

	@Override
	protected void writeTitle(DisplayContext context, TagWriter out, TreeControl tree) throws IOException {
		if (tree.getTitleBar() != null) {
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, tree.getID() + "_title");
			out.writeAttribute("data-title-height",
				Float.toString(com.top_logic.layout.form.treetable.component.Icons.FROZEN_TREE_TABLE_TITLE_HEIGHT.get()
					.getValue()));
			out.endBeginTag();
			{

				out.beginBeginTag(DIV);
				writeTitleClassAttribute(out);
				out.endBeginTag();
				{
					out.beginBeginTag(TABLE);
					out.writeAttribute(CLASS_ATTR, FROZEN_CSS_CLASS);
					writeIECompatibilityAttributesIfNeeded(context, out);
					out.endBeginTag();
					{
						out.beginTag(TR);
						{
							out.beginBeginTag(TH);
							out.writeAttribute(CLASS_ATTR, DefaultTableRenderer.TABLE_TITLE_CSS_CLASS);
							out.endBeginTag();
							{
								super.writeTitle(context, out, tree);
							}
							out.endTag(TH);
						}
						out.endTag(TR);
					}
					out.endTag(TABLE);
				}
				out.endTag(DIV);
			}
			out.endTag(DIV);
		}
	}

	private void writeTitleClassAttribute(TagWriter out) {
		out.writeAttribute(CLASS_ATTR, TREE_FROZEN_TITLE_CSS_CLASS);
	}

	/**
	 * Writes either the fixed part of the table (header and body part) or the
	 * flexible part depending on the value of <code>fixedPart</code>.
	 * 
	 * @param context
	 *        the DisplayContext in which rendering occurs
	 * @param out
	 *        the writer to write content to
	 * @param tree
	 *        the tree currently written
	 * @param fixedPart
	 *        decides whether the fixed or the flexible part of the table will
	 *        be written
	 */
	private void writeTablePart(DisplayContext context, TagWriter out, TreeControl tree, boolean fixedPart) throws IOException {
		final TableDeclaration tableDeclaration = getTableDeclaration();
		final boolean hasHeader = tableDeclaration.hasHeader();
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, tree.getID() + (fixedPart ? "_headerFix" : "_headerFlex"));
		out.writeAttribute("data-border-width", (int) ThemeFactory.getTheme().getValue(Icons.TABLE_COLUMN_BORDER_WIDTH).getValue());
		if (!hasHeader) {
			out.writeAttribute(STYLE_ATTR, "display:none;");
			// marker class for the client. It is used in table.js to decide that the width of the
			// fixed part of the table must be taken from the body of the table
			out.writeAttribute(CLASS_ATTR, "noHeader");
		}
		out.endBeginTag();
		{
			out.beginBeginTag(TABLE);
			out.writeAttribute(CLASS_ATTR, FROZEN_CSS_CLASS);
			writeIECompatibilityAttributesIfNeeded(context, out);
			out.endBeginTag();
			out.beginTag(THEAD);
			{
				if (hasHeader) {
					writeHeader(context, out);
				} else {
					/* write dummy structure as DTD requires TR and TH for THEAD */
					int numberColumns = getHeaderNames(tableDeclaration).size();
					out.beginTag(TR);
					{
						while (numberColumns > 0) {
							numberColumns--;
							out.beginTag(TH);
							out.endTag(TH);
						}
					}
					out.endTag(TR);
				}
			}
			out.endTag(THEAD);
			out.endTag(TABLE);
		}
		out.endTag(DIV);

		String treeBodyID = tree.getID() + (fixedPart ? "_bodyFix" : "_bodyFlex");
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, treeBodyID);
		out.endBeginTag();
		
		// Render viewport container
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, treeBodyID + "_viewport");
		out.endBeginTag();
		{
			// Render first viewport slice (for now, the slice is as great as the whole tree and marked
			// with index 0)
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, treeBodyID + "_viewport_slice-0");
			out.writeAttribute(STYLE_ATTR, "position: absolute; top: 0px; left: 0px;");
			out.endBeginTag();
			
			
			out.beginBeginTag(TABLE);
			out.writeAttribute(CLASS_ATTR, FROZEN_CSS_CLASS);
			writeIECompatibilityAttributesIfNeeded(context, out);
			out.endBeginTag();
			out.beginTag(TBODY);
			{
				writeTreeContents(context, out, new FixedNodeContext(tree, fixedPart));
			}
			out.endTag(TBODY);
			out.endTag(TABLE);
			out.endTag(DIV);
		}
		out.endTag(DIV);
		out.endTag(DIV);
	}

	@Override
	protected List<String> getHeaderNames(TableDeclaration declaration) {
		return getColumnNames(declaration, fixed, numberFixedColumns);
	}

	static List<String> getColumnNames(TableDeclaration declaration, boolean fixed, int numberFixedColumns) {
		final List<String> columnNames = declaration.getColumnNames();
		final int numberColumns = columnNames.size();
		if (fixed) {
			return columnNames.subList(0, Math.min(numberFixedColumns, numberColumns));
		} else {
			if (numberFixedColumns < numberColumns) {
				return columnNames.subList(numberFixedColumns, numberColumns);
			} else {
				return Collections.emptyList();
			}
		}
	}

	@Override
	protected String getControlTag(TreeControl control) {
		return DIV;
	}

	@Override
	protected void writeHeaderClassesContent(TagWriter out) throws IOException {
		super.writeHeaderClassesContent(out);
		out.append(StringServices.BLANK_CHAR);
		out.append(FROZEN_CSS_CLASS);
	}

	@Override
	protected void writeHeaderRowAttributes(DisplayContext context, TagWriter out) throws IOException {
		out.writeAttribute(CLASS_ATTR, CssUtil.joinCssClasses(FROZEN_CSS_CLASS, DefaultTableRenderer.TABLE_HEADER_ROW));
		out.writeAttribute("data-header-row-height",
			(int) ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FROZEN_TABLE_HEADER_ROW_HEIGHT)
				.getValue());
	}

	@Override
	protected String getCurrentNodeId(NodeContext nodeContext) {
		final String idSuffix = ((FixedNodeContext) nodeContext).isFixed() ? FIXED_ID_SUFFIX : FLEX_ID_SUFFIX;
		Object node = nodeContext.currentNode();
		TreeControl tree = nodeContext.getTree();
		return tree.getNodeId(node) + idSuffix;
	}

	@Override
	protected void writeNodeClassesContent(TagWriter out, NodeContext nodeContext) throws IOException {
		super.writeNodeClassesContent(out, nodeContext);

		out.append(FROZEN_CSS_CLASS);
	}

	@Override
	protected void writeNodeAttributes(DisplayContext context, TagWriter writer, NodeContext nodeContext) {
		super.writeNodeAttributes(context, writer, nodeContext);
		writer.writeAttribute("data-row-height", (int) ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FROZEN_TABLE_ROW_HEIGHT).getValue());
	}

	@Override
	public void addNodeUpdateActions(UpdateQueue queue, final TreeControl tree, final Object node, String nodeID) {
		int numberColumns = getTableDeclaration().getColumnNames().size();
		if (0 < numberFixedColumns) {
			// creates update for the fixed part of the node
			queue.add(new ElementReplacement(nodeID + FIXED_ID_SUFFIX, createUpdateFragment(tree, node, true, false)));
		}
		if (numberFixedColumns < numberColumns) {
			// creates update for the flexible part of the subtree
			queue.add(new ElementReplacement(nodeID + FLEX_ID_SUFFIX, createUpdateFragment(tree, node, false, false)));
		}
	}

	@Override
	public void addSubtreeUpdateActions(UpdateQueue queue, final TreeControl tree, final Object node, String nodeID, String stopID) {
		
		// creates update for the fixed part of the subtree
		final HTMLFragment fixedUpdate;
		// creates update for the flexible part of the subtree
		final HTMLFragment flexUpdate;
		int numberColumns = getTableDeclaration().getColumnNames().size();
		if (tree.getModel().getRoot().equals(node)) {
			if (0 < numberFixedColumns) {
				// complete repaint of the tree
				fixedUpdate = new HTMLFragment() {

					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						writeTreeContents(context, out, new FixedNodeContext(tree, true));
					}
				};
				queue.add(new RangeReplacement(nodeID + FIXED_ID_SUFFIX, stopID + FIXED_ID_SUFFIX, fixedUpdate));
			}
			if (numberFixedColumns < numberColumns) {
				flexUpdate = new HTMLFragment() {

					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						writeTreeContents(context, out, new FixedNodeContext(tree, false));
					}
				};
				queue.add(new RangeReplacement(nodeID + FLEX_ID_SUFFIX, stopID + FLEX_ID_SUFFIX, flexUpdate));
			}
		} else {
			if (0 < numberFixedColumns) {
				fixedUpdate = createUpdateFragment(tree, node, true, true);
				queue.add(new RangeReplacement(nodeID + FIXED_ID_SUFFIX, stopID + FIXED_ID_SUFFIX, fixedUpdate));
			}
			if (numberFixedColumns < numberColumns) {
				flexUpdate = createUpdateFragment(tree, node, false, true);
				queue.add(new RangeReplacement(nodeID + FLEX_ID_SUFFIX, stopID + FLEX_ID_SUFFIX, flexUpdate));
			}
		}

		// triggers a resizing of the scroll bars. Must be done since it is
		// possible that there are now more rows to scroll
		queue.add(new JSSnipplet("TABLE.updateViewportHeight('" + tree.getID() + "', 0, " + getVisibleNodeCount(tree) + ");"));
	}

	private HTMLFragment createUpdateFragment(TreeControl tree, Object node, boolean fixedPart, boolean recursive) {
		final FixedNodeContext nodeContext = new FixedNodeContext(tree, fixedPart);
		setupNodeContext(tree, nodeContext, node);
		return createSubtreeFragment(nodeContext, recursive);
	}
	
	@Override
	protected void writeHeader(DisplayContext context, TagWriter out) throws IOException {
		TableDeclaration tableDeclaration = getTableDeclaration();
		List<String> columnNames = getHeaderNames(tableDeclaration);
		
		out.beginBeginTag(TR);
		writeHeaderRowAttributes(context, out);
		out.endBeginTag();
		Resources res = Resources.getInstance();
		final int numberColumns = columnNames.size();
		for (int n = 0, cnt = numberColumns; n < cnt; n++) {
			String            columnName = columnNames.get(n);
			ColumnDeclaration column     = tableDeclaration.getColumnDeclaration(columnName);
			
			String theStyle = StringServices.concatenate(column.getWidthStyle(), column.getHeaderStyle());
			
			out.beginBeginTag(TH);
			writeHeaderClasses(out);
			writeStyleAttributes(out, theStyle);
			out.writeAttribute("data-column-width", getColumnWidth(column));
			out.endBeginTag();
			
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, DefaultTableRenderer.CELL_ADJUSTMENT_CSS_CLASS);
			writeStyleAttributes(out, theStyle);
			out.endBeginTag();
			{
				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, DefaultTableRenderer.CELL_INNER_SPACER_CSS_CLASS);
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

					default:
						throw new UnsupportedOperationException(
							"Header type " + column.getHeaderType() + " not supported.");
				}
				
				out.endTag(DIV);
			}
			
			out.endTag(DIV);
		
			out.endTag(TH);
		}
		if (numberColumns == 0) {
			out.beginTag(TH);
			out.endTag(TH);
		}
		
		out.endTag(TR);
	}
	
	static protected void writeStyleAttributes(TagWriter out, String style) throws IOException {
		style = StringServices.nonNull(style);
		if (!regexp.matcher(style).find()) {
			style = StringServices.concatenate(DefaultTableRenderer.DEFAULT_COLUMN_WIDTH_STYLE, style);
		}
		out.writeAttribute(STYLE_ATTR, style);
	}
	
	private int getColumnWidth(ColumnDeclaration column) {
		return TableUtil.parseColumnWidth(column.getWidthStyle());
	}

	/**
	 * Renders the content of the columns in the table. Actually the same as
	 * {@link com.top_logic.layout.tree.renderer.ThemeTreeTableRenderer.ThemeTableRowRenderer} but
	 * has variable which defines which columns shall be written.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	@Deprecated
	protected static class FixedContentRenderer extends ThemeTableRowRenderer {

		private final int numberFixedColumns;

		public FixedContentRenderer(TreeImageProvider treeImages, TableDeclaration tableDeclaration, int fixedColumns) {
			super(treeImages, tableDeclaration);
			this.numberFixedColumns = fixedColumns;
		}

		@Override
		protected List<String> getColumnNames(TableDeclaration declaration, NodeContext nodeContext) {
			return FixedTreeTableRenderer.getColumnNames(declaration, ((FixedNodeContext) nodeContext).isFixed(), numberFixedColumns);
		}
		
		@Override
		public void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException {
			TableDeclaration tableDeclaration = getTableDeclaration();
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
                writer.endBeginTag();
                
				writer.beginBeginTag(DIV);
				writer.writeAttribute(CLASS_ATTR, DefaultTableRenderer.CELL_ADJUSTMENT_CSS_CLASS);
    			writeStyleAttributes(writer, StringServices.concatenate(column.getWidthStyle(), column.getStyle()));
    			writer.endBeginTag();
    			{
					writer.beginBeginTag(DIV);
					writer.writeAttribute(CLASS_ATTR, DefaultTableRenderer.CELL_INNER_SPACER_CSS_CLASS);
	    			writer.endBeginTag();
					int currentDepth = writer.getDepth();
					try {
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
								Control columnControl =
										controlProvider.createControl(columnValue, column.getStyle());
								if (columnControl != null) {
									columnControl.write(context, writer);
								} else {
									// The ControlProvider may have valid reasons suppress a
									// control ...
									// Logger.warn("Failed to createControl for Column '" +
									// columnName + "'", this);
								}
								break;
							}
						}
					} catch (Throwable throwable) {
						try {
							writer.endAll(currentDepth);
							RuntimeException renderingError = ExceptionUtil.createException(
								"Error occured during rendering of structure edit table cell (Column '" + columnName
								+ "').",
								Collections.singletonList(throwable));
							nodeContext.getTree().produceErrorOutput(context, writer, renderingError);
						} catch (Throwable inner) {
							// In the rare case of catastrophe better throw the original.
							throw throwable;
						}
					}
					writer.endTag(DIV);
				}
				writer.endTag(DIV);
				writer.endTag(TD);
			}
			if (numberColumns == 0) {
				writer.beginTag(TD);
				writer.endTag(TD);
			}

		}
		
		@Override
		protected void writeColumnAttributes(TagWriter out, NodeContext nodeContext, ColumnDeclaration column) throws IOException {
			super.writeColumnAttributes(out, nodeContext, column);
			String style = StringServices.concatenate(column.getWidthStyle(), column.getStyle());
			writeStyleAttributes(out, style);
		}
	}

	/**
	 * Creates a {@link Pattern} that matches the transformed node ids used by a
	 * {@link FixedTreeTableRenderer}.
	 * 
	 * <p>
	 * If the {@link Matcher} matches (i.e. the node id is one produced by a
	 * {@link FixedTreeTableRenderer}) than {@link Matcher#group(int) group(1)} will return the
	 * original node id in the {@link TreeData}.
	 * </p>
	 */
	public static Pattern createNodeIdPattern() {
		StringBuilder regex = new StringBuilder();
		regex.append("(.*)");
		regex.append("(?:");
		regex.append(Pattern.quote(FIXED_ID_SUFFIX));
		regex.append("|");
		regex.append(Pattern.quote(FLEX_ID_SUFFIX));
		regex.append(")");
		return Pattern.compile(regex.toString());
	}

	@Override
	protected void writeColumnHeightStyle(TagWriter out) throws IOException {
		Integer height = (int) ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FROZEN_TABLE_ROW_HEIGHT).getValue() - (int) ThemeFactory.getTheme().getValue(Icons.TABLE_COLUMN_BORDER_WIDTH).getValue();
		out.writeAttribute(HEIGHT_ATTR, (height + "px"));
	}

	/**
	 * A special {@link NodeContext} which also specifies
	 * whether currently the fixed or the flexible content of the node is
	 * written
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	protected static class FixedNodeContext extends NodeContext {

		private final boolean isFixed;

		public FixedNodeContext(TreeControl tree, boolean isFixed) {
			super(tree);
			this.isFixed = isFixed;
		}

		public boolean isFixed() {
			return this.isFixed;
		}
	}

}
