/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import junit.framework.TestCase;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.EditLevel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.table.CellEditing;

/**
 * Tests that an editing restricted to some rows ({@link CellEditing#restrictedTo}) keeps what the
 * editing it restricts says about its rows.
 */
public class TestRestrictedCellEditing extends TestCase {

	/** An editing restricted to some rows requires a value where the editing it restricts does. */
	public void testRequiredIsKept() {
		assertTrue(editing(true).restrictedTo(row -> false).required());
		assertTrue(editing(true).restrictedTo(row -> true).restrictedTo(row -> false).required());
		assertFalse(editing(false).restrictedTo(row -> true).required());
	}

	/** A restriction only removes rows from the edit. */
	public void testRestrictionOnlyRemovesRows() {
		CellEditing restricted = editing(true).restrictedTo("edit"::equals);
		assertTrue(restricted.canEdit("edit"));
		assertFalse(restricted.canEdit("other"));
		assertFalse(new TestEditing(false, false).restrictedTo(row -> true).canEdit("edit"));
	}

	private static CellEditing editing(boolean required) {
		return new TestEditing(true, required);
	}

	private static final class TestEditing implements CellEditing {

		private final boolean _editable;

		private final boolean _required;

		TestEditing(boolean editable, boolean required) {
			_editable = editable;
			_required = required;
		}

		@Override
		public boolean canEdit(Object row) {
			return _editable;
		}

		@Override
		public BoundFieldModel createModel(Object row, FormControl form, EditLevel level) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ReactControl createControl(ReactContext context, Object row, BoundFieldModel model) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean required() {
			return _required;
		}
	}

}
