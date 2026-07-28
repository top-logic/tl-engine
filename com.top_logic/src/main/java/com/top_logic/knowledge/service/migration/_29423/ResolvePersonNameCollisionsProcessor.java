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

import com.top_logic.base.security.device.TLSecurityDeviceManager;
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
 * All accounts are considered across their whole history (including deleted, history-only
 * accounts). Assuming an account's name never changes, a rename rewrites the name in every revision
 * of the account, so that no two case-insensitively equal names ever co-exist at any point in time.
 * The spelling of the surviving names is preserved. Within a collision group a living account is
 * kept (see {@link PersonNameCollisions}); the others are suffixed. On databases that compare names
 * case-insensitively no such collisions can exist, so this processor is a no-op there. Every rename
 * is logged; renames of directory-managed accounts (with a non-empty authentication device) are
 * logged as a warning so the directory synchronization can be verified.
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
			// Group all accounts (across their whole history) by branch; collisions are resolved
			// per branch.
			Map<Long, List<PersonRow>> rowsByBranch = new LinkedHashMap<>();
			for (PersonRow row : readPersons(util, connection)) {
				rowsByBranch.computeIfAbsent(row.branch(), x -> new ArrayList<>()).add(row);
			}

			int renamed = 0;
			for (Map.Entry<Long, List<PersonRow>> entry : rowsByBranch.entrySet()) {
				long branch = entry.getKey();
				Map<TLID, PersonRow> rowById = new HashMap<>();
				List<Account> accounts = new ArrayList<>();
				for (PersonRow row : entry.getValue()) {
					TLID id = LongID.valueOf(row.identifier());
					rowById.put(id, row);
					boolean externallyManaged = !TLSecurityDeviceManager.DB_SECURITY.equals(row.authDeviceID());
					accounts.add(new Account(id, row.name(), row.identifier(), externallyManaged, row.alive()));
				}
				for (Rename rename : PersonNameCollisions.computeRenames(accounts)) {
					PersonRow row = rowById.get(rename.id());
					updateName(util, connection, branch, row.identifier(), rename.newName());
					renamed++;
					if (StringServices.isEmpty(row.authDeviceID())) {
						log.info("Renamed account '" + rename.oldName() + "' to '" + rename.newName() + "'.");
					} else {
						log.info("Renamed account '" + rename.oldName() + "' to '" + rename.newName()
							+ "' (managed by device '" + row.authDeviceID()
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

	/**
	 * Reads every account across all revisions (including deleted, history-only accounts),
	 * deduplicated to one {@link PersonRow} per (branch, identifier). Since an account's name does
	 * not change, any revision provides the name; the account is {@link PersonRow#alive() alive} if
	 * any of its revisions is the current one.
	 */
	private List<PersonRow> readPersons(Util util, PooledConnection connection) throws SQLException {
		List<SQLColumnDefinition> selectColumns = columns(
			util.branchColumnDef(),
			columnDef(BasicTypes.IDENTIFIER_DB_NAME),
			columnDef(PERSON_COL_NAME),
			columnDef(PERSON_COL_AUTH_DEVICE_ID),
			columnDef(BasicTypes.REV_MAX_DB_NAME));
		SQLSelect select = select(selectColumns, table(PERSON_DBNAME));

		CompiledStatement statement = query(select).toSql(connection.getSQLDialect());
		Map<String, PersonRow> byPerson = new LinkedHashMap<>();
		try (ResultSet resultSet = statement.executeQuery(connection)) {
			while (resultSet.next()) {
				long branch = resultSet.getLong(1);
				long identifier = resultSet.getLong(2);
				String name = resultSet.getString(3);
				String authDeviceID = resultSet.getString(4);
				boolean alive = resultSet.getLong(5) == Revision.CURRENT_REV;

				String key = branch + ":" + identifier;
				PersonRow existing = byPerson.get(key);
				if (existing == null) {
					byPerson.put(key, new PersonRow(branch, identifier, name, authDeviceID, alive));
				} else if (alive && !existing.alive()) {
					byPerson.put(key, new PersonRow(branch, identifier, name, authDeviceID, true));
				}
			}
		}
		return new ArrayList<>(byPerson.values());
	}

	/**
	 * Renames an account in all its revisions (its name is constant over time), so that no two
	 * case-insensitively equal names ever co-exist at any point in time.
	 */
	private void updateName(Util util, PooledConnection connection, long branch, long identifier, String newName)
			throws SQLException {
		CompiledStatement statement = query(update(
			table(PERSON_DBNAME),
			and(
				eqSQL(util.branchColumnRef(), literal(DBType.LONG, branch)),
				eqSQL(column(BasicTypes.IDENTIFIER_DB_NAME), literal(DBType.ID, LongID.valueOf(identifier)))),
			columnNames(PERSON_COL_NAME),
			expressions(literal(DBType.STRING, newName)))).toSql(connection.getSQLDialect());
		statement.executeUpdate(connection);
	}

	/**
	 * One account of the {@code PERSON} table, deduplicated across its revisions.
	 *
	 * @param branch
	 *        The branch the account lives on.
	 * @param identifier
	 *        The account's technical identifier.
	 * @param name
	 *        The account's login name.
	 * @param authDeviceID
	 *        The authentication device the account is managed by, or empty for a local account.
	 * @param alive
	 *        Whether any revision of the account is the current one.
	 */
	private record PersonRow(long branch, long identifier, String name, String authDeviceID, boolean alive) {
		// Data record.
	}
}
