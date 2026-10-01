/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.encryption.data;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.sql.SQLAddColumn;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.encryption.SymmetricEncryption;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AttributeLoader;
import com.top_logic.knowledge.service.CommittableAdapter;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.FlexVersionedDataManager;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.SerializingTransformer;
import com.top_logic.knowledge.service.encryption.data.EncryptedFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.MoveFlexBinaryDataProcessor;
import com.top_logic.util.TLContext;

/**
 * Test of binary values of dynamic attributes stored by the {@link EncryptedFlexDataManager} in
 * the table of dynamic binary values.
 *
 * <p>
 * The table must never contain plain text: neither the content of binary attributes nor the
 * serialized primitive values. Values written in the encoding of the generic table of dynamic
 * attribute values (a BLOB column with the cipher text, the cipher text size in the long column)
 * must be readable after {@link MoveFlexBinaryDataProcessor} moved them.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestEncryptedFlexBinary extends AbstractDBKnowledgeBaseTest {

	/**
	 * Name of the binary attribute that holds the serialized primitive values, see
	 * {@link SerializingTransformer}.
	 */
	private static final String SERIALIZED_ATTRIBUTE = "_";

	private static final String BINARY_ATTRIBUTE = "attrB";

	private static final String STRING_ATTRIBUTE = "attrS";

	private static final String SECRET = "Top-Secret-Plaintext-Marker";

	private static final String SECRET_STRING = "Confidential-String-Value";

	private static final String CONTENT_TYPE = "text/x-secret";

	private static final String NAME = "secret.txt";

	private FlexDataManager _flexData;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_flexData = new EncryptedFlexDataManager(TypedConfiguration.newConfigItem(SerializingTransformer.Config.class),
			new FlexVersionedDataManager(kb().getConnectionPool(),
				kb().lookupType(AbstractFlexDataManager.FLEX_DATA),
				kb().lookupType(AbstractFlexDataManager.FLEX_BINARY_DATA)));
	}

	@Override
	protected void tearDown() throws Exception {
		_flexData = null;
		super.tearDown();
	}

	/** Small content is kept in memory when read. */
	public void testSmallContent() throws Exception {
		doTestWriteRead(secretContent(100));
	}

	/** Large content is read from the database on access. */
	public void testLargeContent() throws Exception {
		doTestWriteRead(secretContent(200 * 1024));
	}

	/** Content of unknown size is stored completely. */
	public void testUnknownSize() throws Exception {
		byte[] content = secretContent(10 * 1024);
		KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			FlexData data = loadData(b1, true);
			data.setAttributeValue(BINARY_ATTRIBUTE, new UnknownSizeData(content));
			storeData(b1, data);
			commit(tx);
		}
		assertNoPlainText(b1);
		assertContent(content, CONTENT_TYPE, NAME, loadData(b1, false).getAttributeValue(BINARY_ATTRIBUTE));
	}

	private void doTestWriteRead(byte[] v1) throws Exception {
		byte[] v2 = secretContent(v1.length + 17);

		KnowledgeObject b1;
		FlexData data;
		Revision r1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			data = loadData(b1, true);
			data.setAttributeValue(BINARY_ATTRIBUTE, BinaryDataFactory.createBinaryData(v1, CONTENT_TYPE, NAME));
			data.setAttributeValue(STRING_ATTRIBUTE, SECRET_STRING);
			storeData(b1, data);
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		{
			Transaction tx = begin();
			data.setAttributeValue(BINARY_ATTRIBUTE, BinaryDataFactory.createBinaryData(v2, CONTENT_TYPE, NAME));
			data.setAttributeValue(STRING_ATTRIBUTE, SECRET_STRING + "2");
			storeData(b1, data);
			commit(tx);
		}

		Map<String, byte[]> rows = assertNoPlainText(b1);
		assertTrue("Serialized primitive values are stored as binary value.", rows.containsKey(SERIALIZED_ATTRIBUTE));
		assertTrue(rows.containsKey(BINARY_ATTRIBUTE));
		assertNoPrimitiveRows(b1);

		// Current.
		FlexData current = loadData(b1, false);
		assertContent(v2, CONTENT_TYPE, NAME, current.getAttributeValue(BINARY_ATTRIBUTE));
		assertEquals(SECRET_STRING + "2", current.getAttributeValue(STRING_ATTRIBUTE));

		// Historic.
		FlexData historic = loadData(HistoryUtils.getKnowledgeItem(r1, b1), false);
		assertContent(v1, CONTENT_TYPE, NAME, historic.getAttributeValue(BINARY_ATTRIBUTE));
		assertEquals(SECRET_STRING, historic.getAttributeValue(STRING_ATTRIBUTE));

		// Bulk.
		Map<KnowledgeItem, FlexData> bulk = loadAll(List.of(b1));
		assertContent(v2, CONTENT_TYPE, NAME, bulk.get(b1).getAttributeValue(BINARY_ATTRIBUTE));
		assertEquals(SECRET_STRING + "2", bulk.get(b1).getAttributeValue(STRING_ATTRIBUTE));

		Map<KnowledgeItem, FlexData> historicBulk = loadAll(List.of(HistoryUtils.getKnowledgeItem(r1, b1)));
		assertContent(v1, CONTENT_TYPE, NAME,
			historicBulk.values().iterator().next().getAttributeValue(BINARY_ATTRIBUTE));
	}

	/**
	 * Values in the encoding of the generic table of dynamic attribute values are readable after
	 * the move to the table of dynamic binary values.
	 */
	public void testMigratedValues() throws Exception {
		byte[] v1 = secretContent(3000);
		byte[] v2 = secretContent(80 * 1024);

		KnowledgeObject b1;
		Revision r1;
		Revision r2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		{
			Transaction tx = begin();
			b1.setAttributeValue(A1_NAME, "changed");
			commit(tx);
			r2 = tx.getCommitRevision();
		}

		SymmetricEncryption encryption = EncryptionService.getInstance().getEncryption();
		addBlobColumn();
		long r1Rev = r1.getCommitNumber();
		long r2Rev = r2.getCommitNumber();
		insertEncryptedRow(encryption, b1.tId(), BINARY_ATTRIBUTE, r1Rev, r2Rev - 1, v1, CONTENT_TYPE, NAME);
		insertEncryptedRow(encryption, b1.tId(), BINARY_ATTRIBUTE, r2Rev, Revision.CURRENT_REV, v2, CONTENT_TYPE,
			NAME);
		insertEncryptedRow(encryption, b1.tId(), SERIALIZED_ATTRIBUTE, r1Rev, r2Rev - 1,
			serializeString(STRING_ATTRIBUTE, SECRET_STRING), BinaryData.CONTENT_TYPE_OCTET_STREAM, null);
		insertEncryptedRow(encryption, b1.tId(), SERIALIZED_ATTRIBUTE, r2Rev, Revision.CURRENT_REV,
			serializeString(STRING_ATTRIBUTE, SECRET_STRING + "2"), BinaryData.CONTENT_TYPE_OCTET_STREAM, null);

		migrate();

		Map<String, byte[]> rows = assertNoPlainText(b1);
		assertTrue(rows.containsKey(SERIALIZED_ATTRIBUTE));
		assertTrue(rows.containsKey(BINARY_ATTRIBUTE));

		FlexData current = loadData(b1, false);
		assertContent(v2, CONTENT_TYPE, NAME, current.getAttributeValue(BINARY_ATTRIBUTE));
		assertEquals(SECRET_STRING + "2", current.getAttributeValue(STRING_ATTRIBUTE));

		FlexData historic = loadData(HistoryUtils.getKnowledgeItem(r1, b1), false);
		assertContent(v1, CONTENT_TYPE, NAME, historic.getAttributeValue(BINARY_ATTRIBUTE));
		assertEquals(SECRET_STRING, historic.getAttributeValue(STRING_ATTRIBUTE));

		Map<KnowledgeItem, FlexData> bulk = loadAll(List.of(b1));
		assertContent(v2, CONTENT_TYPE, NAME, bulk.get(b1).getAttributeValue(BINARY_ATTRIBUTE));

		// Migrated values can be updated.
		{
			Transaction tx = begin();
			FlexData data = loadData(b1, true);
			data.setAttributeValue(STRING_ATTRIBUTE, SECRET_STRING + "3");
			storeData(b1, data);
			commit(tx);
		}
		FlexData updated = loadData(b1, false);
		assertContent(v2, CONTENT_TYPE, NAME, updated.getAttributeValue(BINARY_ATTRIBUTE));
		assertEquals(SECRET_STRING + "3", updated.getAttributeValue(STRING_ATTRIBUTE));
		assertNoPlainText(b1);
	}

	/**
	 * The serialization of a single string attribute as written by {@link SerializingTransformer}.
	 */
	private static byte[] serializeString(String attribute, String value) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);
		out.write(AbstractFlexDataManager.STRING_TYPE);
		out.writeUTF(value);
		out.writeUTF(attribute);
		out.flush();
		return buffer.toByteArray();
	}

	private static byte[] secretContent(int size) {
		byte[] marker = SECRET.getBytes(StandardCharsets.UTF_8);
		byte[] result = new byte[size];
		for (int n = 0; n < size; n++) {
			result[n] = marker[n % marker.length];
		}
		return result;
	}

	/**
	 * Asserts that no row of the table of dynamic binary values of the given object contains plain
	 * text and that all content is stored inline.
	 *
	 * @return The stored content by attribute name of the current rows.
	 */
	private Map<String, byte[]> assertNoPlainText(KnowledgeItem item) throws Exception {
		MOKnowledgeItemImpl binaryType = kb().lookupType(AbstractFlexDataManager.FLEX_BINARY_DATA);
		AbstractBinaryAttribute content =
			(AbstractBinaryAttribute) binaryType.getAttribute(AbstractFlexDataManager.CONTENT);
		SymmetricEncryption encryption = EncryptionService.getInstance().getEncryption();

		Map<String, byte[]> result = new HashMap<>();
		PooledConnection connection = kb().getConnectionPool().borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			String select = "SELECT "
				+ sql.columnRef(AbstractFlexDataManager.ATTRIBUTE_DBNAME) + ", "
				+ sql.columnRef(BasicTypes.REV_MAX_DB_NAME) + ", "
				+ sql.columnRef(content.getSizeColumn().getDBName()) + ", "
				+ sql.columnRef(content.getKeyColumn().getDBName()) + ", "
				+ sql.columnRef(content.getDataColumn().getDBName())
				+ " FROM " + sql.tableRef(binaryType.getDBMapping().getDBName())
				+ " WHERE " + sql.columnRef(AbstractFlexDataManager.IDENTIFIER_DBNAME) + " = ?";
			int rowCount = 0;
			try (PreparedStatement statement = connection.prepareStatement(select)) {
				IdentifierUtil.setId(statement, 1, item.getObjectName());
				try (ResultSet rows = statement.executeQuery()) {
					while (rows.next()) {
						rowCount++;
						String attribute = rows.getString(1);
						long revMax = rows.getLong(2);
						long size = rows.getLong(3);
						assertNull("Encrypted content is stored inline: " + attribute, rows.getString(4));
						byte[] data;
						try (InputStream in = sql.getBinaryStream(rows, 5)) {
							data = StreamUtilities.readStreamContents(in);
						}
						assertEquals("Stored size is the size of the stored cipher text: " + attribute,
							data.length, size);
						assertTrue(data.length >= encryption.getCipherTextSize(0));
						String text = new String(data, StandardCharsets.ISO_8859_1);
						assertFalse("Plain text in table of binary values: " + attribute, text.contains(SECRET));
						assertFalse("Plain text in table of binary values: " + attribute,
							text.contains(SECRET_STRING));
						if (revMax == Revision.CURRENT_REV) {
							result.put(attribute, data);
						}
					}
				}
			}
			assertTrue(rowCount > 0);
		} finally {
			kb().getConnectionPool().releaseReadConnection(connection);
		}
		return result;
	}

	/**
	 * Asserts that no primitive value is stored unencrypted in the generic table.
	 */
	private void assertNoPrimitiveRows(KnowledgeItem item) throws Exception {
		PooledConnection connection = kb().getConnectionPool().borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COUNT(*) FROM " + sql.tableRef(AbstractFlexDataManager.FLEX_DATA_DB_NAME)
					+ " WHERE " + sql.columnRef(AbstractFlexDataManager.IDENTIFIER_DBNAME) + " = ?"
					+ " AND " + sql.columnRef(AbstractFlexDataManager.ATTRIBUTE_DBNAME) + " = ?")) {
				IdentifierUtil.setId(statement, 1, item.getObjectName());
				statement.setString(2, STRING_ATTRIBUTE);
				try (ResultSet result = statement.executeQuery()) {
					assertTrue(result.next());
					assertEquals(0, result.getInt(1));
				}
			}
		} finally {
			kb().getConnectionPool().releaseReadConnection(connection);
		}
	}

	private static void assertContent(byte[] expected, String contentType, String name, Object actualValue)
			throws IOException {
		assertNotNull(actualValue);
		BinaryData actual = (BinaryData) actualValue;
		assertEquals(name, actual.getName());
		assertEquals(contentType, actual.getContentType());
		assertEquals(expected.length, actual.getSize());
		try (InputStream in = actual.getStream()) {
			assertEquals(expected, StreamUtilities.readStreamContents(in));
		}
	}

	private FlexData loadData(KnowledgeItem item, boolean mutable) {
		return _flexData.load(item.getKnowledgeBase(), item.tId(), mutable);
	}

	private void storeData(KnowledgeItem item, FlexData data) {
		commitHandler().addCommittable(new CommittableAdapter() {
			@Override
			public boolean prepare(CommitContext context) {
				return _flexData.store(item.tId(), data, context);
			}
		});
	}

	private Map<KnowledgeItem, FlexData> loadAll(List<KnowledgeItem> items) {
		Map<KnowledgeItem, FlexData> result = new HashMap<>();
		_flexData.loadAll(new AttributeLoader<KnowledgeItem>() {
			@Override
			public void loadData(long dataRevision, KnowledgeItem baseObject, FlexData data) {
				result.put(baseObject, data);
			}

			@Override
			public void loadEmpty(long dataRevision, KnowledgeItem baseObject) {
				fail("No values loaded for " + baseObject);
			}
		}, KnowledgeItem.KEY_MAPPING, new ArrayList<>(items), kb());
		return result;
	}

	private void migrate() throws Exception {
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<MigrationProcessor> config =
			TypedConfiguration.parse("processor", PolymorphicConfiguration.class, CharacterContents.newContent(
				"<processor class='" + MoveFlexBinaryDataProcessor.class.getName() + "'/>"));
		MigrationProcessor processor = TypedConfigUtil.createInstance(config);
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = kb().getConnectionPool().borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public MORepository getPersistentRepository() {
					return kb().getMORepository();
				}

				@Override
				public SchemaConfiguration getPersistentSchema() {
					return null;
				}

				@Override
				public boolean hasBranchSupport() {
					return kb().getMORepository().multipleBranches();
				}
			};
			processor.doMigration(context, log, connection);
			connection.commit();
		} finally {
			kb().getConnectionPool().releaseWriteConnection(connection);
		}
		assertFalse("Migration failed: " + log.getErrors(), log.hasErrors());
		assertFalse(hasBlobColumn());
	}

	private void addBlobColumn() throws SQLException {
		if (hasBlobColumn()) {
			return;
		}
		PooledConnection connection = kb().getConnectionPool().borrowWriteConnection();
		try {
			SQLAddColumn addColumn = SQLFactory.addColumn(table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
				MoveFlexBinaryDataProcessor.BLOB_DATA_DB_NAME, DBType.BLOB);
			addColumn.setMandatory(false);
			query(addColumn).toSql(connection.getSQLDialect()).executeUpdate(connection);
			connection.commit();
		} finally {
			kb().getConnectionPool().releaseWriteConnection(connection);
		}
	}

	private boolean hasBlobColumn() throws SQLException {
		DBTable table = DBSchemaUtils.extractTable(kb().getConnectionPool(), DBSchemaFactory.createDBSchema(),
			AbstractFlexDataManager.FLEX_DATA_DB_NAME);
		for (DBColumn column : table.getColumns()) {
			if (MoveFlexBinaryDataProcessor.BLOB_DATA_DB_NAME.equalsIgnoreCase(column.getDBName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Inserts an encrypted binary value into the generic table of dynamic attribute values: the
	 * cipher text in the BLOB column, the cipher text size in the long column, the content type in
	 * the string column and the name in the text column.
	 */
	private void insertEncryptedRow(SymmetricEncryption encryption, ObjectKey key, String attribute, long revMin,
			long revMax, byte[] plainText, String contentType, String name) throws SQLException {
		byte[] cipherText = encryption.encrypt(plainText);
		assertEquals(encryption.getCipherTextSize(plainText.length), cipherText.length);

		PooledConnection connection = kb().getConnectionPool().borrowWriteConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			boolean branches = kb().getMORepository().multipleBranches();
			List<String> columns = new ArrayList<>();
			if (branches) {
				columns.add(AbstractFlexDataManager.BRANCH_DBNAME);
			}
			columns.addAll(List.of(AbstractFlexDataManager.TYPE_DBNAME, AbstractFlexDataManager.IDENTIFIER_DBNAME,
				BasicTypes.REV_MAX_DB_NAME, AbstractFlexDataManager.ATTRIBUTE_DBNAME, BasicTypes.REV_MIN_DB_NAME,
				AbstractFlexDataManager.DATA_TYPE_DBNAME, AbstractFlexDataManager.LONG_DATA_DBNAME,
				AbstractFlexDataManager.VARCHAR_DATA_DBNAME, AbstractFlexDataManager.CLOB_DATA_DBNAME,
				MoveFlexBinaryDataProcessor.BLOB_DATA_DB_NAME));
			StringBuilder insert = new StringBuilder();
			insert.append("INSERT INTO ").append(sql.tableRef(AbstractFlexDataManager.FLEX_DATA_DB_NAME)).append(" (");
			for (int n = 0; n < columns.size(); n++) {
				insert.append(n > 0 ? ", " : "").append(sql.columnRef(columns.get(n)));
			}
			insert.append(") VALUES (");
			for (int n = 0; n < columns.size(); n++) {
				insert.append(n > 0 ? ", ?" : "?");
			}
			insert.append(')');

			try (PreparedStatement statement = connection.prepareStatement(insert.toString())) {
				int index = 1;
				if (branches) {
					statement.setLong(index++, TLContext.TRUNK_ID);
				}
				statement.setString(index++, key.getObjectType().getName());
				IdentifierUtil.setId(statement, index++, key.getObjectName());
				statement.setLong(index++, revMax);
				statement.setString(index++, attribute);
				statement.setLong(index++, revMin);
				statement.setInt(index++, MoveFlexBinaryDataProcessor.BLOB_TYPE);
				statement.setLong(index++, cipherText.length);
				sql.setFromJava(statement, contentType, index++, DBType.STRING);
				sql.setFromJava(statement, name, index++, DBType.CLOB);
				statement.setBytes(index++, cipherText);
				statement.executeUpdate();
			}
			connection.commit();
		} finally {
			kb().getConnectionPool().releaseWriteConnection(connection);
		}
	}

	/**
	 * Content whose size is not known in advance.
	 */
	private static final class UnknownSizeData extends AbstractBinaryData {

		private final byte[] _content;

		UnknownSizeData(byte[] content) {
			_content = content;
		}

		@Override
		public InputStream getStream() {
			return new ByteArrayInputStream(_content);
		}

		@Override
		public long getSize() {
			return -1;
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public String getContentType() {
			return CONTENT_TYPE;
		}

	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return TestEncryptedFlexDataManagerVersioned.encryptedSuite(TestEncryptedFlexBinary.class);
	}

}
