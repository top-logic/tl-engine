/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.security.ReadFilter;

/**
 * The one form a tool result names a model object in, and that the tools read back.
 *
 * <p>
 * {@link ReadFilter#withId(String, String)} writes it and {@link TidResolver#objectId(String)}
 * reads it, and the pair is what makes a text result actionable: the model reads an object in one
 * call and names it again in the next. The two are tested together because either alone is only
 * half a contract - a writer nobody can read back is what this closes.
 * </p>
 *
 * <p>
 * Nothing here renders a label or reaches the knowledge base: what is under test is the string, not
 * the lookup. {@link TidResolver#resolve(String, com.top_logic.tl.ai.core.security.ModelSecurity)}
 * needs both and is covered where a booted application is.
 * </p>
 */
public class TestObjectIds extends TestCase {

	/** The form itself, as the tool descriptions promise it. */
	public void testTheForm() {
		assertEquals("Invoice 4711 [id:GenericObject:5371]",
			ReadFilter.withId("Invoice 4711", "GenericObject:5371"));
	}

	/** An object with no identifier renders as its bare label, not as a broken one. */
	public void testNoIdentifierIsNoMarker() {
		assertEquals("A transient thing", ReadFilter.withId("A transient thing", null));
		assertEquals("A transient thing", ReadFilter.withId("A transient thing", ""));
	}

	/** A label the application left empty still carries the identifier. */
	public void testIdentifierWithoutALabel() {
		assertEquals(" [id:GenericObject:5371]", ReadFilter.withId(null, "GenericObject:5371"));
	}

	/** What the writer wrote is what the reader reads: the round trip that makes the form useful. */
	public void testRoundTrip() {
		assertEquals("GenericObject:5371",
			TidResolver.objectId(ReadFilter.withId("Invoice 4711", "GenericObject:5371")));
	}

	/** A label with brackets of its own does not move the identifier. */
	public void testLabelWithBrackets() {
		String written = ReadFilter.withId("Invoice [draft]", "GenericObject:5371");

		assertEquals("GenericObject:5371", TidResolver.objectId(written));
	}

	/** A bare identifier is what every JSON result carries, and it passes through. */
	public void testBareIdentifier() {
		assertEquals("GenericObject:5371", TidResolver.objectId("GenericObject:5371"));
		assertEquals("GenericObject:5371", TidResolver.objectId("  GenericObject:5371 "));
	}

	/** An unterminated marker is still read: the identifier is what follows it. */
	public void testUnterminatedMarker() {
		assertEquals("GenericObject:5371", TidResolver.objectId("Invoice [id:GenericObject:5371"));
	}
}
