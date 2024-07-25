/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ListModelBuilderProxy;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.AccessContext;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Utils;

/**
 * {@link GridBuilder} that creates a regular grid without hierarchy. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableGridBuilder<R> extends ListModelBuilderProxy
		implements GridBuilder<R>, ConfiguredInstance<TableGridBuilder.Config> {

	private final Config _config;

	/**
	 * Configuration of a {@link TableGridBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<TableGridBuilder<?>> {

		/**
		 * Configuration of the actual {@link ListModelBuilder}, that creates the rows in the table.
		 */
		@Mandatory
		@Name(AbstractTreeGridBuilder.Config.BUILDER_ATTRIBUTE)
		PolymorphicConfiguration<ListModelBuilder> getBuilder();

	}

	/**
	 * Creates a new {@link TableGridBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TableGridBuilder}.
	 */
	public TableGridBuilder(InstantiationContext context, Config config) {
		super(context.getInstance(config.getBuilder()));
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * Creates a {@link TableGridBuilder}.
	 * 
	 * @param impl
	 *        The {@link ListModelBuilder} to adapt.
	 */
	public TableGridBuilder(ListModelBuilder impl) {
		super(impl);
		_config = null;
	}
	
	@Override
	public Collection<CommandHandler> getCommands() {
		return Collections.emptyList();
	}
	
	@Override
	public GridHandler<R> createHandler(GridComponent grid,
			TableConfiguration tableConfiguration, String[] availableColumns, Mapping<Object, ? extends R> toRow, Mapping<? super R, ?> toModel) {
		return new TableGridHandler(grid, tableConfiguration, availableColumns, toRow, toModel);
	}
	
	Collection<?> getListModel(GridComponent grid) {
		return getModel(grid.getModel(), grid);
	}
	
	/**
	 * {@link GridBuilder.GridHandler} that creates a table grid.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	protected class TableGridHandler extends AbstractGridHandler<R> {

		private final TableField _tableField;

		/**
		 * Creates a {@link TableGridHandler}.
		 *
		 * @param toRow
		 *        {@link Mapping} of application models to grid row objects.
		 * @param toModel
		 *        {@link Mapping} of grid row objects to application models.
		 * @param grid
		 *        The context component.
		 * @param tableConfiguration
		 *        The {@link TableConfiguration} for the table display.
		 * @param availableColumns
		 *        The columns to show.
		 */
		public TableGridHandler(GridComponent grid, TableConfiguration tableConfiguration, String[] availableColumns, Mapping<Object, ? extends R> toRow, Mapping<? super R, ?> toModel) {
			super(toRow, toModel, grid);
			TableDragSource dragSource = tableConfiguration.getDragSource();
			if (!(dragSource instanceof GridDragSource)) {
				tableConfiguration.setDragSource(new ProxyGridDragSource(dragSource, grid));
			}
			EditableRowTableModel tableModel = createTable(grid, tableConfiguration, availableColumns);
			_tableField = FormFactory.newTableField(GridComponent.FIELD_TABLE, grid.getConfigKey());
			_tableField.setTableModel(tableModel);
			_tableField.setStableIdSpecialCaseContext(grid);
		}

		private ObjectTableModel createTable(GridComponent grid, TableConfiguration tableConfiguration, String[] availableColumns) {
			List<R> rows = toRows(getListModel(grid));
			ObjectTableModel tableModel = new ObjectTableModel(availableColumns, tableConfiguration, rows) {
				/**
				 * Unwrap rows for bulk access preload.
				 */
				@Override
				public AccessContext prepareRows(Collection<?> accessedObjects, List<String> accessedColumns) {
					@SuppressWarnings("unchecked")
					Collection<R> accessedRows = (Collection<R>) accessedObjects;
					
					return super.prepareRows(toModels(accessedRows), accessedColumns);
				}
			};
			return tableModel;
		}

		@Override
		protected Filter<Object> getSelectionFilter() {
			TableModel tableModel = _tableField.getTableModel();
			SelectionModel selectionModel = _tableField.getSelectionModel();

			return object -> {
				if (_grid.getRowGroup(object) != null) {
					Object tableRow = getFirstTableRow(toGridRow(object));

					boolean hasRow = tableModel.containsRowObject(tableRow);
					boolean isSelectable = selectionModel.isSelectable(tableRow);

					return hasRow && isSelectable;
				}
				
				return false;
			};
		}

		@Override
		public TableField getTableField() {
			return _tableField;
		}
		
		@Override
		public boolean addNewRow(Object rowModel) {
			getTableModel().addRowObject(toGridRow(rowModel));
			return true;
		}

		@Override
		public boolean addNewRows(Collection<?> rowObjects) {
			getTableModel().addAllRowObjects(toRows(rowObjects));
			return true;
		}

		@Override
		public void removeRow(R rowObject) {
			getTableModel().removeRowObject(rowObject);
		}

		@Override
		public Object createRow(Object contextModel, ContextPosition position, Object newRowModel) {
			EditableRowTableModel tableModel = getTableModel();
			int beforeRow;
			switch (position.getStrategy()) {
				case START: {
					beforeRow = 0;
					break;
				}
				case END: {
					beforeRow = tableModel.getRowCount();
					break;
				}
				case BEFORE: {
					beforeRow = tableModel.getRowOfObject(getFirstTableRow(toGridRow(position.getContext())));
					break;
				}
				case AFTER: {
					beforeRow = tableModel.getRowOfObject(getFirstTableRow(toGridRow(position.getContext()))) + 1;
					break;
				}
				default: {
					throw new UnreachableAssertion("No such strategy: " + position.getStrategy());
				}
			}
			R gridRow = toGridRow(newRowModel);
			tableModel.showRowAt(gridRow, beforeRow);
			// Rows are not wrapped.
			Object tableRow = gridRow;
			return tableRow;
		}

		private EditableRowTableModel getTableModel() {
			return (EditableRowTableModel) _tableField.getTableModel();
		}

		@Override
		@SuppressWarnings("unchecked")
		public R getGridRow(Object tableRow) {
			// Rows are not wrapped.
			return (R) tableRow;
		}
		
		@Override
		public Object getParentRow(Object tableRow) {
			return null;
		}

		@Override
		public Collection<?> getTableRows(R gridRow) {
			// Rows are not wrapped.
			return Collections.singletonList(gridRow);
		}

		@Override
		public Object getFirstTableRow(R gridRow) {
			return gridRow;
		}

		@Override
		public List<R> sortGridRows(Collection<R> gridRows) {
			List<R> sortedSelection = new ArrayList<>(gridRows);
			Comparator<R> rowComparator = new MappedComparator<>(new Mapping<R, Object>() {

				@Override
				public Object map(R input) {
					return getFirstTableRow(input);
				}
			}, getTableField().getViewModel().getRowComparator());
			Collections.sort(sortedSelection, rowComparator);
			return sortedSelection;
		}

		@Override
		public Collection<?> getExpansionState() {
			return Collections.emptySet();
		}

		@Override
		public void setExpansionState(Collection<?> expansionState) {
			// Do nothing
		}

		@Override
		protected void setSelection(SelectionModel selectionModel, Set<List<?>> selectedPaths) {
			Set<R> rows = selectedPaths.stream()
				.map(CollectionUtil::getLast)
				.map(this::toGridRow)
				.collect(Collectors.toSet());
			SelectionUtil.setSelection(selectionModel, rows);
		}

	}

	@Override
	public boolean handleTLObjectCreations(GridComponent grid, Stream<? extends TLObject> creations) {
		Collection<? extends TLObject> relevantCreations = creations
			.filter(newObject -> isRelevantCreation(grid, newObject))
			.collect(toUnmodifiableList());
		if (relevantCreations.isEmpty()) {
			return false;
		}
		handleNewRelevantObjects(grid, relevantCreations);
		return true;
	}

	private boolean isRelevantCreation(GridComponent grid, TLObject newObject) {
		if (grid.getRowGroup(newObject) != null) {
			return false;
		}
		if (!supportsListElement(grid, newObject)) {
			return false;
		}
		if (!Utils.equals(grid.getModel(), retrieveModelFromListElement(grid, newObject))) {
			return false;
		}
		return true;
	}

	private void handleNewRelevantObjects(GridComponent grid, Collection<? extends TLObject> creations) {
		if (grid.isVisible()) {
			grid.getHandler().addNewRows(creations);
		} else {
			grid.invalidate();
		}
	}

	@Override
	public void receiveModelChangedEvent(GridComponent grid, Object model) {
		// Nothing.
	}

	@Override
	public boolean supportsRow(GridComponent grid, Object row) {
		return supportsListElement(grid, row);
	}

	@Override
	public Object retrieveModelFromRow(GridComponent grid, Object row) {
		return retrieveModelFromListElement(grid, row);
	}

	@Override
	public Collection<? extends Object> getParentsForRow(GridComponent grid, Object row) {
		return Collections.emptySet();
	}

}
