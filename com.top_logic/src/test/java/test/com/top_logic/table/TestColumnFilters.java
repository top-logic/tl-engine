/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.Option;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.ComparisonOperator;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.RegexpOptionsFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;

/**
 * Test for the concrete {@link com.top_logic.table.ColumnFilter} library.
 */
public class TestColumnFilters extends TestCase {

	/** A reference-like cell value: a business object identified by value equality. */
	private record Department(String key) {
		// Test fixture.
	}

	public void testTextContainsCaseInsensitive() {
		Predicate<String> p = TextColumnFilter.forStrings().predicate(TextFilterState.contains("AL"));
		assertTrue(p.test("Alice"));
		assertTrue(p.test("normal"));
		assertFalse(p.test("Bob"));
		assertFalse(p.test(null));
	}

	public void testTextCaseSensitiveWholeField() {
		Predicate<String> p =
			TextColumnFilter.forStrings().predicate(new TextFilterState("Alice", true, false, true));
		assertTrue(p.test("Alice"));
		assertFalse(p.test("alice"));
		assertFalse(p.test("Alice2"));
	}

	public void testTextRegexp() {
		Predicate<String> p =
			TextColumnFilter.forStrings().predicate(new TextFilterState("^a.*e$", false, true, false));
		assertTrue(p.test("apple"));
		assertTrue(p.test("APPLE"));
		assertFalse(p.test("banana"));
	}

	public void testTextStateEmpty() {
		assertTrue(TextFilterState.contains("").isEmpty());
		assertFalse(TextFilterState.contains("x").isEmpty());
	}

	public void testComparableOperators() {
		ComparableColumnFilter<Integer> filter = ComparableColumnFilter.natural();
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.GE, 10)).test(10));
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.GT, 10)).test(11));
		assertFalse(filter.predicate(RangeFilterState.of(ComparisonOperator.GT, 10)).test(10));
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.LT, 10)).test(9));
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.EQ, 7)).test(7));
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.NE, 7)).test(8));
		assertFalse(filter.predicate(RangeFilterState.of(ComparisonOperator.EQ, 7)).test(null));
	}

	public void testComparableBetween() {
		ComparableColumnFilter<Integer> filter = ComparableColumnFilter.natural();
		Predicate<Integer> p = filter.predicate(RangeFilterState.between(5, 10));
		assertTrue(p.test(5));
		assertTrue(p.test(10));
		assertTrue(p.test(7));
		assertFalse(p.test(4));
		assertFalse(p.test(11));
	}

	public void testRangeStateEmpty() {
		assertTrue(RangeFilterState.of(ComparisonOperator.EQ, null).isEmpty());
		assertTrue(RangeFilterState.between(5, null).isEmpty());
		assertFalse(RangeFilterState.of(ComparisonOperator.EQ, 5).isEmpty());
	}

	public void testOptions() {
		OptionsColumnFilter<String> filter = new OptionsColumnFilter<>(List.of(
			new Option("red", ResKey.text("Red")),
			new Option("blue", ResKey.text("Blue"))));
		assertTrue(filter.countsMatches());
		Predicate<String> p = filter.predicate(new OptionsFilterState(Set.of("red")));
		assertTrue(p.test("red"));
		assertFalse(p.test("blue"));
		assertTrue(new OptionsFilterState(Set.of()).isEmpty());
	}

	public void testBooleanTriState() {
		Predicate<Boolean> onlyTrue = BooleanColumnFilter.INSTANCE.predicate(
			new BooleanFilterState(true, false, false));
		assertTrue(onlyTrue.test(Boolean.TRUE));
		assertFalse(onlyTrue.test(Boolean.FALSE));
		assertFalse(onlyTrue.test(null));

		Predicate<Boolean> falseOrNull = BooleanColumnFilter.INSTANCE.predicate(
			new BooleanFilterState(false, true, true));
		assertFalse(falseOrNull.test(Boolean.TRUE));
		assertTrue(falseOrNull.test(Boolean.FALSE));
		assertTrue(falseOrNull.test(null));
	}

	public void testBooleanStateEmptyWhenAllOrNothing() {
		assertTrue(new BooleanFilterState(false, false, false).isEmpty());
		assertTrue(new BooleanFilterState(true, true, true).isEmpty());
		assertFalse(new BooleanFilterState(true, false, false).isEmpty());
	}

	public void testTextSerialization() {
		TextColumnFilter<String> filter = TextColumnFilter.forStrings();
		TextFilterState original = new TextFilterState("ab", true, false, true);
		assertEquals(original, filter.fromJson(filter.toJson(original)));
		assertNull("Malformed JSON yields no state.", filter.fromJson("not-a-map"));
	}

	public void testComparableSerialization() {
		ComparableColumnFilter<Integer> filter = ComparableColumnFilter.integers();
		RangeFilterState<Integer> original = RangeFilterState.between(5, 10);
		assertEquals(original, filter.fromJson(filter.toJson(original)));

		// A parser-less filter cannot restore typed bounds and so declines persistence.
		assertNull(ComparableColumnFilter.<Integer> natural().toJson(RangeFilterState.of(ComparisonOperator.EQ, 1)));
	}

	/**
	 * A numeric bound is stored as the number it is, not as the text one language writes it in - so
	 * a filter saved by a German user is the same filter when an English one opens it.
	 */
	public void testNumericBoundsAreStoredLanguageIndependently() {
		ComparableColumnFilter<Number> filter = germanNumberFilter();
		RangeFilterState<Number> original = RangeFilterState.of(ComparisonOperator.GE, Double.valueOf(37.5));

		Map<?, ?> json = (Map<?, ?>) filter.toJson(original);
		assertEquals("A stored bound is a number.",
			Double.valueOf(37.5), json.get(ComparableColumnFilter.PRIMARY));
		assertEquals(original, filter.fromJson(json));
	}

	/**
	 * A bound stored as text by an earlier version is read in the language-independent form it was
	 * written in, rather than in the reader's - where a dot groups the digits and would turn 37.5
	 * into 375.
	 */
	public void testNumericBoundStoredAsText() {
		ComparableColumnFilter<Number> filter = germanNumberFilter();

		FilterState restored =
			filter.fromJson(Map.of(ComparableColumnFilter.OPERATOR, ComparisonOperator.GE.name(),
				ComparableColumnFilter.PRIMARY, "37.5"));

		assertEquals(RangeFilterState.of(ComparisonOperator.GE, Double.valueOf(37.5)), restored);
	}

	/**
	 * A bound is typed in the reader's language, and written back the same way.
	 */
	public void testNumericBoundsInTheReadersLanguage() {
		BoundCodec<Number> codec = BoundCodec.numbers(NumberFormat.getInstance(Locale.GERMANY));

		assertEquals(Double.valueOf(37.5), codec.parse("37,5"));
		assertEquals("37,5", codec.format(Double.valueOf(37.5)));
		assertNull("Text that is not a number is not a bound.", codec.parse("kein Wert"));
		assertNull(codec.parse(""));
		assertNull(codec.format(null));
		assertNull(codec.toJson(null));
		assertNull(codec.fromJson(null));
	}

	/**
	 * A whole-number format keeps its bounds whole.
	 */
	public void testWholeNumberBounds() {
		BoundCodec<Number> codec = BoundCodec.numbers(NumberFormat.getIntegerInstance(Locale.GERMANY));

		assertEquals(Long.valueOf(1234), codec.parse("1.234"));
		assertEquals(Long.valueOf(37), codec.fromJson(Double.valueOf(37.0)));
	}

	/**
	 * A bound that is a point in time is stored in ISO-8601 form, which reads back as the moment it
	 * was written - unlike the text {@link Date#toString()} produces.
	 */
	public void testTemporalBoundsAreStoredAsIso() {
		BoundCodec<Date> codec = BoundCodec.dates(
			List.of(new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)), List.of("yyyy-MM-dd"));

		Date bound = codec.parse("01.02.2026");
		assertNotNull(bound);
		assertEquals("01.02.2026", codec.format(bound));
		assertEquals("2026-02-01", codec.toJson(bound));
		assertEquals(bound, codec.fromJson(codec.toJson(bound)));
		assertNull("Text that is not a date is not a bound.", codec.parse("kein Datum"));
		assertNull("The text of Date.toString() is not a stored bound, which is why ISO-8601 is stored.",
			codec.fromJson(bound.toString()));
	}

	/**
	 * A point in time is stored as precisely as its patterns write it.
	 */
	public void testTemporalBoundsKeepTheirPrecision() {
		BoundCodec<Date> codec = BoundCodec.dates(
			List.of(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS", Locale.GERMANY)),
			List.of("yyyy-MM-dd'T'HH:mm:ss.SSS"));

		Date moment = codec.parse("01.02.2026 14:30:15.123");
		assertNotNull(moment);
		assertEquals(moment, codec.fromJson(codec.toJson(moment)));
	}

	private static ComparableColumnFilter<Number> germanNumberFilter() {
		return new ComparableColumnFilter<>(Comparator.comparingDouble(Number::doubleValue),
			BoundCodec.numbers(NumberFormat.getInstance(Locale.GERMANY)));
	}

	public void testOptionsSerializationByIndex() {
		OptionsColumnFilter<String> filter = new OptionsColumnFilter<>(List.of(
			new Option("red", ResKey.text("Red")),
			new Option("blue", ResKey.text("Blue")),
			new Option("green", ResKey.text("Green"))));
		OptionsFilterState original = new OptionsFilterState(Set.of("red", "green"));
		FilterState restored = filter.fromJson(filter.toJson(original));
		assertEquals(Set.of("red", "green"), ((OptionsFilterState) restored).selected());
	}

	public void testBooleanSerialization() {
		BooleanFilterState original = new BooleanFilterState(true, false, true);
		assertEquals(original, BooleanColumnFilter.INSTANCE.fromJson(BooleanColumnFilter.INSTANCE.toJson(original)));
	}

	public void testInversionOptIn() {
		// Only filters for which inversion is meaningful offer it.
		assertTrue(TextColumnFilter.forStrings().supportsInversion());
		assertTrue(ComparableColumnFilter.integers().supportsInversion());
		assertTrue(new OptionsColumnFilter<>(List.of()).supportsInversion());
		assertFalse(BooleanColumnFilter.INSTANCE.supportsInversion());
	}

	public void testStateForText() {
		TextColumnFilter<String> filter = TextColumnFilter.forStrings();
		FilterState state = filter.stateFor("li");
		assertEquals(TextFilterState.contains("li"), state);
		assertTrue(filter.predicate(state).test("Charlie"));
		assertFalse(filter.predicate(state).test("Bob"));

		// A number reaches the filter as its text.
		assertEquals(TextFilterState.contains("30"), filter.stateFor(Integer.valueOf(30)));
	}

	public void testStateForTextOfBusinessObject() {
		// The column shows its values through a label, so the criterion has to match that label -
		// the value's own text names the object, not what the column displays of it.
		TextColumnFilter<Department> filter =
			new TextColumnFilter<>(department -> "Department " + department.key().toUpperCase());
		FilterState state = filter.stateFor(new Department("dev"));
		assertEquals(TextFilterState.contains("Department DEV"), state);
		assertTrue(filter.predicate(state).test(new Department("dev")));
		assertFalse(filter.predicate(state).test(new Department("ops")));
	}

	public void testStateForTextRejectsAlternatives() {
		TextColumnFilter<String> filter = TextColumnFilter.forStrings();
		assertNull("A text pattern matches one text, not a set of them.", filter.stateFor(List.of("a", "b")));
		assertNull(filter.stateFor(null));
	}

	public void testStateForOptionsSingleValue() {
		OptionsColumnFilter<String> filter = colorFilter();
		FilterState state = filter.stateFor("red");
		assertEquals(new OptionsFilterState(Set.of("red")), state);

		Predicate<String> p = filter.predicate(state);
		assertTrue(p.test("red"));
		assertFalse(p.test("blue"));
	}

	public void testStateForOptionsCollection() {
		OptionsColumnFilter<String> filter = colorFilter();
		FilterState state = filter.stateFor(List.of("red", "green"));
		assertEquals(new OptionsFilterState(Set.of("red", "green")), state);

		Predicate<String> p = filter.predicate(state);
		assertTrue(p.test("red"));
		assertTrue(p.test("green"));
		assertFalse(p.test("blue"));
	}

	public void testStateForOptionsBusinessObject() {
		Department dev = new Department("dev");
		Department ops = new Department("ops");
		OptionsColumnFilter<Department> filter = new OptionsColumnFilter<>(List.of(
			new Option(dev, ResKey.text("Development")),
			new Option(ops, ResKey.text("Operations"))));

		// The declared value is the business object itself - an equal one names the same option, so
		// no identifier handling is involved.
		FilterState state = filter.stateFor(new Department("dev"));
		assertEquals(new OptionsFilterState(Set.of(dev)), state);

		Predicate<Department> p = filter.predicate(state);
		assertTrue(p.test(dev));
		assertFalse(p.test(ops));
	}

	public void testStateForOptionsRejectsUnknownValue() {
		OptionsColumnFilter<String> filter = colorFilter();
		assertNull("A value that names no option is no selection.", filter.stateFor("black"));
		assertNull("Neither is it as one of several values.", filter.stateFor(List.of("red", "black")));
		assertNull(filter.stateFor(List.of()));
		assertNull(filter.stateFor(null));
	}

	private static OptionsColumnFilter<String> colorFilter() {
		return new OptionsColumnFilter<>(List.of(
			new Option("red", ResKey.text("Red")),
			new Option("green", ResKey.text("Green")),
			new Option("blue", ResKey.text("Blue"))));
	}

	public void testStateForBoolean() {
		FilterState onlyTrue = BooleanColumnFilter.INSTANCE.stateFor(Boolean.TRUE);
		assertEquals(new BooleanFilterState(true, false, false), onlyTrue);
		assertTrue(BooleanColumnFilter.INSTANCE.predicate(onlyTrue).test(Boolean.TRUE));
		assertFalse(BooleanColumnFilter.INSTANCE.predicate(onlyTrue).test(Boolean.FALSE));

		assertEquals(new BooleanFilterState(false, true, false), BooleanColumnFilter.INSTANCE.stateFor(Boolean.FALSE));
	}

	public void testStateForBooleanNoValue() {
		FilterState empty = BooleanColumnFilter.INSTANCE.stateFor(null);
		assertEquals(new BooleanFilterState(false, false, true), empty);
		assertTrue(BooleanColumnFilter.INSTANCE.predicate(empty).test(null));
		assertFalse(BooleanColumnFilter.INSTANCE.predicate(empty).test(Boolean.TRUE));

		BooleanColumnFilter twoValued =
			new BooleanColumnFilter(ResKey.text("Yes"), ResKey.text("No"), false);
		assertNull("A column whose cells always hold a value has no no-value option.", twoValued.stateFor(null));
	}

	public void testStateForBooleanRejectsOtherValues() {
		assertNull(BooleanColumnFilter.INSTANCE.stateFor("true"));
		assertNull(BooleanColumnFilter.INSTANCE.stateFor(List.of(Boolean.TRUE)));
	}

	public void testStateForComparableRange() {
		ComparableColumnFilter<Integer> filter = ComparableColumnFilter.integers();
		FilterState state = filter.stateFor(List.of(Integer.valueOf(5), Integer.valueOf(10)));
		assertEquals(RangeFilterState.between(Integer.valueOf(5), Integer.valueOf(10)), state);

		Predicate<Integer> p = filter.predicate(state);
		assertTrue(p.test(5));
		assertTrue(p.test(10));
		assertFalse(p.test(4));
		assertFalse(p.test(11));
	}

	public void testStateForComparableSingleValue() {
		ComparableColumnFilter<Integer> filter = ComparableColumnFilter.integers();
		FilterState state = filter.stateFor(Integer.valueOf(7));
		assertEquals(RangeFilterState.of(ComparisonOperator.EQ, Integer.valueOf(7)), state);

		Predicate<Integer> p = filter.predicate(state);
		assertTrue(p.test(7));
		assertFalse(p.test(8));

		assertEquals("A single bound is a single bound, given as a collection or not.", state,
			filter.stateFor(List.of(Integer.valueOf(7))));
	}

	public void testStateForComparableRejectsOtherValues() {
		ComparableColumnFilter<Integer> filter = ComparableColumnFilter.integers();
		assertNull("A range has two bounds, not three.",
			filter.stateFor(List.of(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
		assertNull(filter.stateFor(List.of()));
		assertNull(filter.stateFor(null));
		assertNull("A value the ordering cannot compare is no bound.", filter.stateFor(new Object()));
	}

	public void testStateForRegexpFacets() {
		RegexpOptionsFilter filter = regexpFilter();
		FilterState state = filter.stateFor("^Part");
		assertEquals(new OptionsFilterState(Set.of("^Part")), state);

		Predicate<String> p = filter.predicate(state);
		assertTrue(p.test("Part 4711"));
		assertFalse(p.test("Tool 4711"));

		// Several facets are selected at once, and a row matching any of them is displayed.
		Predicate<String> both = filter.predicate(filter.stateFor(List.of("^Part", "^Tool")));
		assertTrue(both.test("Part 4711"));
		assertTrue(both.test("Tool 4711"));
		assertFalse(both.test("Usage 4711"));
	}

	public void testStateForRegexpRejectsUndeclaredFacet() {
		assertNull("A facet is named by its declared regular expression.", regexpFilter().stateFor("^Usage"));
	}

	private static RegexpOptionsFilter regexpFilter() {
		return new RegexpOptionsFilter(List.of(
			new Option("^Part", ResKey.text("Parts")),
			new Option("^Tool", ResKey.text("Tools"))));
	}

	public void testStateForDefaultsToNotExpressible() {
		// A filter that does not implement the translation reports every value as not expressible, so
		// that a declared criterion for its column can be recognized as an error.
		ColumnFilter<String> filter = new ColumnFilter<>() {
			@Override
			public FilterInput input() {
				return new FilterInput.Text();
			}

			@Override
			public Predicate<String> predicate(FilterState state) {
				return value -> true;
			}
		};
		assertNull(filter.stateFor("anything"));
	}

}
