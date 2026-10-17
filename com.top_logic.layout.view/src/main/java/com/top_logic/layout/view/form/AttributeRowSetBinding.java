/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * {@link RowSetBinding} anchored at a reference attribute of the form object.
 *
 * <p>
 * The row set is the reference value; adding a row creates a new object of the reference's target
 * type; committing writes the persisted row list back to the reference on the editing buffer of the
 * owner. The remove semantics follow the model: rows removed from a {@link TLReference#isComposite()
 * composition} reference are deleted (they cannot exist outside their container), rows removed
 * from a plain reference are only unlinked and keep existing.
 * </p>
 */
public class AttributeRowSetBinding implements RowSetBinding {

	private final String _attributeName;

	private TLStructuredTypePart _part;

	/**
	 * Creates an {@link AttributeRowSetBinding}.
	 *
	 * @param attributeName
	 *        The name of the reference attribute on the form object holding the rows.
	 */
	public AttributeRowSetBinding(String attributeName) {
		_attributeName = attributeName;
	}

	/**
	 * The name of the bound reference attribute.
	 */
	public String getAttributeName() {
		return _attributeName;
	}

	@Override
	public boolean resolve(TLObject formObject) {
		if (formObject == null) {
			_part = null;
			return false;
		}
		TLObject base = baseObject(formObject);
		TLStructuredType type = base.tType();
		_part = type.getPart(_attributeName);
		return _part != null;
	}

	@Override
	public TLClass getRowType() {
		if (_part instanceof TLReference reference) {
			return (TLClass) reference.getType();
		}
		return null;
	}

	@Override
	public TLStructuredTypePart getBoundPart() {
		return _part;
	}

	/**
	 * The rows of the given object the current user may discover.
	 *
	 * <p>
	 * The rows are displayed in a table, which shows no object the user may not discover. Writing
	 * the rows back keeps the ones left out here, see {@link #updateMembership(TLObject, List)}.
	 * </p>
	 */
	@Override
	public List<TLObject> readRows(TLObject object) {
		if (_part == null || object == null) {
			return new ArrayList<>();
		}
		List<?> values = SearchExpression.asList(SearchExpression.filterSecurity(object.tValue(_part)));
		List<TLObject> result = new ArrayList<>(values.size());
		for (Object entry : values) {
			if (entry instanceof TLObject row) {
				result.add(row);
			}
		}
		return result;
	}

	@Override
	public List<TLClass> getCreateTypes() {
		TLClass rowType = getRowType();
		return rowType == null ? List.of() : List.of(rowType);
	}

	@Override
	public RemoveMode getRemoveMode() {
		return isComposite() ? RemoveMode.DELETE : RemoveMode.UNLINK;
	}

	/**
	 * Writes the given rows to the given owner, together with the stored rows the current user may
	 * not discover.
	 *
	 * <p>
	 * The rows the user may not discover are not {@link #readRows(TLObject) displayed}, so the user
	 * cannot have removed them: they stay at their stored positions.
	 * </p>
	 */
	@Override
	public void updateMembership(TLObject owner, List<TLObject> currentRows) {
		owner.tUpdate(_part, withHiddenRows(owner, currentRows));
	}

	@Override
	public void commit(TLObject owner, List<TLObject> persistedRows, List<TLObject> originalRows) {
		// Update the reference in the editing buffer of the owner, so that applying the buffer
		// writes the persisted row list to the owner.
		owner.tUpdate(_part, withHiddenRows(owner, persistedRows));

		if (getRemoveMode() == RemoveMode.DELETE) {
			// Delete objects taken out of a composition (present originally, absent now).
			Set<TLObject> current = new HashSet<>(persistedRows);
			for (TLObject original : originalRows) {
				if (!current.contains(original)) {
					original.tDelete();
				}
			}
		}
	}

	/**
	 * The given rows, with the rows the given owner stores but the current user may not discover
	 * inserted at their stored positions.
	 */
	private List<TLObject> withHiddenRows(TLObject owner, List<TLObject> rows) {
		List<?> stored = SearchExpression.asList(baseObject(owner).tValue(_part));
		List<?> discoverable = SearchExpression.asList(SearchExpression.filterSecurity(stored));
		if (discoverable.size() == stored.size()) {
			return rows;
		}
		Set<?> visible = new HashSet<>(discoverable);
		List<TLObject> result = new ArrayList<>(rows);
		for (int index = 0, cnt = stored.size(); index < cnt; index++) {
			if (stored.get(index) instanceof TLObject hidden && !visible.contains(hidden) && !result.contains(hidden)) {
				result.add(Math.min(index, result.size()), hidden);
			}
		}
		return result;
	}

	private boolean isComposite() {
		return _part instanceof TLReference reference && reference.isComposite();
	}

	private static TLObject baseObject(TLObject object) {
		if (object instanceof TLObjectOverlay overlay) {
			return overlay.getBase();
		}
		return object;
	}

}
