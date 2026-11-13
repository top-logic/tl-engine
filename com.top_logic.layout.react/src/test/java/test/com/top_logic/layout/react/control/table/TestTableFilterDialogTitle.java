/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.react.control.table.ColumnFilterUI;
import com.top_logic.layout.react.control.table.OpenFilterArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.WindowState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.FilterState;
import com.top_logic.table.SortSpec;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;

/**
 * Tests the title of the filter dialog of a {@link TableViewControl}: the {@link ColumnFilterUI#title()
 * title} of a column's own filter editor where it states one, the title of every filter dialog
 * otherwise.
 */
public class TestTableFilterDialogTitle extends TestCase {

	/** The name of the filtered column. */
	private static final String COLUMN = "name";

	/** The title a filter editor states. */
	private static final ResKey CUSTOM_TITLE = ResKey.text("Number range");

	/** A filter editor stating its own title gives the dialog that title. */
	public void testTitleOfTheFilterEditor() {
		assertEquals(Resources.getInstance().getString(CUSTOM_TITLE), dialogTitle(new Editor(CUSTOM_TITLE)));
	}

	/** A filter editor stating no title keeps the title of every filter dialog. */
	public void testDefaultTitleOfAFilterEditor() {
		assertEquals(Resources.getInstance().getString(I18NConstants.JS_TABLE_FILTER), dialogTitle(new Editor(null)));
	}

	/** A column without a filter editor of its own has the title of every filter dialog. */
	public void testDefaultTitleWithoutAFilterEditor() {
		assertEquals(Resources.getInstance().getString(I18NConstants.JS_TABLE_FILTER), dialogTitle(null));
	}

	/**
	 * The title of the filter dialog of the {@link #COLUMN} of a table using the given filter
	 * editor, {@code null} for the built-in one.
	 */
	private static Object dialogTitle(ColumnFilterUI editor) {
		Dialogs dialogs = new Dialogs();
		SSEUpdateQueue queue = new SSEUpdateQueue();
		queue.setDialogManager(dialogs);
		ReactContext context = new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));
		List<Column<String, ?>> columns = List.of(
			DefaultColumn.<String, String> builder(COLUMN, row -> row)
				.renderer(CellContent::text)
				.filter(TextColumnFilter.forStrings())
				.build());
		ListRowSource<String> rows = new ListRowSource<>(List.of("a", "b"), columns);
		TableViewControl<String> table = new TableViewControl<>(context,
			DefaultTableView.create(columns, rows, null, null, SortSpec.NONE, List.of(), List.of(), null), false);
		if (editor != null) {
			table.setFilterUI(COLUMN, editor);
		}

		table.executeCommand(TableViewControl.CMD_OPEN_FILTER, Map.of(OpenFilterArguments.COLUMN, COLUMN));

		ReactControl opened = dialogs.opened();
		assertTrue("The dialog is a window: " + opened, opened instanceof ReactWindowControl);
		return state(opened).get(WindowState.TITLE__PROP);
	}

	/** The state the given control sends to the client. */
	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/** A filter editor stating the given title, building no form. */
	private static class Editor implements ColumnFilterUI {

		private final ResKey _title;

		Editor(ResKey title) {
			_title = title;
		}

		@Override
		public ReactControl buildForm(ReactContext context) {
			return null;
		}

		@Override
		public FilterState read() {
			return null;
		}

		@Override
		public ResKey title() {
			return _title;
		}
	}

	/** Stands in for the dialogs of a window: holds the one that was opened. */
	private static class Dialogs implements DialogManager {

		private ReactControl _opened;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_opened = child;
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_opened = null;
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("No dialog is stacked on the filter dialog.");
		}

		ReactControl opened() {
			assertNotNull("The filter dialog is opened.", _opened);
			return _opened;
		}
	}

	/**
	 * Suite requiring the {@link TypeIndex} the commands are dispatched by, the resources the titles
	 * are resolved from, and the {@link ThemeFactory} the buttons of the dialog take their icons
	 * from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableFilterDialogTitle.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}

}
