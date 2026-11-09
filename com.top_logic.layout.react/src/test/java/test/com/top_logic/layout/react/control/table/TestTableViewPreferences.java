/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.control.table.TableViewPreferences;

/**
 * Tests {@link TableViewPreferences}: the per-person table view - density, stripes and column
 * lines - and the start values written onto the root element of the page.
 */
public class TestTableViewPreferences extends TestCase {

	/**
	 * Only the two densities of the design system count; anything else is no choice.
	 */
	public void testDensityOf() {
		assertEquals("compact", TableViewPreferences.densityOf("compact"));
		assertEquals("normal", TableViewPreferences.densityOf("normal"));
		assertEquals("", TableViewPreferences.densityOf("huge"));
		assertEquals("", TableViewPreferences.densityOf(null));
		assertEquals("", TableViewPreferences.densityOf(Integer.valueOf(1)));
	}

	/**
	 * Without a user context there is nothing to store the view in: the factory values apply and
	 * storing changes nothing.
	 */
	public void testNoContext() {
		assertEquals("", TableViewPreferences.density());
		assertFalse(TableViewPreferences.striped());
		assertFalse(TableViewPreferences.columnLines());

		TableViewPreferences.store("compact", true, true);

		assertEquals("", TableViewPreferences.density());
		assertFalse(TableViewPreferences.striped());
		assertFalse(TableViewPreferences.columnLines());
	}

	/**
	 * What is stored is read back, and a person who stored nothing has the factory values.
	 */
	public void testStoreAndRead() {
		ThreadContext.inSystemContext(TestTableViewPreferences.class, () -> {
			assertEquals("", TableViewPreferences.density());
			assertFalse(TableViewPreferences.striped());
			assertFalse(TableViewPreferences.columnLines());

			TableViewPreferences.store("compact", true, false);

			assertEquals("compact", TableViewPreferences.density());
			assertTrue(TableViewPreferences.striped());
			assertFalse(TableViewPreferences.columnLines());

			TableViewPreferences.store("", false, true);

			assertEquals("", TableViewPreferences.density());
			assertFalse(TableViewPreferences.striped());
			assertTrue(TableViewPreferences.columnLines());
		});
	}

	/**
	 * The start values on the root element: the mark is always there, the others only where they
	 * deviate from the factory values.
	 */
	public void testAttributes() {
		assertEquals("<html data-tl-table-view=\"\">", startTag("", false, false));
		assertEquals("<html data-tl-table-view=\"\" data-tl-table-density=\"normal\">",
			startTag("normal", false, false));
		assertEquals(
			"<html data-tl-table-view=\"\" data-tl-table-density=\"compact\" data-tl-table-striped=\"\""
				+ " data-tl-table-column-lines=\"\">",
			startTag("compact", true, true));
		assertEquals("<html data-tl-table-view=\"\" data-tl-table-column-lines=\"\">",
			startTag("", false, true));
	}

	/**
	 * Without a user context the start tag carries the mark only.
	 */
	public void testAttributesWithoutContext() {
		TagWriter out = new TagWriter();
		out.beginBeginTag("html");
		TableViewPreferences.writeAttributes(out);
		out.endBeginTag();
		assertEquals("<html data-tl-table-view=\"\">", out.toString());
	}

	private static String startTag(String density, boolean striped, boolean columnLines) {
		TagWriter out = new TagWriter();
		out.beginBeginTag("html");
		TableViewPreferences.writeAttributes(out, density, striped, columnLines);
		out.endBeginTag();
		return out.toString();
	}

	/** Suite of tests. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableViewPreferences.class, ThreadContextManager.Module.INSTANCE));
	}

}
