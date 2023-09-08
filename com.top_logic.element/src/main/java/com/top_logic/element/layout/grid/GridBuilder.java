/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ListModelBuilder} for {@link GridComponent}s.
 * 
 * @param <R> The type of technical row objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GridBuilder<R> extends ModelBuilder {

	/**
	 * Algorithm to operate on the {@link TableModel} of a grid.
	 * 
	 * @param <R> The type of technical row objects.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface GridHandler<R> {

		/**
		 * The {@link TableField} of the grid.
		 */
		TableField getTableField();
		
		/**
		 * Adds a new row model object to the grid.
		 * 
		 * @return Whether a new row was created for the given model.
		 */
		boolean addNewRow(Object rowModel);

		/**
		 * Add the row objects to the grid.
		 * 
		 * @return Whether a new row was created.
		 */
		default boolean addNewRows(Collection<?> rowObjects) {
			boolean result = false;
			for (Object object : rowObjects) {
				result |= addNewRow(object);
			}
			return result;
		}

		/**
		 * Removes the given technical row object. 
		 */
		void removeRow(R rowObject);

		/**
		 * Creates a new row for the given row model object in the grid. The row
		 * is created from the given context row object.
		 * 
		 * @param contextModel
		 *        The row that was selected before the creation.
		 * @param position
		 *        Location of the new row to insert.
		 * @param newRowModel
		 *        The new row model that should be inserted into the grid.
		 */
		void createRow(Object contextModel, ContextPosition position, Object newRowModel);

		/**
		 * Retrieve the grid row model from a row object of the {@link #getTableField()} this
		 * {@link GridBuilder.GridHandler} creates.
		 * 
		 * @param tableRow
		 *        Row in {@link #getTableField()}
		 * @return The technical grid row object, see
		 *         {@link GridBuilder#createHandler(GridComponent, TableConfiguration, String[], Mapping, Mapping)}
		 *         .
		 * @see #getTableRows(Object) inverse operation
		 */
		R getGridRow(Object tableRow);

		/**
		 * Retrieve the table row object of the {@link #getTableField()} this
		 * {@link GridBuilder.GridHandler} creates from a row grid object.
		 * <p>
		 * In a table (i.e. non-tree grid), this method just wraps a collection around the given
		 * object.
		 * </p>
		 * <p>
		 * In a tree, this method usually maps from {@link FormGroup} to the corresponding
		 * {@link GridTreeTableNode} instances. There are multiple tree nodes, as an object can
		 * appear in multiple places in a "tree" grid, if it is actually not just a tree, but a
		 * directed graph.
		 * </p>
		 * 
		 * @param gridRow
		 *        A technical grid row object, see
		 *        {@link GridBuilder#createHandler(GridComponent, TableConfiguration, String[], Mapping, Mapping)}
		 *        .
		 * @return The row in {@link #getTableField()}.
		 * 
		 * @see #getGridRow(Object) inverse operation
		 */
		Collection<?> getTableRows(R gridRow);

		/**
		 * The first row object displaying the given grid row.
		 * 
		 * @see #getTableRows(Object)
		 */
		Object getFirstTableRow(R gridRow);

		/**
		 * Establishes the given selection.
		 * 
		 * @param newSelectedModels
		 *        The new model objects to select.
		 */
		void setUISelection(Collection<?> newSelectedModels);

		/**
		 * Marks the corresponding table row for the given grid row as
		 * internally updated.
		 * 
		 * @param gridRow
		 *        The technical row object that was internally updated.
		 * @param structureChange
		 *        Whether the change may affect other related rows (descendants
		 *        in a tree).
		 */
		void updateTableRow(R gridRow, boolean structureChange);

		/**
		 * Creates a new list with the given grid rows sorted by the row order defined by
		 * {@link #getTableField()}.
		 * 
		 * @param gridRows
		 *        The rows to sort.
		 * @return Sorted grid rows.
		 */
		List<R> sortGridRows(Collection<R> gridRows);

		/**
		 * whether a collection of user objects, whose nodes are currently expanded, if the
		 *         grid displays a tree table, an empty collection otherwise.
		 * 
		 * @see #setExpansionState(Collection)
		 */
		Collection<?> getExpansionState();

		/**
		 * @see #getExpansionState()
		 */
		void setExpansionState(Collection<?> expansionState);
	}

	/**
	 * Informs the {@link GridBuilder} that new {@link TLObject business objects} have been created.
	 * <p>
	 * The {@link GridBuilder} will use this information to update the {@link GridComponent}.
	 * </p>
	 */
	boolean handleTLObjectCreations(GridComponent grid, Stream<? extends TLObject> creations);

	/**
	 * A model has been changed by a component in the application.
	 * 
	 * @param model
	 *        The changed model, may be <code>null</code>.
	 */
	void receiveModelChangedEvent(GridComponent grid, Object model);

	/**
	 * Additional command to register on a grid for this type of {@link GridBuilder}.
	 */
	Collection<CommandHandler> getCommands();

	/**
	 * Access to the wrapped {@link ModelBuilder}.
	 */
	ModelBuilder unwrap();

	/**
	 * Creates a {@link GridHandler} that manages the grid table.
	 * 
	 * @param grid
	 *        The owning {@link GridComponent}.
	 * @param tableConfiguration
	 *        The {@link TableConfiguration} of the table to create.
	 * @param availableColumns
	 *        The columns that should be available in the grid.
	 * @param toRow
	 *        A {@link Mapping} from application objects to technical grid row
	 *        objects.
	 * @param toModel
	 *        A {@link Mapping} from grid row object to application model
	 *        objects.
	 */
	GridHandler<R> createHandler(GridComponent grid,
			TableConfiguration tableConfiguration, String[] availableColumns,
			Mapping<Object, ? extends R> toRow, Mapping<? super R, ?> toModel);

	/**
	 * Whether the given element displayed by the {@link GridComponent} using this
	 * {@link GridBuilder}.
	 * 
	 * @param grid
	 *        The context {@link GridComponent}.
	 * @param row
	 *        The potential row element.
	 * @return Whether the given element would be displayed be the context grid, if called now.
	 * 
	 * @see ListModelBuilder#supportsListElement(LayoutComponent, Object)
	 */
	boolean supportsRow(GridComponent grid, Object row);

	/**
	 * Find the component model so that {@link #supportsRow(GridComponent, Object)} for would be
	 * <code>true</code>, if the given component had this model.
	 * 
	 * @param grid
	 *        The context {@link GridComponent}.
	 * @param row
	 *        The row to find a component model for.
	 * @return The component model so that the given element is displayed.
	 * 
	 * @see ListModelBuilder#retrieveModelFromListElement(LayoutComponent, Object)
	 */
	Object retrieveModelFromRow(GridComponent grid, Object row);

}
