/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.table.DeclaredFilters;
import com.top_logic.layout.view.table.DeclaredFilters.Criterion;
import com.top_logic.layout.view.table.DeclaredFilters.Declaration;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.Option;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.ComparisonOperator;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;

/**
 * Test for {@link DeclaredFilters}: how the criteria a table declares become the
 * {@link NamedFilter}s it offers, and how a criterion that cannot be applied is reported.
 */
public class TestDeclaredFilters extends TestCase {

	private static final String NAME = "name";

	private static final String ACTIVE = "active";

	private static final String COMMENT = "comment";

	private static final String PRIORITY = "priority";

	private static final String AMOUNT = "amount";

	private static final String SCRIPTED = "scripted";

	private static final String LOW = "low";

	private static final String HIGH = "high";

	public void testResolveCriteria() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("mine", ResKey.text("My rows"), List.of(
				Criterion.value(NAME, "Alice"),
				Criterion.value(ACTIVE, Boolean.TRUE)))),
			columns());

		assertFalse(log.getErrors().toString(), log.hasErrors());
		assertEquals(1, filters.size());

		NamedFilter filter = filters.get(0);
		assertEquals("mine", filter.id());
		assertEquals(ResKey.text("My rows"), filter.label());
		assertEquals(NamedFilter.Origin.DECLARED, filter.origin());
		assertNull("A declaration without a search term searches for nothing.", filter.search());
		assertEquals(TextFilterState.contains("Alice"), filter.filters().get(NAME));
		assertEquals(new BooleanFilterState(true, false, false), filter.filters().get(ACTIVE));
	}

	public void testUnknownColumnReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("broken", ResKey.text("Broken"), List.of(
				Criterion.value("nonexistent", "Alice")))),
			columns());

		assertTrue("The unknown column must be reported.", log.hasErrors());
		assertContains("nonexistent", log.getErrors());
		assertContains("broken", log.getErrors());
		assertEquals("A filter with an inapplicable criterion is not offered.", 0, filters.size());
	}

	public void testUnfilterableColumnReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("broken", ResKey.text("Broken"), List.of(
				Criterion.value(COMMENT, "Alice")))),
			columns());

		assertTrue("Filtering a column that cannot be filtered must be reported.", log.hasErrors());
		assertContains(COMMENT, log.getErrors());
		assertEquals(0, filters.size());
	}

	public void testInexpressibleValueReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("broken", ResKey.text("Broken"), List.of(
				// A boolean column holds nothing but the two values, so a text names none of them.
				Criterion.value(ACTIVE, "yes")))),
			columns());

		assertTrue("A value the column's filter cannot express must be reported.", log.hasErrors());
		assertContains(ACTIVE, log.getErrors());
		assertContains("yes", log.getErrors());
		assertEquals(0, filters.size());
	}

	public void testAllOrNothing() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("broken", ResKey.text("Broken"), List.of(
				Criterion.value(NAME, "Alice"),
				Criterion.value("nonexistent", "Bob"))),
			new Declaration("fine", ResKey.text("Fine"), List.of(
				Criterion.value(NAME, "Alice")))),
			columns());

		assertTrue(log.hasErrors());
		assertEquals("The intact declaration is still offered.", 1, filters.size());
		assertEquals("fine", filters.get(0).id());
	}

	public void testDuplicateIdReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("mine", ResKey.text("My rows"), List.of(Criterion.value(NAME, "Alice"))),
			new Declaration("mine", ResKey.text("My other rows"), List.of(Criterion.value(NAME, "Bob")))),
			columns());

		assertTrue("Two filters cannot share an identifier.", log.hasErrors());
		assertEquals(1, filters.size());
		assertEquals(TextFilterState.contains("Alice"), filters.get(0).filters().get(NAME));
	}

	/**
	 * Tests a criterion declared as the text pattern of the column's own filter, with the matching
	 * options a single value cannot say.
	 */
	public void testTextState() {
		TextFilterState pattern = new TextFilterState("^Ali", true, true, false);
		NamedFilter filter = resolveOne(Criterion.state(NAME, pattern));

		assertEquals(pattern, filter.filters().get(NAME));
	}

	/**
	 * Tests a criterion declared as a one-sided comparison, which a value cannot say.
	 */
	public void testRangeState() {
		NamedFilter filter =
			resolveOne(Criterion.state(AMOUNT, RangeFilterState.of(ComparisonOperator.GT, Double.valueOf(50))));

		assertEquals(RangeFilterState.of(ComparisonOperator.GT, Double.valueOf(50)),
			filter.filters().get(AMOUNT));
	}

	/**
	 * Tests that the bounds of a declared comparison are brought into the value type the column
	 * works in, so that the criterion equals the same one entered by the user or restored from the
	 * user's personalization.
	 */
	public void testRangeBoundsNormalized() {
		NamedFilter filter = resolveOne(Criterion.state(AMOUNT,
			RangeFilterState.between(Long.valueOf(0), Long.valueOf(50))));

		assertEquals("The whole numbers of the declaration are the fractional bounds of the column.",
			RangeFilterState.between(Double.valueOf(0), Double.valueOf(50)), filter.filters().get(AMOUNT));
	}

	/**
	 * Tests a criterion declared as a selection among the options of the column.
	 */
	public void testOptionsState() {
		NamedFilter filter = resolveOne(Criterion.state(PRIORITY, new OptionsFilterState(Set.of(HIGH, LOW))));

		assertEquals(new OptionsFilterState(Set.of(LOW, HIGH)), filter.filters().get(PRIORITY));
	}

	/**
	 * Tests a criterion declared as several accepted truth values at once, which a single value
	 * cannot say.
	 */
	public void testBooleanState() {
		BooleanFilterState accepted = new BooleanFilterState(false, true, true);
		NamedFilter filter = resolveOne(Criterion.state(ACTIVE, accepted));

		assertEquals(accepted, filter.filters().get(ACTIVE));
	}

	/**
	 * Tests that an inverted criterion accepts the rows it does not select.
	 */
	public void testInverted() {
		TextFilterState pattern = TextFilterState.contains("Alice");
		NamedFilter filter = resolveOne(new Criterion.State(NAME, pattern, true));

		assertEquals(new NegatedFilterState(pattern), filter.filters().get(NAME));
	}

	/**
	 * Tests that a value criterion is inverted, too.
	 */
	public void testInvertedValue() {
		NamedFilter filter = resolveOne(new Criterion.Value(NAME, "Alice", true));

		assertEquals(new NegatedFilterState(TextFilterState.contains("Alice")), filter.filters().get(NAME));
	}

	/**
	 * Tests that inverting a filter that offers no inversion is reported.
	 */
	public void testInversionUnsupportedReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = resolve(log,
			new Criterion.State(ACTIVE, new BooleanFilterState(true, false, false), true));

		assertTrue("A filter that cannot be inverted must be reported.", log.hasErrors());
		assertContains(ACTIVE, log.getErrors());
		assertEquals(0, filters.size());
	}

	/**
	 * Tests that a criterion of another kind than the column's filter is reported.
	 */
	public void testKindMismatchReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = resolve(log, Criterion.state(ACTIVE, TextFilterState.contains("yes")));

		assertTrue("A criterion of another kind must be reported.", log.hasErrors());
		assertContains(ACTIVE, log.getErrors());
		assertContains("text", log.getErrors());
		assertEquals(0, filters.size());
	}

	/**
	 * Tests that no criterion can be declared for a filter bringing its own form.
	 */
	public void testFormFilterReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = resolve(log, Criterion.state(SCRIPTED, TextFilterState.contains("Alice")));

		assertTrue("A filter with a form of its own must be reported.", log.hasErrors());
		assertContains(SCRIPTED, log.getErrors());
		assertEquals(0, filters.size());
	}

	/**
	 * Tests that a selection of what the column does not offer is reported, rather than filtering by
	 * less than the declaration says.
	 */
	public void testUnknownOptionReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = resolve(log, Criterion.state(PRIORITY, new OptionsFilterState(Set.of("none"))));

		assertTrue("A selection the column does not offer must be reported.", log.hasErrors());
		assertContains(PRIORITY, log.getErrors());
		assertEquals(0, filters.size());
	}

	/**
	 * Tests that a criterion selecting nothing leaves its column unfiltered - the case of an input
	 * nothing is selected in - while the rest of the declaration is offered.
	 */
	public void testEmptyCriterionSelectsNothing() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("mine", ResKey.text("My rows"), List.of(
				Criterion.state(NAME, new TextFilterState("", false, false, false)),
				Criterion.state(PRIORITY, new OptionsFilterState(Set.of(HIGH)))))),
			columns());

		assertFalse(log.getErrors().toString(), log.hasErrors());
		assertEquals(1, filters.size());
		assertEquals("A pattern matching nothing is no criterion.", Set.of(PRIORITY),
			filters.get(0).filters().keySet());
	}

	/**
	 * Tests that a declaration all of whose criteria select nothing is withheld: nothing is left of
	 * what it says, and it is offered again as soon as its inputs select something.
	 */
	public void testDeclarationOfEmptyCriteriaWithheld() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = resolve(log,
			Criterion.state(PRIORITY, new OptionsFilterState(Set.of())));

		assertFalse(log.getErrors().toString(), log.hasErrors());
		assertEquals("Nothing is left of what the declaration says.", 0, filters.size());
	}

	/**
	 * Tests that a declaration naming no criterion at all is offered: it says "no filter", and is
	 * the active one exactly while the table is unfiltered.
	 */
	public void testDeclarationWithoutCriteriaIsOffered() {
		BufferingProtocol log = new BufferingProtocol();
		List<Column<Object, ?>> columns = columns();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("all", ResKey.text("All rows"), List.of())), columns);

		assertFalse(log.getErrors().toString(), log.hasErrors());
		assertEquals(1, filters.size());
		NamedFilter all = filters.get(0);
		assertTrue("A filter by nothing.", all.filters().isEmpty());
		assertTrue("It is the criteria of an unfiltered table.", all.matches(Map.of(), null));
	}

	/**
	 * Tests that a selection holding a value the column offers no option for is reported, rather
	 * than filtering by the part of it the column knows.
	 */
	public void testPartiallyUnknownOptionReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters =
			resolve(log, Criterion.state(PRIORITY, new OptionsFilterState(Set.of(LOW, "none"))));

		assertTrue("A value the column offers no option for must be reported.", log.hasErrors());
		assertContains(PRIORITY, log.getErrors());
		assertContains("none", log.getErrors());
		assertEquals(0, filters.size());
	}

	/**
	 * The single filter of a declaration holding the given criterion, requiring that it is offered.
	 */
	private NamedFilter resolveOne(Criterion criterion) {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = resolve(log, criterion);

		assertFalse(log.getErrors().toString(), log.hasErrors());
		assertEquals(1, filters.size());
		return filters.get(0);
	}

	/**
	 * The filters of a declaration holding the given criterion.
	 */
	private List<NamedFilter> resolve(BufferingProtocol log, Criterion criterion) {
		return DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("declared", ResKey.text("Declared"), List.of(criterion))),
			columns());
	}

	/**
	 * One column per kind of filter, plus a column that cannot be filtered.
	 */
	private static List<Column<Object, ?>> columns() {
		Column<Object, String> name = DefaultColumn.<Object, String> builder(NAME, row -> String.valueOf(row))
			.label(ResKey.text("Name"))
			.filter(TextColumnFilter.forStrings())
			.build();
		Column<Object, Boolean> active = DefaultColumn.<Object, Boolean> builder(ACTIVE, row -> Boolean.TRUE)
			.label(ResKey.text("Active"))
			.filter(BooleanColumnFilter.INSTANCE)
			.build();
		Column<Object, String> comment = DefaultColumn.<Object, String> builder(COMMENT, row -> String.valueOf(row))
			.label(ResKey.text("Comment"))
			.build();
		Column<Object, String> priority = DefaultColumn.<Object, String> builder(PRIORITY, row -> LOW)
			.label(ResKey.text("Priority"))
			.filter(new OptionsColumnFilter<>(List.of(
				new Option(LOW, ResKey.text("Low")),
				new Option(HIGH, ResKey.text("High")))))
			.build();
		Column<Object, Number> amount = DefaultColumn.<Object, Number> builder(AMOUNT, row -> Double.valueOf(1))
			.label(ResKey.text("Amount"))
			.filter(new ComparableColumnFilter<>(Comparator.comparingDouble(Number::doubleValue),
				BoundCodec.numbers(fractionalFormat())))
			.build();
		Column<Object, String> scripted = DefaultColumn.<Object, String> builder(SCRIPTED, row -> LOW)
			.label(ResKey.text("Scripted"))
			.filter(new FormFilter())
			.build();
		return List.of(name, active, comment, priority, amount, scripted);
	}

	/**
	 * The format of a column of fractional numbers, in which a whole number is a fraction, too.
	 */
	private static NumberFormat fractionalFormat() {
		return new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	}

	private static void assertContains(String expected, List<String> errors) {
		for (String error : errors) {
			if (error.contains(expected)) {
				return;
			}
		}
		fail("Expected '" + expected + "' to be named in one of: " + errors);
	}

	/**
	 * A filter whose editor is a form of its own, so that no criterion can be declared for it.
	 */
	private static final class FormFilter implements ColumnFilter<String> {

		@Override
		public FilterInput input() {
			return new FilterInput.Form();
		}

		@Override
		public Predicate<String> predicate(FilterState state) {
			return value -> true;
		}

	}

}
