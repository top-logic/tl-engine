/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.kafka.knowledge.service.TLSynced;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;

/**
 * {@link TestCase} for {@link TLSynced#getValueMapping()}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLSyncAnnotationInheritance extends AbstractTLSyncTest {

	private static final Class<TestTLSyncAnnotationInheritance> THIS_CLASS = TestTLSyncAnnotationInheritance.class;

	private static final String SOURCE_MODULE_NAME = THIS_CLASS.getName() + ".source";

	private static final String TARGET_MODULE_NAME = THIS_CLASS.getName() + ".target";

	private static final String ENUM_TYPE_NAME = THIS_CLASS.getSimpleName() + "Enum";

	private static final String ENUM_VALUE_FIRST_NAME = "First";

	private static final String ENUM_VALUE_SECOND_NAME = "Second";

	@SuppressWarnings("unused")
	private static final String ENUM_VALUE_THIRD_NAME = "Third";

	private static final String SOURCE_TYPE_NAME = "SourceType";

	private static final String TARGET_TYPE_NAME = "TargetType";

	private static final String TEST_DEFAULT_REFERENCE_NAME = "defaultReferenceEnum";

	private static final String TEST_INLINE_REFERENCE_NAME = "inlineReferenceEnum";

	private static final String TEST_ASSOCIATION_REFERENCE_NAME = "associationReferenceEnum";

	public void testDefaultReference() {
		testAttributeSync(set(getFirstEnumValue(), getSecondEnumValue()), TEST_DEFAULT_REFERENCE_NAME);
	}

	public void testInlineReference() {
		testAttributeSync(getSecondEnumValue(), TEST_INLINE_REFERENCE_NAME);
	}

	public void testAssociationReference() {
		testAttributeSync(set(getFirstEnumValue(), getSecondEnumValue()), TEST_ASSOCIATION_REFERENCE_NAME);
	}

	protected void testAttributeSync(Object exampleValue, String exampleAttribute) {
		TLObject source = sync(() -> createObject());
		TLObject target = findTargetObject(source);
		assertNotEquals(exampleValue, target.tValueByName(exampleAttribute));
		sync(() -> source.tUpdateByName(exampleAttribute, exampleValue));
		assertEquals(exampleValue, target.tValueByName(exampleAttribute));
		// Cleanup:
		sync(() -> source.tDelete());
	}

	private TLObject findTargetObject(TLObject source) {
		return findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), source);
	}

	private TLObject createObject() {
		return newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME);
	}

	private TLClassifier getFirstEnumValue() {
		return getExampleEnum().getClassifier(ENUM_VALUE_FIRST_NAME);
	}

	private TLClassifier getSecondEnumValue() {
		return getExampleEnum().getClassifier(ENUM_VALUE_SECOND_NAME);
	}

	private TLEnumeration getExampleEnum() {
		return FastList.getFastList(ENUM_TYPE_NAME);
	}

	/**
	 * Creates a {@link TestSuite} for all the tests in {@link TestTLSyncAnnotationInheritance}.
	 */
	public static Test suite() {
		return suite(THIS_CLASS);
	}

}
