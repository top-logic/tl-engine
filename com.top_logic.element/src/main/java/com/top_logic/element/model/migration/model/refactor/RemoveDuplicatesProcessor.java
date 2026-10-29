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
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.TableSelection;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} removing duplicate assignments of an object in the same association.
 *
 * <p>
 * A link is a duplicate, if an earlier link of the same table assigns the same value to the same
 * owner (or to any owner, if {@link Config#getGloballyUnique() globally unique}) in the same
 * attribute (given by the {@link Config#getReferenceColumn() reference column}, if the table has
 * one) during an overlapping range of revisions. If the value end of the table references a
 * specific revision of its value (historic and mixed references), only links to the same revision
 * of the value are duplicates. The history of each value is walked in the order of the start
 * revisions: A duplicate whose life time is covered completely by earlier links is deleted, a
 * duplicate that lives longer gets the revision after the end of the earlier links as start
 * revision. Each cleaned link is reported with its owner, attribute and value.
 * </p>
 *
 * <p>
 * The processor works on the tables of the {@link TableSelection}. If the selection is empty, it
 * works on the {@link Config#getAssociationTable() association table}.
 * </p>
 */
public class RemoveDuplicatesProcessor extends AbstractConfiguredInstance<RemoveDuplicatesProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link RemoveDuplicatesProcessor}.
	 */
	@TagName("remove-duplicates")
	public interface Config<I extends RemoveDuplicatesProcessor> extends PolymorphicConfiguration<I>, TableSelection {

		/**
		 * Configuration name of {@link #getAssociationTable()}.
		 */
		String ASSOCIATION_TABLE = "association-table";

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
		 * Configuration name of {@link #getTargetEnd()}.
		 */
		String TARGET_END = "target-end";

		/**
		 * Configuration name of {@link #getGloballyUnique()}.
		 */
		String GLOBALLY_UNIQUE = "globally-unique";

		/**
		 * The name of the association table that stores the links.
		 *
		 * <p>
		 * Only relevant, if neither {@link #getTables()} nor {@link #getSubtypesOf()} is given.
		 * </p>
		 */
		@Name(ASSOCIATION_TABLE)
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
		@Name(SOURCE_TABLE)
		@Nullable
		String getSourceTable();

		/**
		 * Filter restricting considered links to those reaching objects stored in the table with
		 * the given name.
		 */
		@Name(DEST_TABLE)
		@Nullable
		String getDestTable();

		/**
		 * Name of the reference column in the selected tables that contains the reference
		 * associated with the link stored in a table row.
		 * 
		 * <p>
		 * Links with the same value are only duplicates, if they have the same reference. A table
		 * without this column is cleaned regardless of the reference, unless
		 * {@link #getReference()} is given.
		 * </p>
		 */
		@Name(REFERENCE_COLUMN)
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getReferenceColumn();

		/**
		 * The reference from which duplicates should be removed.
		 * 
		 * <p>
		 * If not given, all links of the selected tables are considered.
		 * </p>
		 */
		@Nullable
		@Name(REFERENCE)
		QualifiedPartName getReference();

		/**
		 * Name of the association end that points to the side from which duplicate assignments are
		 * removed.
		 */
		@Name(TARGET_END)
		@NonNullable
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		String getTargetEnd();

		/**
		 * Whether the same value must be assigned only once in the same reference regardless of its
		 * owner.
		 */
		@Name(GLOBALLY_UNIQUE)
		boolean getGloballyUnique();
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
		MORepository repository = context.getPersistentRepository();
		List<MOClass> tables;
		if (TableSelection.isEmpty(getConfig())) {
			String associationTableName = getConfig().getAssociationTable();
			MetaObject associationTable = repository.getTypeOrNull(associationTableName);
			if (!(associationTable instanceof MOClass)) {
				log.error("Association table '" + associationTableName + "' does not exist.");
				return;
			}
			tables = List.of((MOClass) associationTable);
		} else {
			tables = TableSelection.resolve(log, getConfig(), repository);
		}

		Util util = context.getSQLUtils();
		TypePart reference;
		QualifiedPartName referenceName = getConfig().getReference();
		try {
			reference = referenceName == null ? null : util.getTLTypePartOrFail(connection, referenceName);
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to resolve reference '" + referenceName.getName() + "': " + ex.getMessage(), ex);
			return;
		}

		for (MOClass table : tables) {
			removeDuplicates(context, log, connection, table, reference);
		}
	}

	private void removeDuplicates(MigrationContext context, Log log, PooledConnection connection,
			MOClass associationTable, TypePart reference) {
		String associationTableName = associationTable.getName();
		String sourceTableName = getConfig().getSourceTable();
		String destTableName = getConfig().getDestTable();

		log.info(
			"Removing duplicate assignments"
				+ (reference == null ? "" : " of '" + getConfig().getReference().getName() + "'")
				+ " in '" + associationTableName + "'.");

		MOReference srcEnd = (MOReference) associationTable.getAttribute(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
		MOReference dstEnd = (MOReference) associationTable.getAttribute(DBKnowledgeAssociation.REFERENCE_DEST_NAME);
		MOAttribute metaAttribute = associationTable.getAttributeOrNull(getConfig().getReferenceColumn());
		MOReference metaRef = metaAttribute instanceof MOReference ref ? ref : null;

		Util util = context.getSQLUtils();
		try {
			SQLExpression condition = literalTrueLogical();

			if (reference != null) {
				if (metaRef == null) {
					log.error("Table '" + associationTableName + "' has no reference column '"
						+ getConfig().getReferenceColumn() + "', duplicates not removed.");
					return;
				}
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

			MOReference valueEnd = (MOReference) associationTable.getAttribute(getConfig().getTargetEnd());
			MOReference ownerEnd = valueEnd == dstEnd ? srcEnd : dstEnd;
			DBAttribute valueRevColumn = valueEnd.getColumn(ReferencePart.revision);

			boolean globallyUnique = getConfig().getGloballyUnique();

			/* Note: util().branchColumnDef() may return constant column. It is important (at least
			 * in H2) that no constant column is contained in the result list, otherwise the row can
			 * not be updated, even if the branch column is not updated at all. */
			SQLColumnDefinition branchColumnDef = util.branchColumnDefOrNull();
			List<SQLColumnDefinition> columnDefs = new ArrayList<>();
			int branchCol = addColumn(columnDefs, branchColumnDef);
			addColumn(columnDefs, columnDef(BasicTypes.IDENTIFIER_DB_NAME));
			int revMinCol = addColumn(columnDefs, columnDef(BasicTypes.REV_MIN_DB_NAME));
			int revMaxCol = addColumn(columnDefs, columnDef(BasicTypes.REV_MAX_DB_NAME));
			int ownerCol = addColumn(columnDefs, columnDef(ownerEnd.getColumn(ReferencePart.name).getDBName()));
			int valueCol = addColumn(columnDefs, columnDef(valueEnd.getColumn(ReferencePart.name).getDBName()));
			int attributeCol = metaRef == null ? 0
				: addColumn(columnDefs, columnDef(metaRef.getColumn(ReferencePart.name).getDBName()));
			int valueRevCol = valueRevColumn == null ? 0
				: addColumn(columnDefs, columnDef(valueRevColumn.getDBName()));

			CompiledStatement links = query(
				select(
					columnDefs,
					table(associationTable.getDBMapping().getDBName()),
					condition,
					Util.listWithoutNull(
						util.branchOrderOrNull(),
						globallyUnique ? null
							: order(column(ownerEnd.getColumn(ReferencePart.name).getDBName())),
						order(column(BasicTypes.REV_MIN_DB_NAME))))
				).toSql(connection.getSQLDialect());

			links.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

			// Map, where keys identify an assigned value and the value is the maximum revMax of its
			// assignments seen so far.
			Map<LinkKey, Long> values = new HashMap<>();

			long currentBranch = 0;
			long currentOwner = 0;

			int updated = 0;
			int deleted = 0;
			try (ResultSet resultSet = links.executeQuery(connection)) {
				while (resultSet.next()) {
					long branch = branchCol > 0 ? resultSet.getLong(branchCol) : TLContext.TRUNK_ID;
					long owner = resultSet.getLong(ownerCol);
					long value = resultSet.getLong(valueCol);
					long attribute = attributeCol > 0 ? resultSet.getLong(attributeCol) : 0;
					long valueRev = valueRevCol > 0 ? resultSet.getLong(valueRevCol) : 0;
					long revMin = resultSet.getLong(revMinCol);
					long revMax = resultSet.getLong(revMaxCol);

					if (currentBranch != branch || (!globallyUnique && currentOwner != owner)) {
						values.clear();

						currentBranch = branch;
						currentOwner = owner;
					}

					LinkKey key = new LinkKey(attribute, value, valueRev);
					Long oldRevMaxValue = values.get(key);
					if (oldRevMaxValue == null || oldRevMaxValue.longValue() < revMin) {
						// No overlap with an earlier assignment.
						values.put(key, Long.valueOf(revMax));
						continue;
					}

					String assignment = "value " + value + (valueRevCol > 0 ? "@" + valueRev : "")
						+ " of attribute " + attribute + " to owner " + owner + " in '" + associationTableName + "'";
					long oldRevMax = oldRevMaxValue.longValue();
					if (oldRevMax >= revMax) {
						// The assignment is completely covered by an earlier assignment, delete.
						resultSet.deleteRow();
						deleted++;

						log.info("Deleted assignment of " + assignment + " in time range [" + revMin + ", " + revMax
							+ "].");
					} else {
						// The assignment starts during an earlier assignment, cut its start.
						long newRevMin = oldRevMax + 1;
						resultSet.updateLong(revMinCol, newRevMin);
						resultSet.updateRow();
						updated++;
						values.put(key, Long.valueOf(revMax));

						log.info("Updated assignment of " + assignment + " from time range [" + revMin + ", " + revMax
							+ "] to [" + newRevMin + ", " + revMax + "].");
					}
				}
			}

			if (deleted > 0 || updated > 0) {
				context.invalidateXRef(associationTable);
			}
			log.info("Deleted " + deleted + " assignments and adjusted " + updated + " assignments in '"
				+ associationTableName + "'.");
		} catch (SQLException ex) {
			log.error(
				"Failed to remove duplicates from '" + associationTableName + "': " + ex.getMessage(), ex);
		}
	}

	/**
	 * Adds the given column to the given list, if it is not <code>null</code>.
	 *
	 * @return The (1-based) index of the column in the result set, or 0, if the column was not
	 *         added.
	 */
	private static int addColumn(List<SQLColumnDefinition> columnDefs, SQLColumnDefinition column) {
		if (column == null) {
			return 0;
		}
		columnDefs.add(column);
		return columnDefs.size();
	}

	/**
	 * Identification of a value assigned to an owner: A value is a duplicate only within the same
	 * attribute, and values in different revisions are different.
	 */
	private record LinkKey(long attribute, long value, long valueRev) {
		// Pure value.
	}

}
