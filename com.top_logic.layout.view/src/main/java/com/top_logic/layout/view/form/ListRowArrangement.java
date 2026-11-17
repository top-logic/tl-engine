/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.layout.view.model.TableSelectionBinding;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.table.Column;
import com.top_logic.table.RowSource;
import com.top_logic.table.impl.ListRowSource;

/**
 * {@link RowArrangement} of a flat table: the rows are a list, each row keyed by its row object.
 *
 * <p>
 * Outside an edit session the rows follow the model through a {@link RowSourceObserver}, which
 * computes them again with the row function when an observed object or an input channel changes;
 * an arrangement without row function keeps the rows it was given. The selection is bound through
 * a {@link TableSelectionBinding}.
 * </p>
 */
public class ListRowArrangement implements RowArrangement {

	private final Function<Object[], Collection<?>> _rowFunction;

	private final Set<TLStructuredType> _observedTypes;

	private final List<ViewChannel> _inputChannels;

	private final List<ViewChannel> _rowIndependentInputs;

	private final List<ViewChannel> _cellIndependentInputs;

	private final List<ViewChannel> _cellInputs;

	/**
	 * The source created last, {@code null} before
	 * {@link #createSource(List, RowSetEditSession, List)}.
	 */
	private ListRowSource<TLObject> _source;

	/** The selection binding of the table built last, {@code null} without one. */
	private TableSelectionBinding _selectionBinding;

	/** The observation of the model for {@link #_source}, {@code null} while there is none. */
	private RowSourceObserver<TLObject> _observer;

	/**
	 * Creates a {@link ListRowArrangement} whose rows do not follow the model.
	 */
	public ListRowArrangement() {
		this(null, Set.of(), List.of(), List.of(), List.of(), List.of());
	}

	/**
	 * Creates a {@link ListRowArrangement}.
	 *
	 * <p>
	 * An input change that leaves the rows as they are - a change of a row-independent input, or of
	 * a cell-independent input yielding the same rows - keeps the cells and is reported as
	 * {@link RowArrangement.Display#inputsChanged()}. A change of any other input computes the rows
	 * again and is reported as {@link RowArrangement.Display#rowsReplaced()}.
	 * </p>
	 *
	 * @param rowFunction
	 *        Computes the row objects from the values of all input channels, in their order;
	 *        {@code null} for rows that do not follow the model.
	 * @param observedTypes
	 *        Types whose object changes compute the rows again.
	 * @param inputChannels
	 *        Channels whose values the row function is called with.
	 * @param rowIndependentInputs
	 *        Those of the input channels whose changes cannot change the rows, see
	 *        {@link RowSourceObserver#withRowIndependentInputs(Collection)}.
	 * @param cellIndependentInputs
	 *        Those of the input channels no cell reads, see
	 *        {@link RowSourceObserver#onInputsChanged(Collection, Runnable)}.
	 * @param cellInputs
	 *        Channels the cells read that are no input channels, see
	 *        {@link RowSourceObserver#withCellInputs(Collection)}.
	 */
	public ListRowArrangement(Function<Object[], Collection<?>> rowFunction, Set<TLStructuredType> observedTypes,
			List<ViewChannel> inputChannels, List<ViewChannel> rowIndependentInputs,
			List<ViewChannel> cellIndependentInputs, List<ViewChannel> cellInputs) {
		_rowFunction = rowFunction;
		_observedTypes = observedTypes;
		_inputChannels = inputChannels;
		_rowIndependentInputs = rowIndependentInputs;
		_cellIndependentInputs = cellIndependentInputs;
		_cellInputs = cellInputs;
	}

	@Override
	public RowSource<TLObject> createSource(List<? extends TLObject> rows, RowSetEditSession session,
			List<Column<TLObject, ?>> columns) {
		_source = new ListRowSource<>(new ArrayList<>(rows), columns);
		return _source;
	}

	@Override
	public void bindSelection(TableViewControl<TLObject> table, ViewChannel channel) {
		if (channel != null) {
			_selectionBinding = new TableSelectionBinding(table, channel);
		}
	}

	@Override
	public void observe(Display display) {
		if (_rowFunction == null) {
			return;
		}
		_observer = new RowSourceObserver<>(_source, _rowFunction, _observedTypes, _inputChannels,
			() -> {
				display.rowsReplaced();
				if (_selectionBinding != null) {
					_selectionBinding.rowsRefreshed();
				}
			})
				.withRowIndependentInputs(_rowIndependentInputs)
				.withCellInputs(_cellInputs)
				.onInputsChanged(_cellIndependentInputs, display::inputsChanged);
	}

	@Override
	public void attach(ModelScope scope) {
		if (_observer != null) {
			_observer.attach(scope);
		}
	}

	@Override
	public void detach() {
		if (_observer != null) {
			_observer.detach();
		}
	}

	@Override
	public void release() {
		if (_observer != null) {
			_observer.detach();
			_observer = null;
		}
		if (_selectionBinding != null) {
			_selectionBinding.dispose();
			_selectionBinding = null;
		}
	}

	@Override
	public void setRows(List<? extends TLObject> rows) {
		if (_source != null) {
			_source.setElements(new ArrayList<>(rows));
		}
	}

	@Override
	public Object key(TLObject row) {
		// The row source keys rows by identity.
		return row;
	}

	@Override
	public TLObject row(Object key) {
		return key instanceof TLObject row ? row : null;
	}

}
