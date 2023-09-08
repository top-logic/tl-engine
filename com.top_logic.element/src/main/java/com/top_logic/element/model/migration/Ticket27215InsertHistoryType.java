/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttributeFactory;
import com.top_logic.element.meta.kbbased.PersistentReference;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociationEnd;

/**
 * {@link MigrationProcessor} that creates the column {@link PersistentReference#HISTORY_TYPE_ATTR}
 * and fills it with value {@link HistoryType#CURRENT} for all {@link TLAssociationEnd}.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Ticket27215InsertHistoryType implements MigrationProcessor {

	/** Inlined value of {@link KBBasedMetaAttributeFactory#ASSOCIATION_END_IMPL}. */
	private static final String ASSOCIATION_END_VALUE = "association-end";

	/** Database name of column {@link PersistentReference#HISTORY_TYPE_ATTR}. */
	private static final String HISTORY_TYPE_COL = "HISTORY_TYPE";

	/** Database name of column {@link KBBasedMetaAttribute#IMPLEMENTATION_NAME}. */
	private static final String IMPL_COL = "IMPL";

	/** Database name of table {@link ApplicationObjectUtil#META_ATTRIBUTE_OBJECT_TYPE}. */
	private static final String META_ATTRIBUTE_TABLE = "META_ATTRIBUTE";

	/** Inlined value of {@link HistoryType#CURRENT}. */
	private static final String HISTORY_TYPE_CURRENT = "current";

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			tryMigrate(log, connection);
		} catch (SQLException ex) {
			log.error("Failed to update history type: " + ex.getMessage(), ex);
		}
	}

	private void tryMigrate(Log log, PooledConnection connection) throws SQLException {
		SQLProcessor processor = new SQLProcessor(connection);

		processor.execute(
			alterTable(
				metaAttributeTable(),
				addColumn(HISTORY_TYPE_COL, DBType.STRING).setSize(150)));
			
		
		int cntUpdate = processor.execute(
			update(
				metaAttributeTable(),
				eq(column(IMPL_COL),
					literalString(ASSOCIATION_END_VALUE)),
				columnNames(HISTORY_TYPE_COL),
				expressions(literalString(HISTORY_TYPE_CURRENT))));
		log.info("Updated history type column of '" + cntUpdate + "' association end's to value 'current'.");
	}

	private SQLTable metaAttributeTable() {
		return table(META_ATTRIBUTE_TABLE);
	}

}
