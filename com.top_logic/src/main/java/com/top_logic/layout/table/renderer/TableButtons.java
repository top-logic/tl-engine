/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import static com.top_logic.layout.table.TableViewModel.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.DynamicDelegatingCommandModel;
import com.top_logic.layout.basic.PopupCommand;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.AbstractDynamicCommand;
import com.top_logic.layout.form.model.AbstractExecutabilityModel;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ConstraintCheckingVetoListener;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.selection.FilterBasedMarker;
import com.top_logic.layout.form.selection.FixedOptionAdorner;
import com.top_logic.layout.form.selection.MultiSelectDialog;
import com.top_logic.layout.form.selection.OptionRenderer;
import com.top_logic.layout.form.selection.SelectDialogConfig;
import com.top_logic.layout.form.selection.SelectDialogProvider;
import com.top_logic.layout.form.selection.SelectorContext;
import com.top_logic.layout.list.ListItemCssAdorner;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ui.button.LabeledButtonNaming;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData.NoTableDataOwner;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataListener;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableModelUtils;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.SortConfigDialog;
import com.top_logic.layout.table.control.StructuredColumnLabels;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableControl.OpenFilterDialogAction;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.filter.TableFilterEvent;
import com.top_logic.layout.table.filter.TableFilterEvent.FilterEventTypes;
import com.top_logic.layout.table.filter.TableFilterListener;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.Header;
import com.top_logic.layout.table.model.LoadNamedSetting;
import com.top_logic.layout.table.model.ManageMultipleTableSettings;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableDataExport;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarChangeListener;
import com.top_logic.layout.toolbar.ToolBarOwner;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Utilities for rendering the table header.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableButtons {

	static final ExecutableState DISABLED_FILTER_NOT_ACTIVE =
		ExecutableState.createDisabledState(I18NConstants.NOT_EXECUTABLE_FILTER_NOT_ACTIVE);

	/**
	 * Warn the user that the list must not be empty.
	 */
	public static final class ColumnNumberConstraint extends AbstractConstraint {

		private final int _maxColumns;

		public ColumnNumberConstraint(int maxColumns) {
			_maxColumns = maxColumns;
		}

		@Override
		public boolean check(Object value) throws CheckException {
			List<?> selection = (List<?>) value;
			int selectionSize = selection.size();
			if (isEmptySelection(selection)) {
				throw new CheckException(Resources.getInstance().getString(I18NConstants.ERR_NO_EMPTY_COLUMN_LIST));
			} else if (selectionSize > _maxColumns) {
				throw new CheckException(Resources.getInstance().getMessage(I18NConstants.ERR_TOO_MANY_COLUMNS_SELECTED__CNT_MAX,
					selectionSize, _maxColumns));
			}
			return true;
		}

		private boolean isEmptySelection(List<?> selection) {
			return selection.size() == 0 || (selection.size() == 1 && selection.get(0) == TableSeparator.INSTANCE);
		}
	}

	/**
	 * Marker to find the internal export command in a tool-bar group.
	 */
	public static final Property<Boolean> INTERNAL_EXPORT = TypedAnnotatable.property(Boolean.class, "internalExport");

	/**
	 * Creates a {@link CommandModel} which exports the given table data.
	 * 
	 * @param table
	 *        The table data to export.
	 * @throws IllegalArgumentException
	 *         if the table does not provide an exporter.
	 * 
	 * @see TableConfiguration#getExporter()
	 */
	public static CommandModel createExportButton(TableData table) {
		TableConfiguration tableConfig = table.getTableModel().getTableConfiguration();
		TableDataExport exporter = tableConfig.getExporter();
		if (exporter == null) {
			throw new IllegalArgumentException("Given table can not be exported.");
		}
		return createExportButton(table, exporter);
	}

	/**
	 * Creates a {@link CommandModel} which exports the given table data with the given exporter.
	 * 
	 * @param table
	 *        The table data to export.
	 * @param exporter
	 *        The exported that actually exports the given table.
	 */
	public static CommandModel createExportButton(TableData table, TableDataExport exporter) {
		TableExportCommand exportTable = new TableExportCommand(table, exporter);
		final CommandModel button = CommandModelFactory.commandModel(exportTable);
		initButton(button, table,
			com.top_logic.layout.table.component.Icons.EXPORT_GRID,
			com.top_logic.layout.table.component.Icons.EXPORT_GRID_DISABLED,
			I18NConstants.EXPORT_EXCEL);
		button.set(INTERNAL_EXPORT, Boolean.TRUE);
		return button;
	}

	private static class TableExportCommand extends AbstractDynamicCommand {

		private final TableData _table;

		private final TableDataExport _exporter;

		public TableExportCommand(TableData table, TableDataExport exporter) {
			_table = table;
			_exporter = exporter;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			return _exporter.exportTableData(context, _table);
		}

		@Override
		protected ExecutableState calculateExecutability() {
			return _exporter.getExecutability(_table);
		}

	}

	/**
	 * The button, which resets all personal configuration of table.
	 */
	public static CommandModel createResetTableCommand(final TableData table) {
		Command resetTableCommand = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				table.getViewModel().resetToConfiguration();
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		return createButton(resetTableCommand, table, Icons.RESET_TABLE,
			I18NConstants.RESET_TABLE_CONFIGURATION);
	}

	/**
	 * The button that automatically fits the width of each column to its widest content.
	 */
	public static CommandModel createAutofitColumnsCommand(final TableData table) {
		Command autofitColumnsCommand = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				if (table.isSet(TableControl.CONTROL_ID_PROPERTY)) {
					commandContext.getWindowScope().getTopLevelFrameScope().addClientAction(new JSFunctionCall(
						table.get(TableControl.CONTROL_ID_PROPERTY), "TABLE", "autofitColumnWidths"));
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		return createButton(autofitColumnsCommand, table, Icons.AUTO_FIT_COLUMNS,
			I18NConstants.AUTO_FIT_COLUMNS);
	}

	/**
	 * The button opening the multi-sort dialog.
	 */
	public static CommandModel createSortConfigOpener(final TableData table) {
		Command openSortConfig = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				SortConfigDialog.openDialog(commandContext, table);
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		return createButton(openSortConfig, table, Icons.SORT_BUTTON_ICON, I18NConstants.OPEN_SORT_CONFIG_DIALOG_TOOLTIP);
	}

	/**
	 * The button opening the column chooser.
	 */
	public static CommandModel createColumnSelector(final TableData table) {
		Command openColumnSelector = new ConfigureColumnsCommand(table, ColumnLabelProvider.newInstance(table));

		TableModel model = table.getTableModel();
		boolean canSelectColumns =
			model.getTableConfiguration().getColumnCustomization() == ColumnCustomization.SELECTION;
		ResKey tooltipKey = canSelectColumns ? I18NConstants.OPEN_COLUMN_CONFIG_DIALOG_TOOLTIP : I18NConstants.OPEN_COLUMN_ORDER_DIALOG_TOOLTIP;
		CommandModel button = createButton(openColumnSelector, table, Icons.TBL_COLUMN_CONFIG, tooltipKey);

		// Do not record opening, because closing is also not recorded.
		ScriptingRecorder.annotateAsDontRecord(button);
		return button;
	}

	/**
	 * The reset button for filters.
	 */
	public static CommandModel createResetFilter(final TableData table) {
		Command theResetCommand = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext aCommandContext) {
				TableViewModel theModel = table.getViewModel();

				theModel.resetAllFilters();
				theModel.saveFilter();

		    	return HandlerResult.DEFAULT_RESULT;
			}
		};

		ExecutabilityModel executability = new AbstractExecutabilityModel() {
			@Override
			protected ExecutableState calculateExecutability() {
				if (!table.getViewModel().hasFilters()) {
					return ExecutableState.NOT_EXEC_HIDDEN;
				}
				if (!table.getViewModel().hasActiveFilters()) {
					return DISABLED_FILTER_NOT_ACTIVE;
				}
				return ExecutableState.EXECUTABLE;
			}
		};

		FilterListeningCommand commandModel = new FilterListeningCommand(theResetCommand, executability);
		commandModel.attachTable(table);

		initButton(commandModel, table, Icons.REMOVE_FILTER, Icons.REMOVE_FILTER_DISABLED, I18NConstants.REMOVE_FILTER_TOOLTIP);
		return commandModel;
	}

	/**
	 * Write the reset button for filter of specified column.
	 */
	public static void writeResetFilterForColumn(DisplayContext aContext, TagWriter anOut, final TableData table,
			final String columnName) throws IOException {

		TableViewModel viewModel = table.getViewModel();
		Command theResetCommand = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext aCommandContext) {
				viewModel.getFilter(columnName).reset();
		
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		ExecutabilityModel executability = new AbstractExecutabilityModel() {
			@Override
			protected ExecutableState calculateExecutability() {
				return ExecutableState.visibleIf(isFilterActive(viewModel, columnName));
			}
		};

		CommandModel theCommandModel = CommandModelFactory.commandModel(theResetCommand, executability);

		boolean filterActive = isFilterActive(viewModel, columnName);
		theCommandModel.setVisible(filterActive);
		if (filterActive) {
			theCommandModel.setExecutable();
		} else {
			theCommandModel.setNotExecutable(I18NConstants.NOT_EXECUTABLE_FILTER_NOT_ACTIVE);
		}
		
		initButton(theCommandModel, table, Icons.REMOVE_FILTER, Icons.REMOVE_FILTER_DISABLED,
			I18NConstants.REMOVE_CERTAIN_FILTER_TOOLTIP);

		ButtonControl button = new TableFilterButtonControl(theCommandModel, viewModel.getFilter(columnName));
		
		writeToolbarView(aContext, anOut, button);
	}

	static boolean isFilterActive(TableViewModel viewModel, final String columnName) {
		return viewModel.getFilter(columnName) != null &&
			viewModel.getFilter(columnName).isActive();
	}

	/**
	 * The opener button for global table filters.
	 */
	public static CommandModel createGlobalFilter(final TableData tableData) {
		OpenGlobalFilter openGlobalFilter = new OpenGlobalFilter(tableData);

		ExecutabilityModel exectutable = new AbstractExecutabilityModel() {
			@Override
			protected ExecutableState calculateExecutability() {
				return ExecutableState.visibleIf(tableData.getViewModel().hasGlobalFilter());
			}
		};
		
		FilterListeningCommand commandModel = new OpenGlobalFilterCommandModel(openGlobalFilter, exectutable) {
			@Override
			protected void updateFilterState() {
				super.updateFilterState();

				ThemeImage filterImage;
				if (tableData.getViewModel().hasActiveGlobalFilter()) {
					filterImage = Icons.FILTER_GLOBAL_ACTIVE;
				} else {
					filterImage = Icons.FILTER_GLOBAL;
				}
				setImage(filterImage);
			}
		};
		initButton(commandModel, tableData, null, null, I18NConstants.GLOBAL_FILTER_TOOLTIP);

		commandModel.updateFilterState();
		commandModel.attachTable(tableData);

		return commandModel;
	}

	/**
	 * {@link CommandModel} for {@link OpenGlobalFilter}.
	 * 
	 * <p>
	 * This is an extra class to be able to identify it during application testing.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class OpenGlobalFilterCommandModel extends FilterListeningCommand {

		OpenGlobalFilterCommandModel(OpenGlobalFilter aCommand, ExecutabilityModel executability) {
			super(aCommand, executability);
		}

	}

	/**
	 * {@link Command} opening the global filter popup.
	 */
	public static final class OpenGlobalFilter extends PopupCommand {
		private final TableData _tableData;

		OpenGlobalFilter(TableData tableData) {
			_tableData = tableData;
		}

		@Override
		public HandlerResult showPopup(DisplayContext context, PopupHandler handler) {
			String filterPosition = TableViewModel.GLOBAL_TABLE_FILTER_ID;
			return OpenFilterDialogAction.openTableFilter(context, handler, _tableData, filterPosition);
		}
	}

	private static CommandModel createButton(Command command, TableData table, ThemeImage image, ResKey labelKey) {
		CommandModel button = CommandModelFactory.commandModel(command);
		initButton(button, table, image, null, labelKey);
		return button;
	}

	static void write(DisplayContext context, TagWriter out, HTMLFragment view) throws IOException {
		writeToolbarView(context, out, view);
	}

	static void initButton(CommandModel button, TableData table, ThemeImage image, ThemeImage disabledImage,
			ResKey labelKey) {
		if (table.getOwner() != NoTableDataOwner.INSTANCE) {
			button.set(LabeledButtonNaming.BUSINESS_OBJECT, table);
		}
		button.setImage(image);
		button.setNotExecutableImage(disabledImage);
		button.setTooltip(ResKey.fallback(labelKey.tooltip(), labelKey));
		button.setAltText(labelKey);
		button.setLabel(labelKey);
	}

	/**
	 * Writes a command button within the table toolbar.
	 */
	public static void writeToolbarView(DisplayContext context, TagWriter out, HTMLFragment view)
			throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "tblButton");
		out.endBeginTag();
		{
			view.write(context, out);
		}
		out.endTag(SPAN);
	}

	/**
	 * Dialog to configure the columns to display.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ConfigureColumnsCommand implements Command {
	
		protected final TableData _table;
	
		protected final LabelProvider _columnLabels;
	
		public ConfigureColumnsCommand(TableData table, LabelProvider columnLabels) {
			_table = table;
			_columnLabels = columnLabels;
		}
	
		@Override
		public HandlerResult executeCommand(DisplayContext commandContext) {
			FormContext selectContext =
				new FormContext("columnSelect", I18NConstants.TABLE);
			final TableViewModel model = viewModel();
			SelectField columnsSelect = getColumnsSelectField(model);
	
			selectContext.addMember(columnsSelect);
			ScriptingRecorder.annotateAsDontRecord(selectContext);
	
			final WindowScope windowScope = commandContext.getWindowScope();
			columnsSelect.addValueListener(new ValueListener() {
				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					SelectField columnSelect = (SelectField) field;
					List<?> selectedColumns = columnSelect.getSelection();
					List<String> columnNames = new ArrayList<>(selectedColumns.size());
					int fixedColumns = NO_FIXED_COLUMN_PERSONALIZATION;
					for (int i = 0; i < selectedColumns.size(); i++) {
						Object selectedObject = selectedColumns.get(i);
						if (selectedObject != TableSeparator.INSTANCE) {
							columnNames.add(((Column) selectedObject).getName());
						} else {
							fixedColumns = i;
						}
					}
					if (ScriptingRecorder.isRecordingActive()) {
						ScriptingRecorder.recordSetTableColumns(tableData(), columnNames);
					}
					int firstAddedColumn = getIndexOfFirstAddedColumn(oldValue, newValue);
					updateColumns(windowScope, model, columnNames, fixedColumns, firstAddedColumn);
				}

				@SuppressWarnings("unchecked")
				private int getIndexOfFirstAddedColumn(Object oldValue, Object newValue) {
					ArrayList<Object> visibleColumns = new ArrayList<>((List<Object>) newValue);
					visibleColumns.remove(TableSeparator.INSTANCE);
					ArrayList<Object> addedColumns = new ArrayList<>((List<Object>) newValue);
					addedColumns.removeAll((List<?>) oldValue);
					if (addedColumns.size() > 0) {
						int firstAddedColumnIndex = visibleColumns.indexOf(addedColumns.get(0));
						return firstAddedColumnIndex;
					} else {
						return -1;
					}
				}

				@SuppressWarnings("hiding")
				private void scrollToColumnIndex(TableViewModel model, int columnIndex) {
					model.getClientDisplayData().getVisiblePaneRequest()
						.setColumnRange(IndexRange.singleIndex(columnIndex));
				}

				@SuppressWarnings("hiding")
				void updateColumns(final WindowScope window, final TableViewModel model, final List<String> columnNames,
						final int fixedColumns, final int firstAddedColumn) {
					try {
						model.setColumns(columnNames);
						if (fixedColumns > NO_FIXED_COLUMN_PERSONALIZATION) {
							model.setPersonalFixedColumns(fixedColumns);
						}
						if (firstAddedColumn > -1) {
							scrollToColumnIndex(model, firstAddedColumn);
						}
					} catch (VetoException ex) {
						ex.setContinuationCommand(new Command() {
							@Override
							public HandlerResult executeCommand(DisplayContext context) {
								updateColumns(window, model, columnNames, fixedColumns, firstAddedColumn);
								return HandlerResult.DEFAULT_RESULT;
							}
						});
						ex.process(window);
					}
				}
			});
	
			MultiSelectDialog dialog = createMultiSelectDialog(columnsSelect);
	
			dialog.open(windowScope);
			return HandlerResult.DEFAULT_RESULT;
		}

		private TableViewModel viewModel() {
			return tableData().getViewModel();
		}

		private TableModel tableModel() {
			return tableData().getTableModel();
		}

		TableData tableData() {
			return _table;
		}

		private SelectField getColumnsSelectField(TableViewModel model) {
			Header header = model.getHeader();
			List<String> allColumnNamesConfigSortOrder = model.getApplicationModel().getColumnNames();
			List<Column> defaultColumns = new ArrayList<>();
			final Set<Column> mandatoryColumns = new HashSet<>();
			for (String columnName : allColumnNamesConfigSortOrder) {
				Column column = header.getColumn(columnName);
				if (column.isExcluded()) {
					continue;
				}
				ColumnConfiguration columnConfig = column.getConfig();
				if (columnConfig.isMandatory()) {
					mandatoryColumns.add(column);
				}
				if (columnConfig.isVisible()) {
					defaultColumns.add(column);
				}
			}
	
			List<?> currentColumns = getDialogColumns(header.getColumns());
			List<?> allColumns = getDialogColumns(header.getAllElementaryColumns());
	
			TableConfiguration tableConfiguration = tableModel().getTableConfiguration();
			boolean canSelectColumns = tableConfiguration.getColumnCustomization() == ColumnCustomization.SELECTION;
			DialogColumnLabelProvider dialogColumnLabelProvider =
				new DialogColumnLabelProvider(new StructuredColumnLabels(_columnLabels));
			SelectField columnsSelect =
				FormFactory.newSelectField(canSelectColumns ? "selectedColumns" : "columnOrder", allColumns, true,
					false);
			columnsSelect.setValue(currentColumns);
			columnsSelect.setDefaultValue(getDefaultDialogColumns(defaultColumns));
			columnsSelect.setOptionComparator(LabelComparator.newCachingInstance(dialogColumnLabelProvider));
			columnsSelect.setCustomOrder(true);
			columnsSelect.setOptionLabelProvider(dialogColumnLabelProvider);
			if (canSelectColumns) {
				columnsSelect.setFixedOptions(new SetFilter(getFixedDialogColumns(mandatoryColumns)));
				columnsSelect.addConstraint(new ColumnNumberConstraint(tableConfiguration.getMaxColumns()));
			}
			columnsSelect.addValueVetoListener(new ConstraintCheckingVetoListener());
			return columnsSelect;
		}

		private List<?> getDialogColumns(List<Column> columns) {
			int fixedColumnCount = _table.getViewModel().getFixedColumnCount();
			return getDialogColumns(columns, fixedColumnCount);
		}

		private List<?> getDialogColumns(List<Column> columns, int fixedColumnCount) {
			ArrayList<Object> dialogColumns = new ArrayList<>(columns.size() + 1);
			int i = 0;
			for (; i < columns.size(); i++) {
				if(i == fixedColumnCount) {
					dialogColumns.add(TableSeparator.INSTANCE);
				}
				dialogColumns.add(columns.get(i));
			}
			if (i == fixedColumnCount) {
				dialogColumns.add(TableSeparator.INSTANCE);
			}
			return dialogColumns;
		}

		private List<?> getDefaultDialogColumns(List<Column> defaultColumns) {
			TableViewModel viewModel = _table.getViewModel();
			int fixedColumnCount =
				TableModelUtils.getConfiguredFixedColumns(viewModel.getTableConfiguration(), defaultColumns);
			return getDialogColumns(defaultColumns, fixedColumnCount);
		}

		private Set<?> getFixedDialogColumns(Set<Column> columns) {
			HashSet<Object> fixedColumns = new HashSet<>();
			for (Column column : columns) {
				fixedColumns.add(column);
			}
			if (_table.getViewModel().getFixedColumnCount() != TableViewModel.NO_FIXED_COLUMN_PERSONALIZATION) {
				fixedColumns.add(TableSeparator.INSTANCE);
			}
			return fixedColumns;
		}

		/**
		 * Allow subclasses to create Subclasses of a MultiSelectDialog.
		 */
		@SuppressWarnings("unchecked")
		protected MultiSelectDialog createMultiSelectDialog(SelectField columnsSelect) {
			SelectDialogConfig config = SelectDialogProvider.newDefaultConfig();
			boolean canSelectColumns =
				tableModel().getTableConfiguration().getColumnCustomization() == ColumnCustomization.SELECTION;
			config.setShowOptions(canSelectColumns);
			if (!canSelectColumns) {
				// Make sure that the buttons appear on the right side of the selection.
				config.setLeftToRight(false);
			}
			Filter<Object> fixedOptions = columnsSelect.getFixedOptionsNonNull();
			OptionRenderer dialogListRenderer = new OptionRenderer(true);
			dialogListRenderer.addListItemAdorner(new FixedOptionAdorner(fixedOptions));
			dialogListRenderer.addListItemAdorner(TableSeparatorAdorner.INSTANCE);
			dialogListRenderer.setFixedOptionMarker(new FilterBasedMarker(fixedOptions));
			config.setDialogListRenderer(dialogListRenderer);
			MultiSelectDialog dialog = new MultiSelectDialog(columnsSelect, config) {
				@Override
				protected SelectorContext createSelectorContext(DialogWindowControl dialogControl) {
					final SelectorContext selectorContext = super.createSelectorContext(dialogControl);
	
					CommandField resetButton = new CommandField("reset") {
						@Override
						public HandlerResult executeCommand(DisplayContext context) {
							SelectorContext selectorContext = (SelectorContext) getFormContext();
							selectorContext.restoreDefaultSelection();
							return HandlerResult.DEFAULT_RESULT;
						}
					};

					insertButton(selectorContext.getButtons(), 0, resetButton);

					return selectorContext;
				}

				protected void insertButton(FormGroup buttons, int index, CommandField newButton) {
					ArrayList<FormMember> existingButtons = CollectionUtil.toList(buttons.getMembers());
					for (FormMember member : existingButtons) {
						buttons.removeMember(member);
					}
					existingButtons.add(index, newButton);

					for (FormMember member : existingButtons) {
						buttons.addMember(member);
					}
				}
			};
			return dialog;
		}
	}

	/**
	 * {@link ButtonControl} that is attached to a {@link TableFilter} and is visible only, if the
	 * specified filter is active.
	 */
	public static class TableFilterButtonControl extends ButtonControl implements TableFilterListener {
		private TableFilter filter;

		TableFilterButtonControl(CommandModel model, TableFilter filter) {
			super(model);
			this.filter = filter;
		}

		@Override
		protected void internalAttach() {
			super.internalAttach();
			if (filter != null) {
				filter.addTableFilterListener(this);
			}
		}

		@Override
		protected void internalDetach() {
			super.internalDetach();
			if (filter != null) {
				filter.removeTableFilterListener(this);
			}
		}

		@Override
		public void handleTableFilterEvent(TableFilterEvent event) {
			if (event.getEventType() == FilterEventTypes.CONFIGURATION_UPDATE ||
				event.getEventType() == FilterEventTypes.DEACTIVATION) {
				boolean filterActive = filter.isActive();
				getModel().setVisible(filterActive);
				if (filterActive) {
					getModel().setExecutable();
				} else {
					getModel().setNotExecutable(I18NConstants.NOT_EXECUTABLE_FILTER_NOT_ACTIVE);
				}
			}
		}
	}

	static class FilterListeningCommand extends DynamicDelegatingCommandModel implements TableModelListener,
			TableDataListener, ToolBarChangeListener {

		private TableViewModel _observedViewModel;

		private TableData _observedTable;

		private ToolBar _myToolbar;

		public FilterListeningCommand(Command aCommand, ExecutabilityModel executability) {
			super(aCommand, executability);
		}

		@Override
		public void handleTableModelEvent(TableModelEvent event) {
			switch (event.getType()) {
				case TableModelEvent.COLUMN_FILTER_UPDATE:
				case TableModelEvent.COLUMNS_UPDATE:
					updateFilterState();
					break;
			}
		}

		protected void updateFilterState() {
			// Hook for subclasses.
		}

		@Override
		public void notifyTableViewModelChanged(TableData source, TableViewModel oldValue, TableViewModel newValue) {
			detachView();
			attachView(newValue);
		}

		@Override
		public void notifyToolbarChange(ToolBarOwner sender, ToolBar oldValue, ToolBar newValue) {
			if (newValue != _myToolbar) {
				detachView();
				detachTable();
			}
		}

		@Override
		public void notifySelectionModelChanged(TableData source, SelectionModel oldValue, SelectionModel newValue) {
			// FilterListeningCommand shall not listen to TableData's selection model.
			throw new UnsupportedOperationException();
		}

		public void attachTable(final TableData table) {
			_observedTable = table;
			_myToolbar = table.getToolBar();
			_observedTable.addListener(TableData.VIEW_MODEL_PROPERTY, this);
			_observedTable.addListener(TableData.TOOLBAR_PROPERTY, this);
			attachView(_observedTable.getViewModel());
		}

		private void detachTable() {
			if (_observedTable != null) {
				_observedTable.removeListener(TableData.VIEW_MODEL_PROPERTY, this);
				_observedTable.removeListener(TableData.TOOLBAR_PROPERTY, this);
				_observedTable = null;
			}
		}

		private void attachView(TableViewModel viewModel) {
			if (viewModel != null) {
				_observedViewModel = viewModel;
				_observedViewModel.addTableModelListener(this);
			}
		}

		private void detachView() {
			if (_observedViewModel != null) {
				_observedViewModel.removeTableModelListener(this);
				_observedViewModel = null;
			}
		}

	}

	static class TableSeparator {

		/** Marker instance for table separators */
		public static final TableSeparator INSTANCE = new TableSeparator();

		private TableSeparator() {
			// Singleton
		}
	}

	static class DialogColumnLabelProvider implements LabelProvider {

		private LabelProvider _columnLabelProvider;

		DialogColumnLabelProvider(LabelProvider columnLabelProvider) {
			_columnLabelProvider = columnLabelProvider;
		}

		@Override
		public String getLabel(Object object) {
			if (object != TableSeparator.INSTANCE) {
				return _columnLabelProvider.getLabel(object);
			} else {
				return "";
			}
		}
	}

	static class TableSeparatorAdorner implements ListItemCssAdorner {

		public static final ListItemCssAdorner INSTANCE = new TableSeparatorAdorner();

		@Override
		public void addClasses(Appendable out, Object listItem, boolean isSelected, boolean isSelectable,
				boolean isFocused) throws IOException {
			if (listItem == TableSeparator.INSTANCE) {
				out.append("tableSeparator");
			}
		}
	}

	/**
	 * Creates a {@link CommandModel} using {@link LoadNamedSetting} command.
	 * 
	 * @param table
	 *        The {@link TableData} to create {@link CommandModel} for.
	 */
	public static CommandModel createSettingsLoaderCommand(TableData table, ConfigKey configKey) {
		TableViewModel viewModel = table.getViewModel();
		Consumer<ConfigKey> configLoader = key -> {
			viewModel.loadConfiguration(key);
			/* Ensure that the loaded configuration is made persistent under the default
			 * configuration. */
			viewModel.storeConfiguration(viewModel.getConfigKey());
		};
		Command command = new LoadNamedSetting(configKey, configLoader);
		CommandModel commandModel =
			CommandModelFactory.commandModel(command, new MultipleTableSettingsExecutability(configKey) {

				@Override
				protected ExecutableState calculateExecutability(String configKeyString) {
					if (TLContext.getContext().getPersonalConfiguration().getValue(configKeyString) == null) {
						return ExecutableState.createDisabledState(I18NConstants.NO_NAMED_TABLE_SETTINGS_AVAILABLE);
					}
					return super.calculateExecutability(configKeyString);
				}
			});
		initButton(commandModel, table, Icons.LOAD_NAMED_TABLE_SETTINGS, null, I18NConstants.LOAD_NAMED_TABLE_SETTINGS);
		return commandModel;
	}

	/**
	 * Creates a {@link CommandModel} using {@link ManageMultipleTableSettings} command.
	 * 
	 * @param table
	 *        The {@link TableData} to create {@link CommandModel} for.
	 */
	public static CommandModel createSettingsEditorCommand(TableData table, ConfigKey configKey) {
		TableViewModel viewModel = table.getViewModel();
		Consumer<ConfigKey> configSaver = key -> viewModel.storeConfiguration(key);
		Consumer<ConfigKey> configRemover = key -> viewModel.removeConfiguration(key);
		Command command = new ManageMultipleTableSettings(configKey, configSaver, configRemover);
		CommandModel commandModel =
			CommandModelFactory.commandModel(command, new MultipleTableSettingsExecutability(configKey));
		initButton(commandModel, table, Icons.EDIT_NAMED_TABLE_SETTINGS, null, I18NConstants.EDIT_NAMED_TABLE_SETTINGS);
		return commandModel;
	}
}