/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.rewriters;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseMigrationTest;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.migration.rewriters.AttributeRewriter;
import com.top_logic.knowledge.service.db2.migration.rewriters.PatternMatchValueChange;

/**
 * Test for {@link PatternMatchValueChange}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPatternMatchValueChange extends AbstractDBKnowledgeBaseMigrationTest {

	public void testAttributeFilter() throws DataObjectException {
		Transaction tx = kb().beginTransaction();

		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(B1_NAME, "value1");
		KnowledgeObject b2 = newB("b2");
		b2.setAttributeValue(B1_NAME, "other_value");
		KnowledgeObject c = newC("c1");
		c.setAttributeValue(B1_NAME, "value2");
		tx.commit();

		AttributeRewriter.Config rewriterConf = TypedConfiguration.newConfigItem(AttributeRewriter.Config.class);
		rewriterConf.setImplementationClass(AttributeRewriter.class);
		rewriterConf.setTypeNames(set(b1.tTable().getName()));

		PatternMatchValueChange.Config config = TypedConfiguration.newConfigItem(PatternMatchValueChange.Config.class);
		config.setImplementationClass(PatternMatchValueChange.class);
		config.setAttribute(B1_NAME);
		config.setOldValuePattern("value(\\d)");
		config.setNewValuePattern("value$1_new");
		rewriterConf.setAlgorithm(config);
		EventRewriter rewriter = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(rewriterConf);
		doMigration(list(rewriter));

		updateSessionRevisionNode2();

		KnowledgeItem b1Node2 = node2Item(b1);
		assertEquals("b1 is rewritten", "value1_new", b1Node2.getAttributeValue(B1_NAME));
		KnowledgeItem b2Node2 = node2Item(b2);
		assertEquals("b2 is not rewritten. Wrong value.", b2.getAttributeValue(B1_NAME),
			b2Node2.getAttributeValue(B1_NAME));
		KnowledgeItem cNode2 = node2Item(c);
		assertEquals("Type C is not rewritten.", c.getAttributeValue(B1_NAME), cNode2.getAttributeValue(B1_NAME));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPatternMatchValueChange}.
	 */
	public static Test suite() {
		return suite(TestPatternMatchValueChange.class);
	}

}
