/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} moving the links of all references with a given
 * {@link TLStructuredTypePart#isBag() bag} flag from one table to another.
 *
 * <p>
 * The references are read from the model stored in the database. For each configured
 * {@link Config#getTables() pair of tables}, the links of these references are moved from the
 * source to the destination table, e.g. from the tables that hold each value of a reference at
 * most once to the tables for bags. The number of moved links is reported per reference.
 * </p>
 *
 * @see MoveLinksProcessor
 * @see Util#getTLReferenceDefinitionsByBag(PooledConnection, boolean)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MoveLinksByBagProcessor extends AbstractConfiguredInstance<MoveLinksByBagProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link MoveLinksByBagProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends MoveLinksByBagProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * Tag name of {@link Config} in a migration script.
		 */
		String TAG_NAME = "move-links-by-bag";

		/**
		 * Configuration name of {@link #getTables()}.
		 */
		String TABLES = "tables";

		/**
		 * Configuration name of {@link #getBag()}.
		 */
		String BAG = "bag";

		/**
		 * Configuration name of {@link #getReferenceColumn()}.
		 */
		String REFERENCE_COLUMN = "reference-column";

		/**
		 * The tables to move the links between.
		 */
		@Name(TABLES)
		@EntryTag(TablePair.TAG_NAME)
		List<TablePair> getTables();

		/**
		 * Whether the links of the references that may hold a value more than once are moved.
		 *
		 * <p>
		 * If <code>false</code>, the links of the references that hold each value at most once are
		 * moved.
		 * </p>
		 */
		@Name(BAG)
		@BooleanDefault(true)
		boolean getBag();

		/**
		 * @see #getBag()
		 */
		void setBag(boolean value);

		/**
		 * Name of the reference column of the source tables that contains the reference
		 * associated with the link stored in a table row.
		 */
		@Name(REFERENCE_COLUMN)
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getReferenceColumn();
	}

	/**
	 * A table to move links from together with the table to move them to.
	 */
	public interface TablePair extends ConfigurationItem {

		/**
		 * Tag name of a {@link TablePair} in {@link Config#getTables()}.
		 */
		String TAG_NAME = "table";

		/**
		 * Configuration name of {@link #getSourceTable()}.
		 */
		String SOURCE_TABLE = "source-table";

		/**
		 * Configuration name of {@link #getDestTable()}.
		 */
		String DEST_TABLE = "dest-table";

		/**
		 * Name of the type of the table to take the links from.
		 */
		@Name(SOURCE_TABLE)
		@Mandatory
		String getSourceTable();

		/**
		 * @see #getSourceTable()
		 */
		void setSourceTable(String value);

		/**
		 * Name of the type of the table to move the links to.
		 */
		@Name(DEST_TABLE)
		@Mandatory
		String getDestTable();

		/**
		 * @see #getDestTable()
		 */
		void setDestTable(String value);
	}

	/**
	 * Creates a {@link MoveLinksByBagProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MoveLinksByBagProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		boolean bag = getConfig().getBag();
		String kind = bag ? "bag references" : "references that are no bags";

		Map<TLID, String> references;
		try {
			references = context.getSQLUtils().getTLReferenceDefinitionsByBag(connection, bag);
		} catch (SQLException ex) {
			log.error("Failed to fetch the " + kind + ": " + ex.getMessage(), ex);
			return;
		}
		log.info("Moving the links of " + references.size() + " " + kind + ".");

		MORepository repository = context.getPersistentRepository();
		for (TablePair tables : getConfig().getTables()) {
			MOClass sourceTable = table(log, repository, tables.getSourceTable());
			MOClass destTable = table(log, repository, tables.getDestTable());
			if (sourceTable == null || destTable == null) {
				continue;
			}
			moveLinks(context, log, connection, references, sourceTable, destTable);
		}
	}

	private void moveLinks(MigrationContext context, Log log, PooledConnection connection,
			Map<TLID, String> references, MOClass sourceTable, MOClass destTable) {
		String sourceName = sourceTable.getName();
		String destName = destTable.getName();
		String referenceColumn = getConfig().getReferenceColumn();
		if (sourceTable.getAttributeOrNull(referenceColumn) == null) {
			log.error("Table '" + sourceName + "' has no reference column '" + referenceColumn
				+ "', links not moved to '" + destName + "'.");
			return;
		}

		int total = 0;
		try {
			for (Entry<TLID, String> reference : references.entrySet()) {
				TLID id = reference.getKey();
				int copied = MoveLinksProcessor.copyLinks(connection, sourceTable, destTable, referenceColumn, id);
				if (copied == 0) {
					continue;
				}
				int deleted = MoveLinksProcessor.deleteLinks(connection, sourceTable, referenceColumn, id);
				if (deleted != copied) {
					log.error("Copied " + copied + " links of '" + reference.getValue() + "' from '" + sourceName
						+ "' to '" + destName + "', but deleted " + deleted + ".");
				}
				log.info(
					"Moved " + copied + " links of '" + reference.getValue() + "' from '" + sourceName + "' to '"
						+ destName + "'.");
				total += copied;
			}
		} catch (SQLException ex) {
			log.error("Failed to move links from '" + sourceName + "' to '" + destName + "': " + ex.getMessage(), ex);
		}

		if (total > 0) {
			context.invalidateXRef(sourceTable);
			context.invalidateXRef(destTable);
		}
		log.info("Moved " + total + " links from '" + sourceName + "' to '" + destName + "'.");
	}

	private static MOClass table(Log log, MORepository repository, String name) {
		MetaObject type = repository.getTypeOrNull(name);
		if (!(type instanceof MOClass)) {
			log.error("Table '" + name + "' does not exist.");
			return null;
		}
		return (MOClass) type;
	}

}
