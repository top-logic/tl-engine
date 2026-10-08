/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMarker;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropObjectsArguments;
import com.top_logic.layout.react.control.dnd.DropProbeArguments;
import com.top_logic.layout.react.control.dnd.DropRequest;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropTarget;
import com.top_logic.layout.react.control.dnd.DropVerdict;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
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
 * Both directions of the two-stage acceptance are exercised: a drop of a kind the target never
 * declared, and one naming rows the source does not display, are refused without the handler being
 * asked - the client-side check that precedes a drop narrows the gesture for the user, it does not
 * decide it.
 * </p>
 *
 * <p>
 * The {@link DropTarget#check(DropRequest) verdict} of the target is asked both by a drop, which it
 * can refuse, and by the {@code dropProbe} command a client sends while a drag hovers, which answers
 * it in the client state, with the marker to draw, without applying anything. Where the drop is made
 * is reported as a row and a {@link DropZone zone}; the table resolves it into the
 * {@link DropLocation} of each {@link DropMode}, as in a flat list.
 * </p>
 */
public class TestTableViewDragDrop extends TestCase {

	/** The drag kind both tables agree on. */
	private static final String PERSON = "person";

	private static final String DROP = "drop";

	private static final String DROP_PROBE = "dropProbe";

	private static final String DROP_OBJECTS = DropSupport.CMD_DROP_OBJECTS;

	/** State key of the verdicts answered to drop probes. */
	private static final String DROP_VERDICTS = "dropVerdicts";

	/** Entry of a verdict telling whether the drop is accepted. */
	private static final String VERDICT_ACCEPTED = "accepted";

	/** Entry of a refusing verdict holding the reason. */
	private static final String VERDICT_REASON = "reason";

	/** Entry of an accepting verdict holding the marker to draw. */
	private static final String VERDICT_MARKER = DropSupport.VERDICT_MARKER;

	/** Entry of an accepting verdict holding the key of the row the marker is drawn at. */
	private static final String VERDICT_MARKER_KEY = DropSupport.VERDICT_MARKER_KEY;

	/** State key of the announced drop modes. */
	private static final String DROP_MODES = DropSupport.DROP_MODES;

	/** State key telling whether any drag is accepted. */
	private static final String DROP_ACCEPTS_ANY = "dropAcceptsAny";

	/** State key of the accepted drag kinds. */
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
	 * {@link #_check} says - by default that of {@link DropTarget#check(DropRequest)}.
	 */
	private static final class Announced implements DropTarget {

		private AcceptedKinds _acceptedKinds;

		private Set<DropMode> _modes;

		DropEvent _event;

		DropRequest _checked;

		Function<DropRequest, DropVerdict> _check;

		Announced(AcceptedKinds acceptedKinds, DropMode... modes) {
			_acceptedKinds = acceptedKinds;
			_modes = new LinkedHashSet<>(List.of(modes));
		}

		@Override
		public AcceptedKinds acceptedKinds() {
			return _acceptedKinds;
		}

		@Override
		public Set<DropMode> dropModes() {
			return _modes;
		}

		@Override
		public DropVerdict check(DropRequest request) {
			_checked = request;
			return _check == null ? DropTarget.super.check(request) : _check.apply(request);
		}

		@Override
		public void onDrop(DropEvent event) {
			_event = event;
		}
	}

	/**
	 * One operation of an {@link Operations} target: a mode and the reason it refuses a location
	 * with ({@code null} to accept).
	 */
	private static final class Operation {

		final DropMode _mode;

		final Function<DropLocation, ResKey> _refusal;

		final List<DropLocation> _applied = new ArrayList<>();

		Operation(DropMode mode, Function<DropLocation, ResKey> refusal) {
			_mode = mode;
			_refusal = refusal;
		}

	}

	/**
	 * A {@link DropTarget} of several operations, tried in their order: the first one finding a
	 * location for its mode and not refusing it wins.
	 */
	private static final class Operations implements DropTarget {

		final List<Operation> _operations;

		Operations(Operation... operations) {
			_operations = List.of(operations);
		}

		@Override
		public AcceptedKinds acceptedKinds() {
			return AcceptedKinds.ANY;
		}

		@Override
		public Set<DropMode> dropModes() {
			Set<DropMode> result = new LinkedHashSet<>();
			for (Operation operation : _operations) {
				result.add(operation._mode);
			}
			return result;
		}

		@Override
		public DropVerdict check(DropRequest request) {
			Operation winner = winner(request);
			if (winner != null) {
				return DropVerdict.accepted(request.location(winner._mode));
			}
			ResKey first = null;
			for (Operation operation : _operations) {
				DropLocation location = request.location(operation._mode);
				if (location != null && first == null) {
					first = operation._refusal.apply(location);
				}
			}
			return DropVerdict.refused(first != null ? first : REFUSAL);
		}

		private Operation winner(DropRequest request) {
			for (Operation operation : _operations) {
				DropLocation location = request.location(operation._mode);
				if (location != null && operation._refusal.apply(location) == null) {
					return operation;
				}
			}
			return null;
		}

		@Override
		public void onDrop(DropEvent event) {
			Operation winner = winner(DropRequest.of(event));
			winner._applied.add(event.location());
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
		_announced = new Announced(AcceptedKinds.of(List.of(PERSON)), DropMode.ONTO);
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

	/**
	 * Asserts that the drop was refused, and reported as the warning of a refusal rather than as an
	 * error.
	 */
	private static void assertRefused(String message, HandlerResult result) {
		assertFalse(message, result.isSuccess());
		assertEquals(message + " A refused drop is a warning.", ErrorSeverity.WARNING, result.getErrorSeverity());
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
			DropArguments.ZONE, DropZone.MIDDLE.wireName()));

		assertApplied("A drop of an accepted type on a displayed row must be applied.", result);
		DropEvent event = _announced._event;
		assertNotNull("The drop target must be asked to apply the drop.", event);
		assertEquals(List.of(PEOPLE.get(1)), event.objects());
		assertEquals(PEOPLE.get(0), event.target());
		assertEquals(new DropLocation.Onto(PEOPLE.get(0)), event.location());
		assertSame("The event names the control the objects were dragged out of.", _source, event.source());
		assertEquals("The event names the kind of the drag.", PERSON, event.kind());
	}

	/**
	 * A drop beside the rows of a target taking drops on the table as a whole is made on the table:
	 * its location names no row.
	 */
	public void testDropOnTheTableNamesNoRow() {
		Announced onTable = new Announced(AcceptedKinds.of(List.of(PERSON)), DropMode.CONTROL);
		_target.setDropTarget(onTable);

		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(2),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.ZONE, DropZone.NONE.wireName()));

		assertApplied("A drop on the table itself must be applied.", result);
		DropEvent event = onTable._event;
		assertNotNull(event);
		assertEquals(List.of(PEOPLE.get(2)), event.objects());
		assertNull(event.target());
		assertEquals(new DropLocation.Control(), event.location());
	}

	/** A target taking drops onto rows only has no location beside the rows: such a drop is refused. */
	public void testDropBesideTheRowsIsRefusedByAnOntoTarget() {
		HandlerResult result = drop(_target, dropAt(rowKey(1), null, DropZone.NONE));

		assertRefused("A drop beside the rows has no row to be made onto.", result);
		assertNull(_announced._event);
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
			DropArguments.ZONE, DropZone.MIDDLE.wireName()));

		assertApplied("A drop of the source's selection must be applied.", result);
		DropEvent event = _announced._event;
		assertNotNull(event);
		assertEquals("The selection is dragged, not the row the keys name.",
			List.of(PEOPLE.get(0)), event.objects());
		assertEquals(PEOPLE.get(1), event.target());
	}

	/**
	 * A target that is no row target receives every drop on the table as a whole, even one the client
	 * announced on a row.
	 */
	public void testARowTargetIsIgnoredWhenRowsAreNoTargets() {
		Announced onTableOnly = new Announced(AcceptedKinds.of(List.of(PERSON)), DropMode.CONTROL);
		_target.setDropTarget(onTableOnly);

		HandlerResult result = drop(_target, dropAt(rowKey(1), rowKey(0), DropZone.MIDDLE));

		assertApplied("A drop on a table that is no row target must be applied.", result);
		assertNotNull(onTableOnly._event);
		assertNull("A target that is no row target never gets a row.", onTableOnly._event.target());
		assertEquals(new DropLocation.Control(), onTableOnly._event.location());
	}

	/** A drop whose source drags a kind the target never declared is refused. */
	public void testDropOfAnUnacceptedKindIsRefused() {
		_source.setDragSource("milestone");

		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, rowKey(0),
			DropArguments.ZONE, DropZone.MIDDLE.wireName()));

		assertRefused("A drop of an unaccepted kind must be refused.", result);
		assertNull("The drop target must not be asked to apply it.", _announced._event);
	}

	/** So is a drop naming rows the source control does not display. */
	public void testDropOfUnknownKeysIsRefused() {
		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(99),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, rowKey(0),
			DropArguments.ZONE, DropZone.MIDDLE.wireName()));

		assertRefused("A drop of rows that resolve to nothing must be refused.", result);
		assertNull("The drop target must not be asked to apply it.", _announced._event);
	}

	/** So is a drop on a row the receiving table does not display. */
	public void testDropOnAnUnknownRowIsRefused() {
		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.TARGET_KEY, rowKey(99),
			DropArguments.ZONE, DropZone.MIDDLE.wireName()));

		assertRefused("A drop on a row that resolves to nothing must be refused.", result);
		assertNull("The drop target must not be asked to apply it.", _announced._event);
	}

	/** A table that declares no drop target refuses a drop outright. */
	public void testATableWithoutADropTargetRefusesADrop() {
		TableViewControl<Person> plain = newTable();

		HandlerResult result = drop(plain, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.ZONE, DropZone.NONE.wireName()));

		assertRefused("A table accepting no drop must refuse one.", result);
	}

	/**
	 * A drag without a kind is refused by a target listing kinds, and accepted by one accepting any
	 * drag.
	 */
	public void testUnkindedDragIsAcceptedOnlyByATargetAcceptingAny() {
		_source.setDragSource(null);

		assertRefused("A target listing kinds refuses a drag without a kind.",
			drop(_target, dropOn(rowKey(1), rowKey(0))));
		assertNull(_announced._event);

		Announced any = new Announced(AcceptedKinds.ANY, DropMode.ONTO);
		_target.setDropTarget(any);
		assertApplied("A target accepting any drag takes one without a kind.",
			drop(_target, dropOn(rowKey(1), rowKey(0))));
		assertNotNull(any._event);
		assertNull("The event of a drag without a kind names none.", any._event.kind());
	}

	/** A target accepting any drag takes a drag of any kind as well. */
	public void testTargetAcceptingAnyTakesAKindedDrag() {
		Announced any = new Announced(AcceptedKinds.ANY, DropMode.ONTO);
		_target.setDropTarget(any);
		_source.setDragSource("milestone");

		assertApplied("A target accepting any drag takes one of any kind.",
			drop(_target, dropOn(rowKey(1), rowKey(0))));
		assertEquals("milestone", any._event.kind());
	}

	/** A drop whose source control has dragging switched off is refused. */
	public void testDropFromANonDraggingSourceIsRefused() {
		_source.setDragEnabled(false);

		HandlerResult result = drop(_target, Map.of(
			DropArguments.SOURCE, _source.getID(),
			DropArguments.KEYS, rowKey(1),
			DropArguments.SELECTION, Boolean.FALSE,
			DropArguments.ZONE, DropZone.NONE.wireName()));

		assertRefused("A drop from a table whose rows are not draggable must be refused.", result);
		assertNull("The drop target must not be asked to apply it.", _announced._event);
	}

	/**
	 * A drop the target's check refuses is answered with the check's reason, and the target is not
	 * asked to apply it.
	 */
	public void testRefusedVerdictBlocksTheDrop() {
		_announced._check = notOntoAlice();

		HandlerResult result = drop(_target, dropOn(rowKey(1), rowKey(0)));

		assertRefused("A drop the check refuses must be refused.", result);
		assertEquals("The refusal names the check's reason.", List.of(REFUSAL), result.getEncodedErrors());
		assertEquals("The reason is the message of the warning.", REFUSAL, result.getErrorMessage());
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
		_announced._check = notOntoAlice();

		assertApplied("A probe never fails.", probe("drag1", "p1", rowKey(1), rowKey(0)));
		assertApplied("A probe never fails.", probe("drag1", "p2", rowKey(1), rowKey(2)));

		Map<String, Map<String, Object>> verdicts = verdicts(_target);
		assertEquals(Boolean.FALSE, verdicts.get("p1").get(VERDICT_ACCEPTED));
		assertEquals(REFUSAL_TEXT, verdicts.get("p1").get(VERDICT_REASON));
		assertEquals(Boolean.TRUE, verdicts.get("p2").get(VERDICT_ACCEPTED));
		assertFalse(verdicts.get("p2").containsKey(VERDICT_REASON));
		assertEquals("A drop onto a row highlights the row.", DropMarker.INTO.wireName(),
			verdicts.get("p2").get(VERDICT_MARKER));
		assertEquals(rowKey(2), verdicts.get("p2").get(VERDICT_MARKER_KEY));
		assertFalse("A refusal draws no marker.", verdicts.get("p1").containsKey(VERDICT_MARKER));
		assertNull("A probe must not apply the drop.", _announced._event);
	}

	/** A drop the resolution refuses already is answered as refused by a probe, too. */
	public void testProbeOfAnUnacceptedKindIsRefused() {
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
		assertRefused("A drag of a refused row must be refused.", result);
		assertNull("The drop target must not be asked to apply it.", _announced._event);

		_source.selectRow(PEOPLE.get(1));
		Map<String, Object> ofSelection = new HashMap<>(dropOn(rowKey(0), rowKey(2)));
		ofSelection.put(DropArguments.SELECTION, Boolean.TRUE);
		assertRefused("A selection including a refused row is refused as a whole.",
			drop(_target, ofSelection));
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

	/** Refreshing the drop target announces its changed accepted kinds. */
	public void testRefreshDropTargetAnnouncesTheAcceptedKinds() {
		assertEquals(Boolean.FALSE, _target.clientState(DROP_ACCEPTS_ANY));
		assertEquals(List.of(PERSON), _target.clientState(DROP_ACCEPTS));

		_announced._acceptedKinds = AcceptedKinds.of(List.of(PERSON, "milestone"));
		_target.refreshDropTarget();

		assertEquals(List.of(PERSON, "milestone"), _target.clientState(DROP_ACCEPTS));

		_announced._acceptedKinds = AcceptedKinds.ANY;
		_target.refreshDropTarget();

		assertEquals(Boolean.TRUE, _target.clientState(DROP_ACCEPTS_ANY));
		assertEquals(List.of(), _target.clientState(DROP_ACCEPTS));
	}

	/**
	 * The table announces the modes of its target's operations, in the target's order, and announces
	 * them again when the target changes - the client splits a row into the zones they need.
	 */
	public void testDropModesAreAnnounced() {
		assertEquals(List.of(DropMode.ONTO.wireName()), _target.clientState(DROP_MODES));

		_announced._modes = new LinkedHashSet<>(List.of(DropMode.ORDERED, DropMode.ONTO));
		_target.refreshDropTarget();
		assertEquals(List.of(DropMode.ORDERED.wireName(), DropMode.ONTO.wireName()),
			_target.clientState(DROP_MODES));

		_target.setDropTarget(new Announced(AcceptedKinds.ANY, DropMode.CONTROL));
		assertEquals(List.of(DropMode.CONTROL.wireName()), _target.clientState(DROP_MODES));

		_target.setDropTarget(null);
		assertEquals("A table accepting no drop announces no mode.", List.of(), _target.clientState(DROP_MODES));
	}

	/**
	 * An insertion from the upper part of a row is made before that row, from its lower part before
	 * the next row - at the end below the last one -, and beside the rows at the end. The middle of a
	 * row is no place of an insertion.
	 */
	public void testOrderedLocations() {
		Announced ordered = new Announced(AcceptedKinds.of(List.of(PERSON)), DropMode.ORDERED);
		_target.setDropTarget(ordered);

		assertInserted(ordered, dropAt(rowKey(0), rowKey(1), DropZone.UPPER), PEOPLE.get(1));
		assertInserted(ordered, dropAt(rowKey(0), rowKey(1), DropZone.LOWER), PEOPLE.get(2));
		assertInserted(ordered, dropAt(rowKey(0), rowKey(2), DropZone.LOWER), null);
		assertInserted(ordered, dropAt(rowKey(0), null, DropZone.NONE), null);

		ordered._event = null;
		assertRefused("The middle of a row is no place of an insertion.",
			drop(_target, dropAt(rowKey(0), rowKey(1), DropZone.MIDDLE)));
		assertNull(ordered._event);
	}

	private void assertInserted(Announced target, Map<String, Object> arguments, Person before) {
		target._event = null;
		assertApplied("An insertion must be applied.", drop(_target, arguments));
		assertEquals(new DropLocation.Insert(null, before), target._event.location());
	}

	/**
	 * The marker of an insertion is a line before the row whose upper part the pointer is in, after
	 * the row whose lower part it is in, and after the last row beside the rows.
	 */
	public void testOrderedMarkers() {
		_target.setDropTarget(new Announced(AcceptedKinds.of(List.of(PERSON)), DropMode.ORDERED));

		probeAt("drag1", "upper", dropAt(rowKey(0), rowKey(1), DropZone.UPPER));
		probeAt("drag1", "lower", dropAt(rowKey(0), rowKey(1), DropZone.LOWER));
		probeAt("drag1", "beside", dropAt(rowKey(0), null, DropZone.NONE));

		Map<String, Map<String, Object>> verdicts = verdicts(_target);
		assertMarker(verdicts.get("upper"), DropMarker.BEFORE, rowKey(1));
		assertMarker(verdicts.get("lower"), DropMarker.AFTER, rowKey(1));
		assertMarker(verdicts.get("beside"), DropMarker.AFTER, rowKey(2));
	}

	/** A drop onto a row is made onto it, whichever part of the row the pointer is in. */
	public void testOntoInAnyZone() {
		for (DropZone zone : List.of(DropZone.UPPER, DropZone.MIDDLE, DropZone.LOWER)) {
			_announced._event = null;
			assertApplied("A drop onto a row is applied in its " + zone + " zone.",
				drop(_target, dropAt(rowKey(0), rowKey(1), zone)));
			assertEquals(new DropLocation.Onto(PEOPLE.get(1)), _announced._event.location());
		}
	}

	/**
	 * Of an insertion and a drop onto rows, the first operation that accepts the drop at its location
	 * wins: where the insertion refuses, the drop onto the row takes over, and the marker is that of
	 * the drop onto the row. The probe and the drop agree, and only the winning operation applies.
	 */
	public void testFirstAcceptingOperationWins() {
		Operation insert = new Operation(DropMode.ORDERED,
			location -> PEOPLE.get(1).equals(((DropLocation.Insert) location).before()) ? REFUSAL : null);
		Operation onto = new Operation(DropMode.ONTO, location -> null);
		_target.setDropTarget(new Operations(insert, onto));
		assertEquals(List.of(DropMode.ORDERED.wireName(), DropMode.ONTO.wireName()),
			_target.clientState(DROP_MODES));

		probeAt("drag1", "beforeBob", dropAt(rowKey(0), rowKey(1), DropZone.UPPER));
		probeAt("drag1", "beforeCarol", dropAt(rowKey(0), rowKey(2), DropZone.UPPER));
		Map<String, Map<String, Object>> verdicts = verdicts(_target);
		assertMarker(verdicts.get("beforeBob"), DropMarker.INTO, rowKey(1));
		assertMarker(verdicts.get("beforeCarol"), DropMarker.BEFORE, rowKey(2));

		assertApplied("The drop onto bob applies.", drop(_target, dropAt(rowKey(0), rowKey(1), DropZone.UPPER)));
		assertEquals(List.of(new DropLocation.Onto(PEOPLE.get(1))), onto._applied);
		assertEquals("The refusing insertion applies nothing.", List.of(), insert._applied);

		assertApplied("The insertion before carol applies.",
			drop(_target, dropAt(rowKey(0), rowKey(2), DropZone.UPPER)));
		assertEquals(List.of(new DropLocation.Insert(null, PEOPLE.get(2))), insert._applied);
		assertEquals("The drop onto rows applies nothing more.", 1, onto._applied.size());
	}

	/** Between two operations accepting the same place, the one declared first wins. */
	public void testDeclaredOrderDecides() {
		Operation onto = new Operation(DropMode.ONTO, location -> null);
		Operation insert = new Operation(DropMode.ORDERED, location -> null);
		_target.setDropTarget(new Operations(onto, insert));

		probeAt("drag1", "p1", dropAt(rowKey(0), rowKey(2), DropZone.UPPER));
		assertMarker(verdicts(_target).get("p1"), DropMarker.INTO, rowKey(2));
		assertApplied("", drop(_target, dropAt(rowKey(0), rowKey(2), DropZone.UPPER)));
		assertEquals(1, onto._applied.size());
		assertEquals(List.of(), insert._applied);

		Operation insertFirst = new Operation(DropMode.ORDERED, location -> null);
		Operation ontoSecond = new Operation(DropMode.ONTO, location -> null);
		_target.setDropTarget(new Operations(insertFirst, ontoSecond));

		probeAt("drag2", "p2", dropAt(rowKey(0), rowKey(2), DropZone.UPPER));
		assertMarker(verdicts(_target).get("p2"), DropMarker.BEFORE, rowKey(2));
		assertApplied("", drop(_target, dropAt(rowKey(0), rowKey(2), DropZone.UPPER)));
		assertEquals(List.of(new DropLocation.Insert(null, PEOPLE.get(2))), insertFirst._applied);
		assertEquals(List.of(), ontoSecond._applied);
	}

	/**
	 * A recorded insertion names the location it was applied at - not the place it was made at -,
	 * and its replay offers exactly that location to the same operation.
	 */
	public void testRecordedInsertionReplaysAtTheSameLocation() {
		Recording recording = new Recording(DropMode.ORDERED);

		DropObjectsArguments recorded = recording.record(DropZone.LOWER, 0);
		assertEquals(DropMode.ORDERED.wireName(), recorded.getMode());
		assertNotNull("The row inserted before is named.", recorded.getBefore());
		assertNull(recorded.getParent());
		assertNull(recorded.getTargetObject());

		DropEvent replayed = recording.replay(recorded);
		assertEquals("The replay inserts before the row the drop inserted before.",
			new DropLocation.Insert(null, "t2"), replayed.location());
		assertEquals(List.of("s1"), replayed.objects());
		assertNull("A replayed drop names no source control.", replayed.source());
		assertEquals(PERSON, replayed.kind());
	}

	/** A recorded drop onto a row names the row, and its replay drops onto the same row. */
	public void testRecordedOntoReplaysAtTheSameLocation() {
		Recording recording = new Recording(DropMode.ONTO);

		DropObjectsArguments recorded = recording.record(DropZone.MIDDLE, 1);
		assertEquals(DropMode.ONTO.wireName(), recorded.getMode());
		assertNotNull(recorded.getTargetObject());

		assertEquals(new DropLocation.Onto("t2"), recording.replay(recorded).location());
	}

	/**
	 * Two tables of strings - nameable for the script recorder - dragging from the one to the other,
	 * whose drop target offers the given mode.
	 */
	private final class Recording {

		private final TableViewControl<String> _from;

		private final TableViewControl<String> _to;

		private final Announced _dropTarget;

		Recording(DropMode mode) {
			_from = stringTable(List.of("s1", "s2"));
			_from.setDragSource(PERSON);
			_to = stringTable(List.of("t1", "t2", "t3"));
			_dropTarget = new Announced(AcceptedKinds.of(List.of(PERSON)), mode);
			_to.setDropTarget(_dropTarget);
			_from.attach();
			_to.attach();
		}

		/** Records a drop of the first source row in the given zone of the given target row. */
		DropObjectsArguments record(DropZone zone, int targetRow) {
			Map<String, Object> arguments = new HashMap<>();
			arguments.put(DropArguments.SOURCE, _from.getID());
			arguments.put(DropArguments.KEYS, rowKey(0));
			arguments.put(DropArguments.SELECTION, Boolean.FALSE);
			arguments.put(DropArguments.TARGET_KEY, rowKey(targetRow));
			arguments.put(DropArguments.ZONE, zone.wireName());
			RecordedCommand recorded = _to.recordCommand(DROP, arguments);
			assertNotNull("The drop must be recorded.", recorded);
			assertTrue("The drop is recorded in replay-stable form.",
				recorded.command() instanceof DropObjectsArguments);
			assertEquals(DROP_OBJECTS, recorded.command().getName());
			assertNull("Recording applies nothing.", _dropTarget._event);
			return (DropObjectsArguments) recorded.command();
		}

		/** Replays the given recorded drop, and returns what the drop target was asked to apply. */
		DropEvent replay(DropObjectsArguments recorded) {
			// A replay runs within an interaction, in which the recorded identities are resolved.
			HandlerResult result = new InteractionWithoutSession().runWithContext(
				() -> _to.executeClientCommand(DROP_OBJECTS, ReactCommands.arguments(recorded)));
			assertApplied("The recorded drop must replay.", result);
			assertNotNull(_dropTarget._event);
			return _dropTarget._event;
		}

	}

	private TableViewControl<String> stringTable(List<String> rows) {
		List<Column<String, ?>> columns = List.of(DefaultColumn.<String, String> builder("name", value -> value).build());
		return new TableViewControl<>(_context, DefaultTableView.create(columns, new ListRowSource<>(rows, columns)),
			false);
	}

	private static void assertMarker(Map<String, Object> verdict, DropMarker marker, String markerKey) {
		assertEquals("The drop must be accepted: " + verdict, Boolean.TRUE, verdict.get(VERDICT_ACCEPTED));
		assertEquals(marker.wireName(), verdict.get(VERDICT_MARKER));
		assertEquals(markerKey, verdict.get(VERDICT_MARKER_KEY));
	}

	/** The arguments of a drop of the given source row onto the given target row. */
	private Map<String, Object> dropOn(String key, String targetKey) {
		return dropAt(key, targetKey, DropZone.MIDDLE);
	}

	/**
	 * The arguments of a drop of the given source row in the given zone of the given target row,
	 * beside the rows for a {@code null} target row.
	 */
	private Map<String, Object> dropAt(String key, String targetKey, DropZone zone) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _source.getID());
		arguments.put(DropArguments.KEYS, key);
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		if (targetKey != null) {
			arguments.put(DropArguments.TARGET_KEY, targetKey);
		}
		arguments.put(DropArguments.ZONE, zone.wireName());
		return arguments;
	}

	private HandlerResult probe(String drag, String probe, String key, String targetKey) {
		return probeAt(drag, probe, dropOn(key, targetKey));
	}

	private HandlerResult probeAt(String drag, String probe, Map<String, Object> drop) {
		Map<String, Object> arguments = new HashMap<>(drop);
		arguments.put(DropProbeArguments.DRAG, drag);
		arguments.put(DropProbeArguments.PROBE, probe);
		return _target.executeClientCommand(DROP_PROBE, arguments);
	}

	/** A check refusing a drop onto alice and accepting any other drop onto a row. */
	private static Function<DropRequest, DropVerdict> notOntoAlice() {
		return request -> {
			DropLocation location = request.location(DropMode.ONTO);
			if (location == null) {
				return DropVerdict.refused(REFUSAL);
			}
			return PEOPLE.get(0).equals(((DropLocation.Onto) location).target()) ? DropVerdict.refused(REFUSAL)
				: DropVerdict.accepted(location);
		};
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
	 * drop's message need, and the {@link ModelResolver} a recorded drop names its objects with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableViewDragDrop.class, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
