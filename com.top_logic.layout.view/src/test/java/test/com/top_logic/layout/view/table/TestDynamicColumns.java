/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.layout.view.table.DynamicColumnSet;
import com.top_logic.layout.view.table.DynamicColumns;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for the {@code <dynamic-columns>} of a table: one column per object of a computed set, and
 * what each of those columns shows, is named and labelled after, and lets the user edit.
 */
public class TestDynamicColumns extends TestCase {

	/** The module holding the test model. */
	private static final String MODULE = "test.dynamicColumns";

	/** The type the computed columns hold values of. */
	private TLClass _valueType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, MODULE);
		_valueType = TLModelUtil.addClass(module, "Value");
	}

	/**
	 * Every object of the set stands for one column, in that order, named after the declaration and
	 * the object, and labelled as the set says.
	 */
	public void testOneColumnPerObject() {
		List<ColumnSetup> columns = resolve(declaration(), set("first", "second"));

		assertEquals(List.of("milestones.first", "milestones.second"), names(columns));
		assertEquals("The column is labelled after the object standing for it.",
			ResKey.text("Label of first"), columns.get(0).label());
		assertEquals("The columns share what the declaration says about all of them.",
			_valueType, columns.get(0).type().type());
		assertFalse("The columns are displayed from the start.", columns.get(0).hiddenByDefault());
	}

	/**
	 * A column shows what the value function yields for its row and the object it stands for.
	 */
	public void testValueIsBoundToTheColumnObject() {
		List<ColumnSetup> columns = resolve(declaration(), set("first", "second"));

		assertEquals("row/first", columns.get(0).value().apply("row"));
		assertEquals("Every column reads its own object.", "row/second",
			columns.get(1).value().apply("row"));
	}

	/**
	 * An object standing for nothing contributes no column.
	 */
	public void testNothingStandsForNoColumn() {
		assertEquals("A set holding nothing contributes no columns.",
			List.of(), names(resolve(declaration(), set())));
		assertEquals(List.of("milestones.first"),
			names(resolve(declaration(), set("first", null))));
	}

	/**
	 * The columns exist only once the set is at hand, so a declaration contributes no name and no
	 * sort order before the table is built.
	 */
	public void testColumnsAreKnownOnlyOnceTheSetIsAtHand() {
		DynamicColumns declaration = declaration();

		assertEquals(List.of(), declaration.declaredNames());
		assertEquals(List.of(), declaration.defaultSort());
	}

	/**
	 * A column is edited exactly where the set writes an edited value back, on the rows it accepts
	 * for the object the column stands for.
	 */
	public void testEditingFollowsTheUpdate() {
		Columns edited = set("first", "second");
		edited.setUpdate(column -> (row, value) -> {
			// What an edit writes is what the set does with it; the column only offers it.
		});
		edited.setCanUpdate(column -> row -> row.equals(column));

		List<ColumnSetup> columns = resolve(declaration(), edited);

		assertNotNull("The column writes an edited value back.", columns.get(0).editing());
		assertTrue("The predicate is applied to the row and the object of its own column.",
			columns.get(0).editing().canEdit("first"));
		assertFalse(columns.get(0).editing().canEdit("second"));
		assertTrue(columns.get(1).editing().canEdit("second"));

		assertNull("A column writing nothing back is displayed, not edited.",
			resolve(declaration(), set("first")).get(0).editing());
	}

	/**
	 * A column whose values are of an unknown type is not edited: which control enters a value
	 * follows from that type.
	 */
	public void testUntypedColumnsAreNotEdited() {
		Columns untyped = set("first");
		untyped.setType(column -> ColumnType.UNRESOLVED);
		untyped.setUpdate(column -> (row, value) -> fail("The column is not edited."));

		assertNull(resolve(declaration(), untyped).get(0).editing());
	}

	/**
	 * Columns declared read-only stay read-only, whatever the set would let them write back.
	 */
	public void testReadonlyColumnsAreNotEdited() {
		Columns edited = set("first");
		edited.setUpdate(column -> (row, value) -> fail("The column is read-only."));

		assertNull(resolve(new DynamicColumns("milestones", 0, true, scope -> edited), edited).get(0).editing());
	}

	/**
	 * What a column shows for a group of rows is computed from the group's rows and the object the
	 * column stands for.
	 */
	public void testAggregateIsBoundToTheColumnObject() {
		Columns aggregated = set("first", "second");
		aggregated.setAggregate(column -> rows -> rows.size() + " of " + column);

		List<ColumnSetup> columns = resolve(declaration(), aggregated);

		assertEquals("2 of first", columns.get(0).aggregate().apply(List.of("a", "b")));
		assertEquals("2 of second", columns.get(1).aggregate().apply(List.of("a", "b")));
		assertNull("A column aggregating nothing leaves its group cell empty.",
			resolve(declaration(), set("first")).get(0).aggregate());
	}

	/**
	 * The display width the declaration states is the width of every column it contributes.
	 */
	public void testWidthIsSharedByEveryColumn() {
		for (ColumnSetup setup : resolve(declaration(), set("first", "second"))) {
			assertEquals(120, setup.width());
		}

		assertEquals("Columns keep the width their type derives unless the declaration states one.",
			0, resolve(new DynamicColumns("milestones", 0, false, scope -> set("first")), set("first"))
				.get(0).width());
	}

	/** The columns the given declaration contributes for the given set, in display order. */
	private static List<ColumnSetup> resolve(DynamicColumns declaration, DynamicColumnSet set) {
		return declaration.columns(set, new ColumnResolution(null, null));
	}

	/** The names of the given columns, in display order. */
	private static List<String> names(List<ColumnSetup> columns) {
		return columns.stream().map(ColumnSetup::name).toList();
	}

	/** A declaration naming its columns after {@code milestones} and displaying them 120px wide. */
	private static DynamicColumns declaration() {
		return new DynamicColumns("milestones", 120, false, scope -> {
			throw new AssertionError("The columns are the ones the test hands over.");
		});
	}

	/** A set standing for one column per given object. */
	private Columns set(Object... columns) {
		return new Columns(Arrays.asList(columns), _valueType);
	}

	/**
	 * A {@link DynamicColumnSet} standing for one column per given object, showing what the object
	 * says about its column.
	 */
	private static class Columns implements DynamicColumnSet {

		private final List<?> _columns;

		private Function<Object, ColumnType> _type;

		private Function<Object, BiConsumer<Object, Object>> _update = column -> null;

		private Function<Object, Predicate<Object>> _canUpdate = column -> null;

		private Function<Object, Function<List<Object>, Object>> _aggregate = column -> null;

		/**
		 * Creates a {@link Columns} standing for the given objects, all of them holding values of
		 * the given type.
		 */
		Columns(List<?> columns, TLClass valueType) {
			_columns = columns;
			_type = column -> ColumnType.of(valueType, false);
		}

		/** @see #type(Object) */
		void setType(Function<Object, ColumnType> type) {
			_type = type;
		}

		/** @see #update(Object) */
		void setUpdate(Function<Object, BiConsumer<Object, Object>> update) {
			_update = update;
		}

		/** @see #canUpdate(Object) */
		void setCanUpdate(Function<Object, Predicate<Object>> canUpdate) {
			_canUpdate = canUpdate;
		}

		/** @see #aggregate(Object) */
		void setAggregate(Function<Object, Function<List<Object>, Object>> aggregate) {
			_aggregate = aggregate;
		}

		@Override
		public List<?> columns() {
			return _columns;
		}

		@Override
		public String name(Object column) {
			return String.valueOf(column);
		}

		@Override
		public ResKey label(Object column) {
			return ResKey.text("Label of " + column);
		}

		@Override
		public ColumnType type(Object column) {
			return _type.apply(column);
		}

		@Override
		public Function<Object, Object> value(Object column) {
			return row -> row + "/" + column;
		}

		@Override
		public BiConsumer<Object, Object> update(Object column) {
			return _update.apply(column);
		}

		@Override
		public Predicate<Object> canUpdate(Object column) {
			return _canUpdate.apply(column);
		}

		@Override
		public Function<List<Object>, Object> aggregate(Object column) {
			return _aggregate.apply(column);
		}

	}

	/**
	 * Test suite requiring the {@link AttributeSettings} the test model is built with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDynamicColumns.class, AttributeSettings.Module.INSTANCE));
	}

}
