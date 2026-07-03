/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.DBBinaryData;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.element.meta.kbbased.storage.HistoricStorage;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager.FlexDataValue;
import com.top_logic.knowledge.service.db2.ItemQuery.DirectItemResult;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} moving the values of a historic {@link MOReference} that were stored by
 * a {@link com.top_logic.element.meta.kbbased.storage.HistoricStorage} in the flex-data table to the
 * reference's aspect columns.
 *
 * <p>
 * A {@code HistoricStorage} keeps a historic reference in four flex attributes
 * ({@code <base>_BRC}, {@code <base>_REV}, {@code <base>_ID}, {@code <base>_TYPE}). This processor
 * moves these values into the {@link ReferencePart} columns of the configured {@link MOReference},
 * so that the reference is afterwards accessed as if it had been stored by a
 * {@link com.top_logic.element.meta.kbbased.storage.ForeignKeyStorage}.
 * </p>
 *
 * <p>
 * The processor does <b>not</b> create any database column. The caller is responsible for ensuring
 * that the target {@link MOReference} and its aspect columns exist (e.g. by a preceding schema
 * migration).
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MoveHistoricReferenceToColumn
		extends AbstractConfiguredInstance<MoveHistoricReferenceToColumn.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link MoveHistoricReferenceToColumn}.
	 */
	@TagName("move-historic-reference-to-column")
	public interface Config<I extends MoveHistoricReferenceToColumn> extends PolymorphicConfiguration<I> {

		/**
		 * Name of the table ({@link MOStructure}) defining the target {@link #getReference()
		 * reference}.
		 */
		@Name("table")
		@Mandatory
		String getTable();

		/**
		 * Name of the target {@link MOReference} in {@link #getTable()} whose aspect columns are
		 * filled with the migrated values.
		 */
		@Name("reference")
		@Mandatory
		String getReference();

		/**
		 * Base name of the source flex attributes ({@code <source-attribute>_BRC},
		 * {@code <source-attribute>_REV}, {@code <source-attribute>_ID},
		 * {@code <source-attribute>_TYPE}) holding the values written by the
		 * {@code HistoricStorage}.
		 */
		@Name("source-attribute")
		@Mandatory
		String getSourceAttribute();

		/**
		 * Whether the migrated entries must not be deleted from the flex-data table.
		 */
		@Name("keep-flex-data")
		boolean isKeepFlexData();

	}

	/**
	 * Creates a {@link MoveHistoricReferenceToColumn} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MoveHistoricReferenceToColumn(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			migrate(context, log, connection);
		} catch (SQLException ex) {
			log.error("Failed to move historic reference '" + getConfig().getReference() + "' of table '"
				+ getConfig().getTable() + "' to columns.", ex);
		}
	}

	private void migrate(MigrationContext context, Log log, PooledConnection connection) throws SQLException {
		Config<?> config = getConfig();
		String tableName = config.getTable();
		String referenceName = config.getReference();
		String sourceAttribute = config.getSourceAttribute();

		MORepository repository = context.getPersistentRepository();
		MetaObject type = repository.getTypeOrNull(tableName);
		if (!(type instanceof MOStructure table)) {
			log.error("Table '" + tableName + "' does not exist or has no attributes.");
			return;
		}
		MOAttribute attribute = table.getAttributeOrNull(referenceName);
		if (attribute == null) {
			log.error("Table '" + tableName + "' does not define attribute '" + referenceName + "'.");
			return;
		}
		if (!(attribute instanceof MOReference reference)) {
			log.error("Attribute '" + referenceName + "' of table '" + tableName + "' is not a reference.");
			return;
		}
		if (reference.getHistoryType() != HistoryType.HISTORIC) {
			log.error("Reference '" + referenceName + "' of table '" + tableName
				+ "' is not historic, but '" + reference.getHistoryType() + "'.");
			return;
		}

		DBAttribute revisionColumn = reference.getColumn(ReferencePart.revision);
		DBAttribute idColumn = reference.getColumn(ReferencePart.name);
		// The branch column only exists for a branch-global reference; the type column only exists
		// for a polymorphic reference. When a column is absent, the corresponding value cannot be
		// stored and must match the implicit value (see consistency checks below).
		DBAttribute branchColumn = reference.getColumn(ReferencePart.branch);
		DBAttribute typeColumn = reference.getColumn(ReferencePart.type);

		boolean hasBranch = context.hasBranchSupport();

		List<ReferenceValue> values = readReferenceValues(connection, table, tableName, sourceAttribute, hasBranch);
		if (values.isEmpty()) {
			log.info("No values to move for reference '" + referenceName + "' of table '" + tableName + "'.");
			return;
		}

		List<ReferenceValue> incomplete = new ArrayList<>();
		for (ReferenceValue value : values) {
			if (!value.isComplete()) {
				incomplete.add(value);
			}
		}
		if (!incomplete.isEmpty()) {
			log.error("Cannot move values of reference '" + referenceName + "' of table '" + tableName
				+ "': found " + incomplete.size()
				+ " incomplete stored value(s) missing a branch, revision, id or type part: " + incomplete);
			return;
		}

		if (typeColumn == null) {
			// Monomorphic reference: no type column, all values must have the reference's target
			// type.
			String targetType = reference.getMetaObject().getName();
			List<ReferenceValue> foreignTypeValues = new ArrayList<>();
			for (ReferenceValue value : values) {
				if (value._refType != null && !targetType.equals(value._refType)) {
					foreignTypeValues.add(value);
				}
			}
			if (!foreignTypeValues.isEmpty()) {
				log.error("Cannot move values of monomorphic reference '" + referenceName + "' of table '" + tableName
					+ "' with target type '" + targetType + "': found " + foreignTypeValues.size()
					+ " stored value(s) of a foreign type that cannot be represented without a type column: "
					+ foreignTypeValues);
				return;
			}
		}

		if (branchColumn == null) {
			// Reference is not branch-global: no branch column, the target must live on the same
			// branch as the owning object.
			List<ReferenceValue> foreignBranchValues = new ArrayList<>();
			for (ReferenceValue value : values) {
				if (value._refBranch != null && value._refBranch.longValue() != value._branch) {
					foreignBranchValues.add(value);
				}
			}
			if (!foreignBranchValues.isEmpty()) {
				log.error("Cannot move values of non-branch-global reference '" + referenceName + "' of table '"
					+ tableName + "': found " + foreignBranchValues.size()
					+ " stored target(s) on a branch different from the owning object's branch"
					+ " that cannot be represented without a branch column: " + foreignBranchValues);
				return;
			}
		}

		updateColumns(context, log, connection, table, branchColumn, revisionColumn, idColumn, typeColumn, values);

		if (!config.isKeepFlexData()) {
			deleteFlexValues(log, connection, tableName, sourceAttribute);
		}
	}

	/**
	 * Reconstructs the historic reference value for every object version of the given table.
	 *
	 * <p>
	 * The four aspects of a historic reference are stored as separate flex attributes. When the
	 * reference value changes, only the aspects that actually change get a new flex-table row; an
	 * unchanged aspect keeps its existing row (with an earlier {@code revMin}). Therefore the four
	 * aspects cannot be grouped by a common revision. Instead, each aspect is read into its own
	 * per-object timeline, and the value valid for a given object version is looked up in each
	 * timeline separately.
	 * </p>
	 */
	private List<ReferenceValue> readReferenceValues(PooledConnection connection, MOStructure table, String tableName,
			String sourceAttribute, boolean hasBranch) throws SQLException {
		String branchAttr = sourceAttribute + HistoricStorage.KO_ATT_SUFFIX_BRANCH;
		String revisionAttr = sourceAttribute + HistoricStorage.KO_ATT_SUFFIX_REVISION;
		String idAttr = sourceAttribute + HistoricStorage.KO_ATT_SUFFIX_ID;
		String typeAttr = sourceAttribute + HistoricStorage.KO_ATT_SUFFIX_TYPE;

		Map<BranchId, TreeMap<Long, AspectEntry>> branchTimeline = new HashMap<>();
		Map<BranchId, TreeMap<Long, AspectEntry>> revisionTimeline = new HashMap<>();
		Map<BranchId, TreeMap<Long, AspectEntry>> idTimeline = new HashMap<>();
		Map<BranchId, TreeMap<Long, AspectEntry>> typeTimeline = new HashMap<>();

		readAspectTimelines(connection, tableName, hasBranch, branchAttr, revisionAttr, idAttr, typeAttr,
			branchTimeline, revisionTimeline, idTimeline, typeTimeline);

		Set<BranchId> keys = new HashSet<>();
		keys.addAll(branchTimeline.keySet());
		keys.addAll(revisionTimeline.keySet());
		keys.addAll(idTimeline.keySet());
		keys.addAll(typeTimeline.keySet());
		if (keys.isEmpty()) {
			return Collections.emptyList();
		}
		Set<TLID> ids = new HashSet<>();
		for (BranchId key : keys) {
			ids.add(key.id());
		}

		DBHelper sqlDialect = connection.getSQLDialect();
		List<ReferenceValue> result = new ArrayList<>();
		Iterator<List<TLID>> chunks = CollectionUtil.chunk(sqlDialect.getMaxSetSize(), ids.iterator());
		while (chunks.hasNext()) {
			List<TLID> chunk = chunks.next();

			List<SQLColumnDefinition> columns = new ArrayList<>();
			if (hasBranch) {
				columns.add(columnDef(BasicTypes.BRANCH_DB_NAME));
			}
			Collections.addAll(columns,
				columnDef(BasicTypes.IDENTIFIER_DB_NAME),
				columnDef(BasicTypes.REV_MIN_DB_NAME),
				columnDef(BasicTypes.REV_MAX_DB_NAME));

			CompiledStatement select = query(
				select(
					columns,
					table(table.getDBMapping().getDBName()),
					inSet(column(BasicTypes.IDENTIFIER_DB_NAME), chunk, DBType.ID))).toSql(sqlDialect);

			try (ResultSet rows = select.executeQuery(connection)) {
				while (rows.next()) {
					int colIdx = 1;
					long branch = hasBranch ? rows.getLong(colIdx++) : TLContext.TRUNK_ID;
					TLID id = IdentifierUtil.getId(rows, colIdx++);
					long revMin = rows.getLong(colIdx++);
					long revMax = rows.getLong(colIdx++);

					BranchId key = new BranchId(branch, id);
					Long refBranch = (Long) resolve(branchTimeline, key, revMin);
					Long refRevision = (Long) resolve(revisionTimeline, key, revMin);
					TLID refId = (TLID) resolve(idTimeline, key, revMin);
					String refType = (String) resolve(typeTimeline, key, revMin);

					if (refBranch == null && refRevision == null && refId == null && refType == null) {
						// The reference is null during this object version, nothing to store.
						continue;
					}

					ReferenceValue value = new ReferenceValue(branch, id, revMin, revMax);
					value._refBranch = refBranch;
					value._refRevision = refRevision;
					value._refId = refId;
					value._refType = refType;
					result.add(value);
				}
			}
		}
		return result;
	}

	/**
	 * Reads the flex-table rows of the four reference aspects into per-object timelines (mapping the
	 * {@code revMin} of a value to the value that is valid from that revision on).
	 */
	private void readAspectTimelines(PooledConnection connection, String tableName, boolean hasBranch,
			String branchAttr, String revisionAttr, String idAttr, String typeAttr,
			Map<BranchId, TreeMap<Long, AspectEntry>> branchTimeline,
			Map<BranchId, TreeMap<Long, AspectEntry>> revisionTimeline,
			Map<BranchId, TreeMap<Long, AspectEntry>> idTimeline,
			Map<BranchId, TreeMap<Long, AspectEntry>> typeTimeline) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();

		List<SQLColumnDefinition> columns = new ArrayList<>();
		if (hasBranch) {
			columns.add(columnDef(AbstractFlexDataManager.BRANCH_DBNAME));
		}
		Collections.addAll(columns,
			columnDef(AbstractFlexDataManager.IDENTIFIER_DBNAME),
			columnDef(BasicTypes.REV_MAX_DB_NAME),
			columnDef(BasicTypes.REV_MIN_DB_NAME),
			columnDef(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
			columnDef(AbstractFlexDataManager.DATA_TYPE_DBNAME),
			columnDef(AbstractFlexDataManager.LONG_DATA_DBNAME),
			columnDef(AbstractFlexDataManager.DOUBLE_DATA_DBNAME),
			columnDef(AbstractFlexDataManager.VARCHAR_DATA_DBNAME),
			columnDef(AbstractFlexDataManager.CLOB_DATA_DBNAME),
			columnDef(AbstractFlexDataManager.BLOB_DATA_DBNAME));

		CompiledStatement select = query(
			select(
				columns,
				table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
				and(
					eq(column(AbstractFlexDataManager.TYPE_DBNAME), literalString(tableName)),
					inSet(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
						Arrays.asList(branchAttr, revisionAttr, idAttr, typeAttr), DBType.STRING)))).toSql(sqlDialect);

		int attrIdx = hasBranch ? 5 : 4;
		int dataTypeIdx = attrIdx + 1;

		try (ResultSet rows = select.executeQuery(connection)) {
			FlexDataValue valueAccess = new FlexDataValue() {

				@Override
				public byte getDataType() throws SQLException {
					return rows.getByte(dataTypeIdx);
				}

				@Override
				public long getLongData() throws SQLException {
					return rows.getLong(dataTypeIdx + 1);
				}

				@Override
				public double getDoubleData() throws SQLException {
					return rows.getDouble(dataTypeIdx + 2);
				}

				@Override
				public String getVarcharData() throws SQLException {
					return rows.getString(dataTypeIdx + 3);
				}

				@Override
				public String getClobData() throws SQLException {
					return DirectItemResult.getClobStringValue(sqlDialect, rows, dataTypeIdx + 4);
				}

				@Override
				public BinaryData getBlobData() throws SQLException {
					return DBBinaryData.fromBlobColumn(sqlDialect, rows, getClobData(), dataTypeIdx + 3,
						dataTypeIdx + 1, dataTypeIdx + 5);
				}
			};

			while (rows.next()) {
				int colIdx = 1;
				long branch = hasBranch ? rows.getLong(colIdx++) : TLContext.TRUNK_ID;
				TLID id = IdentifierUtil.getId(rows, colIdx++);
				long revMax = rows.getLong(colIdx++);
				long revMin = rows.getLong(colIdx++);
				String attr = rows.getString(colIdx++);
				Object value = AbstractFlexDataManager.fetchValue(valueAccess);

				Map<BranchId, TreeMap<Long, AspectEntry>> timeline;
				if (attr.equals(branchAttr)) {
					timeline = branchTimeline;
				} else if (attr.equals(revisionAttr)) {
					timeline = revisionTimeline;
				} else if (attr.equals(idAttr)) {
					timeline = idTimeline;
				} else if (attr.equals(typeAttr)) {
					timeline = typeTimeline;
				} else {
					continue;
				}
				timeline.computeIfAbsent(new BranchId(branch, id), k -> new TreeMap<>())
					.put(revMin, new AspectEntry(revMax, value));
			}
		}
	}

	/**
	 * The aspect value valid at the given revision, or <code>null</code> if the aspect has no value
	 * at that revision.
	 */
	private static Object resolve(Map<BranchId, TreeMap<Long, AspectEntry>> timeline, BranchId key, long revision) {
		TreeMap<Long, AspectEntry> entries = timeline.get(key);
		if (entries == null) {
			return null;
		}
		Map.Entry<Long, AspectEntry> floor = entries.floorEntry(revision);
		if (floor == null) {
			return null;
		}
		AspectEntry entry = floor.getValue();
		return entry.revMax() >= revision ? entry.value() : null;
	}

	private void updateColumns(MigrationContext context, Log log, PooledConnection connection, MOStructure table,
			DBAttribute branchColumn, DBAttribute revisionColumn, DBAttribute idColumn, DBAttribute typeColumn,
			List<ReferenceValue> values) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		Util sqlUtil = context.getSQLUtils();

		List<Parameter> parameters = new ArrayList<>();
		parameters.add(sqlUtil.branchParamDef());
		parameters.add(parameterDef(DBType.ID, "ownerId"));
		parameters.add(parameterDef(DBType.LONG, "ownerRevMin"));

		List<String> updateColumns = new ArrayList<>();
		List<SQLExpression> updateValues = new ArrayList<>();
		if (branchColumn != null) {
			parameters.add(parameterDef(branchColumn.getSQLType(), "branchValue"));
			updateColumns.add(branchColumn.getDBName());
			updateValues.add(parameter(branchColumn.getSQLType(), "branchValue"));
		}
		parameters.add(parameterDef(revisionColumn.getSQLType(), "revisionValue"));
		updateColumns.add(revisionColumn.getDBName());
		updateValues.add(parameter(revisionColumn.getSQLType(), "revisionValue"));
		parameters.add(parameterDef(idColumn.getSQLType(), "idValue"));
		updateColumns.add(idColumn.getDBName());
		updateValues.add(parameter(idColumn.getSQLType(), "idValue"));
		if (typeColumn != null) {
			parameters.add(parameterDef(typeColumn.getSQLType(), "typeValue"));
			updateColumns.add(typeColumn.getDBName());
			updateValues.add(parameter(typeColumn.getSQLType(), "typeValue"));
		}

		CompiledStatement update = query(
			parameters,
			update(
				table(table.getDBMapping().getDBName()),
				and(
					sqlUtil.eqBranch(),
					eq(column(BasicTypes.IDENTIFIER_DB_NAME), parameter(DBType.ID, "ownerId")),
					// Each reference value has been reconstructed for exactly one object row
					// version, which is identified by (branch, id, revMin).
					eq(column(BasicTypes.REV_MIN_DB_NAME), parameter(DBType.LONG, "ownerRevMin"))),
				updateColumns,
				updateValues)).toSql(sqlDialect);

		int batchSize = 0;
		int maxBatchSize = sqlDialect.getMaxBatchSize(parameters.size());
		int totalUpdates = 0;
		try (Batch batch = update.createBatch(connection)) {
			for (ReferenceValue value : values) {
				List<Object> arguments = new ArrayList<>();
				arguments.add(value._branch);
				arguments.add(value._id);
				arguments.add(value._revMin);
				if (branchColumn != null) {
					arguments.add(value._refBranch);
				}
				arguments.add(value._refRevision);
				arguments.add(value._refId);
				if (typeColumn != null) {
					arguments.add(value._refType);
				}
				batch.addBatch(arguments.toArray());
				if (++batchSize >= maxBatchSize) {
					totalUpdates += sum(batch.executeBatch());
					batchSize = 0;
				}
			}
			if (batchSize > 0) {
				totalUpdates += sum(batch.executeBatch());
			}
		}
		log.info("Moved " + values.size() + " historic reference value(s) into columns of table '" + table.getName()
			+ "', updated " + totalUpdates + " row version(s).");
	}

	private void deleteFlexValues(Log log, PooledConnection connection, String tableName, String sourceAttribute)
			throws SQLException {
		List<String> attributes = Arrays.asList(
			sourceAttribute + HistoricStorage.KO_ATT_SUFFIX_BRANCH,
			sourceAttribute + HistoricStorage.KO_ATT_SUFFIX_REVISION,
			sourceAttribute + HistoricStorage.KO_ATT_SUFFIX_ID,
			sourceAttribute + HistoricStorage.KO_ATT_SUFFIX_TYPE);

		CompiledStatement delete = query(
			delete(
				table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
				and(
					eq(column(AbstractFlexDataManager.TYPE_DBNAME), literalString(tableName)),
					inSet(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME), attributes, DBType.STRING))))
						.toSql(connection.getSQLDialect());
		int deleted = delete.executeUpdate(connection);
		log.info("Deleted " + deleted + " migrated flex entry/entries for attribute '" + sourceAttribute
			+ "' of table '" + tableName + "'.");
	}

	private static int sum(int[] updateCounts) {
		int result = 0;
		for (int count : updateCounts) {
			if (count > 0) {
				result += count;
			}
		}
		return result;
	}

	/**
	 * A historic reference value assembled from the four flex attributes of a single object version.
	 */
	private static class ReferenceValue {

		final long _branch;

		final TLID _id;

		final long _revMin;

		final long _revMax;

		Long _refBranch;

		Long _refRevision;

		TLID _refId;

		String _refType;

		ReferenceValue(long branch, TLID id, long revMin, long revMax) {
			_branch = branch;
			_id = id;
			_revMin = revMin;
			_revMax = revMax;
		}

		/**
		 * Whether all four aspects (branch, revision, id and type) of the historic reference were
		 * found in the flex-data table.
		 */
		boolean isComplete() {
			return _refBranch != null && _refRevision != null && _refId != null && _refType != null;
		}

		@Override
		public String toString() {
			return "ReferenceValue [owner=" + _id + "@" + _branch + "[" + _revMin + "," + _revMax + "]"
				+ ", branch=" + _refBranch + ", revision=" + _refRevision + ", id=" + _refId + ", type=" + _refType
				+ "]";
		}
	}

	/**
	 * Key identifying an object by its branch and id, used to group the aspect timelines.
	 */
	private record BranchId(long branch, TLID id) {
		// Value key with generated equals/hashCode.
	}

	/**
	 * A single flex-table value version of one reference aspect: the value and the last revision
	 * ({@code revMax}) up to which it is valid. The starting revision ({@code revMin}) is the key in
	 * the {@link TreeMap} timeline.
	 */
	private record AspectEntry(long revMax, Object value) {
		// Immutable value holder.
	}

}
