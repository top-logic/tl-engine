/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.dsa.repos.Repository;
import com.top_logic.dsa.repos.file.FileRepository;
import com.top_logic.knowledge.objects.DCMetaData;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.processors.AddPrimitiveMOAttributeProcessor;
import com.top_logic.knowledge.service.migration.processors.FileRepositoryContentReader;
import com.top_logic.knowledge.service.migration.processors.MigrateDocumentContentProcessor;
import com.top_logic.knowledge.service.migration.processors.SQLProcessor;
import com.top_logic.knowledge.wrap.Document;

/**
 * Test of {@link MigrateDocumentContentProcessor} with {@link FileRepositoryContentReader}.
 *
 * <p>
 * The repository is created on disk with {@link FileRepository}. The rows of the document table
 * {@link #TABLE} reference versions of the repository, as documents did before their content was
 * stored in the binary attribute {@link Document#CONTENT}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMigrateDocumentContent extends AbstractBinaryMigrationTest {

	static final String TABLE = "MigDocument";

	private static final String USER = "tester";

	private static final String DOCUMENT_PATH = "_folder/_report.txt";

	private static final String DELETED_PATH = "other/deleted.txt";

	private static final String MIGRATION_SCRIPT =
		"WEB-INF/kbase/migration/tl/Ticket_29715_document_content.migration.xml";

	private File _repositoryDir;

	private File _atticDir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		File base = createdCleanTestDir(getClass().getSimpleName() + "Repository");
		_repositoryDir = new File(base, "repository");
		_atticDir = new File(base, "attic");
	}

	@Override
	protected TypeProvider sourceTypes() {
		return types(false);
	}

	@Override
	protected TypeProvider targetTypes() {
		return types(true);
	}

	private static TypeProvider types(boolean withContent) {
		return (log, typeFactory, typeRepository) -> {
			try {
				// The physical resource is inherited from the knowledge object table.
				String attributes =
					attribute(Document.NAME_ATTRIBUTE, "String")
						+ attribute(DCMetaData.FORMAT, "String")
						+ attribute(Document.VERSION_NUMBER, "Integer")
						+ (withContent
							? "<" + HybridBinaryAttribute.Config.TAG_NAME + " " + DOXMLConstants.ATT_NAME_ATTRIBUTE
								+ "='" + Document.CONTENT + "' " + HybridBinaryAttribute.Config.THRESHOLD + "='1KB'/>"
							: "");
				MetaObjectConfig config = TypedConfiguration.parse(DOXMLConstants.META_OBJECT_ELEMENT,
					MetaObjectConfig.class,
					CharacterContents.newContent("<" + DOXMLConstants.META_OBJECT_ELEMENT + " "
						+ DOXMLConstants.OBJECT_NAME_ATTRIBUTE + "='" + TABLE + "'><attributes>" + attributes
						+ "</attributes></" + DOXMLConstants.META_OBJECT_ELEMENT + ">"));
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

	private static String attribute(String name, String type) {
		return "<" + DOXMLConstants.MO_ATTRIBUTE_ELEMENT + " " + DOXMLConstants.ATT_NAME_ATTRIBUTE + "='" + name
			+ "' " + DOXMLConstants.ATT_TYPE_ATTRIBUTE + "='" + type + "' mandatory='false'/>";
	}

	@Override
	protected MORepository persistentRepository() {
		// The stored schema declares the content attribute, which is added before the content is
		// migrated.
		return kbNode2().getMORepository();
	}

	/**
	 * All revisions of a document receive the content of their repository version, inline or in
	 * the blob store according to the threshold of the attribute.
	 */
	public void testMigrateRevisions() throws Exception {
		byte[] v1 = randomContent(200);
		byte[] v2 = randomContent(5000);
		byte[] deleted = randomContent(300);

		Repository repository = createRepository();
		createVersions(repository, DOCUMENT_PATH, v1, v2);
		createVersions(repository, DELETED_PATH, deleted);
		repository.delete(USER, DELETED_PATH, true);
		assertFalse(repository.existsEntry(DELETED_PATH));

		Transaction tx1 = begin();
		KnowledgeObject document = newA(TABLE, "doc");
		setRow(document, DOCUMENT_PATH, "report.txt", "text/plain", 1);
		KnowledgeObject deletedDocument = newA(TABLE, "deleted");
		setRow(deletedDocument, DELETED_PATH, "deleted.txt", "text/csv", 1);
		KnowledgeObject missing = newA(TABLE, "missing");
		setRow(missing, "missing/none.txt", "none.txt", "text/plain", 1);
		KnowledgeObject mail = newA(TABLE, "mail");
		setRow(mail, "mail://INBOX/1", "mail.txt", "text/plain", 1);
		KnowledgeObject empty = newA(TABLE, "empty");
		setRow(empty, DOCUMENT_PATH, "empty.txt", "text/plain", 0);
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		document.setAttributeValue(Document.VERSION_NUMBER, Integer.valueOf(2));
		document.setAttributeValue(DCMetaData.FORMAT, "text/csv");
		commit(tx2);

		addContentColumns();
		BufferingProtocol log = migrate(processor(), TypedConfiguration.newConfigItem(SchemaConfiguration.class));
		assertTrue(log.getInfos().toString(), log.getInfos().stream()
			.anyMatch(message -> message.contains("rows with content missing in the repository: 1")));

		refetchNode2();
		KnowledgeItem current = node2Item(document);
		BinaryData currentContent = (BinaryData) current.getAttributeValue(Document.CONTENT);
		assertTrue("Content above the threshold is stored in the blob store.",
			currentContent instanceof BlobBinaryData);
		assertContent(v2, "text/csv", "report.txt", currentContent);

		KnowledgeItem historic = HistoryUtils.getKnowledgeItem(
			HistoryUtils.getHistoryManager(kbNode2()).getRevision(r1.getCommitNumber()), current);
		BinaryData historicContent = (BinaryData) historic.getAttributeValue(Document.CONTENT);
		assertFalse("Content below the threshold is stored inline.", historicContent instanceof BlobBinaryData);
		assertContent(v1, "text/plain", "report.txt", historicContent);

		assertContent(deleted, "text/csv", "deleted.txt",
			(BinaryData) node2Item(deletedDocument).getAttributeValue(Document.CONTENT));
		assertNull(node2Item(missing).getAttributeValue(Document.CONTENT));
		assertNull(node2Item(mail).getAttributeValue(Document.CONTENT));
		assertNull(node2Item(empty).getAttributeValue(Document.CONTENT));

		// A second run leaves migrated rows unchanged.
		migrate(processor(), TypedConfiguration.newConfigItem(SchemaConfiguration.class));
		refetchNode2();
		assertEquals(((BlobBinaryData) currentContent).getKey(),
			((BlobBinaryData) node2Item(document).getAttributeValue(Document.CONTENT)).getKey());
	}

	/**
	 * The migration script of the document content parses into the expected processors.
	 */
	public void testMigrationScript() throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(MigrationConfig.class);
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("migration", descriptor));
		File script = new File(ModuleLayoutConstants.WEBAPP_DIR, MIGRATION_SCRIPT);
		MigrationConfig migration = (MigrationConfig) reader
			.setSources(BinaryDataFactory.createBinaryData(script)).read();

		List<? extends PolymorphicConfiguration<?>> processors = migration.getProcessors();
		assertEquals(2, processors.size());

		AddPrimitiveMOAttributeProcessor.Config<?> add = (AddPrimitiveMOAttributeProcessor.Config<?>) processors.get(0);
		assertEquals(Document.OBJECT_NAME, add.getTable());
		assertTrue(add.getAttribute() instanceof HybridBinaryAttribute.Config);
		assertEquals(Document.CONTENT, add.getAttribute().getAttributeName());
		assertTrue(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(add.getAttribute()) instanceof HybridBinaryAttribute);

		MigrateDocumentContentProcessor.Config<?> content =
			(MigrateDocumentContentProcessor.Config<?>) processors.get(1);
		assertEquals(Document.OBJECT_NAME, content.getTable());
		assertEquals(Document.CONTENT, content.getAttribute());
		assertTrue(content.getReader() instanceof FileRepositoryContentReader.Config);
	}

	private String processor() {
		return "<processor class='" + MigrateDocumentContentProcessor.class.getName() + "'"
			+ " " + MigrateDocumentContentProcessor.Config.TABLE + "='" + TABLE + "'>"
			+ "<" + MigrateDocumentContentProcessor.Config.READER + " class='"
			+ FileRepositoryContentReader.class.getName() + "'"
			+ " " + FileRepositoryContentReader.Config.PATH + "='" + _repositoryDir.getAbsolutePath() + "'"
			+ " " + FileRepositoryContentReader.Config.ATTIC + "='" + _atticDir.getAbsolutePath() + "'"
			+ "/></processor>";
	}

	private Repository createRepository() {
		FileRepository.Config<?> config = TypedConfiguration.newConfigItem(FileRepository.Config.class);
		config.setPath(_repositoryDir.getAbsolutePath());
		config.setWorkarea("");
		config.setAttic(_atticDir.getAbsolutePath());
		return (Repository) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static void createVersions(Repository repository, String path, byte[]... versions) throws Exception {
		int slash = path.lastIndexOf('/');
		String folder = path.substring(0, slash);
		String name = path.substring(slash + 1);
		if (!repository.exists(folder)) {
			assertTrue(repository.mkdir("", folder));
		}
		for (int n = 0; n < versions.length; n++) {
			if (n > 0) {
				assertTrue(repository.lock(USER, path));
			}
			assertEquals(n + 1, repository.create(USER, folder, name, new ByteArrayInputStream(versions[n])));
			assertTrue(repository.unlock(USER, path));
		}
	}

	private static void setRow(KnowledgeObject row, String path, String name, String format, int version)
			throws Exception {
		row.setAttributeValue(KOAttributes.PHYSICAL_RESOURCE,
			path.contains(MigrateDocumentContentProcessor.PROTOCOL_SEPARATOR) ? path
				: MigrateDocumentContentProcessor.Config.DEFAULT_PROTOCOL
					+ MigrateDocumentContentProcessor.PROTOCOL_SEPARATOR + path);
		row.setAttributeValue(Document.NAME_ATTRIBUTE, name);
		row.setAttributeValue(DCMetaData.FORMAT, format);
		row.setAttributeValue(Document.VERSION_NUMBER, Integer.valueOf(version));
	}

	/**
	 * Adds the columns of the content attribute to the table, as the migration script does before
	 * migrating the content.
	 */
	private void addContentColumns() throws Exception {
		MOClass target = (MOClass) kbNode2().getMORepository().getMetaObject(TABLE);
		AbstractBinaryAttribute content = (AbstractBinaryAttribute) target.getAttribute(Document.CONTENT);
		String tableName = target.getDBMapping().getDBName();

		PooledConnection connection = kb().getConnectionPool().borrowWriteConnection();
		try {
			SQLProcessor sql = new SQLProcessor(connection);
			for (DBAttribute column : content.getDbMapping()) {
				DBColumn definition = SchemaSetup.createColumn(column);
				sql.execute(addColumn(table(tableName), definition, null));
			}
			connection.commit();
		} finally {
			kb().getConnectionPool().releaseWriteConnection(connection);
		}
	}

	public static Test suite() {
		return binaryMigrationSuite(TestMigrateDocumentContent.class);
	}

}
