/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.migration.DropColumnProcessor;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.processors.MigrateDocumentContentProcessor;

/**
 * Test of dropping the physical resource column with the {@link DropColumnProcessor} of the
 * migration script {@value #MIGRATION_SCRIPT}.
 *
 * <p>
 * The column is declared on the abstract table {@link #BASE} and is dropped from all its concrete
 * tables, the declaration is removed from the stored schema. The table {@link #OTHER} is no
 * subtype of {@link #BASE} and keeps its columns.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDropPhysicalResource extends AbstractBinaryMigrationTest {

	static final String MIGRATION_SCRIPT = "WEB-INF/kbase/migration/tl/Ticket_29715_drop_physical_resource.migration.xml";

	static final String BASE = "DropBase";

	private static final String TABLE_A = "DropA";

	private static final String TABLE_B = "DropB";

	static final String OTHER = "DropOther";

	private static final String RESOURCE = MigrateDocumentContentProcessor.PHYSICAL_RESOURCE;

	private static final String LABEL = "label";

	@Override
	protected TypeProvider sourceTypes() {
		return types(true);
	}

	@Override
	protected TypeProvider targetTypes() {
		return types(false);
	}

	private static TypeProvider types(boolean withResource) {
		return (log, typeFactory, typeRepository) -> {
			try {
				MOKnowledgeItemImpl base = new MOKnowledgeItemImpl(BASE);
				base.setAbstract(true);
				base.setSuperclass(new DeferredMetaObject(B_NAME));
				if (withResource) {
					addAttributes(base, attribute(RESOURCE));
				}
				typeRepository.addMetaObject(base);

				for (String name : List.of(TABLE_A, TABLE_B)) {
					MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(name);
					type.setSuperclass(new DeferredMetaObject(BASE));
					addAttributes(type, attribute(LABEL));
					typeRepository.addMetaObject(type);
				}

				MOKnowledgeItemImpl other = new MOKnowledgeItemImpl(OTHER);
				other.setSuperclass(new DeferredMetaObject(B_NAME));
				addAttributes(other, attribute(RESOURCE) + attribute(LABEL));
				typeRepository.addMetaObject(other);
			} catch (Exception ex) {
				throw new AssertionError("Creating test types failed.", ex);
			}
		};
	}

	private static void addAttributes(MOClass type, String attributes) throws Exception {
		for (AttributeConfig config : parse(type.getName(), null, false, attributes).getAttributes()) {
			type.addAttribute(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config));
		}
	}

	private static MetaObjectConfig parse(String name, String superClass, boolean isAbstract, String attributes)
			throws Exception {
		return TypedConfiguration.parse(DOXMLConstants.META_OBJECT_ELEMENT, MetaObjectConfig.class,
			CharacterContents.newContent("<" + DOXMLConstants.META_OBJECT_ELEMENT + " "
				+ DOXMLConstants.OBJECT_NAME_ATTRIBUTE + "='" + name + "'"
				+ (superClass != null ? " " + DOXMLConstants.SUPERCLASS_ATTRIBUTE + "='" + superClass + "'" : "")
				+ (isAbstract ? " " + DOXMLConstants.ABSTRACT_ATTRIBUTE + "='true'" : "")
				+ "><attributes>" + attributes + "</attributes></" + DOXMLConstants.META_OBJECT_ELEMENT + ">"));
	}

	private static String attribute(String name) {
		return "<" + DOXMLConstants.MO_ATTRIBUTE_ELEMENT + " " + DOXMLConstants.ATT_NAME_ATTRIBUTE + "='" + name
			+ "' " + DOXMLConstants.ATT_TYPE_ATTRIBUTE + "='String' mandatory='false'/>";
	}

	/**
	 * The schema stored in the database before the migration.
	 */
	private static SchemaConfiguration storedSchema() throws Exception {
		MetaObjectsConfig types = TypedConfiguration.newConfigItem(MetaObjectsConfig.class);
		types.getTypes().put(BASE, parse(BASE, null, true, attribute(RESOURCE)));
		types.getTypes().put(TABLE_A, parse(TABLE_A, BASE, false, attribute(LABEL)));
		types.getTypes().put(TABLE_B, parse(TABLE_B, BASE, false, attribute(LABEL)));
		types.getTypes().put(OTHER, parse(OTHER, null, false, attribute(RESOURCE) + attribute(LABEL)));

		SchemaConfiguration schema = TypedConfiguration.newConfigItem(SchemaConfiguration.class);
		schema.setMetaObjects(types);
		return schema;
	}

	/**
	 * The column is dropped from all tables of the base type and removed from the stored schema;
	 * the remaining values of the rows, including history, stay readable.
	 */
	public void testDropColumn() throws Exception {
		Transaction tx1 = begin();
		KnowledgeObject a = newA(TABLE_A, "a");
		a.setAttributeValue(RESOURCE, "repository://a.txt");
		a.setAttributeValue(LABEL, "A1");
		KnowledgeObject b = newA(TABLE_B, "b");
		b.setAttributeValue(RESOURCE, "mail://INBOX?1");
		b.setAttributeValue(LABEL, "B1");
		KnowledgeObject other = newA(OTHER, "other");
		other.setAttributeValue(RESOURCE, "kept");
		commit(tx1);

		Transaction tx2 = begin();
		a.setAttributeValue(LABEL, "A2");
		commit(tx2);

		assertTrue(hasColumn(TABLE_A, RESOURCE));
		assertTrue(hasColumn(TABLE_B, RESOURCE));

		SchemaConfiguration schema = storedSchema();
		BufferingProtocol log = migrate(processor(), schema);
		assertTrue(log.getInfos().toString(),
			log.getInfos().stream().anyMatch(message -> message.contains("Dropping column")));

		assertFalse(hasColumn(TABLE_A, RESOURCE));
		assertFalse(hasColumn(TABLE_B, RESOURCE));
		assertTrue(hasColumn(TABLE_A, LABEL));
		assertTrue(hasColumn(OTHER, RESOURCE));

		MetaObjectConfig storedBase = (MetaObjectConfig) schema.getMetaObjects().getTypes().get(BASE);
		assertTrue(storedBase.getAttributes().isEmpty());
		MetaObjectConfig storedOther = (MetaObjectConfig) schema.getMetaObjects().getTypes().get(OTHER);
		assertEquals(2, storedOther.getAttributes().size());

		refetchNode2();
		assertEquals("A2", node2Item(a).getAttributeValue(LABEL));
		assertEquals("B1", node2Item(b).getAttributeValue(LABEL));
		assertEquals("kept", node2Item(other).getAttributeValue(RESOURCE));
	}

	/**
	 * The migration script drops the physical resource declared on the knowledge object table.
	 */
	public void testMigrationScript() throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(MigrationConfig.class);
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("migration", descriptor));
		File script = new File(ModuleLayoutConstants.WEBAPP_DIR, MIGRATION_SCRIPT);
		MigrationConfig migration =
			(MigrationConfig) reader.setSources(BinaryDataFactory.createBinaryData(script)).read();

		List<? extends PolymorphicConfiguration<?>> processors = migration.getProcessors();
		assertEquals(1, processors.size());
		DropColumnProcessor.Config<?> drop = (DropColumnProcessor.Config<?>) processors.get(0);
		assertEquals("KnowledgeObject", drop.getTable());
		assertEquals(RESOURCE, drop.getColumn());
		assertTrue(drop.getUpdateData());
		assertTrue(drop.getUpdateSchema());
	}

	private String processor() {
		return "<processor class='" + DropColumnProcessor.class.getName() + "' table='" + BASE + "' column='"
			+ RESOURCE + "'/>";
	}

	private boolean hasColumn(String tableName, String attributeName) throws Exception {
		MOClass type = (MOClass) kb().getMORepository().getMetaObject(tableName);
		String dbTable = type.getDBMapping().getDBName();
		String dbColumn = com.top_logic.basic.sql.SQLH.mangleDBName(attributeName);

		PooledConnection connection = kb().getConnectionPool().borrowReadConnection();
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			try (ResultSet columns = metaData.getColumns(null, null, null, null)) {
				while (columns.next()) {
					if (dbTable.equalsIgnoreCase(columns.getString("TABLE_NAME"))
						&& dbColumn.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
						return true;
					}
				}
			}
			return false;
		} finally {
			kb().getConnectionPool().releaseReadConnection(connection);
		}
	}

	public static Test suite() {
		return binaryMigrationSuite(TestDropPhysicalResource.class);
	}

}
