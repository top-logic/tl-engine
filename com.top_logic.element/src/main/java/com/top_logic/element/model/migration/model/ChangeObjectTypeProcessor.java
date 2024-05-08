/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} that changes the type of objects from a concrete source type to a
 * concrete destination type.
 */
public class ChangeObjectTypeProcessor extends AbstractConfiguredInstance<ChangeObjectTypeProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link ChangeObjectTypeProcessor}.
	 */
	@TagName("change-object-type")
	public interface Config<I extends ChangeObjectTypeProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * The name of the table, where the objects of the source type are stored.
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

	/**
	 * Creates a {@link ChangeObjectTypeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChangeObjectTypeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.get(Util.PROPERTY);
		
		Config<?> config = getConfig();
		try {
			Type sourceType = util.getTLTypeOrFail(connection, config.getSourceType());
			Type targetType = util.getTLTypeOrFail(connection, config.getTargetType());
			
			MOClass table = (MOClass) context.getSchemaRepository().getType(config.getTable());
			MOReference tType = (MOReference) table.getAttribute(PersistentObject.T_TYPE_ATTR);

			String tableName = table.getDBMapping().getDBName();
			String tTypeColumn = tType.getColumn(ReferencePart.name).getDBName();

			CompiledStatement update = query(
				update(
					table(tableName),
					eqSQL(literal(DBType.ID, sourceType.getID()), column(tTypeColumn)),
					columnNames(tTypeColumn),
					expressions(literal(DBType.ID, targetType.getID())))).toSql(connection.getSQLDialect());
			
			int cnt = update.executeUpdate(connection);

			log.info("Changed type of " + cnt + " objects in table '" + tableName + "' from '"
				+ config.getSourceType().getName() + "' to '" + config.getTargetType().getName() + "'.");
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to change type of objects in table '" + config.getTable() + "' from '"
				+ config.getSourceType().getName() + "' to '" + config.getTargetType().getName() + "'.", ex);
		}
	}

}
