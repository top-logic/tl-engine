/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.table.ColumnFilterState;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;
import com.top_logic.table.Option;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ColumnFilterEditor;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.ComparisonOperator;
import com.top_logic.table.filter.FilterEditor;
import com.top_logic.table.filter.FilterEditors;
import com.top_logic.table.filter.FilterField;
import com.top_logic.table.filter.I18NConstants;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.filter.TextMatchMode;

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
		field(editor, 1).setValue(TextMatchMode.WHOLE_WORD);   // search mode
		field(editor, 2).setValue(Boolean.TRUE);   // case sensitive
		TextFilterState state = (TextFilterState) editor.read();
		assertEquals("abc", state.pattern());
		assertTrue(state.caseSensitive());
		assertEquals(TextMatchMode.WHOLE_WORD, state.mode());
		assertFalse(state.wholeField());
	}

	public void testTextEditorSeededFromState() {
		FilterEditor editor =
			FilterEditors.create(TextColumnFilter.forStrings(),
				new TextFilterState("x", true, TextMatchMode.WORD_START, true), MatchCounts.NONE);
		assertEquals("x", field(editor, 0).getValue());
		assertEquals(TextMatchMode.WORD_START, field(editor, 1).getValue());
		assertEquals(Boolean.TRUE, field(editor, 2).getValue());
		assertTrue("The whole-field flag is kept.", ((TextFilterState) editor.read()).wholeField());
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

	public void testBooleanEditorWithoutNoValueOption() {
		BooleanColumnFilter filter =
			new BooleanColumnFilter(ResKey.text("Yes"), ResKey.text("No"), false);
		FilterEditor editor = FilterEditors.create(filter, null, MatchCounts.NONE);
		assertEquals("A column without empty cells offers just the two value options, and no invert.", 2,
			editor.fields().size());

		field(editor, 0).setValue(Boolean.TRUE);    // accept true
		BooleanFilterState state = (BooleanFilterState) editor.read();
		assertTrue(state.acceptTrue());
		assertFalse(state.acceptFalse());
		assertFalse(state.acceptNull());
	}

	/**
	 * A yes / no filter offers no inversion: each of its values is an option of its own, so the
	 * inverse of a selection is the selection of the other options. Every other kind offers it.
	 */
	public void testInvertNotOfferedForBoolean() {
		ColumnFilterEditor bool =
			FilterEditors.create(List.of(BooleanColumnFilter.INSTANCE), null, kind -> MatchCounts.NONE);
		assertFalse(bool.invertOffered());
		assertEquals("True, false, no value.", 3, bool.fields().size());
		for (FilterField field : bool.fields()) {
			assertNotSame(I18NConstants.FILTER_INVERT, field.label());
		}

		ColumnFilterEditor text =
			FilterEditors.create(List.of(TextColumnFilter.forStrings()), null, kind -> MatchCounts.NONE);
		assertTrue(text.invertOffered());
		assertEquals(I18NConstants.FILTER_INVERT, text.fields().get(text.fields().size() - 1).label());
	}

	/**
	 * An inverted yes / no state, e.g. of a declared filter, keeps the checkbox, so that it can be
	 * undone.
	 */
	public void testInvertKeptForInvertedBoolean() {
		ColumnFilterEditor bool = FilterEditors.create(List.of(BooleanColumnFilter.INSTANCE),
			ColumnFilterState.invert(new BooleanFilterState(true, false, false)), kind -> MatchCounts.NONE);
		assertTrue(bool.invertOffered());
		FieldModel invert = bool.fields().get(bool.fields().size() - 1).model();
		assertEquals(Boolean.TRUE, invert.getValue());

		invert.setValue(Boolean.FALSE);
		assertEquals(new BooleanFilterState(true, false, false), ColumnFilterState.innerOf(bool.read()));
		assertFalse(ColumnFilterState.isInverted(bool.read()));
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
		// Pattern, search mode, case sensitive + the invert checkbox last.
		assertEquals(4, fields.size());

		field(editor, 0).setValue("abc");        // pattern
		field(editor, 3).setValue(Boolean.TRUE); // invert
		FilterState state = editor.read();
		assertEquals(ColumnFilterState.invert(TextFilterState.contains("abc")), state);
	}

	public void testInvertEditorSeededFromNegatedState() {
		FilterEditor editor = FilterEditors.create(TextColumnFilter.forStrings(),
			ColumnFilterState.invert(TextFilterState.contains("x")), MatchCounts.NONE);
		assertEquals("x", field(editor, 0).getValue());
		assertEquals("Invert checkbox reflects the persisted inversion.", Boolean.TRUE, field(editor, 3).getValue());
	}

	public void testInvertCheckboxIgnoredWhenSelectionEmpty() {
		FilterEditor editor = FilterEditors.create(TextColumnFilter.forStrings(), null, MatchCounts.NONE);
		field(editor, 3).setValue(Boolean.TRUE); // invert, but no pattern
		FilterState state = editor.read();
		assertFalse("Inverting an empty selection stays inactive.", state instanceof ColumnFilterState);
		assertTrue(state.isEmpty());
	}

	public void testComparableEditorReadBack() {
		FilterEditor editor = FilterEditors.create(ComparableColumnFilter.integers(), null, MatchCounts.NONE);
		field(editor, 0).setValue(ComparisonOperator.GE);   // operator
		field(editor, 1).setValue(Integer.valueOf(18));     // primary, typed

		@SuppressWarnings("unchecked")
		RangeFilterState<Integer> state = (RangeFilterState<Integer>) editor.read();
		assertEquals(ComparisonOperator.GE, state.operator());
		assertEquals(Integer.valueOf(18), state.primary());
		assertNull(state.secondary());
	}

	/**
	 * A bound field holds the typed bound and carries the codec a UI without a typed input writes and
	 * reads it with, in the reader's language: a German user sees and types the decimal fraction
	 * with a comma.
	 */
	@SuppressWarnings("unchecked")
	public void testComparableEditorInTheReadersLanguage() {
		ComparableColumnFilter<Number> filter = new ComparableColumnFilter<>(
			Comparator.comparingDouble(Number::doubleValue),
			BoundCodec.numbers(NumberFormat.getInstance(Locale.GERMANY)));
		FilterEditor editor = FilterEditors.create(filter,
			RangeFilterState.of(ComparisonOperator.GE, Double.valueOf(37.5)), MatchCounts.NONE);

		FilterField primary = editor.fields().get(1);
		assertEquals(Double.valueOf(37.5), primary.model().getValue());
		BoundCodec<Number> codec = (BoundCodec<Number>) primary.valueCodec();
		assertSame(filter.codec(), codec);
		assertEquals("37,5", codec.format((Number) primary.model().getValue()));

		primary.model().setValue(codec.parse("12,25"));
		assertEquals(Double.valueOf(12.25), ((RangeFilterState<?>) editor.read()).primary());

		primary.model().setValue(codec.parse("kein Wert"));
		assertNull("Text that is not a number leaves the bound unset.",
			((RangeFilterState<?>) editor.read()).primary());

		assertNull("The operator holds no value of the column.", editor.fields().get(0).valueCodec());
	}

	public void testComparableEditorBetween() {
		FilterEditor editor = FilterEditors.create(ComparableColumnFilter.integers(),
			RangeFilterState.between(5, 10), MatchCounts.NONE);
		// Seeded from state.
		assertEquals(ComparisonOperator.BETWEEN, field(editor, 0).getValue());
		assertEquals(Integer.valueOf(5), field(editor, 1).getValue());
		assertEquals(Integer.valueOf(10), field(editor, 2).getValue());

		@SuppressWarnings("unchecked")
		RangeFilterState<Integer> state = (RangeFilterState<Integer>) editor.read();
		assertEquals(Integer.valueOf(5), state.primary());
		assertEquals(Integer.valueOf(10), state.secondary());
	}

}
