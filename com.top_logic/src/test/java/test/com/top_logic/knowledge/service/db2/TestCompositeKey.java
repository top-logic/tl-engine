/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.dummy.DummyKnowledgeObject;

import com.top_logic.basic.StringID;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.service.db2.CompositeKey;

/**
 * Test case for {@link CompositeKey}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestCompositeKey extends TestCase {

	public void testEquality() throws DataObjectException {
		MOAttributeImpl x = new MOAttributeImpl("x", MOPrimitive.STRING);
		MOAttributeImpl y = new MOAttributeImpl("y", MOPrimitive.LONG);
		MOAttributeImpl z = new MOAttributeImpl("z", MOPrimitive.LONG);

		MOClass type = new MOClassImpl("A");
		type.addAttribute(y);
		type.addAttribute(x);
		type.addAttribute(z);

		type.freeze();

		DummyKnowledgeObject a1 = DummyKnowledgeObject.item(StringID.valueOf("a1"), type);
		a1.setAttributeValue("x", "xxx");
		a1.setAttributeValue("y", 42L);
		a1.setAttributeValue("z", null);

		DummyKnowledgeObject a2 = DummyKnowledgeObject.item(StringID.valueOf("a2"), type);
		a2.setAttributeValue("x", "xxx");
		a2.setAttributeValue("y", null);
		a2.setAttributeValue("z", 42L);

		DummyKnowledgeObject a3 = DummyKnowledgeObject.item(StringID.valueOf("a3"), type);
		a3.setAttributeValue("x", "xxx");
		a3.setAttributeValue("y", 42L);
		a3.setAttributeValue("z", 42L);

		CompositeKey a1KeyXY = createCompositeKey(a1, Arrays.<MOAttribute> asList(x, y));
		CompositeKey a1KeyXYZ = createCompositeKey(a1, Arrays.<MOAttribute> asList(x, y, z));
		CompositeKey a1KeyXYZ1 = createCompositeKey(a1, Arrays.<MOAttribute> asList(x, y, z));
		CompositeKey a1KeyXZ = createCompositeKey(a1, Arrays.<MOAttribute> asList(x, z));

		CompositeKey a2KeyXY = createCompositeKey(a2, Arrays.<MOAttribute> asList(x, y));
		CompositeKey a2KeyXZ = createCompositeKey(a2, Arrays.<MOAttribute> asList(x, z));

		CompositeKey a3KeyXY = createCompositeKey(a3, Arrays.<MOAttribute> asList(x, y));
		CompositeKey a3KeyXZ = createCompositeKey(a3, Arrays.<MOAttribute> asList(x, z));

		assertEqualsKey("Identical key.", a1KeyXY, a1KeyXY);
		assertEqualsKey("Key for same object.", a1KeyXYZ, a1KeyXYZ1);

		BasicTestCase.assertNotEquals("Different values in key.", a1KeyXY, a2KeyXY);
		BasicTestCase.assertNotEquals("Different size.", a1KeyXY, a1KeyXYZ);
		BasicTestCase.assertNotEquals("Null compare.", a1KeyXY, null);
		BasicTestCase.assertNotEquals("Other type compare.", a1KeyXY, "foobar");

		// Equality is not based on the type, but only on the values.
		assertEqualsKey("Same values, different attributes.", a1KeyXY, a2KeyXZ);

		assertEqualsKey("Same values, same attributes.", a1KeyXY, a3KeyXY);
		assertEqualsKey("Same values, same attributes.", a2KeyXZ, a3KeyXZ);

		BasicTestCase.assertNotEquals(a1KeyXZ, a3KeyXY);
		BasicTestCase.assertNotEquals(a1KeyXZ, a3KeyXZ);
	}

	private CompositeKey createCompositeKey(DummyKnowledgeObject item, List<MOAttribute> attributes) {
		Object[] storage = item.getStorage();
		int size = attributes.size();
		Object[] keyStore = new Object[size];
		for (int i = 0; i < size; i++) {
			MOAttribute attribute = attributes.get(i);
			Object attributeValue = attribute.getStorage().getCacheValue(attribute, item, storage);
			keyStore[i] = attributeValue;
		}
		return new CompositeKey(keyStore);
	}

	private static void assertEqualsKey(String message, Object o1, Object o2) {
		BasicTestCase.assertEquals(message, o1, o2);
		BasicTestCase.assertEquals(message, o1.hashCode(), o2.hashCode());
	}

}
