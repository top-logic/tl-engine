/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.dob.attr.InlineBinaryAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.attr.RefBinaryAttribute;
import com.top_logic.dob.meta.DefaultMORepository;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.migration.processors.MigrateBinaryAttributeProcessor;

/**
 * Test of {@link MigrateBinaryAttributeProcessor}.
 *
 * <p>
 * Each test migrates the attribute {@link #DATA} of its own table, declared with the source kind in
 * {@link #kb()} and with the target kind in {@link #kbNode2()}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMigrateBinaryAttribute extends AbstractBinaryMigrationTest {

	static final String DATA = "data";

	static final String DYNAMIC = "dynBinary";

	static final String BLOB_TO_INLINE = "MigBlobInline";

	static final String BLOB_TO_REF = "MigBlobRef";

	static final String INLINE_TO_REF = "MigInlineRef";

	static final String REF_TO_INLINE = "MigRefInline";

	static final String REF_TO_OTHER_STORE = "MigRefStore";

	static final String HYBRID_THRESHOLD = "MigHybrid";

	static final String UNCHANGED = "MigUnchanged";

	private static final String PLAIN_BLOB =
		"<" + DOXMLConstants.MO_ATTRIBUTE_ELEMENT + " " + DOXMLConstants.ATT_NAME_ATTRIBUTE + "='" + DATA + "' "
			+ DOXMLConstants.ATT_TYPE_ATTRIBUTE + "='" + AbstractBinaryAttribute.Config.BLOB_TYPE_NAME + "'/>";

	private static final String INLINE = binary(BinaryAttributeKind.INLINE, "");

	private static final String REF_DEFAULT = binary(BinaryAttributeKind.REF, "");

	private static final String REF_OTHER =
		binary(BinaryAttributeKind.REF, AbstractBinaryAttribute.ExternalConfig.STORE + "='" + OTHER_STORE + "'");

	private static final String HYBRID_4KB =
		binary(BinaryAttributeKind.HYBRID, HybridBinaryAttribute.Config.THRESHOLD + "='4KB'");

	private static final String HYBRID_1KB =
		binary(BinaryAttributeKind.HYBRID, HybridBinaryAttribute.Config.THRESHOLD + "='1KB'");

	private static final String[][] TABLES = {
		{ BLOB_TO_INLINE, PLAIN_BLOB, INLINE },
		{ BLOB_TO_REF, PLAIN_BLOB, REF_OTHER },
		{ INLINE_TO_REF, INLINE, REF_DEFAULT },
		{ REF_TO_INLINE, REF_DEFAULT, INLINE },
		{ REF_TO_OTHER_STORE, REF_DEFAULT, REF_OTHER },
		{ HYBRID_THRESHOLD, HYBRID_4KB, HYBRID_1KB },
		{ UNCHANGED, REF_OTHER, REF_OTHER },
	};

	private static String binary(BinaryAttributeKind kind, String attributes) {
		return "<" + kind.getExternalName() + " " + DOXMLConstants.ATT_NAME_ATTRIBUTE + "='" + DATA + "' "
			+ attributes + "/>";
	}

	private static String tableSchema(String table, String attribute) {
		return "<" + DOXMLConstants.META_OBJECT_ELEMENT + " " + DOXMLConstants.OBJECT_NAME_ATTRIBUTE + "='" + table
			+ "'><attributes>" + attribute + "</attributes></" + DOXMLConstants.META_OBJECT_ELEMENT + ">";
	}

	@Override
	protected TypeProvider sourceTypes() {
		return types(1);
	}

	@Override
	protected TypeProvider targetTypes() {
		return types(2);
	}

	private static TypeProvider types(int column) {
		return (log, typeFactory, typeRepository) -> {
			try {
				for (String[] table : TABLES) {
					String declaration = table[column];
					if (declaration == PLAIN_BLOB) {
						// A knowledge base cannot declare a plain Blob attribute. The values are
						// written to an inline attribute, whose columns are reduced to the single
						// content column of a plain Blob attribute before the migration, see
						// toPlainBlobLayout(String).
						declaration = INLINE;
					}
					typeRepository.addMetaObject(createType(parseTable(table[0], declaration)));
				}
			} catch (Exception ex) {
				throw new AssertionError("Creating test types failed.", ex);
			}
		};
	}

	private static MetaObjectConfig parseTable(String table, String attribute) throws Exception {
		return TypedConfiguration.parse(DOXMLConstants.META_OBJECT_ELEMENT, MetaObjectConfig.class,
			CharacterContents.newContent(tableSchema(table, attribute)));
	}

	private static MOClass createType(MetaObjectConfig config) throws Exception {
		MOKnowledgeItemImpl type = new MOKnowledgeItemImpl(config.getObjectName());
		type.setSuperclass(new DeferredMetaObject(B_NAME));
		for (AttributeConfig attributeConfig : config.getAttributes()) {
			type.addAttribute(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(attributeConfig));
		}
		return type;
	}

	/**
	 * The stored schema during the migration, containing the source declaration of the given
	 * table.
	 */
	private static SchemaConfiguration schema(String table) throws Exception {
		for (String[] entry : TABLES) {
			if (entry[0].equals(table)) {
				SchemaConfiguration schema = TypedConfiguration.newConfigItem(SchemaConfiguration.class);
				MetaObjectsConfig types = TypedConfiguration.newConfigItem(MetaObjectsConfig.class);
				types.getTypes().put(table, parseTable(table, entry[1]));
				schema.setMetaObjects(types);
				return schema;
			}
		}
		throw new AssertionError("No such table: " + table);
	}

	private static String processor(String table, String attribute, BinaryAttributeKind kind, String settings) {
		return "<processor class='" + MigrateBinaryAttributeProcessor.class.getName() + "'"
			+ " " + MigrateBinaryAttributeProcessor.Config.TABLE + "='" + table + "'"
			+ " " + MigrateBinaryAttributeProcessor.Config.ATTRIBUTE + "='" + attribute + "'"
			+ " " + MigrateBinaryAttributeProcessor.Config.KIND + "='" + kind.getExternalName() + "'"
			+ " " + settings + "/>";
	}

	/** A plain BLOB column becomes an inline binary attribute with metadata, including history. */
	public void testBlobToInline() throws Exception {
		byte[] v1 = randomContent(3000);
		byte[] v2 = randomContent(5000);
		History history = createHistory(BLOB_TO_INLINE, BinaryDataFactory.createBinaryData(v1),
			BinaryDataFactory.createBinaryData(v2));

		migrate(BLOB_TO_INLINE, BinaryAttributeKind.INLINE, "", InlineBinaryAttribute.class);

		Set<String> columns = columns(BLOB_TO_INLINE);
		assertFalse(columns.contains("DATA"));
		assertTrue(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_DATA));
		assertTrue(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_SIZE));
		assertTrue(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_CONTENT_TYPE));
		assertTrue(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_NAME));

		refetchNode2();
		BinaryData current = history.current();
		assertFalse(current instanceof BlobBinaryData);
		assertContent(v2, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME, current);
		assertContent(v1, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME, history.historic());
	}

	/** A plain BLOB column becomes a reference attribute, the content is uploaded. */
	public void testBlobToRef() throws Exception {
		byte[] v1 = randomContent(3000);
		byte[] v2 = randomContent(5000);
		History history = createHistory(BLOB_TO_REF, BinaryDataFactory.createBinaryData(v1),
			BinaryDataFactory.createBinaryData(v2));

		migrate(BLOB_TO_REF, BinaryAttributeKind.REF,
			MigrateBinaryAttributeProcessor.Config.STORE + "='" + OTHER_STORE + "'", RefBinaryAttribute.class);

		Set<String> columns = columns(BLOB_TO_REF);
		assertFalse(columns.contains("DATA"));
		assertFalse(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_DATA));
		assertTrue(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_KEY));

		refetchNode2();
		assertInStore(OTHER_STORE, history.current());
		assertContent(v2, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME, history.current());
		assertInStore(OTHER_STORE, history.historic());
		assertContent(v1, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME, history.historic());
	}

	/** Inline content is uploaded to the default store. */
	public void testInlineToRef() throws Exception {
		byte[] v1 = randomContent(3000);
		byte[] v2 = randomContent(5000);
		History history = createHistory(INLINE_TO_REF, BinaryDataFactory.createBinaryData(v1, "text/plain", "v1.txt"),
			BinaryDataFactory.createBinaryData(v2, "image/png", "v2.png"));

		migrate(INLINE_TO_REF, BinaryAttributeKind.REF, "", RefBinaryAttribute.class);

		assertFalse(columns(INLINE_TO_REF).contains("DATA" + AbstractBinaryAttribute.SUFFIX_DATA));

		refetchNode2();
		assertInStore(null, history.current());
		assertContent(v2, "image/png", "v2.png", history.current());
		assertInStore(null, history.historic());
		assertContent(v1, "text/plain", "v1.txt", history.historic());
	}

	/** Content of a blob store is moved into the database. */
	public void testRefToInline() throws Exception {
		byte[] v1 = randomContent(3000);
		byte[] v2 = randomContent(5000);
		History history = createHistory(REF_TO_INLINE, BinaryDataFactory.createBinaryData(v1, "text/plain", "v1.txt"),
			BinaryDataFactory.createBinaryData(v2, "image/png", "v2.png"));

		migrate(REF_TO_INLINE, BinaryAttributeKind.INLINE, "", InlineBinaryAttribute.class);

		Set<String> columns = columns(REF_TO_INLINE);
		assertFalse(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_KEY));
		assertFalse(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_HASH));
		assertTrue(columns.contains("DATA" + AbstractBinaryAttribute.SUFFIX_DATA));

		// The content must be read from the database, not from the store.
		for (String key : storeKeys(null)) {
			BlobStoreService.getInstance().getDefaultStore().delete(key);
		}

		refetchNode2();
		assertFalse(history.current() instanceof BlobBinaryData);
		assertContent(v2, "image/png", "v2.png", history.current());
		assertFalse(history.historic() instanceof BlobBinaryData);
		assertContent(v1, "text/plain", "v1.txt", history.historic());
	}

	/** Content is copied to another store. */
	public void testRefToOtherStore() throws Exception {
		byte[] v1 = randomContent(3000);
		byte[] v2 = randomContent(5000);
		History history =
			createHistory(REF_TO_OTHER_STORE, BinaryDataFactory.createBinaryData(v1, "text/plain", "v1.txt"),
				BinaryDataFactory.createBinaryData(v2, "image/png", "v2.png"));
		Set<String> formerKeys = storeKeys(null);

		migrate(REF_TO_OTHER_STORE, BinaryAttributeKind.REF,
			MigrateBinaryAttributeProcessor.Config.STORE + "='" + OTHER_STORE + "'", RefBinaryAttribute.class);

		refetchNode2();
		assertInStore(OTHER_STORE, history.current());
		assertContent(v2, "image/png", "v2.png", history.current());
		assertInStore(OTHER_STORE, history.historic());
		assertContent(v1, "text/plain", "v1.txt", history.historic());

		assertFalse(formerKeys.contains(((BlobBinaryData) history.current()).getKey()));
		assertEquals("Former content is left for the garbage collection.", formerKeys, storeKeys(null));
	}

	/** Lowering the threshold of a hybrid attribute moves only the content above it. */
	public void testHybridThreshold() throws Exception {
		byte[] small = randomContent(500);
		byte[] medium = randomContent(2000);
		byte[] large = randomContent(5000);

		Transaction tx = begin();
		KnowledgeObject smallItem = newA(HYBRID_THRESHOLD, "small");
		smallItem.setAttributeValue(DATA, BinaryDataFactory.createBinaryData(small, "a/b", "small"));
		KnowledgeObject mediumItem = newA(HYBRID_THRESHOLD, "medium");
		mediumItem.setAttributeValue(DATA, BinaryDataFactory.createBinaryData(medium, "a/b", "medium"));
		KnowledgeObject largeItem = newA(HYBRID_THRESHOLD, "large");
		largeItem.setAttributeValue(DATA, BinaryDataFactory.createBinaryData(large, "a/b", "large"));
		commit(tx);
		assertFalse(mediumItem.getAttributeValue(DATA) instanceof BlobBinaryData);
		String largeKey = ((BlobBinaryData) largeItem.getAttributeValue(DATA)).getKey();

		BufferingProtocol log = migrate(HYBRID_THRESHOLD, BinaryAttributeKind.HYBRID,
			MigrateBinaryAttributeProcessor.Config.THRESHOLD + "='1KB'", HybridBinaryAttribute.class);
		assertTrue(log.getInfos().toString(),
			log.getInfos().stream().anyMatch(info -> info.startsWith("Moved content of 1 of 3 values")));

		refetchNode2();
		BinaryData smallValue = (BinaryData) node2Item(smallItem).getAttributeValue(DATA);
		assertFalse(smallValue instanceof BlobBinaryData);
		assertContent(small, "a/b", "small", smallValue);
		BinaryData mediumValue = (BinaryData) node2Item(mediumItem).getAttributeValue(DATA);
		assertInStore(null, mediumValue);
		assertContent(medium, "a/b", "medium", mediumValue);
		BinaryData largeValue = (BinaryData) node2Item(largeItem).getAttributeValue(DATA);
		assertEquals("Content above both thresholds is not uploaded again.", largeKey,
			((BlobBinaryData) largeValue).getKey());
		assertContent(large, "a/b", "large", largeValue);
	}

	/** A migration to the current kind and store does nothing. */
	public void testUnchanged() throws Exception {
		Transaction tx = begin();
		KnowledgeObject item = newA(UNCHANGED, "a1");
		item.setAttributeValue(DATA, BinaryDataFactory.createBinaryData(randomContent(100)));
		commit(tx);

		BufferingProtocol log = migrate(processor(UNCHANGED, DATA, BinaryAttributeKind.REF,
			MigrateBinaryAttributeProcessor.Config.STORE + "='" + OTHER_STORE + "'"), schema(UNCHANGED));
		assertTrue(log.getInfos().toString(),
			log.getInfos().stream().anyMatch(info -> info.contains("is already stored as")));
	}

	/** Values of a dynamic attribute are moved to another store. */
	public void testDynamicToOtherStore() throws Exception {
		byte[] small = randomContent(1000);
		byte[] large = randomContent(70 * 1024);

		Transaction tx1 = begin();
		KnowledgeObject item = newE("e1");
		item.setAttributeValue(DYNAMIC, BinaryDataFactory.createBinaryData(small, "text/plain", "small.txt"));
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		item.setAttributeValue(DYNAMIC, BinaryDataFactory.createBinaryData(large, "image/png", "large.png"));
		commit(tx2);
		assertInStore(null, (BinaryData) item.getAttributeValue(DYNAMIC));

		BufferingProtocol log = migrate(processor(E_NAME, DYNAMIC, BinaryAttributeKind.REF,
			MigrateBinaryAttributeProcessor.Config.STORE + "='" + OTHER_STORE + "'"), schema(UNCHANGED));
		assertTrue(log.getInfos().toString(),
			log.getInfos().stream().anyMatch(info -> info.startsWith("Moved content of 2 of 2 values")));

		refetchNode2();
		KnowledgeItem node2Item = node2Item(item);
		BinaryData current = (BinaryData) node2Item.getAttributeValue(DYNAMIC);
		assertInStore(OTHER_STORE, current);
		assertContent(large, "image/png", "large.png", current);
		KnowledgeItem historic =
			HistoryUtils.getKnowledgeItem(HistoryUtils.getHistoryManager(kbNode2()).getRevision(r1.getCommitNumber()),
				node2Item);
		BinaryData historicValue = (BinaryData) historic.getAttributeValue(DYNAMIC);
		assertInStore(OTHER_STORE, historicValue);
		assertContent(small, "text/plain", "small.txt", historicValue);
	}

	private BufferingProtocol migrate(String table, BinaryAttributeKind kind, String settings,
			Class<? extends AbstractBinaryAttribute> expectedType) throws Exception {
		BufferingProtocol log = migrateAndCheckSchema(table, processor(table, DATA, kind, settings), kind);
		assertTrue(kbNode2().getMORepository().getMetaObject(table) instanceof MOClass);
		assertTrue(expectedType.isInstance(((MOClass) kbNode2().getMORepository().getMetaObject(table))
			.getAttribute(DATA)));
		return log;
	}

	private BufferingProtocol migrateAndCheckSchema(String table, String processor, BinaryAttributeKind kind)
			throws Exception {
		if (isPlainBlob(table)) {
			toPlainBlobLayout(table);
			_plainBlobTable = table;
		}
		_expectedStoredKind = kind;
		_expectedStoredTable = table;
		try {
			return migrate(processor, schema(table));
		} finally {
			_expectedStoredKind = null;
			_expectedStoredTable = null;
			_plainBlobTable = null;
		}
	}

	private static boolean isPlainBlob(String table) {
		for (String[] entry : TABLES) {
			if (entry[0].equals(table)) {
				return entry[1] == PLAIN_BLOB;
			}
		}
		return false;
	}

	/**
	 * Reduces the columns of the inline attribute {@link #DATA} of the given table to the single
	 * content column of a plain Blob attribute, as stored by an earlier version.
	 */
	private void toPlainBlobLayout(String table) throws SQLException {
		MOClass type = (MOClass) kb().getMORepository().getMetaObject(table);
		AbstractBinaryAttribute inline = (AbstractBinaryAttribute) type.getAttribute(DATA);
		String tableName = type.getDBMapping().getDBName();

		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			DBHelper sqlDialect = connection.getSQLDialect();
			for (DBAttribute column : new DBAttribute[] { inline.getSizeColumn(), inline.getContentTypeColumn(),
				inline.getNameColumn() }) {
				query(dropColumn(table(tableName), column.getDBName())).toSql(sqlDialect).executeUpdate(connection);
			}
			query(modifyColumnName(table(tableName), inline.getDataColumn().getDBName(), DBType.BLOB,
				plainBlobAttribute().getDbMapping()[0].getDBName())).toSql(sqlDialect).executeUpdate(connection);
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	private static MOAttribute plainBlobAttribute() {
		return new MOAttributeImpl(DATA, MOPrimitive.BLOB, !MOAttribute.MANDATORY);
	}

	/**
	 * The table whose attribute {@link #DATA} is stored as plain Blob attribute during the
	 * migration, <code>null</code> if none.
	 */
	private String _plainBlobTable;

	@Override
	protected MORepository persistentRepository() {
		MORepository repository = super.persistentRepository();
		if (_plainBlobTable == null) {
			return repository;
		}
		MOClass type = (MOClass) repository.getMetaObject(_plainBlobTable);
		MOClassImpl plainType = new MOClassImpl(_plainBlobTable);
		plainType.setSuperclass(type.getSuperclass());
		plainType.setDBName(type.getDBMapping().getDBName());
		plainType.addAttribute(plainBlobAttribute());

		MORepository result = new DefaultMORepository(repository.multipleBranches());
		for (MetaObject other : repository.getMetaObjects()) {
			if (!other.getName().equals(_plainBlobTable)) {
				result.addMetaObject(other);
			}
		}
		result.addMetaObject(plainType.resolve(result));
		plainType.freeze();
		return result;
	}

	private BinaryAttributeKind _expectedStoredKind;

	private String _expectedStoredTable;

	@Override
	protected void migrated(PooledConnection connection) throws Exception {
		if (_expectedStoredKind == null) {
			return;
		}
		SchemaConfiguration stored = KBSchemaUtil.loadSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		MetaObjectConfig table = (MetaObjectConfig) stored.getMetaObjects().getTypes().get(_expectedStoredTable);
		AttributeConfig attribute = table.getAttributes().get(0);
		assertEquals(DATA, attribute.getAttributeName());
		assertTrue(_expectedStoredKind.getConfigType().isInstance(attribute));
	}

	private History createHistory(String table, BinaryData v1, BinaryData v2) throws Exception {
		Transaction tx1 = begin();
		KnowledgeObject item = newA(table, "a1");
		item.setAttributeValue(DATA, v1);
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		item.setAttributeValue(DATA, v2);
		commit(tx2);

		return new History(item, r1);
	}

	private final class History {
		private final KnowledgeObject _item;

		private final Revision _r1;

		History(KnowledgeObject item, Revision r1) {
			_item = item;
			_r1 = r1;
		}

		BinaryData current() {
			return (BinaryData) node2Item(_item).getAttributeValue(DATA);
		}

		BinaryData historic() {
			KnowledgeItem node2Item = node2Item(_item);
			KnowledgeItem historic = HistoryUtils.getKnowledgeItem(
				HistoryUtils.getHistoryManager(kbNode2()).getRevision(_r1.getCommitNumber()), node2Item);
			return (BinaryData) historic.getAttributeValue(DATA);
		}
	}

	private static void assertInStore(String storeName, BinaryData value) {
		assertTrue("Not in a blob store: " + value, value instanceof BlobBinaryData);
		BlobStoreService service = BlobStoreService.getInstance();
		assertSame(service.getStore(storeName), service.getStore(((BlobBinaryData) value).getStoreName()));
	}

	private static Set<String> storeKeys(String storeName) throws Exception {
		Set<String> result = new HashSet<>();
		try (var blobs = BlobStoreService.getInstance().getStore(storeName).list()) {
			blobs.forEach(blob -> result.add(blob.key()));
		}
		return result;
	}

	private Set<String> columns(String table) throws SQLException {
		String dbName = ((MOClass) kb().getMORepository().getMetaObject(table)).getDBMapping().getDBName();
		DBTable dbTable =
			DBSchemaUtils.extractTable(kb().getConnectionPool(), DBSchemaFactory.createDBSchema(), dbName);
		Set<String> result = new HashSet<>();
		for (DBColumn column : dbTable.getColumns()) {
			result.add(column.getDBName().toUpperCase());
		}
		return result;
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return binaryMigrationSuite(TestMigrateBinaryAttribute.class);
	}

}
