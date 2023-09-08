/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff.model;


import static test.com.top_logic.config.xdiff.TestingUtil.*;
import junit.framework.TestCase;

import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.Node;

/**
 * Test case for the {@link Node} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestNode extends TestCase {

	public void testWeight() {
		assertEquals(9, fixture("<a><b a1='v1' a2='v2'>text</b><c a3='v3'><!--Comment--></c></a>").getWeight());
	}

	public void testDeepEquals() {
		deepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<b a1='v1'>text<!--comment--></b>");
	}

	public void testDeepEqualsWhiteSpace() {
		notDeepEquals(
			"<b a1='v1'></b>",
			"<b a1='v1'>   </b>");
	}

	public void testDeepEqualsInsertedComment() {
		notDeepEquals(
			"<b a1='v1'></b>",
			"<b a1='v1'><!--comment--></b>");
	}

	public void testDeepEqualsCharacterEntity() {
		deepEquals(
			"<b a1='v1'>&#65;</b>",
			"<b a1='v1'>A</b>");
	}

	public void testDeepEqualsPredefinedEntity() {
		deepEquals(
			"<b a1='&quot;'></b>",
			"<b a1='\"'></b>");
	}

	public void testDeepEqualsAttributeValues() {
		notDeepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<b a1='v2'>text<!--comment--></b>");
	}

	public void testDeepEqualsAttributeNames() {
		notDeepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<b a2='v1'>text<!--comment--></b>");
	}

	public void testDeepEqualsElementName() {
		notDeepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<c a1='v1'>text<!--comment--></c>");
	}

	public void testDeepEqualsText() {
		notDeepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<b a1='v1'>TEXT<!--comment--></b>");
	}

	public void testDeepEqualsComment() {
		notDeepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<b a1='v1'>text<!--COMMENT--></b>");
	}

	public void testDeepEqualsAttributeCount() {
		notDeepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<b a1='v1' a2='v2'>text<!--comment--></b>");
	}

	public void testDeepEqualsInsertedElement() {
		notDeepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<b a1='v1'>text<c/><!--comment--></b>");
	}

	public void testDeepEqualsInsertedAttribute() {
		notDeepEquals(
			"<b a1='v1'>text<!--comment--></b>",
			"<b>text<!--comment--></b>");
	}

	public void testDeepEqualsSameNamespaceAndPrefix() {
		deepEquals(
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><x:c x:a1='v1'/></b>", 
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><x:c x:a1='v1'/></b>");
	}

	public void testDeepEqualsDifferentElementNamespace() {
		notDeepEquals(
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><x:c x:a1='v1'/></b>", 
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><y:c x:a1='v1'/></b>");
	}

	public void testDeepEqualsDifferentAttributeNamespace() {
		notDeepEquals(
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><x:c x:a1='v1'/></b>", 
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><x:c y:a1='v1'/></b>");
	}

	public void testDeepEqualsDifferentPrefix() {
		deepEquals(
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><x:c y:a1='v1'/></b>", 
			"<b xmlns:y='http://foo' xmlns:x='http://bar'><y:c x:a1='v1'/></b>");
	}

	public void testDeepEqualsDifferentElementNamespaceURI() {
		notDeepEquals(
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><x:c y:a1='v1'/></b>", 
			"<b xmlns:x='http://FOO' xmlns:y='http://bar'><x:c y:a1='v1'/></b>");
	}

	public void testDeepEqualsDifferentAttributeNamespaceURI() {
		notDeepEquals(
			"<b xmlns:x='http://foo' xmlns:y='http://bar'><x:c y:a1='v1'/></b>", 
			"<b xmlns:x='http://foo' xmlns:y='http://BAR'><x:c y:a1='v1'/></b>");
	}

	public void testDeepEqualsPartOfDocument() {
		Document d1 = fixture("<a><b a1='v1' a2='v2'>text</b></a>");
		Document d2 = fixture("<b a1='v1' a2='v2'>text</b>");
		assertTrue(d1.getChild(0).getChild(0).equalsNode(d2.getChild(0)));
	}
	
	private void deepEquals(String xml1, String xml2) {
		assertTrue(TestNode.compareDocuments(xml1, xml2));
	}

	private void notDeepEquals(String xml1, String xml2) {
		assertFalse(TestNode.compareDocuments(xml1, xml2));
	}

	private static boolean compareDocuments(String xml1, String xml2) {
		return fixture(xml1).equalsNode(fixture(xml2));
	}
	
}
