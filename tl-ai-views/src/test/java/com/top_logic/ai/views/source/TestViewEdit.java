/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.source;

import junit.framework.TestCase;

/**
 * An edit of a hand-written view changes one element and keeps every other character.
 */
public class TestViewEdit extends TestCase {

	private static final String VIEW = """
		<?xml version="1.0" encoding="utf-8" ?>
		<!-- A hand-written view. -->
		<view>
			<!-- The channel the table writes. -->
			<channels>
				<channel name="selected"/>
			</channels>
			<table selection="selected"   types="a:B">
				<columns>
					<column attribute="name"/>
					<column attribute="start"/>
				</columns>
			</table>
		</view>
		""";

	/** Setting an attribute changes its value and nothing else, the odd spacing included. */
	public void testSetChangesOnlyTheValue() {
		String result = new ViewEdit.Set("/view/table", "selection", "other").apply(VIEW);

		assertEquals(VIEW.replace("selection=\"selected\"", "selection=\"other\""), result);
	}

	/** A new attribute goes behind the last one, an empty value removes it. */
	public void testSetAddsAndRemoves() {
		String added = new ViewEdit.Set("/view/table/columns/column[2]", "sort", "ASC").apply(VIEW);
		assertEquals(VIEW.replace("<column attribute=\"start\"/>", "<column attribute=\"start\" sort=\"ASC\"/>"), added);

		String removed = new ViewEdit.Set("/view/table", "types", null).apply(VIEW);
		assertEquals(VIEW.replace("   types=\"a:B\"", ""), removed);
	}

	/** An element inserted into another goes last, on its own line, indented like its siblings. */
	public void testInsertIntoIndentsLikeTheSiblings() {
		String result = new ViewEdit.Insert("/view/table/columns", "<column attribute=\"end\"/>",
			ViewEdit.Position.INTO).apply(VIEW);

		assertEquals(VIEW.replace("\t\t\t<column attribute=\"start\"/>\n",
			"\t\t\t<column attribute=\"start\"/>\n\t\t\t<column attribute=\"end\"/>\n"), result);
	}

	/** Inserting into an empty-element tag opens it. */
	public void testInsertIntoAnEmptyElementOpensIt() {
		String result = new ViewEdit.Insert("/view/channels/channel", "<x/>", ViewEdit.Position.INTO).apply(VIEW);

		assertTrue(result, result.contains("\t\t<channel name=\"selected\">\n\t\t\t<x/>\n\t\t</channel>\n"));
	}

	/** Multi-line XML keeps its own nesting, shifted to where it goes. */
	public void testReplaceShiftsMultiLineXml() {
		String result = new ViewEdit.Replace("/view/table/columns/column[attribute=name]",
			"<column attribute=\"name\">\n  <label>\n    <en>Name</en>\n  </label>\n</column>").apply(VIEW);

		assertTrue(result, result.contains(
			"\t\t\t<column attribute=\"name\">\n\t\t\t  <label>\n\t\t\t    <en>Name</en>\n\t\t\t  </label>\n\t\t\t</column>\n"
				+ "\t\t\t<column attribute=\"start\"/>"));
	}

	/** Removing an element takes its line; the comments around it stay. */
	public void testRemoveTakesTheLineAndKeepsTheComments() {
		String result = new ViewEdit.Remove("/view/table/columns/column[1]").apply(VIEW);

		assertEquals(VIEW.replace("\t\t\t<column attribute=\"name\"/>\n", ""), result);
		assertTrue(result.contains("<!-- The channel the table writes. -->"));
	}

	/** An address that names nothing says what is there. */
	public void testAWrongAddressNamesWhatIsThere() {
		try {
			new ViewEdit.Remove("/view/form").apply(VIEW);
			fail("Nothing is at /view/form.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("/view/channels"));
			assertTrue(ex.getMessage(), ex.getMessage().contains("/view/table"));
		}
	}

	/** An edit that would break the XML is refused, naming the line. */
	public void testAnEditThatBreaksTheXmlIsRefused() {
		try {
			new ViewEdit.Insert("/view/table", "<columns>", ViewEdit.Position.AFTER).apply(VIEW);
			fail("The result is not well-formed.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("line"));
		}
	}

	/** The outline gives each element its line and address, and folds a label into its owner. */
	public void testOutline() {
		String outline = ViewSource.parse(VIEW.replace("<column attribute=\"name\"/>",
			"<column attribute=\"name\"><label><en>Name</en><de>Name</de></label></column>")).outline();

		assertTrue(outline, outline.contains("8: /view/table selection=\"selected\" types=\"a:B\"\n"));
		assertTrue(outline, outline.contains("10: /view/table/columns/column[1] attribute=\"name\" label(en: Name; de: Name)\n"));
		assertTrue(outline, outline.contains("11: /view/table/columns/column[2] attribute=\"start\"\n"));
	}
}
