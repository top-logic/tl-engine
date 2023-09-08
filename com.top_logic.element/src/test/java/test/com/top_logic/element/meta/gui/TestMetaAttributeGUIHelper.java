/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.gui;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.structured.model.TestTypesFactory;
import test.com.top_logic.element.util.ElementTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.layout.form.FormMember;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link MetaAttributeGUIHelper}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestMetaAttributeGUIHelper extends BasicTestCase {

	/**
	 * Tests that overridden attributes produces the same {@link FormMember} names.
	 */
	public void testFormMemberNames() {
		TLStructuredTypePart originalAttribute = TestTypesFactory.getAs1AAttr();
		TLStructuredTypePart overriddenAttribute = TestTypesFactory.getAs1ANodeAttr();
		assertNotSame("Test that for different attributes the same name is created.", originalAttribute,
			overriddenAttribute);
		assertSame(originalAttribute.getDefinition(), overriddenAttribute.getDefinition());
		String memberName = MetaAttributeGUIHelper.getAttributeIDCreate(originalAttribute);
		String overriddenMemberName = MetaAttributeGUIHelper.getAttributeIDCreate(overriddenAttribute);
		assertEquals(memberName, overriddenMemberName);
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestMetaAttributeGUIHelper}.
	 */
	public static Test suite() {
		Test t = new TestSuite(TestMetaAttributeGUIHelper.class);
		t = ServiceTestSetup.createSetup(t, ModelService.Module.INSTANCE);
		t = KBSetup.getKBTest(t, KBSetup.DEFAULT_KB);
		t = ElementTestSetup.createElementTestSetup(t);
		return t;
	}

}

