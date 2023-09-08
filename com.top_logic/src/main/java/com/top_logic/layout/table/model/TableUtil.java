/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.util.BusinessModelMapping;
import com.top_logic.layout.form.util.BusinessModelMappingAdapter;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableRenderer.RenderState;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.command.TableCommandProvider;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.provider.ColumnOption;
import com.top_logic.layout.table.provider.ColumnProviderConfig;
import com.top_logic.layout.table.provider.PseudoColumn;
import com.top_logic.layout.table.provider.TypePartColumn;
import com.top_logic.layout.table.renderer.TableButtons;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Utilities for tables, mainly for configuration parsing of {@link ColumnConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableUtil {

	@SuppressWarnings("rawtypes")
	static Map<String, AbstractProperty> index(Map<String, AbstractProperty> base,
			AbstractProperty... properties) {
		HashMap<String, AbstractProperty> result = new HashMap<>();
		result.putAll(base);
		for (AbstractProperty property : properties) {
			result.put(property.getConfigName(), property);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	static Map<String, AbstractProperty> index(AbstractProperty... properties) {
		return index(Collections.<String, AbstractProperty> emptyMap(), properties);
	}
	
	/**
	 * The heuristic for "the nearest object in a table".
	 * <p>
	 * If there is a predecessor, it is returned, otherwise the successor. In this case, the
	 * successor is at index 0.
	 * </p>
	 * 
	 * @return The index of the "nearest object" for an object that would be inserted at the given
	 *         position in a table.
	 */
	public static int toNearestPosition(int insertPosition) {
		if (insertPosition == 0) {
			return 0;
		}
		return insertPosition - 1;
	}

	/**
	 * Installs the {@link BusinessModelMapping} (resp. a derivative of it) into the given column as
	 * {@link ColumnConfiguration#getSortKeyProvider() sort key provider}.
	 * 
	 * @param column
	 *        The {@link ColumnConfiguration} to adapt.
	 */
	public static void setBusinessModelMapping(ColumnConfiguration column) {
		Mapping<Object, ?> custom = column.getSortKeyProvider();
		if (custom == Mappings.identity() || custom == null) {
			column.setSortKeyProvider(BusinessModelMapping.INSTANCE);
		} else {
			column.setSortKeyProvider(new BusinessModelMappingAdapter(custom));
		}
	}

	/**
	 * Removes toolbar groups created by default and all configured commands from the given
	 * {@link TableData}.
	 * 
	 * @param toolbar
	 *        The {@link ToolBar} to remove default groups from.
	 */
	public static void removeTableButtons(ToolBar toolbar, TableData table) {
		removeExportButtons(toolbar);
		removeDefaultTableButtons(toolbar);
		removeConfiguredTableButtons(toolbar, table);
	}

	private static void removeDefaultTableButtons(ToolBar toolbar) {
		toolbar.removeGroup(CommandHandlerFactory.TABLE_BUTTONS_GROUP);
		toolbar.removeGroup(CommandHandlerFactory.TABLE_CONFIG_GROUP);
	}

	private static void removeConfiguredTableButtons(ToolBar toolbar, TableData table) {
		if (table != null) {
			for (String commandClique : getConfiguredTableCommands(table).keySet()) {
				toolbar.removeGroup(commandClique);
			}
		}
	}

	private static void removeExportButtons(ToolBar toolbar) {
		ToolBarGroup exportGroup = toolbar.getGroup(CommandHandlerFactory.EXPORT_BUTTONS_GROUP);
		if (exportGroup != null) {
			int index = 0;
			for (HTMLFragment view : exportGroup.getViews()) {
				if (view instanceof ButtonControl) {
					CommandModel command = ((ButtonControl) view).getModel();
					if (command.isSet(TableButtons.INTERNAL_EXPORT)) {
						exportGroup.removeView(index);
						break;
					}
				}
				index++;
			}
			if (exportGroup.isEmpty()) {
				toolbar.removeGroup(CommandHandlerFactory.EXPORT_BUTTONS_GROUP);
			}
		}
	}

	private static Map<String, Collection<TableCommandProvider>> getConfiguredTableCommands(TableData table) {
		return table.getTableModel().getHeader().getTableConfiguration().getCommands();
	}

	/**
	 * Creates {@link ColumnOption}s for a given collection of type parts.
	 */
	public static Collection<ColumnOption> createColumnOptions(Collection<? extends TLStructuredTypePart> parts) {
		return createColumnOptions(parts, Collections.emptyList());
	}

	/**
	 * Creates {@link ColumnOption}s for a given collection of type parts and table configuration
	 * plug-ins.
	 */
	public static Collection<ColumnOption> createColumnOptions(Collection<? extends TLStructuredTypePart> parts,
			Collection<PolymorphicConfiguration<? extends TableConfigurationProvider>> providers) {
		Map<String, ColumnOption> columnOptions = new HashMap<>();

		for (TLTypePart part : parts) {
			columnOptions.putIfAbsent(part.getName(), new TypePartColumn(part));
		}

		for (PolymorphicConfiguration<? extends TableConfigurationProvider> providerConfig : providers) {
			if (providerConfig instanceof ColumnProviderConfig) {
				ColumnProviderConfig columnConfig = (ColumnProviderConfig) providerConfig;
				columnOptions.putIfAbsent(columnConfig.getColumnId(),
					new PseudoColumn(columnConfig.getColumnLabel(), columnConfig.getColumnId()));
			}
		}

		return columnOptions.values();
	}

	/**
	 * Selects the object by the given row in the {@link SelectionModel} of this {@link TableData}.
	 * 
	 * @return True if the selection has changed.
	 */
	public static boolean selectRow(TableData tableData, int row) {
		Object rowObject = tableData.getViewModel().getRowObject(row);

		SelectionModel selectionModel = tableData.getSelectionModel();

		Set<?> newSelection = Collections.singleton(rowObject);
		Set<?> oldSelection = selectionModel.getSelection();

		selectionModel.setSelection(newSelection);

		return !newSelection.equals(oldSelection);
	}

	/**
	 * Assumes that only one row is selected. Returns the selected row index.
	 */
	public static int getSingleSelectedRow(TableData tableData) {
		Set<?> selection = tableData.getSelectionModel().getSelection();
		Object selectedObject = CollectionUtil.getSingleValueFromCollection(selection);
		return tableData.getViewModel().getRowOfObject(selectedObject);

	}

	/**
	 * Listener for the {@link TableModelEvent}s that are sent when the underlying
	 * {@link TableModel} has changed to update the current selection.
	 */
	public static TableModelListener createUpdateSelectionListener(TableData tableData) {
		return new TableModelListener() {

			@Override
			public void handleTableModelEvent(TableModelEvent event) {
				TableModel tableModel = (TableModel) event.getSource();
				SelectionModel selectionModel = tableData.getSelectionModel();
				Set<?> selection = selectionModel.getSelection();

				switch (event.getType()) {
					case TableModelEvent.DELETE: {
						Set<?> newSelection = CollectionFactory.set(selection);
						newSelection.retainAll(tableModel.getAllRows());

						selectionModel.setSelection(newSelection);
						break;
					}
					case TableModelEvent.INVALIDATE: {
						Collection<?> allRows = tableModel.getAllRows();
						if (allRows.size() > 0) {
							Set<Object> newSelection = new HashSet<>();
							for (Object selectedObject : selection) {
								if (allRows.contains(selectedObject)) {
									newSelection.add(selectedObject);
								}
							}
							selectionModel.setSelection(newSelection);
						} else {
							selectionModel.clear();
						}
						break;
					}
				}
			}
		};
	}
	
	
	/**
	 * Creates a {@link Renderer} for the given table row index.
	 * 
	 * @param state
	 *        Render state.
	 * @param row
	 *        Table row index to write.
	 */
	public static Renderer<Object> createRendererForRow(RenderState state, int row) {
		return new Renderer<>() {
			
			@Override
			public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
				state.writeRow(context, out, row);
			}
		};
	};

	/**
	 * Creates the {@link HTMLFragment} to write all table rows in the range specified by the given
	 * first and last table row index.
	 * 
	 * @param state
	 *        Render state.
	 * @param firstRow
	 *        Table row index of the first row to render.
	 * @param lastRow
	 *        Table row index of the last row to render.
	 */
	public static HTMLFragment createTableRowsFragment(RenderState state, int firstRow, int lastRow) {
		TableControl tableControl = state.getView();
		
		return new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				for (int row = firstRow; row <= lastRow; row++) {
					tableControl.addRowScope(TableUtil.createRendererForRow(state, row), context, out, row);
				}
			}

		};
	};

	/**
	 * Parses the given String typed column width as an int.
	 * 
	 * @param columnWidth
	 *        String typed column width (sequence of digits).
	 */
	public static int parseColumnWidth(String columnWidth) {
		Matcher matcher = Pattern.compile("\\d+").matcher(columnWidth);

		if (matcher.find()) {
			return Integer.parseInt(matcher.group());
		} else {
			return 0;
		}
	}

	/**
	 * Returns the client column index for a given column index on the server.
	 * 
	 * For the client an additional column to separate the tables fixed and flexible area is
	 * rendered.
	 * 
	 * @see #getServerColumnIndex(TableViewModel, int)
	 */
	public static int getClientColumnIndex(TableViewModel tableViewModel, int serverColumnIndex) {
		int fixedColumnCount = tableViewModel.getFixedColumnCount();

		if (fixedColumnCount >= 0 && serverColumnIndex >= fixedColumnCount) {
			return serverColumnIndex + 1;
		} else {
			return serverColumnIndex;
		}
	}

	/**
	 * Returns the server column index for a given client index on the server.
	 * 
	 * For the client an additional column to separate the tables fixed and flexible area is
	 * rendered.
	 * 
	 * @see #getClientColumnIndex(TableViewModel, int)
	 */
	public static int getServerColumnIndex(TableViewModel tableViewModel, int clientColumnIndex) {
		int fixedColumnCount = tableViewModel.getFixedColumnCount();

		if (fixedColumnCount >= 0 && clientColumnIndex > fixedColumnCount) {
			return clientColumnIndex - 1;
		} else {
			return clientColumnIndex;
		}
	}
}
