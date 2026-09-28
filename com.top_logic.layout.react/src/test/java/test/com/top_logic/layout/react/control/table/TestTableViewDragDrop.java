/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropPosition;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.dnd.DropVerdict;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
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
 *
 * <p>
 * The {@link DropTarget#check(DropEvent) verdict} of the target is asked both by a drop, which it can
 * refuse, and by the {@code dropProbe} command a client sends while a drag hovers, which answers it
 * in the client state without applying anything.
 * </p>
 */
public class TestTableViewDragDrop extends TestCase {

	/** The type tag both tables agree on. */
	private static final String PERSON = "person";

	private static final String DROP = "drop";

	private static final String DROP_PROBE = "dropProbe";

	/** State key of the verdicts answered to drop probes. */
	private static final String DROP_VERDICTS = "dropVerdicts";

	/** Entry of a verdict telling whether the drop is accepted. */
	private static final String VERDICT_ACCEPTED = "accepted";

	/** Entry of a refusing verdict holding the reason. */
	private static final String VERDICT_REASON = "reason";

	/** State key of the accepted type tags. */
	private static final String DROP_ACCEPTS = "dropAccepts";

	/** State key of the client's row list. */
	private static final String ROWS = "rows";

	/** Row entry telling whether the row may be dragged. */
	private static final String ROW_DRAGGABLE = "draggable";

	/** The reason the tests' drop target refuses a drop with. */
	private static final String REFUSAL_TEXT = "Not onto alice.";

	private static final ResKey REFUSAL = ResKey.text(REFUSAL_TEXT);

	private record Person(String name) {
		// Test fixture.
	}

	/** A {@link TableViewControl} whose client state the test reads. */
	private static final class Table extends TableViewControl<Person> {

		Table(ReactContext context, TableView<Person> view) {
			super(context, view, false);
		}

		Object clientState(String key) {
			return getState(key);
		}

	}

	/**
	 * A {@link DropTarget} that remembers what it was announced and asked, and gives the verdict its
	 * {@link #_check} says.
	 */
	private static final class Announced implements DropTarget {

		private Collection<String> _acceptedTypes;

		private final boolean _dropOnRows;

		DropEvent _event;

		DropEvent _checked;

		Function<DropEvent, DropVerdict> _check = event -> DropVerdict.ACCEPTED;

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
		public DropVerdict check(DropEvent event) {
			_checked = event;
			return _check.apply(event);
		}

		@Override
		public void onDrop(DropEvent event) {
			_event = event;
		}
	}

	private static final List<Person> PEOPLE = List.of(new Person("alice"), new Person("bob"), new Person("carol"));

	private ReactContext _context;

	private Table _source;

	private Table _target;

	private Announced _announced;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// One context, hence one control registry: the drop resolves its source control out of it.
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_source = newTable();
		_source.setDragSource(PERSON);
		_target = newTable();
		_announced = new Announced(List.of(PERSON), true);
		_target.setDropTarget(_announced);

		// Both tables are displayed: only a displayed control can be addressed by its ID.
		_source.attach();
		_target.attach();
	}

	private Table newTable() {
		List<Column<Person, ?>> columns = List.of(DefaultColumn.<Person, String> builder("name", Person::name).build());
		TableView<Person> view = DefaultTableView.create(columns, new ListRowSource<>(PEOPLE, columns));
		return new Table(_context, view);
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
	 * A drop the target's check refuses is answered with the check's reason, and the target is not
	 * asked to apply it.
	 */
	public void testRefusedVerdictBlocksTheDrop() {
		_announced._check = event -> PEOPLE.get(0).equals(event.target()) ? DropVerdict.refused(REFUSAL)
			: DropVerdict.ACCEPTED;

		HandlerResult result = drop(_target, dropOn(rowKey(1), rowKey(0)));

		assertFalse("A drop the check refuses must be refused.", result.isSuccess());
		assertEquals("The refusal names the check's reason.", List.of(REFUSAL), result.getEncodedErrors());
		assertNotNull("The check was asked.", _announced._checked);
		assertEquals(List.of(PEOPLE.get(1)), _announced._checked.objects());
		assertNull("The drop target must not be asked to apply it.", _announced._event);

		assertApplied("A drop the check accepts is applied.", drop(_target, dropOn(rowKey(1), rowKey(2))));
		assertEquals(PEOPLE.get(2), _announced._event.target());
	}

	/**
	 * A probe answers the verdict under its identifier, the reason of a refusal in the user's
	 * language, and applies nothing.
	 */
	public void testProbeAnswersTheVerdictWithoutApplyingTheDrop() {
		_announced._check = event -> PEOPLE.get(0).equals(event.target()) ? DropVerdict.refused(REFUSAL)
			: DropVerdict.ACCEPTED;

		assertApplied("A probe never fails.", probe("drag1", "p1", rowKey(1), rowKey(0)));
		assertApplied("A probe never fails.", probe("drag1", "p2", rowKey(1), rowKey(2)));

		Map<String, Map<String, Object>> verdicts = verdicts(_target);
		assertEquals(Boolean.FALSE, verdicts.get("p1").get(VERDICT_ACCEPTED));
		assertEquals(REFUSAL_TEXT, verdicts.get("p1").get(VERDICT_REASON));
		assertEquals(Boolean.TRUE, verdicts.get("p2").get(VERDICT_ACCEPTED));
		assertFalse(verdicts.get("p2").containsKey(VERDICT_REASON));
		assertNull("A probe must not apply the drop.", _announced._event);
	}

	/** A drop the resolution refuses already is answered as refused by a probe, too. */
	public void testProbeOfAnUnacceptedTypeIsRefused() {
		_source.setDragSource("milestone");

		assertApplied("A probe never fails.", probe("drag1", "p1", rowKey(1), rowKey(0)));

		Map<String, Object> verdict = verdicts(_target).get("p1");
		assertEquals(Boolean.FALSE, verdict.get(VERDICT_ACCEPTED));
		assertNotNull("A refusal names its reason.", verdict.get(VERDICT_REASON));
		assertNull("The check is not asked for a drop the resolution refuses.", _announced._checked);
	}

	/** The verdicts of one drag accumulate; a probe of the next drag discards them. */
	public void testProbeOfTheNextDragDiscardsTheVerdicts() {
		probe("drag1", "p1", rowKey(1), rowKey(0));
		probe("drag1", "p2", rowKey(1), rowKey(2));
		assertEquals(2, verdicts(_target).size());

		probe("drag2", "p3", rowKey(1), rowKey(2));
		assertEquals("Only the verdicts of the running drag are kept.", List.of("p3"),
			List.copyOf(verdicts(_target).keySet()));
	}

	/** The probe is technical: it is never recorded. */
	public void testProbeIsNotRecorded() {
		assertFalse(_target.isRecordable(DROP_PROBE));
		assertTrue(_target.isRecordable(DROP));
	}

	/**
	 * A row the drag source's predicate refuses is announced as not draggable, and a drag including
	 * it is refused as a whole.
	 */
	public void testARefusedRowIsNotDraggable() {
		_source.setDragSource(PERSON, person -> !person.name().equals("bob"));

		assertEquals(Boolean.TRUE, rowState(_source, 0).get(ROW_DRAGGABLE));
		assertEquals(Boolean.FALSE, rowState(_source, 1).get(ROW_DRAGGABLE));

		HandlerResult result = drop(_target, dropOn(rowKey(1), rowKey(0)));
		assertFalse("A drag of a refused row must be refused.", result.isSuccess());
		assertNull("The drop target must not be asked to apply it.", _announced._event);

		_source.selectRow(PEOPLE.get(1));
		Map<String, Object> ofSelection = new HashMap<>(dropOn(rowKey(0), rowKey(2)));
		ofSelection.put(DropArguments.SELECTION, Boolean.TRUE);
		assertFalse("A selection including a refused row is refused as a whole.",
			drop(_target, ofSelection).isSuccess());
		assertNull(_announced._event);

		assertApplied("A draggable row is still dropped.", drop(_target, dropOn(rowKey(2), rowKey(0))));
	}

	/** Refreshing the drag source asks the predicate again. */
	public void testRefreshDragSourceReevaluatesTheRows() {
		boolean[] bobDraggable = { false };
		_source.setDragSource(PERSON, person -> bobDraggable[0] || !person.name().equals("bob"));
		assertEquals(Boolean.FALSE, rowState(_source, 1).get(ROW_DRAGGABLE));

		bobDraggable[0] = true;
		_source.refreshDragSource();
		assertEquals(Boolean.TRUE, rowState(_source, 1).get(ROW_DRAGGABLE));
	}

	/** Refreshing the drop target announces its changed accepted types. */
	public void testRefreshDropTargetAnnouncesTheAcceptedTypes() {
		assertEquals(List.of(PERSON), _target.clientState(DROP_ACCEPTS));

		_announced._acceptedTypes = List.of(PERSON, "milestone");
		_target.refreshDropTarget();

		assertEquals(List.of(PERSON, "milestone"), _target.clientState(DROP_ACCEPTS));
	}

	/** The arguments of a drop of the given source row on the given target row. */
	private Map<String, Object> dropOn(String key, String targetKey) {
		return Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, key,
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, targetKey,
			DropArguments.POSITION, DropPosition.ONTO.wireName());
	}

	private HandlerResult probe(String drag, String probe, String key, String targetKey) {
		Map<String, Object> arguments = new HashMap<>(dropOn(key, targetKey));
		arguments.put(DropProbeArguments.DRAG, drag);
		arguments.put(DropProbeArguments.PROBE, probe);
		return _target.executeClientCommand(DROP_PROBE, arguments);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Map<String, Object>> verdicts(Table table) {
		return (Map<String, Map<String, Object>>) table.clientState(DROP_VERDICTS);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> rowState(Table table, int index) {
		return ((List<Map<String, Object>>) table.clientState(ROWS)).get(index);
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
