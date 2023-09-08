/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;
import junit.framework.TestCase;

import com.top_logic.basic.col.TransitiveRelations;
import com.top_logic.basic.col.TransitiveRelations.TransitiveRelation;

/**
 * Test case for {@link TransitiveRelations}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTransitiveRelations extends TestCase {

	private TransitiveRelations<String> _relations;

	private TransitiveRelation<String> _relation;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_relations = new TransitiveRelations<>();
		_relation = _relations.makeRelation("r1");
	}

	public void testPartnerTransitivity() {
		_relation.link("a", "b");
		_relation.link("c", "d");
		_relation.link("e", "f");

		assertRelated("a", "a");
		assertRelated("a", "b");
		assertNotRelated("a", "d");
		assertNotRelated("a", "f");

		_relation.link("b", "d");

		assertRelated("a", "a");
		assertRelated("a", "b");
		assertRelated("a", "d");
		assertNotRelated("a", "f");
	}

	public void testPartnersByName() {
		_relation.assignToGroup("g1", "a");
		_relation.assignToGroup("g1", "b");
		_relation.assignToGroup("g1", "c");

		_relation.assignToGroup("g2", "d");
		_relation.assignToGroup("g2", "e");

		_relation.assignToGroup("g3", "f");
		_relation.assignToGroup("g3", "g");

		assertRelated("a", "a");
		assertRelated("a", "b");
		assertRelated("a", "c");

		assertRelated("d", "d");
		assertRelated("d", "e");

		assertRelated("f", "f");
		assertRelated("f", "g");

		assertNotRelated("a", "d");
		assertNotRelated("a", "f");
		assertNotRelated("a", "g");

		_relation.link("b", "e");

		assertRelated("b", "d");
		assertRelated("b", "e");

		assertRelated("a", "d");
		assertRelated("a", "e");

		assertNotRelated("a", "f");
		assertNotRelated("a", "g");
	}

	public void testDeclaredRelation() {
		assertFalse(_relations.getRelation("r1").isRelated("a", "b"));
	}

	public void testUndeclaredRelation() {
		assertFalse(_relations.getRelation("undeclared").isRelated("a", "b"));
	}

	public void testMakeRelation() {
		_relation.link("a", "b");
		assertRelated("a", "b");

		assertTrue(_relations.makeRelation("r1").isRelated("a", "b"));
	}

	public void testAllRelated() {
		_relation.link("a", "b");
		_relation.link("b", "c");
		_relation.link("d", "c");

		_relation.link("x", "y");

		assertEquals(set("a", "b", "c", "d"), _relation.getRelated("a"));
		assertEquals(set("x", "y"), _relation.getRelated("y"));

		assertEquals(set(), _relation.getRelated("foobar"));
	}

	private void assertRelated(String comp1, String comp2) {
		assertTrue(_relation.isRelated(comp1, comp2));
		assertTrue(_relation.isRelated(comp2, comp1));
	}

	private void assertNotRelated(String comp1, String comp2) {
		assertFalse(_relation.isRelated(comp1, comp2));
		assertFalse(_relation.isRelated(comp2, comp1));
	}

}
