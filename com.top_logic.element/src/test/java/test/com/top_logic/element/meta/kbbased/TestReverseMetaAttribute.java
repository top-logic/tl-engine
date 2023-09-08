/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.meta.OrderedListHelper;
import test.com.top_logic.element.structured.model.ANode;
import test.com.top_logic.element.structured.model.BNode;
import test.com.top_logic.element.structured.model.TestTypesFactory;
import test.com.top_logic.element.util.ElementTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.model.ModelService;

/**
 * The class {@link TestReverseMetaAttribute} tests reverse {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestReverseMetaAttribute extends BasicTestCase {

	public void testDoubleReverse() throws KnowledgeBaseException, ConfigurationException {
		Transaction tx = kb().beginTransaction();
		TestTypesFactory factory = TestTypesFactory.getInstance();
		ANode root = factory.getRootSingleton();
		BNode b1 = (BNode) root.createChild("child1", BNode.B_NODE_TYPE);
		OrderedListHelper.initMandatoryFields(root, b1);
		BNode b2 = (BNode) root.createChild("child2", BNode.B_NODE_TYPE);
		OrderedListHelper.initMandatoryFields(root, b2);
		BNode b3 = (BNode) root.createChild("child3", BNode.B_NODE_TYPE);
		OrderedListHelper.initMandatoryFields(root, b3);
		b1.setStructure(Collections.singleton(b2));
		b2.setStructure(Collections.singleton(b3));
		assertEquals(Collections.singleton(b1), b3.getDoubleStructureReverse());
		tx.commit();
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestReverseMetaAttribute}.
	 */
	public static Test suite() {
		Test t = new TestSuite(TestReverseMetaAttribute.class);
		t = ServiceTestSetup.createSetup(t, ModelService.Module.INSTANCE);
		t = KBSetup.getKBTest(t, KBSetup.DEFAULT_KB);
		t = ElementTestSetup.createElementTestSetup(t);
		return t;
	}
}
