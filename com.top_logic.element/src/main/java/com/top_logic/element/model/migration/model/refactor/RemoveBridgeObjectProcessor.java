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
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} that makes logical links represented by a bridge object between source
 * and target objects direct links.
 * 
 * <p>
 * In the following situation, where a `Src1` object is linked through a `owner` Reference to a
 * `Bridge1` object that itself is linked through a `child` reference to multiple `Dest` objects,
 * this processor creates direct links between `Src1` and `Dest1`..`Dest2` objects.
 * </p>
 * 
 * <pre>
 *                                  +--> Dest1
 * Src1 --owner--> Bridge1 --child--|
 *                                  +--> Dest2
 * </pre>
 * 
 * <p>
 * Note: This migration step creates new links allowing to delete the bridge object (and its links)
 * later on. The actual deletion must be done in an explicit step later on.
 * </p>
 */
public class RemoveBridgeObjectProcessor extends AbstractConfiguredInstance<RemoveBridgeObjectProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link RemoveBridgeObjectProcessor}.
	 */
	@TagName("remove-bridge-object")
	public interface Config<I extends RemoveBridgeObjectProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * The reference linking source objects to bridge objects.
		 */
		@Mandatory
		@Name("source-reference")
		QualifiedPartName getSourceReference();

		/**
		 * The association table storing source reference links.
		 */
		@Name("source-association-table")
		@StringDefault(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION)
		String getSourceAssociationTable();

		/**
		 * The column containing the reference in the {@link #getSourceAssociationTable()}.
		 */
		@Name("source-reference-column")
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getSourceReferenceColumn();

		/**
		 * The column containing the foreign key in {@link #getSourceAssociationTable()} pointing to
		 * the new source of the direct reference (the owner of the objects pointed to by the bridge
		 * object).
		 */
		@Name("source-owner-column")
		@StringDefault(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)
		String getSourceOwnerColumn();

		/**
		 * The column containing the foreign key pointing to the bridge object in the
		 * {@link #getSourceAssociationTable()}.
		 */
		@Name("source-bridge-column")
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		String getSourceBridgeColumn();

		/**
		 * The name of the table containing the bridge objects.
		 */
		@Name("bridge-table")
		@StringDefault(TLAnnotations.GENERIC_TABLE_NAME)
		String getBridgeTable();

		/**
		 * The bridge object type.
		 */
		@Mandatory
		@Name("bridge-type")
		QualifiedTypeName getBridgeType();

		/**
		 * The reference linking source objects to bridge objects.
		 */
		@Mandatory
		@Name("dest-reference")
		QualifiedPartName getDestReference();

		/**
		 * The association table storing source reference links.
		 */
		@Name("dest-association-table")
		@StringDefault(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION)
		String getDestAssociationTable();

		/**
		 * The column containing the reference in the {@link #getSourceAssociationTable()}.
		 */
		@Name("dest-reference-column")
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getDestReferenceColumn();

		/**
		 * The column containing the foreign key pointing to the bridge object in the
		 * {@link #getDestAssociationTable()}.
		 */
		@Name("dest-bridge-column")
		@StringDefault(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)
		String getDestBridgeColumn();

		/**
		 * The column containing the foreign key in {@link #getDestAssociationTable()} pointing to
		 * the new values of the direct reference (the objects pointed to by the bridge object).
		 */
		@Name("dest-value-column")
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		String getDestValueColumn();

		/**
		 * The association table that should receive the new direct links.
		 */
		@Name("direct-association-table")
		@StringDefault(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION)
		String getDirectAssociationTable();

		/**
		 * The direct model reference that should be synthesized.
		 * 
		 * <p>
		 * After the migration, the direct reference connects the source objects directly with the
		 * destination objects without the intermitting bridge objects.
		 * </p>
		 */
		@Mandatory
		@Name("direct-reference")
		QualifiedPartName getDirectReference();

		/**
		 * The column containing the reference in the {@link #getDirectAssociationTable()}.
		 */
		@Name("direct-reference-column")
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getDirectReferenceColumn();

		/**
		 * The foreign key column pointing to the source object in the direct association table.
		 */
		@Name("direct-source-key-column")
		@StringDefault(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)
		String getDirectSourceKeyColumn();

		/**
		 * The foreign key column pointing to the destination object in the direct association
		 * table.
		 */
		@Name("direct-dest-key-column")
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		String getDirectDestKeyColumn();

	}

	/**
	 * Creates a {@link RemoveBridgeObjectProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RemoveBridgeObjectProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();
		Config<?> config = getConfig();
		try {
			MORepository repository = context.getPersistentRepository();

			MOClass sourceTable = (MOClass) repository.getType(config.getSourceAssociationTable());
			MOReference sourceMetaRef = (MOReference) sourceTable.getAttribute(config.getSourceReferenceColumn());
			MOReference sourceOwner = (MOReference) sourceTable.getAttribute(config.getSourceOwnerColumn());
			MOReference sourceBridge = (MOReference) sourceTable.getAttribute(config.getSourceBridgeColumn());
			TypePart sourceRef = util.getTLTypePartOrFail(connection, config.getSourceReference());

			MOClass destTable = (MOClass) repository.getType(config.getDestAssociationTable());
			MOReference destMetaRef = (MOReference) destTable.getAttribute(config.getDestReferenceColumn());
			MOReference destBridge = (MOReference) destTable.getAttribute(config.getDestBridgeColumn());
			MOReference destValue = (MOReference) destTable.getAttribute(config.getDestValueColumn());
			TypePart destRef = util.getTLTypePartOrFail(connection, config.getDestReference());

			MOClass bridgeTable = (MOClass) repository.getType(config.getBridgeTable());
			MOReference bridgeTypeKey = (MOReference) bridgeTable.getAttribute(PersistentObject.T_TYPE_ATTR);
			Type bridgeType = util.getTLTypeOrFail(connection, config.getBridgeType());

			MOClass directTable = (MOClass) repository.getType(config.getDirectAssociationTable());
			MOReference directMetaRef = (MOReference) destTable.getAttribute(config.getDirectReferenceColumn());
			MOReference directSrc = (MOReference) destTable.getAttribute(config.getDirectSourceKeyColumn());
			MOReference directDst = (MOReference) destTable.getAttribute(config.getDirectDestKeyColumn());
			TypePart directRef = util.getTLTypePartOrFail(connection, config.getDirectReference());

			// Table aliases.
			String dst = "d";
			String bridge = "b";
			String src = "s";

			// Columns to copy from dest table to direct table.
			List<String> insertColumns = new ArrayList<>();
			List<SQLColumnDefinition> selectColumns = new ArrayList<>();
			List<DBType> selectTypes = new ArrayList<>();

			List<SQLExpression> insertValues = new ArrayList<>();
			List<Parameter> insertParameters = new ArrayList<>();

			// Make sure, the synthesized ID parameter is first in the parameter list.
			insertParameters.add(parameterDef(DBType.ID, BasicTypes.IDENTIFIER_DB_NAME));

			for (MOAttribute attr : destTable.getAttributes()) {
				if (attr.getName().equals(config.getDestBridgeColumn())) {
					// Fill source column of direct link with source key.
					for (int part = 0; part < sourceOwner.getDbMapping().length; part++) {
						DBAttribute fromColumn = sourceOwner.getDbMapping()[part];
						DBAttribute toColumn = directSrc.getDbMapping()[part];
						insertColumns.add(toColumn.getDBName());
						selectColumns.add(columnDef(
							fromColumn.getDBName(),
							src,
							fromColumn.getDBName()));

						DBType columnType = toColumn.getSQLType();
						insertParameters.add(parameterDef(columnType, toColumn.getDBName()));
						insertValues.add(parameter(columnType, toColumn.getDBName()));
						selectTypes.add(columnType);
					}
				} else if (attr.getName().equals(config.getDestValueColumn())) {
					// Fill destination column of direct link with original values.
					for (int part = 0; part < directDst.getDbMapping().length; part++) {
						DBAttribute fromColumn = destValue.getDbMapping()[part];
						DBAttribute toColumn = directDst.getDbMapping()[part];

						insertColumns.add(toColumn.getDBName());
						selectColumns.add(columnDef(
							fromColumn.getDBName(),
							dst,
							fromColumn.getDBName()));

						DBType columnType = toColumn.getSQLType();
						insertParameters.add(parameterDef(columnType, toColumn.getDBName()));
						insertValues.add(parameter(columnType, toColumn.getDBName()));
						selectTypes.add(columnType);
					}
				} else if (attr.getName().equals(config.getDestReferenceColumn())) {
					// Fill reference of direct link with direct reference.
					insertColumns.add(directMetaRef.getColumn(ReferencePart.name).getDBName());
					insertValues.add(literal(DBType.ID, directRef.getDefinition()));
				} else if (attr.getName().equals(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME)) {
					// Cannot copy, must synthesize.
					insertColumns.add(BasicTypes.IDENTIFIER_DB_NAME);
					insertValues.add(parameter(DBType.ID, BasicTypes.IDENTIFIER_DB_NAME));
				} else if (directTable.getAttributeOrNull(attr.getName()) == null) {
					// Not defined in target table.
				} else {
					// All other columns are taken from the existing dest association table.
					for (DBAttribute column : attr.getDbMapping()) {
						insertColumns.add(column.getDBName());
						selectColumns.add(columnDef(
							column.getDBName(),
							dst,
							column.getDBName()));

						DBType columnType = column.getSQLType();
						insertParameters.add(parameterDef(columnType, column.getDBName()));
						insertValues.add(parameter(columnType, column.getDBName()));
						selectTypes.add(columnType);
					}
				}
			}

			boolean hasBranches = util.hasBranches();
			DBHelper sqlDialect = connection.getSQLDialect();
			CompiledStatement insert = query(insertParameters,
				insert(
					table(directTable.getDBMapping().getDBName()),
					insertColumns,
					insertValues)).toSql(sqlDialect);

			CompiledStatement select = query(
				select(
					selectColumns,
					innerJoin(
						innerJoin(
							table(destTable.getDBMapping().getDBName(), dst),
							table(sourceTable.getDBMapping().getDBName(), src),
							and(
								hasBranches ? eqSQL(util.branchColumnRef(dst), util.branchColumnRef(src))
									: literalTrueLogical(),
								eqSQL(
									column(src, sourceMetaRef.getColumn(ReferencePart.name).getDBName()),
									literal(DBType.ID, sourceRef.getDefinition())),
								eqSQL(
									column(dst, destBridge.getColumn(ReferencePart.name).getDBName()),
									column(src, sourceBridge.getColumn(ReferencePart.name).getDBName())),
								le(column(dst, BasicTypes.REV_MAX_DB_NAME),
									column(src, BasicTypes.REV_MAX_DB_NAME)),
								ge(column(dst, BasicTypes.REV_MAX_DB_NAME),
									column(src, BasicTypes.REV_MIN_DB_NAME)))),
						table(bridgeTable.getDBMapping().getDBName(), bridge),
						and(
							hasBranches ? eqSQL(util.branchColumnRef(dst), util.branchColumnRef(bridge))
								: literalTrueLogical(),
							eqSQL(
								column(bridge, bridgeTypeKey.getColumn(ReferencePart.name).getDBName()),
								literal(DBType.ID, bridgeType.getID())),
							eqSQL(
								column(dst, destBridge.getColumn(ReferencePart.name).getDBName()),
								column(bridge, BasicTypes.IDENTIFIER_DB_NAME)),
							le(column(dst, BasicTypes.REV_MAX_DB_NAME), column(bridge, BasicTypes.REV_MAX_DB_NAME)),
							ge(column(dst, BasicTypes.REV_MAX_DB_NAME), column(bridge, BasicTypes.REV_MIN_DB_NAME))
						)
					),
					eqSQL(
						column(dst, destMetaRef.getColumn(ReferencePart.name).getDBName()),
						literal(DBType.ID, destRef.getDefinition())
					)
				)).toSql(sqlDialect);

			log.info("Synthesizing direct references '" + config.getDirectReference().getName()
				+ "' in table '" + directTable.getName() + "'.");

			// A second connection is needed, ensure a consistent view of both connections.
			connection.commit();

			long start = System.nanoTime();
			int cnt = 0;
			int batchSize = 0;

			int selectSize = selectColumns.size();
			int batchParameters = selectSize + 1;
			Object[] values = new Object[batchParameters];
			int maxBatchSize = sqlDialect.getMaxBatchSize(batchParameters);

			select.setFetchSize(1000);

			ConnectionPool pool = connection.getPool();
			PooledConnection seond = pool.borrowWriteConnection();
			try {
				try (ResultSet result = select.executeQuery(seond)) {
					try (Batch batch = insert.createBatch(connection)) {
						while (result.next()) {
							int pos = 0;
							values[pos++] = util.newID(connection);
							for (int n = 0; n < selectSize; n++) {
								values[pos++] = sqlDialect.mapToJava(result, n + 1, selectTypes.get(n));
							}

							batch.addBatch(values);

							if (++batchSize >= maxBatchSize) {
								long now = System.nanoTime();
								if (now - start > 1000_000_000L) {
									log.info("Created " + cnt + " links while synthesizing direct references '"
										+ config.getDirectReference().getName() + "' in table '" + directTable.getName()
										+ "'.");
									start = now;
								}

								cnt += batchSize;
								batchSize = 0;
								batch.executeBatch();
							}
						}

						if (batchSize > 0) {
							cnt += batchSize;
							batchSize = 0;

							batch.executeBatch();
						}
					}
				}
			} finally {
				pool.releaseWriteConnection(seond);
			}

			log.info("Synthesized " + cnt + " direct references '" + config.getDirectReference().getName()
				+ "' in table '" + directTable.getName() + "'.");
		} catch (MigrationException | SQLException ex) {
			log.error("Failed to synthesize direct references '" + config.getDirectReference().getName()
				+ "' in table '" + config.getDirectAssociationTable() + "': "
				+ ex.getMessage(), ex);
		}
	}
}
