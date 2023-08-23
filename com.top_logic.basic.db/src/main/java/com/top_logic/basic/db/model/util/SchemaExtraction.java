/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model.util;

import static com.top_logic.basic.db.model.DBSchemaFactory.*;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBForeignKey;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBIndex.Kind;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.DBTableRef;
import com.top_logic.basic.sql.DBConstraintType;
import com.top_logic.basic.sql.DBDeferability;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;

/**
 * Creation of a {@link DBSchema} definition from an existing JDBC data source.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SchemaExtraction {
	
	private static final NamedConstant NONE = new NamedConstant("none");

	public static class ExtractionError extends RuntimeException {

		public ExtractionError() {
			super();
		}

		public ExtractionError(String message, Throwable cause) {
			super(message, cause);
		}

		public ExtractionError(String message) {
			super(message);
		}

		public ExtractionError(Throwable cause) {
			super(cause);
		}

	}

	private final DatabaseMetaData metaData;

	private final DBHelper sqlDialect;

	/**
	 * Creates a {@link SchemaExtraction}.
	 * 
	 * @param metaData
	 *        The {@link DatabaseMetaData} to interpret.
	 * @param sqlDialect
	 *        The SQL dialect of the source database.
	 */
	public SchemaExtraction(DatabaseMetaData metaData, DBHelper sqlDialect) {
		this.metaData = metaData;
		this.sqlDialect = sqlDialect;
	}

	/**
	 * Extract the definition of all tables from the underlying meta data and
	 * adds it to the given {@link DBSchema}.
	 * 
	 * @param schema
	 *        The schema to create the extracted table in.
	 * @throws SQLException
	 *         If access to the source database fails.
	 */
	public void addTables(DBSchema schema) throws SQLException {
		String catalog = metaData.getConnection().getCatalog();
		String schemaName = sqlDialect.getCurrentSchema(metaData.getConnection());

		schema.setName(schemaName);

		String[] types = {"TABLE"};
		try (ResultSet tables = metaData.getTables(catalog, schemaName, "%", types)) {
			int TABLE_NAME = 3;
			int REMARKS = 5;
			
			while (tables.next()) {
				String tableName = tables.getString(TABLE_NAME);
				String comment = tables.getString(REMARKS);
				
				DBTable table = addTable(schema, tableName);
				if (!StringServices.isEmpty(comment)) {
					table.setComment(comment);
				}
			}
		}

		for (DBTable table : schema.getTables()) {
			addForeignKeys(catalog, schemaName, table);
		}
	}

	// parent key table catalog (may be null)
	static final int XREF_PKTABLE_CAT = 1;

	// parent key table schema (may be null)
	static final int XREF_PKTABLE_SCHEM = 2;

	// parent key table name
	static final int PKTABLE_NAME = 3;

	// parent key column name
	static final int XREF_PKCOLUMN_NAME = 4;

	// foreign key table catalog (may be null) being exported (may be null)
	static final int XREF_FKTABLE_CAT = 5;

	// foreign key table schema (may be null) being exported (may be null)
	static final int XREF_FKTABLE_SCHEM = 6;

	// foreign key table name being exported
	static final int XREF_FKTABLE_NAME = 7;

	// foreign key column name being exported
	static final int XREF_FKCOLUMN_NAME = 8;

	// sequence number within foreign key (a value of 1 represents the first column of the
	// foreign key, a value of 2 would represent the second column within the foreign key).
	static final int XREF_KEY_SEQ = 9;

	// What happens to foreign key when parent key is updated:
	static final int XREF_UPDATE_RULE = 10;

	// What happens to the foreign key when parent key is deleted.
	static final int XREF_DELETE_RULE = 11;

	// foreign key name (may be null)
	static final int XREF_FK_NAME = 12;

	// parent key name (may be null)
	static final int XREF_PK_NAME = 13;

	// can the evaluation of foreign key constraints be deferred until commit
	static final int XREF_DEFERRABILITY = 14;

	private void addForeignKeys(String catalog, String schemaName, DBTable table) throws SQLException {
		String tableName = table.getDBName();
		try (ResultSet crossReference = metaData.getImportedKeys(catalog, schemaName, tableName)) {
			while (crossReference.next()) {
				int seq = crossReference.getInt(XREF_KEY_SEQ);
				if (seq != 1) {
					// Composed foreign keys not supported.
					continue;
				}

				String fkTableName = crossReference.getString(XREF_FKTABLE_NAME);
				String pkTableName = crossReference.getString(PKTABLE_NAME);
				String fkColumnName = crossReference.getString(XREF_FKCOLUMN_NAME);
				String pkColumnName = crossReference.getString(XREF_PKCOLUMN_NAME);
				String fkName = crossReference.getString(XREF_FK_NAME);

				assert tableName.equals(fkTableName) : "Unexpected table name: " + fkTableName + ", expected: "
					+ tableName;

				DBConstraintType onUpdate = type(crossReference.getShort(XREF_UPDATE_RULE));
				DBConstraintType onDelete = type(crossReference.getShort(XREF_DELETE_RULE));
				DBDeferability deferability = deferability(crossReference.getShort(XREF_DEFERRABILITY));

				DBForeignKey key = TypedConfiguration.newConfigItem(DBForeignKey.class);

				key.setName(fkName);
				key.setOnUpdate(onUpdate);
				key.setOnDelete(onDelete);
				key.setDeferability(deferability);

				DBTableRef targetTableRef = TypedConfiguration.newConfigItem(DBTableRef.class);
				targetTableRef.setName(pkTableName);
				key.setTargetTableRef(targetTableRef);

				DBColumnRef sourceRef = TypedConfiguration.newConfigItem(DBColumnRef.class);
				sourceRef.setName(fkColumnName);
				key.getSourceColumnRefs().add(sourceRef);

				DBColumnRef targetRef = TypedConfiguration.newConfigItem(DBColumnRef.class);
				targetRef.setName(pkColumnName);
				key.getTargetColumnRefs().add(targetRef);

				table.getForeignKeys().add(key);
			}
		}
	}
	
	private static DBDeferability deferability(short value) {
		switch (value) {
			case DatabaseMetaData.importedKeyInitiallyDeferred:
				return DBDeferability.DEFERRED;
			case DatabaseMetaData.importedKeyInitiallyImmediate:
				return DBDeferability.IMMEDIATE;
			case DatabaseMetaData.importedKeyNotDeferrable:
				return DBDeferability.STRICT;
		}
		return DBDeferability.DEFERRED;
	}

	private static DBConstraintType type(short value) {
		switch (value) {
			case DatabaseMetaData.importedKeyRestrict:
				return DBConstraintType.RESTRICT;
			case DatabaseMetaData.importedKeyCascade:
				return DBConstraintType.CASCADE;
			case DatabaseMetaData.importedKeyNoAction:
			case DatabaseMetaData.importedKeySetDefault:
			case DatabaseMetaData.importedKeySetNull:
				return DBConstraintType.CLEAR;
		}
		return DBConstraintType.RESTRICT;
	}

	/**
	 * Extract the definition of the table with the given name from the
	 * underlying meta data and adds it to the given {@link DBSchema}.
	 * 
	 * @param schema
	 *        The schema to create the extracted table in.
	 * @param tableName
	 *        The name of the table to extract.
	 * @return The extracted table.
	 * @throws SQLException
	 *         If access to the source database fails.
	 */
	public DBTable addTable(DBSchema schema, String tableName) throws SQLException {
		DBTable table = DBSchemaFactory.createTable(tableName);
		String sourceCatalog = metaData.getConnection().getCatalog();
		String sourceSchema = sqlDialect.getCurrentSchema(metaData.getConnection());
		try (ResultSet columns =
			metaData.getColumns(sourceCatalog, sourceSchema, sqlDialect.tablePattern(tableName), "%")) {
			int COLUMN_NAME = 4;
			int DATA_TYPE = 5;
			int TYPE_NAME = 6;
			int COLUMN_SIZE = 7;
			int DECIMAL_DIGITS = 9;
			int NULLABLE = 11;
			int REMARKS = 12;
			int CHAR_OCTET_LENGTH = 16;
			
			while (columns.next()) {
				String columnName = columns.getString(COLUMN_NAME);
				String comment = columns.getString(REMARKS);
				
				int sqlType = columns.getInt(DATA_TYPE);
				String sqlTypeName = columns.getString(TYPE_NAME);
				int nullable = columns.getInt(NULLABLE);
				int size = columns.getInt(COLUMN_SIZE);
				int scale = columns.getInt(DECIMAL_DIGITS);
				
				DBType dbType = sqlDialect.analyzeSqlType(sqlType, sqlTypeName, size, scale);
				DBColumn column = DBSchemaFactory.createColumn(columnName);
				column.setType(dbType);
				
				if (dbType.binaryParam) {
					int octetSize = columns.getInt(CHAR_OCTET_LENGTH);
					boolean binary = sqlDialect.analyzeSqlTypeBinary(sqlType, sqlTypeName, size, octetSize);
					column.setBinary(binary);
				}
	
				column.setMandatory(nullable == DatabaseMetaData.columnNoNulls);
				if (!StringServices.isEmpty(comment)) {
					column.setComment(comment);
				}
				
				if (dbType.sizeParam) {
					column.setSize(size);
				}
				
				if (dbType.precisionParam) {
					column.setPrecision(scale);
				}
				
				table.getColumns().add(column);
			}
		}
		assert table.getColumns().size() > 0 : "No columns in table.";
		
		String pkName = null;
		List<DBColumn> keyColumns = new ArrayList<>(); 
		try (ResultSet primaryKeys =
			metaData.getPrimaryKeys(sourceCatalog, sourceSchema, sqlDialect.tablePattern(tableName))) {
			final int COLUMN_NAME = 4; 
			final int KEY_SEQ = 5;
			final int PK_NAME = 6;
			
			int keySize = 0;
			while (primaryKeys.next()) {
				pkName = primaryKeys.getString(PK_NAME);
				
				int order = primaryKeys.getShort(KEY_SEQ);
				String columnName = primaryKeys.getString(COLUMN_NAME);
				
				DBColumn column = table.getColumn(columnName);
				if (column == null) {
					throw new ExtractionError("Column '" + columnName + "' does not exist in extracted table.");
				}
				
				while (keyColumns.size() < order) {
					keyColumns.add(null);
				}
				
				DBColumn clash = keyColumns.set(order - 1, column);
				if (clash != null) {
					throw new ExtractionError("Same index '" + order + "' seen twice for column '" + clash.getName() + "' and '" + column.getName() + "'.");
				}
				
				keySize++;
			}
			
			if (keySize < keyColumns.size()) {
				throw new ExtractionError("Missing columns in index of size '" + keyColumns.size() + "', only '" + keySize + "' seen.");
			}
		}

		if (! keyColumns.isEmpty()) {
			DBPrimary primaryKey = DBSchemaFactory.createPrimary();
			primaryKey.setName(pkName);
			primaryKey.setKind(Kind.PRIMARY);
			primaryKey.getColumnRefs().addAll(refs(keyColumns));
			table.setPrimaryKey(primaryKey);
		}
		
		try (ResultSet indexInfo = metaData.getIndexInfo(sourceCatalog, sourceSchema, tableName, /* unique */ false,
			/* approximate */ false)) {
			Object currentIndexName = NONE;
			DBIndex currentIndex = null;
			while (indexInfo.next()) {
				final int NON_UNIQUE = 4; 
				// final int INDEX_QUALIFIER = 5; 
				final int INDEX_NAME = 6; 
				final int TYPE = 7; 
				// final int ORDINAL_POSITION = 8; 
				final int COLUMN_NAME = 9; 

				short type = indexInfo.getShort(TYPE);
				if (type == DatabaseMetaData.tableIndexStatistic) {
					continue;
				}
				
				// Separate indices.
				String indexName = indexInfo.getString(INDEX_NAME);
				
				// Skip primary key.
				if (StringServices.equals(indexName, pkName)) {
					continue;
				}
				
				if (! StringServices.equals(indexName, currentIndexName)) {
					currentIndexName = indexName;
					
					currentIndex = DBSchemaFactory.createIndex(indexName);
					table.getIndices().add(currentIndex);
					
					boolean nonUnique = indexInfo.getBoolean(NON_UNIQUE);
					currentIndex.setKind(nonUnique ? Kind.DEFAULT : Kind.UNIQUE);
				}
				
				assert currentIndex != null : "Index is allocated in first iteration.";
				
				String columnName = indexInfo.getString(COLUMN_NAME);
				DBColumn column = table.getColumn(columnName);
				if (column == null) {
					throw new ExtractionError("Column '" + columnName + "' does not exist in extracted table.");
				}
				currentIndex.getColumnRefs().add(ref(column));
			}
		}
		schema.getTables().add(table);
		
		return table;
	}
}