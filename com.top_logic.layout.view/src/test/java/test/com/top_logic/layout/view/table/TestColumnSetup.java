/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.table.ColumnBinding;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.table.Column;

/**
 * Test for {@link ColumnSetup}: the column it builds is displayed in the width it configures, and
 * keeps the width of the column its {@link ColumnBinding} built when it configures none.
 */
public class TestColumnSetup extends TestCase {

	/** The name of the column under test. */
	private static final String ATTRIBUTE = "name";

	/** The header label of the column under test. */
	private static final ResKey LABEL = ResKey.text("Name");

	/** The width a column has that configures none. */
	private static final int DEFAULT_WIDTH = 150;

	public void testConfiguredWidth() {
		Column<Object, ?> column = setup(220).buildColumn();

		assertEquals("The column is displayed in the configured width.", 220, column.defaultWidth());
		assertEquals("Everything but the width comes from the binding's column.", ATTRIBUTE, column.name());
		assertEquals(LABEL, column.label());
		assertTrue("The type-derived column is filterable.", column.filter().isPresent());
		assertTrue("The type-derived column is sortable.", column.sort().isPresent());
	}

	public void testWidthlessColumnKeepsTheBindingsWidth() {
		Column<Object, ?> column = setup(0).buildColumn();

		assertEquals("Without a configured width, the column keeps its own.", DEFAULT_WIDTH,
			column.defaultWidth());
		assertEquals(ATTRIBUTE, column.name());
	}

	/**
	 * A type-derived setup for the given width, over an unresolved attribute - which yields the
	 * label column every attribute falls back to.
	 */
	private static ColumnSetup setup(int width) {
		return new ColumnSetup(ATTRIBUTE, LABEL, null, null, ColumnBinding.TYPE_DERIVED, width);
	}

	/**
	 * Test suite requiring the {@link ColumnProviderService}, which the type-derived
	 * {@link ColumnBinding} builds its columns through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnSetup.class, ColumnProviderService.Module.INSTANCE));
	}

}
