/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} that changes the type of objects of a given reference from a concrete
 * source type to a concrete destination type.
 *
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class ChangeReferenceObjectsTypeProcessor
		extends AbstractConfiguredInstance<ChangeReferenceObjectsTypeProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link ChangeReferenceObjectsTypeProcessor}.
	 */
	@TagName("change-reference-objects-type")
	public interface Config<I extends ChangeReferenceObjectsTypeProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * Qualified name of the reference of whose objects the type should be changed.
		 */
		@Mandatory
		@Name("reference")
		QualifiedPartName getReference();

		/**
		 * Database name of the association table of the given {@link #getReference() reference}.
		 */
		@Mandatory
		@Name("association-table")
		String getAssociationTable();

		/**
		 * The name of the table, where the objects whose type should be changed are stored.
		 */
		@Mandatory
		@Name("table")
		String getTable();

		/**
		 * The type of the objects that should be changed.
		 */
		@Mandatory
		@Name("source-type")
		QualifiedTypeName getSourceType();

		/**
		 * The new type the modified objects should be assigned to.
		 */
		@Mandatory
		@Name("target-type")
		QualifiedTypeName getTargetType();
	}

	private static String META_ATTRIBUTE_ID_DB_NAME = "META_ATTRIBUTE_ID";

	private static String DEST_ID_DB_NAME = "DEST_ID";

	/**
	 * Creates a {@link ChangeReferenceObjectsTypeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChangeReferenceObjectsTypeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();

		Config<?> config = getConfig();
		try {
			DBHelper sqlDialect = connection.getSQLDialect();

			TypePart reference = util.getTLTypePartOrFail(connection, config.getReference());
			Set<TLID> targetIds = selectTargetIds(log, connection, sqlDialect, reference, config.getAssociationTable());

			Type sourceType = util.getTLTypeOrFail(connection, config.getSourceType());
			Type targetType = util.getTLTypeOrFail(connection, config.getTargetType());

			MOClass table = (MOClass) context.getSchemaRepository().getType(config.getTable());
			MOReference tType = (MOReference) table.getAttribute(PersistentObject.T_TYPE_ATTR);

			String tableName = table.getDBMapping().getDBName();
			String tTypeColumn = tType.getColumn(ReferencePart.name).getDBName();

			CompiledStatement updateStatement = query(
				update(
					table(tableName),
					and(
						eqSQL(column(tTypeColumn), literal(DBType.ID, sourceType.getID())),
						inSet(column(BasicTypes.IDENTIFIER_DB_NAME), targetIds, DBType.ID)),
					columnNames(tTypeColumn),
					expressions(literal(DBType.ID, targetType.getID())))).toSql(sqlDialect);

			int updatedRows = updateStatement.executeUpdate(connection);

			log.info("Changed type of reference '" + config.getReference().getName() + "' " + updatedRows
				+ " objects in table '" + tableName + "' from '"
				+ config.getSourceType().getName() + "' to '" + config.getTargetType().getName() + "'.");
		} catch (SQLException | MigrationException exception) {
			log.error("Failed to change type of the reference '" + config.getReference().getName()
				+ "' objects in table '" + config.getTable() + "' from '"
				+ config.getSourceType().getName() + "' to '" + config.getTargetType().getName() + "'.", exception);
		}
	}

	/**
	 * Returns the links target identifiers of the given {@link TypePart reference} in the given
	 * association table.
	 */
	private Set<TLID> selectTargetIds(Log log, PooledConnection connection, DBHelper helper, TypePart reference,
			String tableName) {
		Set<TLID> targetIds = new HashSet<>();

		try (ResultSet result = createTargetIdSelectStatement(helper, reference, tableName).executeQuery(connection)) {
			while (result.next()) {
				targetIds.add(IdentifierUtil.fromExternalForm(Long.toString(result.getLong(1))));
			}
		} catch (SQLException exception) {
			log.error("Failed to select target identifiers in association table '" + tableName
				+ "' for links of reference '" + reference.getPartName() + "'.", exception);
		}

		return targetIds;
	}

	private CompiledStatement createTargetIdSelectStatement(DBHelper helper, TypePart reference, String tableName) {
		return query(
			select(
				columns(columnDef(DEST_ID_DB_NAME)),
				table(tableName),
				eqSQL(column(META_ATTRIBUTE_ID_DB_NAME), literal(DBType.ID, reference.getDefinition())),
				noOrder())).toSql(helper);
	}

}
