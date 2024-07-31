/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import junit.framework.Test;

import com.top_logic.element.meta.PersistentClass;
import com.top_logic.element.model.PersistentModule;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test class for {@link PersistentClass}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPersistentClass extends TestWithModelExtension {

	private TLModule _newModule;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_newModule = addModule("newModule");
	}

	@Override
	protected void tearDown() throws Exception {
		delete((PersistentModule) _newModule);
		super.tearDown();
	}

	public void testModifiableGeneralizations1() throws KnowledgeBaseException {
		testModifiableGeneralizations(true);
	}

	public void testModifiableGeneralizations2() throws KnowledgeBaseException {
		testModifiableGeneralizations(false);
	}

	private void testModifiableGeneralizations(boolean withinTx) throws KnowledgeBaseException {
		Transaction tx = _kb.beginTransaction();
		TLClass subClass = TLModelUtil.addClass(_newModule, "newClass");
		TLClass superClass1 = TLModelUtil.addClass(_newModule, "newClass2");
		TLClass superClass2 = TLModelUtil.addClass(_newModule, "newClass3");
		if (withinTx) {
			tx.commit();
			tx = _kb.beginTransaction();
		}
		subClass.getGeneralizations().add(superClass1);
		assertEquals(list(superClass1), subClass.getGeneralizations());
		assertEquals(set(subClass), toSet(superClass1.getSpecializations()));
		assertEquals(set(), toSet(superClass2.getSpecializations()));
		if (withinTx) {
			tx.commit();
			assertEquals("Generalizations does not change after commit.", list(superClass1),
				subClass.getGeneralizations());
			assertEquals("Generalizations does not change after commit.", set(subClass),
				toSet(superClass1.getSpecializations()));
			assertEquals("Generalizations does not change after commit.", set(),
				toSet(superClass2.getSpecializations()));
			tx = _kb.beginTransaction();
		}

		subClass.getGeneralizations().add(0, superClass2);
		assertEquals(list(superClass2, superClass1), subClass.getGeneralizations());
		assertEquals(set(subClass), toSet(superClass1.getSpecializations()));
		assertEquals(set(subClass), toSet(superClass2.getSpecializations()));
		if (withinTx) {
			tx.commit();
			assertEquals("Generalizations does not change after commit.", list(superClass2, superClass1),
				subClass.getGeneralizations());
			assertEquals("Generalizations does not change after commit.", set(subClass),
				toSet(superClass1.getSpecializations()));
			assertEquals("Generalizations does not change after commit.", set(subClass),
				toSet(superClass2.getSpecializations()));
			tx = _kb.beginTransaction();
		}

		subClass.getGeneralizations().remove(superClass1);
		assertEquals(list(superClass2), subClass.getGeneralizations());
		assertEquals(set(), toSet(superClass1.getSpecializations()));
		assertEquals(set(subClass), toSet(superClass2.getSpecializations()));
		if (withinTx) {
			tx.commit();
			assertEquals("Generalizations does not change after commit.", list(superClass2),
				subClass.getGeneralizations());
			assertEquals("Generalizations does not change after commit.", set(),
				toSet(superClass1.getSpecializations()));
			assertEquals("Generalizations does not change after commit.", set(subClass),
				toSet(superClass2.getSpecializations()));
			tx = _kb.beginTransaction();
		}
		tx.commit();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPersistentClass}.
	 */
	public static Test suite() {
		return suite(TestPersistentClass.class);
	}

}
