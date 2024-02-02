/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration._25881;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.Arrays;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} updating the table {@link FastList#OBJECT_NAME} to be compatible with
 * Ticket #25881.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateFastListTable implements MigrationProcessor {

	private Util _util;

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			String scopeTypeColName =
				ReferencePart.type.getReferenceAspectColumnName(SQLH.mangleDBName(FastList.SCOPE_REF));
			String scopeIDColName =
				ReferencePart.name.getReferenceAspectColumnName(SQLH.mangleDBName(FastList.SCOPE_REF));

			addScopeColumns(connection, scopeTypeColName, scopeIDColName);

			removeModuleTypeColumn(connection);

			addIndexes(connection, scopeIDColName);

		} catch (SQLException ex) {
			log.error("Unable to update table " + enumTable() + ".", ex);
		}
	}

	private void addIndexes(PooledConnection connection, String scopeIDColName) throws SQLException {
		execute(connection, addIndex(enumTable(), "FAST_LIST_NAME_IDX", true, _util.branchColumnOrNull(),
			scopeIDColName, SQLH.mangleDBName(FastList.NAME_ATTRIBUTE), BasicTypes.REV_MAX_DB_NAME));

		execute(connection, addIndex(enumTable(), "FAST_LIST_SCOPE", false, _util.branchColumnOrNull(),
			scopeIDColName, BasicTypes.REV_MAX_DB_NAME));
	}

	private void removeModuleTypeColumn(PooledConnection connection) throws SQLException {
		/* Module reference is now monomorphic. */
		execute(connection, alterTable(enumTable(),
			dropColumn(ReferencePart.type.getReferenceAspectColumnName(SQLH.mangleDBName(FastList.MODULE_REF)))));
	}

	private void addScopeColumns(PooledConnection connection, String scopeTypeColName, String scopeIDColName)
			throws SQLException {
		/* Add scope type column. */
		execute(connection,
			alterTable(enumTable(), addColumn(scopeTypeColName, DBType.STRING).setBinary(true).setSize(150)));
		/* Add scope id column. */
		execute(connection, alterTable(enumTable(), addColumn(scopeIDColName, DBType.LONG)));
		/* Update "scope" columns with corresponding values of "module" columns. */
		execute(connection, update(
			enumTable(),
			literalTrueLogical(),
			Arrays.asList(
				scopeTypeColName,
				scopeIDColName),
			Arrays.asList(
				literalString(TlModelFactory.KO_NAME_TL_MODULE),
				column(ReferencePart.name.getReferenceAspectColumnName(SQLH.mangleDBName(FastList.MODULE_REF))))));

		/* Make scope type column mandatory. */
		execute(connection, alterTable(enumTable(), modifyColumnMandatory(scopeIDColName, DBType.LONG, true)));
		/* Make scope id column mandatory. */
		execute(connection, alterTable(enumTable(),
			modifyColumnMandatory(scopeTypeColName, DBType.STRING, true).setBinary(true).setSize(150)));
	}

	private static SQLTable enumTable() {
		return table(SQLH.mangleDBName(FastList.OBJECT_NAME));
	}

	private void execute(PooledConnection connection, SQLStatement stmt) throws SQLException {
		query(stmt).toSql(connection.getSQLDialect()).executeUpdate(connection);
	}

}

