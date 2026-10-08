/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.dnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.top_logic.layout.react.control.dnd.DropPosition;
import com.top_logic.layout.react.control.dnd.DropVerdict;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.dnd.DropBinding;
import com.top_logic.layout.view.dnd.DropScope;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests {@link DropBinding}, the drop target of declared {@code <drop>}s: which drag kinds it
 * accepts, whether single items are targets, and which declared drop applies a drop that arrives.
 *
 * <p>
 * Tables serve as source and target control: the drop is driven through the control seam the client
 * uses - the {@code drop} command of the receiving table, naming the source table and its dragged
 * row - so what is exercised is the same path a dragged item takes. A row of the target table is
 * the item an {@link DropScope#ITEM} drop is made on.
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
		return new DropBinding.Drop(accepted, DropScope.CONTROL, null, List.of(action));
	}

	private DropBinding.Drop rowDrop(String acceptedKind, Recorder action) {
		return new DropBinding.Drop(kinds(acceptedKind), DropScope.ITEM, _targetChannel,
			List.of(action));
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
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(DropArguments.SOURCE, _source.getID());
		arguments.put(DropArguments.KEYS, rowKey(sourceIndex));
		arguments.put(DropArguments.SELECTION, Boolean.FALSE);
		if (targetIndex >= 0) {
			arguments.put(DropArguments.TARGET_KEY, rowKey(targetIndex));
			arguments.put(DropArguments.POSITION, DropPosition.ONTO.wireName());
		} else {
			arguments.put(DropArguments.POSITION, DropPosition.NONE.wireName());
		}
		return target.executeClientCommand("drop", arguments);
	}

	/**
	 * The kinds the table accepts are those of all its drops, and its rows are targets as soon as one
	 * of them targets rows.
	 */
	public void testAcceptedKindsAndRowTargeting() {
		DropBinding both = new DropBinding(_context,
			List.of(tableDrop(ROW_KIND, _onTable), rowDrop(OTHER_KIND, _onRow)));

		assertEquals(kinds(ROW_KIND, OTHER_KIND), both.acceptedKinds());
		assertTrue("A drop targeting rows makes the rows targets.", both.dropOnRows());

		DropBinding onTableOnly =
			new DropBinding(_context, List.of(tableDrop(ROW_KIND, _onTable)));
		assertEquals(kinds(ROW_KIND), onTableOnly.acceptedKinds());
		assertFalse("Without a row drop the rows are no targets.", onTableOnly.dropOnRows());

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
			newTargetTable(List.of(tableDrop(ROW_KIND, _onTable), rowDrop(ROW_KIND, _onRow)));

		assertTrue(drop(target, 2, 1).isSuccess());

		assertTrue("The chain of the row drop must run.", _onRow._executed);
		assertFalse("The table drop must not run as well.", _onTable._executed);
		assertEquals(List.of(SOURCE_ROWS.get(2)), _onRow._input);
		assertEquals("The row dropped on is published before the chain runs.",
			TARGET_ROWS.get(1), _targetChannel.get());
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
		arguments.put(DropArguments.POSITION, DropPosition.NONE.wireName());
		assertTrue(target.executeClientCommand("drop", arguments).isSuccess());

		assertEquals(List.of(SOURCE_ROWS.get(0)), _onTable._input);
	}

	/** The reason the test rules refuse with. */
	private static final ResKey REASON = ResKey.text("Not here.");

	/**
	 * A drop whose table-wide state refuses is not announced - its kinds and its row targeting drop
	 * out - and is refused with the state's reason; it is offered again as soon as the state allows.
	 */
	public void testTableWideRefusalRemovesTheDrop() {
		ExecutableState[] state = { ExecutableState.createDisabledState(REASON) };
		DropBinding.Drop restricted = new DropBinding.Drop(kinds(ROW_KIND), DropScope.ITEM,
			_targetChannel, List.of(_onRow), () -> state[0], ViewExecutabilityRule.ALWAYS_EXECUTABLE, null);
		DropBinding binding = new DropBinding(_context, List.of(restricted, tableDrop(OTHER_KIND, _onTable)));

		assertEquals("A disabled drop contributes no kind.", kinds(OTHER_KIND), binding.acceptedKinds());
		assertFalse("A disabled row drop makes no row a target.", binding.dropOnRows());
		assertRefused(REASON, binding.check(event(TARGET_ROWS.get(0))));

		state[0] = ExecutableState.NOT_EXEC_HIDDEN;
		assertRefused(I18NConstants.ERROR_DROP_REFUSED, binding.check(event(TARGET_ROWS.get(0))));

		state[0] = ExecutableState.EXECUTABLE;
		assertEquals(kinds(ROW_KIND, OTHER_KIND), binding.acceptedKinds());
		assertTrue(binding.dropOnRows());
		assertTrue(binding.check(event(TARGET_ROWS.get(0))).isAccepted());
	}

	/**
	 * The target rule of a row drop refuses a single row with its reason, and a drop made there
	 * anyway runs nothing and publishes no target.
	 */
	public void testTargetRuleRefusesARow() {
		String refusedRow = TARGET_ROWS.get(1);
		ViewExecutabilityRule targetRule = row -> refusedRow.equals(row)
			? ExecutableState.createDisabledState(REASON) : ExecutableState.EXECUTABLE;
		DropBinding.Drop restricted = new DropBinding.Drop(kinds(ROW_KIND), DropScope.ITEM,
			_targetChannel, List.of(_onRow), () -> ExecutableState.EXECUTABLE, targetRule, null);
		DropBinding binding = new DropBinding(_context, List.of(restricted));
		TableViewControl<String> target = newTable(TARGET_ROWS);
		target.setDropTarget(binding);

		assertTrue(binding.check(event(TARGET_ROWS.get(0))).isAccepted());
		assertRefused(REASON, binding.check(event(refusedRow)));

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
	 * called with the target row and the dragged objects.
	 */
	public void testRefuseIf() {
		Object[] result = new Object[1];
		List<Object> seen = new ArrayList<>();
		BiFunction<Object, List<?>, Object> refuseIf = (target, objects) -> {
			seen.add(target);
			seen.add(objects);
			return result[0];
		};
		DropBinding binding = new DropBinding(_context, List.of(new DropBinding.Drop(
			kinds(ROW_KIND), DropScope.ITEM, _targetChannel, List.of(_onRow),
			() -> ExecutableState.EXECUTABLE, ViewExecutabilityRule.ALWAYS_EXECUTABLE, refuseIf)));

		result[0] = null;
		assertTrue("No value accepts.", binding.check(event(TARGET_ROWS.get(1))).isAccepted());
		assertEquals("The function gets the target row and the dragged objects.",
			List.of(TARGET_ROWS.get(1), List.of(SOURCE_ROWS.get(0))), seen);

		result[0] = Boolean.FALSE;
		assertTrue("False accepts.", binding.check(event(TARGET_ROWS.get(1))).isAccepted());

		result[0] = Boolean.TRUE;
		assertRefused(I18NConstants.ERROR_DROP_REFUSED, binding.check(event(TARGET_ROWS.get(1))));

		result[0] = "Not here.";
		assertRefused(REASON, binding.check(event(TARGET_ROWS.get(1))));
		assertNull("A check writes no target channel.", _targetChannel.get());
	}

	/**
	 * A drag no declared drop matches is refused with the generic reason.
	 */
	public void testNoMatchIsRefused() {
		DropBinding binding = new DropBinding(_context, List.of(tableDrop(OTHER_KIND, _onTable)));

		assertRefused(com.top_logic.layout.react.I18NConstants.ERROR_DROP_NOT_ACCEPTED, binding.check(event(null)));
	}

	/**
	 * Applying a drop passes over a disabled declared drop - to the next matching one, or to none.
	 */
	public void testOnDropSkipsDisabledDrops() {
		DropBinding.Drop disabled = new DropBinding.Drop(kinds(ROW_KIND), DropScope.CONTROL, null,
			List.of(_onRow), () -> ExecutableState.createDisabledState(REASON),
			ViewExecutabilityRule.ALWAYS_EXECUTABLE, null);

		new DropBinding(_context, List.of(disabled)).onDrop(event(null));
		assertFalse("A disabled drop applies nothing.", _onRow._executed);

		new DropBinding(_context, List.of(disabled, tableDrop(ROW_KIND, _onTable))).onDrop(event(null));
		assertFalse(_onRow._executed);
		assertTrue("The next matching drop applies.", _onTable._executed);
	}

	/**
	 * A drop of the source table's first row, on the given row of a target table, or on the table
	 * itself for {@code null}.
	 */
	private DropEvent event(String targetRow) {
		return new DropEvent(_source, _source.dragKind(), List.of(SOURCE_ROWS.get(0)), targetRow,
			targetRow == null ? DropPosition.NONE : DropPosition.ONTO);
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
