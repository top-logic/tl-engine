/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} moving composition links from a source to a target table.
 * 
 * @see InlineCompositionLinks
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMoveCompositionLinks<C extends AbstractMoveCompositionLinks.Config<?>>
		extends AbstractConfiguredInstance<C>
		implements MigrationProcessor {

	/**
	 * Configuration of {@link AbstractMoveCompositionLinks}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends AbstractMoveCompositionLinks<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Name of the composition {@link TLReference} whose values must be moved.
		 */
		@Mandatory
		QualifiedPartName getReference();

		/**
		 * Name of the {@link MOClass} which contains the links to move.
		 */
		@StringDefault(ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION)
		String getSourceTable();

	}

	/**
	 * Creates a new {@link AbstractMoveCompositionLinks} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractMoveCompositionLinks}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public AbstractMoveCompositionLinks(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
	}

	private Util _util;

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.getSQLUtils();
			migrateData(log, connection);
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to migrate composition references.", ex);
		}
	}

	/**
	 * {@link Util} instance for this {@link MigrationProcessor}.
	 */
	protected Util util() {
		return _util;
	}

	/**
	 * Migrates the actual data.
	 */
	protected void migrateData(Log log, PooledConnection connection) throws SQLException, MigrationException {
		TypePart composition = _util.getTLTypePartOrFail(connection, getConfig().getReference());
		Set<TLID> specializations = _util.getTransitiveSpecializations(connection, composition.getOwner());
		DBHelper sql = connection.getSQLDialect();

		CompiledStatement sourceTablesQ = query(
			parameters(
				parameterDef(DBType.ID, "reference")),
			selectDistinct(
				columns(
					columnDef(Util.refType(SourceReference.REFERENCE_SOURCE_NAME))),
				table(SQLH.mangleDBName(getConfig().getSourceTable())),
				and(
					eqSQL(
						column(Util.refID(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)),
						parameter(DBType.ID, "reference"))))).toSql(sql);

		Collection<String> tables = new HashSet<>();
		try (ResultSet sourceTable = sourceTablesQ.executeQuery(connection, composition.getDefinition())) {
			while (sourceTable.next()) {
				tables.add(sourceTable.getString(1));
			}
		}
		if (tables.isEmpty()) {
			return;
		}

		/* Mapping branch -> table -> (elements which have a specialization type) */
		Map<Long, Map<String, Set<TLID>>> sources = new HashMap<>();
		for (String sourceTable : tables) {
			CompiledStatement sourceElementsDataQ = query(
				parameters(
					setParameterDef("tTypes", DBType.ID)),
				selectDistinct(
					columns(
						_util.branchColumnDef(),
						columnDef(BasicTypes.IDENTIFIER_DB_NAME)),
					table(SQLH.mangleDBName(sourceTable)),
					and(
						inSet(
							column(Util.refID(TLObject.T_TYPE_ATTR)),
							setParameter("tTypes", DBType.ID))))).toSql(sql);
			try (ResultSet sourceElements = sourceElementsDataQ.executeQuery(connection, specializations)) {
				while (sourceElements.next()) {
					long branch = sourceElements.getLong(1);
					Map<String, Set<TLID>> idsByTable = sources.get(branch);
					if (idsByTable == null) {
						idsByTable = new HashMap<>();
						sources.put(branch, idsByTable);
					}
					Set<TLID> ids = idsByTable.get(sourceTable);
					if (ids == null) {
						ids = new HashSet<>();
						idsByTable.put(sourceTable, ids);
					}
					ids.add(LongID.valueOf(sourceElements.getLong(2)));
				}
			}
		}

		for (Entry<Long, Map<String, Set<TLID>>> e : sources.entrySet()) {
			Long branch = e.getKey();
			for (Entry<String, Set<TLID>> f : e.getValue().entrySet()) {
				String table = f.getKey();
				moveLinks(log, connection, branch, composition.getDefinition(), table, f.getValue());
			}
		}
	}

	/**
	 * Moves the links from the {@link Config#getSourceTable() source table} to the target table.
	 *
	 * @param refId
	 *        ID of the {@link TLReference#getDefinition() definition} of the composition reference.
	 * @param sourceTable
	 *        Name of the {@link MOClass table} containing the elements with the given ids.
	 * @param sourceElements
	 *        {@link TLID Local ids} of the elements which are sources of the links to move.
	 */
	protected abstract void moveLinks(Log log, PooledConnection connection, long branch, TLID refId, String sourceTable,
			Set<TLID> sourceElements) throws SQLException;
}
