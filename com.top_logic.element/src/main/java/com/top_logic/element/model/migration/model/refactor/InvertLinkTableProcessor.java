/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} swapping link directions in an association table.
 */
public class InvertLinkTableProcessor extends AbstractConfiguredInstance<InvertLinkTableProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link InvertLinkTableProcessor}.
	 */
	@TagName("invert-link-table")
	public interface Config<I extends InvertLinkTableProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the link table to invert links in.
		 */
		String getTable();

		/**
		 * @see #getTable()
		 */
		void setTable(String value);

		/**
		 * The name of the source end of the association.
		 */
		@StringDefault(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)
		String getSrc();

		/**
		 * The name of the destination end of the association.
		 */
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		String getDest();

		/**
		 * Name of the reference column of the given {@link #getTable()} that contains the reference
		 * associated with the link stored in a table row.
		 * 
		 * <p>
		 * The value is only relevant, if {@link #getReference()} is given.
		 * </p>
		 */
		@Name("reference-column")
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getReferenceColumn();

		/**
		 * The reference whose links should be inverted.
		 * 
		 * <p>
		 * If not given, all links of the given {@link #getTable()} are inverted.
		 * </p>
		 * 
		 * <p>
		 * Note: Inverting links is not useful by its own, but only as part of a complex model
		 * refactoring. Since the reference to which a link belongs is stored in the given
		 * {@link #getReferenceColumn()}, this reference most likely must also be exchanged later
		 * on.
		 * </p>
		 */
		@Name("reference")
		QualifiedPartName getReference();
	}

	/**
	 * Creates a {@link InvertLinkTableProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InvertLinkTableProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String tableName = getConfig().getTable();
		QualifiedPartName invertedReference = getConfig().getReference();

		log.info("Inverting links "
			+ (invertedReference == null ? "" : "of reference '" + invertedReference.getName() + "' ")
			+ "in table '" + tableName + "'. ");

		MORepository repository = context.getPersistentRepository();
		MOStructure table = (MOStructure) repository.getMetaObject(tableName);

		MOReference srcRef = (MOReference) table.getAttribute(getConfig().getSrc());
		MOReference destRef = (MOReference) table.getAttribute(getConfig().getDest());

		MOReference metaRef =
			invertedReference == null ? null : (MOReference) table.getAttribute(getConfig().getReferenceColumn());

		Util util = context.getSQLUtils();

		try {
			TypePart reference =
				invertedReference == null ? null : util.getTLTypePartOrFail(connection, invertedReference);

			int cnt = invertLinks(util, connection, table, srcRef, destRef,
				reference == null || metaRef == null ? 
					SQLFactory.literalTrueLogical() : 
					eqSQL(column(metaRef.getColumn(ReferencePart.name).getDBName()),
						literal(DBType.ID, reference.getDefinition())));
			log.info("Inverted " + cnt + " links in table '" + table.getName() + "'.");
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to invert links in table '" + table.getName() + "': " + ex.getMessage());
		}
	}

	/**
	 * Exchanges the source and destination columns of all rows of the given table matching the
	 * given condition.
	 * 
	 * <p>
	 * The rows are read and each row is written back with explicitly exchanged values. An
	 * <code>UPDATE</code> assigning the columns to each other is not dialect-neutral: MySQL
	 * evaluates the assignments of a single-table <code>UPDATE</code> from left to right and sees
	 * the values already assigned to the row, which would make both ends the old destination.
	 * </p>
	 * 
	 * @return The number of inverted links.
	 */
	private int invertLinks(Util sqlUtil, PooledConnection connection, MOStructure table, MOReference srcRef,
			MOReference destRef, SQLExpression condition) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		String tableName = table.getDBMapping().getDBName();

		DBAttribute srcTypeCol = srcRef.getColumn(ReferencePart.type);
		DBAttribute srcIdCol = srcRef.getColumn(ReferencePart.name);
		DBAttribute destTypeCol = destRef.getColumn(ReferencePart.type);
		DBAttribute destIdCol = destRef.getColumn(ReferencePart.name);

		CompiledStatement select = query(
			select(
				columns(
					sqlUtil.branchColumnDef(),
					columnDef(BasicTypes.IDENTIFIER_DB_NAME),
					columnDef(BasicTypes.REV_MIN_DB_NAME),
					columnDef(srcTypeCol.getDBName()),
					columnDef(srcIdCol.getDBName()),
					columnDef(destTypeCol.getDBName()),
					columnDef(destIdCol.getDBName())),
				table(tableName),
				condition)).toSql(sqlDialect);

		List<Object[]> rows = new ArrayList<>();
		try (ResultSet result = select.executeQuery(connection)) {
			while (result.next()) {
				rows.add(new Object[] {
					Long.valueOf(result.getLong(1)),
					IdentifierUtil.getId(result, 2),
					Long.valueOf(result.getLong(3)),
					sqlDialect.mapToJava(result, 4, srcTypeCol.getSQLType()),
					sqlDialect.mapToJava(result, 5, srcIdCol.getSQLType()),
					sqlDialect.mapToJava(result, 6, destTypeCol.getSQLType()),
					sqlDialect.mapToJava(result, 7, destIdCol.getSQLType()),
				});
			}
		}

		CompiledStatement update = query(Arrays.asList(
			sqlUtil.branchParamDef(),
			parameterDef(DBType.ID, "id"),
			parameterDef(DBType.LONG, "revMin"),
			parameterDef(srcTypeCol.getSQLType(), "srcType"),
			parameterDef(srcIdCol.getSQLType(), "srcId"),
			parameterDef(destTypeCol.getSQLType(), "destType"),
			parameterDef(destIdCol.getSQLType(), "destId")),
			update(table(tableName),
				and(
					sqlUtil.eqBranch(),
					eq(column(BasicTypes.IDENTIFIER_DB_NAME), parameter(DBType.ID, "id")),
					eq(column(BasicTypes.REV_MIN_DB_NAME), parameter(DBType.LONG, "revMin"))),
				columnNames(
					srcTypeCol.getDBName(),
					srcIdCol.getDBName(),
					destTypeCol.getDBName(),
					destIdCol.getDBName()),
				expressions(
					parameter(srcTypeCol.getSQLType(), "srcType"),
					parameter(srcIdCol.getSQLType(), "srcId"),
					parameter(destTypeCol.getSQLType(), "destType"),
					parameter(destIdCol.getSQLType(), "destId")))).toSql(sqlDialect);

		int batchSize = 0;
		int maxBatchSize = sqlDialect.getMaxBatchSize(7);
		int totalUpdates = 0;
		try (Batch batch = update.createBatch(connection)) {
			for (Object[] row : rows) {
				// Exchange the read source values (entries 3, 4) and destination values (entries 5, 6).
				batch.addBatch(row[0], row[1], row[2], row[5], row[6], row[3], row[4]);
				if (++batchSize >= maxBatchSize) {
					batch.executeBatch();
					totalUpdates += batchSize;
					batchSize = 0;
				}
			}
			if (batchSize > 0) {
				batch.executeBatch();
				totalUpdates += batchSize;
			}
		}
		return totalUpdates;
	}

}
