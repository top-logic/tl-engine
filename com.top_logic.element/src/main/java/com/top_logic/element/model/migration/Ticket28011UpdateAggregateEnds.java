/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.SQLProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} that updates the "self" end of a composite association by setting
 * aggregate="true" and multiple="false".
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Ticket28011UpdateAggregateEnds implements MigrationProcessor {

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		log.info("Updating aggregation ends.");
		try {
			tryMigrate(log, connection, context.getSQLUtils());
		} catch (SQLException ex) {
			log.error("Failed to update aggregation ends: " + ex.getMessage(), ex);
		}
	}

	private void tryMigrate(Log log, PooledConnection connection, Util util) throws SQLException {
		SQLProcessor processor = new SQLProcessor(connection);

		SQLSelect ownerSelect = select(
			Util.listWithoutNull(
				util.branchColumnDef(),
				columnDef(Util.refID(TLAssociationEnd.OWNER_ATTR))),
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			and(
				eq(
					column(SQLH.mangleDBName(TLAssociationEnd.COMPOSITE_ATTR)),
					literalTrueValue()),
				eq(
					column(SQLH.mangleDBName(ApplicationObjectUtil.IMPLEMENTATION_NAME)),
					literalString(TLStructuredTypeColumns.ASSOCIATION_END_IMPL))));
		Map<Long, Set<TLID>> ownerIdsByBranch = new HashMap<>();
		try (ResultSet owners = processor.queryResultSet(ownerSelect)) {
			while (owners.next()) {
				ownerIdsByBranch
					.computeIfAbsent(owners.getLong(1), b -> new HashSet<>())
					.add(IdentifierUtil.getId(owners, 2));
			}
		}

		CompiledStatement update = query(
			parameters(
				util.branchParamDef(),
				setParameterDef("ownerIds", DBType.ID)),
			update(
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					util.eqBranch(),
					inSet(column(Util.refID(TLAssociationEnd.OWNER_ATTR)),
						setParameter("ownerIds", DBType.ID)),
					eq(column(SQLH.mangleDBName(TLStructuredTypePart.NAME_ATTR)),
						literalString(TLStructuredTypeColumns.SELF_ASSOCIATION_END_NAME))),
				Arrays.asList(
					SQLH.mangleDBName(SQLH.mangleDBName(TLAssociationEnd.AGGREGATE_ATTR)),
					SQLH.mangleDBName(SQLH.mangleDBName(TLAssociationEnd.MULTIPLE_ATTR))),
				Arrays.asList(
					literalTrueValue(),
					literalFalseValue()))).toSql(connection.getSQLDialect());

		int cnt = 0;
		for (Entry<Long, Set<TLID>> idsByBranch : ownerIdsByBranch.entrySet()) {
			cnt += update.executeUpdate(connection, idsByBranch.getKey(), idsByBranch.getValue());
		}

		log.info("Updated '" + cnt + "' opposite ends of composition ends.");
	}

}
