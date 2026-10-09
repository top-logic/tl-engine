/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.dnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.dnd.AcceptedKinds;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropEvent;
import com.top_logic.layout.react.control.dnd.DropLocation;
import com.top_logic.layout.react.control.dnd.DropMode;
import com.top_logic.layout.react.control.dnd.DropRequest;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.control.dnd.DropVerdict;
import com.top_logic.layout.react.control.dnd.DropZone;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropReference;
import com.top_logic.layout.view.dnd.DropSignature;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests {@link DropBinding}, the drop target of declared {@code <drop>}s: which drag kinds it
 * accepts, which modes it announces, and which declared drop applies a drop that arrives.
 *
 * <p>
 * Tables serve as source and target control: the drop is driven through the control seam the client
 * uses - the {@code drop} command of the receiving table, naming the source table and its dragged
 * row - so what is exercised is the same path a dragged item takes. A row of the target table is
 * the item an {@link DropMode#ONTO} drop is made on.
 * </p>
 */
public class TestDropBinding extends TestCase {

	/** The kind the rows of the source table are dragged as. */
	private static final String ROW_KIND = "row";

	/** A kind the source table does not drag unless a test switches it to it. */
	private static final String OTHER_KIND = "other";

	private static final String COLUMN_VALUE = "value";

	private static final List<String> SOURCE_ROWS = List.of("a1", "a2", "a3");

	private static final List<String> TARGET_ROWS = List.of("b1", "b2");

	/** A {@link ViewAction} remembering what the chain handed it. */
	private static final class Recorder implements ViewAction {

		Object _input;

		boolean _executed;

		@Override
		public Object execute(ReactContext context, Object input) {
			_executed = true;
			_input = input;
			return input;
		}
	}

	private ReactContext _context;

	private TableViewControl<String> _source;

	private ViewChannel _targetChannel;

	private ViewChannel _beforeChannel;

	private Recorder _onTable;

	private Recorder _onRow;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// One context, hence one control registry: the drop resolves its source control out of it.
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_source = newTable(SOURCE_ROWS);
		_source.setDragSource(ROW_KIND);
		// The source table is displayed: only a displayed control can be addressed by its ID.
		_source.attach();
		_targetChannel = new DefaultViewChannel("dropTarget");
		_beforeChannel = new DefaultViewChannel("dropBefore");
		_onTable = new Recorder();
		_onRow = new Recorder();
	}

	private TableViewControl<String> newTable(List<String> rows) {
		List<Column<String, ?>> columns =
			List.of(DefaultColumn.<String, String> builder(COLUMN_VALUE, value -> value).build());
		return new TableViewControl<>(_context,
			DefaultTableView.create(columns, new ListRowSource<>(rows, columns)), false);
	}

	/**
	 * A table accepting the given drops.
	 */
	private TableViewControl<String> newTargetTable(List<DropBinding.Drop> drops) {
		TableViewControl<String> target = newTable(TARGET_ROWS);
		target.setDropTarget(new DropBinding(_context, drops));
		return target;
	}

	private static AcceptedKinds kinds(String... kinds) {
		return AcceptedKinds.of(List.of(kinds));
	}

	private DropBinding.Drop tableDrop(String acceptedKind, Recorder action) {
		return tableDrop(kinds(acceptedKind), action);
	}

	private DropBinding.Drop tableDrop(AcceptedKinds accepted, Recorder action) {
		return new DropBinding.Drop(accepted, DropSignature.CONTROL, Map.of(), List.of(action));
	}

	private DropBinding.Drop rowDrop(String acceptedKind, Recorder action) {
		return new DropBinding.Drop(kinds(acceptedKind), DropSignature.ONTO, targetChannel(), List.of(action));
	}

	/** The channel map publishing the target of a drop on {@link #_targetChannel}. */
	private Map<DropReference, ViewChannel> targetChannel() {
		return Map.of(DropReference.TARGET, _targetChannel);
	}

	/**
	 * An insertion among the rows, publishing the row it inserts before on {@link #_beforeChannel}.
	 */
	private DropBinding.Drop orderedDrop(String acceptedKind, Recorder action,
			BiFunction<List<?>, List<?>, Object> refuseIf) {
		return new DropBinding.Drop(kinds(acceptedKind), DropSignature.ORDERED_LIST,
			Map.of(DropReference.BEFORE, _beforeChannel), List.of(action), () -> ExecutableState.EXECUTABLE,
			ViewExecutabilityRule.ALWAYS_EXECUTABLE, refuseIf);
	}

	/** The client-side key of the row at the given index of a table. */
	private static String rowKey(int rowIndex) {
		return "row_" + rowIndex;
	}

	/**
	 * Drops the source table's row {@code sourceIndex} on the given table - on its row
	 * {@code targetIndex}, or on the table itself when that is negative.
	 */
	private HandlerResult drop(TableViewControl<String> target, int sourceIndex, int targetIndex) {
		return drop(target, sourceIndex, targetIndex, targetIndex >= 0 ? DropZone.MIDDLE : DropZone.NONE);
	}

	/**
	 * Drops the source table's row {@code sourceIndex} in the given zone of the given table's row
	 * {@code targetIndex}, or beside its rows when that is negative.
	 */
	private HandlerResult drop(TableViewControl<String> target, int sourceIndex, int targetIndex, DropZone zone) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _source.getID());
		arguments.put(DropArguments.KEYS, rowKey(sourceIndex));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		if (targetIndex >= 0) {
			arguments.put(DropArguments.TARGET_KEY, rowKey(targetIndex));
			arguments.put(DropArguments.ZONE, zone.wireName());
		} else {
			arguments.put(DropArguments.ZONE, DropZone.NONE.wireName());
		}
		return target.executeClientCommand(DropSupport.CMD_DROP, arguments);
	}

	/**
	 * The kinds the table accepts are those of all its drops, and the modes it announces those of
	 * all its drops, in declaration order.
	 */
	public void testAcceptedKindsAndModes() {
		DropBinding both = new DropBinding(_context,
			List.of(tableDrop(ROW_KIND, _onTable), rowDrop(OTHER_KIND, _onRow)));

		assertEquals(kinds(ROW_KIND, OTHER_KIND), both.acceptedKinds());
		assertEquals("A drop onto rows announces its mode.", List.of(DropMode.CONTROL, DropMode.ONTO),
			List.copyOf(both.dropModes()));

		DropBinding onTableOnly =
			new DropBinding(_context, List.of(tableDrop(ROW_KIND, _onTable)));
		assertEquals(kinds(ROW_KIND), onTableOnly.acceptedKinds());
		assertEquals("Without a row drop only the table is a target.", Set.of(DropMode.CONTROL),
			onTableOnly.dropModes());

		DropBinding withAny = new DropBinding(_context,
			List.of(tableDrop(ROW_KIND, _onTable), tableDrop(AcceptedKinds.ANY, _onRow)));
		assertEquals("A drop accepting any drag makes the binding accept any.", AcceptedKinds.ANY,
			withAny.acceptedKinds());
	}

	/**
	 * A drop accepting any drag takes a drag of a kind as well as one without a kind.
	 */
	public void testDropAcceptingAnyTakesKindedAndUnkindedDrags() {
		TableViewControl<String> target = newTargetTable(List.of(tableDrop(AcceptedKinds.ANY, _onTable)));

		assertTrue("A drag of a kind is taken.", drop(target, 0, -1).isSuccess());
		assertEquals(List.of(SOURCE_ROWS.get(0)), _onTable._input);

		_source.setDragSource(null);
		assertTrue("A drag without a kind is taken.", drop(target, 1, -1).isSuccess());
		assertEquals(List.of(SOURCE_ROWS.get(1)), _onTable._input);
	}

	/**
	 * A drop listing kinds refuses a drag without a kind and a drag of a kind it does not list.
	 */
	public void testDropListingKindsRefusesUnkindedAndOtherDrags() {
		TableViewControl<String> target = newTargetTable(List.of(tableDrop(ROW_KIND, _onTable)));

		_source.setDragSource(null);
		assertFalse("A drag without a kind must be refused.", drop(target, 0, -1).isSuccess());
		assertFalse(_onTable._executed);

		_source.setDragSource(OTHER_KIND);
		assertFalse("A drag of an unlisted kind must be refused.", drop(target, 0, -1).isSuccess());
		assertFalse(_onTable._executed);
	}

	/**
	 * Of two drops on the same target, the one accepting the kind of the drag applies it.
	 */
	public void testDropsAreSelectedByKind() {
		Recorder onOther = new Recorder();
		TableViewControl<String> target =
			newTargetTable(List.of(tableDrop(ROW_KIND, _onTable), tableDrop(OTHER_KIND, onOther)));

		assertTrue(drop(target, 0, -1).isSuccess());
		assertTrue("The drop accepting the row kind applies a row drag.", _onTable._executed);
		assertFalse(onOther._executed);

		_onTable._executed = false;
		_source.setDragSource(OTHER_KIND);
		assertTrue(drop(target, 0, -1).isSuccess());
		assertTrue("The drop accepting the other kind applies a drag of that kind.", onOther._executed);
		assertFalse(_onTable._executed);
	}

	/**
	 * A drop on the table runs the chain of the table drop, with the dragged objects as its input.
	 */
	public void testTableDropRunsItsChain() {
		TableViewControl<String> target = newTargetTable(List.of(tableDrop(ROW_KIND, _onTable)));

		assertTrue(drop(target, 1, -1).isSuccess());

		assertTrue("The chain of the table drop must run.", _onTable._executed);
		assertEquals("The chain receives the dragged objects.", List.of(SOURCE_ROWS.get(1)), _onTable._input);
		assertNull("A table drop has no target row to publish.", _targetChannel.get());
	}

	/**
	 * A drop on a row runs the chain of the row drop, and the row dropped on reaches the chain
	 * through the drop's target channel.
	 */
	public void testRowDropPublishesTheTargetRow() {
		TableViewControl<String> target =
			newTargetTable(List.of(rowDrop(ROW_KIND, _onRow), tableDrop(ROW_KIND, _onTable)));

		assertTrue(drop(target, 2, 1).isSuccess());

		assertTrue("The chain of the row drop must run.", _onRow._executed);
		assertFalse("The table drop must not run as well.", _onTable._executed);
		assertEquals(List.of(SOURCE_ROWS.get(2)), _onRow._input);
		assertEquals("The row dropped on is published before the chain runs.",
			TARGET_ROWS.get(1), _targetChannel.get());
	}

	/**
	 * The declared order decides between a table drop and a row drop accepting the same kind: the
	 * one declared first applies a drop on a row; a drop beside the rows has no row the row drop
	 * could take, and goes to the table drop wherever that is declared.
	 */
	public void testDeclaredOrderDecidesBetweenTableAndRowDrop() {
		TableViewControl<String> tableFirst =
			newTargetTable(List.of(tableDrop(ROW_KIND, _onTable), rowDrop(ROW_KIND, _onRow)));

		assertTrue(drop(tableFirst, 0, 1).isSuccess());
		assertTrue("The table drop declared first takes a drop on a row.", _onTable._executed);
		assertFalse(_onRow._executed);
		assertNull("A table drop publishes no row.", _targetChannel.get());

		_onTable._executed = false;
		TableViewControl<String> rowFirst =
			newTargetTable(List.of(rowDrop(ROW_KIND, _onRow), tableDrop(ROW_KIND, _onTable)));

		assertTrue(drop(rowFirst, 0, 1).isSuccess());
		assertTrue("The row drop declared first takes a drop on a row.", _onRow._executed);
		assertFalse(_onTable._executed);
		assertEquals(TARGET_ROWS.get(1), _targetChannel.get());

		_onRow._executed = false;
		assertTrue(drop(rowFirst, 0, -1).isSuccess());
		assertTrue("A drop beside the rows goes to the table drop.", _onTable._executed);
		assertFalse(_onRow._executed);
	}

	/**
	 * A row drop refusing a drop on a row hands it on to a table drop declared after it, and the
	 * verdict names the location of the table drop.
	 */
	public void testARefusingRowDropHandsOnToTheTableDrop() {
		DropBinding.Drop refusing = new DropBinding.Drop(kinds(ROW_KIND), DropSignature.ONTO, targetChannel(),
			List.of(_onRow), () -> ExecutableState.EXECUTABLE, ViewExecutabilityRule.ALWAYS_EXECUTABLE,
			(objects, references) -> "Not here.");
		DropBinding binding = new DropBinding(_context, List.of(refusing, tableDrop(ROW_KIND, _onTable)));

		DropVerdict verdict = binding.check(request(TARGET_ROWS.get(0)));
		assertTrue(verdict.isAccepted());
		assertEquals(new DropLocation.Control(), verdict.location());

		TableViewControl<String> target = newTable(TARGET_ROWS);
		target.setDropTarget(binding);
		assertTrue(drop(target, 0, 0).isSuccess());
		assertTrue("The table drop applies what the row drop refuses.", _onTable._executed);
		assertFalse(_onRow._executed);
		assertNull(_targetChannel.get());

		DropBinding alone = new DropBinding(_context, List.of(refusing));
		assertRefused(REASON, alone.check(request(TARGET_ROWS.get(0))));
	}

	/**
	 * A drop on a row whose kind no row drop accepts falls back to the table drop - a table
	 * accepting drags of one kind on its rows still accepts another kind as a whole, wherever the
	 * pointer happened to be.
	 */
	public void testARowDropFallsBackToTheTableDrop() {
		TableViewControl<String> target =
			newTargetTable(List.of(rowDrop(OTHER_KIND, _onRow), tableDrop(ROW_KIND, _onTable)));

		assertTrue(drop(target, 0, 1).isSuccess());

		assertTrue("The table drop applies what the row drop does not accept.", _onTable._executed);
		assertFalse("The row drop accepts another kind.", _onRow._executed);
		assertNull("The fallback is a drop on the table, which publishes no row.", _targetChannel.get());
	}

	/**
	 * A drag of a kind no drop accepts is refused by the table before any chain is asked - the
	 * client is told the accepted kinds, so it never offers such a drop in the first place.
	 */
	public void testAnUnacceptedKindRunsNothing() {
		TableViewControl<String> target =
			newTargetTable(List.of(tableDrop(OTHER_KIND, _onTable), rowDrop(OTHER_KIND, _onRow)));

		assertFalse("A drop of an unaccepted kind must be refused.", drop(target, 0, -1).isSuccess());

		assertFalse(_onTable._executed);
		assertFalse(_onRow._executed);
	}

	/**
	 * Dragging a selected row drags the whole selection, and the chain gets all of it.
	 */
	public void testTheSelectionIsDropped() {
		TableViewControl<String> target = newTargetTable(List.of(tableDrop(ROW_KIND, _onTable)));
		_source.selectRow(SOURCE_ROWS.get(0));

		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _source.getID());
		arguments.put(DropArguments.KEYS, rowKey(0));
		arguments.put(DropArguments.SELECTION, Boolean.TRUE);
		arguments.put(DropArguments.ZONE, DropZone.NONE.wireName());
		assertTrue(target.executeClientCommand(DropSupport.CMD_DROP, arguments).isSuccess());

		assertEquals(List.of(SOURCE_ROWS.get(0)), _onTable._input);
	}

	/** The reason the test rules refuse with. */
	private static final ResKey REASON = ResKey.text("Not here.");

	/**
	 * A drop whose table-wide state refuses is not announced - its kinds and its mode drop out -
	 * and is refused with the state's reason; it is offered again as soon as the state allows.
	 */
	public void testTableWideRefusalRemovesTheDrop() {
		ExecutableState[] state = { ExecutableState.createDisabledState(REASON) };
		DropBinding.Drop restricted = new DropBinding.Drop(kinds(ROW_KIND), DropSignature.ONTO,
			targetChannel(), List.of(_onRow), () -> state[0], ViewExecutabilityRule.ALWAYS_EXECUTABLE, null);
		DropBinding binding = new DropBinding(_context, List.of(restricted, tableDrop(OTHER_KIND, _onTable)));

		assertEquals("A disabled drop contributes no kind.", kinds(OTHER_KIND), binding.acceptedKinds());
		assertEquals("A disabled row drop announces no mode.", Set.of(DropMode.CONTROL), binding.dropModes());
		assertRefused(REASON, binding.check(request(TARGET_ROWS.get(0))));

		state[0] = ExecutableState.NOT_EXEC_HIDDEN;
		assertRefused(I18NConstants.ERROR_DROP_REFUSED, binding.check(request(TARGET_ROWS.get(0))));

		state[0] = ExecutableState.EXECUTABLE;
		assertEquals(kinds(ROW_KIND, OTHER_KIND), binding.acceptedKinds());
		assertEquals(List.of(DropMode.ONTO, DropMode.CONTROL), List.copyOf(binding.dropModes()));
		assertTrue(binding.check(request(TARGET_ROWS.get(0))).isAccepted());
	}

	/**
	 * The target rule of a row drop refuses a single row with its reason, and a drop made there
	 * anyway runs nothing and publishes no target.
	 */
	public void testTargetRuleRefusesARow() {
		String refusedRow = TARGET_ROWS.get(1);
		ViewExecutabilityRule targetRule = row -> refusedRow.equals(row)
			? ExecutableState.createDisabledState(REASON) : ExecutableState.EXECUTABLE;
		DropBinding.Drop restricted = new DropBinding.Drop(kinds(ROW_KIND), DropSignature.ONTO,
			targetChannel(), List.of(_onRow), () -> ExecutableState.EXECUTABLE, targetRule, null);
		DropBinding binding = new DropBinding(_context, List.of(restricted));
		TableViewControl<String> target = newTable(TARGET_ROWS);
		target.setDropTarget(binding);

		assertTrue(binding.check(request(TARGET_ROWS.get(0))).isAccepted());
		assertRefused(REASON, binding.check(request(refusedRow)));

		HandlerResult result = drop(target, 0, 1);
		assertFalse("A drop on a refused row must fail.", result.isSuccess());
		assertFalse("A refused drop runs no chain.", _onRow._executed);
		assertNull("A refused drop publishes no target.", _targetChannel.get());

		assertTrue(drop(target, 0, 0).isSuccess());
		assertTrue(_onRow._executed);
	}

	/**
	 * The refusal function is interpreted like a {@code disabled-if}: no value and {@code false}
	 * accept, {@code true} refuses with the generic reason, a text refuses with that text. It is
	 * called with the dragged objects and the target row.
	 */
	public void testRefuseIf() {
		Object[] result = new Object[1];
		List<Object> seen = new ArrayList<>();
		BiFunction<List<?>, List<?>, Object> refuseIf = (objects, references) -> {
			seen.add(objects);
			seen.add(references);
			return result[0];
		};
		DropBinding binding = new DropBinding(_context, List.of(new DropBinding.Drop(
			kinds(ROW_KIND), DropSignature.ONTO, targetChannel(), List.of(_onRow),
			() -> ExecutableState.EXECUTABLE, ViewExecutabilityRule.ALWAYS_EXECUTABLE, refuseIf)));

		result[0] = null;
		assertTrue("No value accepts.", binding.check(request(TARGET_ROWS.get(1))).isAccepted());
		assertEquals("The function gets the dragged objects and the target row.",
			List.of(List.of(SOURCE_ROWS.get(0)), List.of(TARGET_ROWS.get(1))), seen);

		result[0] = Boolean.FALSE;
		assertTrue("False accepts.", binding.check(request(TARGET_ROWS.get(1))).isAccepted());

		result[0] = Boolean.TRUE;
		assertRefused(I18NConstants.ERROR_DROP_REFUSED, binding.check(request(TARGET_ROWS.get(1))));

		result[0] = "Not here.";
		assertRefused(REASON, binding.check(request(TARGET_ROWS.get(1))));
		assertNull("A check writes no target channel.", _targetChannel.get());
	}

	/**
	 * A drag no declared drop matches is refused with the generic reason.
	 */
	public void testNoMatchIsRefused() {
		DropBinding binding = new DropBinding(_context, List.of(tableDrop(OTHER_KIND, _onTable)));

		assertRefused(com.top_logic.layout.react.I18NConstants.ERROR_DROP_NOT_ACCEPTED, binding.check(request(null)));
	}

	/**
	 * Applying a drop passes over a disabled declared drop - to the next matching one, or to none.
	 */
	public void testOnDropSkipsDisabledDrops() {
		DropBinding.Drop disabled = new DropBinding.Drop(kinds(ROW_KIND), DropSignature.CONTROL, Map.of(),
			List.of(_onRow), () -> ExecutableState.createDisabledState(REASON),
			ViewExecutabilityRule.ALWAYS_EXECUTABLE, null);

		new DropBinding(_context, List.of(disabled)).onDrop(event());
		assertFalse("A disabled drop applies nothing.", _onRow._executed);

		new DropBinding(_context, List.of(disabled, tableDrop(ROW_KIND, _onTable))).onDrop(event());
		assertFalse(_onRow._executed);
		assertTrue("The next matching drop applies.", _onTable._executed);
	}

	/** A value no drop publishes, marking a channel as not written. */
	private static final String UNWRITTEN = "unwritten";

	/**
	 * An insertion gets the row it inserts before in its refusal function and its before channel:
	 * the row of the upper zone, the row following the lower zone, and {@code null} below the last
	 * row and beside the rows.
	 */
	public void testOrderedDropPublishesTheRowToInsertBefore() {
		List<List<?>> seen = new ArrayList<>();
		TableViewControl<String> target = newTargetTable(List.of(orderedDrop(ROW_KIND, _onTable,
			(objects, references) -> {
				seen.add(references);
				return null;
			})));

		assertInsertion(target, 0, DropZone.UPPER, TARGET_ROWS.get(0), seen);
		assertInsertion(target, 0, DropZone.LOWER, TARGET_ROWS.get(1), seen);
		assertInsertion(target, 1, DropZone.UPPER, TARGET_ROWS.get(1), seen);
		assertInsertion(target, 1, DropZone.LOWER, null, seen);
		assertInsertion(target, -1, DropZone.NONE, null, seen);
		assertEquals("An insertion has no target row.", UNWRITTEN, _targetChannel.get());
	}

	private void assertInsertion(TableViewControl<String> target, int rowIndex, DropZone zone, String expectedBefore,
			List<List<?>> seen) {
		String place = "row " + rowIndex + ", zone " + zone;
		seen.clear();
		_beforeChannel.set(UNWRITTEN);
		_targetChannel.set(UNWRITTEN);
		_onTable._executed = false;

		assertTrue(place, drop(target, 0, rowIndex, zone).isSuccess());
		assertTrue(place, _onTable._executed);
		assertEquals(place + ": the chain receives the dragged objects.", List.of(SOURCE_ROWS.get(0)),
			_onTable._input);
		assertEquals(place + ": the before channel is written before the chain runs.", expectedBefore,
			_beforeChannel.get());
		assertFalse(place + ": the refusal function is asked.", seen.isEmpty());
		for (List<?> references : seen) {
			assertEquals(place + ": the refusal function gets the row to insert before.",
				Collections.singletonList(expectedBefore), references);
		}
	}

	/**
	 * The middle of a row is no insertion: an ordered drop alone has no location there.
	 */
	public void testOrderedDropHasNoLocationInTheMiddleOfARow() {
		TableViewControl<String> target = newTargetTable(List.of(orderedDrop(ROW_KIND, _onTable, null)));

		assertFalse(drop(target, 0, 0, DropZone.MIDDLE).isSuccess());
		assertFalse(_onTable._executed);
	}

	/**
	 * An ordered drop declared before a row drop takes the upper and lower zones of a row and the
	 * place beside the rows, and leaves the middle of a row to the row drop; a row drop declared
	 * first takes the whole row, and leaves only the place beside the rows to the ordered drop.
	 */
	public void testOrderedAndRowDropsAreSelectedByZoneAndDeclaredOrder() {
		TableViewControl<String> orderedFirst = newTargetTable(
			List.of(orderedDrop(ROW_KIND, _onTable, null), rowDrop(ROW_KIND, _onRow)));

		assertApplied(orderedFirst, 0, DropZone.UPPER, _onTable, _onRow);
		assertEquals(TARGET_ROWS.get(0), _beforeChannel.get());
		assertApplied(orderedFirst, 0, DropZone.MIDDLE, _onRow, _onTable);
		assertEquals(TARGET_ROWS.get(0), _targetChannel.get());
		assertApplied(orderedFirst, 0, DropZone.LOWER, _onTable, _onRow);
		assertEquals(TARGET_ROWS.get(1), _beforeChannel.get());
		assertApplied(orderedFirst, -1, DropZone.NONE, _onTable, _onRow);
		assertNull(_beforeChannel.get());

		TableViewControl<String> rowFirst = newTargetTable(
			List.of(rowDrop(ROW_KIND, _onRow), orderedDrop(ROW_KIND, _onTable, null)));

		assertApplied(rowFirst, 0, DropZone.UPPER, _onRow, _onTable);
		assertApplied(rowFirst, 0, DropZone.LOWER, _onRow, _onTable);
		assertApplied(rowFirst, -1, DropZone.NONE, _onTable, _onRow);
	}

	/**
	 * An ordered drop refusing an insertion hands the drop on to the row drop declared after it.
	 */
	public void testARefusingOrderedDropHandsOnToTheRowDrop() {
		TableViewControl<String> target = newTargetTable(List.of(
			orderedDrop(ROW_KIND, _onTable,
				(objects, references) -> TARGET_ROWS.get(0).equals(references.get(0)) ? "Not here." : null),
			rowDrop(ROW_KIND, _onRow)));

		assertApplied(target, 0, DropZone.UPPER, _onRow, _onTable);
		assertEquals(TARGET_ROWS.get(0), _targetChannel.get());
		assertApplied(target, 0, DropZone.LOWER, _onTable, _onRow);
		assertEquals(TARGET_ROWS.get(1), _beforeChannel.get());
	}

	private void assertApplied(TableViewControl<String> target, int rowIndex, DropZone zone, Recorder applied,
			Recorder notApplied) {
		String place = "row " + rowIndex + ", zone " + zone;
		applied._executed = false;
		notApplied._executed = false;
		assertTrue(place, drop(target, 0, rowIndex, zone).isSuccess());
		assertTrue(place + ": expected drop not applied.", applied._executed);
		assertFalse(place + ": unexpected drop applied.", notApplied._executed);
	}

	/**
	 * A drop of the source table's first row on the table itself, ready for application.
	 */
	private DropEvent event() {
		return new DropEvent(_source, _source.dragKind(), List.of(SOURCE_ROWS.get(0)), new DropLocation.Control());
	}

	/**
	 * A drop of the source table's first row, on the given row of a target table, or beside its
	 * rows for {@code null} - located as a table locates it.
	 */
	private DropRequest request(String targetRow) {
		return new DropRequest(_source, _source.dragKind(), List.of(SOURCE_ROWS.get(0)), mode -> {
			switch (mode) {
				case CONTROL:
					return new DropLocation.Control();
				case ONTO:
					return targetRow == null ? null : new DropLocation.Onto(targetRow);
				default:
					return null;
			}
		});
	}

	private static void assertRefused(ResKey expectedReason, DropVerdict verdict) {
		assertFalse("The drop must be refused.", verdict.isAccepted());
		assertEquals(expectedReason, verdict.reason());
	}

	/**
	 * The test suite, started with the resource bundles a table's column labels need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDropBinding.class, ResourcesModule.Module.INSTANCE));
	}

}
