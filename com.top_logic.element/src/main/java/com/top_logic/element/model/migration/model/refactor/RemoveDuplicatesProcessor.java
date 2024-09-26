/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} removing duplicate assignments of an object in the same association.
 */
public class RemoveDuplicatesProcessor extends AbstractConfiguredInstance<RemoveDuplicatesProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link RemoveDuplicatesProcessor}.
	 */
	@TagName("remove-duplicates")
	public interface Config<I extends RemoveDuplicatesProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * The name of the association table that stores the links.
		 */
		@NonNullable
		@StringDefault(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION)
		String getAssociationTable();

		/**
		 * @see #getAssociationTable()
		 */
		void setAssociationTable(String value);

		/**
		 * Filter restricting considered links to those originating from the table with the given
		 * name.
		 */
		@Name("source-table")
		@Nullable
		String getSourceTable();

		/**
		 * Filter restricting considered links to those reaching objects stored in the table with
		 * the given name.
		 */
		@Name("dest-table")
		@Nullable
		String getDestTable();

		/**
		 * Name of the reference column in the given {@link #getAssociationTable()} that contains
		 * the reference associated with the link stored in a table row.
		 * 
		 * <p>
		 * The value is only relevant, if {@link #getReference()} is given.
		 * </p>
		 */
		@Name("reference-column")
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getReferenceColumn();

		/**
		 * The reference from which duplicates should be removed.
		 * 
		 * <p>
		 * If not given, all links of the given {@link #getAssociationTable()} are considered.
		 * </p>
		 */
		@Nullable
		@Name("reference")
		QualifiedPartName getReference();

		/**
		 * Name of the association end that points to the side from which duplicate assignments are
		 * removed.
		 */
		@NonNullable
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		String getTargetEnd();
	}

	/**
	 * Creates a {@link RemoveDuplicatesProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RemoveDuplicatesProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String associationTableName = getConfig().getAssociationTable();
		String sourceTableName = getConfig().getSourceTable();
		String destTableName = getConfig().getDestTable();
		QualifiedPartName referenceName = getConfig().getReference();

		log.info("Removing duplicate assignments in '" + associationTableName + "'.");

		MORepository repository = context.getPersistentRepository();
		MOStructure associationTable = (MOStructure) repository.getMetaObject(associationTableName);

		MOReference srcEnd = (MOReference) associationTable.getAttribute(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		MOReference dstEnd = (MOReference) associationTable.getAttribute(DBKnowledgeAssociation.REFERENCE_DEST_NAME);

		Util util = context.getSQLUtils();
		try {
			TypePart reference = referenceName == null ? null : util.getTLTypePartOrFail(connection, referenceName);
			
			SQLExpression condition = literalTrueLogical();

			if (reference != null) {
				MOReference metaRef = (MOReference) associationTable.getAttribute(getConfig().getReferenceColumn());
				condition = and(condition,
					eqSQL(
						column(metaRef.getColumn(ReferencePart.name).getDBName()),
						literal(DBType.ID, reference.getDefinition())));
			}

			if (sourceTableName != null) {
				condition = and(condition,
					eqSQL(
						column(srcEnd.getColumn(ReferencePart.type).getDBName()),
						literal(DBType.STRING, sourceTableName)));
			}

			if (destTableName != null) {
				condition = and(condition,
					eqSQL(
						column(dstEnd.getColumn(ReferencePart.type).getDBName()),
						literal(DBType.STRING, destTableName)));
			}

			MOReference valuetEnd = (MOReference) associationTable.getAttribute(getConfig().getTargetEnd());
			MOReference ownerEnd = valuetEnd == dstEnd ? srcEnd : dstEnd;

			List<SQLColumnDefinition> columnDefs = columns(
				util.branchColumnDef(),
				columnDef(BasicTypes.IDENTIFIER_DB_NAME),
				columnDef(BasicTypes.REV_MIN_DB_NAME),
				columnDef(BasicTypes.REV_MAX_DB_NAME),
				columnDef(ownerEnd.getColumn(ReferencePart.name).getDBName()),
				columnDef(valuetEnd.getColumn(ReferencePart.name).getDBName()));

			CompiledStatement links = query(
				select(
					columnDefs,
					table(associationTable.getDBMapping().getDBName()),
					condition,
					Util.listWithoutNull(
						util.branchOrderOrNull(),
						order(false, column(ownerEnd.getColumn(ReferencePart.name).getDBName())),
						order(false, column(BasicTypes.REV_MIN_DB_NAME))))
				).toSql(connection.getSQLDialect());

			links.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

			// Map, where keys are value IDs and value is the revMax of the value assignment.
			Map<Long, Long> values = new HashMap<>();

			long currentBranch = 0;
			long currentOwner = 0;

			int updated = 0;
			int deleted = 0;
			try (ResultSet resultSet = links.executeQuery(connection)) {
				while (resultSet.next()) {
					long branch = resultSet.getLong(1);
					long owner = resultSet.getLong(5);
					long value = resultSet.getLong(6);
					long revMin = resultSet.getLong(3);
					long revMax = resultSet.getLong(4);

					if (currentBranch != branch || currentOwner != owner) {
						values.clear();

						currentBranch = branch;
						currentOwner = owner;
					}

					Long valueId = Long.valueOf(value);
					Long oldRevMaxValue = values.put(valueId, Long.valueOf(revMax));
					if (oldRevMaxValue != null) {
						// This could be a clash.
						long oldRevMax = oldRevMaxValue.longValue();
						if (oldRevMax >= revMin) {
							// The new assignment and the old assignment overlaps.
							if (oldRevMax >= revMax) {
								// The new assignment is completely covered by the old assignment,
								// delete.
								resultSet.deleteRow();
								deleted++;
							} else {
								// Update revMin of link.
								resultSet.updateLong(3, oldRevMax + 1);
								resultSet.updateRow();
								updated++;
							}
						}
					}
				}
			}

			log.info("Deleted " + deleted + " assignments and adjusted " + updated + " assignments in '"
				+ associationTableName + "'.");
		} catch (SQLException | MigrationException ex) {
			log.error(
				"Failed to remove duplicates from '" + associationTableName + "': " + ex.getMessage(), ex);
		}
	}

}
