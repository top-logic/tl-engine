/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.table.ColumnView;
import com.top_logic.util.TLContextManager;

/**
 * Tests the widths of the columns a {@link RowSetTableControl} builds: in edit mode the
 * {@link RowSetTableControl#COLUMN_DELETE row-removal column} is pinned to the end of the table and
 * keeps its width, so that the last data column is the one growing into the room the columns leave.
 */
public class TestRowSetTableColumnWidths extends AbstractModelAccessTest {

	/** Name of the task attribute displayed in the last data column. */
	private static final String NOTE = "note";

	/** Declared width of the {@link #NAME} column. */
	private static final int NAME_WIDTH = 150;

	/** Declared width of the {@link #NOTE} column. */
	private static final int NOTE_WIDTH = 300;

	private FormControl _form;

	private RowSetTableControl _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ReactContext reactContext =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		TLContextManager.inPersonContext(_responsible, () -> {
			_form = new FormControl(reactContext, _project, "no model", NoTokenHandling.INSTANCE);
			_table = new RowSetTableControl(new DefaultViewContext(reactContext), _form,
				new AttributeRowSetBinding(TASKS), List.of(column(NAME, NAME_WIDTH), column(NOTE, NOTE_WIDTH)),
				RowEditPolicy.ALL);
			_table.init();
		});
	}

	@Override
	protected Person initialUser() {
		return _responsible;
	}

	/**
	 * In view mode the table shows the data columns only.
	 */
	public void testViewModeHasNoDeleteColumn() {
		assertEquals(List.of(NAME, NOTE), names(columns()));
	}

	/**
	 * In edit mode the row-removal column trails the data columns, pinned and with its own width;
	 * the last data column is the last unpinned one - the column the client lets grow - and keeps
	 * its declared width.
	 */
	public void testDeleteColumnIsPinnedInEditMode() {
		assertTrue(_form.enterEditMode());

		List<ColumnView> columns = columns();
		assertEquals(List.of(NAME, NOTE, RowSetTableControl.COLUMN_DELETE), names(columns));

		ColumnView delete = columns.get(2);
		assertTrue("The row-removal column is pinned to the end.", delete.pinnedEnd());
		assertEquals(RowSetTableControl.ACTION_COLUMN_WIDTH, delete.width());

		ColumnView note = columns.get(1);
		assertSame("The last data column is the one that grows.", note, lastUnpinned(columns));
		assertEquals(NOTE_WIDTH, note.width());
		assertEquals(NAME_WIDTH, columns.get(0).width());
	}

	private List<ColumnView> columns() {
		return _table.getTableControl().getView().columns();
	}

	private static ColumnView lastUnpinned(List<ColumnView> columns) {
		ColumnView result = null;
		for (ColumnView column : columns) {
			if (!column.pinnedEnd()) {
				result = column;
			}
		}
		return result;
	}

	private static List<String> names(List<ColumnView> columns) {
		List<String> result = new ArrayList<>();
		for (ColumnView column : columns) {
			result.add(column.name());
		}
		return result;
	}

	/** A {@code <column>} declaration for the given attribute with the given width. */
	private static ColumnDeclaration column(String attribute, int width) {
		AttributeColumn.Config config = TypedConfiguration.newConfigItem(AttributeColumn.Config.class);
		set(config, AttributeColumn.Config.ATTRIBUTE, attribute);
		set(config, AbstractColumnDeclaration.Config.WIDTH, width);
		return (ColumnDeclaration) new DefaultInstantiationContext(TestRowSetTableColumnWidths.class)
			.getInstance(config);
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, and the
	 * services building its data columns and their cells.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetTableColumnWidths.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE);
	}

}
