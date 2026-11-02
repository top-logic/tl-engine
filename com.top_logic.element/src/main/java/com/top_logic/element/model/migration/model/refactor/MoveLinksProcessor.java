/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} moving reference links from one table to another.
 */
public class MoveLinksProcessor extends AbstractConfiguredInstance<MoveLinksProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link MoveLinksProcessor}.
	 */
	@TagName("move-links")
	public interface Config<I extends MoveLinksProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration name of {@link #getSourceTable()}.
		 */
		String SOURCE_TABLE = "source-table";

		/**
		 * Configuration name of {@link #getDestTable()}.
		 */
		String DEST_TABLE = "dest-table";

		/**
		 * Configuration name of {@link #getReferenceColumn()}.
		 */
		String REFERENCE_COLUMN = "reference-column";

		/**
		 * Configuration name of {@link #getReference()}.
		 */
		String REFERENCE = "reference";

		/**
		 * Name of the table to take the links from.
		 */
		@Name(SOURCE_TABLE)
		String getSourceTable();

		/**
		 * @see #getSourceTable()
		 */
		void setSourceTable(String value);

		/**
		 * Name of the table to move the links to.
		 */
		@Name(DEST_TABLE)
		String getDestTable();

		/**
		 * @see #getDestTable()
		 */
		void setDestTable(String value);

		/**
		 * Name of the reference column of the given {@link #getSourceTable()} that contains the
		 * reference associated with the link stored in a table row.
		 */
		@Name(REFERENCE_COLUMN)
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getReferenceColumn();

		/**
		 * The reference whose links should be moved.
		 */
		@Mandatory
		@Name(REFERENCE)
		QualifiedPartName getReference();

		/**
		 * @see #getReference()
		 */
		void setReference(QualifiedPartName value);
	}

	/**
	 * Creates a {@link MoveLinksProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MoveLinksProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String sourceTableName = getConfig().getSourceTable();
		String destTableName = getConfig().getDestTable();
		log.info(
			"Moving links for '" + getConfig().getReference().getName() + "' from table '" + sourceTableName + "' to '"
			+ destTableName + "'. ");

		MORepository repository = context.getPersistentRepository();
		MOStructure sourceTable = (MOStructure) repository.getMetaObject(sourceTableName);
		MOStructure destTable = (MOStructure) repository.getMetaObject(destTableName);

		Util util = context.getSQLUtils();
		try {
			TypePart reference = util.getTLTypePartOrFail(connection, getConfig().getReference());
			String referenceColumn = getConfig().getReferenceColumn();

			int cntCopy = copyLinks(connection, sourceTable, destTable, referenceColumn, reference.getDefinition());
			log.info("Copied " + cntCopy + " rows from table '" + sourceTableName + "' to '" + destTableName + "'.");

			int cntDelete = deleteLinks(connection, sourceTable, referenceColumn, reference.getDefinition());
			log.info("Deleted " + cntDelete + " rows from table '" + sourceTableName + ".");

			context.invalidateXRef(sourceTable);
			context.invalidateXRef(destTable);
		} catch (SQLException | MigrationException ex) {
			log.error(
				"Failed to move links of reference '" + getConfig().getReference().getName() + "' from '"
					+ sourceTableName + "' to '" + destTableName + "': " + ex.getMessage(),
				ex);
		}
	}

	/**
	 * Copies the links of a reference from one table to another.
	 *
	 * <p>
	 * The columns of the source table that the destination table also has are copied.
	 * </p>
	 *
	 * @param sourceTable
	 *        The table to copy the links from.
	 * @param destTable
	 *        The table to copy the links to.
	 * @param referenceColumn
	 *        The name of the reference attribute of the source table that holds the reference of
	 *        a link.
	 * @param reference
	 *        The identifier of the (definition of the) reference whose links are copied.
	 * @return The number of copied links.
	 */
	public static int copyLinks(PooledConnection connection, MOStructure sourceTable, MOStructure destTable,
			String referenceColumn, TLID reference) throws SQLException {
		List<String> columns = new ArrayList<>();
		List<SQLColumnDefinition> columnDefs = new ArrayList<>();
		for (MOAttribute attr : sourceTable.getAttributes()) {
			if (destTable.getAttributeOrNull(attr.getName()) == null) {
				continue;
			}

			for (DBAttribute column : attr.getDbMapping()) {
				columns.add(column.getDBName());
				columnDefs.add(columnDef(column.getDBName()));
			}
		}

		CompiledStatement copy = query(
			insert(
				table(destTable.getDBMapping().getDBName()),
				columns,
				select(
					columnDefs,
					table(sourceTable.getDBMapping().getDBName()),
					hasReference(sourceTable, referenceColumn, reference))))
						.toSql(connection.getSQLDialect());

		return copy.executeUpdate(connection);
	}

	/**
	 * Deletes the links of a reference from a table.
	 *
	 * @param table
	 *        The table to delete the links from.
	 * @param referenceColumn
	 *        The name of the reference attribute of the table that holds the reference of a link.
	 * @param reference
	 *        The identifier of the (definition of the) reference whose links are deleted.
	 * @return The number of deleted links.
	 */
	public static int deleteLinks(PooledConnection connection, MOStructure table, String referenceColumn,
			TLID reference) throws SQLException {
		CompiledStatement delete = query(
			delete(
				table(table.getDBMapping().getDBName()),
				hasReference(table, referenceColumn, reference)))
					.toSql(connection.getSQLDialect());

		return delete.executeUpdate(connection);
	}

	private static SQLExpression hasReference(MOStructure table, String referenceColumn, TLID reference) {
		MOReference metaRef = (MOReference) table.getAttribute(referenceColumn);
		return eqSQL(column(metaRef.getColumn(ReferencePart.name).getDBName()), literal(DBType.ID, reference));
	}

}
