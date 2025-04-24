/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.migration.Util;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} that replaces the ids of {@link TLStructuredTypePart} by the id of
 * their {@link TLStructuredTypePart#getDefinition() definition} in reference table.
 */
public class ChangeConcreteToDefinitionId extends AbstractConfiguredInstance<ChangeConcreteToDefinitionId.Config>
		implements MigrationProcessor {

	/**
	 * Typed configuration interface definition for {@link ChangeConcreteToDefinitionId}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ChangeConcreteToDefinitionId> {

		/**
		 * Name of the table to adapt.
		 */
		@Mandatory
		String getTable();

		/**
		 * Name of the {@link MOReference} that stores the {@link TLStructuredTypePart}.
		 */
		@Mandatory
		String getPartColumn();

		/**
		 * Table columns which identify the object.
		 */
		@Format(CommaSeparatedStrings.class)
		@Mandatory
		List<String> getIDColumns();

	}

	/**
	 * Create a {@link ChangeConcreteToDefinitionId}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ChangeConcreteToDefinitionId(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			tryMigrate(context, log, connection);
		} catch (SQLException ex) {
			log.error("Failed to update folder references: " + ex.getMessage(), ex);
		}
	}

	private void tryMigrate(MigrationContext context, Log log, PooledConnection connection)
			throws SQLException {
		log.info("Replace concrete part ids in table '" + getConfig().getTable() + "' by their definition ids.");

		DBHelper sqlDialect = connection.getSQLDialect();
		Util sqlUtils = context.getSQLUtils();

		MORepository repository = context.getPersistentRepository();
		MOClass table = (MOClass) repository.getType(getConfig().getTable());
		MOReference partCol = MetaObjectUtils.getReference(table, getConfig().getPartColumn());

		Set<TLID> allUsedPartIds = new HashSet<>();

		log.info("Fetch all used attribute ids.");
		CompiledStatement query = query(
			selectDistinct(
				columns(
					columnDef(partCol.getColumn(ReferencePart.name).getDBName())),
				table(table.getDBMapping().getDBName()))).toSql(sqlDialect);
		try (ResultSet result = query.executeQuery(connection)) {
			while (result.next()) {
				allUsedPartIds.add(IdentifierUtil.getId(result, 1));
			}
		}

		log.info("Fetch definition ids.");

		Map<TLID, Set<TLID>> usedOverriddenIds = new HashMap<>();
		Map<TLID, TLID> definitionIds = new HashMap<>();
		Iterator<List<TLID>> chunks = CollectionUtilShared.chunk(sqlDialect.getMaxSetSize(), allUsedPartIds.iterator());
		while (chunks.hasNext()) {
			List<TLID> chunk = chunks.next();

			query = query(selectDistinct(
				columns(
					columnDef(BasicTypes.IDENTIFIER_DB_NAME),
					columnDef(Util.refID(TLStructuredTypePart.DEFINITION_ATTR))),
				table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
				and(
					inSet(column(BasicTypes.IDENTIFIER_DB_NAME), chunk, DBType.ID),
					not(
						eqSQL(column(BasicTypes.IDENTIFIER_DB_NAME),
							column(Util.refID(TLStructuredTypePart.DEFINITION_ATTR)))))

			)).toSql(sqlDialect);

			try (ResultSet result = query.executeQuery(connection)) {
				while (result.next()) {
					TLID id = IdentifierUtil.getId(result, 1);
					TLID defId = IdentifierUtil.getId(result, 2);
					MultiMaps.add(usedOverriddenIds, defId, id);
					definitionIds.put(id, defId);
				}
			}
		}

		if (definitionIds.isEmpty()) {
			// Only definition attributes are found in table.
			log.info("No update necessary.");
			return;
		}

		// Add definition as its own definition.
		for (TLID definition : definitionIds.values().toArray(TLID[]::new)) {
			definitionIds.put(definition, definition);
		}

		int updatedRows = 0;
		chunks = CollectionUtilShared.chunk(sqlDialect.getMaxSetSize(), definitionIds.keySet().iterator());
		while (chunks.hasNext()) {
			List<TLID> chunk = chunks.next();

			List<SQLColumnDefinition> columns = new ArrayList<>();
			SQLColumnDefinition branchCol = sqlUtils.branchColumnDefOrNull();
			if (branchCol != null) {
				columns.add(branchCol);
			}
			columns.add(columnDef(BasicTypes.IDENTIFIER_DB_NAME));
			columns.add(columnDef(BasicTypes.REV_MAX_DB_NAME));
			columns.add(columnDef(BasicTypes.REV_MIN_DB_NAME));
			columns.add(columnDef(partCol.getColumn(ReferencePart.name).getDBName()));
			for (String idCol : getConfig().getIDColumns()) {
				columns.add(columnDef(idCol));
			}
			List<SQLOrder> orders = new ArrayList<>();
			orders.add(order(true, column(BasicTypes.REV_MIN_DB_NAME)));
			for (String idCol : getConfig().getIDColumns()) {
				orders.add(order(false, column(idCol)));
			}

			query = query(select(
				columns,
				table(table.getDBMapping().getDBName()),
				inSet(column(partCol.getColumn(ReferencePart.name).getDBName()), chunk, DBType.ID),
				orders).forUpdate()).toSql(sqlDialect);
			query.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

			Map<Object, Long> lastRows = new HashMap<>();
			try (ResultSet result = query.executeQuery(connection)) {
				while (result.next()) {
					boolean changed = false;

					int branchInc;
					long branch;
					if (branchCol != null) {
						branch = result.getLong(1);
						branchInc = 0;
					} else {
						branch = TLContext.TRUNK_ID;
						branchInc = -1;
					}
					long revMax = result.getLong(3 + branchInc);
					long revMin = result.getLong(4 + branchInc);
					TLID attributeID = IdentifierUtil.getId(result, 5 + branchInc);
					TLID definition = definitionIds.get(attributeID);
					List<Object> rowID = new ArrayList<>();
					rowID.add(branch);
					rowID.add(definition);
					int base = 6 + branchInc;
					for (int i = 0; i < getConfig().getIDColumns().size(); i++) {
						rowID.add(result.getObject(base + i));
					}

					if (!definition.equals(attributeID)) {
						IdentifierUtil.setId(result, 5 + branchInc, definition);
						changed = true;
					}

					Long lastRow = lastRows.get(rowID);
					if (lastRow == null) {
						lastRows.put(rowID, revMin);
					} else {
						assert revMin < lastRow;
						if (revMax >= lastRow) {
							result.updateLong(3 + branchInc, lastRow - 1);
							changed = true;
						}
						lastRows.put(rowID, revMin);
					}

					if (changed) {
						updatedRows++;
						result.updateRow();
					}

				}
			}
		}
		log.info("Updated " + updatedRows + " rows.");

	}

}
