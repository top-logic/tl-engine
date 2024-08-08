/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} deleting objects of some type.
 * 
 * <p>
 * Note: References connecting the deleted object with other objects are not deleted automatically.
 * You are responsible for consistently deleting all links (incoming and outgoing) that link any of
 * the deleted objects to other non-deleted objects. If you fail to do so, your database becomes
 * inconsistent and invalid-link-errors may occur at runtime.
 * </p>
 */
public class DeleteObjectsProcessor extends AbstractConfiguredInstance<DeleteObjectsProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link DeleteObjectsProcessor}.
	 */
	@TagName("delete-objects")
	public interface Config<I extends DeleteObjectsProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the table where the objects are stored.
		 */
		@Name("table")
		String getTable();

		/**
		 * Name of the reference column of the given {@link #getTable()} that contains the reference
		 * associated with the link stored in a table row.
		 */
		@Name("type-column")
		@StringDefault(PersistentObject.T_TYPE_ATTR)
		String getTypeColumn();

		/**
		 * The reference whose links should be deleted.
		 */
		@Mandatory
		@Name("type")
		QualifiedTypeName getType();

		/**
		 * Whether to also delete all objects of sub-types of the given type from the same table.
		 */
		boolean getIncludeSubtypes();
	}

	/**
	 * Creates a {@link DeleteObjectsProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteObjectsProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String tableName = getConfig().getTable();
		log.info(
			"Deleting objects of '" + getConfig().getType().getName() + "' from table '" + tableName + "'.");

		MORepository repository = context.getPersistentRepository();
		MOStructure table = (MOStructure) repository.getMetaObject(tableName);
		MOReference typeRef = (MOReference) table.getAttribute(getConfig().getTypeColumn());

		Util util = context.getSQLUtils();
		try {
			Type type = util.getTLTypeOrFail(connection, getConfig().getType());

			SQLExpression condition;
			if (getConfig().getIncludeSubtypes()) {
				Set<TLID> types = util.getTransitiveSpecializations(connection, type);

				condition = inSet(column(typeRef.getColumn(ReferencePart.name).getDBName()),
					setLiteral(types, DBType.ID));
			} else {
				condition = eqSQL(column(typeRef.getColumn(ReferencePart.name).getDBName()),
					literal(DBType.ID, type.getID()));
			}

			CompiledStatement delete = query(
				delete(
					table(table.getDBMapping().getDBName()),
					condition))
				.toSql(connection.getSQLDialect());

			int cntDelete = delete.executeUpdate(connection);
			log.info("Deleted " + cntDelete + " rows from table '" + tableName + ".");
		} catch (SQLException | MigrationException ex) {
			log.error(
				"Failed to delete objects of type '" + getConfig().getType().getName() + "' from '"
					+ tableName + "': " + ex.getMessage(),
				ex);
		}
	}

}
