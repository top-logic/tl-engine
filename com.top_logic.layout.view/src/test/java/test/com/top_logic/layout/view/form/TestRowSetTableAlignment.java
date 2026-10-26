/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
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
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;

/**
 * Tests that a {@link RowSetTableControl} keeps the {@link com.top_logic.table.Column#alignment()
 * alignment} of its columns when it adapts them to its rows: a column of numbers is told to the
 * client as ending at the end of its cells, a text column is not.
 */
public class TestRowSetTableAlignment extends AbstractModelAccessTest {

	/** Name of the numeric attribute of a task. */
	private static final String NUMBER = "number";

	/** Matches the state of the column with the given name, which holds no nested objects. */
	private static Pattern columnState(String name) {
		return Pattern.compile("\\{[^{}]*\"name\":\"" + name + "\"[^{}]*\\}");
	}

	/** A number column of a row-set table ends at the end of its cells, a text column does not. */
	public void testNumbersAlignToTheEnd() {
		becomeUser(_responsible);
		ReactContext reactContext =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		FormControl form = new FormControl(reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(reactContext), form,
			new AttributeRowSetBinding(TASKS), List.of(), RowEditPolicy.ALL);
		table.init();

		String json = table.getTableControl().stateAsJSON();

		assertTrue(json, json.contains("\"columns\""));
		Matcher number = columnState(NUMBER).matcher(json);
		assertTrue("The number column is displayed: " + json, number.find());
		assertTrue(number.group(), number.group().contains("\"align\":\"end\""));
		Matcher name = columnState(NAME).matcher(json);
		assertTrue("The name column is displayed: " + json, name.find());
		assertFalse(name.group(), name.group().contains("\"align\""));
	}

	/**
	 * The column a row-set table adapts to its rows keeps the place at the end and the CSS class
	 * its original column asks for: an action column must stay pinned, and keep dropping the padding
	 * of a text cell.
	 */
	public void testAdaptedColumnKeepsPinningAndCssClass() throws ReflectiveOperationException {
		becomeUser(_responsible);
		ReactContext reactContext =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		FormControl form = new FormControl(reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(reactContext), form,
			new AttributeRowSetBinding(TASKS), List.of(), RowEditPolicy.ALL);

		Column<Object, Object> inner = new Column<>() {
			@Override
			public String name() {
				return "action";
			}

			@Override
			public ResKey label() {
				return ResKey.text("action");
			}

			@Override
			public Object value(Object row) {
				return null;
			}

			@Override
			public CellRenderer<Object> renderer() {
				return null;
			}

			@Override
			public boolean pinnedEnd() {
				return true;
			}

			@Override
			public String cssClass() {
				return "tl-test-class";
			}
		};
		Class<?> adapter = Class.forName(RowSetTableControl.class.getName() + "$EditAwareColumn");
		Constructor<?> constructor = adapter.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		Column<?, ?> adapted = (Column<?, ?>) constructor.newInstance(table, inner, null, false);

		assertTrue(adapted.pinnedEnd());
		assertEquals("tl-test-class", adapted.cssClass());
	}

	/** The test suite, starting the theme, the resources and the services the table builds its columns and cells through. */
	public static Test suite() {
		return suiteWith(TestRowSetTableAlignment.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE);
	}

}
