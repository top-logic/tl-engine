/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.element.model.migration.model.refactor.MakeFlexAttributeProcessor.TypesConfig;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager.FlexDataValue;
import com.top_logic.knowledge.service.db2.ItemQuery.DirectItemResult;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} moving attribute values from the attribute table to the row table.
 * 
 * @see MakeFlexAttributeProcessor
 */
public class MakeColumnAttributeProcessor extends AbstractConfiguredInstance<MakeColumnAttributeProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link MakeColumnAttributeProcessor}.
	 */
	@TagName("make-column-attribute")
	public interface Config<I extends MakeColumnAttributeProcessor> extends PolymorphicConfiguration<I>, TypesConfig {
		/**
		 * Name of the table to store column values to.
		 */
		@Name("table")
		@Mandatory
		String getTable();

		/**
		 * Name of the column to store attribute values to.
		 * 
		 * <p>
		 * If not given, the same name as the {@link #getAttribute()} is used.
		 * </p>
		 */
		@Name("column")
		@Nullable
		String getColumn();

		/**
		 * Name of the flex attribute to take values from.
		 */
		@Name("attribute")
		@Mandatory
		String getAttribute();

		/**
		 * Whether data in flex table must not be deleted.
		 */
		boolean isDoNotDeleteDataFromFlexTable();

		/**
		 * Converter to convert values before they are stored to the column.
		 */
		PolymorphicConfiguration<ValueConverter> getValueConverter();

	}

	/**
	 * Converter that converts the given value to store to given column.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ValueConverter {

		/**
		 * Converts the given value.
		 * 
		 * @param log
		 *        Log to write messages to.
		 * @param sqlDialect
		 *        SQL dialect of the database.
		 * @param column
		 *        the column to which the object will be stored.
		 * @param value
		 *        The value to store.
		 * @return Converted value.
		 */
		Object convert(Log log, DBHelper sqlDialect, DBAttribute column, Object value) throws SQLException;
	}

	private ValueConverter _valueConverter;

	/**
	 * Creates a {@link MakeColumnAttributeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MakeColumnAttributeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
		_valueConverter = context.getInstance(config.getValueConverter());
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config<?> config = getConfig();
		String tableName = config.getTable();

		String typeNames =
			config.getTypes().stream().map(t -> "'" + t.getName() + "'").collect(Collectors.joining(", "));
		String columnName = config.getColumn();
		String attributeName = config.getAttribute();
		if (columnName == null) {
			columnName = attributeName;
		}

		log.info("Moving values of type " + typeNames + " from flex attribute '" + attributeName + "' to column '"
				+ columnName + "' in table '" + tableName + "'.");

		MORepository repository = context.getPersistentRepository();
		MOStructure table = (MOStructure) repository.getMetaObject(tableName);
		MOStructure flexTable = (MOStructure) repository.getMetaObject(AbstractFlexDataManager.FLEX_DATA);
		MOReference typeRef = (MOReference) table.getAttribute(PersistentObject.TYPE_REF);

		MOAttribute column = table.getAttribute(columnName);
		if (column == null) {
			log.info("Column '" + columnName + "' of table '" + tableName + "' does not exist, ignoring.", Log.WARN);
			return;
		}
		if (column.getDbMapping().length != 1) {
			log.error("Trying to move flex values to column '" + columnName + "' of table '" + tableName
					+ "' with non-trivial DB mapping of size " + column.getDbMapping().length + ".");
			return;
		}

		DBAttribute dbColumn = column.getDbMapping()[0];

		try {
			Set<TLID> changedTypes = MakeFlexAttributeProcessor.resolveObjectTypeIds(context, connection, config);

			log.info("Moving column values of objects with concrete type IDs: " + changedTypes);

			boolean hasBranch = context.hasBranchSupport();
			List<AttributeValue> flexValues = getFlexValues(connection, table, attributeName, flexTable, typeRef,
				changedTypes, hasBranch);

			List<AttributeValue> rowValues =
				getRowValuesToUpdate(log, connection, table, flexTable, dbColumn, hasBranch,
				flexValues);

			updateTableRows(context, log, connection, table, dbColumn, rowValues);

			if (!getConfig().isDoNotDeleteDataFromFlexTable()) {
				deleteFlexValues(context, log, connection, attributeName, flexTable, flexValues);
			}
		} catch (SQLException | MigrationException ex) {
			log.error(
				"Failed to move values for attribute '" + attributeName + "' from flex data to column '" + columnName
						+ "' in '" + tableName + "': " + ex.getMessage(),
				ex);
		}
	}

	private void deleteFlexValues(MigrationContext context, Log log, PooledConnection connection, String attributeName,
			MOStructure flexTable, List<AttributeValue> flexValues) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		Util sqlUtil = context.getSQLUtils();

		CompiledStatement deleteStmt = query(Arrays.asList(
			sqlUtil.branchParamDef(),
			parameterDef(DBType.ID, "id"),
			parameterDef(DBType.LONG, "revMin")),
			delete(table(flexTable.getDBMapping().getDBName()),
				and(
					sqlUtil.eqBranch(),
					eq(column(BasicTypes.IDENTIFIER_DB_NAME), parameter(DBType.ID, "id")),
					eq(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME), literalString(attributeName)),
					eq(column(BasicTypes.REV_MIN_DB_NAME), parameter(DBType.LONG, "revMin")))))
						.toSql(sqlDialect);
		int batchSize = 0;
		int maxBatchSize = sqlDialect.getMaxBatchSize(3);
		int totalUpdates = 0;
		try (Batch batch = deleteStmt.createBatch(connection)) {
			for (AttributeValue flexValue : flexValues) {
				batch.addBatch(flexValue._branch, flexValue._id, flexValue._revMin);
				if (++batchSize >= maxBatchSize) {
					batch.executeBatch();
					totalUpdates += batchSize;
					batchSize = 0;
				}
			}
			if (batchSize > 0) {
				batch.executeBatch();
				totalUpdates += batchSize;
			}
		}
		log.info("Deleted " + totalUpdates + " rows in flex table for attribute '" + attributeName + "'.");
	}

	private void updateTableRows(MigrationContext context, Log log, PooledConnection connection, MOStructure table,
			DBAttribute dbColumn, List<AttributeValue> rowValues) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		Util sqlUtil = context.getSQLUtils();

		CompiledStatement updateStmt = query(Arrays.asList(
			sqlUtil.branchParamDef(),
			parameterDef(DBType.ID, "id"),
			parameterDef(DBType.LONG, "revMin"),
			parameterDef(dbColumn.getSQLType(), "value")),
			update(table(table.getDBMapping().getDBName()),
				and(
					sqlUtil.eqBranch(),
					eq(column(BasicTypes.IDENTIFIER_DB_NAME), parameter(DBType.ID, "id")),
					eq(column(BasicTypes.REV_MIN_DB_NAME), parameter(DBType.LONG, "revMin"))),
				Collections.singletonList(dbColumn.getDBName()),
				Collections.singletonList(parameter(dbColumn.getSQLType(), "value")))).toSql(sqlDialect);
		int batchSize = 0;
		int maxBatchSize = sqlDialect.getMaxBatchSize(4);
		int totalUpdates = 0;
		try (Batch batch = updateStmt.createBatch(connection)) {
			for (AttributeValue rowValue : rowValues) {
				Object value = rowValue._value;
				if (_valueConverter != null) {
					value = _valueConverter.convert(log, sqlDialect, dbColumn, value);
				}
				batch.addBatch(rowValue._branch, rowValue._id, rowValue._revMin, value);
				if (++batchSize >= maxBatchSize) {
					batch.executeBatch();
					totalUpdates += batchSize;
					batchSize = 0;
				}
			}
			if (batchSize > 0) {
				batch.executeBatch();
				totalUpdates += batchSize;
			}

		}
		log.info("Updated " + totalUpdates + " rows in table " + table.getName() + ".");
	}

	private List<AttributeValue> getRowValuesToUpdate(Log log, PooledConnection connection, MOStructure table,
			MOStructure flexTable,DBAttribute dbColumn, boolean hasBranch, List<AttributeValue> flexValues)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		List<AttributeValue> rowValues = new ArrayList<>();
		List<SQLColumnDefinition> tableColumns = new ArrayList<>();
		if (hasBranch) {
			tableColumns.add(columnDef(BasicTypes.BRANCH_DB_NAME));
		}
		Collections.addAll(tableColumns,
			columnDef(BasicTypes.IDENTIFIER_DB_NAME),
			columnDef(BasicTypes.REV_MIN_DB_NAME),
			columnDef(BasicTypes.REV_MAX_DB_NAME),
			columnDef(dbColumn.getDBName()));

		List<SQLOrder> tableOrders = new ArrayList<>();
		if (hasBranch) {
			tableOrders.add(order(column(BasicTypes.BRANCH_DB_NAME)));
		}
		tableOrders.add(order(column(BasicTypes.IDENTIFIER_DB_NAME)));
		tableOrders.add(order(column(BasicTypes.REV_MIN_DB_NAME)));

		Iterator<List<TLID>> chunks = CollectionUtil.chunk(sqlDialect.getMaxSetSize(),
			flexValues.stream().map(fv -> fv._id).distinct().iterator());
		int valuesIDX = 0;
		while (chunks.hasNext()) {
			List<TLID> chunk = chunks.next();

			CompiledStatement updateTable = query(
				select(
					tableColumns,
					table(table.getDBMapping().getDBName()),
					inSet(column(BasicTypes.IDENTIFIER_DB_NAME), chunk, DBType.ID),
					tableOrders)
						.forUpdate())
							.toSql(sqlDialect);
			updateTable.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			try (ResultSet result = updateTable.executeQuery(connection)) {
				boolean foundRow = false;
				while (result.next() && valuesIDX < flexValues.size()) {
					int colIdx = 1;
					long branch;
					if (hasBranch) {
						branch = result.getLong(colIdx++);
					} else {
						branch = TLContext.TRUNK_ID;
					}
					TLID objectId = IdentifierUtil.getId(result, colIdx++);
					long revMin = result.getLong(colIdx++);
					long revMax = result.getLong(colIdx++);

					while (valuesIDX < flexValues.size()) {
						AttributeValue flexValue = flexValues.get(valuesIDX);

						if (branch == flexValue._branch) {
							if (flexValue._id.equals(objectId)) {
								if (flexValue._revMin == revMin) {
									foundRow = true;
									rowValues.add(
										new AttributeValue(objectId, branch, revMin, revMax, flexValue._value));
									if (flexValue._revMax == revMax) {
										// row lifetime == flex lifetime
										foundRow = false;
										valuesIDX++;
									} else if (flexValue._revMax > revMax) {
										// flex lifetime longer than row lifetime => OK
									} else {
										// row lifetime longer than flex lifetime => Flex value
										// changed but no entry in row. Unexpected!!
										log.error("Lifetime of flex value " + flexValue.toObjectKey(flexTable)
												+ " ended at '" + flexValue._revMax
												+ "' but there is no corresponding object row.");
									}
								} else if (flexValue._revMin > revMin) {
									// Flex value not yet alive, value was set later => OK
								} else {
									// Different value changed in lifetime of flex data => OK
									if (foundRow) {
										rowValues
											.add(new AttributeValue(objectId, branch, revMin, revMax,
												flexValue._value));
										if (flexValue._revMax == revMax) {
											// row lifetime ends with flex lifetime
											foundRow = false;
											valuesIDX++;
										} else if (flexValue._revMax > revMax) {
											// flex lifetime longer than row lifetime => OK
										} else {
											// row lifetime longer than flex lifetime => Flex value
											// changed but no entry in row. Unexpected!!
											log.error("Lifetime of flex value " + flexValue.toObjectKey(flexTable)
													+ " ended at '" + flexValue._revMax
													+ "' but there is no corresponding object row.");
										}
									} else {
										// No entry for object at time when flex value was set.
										// Unexpected.
										log.error("No object row found for " + flexValue.toObjectKey(flexTable)
												+ ".");
									}
								}
							} else if (flexValue._id.compareTo(objectId) < 0) {
								// No row found for flex value entry. Unexpected. Ignore and try
								// next.
								valuesIDX++;
								log.error("No object row found for " + flexValue.toObjectKey(flexTable)
										+ ". Ignoring entry.");
								continue;
							} else {
								// Row for object without flex value. Ignore and try next.
								valuesIDX++;
								continue;
							}
						} else if (flexValue._branch < branch) {
							// No row found for flex value entry. Unexpected. Ignore and try
							// next.
							valuesIDX++;
							log.error(
								"No object row found for " + flexValue.toObjectKey(flexTable) + ". Ignoring entry.");
							continue;
						} else {
							// Row for object without flex value. Ignore and try next.
							valuesIDX++;
							continue;
						}

						break;
					}
				}
			}
		}

		if (valuesIDX < flexValues.size()) {
			// No row object found for flex data. Unexpected.
			log.error("No object rows found for flex entries: " + flexValues.subList(valuesIDX, flexValues.size()));
		}
		return rowValues;
	}

	private List<AttributeValue> getFlexValues(PooledConnection connection, MOStructure table, String attributeName,
			MOStructure flexTable, MOReference typeRef, Set<TLID> changedTypes, boolean hasBranch)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		List<SQLColumnDefinition> flexColumns = new ArrayList<>();
		if (hasBranch) {
			flexColumns.add(columnDef(AbstractFlexDataManager.BRANCH_DBNAME));
		}
		Collections.addAll(flexColumns,
			columnDef(AbstractFlexDataManager.IDENTIFIER_DBNAME),
			columnDef(BasicTypes.REV_MAX_DB_NAME),
			columnDef(BasicTypes.REV_MIN_DB_NAME),
			columnDef(AbstractFlexDataManager.DATA_TYPE_DBNAME),
			columnDef(AbstractFlexDataManager.LONG_DATA_DBNAME),
			columnDef(AbstractFlexDataManager.DOUBLE_DATA_DBNAME),
			columnDef(AbstractFlexDataManager.VARCHAR_DATA_DBNAME),
			columnDef(AbstractFlexDataManager.CLOB_DATA_DBNAME),
			columnDef(AbstractFlexDataManager.BLOB_DATA_DBNAME));

		List<SQLOrder> orders = new ArrayList<>();
		if (hasBranch) {
			orders.add(order(column(AbstractFlexDataManager.BRANCH_DBNAME)));
		}
		orders.add(order(column(AbstractFlexDataManager.IDENTIFIER_DBNAME)));
		orders.add(order(column(BasicTypes.REV_MIN_DB_NAME)));

		List<AttributeValue> flexValues = new ArrayList<>();
		CompiledStatement selectValues = query(
			select(
				flexColumns,
				table(flexTable.getDBMapping().getDBName()),
				and(
					eq(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME), literalString(attributeName)),
					inSetSelect(
						column(AbstractFlexDataManager.IDENTIFIER_DBNAME),
						selectDistinct(
							columns(columnDef(BasicTypes.IDENTIFIER_DB_NAME)),
							table(table.getDBMapping().getDBName()),
							inSet(
								column(typeRef.getColumn(ReferencePart.name).getDBName()),
								setLiteral(changedTypes, DBType.ID))))),
				orders)).toSql(sqlDialect);

		try (ResultSet flexValueData = selectValues.executeQuery(connection)) {
			FlexDataValue valueAccess = new AbstractFlexDataManager.FlexDataValue() {

				int _dataIDX = hasBranch ? 5 : 4;

				@Override
				public byte getDataType() throws SQLException {
					return flexValueData.getByte(_dataIDX);
				}

				@Override
				public long getLongData() throws SQLException {
					return flexValueData.getLong(_dataIDX + 1);
				}

				@Override
				public double getDoubleData() throws SQLException {
					return flexValueData.getDouble(_dataIDX + 2);
				}

				@Override
				public String getVarcharData() throws SQLException {
					return flexValueData.getString(_dataIDX + 3);
				}

				@Override
				public String getClobData() throws SQLException {
					return DirectItemResult.getClobStringValue(sqlDialect, flexValueData, _dataIDX + 4);
				}

				@Override
				public BinaryData getBlobData() throws SQLException {
					int contentTypeIndex = _dataIDX + 3;
					int sizeIndex = _dataIDX + 1;
					int blobIndex = _dataIDX + 5;
					return DBBinaryData.fromBlobColumn(sqlDialect, flexValueData, getClobData(), contentTypeIndex,
						sizeIndex,
						blobIndex);
				}

			};
			while (flexValueData.next()) {
				int colIdx = 1;
				long branch;
				if (hasBranch) {
					branch = flexValueData.getLong(colIdx++);
				} else {
					branch = TLContext.TRUNK_ID;
				}
				TLID objectID = IdentifierUtil.getId(flexValueData, colIdx++);
				long revMax = flexValueData.getLong(colIdx++);
				long revMin = flexValueData.getLong(colIdx++);
				Object value = AbstractFlexDataManager.fetchValue(valueAccess);
				flexValues.add(new AttributeValue(objectID, branch, revMin, revMax, value));
			}
		}
		return flexValues;
	}

	private static class AttributeValue {

		final TLID _id;

		final long _branch;

		final long _revMin;

		final long _revMax;

		final Object _value;

		AttributeValue(TLID id, long branch, long revMin, long revMax, Object value) {
			_id = id;
			_branch = branch;
			_revMin = revMin;
			_revMax = revMax;
			_value = value;
		}

		ObjectKey toObjectKey(MetaObject type) {
			return new DefaultObjectKey(_branch, _revMin, type, _id);
		}

		@Override
		public String toString() {
			return "AttributeValue [_id=" + _id + "@" + _branch + "[" + _revMin + "," + _revMax + "], _value=" + _value
					+ "]";
		}

	}
}
