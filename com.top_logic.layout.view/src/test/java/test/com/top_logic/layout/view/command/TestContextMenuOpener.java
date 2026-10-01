/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.MenuRenderer;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.Targeted;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.state.MenuState.EntryType;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Test for {@link ContextMenuOpener}.
 */
public class TestContextMenuOpener extends TestCase {

	public void testOpenAssemblesMenuFromContributions() {
		AtomicReference<Object> rowTarget = new AtomicReference<>();
		AtomicReference<Object> cellTarget = new AtomicReference<>();

		CommandModel edit = FakeCommandModels.contextMenu("edit", "Edit", true, true);
		CommandModel delete = FakeCommandModels.contextMenu("delete", "Delete", true, true);
		CommandModel copy = FakeCommandModels.contextMenu("copy", "Copy Cell", true, true);

		ContextMenuContribution cellContribution =
			new ContextMenuContribution(cellTarget::set, List.of(copy));
		ContextMenuContribution rowContribution =
			new ContextMenuContribution(rowTarget::set, List.of(edit, delete));

		RecordingRenderer renderer = new RecordingRenderer();
		ContextMenuOpener opener = new ContextMenuOpener(renderer);

		opener.open(10, 20, List.of(
			new Targeted(cellContribution, "cellValue"),
			new Targeted(rowContribution, "rowValue")));

		assertEquals("cellValue", cellTarget.get());
		assertEquals("rowValue", rowTarget.get());

		List<MenuEntry> items = renderer.lastItems;
		assertEquals(4, items.size());
		assertEquals("0:0", items.get(0).id());
		assertEquals(EntryType.SEPARATOR, items.get(1).type());
		assertEquals("1:0", items.get(2).id());
		assertEquals("1:1", items.get(3).id());
		assertEquals(10, renderer.lastX);
		assertEquals(20, renderer.lastY);
	}

	public void testOpenSkipsEmptyContributionsAndDoesNotOpenIfAllEmpty() {
		AtomicReference<Object> t = new AtomicReference<>();
		CommandModel invisible = FakeCommandModels.contextMenu("x", "X", false, true);
		ContextMenuContribution contribution = new ContextMenuContribution(t::set, List.of(invisible));

		RecordingRenderer renderer = new RecordingRenderer();
		ContextMenuOpener opener = new ContextMenuOpener(renderer);

		opener.open(0, 0, List.of(new Targeted(contribution, "whatever")));

		assertFalse("Must not open an empty menu", renderer.opened);
	}

	public void testSelectDispatchesToCorrectCommand() {
		AtomicReference<Object> t0 = new AtomicReference<>();
		AtomicReference<Object> t1 = new AtomicReference<>();
		CountingCommandModel cmdA = new CountingCommandModel("edit");
		CountingCommandModel cmdB = new CountingCommandModel("edit");

		ContextMenuContribution c0 = new ContextMenuContribution(t0::set, List.of(cmdA));
		ContextMenuContribution c1 = new ContextMenuContribution(t1::set, List.of(cmdB));

		RecordingRenderer renderer = new RecordingRenderer();
		ContextMenuOpener opener = new ContextMenuOpener(renderer);

		opener.open(0, 0, List.of(new Targeted(c0, "obj0"), new Targeted(c1, "obj1")));
		renderer.selectHandler.apply("1:0");

		assertEquals(0, cmdA.invocations);
		assertEquals(1, cmdB.invocations);
	}

	/**
	 * The command whose effect is in force yields an entry marked as active, while the alternatives
	 * it is chosen among do not - and being the active one leaves the entry selectable, so that
	 * choosing it again still runs the command.
	 */
	public void testActiveCommandYieldsMarkedSelectableEntry() {
		AtomicReference<Object> target = new AtomicReference<>();
		CommandModel dark = FakeCommandModels.contextMenu("dark", "Dark", true, true, true);
		CommandModel light = FakeCommandModels.contextMenu("light", "Light", true, true, false);

		ContextMenuContribution themes =
			new ContextMenuContribution(target::set, List.of(dark, light));

		RecordingRenderer renderer = new RecordingRenderer();
		ContextMenuOpener opener = new ContextMenuOpener(renderer);

		opener.open(0, 0, List.of(new Targeted(themes, "anything")));

		List<MenuEntry> items = renderer.lastItems;
		assertEquals(2, items.size());
		assertTrue("The command in force must be marked.", items.get(0).active());
		assertFalse("An alternative not in force must not be marked.", items.get(1).active());
		assertFalse("The active entry stays selectable.", items.get(0).disabled());
	}

	/** Selecting the active entry runs its command again. */
	public void testActiveEntryIsStillDispatched() {
		AtomicReference<Object> target = new AtomicReference<>();
		CountingCommandModel active = new CountingCommandModel("dark", true);
		ContextMenuContribution themes = new ContextMenuContribution(target::set, List.of(active));

		RecordingRenderer renderer = new RecordingRenderer();
		ContextMenuOpener opener = new ContextMenuOpener(renderer);

		opener.open(0, 0, List.of(new Targeted(themes, "anything")));
		renderer.selectHandler.apply("0:0");

		assertEquals(1, active.invocations);
	}

	/**
	 * Selecting a command its rules refuse - whatever entry the client addresses, and however the
	 * menu displayed it - runs nothing, and the selection answers with the refusal, which carries
	 * the reason the rule gave.
	 */
	public void testSelectingARefusedCommandReportsTheRefusal() {
		ResKey reason = ResKey.text("Only for open tickets.");
		CountingCommandModel refused = new CountingCommandModel("close");
		RecordingRenderer renderer = openMenuFor(refused);

		refused._state = ExecutableState.createDisabledState(reason);
		HandlerResult result = renderer.selectHandler.apply("0:0");

		assertEquals("A refused command must not run.", 0, refused.invocations);
		assertFalse("The refusal is reported.", result.isSuccess());
		assertEquals("A refusal is no malfunction.", ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals("The user learns why.", reason, result.getErrorMessage());
		assertFalse("The menu closes nonetheless.", renderer.opened);
	}

	/** A refused command's entry is displayed disabled, and carries the state with the reason. */
	public void testTheEntryOfARefusedCommandCarriesItsState() {
		ExecutableState state = ExecutableState.createDisabledState(ResKey.text("Only for open tickets."));
		CountingCommandModel refused = new CountingCommandModel("close");
		refused._state = state;

		RecordingRenderer renderer = openMenuFor(refused);

		MenuEntry entry = renderer.lastItems.get(0);
		assertTrue(entry.disabled());
		assertSame(state, entry.state());
	}

	/**
	 * What a selected command reports is the result of the selection, a failure of its own
	 * included - it is not dropped on the way.
	 */
	public void testTheResultOfTheSelectedCommandIsReturned() {
		CountingCommandModel failing = new CountingCommandModel("save");
		failing._result = HandlerResult.error(ResKey.text("Storage is full."));
		RecordingRenderer renderer = openMenuFor(failing);

		HandlerResult result = renderer.selectHandler.apply("0:0");

		assertEquals(1, failing.invocations);
		assertSame("The command's own result reaches the caller.", failing._result, result);
	}

	/** An executable command runs, and its success is the result of the selection. */
	public void testAnExecutableCommandRuns() {
		CountingCommandModel command = new CountingCommandModel("edit");
		RecordingRenderer renderer = openMenuFor(command);

		HandlerResult result = renderer.selectHandler.apply("0:0");

		assertEquals(1, command.invocations);
		assertTrue(result.isSuccess());
	}

	/**
	 * A menu opened at an element is shown there, and closing it without a selection tells the one
	 * who opened it - once.
	 */
	public void testOpenAtAnchorReportsTheClose() {
		AtomicInteger closed = new AtomicInteger();
		RecordingRenderer renderer = new RecordingRenderer();
		ContextMenuOpener opener = new ContextMenuOpener(renderer);

		boolean shown = opener.open("anchor-1", contributionsFor(new CountingCommandModel("edit")),
			closed::incrementAndGet);

		assertTrue("A menu with entries is shown.", shown);
		assertEquals("anchor-1", renderer.anchorId);
		assertEquals("Nothing is closed yet.", 0, closed.get());

		renderer.closeHandler.run();
		assertEquals(1, closed.get());

		renderer.closeHandler.run();
		assertEquals("The close is reported once.", 1, closed.get());
	}

	/** A selection closes the menu, and the one who opened it learns that, too. */
	public void testSelectionReportsTheClose() {
		AtomicInteger closed = new AtomicInteger();
		CountingCommandModel command = new CountingCommandModel("edit");
		RecordingRenderer renderer = new RecordingRenderer();
		ContextMenuOpener opener = new ContextMenuOpener(renderer);

		opener.open("anchor-1", contributionsFor(command), closed::incrementAndGet);
		renderer.selectHandler.apply("0:0");

		assertEquals(1, command.invocations);
		assertEquals(1, closed.get());
	}

	/**
	 * A menu opened elsewhere replaces the open one, whose opener learns that its menu is closed -
	 * but only once the new menu is actually shown: an opening that offers nothing leaves the open
	 * menu standing.
	 */
	public void testReplacingTheMenuReportsTheCloseOfTheOldOne() {
		AtomicInteger closedFirst = new AtomicInteger();
		AtomicInteger closedSecond = new AtomicInteger();
		RecordingRenderer renderer = new RecordingRenderer();
		ContextMenuOpener opener = new ContextMenuOpener(renderer);

		opener.open("anchor-1", contributionsFor(new CountingCommandModel("edit")), closedFirst::incrementAndGet);

		CommandModel invisible = FakeCommandModels.contextMenu("x", "X", false, true);
		boolean shown = opener.open("anchor-2", contributionsFor(invisible), closedSecond::incrementAndGet);
		assertFalse("A menu without entries is not shown.", shown);
		assertEquals("The open menu stays.", "anchor-1", renderer.anchorId);
		assertEquals(0, closedFirst.get());

		opener.open(5, 7, contributionsFor(new CountingCommandModel("copy")), closedSecond::incrementAndGet);
		assertEquals("The replaced menu is reported closed.", 1, closedFirst.get());
		assertEquals(0, closedSecond.get());

		renderer.closeHandler.run();
		assertEquals("The replaced menu's close is not reported again.", 1, closedFirst.get());
		assertEquals(1, closedSecond.get());
	}

	/** The contributions of a menu offering the given command alone. */
	private static List<Targeted> contributionsFor(CommandModel command) {
		AtomicReference<Object> target = new AtomicReference<>();
		ContextMenuContribution contribution = new ContextMenuContribution(target::set, List.of(command));
		return List.of(new Targeted(contribution, "anything"));
	}

	/** Opens a menu offering the given command alone. */
	private static RecordingRenderer openMenuFor(CommandModel command) {
		AtomicReference<Object> target = new AtomicReference<>();
		ContextMenuContribution contribution = new ContextMenuContribution(target::set, List.of(command));
		RecordingRenderer renderer = new RecordingRenderer();
		new ContextMenuOpener(renderer).open(0, 0, List.of(new Targeted(contribution, "anything")));
		return renderer;
	}

	static final class RecordingRenderer implements MenuRenderer {
		List<MenuEntry> lastItems;

		int lastX;

		int lastY;

		String anchorId;

		boolean opened;

		Function<String, HandlerResult> selectHandler;

		Runnable closeHandler;

		@Override
		public void show(int x, int y, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
				Runnable closeHandler) {
			this.opened = true;
			this.lastX = x;
			this.lastY = y;
			this.anchorId = null;
			this.lastItems = items;
			this.selectHandler = selectHandler;
			this.closeHandler = closeHandler;
		}

		@Override
		public void show(String anchor, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
				Runnable closeHandler) {
			this.opened = true;
			this.anchorId = anchor;
			this.lastItems = items;
			this.selectHandler = selectHandler;
			this.closeHandler = closeHandler;
		}

		@Override
		public void hide() {
			this.opened = false;
		}
	}

	static final class CountingCommandModel extends FakeCommandModelBase {
		int invocations;

		private final boolean _active;

		/** The state the command's rules assign, see {@link #getExecutableState()}. */
		ExecutableState _state = ExecutableState.EXECUTABLE;

		/** What the command reports when it runs. */
		HandlerResult _result = HandlerResult.DEFAULT_RESULT;

		CountingCommandModel(String name) {
			this(name, false);
		}

		CountingCommandModel(String name, boolean active) {
			super(name);
			_active = active;
		}

		@Override
		public boolean isActive() {
			return _active;
		}

		@Override
		public boolean isExecutable() {
			return _state.isExecutable();
		}

		@Override
		public boolean isVisible() {
			return _state.isVisible();
		}

		@Override
		public ExecutableState getExecutableState() {
			return _state;
		}

		@Override
		public HandlerResult perform(ReactContext ctx) {
			invocations++;
			return _result;
		}
	}
}
