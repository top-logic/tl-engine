/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import junit.framework.TestCase;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.MenuRenderer;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.Targeted;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.tool.boundsec.HandlerResult;

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
		assertEquals("separator", items.get(1).type());
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
		renderer.selectHandler.accept("1:0");

		assertEquals(0, cmdA.invocations);
		assertEquals(1, cmdB.invocations);
	}

	static final class RecordingRenderer implements MenuRenderer {
		List<MenuEntry> lastItems;

		int lastX;

		int lastY;

		boolean opened;

		Consumer<String> selectHandler;

		Runnable closeHandler;

		@Override
		public void show(int x, int y, List<MenuEntry> items, Consumer<String> selectHandler, Runnable closeHandler) {
			this.opened = true;
			this.lastX = x;
			this.lastY = y;
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

		CountingCommandModel(String name) {
			super(name);
		}

		@Override
		public HandlerResult executeCommand(ReactContext ctx) {
			invocations++;
			return HandlerResult.DEFAULT_RESULT;
		}
	}
}
