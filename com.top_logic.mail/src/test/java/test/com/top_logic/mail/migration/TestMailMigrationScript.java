/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.migration;

import java.io.File;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.processors.AddPrimitiveMOAttributeProcessor;
import com.top_logic.knowledge.service.migration.processors.CopyColumnProcessor;
import com.top_logic.knowledge.service.migration.processors.MigrateDocumentContentProcessor;
import com.top_logic.knowledge.service.migration.processors.RepositoryContentReader;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.mail.base.Mail;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.migration.MailContentReader;

/**
 * Test of the migration script moving the data source names of mails and the content of mail
 * attachments out of the physical resource.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMailMigrationScript extends BasicTestCase {

	private static final String MIGRATION_SCRIPT = "WEB-INF/kbase/migration/tl-mail/Ticket_29715_mail_url.migration.xml";

	/**
	 * The script adds the attribute {@link Mail#MAIL_URL}, copies the physical resource into it and
	 * reads the content of attachments from the mail server.
	 */
	public void testMigrationScript() throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(MigrationConfig.class);
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("migration", descriptor));
		File script = new File(ModuleLayoutConstants.WEBAPP_DIR, MIGRATION_SCRIPT);
		MigrationConfig migration =
			(MigrationConfig) reader.setSources(BinaryDataFactory.createBinaryData(script)).read();

		List<? extends PolymorphicConfiguration<?>> processors = migration.getProcessors();
		assertEquals(3, processors.size());

		AddPrimitiveMOAttributeProcessor.Config<?> add = (AddPrimitiveMOAttributeProcessor.Config<?>) processors.get(0);
		assertEquals(Mail.OBJECT_NAME, add.getTable());
		assertEquals(Mail.MAIL_URL, add.getAttribute().getAttributeName());

		CopyColumnProcessor.Config<?> copy = (CopyColumnProcessor.Config<?>) processors.get(1);
		assertEquals(Mail.OBJECT_NAME, copy.getTable());
		assertEquals(MigrateDocumentContentProcessor.PHYSICAL_RESOURCE, copy.getSource());
		assertEquals(Mail.MAIL_URL, copy.getTarget());

		MigrateDocumentContentProcessor.Config<?> content =
			(MigrateDocumentContentProcessor.Config<?>) processors.get(2);
		assertEquals(Document.OBJECT_NAME, content.getTable());
		assertEquals(Document.CONTENT, content.getAttribute());
		assertEquals(MailFolder.MAIL_DSA_PREFIX,
			content.getProtocol() + MigrateDocumentContentProcessor.PROTOCOL_SEPARATOR);
		assertTrue(content.getReader() instanceof MailContentReader.Config);

		RepositoryContentReader mailReader =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(content.getReader());
		assertFalse("Attachments have content without being updated.", mailReader.isVersioned());
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestMailMigrationScript.class);
	}

}
