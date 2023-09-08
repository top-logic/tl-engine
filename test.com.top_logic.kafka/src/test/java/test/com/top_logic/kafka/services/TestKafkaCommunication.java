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

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;

/**
 * Test case to export import objects using Kafka.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestKafkaCommunication extends AbstractTLSyncTest {

	static String SOURCE_MODULE_NAME = TestKafkaCommunication.class.getName() + ".source";

	static String TARGET_MODULE_NAME = TestKafkaCommunication.class.getName() + ".target";

	static String SOURCE_TYPE_NAME = "Node";

	static String TARGET_TYPE_NAME = "TargetNode";

	static String EXPORTED_STRING_ATTRIBUTE = "exported";

	static String IMPORTED_STRING_ATTRIBUTE = "imported";

	static String NOT_EXPORTED_STRING_ATTRIBUTE = "notExported";

	static String REFERENCE_SOURCE_ATTRIBUTE = "otherNode";

	static String REFERENCE_TARGET_ATTRIBUTE = REFERENCE_SOURCE_ATTRIBUTE;

	static String REFERENCE_INLINE_SOURCE_ATTRIBUTE = "otherNodeInline";

	static String REFERENCE_INLINE_TARGET_ATTRIBUTE = REFERENCE_INLINE_SOURCE_ATTRIBUTE;

	public void testNodeDeletion() throws Exception {
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				commit(tx);
			}
		});

		TLObject targetItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceItem.get());
		assertNotNull(targetItem);
		assertTrue(targetItem.tHandle().isAlive());
		runWithKafka(new Execution() {

			@Override
			public void run() throws DataObjectException {
				Transaction tx = begin();
				sourceItem.get().tHandle().delete();
				commit(tx);
			}
		});

		assertFalse(targetItem.tHandle().isAlive());
	}

	public void testEmptyNodeCreation() throws Exception {
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>(); 
		runWithKafka(new Execution() {
			
			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				commit(tx);
			}
		});

		TLObject targetItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceItem.get());
		assertNotNull(targetItem);

		assertNull(getValue(targetItem, IMPORTED_STRING_ATTRIBUTE));
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				setValue(sourceItem.get(), EXPORTED_STRING_ATTRIBUTE, "string value");
				commit(tx);
			}
		});

		assertEquals("string value", getValue(targetItem, IMPORTED_STRING_ATTRIBUTE));
	}

	public void testNonEmptyNodeCreation() throws Exception {
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				setValue(sourceItem.get(), EXPORTED_STRING_ATTRIBUTE, "string value");
				commit(tx);
			}
		});

		TLObject targetItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceItem.get());
		assertNotNull(targetItem);

		assertEquals("string value", getValue(targetItem, IMPORTED_STRING_ATTRIBUTE));
	}

	public void testNotExportedProperty() throws Exception {
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				setValue(sourceItem.get(), NOT_EXPORTED_STRING_ATTRIBUTE, "notExported");
				commit(tx);
			}
		});

		TLObject targetItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceItem.get());
		assertNotNull(targetItem);

		assertNull(getValue(targetItem, NOT_EXPORTED_STRING_ATTRIBUTE));
	}

	public void testReferencedProperty() throws Exception {
		testReferenceAttribute(REFERENCE_SOURCE_ATTRIBUTE, REFERENCE_TARGET_ATTRIBUTE);
	}

	public void testReferencedPropertyInline() throws Exception {
		testReferenceAttribute(REFERENCE_INLINE_SOURCE_ATTRIBUTE, REFERENCE_INLINE_TARGET_ATTRIBUTE);
	}

	private void testReferenceAttribute(final String referenceSourceAttribute, String referenceTargetAttribute)
			throws Exception {
		final AtomicReference<TLObject> sourceItem = new AtomicReference<>();
		final AtomicReference<TLObject> destItem = new AtomicReference<>();
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				sourceItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				destItem.set(newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME));
				setValue(sourceItem.get(), referenceSourceAttribute, destItem.get());
				commit(tx);
			}

		});

		TLObject targetSourceItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), sourceItem.get());
		assertNotNull(targetSourceItem);
		TLObject targetDestItem = findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), destItem.get());
		assertNotNull(targetDestItem);
		assertEquals(targetDestItem, getValue(targetSourceItem, referenceTargetAttribute));
		assertEquals(null, getValue(targetDestItem, referenceTargetAttribute));
		
		// update Reference
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				setValue(sourceItem.get(), referenceSourceAttribute, sourceItem.get());
				commit(tx);
			}

		});
		assertEquals(targetSourceItem, getValue(targetSourceItem, referenceTargetAttribute));

		// delete Reference
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				setValue(sourceItem.get(), referenceSourceAttribute, null);
				commit(tx);
			}

		});
		assertEquals(null, getValue(targetSourceItem, referenceTargetAttribute));

		// set Reference
		runWithKafka(new Execution() {

			@Override
			public void run() {
				Transaction tx = begin();
				setValue(sourceItem.get(), referenceSourceAttribute, destItem.get());
				commit(tx);
			}

		});
		assertEquals(targetDestItem, getValue(targetSourceItem, referenceTargetAttribute));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestKafkaCommunication}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		TestFactory factory = DefaultTestFactory.INSTANCE;
		if (false) {
			factory = new SingleTestFactory("testReferencedPropertyInline");
		}
		return suite(TestKafkaCommunication.class, factory);
	}

}
