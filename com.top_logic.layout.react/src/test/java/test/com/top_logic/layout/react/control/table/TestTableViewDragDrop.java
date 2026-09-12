/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropPosition;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.table.Column;
import com.top_logic.table.TableView;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the drag-and-drop seam of {@link TableViewControl}: a {@code drop} command names the
 * dragged rows by the source control's client-side keys, and the objects reaching the
 * {@link DropTarget} are the ones that source resolves them to.
 *
 * <p>
 * Both directions of the two-stage acceptance are exercised: a drop of a type the target never
 * declared, and one naming rows the source does not display, are refused without the handler being
 * asked - the client-side check that precedes a drop narrows the gesture for the user, it does not
 * decide it.
 * </p>
 */
public class TestTableViewDragDrop extends TestCase {

	/** The type tag both tables agree on. */
	private static final String PERSON = "person";

	private static final String DROP = "drop";

	private record Person(String name) {
		// Test fixture.
	}

	/** A {@link DropTarget} that only remembers what it was announced. */
	private static final class Announced implements DropTarget {

		private final Collection<String> _acceptedTypes;

		private final boolean _dropOnRows;

		DropEvent _event;

		Announced(Collection<String> acceptedTypes, boolean dropOnRows) {
			_acceptedTypes = acceptedTypes;
			_dropOnRows = dropOnRows;
		}

		@Override
		public Collection<String> acceptedTypes() {
			return _acceptedTypes;
		}

		@Override
		public boolean dropOnRows() {
			return _dropOnRows;
		}

		@Override
		public void onDrop(DropEvent event) {
			_event = event;
		}
	}

	private static final List<Person> PEOPLE = List.of(new Person("alice"), new Person("bob"), new Person("carol"));

	private ReactContext _context;

	private TableViewControl<Person> _source;

	private TableViewControl<Person> _target;

	private Announced _announced;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// One context, hence one control registry: the drop resolves its source control out of it.
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		_source = newTable();
		_source.setDragSource(PERSON);
		_target = newTable();
		_announced = new Announced(List.of(PERSON), true);
		_target.setDropTarget(_announced);
	}

	private TableViewControl<Person> newTable() {
		List<Column<Person, ?>> columns = List.of(DefaultColumn.<Person, String> builder("name", Person::name).build());
		TableView<Person> view = DefaultTableView.create(columns, new ListRowSource<>(PEOPLE, columns));
		return new TableViewControl<>(_context, view, false);
	}

	/** The client-side key of the row at the given index, as the table puts it into its row state. */
	private static String rowKey(int rowIndex) {
		return "row_" + rowIndex;
	}

	private HandlerResult drop(TableViewControl<Person> target, Map<String, Object> arguments) {
		return target.executeClientCommand(DROP, arguments);
	}

	/** Asserts that the drop was applied, naming why it was not if it was refused. */
	private static void assertApplied(String message, HandlerResult result) {
		StringBuilder cause = new StringBuilder();
		for (Throwable ex = result.getException(); ex != null; ex = ex.getCause()) {
			cause.append(" <- ").append(ex);
		}
		assertTrue(message + " Refused with: " + result.getEncodedErrors() + cause, result.isSuccess());
	}

	/**
	 * The dragged rows and the row dropped on reach the handler as their business objects, resolved
	 * by the control each of them belongs to.
	 */
	public void testDropNamesTheDraggedObjectsAndTheTargetRow() {
		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, rowKey(0),
			DropArguments.POSITION, DropPosition.ONTO.wireName()));

		assertApplied("A drop of an accepted type on a displayed row must be applied.", result);
		DropEvent event = _announced._event;
		assertNotNull("The drop target must be asked to apply the drop.", event);
		assertEquals(List.of(PEOPLE.get(1)), event.objects());
		assertEquals(PEOPLE.get(0), event.target());
		assertEquals(DropPosition.ONTO, event.position());
		assertSame("The event names the control the objects were dragged out of.", _source, event.source());
	}

	/**
	 * A drop on the table itself names no row: the position is {@link DropPosition#NONE}, whatever
	 * the client sent.
	 */
	public void testDropOnTheTableNamesNoRow() {
		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(2),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.POSITION, DropPosition.NONE.wireName()));

		assertApplied("A drop on the table itself must be applied.", result);
		DropEvent event = _announced._event;
		assertNotNull(event);
		assertEquals(List.of(PEOPLE.get(2)), event.objects());
		assertNull(event.target());
		assertEquals(DropPosition.NONE, event.position());
	}

	/**
	 * Dragging a selected row drags the source's whole selection - which the source reads from its
	 * own selection state, not from the keys the client sent (a virtualized table cannot enumerate a
	 * selection reaching beyond its rendered rows).
	 */
	public void testDropOfASelectionTakesTheSourceSelection() {
		_source.selectRow(PEOPLE.get(0));

		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(2),
			DropArguments.SELECTION, Boolean.TRUE,
			DropArguments.TARGET_KEY, rowKey(1),
			DropArguments.POSITION, DropPosition.AFTER.wireName()));

		assertApplied("A drop of the source's selection must be applied.", result);
		DropEvent event = _announced._event;
		assertNotNull(event);
		assertEquals("The selection is dragged, not the row the keys name.",
			List.of(PEOPLE.get(0)), event.objects());
		assertEquals(PEOPLE.get(1), event.target());
		assertEquals(DropPosition.AFTER, event.position());
	}

	/**
	 * A target that is no row target receives every drop on the table as a whole, even one the client
	 * announced on a row.
	 */
	public void testARowTargetIsIgnoredWhenRowsAreNoTargets() {
		Announced onTableOnly = new Announced(List.of(PERSON), false);
		_target.setDropTarget(onTableOnly);

		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, rowKey(0),
			DropArguments.POSITION, DropPosition.ONTO.wireName()));

		assertApplied("A drop on a table that is no row target must be applied.", result);
		assertNotNull(onTableOnly._event);
		assertNull("A target that is no row target never gets a row.", onTableOnly._event.target());
		assertEquals(DropPosition.NONE, onTableOnly._event.position());
	}

	/** A drop whose source drags a type the target never declared is refused. */
	public void testDropOfAnUnacceptedTypeIsRefused() {
		_source.setDragSource("milestone");

		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, rowKey(0),
			DropArguments.POSITION, DropPosition.ONTO.wireName()));

		assertFalse("A drop of an unaccepted type must be refused.", result.isSuccess());
		assertNull("The drop target must not be asked to apply it.", _announced._event);
	}

	/** So is a drop naming rows the source control does not display. */
	public void testDropOfUnknownKeysIsRefused() {
		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(99),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, rowKey(0),
			DropArguments.POSITION, DropPosition.ONTO.wireName()));

		assertFalse("A drop of rows that resolve to nothing must be refused.", result.isSuccess());
		assertNull("The drop target must not be asked to apply it.", _announced._event);
	}

	/** So is a drop on a row the receiving table does not display. */
	public void testDropOnAnUnknownRowIsRefused() {
		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, rowKey(99),
			DropArguments.POSITION, DropPosition.ONTO.wireName()));

		assertFalse("A drop on a row that resolves to nothing must be refused.", result.isSuccess());
		assertNull("The drop target must not be asked to apply it.", _announced._event);
	}

	/** A table that declares no drop target refuses a drop outright. */
	public void testATableWithoutADropTargetRefusesADrop() {
		TableViewControl<Person> plain = newTable();

		HandlerResult result = drop(plain, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.POSITION, DropPosition.NONE.wireName()));

		assertFalse("A table accepting no drop must refuse one.", result.isSuccess());
	}

	/** A drop whose source control is not a drag source at all is refused. */
	public void testDropFromANonDraggingSourceIsRefused() {
		_source.setDragSource(null);

		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.POSITION, DropPosition.NONE.wireName()));

		assertFalse("A drop from a table whose rows are not draggable must be refused.", result.isSuccess());
		assertNull("The drop target must not be asked to apply it.", _announced._event);
	}

	/**
	 * The test suite, started with the resource bundles the table's column labels and a refused
	 * drop's message need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableViewDragDrop.class, ResourcesModule.Module.INSTANCE));
	}

}
