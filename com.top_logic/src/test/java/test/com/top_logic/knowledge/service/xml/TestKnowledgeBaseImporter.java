/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.xml;

import java.util.Iterator;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.xml.KnowledgeBaseImporter;

/**
 * Tests the {@link KnowledgeBaseImporter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestKnowledgeBaseImporter extends BasicTestCase {
	
	private String getImportFile(String resource) {
		return "webinf://kbase/" + TestKnowledgeBaseImporter.class.getSimpleName() + "/" + resource;
	}

	public void testImport() throws DataObjectException {
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		try {
			KnowledgeBaseImporter.importObjects(kb, getImportFile("KBData.xml"), false, new AssertProtocol());
			checkImport(kb);
		} finally {
			tx.rollback();
		}
	}

	private void checkImport(KnowledgeBase kb) throws DataObjectException {
		Iterator<KnowledgeItem> sources =
			kb.getObjectsByAttribute(KBTestMeta.TEST_B, KBTestMeta.TEST_A_NAME, "TestBSourceName");
		assertTrue("No import of source object.", sources.hasNext());
		KnowledgeItem importedSource = sources.next();
		assertFalse("More than one object with given name. Test may not be stable.", sources.hasNext());

		Iterator<KnowledgeItem> dests =
			kb.getObjectsByAttribute(KBTestMeta.TEST_B, KBTestMeta.TEST_A_NAME, "TestBDestName");
		assertTrue("No import of destination object.", dests.hasNext());
		KnowledgeItem importedDest = dests.next();
		assertFalse("More than one object with given name. Test may not be stable.", dests.hasNext());

		Iterator<KnowledgeAssociation> associations =
			((KnowledgeObject) importedSource).getOutgoingAssociations(KBTestMeta.ASSOC_A);
		assertTrue("No import of association with source " + importedSource + ".", associations.hasNext());
		KnowledgeAssociation importedAssocation = associations.next();
		assertFalse("More than one object with given name. Test may not be stable.", dests.hasNext());
		assertEquals(importedDest, importedAssocation.getDestinationObject());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestKnowledgeBaseImporter}.
	 */
	public static Test suite() {
		return KBSetup.getKBTest(TestKnowledgeBaseImporter.class);
	}
}

