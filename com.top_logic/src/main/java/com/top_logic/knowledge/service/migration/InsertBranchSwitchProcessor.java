/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.db.sql.SQLInsert;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.knowledge.service.db2.BranchSupport;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} inserting initial branch-switch rows for new tables.
 * 
 * @see Config#getTableTypes()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InsertBranchSwitchProcessor extends AbstractConfiguredInstance<InsertBranchSwitchProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link InsertBranchSwitchProcessor}.
	 */
	public interface Config extends PolymorphicConfiguration<InsertBranchSwitchProcessor> {
		/**
		 * Names of the table types to create branch switch entries for.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getTableTypes();
	}

	/**
	 * Creates a {@link InsertBranchSwitchProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InsertBranchSwitchProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			SQLQuery<SQLInsert> insert = query(
				parameters(parameterDef(DBType.STRING, "typeName")),
				insert(
					table(SQLH.mangleDBName(BranchSupport.BRANCH_SWITCH_TYPE_NAME)),
					columnNames(
						BranchSupport.LINK_BRANCH_COLUMN,
						BranchSupport.LINK_TYPE_COLUMN,
						BranchSupport.LINK_DATA_BRANCH_COLUMN),
					expressions(
						literalLong(TLContext.TRUNK_ID),
						parameter(DBType.STRING, "typeName"),
						literalLong(TLContext.TRUNK_ID))));

			CompiledStatement statement = insert.toSql(connection.getSQLDialect());
			for (String typeName : getConfig().getTableTypes()) {
				statement.executeUpdate(connection, typeName);
			}
			log.info("Inserted branch switch entries: " + getConfig().getTableTypes());
		} catch (SQLException ex) {
			log.error("Failed to insert branch switch entries.", ex);
		}
	}

}
