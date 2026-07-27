/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link RowSetBinding} whose row set is computed by a query.
 *
 * <p>
 * Unlike an {@link AttributeRowSetBinding}, membership semantics are not derivable from the model:
 * the types offered for row creation and the remove semantics are explicit configuration. Without
 * such configuration the binding offers pure cell editing over the query result (no add, no
 * remove), which requires no configuration at all.
 * </p>
 *
 * <p>
 * Nothing is written back to the form object on commit; created rows are persisted and removed rows
 * deleted (with {@link com.top_logic.layout.view.form.RowSetBinding.RemoveMode#DELETE}) by the
 * {@link AbstractCompositionControl}'s commit, after which a re-evaluation of the query observes the
 * new state.
 * </p>
 */
public class QueryRowSetBinding implements RowSetBinding {

	private final Supplier<List<TLObject>> _rows;

	private final TLClass _rowType;

	private final List<TLClass> _createTypes;

	private final RemoveMode _removeMode;

	/**
	 * Creates a {@link QueryRowSetBinding}.
	 *
	 * @param rows
	 *        Computes the current row objects.
	 * @param rowType
	 *        The row type for column resolution, or {@code null} if unknown.
	 * @param createTypes
	 *        The concrete types offered for row creation; empty disables row creation.
	 * @param removeMode
	 *        The remove semantics;
	 *        {@link com.top_logic.layout.view.form.RowSetBinding.RemoveMode#UNLINK} is not
	 *        meaningful for a query (there is no membership to unlink from) and is treated like
	 *        {@link com.top_logic.layout.view.form.RowSetBinding.RemoveMode#NONE}.
	 */
	public QueryRowSetBinding(Supplier<List<TLObject>> rows, TLClass rowType, List<TLClass> createTypes,
			RemoveMode removeMode) {
		_rows = rows;
		_rowType = rowType;
		_createTypes = List.copyOf(createTypes);
		_removeMode = removeMode;
	}

	@Override
	public boolean resolve(TLObject formObject) {
		return true;
	}

	@Override
	public TLClass getRowType() {
		return _rowType;
	}

	@Override
	public TLStructuredTypePart getBoundPart() {
		return null;
	}

	@Override
	public List<TLObject> readRows(TLObject object) {
		return _rows.get();
	}

	@Override
	public List<TLClass> getCreateTypes() {
		return _createTypes;
	}

	@Override
	public RemoveMode getRemoveMode() {
		return _removeMode;
	}

	@Override
	public void updateMembership(FormControl form, List<TLObject> currentRows) {
		// No bound attribute: membership changes are buffered in the edit session only.
	}

	@Override
	public void commit(Transaction tx, FormControl form, List<TLObject> persistedRows, List<TLObject> originalRows) {
		if (_removeMode == RemoveMode.DELETE) {
			Set<TLObject> current = new HashSet<>(persistedRows);
			for (TLObject original : originalRows) {
				if (!current.contains(original)) {
					original.tDelete();
				}
			}
		}
	}

}
