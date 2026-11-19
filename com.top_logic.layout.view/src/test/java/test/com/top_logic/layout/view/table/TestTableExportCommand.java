/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.office.excel.handler.POITypeProvider;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
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
import com.top_logic.layout.react.theme.RoleFills;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.table.TableExportCommand;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

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

	/** The token sheet of the design system, its light block first. */
	private static final String DESIGN_SYSTEM_TOKENS = "/style/tl-design-system/tokens.css";

	/** The primitives the token sheet of the design system refers to. */
	private static final String DESIGN_SYSTEM_PRIMITIVES = "/style/tl-design-system/primitives.css";

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

		command(1000).bind(() -> view, Map::of, () -> new Object[0]).execute(context(), null);

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
	 * Tests that the color roles of the table are written as the fills of the cells: a heading in the
	 * role of its column, a data cell in its own role, the other cells of its row in the role of the
	 * row, and a cell without a role unfilled. The fills are the light surfaces of the roles, as the
	 * token sheets of the design system define them, and cells of the same role share one style.
	 */
	public void testExportsRoleFills() throws IOException {
		List<Item> items = List.of(new Item("Item 0", 0), new Item("Item 1", 1), new Item("Item 2", 2));
		List<Column<Item, ?>> columns = new ArrayList<>();
		columns.add(new DelegatingColumn<>(DefaultColumn.<Item, String> builder("name", Item::name)
			.label(ResKey.text("Name"))
			.build()) {
			@Override
			public ValueColor headerRole() {
				return ValueColor.WARNING;
			}
		});
		columns.add(DefaultColumn.<Item, Integer> builder("count", Item::count)
			.label(ResKey.text("Count"))
			.color(item -> item.count() == 1 ? ValueColor.INFO : null)
			.build());
		DefaultTableView<Item> view = DefaultTableView.create(columns, new ListRowSource<>(items, columns));
		view.setRowColor(item -> item.count() > 0 ? ValueColor.SUCCESS : null);

		command(1000).bind(() -> view, Map::of, () -> new Object[0]).execute(context(), null);

		try (InputStream in = _window._delivered.get(0).getStream(); Workbook workbook = WorkbookFactory.create(in)) {
			Sheet sheet = workbook.getSheetAt(0);
			assertFill(lightSurface("status-warning-subtle"), sheet.getRow(0).getCell(0));
			assertFill(null, sheet.getRow(0).getCell(1));

			assertFill(null, sheet.getRow(1).getCell(0));
			assertFill(null, sheet.getRow(1).getCell(1));

			assertFill(lightSurface("status-success-subtle"), sheet.getRow(2).getCell(0));
			assertFill(lightSurface("status-info-subtle"), sheet.getRow(2).getCell(1));

			assertFill(lightSurface("status-success-subtle"), sheet.getRow(3).getCell(0));
			assertFill(lightSurface("status-success-subtle"), sheet.getRow(3).getCell(1));

			assertEquals("Cells of the same role share one style.", sheet.getRow(2).getCell(0).getCellStyle().getIndex(),
				sheet.getRow(3).getCell(0).getCellStyle().getIndex());
		}
		assertEquals(lightSurface("surface-brand-subtle"), RoleFills.fill(ValueColor.BRAND));
		assertEquals(lightSurface("category-3-subtle"), RoleFills.fill(ValueColor.CATEGORY_3));
		assertNull("The neutral role is drawn without a tint.", RoleFills.fill(ValueColor.NEUTRAL));
	}

	private static void assertFill(Color expected, Cell cell) {
		XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
		if (expected == null) {
			assertEquals(cell.getAddress().formatAsString(), FillPatternType.NO_FILL, style.getFillPattern());
			return;
		}
		assertEquals(cell.getAddress().formatAsString(), FillPatternType.SOLID_FOREGROUND, style.getFillPattern());
		XSSFColor fill = style.getFillForegroundColorColor();
		byte[] rgb = fill.getRGB();
		assertEquals(cell.getAddress().formatAsString(), expected,
			new Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
	}

	/**
	 * The value of the given color token in the light mode, as the token sheets generated from the
	 * token definitions of the design system declare it: the light block of
	 * {@value #DESIGN_SYSTEM_TOKENS} refers to a primitive of {@value #DESIGN_SYSTEM_PRIMITIVES}.
	 */
	private static Color lightSurface(String token) throws IOException {
		String reference = declaration(resource(DESIGN_SYSTEM_TOKENS), token);
		Matcher primitive = Pattern.compile("var\\(--tl-([a-z0-9-]+)\\)").matcher(reference);
		String value = primitive.matches() ? declaration(resource(DESIGN_SYSTEM_PRIMITIVES), primitive.group(1)) : reference;
		return Color.decode(value);
	}

	/** The value of the first declaration of the given custom property in the given sheet. */
	private static String declaration(String css, String token) {
		Matcher matcher = Pattern.compile("--tl-" + Pattern.quote(token) + ":\\s*([^;]+);").matcher(css);
		assertTrue("No token '" + token + "'.", matcher.find());
		return matcher.group(1).trim();
	}

	private static String resource(String name) throws IOException {
		try (InputStream in = FileManager.getInstance().getStream(name)) {
			return StreamUtilities.readAllFromStream(in, "utf-8");
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
