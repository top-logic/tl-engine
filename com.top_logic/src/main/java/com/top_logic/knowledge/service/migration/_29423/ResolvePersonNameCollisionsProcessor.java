/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration._29423;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.LongID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration._29423.PersonNameCollisions.Account;
import com.top_logic.knowledge.service.migration._29423.PersonNameCollisions.Rename;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} that renames accounts whose names collide case-insensitively, so that
 * at most one account per case-insensitive name remains (Ticket #29423).
 *
 * <p>
 * The spelling of the surviving names is preserved. Within a collision group the oldest account
 * (smallest identifier) keeps its name; the others are suffixed (see {@link PersonNameCollisions}).
 * On databases that compare names case-insensitively no such collisions can exist, so this
 * processor is a no-op there. Every rename is logged; renames of directory-managed accounts (with a
 * non-empty authentication device) are logged as a warning so the directory synchronization can be
 * verified.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResolvePersonNameCollisionsProcessor
		extends AbstractConfiguredInstance<ResolvePersonNameCollisionsProcessor.Config<?>>
		implements MigrationProcessor {

	private static final String PERSON_DBNAME = "PERSON";

	private static final String PERSON_COL_NAME = "NAME";

	private static final String PERSON_COL_AUTH_DEVICE_ID = "AUTH_DEVICE_ID";

	/**
	 * Configuration options for {@link ResolvePersonNameCollisionsProcessor}.
	 */
	@TagName("resolve-person-name-collisions")
	public interface Config<I extends ResolvePersonNameCollisionsProcessor> extends PolymorphicConfiguration<I> {
		// No options.
	}

	/**
	 * Creates a {@link ResolvePersonNameCollisionsProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ResolvePersonNameCollisionsProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();
		try {
			// Group the current accounts by branch; collisions are resolved per branch.
			Map<Long, List<PersonRow>> rowsByBranch = new LinkedHashMap<>();
			for (PersonRow row : readCurrentPersons(util, connection)) {
				rowsByBranch.computeIfAbsent(row._branch, x -> new ArrayList<>()).add(row);
			}

			int renamed = 0;
			for (Map.Entry<Long, List<PersonRow>> entry : rowsByBranch.entrySet()) {
				long branch = entry.getKey();
				Map<TLID, PersonRow> rowById = new HashMap<>();
				List<Account> accounts = new ArrayList<>();
				for (PersonRow row : entry.getValue()) {
					TLID id = LongID.valueOf(row._identifier);
					rowById.put(id, row);
					accounts.add(new Account(id, row._name, row._identifier, false));
				}
				for (Rename rename : PersonNameCollisions.computeRenames(accounts)) {
					PersonRow row = rowById.get(rename.getId());
					updateName(util, connection, branch, row._identifier, rename.getNewName());
					renamed++;
					if (StringServices.isEmpty(row._authDeviceID)) {
						log.info("Renamed account '" + rename.getOldName() + "' to '" + rename.getNewName() + "'.");
					} else {
						log.info("Renamed account '" + rename.getOldName() + "' to '" + rename.getNewName()
							+ "' (managed by device '" + row._authDeviceID
							+ "'); please verify the directory synchronization.", Log.WARN);
					}
				}
			}

			if (renamed == 0) {
				log.info("No case-insensitive account-name collisions found.");
			}
		} catch (SQLException ex) {
			log.error("Failed to resolve case-insensitive account-name collisions.", ex);
		}
	}

	private List<PersonRow> readCurrentPersons(Util util, PooledConnection connection) throws SQLException {
		List<SQLColumnDefinition> selectColumns = columns(
			util.branchColumnDef(),
			columnDef(BasicTypes.IDENTIFIER_DB_NAME),
			columnDef(PERSON_COL_NAME),
			columnDef(PERSON_COL_AUTH_DEVICE_ID));
		SQLSelect select = select(
			selectColumns,
			table(PERSON_DBNAME),
			eqSQL(column(BasicTypes.REV_MAX_DB_NAME), literal(DBType.LONG, Revision.CURRENT_REV)),
			orders(order(column(BasicTypes.IDENTIFIER_DB_NAME))));

		CompiledStatement statement = query(select).toSql(connection.getSQLDialect());
		List<PersonRow> result = new ArrayList<>();
		try (ResultSet resultSet = statement.executeQuery(connection)) {
			while (resultSet.next()) {
				result.add(new PersonRow(
					resultSet.getLong(1),
					resultSet.getLong(2),
					resultSet.getString(3),
					resultSet.getString(4)));
			}
		}
		return result;
	}

	private void updateName(Util util, PooledConnection connection, long branch, long identifier, String newName)
			throws SQLException {
		CompiledStatement statement = query(update(
			table(PERSON_DBNAME),
			and(
				eqSQL(util.branchColumnRef(), literal(DBType.LONG, branch)),
				eqSQL(column(BasicTypes.IDENTIFIER_DB_NAME), literal(DBType.ID, LongID.valueOf(identifier))),
				eqSQL(column(BasicTypes.REV_MAX_DB_NAME), literal(DBType.LONG, Revision.CURRENT_REV))),
			columnNames(PERSON_COL_NAME),
			expressions(literal(DBType.STRING, newName)))).toSql(connection.getSQLDialect());
		statement.executeUpdate(connection);
	}

	/**
	 * A current row of the {@code PERSON} table relevant for the collision analysis.
	 */
	private static final class PersonRow {

		final long _branch;

		final long _identifier;

		final String _name;

		final String _authDeviceID;

		PersonRow(long branch, long identifier, String name, String authDeviceID) {
			_branch = branch;
			_identifier = identifier;
			_name = name;
			_authDeviceID = authDeviceID;
		}
	}
}
