/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;

import com.top_logic.basic.Log;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.PersistentReference;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.SQLProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} that creates the column {@link PersistentReference#HISTORY_TYPE_ATTR}
 * and fills it with value {@link HistoryType#CURRENT} for all {@link TLAssociationEnd}.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Ticket28263InitDeletionPolicy implements MigrationProcessor {

	/** Inlined value of {@link TLStructuredTypeColumns#ASSOCIATION_END_IMPL}. */
	private static final String ASSOCIATION_END_VALUE = "association-end";

	/** Database name of column {@link PersistentReference#DELETION_POLICY_ATTR}. */
	private static final String VALUE_COL = "DELETION_POLICY";

	/** Database name of column {@link KBBasedMetaAttribute#IMPLEMENTATION_NAME}. */
	private static final String IMPL_COL = "IMPL";

	/** Database name of table {@link ApplicationObjectUtil#META_ATTRIBUTE_OBJECT_TYPE}. */
	private static final String TABLE = "META_ATTRIBUTE";

	/** Inlined value of {@link DeletionPolicy#CLEAR_REFERENCE}. */
	private static final String DEFAULT_VALUE = "clear-reference";

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

		int cntUpdate = processor.execute(
			update(
				table(TABLE),
				eq(column(IMPL_COL),
					literalString(ASSOCIATION_END_VALUE)),
				columnNames(VALUE_COL),
				expressions(literalString(DEFAULT_VALUE))));
		log.info("Initialized deletion policy column of '" + cntUpdate + "' association end's.");

		util.setDeletionColumn(true);
	}

}
