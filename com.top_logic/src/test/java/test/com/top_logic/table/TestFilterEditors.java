/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.Option;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.ComparisonOperator;
import com.top_logic.table.filter.FilterEditor;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;

/**
 * Test for {@link FilterEditors}: each editor seeds from the current state and reads the
 * edited field values back into a {@link com.top_logic.table.FilterState}.
 */
public class TestFilterEditors extends TestCase {

	private static FieldModel field(FilterEditor editor, int index) {
		return editor.fields().get(index).model();
	}

	public void testTextEditorReadBack() {
		FilterEditor editor = FilterEditors.create(TextColumnFilter.forStrings(), null, MatchCounts.NONE);
		assertTrue(((TextFilterState) editor.read()).isEmpty());

		field(editor, 0).setValue("abc");   // pattern
		field(editor, 1).setValue(Boolean.TRUE);   // case sensitive
		field(editor, 3).setValue(Boolean.TRUE);   // whole field
		TextFilterState state = (TextFilterState) editor.read();
		assertEquals("abc", state.pattern());
		assertTrue(state.caseSensitive());
		assertFalse(state.regexp());
		assertTrue(state.wholeField());
	}

	public void testTextEditorSeededFromState() {
		FilterEditor editor =
			FilterEditors.create(TextColumnFilter.forStrings(), new TextFilterState("x", true, false, false),
				MatchCounts.NONE);
		assertEquals("x", field(editor, 0).getValue());
		assertEquals(Boolean.TRUE, field(editor, 1).getValue());
	}

	public void testBooleanEditorReadBack() {
		FilterEditor editor = FilterEditors.create(BooleanColumnFilter.INSTANCE, null, MatchCounts.NONE);
		field(editor, 0).setValue(Boolean.TRUE);    // accept true
		field(editor, 2).setValue(Boolean.TRUE);    // accept null
		BooleanFilterState state = (BooleanFilterState) editor.read();
		assertTrue(state.acceptTrue());
		assertFalse(state.acceptFalse());
		assertTrue(state.acceptNull());
	}

	public void testOptionsEditorReadBack() {
		OptionsColumnFilter<String> filter = new OptionsColumnFilter<>(List.of(
			new Option("red", ResKey.text("Red")),
			new Option("blue", ResKey.text("Blue")),
			new Option("green", ResKey.text("Green"))));
		FilterEditor editor = FilterEditors.create(filter, null, MatchCounts.NONE);
		// Three options plus the appended invert checkbox.
		assertEquals(4, editor.fields().size());

		field(editor, 0).setValue(Boolean.TRUE);    // red
		field(editor, 2).setValue(Boolean.TRUE);    // green
		OptionsFilterState state = (OptionsFilterState) editor.read();
		assertEquals(java.util.Set.of("red", "green"), state.selected());
	}

	public void testInvertCheckboxWrapsStateOnRead() {
		FilterEditor editor = FilterEditors.create(TextColumnFilter.forStrings(), null, MatchCounts.NONE);
		List<?> fields = editor.fields();
		// Pattern + 3 text options + the invert checkbox last.
		assertEquals(5, fields.size());

		field(editor, 0).setValue("abc");        // pattern
		field(editor, 4).setValue(Boolean.TRUE); // invert
		FilterState state = editor.read();
		assertTrue(state instanceof NegatedFilterState);
		assertEquals("abc", ((TextFilterState) ((NegatedFilterState) state).inner()).pattern());
	}

	public void testInvertEditorSeededFromNegatedState() {
		FilterEditor editor = FilterEditors.create(TextColumnFilter.forStrings(),
			new NegatedFilterState(TextFilterState.contains("x")), MatchCounts.NONE);
		assertEquals("x", field(editor, 0).getValue());
		assertEquals("Invert checkbox reflects the persisted inversion.", Boolean.TRUE, field(editor, 4).getValue());
	}

	public void testInvertCheckboxIgnoredWhenSelectionEmpty() {
		FilterEditor editor = FilterEditors.create(TextColumnFilter.forStrings(), null, MatchCounts.NONE);
		field(editor, 4).setValue(Boolean.TRUE); // invert, but no pattern
		FilterState state = editor.read();
		assertFalse("Inverting an empty selection stays inactive.", state instanceof NegatedFilterState);
		assertTrue(state.isEmpty());
	}

	public void testComparableEditorReadBack() {
		FilterEditor editor = FilterEditors.create(ComparableColumnFilter.integers(), null, MatchCounts.NONE);
		field(editor, 0).setValue(ComparisonOperator.GE);   // operator
		field(editor, 1).setValue("18");                    // primary

		@SuppressWarnings("unchecked")
		RangeFilterState<Integer> state = (RangeFilterState<Integer>) editor.read();
		assertEquals(ComparisonOperator.GE, state.operator());
		assertEquals(Integer.valueOf(18), state.primary());
		assertNull(state.secondary());
	}

	public void testComparableEditorBetween() {
		FilterEditor editor = FilterEditors.create(ComparableColumnFilter.integers(),
			RangeFilterState.between(5, 10), MatchCounts.NONE);
		// Seeded from state.
		assertEquals(ComparisonOperator.BETWEEN, field(editor, 0).getValue());
		assertEquals("5", field(editor, 1).getValue());
		assertEquals("10", field(editor, 2).getValue());

		@SuppressWarnings("unchecked")
		RangeFilterState<Integer> state = (RangeFilterState<Integer>) editor.read();
		assertEquals(Integer.valueOf(5), state.primary());
		assertEquals(Integer.valueOf(10), state.secondary());
	}

}
