/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.scripting.recorder.ref.DefaultNamedModel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.structure.DefaultExpandable;
import com.top_logic.layout.table.command.TableCommandProvider;
import com.top_logic.layout.table.control.SelectionVetoListener;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.dnd.NoTableDrag;
import com.top_logic.layout.table.dnd.NoTableDrop;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.dnd.TableDropTarget;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.Enabled;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableDataExport;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.table.renderer.FilterOptionsUtil;
import com.top_logic.layout.table.renderer.TableButtons;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Utils;

/**
 * The class {@link DefaultTableData} is the
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultTableData extends PropertyObservableBase implements TableData, LazyTypedAnnotatableMixin {

	private static final String TABLE_SETTINGS_KEY_PREFIX = "tableSettings.";

	@Inspectable
	private ToolBar _toolbar;

	@Inspectable
	private TableModel _tableModel;

	private TableViewModel _viewModel;

	private TableModelListener _multiSortDialogListener;

	private final TableDataOwner owner;

	private final ConfigKey _configKey;

	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();

	@Inspectable
	private SelectionModel _selectionModel;

	private TableModelListener _updateSelectionListener;

	private boolean _updateSelectionOnTableEvents;

	private List<SelectionVetoListener> _vetoListeners = Collections.emptyList();

	/**
	 * Creates a default {@link TableData}.
	 * 
	 * @param owner
	 *        Element holding this {@link TableData}.
	 * @param configKey
	 *        Personalization keys.
	 * @param updateSelectionOnTableEvents
	 *        True if the {@link TableData} updates his selection when table model events are sent.
	 */
	public DefaultTableData(TableDataOwner owner, ConfigKey configKey, boolean updateSelectionOnTableEvents) {
		assert owner != null;
		assert configKey != null;

		// May be null in case of a wrapping model providing the resources.
		//
		// assert resources != null;

		this.owner = owner;
		_configKey = configKey;
		this._updateSelectionOnTableEvents = updateSelectionOnTableEvents;
		this._tableModel = null;
		_selectionModel = new DefaultSingleSelectionModel(this);
		_updateSelectionListener = TableUtil.createUpdateSelectionListener(this);
	}

	@Override
	public ToolBar getToolBar() {
		return _toolbar;
	}

	@Override
	public void setToolBar(ToolBar newToolBar) {
		ToolBar oldToolBar = _toolbar;
		if (newToolBar == oldToolBar) {
			return;
		}

		boolean hasViewModel = _viewModel != null;
		if (hasViewModel) {
			removeTableButtons();
		}
		_toolbar = newToolBar;
		if (hasViewModel) {
			addTableButtons();
		}
		
		notifyListeners(TOOLBAR_PROPERTY, self(), oldToolBar, newToolBar);
	}

	@Override
	public TableModel getTableModel() {
		return _tableModel;
	}

	@Override
	public CheckScope getCheckScope() {
		return CheckScope.NO_CHECK;
	}

	@Override
	public void setTableModel(TableModel newModel) {
		TableModel oldModel = this.getTableModel();

		if (Utils.equals(newModel, oldModel)) {
			return;
		}

		removeTableButtons();
		
		TableViewModel oldViewModel = _viewModel;

		_tableModel = newModel;
		_viewModel = null;

		// Do not retrieve the TableViewModel, if has not already been created
		// so far. Its creation has to be delayed to the latest possible time.
		// During its creation, personal configuration is loaded, and validation
		// happens. At the time, the table data is initialized with a table model,
		// this is not yet possible.
		if (oldViewModel != null) {
			removeUpdateSelectionListener(oldViewModel);
			// Hot swap, inform listeners.
			TableViewModel newViewModel = getViewModel();
			notifyListeners(VIEW_MODEL_PROPERTY, self(), oldViewModel, newViewModel);
			return;
		}
		/* If this happens during rendering, its too late for registering a ToBeValidated. This
		 * happens for example if a SelectField is rendered as a table. The TableData will be
		 * created by the control during rendering. */
		if (getLayoutContext().isInCommandPhase()) {
			new TableViewModelEnforcer(this).register();
		}
	}

	private LayoutContext getLayoutContext() {
		return DefaultDisplayContext.getDisplayContext().getLayoutContext();
	}

	private void addTableButtons() {
		if (noButtons()) {
			return;
		}

		TableDataExport exporter = getConfiguration().getExporter();
		if (exporter != null) {
			ToolBarGroup exportGroup = _toolbar.defineGroup(CommandHandlerFactory.EXPORT_BUTTONS_GROUP);

			exportGroup.addButton(TableButtons.createExportButton(this, exporter));
		}

		TableViewModel viewModel = getViewModel();
		ToolBarGroup filterGroup = null;
		if (viewModel.hasGlobalFilter()) {
			filterGroup = _toolbar.defineGroup(CommandHandlerFactory.TABLE_BUTTONS_GROUP);
			filterGroup.addButton(TableButtons.createGlobalFilter(this));
		}

		/* Whether any button are added to re-configure the table. If there are no such commands it
		 * is not necessary to render a "reset to configuration" button. */
		boolean hasTableConfigButtons = false;
		if (viewModel.hasFilters()) {
			if (viewModel.hasFilterOptions()) {
				tableConfigGroup().addButton(FilterOptionsUtil.createFilterOptions(this));
				hasTableConfigButtons = true;
			}

			if (filterGroup == null) {
				filterGroup = _toolbar.defineGroup(CommandHandlerFactory.TABLE_BUTTONS_GROUP);
			}
			filterGroup.addButton(TableButtons.createResetFilter(this));
		}

		final CommandModel sortConfigOpener = createMultiSortDialogOpener(viewModel);
		if (sortConfigOpener != null) {
			tableConfigGroup().addButton(sortConfigOpener);
			hasTableConfigButtons = true;
		}

		if (showColumnConfigDialog(getTableModel())) {
			tableConfigGroup().addButton(TableButtons.createColumnSelector(this));
			hasTableConfigButtons = true;
		}

		for (Entry<String, Collection<TableCommandProvider>> commands : getConfiguration().getCommands().entrySet()) {
			for (TableCommandProvider provider : commands.getValue()) {
				ToolBarGroup group = _toolbar.defineGroup(commands.getKey());
				group.addButton(provider.createCommand(self()));
			}
		}
		if (getConfiguration().getSupportMultipleSettings()) {
			ConfigKey configKey;
			String key = getConfiguration().getMultipleSettingsKey();
			if (key == null || key.isEmpty()) {
				configKey = getViewModel().getConfigKey();
			} else {
				configKey = ConfigKey.named(TABLE_SETTINGS_KEY_PREFIX + key);
			}
			ToolBarGroup configGroup = tableConfigGroup();
			configGroup.addButton(TableButtons.createSettingsLoaderCommand(this, configKey));
			configGroup.addButton(TableButtons.createSettingsEditorCommand(this, configKey));
			hasTableConfigButtons = true;
		}

		if (hasTableConfigButtons) {
			tableConfigGroup().addButton(TableButtons.createResetTableCommand(this));
		}
	}

	private ToolBarGroup tableConfigGroup() {
		return _toolbar.defineGroup(CommandHandlerFactory.TABLE_CONFIG_GROUP);
	}

	private TableConfiguration getConfiguration() {
		return getTableModel().getHeader().getTableConfiguration();
	}

	private CommandModel createMultiSortDialogOpener(TableViewModel viewModel) {
		Enabled multiSort = viewModel.getTableConfiguration().getMultiSort();
		final CommandModel sortConfigOpener;
		switch (multiSort) {
			case never:
				sortConfigOpener = null;
				break;
			case auto:
				sortConfigOpener = TableButtons.createSortConfigOpener(this);
				_multiSortDialogListener = new TableModelListener() {

					@Override
					public void handleTableModelEvent(TableModelEvent event) {
						if (event.getType() != TableModelEvent.COLUMNS_UPDATE) {
							return;
						}
						setVisibility((TableViewModel) event.getSource(), sortConfigOpener);
					}
				};
				_viewModel.addTableModelListener(_multiSortDialogListener);
				setVisibility(viewModel, sortConfigOpener);
				break;
			case always:
				sortConfigOpener = TableButtons.createSortConfigOpener(this);
				break;
			default:
				throw new UnreachableAssertion("No such option: " + multiSort);
		}
		return sortConfigOpener;
	}

	static void setVisibility(TableViewModel viewModel, CommandModel model) {
		int sortableCnt = 0;
		for (int n = 0, cnt = viewModel.getColumnCount(); n < cnt; n++) {
			if (viewModel.isSortable(n)) {
				sortableCnt++;

				if (sortableCnt > 1) {
					model.setVisible(true);
					return;
				}
			}
		}
		model.setVisible(false);
	}

	private boolean showColumnConfigDialog(TableModel applicationModel) {
		return hasMultipleSelectableColumns(applicationModel) &&
			applicationModel.getTableConfiguration().getColumnCustomization() != ColumnCustomization.NONE;
	}

	private boolean hasMultipleSelectableColumns(TableModel applicationModel) {
		int selectableColumnCount = 0;
		for (String columnName : applicationModel.getColumnNames()) {
			if (isSelectableColumn(applicationModel, columnName)) {
				selectableColumnCount++;
				if (selectableColumnCount > 1) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isSelectableColumn(TableModel tableModel, String columnName) {
		if (isIgnoredColumn(columnName)) {
			return false;
		} else {
			ColumnConfiguration columnConfiguration = tableModel.getColumnDescription(columnName);
			return columnConfiguration.getVisibility() == DisplayMode.visible
				|| columnConfiguration.getVisibility() == DisplayMode.hidden
				|| columnConfiguration.getVisibility() == DisplayMode.mandatory;
		}
	}

	private boolean isIgnoredColumn(String columnName) {
		return columnName.equals(TableControl.SELECT_COLUMN_NAME);
	}

	private void removeTableButtons() {
		if (noButtons()) {
			return;
		}

		TableUtil.removeTableButtons(_toolbar, this);
		if (_viewModel != null && _multiSortDialogListener != null) {
			_viewModel.removeTableModelListener(_multiSortDialogListener);
			_multiSortDialogListener = null;
		}
	}

	private boolean noButtons() {
		return _toolbar == null || _tableModel == null;
	}

	@Override
	public final TableViewModel getViewModel() {
		if (_viewModel == null && _tableModel != null) {
			_viewModel = getViewModelFromTableModel(_tableModel);
			addUpdateSelectionListener(_viewModel);
			addTableButtons();
		}

		return _viewModel;
	}

	private void addUpdateSelectionListener(TableViewModel viewModel) {
		if (_updateSelectionOnTableEvents) {
			viewModel.getApplicationModel().addTableModelListener(_updateSelectionListener);
		}
	}

	private void removeUpdateSelectionListener(TableViewModel viewModel) {
		if (_updateSelectionOnTableEvents) {
			viewModel.getApplicationModel().removeTableModelListener(_updateSelectionListener);
		}
	}

	private TableViewModel getViewModelFromTableModel(TableModel tableModel) {
		if (tableModel instanceof TableViewModel) {
			return (TableViewModel) tableModel;
		} else {
			return new TableViewModel(tableModel, _configKey);
		}
	}

	@Override
	public ResourceView getTableResources() {
		return getTableModel().getTableConfiguration().getResPrefix();
	}

	/**
	 * Creates an instance of this class to be used as implementation for the specified object.
	 * 
	 * @param proxy
	 *        the object to create a implementation instance for
	 * @param owner
	 *        the {@link TableData#getOwner() owner} of the new {@link TableData}.
	 * @param configKey
	 *        The key to store personal table configuration with.
	 * @param updateSelectionOnTableEvents
	 *        True if the {@link TableData} updates his selection when table model events are sent.
	 * 
	 * @return the newly created implementation instance for the specified object
	 */
	public static TableData createTableDataImplementation(TableData proxy, TableDataOwner owner, ConfigKey configKey,
			boolean updateSelectionOnTableEvents) {
		return new TableDataImplementation(proxy, owner, configKey, updateSelectionOnTableEvents);
    }

	/**
	 * Returns the reference to the actual object. In case the receiver acts as a proxied object for
	 * some other instance, the proxy instance is returned. Otherwise, the receiver itself is
	 * returned.
	 * 
	 * @return the actual object
	 */
    protected TableData self() {
        return this;
    }

	/**
	 * This method creates a {@link TableData} that has no personal table configuration and is not
	 * accessible for scripting tests.
	 * 
	 * @param table
	 *        the {@link TableModel} for the new {@link DefaultTableData}
	 */
	public static final TableData createAnonymousTableData(TableModel table) {
		return createTableData(NoTableDataOwner.INSTANCE, table, ConfigKey.none());
	}

	/**
	 * Creates a {@link TableData}.
	 * 
	 * @param owner
	 *        See {@link #getOwner()}.
	 * @param tableModel
	 *        The initial {@link TableModel}.
	 * @param configKey
	 *        The {@link ConfigKey}.
	 */
	public static TableData createTableData(TableDataOwner owner, TableModel tableModel, ConfigKey configKey) {
		DefaultTableData result = new DefaultTableData(owner, configKey, true);
		result.setTableModel(tableModel);
		result.setToolBar(new DefaultToolBar(result, new DefaultExpandable()));
		return result;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(self());
	}

	@Override
	public final TableDataOwner getOwner() {
		return owner;
	}

	@Override
	public void setSelectionModel(SelectionModel selectionModel) {
		SelectionModel oldSelectionModel = _selectionModel;
		_selectionModel = selectionModel;
		notifyListeners(SELECTION_MODEL_PROPERTY, self(), oldSelectionModel, selectionModel);
	}

	@Override
	public SelectionModel getSelectionModel() {
		return _selectionModel;
	}

	@Override
	public TableDragSource getDragSource() {
		if (_tableModel == null) {
			return NoTableDrag.INSTANCE;
		}
		return _tableModel.getTableConfiguration().getDragSource();
	}

	@Override
	public List<TableDropTarget> getDropTargets() {
		if (_tableModel == null) {
			return Arrays.asList(NoTableDrop.INSTANCE);
		}
		return _tableModel.getTableConfiguration().getDropTargets();
	}

	@Override
	public ContextMenuProvider getContextMenu() {
		if (_tableModel == null) {
			return NoContextMenuProvider.INSTANCE;
		}
		return _tableModel.getTableConfiguration().getContextMenu();
	}

	@Override
	public void addSelectionVetoListener(SelectionVetoListener listener) {
		if (_vetoListeners == Collections.<SelectionVetoListener> emptyList()) {
			_vetoListeners = new ArrayList<>();
		}
		_vetoListeners.add(listener);
	}

	@Override
	public void removeSelectionVetoListener(SelectionVetoListener listener) {
		if (_vetoListeners.isEmpty()) {
			return;
		}
		_vetoListeners.remove(listener);
	}

	@Override
	public Collection<SelectionVetoListener> getSelectionVetoListeners() {
		return _vetoListeners;
	}

	@Override
	public InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore() {
		return _properties;
	}

	@Override
	public void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties) {
		_properties = newProperties;
	}

	private static final class TableDataImplementation extends DefaultTableData {
		private final TableData proxy;

		public TableDataImplementation(TableData proxy, TableDataOwner owner, ConfigKey configKey,
				boolean updateSelectionOnTableEvents) {
			super(owner, configKey, updateSelectionOnTableEvents);
			this.proxy = proxy;
			setToolBar(new DefaultToolBar(proxy, new DefaultExpandable()));
		}

		@Override
		protected TableData self() {
		    return proxy;
		}

		@Override
		public boolean isSet(Property<?> property) {
			return proxy.isSet(property);
		}

		@Override
		public <T> T get(Property<T> property) {
			return proxy.get(property);
		}

		@Override
		public <T> T set(Property<T> property, T value) {
			return proxy.set(property, value);
		}

		@Override
		public <T> T reset(Property<T> property) {
			return proxy.reset(property);
		}
	}

	/**
	 * This class is used, is used if there is no {@link TableDataOwner}. (For whatever reason.)
	 */
	public static final class NoTableDataOwner extends DefaultNamedModel implements TableDataOwner {
	
		/**
		 * Singleton {@link DefaultTableData.NoTableDataOwner} instance.
		 */
		public static final DefaultTableData.NoTableDataOwner INSTANCE = new DefaultTableData.NoTableDataOwner();
		
		private NoTableDataOwner() {
			// Singleton constructor.
		}
	
		@Override
		public TableData getTableData() {
			throw new UnsupportedOperationException("There is no " + TableDataOwner.class.getSimpleName()
				+ " for this " + TableData.class.getSimpleName() + "!");
		}
	
	}
}
