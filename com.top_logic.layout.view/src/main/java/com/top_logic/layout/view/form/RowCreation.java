/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.List;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * How the {@link RowSetTableControl#getAddRowCommand() add command} of a row-editing table creates
 * a row: which types it offers, and where the new row goes.
 *
 * <p>
 * The rows of a flat table are created as the {@link RowSetBinding} says, see {@link #BINDING}. A
 * table whose new row depends on the selected rows - the child of the selected row in a tree -
 * creates them otherwise.
 * </p>
 */
public interface RowCreation {

	/**
	 * Offers the {@link RowSetEditSession#createTypes() create types} of the session and adds a
	 * row of the chosen type to the row set, independent of the selection.
	 */
	RowCreation BINDING = new RowCreation() {
		@Override
		public boolean offersCreation(RowSetEditSession session) {
			return !session.createTypes().isEmpty();
		}

		@Override
		public List<TLClass> createTypes(RowSetEditSession session, List<TLObject> selectedRows) {
			return session.createTypes();
		}

		@Override
		public TLObject addRow(RowSetEditSession session, TLClass type, List<TLObject> selectedRows) {
			return session.addRow(type, null);
		}
	};

	/**
	 * Whether the table offers to create rows at all while the given session runs: whether the add
	 * command is displayed.
	 *
	 * @param session
	 *        The running edit session.
	 */
	boolean offersCreation(RowSetEditSession session);

	/**
	 * The types the user may choose from for a new row, for the given selection.
	 *
	 * @param session
	 *        The running edit session.
	 * @param selectedRows
	 *        The selected rows, the row objects the session edits.
	 * @return The concrete types offered, sorted for display; empty where no row can be created for
	 *         the selection, so that the add command is not executable.
	 */
	List<TLClass> createTypes(RowSetEditSession session, List<TLObject> selectedRows);

	/**
	 * Adds a new row of the given type for the given selection.
	 *
	 * @param session
	 *        The running edit session.
	 * @param type
	 *        One of the {@link #createTypes(RowSetEditSession, List) types offered} for the
	 *        selection.
	 * @param selectedRows
	 *        The selected rows, the row objects the session edits.
	 * @return The new row, the buffer the session edits it through; {@code null} if none was
	 *         created.
	 */
	TLObject addRow(RowSetEditSession session, TLClass type, List<TLObject> selectedRows);

}
