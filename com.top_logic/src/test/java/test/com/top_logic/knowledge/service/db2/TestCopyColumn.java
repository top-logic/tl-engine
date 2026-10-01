/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.migration.processors.CopyColumnProcessor;

/**
 * Test of {@link CopyColumnProcessor}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestCopyColumn extends AbstractBinaryMigrationTest {

	private static final String TABLE = "CopyTable";

	private static final String SOURCE = "physicalResource";

	private static final String TARGET = "mailURL";

	@Override
	protected TypeProvider sourceTypes() {
		return types();
	}

	@Override
	protected TypeProvider targetTypes() {
		return types();
	}

	private static TypeProvider types() {
		return (log, typeFactory, typeRepository) -> {
			try {
				MetaObjectConfig config = TypedConfiguration.parse(DOXMLConstants.META_OBJECT_ELEMENT,
					MetaObjectConfig.class,
					CharacterContents.newContent("<" + DOXMLConstants.META_OBJECT_ELEMENT + " "
						+ DOXMLConstants.OBJECT_NAME_ATTRIBUTE + "='" + TABLE + "'><attributes>"
						+ attribute(SOURCE) + attribute(TARGET) + "</attributes></"
						+ DOXMLConstants.META_OBJECT_ELEMENT + ">"));
				MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(TABLE);
				type.setSuperclass(new DeferredMetaObject(B_NAME));
				for (AttributeConfig attributeConfig : config.getAttributes()) {
					type.addAttribute(
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(attributeConfig));
				}
				typeRepository.addMetaObject(type);
			} catch (Exception ex) {
				throw new AssertionError("Creating test types failed.", ex);
			}
		};
	}

	private static String attribute(String name) {
		return "<" + DOXMLConstants.MO_ATTRIBUTE_ELEMENT + " " + DOXMLConstants.ATT_NAME_ATTRIBUTE + "='" + name
			+ "' " + DOXMLConstants.ATT_TYPE_ATTRIBUTE + "='String' mandatory='false'/>";
	}

	/**
	 * Values of all revisions are copied, rows without source value keep their target value.
	 */
	public void testCopy() throws Exception {
		Transaction tx1 = begin();
		KnowledgeObject mail = newA(TABLE, "mail");
		mail.setAttributeValue(SOURCE, "mail://INBOX?1");
		KnowledgeObject empty = newA(TABLE, "empty");
		empty.setAttributeValue(TARGET, "kept");
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		mail.setAttributeValue(SOURCE, "mail://INBOX?2");
		commit(tx2);

		BufferingProtocol log = migrate(processor(), TypedConfiguration.newConfigItem(SchemaConfiguration.class));
		assertTrue(log.getInfos().toString(), log.getInfos().stream()
			.anyMatch(message -> message.contains("in 2 rows")));

		refetchNode2();
		KnowledgeItem current = node2Item(mail);
		assertEquals("mail://INBOX?2", current.getAttributeValue(TARGET));
		KnowledgeItem historic = HistoryUtils.getKnowledgeItem(
			HistoryUtils.getHistoryManager(kbNode2()).getRevision(r1.getCommitNumber()), current);
		assertEquals("mail://INBOX?1", historic.getAttributeValue(TARGET));
		assertEquals("kept", node2Item(empty).getAttributeValue(TARGET));
	}

	private static String processor() {
		return "<processor class='" + CopyColumnProcessor.class.getName() + "'"
			+ " " + CopyColumnProcessor.Config.TABLE + "='" + TABLE + "'"
			+ " " + CopyColumnProcessor.Config.SOURCE + "='" + SOURCE + "'"
			+ " " + CopyColumnProcessor.Config.TARGET + "='" + TARGET + "'/>";
	}

	public static Test suite() {
		return binaryMigrationSuite(TestCopyColumn.class);
	}

}
