/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.model.TLReference;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} adding links to the association table of the configured
 * {@link TLReference} between all objects of the configured source and target types.
 *
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class AddLinksProcessor extends AbstractConfiguredInstance<AddLinksProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link AddLinksProcessor}.
	 */
	public interface Config<I extends AddLinksProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * Qualified name of the reference for that links should be added.
		 */
		@Mandatory
		@Name("reference")
		QualifiedPartName getReference();

		/**
		 * Whether this reference is sorted.
		 */
		@Mandatory
		@Name("is-sorted")
		boolean isSorted();

		/**
		 * Database name of the association table of the given {@link #getReference() reference}.
		 */
		@Mandatory
		@Name("association-table")
		String getAssociationTable();

		/**
		 * Qualified name of the type whose instances are the source of the links that should be
		 * added to {@link #getAssociationTable()}.
		 */
		@Mandatory
		@Name("source-type")
		QualifiedTypeName getSourceType();

		/**
		 * Database table name where objects of the {@link #getSourceType() source type} are stored.
		 */
		@Mandatory
		@Name("source-table")
		String getSourceTable();

		/**
		 * Qualified name of the type whose instances are the target of the links that should be
		 * added to {@link #getAssociationTable()}.
		 */
		@Mandatory
		@Name("target-type")
		QualifiedTypeName getTargetType();

		/**
		 * Database table name where objects of the {@link #getTargetType() source type} are stored.
		 */
		@Mandatory
		@Name("target-table")
		String getTargetTable();

	}

	private static String SOURCE_TYPE_DB_NAME = "SOURCE_TYPE";

	private static String SOURCE_ID_DB_NAME = "SOURCE_ID";

	private static String DEST_TYPE_DB_NAME = "DEST_TYPE";

	private static String DEST_ID_DB_NAME = "DEST_ID";

	private static String META_ATTRIBUTE_ID_DB_NAME = "META_ATTRIBUTE_ID";

	private static String T_TYPE_ID_DB_NAME = "T_TYPE_ID";

	private static String SORT_ORDER_DB_NAME = "SORT_ORDER";

	/**
	 * Creates a {@link AddLinksProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AddLinksProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();

		Config<?> config = getConfig();

		try {
			DBHelper sqlDialect = connection.getSQLDialect();

			CompiledStatement insertStatement =
				createInsertStatement(sqlDialect, util, getTableName(context, config.getAssociationTable()));

			Type source = util.getTLTypeOrFail(connection, config.getSourceType());
			String sourceTableName = getTableName(context, config.getSourceTable());
			Map<Long, Pair<Long, Long>> sourceLifetimeByIds =
				getObjectLifetimeById(log, connection, sqlDialect, source, sourceTableName);

			Type target = util.getTLTypeOrFail(connection, config.getTargetType());
			String targetTableName = getTableName(context, config.getTargetTable());
			Map<Long, Pair<Long, Long>> targetLifetimeByIds =
				getObjectLifetimeById(log, connection, sqlDialect, target, targetTableName);

			TypePart reference = util.getTLTypePartOrFail(connection, config.getReference());

			boolean isSorted = config.isSorted();
			int addedRows = 0;
			for (Entry<Long, Pair<Long, Long>> sourceLifetimeById : sourceLifetimeByIds.entrySet()) {
				int sortOrderCounter = 1;

				for (Entry<Long, Pair<Long, Long>> targetLifetimeById : targetLifetimeByIds.entrySet()) {
					Pair<Long, Long> sourceLifetime = sourceLifetimeById.getValue();
					Pair<Long, Long> targetLifetime = targetLifetimeById.getValue();

					if (hasIntersection(sourceLifetime, targetLifetime)) {
						TLID id = util.newID(connection);
						long revMax = Math.min(sourceLifetime.getSecond(), targetLifetime.getSecond());
						long revMin = Math.max(sourceLifetime.getFirst(), targetLifetime.getFirst());
						TLID sourceId = IdentifierUtil.fromExternalForm(sourceLifetimeById.getKey().toString());
						TLID targetId = IdentifierUtil.fromExternalForm(targetLifetimeById.getKey().toString());
						Integer sortOrder = isSorted ? sortOrderCounter : null;

						addedRows += insertStatement.executeUpdate(connection,
							TLContext.TRUNK_ID, 
							id, 
							revMax,
							revMin,
							revMin,
							config.getSourceTable(),
							sourceId,
							config.getTargetTable(),
							targetId,
							reference.getDefinition(), 
							sortOrder
						);

						sortOrderCounter += OrderedLinkUtil.INSERT_INC;
					}
				}
			}

			log.info("Added " + addedRows + " links in table '" + config.getAssociationTable() + "' from '"
				+ config.getSourceType().getName() + "' to '" + config.getTargetType().getName() + "'.");
		} catch (SQLException | MigrationException exception) {
			log.error("Failed to add links in table '" + config.getAssociationTable() + "' from '"
				+ config.getSourceType().getName() + "' to '" + config.getTargetType().getName() + "'.", exception);
		}
	}

	private CompiledStatement createInsertStatement(DBHelper sqlDialect, Util util, String tableName) {
		List<SQLExpression> values = new ArrayList<>();

		SQLExpression branchParam = util.branchParamOrNull();
		if (branchParam != null) {
			values.add(branchParam);
		}
		values.add(parameter(DBType.ID, BasicTypes.IDENTIFIER_DB_NAME));
		values.add(parameter(DBType.LONG, BasicTypes.REV_MAX_DB_NAME));
		values.add(parameter(DBType.LONG, BasicTypes.REV_MIN_DB_NAME));
		values.add(parameter(DBType.LONG, BasicTypes.REV_CREATE_DB_NAME));
		values.add(parameter(DBType.STRING, SOURCE_TYPE_DB_NAME));
		values.add(parameter(DBType.ID, SOURCE_ID_DB_NAME));
		values.add(parameter(DBType.STRING, DEST_TYPE_DB_NAME));
		values.add(parameter(DBType.ID, DEST_ID_DB_NAME));
		values.add(parameter(DBType.ID, META_ATTRIBUTE_ID_DB_NAME));
		values.add(parameter(DBType.INT, SORT_ORDER_DB_NAME));

		List<String> columnNames = new ArrayList<>();

		String branchColumn = util.branchColumnOrNull();
		if (branchColumn != null) {
			columnNames.add(branchColumn);
		}
		columnNames.add(BasicTypes.IDENTIFIER_DB_NAME);
		columnNames.add(BasicTypes.REV_MAX_DB_NAME);
		columnNames.add(BasicTypes.REV_MIN_DB_NAME);
		columnNames.add(BasicTypes.REV_CREATE_DB_NAME);
		columnNames.add(SOURCE_TYPE_DB_NAME);
		columnNames.add(SOURCE_ID_DB_NAME);
		columnNames.add(DEST_TYPE_DB_NAME);
		columnNames.add(DEST_ID_DB_NAME);
		columnNames.add(META_ATTRIBUTE_ID_DB_NAME);
		columnNames.add(SORT_ORDER_DB_NAME);

		SQLInsert insert = insert(table(tableName), columnNames, values);

		ArrayList<Parameter> parameters = new ArrayList<>();
		parameters.add(util.branchParamDef());
		parameters.add(parameterDef(DBType.ID, BasicTypes.IDENTIFIER_DB_NAME));
		parameters.add(parameterDef(DBType.LONG, BasicTypes.REV_MAX_DB_NAME));
		parameters.add(parameterDef(DBType.LONG, BasicTypes.REV_MIN_DB_NAME));
		parameters.add(parameterDef(DBType.LONG, BasicTypes.REV_CREATE_DB_NAME));
		parameters.add(parameterDef(DBType.STRING, SOURCE_TYPE_DB_NAME));
		parameters.add(parameterDef(DBType.ID, SOURCE_ID_DB_NAME));
		parameters.add(parameterDef(DBType.STRING, DEST_TYPE_DB_NAME));
		parameters.add(parameterDef(DBType.ID, DEST_ID_DB_NAME));
		parameters.add(parameterDef(DBType.ID, META_ATTRIBUTE_ID_DB_NAME));
		parameters.add(parameterDef(DBType.INT, SORT_ORDER_DB_NAME));

		return query(parameters, insert).toSql(sqlDialect);
	}

	private String getTableName(MigrationContext context, String table) {
		return ((MOClass) context.getSchemaRepository().getType(table)).getDBMapping().getDBName();
	}

	private Map<Long, Pair<Long, Long>> getObjectLifetimeById(Log log, Connection connection, DBHelper helper, Type type,
			String tableName) {
		CompiledStatement selectStatement = createSelectStatement(helper, tableName, type);

		Map<Long, Pair<Long, Long>> lifetimeById = new HashMap<>();

		try (ResultSet result = selectStatement.executeQuery(connection)) {
			while (result.next()) {
				long id = result.getLong(1);
				long revMin = result.getLong(2);
				long revMax = result.getLong(3);

				Pair<Long, Long> pair = lifetimeById.get(id);
				if (pair == null) {
					lifetimeById.put(id, new Pair<>(revMin, revMax));
				} else {
					lifetimeById.put(id, new Pair<>(pair.getFirst(), revMax));
				}
			}
		} catch (SQLException exception) {
			log.error("Failed to select revisions and id in table '" + tableName + "' for objects of type '"
				+ type.getTypeName() + "'.", exception);
		}

		return lifetimeById;
	}

	private CompiledStatement createSelectStatement(DBHelper sqlDialect, String tableName, Type type) {
		return query(
			select(
				columns(
					columnDef(BasicTypes.IDENTIFIER_DB_NAME),
					columnDef(BasicTypes.REV_MIN_DB_NAME),
					columnDef(BasicTypes.REV_MAX_DB_NAME)
				),
				table(tableName),
				eqSQL(column(T_TYPE_ID_DB_NAME), literal(DBType.ID, type.getID())),
				orders(
					order(false, column(BasicTypes.IDENTIFIER_DB_NAME)),
					order(false, column(BasicTypes.REV_MIN_DB_NAME))
				)
			)
		).toSql(sqlDialect);
	}

	private boolean hasIntersection(Pair<Long, Long> lifetime1, Pair<Long, Long> lifetime2) {
		return lifetime1.getFirst() <= lifetime2.getSecond() && lifetime1.getSecond() >= lifetime2.getFirst();
	}

}
