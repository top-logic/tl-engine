/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLDelete;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.knowledge.service.db2.BranchSupport;

/**
 * {@link MigrationProcessor} removing branch-switch rows for old tables.
 * 
 * @see Config#getTableTypes()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveBranchSwitchProcessor extends AbstractConfiguredInstance<RemoveBranchSwitchProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link RemoveBranchSwitchProcessor}.
	 */
	public interface Config extends PolymorphicConfiguration<RemoveBranchSwitchProcessor> {
		/**
		 * Names of the table types to remove from branch switch table.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getTableTypes();

		/**
		 * Setter for {@link #getTableTypes()}.
		 */
		void setTableTypes(List<String> value);
	}

	/**
	 * Creates a {@link RemoveBranchSwitchProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RemoveBranchSwitchProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			SQLQuery<SQLDelete> deleteSQL = query(
				parameters(parameterDef(DBType.STRING, "typeName")),
				delete(
					table(SQLH.mangleDBName(BranchSupport.BRANCH_SWITCH_TYPE_NAME)),
					eq(column(BranchSupport.LINK_TYPE_COLUMN), parameter(DBType.STRING, "typeName"))));

			CompiledStatement statement = deleteSQL.toSql(connection.getSQLDialect());
			for (String typeName : getConfig().getTableTypes()) {
				statement.executeUpdate(connection, typeName);
			}
			log.info("Removing branch switch entries: " + getConfig().getTableTypes());
		} catch (SQLException ex) {
			log.error("Failed to remove branch switch entries.", ex);
		}
	}

}
