/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * 
 */
public class CopyTableDataProcessor extends AbstractConfiguredInstance<CopyTableDataProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link CopyTableDataProcessor}.
	 */
	@TagName("copy-table-data")
	public interface Config<I extends CopyTableDataProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the table to copy from.
		 */
		String getSourceTable();

		/**
		 * Name of the table to copy to.
		 */
		String getDestTable();
	}

	/**
	 * Creates a {@link CopyTableDataProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CopyTableDataProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String sourceTableName = getConfig().getSourceTable();
		String destTableName = getConfig().getDestTable();
		log.info("Copying data from table '" + sourceTableName + "' to '" + destTableName + "'. ");

		MORepository repository = context.getPersistentRepository();
		MOStructure sourceTable = (MOStructure) repository.getMetaObject(sourceTableName);
		MOStructure destTable = (MOStructure) repository.getMetaObject(destTableName);

		try {
			List<String> columns = new ArrayList<>();
			List<SQLColumnDefinition> columnDefs = new ArrayList<>();
			for (MOAttribute attr : sourceTable.getAttributes()) {
				for (DBAttribute column : attr.getDbMapping()) {
					columns.add(column.getDBName());
					columnDefs.add(columnDef(column.getDBName()));
				}
			}

			CompiledStatement sql = query(
				insert(
					table(destTable.getDBMapping().getDBName()),
					columns,
					select(columnDefs, table(sourceTable.getDBMapping().getDBName()))))
				.toSql(connection.getSQLDialect());

			int cnt = sql.executeUpdate(connection);
			log.info("Copied " + cnt + " rows from table '" + sourceTableName + "' to '" + destTableName + "'.");
		} catch (SQLException ex) {
			log.error("Failed to copy table '" + sourceTableName + "' to '" + destTableName + "': " + ex.getMessage(),
				ex);
		}
	}

}
