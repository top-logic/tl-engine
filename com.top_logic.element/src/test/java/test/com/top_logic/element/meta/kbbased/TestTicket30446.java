/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.meta.model.ticket30446.Item;
import test.com.top_logic.element.meta.model.ticket30446.Owner;
import test.com.top_logic.element.meta.model.ticket30446.Ticket30446Factory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.config.JavaName;
import com.top_logic.util.model.ModelService;

/**
 * Test of the access methods generated for attributes with a {@link JavaName} annotation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTicket30446 extends BasicTestCase {

	public void testPropertyWithJavaName() {
		Ticket30446Factory factory = Ticket30446Factory.getInstance();
		Date changed = new Date(1000000000000L);
		Date updated = new Date(2000000000000L);
		Owner owner;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			owner = factory.createOwner();
			owner.setConfigurationChanged(changed);
			tx.commit();
		}

		assertEquals(changed, owner.getConfigurationChanged());
		assertEquals(changed, owner.tValueByName(Owner.LAST_MODIFIED_ATTR));

		long before = System.currentTimeMillis();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			owner.tUpdateByName(Owner.LAST_MODIFIED_ATTR, updated);
			tx.commit();
		}
		long after = System.currentTimeMillis();
		assertEquals(updated, owner.getConfigurationChanged());

		Long technicalTimestamp = ((Wrapper) owner).getLastModified();
		assertNotNull("Technical modification timestamp missing.", technicalTimestamp);
		assertTrue("Not the commit time of the last change: " + technicalTimestamp,
			before <= technicalTimestamp.longValue() && technicalTimestamp.longValue() <= after);
	}

	public void testReferenceWithJavaName() {
		Ticket30446Factory factory = Ticket30446Factory.getInstance();
		Owner owner;
		Item item1, item2;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			owner = factory.createOwner();
			item1 = factory.createItem();
			item2 = factory.createItem();
			owner.addLinkedItem(item1);
			owner.addLinkedItem(item2);
			tx.commit();
		}

		assertEquals(set(item1, item2), owner.getLinkedItems());
		assertEquals(set(item1, item2), owner.tValueByName(Owner.TYPES_ATTR));

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			owner.removeLinkedItem(item1);
			tx.commit();
		}
		assertEquals(set(item2), owner.getLinkedItems());
		assertEquals(set(item2), owner.tValueByName(Owner.TYPES_ATTR));

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			owner.setLinkedItems(set(item1));
			tx.commit();
		}
		assertEquals(set(item1), owner.tValueByName(Owner.TYPES_ATTR));

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			owner.getLinkedItemsModifiable().add(item2);
			tx.commit();
		}
		assertEquals(set(item1, item2), owner.tValueByName(Owner.TYPES_ATTR));
	}

	static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestTicket30446}.
	 */
	public static Test suite() {
		Test t = new TestSuite(TestTicket30446.class);
		t = ServiceTestSetup.createSetup(t, ModelService.Module.INSTANCE);
		t = KBSetup.getKBTest(t, KBSetup.DEFAULT_KB);
		return t;
	}
}
