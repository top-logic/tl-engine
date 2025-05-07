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
 * Tests override attributes changing "mandatory" and "abstract".
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestOverrideAttributes extends AbstractModelPatchTest {

	public void testOverrideMandatory() {
		TLModel model;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
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

	public void testOverrideAbstract() {
		TLModel model;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			model = loadModel("test-override-abstract.model.xml");
			tx.commit();
		}
		TLModule module = model.getModule("m0");
		TLClass base = (TLClass) module.getType("Base");
		TLClass ext1 = (TLClass) module.getType("Ext1");
		TLClass ext2 = (TLClass) module.getType("Ext2");
		assertTrue("Attribute is declared as 'abstract'.", base.getPartOrFail("a").isAbstract());
		assertFalse("Attribute override is not declared as 'abstract'.", ext1.getPartOrFail("a").isAbstract());
		assertFalse("Attribute override is not declared as 'abstract'.", ext2.getPartOrFail("a").isAbstract());

		assertTrue("Attribute is declared as 'abstract'.", base.getPartOrFail("b").isAbstract());
		assertTrue("Attribute override is declared as 'abstract'.", ext1.getPartOrFail("b").isAbstract());
		assertFalse("Attribute override is not declared as 'abstract'.", ext2.getPartOrFail("b").isAbstract());
	}

	public void testMakeAttributeAbstract() {
		TLModel model;
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			model = loadModel("test-make-abstract-left.model.xml");
			tx.commit();
		}
		TLClass baseA = (TLClass) model.getModule("m0").getType("A");
		TLClass baseB = (TLClass) model.getModule("m0").getType("B");
		assertFalse(baseA.getPartOrFail("a").isAbstract());
		assertFalse(baseB.getPartOrFail("a").isAbstract());

		TLModel newModel = loadModelTransient("test-make-abstract-right.model.xml");
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			applyDiff(model, newModel);
			tx.commit();
		}

		TLClass incrementedA = (TLClass) model.getModule("m0").getType("A");
		TLClass incrementedB = (TLClass) model.getModule("m0").getType("B");
		assertTrue(incrementedA.getPartOrFail("a").isAbstract());
		assertFalse(incrementedB.getPartOrFail("a").isAbstract());
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(TestOverrideAttributes.class);
	}

}
