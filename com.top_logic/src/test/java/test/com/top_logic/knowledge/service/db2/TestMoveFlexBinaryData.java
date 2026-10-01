/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Test;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.db.sql.SQLAddColumn;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.processors.MoveFlexBinaryDataProcessor;
import com.top_logic.util.TLContext;

/**
 * Test of {@link MoveFlexBinaryDataProcessor}.
 *
 * <p>
 * Binary values are inserted with plain SQL into the generic table of dynamic attribute values,
 * extended by the BLOB column it had before binary values were stored in a table of their own.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMoveFlexBinaryData extends AbstractBinaryMigrationTest {

	private static final String ATTR = "dynBinary";

	private static final TypeProvider NO_TYPES = (log, typeFactory, typeRepository) -> {
		// No additional types.
	};

	@Override
	protected TypeProvider sourceTypes() {
		return NO_TYPES;
	}

	@Override
	protected TypeProvider targetTypes() {
		return NO_TYPES;
	}

	/** Binary rows are moved with their revision ranges and the generic table loses its BLOB column. */
	public void testMove() throws Exception {
		byte[] v1 = randomContent(3000);
		byte[] v2 = randomContent(80 * 1024);

		Transaction tx1 = begin();
		KnowledgeObject item = newE("e1");
		commit(tx1);
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		item.setAttributeValue(A1_NAME, "changed");
		commit(tx2);
		Revision r2 = tx2.getCommitRevision();

		addBlobColumn();
		insertBinaryRow(item.tId(), r1.getCommitNumber(), r2.getCommitNumber() - 1, v1, Long.valueOf(v1.length),
			"text/plain", "v1.txt");
		// Without size and content type.
		insertBinaryRow(item.tId(), r2.getCommitNumber(), Revision.CURRENT_REV, v2, null, null, null);

		// The table of binary values is created by the migration.
		dropBinaryTable();

		migrate(processor(""), emptySchema());

		assertFalse(hasBlobColumn());
		assertEquals(0, countBinaryFlexRows());

		refetchNode2();
		KnowledgeItem node2Item = node2Item(item);
		BinaryData current = (BinaryData) node2Item.getAttributeValue(ATTR);
		assertFalse("Content is kept inline.", current instanceof BlobBinaryData);
		assertContent(v2, BinaryData.CONTENT_TYPE_OCTET_STREAM, BinaryData.NO_NAME, current);

		KnowledgeItem historic = HistoryUtils.getKnowledgeItem(
			HistoryUtils.getHistoryManager(kbNode2()).getRevision(r1.getCommitNumber()), node2Item);
		assertContent(v1, "text/plain", "v1.txt", (BinaryData) historic.getAttributeValue(ATTR));
	}

	/** Content from the threshold on is uploaded to the configured store. */
	public void testUpload() throws Exception {
		byte[] small = randomContent(500);
		byte[] large = randomContent(5000);

		Transaction tx1 = begin();
		KnowledgeObject smallItem = newE("small");
		KnowledgeObject largeItem = newE("large");
		commit(tx1);
		long rev = tx1.getCommitRevision().getCommitNumber();

		addBlobColumn();
		insertBinaryRow(smallItem.tId(), rev, Revision.CURRENT_REV, small, Long.valueOf(small.length), "a/b",
			"small");
		insertBinaryRow(largeItem.tId(), rev, Revision.CURRENT_REV, large, Long.valueOf(large.length), "a/b",
			"large");

		migrate(processor(MoveFlexBinaryDataProcessor.Config.UPLOAD + "='true' "
			+ MoveFlexBinaryDataProcessor.Config.STORE + "='" + OTHER_STORE + "' "
			+ MoveFlexBinaryDataProcessor.Config.THRESHOLD + "='1KB'"), emptySchema());

		refetchNode2();
		BinaryData smallValue = (BinaryData) node2Item(smallItem).getAttributeValue(ATTR);
		assertFalse(smallValue instanceof BlobBinaryData);
		assertContent(small, "a/b", "small", smallValue);
		BinaryData largeValue = (BinaryData) node2Item(largeItem).getAttributeValue(ATTR);
		assertTrue(largeValue instanceof BlobBinaryData);
		assertEquals(OTHER_STORE, ((BlobBinaryData) largeValue).getStoreName());
		assertContent(large, "a/b", "large", largeValue);
	}

	private static String processor(String settings) {
		return "<processor class='" + MoveFlexBinaryDataProcessor.class.getName() + "' " + settings + "/>";
	}

	private static SchemaConfiguration emptySchema() {
		return null;
	}

	private boolean multipleBranches() {
		return kb().getMORepository().multipleBranches();
	}

	private void addBlobColumn() throws SQLException {
		if (hasBlobColumn()) {
			return;
		}
		PooledConnection connection = kb().getConnectionPool().borrowWriteConnection();
		try {
			SQLAddColumn addColumn =
				SQLFactory.addColumn(table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
					MoveFlexBinaryDataProcessor.BLOB_DATA_DB_NAME, DBType.BLOB);
			addColumn.setMandatory(false);
			query(addColumn).toSql(connection.getSQLDialect()).executeUpdate(connection);
			connection.commit();
		} finally {
			kb().getConnectionPool().releaseWriteConnection(connection);
		}
	}

	private void dropBinaryTable() throws SQLException {
		PooledConnection connection = kb().getConnectionPool().borrowWriteConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			try (PreparedStatement statement = connection.prepareStatement(
				"DROP TABLE " + sql.tableRef(AbstractFlexDataManager.FLEX_BINARY_DATA_DB_NAME))) {
				statement.executeUpdate();
			}
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

	private int countBinaryFlexRows() throws SQLException {
		PooledConnection connection = kb().getConnectionPool().borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COUNT(*) FROM " + sql.tableRef(AbstractFlexDataManager.FLEX_DATA_DB_NAME)
					+ " WHERE " + sql.columnRef(AbstractFlexDataManager.DATA_TYPE_DBNAME) + " = ?")) {
				statement.setInt(1, MoveFlexBinaryDataProcessor.BLOB_TYPE);
				try (ResultSet result = statement.executeQuery()) {
					assertTrue(result.next());
					return result.getInt(1);
				}
			}
		} finally {
			kb().getConnectionPool().releaseReadConnection(connection);
		}
	}

	private void insertBinaryRow(ObjectKey key, long revMin, long revMax, byte[] content, Long size,
			String contentType, String name) throws SQLException {
		PooledConnection connection = kb().getConnectionPool().borrowWriteConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			boolean branches = multipleBranches();
			StringBuilder insert = new StringBuilder();
			insert.append("INSERT INTO ").append(sql.tableRef(AbstractFlexDataManager.FLEX_DATA_DB_NAME)).append(" (");
			if (branches) {
				insert.append(sql.columnRef(AbstractFlexDataManager.BRANCH_DBNAME)).append(", ");
			}
			insert.append(sql.columnRef(AbstractFlexDataManager.TYPE_DBNAME)).append(", ")
				.append(sql.columnRef(AbstractFlexDataManager.IDENTIFIER_DBNAME)).append(", ")
				.append(sql.columnRef(BasicTypes.REV_MAX_DB_NAME)).append(", ")
				.append(sql.columnRef(AbstractFlexDataManager.ATTRIBUTE_DBNAME)).append(", ")
				.append(sql.columnRef(BasicTypes.REV_MIN_DB_NAME)).append(", ")
				.append(sql.columnRef(AbstractFlexDataManager.DATA_TYPE_DBNAME)).append(", ")
				.append(sql.columnRef(AbstractFlexDataManager.LONG_DATA_DBNAME)).append(", ")
				.append(sql.columnRef(AbstractFlexDataManager.VARCHAR_DATA_DBNAME)).append(", ")
				.append(sql.columnRef(AbstractFlexDataManager.CLOB_DATA_DBNAME)).append(", ")
				.append(sql.columnRef(MoveFlexBinaryDataProcessor.BLOB_DATA_DB_NAME))
				.append(") VALUES (");
			int params = branches ? 11 : 10;
			for (int n = 0; n < params; n++) {
				if (n > 0) {
					insert.append(", ");
				}
				insert.append('?');
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
				statement.setString(index++, ATTR);
				statement.setLong(index++, revMin);
				statement.setInt(index++, MoveFlexBinaryDataProcessor.BLOB_TYPE);
				sql.setFromJava(statement, size, index++, DBType.LONG);
				sql.setFromJava(statement, contentType, index++, DBType.STRING);
				sql.setFromJava(statement, name, index++, DBType.CLOB);
				statement.setBytes(index++, content);
				statement.executeUpdate();
			}
			connection.commit();
		} finally {
			kb().getConnectionPool().releaseWriteConnection(connection);
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return binaryMigrationSuite(TestMoveFlexBinaryData.class);
	}

}
