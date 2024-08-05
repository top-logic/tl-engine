/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.wrap;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Test {@link Person}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestPerson extends BasicTestCase {

	private static int _nextId = 1;

	private int _startId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_startId = _nextId;
	}

	@Override
	protected void tearDown() throws Exception {
		KnowledgeBase kb = kb();

		Transaction tx = kb.beginTransaction();
		for (int usedId = _startId; usedId < _nextId; usedId++) {
			String name = getName(usedId);

			Group g = Group.getGroupByName(name);
			if (g != null) {
				g.tDelete();
			}

			Person p = Person.byName(name);
			if (p != null) {
				p.tDelete();
			}
		}
		tx.commit();

		super.tearDown();
	}

	/**
	 * @see "Ticket #3171: Security-Problem durch Wiederverwendung existierender Representative-Group für neuen Benutzer"
	 * @see "Ticket #3350: Person.getRepresentativeGroup() überprüft nur auf (nicht eindeutigen) Namen"
	 */
	public void testRepresentativeGroup() {
		Person p1;
		{
			try (Transaction tx = kb().beginTransaction()) {
				p1 = mkPerson();
				tx.commit();
			}

		}

		Person p2 = Person.byName(p1.getName());
		assertSame(p1, p2);
		assertNotNull(p1.getRepresentativeGroup());
		assertSame(p1.getRepresentativeGroup(), p2.getRepresentativeGroup());

		// Delete representative group.
		{
			try (Transaction tx = kb().beginTransaction()) {
				p1.getRepresentativeGroup().tDelete();
				tx.commit();
			}
		}

		if (p2.getRepresentativeGroup() == null) {
			fail("Test should fail due to the known bug in ticket #8958: Stale cache.");
		} else {
			/* Expected due to the known bug in ticket #8958: Stale cache. */
		}

	}

	/**
	 * Tests that the person is in its representative group.
	 */
	public void testIsInRepresentativeGroup() {
		Person p1;
		try (Transaction tx = kb().beginTransaction()) {
			p1 = mkPerson();
			tx.commit();
		}
		assertTrue(p1.isInGroup(p1.getRepresentativeGroup()));
	}

	/**
	 * Tests that a person can not have the name of a group.
	 */
	public void testCreatePersonWithGroupName() {
		String groupName = mkName();

		try (Transaction tx = kb().beginTransaction()) {
			Group group = Group.createGroup(groupName);
			assertNotNull("Creating of group with name " + groupName + " should work.", group);
			tx.commit();
		}

		try {
			try (Transaction tx = kb().beginTransaction()) {
				mkPerson(groupName);
				tx.commit();
			}
			fail("Ticket #9261: Group with name '" + groupName + "' already exists");
		} catch (KnowledgeBaseException ex) {
			// expected
		}
	}

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private Person mkPerson() {
		return mkPerson(mkName());
	}

	private Person mkPerson(String name) {
		return Person.create(kb(), name, "dbSecurity");
	}

	private String mkName() {
		int id = _nextId++;
		return getName(id);
	}

	private String getName(int id) {
		return getClass().getSimpleName() + id;
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test suite() {
		final Test innerTest = PersonManagerSetup.createPersonManagerSetup(
			TestPersonSetup.wrap(new TestSuite(TestPerson.class)));
		return innerTest;
	}

}
