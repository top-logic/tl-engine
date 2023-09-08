/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.rewriters;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseMigrationTest;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.migration.rewriters.AttributePatternValueConversion;

/**
 * Test for {@link AttributePatternValueConversion}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestAttributePatternValueConversion extends AbstractDBKnowledgeBaseMigrationTest {

	public static final class TestMapping implements Mapping<Object, String> {

		/** Singleton {@link TestMapping} instance. */
		public static final TestMapping INSTANCE = new TestMapping();

		@Override
		public String map(Object input) {
			if (input == null) {
				return null;
			}
			return new StringBuilder(input.toString()).reverse().toString();
		}

	}

	public void testAttributeFilter() throws DataObjectException {
		Transaction tx = kb().beginTransaction();

		KnowledgeObject b = newB("b1");
		b.setAttributeValue(B1_NAME, "b1Val");
		b.setAttributeValue(B2_NAME, "b2Val");
		b.setAttributeValue(B3_NAME, "b3Val");
		KnowledgeObject c = newC("c1");
		c.setAttributeValue(B1_NAME, "b1Val");
		c.setAttributeValue(B2_NAME, "b2Val");
		c.setAttributeValue(B3_NAME, "b3Val");
		tx.commit();

		AttributePatternValueConversion.Config config =
			TypedConfiguration.newConfigItem(AttributePatternValueConversion.Config.class);
		config.setImplementationClass(AttributePatternValueConversion.class);
		config.setTypeNames(set(b.tTable().getName()));
		config.setAttributePattern("b(1|2)");
		PolymorphicConfiguration<Mapping<Object, ?>> mapping =
			TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		mapping.setImplementationClass(TestMapping.class);
		config.setValueMapping(mapping);
		EventRewriter rewriter = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		doMigration(list(rewriter));

		updateSessionRevisionNode2();

		KnowledgeItem bNode2 = node2Item(b);
		assertEquals("b1 is rewritten.", TestMapping.INSTANCE.map(b.getAttributeValue(B1_NAME)),
			bNode2.getAttributeValue(B1_NAME));
		assertEquals("b2 is rewritten.", TestMapping.INSTANCE.map(b.getAttributeValue(B2_NAME)),
			bNode2.getAttributeValue(B2_NAME));
		assertEquals("b3 is not rewritten.", b.getAttributeValue(B3_NAME), bNode2.getAttributeValue(B3_NAME));
		KnowledgeItem cNode2 = node2Item(c);
		assertEquals("Type C is not rewritten.", c.getAttributeValue(B1_NAME), cNode2.getAttributeValue(B1_NAME));
		assertEquals("Type C is not rewritten.", c.getAttributeValue(B2_NAME), cNode2.getAttributeValue(B2_NAME));
		assertEquals("Type C is not rewritten.", c.getAttributeValue(B3_NAME), cNode2.getAttributeValue(B3_NAME));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestAttributePatternValueConversion}.
	 */
	public static Test suite() {
		return suite(TestAttributePatternValueConversion.class);
	}

}
