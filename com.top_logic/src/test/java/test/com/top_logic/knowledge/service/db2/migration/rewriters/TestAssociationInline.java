/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.rewriters;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseMigrationTest;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DestinationReference;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.knowledge.service.db2.migration.rewriters.AssociationInline;

/**
 * Tests for {@link AssociationInline}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestAssociationInline extends AbstractDBKnowledgeBaseMigrationTest {

	public void testInlineAssociation() throws DataObjectException {
		Transaction tx = kb().beginTransaction();

		KnowledgeObject source = newE("e1");
		KnowledgeObject dest = newE("e2");
		KnowledgeAssociation association = newAB(source, dest);
		KnowledgeAssociation untouchedAssociation = newBC(dest, source);
		assertNotEquals(association.tTable().getName(), untouchedAssociation.tTable().getName());

		tx.commit();

		AssociationInline.Config inlineConfig = TypedConfiguration.newConfigItem(AssociationInline.Config.class);
		inlineConfig.setTypeNames(set(association.tTable().getName()));
		inlineConfig.setTargetObject(SourceReference.REFERENCE_SOURCE_NAME);
		inlineConfig.setReferenceValue(DestinationReference.REFERENCE_DEST_NAME);
		inlineConfig.setReferenceName(KnowledgeBaseTestScenarioConstants.UNTYPED_POLY_CUR_GLOBAL_NAME);
		EventRewriter inliner = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(inlineConfig);
		doMigration(list(inliner), tx.getCommitRevision());

		updateSessionRevisionNode2();
		KnowledgeItem node2Source = node2Item(source);
		KnowledgeItem node2Dest = node2Item(dest);

		assertEquals("Association was inlined", node2Dest,
			node2Source.getAttributeValue(KnowledgeBaseTestScenarioConstants.UNTYPED_POLY_CUR_GLOBAL_NAME));
		assertNull("Association has to be skipped", node2Item(association));

		assertNull("Association was not inlined",
			node2Dest.getAttributeValue(KnowledgeBaseTestScenarioConstants.UNTYPED_POLY_CUR_GLOBAL_NAME));
		assertNotNull("Association has not to be skipped", node2Item(untouchedAssociation));

		tx = kb().beginTransaction();
		association.delete();
		tx.commit();

		doMigration(list(inliner), tx.getCommitRevision());

		updateSessionRevisionNode2();
		assertNull("Association was deleted",
			node2Source.getAttributeValue(KnowledgeBaseTestScenarioConstants.UNTYPED_POLY_CUR_GLOBAL_NAME));

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestAssociationInline}.
	 */
	public static Test suite() {
		return suite(TestAssociationInline.class);
	}

}

