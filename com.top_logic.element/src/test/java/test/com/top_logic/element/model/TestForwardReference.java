/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ThreadContextDecorator;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLReference;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link TLModelUtil#addForwardReference(TLClass, String, com.top_logic.model.TLType)}.
 */
@SuppressWarnings("javadoc")
public class TestForwardReference extends BasicTestCase {

	private static final String MODULE = "test.forwardReference";

	private static final String REFERENCE = "target";

	private static final String SOURCE = "Source";

	private static final String TARGET = "Target";

	public void testCreate() {
		TLModule module;
		TLClass source;
		TLReference reference;
		try (Transaction tx = kbTransaction()) {
			module = TLModelUtil.addModule(ModelService.getApplicationModel(), MODULE);
			source = TLModelUtil.addClass(module, SOURCE);
			TLClass target = TLModelUtil.addClass(module, TARGET);
			reference = TLModelUtil.addForwardReference(source, REFERENCE, target);
			tx.commit();
		}

		TLAssociationEnd end = reference.getEnd();
		TLAssociation association = end.getOwner();
		assertEquals(TLStructuredTypeColumns.syntheticAssociationName(SOURCE, REFERENCE), association.getName());
		assertEquals(1, end.getEndIndex());
		assertEquals(REFERENCE, end.getName());
		assertTrue(end.canNavigate());
		assertFalse(end.isComposite());
		assertFalse(end.isAggregate());
		assertFalse(reference.isBackwards());

		TLAssociationEnd sourceEnd = TLModelUtil.getOtherEnd(end);
		assertEquals(TLStructuredTypeColumns.SELF_ASSOCIATION_END_NAME, sourceEnd.getName());
		assertEquals(source, sourceEnd.getType());
		assertTrue(sourceEnd.isMultiple());
		assertFalse(sourceEnd.isAggregate());

		assertFalse(reference.isMandatory());
		assertFalse(reference.isMultiple());

		try (Transaction tx = kbTransaction()) {
			TLModelUtil.deleteRecursive(module);
			tx.commit();
		}
	}

	private static Transaction kbTransaction() {
		return PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("TestForwardReference"));
	}

	public static Test suite() {
		TestSuite innerSuite = new TestSuite(TestForwardReference.class);
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(ThreadContextDecorator.INSTANCE, innerSuite,
				DynamicModelService.Module.INSTANCE));
	}

}
