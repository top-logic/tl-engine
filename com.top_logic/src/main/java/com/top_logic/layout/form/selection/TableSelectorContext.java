/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import static com.top_logic.layout.form.selection.TreeSelectorContextUtil.*;
import static com.top_logic.mig.html.NoLinkResourceProvider.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.model.AbstractSelectionModel;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ConstantField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.TreeTableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.scripting.recorder.DynamicRecordable;
import com.top_logic.layout.table.CombiningRowClassProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableControl.DefaultSelectionStrategy;
import com.top_logic.layout.table.control.TableControl.SelectionStrategy;
import com.top_logic.layout.table.control.TableControl.SelectionType;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.SetTableResPrefix;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.table.tree.TreeNodeUnwrappingProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeTableBuilderAdapter;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.layout.tree.renderer.TreeCellRenderer;
import com.top_logic.mig.html.AbstractRestrainedSelectionModel;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.provider.resource.INoLinkResourceProvider;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Base class for {@link FormContext} implementations of table based selection views.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableSelectorContext extends FormContext implements DynamicRecordable {

	private static final String SELECT_DIALOG_SUB_KEY = "_selectDialog";

	/** Name of the {@link TableField} displaying the options. */
	public static final String SELECTION_TABLE = "selectionTable";

	private Command _closeAction;

	private SelectField _targetSelectField;

	private Map<Object, DefaultTreeTableNode> _nodeCache;

	private int _optionsPerPage;

	private int _initialTreeExpansionDepth;

	/**
	 * Create a new {@link TableSelectorContext}
	 */
	@SuppressWarnings({ "deprecation" })
	public TableSelectorContext(SelectField targetSelectField, Command closeAction, int optionsPerPage, int initialTreeExpansionDepth) {
		super(SelectorContext.CONTEXT_NAME, ResPrefix.legacyPackage(SelectorContext.class));
		_optionsPerPage = optionsPerPage;
		_initialTreeExpansionDepth = initialTreeExpansionDepth;
		_nodeCache = new HashMap<>();
		_targetSelectField = targetSelectField;
		_closeAction = createPurgingCloseCommand(closeAction);
		createTitle();
		createButtons();
		addMember(createTableField());
	}

	private Command createPurgingCloseCommand(final Command closeAction) {
		return new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				TableField tableField = getTableField();
				resetTableFilters(tableField);
				return closeAction.executeCommand(context);
			}
		};
	}

	private void createTitle() {
		Resources resources = Resources.getInstance();
		ConstantField title = new ConstantField(SelectorContext.TITLE_FIELD_NAME, !AbstractFormField.IMMUTABLE) {
			@Override
			public <R, A> R visit(FormMemberVisitor<R, A> v, A arg) {
				return v.visitFormMember(this, arg);
			}
		};
		String targetFieldLabel = _targetSelectField.getLabel();
		title.setLabel(resources.getString(I18NConstants.POPUP_SELECT_TITLE__FIELD.fill(targetFieldLabel)));
		addMember(title);

	}

	/**
	 * Creates the {@link TableField} holding the options.
	 */
	public TableField createTableField() {
		AbstractRestrainedSelectionModel tableSelectionModel;
		if (_targetSelectField.isMultiple()) {
			tableSelectionModel = new DefaultMultiSelectionModel(SelectionModelOwner.NO_OWNER);
		} else {
			tableSelectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		}

		List<TableConfigurationProvider> tableConfigurationProviders = getTableConfigurationProviders();
		TableConfiguration tableConfiguration = TableConfigurationFactory.build(tableConfigurationProviders);
		TableField tableField;
		if (!_targetSelectField.isOptionsTree()) {
			tableField = createFlatTableField(tableConfiguration, tableSelectionModel);
		} else {
			tableField = createTreeTableField(tableConfiguration, tableSelectionModel);
		}
		tableField.setControlProvider(createTableDialogControlProvider(tableConfiguration));
		setSelectionModel(tableField, tableSelectionModel);
		tableField.setSelectable(true);
		
		tableSelectionModel.addSelectionListener(new SelectionChangedListener(tableSelectionModel.getSelection()));
		return tableField;
	}

	private TableField createFlatTableField(TableConfiguration tableConfiguration,
			AbstractRestrainedSelectionModel tableSelectionModel) {

		List<Object> allRows = new ArrayList<>();
		List<?> selection = _targetSelectField.getSelection();
		allRows.addAll(selection);
		Set<?> selectionSet = _targetSelectField.getSelectionSet();
		for (Iterator<?> it = _targetSelectField.getOptionModel().iterator(); it.hasNext();) {
			Object element = it.next();
			if (selectionSet.contains(element)) {
				continue;
			}

			allRows.add(element);
		}

		ObjectTableModel tableModel =
			new ObjectTableModel(tableConfiguration.getDefaultColumns(), tableConfiguration, allRows);
		TableField tableField = FormFactory.newTableField(SELECTION_TABLE, getConfigKey());

		initSelectionModel(tableSelectionModel, selectionSet);

		TableViewModel viewModel = new TableViewModel(tableModel, getConfigKey()) {
			@Override
			public Comparator<Object> getRowComparator() {
				return new SelectionFirstComparator<>(tableSelectionModel.getSelection(), super.getRowComparator());
			}
		};

		tableField.setTableModel(viewModel);
		removeSelectionBlockingFilters(tableField, tableSelectionModel);
		showFirstSelectedRow(tableField, tableSelectionModel);
		return tableField;
	}

	@SuppressWarnings("unchecked")
	private TableField createTreeTableField(TableConfiguration tableConfiguration,
			AbstractRestrainedSelectionModel tableSelectionModel) {
		TreeTableField treeTableField = FormFactory.newTreeTableField(SELECTION_TABLE, getConfigKey());
		TLTreeModel<TLTreeNode<?>> treeModel =
			(TLTreeModel<TLTreeNode<?>>) _targetSelectField.getOptionsAsTree().getBaseModel();
		Object rootUserObject = treeModel.getRoot();
		TreeBuilder<DefaultTreeTableNode> treeBuilder =
			new TreeTableBuilderAdapter<TLTreeNode<?>>(treeModel);
		DefaultTreeTableModel treeTableModel =
			new DefaultTreeTableModel(treeBuilder, rootUserObject, tableConfiguration.getDefaultColumns(),
				tableConfiguration);
		treeTableField.setTree(treeTableModel);
		treeTableModel.setRootVisible(_targetSelectField.getOptionsAsTree().showRootNode());
		convertTreeNodeSelection(treeTableModel, tableSelectionModel);
		removeSelectionBlockingFilters(treeTableField, tableSelectionModel);
		expandNodes(treeTableModel, _initialTreeExpansionDepth, tableSelectionModel.getSelection());
		showFirstSelectedRow(treeTableField, tableSelectionModel);
		addTreeButtons(treeTableField, treeTableModel);
		return treeTableField;
	}

	@SuppressWarnings("unchecked")
	private void convertTreeNodeSelection(DefaultTreeTableModel treeTableModel,
			AbstractRestrainedSelectionModel selectionModel) {
		Set<DefaultTreeTableNode> treeTableSelection = new HashSet<>();
		TLTreeModel<Object> treeModel = (TLTreeModel<Object>) _targetSelectField.getOptionsAsTree().getBaseModel();
		for (Object selectedNode : _targetSelectField.getSelectionSet()) {
			List<Object> pathToRoot = treeModel.createPathToRoot(selectedNode);
			DefaultTreeTableNode treeTableNode;
			if (!CollectionUtil.isEmptyOrNull(pathToRoot)) {
				treeTableNode = getTreeTableNode(treeTableModel, pathToRoot);
			} else {
				treeTableNode = createTreeTableNode(treeTableModel, selectedNode);
			}
			treeTableSelection.add(treeTableNode);
		}
		_nodeCache.clear();
		initSelectionModel(selectionModel, treeTableSelection);
	}

	private DefaultTreeTableNode createTreeTableNode(DefaultTreeTableModel treeTableModel, Object selectedNode) {
		DefaultTreeTableNode treeTableNode = new DefaultTreeTableNode(treeTableModel, null, selectedNode);
		treeTableModel.getRoot().addChild(treeTableNode);
		return treeTableNode;
	}

	private DefaultTreeTableNode getTreeTableNode(DefaultTreeTableModel treeTableModel, List<Object> treeNodePath) {
		if (istRootNodeSelected(treeTableModel, treeNodePath)) {
			return treeTableModel.getRoot();
		}

		DefaultTreeTableNode treeTableNode = treeTableModel.getRoot();
		for (int i = treeNodePath.size() - 2; i >= 0; i--) {
			Object childTreeNode = treeNodePath.get(i);
			if (_nodeCache.containsKey(childTreeNode)) {
				treeTableNode = _nodeCache.get(childTreeNode);
			} else {
				treeTableNode = searchChildTreeTableNode(treeTableNode, childTreeNode);
				if (treeTableNode == null) {
					treeTableNode = createTreeTableNode(treeTableModel, treeNodePath.get(0));
					break;
				}
				_nodeCache.put(childTreeNode, treeTableNode);
			}
		}
		return treeTableNode;
	}

	private boolean istRootNodeSelected(DefaultTreeTableModel treeTableModel, List<Object> treeNodePath) {
		return treeNodePath.size() == 1 && treeTableModel.getRoot().getBusinessObject().equals(treeNodePath.get(0));
	}

	private DefaultTreeTableNode searchChildTreeTableNode(DefaultTreeTableNode treeTableNode, Object childTreeNode) {
		for (DefaultTreeTableNode childTreeTableNode : treeTableNode.getChildren()) {
			if (childTreeTableNode.getBusinessObject().equals(childTreeNode)) {
				return childTreeTableNode;
			}
		}
		return null;
	}

	private void removeSelectionBlockingFilters(TableField treeTableField, SelectionModel selectionModel) {
		for (Object selectedNode : selectionModel.getSelection()) {
			treeTableField.getViewModel().adjustFiltersForRow(selectedNode);
		}
	}

	@SuppressWarnings("unchecked")
	private void showFirstSelectedRow(TableField treeTableField, SelectionModel selectionModel) {
		if (!selectionModel.getSelection().isEmpty()) {
			TableViewModel viewModel = treeTableField.getViewModel();
			List<Object> displayedRows = viewModel.getDisplayedRows();
			for (int i = 0; i < displayedRows.size(); i++) {
				Object row = displayedRows.get(i);
				if (selectionModel.isSelected(row)) {
					viewModel.getClientDisplayData().getVisiblePaneRequest().setPersistentRowRange(IndexRange.singleIndex(i));
					break;
				}
			}
		}
	}

	private void addTreeButtons(TreeTableField treeTableField, final TreeUIModel<?> treeModel) {
		ToolBar toolBar = treeTableField.getToolBar();
		ToolBarGroup expansionStateGroup = toolBar.defineGroup("expand-collapse");
		expansionStateGroup.addButton(createExpandAllCommand(treeModel));
		expansionStateGroup.addButton(createCollapseAllCommand(treeModel));
	}

	private CommandModel createExpandAllCommand(final TreeUIModel<?> treeModel) {
		String commandLabel =
			Resources.getInstance().getString(com.top_logic.layout.form.selection.I18NConstants.EXPAND_ALL);
		return createExpansionChangeCommand(com.top_logic.layout.basic.Icons.EXPAND_ALL, commandLabel, treeModel, true);
	}

	private CommandModel createCollapseAllCommand(final TreeUIModel<?> treeModel) {
		String commandLabel =
			Resources.getInstance().getString(com.top_logic.layout.form.selection.I18NConstants.COLLAPSE_ALL);
		return createExpansionChangeCommand(com.top_logic.layout.basic.Icons.COLLAPSE_ALL, commandLabel, treeModel,
			false);
	}

	private CommandModel createExpansionChangeCommand(ThemeImage commandImage, String commandLabel,
			final TreeUIModel<?> treeModel, final boolean expand) {
		CommandModel expansionChangeCommand = new AbstractCommandModel() {

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				TreeUIModelUtil.setExpandedAll(treeModel, treeModel.getRoot(), expand);
				return DefaultHandlerResult.DEFAULT_RESULT;
			}
		};
		expansionChangeCommand.setImage(commandImage);
		expansionChangeCommand
			.setLabel(commandLabel);
		return expansionChangeCommand;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Filter<Object> selectableOptionsFilterRaw() {
		Filter rawFilter = FilterFactory.not(_targetSelectField.getFixedOptionsNonNull());
		if (_targetSelectField.isOptionsTree()) {
			rawFilter =
				FilterFactory.and(rawFilter, _targetSelectField.getOptionsAsTree().getSelectableOptionsFilter());
		}
		return rawFilter;
	}

	private Filter<? extends Object> getSelectableOptionsFilter() {
		return getOptionFilter(selectableOptionsFilterRaw());
	}

	private Filter<? extends Object> getNonSelectableOptionsFilter() {
		return getOptionFilter(FilterFactory.<Object> not(selectableOptionsFilterRaw()));
	}

	private Filter<? extends Object> getOptionFilter(Filter<Object> selectFieldFilter) {
		if (!_targetSelectField.isOptionsTree()) {
			return selectFieldFilter;
		} else {
			return new FixedOptionsTreeTableFilter(selectFieldFilter);
		}
	}

	private static void setSelectionModel(TableField tableField, AbstractSelectionModel model) {
		model.initOwner(tableField);
		tableField.setSelectionModel(model);
	}

	private DefaultFormFieldControlProvider createTableDialogControlProvider(
			final TableConfiguration tableConfiguration) {
		return new DefaultFormFieldControlProvider() {

			@Override
			public Control visitTableField(TableField aMember, Void arg) {
				TableControl tableControl =
					TableTag.createTableControl(aMember, tableConfiguration.getTableRenderer(), true);
				tableControl.setSelectionStrategy(AddBySimpleClickStrategy.INSTANCE);
				return tableControl;
			}
		};
	}

	private void initSelectionModel(AbstractRestrainedSelectionModel selectionModel,
			Set<?> selectedOptions) {
		Filter<?> selectableOptionsFilter = getSelectableOptionsFilter();
		selectionModel.setSelection(selectedOptions);
		selectionModel.setSelectionFilter(selectableOptionsFilter);
		selectionModel.setDeselectionFilter(selectableOptionsFilter);
	}

	private List<TableConfigurationProvider> getTableConfigurationProviders() {
		List<TableConfigurationProvider> providers = new ArrayList<>();
		/* Set the resources *before* the configuration from the selectfield to not override a
		 * potential table ResPrefix. */
		providers.add(new SetTableResPrefix(_targetSelectField.getResources()));
		providers.add(getTableConfigurationFromField());
		if (_targetSelectField.isOptionsTree()) {
			providers.add(TreeSelectionProvider.INSTANCE);
			providers.add(TreeNodeUnwrappingProvider.INSTANCE);
		}

		// Note: This provider must be executed after a tree column has been potentially created,
		// because it prevents clicks in all columns except the tree column.
		providers.add(new DialogSettingsProvider());

		providers.add(getRowClassProvider());
		providers.add(GenericTableConfigurationProvider.showDefaultColumns());
		return providers;
	}

	private TableConfigurationProvider getRowClassProvider() {
		return new TableConfigurationProvider() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				FixedOptionClassProvider fixedOptionsClassProvider =
					new FixedOptionClassProvider(getNonSelectableOptionsFilter());
				RowClassProvider rowClassProvider = table.getRowClassProvider();
				if (rowClassProvider != null) {
					rowClassProvider =
						new CombiningRowClassProvider(Arrays.asList(rowClassProvider, fixedOptionsClassProvider));
				} else {
					rowClassProvider = fixedOptionsClassProvider;
				}
				table.setRowClassProvider(rowClassProvider);
			}
		};
	}

	private void resetTableFilters(TableField tableField) {
		tableField.getViewModel().resetAllFilters();
	}

	private TableConfigurationProvider getTableConfigurationFromField() {
		TableConfigurationProvider config = _targetSelectField.getDialogTableConfigurationProvider();
		if (config != null) {
			return config;
		}
		return _targetSelectField.getTableConfigurationProvider();
	}

	private void createButtons() {
		final CommandField acceptButton = createAcceptCommand();
		CommandField cancelButton = createCancelCommand();
		FormGroup buttons = SelectorContextUtil.createButtonGroup(acceptButton, cancelButton, getResources());
		addMember(buttons);
	}

	private CommandField createCancelCommand() {
		CommandField cancelButton = SelectorContextUtil.createCancelButton(getCloseCommand());

		return cancelButton;
	}

	private CommandField createAcceptCommand() {
		Command command = new TableAcceptSelection(this, _targetSelectField);
		CommandField acceptButton = SelectorContextUtil.createAcceptButton(command);
		setExecutability(acceptButton, false);

		return acceptButton;
	}

	private void setExecutability(CommandField accept, boolean changed) {
		if (!_targetSelectField.hasValue()) {
			accept.setExecutable();
			return;
		}
		if (changed) {
			accept.setExecutable();
		} else {
			accept.setNotExecutable(I18NConstants.POPUP_SELECT_SUBMIT_NO_CHANGE);
		}
	}
	
	Command getCloseCommand() {
		return _closeAction;
	}

	CommandField getApplyCommand() {
		return (CommandField) getButtons().getFirstMemberRecursively(SelectorContext.ACCEPT_SELECTION);
	}

	List<?> getSelection() {
		TableField tableField = getTableField();
		return CollectionUtil.toList((Set<?>) tableField.getSelectionModel().getSelection());
	}

	private TableField getTableField() {
		return (TableField) getFormContext().getFirstMemberRecursively(SELECTION_TABLE);
	}

	/**
	 * {@link FormGroup}, that holds the button {@link CommandField}s of this context.
	 */
	public FormGroup getButtons() {
		return (FormGroup) getMember(SelectorContext.BUTTONS);
	}

	/**
	 * Returns the {@link ConfigKey} of the {@link TableField}.
	 * 
	 * <p>
	 * If a DialogTableConfigurationProvider exists, the {@link ConfigKey} will be expanded with
	 * {@link #SELECT_DIALOG_SUB_KEY}. Therefore tables in dialogs can have a different
	 * configuration than tables in formulas. If DialogTableConfigurationProvider is
	 * <code>null</code>, the tables in dialogs will have the same configuration as tables in
	 * formulas.
	 * </p>
	 * 
	 * @return {@link ConfigKey} of the {@link TableField}
	 */
	private ConfigKey getConfigKey() {
		TableConfigurationProvider config = _targetSelectField.getDialogTableConfigurationProvider();
		ConfigKey configKey;
		if (config != null) {
			configKey = ConfigKey.derived(_targetSelectField.getConfigKey(), SELECT_DIALOG_SUB_KEY);
		} else {
			configKey = _targetSelectField.getConfigKey();
		}
		return configKey;
	}

	static ResourceProvider toNoLinkResourceProvider(ColumnConfiguration column) {
		if (column.getResourceProvider() instanceof INoLinkResourceProvider) {
			/* Don't change the ResourceProvider for two reasons: 1. It is a performance
			 * optimization to not wrap a NoLinkResourceProvider into another
			 * NoLinkResourceProvider. 2. It is an intentional way for app developers to re-enable
			 * links in this column if they want that. They just need to create a subclass of the
			 * NoLinkResourceProvider that creates a link. */
			return column.getResourceProvider();
		}
		return createNoLinkResourceProvider(column.getResourceProvider());
	}

	private static final class SelectionFirstComparator<T> implements Comparator<T> {

		private final Set<?> _selectionSet;

		private final Comparator<? super T> _comparator;

		/**
		 * Creates a {@link SelectionFirstComparator}.
		 */
		private SelectionFirstComparator(Set<?> selectionSet, Comparator<? super T> comparator) {
			_selectionSet = selectionSet;
			_comparator = comparator;
		}

		@Override
		public int compare(T o1, T o2) {
			boolean selected1 = _selectionSet.contains(o1);
			boolean selected2 = _selectionSet.contains(o2);

			if (selected1 ^ selected2) {
				return selected1 ? -1 : 1;
			}

			return _comparator.compare(o1, o2);
		}
	}

	private final class FixedOptionClassProvider implements RowClassProvider {

		private static final String FIXED_OPTION_CSS_CLASS = "fixedOption";

		private Filter<Object> _fixedOptionsFilter;

		@SuppressWarnings("unchecked")
		FixedOptionClassProvider(Filter<?> fixedOptionsFilter) {
			_fixedOptionsFilter = (Filter<Object>) fixedOptionsFilter;
		}

		@Override
		public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row) {
			Object rowObject = view.getViewModel().getRowObject(displayedRow);
			if (_fixedOptionsFilter.accept(rowObject)) {
				return FIXED_OPTION_CSS_CLASS;
			}
			return null;
		}
	}

	private final class DialogSettingsProvider implements TableConfigurationProvider {
		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			String idColumn = table.getIDColumn();
			if (idColumn != null) {
				ColumnConfiguration column = table.getDeclaredColumn(idColumn);
				column.setCellRenderer(Column.toIdColumn(column.finalCellRenderer(), table));

				// Prevent generic logic from wrapping the cell again.
				table.setIDColumn(null);
			}

			makeTableFrozen(table, _optionsPerPage);
			makeTableContentsUnclickable(table);
			firstSortBySelectColumn(table);
		}

		private void makeTableFrozen(TableConfiguration table, int optionsPerPage) {
			table.setFixedColumnCount(1);
			table.setTableRenderer(DefaultTableRenderer.newInstance());
			table.setPageSizeOptions(optionsPerPage);
		}

		private void makeTableContentsUnclickable(TableConfiguration table) {
			for (ColumnConfiguration columnConfiguration : table.getElementaryColumns()) {
				if (!isSelectionColumn(columnConfiguration)) {
					noLinks(columnConfiguration);
				}
			}
		}

		private boolean isSelectionColumn(ColumnConfiguration columnConfiguration) {
			return columnConfiguration.getName().equals(TableControl.SELECT_COLUMN_NAME);
		}

		private void noLinks(ColumnConfiguration column) {
			column.setCellRenderer(ResourceRenderer.noLinks(column.finalCellRenderer()));
		}

		private void firstSortBySelectColumn(TableConfiguration table) {
			SortConfig selectAscending = SortConfigFactory.sortConfig(TableControl.SELECT_COLUMN_NAME, true);
			Iterator<SortConfig> origOrder = table.getDefaultSortOrder().iterator();
			List<SortConfig> newOrder;
			if (origOrder.hasNext()) {
				newOrder = new ArrayList<>();
				newOrder.add(selectAscending);
				do {
					SortConfig order = origOrder.next();
					if (TableControl.SELECT_COLUMN_NAME.equals(order.getColumnName())) {
						continue;
					}
					newOrder.add(order);
				} while (origOrder.hasNext());
			} else {
				newOrder = Collections.singletonList(selectAscending);
			}

			table.setDefaultSortOrder(newOrder);
		}
	}

	private static final class TreeSelectionProvider implements TableConfigurationProvider {

		/**
		 * Singleton {@link TreeSelectionProvider} instance.
		 */
		public static final TreeSelectionProvider INSTANCE = new TreeSelectionProvider();

		private TreeSelectionProvider() {
			// Singleton constructor.
		}

		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			table.setTree(true);

			if (hasTreeColumn(table)) {
				// Prevent generic logic from wrapping the cell agaion.
				table.setIDColumn(null);
			}
		}

		private boolean hasTreeColumn(TableConfiguration table) {
			for (ColumnConfiguration column : table.getDeclaredColumns()) {
				if (column.finalCellRenderer() instanceof TreeCellRenderer) {
					return true;
				}
			}
			return false;
		}
	}

	private final class SelectionChangedListener implements SelectionListener {

		private Set<?> _referenceSelection;

		SelectionChangedListener(Set<?> referenceSelection) {
			_referenceSelection = new HashSet<>(referenceSelection);
		}

		@Override
		public void notifySelectionChanged(SelectionModel model, SelectionEvent event) {
			boolean changed =
				!CollectionUtil.containsSame(_referenceSelection, event.getNewSelection());
			setExecutability(getApplyCommand(), changed);
		}
	}

	private static class FixedOptionsTreeTableFilter implements Filter<DefaultTreeTableNode> {

		private Filter<? super Object> _selectFieldFilter;

		FixedOptionsTreeTableFilter(Filter<? super Object> selectFieldFilter) {
			_selectFieldFilter = selectFieldFilter;
		}

		@Override
		public boolean accept(DefaultTreeTableNode node) {
			return _selectFieldFilter.accept(node.getBusinessObject());
		}
	}

	/**
	 * {@link SelectionStrategy}, that handles selection requests like the
	 * {@link DefaultSelectionStrategy}, but treating Ctrl modifier key is pressed always.
	 */
	public static class AddBySimpleClickStrategy implements SelectionStrategy {

		/** Static instance of {@link AddBySimpleClickStrategy} */
		public static final SelectionStrategy INSTANCE = new AddBySimpleClickStrategy();

		@Override
		public SelectionType getSelectionType(SelectionModel selectionModel, boolean ctrlKey, boolean shiftKey) {
			SelectionType selectionType;
			if (selectionModel instanceof SingleSelectionModel) {
				selectionType = SelectionType.TOGGLE_SINGLE;
			} else {
				if (shiftKey) {
					selectionType = SelectionType.TOGGLE_AREA;
				} else {
					selectionType = SelectionType.TOGGLE_SINGLE;
				}
			}
			return selectionType;
		}

	}
}
