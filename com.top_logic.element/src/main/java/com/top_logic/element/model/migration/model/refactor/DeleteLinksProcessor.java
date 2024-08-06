/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

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
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} deleting links of some reference.
 */
public class DeleteLinksProcessor extends AbstractConfiguredInstance<DeleteLinksProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link DeleteLinksProcessor}.
	 */
	@TagName("delete-links")
	public interface Config<I extends DeleteLinksProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the table where the links are stored.
		 */
		@Name("table")
		String getTable();

		/**
		 * Name of the reference column of the given {@link #getTable()} that contains the reference
		 * associated with the link stored in a table row.
		 */
		@Name("reference-column")
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getReferenceColumn();

		/**
		 * The reference whose links should be deleted.
		 */
		@Mandatory
		@Name("reference")
		QualifiedPartName getReference();
	}

	/**
	 * Creates a {@link DeleteLinksProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteLinksProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String tableName = getConfig().getTable();
		log.info(
			"Deleting links for '" + getConfig().getReference().getName() + "' from table '" + tableName + "'.");

		MORepository repository = context.getPersistentRepository();
		MOStructure table = (MOStructure) repository.getMetaObject(tableName);
		MOReference metaRef = (MOReference) table.getAttribute(getConfig().getReferenceColumn());

		Util util = context.getSQLUtils();
		try {
			TypePart reference = util.getTLTypePartOrFail(connection, getConfig().getReference());

			CompiledStatement delete = query(
				delete(
					table(table.getDBMapping().getDBName()),
					eqSQL(column(metaRef.getColumn(ReferencePart.name).getDBName()),
						literal(DBType.ID, reference.getDefinition()))))
				.toSql(connection.getSQLDialect());

			int cntDelete = delete.executeUpdate(connection);
			log.info("Deleted " + cntDelete + " rows from table '" + tableName + ".");
		} catch (SQLException | MigrationException ex) {
			log.error(
				"Failed to delete links of reference '" + getConfig().getReference().getName() + "' from '"
					+ tableName + "': " + ex.getMessage(),
				ex);
		}
	}

}
