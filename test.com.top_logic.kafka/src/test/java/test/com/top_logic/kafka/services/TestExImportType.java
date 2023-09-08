/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.SingleTestFactory;
import test.com.top_logic.basic.TestFactory;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;

/**
 * Test case for TLSync
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestExImportType extends AbstractTLSyncTest {

	static String MODULE_NAME = TestExImportType.class.getName();

	static String TYPE_NAME = "Node";

	static String OTHER_NODE_ATTRIBUTE = "otherNode";

	static String STRING_ATTRIBUTE = "string";

	public void testExImportType() throws Exception {
		// Create
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(MODULE_NAME, TYPE_NAME));
				commit(tx);

			}
		});
		TLObject targetItem = findReceivedObjectFor(type(MODULE_NAME, TYPE_NAME), sourceItem.get());
		assertNotNull(targetItem);

		int sentEvents;
		// trigger kafka, especially to check that create operation of target item is not propagated
		sentEvents = triggerKafka();
		assertEquals(0, sentEvents);

		// Update
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.get().tUpdateByName(STRING_ATTRIBUTE, "value");
				commit(tx);

			}
		});
		assertEquals("value", targetItem.tValueByName(STRING_ATTRIBUTE));

		// trigger kafka, especially to check that update operation of target item is not propagated
		sentEvents = triggerKafka();
		assertEquals(0, sentEvents);

		// Deletion
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.get().tDelete();
				commit(tx);

			}
		});
		assertFalse(targetItem.tHandle().isAlive());

		// trigger kafka, especially to check that delete operation of target item is not propagated
		sentEvents = triggerKafka();
		assertEquals(0, sentEvents);
	}

	public void testExImportAssociation() throws Exception {
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		final AtomicReference<TLObject> targetItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(MODULE_NAME, TYPE_NAME));
				targetItem.set(newObject(MODULE_NAME, TYPE_NAME));
				sourceItem.get().tUpdateByName(OTHER_NODE_ATTRIBUTE, targetItem.get());
				commit(tx);

			}
		});
		TLObject sourceItemTarget = findReceivedObjectFor(type(MODULE_NAME, TYPE_NAME), sourceItem.get());
		assertNotNull(sourceItemTarget);
		TLObject targetItemTarget = findReceivedObjectFor(type(MODULE_NAME, TYPE_NAME), targetItem.get());
		assertNotNull(targetItemTarget);
		assertSame(targetItemTarget, sourceItemTarget.tValueByName(OTHER_NODE_ATTRIBUTE));

		int sentEvents;
		/* Trigger kafka, especially to check that create operation of target associations are not
		 * propagated. */
		sentEvents = triggerKafka();
		assertEquals(0, sentEvents);

		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.get().tUpdateByName(OTHER_NODE_ATTRIBUTE, null);
				commit(tx);
			}
		});
		assertNull(sourceItemTarget.tValueByName(OTHER_NODE_ATTRIBUTE));

		/* Trigger kafka, especially to check that delete operation of target associations are not
		 * propagated. */
		sentEvents = triggerKafka();
		assertEquals(0, sentEvents);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestExImportType}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		TestFactory factory = DefaultTestFactory.INSTANCE;
		if (false) {
			factory = new SingleTestFactory("testCorrectAssociationTarget");
		}
		return suite(TestExImportType.class, factory);
	}

}
