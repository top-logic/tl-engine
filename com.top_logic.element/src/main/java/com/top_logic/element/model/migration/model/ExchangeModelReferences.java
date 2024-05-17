/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.element.model.migration.model.ExchangeModelReferences.Config.AssociationUpdate;
import com.top_logic.element.model.migration.model.ExchangeModelReferences.Config.TableUpdate;
import com.top_logic.element.model.migration.model.ExchangeModelReferences.Config.UpdateTarget;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.BranchIdType;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} that exchanges references to model elements (such as enumeration
 * classifiers).
 * 
 * <p>
 * This processor can be used in combination with a type change of a reference attribute from enum A
 * to enum B to update the data in addition to the model update.
 * </p>
 */
public class ExchangeModelReferences extends AbstractConfiguredInstance<ExchangeModelReferences.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link ExchangeModelReferences}.
	 */
	@TagName("exchange-model-refs")
	public interface Config<I extends ExchangeModelReferences> extends PolymorphicConfiguration<I> {

		/**
		 * The tables to update.
		 */
		@DefaultContainer
		@Name("updates")
		List<UpdateTarget> getUpdates();

		/**
		 * Mapping of qualified names of model elements.
		 * 
		 * <p>
		 * The key is the qualified name of the original model element and the value is the
		 * qualified name of the model element that should be assigned instead.
		 * </p>
		 */
		@Name("model-refs")
		@MapBinding(tag = "model-ref", key = "source", attribute = "target")
		Map<String, String> getModelRefs();

		/**
		 * Base configuration for all update targets.
		 */
		@Abstract
		interface UpdateTarget extends ConfigurationItem {
			/**
			 * The name of the table, that defines the columns to be updated.
			 */
			@Mandatory
			@Name("table")
			String getTable();
		}

		/**
		 * Specification of a table to update.
		 */
		@TagName("table-update")
		interface TableUpdate extends UpdateTarget {
			/**
			 * The references attributes of the given table that potentially store model references
			 * to update.
			 */
			@Mandatory
			@Name("columns")
			@Format(CommaSeparatedStrings.class)
			List<String> getColumns();

			/**
			 * The type of objects stored in this table that should be changed.
			 */
			@Name("type")
			QualifiedTypeName getType();
		}

		/**
		 * Specification of an association table to update.
		 */
		@TagName("association-update")
		interface AssociationUpdate extends UpdateTarget {
			/**
			 * Name of the reference column of the given {@link #getTable()} that contains the
			 * reference associated with the link stored in a table row.
			 */
			@Nullable
			@Name("reference-column")
			@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
			String getReferenceColumn();

			/**
			 * The reference attribute of the association table that points to the value that should
			 * be mapped.
			 */
			@Name("value-column")
			@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
			String getValueColumn();

			/**
			 * The reference stored in this association table that potentially stores mapped model
			 * parts.
			 */
			@Name("reference")
			QualifiedPartName getReference();
		}
	}

	/**
	 * Creates a {@link ExchangeModelReferences} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExchangeModelReferences(InstantiationContext context, Config<?> config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.get(Util.PROPERTY);
		
		Config<?> config = getConfig();
		try {
			Map<TLID, TLID> modelMapping = new HashMap<>();
			for (Entry<String, String> mapping : config.getModelRefs().entrySet()) {
				String source = mapping.getKey();
				String target = mapping.getValue();

				BranchIdType sourceId = util.getModelPartOrFail(connection, TLContext.TRUNK_ID, source);
				BranchIdType targetId = util.getModelPartOrFail(connection, TLContext.TRUNK_ID, target);

				modelMapping.put(sourceId.getID(), targetId.getID());
			}

			TLID nullId = IdentifierUtil.nullIdForMandatoryDatabaseColumns();

			for (UpdateTarget update : config.getUpdates()) {
				String tableName = update.getTable();
				MOClass table = (MOClass) context.getSchemaRepository().getType(tableName);

				List<SQLColumnDefinition> columns = new ArrayList<>(Util.listWithoutNull(columns(
					util.branchColumnDefOrNull(),
					columnDef(BasicTypes.IDENTIFIER_DB_NAME),
					columnDef(BasicTypes.REV_MIN_DB_NAME),
					columnDef(BasicTypes.REV_MAX_DB_NAME))));

				int updates = 0;
				int valueIndex = util.getBranchIndexInc() + 4;
				if (update instanceof TableUpdate) {
					TableUpdate tableUpdate = (TableUpdate) update;

					for (String column : tableUpdate.getColumns()) {
						MOReference ref = (MOReference) table.getAttribute(column);

						columns.add(columnDef(
							ref.getColumn(ReferencePart.name).getDBName()));
					}

					MOReference tType = (MOReference) table.getAttribute(PersistentObject.T_TYPE_ATTR);
					String tTypeColumn = tType.getColumn(ReferencePart.name).getDBName();

					Type type = util.getTLTypeOrFail(connection, tableUpdate.getType());
					Collection<TLID> implIds = util.getImplementationIds(connection, type);

					CompiledStatement select = query(
						select(
							columns,
							table(table.getDBMapping().getDBName()),
							inSet(column(tTypeColumn), implIds, DBType.ID))).toSql(connection.getSQLDialect());

					select.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
					int valueColumns = tableUpdate.getColumns().size();

					try (ResultSet result = select.executeQuery(connection)) {
						while (result.next()) {
							// long objBranch = result.getLong(1);
							// long objId = result.getLong(2);
							// long revMin = result.getLong(3);
							// long revMax = result.getLong(4);

							boolean wasUpdated = false;
							for (int index = valueIndex,
									cnt = 0; cnt < valueColumns; index++, cnt++) {
								TLID id = IdentifierUtil.getId(result, index);
								if (id == null || id.equals(nullId)) {
									continue;
								}

								TLID newId = modelMapping.get(id);
								if (newId == null) {
									continue;
								}

								IdentifierUtil.setId(result, index, newId);
								wasUpdated = true;
								updates++;
							}

							if (wasUpdated) {
								result.updateRow();
							}
						}
					}
					log.info(
						"Updated " + updates + " model references in table '" + tableName + "'.");
				} else {
					AssociationUpdate associationUpdate = (AssociationUpdate) update;

					MOReference ref = (MOReference) table.getAttribute(associationUpdate.getReferenceColumn());

					MOReference value = (MOReference) table.getAttribute(associationUpdate.getValueColumn());
					columns.add(columnDef(
						value.getColumn(ReferencePart.name).getDBName()));

					TypePart refId = util.getTLTypePartOrFail(connection, associationUpdate.getReference());

					CompiledStatement select = query(
						select(
							columns,
							table(table.getDBMapping().getDBName()),
							eqSQL(column(ref.getColumn(ReferencePart.name).getDBName()),
								literal(DBType.ID, refId.getDefinition())))).toSql(connection.getSQLDialect());

					select.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
					try (ResultSet result = select.executeQuery(connection)) {
						while (result.next()) {
							// long objBranch = result.getLong(1);
							// long objId = result.getLong(2);
							// long revMin = result.getLong(3);
							// long revMax = result.getLong(4);

							TLID id = IdentifierUtil.getId(result, valueIndex);
							if (id == null || id.equals(nullId)) {
								continue;
							}

							TLID newId = modelMapping.get(id);
							if (newId == null) {
								continue;
							}

							IdentifierUtil.setId(result, valueIndex, newId);
							updates++;
							result.updateRow();
						}
					}
					String refName = associationUpdate.getReference().getTypeName() + "#"
						+ associationUpdate.getReference().getPartName();
					log.info(
						"Updated " + updates + " model references '" + refName + "' in table '" + tableName + "'.");
				}
			}
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to migrate model references.", ex);
		}
	}

}
