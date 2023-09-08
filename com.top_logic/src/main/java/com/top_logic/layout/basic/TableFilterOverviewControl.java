/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.DecoratedControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.FilterDecorator;
import com.top_logic.layout.form.control.FormGroupControl;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.FilterFormOwner;
import com.top_logic.layout.table.control.StructuredColumnLabels;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.Header;
import com.top_logic.layout.table.renderer.ColumnLabelProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link Control}, that provides a separate view to table column filters.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableFilterOverviewControl extends AbstractConstantControl {

	private static final String SIDEBAR_FILTER_CONTAINER_CLASS = "sidebarFilterContainer";

	private static final ResKey TL_SIDEBAR_FILTER_SELECT = I18NConstants.FILTER_SIDEBAR_SELECT;

	private static final String PERSONALIZAION_PREFIX = "filterSidebar.";
	private static final ResPrefix RES_PREFIX = I18NConstants.FILTER_SIDEBAR;

	private static final ResKey TL_SIDEBAR_FILTER_REFRESH = I18NConstants.SIDEBAR_REFRESH;

	private static final ResKey TL_SIDEBAR_FILTER_REFRESH_DISABLED = I18NConstants.SIDEBAR_REFRESH_DISABLED;

	private static final ResKey TL_SIDEBAR_FILTER_CHOOSER = I18NConstants.SIDEBAR_FILTERS;

	private static final ResKey TL_SIDEBAR_FILTER_COLLAPSE_ALL = I18NConstants.SIDEBAR_COLLAPSE_ALL;

	private static final ResKey TL_SIDEBAR_FILTER_EXPAND_ALL = I18NConstants.SIDEBAR_EXPAND_ALL;
	
	private TableDataProvider tableDataProvider;

	private FormContext sidebarContext;
	private SelectField lazyFilters;

	private CommandField refreshCommand;
	private CommandField expandAll;
	private CommandField collapseAll;
	
	private List<FormGroup> filterGroups;
	
	/**
	 * Create a new {@link TableFilterOverviewControl}
	 */
	public TableFilterOverviewControl(ToolBar toolBar, TableDataProvider tableDataProvider) {
		this.tableDataProvider = tableDataProvider;
		sidebarContext = createSidebarFilterChooserContext();

		refreshCommand = createRefreshCommand(sidebarContext);
		expandAll = createExpandAllCommand(sidebarContext);
		collapseAll = createCollapseAllCommand(sidebarContext);

		ToolBarGroup refreshGroup = toolBar.defineGroup(CommandHandlerFactory.REFRESH_GROUP);
		refreshGroup.addButton(refreshCommand);

		ToolBarGroup configGroup = toolBar.defineGroup(CommandHandlerFactory.FILTER_SELECT_GROUP);
		configGroup.addButton(createSelectFilterCommand(sidebarContext));

		ToolBarGroup expandGroup = toolBar.defineGroup(CommandHandlerFactory.EXPAND_COLLAPSE_GROUP);
		expandGroup.addButton(expandAll);
		expandGroup.addButton(collapseAll);

		filterGroups = new ArrayList<>();
	}
	
	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		filterGroups.clear();

		// Establish link between form context of toolbar buttons and component table data to allow
		// identifying buttons during test.
		addFormHandlerToContext(this, sidebarContext);

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeTableFilter(context, out);
		out.endTag(DIV);
	}

	private void writeTableFilter(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, SIDEBAR_FILTER_CONTAINER_CLASS);
		out.endBeginTag();
		SelectField filterSelectionField = getFilterSelectionField();
		Iterator<Column> selectionIterator = filterSelectionField.getSelectionIterator();
		while(selectionIterator.hasNext()) {
			writeColumnFilter(context, out, selectionIterator.next());
		}
		out.endTag(DIV);
	}

	private void writeColumnFilter(DisplayContext context, TagWriter out, Column column)
			throws IOException {
		String columnName = column.getName();
		FormContext filterContext = createFilterFormContext(columnName);
		filterGroups.add(filterContext);

		FormGroupControl formGroupControl = new FormGroupControl(filterContext);
		addFormHandlerToContext(formGroupControl, filterContext);
		TableFilter filter = viewModel().getFilter(columnName);
		final Control filterControl = filter.createFilterControl(context, filterContext);
		formGroupControl.addChild(filterControl);
		formGroupControl.setCollapsible(true);
		DecoratedControlRenderer<FormGroupControl> renderer = new DecoratedControlRenderer<>(DIV,
			new FilterDecorator(column.getName(), tableData()),
			new Renderer<FormGroupControl>() {
				@Override
				public void write(DisplayContext context, TagWriter out, FormGroupControl value) throws IOException {
					FormGroup formGroup = value.getFormGroup();
					if (!formGroup.isCollapsed()) {
						filterControl.write(context, out);
					}
				}
			});
		formGroupControl.setRenderer(renderer);
		formGroupControl.write(context, out);
	}

	private void addFormHandlerToContext(final AbstractControlBase formGroupControl, FormContext filterContext) {
		FilterFormOwner.register(filterContext, tableData(), formGroupControl);
	}

	TableModel tableModel() {
		return tableData().getTableModel();
	}

	TableViewModel viewModel() {
		return tableData().getViewModel();
	}

	TableData tableData() {
		return tableDataProvider.getTableData();
	}

	private FormContext createFilterFormContext(String columnName) {
		FormContext filterContext = new FormContext(columnName + "-filterSideBar", TableFilter.RES_PREFIX);
		filterContext.setCollapsed(loadFilterGroupCollapseState(columnName));
		filterContext.addListener(Collapsible.COLLAPSED_PROPERTY, new CollapsedListener() {
			
			@Override
			public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
				FormGroup formGroup = (FormGroup) collapsible;
				storeFilterGroupCollapseState(formGroup.getName(), newValue);
				return Bubble.BUBBLE;
			}
		});
		return filterContext;
	}

	@SuppressWarnings("hiding")
	private SelectField createFilterSelectionField(FormContext sidebarContext) {
		SelectField filterChooser = createFilterChooser();
		List<Column> selection = getColumnsByNames(viewModel().getSidebarFilters());
		filterChooser.setAsSelection(selection);
		filterChooser.addValueListener(new ValueListener() {
			@SuppressWarnings({ "synthetic-access", "unchecked" })
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				List<Column> newSelectedColumns = (List<Column>) newValue;
				List<Column> invisibleColumns = getNewInvisibleFilters(newSelectedColumns, (List<Column>) oldValue);
				TableViewModel viewModel = viewModel();
				List<String> columnNames = viewModel.getColumnNames();
				for (Column column : invisibleColumns) {
					if(!columnNames.contains(column.getName())) {
						viewModel.resetFilter(viewModel.getApplicationModel().getColumnIndex(column.getName()));
					}
					resetFilterGroupCollapseState(column.getName());
				}

				viewModel.setSidebarFilters(getColumnNames(newSelectedColumns));
				requestRepaint();
			}

			private List<String> getColumnNames(List<Column> invisibleColumns) {
				List<String> columnNames = new ArrayList<>();
				for (Column column : invisibleColumns) {
					columnNames.add(column.getName());
				}
				return columnNames;
			}
		});
		filterChooser.setLabel(Resources.getInstance().getString(TL_SIDEBAR_FILTER_CHOOSER));
		sidebarContext.addMember(filterChooser);

		return filterChooser;
	}

	private FormContext createSidebarFilterChooserContext() {
		FormContext sidebarContext = new FormContext("sidebarFilterSelector", RES_PREFIX);
		return sidebarContext;
	}

	private CommandField createRefreshCommand(FormContext sidebarContext) {
		CommandField command = FormFactory.newCommandField("refreshSidebar", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				viewModel().reapplyFilters();
				requestRepaint();
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		command.setImage(Icons.BUTTON_REFRESH);
		command.setNotExecutableImage(Icons.BUTTON_REFRESH_DISABLED);
		command.setLabel(Resources.getInstance().getString(TL_SIDEBAR_FILTER_REFRESH));
		command.setNotExecutableReasonKey(TL_SIDEBAR_FILTER_REFRESH_DISABLED);
		sidebarContext.addMember(command);

		return command;
	}

	private CommandField createExpandAllCommand(FormContext sidebarContext) {
		String commandName = "expandAll";
		boolean doCollapse = false;
		CommandField result = createCommand(sidebarContext, commandName, TL_SIDEBAR_FILTER_EXPAND_ALL, Icons.EXPAND_ALL,
			Icons.EXPAND_ALL_DISABLED, doCollapse);
		result.setNotExecutableReasonKey(I18NConstants.CANNOT_EXPAND_ALREADY_ALL_EXPANDED);
		return result;
	}

	private CommandField createCollapseAllCommand(FormContext sidebarContext) {
		String commandName = "collapseAll";
		boolean doCollapse = true;
		CommandField result =
			createCommand(sidebarContext, commandName, TL_SIDEBAR_FILTER_COLLAPSE_ALL, Icons.COLLAPSE_ALL,
			Icons.COLLAPSE_ALL_DISABLED, doCollapse);
		result.setNotExecutableReasonKey(I18NConstants.CANNOT_COLLAPSE_ALREADY_ALL_COLLAPSED);
		return result;
	}

	private CommandField createCommand(FormContext sidebarContext, String name, ResKey labelKey,
			ThemeImage enabledImage, ThemeImage disabledImage, final boolean doCollapse) {
		CommandField command = FormFactory.newCommandField(name, new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				for (FormGroup formGroup : filterGroups) {
					formGroup.setCollapsed(doCollapse);
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		command.setImage(enabledImage);
		command.setNotExecutableImage(disabledImage);
		command.setLabel(Resources.getInstance().getString(labelKey));
		sidebarContext.addMember(command);
		return command;
	}

	private CommandField createSelectFilterCommand(FormContext sidebarContext) {
		CommandField command = FormFactory.newCommandField("selectFilters", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				SelectionControl.openSelectorPopup(context, getScope().getFrameScope().getWindowScope(),
					getFilterSelectionField());
				return HandlerResult.DEFAULT_RESULT;
			}
		});

		// Do not record, because the final selection is recorded. Just opening the selection dialog
		// must not be recorded, because the selection dialog itself is not scriptable. Only the
		// final selection is recorded.
		ScriptingRecorder.annotateAsDontRecord(command);

		command.setImage(Icons.SELECT_FILTER);
		command.setLabel(Resources.getInstance().getString(TL_SIDEBAR_FILTER_SELECT));
		sidebarContext.addMember(command);
		return command;
	}

	private SelectField createFilterChooser() {
		StructuredColumnLabels structuredLabels =
			new StructuredColumnLabels(ColumnLabelProvider.newInstance(tableData()));
		SelectField filterSelectField =
			FormFactory.newSelectField("selectedFilters", getFilterColumns(), true,
				false);
		filterSelectField.setOptionComparator(LabelComparator.newCachingInstance(structuredLabels));
		filterSelectField.setCustomOrder(true);
		filterSelectField.setOptionLabelProvider(structuredLabels);
		return filterSelectField;
	}

	private List<Column> getFilterColumns() {
		return getColumnsByNames(viewModel().getAvailableSidebarFilters());
	}

	private void resetFilterGroupCollapseState(String columnName) {
		storeFilterGroupCollapseState(columnName, null);
	}
	
	private boolean loadFilterGroupCollapseState(String columnName) {
		PersonalConfiguration personalConfig = PersonalConfiguration.getPersonalConfiguration();
		Object rawValue = personalConfig.getJSONValue(columnConfigKey(columnName));
		if(rawValue != null && rawValue instanceof Boolean) {
			return (Boolean) rawValue;
		} else {
			return false;
		}
	}

	private void storeFilterGroupCollapseState(String columnName, Boolean collapseState) {
		PersonalConfiguration personalConfig = PersonalConfiguration.getPersonalConfiguration();
		personalConfig.setJSONValue(columnConfigKey(columnName), collapseState);
	}

	private List<Column> getColumnsByNames(List<String> columnNames) {
		List<Column> columns = new ArrayList<>();
		TableModel applicationModel = tableModel();
		Header header = applicationModel.getHeader();
		for (String storedColumnName : columnNames) {
			Column column = header.getColumn(storedColumnName);
			columns.add(column);
		}
		return columns;
	}
	
	private String columnConfigKey(String columnName) {
		return tableConfigPrefix() + PERSONALIZAION_PREFIX + columnName;
	}

	private String tableConfigPrefix() {
		return viewModel().getConfigKey().get();
	}

	private List<Column> getNewInvisibleFilters(List<Column> newColumns, List<Column> oldColumns) {
		List<Column> invisibleColumns = new ArrayList<>();
		for (Column column : oldColumns) {
			if(!newColumns.contains(column)) {
				invisibleColumns.add(column);
			}
		}
		return invisibleColumns;
	}

	private SelectField getFilterSelectionField() {
		if (lazyFilters == null) {
			lazyFilters = createFilterSelectionField(sidebarContext);
		}
		return lazyFilters;
	}

}
