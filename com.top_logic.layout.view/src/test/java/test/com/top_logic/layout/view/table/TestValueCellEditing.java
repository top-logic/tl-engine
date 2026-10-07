/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.element.meta.kbbased.storage.mappings.IntMapping;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.table.CellEditing;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.layout.view.table.ValueCellEditing;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link ValueCellEditing}: which rows of a column over computed values offer an edit, and
 * what an edited cell does with the value entered into it.
 */
public class TestValueCellEditing extends TestCase {

	/** The column the rows under test are edited through. */
	private static final String COLUMN = "total";

	/** The module holding the test model. */
	private static final String MODULE = "test.valueCellEditing";

	/** What the update function was called with, in call order. */
	private List<Object[]> _updates;

	/** The type of the edited values. */
	private TLType _valueType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_updates = new ArrayList<>();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, MODULE);
		_valueType = TLModelUtil.addDatatype(module, module, "Integer", Kind.INT, IntMapping.INSTANCE);
	}

	/**
	 * A cell offers the edit exactly on the rows the column's predicate accepts.
	 */
	public void testOnlyAcceptedRowsAreEdited() {
		CellEditing editing = editing(row -> Boolean.TRUE.equals(row(row).get("open")));

		assertTrue("An open row is edited.", editing.canEdit(row(Boolean.TRUE, 1)));
		assertFalse("A closed row is not.", editing.canEdit(row(Boolean.FALSE, 1)));
	}

	/**
	 * The field of a cell shows what the column computes for its row.
	 */
	public void testTheFieldShowsTheComputedValue() {
		Map<String, Object> row = row(Boolean.TRUE, 3);

		assertEquals(Integer.valueOf(3), editing().createModel(row, null).getValue());
	}

	/**
	 * An edited value is written by the column's update function, which receives the row and the
	 * value entered, and the field then shows what the row holds.
	 */
	public void testEditingRunsTheUpdate() {
		Map<String, Object> row = row(Boolean.TRUE, 3);
		BoundFieldModel field = editing().createModel(row, null);

		List<Object> changes = new ArrayList<>();
		field.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				changes.add(newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not under test.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Not under test.
			}
		});

		field.setValue(Integer.valueOf(7));

		assertEquals("The update is called once, with the row and the entered value.", 1, _updates.size());
		assertSame("The update receives the edited row.", row, _updates.get(0)[0]);
		assertEquals("The update receives the entered value.", Integer.valueOf(7), _updates.get(0)[1]);
		assertEquals("The field shows what the update wrote.", Integer.valueOf(7), field.getValue());
		assertEquals("The edit is announced.", List.of(Integer.valueOf(7)), changes);
		assertTrue("An edited field differs from what it started with.", field.isDirty());
	}

	/**
	 * Entering the value a cell already holds is no edit: nothing is written and nothing is
	 * announced.
	 */
	public void testUnchangedValueWritesNothing() {
		Map<String, Object> row = row(Boolean.TRUE, 3);
		BoundFieldModel field = editing().createModel(row, null);

		field.setValue(Integer.valueOf(3));

		assertEquals("Nothing was written.", List.of(), _updates);
		assertFalse("The field holds what it started with.", field.isDirty());
	}

	/**
	 * A value the row changed elsewhere reaches the field when it is refreshed.
	 */
	public void testRefreshShowsWhatTheRowHolds() {
		Map<String, Object> row = row(Boolean.TRUE, 3);
		BoundFieldModel field = editing().createModel(row, null);

		row.put(COLUMN, Integer.valueOf(5));
		field.refreshFromObject();

		assertEquals(Integer.valueOf(5), field.getValue());
	}

	/** Editing of a column whose every row is edited. */
	private CellEditing editing() {
		return editing(row -> true);
	}

	/**
	 * Editing of a column reading and writing the {@link #COLUMN} entry of its rows, recording
	 * every write.
	 *
	 * @param canUpdate
	 *        Which rows offer the edit.
	 */
	private CellEditing editing(Predicate<Object> canUpdate) {
		return new ValueCellEditing(ColumnType.of(_valueType, false),
			row -> row(row).get(COLUMN),
			(row, value) -> {
				_updates.add(new Object[] { row, value });
				row(row).put(COLUMN, value);
			},
			canUpdate);
	}

	/** A row that is open or closed, holding the given value in the edited column. */
	private static Map<String, Object> row(Boolean open, int value) {
		Map<String, Object> result = new HashMap<>();
		result.put("open", open);
		result.put(COLUMN, Integer.valueOf(value));
		return result;
	}

	/** The given row as the map the test rows are. */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> row(Object row) {
		return (Map<String, Object>) row;
	}

	/**
	 * Test suite requiring the {@link AttributeSettings} the test model is built through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestValueCellEditing.class, AttributeSettings.Module.INSTANCE));
	}

}
