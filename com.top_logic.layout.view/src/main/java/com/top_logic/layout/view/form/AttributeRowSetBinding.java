/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link RowSetBinding} anchored at a reference attribute of the form object.
 *
 * <p>
 * The row set is the reference value; adding a row creates a new object of the reference's target
 * type; committing writes the persisted row list back to the reference on the form overlay. The
 * remove semantics follow the model: rows removed from a {@link TLReference#isComposite()
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

	@Override
	public List<TLObject> readRows(TLObject object) {
		if (_part == null || object == null) {
			return new ArrayList<>();
		}
		Object value = object.tValue(_part);
		if (value instanceof Collection<?> collection) {
			List<TLObject> result = new ArrayList<>(collection.size());
			for (Object entry : collection) {
				if (entry instanceof TLObject row) {
					result.add(row);
				}
			}
			return result;
		}
		List<TLObject> result = new ArrayList<>();
		if (value instanceof TLObject row) {
			result.add(row);
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

	@Override
	public void updateMembership(FormControl form, List<TLObject> currentRows) {
		form.getOverlay().tUpdate(_part, currentRows);
	}

	@Override
	public void commit(Transaction tx, FormControl form, List<TLObject> persistedRows, List<TLObject> originalRows) {
		// Update the reference in the main overlay so that the main overlay's apply() writes the
		// persisted row list to the base object.
		form.getOverlay().tUpdate(_part, persistedRows);

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
