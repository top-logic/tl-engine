/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.kafka.knowledge.service.TLSynced;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for {@link TLSynced#getValueMapping()}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLSyncValueMappings extends AbstractTLSyncTest {

	private static final String SOURCE_MODULE_NAME = TestTLSyncValueMappings.class.getName() + ".source";

	private static final String TARGET_MODULE_NAME = TestTLSyncValueMappings.class.getName() + ".target";

	private static final String ENUM_TYPE_NAME = "EnumType";

	@SuppressWarnings("unused")
	private static final String ENUM_VALUE_FIRST_NAME = "First";

	private static final String ENUM_VALUE_SECOND_NAME = "Second";

	@SuppressWarnings("unused")
	private static final String ENUM_VALUE_THIRD_NAME = "Third";

	private static final String SOURCE_TYPE_NAME = "SourceType";

	private static final String TARGET_TYPE_NAME = "TargetType";

	private static final String TEST_TL_TYPE_ATTRIBUTE_NAME = "testTlType";

	private static final String TEST_TL_ATTRIBUTE_ATTRIBUTE_NAME = "testTlAttribute";

	private static final String TEST_TL_ENUM_ATTRIBUTE_NAME = "testTlEnum";

	public void testTLClassViaQualifiedName() {
		testAttributeSync(getExampleType(), TEST_TL_TYPE_ATTRIBUTE_NAME);
	}

	public void testTLAttributeViaQualifiedName() {
		testAttributeSync(getExampleAttribute(), TEST_TL_ATTRIBUTE_ATTRIBUTE_NAME);
	}

	public void testTLEnumViaQualifiedName() {
		testAttributeSync(getExampleEnum(), TEST_TL_ENUM_ATTRIBUTE_NAME);
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

	private TLClassifier getExampleEnum() {
		return FastList.getFastList(ENUM_TYPE_NAME).getClassifier(ENUM_VALUE_SECOND_NAME);
	}

	private TLClassPart getExampleAttribute() {
		return getAttribute(
			StructuredElement.MODULE_NAME, StructuredElement.TL_TYPE_NAME, StructuredElement.PARENT_ATTR);
	}

	private TLClass getExampleType() {
		return getClass(StructuredElement.MODULE_NAME, StructuredElement.TL_TYPE_NAME);
	}

	private TLClassPart getAttribute(String module, String type, String attribute) {
		return (TLClassPart) getClass(module, type).getPart(attribute);
	}

	private TLClass getClass(String module, String type) {
		return (TLClass) getModule(module).getType(type);
	}

	private TLModule getModule(String module) {
		return ModelService.getApplicationModel().getModule(module);
	}

	/**
	 * Creates a {@link TestSuite} for all the tests in {@link TestTLSyncValueMappings}.
	 */
	public static Test suite() {
		return suite(TestTLSyncValueMappings.class);
	}

}
