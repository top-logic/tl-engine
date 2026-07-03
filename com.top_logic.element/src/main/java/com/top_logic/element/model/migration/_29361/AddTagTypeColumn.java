/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration._29361;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.element.version.intf.TagFactory;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} updating the table {@link TagFactory#KO_NAME_TAG} to be compatible
 * with Ticket #29361.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AddTagTypeColumn implements MigrationProcessor {

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			MetaObject tagType = context.getPersistentRepository().getTypeOrNull("Tag");
			if (tagType == null) {
				log.info("No Tag table to update.");
				return;
			}

			MOClass tagClass = (MOClass) tagType;
			MOAttribute taggedObjReference = tagClass.getAttributeOrNull("taggedObj");
			if (taggedObjReference == null) {
				log.info("No taggedObj reference.", Protocol.WARN);
				return;
			}
			String typeColName = Util.refType(taggedObjReference.getName());
			execute(connection,
				addColumn(table(tagClass.getDBMapping().getDBName()), typeColName, DBType.STRING)
					.setBinary(true)
					.setSize(150));

		} catch (SQLException ex) {
			log.error("Unable to update table Tag table.", ex);
		}
	}

	private void execute(PooledConnection connection, SQLStatement stmt) throws SQLException {
		query(stmt).toSql(connection.getSQLDialect()).executeUpdate(connection);
	}

}

