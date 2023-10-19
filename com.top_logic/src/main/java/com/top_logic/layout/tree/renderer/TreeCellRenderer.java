/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ConstantCommandModel;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link RowTypeCellRenderer} that renders {@link TreeUINode} rows displaying an expand/collapse
 * button.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeCellRenderer extends RowTypeCellRenderer {
	
	private static final String TL_ATTRIBUTE_IS_TREE_NODE = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "treenode";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final Property<TreeUINode<?>> FOCUSED_NODE =
		(Property) TypedAnnotatable.property(TreeUINode.class, "focusedNode");

	private static final Property<String> FOCUSED_COL_NAME =
		TypedAnnotatable.property(String.class, "focusedColumnName");

	/**
	 * Special value that requests the indent chars to be retrieved from the theme.
	 */
	public static final int DEFAULT_INDENT_CHARS = -1;

	/** The {@link ConfigurationItem} for the {@link TreeCellRenderer}. */
	public interface Config extends RowTypeCellRenderer.Config {

		@Name(INDENT_CHARS_PROPERTY)
		@IntDefault(DEFAULT_INDENT_CHARS)
		int getIndentChars();

		/**
		 * Is normally used to unwrap tree nodes and create the tree expansion icons.
		 * 
		 * <p>
		 * In this use case, the type icons are rendered by the content renderer. Therefore, this
		 * implementations used the {@link NoResourceProvider} by default.
		 * </p>
		 */
		@Override
		@ItemDefault(NoResourceProvider.class)
		PolymorphicConfiguration<? extends ResourceProvider> getResourceProvider();

	}

	/**
	 * Configuration option to configure the subtree indentation character count.
	 */
	public static final String INDENT_CHARS_PROPERTY = "indentChars";

	private static final AbstractButtonRenderer<?> TOGGLE_RENDERER =
		ImageButtonRenderer.newSystemButtonRenderer(FormConstants.TOGGLE_BUTTON_CSS_CLASS);

	private final String _indentText;

	/**
	 * Creates a {@link TreeCellRenderer} from configuration.
	 */
	public TreeCellRenderer(InstantiationContext context, Config config) {
		super(context, config);
		_indentText = getIndentText(config);
	}

	/**
	 * Creates a {@link TreeCellRenderer}.
	 */
	public TreeCellRenderer(ResourceProvider resourceProvider, CellRenderer contentRenderer, int indentChars) {
		super(resourceProvider, contentRenderer);
		_indentText = getIndentText(indentChars);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	private String getIndentText(Config config) {
		return getIndentText(config.getIndentChars());
	}

	private String getIndentText(int indentChars) {
		if (indentChars == DEFAULT_INDENT_CHARS) {
			indentChars = ThemeFactory.getTheme().getValue(Icons.TREE_INDENT_CHARS);
		}
		StringBuilder buffer = new StringBuilder(indentChars);
		for (int n = 0; n < indentChars; n++) {
			buffer.append(NBSP);
		}
		return buffer.toString();
	}

	/**
	 * {@link ModelNamingScheme} for renderer-created {@link Toggle} commands.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ToggleNaming extends AbstractModelNamingScheme<Toggle, ToggleNaming.ToggleName> {

		/**
		 * {@link ModelName} of {@link Toggle tree node toggle} commands.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public interface ToggleName extends ModelName {

			/**
			 * Name of the context model.
			 */
			ModelName getContextModel();

			/**
			 * @see #getContextModel()
			 */
			void setContextModel(ModelName referenceValue);

			/**
			 * The name of the node being toggled.
			 */
			ModelName getNode();

			/**
			 * Setter for {@link #getNode()}.
			 */
			void setNode(ModelName value);

			/**
			 * Whether to expand (in contrast to collapse) a node.
			 */
			@BooleanDefault(true)
			boolean getExpand();

			/**
			 * @see #getExpand()
			 */
			void setExpand(boolean value);

		}

		@Override
		public Class<ToggleName> getNameClass() {
			return ToggleName.class;
		}

		@Override
		public Class<Toggle> getModelClass() {
			return Toggle.class;
		}

		@Override
		public Toggle locateModel(ActionContext context, ToggleName name) {
			// The type 'CheckScoped' is not usable itself as a value context,
			// but multiple subtypes are.
			TableData model = (TableData) context.resolve(name.getContextModel());
			AbstractTreeTableNode<?> node = (AbstractTreeTableNode<?>) context.resolve(name.getNode(), model);
			return new SetExpansionState(node, model, name.getExpand());
		}

		@Override
		protected void initName(ToggleName name, Toggle model) {
			TableData valueContext = model.getTableData();
			name.setContextModel(ModelResolver.buildModelName(valueContext));
			name.setNode(ModelResolver.buildModelName(valueContext, model.getNode()));
			name.setExpand(!model.getNode().isExpanded());
		}

	}

	private static class SetExpansionState extends Toggle {

		private boolean _expand;

		public SetExpansionState(AbstractTreeTableNode<?> node, TableData tableData, boolean expand) {
			super(node, null, tableData);

			_expand = expand;
		}

		@Override
		protected boolean shouldExpand() {
			return _expand;
		}

	}

	private static class Toggle extends ConstantCommandModel {
		private final AbstractTreeTableNode<? extends Object> _node;

		private final TableData _tableData;

		private String _columnName;

		public Toggle(AbstractTreeTableNode<? extends Object> node, String colName, TableData tableData) {
			_node = node;
			_columnName = colName;
			_tableData = tableData;
		}

		public TableData getTableData() {
			return _tableData;
		}

		public TreeUINode<?> getNode() {
			return _node;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			final boolean expand = shouldExpand();
			if (!expand) {
				// Check, whether the current selection is collapsed. If that's the case, contents
				// have to be stored before collapsing can proceed.
				DirtyHandling dirtyhandling = DirtyHandling.getInstance();
				Collection<? extends ChangeHandler> affectedFormHandlers =
					getTableData().getCheckScope().getAffectedFormHandlers();
				if (dirtyhandling.checkDirty(affectedFormHandlers)) {
					Command continuation = new Command() {
						@Override
						public HandlerResult executeCommand(DisplayContext continuationContext) {
							return doExpand(continuationContext, expand);
						}
					};
					dirtyhandling.openConfirmDialog(continuation, affectedFormHandlers, context.getWindowScope());
					return HandlerResult.DEFAULT_RESULT;
				}
			}
			return doExpand(context, expand);
		}

		protected boolean shouldExpand() {
			return !_node.isExpanded();
		}

		HandlerResult doExpand(DisplayContext context, boolean expand) {
			_node.setExpanded(expand);

			// Cannot just call this.focus(), due to table will be fully redrawn
			// after this command has been processed and all current toggle command models will be
			// dropped at rendering time.
			context.set(FOCUSED_NODE, getNode());
			context.set(FOCUSED_COL_NAME, _columnName);

			TableViewModel viewModel = _tableData.getViewModel();
			if (viewModel != null) {
				viewModel.getClientDisplayData().getVisiblePaneRequest().setRowRange(getSubtreeIndexRange());
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		private IndexRange getSubtreeIndexRange() {
			int firstRow = _node.getPosition();
			int lastRow = firstRow + _node.getVisibleSubtreeSize() - 1;

			return IndexRange.multiIndex(firstRow, lastRow);
		}

		@Override
		public ThemeImage getImage() {
			if (_node.isExpanded()) {
				return Icons.TREE_EXPANDED;
			} else {
				return Icons.TREE_COLLAPSED;
			}
		}

	}
	
	@Override
	protected void writeCellAttributes(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		super.writeCellAttributes(context, out, cell);
		out.writeAttribute(TL_ATTRIBUTE_IS_TREE_NODE, true);
	}

	@Override
	protected void writeDecorationContent(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		writeExpandButton(context, out, cell);
		writeTypeImage(context, out, cell);
	}

	/**
	 * Writes the type image.
	 * 
	 * @param context
	 *        Render context.
	 * @param out
	 *        {@link TagWriter} to add output to.
	 * @param cell
	 *        Rendered cell.
	 */
	protected void writeTypeImage(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		super.writeDecorationContent(context, out, cell);
	}

	/**
	 * Writes the button to toggle expansion state of the tree node.
	 * 
	 * @param context
	 *        Render context.
	 * @param out
	 *        {@link TagWriter} to add output to.
	 * @param cell
	 *        Rendered cell.
	 */
	protected void writeExpandButton(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		AbstractTreeTableNode<?> node = (AbstractTreeTableNode<?>) cell.getRowObject();

		int indent = 0;
		TreeUINode<?> parent = node.getParent();
		while (parent != null) {
			indent++;
			parent = parent.getParent();
		}

		if (!((TreeUIModel<?>) node.getModel()).isRootVisible()) {
			indent--;
		}

		out.beginTag(SPAN);
		writeIndent(out, indent);
		if (isLeaf(node)) {
			Icons.TREE_LEAF.write(context, out);
		} else {
			String colName = cell.getColumnName();
			Toggle toggle = new Toggle(node, colName, cell.getView().getTableData());
			if (shallFocusNode(context, node, colName)) {
				toggle.focus();
			}
			new ButtonControl(toggle, TOGGLE_RENDERER).write(context, out);
		}
		out.endTag(SPAN);
	}

	private boolean shallFocusNode(DisplayContext context, TreeUINode<?> currentNode, String columName) {
		if (currentNode != context.get(FOCUSED_NODE)) {
			return false;
		}
		String cachedColName = context.get(FOCUSED_COL_NAME);
		if (cachedColName == null) {
			/* Must actually just occur in scripted test. In this case no column name is
			 * recorded. */
			return true;
		}
		if (!cachedColName.equals(columName)) {
			/* A Toggle command in a different column for the same node was executed. This occurs
			 * when in a tree table more than one column uses the TreeCellRenderer. */
			return false;
		}
		return true;
	}

	private boolean isLeaf(TreeUINode<?> node) {
		return node.getChildCount() == 0;
	}

	private void writeIndent(TagWriter out, int indent) {
		for (int n = 0, cnt = indent; n < cnt; n++) {
			out.writeText(_indentText);
		}
	}
}
