/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;

/**
 * Tests the {@link ReactPanelControl#FILL} state of a {@link RowSetTableControl}: a framed table is
 * sized by its rows, a frameless one fills the height its container offers.
 */
public class TestRowSetTableFill extends AbstractModelAccessTest {

	/**
	 * A table as created is framed and therefore content-sized; switching it to frameless lets it
	 * fill, switching it back makes it content-sized again.
	 */
	public void testFillFollowsFramed() {
		becomeUser(_responsible);
		ReactContext reactContext =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		FormControl form = new FormControl(reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(reactContext), form,
			new AttributeRowSetBinding(STEPS), List.of(), RowEditPolicy.ALL);

		assertEquals("A framed table is content-sized.", Boolean.FALSE, fill(table));

		table.setFramed(false);
		assertEquals("A frameless table fills.", Boolean.TRUE, fill(table));

		table.setFramed(true);
		assertEquals("A framed table is content-sized.", Boolean.FALSE, fill(table));
	}

	private static Object fill(RowSetTableControl table) {
		return table.scriptingScalarState().get(ReactPanelControl.FILL);
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its error icon.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetTableFill.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE);
	}

}
