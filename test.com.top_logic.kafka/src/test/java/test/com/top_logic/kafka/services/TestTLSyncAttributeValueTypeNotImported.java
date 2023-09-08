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
import com.top_logic.model.TLObject;

/**
 * {@link TestCase} for receiving a value whose type is not imported.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLSyncAttributeValueTypeNotImported extends AbstractTLSyncTest {

	private static final Class<TestTLSyncAttributeValueTypeNotImported> THIS_CLASS =
		TestTLSyncAttributeValueTypeNotImported.class;

	private static final boolean SINGLE = false;

	private static final boolean MULTIPLE = true;

	private static final String SOURCE_MODULE_NAME = THIS_CLASS.getName() + ".source";

	private static final String TARGET_MODULE_NAME = THIS_CLASS.getName() + ".target";

	private static final String SOURCE_TYPE_NAME = "SourceType";

	private static final String VALUE_TYPE_NAME = "ValueType";

	private static final String TARGET_TYPE_NAME = "TargetType";

	private static final String COUNTER_ATTRIBUTE_NAME = "counter";

	private static final String DEFAULT_SINGLE_REFERENCE_ATTRIBUTE_NAME = "defaultSingleReference";

	private static final String DEFAULT_MULTIPLE_REFERENCE_ATTRIBUTE_NAME = "defaultMultipleReference";

	private static final String INLINE_REFERENCE_ATTRIBUTE_NAME = "inlineReference";

	private static final String ASSOCIATION_REFERENCE_ATTRIBUTE_NAME = "associationReference";

	public void testUpdateOfDefaultSingleReference() {
		testUpdateOfReferenceToDeletedObject(DEFAULT_SINGLE_REFERENCE_ATTRIBUTE_NAME, SINGLE);
	}

	public void testUpdateOfDefaultMultipleReference() {
		testUpdateOfReferenceToDeletedObject(DEFAULT_MULTIPLE_REFERENCE_ATTRIBUTE_NAME, MULTIPLE);
	}

	public void testUpdateOfInlineReference() {
		testUpdateOfReferenceToDeletedObject(INLINE_REFERENCE_ATTRIBUTE_NAME, SINGLE);
	}

	public void testUpdateOfAssociationReference() {
		testUpdateOfReferenceToDeletedObject(ASSOCIATION_REFERENCE_ATTRIBUTE_NAME, MULTIPLE);
	}

	private void testUpdateOfReferenceToDeletedObject(String attributeName, boolean multiple) {
		TLObject sourceSelf = sync(() -> {
			TLObject self = newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME);
			setCounter(self, 1);
			return self;
		});
		TLObject sourceValue = sync(() -> newObject(SOURCE_MODULE_NAME, VALUE_TYPE_NAME));
		assertNull(findTargetObject(sourceValue));
		sync(() -> {
			setCounter(sourceSelf, 2);
			setReference(sourceSelf, attributeName, sourceValue, multiple);
		});
		TLObject targetSelf = findTargetObject(sourceSelf);
		assertTrue(sourceSelf.tValid());
		assertTrue(sourceValue.tValid());
		assertTrue(targetSelf.tValid());
		assertEquals(2, getCounter(sourceSelf));
		assertEquals(sourceValue, getReference(sourceSelf, attributeName, multiple));
		assertEquals(2, getCounter(targetSelf));
		assertEquals(null, getReference(targetSelf, attributeName, multiple));
		sync(() -> {
			setCounter(sourceSelf, 3);
			setReference(sourceSelf, attributeName, null, multiple);
		});
		assertEquals(3, getCounter(sourceSelf));
		assertEquals(null, getReference(sourceSelf, attributeName, multiple));
		assertEquals(3, getCounter(targetSelf));
		assertEquals(null, getReference(targetSelf, attributeName, multiple));
		// Clean up:
		sync(() -> {
			sourceSelf.tDelete();
			sourceValue.tDelete();
		});
	}

	private void setReference(TLObject self, String attributeName, TLObject value, boolean multiple) {
		self.tUpdateByName(attributeName, wrapValue(value, multiple));
	}

	private Object wrapValue(TLObject value, boolean multiple) {
		if (multiple) {
			if (value == null) {
				return emptySet();
			}
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
