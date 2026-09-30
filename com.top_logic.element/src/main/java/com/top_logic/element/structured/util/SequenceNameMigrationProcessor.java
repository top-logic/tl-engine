/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.db2.SequenceTypeProvider;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} rewriting object-annotated sequence names to the unified naming scheme.
 *
 * <p>
 * {@link SequenceIdGenerator} and {@link SequenceDefaultProvider} formerly placed the technical
 * suffix {@link #LEGACY_SUFFIX} between the base name and the context part
 * (<code>base_NumberHandler_context</code>), while the <code>generateSequenceId</code>/
 * <code>resetSequence</code> TL-Script functions appended the suffix last
 * (<code>base_context_SequenceId</code>). Both now use the shared {@link
 * SequenceIdGenerator#sequenceName(String, Object)} layout (<code>base_context_SequenceId</code>).
 * </p>
 *
 * <p>
 * This processor moves every existing sequence row that still carries the legacy suffix to the
 * unified name: the {@link #LEGACY_SUFFIX} token marks the boundary between base name and context,
 * so the rewrite is unambiguous. If a unified row already exists (created earlier by one of the
 * script functions), the higher counter value is kept so that no number is ever handed out twice.
 * </p>
 *
 * @author <a href="mailto:bernhard.haumacher@top-logic.com">Bernhard Haumacher</a>
 */
public class SequenceNameMigrationProcessor implements MigrationProcessor {

	/**
	 * Technical suffix that {@link SequenceIdGenerator} and {@link SequenceDefaultProvider} inserted
	 * between the base name and the context part before the naming scheme was unified.
	 */
	private static final String LEGACY_SUFFIX = "_NumberHandler";

	/**
	 * Rewrites a legacy sequence name (with {@link #LEGACY_SUFFIX} between base name and context) to
	 * the unified name (context followed by {@link SequenceIdGenerator#SEQUENCE_SUFFIX}).
	 *
	 * @param legacyId
	 *        The stored sequence name.
	 * @return The unified sequence name, or <code>null</code> if the given name does not carry the
	 *         legacy suffix (and therefore needs no migration).
	 */
	public static String toUnifiedName(String legacyId) {
		int suffixStart = legacyId.indexOf(LEGACY_SUFFIX);
		if (suffixStart < 0) {
			return null;
		}
		String base = legacyId.substring(0, suffixStart);
		String contextPart = legacyId.substring(suffixStart + LEGACY_SUFFIX.length());
		return base + contextPart + SequenceIdGenerator.SEQUENCE_SUFFIX;
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		DBHelper sql;
		try {
			sql = connection.getSQLDialect();
		} catch (SQLException ex) {
			log.error("Unable to access the SQL dialect for the sequence name migration.", ex);
			return;
		}

		String table = sql.tableRef(SequenceTypeProvider.sequenceTableName());
		String idColumn = sql.columnRef(SequenceTypeProvider.sequenceIdColumnName());
		String valueColumn = sql.columnRef(SequenceTypeProvider.sequenceValueColumnName());

		Map<String, Long> values = readSequences(log, connection, table, idColumn, valueColumn);
		if (values == null) {
			return;
		}

		Map<String, Long> targets = new HashMap<>();
		List<String> obsolete = new ArrayList<>();
		for (Map.Entry<String, Long> entry : values.entrySet()) {
			String id = entry.getKey();
			String newId = toUnifiedName(id);
			if (newId == null) {
				continue;
			}

			long value = entry.getValue().longValue();
			Long resolved = targets.get(newId);
			if (resolved == null) {
				// Keep an already existing unified counter (created by a script function) if it is
				// higher, so that no number is reused.
				resolved = values.get(newId);
			}
			targets.put(newId, resolved == null ? value : Math.max(resolved.longValue(), value));
			obsolete.add(id);
		}

		if (obsolete.isEmpty()) {
			log.info("No object-annotated sequences to migrate.");
			return;
		}

		if (!deleteRows(log, connection, table, idColumn, obsolete)) {
			return;
		}
		if (!writeRows(log, connection, table, idColumn, valueColumn, values, targets)) {
			return;
		}

		log.info("Migrated " + obsolete.size() + " object-annotated sequence(s) to the unified naming scheme.");
	}

	private Map<String, Long> readSequences(Log log, PooledConnection connection, String table, String idColumn,
			String valueColumn) {
		Map<String, Long> values = new HashMap<>();
		String select = "SELECT " + idColumn + ", " + valueColumn + " FROM " + table;
		try (PreparedStatement statement = connection.prepareStatement(select);
				ResultSet result = statement.executeQuery()) {
			while (result.next()) {
				values.put(result.getString(1), Long.valueOf(result.getLong(2)));
			}
		} catch (SQLException ex) {
			log.error("Unable to read the sequence table for the sequence name migration.", ex);
			return null;
		}
		return values;
	}

	private boolean deleteRows(Log log, PooledConnection connection, String table, String idColumn,
			List<String> obsolete) {
		String delete = "DELETE FROM " + table + " WHERE " + idColumn + "=?";
		try (PreparedStatement statement = connection.prepareStatement(delete)) {
			for (String id : obsolete) {
				statement.setString(1, id);
				statement.addBatch();
			}
			statement.executeBatch();
			return true;
		} catch (SQLException ex) {
			log.error("Unable to remove legacy sequence rows during the sequence name migration.", ex);
			return false;
		}
	}

	private boolean writeRows(Log log, PooledConnection connection, String table, String idColumn, String valueColumn,
			Map<String, Long> existing, Map<String, Long> targets) {
		String update = "UPDATE " + table + " SET " + valueColumn + "=? WHERE " + idColumn + "=?";
		String insert = "INSERT INTO " + table + " (" + idColumn + ", " + valueColumn + ") VALUES (?, ?)";
		try (PreparedStatement updateStatement = connection.prepareStatement(update);
				PreparedStatement insertStatement = connection.prepareStatement(insert)) {
			for (Map.Entry<String, Long> target : targets.entrySet()) {
				String newId = target.getKey();
				long value = target.getValue().longValue();
				if (existing.containsKey(newId)) {
					updateStatement.setLong(1, value);
					updateStatement.setString(2, newId);
					updateStatement.executeUpdate();
				} else {
					insertStatement.setString(1, newId);
					insertStatement.setLong(2, value);
					insertStatement.executeUpdate();
				}
			}
			return true;
		} catch (SQLException ex) {
			log.error("Unable to write migrated sequence rows during the sequence name migration.", ex);
			return false;
		}
	}

}
