/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.List;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.table.Column;
import com.top_logic.table.Row;
import com.top_logic.table.RowSource;

/**
 * How a {@link RowSetTableControl} arranges its rows: where its {@link RowSource} takes them from,
 * how they follow the model outside an edit session, how its selection is bound, and how a row
 * relates to its {@link Row#key() key} in the table.
 *
 * <p>
 * The {@link RowSetTableControl} builds a table anew on every switch between view and edit mode.
 * For each build it {@link #release() releases} what it bound to the previous table,
 * {@link #createSource(List, List) creates the source} of the new one, {@link #bindSelection binds
 * its selection} and - outside an edit session - {@link #observe(Display) observes} the model for
 * it. While the session runs, the rows are frozen: they change only when the session replaces
 * them, see {@link #setRows(List)}.
 * </p>
 */
public interface RowArrangement {

	/**
	 * What a {@link RowArrangement} tells the table displaying its rows.
	 */
	interface Display {

		/**
		 * The rows were computed anew and are in the source: the table builds its cells again and
		 * pushes its rows.
		 */
		void rowsReplaced();

		/**
		 * An input changed that leaves the rows and their cells as they are: what the table
		 * computes from the inputs apart from the rows is computed again.
		 */
		void inputsChanged();
	}

	/**
	 * Creates the source of a table built with the given columns, holding the given rows.
	 *
	 * @param rows
	 *        The row objects of the table: those of the running edit session, or those of the
	 *        binding outside one.
	 * @param columns
	 *        The columns of the table.
	 */
	RowSource<TLObject> createSource(List<? extends TLObject> rows, List<Column<TLObject, ?>> columns);

	/**
	 * Binds the selection of the given table, built over the source {@link #createSource(List, List)
	 * created} last, two-way to the given channel.
	 *
	 * @param table
	 *        The table whose selection is bound.
	 * @param channel
	 *        The selection channel, {@code null} for a table publishing no selection.
	 */
	void bindSelection(TableViewControl<TLObject> table, ViewChannel channel);

	/**
	 * Begins to follow the model and the inputs with the rows of the source
	 * {@link #createSource(List, List) created} last, telling the given display about every
	 * change.
	 *
	 * <p>
	 * Called outside an edit session only. The observation takes effect while the arrangement is
	 * {@link #attach(ModelScope) attached}.
	 * </p>
	 */
	void observe(Display display);

	/**
	 * Begins or resumes the {@link #observe(Display) observation} on the given scope.
	 */
	void attach(ModelScope scope);

	/**
	 * Suspends the {@link #observe(Display) observation}: the rows stay as they are until the
	 * arrangement is {@link #attach(ModelScope) attached} again.
	 */
	void detach();

	/**
	 * Stops the {@link #observe(Display) observation} and drops the
	 * {@link #bindSelection(TableViewControl, ViewChannel) selection binding} of the table built
	 * last.
	 */
	void release();

	/**
	 * Replaces the rows of the source {@link #createSource(List, List) created} last while an edit
	 * session runs.
	 *
	 * @param rows
	 *        The row objects of the session.
	 */
	void setRows(List<? extends TLObject> rows);

	/**
	 * The {@link Row#key() key} of the row displaying the given row object.
	 */
	Object key(TLObject row);

	/**
	 * The row object displayed in the row with the given {@link Row#key() key}, {@code null} where
	 * the key names no row object.
	 */
	TLObject row(Object key);

}
