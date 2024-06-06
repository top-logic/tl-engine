/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} moving reference links from one table to another.
 */
public class MoveLinksProcessor extends AbstractConfiguredInstance<MoveLinksProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link MoveLinksProcessor}.
	 */
	@TagName("move-links")
	public interface Config<I extends MoveLinksProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the table to take the links from.
		 */
		@Name("source-table")
		String getSourceTable();

		/**
		 * Name of the table to move the links to.
		 */
		@Name("dest-table")
		String getDestTable();

		/**
		 * Name of the reference column of the given {@link #getSourceTable()} that contains the
		 * reference associated with the link stored in a table row.
		 */
		@Name("reference-column")
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getReferenceColumn();

		/**
		 * The reference whose links should be moved.
		 */
		@Mandatory
		@Name("reference")
		QualifiedPartName getReference();
	}

	/**
	 * Creates a {@link MoveLinksProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MoveLinksProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String sourceTableName = getConfig().getSourceTable();
		String destTableName = getConfig().getDestTable();
		log.info(
			"Moving links for '" + getConfig().getReference().getName() + "' from table '" + sourceTableName + "' to '"
			+ destTableName + "'. ");

		MORepository repository = context.getPersistentRepository();
		MOStructure sourceTable = (MOStructure) repository.getMetaObject(sourceTableName);
		MOStructure destTable = (MOStructure) repository.getMetaObject(destTableName);
		MOReference metaRef = (MOReference) sourceTable.getAttribute(getConfig().getReferenceColumn());

		Util util = context.getSQLUtils();
		try {
			TypePart reference = util.getTLTypePartOrFail(connection, getConfig().getReference());

			List<String> columns = new ArrayList<>();
			List<SQLColumnDefinition> columnDefs = new ArrayList<>();
			for (MOAttribute attr : sourceTable.getAttributes()) {
				if (destTable.getAttributeOrNull(attr.getName()) == null) {
					continue;
				}

				for (DBAttribute column : attr.getDbMapping()) {
					columns.add(column.getDBName());
					columnDefs.add(columnDef(column.getDBName()));
				}
			}

			CompiledStatement copy = query(
				insert(
					table(destTable.getDBMapping().getDBName()),
					columns,
					select(
						columnDefs,
						table(sourceTable.getDBMapping().getDBName()),
						eqSQL(column(metaRef.getColumn(ReferencePart.name).getDBName()),
							literal(DBType.ID, reference.getDefinition())))))
								.toSql(connection.getSQLDialect());

			int cntCopy = copy.executeUpdate(connection);
			log.info("Copied " + cntCopy + " rows from table '" + sourceTableName + "' to '" + destTableName + "'.");

			CompiledStatement delete = query(
				delete(
					table(sourceTable.getDBMapping().getDBName()),
					eqSQL(column(metaRef.getColumn(ReferencePart.name).getDBName()),
						literal(DBType.ID, reference.getDefinition()))))
				.toSql(connection.getSQLDialect());

			int cntDelete = delete.executeUpdate(connection);
			log.info("Deleted " + cntDelete + " rows from table '" + sourceTableName + ".");
		} catch (SQLException | MigrationException ex) {
			log.error(
				"Failed to move links of reference '" + getConfig().getReference().getName() + "' from '"
					+ sourceTableName + "' to '" + destTableName + "': " + ex.getMessage(),
				ex);
		}
	}

}
