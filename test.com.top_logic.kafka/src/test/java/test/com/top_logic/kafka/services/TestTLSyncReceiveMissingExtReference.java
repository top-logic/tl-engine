/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import static java.util.Collections.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLObject;

/**
 * {@link TestCase} for receiving an {@link ExtReference} which cannot be resolved.
 * <p>
 * That happens for example if the object has been deleted in the receiving system.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLSyncReceiveMissingExtReference extends AbstractTLSyncTest {

	private static final Class<TestTLSyncReceiveMissingExtReference> THIS_CLASS =
		TestTLSyncReceiveMissingExtReference.class;

	private static final boolean IGNORE_LOGGED_ERRORS = true;

	private static final boolean SINGLE = false;

	private static final boolean MULTIPLE = true;

	private static final String SOURCE_MODULE_NAME = THIS_CLASS.getName() + ".source";

	private static final String TARGET_MODULE_NAME = THIS_CLASS.getName() + ".target";

	private static final String SOURCE_TYPE_NAME = "SourceType";

	private static final String TARGET_TYPE_NAME = "TargetType";

	private static final String COUNTER_ATTRIBUTE_NAME = "counter";

	private static final String DEFAULT_SINGLE_REFERENCE_ATTRIBUTE_NAME = "defaultSingleReference";

	private static final String DEFAULT_MULTIPLE_REFERENCE_ATTRIBUTE_NAME = "defaultMultipleReference";

	private static final String INLINE_REFERENCE_ATTRIBUTE_NAME = "inlineReference";

	private static final String ASSOCIATION_REFERENCE_ATTRIBUTE_NAME = "associationReference";

	public void testUpdateOfDeletedObject() {
		TLObject sourceA = sync(() -> {
			TLObject object = createObject();
			setCounter(object, 1);
			return object;
		});
		TLObject targetA = findTargetObject(sourceA);
		/* This second object is used to test that TL-Sync applies the rest of the changeset. */
		TLObject sourceB = sync(() -> {
			TLObject object = createObject();
			setCounter(object, 5);
			return object;
		});
		TLObject targetB = findTargetObject(sourceB);
		KBUtils.inTransaction(targetA::tDelete);
		sync(IGNORE_LOGGED_ERRORS, () -> {
			setCounter(sourceA, 2);
			setCounter(sourceB, 6);
		});
		assertTrue(sourceA.tValid());
		assertTrue(sourceB.tValid());
		assertFalse(targetA.tValid());
		assertTrue(targetB.tValid());
		assertEquals(2, getCounter(sourceA));
		assertEquals(6, getCounter(sourceB));
		assertEquals(6, getCounter(targetB));
		// Clean up:
		sync(IGNORE_LOGGED_ERRORS, () -> {
			sourceA.tDelete();
			sourceB.tDelete();
		});
	}

	public void testDeleteOfDeletedObject() {
		TLObject sourceA = sync(() -> {
			TLObject object = createObject();
			setCounter(object, 1);
			return object;
		});
		TLObject targetA = findTargetObject(sourceA);
		/* This second object is used to test that TL-Sync applies the rest of the changeset. */
		TLObject sourceB = sync(() -> {
			TLObject object = createObject();
			setCounter(object, 5);
			return object;
		});
		TLObject targetB = findTargetObject(sourceB);
		KBUtils.inTransaction(targetA::tDelete);
		sync(IGNORE_LOGGED_ERRORS, () -> {
			sourceA.tDelete();
			setCounter(sourceB, 6);
		});
		assertFalse(sourceA.tValid());
		assertTrue(sourceB.tValid());
		assertFalse(targetA.tValid());
		assertTrue(targetB.tValid());
		assertEquals(6, getCounter(sourceB));
		assertEquals(6, getCounter(targetB));
		// Clean up:
		sync(IGNORE_LOGGED_ERRORS, () -> {
			sourceB.tDelete();
		});
	}

	public void testUpdateOfDefaultSingleReferenceToDeletedObject() {
		testUpdateOfReferenceToDeletedObject(DEFAULT_SINGLE_REFERENCE_ATTRIBUTE_NAME, SINGLE);
	}

	public void testUpdateOfDefaultMultipleReferenceToDeletedObject() {
		testUpdateOfReferenceToDeletedObject(DEFAULT_MULTIPLE_REFERENCE_ATTRIBUTE_NAME, MULTIPLE);
	}

	public void testUpdateOfInlineReferenceToDeletedObject() {
		testUpdateOfReferenceToDeletedObject(INLINE_REFERENCE_ATTRIBUTE_NAME, SINGLE);
	}

	public void testUpdateOfAssociationReferenceToDeletedObject() {
		testUpdateOfReferenceToDeletedObject(ASSOCIATION_REFERENCE_ATTRIBUTE_NAME, MULTIPLE);
	}

	private void testUpdateOfReferenceToDeletedObject(String attributeName, boolean multiple) {
		TLObject sourceSelf = sync(() -> {
			TLObject self = createObject();
			setCounter(self, 1);
			return self;
		});
		TLObject sourceValue = sync(this::createObject);
		TLObject targetValue = findTargetObject(sourceValue);
		KBUtils.inTransaction(targetValue::tDelete);
		sync(IGNORE_LOGGED_ERRORS, () -> {
			setCounter(sourceSelf, 2);
			setReference(sourceSelf, attributeName, sourceValue, multiple);
		});
		TLObject targetSelf = findTargetObject(sourceSelf);
		assertTrue(sourceSelf.tValid());
		assertTrue(sourceValue.tValid());
		assertTrue(targetSelf.tValid());
		assertFalse(targetValue.tValid());
		assertEquals(2, getCounter(sourceSelf));
		assertEquals(sourceValue, getReference(sourceSelf, attributeName, multiple));
		assertEquals(2, getCounter(targetSelf));
		assertEquals(null, getReference(targetSelf, attributeName, multiple));
		// Clean up:
		sync(IGNORE_LOGGED_ERRORS, () -> {
			sourceSelf.tDelete();
			sourceValue.tDelete();
		});
	}

	private void setReference(TLObject self, String attributeName, TLObject value, boolean multiple) {
		self.tUpdateByName(attributeName, wrapValue(value, multiple));
	}

	private Object wrapValue(TLObject value, boolean multiple) {
		if (multiple) {
			return singleton(value);
		}
		return value;
	}

	private Object getReference(TLObject targetSelf, String attributeName, boolean multiple) {
		return unwrapValue(targetSelf.tValueByName(attributeName), multiple);
	}

	private Object unwrapValue(Object value, boolean multiple) {
		if (multiple) {
			return CollectionUtil.getSingleValueFrom(value);
		}
		return value;
	}

	private TLObject findTargetObject(TLObject source) {
		return findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), source);
	}

	private TLObject createObject() {
		return newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME);
	}

	private void setCounter(TLObject self, int value) {
		self.tUpdateByName(COUNTER_ATTRIBUTE_NAME, value);
	}

	private int getCounter(TLObject self) {
		return (int) self.tValueByName(COUNTER_ATTRIBUTE_NAME);
	}

	/**
	 * Creates a {@link TestSuite} for all the tests in this class.
	 */
	public static Test suite() {
		return suite(THIS_CLASS);
	}

}
