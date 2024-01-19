/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} adjusting table names to the changed convention introduced in Ticket
 * #25893.
 */
public class UpgradeTableNameConvention implements MigrationProcessor {

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		SchemaConfiguration currentSchema = context.getPersistentSchema();
		Collection<MetaObjectName> metaObjects = currentSchema.getMetaObjects().getTypes().values();
		for (MetaObjectName type : metaObjects) {
			if (type instanceof MetaObjectConfig) {
				MetaObjectConfig object = (MetaObjectConfig) type;
				if (object.isAbstract()) {
					continue;
				}
				String dbName = object.getDBName();
				if (dbName == null) {
					String name = object.getObjectName();
					String effectiveDbName = SQLH.mangleDBName(name);
					String oldDbName = legacyMangleDBName(name);
					if (!oldDbName.equals(effectiveDbName)) {
						log.info("Renaming table '" + oldDbName + "' to '" + effectiveDbName +  "'.");
						
						try (PreparedStatement statement = connection.prepareStatement(
							"ALTER TABLE \"" + oldDbName + "\" RENAME TO \"" + effectiveDbName + "\"")) {

							statement.executeUpdate();
						} catch (SQLException ex) {
							log.error("Failed to rename table: " + oldDbName, ex);
						}
					}
				}
			}
		}

	}

	private static String legacyMangleDBName(String name) {
		int len = name.length();
		StringBuffer dbname = new StringBuffer(len + 10);
		boolean useUnnecessaryUnderscores = false;
		for (int i = 0; i < len; i++) {
			char c = name.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				boolean lastLowerCase = i > 0 && Character.isLowerCase(name.charAt(i - 1));
				boolean nextLowerCase = i + 1 < len && Character.isLowerCase(name.charAt(i + 1));
				if (useUnnecessaryUnderscores || lastLowerCase || nextLowerCase) {
					dbname.append('_');
				}
			}
			if (Character.isLetterOrDigit(c)) {
				dbname.append(Character.toUpperCase(c));
			} else {
				// anything else is folded to _ ...
				dbname.append('_');
			}
		}
		String result = dbname.toString();

		return result;

	}

}
