/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.BranchSupport;
import com.top_logic.knowledge.service.db2.DBTypeRepository;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} aligning the physical layout of the existing tables with the layout
 * options of the configured schema.
 *
 * <p>
 * The layout options of a {@link SchemaConfiguration} (see {@link #LAYOUT_OPTIONS}) determine how
 * the tables are built from the type definitions. The processor compares the layout options of the
 * schema stored in the database (see {@link MigrationContext#getPersistentSchema()}) with the ones
 * of the application configuration (see {@link MigrationContext#getApplicationSchema()}). If they
 * differ, the table definitions of all types of the stored schema are computed once with the
 * stored and once with the target schema (see
 * {@link #withConfiguredLayout(SchemaConfiguration, SchemaConfiguration)}), and the existing
 * tables are adapted to the target definitions, see
 * {@link DBSchemaUtils#alignTable(Log, PooledConnection, DBTable, DBTable, java.util.Collection, Map)}:
 * Branch columns missing in a table are added and filled with the trunk, branch columns not part
 * of the target definition are dropped, and the primary keys and indexes are converted.
 * Afterwards, the target schema is stored.
 * </p>
 *
 * <p>
 * Dropping branch columns requires that the database contains only the trunk: The
 * {@link BasicTypes#BRANCH_TYPE_NAME branch table} must not contain another branch, and no dropped
 * branch column may contain a value other than the trunk. Otherwise, the processor reports an error
 * without changing the database.
 * </p>
 *
 * <p>
 * Other differences between the stored and the configured schema (e.g. added types or changed
 * index declarations) are neither applied to the tables nor to the stored schema.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AlignTableLayoutProcessor extends AbstractConfiguredInstance<AlignTableLayoutProcessor.Config>
		implements MigrationProcessor {

	/**
	 * The properties of a {@link SchemaConfiguration} that determine the layout of the tables.
	 */
	public static final List<String> LAYOUT_OPTIONS =
		List.of(SchemaConfiguration.BRANCH_LAST_ATTRIBUTE, SchemaConfiguration.MULTIPLE_BRANCHES_ATTRIBUTE);

	/**
	 * Names of the types whose definitions depend on {@link SchemaConfiguration#hasMultipleBranches()}.
	 *
	 * <p>
	 * A single-branch schema declares <code>singleBranchMeta.xml</code>, which replaces the branch
	 * attribute of {@link BasicTypes#ITEM_TYPE_NAME} by a computed attribute and removes it from its
	 * primary key. Since the stored schema contains the resolved type definitions, the definitions of
	 * these types are taken from the configured schema, if
	 * {@link SchemaConfiguration#MULTIPLE_BRANCHES_ATTRIBUTE} changes.
	 * </p>
	 */
	public static final List<String> BRANCH_DEPENDENT_TYPES = List.of(BasicTypes.ITEM_TYPE_NAME);

	/**
	 * Configuration options of {@link AlignTableLayoutProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<AlignTableLayoutProcessor> {

		/**
		 * Tag name of {@link Config} in a migration script.
		 */
		String TAG_NAME = "align-table-layout";

	}

	/**
	 * Creates a {@link AlignTableLayoutProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AlignTableLayoutProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Whether the given schemas differ in one of their {@link #LAYOUT_OPTIONS}.
	 *
	 * @param stored
	 *        The schema the tables of the database were built with.
	 * @param configured
	 *        The schema of the application configuration.
	 */
	public static boolean hasLayoutChange(SchemaConfiguration stored, SchemaConfiguration configured) {
		for (String option : LAYOUT_OPTIONS) {
			if (!Objects.equals(value(stored, option), value(configured, option))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates the schema to convert the database to: a copy of the given stored schema with the
	 * {@link #LAYOUT_OPTIONS} of the given configured schema.
	 *
	 * <p>
	 * If {@link SchemaConfiguration#hasMultipleBranches()} differs, the definitions of the
	 * {@link #BRANCH_DEPENDENT_TYPES} are taken from the configured schema as well.
	 * </p>
	 *
	 * @param stored
	 *        The resolved schema the tables of the database were built with.
	 * @param configured
	 *        The resolved schema of the application configuration.
	 */
	public static SchemaConfiguration withConfiguredLayout(SchemaConfiguration stored,
			SchemaConfiguration configured) {
		SchemaConfiguration result = TypedConfiguration.copy(stored);
		for (String option : LAYOUT_OPTIONS) {
			PropertyDescriptor property = result.descriptor().getProperty(option);
			result.update(property, value(configured, option));
		}
		if (stored.hasMultipleBranches() != configured.hasMultipleBranches()) {
			Map<String, MetaObjectName> resultTypes = result.getMetaObjects().getTypes();
			Map<String, MetaObjectName> configuredTypes = configured.getMetaObjects().getTypes();
			for (String typeName : BRANCH_DEPENDENT_TYPES) {
				MetaObjectName configuredType = configuredTypes.get(typeName);
				if (configuredType == null) {
					resultTypes.remove(typeName);
				} else {
					resultTypes.put(typeName, TypedConfiguration.copy(configuredType));
				}
			}
		}
		return result;
	}

	private static Object value(SchemaConfiguration schema, String option) {
		return schema.value(schema.descriptor().getProperty(option));
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		SchemaConfiguration stored = context.getPersistentSchema();
		if (stored == null) {
			log.info("No stored schema, table layout not aligned.");
			return;
		}
		SchemaConfiguration configured = context.getApplicationSchema();
		if (!hasLayoutChange(stored, configured)) {
			return;
		}

		SchemaConfiguration target = withConfiguredLayout(stored, configured);
		log.info("Aligning the table layout with the configured layout options: " + describeLayout(target)
			+ " (stored: " + describeLayout(stored) + ").");
		try {
			boolean versioning = !context.getKnowledgeBaseConfiguration().getDisableVersioning();
			DBHelper sqlDialect = connection.getSQLDialect();
			MORepository currentTypes = createRepository(connection, stored, versioning);
			MORepository targetTypes = createRepository(connection, target, versioning);

			List<TableChange> changes = new ArrayList<>();
			DBSchema currentTables = DBSchemaUtils.newDBSchema();
			DBSchema targetTables = DBSchemaUtils.newDBSchema();
			for (MetaObject type : targetTypes.getMetaObjects()) {
				MOClass targetType = tableType(type);
				MOClass currentType = targetType == null ? null : tableType(currentTypes.getTypeOrNull(type.getName()));
				if (currentType == null) {
					continue;
				}
				DBTable targetTable = SchemaSetup.createTable(targetType);
				if (!DBSchemaUtils.exists(connection, targetTable)) {
					continue;
				}
				DBTable currentTable = SchemaSetup.createTable(currentType);
				targetTables.getTables().add(targetTable);
				currentTables.getTables().add(currentTable);

				Map<String, String> currentBranchColumns = branchColumns(sqlDialect, currentType);
				Map<String, String> targetBranchColumns = branchColumns(sqlDialect, targetType);
				List<String> dropColumns = new ArrayList<>();
				for (String column : currentBranchColumns.keySet()) {
					if (targetTable.getColumn(column) == null) {
						dropColumns.add(column);
					}
				}
				Map<String, String> addColumns = new LinkedHashMap<>();
				for (Map.Entry<String, String> entry : targetBranchColumns.entrySet()) {
					if (currentTable.getColumn(entry.getKey()) == null) {
						addColumns.put(entry.getKey(), entry.getValue());
					}
				}
				changes.add(new TableChange(currentTable, targetTable, dropColumns, addColumns));
			}

			List<String> violations = checkTrunkOnly(connection, currentTypes, changes);
			if (!violations.isEmpty()) {
				log.error("Cannot convert the database to the configured layout options " + describeLayout(target)
					+ ", since it contains data of branches other than the trunk: "
					+ String.join("; ", violations));
				return;
			}

			int changed = 0;
			for (TableChange change : changes) {
				if (DBSchemaUtils.alignTable(log, connection, change._current, change._target, change._dropColumns,
					change._addColumns)) {
					changed++;
				}
			}

			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, target);
			context.reloadPersistentSchemaState();
			log.info("Aligned the layout of " + changed + " tables.");
		} catch (SQLException | ConfigurationException | DataObjectException ex) {
			log.error("Failed to align the table layout with the configured layout options.", ex);
		}
	}

	private static MORepository createRepository(PooledConnection connection, SchemaConfiguration schema,
			boolean versioning) throws SQLException, ConfigurationException, DataObjectException {
		SchemaSetup setup = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(schema);
		return DBTypeRepository.newRepository(connection.getSQLDialect(), setup, versioning);
	}

	/**
	 * The given type, if it is stored in a table of its own.
	 */
	private static MOClass tableType(MetaObject type) {
		if (type == null || type.getKind() != MetaObject.Kind.item) {
			return null;
		}
		MOClass result = (MOClass) type;
		return result.isAbstract() ? null : result;
	}

	/**
	 * The branch columns of the table of the given type, mapped to the SQL expression of their
	 * value in a database containing only the trunk.
	 *
	 * <p>
	 * Branch columns are the columns of the {@link BasicTypes#BRANCH_ATTRIBUTE_NAME branch
	 * attribute} and the {@link ReferencePart#branch branch columns} of references. The branch
	 * column of a reference is set where the reference is set.
	 * </p>
	 */
	private static Map<String, String> branchColumns(DBHelper sqlDialect, MOClass type) {
		String trunk = Long.toString(TLContext.TRUNK_ID);
		Map<String, String> result = new LinkedHashMap<>();
		for (DBAttribute column : type.getDBMapping().getDBAttributes()) {
			MOAttribute attribute = column.getAttribute();
			if (attribute == null) {
				continue;
			}
			if (BasicTypes.BRANCH_ATTRIBUTE_NAME.equals(attribute.getName())) {
				result.put(column.getDBName(), trunk);
			} else if (attribute instanceof MOReference reference
				&& reference.getColumn(ReferencePart.branch) == column) {
				DBAttribute idColumn = reference.getColumn(ReferencePart.name);
				result.put(column.getDBName(), "CASE WHEN " + sqlDialect.columnRef(idColumn.getDBName())
					+ " IS NULL THEN NULL ELSE " + trunk + " END");
			}
		}
		return result;
	}

	/**
	 * Checks that the branch columns to drop contain only the trunk.
	 *
	 * @return Descriptions of the found values of other branches, empty if the database contains
	 *         only the trunk.
	 */
	private static List<String> checkTrunkOnly(PooledConnection connection, MORepository currentTypes,
			List<TableChange> changes) throws SQLException {
		List<String> result = new ArrayList<>();
		if (changes.stream().allMatch(change -> change._dropColumns.isEmpty())) {
			return result;
		}

		MOClass branchType = (MOClass) currentTypes.getTypeOrNull(BasicTypes.BRANCH_TYPE_NAME);
		if (branchType != null) {
			checkTrunkOnly(connection, result, branchType.getDBMapping().getDBName(),
				BranchSupport.getBranchIDAttr(branchType).getDBName());
		}
		for (TableChange change : changes) {
			for (String column : change._dropColumns) {
				checkTrunkOnly(connection, result, change._current.getDBName(), column);
			}
		}
		return result;
	}

	private static void checkTrunkOnly(PooledConnection connection, List<String> violations, String table,
			String column) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		String sql = "SELECT DISTINCT " + sqlDialect.columnRef(column) + " FROM " + sqlDialect.tableRef(table)
			+ " WHERE " + sqlDialect.columnRef(column) + " <> " + TLContext.TRUNK_ID;
		List<Long> branches = new ArrayList<>();
		try (Statement statement = connection.createStatement()) {
			try (ResultSet result = statement.executeQuery(sql)) {
				while (result.next()) {
					branches.add(result.getLong(1));
				}
			}
		}
		if (!branches.isEmpty()) {
			violations.add("table '" + table + "', column '" + column + "' contains branches " + branches);
		}
	}

	private static String describeLayout(SchemaConfiguration schema) {
		return LAYOUT_OPTIONS.stream().map(option -> option + "=" + value(schema, option))
			.collect(Collectors.joining(", "));
	}

	/**
	 * Conversion of a single table.
	 */
	private static class TableChange {

		final DBTable _current;

		final DBTable _target;

		final List<String> _dropColumns;

		final Map<String, String> _addColumns;

		TableChange(DBTable current, DBTable target, List<String> dropColumns, Map<String, String> addColumns) {
			_current = current;
			_target = target;
			_dropColumns = dropColumns;
			_addColumns = addColumns;
		}

	}

}
