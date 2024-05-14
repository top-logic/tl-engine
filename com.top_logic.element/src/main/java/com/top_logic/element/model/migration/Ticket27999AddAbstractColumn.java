/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.SQLProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} that creates the column {@link TLStructuredTypePart#ABSTRACT_ATTR}.
 * The default for new column is <code>false</code>.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Ticket27999AddAbstractColumn implements MigrationProcessor {

	/** Database name of column {@link TLStructuredTypePart#ABSTRACT_ATTR}. */
	private static final String ABSTRACT_COL = "ABSTRACT";

	/** Database name of table {@link ApplicationObjectUtil#META_ATTRIBUTE_OBJECT_TYPE}. */
	private static final String META_ATTRIBUTE_TABLE = "META_ATTRIBUTE";

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			tryMigrate(log, connection, context.getSQLUtils());
		} catch (SQLException ex) {
			log.error("Failed to update history type: " + ex.getMessage(), ex);
		}
	}

	private void tryMigrate(Log log, PooledConnection connection, Util util) throws SQLException {
		SQLProcessor processor = new SQLProcessor(connection);

		processor.execute(
			alterTable(
				metaAttributeTable(),
				addColumn(ABSTRACT_COL, DBType.BOOLEAN)));
			
		log.info("Created " + ABSTRACT_COL + "-column.");

		util.setAbstractColumn(true);
	}

	private SQLTable metaAttributeTable() {
		return table(META_ATTRIBUTE_TABLE);
	}

}
