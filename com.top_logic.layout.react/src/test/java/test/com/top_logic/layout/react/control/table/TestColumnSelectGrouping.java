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

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.GroupByArguments;
import com.top_logic.layout.react.control.table.ReactColumnSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.ColumnOption;

/**
 * Test for the grouping offered by the {@link ReactColumnSelectControl}: a table offers to group
 * its rows by a column, a tree table does not.
 */
@SuppressWarnings("javadoc")
public class TestColumnSelectGrouping extends TestCase {

	private static final String COLUMN = "status";

	public void testATableOffersGrouping() {
		ReactColumnSelectControl select = select(true);
		assertEquals(Boolean.TRUE, select.scriptingScalarState().get("groupable"));

		groupBy(select);
		assertEquals(COLUMN, select.groupedColumn());
	}

	public void testATreeOffersNoGrouping() {
		ReactColumnSelectControl select = select(false);
		assertEquals(Boolean.FALSE, select.scriptingScalarState().get("groupable"));

		groupBy(select);
		assertNull("A tree cannot be grouped.", select.groupedColumn());
	}

	private static ReactColumnSelectControl select(boolean groupable) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		List<ColumnOption> options = List.of(new ColumnOption(COLUMN, ResKey.text("Status"), true));
		return new ReactColumnSelectControl(context, options, null, groupable);
	}

	private static void groupBy(ReactColumnSelectControl select) {
		select.executeClientCommand("groupBy", Map.of(GroupByArguments.COLUMN, COLUMN));
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnSelectGrouping.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
