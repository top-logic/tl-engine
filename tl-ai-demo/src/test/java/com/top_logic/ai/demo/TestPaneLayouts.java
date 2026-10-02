/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.chat.view.PaneLayouts;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * What the chat's generic pane shows of this demo's events, which no view configures (#616).
 *
 * <p>
 * {@code demo.events:Event} is the type the pane has to get right without help: it names no main
 * properties, inherits its name, holds four compositions and a long description. A form that ended
 * with the event's name, or a table whose columns were its compositions printed as text, is what the
 * pane showed before these rules.
 * </p>
 */
public class TestPaneLayouts extends BasicTestCase {

	/** The inherited name leads; the event's own attributes follow. */
	public void testInheritedAttributesComeFirst() {
		List<String> names = names(PaneLayouts.ordered(event()));

		assertEquals("name", names.get(0));
		assertTrue(names.toString(), names.indexOf("description") < names.indexOf("startDate"));
		assertTrue("Every attribute is there, once: " + names, names.contains("balance"));
		assertEquals(names.size(), names.stream().distinct().count());
	}

	/** A composition is told apart, because the form shows it as a table of its own. */
	public void testCompositionsAreRecognised() {
		for (TLStructuredTypePart part : PaneLayouts.ordered(event())) {
			boolean expected = List.of("sessions", "registrations", "sponsorships", "invoices", "attachments")
				.contains(part.getName());
			if (expected) {
				assertTrue(part.getName(), PaneLayouts.isComposition(part));
			}
		}
		assertFalse(PaneLayouts.isComposition(event().getPart("venue")));
	}

	/**
	 * A table of events chooses columns that scan: the name first, no composition, nothing
	 * multi-valued, no long text - the rest is the column selection's.
	 */
	public void testATableChoosesColumnsThatScan() {
		List<String> columns = PaneLayouts.chosenColumns(event());

		assertEquals("name", columns.get(0));
		assertEquals(PaneLayouts.MAX_CHOSEN_COLUMNS, columns.size());
		assertFalse("A description is read, not scanned: " + columns, columns.contains("description"));
		assertFalse(columns.toString(), columns.contains("sessions"));
		assertFalse(columns.toString(), columns.contains("attachments"));
		assertTrue(columns.toString(), columns.contains("startDate"));
	}

	private static TLStructuredType event() {
		return (TLStructuredType) TLModelUtil.findType("demo.events:Event");
	}

	private static List<String> names(List<TLStructuredTypePart> parts) {
		List<String> result = new ArrayList<>();
		for (TLStructuredTypePart part : parts) {
			result.add(part.getName());
		}
		return result;
	}

	/**
	 * The demo's model, in a knowledge base of its own.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestPaneLayouts.class), ModelService.Module.INSTANCE));
	}
}
