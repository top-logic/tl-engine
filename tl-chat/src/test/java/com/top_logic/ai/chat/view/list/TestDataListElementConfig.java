/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view.list;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.TypedConfiguration;

/**
 * Checks the defaults of the {@code <data-list>} configuration: search and collapse on, no grouping
 * or selection until configured.
 */
public class TestDataListElementConfig extends TestCase {

	/** An unconfigured {@code <data-list>} has the list defaults. */
	public void testDefaults() {
		DataListElement.Config config = TypedConfiguration.newConfigItem(DataListElement.Config.class);

		assertTrue("Search on by default.", config.getSearch());
		assertTrue("Collapsible by default.", config.getCollapsible());
		assertNull("No grouping until a group-by expression is set.", config.getGroupBy());
		assertNull("No lines configured by default.", config.getLines());
		assertNull("No selection channel by default.", config.getSelection());
		assertNull("No title by default.", config.getTitle());
		assertFalse("No create button by default.", config.getCreate());
		assertTrue("No input channels by default.", config.getInputs().isEmpty());
		assertTrue("No types configured by default.", config.getTypes().isEmpty());
		assertTrue("A static list by default (no observed types).", config.getObservedTypes().isEmpty());
		assertEquals("TLDataList", config.getReactElement());
		assertEquals(DataListElement.class, config.getImplementationClass());
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestDataListElementConfig.class));
	}
}
