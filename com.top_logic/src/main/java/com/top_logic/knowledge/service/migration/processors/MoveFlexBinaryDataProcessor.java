/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} moving binary values of dynamic attributes from the generic table of
 * dynamic attribute values to the table of dynamic binary values.
 *
 * <p>
 * In the generic table, a binary value is a row with the data type {@value #BLOB_TYPE}, the size in
 * the long column, the content type in the string column, the name in the text column and the
 * content in a BLOB column {@value #BLOB_DATA_DB_NAME}. All these rows are moved with their
 * revision ranges into the table of dynamic binary values, keeping the content inline. A missing
 * size is computed from the content, a missing content type is set to
 * <code>application/octet-stream</code>. Afterwards, the rows are deleted from the generic table
 * and its BLOB column is dropped. The table of dynamic binary values is created, if it does not
 * exist yet.
 * </p>
 *
 * <p>
 * Optionally, the moved content from a threshold on is uploaded to a blob store in the same step.
 * The content can also be moved later on with {@link MigrateBinaryAttributeProcessor}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MoveFlexBinaryDataProcessor extends AbstractConfiguredInstance<MoveFlexBinaryDataProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Data type of a binary value in the generic table of dynamic attribute values.
	 */
	public static final byte BLOB_TYPE = 50;

	/**
	 * Name of the BLOB column of the generic table of dynamic attribute values.
	 */
	public static final String BLOB_DATA_DB_NAME = "BLOB_DATA";

	/**
	 * Configuration options for {@link MoveFlexBinaryDataProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends MoveFlexBinaryDataProcessor> extends PolymorphicConfiguration<I> {

		/** Tag name of a {@link MoveFlexBinaryDataProcessor} in a migration script. */
		String TAG_NAME = "move-flex-binary-data";

		/** Configuration name of {@link #isUpload()}. */
		String UPLOAD = "upload";

		/** Configuration name of {@link #getStore()}. */
		String STORE = "store";

		/** Configuration name of {@link #getThreshold()}. */
		String THRESHOLD = "threshold";

		/**
		 * Whether moved content from the threshold on is uploaded to the blob store.
		 *
		 * <p>
		 * Without upload, all moved content is kept in the database.
		 * </p>
		 */
		@Name(UPLOAD)
		boolean isUpload();

		/**
		 * The name of the blob store to upload to.
		 *
		 * <p>
		 * If no store is given, the default store of the blob store service is used.
		 * </p>
		 */
		@Name(STORE)
		@Nullable
		String getStore();

		/**
		 * The size from which on moved content is uploaded to the blob store.
		 *
		 * <p>
		 * The size is given in bytes, optionally with a unit, e.g. <code>64KB</code> or
		 * <code>1MB</code>.
		 * </p>
		 */
		@Name(THRESHOLD)
		@Format(MemorySizeFormat.class)
		@LongDefault(HybridBinaryAttribute.DEFAULT_THRESHOLD)
		long getThreshold();

	}

	/**
	 * Creates a {@link MoveFlexBinaryDataProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MoveFlexBinaryDataProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			MOStructure binaryTable = ensureTable(context, log, connection);
			AbstractBinaryAttribute content =
				(AbstractBinaryAttribute) binaryTable.getAttribute(AbstractFlexDataManager.CONTENT);

			if (!hasBlobColumn(connection)) {
				log.info("No column '" + BLOB_DATA_DB_NAME + "' in table '" + AbstractFlexDataManager.FLEX_DATA_DB_NAME
					+ "', no binary values to move.");
				return;
			}

			int moved = moveRows(context, connection, binaryTable, content);
			log.info("Moved " + moved + " binary values from table '" + AbstractFlexDataManager.FLEX_DATA_DB_NAME
				+ "' to table '" + binaryTable.getDBMapping().getDBName() + "'.");

			BinaryContentMigration.fillMissingMetadata(log, connection, binaryTable, content, null);

			int deleted = query(
				delete(table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
					eqSQL(column(AbstractFlexDataManager.DATA_TYPE_DBNAME), literal(DBType.INT, BLOB_TYPE))))
						.toSql(connection.getSQLDialect()).executeUpdate(connection);
			log.info("Deleted " + deleted + " binary values from table '" + AbstractFlexDataManager.FLEX_DATA_DB_NAME
				+ "'.");

			new SQLProcessor(connection)
				.execute(dropColumn(table(AbstractFlexDataManager.FLEX_DATA_DB_NAME), BLOB_DATA_DB_NAME));
			log.info("Dropped column '" + BLOB_DATA_DB_NAME + "' of table '" + AbstractFlexDataManager.FLEX_DATA_DB_NAME
				+ "'.");

			Config<?> config = getConfig();
			if (config.isUpload()) {
				BinaryPlacement placement =
					new BinaryPlacement(BinaryAttributeKind.HYBRID, config.getStore(), config.getThreshold());
				BinaryContentMigration.moveContent(log, connection, binaryTable, content, content, placement,
					isNull(column(content.getKeyColumn().getDBName())));
			}
		} catch (SQLException | IOException | RuntimeException ex) {
			log.error("Failed to move binary values from table '" + AbstractFlexDataManager.FLEX_DATA_DB_NAME
				+ "': " + ex.getMessage(), ex);
		}
	}

	private int moveRows(MigrationContext context, PooledConnection connection, MOStructure binaryTable,
			AbstractBinaryAttribute content) throws SQLException {
		List<String> targetColumns = new ArrayList<>();
		List<SQLColumnDefinition> sourceColumns = new ArrayList<>();
		if (context.hasBranchSupport()) {
			targetColumns.add(AbstractFlexDataManager.BRANCH_DBNAME);
			sourceColumns.add(columnDef(AbstractFlexDataManager.BRANCH_DBNAME));
		}
		add(targetColumns, sourceColumns, AbstractFlexDataManager.TYPE_DBNAME, AbstractFlexDataManager.TYPE_DBNAME);
		add(targetColumns, sourceColumns, AbstractFlexDataManager.IDENTIFIER_DBNAME,
			AbstractFlexDataManager.IDENTIFIER_DBNAME);
		add(targetColumns, sourceColumns, BasicTypes.REV_MAX_DB_NAME, BasicTypes.REV_MAX_DB_NAME);
		add(targetColumns, sourceColumns, AbstractFlexDataManager.ATTRIBUTE_DBNAME,
			AbstractFlexDataManager.ATTRIBUTE_DBNAME);
		add(targetColumns, sourceColumns, BasicTypes.REV_MIN_DB_NAME, BasicTypes.REV_MIN_DB_NAME);
		add(targetColumns, sourceColumns, content.getSizeColumn().getDBName(),
			AbstractFlexDataManager.LONG_DATA_DBNAME);
		add(targetColumns, sourceColumns, content.getContentTypeColumn().getDBName(),
			AbstractFlexDataManager.VARCHAR_DATA_DBNAME);
		add(targetColumns, sourceColumns, content.getNameColumn().getDBName(),
			AbstractFlexDataManager.CLOB_DATA_DBNAME);
		add(targetColumns, sourceColumns, content.getDataColumn().getDBName(), BLOB_DATA_DB_NAME);

		CompiledStatement copy = query(
			insert(
				table(binaryTable.getDBMapping().getDBName()),
				targetColumns,
				select(
					sourceColumns,
					table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
					and(
						eqSQL(column(AbstractFlexDataManager.DATA_TYPE_DBNAME), literal(DBType.INT, BLOB_TYPE)),
						not(isNull(column(BLOB_DATA_DB_NAME)))))))
							.toSql(connection.getSQLDialect());
		return copy.executeUpdate(connection);
	}

	private static void add(List<String> targetColumns, List<SQLColumnDefinition> sourceColumns, String target,
			String source) {
		targetColumns.add(target);
		sourceColumns.add(columnDef(source));
	}

	private static boolean hasBlobColumn(PooledConnection connection) throws SQLException {
		DBTable flexTable = DBSchemaUtils.extractTable(connection.getPool(), DBSchemaFactory.createDBSchema(),
			AbstractFlexDataManager.FLEX_DATA_DB_NAME);
		if (flexTable == null) {
			return false;
		}
		for (DBColumn column : flexTable.getColumns()) {
			if (BLOB_DATA_DB_NAME.equalsIgnoreCase(column.getDBName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the table of the given type exists in the database.
	 */
	static boolean exists(PooledConnection connection, MOStructure table) throws SQLException {
		return DBSchemaUtils.exists(connection, table.getDBMapping().getDBName());
	}

	/**
	 * Creates the table of dynamic binary values, if it does not exist yet.
	 *
	 * @return The type of the table of dynamic binary values.
	 */
	public static MOStructure ensureTable(MigrationContext context, Log log, PooledConnection connection)
			throws SQLException {
		MOStructure binaryTable =
			(MOStructure) context.getPersistentRepository().getTypeOrNull(AbstractFlexDataManager.FLEX_BINARY_DATA);
		if (binaryTable == null) {
			throw new SQLException("No type '" + AbstractFlexDataManager.FLEX_BINARY_DATA + "' in the schema.");
		}
		if (!exists(connection, binaryTable)) {
			DBSchema schema = DBSchemaFactory.createDBSchema();
			// Allocate the table in the current schema.
			schema.setName(null);
			DBTable table = SchemaSetup.createTable((MOClass) binaryTable);
			schema.getTables().add(table);
			DBSchemaUtils.create(connection, table);
			log.info("Created table '" + binaryTable.getDBMapping().getDBName() + "'.");
		}
		return binaryTable;
	}

}
