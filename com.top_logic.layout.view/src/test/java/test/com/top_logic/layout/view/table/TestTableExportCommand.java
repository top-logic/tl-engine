/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.office.excel.handler.POITypeProvider;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.table.TableExportCommand;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests {@link TableExportCommand}: what the toolbar command of a {@code <table>} hands to the
 * browser.
 */
public class TestTableExportCommand extends BasicTestCase {

	private record Item(String name, int count) {
		// Test fixture.
	}

	/**
	 * A window queue remembering the files delivered to it, without a browser connected.
	 */
	private static final class Window extends SSEUpdateQueue {

		final List<BinaryData> _delivered = Collections.synchronizedList(new ArrayList<>());

		final CountDownLatch _deliveredOne = new CountDownLatch(1);

		@Override
		public String deliverDownload(BinaryData data, Runnable discard) {
			_delivered.add(data);
			_deliveredOne.countDown();
			return super.deliverDownload(data, discard);
		}
	}

	/**
	 * Dialogs of a window, remembering which are open.
	 */
	private static final class Dialogs implements DialogManager {

		final List<ReactControl> _open = Collections.synchronizedList(new ArrayList<>());

		final CountDownLatch _closed = new CountDownLatch(1);

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child, DialogResultHandler<Void> handler) {
			_open.add(child);
			return new DialogHandle() {
				@Override
				public void close(DialogResult<Void> result) {
					if (_open.remove(child)) {
						handler.onResult(result);
						_closed.countDown();
					}
				}

				@Override
				public void setClosable(boolean closable) {
					// All dialogs are closable here.
				}

				@Override
				public boolean isClosable() {
					return true;
				}
			};
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException();
		}
	}

	private final Window _window = new Window();

	private final Dialogs _dialogs = new Dialogs();

	private ReactContext context() {
		_window.setDialogManager(_dialogs);
		return new DefaultReactContext("", "test", _window, new ReactWindowRegistry("test"));
	}

	private static DefaultTableView<Item> view(int rows) {
		List<Item> items = new ArrayList<>();
		for (int n = 0; n < rows; n++) {
			items.add(new Item("Item " + n, n));
		}
		List<Column<Item, ?>> columns = new ArrayList<>();
		columns.add(DefaultColumn.<Item, Item> builder("open", item -> item)
			.renderer(item -> CellContent.text(">"))
			.selectable(false)
			.build());
		columns.add(DefaultColumn.<Item, String> builder("name", Item::name)
			.label(ResKey.text("Name"))
			.build());
		columns.add(DefaultColumn.<Item, Integer> builder("count", Item::count)
			.label(ResKey.text("Count"))
			.sort(() -> Comparator.naturalOrder())
			.build());
		return DefaultTableView.create(columns, new ListRowSource<>(items, columns));
	}

	private static TableExportCommand command(int progressThreshold) {
		TableExportCommand.Config config = TypedConfiguration.newConfigItem(TableExportCommand.Config.class);
		config.setProgressThreshold(progressThreshold);
		return new TableExportCommand(config);
	}

	/**
	 * Tests that the export configured as a command of its own - among the commands of a panel,
	 * say, where it has no table to export - is refused when the configuration is read.
	 */
	public void testFreeStandingCommandIsConfigurationError() {
		TableExportCommand.Config config = TypedConfiguration.newConfigItem(TableExportCommand.Config.class);
		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);

		context.getInstance(config);

		assertEquals("Configured as a command of its own, the export is a configuration error.", 1,
			log.getErrors().size());
		assertTrue(log.getErrors().get(0), log.getErrors().get(0).contains("<export> of a <table>"));
	}

	/**
	 * Tests that a small table is exported right away: the browser receives the file holding the
	 * displayed data columns and rows, sorted as the table is.
	 */
	public void testExportsRightAway() throws IOException {
		DefaultTableView<Item> view = view(3);
		view.sort(new SortSpec(List.of(new SortColumn("count", false))));

		HandlerResult result =
			command(1000).bind(() -> view, Map::of, () -> new Object[0]).execute(context(), null);

		assertTrue(result.isSuccess());
		assertTrue("No dialog for a small table.", _dialogs._open.isEmpty());
		assertEquals(1, _window._delivered.size());
		BinaryData file = _window._delivered.get(0);
		assertTrue(file.getName(), file.getName().endsWith(".xlsx"));
		try (InputStream in = file.getStream(); Workbook workbook = WorkbookFactory.create(in)) {
			Sheet sheet = workbook.getSheetAt(0);
			assertEquals("Name", sheet.getRow(0).getCell(0).getStringCellValue());
			assertEquals("Count", sheet.getRow(0).getCell(1).getStringCellValue());
			assertEquals(2, sheet.getRow(0).getLastCellNum());
			assertEquals("Item 2", sheet.getRow(1).getCell(0).getStringCellValue());
			assertEquals("Item 0", sheet.getRow(3).getCell(0).getStringCellValue());
		}
	}

	/**
	 * Tests that a table of the progress threshold or more rows is exported in the background: a
	 * dialog shows the progress, closes when the file is written, and the browser receives it.
	 */
	public void testExportsInBackground() throws Exception {
		DefaultTableView<Item> view = view(50);

		command(10).bind(() -> view, Map::of, () -> new Object[0]).execute(context(), null);

		assertTrue("The file must be delivered.", _window._deliveredOne.await(20, TimeUnit.SECONDS));
		assertTrue("The dialog must close.", _dialogs._closed.await(20, TimeUnit.SECONDS));
		assertTrue(_dialogs._open.isEmpty());
		try (InputStream in = _window._delivered.get(0).getStream(); Workbook workbook = WorkbookFactory.create(in)) {
			assertEquals(51, workbook.getSheetAt(0).getPhysicalNumberOfRows());
		}
	}

	/**
	 * The suite with the services an export needs, and the scheduler a background export runs on.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestTableExportCommand.class,
				LabelProviderService.Module.INSTANCE,
				POITypeProvider.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}

}
