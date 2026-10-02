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
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.io.blob.BlobStoreNames;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tool.boundsec.CommandHandler.ConfirmConfig.VisibleIf;

/**
 * {@link MigrationProcessor} moving binary values of dynamic attributes from a table of dynamic
 * attribute values to a table of dynamic binary values.
 *
 * <p>
 * The source table is the table of the {@link Config#getSourceType() source type}, a table of
 * dynamic attribute values. The target table is the table of the {@link Config#getTargetType()
 * target type}, a table of dynamic binary values. By default, the
 * values are moved from the generic table of dynamic attribute values of the knowledge base to its
 * table of dynamic binary values. The journal of the knowledge base keeps its values in pairs of such
 * tables, too.
 * </p>
 *
 * <p>
 * In the source table, a binary value is a row with the data type {@value #BLOB_TYPE}, the size in
 * the long column, the content type in the string column, the name in the text column and the
 * content in a BLOB column {@value #BLOB_DATA_DB_NAME}. All these rows are moved with their
 * revision ranges into the target table, keeping the content inline. A missing size is computed
 * from the content, a missing content type is set to <code>application/octet-stream</code>.
 * Afterwards, the rows are deleted from the source table and its BLOB column is dropped.
 * </p>
 *
 * <p>
 * The target table is created, if it does not exist yet, as soon as the target type is part of the
 * persistent schema. If the target type is not part of the persistent schema, or the source table
 * does not exist, there is nothing to move and the processor only logs this fact.
 * </p>
 *
 * <p>
 * Optionally, the moved content from a threshold on is uploaded to a blob store in the same step.
 * The content can also be moved later on with {@link MigrateBinaryAttributeProcessor}.
 * </p>
 *
 * @implNote The source type is created by
 *           {@link AbstractFlexDataManager#createFlexDataType(String, String, boolean)}, the target
 *           type by {@link AbstractFlexDataManager#createFlexBinaryDataType(String, String, boolean)}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MoveFlexBinaryDataProcessor extends AbstractConfiguredInstance<MoveFlexBinaryDataProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Data type of a binary value in a table of dynamic attribute values.
	 */
	public static final byte BLOB_TYPE = 50;

	/**
	 * Name of the BLOB column of a table of dynamic attribute values.
	 */
	public static final String BLOB_DATA_DB_NAME = "BLOB_DATA";

	/**
	 * Configuration options for {@link MoveFlexBinaryDataProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends MoveFlexBinaryDataProcessor> extends PolymorphicConfiguration<I> {

		/** Tag name of a {@link MoveFlexBinaryDataProcessor} in a migration script. */
		String TAG_NAME = "move-flex-binary-data";

		/** Configuration name of {@link #getSourceType()}. */
		String SOURCE_TYPE = "source-type";

		/** Configuration name of {@link #getTargetType()}. */
		String TARGET_TYPE = "target-type";

		/** Configuration name of {@link #isUpload()}. */
		String UPLOAD = "upload";

		/** Configuration name of {@link #getStore()}. */
		String STORE = "store";

		/** Configuration name of {@link #getThreshold()}. */
		String THRESHOLD = "threshold";

		/**
		 * Name of the type of the table of dynamic attribute values to move the binary values from.
		 *
		 * <p>
		 * The type has the layout of the generic table of dynamic attribute values
		 * {@link AbstractFlexDataManager#FLEX_DATA}, which is the default.
		 * </p>
		 */
		@Name(SOURCE_TYPE)
		@NonNullable
		@StringDefault(AbstractFlexDataManager.FLEX_DATA)
		String getSourceType();

		/**
		 * Name of the type of the table of dynamic binary values to move the binary values to.
		 *
		 * <p>
		 * The type has the layout of the table of dynamic binary values
		 * {@link AbstractFlexDataManager#FLEX_BINARY_DATA}, which is the default.
		 * </p>
		 */
		@Name(TARGET_TYPE)
		@NonNullable
		@StringDefault(AbstractFlexDataManager.FLEX_BINARY_DATA)
		String getTargetType();

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
		@Options(fun = BlobStoreNames.class)
		@DynamicMode(fun = VisibleIf.class, args = @Ref(UPLOAD))
		String getStore();

		/**
		 * The size from which on moved content is uploaded to the blob store.
		 *
		 * <p>
		 * The size is given in bytes, optionally with a unit, e.g. <code>64KB</code> or
		 * <code>1MB</code>. With a size of zero, all moved content is uploaded. The size must not
		 * exceed 2147483639 bytes (2 GB minus 8 bytes), the largest threshold for content of
		 * unknown size.
		 * </p>
		 *
		 * @implNote The upper bound is {@link BlobUpload#MAX_BUFFERED_THRESHOLD}.
		 */
		@Name(THRESHOLD)
		@Format(MemorySizeFormat.class)
		@LongDefault(HybridBinaryAttribute.DEFAULT_THRESHOLD)
		@Constraint(NonNegative.class)
		@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = BlobUpload.MAX_BUFFERED_THRESHOLD)
		@DynamicMode(fun = VisibleIf.class, args = @Ref(UPLOAD))
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
		Config<?> config = getConfig();
		String sourceTypeName = config.getSourceType();
		try {
			MORepository repository = context.getPersistentRepository();
			MOStructure binaryTable = (MOStructure) repository.getTypeOrNull(config.getTargetType());
			if (binaryTable == null) {
				log.info("No type '" + config.getTargetType() + "' in the schema, no binary values to move from type '"
					+ sourceTypeName + "'.");
				return;
			}
			ensureTable(log, connection, binaryTable);
			AbstractBinaryAttribute content =
				(AbstractBinaryAttribute) binaryTable.getAttribute(AbstractFlexDataManager.CONTENT);

			MOStructure sourceType = (MOStructure) repository.getTypeOrNull(sourceTypeName);
			if (sourceType == null) {
				log.info("No type '" + sourceTypeName + "' in the schema, no binary values to move.");
				return;
			}
			String sourceTable = sourceType.getDBMapping().getDBName();
			if (!DBSchemaUtils.exists(connection, sourceTable)) {
				log.info("No table '" + sourceTable + "' in the database, no binary values to move.");
				return;
			}
			if (!hasBlobColumn(connection, sourceTable)) {
				log.info("No column '" + BLOB_DATA_DB_NAME + "' in table '" + sourceTable
					+ "', no binary values to move.");
				return;
			}

			int moved = moveRows(context, connection, sourceTable, binaryTable, content);
			log.info("Moved " + moved + " binary values from table '" + sourceTable + "' to table '"
				+ binaryTable.getDBMapping().getDBName() + "'.");

			BinaryContentMigration.fillMissingMetadata(log, connection, binaryTable, content, null);

			int deleted = query(
				delete(table(sourceTable),
					eqSQL(column(AbstractFlexDataManager.DATA_TYPE_DBNAME), literal(DBType.INT, BLOB_TYPE))))
						.toSql(connection.getSQLDialect()).executeUpdate(connection);
			log.info("Deleted " + deleted + " binary values from table '" + sourceTable + "'.");

			new SQLProcessor(connection).execute(dropColumn(table(sourceTable), BLOB_DATA_DB_NAME));
			log.info("Dropped column '" + BLOB_DATA_DB_NAME + "' of table '" + sourceTable + "'.");

			if (config.isUpload()) {
				BinaryPlacement placement =
					new BinaryPlacement(BinaryAttributeKind.HYBRID, config.getStore(), config.getThreshold());
				BinaryContentMigration.moveContent(log, connection, binaryTable, content, content, placement,
					isNull(column(content.getKeyColumn().getDBName())));
			}
		} catch (SQLException | IOException | RuntimeException ex) {
			log.error("Failed to move binary values from type '" + sourceTypeName + "' to type '"
				+ config.getTargetType() + "': " + ex.getMessage(), ex);
		}
	}

	private int moveRows(MigrationContext context, PooledConnection connection, String sourceTable,
			MOStructure binaryTable, AbstractBinaryAttribute content) throws SQLException {
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
					table(sourceTable),
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

	private static boolean hasBlobColumn(PooledConnection connection, String tableName) throws SQLException {
		DBTable flexTable = DBSchemaUtils.extractTable(connection.getPool(), DBSchemaFactory.createDBSchema(),
			tableName);
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
	 * Creates the table of dynamic binary values {@link AbstractFlexDataManager#FLEX_BINARY_DATA}, if it
	 * does not exist yet.
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
		ensureTable(log, connection, binaryTable);
		return binaryTable;
	}

	/**
	 * Creates the table of the given type, if it does not exist yet.
	 */
	private static void ensureTable(Log log, PooledConnection connection, MOStructure binaryTable)
			throws SQLException {
		if (!exists(connection, binaryTable)) {
			DBSchema schema = DBSchemaFactory.createDBSchema();
			// Allocate the table in the current schema.
			schema.setName(null);
			DBTable table = SchemaSetup.createTable((MOClass) binaryTable);
			schema.getTables().add(table);
			DBSchemaUtils.create(connection, table);
			log.info("Created table '" + binaryTable.getDBMapping().getDBName() + "'.");
		}
	}

}
