/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration._23376;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLUpdate;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} updating id aspect of references as migration for Ticket 23376.
 *
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class Ticket23376UpdateReferenceIdColumns implements MigrationProcessor {

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		DBHelper sqlDialect = getDBHelper(log, connection);

		MORepository repository = context.getSchemaRepository();
		Collection<? extends MetaObject> metaObjects = repository.getMetaObjects();

		for (MetaObject metaObject : metaObjects) {
			if (!(metaObject instanceof MOClass)) {
				continue;
			}

			String tableName = ((MOClass) metaObject).getDBMapping().getDBName();

			List<? extends MOReference> references = MetaObjectUtils.getReferences(metaObject);

			for (MOReference reference : references) {
				DBAttribute name = reference.getColumn(ReferencePart.name);
				DBAttribute branch = reference.getColumn(ReferencePart.branch);
				DBAttribute revision = reference.getColumn(ReferencePart.revision);

				migrateColumn(log, connection, sqlDialect, tableName, name.getDBName());
				migrateColumn(log, connection, sqlDialect, tableName, branch.getDBName());
				migrateColumn(log, connection, sqlDialect, tableName, revision.getDBName());
			}
		}
	}

	private void migrateColumn(Log log, Connection connection, DBHelper helper, String tableName, String columnName) {
		SQLQuery<?> replaceNullValues = createQueryToReplaceNullValues(tableName, columnName);
		SQLQuery<?> setColumnMandatory = createQueryToSetColumnMandatory(tableName, columnName);

		executeSQLQuery(log, connection, helper, replaceNullValues);
		executeSQLQuery(log, connection, helper, setColumnMandatory);
	}

	private DBHelper getDBHelper(Log log, PooledConnection connection) {
		try {
			return connection.getSQLDialect();
		} catch (SQLException exception) {
			log.error("Could not resolve sql dialect.'", exception);

			throw new RuntimeException(exception);
		}
	}

	private SQLQuery<SQLUpdate> createQueryToReplaceNullValues(String tableName, String columnName) {
		return query(
			update(
				table(tableName), 
				isNull(column(columnName)),
				columnNames(columnName), 
				expressions(literalInteger(0))
			)
		);
	}

	private SQLQuery<?> createQueryToSetColumnMandatory(String tableName, String columnName) {
		return query(
			alterTable(
				table(tableName),
				modifyColumnMandatory(columnName, DBType.LONG, true)
			)
		);
	}

	private void executeSQLQuery(Log log, Connection connection, DBHelper helper, SQLQuery<?> query) {
		try {
			query.toSql(helper).executeQuery(connection);
		} catch (SQLException exception) {
			log.error("Failed to execute query: '" + query, exception);
		}
	}

}
