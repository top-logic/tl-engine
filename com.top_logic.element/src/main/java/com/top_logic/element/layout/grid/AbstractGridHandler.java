/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.element.layout.grid.GridBuilder.GridHandler;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionUtil;
import com.top_logic.model.TLObject;

/**
 * Common base class for {@link GridHandler} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractGridHandler<R> implements GridHandler<R> {

	private final Mapping<Object, ? extends R> _toRow;

	private final Mapping<? super R, ?> _toModel;

	final GridComponent _grid;

	/**
	 * Creates a {@link AbstractGridHandler}.
	 * 
	 * @param toRow
	 *        See
	 *        {@link GridBuilder#createHandler(GridComponent, TableConfiguration, String[], Mapping, Mapping)}
	 *        .
	 * @param toModel
	 *        See
	 *        {@link GridBuilder#createHandler(GridComponent, TableConfiguration, String[], Mapping, Mapping)}
	 *        .
	 */
	public AbstractGridHandler(Mapping<Object, ? extends R> toRow, Mapping<? super R, ?> toModel, GridComponent grid) {
		_toRow = toRow;
		_toModel = toModel;
		_grid = grid;
	}

	protected final List<R> toRows(Collection<?> modelObjects) {
		if (modelObjects == null) {
			return Collections.emptyList();
		}
		ArrayList<R> rows  = new ArrayList<>(modelObjects.size());
		for (Object rowObject : modelObjects) {
		    rows.add(toGridRow(rowObject));
		}
		return rows;
	}

	protected final List<Object> toModels(Collection<? extends R> accessedObjects) {
		ArrayList<Object> baseObjects = new ArrayList<>(accessedObjects.size());
		for (R rowGroup : accessedObjects) {
			Object obj = getGridRowModel(rowGroup);
			if (obj == null) {
				continue;
			}
			baseObjects.add(obj);
		}
		return baseObjects;
	}

	@Override
	public void updateTableRow(R gridRow, boolean structureChange) {
		TableModel tableModel = getTableField().getTableModel();
		for (Object obj : getTableRows(gridRow)) {
			int updatedRowId = tableModel.getRowOfObject(obj);
			tableModel.updateRows(updatedRowId, updatedRowId);
		}
	}

	/** Returns the {@link FormGroup} representing the the given {@link TLObject}. */
	protected final R toGridRow(Object rowModel) {
		return _toRow.map(rowModel);
	}

	/** Returns the {@link TLObject} represented by the given {@link FormGroup}. */
	protected final Object getGridRowModel(R gridRow) {
		return _toModel.map(gridRow);
	}

	private Set<R> selectionToFormGroups(Object selection) {
		if (selection instanceof Collection) {
			return set(toRows((Collection<?>) selection));
		}
		return singleton(toGridRow(selection));
	}

	private void adjustFiltersForRow(Object tableRow) {
		TableViewModel viewModel = getTableField().getViewModel();
		viewModel.adjustFiltersForRow(tableRow);
	}

	/**
	 * Sets the selection in {@link SelectionModel}.
	 * 
	 * @param selectionModel
	 *        the {@link SelectionModel}
	 * @param rows
	 *        selected rows
	 */
	protected void setSelection(SelectionModel selectionModel, Set<?> rows) {
		SelectionUtil.setSelection(selectionModel, rows);
	}
	
	@Override
	public void setUISelection(Collection<?> newSelection) {
		if (newSelection.isEmpty()) {
			setDefaultUISelection();
		} else {
			Set<Object> newSelectedObjects = new HashSet<>();

			Filter<Object> selectionFilter = getSelectionFilter();

			for (Object selectedObject : newSelection) {
				if (selectionFilter.test(selectedObject)) {
					newSelectedObjects.add(selectedObject);
				}
			}

			if (newSelectedObjects.isEmpty()) {
				Set<?> oldSelectedObjects = _grid.getSelectionModel().getSelection();

				if (oldSelectedObjects.isEmpty() || !isSelectionValid(selectionFilter, oldSelectedObjects)) {
					setDefaultUISelection();
				}
			} else {
				setUISelectionInternal(newSelectedObjects);
			}
		}
	}

	private void setDefaultUISelection() {
		Object defaultSelection = _grid.getDefaultSelection();

		if (defaultSelection != null) {
			setUISelectionInternal(Collections.singleton(defaultSelection));
		} else {
			setUISelectionInternal(Collections.emptySet());
		}
	}

	private boolean isSelectionValid(Filter<Object> selectionFilter, Set<?> selection) {
		for (Object selectedObject : selection) {
			if (!selectionFilter.test(getGridRowModel(getGridRow(selectedObject)))) {
				return false;
			}
		}

		return true;
	}

	private void setUISelectionInternal(Set<Object> newSelectedObjects) {
		SelectionModel selectionModel = getTableField().getSelectionModel();

		if (newSelectedObjects.isEmpty()) {
			selectionModel.clear();
		} else {
			if (newSelectedObjects.size() == 1 && !selectionModel.isMultiSelectionSupported()) {
				Object tableRow = getFirstTableRow(toGridRow(CollectionUtils.extractSingleton(newSelectedObjects)));

				if (getTableField().getTableModel().getRowOfObject(tableRow) == TableModel.NO_ROW) {
					adjustFiltersForRow(tableRow);
				}
			}

			setSelection(selectionModel, selectionToFormGroups(newSelectedObjects));
		}
	}

	/**
	 * Filters selectable business objects.
	 */
	protected abstract Filter<Object> getSelectionFilter();
}
