/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.RowCreation;
import com.top_logic.layout.view.form.RowSetEditSession;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link RowCreation} of a row-editing tree table: a new row is a child of the selected row, or a
 * top-level row while no row is selected.
 *
 * <p>
 * The parent of the new row is the selected row - the buffer the session edits it through - or the
 * {@link TreeRowArrangement#rootObject() object the tree is built from} while no row is selected.
 * With more than one selected row, no row is created. A function of the parent computes the type of
 * the new row: a concrete class is created directly, an abstract one stands for its concrete
 * specializations, which the user chooses from, and nothing offers no creation for the parent.
 * </p>
 *
 * <p>
 * The new row is created within the buffer of its parent, and a function of the parent and the new
 * row inserts the row where the children function of the tree finds it - typically into a
 * composition of the parent. It writes into the buffers of the session, so the insertion is an edit
 * like any other: stored with the form, discarded when the form is cancelled. The parent of a
 * top-level row is edited through {@link RowSetEditSession#editObject(TLObject)}. The tree then
 * computes the children of the parent again, so the new row is displayed under it.
 * </p>
 *
 * <p>
 * Both functions see the values of the input channels in front of their own arguments, and run in
 * a {@link TracingScope} over the {@link RowSetEditSession#overlays() overlays} of the form, so an
 * object they navigate to is read as it is edited.
 * </p>
 */
public class ChildRowCreation implements RowCreation {

	private final QueryExecutor _createType;

	private final QueryExecutor _insert;

	private final List<ViewChannel> _inputChannels;

	private final TreeRowArrangement _arrangement;

	/**
	 * Creates a {@link ChildRowCreation}.
	 *
	 * @param createType
	 *        Computes the type of a new row from the values of the input channels and the parent: a
	 *        model class, or nothing.
	 * @param insert
	 *        Inserts the new row, taking the values of the input channels, the parent and the new
	 *        row.
	 * @param inputChannels
	 *        The input channels whose values the functions see.
	 * @param arrangement
	 *        The tree the rows are arranged in.
	 */
	public ChildRowCreation(QueryExecutor createType, QueryExecutor insert, List<ViewChannel> inputChannels,
			TreeRowArrangement arrangement) {
		_createType = createType;
		_insert = insert;
		_inputChannels = inputChannels;
		_arrangement = arrangement;
	}

	/**
	 * Rows are created while the rows of the session may be changed; which ones depends on the
	 * selection.
	 */
	@Override
	public boolean offersCreation(RowSetEditSession session) {
		return session.isEditable();
	}

	@Override
	public List<TLClass> createTypes(RowSetEditSession session, List<TLObject> selectedRows) {
		TLObject parent = parent(selectedRows);
		if (parent == null || !session.isEditable() || session.isRemoved(parent)) {
			return List.of();
		}
		Object result;
		try (TracingScope scope = TracingScope.open(null, session.overlays())) {
			result = _createType.execute(ChannelInputs.arguments(_inputChannels, parent));
		}
		if (result == null) {
			return List.of();
		}
		if (!(result instanceof TLClass type)) {
			throw new IllegalStateException("The '" + TreeTableElement.AddChildConfig.CREATE_TYPE
				+ "' of an <" + TreeTableElement.Config.ADD_CHILD + "> must compute a model class, got: " + result);
		}
		return session.createTypes(ModelAccessPolicy.concreteTypes(type));
	}

	@Override
	public TLObject addRow(RowSetEditSession session, TLClass type, List<TLObject> selectedRows) {
		TLObject parent = parent(selectedRows);
		if (parent == null) {
			return null;
		}
		TLObject parentBuffer = selectedRows.isEmpty() ? session.editObject(parent) : parent;
		if (parentBuffer == null) {
			return null;
		}
		TLObject row = session.addRow(type, parentBuffer, null);
		if (row == null) {
			return null;
		}
		try (TracingScope scope = TracingScope.open(null, session.overlays())) {
			_insert.execute(ChannelInputs.arguments(_inputChannels, parentBuffer, row));
		}
		_arrangement.childrenChanged(parentBuffer);
		return row;
	}

	/**
	 * The parent of a new row for the given selection: the selected row, the object the tree is
	 * built from without a selection, {@code null} for several selected rows or a tree built from
	 * no model object.
	 */
	private TLObject parent(List<TLObject> selectedRows) {
		switch (selectedRows.size()) {
			case 0:
				return _arrangement.rootObject() instanceof TLObject root ? root : null;
			case 1:
				return selectedRows.get(0);
			default:
				return null;
		}
	}

}
