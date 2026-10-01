/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.blob.BlobBinaryData;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.storage.BinaryColumnsStorage;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Moves the content of binary values stored in the columns of an {@link AbstractBinaryAttribute}
 * of a table between the database and blob stores.
 *
 * <p>
 * All rows of the table are processed, including historic revisions. The rows are read with a
 * single query and updated in batches; the content of each row is streamed and never kept in
 * memory for more than one batch.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class BinaryContentMigration {

	/**
	 * The content type of values without a stored content type.
	 */
	public static final String DEFAULT_CONTENT_TYPE = BinaryData.CONTENT_TYPE_OCTET_STREAM;

	/**
	 * Upper bound of the number of rows updated in one batch.
	 */
	private static final int MAX_BATCH_SIZE = 100;

	/**
	 * Number of processed rows after which progress is logged.
	 */
	private static final int LOG_INTERVAL = 10000;

	private BinaryContentMigration() {
		// Utility class.
	}

	/**
	 * Fills the size and the content type of rows that have inline content but no size.
	 *
	 * <p>
	 * The size is computed by reading the inline content, the content type of rows without content
	 * type is set to {@link #DEFAULT_CONTENT_TYPE}. The name is left empty.
	 * </p>
	 *
	 * @param log
	 *        The log to report to.
	 * @param connection
	 *        The connection to the database.
	 * @param table
	 *        The table to update.
	 * @param attribute
	 *        The attribute whose columns are filled. It must have a
	 *        {@link AbstractBinaryAttribute#getDataColumn() data column}.
	 * @param filter
	 *        Additional condition restricting the rows to update, <code>null</code> for all rows.
	 * @return The number of updated rows.
	 */
	public static int fillMissingMetadata(Log log, PooledConnection connection, MOStructure table,
			AbstractBinaryAttribute attribute, SQLExpression filter) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		String tableName = table.getDBMapping().getDBName();
		List<DBAttribute> keyColumns = keyColumns(table);
		DBAttribute sizeColumn = attribute.getSizeColumn();
		DBAttribute contentTypeColumn = attribute.getContentTypeColumn();
		DBAttribute dataColumn = attribute.getDataColumn();

		List<SQLColumnDefinition> columns = new ArrayList<>();
		for (DBAttribute keyColumn : keyColumns) {
			columns.add(columnDef(keyColumn.getDBName()));
		}
		columns.add(columnDef(dataColumn.getDBName()));
		CompiledStatement select = query(
			select(columns, table(tableName),
				and(nonNull(filter),
					isNull(column(sizeColumn.getDBName())),
					not(isNull(column(dataColumn.getDBName())))))).toSql(sqlDialect);

		String update = "UPDATE " + sqlDialect.tableRef(tableName)
			+ " SET " + sqlDialect.columnRef(sizeColumn.getDBName()) + " = ?"
			+ " WHERE " + keyCondition(sqlDialect, keyColumns);

		int maxBatchSize = maxBatchSize(sqlDialect, 1 + keyColumns.size());
		int total = 0;
		try (PreparedStatement statement = connection.prepareStatement(update)) {
			int batchSize = 0;
			try (ResultSet rows = select.executeQuery(connection)) {
				while (rows.next()) {
					Object[] key = readKey(sqlDialect, rows, keyColumns);
					long size;
					try (InputStream content = sqlDialect.getBinaryStream(rows, keyColumns.size() + 1)) {
						size = StreamUtilities.size(content);
					} catch (IOException ex) {
						throw new SQLException("Reading content of table '" + tableName + "' failed.", ex);
					}

					sqlDialect.setFromJava(statement, Long.valueOf(size), 1, sizeColumn.getSQLType());
					bindKey(sqlDialect, statement, 2, keyColumns, key);
					statement.addBatch();
					if (++batchSize >= maxBatchSize) {
						statement.executeBatch();
						total += batchSize;
						batchSize = 0;
					}
				}
			}
			if (batchSize > 0) {
				statement.executeBatch();
				total += batchSize;
			}
		}

		CompiledStatement setContentType = query(
			update(table(tableName),
				and(nonNull(filter),
					isNull(column(contentTypeColumn.getDBName())),
					not(isNull(column(sizeColumn.getDBName())))),
				columnNames(contentTypeColumn.getDBName()),
				expressions(literal(DBType.STRING, DEFAULT_CONTENT_TYPE)))).toSql(sqlDialect);
		int typesSet = setContentType.executeUpdate(connection);

		log.info("Computed the size of " + total + " and set the content type of " + typesSet
			+ " values of attribute '" + attribute.getName() + "' in table '" + table.getName() + "'.");
		return total;
	}

	/**
	 * Re-stores the content of all rows of a table according to the given placement.
	 *
	 * <p>
	 * Each row is read through the columns of the source attribute and written to the columns of
	 * the target attribute. Rows whose content is already where the placement requires it are not
	 * updated. Content that must be stored externally is uploaded to the store of the placement,
	 * also when it is stored in another store (the content in the other store becomes
	 * unreferenced). Content that must be stored inline is read from its store and written to the
	 * data column of the target.
	 * </p>
	 *
	 * <p>
	 * Source and target must have the same database base name, so that their data columns, and
	 * their key and hash columns, are the same columns. All columns of the target must exist.
	 * </p>
	 *
	 * @param log
	 *        The log to report to.
	 * @param connection
	 *        The connection to the database.
	 * @param table
	 *        The table to update.
	 * @param source
	 *        The attribute describing the columns that currently hold the values.
	 * @param target
	 *        The attribute describing the columns to write.
	 * @param placement
	 *        The decision where to store the content of each row. The placement must be consistent
	 *        with the target: a target without data column only takes external content, a target
	 *        without key column only inline content.
	 * @param filter
	 *        Additional condition restricting the rows to process, <code>null</code> for all rows.
	 * @return The number of updated rows.
	 */
	public static int moveContent(Log log, PooledConnection connection, MOStructure table,
			AbstractBinaryAttribute source, AbstractBinaryAttribute target, BinaryPlacement placement,
			SQLExpression filter) throws SQLException, IOException {
		DBHelper sqlDialect = connection.getSQLDialect();
		String tableName = table.getDBMapping().getDBName();
		List<DBAttribute> keyColumns = keyColumns(table);

		List<SQLColumnDefinition> columns = new ArrayList<>();
		for (DBAttribute keyColumn : keyColumns) {
			columns.add(columnDef(keyColumn.getDBName()));
		}
		Map<DBAttribute, Integer> sourceIndex = new IdentityHashMap<>();
		for (DBAttribute column : source.getDbMapping()) {
			columns.add(columnDef(column.getDBName()));
			sourceIndex.put(column, Integer.valueOf(columns.size()));
		}
		CompiledStatement select = query(
			select(columns, table(tableName),
				and(nonNull(filter), not(isNull(column(source.getSizeColumn().getDBName())))))).toSql(sqlDialect);

		DBAttribute[] targetColumns = target.getDbMapping();
		StringBuilder update = new StringBuilder();
		update.append("UPDATE ").append(sqlDialect.tableRef(tableName)).append(" SET ");
		for (int n = 0; n < targetColumns.length; n++) {
			if (n > 0) {
				update.append(", ");
			}
			update.append(sqlDialect.columnRef(targetColumns[n].getDBName())).append(" = ?");
		}
		update.append(" WHERE ").append(keyCondition(sqlDialect, keyColumns));

		log.info("Moving content of attribute '" + source.getName() + "' in table '" + table.getName() + "' to "
			+ placement + ".");

		int maxBatchSize = maxBatchSize(sqlDialect, targetColumns.length + keyColumns.size());
		int total = 0;
		int processed = 0;
		List<InputStream> openStreams = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(update.toString())) {
			int batchSize = 0;
			try (ResultSet rows = select.executeQuery(connection)) {
				while (rows.next()) {
					processed++;
					if (processed % LOG_INTERVAL == 0) {
						log.info("Processed " + processed + " rows of table '" + table.getName() + "', updated "
							+ (total + batchSize) + ".");
					}

					Object[] key = readKey(sqlDialect, rows, keyColumns);
					BinaryData placed = placeRow(sqlDialect, rows, source, sourceIndex, placement, openStreams);
					if (placed == null) {
						continue;
					}

					Object[] values = BinaryColumnsStorage.columnValues(target, placed);
					for (int n = 0; n < targetColumns.length; n++) {
						sqlDialect.setFromJava(statement, values[n], n + 1, targetColumns[n].getSQLType());
					}
					bindKey(sqlDialect, statement, targetColumns.length + 1, keyColumns, key);
					statement.addBatch();
					if (++batchSize >= maxBatchSize) {
						executeBatch(statement, openStreams);
						total += batchSize;
						batchSize = 0;
					}
				}
			}
			if (batchSize > 0) {
				executeBatch(statement, openStreams);
				total += batchSize;
			}
		} finally {
			closeAll(openStreams);
		}

		log.info("Moved content of " + total + " of " + processed + " values of attribute '" + source.getName()
			+ "' in table '" + table.getName() + "'.");
		return total;
	}

	/**
	 * Decides where the content of the current row goes.
	 *
	 * @return The value to store in the row, <code>null</code> if the row is left unchanged.
	 */
	private static BinaryData placeRow(DBHelper sqlDialect, ResultSet rows, AbstractBinaryAttribute source,
			Map<DBAttribute, Integer> sourceIndex, BinaryPlacement placement, List<InputStream> openStreams)
			throws SQLException, IOException {
		BlobBinaryData reference =
			BinaryColumnsStorage.fetchReference(rows, source, column -> sourceIndex.get(column).intValue());
		long size = rows.getLong(sourceIndex.get(source.getSizeColumn()).intValue());
		boolean external = placement.isExternal(size);
		if (reference == null) {
			DBAttribute dataColumn = source.getDataColumn();
			if (dataColumn == null || !external) {
				// Inline content stays inline.
				return null;
			}
			String contentType = rows.getString(sourceIndex.get(source.getContentTypeColumn()).intValue());
			String name = rows.getString(sourceIndex.get(source.getNameColumn()).intValue());

			// The data column is the last column of the row and is read last.
			try (InputStream content = sqlDialect.getBinaryStream(rows, sourceIndex.get(dataColumn).intValue())) {
				if (content == null) {
					return null;
				}
				return BlobUpload.upload(placement.getStoreName(), content, size, contentType, name);
			}
		}

		if (external) {
			if (placement.isTargetStore(reference.getStoreName())) {
				return null;
			}
			return BlobUpload.upload(placement.getStoreName(), reference);
		}
		return new TrackedContent(reference, openStreams);
	}

	/**
	 * Prepares the given value for storing it according to the given placement.
	 *
	 * <p>
	 * Content that must be stored externally is uploaded to the store of the placement, unless it
	 * is already stored there. Content that must be stored inline is returned as {@link BinaryData}
	 * that is no {@link BlobBinaryData}, so that it is written to the data column.
	 * </p>
	 *
	 * @param value
	 *        The value to store, or <code>null</code>.
	 * @param placement
	 *        The decision where to store the content.
	 * @return The value to write with
	 *         {@link BinaryColumnsStorage#columnValues(AbstractBinaryAttribute, BinaryData)}.
	 */
	public static BinaryData place(BinaryData value, BinaryPlacement placement) throws IOException {
		if (value == null) {
			return null;
		}
		BinaryData data = BlobUpload.withKnownSize(value);
		if (placement.isExternal(data.getSize())) {
			if (data instanceof BlobBinaryData blob && placement.isTargetStore(blob.getStoreName())) {
				return blob;
			}
			return BlobUpload.upload(placement.getStoreName(), data);
		}
		if (data instanceof BlobBinaryData) {
			return new TrackedContent(data, null);
		}
		return data;
	}

	private static void executeBatch(PreparedStatement statement, List<InputStream> openStreams)
			throws SQLException {
		try {
			statement.executeBatch();
		} finally {
			closeAll(openStreams);
		}
	}

	private static void closeAll(List<InputStream> openStreams) {
		for (InputStream stream : openStreams) {
			try {
				stream.close();
			} catch (IOException ex) {
				// Ignore, the content has been consumed.
			}
		}
		openStreams.clear();
	}

	/**
	 * The primary key columns of the given table.
	 */
	static List<DBAttribute> keyColumns(MOStructure table) throws SQLException {
		MOIndex primaryKey = table.getPrimaryKey();
		if (primaryKey == null) {
			throw new SQLException("Table '" + table.getName() + "' has no primary key.");
		}
		return primaryKey.getKeyAttributes();
	}

	private static SQLExpression nonNull(SQLExpression filter) {
		return filter == null ? literalTrueLogical() : filter;
	}

	private static String keyCondition(DBHelper sqlDialect, List<DBAttribute> keyColumns) {
		StringBuilder result = new StringBuilder();
		for (DBAttribute keyColumn : keyColumns) {
			if (result.length() > 0) {
				result.append(" AND ");
			}
			result.append(sqlDialect.columnRef(keyColumn.getDBName())).append(" = ?");
		}
		return result.toString();
	}

	private static Object[] readKey(DBHelper sqlDialect, ResultSet rows, List<DBAttribute> keyColumns)
			throws SQLException {
		Object[] key = new Object[keyColumns.size()];
		for (int n = 0; n < key.length; n++) {
			key[n] = sqlDialect.mapToJava(rows, n + 1, keyColumns.get(n).getSQLType());
		}
		return key;
	}

	private static void bindKey(DBHelper sqlDialect, PreparedStatement statement, int offset,
			List<DBAttribute> keyColumns, Object[] key) throws SQLException {
		for (int n = 0; n < key.length; n++) {
			sqlDialect.setFromJava(statement, key[n], offset + n, keyColumns.get(n).getSQLType());
		}
	}

	private static int maxBatchSize(DBHelper sqlDialect, int parameters) {
		return Math.max(1, Math.min(MAX_BATCH_SIZE, sqlDialect.getMaxBatchSize(parameters)));
	}

	/**
	 * Content to write inline, whose streams are closed after the batch writing them.
	 */
	private static final class TrackedContent extends AbstractBinaryData {

		private final BinaryData _content;

		private final List<InputStream> _openStreams;

		/**
		 * Creates a {@link TrackedContent}.
		 *
		 * @param content
		 *        The content to write.
		 * @param openStreams
		 *        The list to register opened streams in, <code>null</code> to leave closing
		 *        streams to the reader.
		 */
		TrackedContent(BinaryData content, List<InputStream> openStreams) {
			_content = content;
			_openStreams = openStreams;
		}

		@Override
		public long getSize() {
			return _content.getSize();
		}

		@Override
		public String getContentType() {
			return _content.getContentType();
		}

		@Override
		public String getName() {
			return _content.getName();
		}

		@Override
		public InputStream getStream() throws IOException {
			InputStream result = _content.getStream();
			if (_openStreams != null) {
				_openStreams.add(result);
			}
			return result;
		}

	}

}
