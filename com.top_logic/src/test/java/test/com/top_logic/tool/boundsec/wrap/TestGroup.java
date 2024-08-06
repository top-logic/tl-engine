/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.wrap;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Test case for {@link Group}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestGroup extends BasicTestCase {

	private static int nextId = 1;
	private int startId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.startId = nextId;
	}
	
	@Override
	protected void tearDown() throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		
		Transaction tx = kb.beginTransaction();
		for (int usedId = startId; usedId < nextId; usedId++) {
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

	public void testCreate() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		
		Transaction tx = kb.beginTransaction();
		Person p1 = mkPerson();
		Group g1 = mkGroup(p1);
		Person p2 = mkPerson();
		Group g2 = mkGroup(p2);
		Person p3 = mkPerson();
		Group g3 = mkGroup(p3);
		g1.addMember(g2);
		g1.addMember(g3);
		
		Person p4 = mkPerson();
		Group g4 = mkGroup(p4);
		g2.addMember(g4);
		tx.commit();
		
		assertEquals(set(p1, p2, p3, p4), toSet(g1.getMembers(true)));
	}

	public void testContainmentSafety() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		
		Transaction tx = kb.beginTransaction();
		Person p1 = mkPerson();
		Group g1 = mkGroup(p1);
		Person p2 = mkPerson();
		Group g2 = mkGroup(p2);
		g1.addMember(g2);
		
		// Externally create cyclic containment relation.
		kb.createAssociation(g1.tHandle(), g2.tHandle(), Group.GROUP_ASSOCIATION);
		
		tx.commit();
		
		// Make sure, that containment can be queried even if containment is cyclic.
		assertEquals(set(p1, p2), toSet(g1.getMembers(true)));
		assertEquals(set(p1, p2), toSet(g2.getMembers(true)));
	}
	
	public void testPreventRecursion0() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		
		Transaction tx = kb.beginTransaction();
		Person p1 = mkPerson();
		Group g1 = mkGroup(p1);
		try {
			g1.addMember(g1);
			fail("A group must prevent recursive containment.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
		tx.commit();
		
		assertEquals(set(p1), toSet(g1.getMembers(true)));
	}
	
	public void testPreventRecursion1() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		
		Transaction tx = kb.beginTransaction();
		Person p1 = mkPerson();
		Group g1 = mkGroup(p1);
		Person p2 = mkPerson();
		Group g2 = mkGroup(p2);
		g1.addMember(g2);
		try {
			g2.addMember(g1);
			fail("A group must prevent recursive containment.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
		tx.commit();
		
		assertEquals(set(p1, p2), toSet(g1.getMembers(true)));
		assertEquals(set(p2), toSet(g2.getMembers(true)));
	}

	public void testPreventRecursion2() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		
		Transaction tx = kb.beginTransaction();
		Person p1 = mkPerson();
		Group g1 = mkGroup(p1);
		Person p2 = mkPerson();
		Group g2 = mkGroup(p2);
		g1.addMember(g2);
		Person p3 = mkPerson();
		Group g3 = mkGroup(p3);
		Person p4 = mkPerson();
		Group g4 = mkGroup(p4);
		g3.addMember(g4);
		
		g4.addMember(g1);
		try {
			g2.addMember(g3);
			fail("A group must prevent recursive containment.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
		tx.commit();
		
		assertEquals(set(p1, p2), toSet(g1.getMembers(true)));
		assertEquals(set(p2), toSet(g2.getMembers(true)));
		assertEquals(set(p1, p2, p3, p4), toSet(g3.getMembers(true)));
		assertEquals(set(p1, p2, p4), toSet(g4.getMembers(true)));
	}
	
	public void testIsInGroup() {
		Person p1 = mkPerson();
		Group g1 = mkGroup(p1);
		assertTrue(p1.isInGroup(g1));
	}

	public void testIsInGroupRecursive() {
		Person p1 = mkPerson();
		Group g1 = mkGroup(p1);
		Group g2 = mkGroup();
		g2.addMember(g1);
		assertTrue(p1.isInGroup(g2));
	}

	public void testIsInAnyGroup() {
		Person p1 = mkPerson();
		Group g1 = mkGroup(p1);
		Group g2 = mkGroup();
		Group g3 = mkGroup();
		assertTrue(p1.isInAnyGroup(Arrays.asList(g1, g2)));
		assertTrue(p1.isInAnyGroup(Arrays.asList(g2, g1)));
		assertFalse(p1.isInAnyGroup(Arrays.asList(g2, g3)));
	}

	private Group mkGroup(Person p) {
		Group g = mkGroup();
		g.addMember(p);
		return g;
	}

	private Group mkGroup() {
		return Group.createGroup(mkName());
	}

	private Person mkPerson() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		String name = mkName();
		return Person.create(kb, name, "dbSecurity");
	}
	
	private String mkName() {
		int id = nextId++;
		return getName(id);
	}

	private String getName(int id) {
		return getClass().getSimpleName() + id;
	}
	
	
	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestGroup.class));
	}
	
}
