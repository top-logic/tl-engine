/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.diff;

import junit.framework.Test;

import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;

/**
 * Tests override attributes changing "mandatory".
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestOverrideAttributes extends AbstractModelPatchTest {

	public void testOverrideMandatory() {
		TLModel model;
		try (Transaction tx = kb().beginTransaction()) {
			model = loadModel("test-override-mandatory.model.xml");
			tx.commit();
		}
		TLModule module = model.getModule("m0");
		TLClass base = (TLClass) module.getType("Base");
		TLClass ext = (TLClass) module.getType("Ext1");
		assertTrue("Attribute is declared as 'mandatory'.", base.getPartOrFail("a_mandatory").isMandatory());
		assertTrue("Overriding attribute inherits 'mandatory'.", ext.getPartOrFail("a_mandatory").isMandatory());

		assertFalse("Attribute is not declared as 'mandatory'.", base.getPartOrFail("a_not_mandatory").isMandatory());
		assertFalse("Overriding attribute inherits 'not mandatory'.",
			ext.getPartOrFail("a_not_mandatory").isMandatory());

		assertFalse("Attribute is not declared as 'mandatory'.", base.getPartOrFail("a").isMandatory());
		assertTrue("Overriding attribute declares 'mandatory'.", ext.getPartOrFail("a").isMandatory());
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(TestOverrideAttributes.class);
	}

}
