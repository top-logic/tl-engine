/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLReference;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} that changes the {@link TLReference} of links stored in polymorphic
 * link tables.
 */
public class ChangeLinkReferenceProcessor extends AbstractConfiguredInstance<ChangeLinkReferenceProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link ChangeLinkReferenceProcessor}.
	 */
	@TagName("change-link-reference")
	public interface Config<I extends ChangeLinkReferenceProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * The name of the table, where the objects of the source type are stored.
		 */
		@Mandatory
		@Name("table")
		String getTable();

		/**
		 * Name of the reference column of the given {@link #getTable()} that contains the reference
		 * associated with the link stored in a table row.
		 */
		@Name("reference-column")
		@StringDefault(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)
		String getReferenceColumn();

		/**
		 * The reference in the source model.
		 */
		@Mandatory
		@Name("source-ref")
		QualifiedPartName getSourceRef();

		/**
		 * The reference in the target model to which the existing links should be assigned to.
		 */
		@Mandatory
		@Name("target-ref")
		QualifiedPartName getTargetRef();
	}

	/**
	 * Creates a {@link ChangeLinkReferenceProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChangeLinkReferenceProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();
		
		Config<?> config = getConfig();
		try {
			QualifiedPartName sourceConfig = config.getSourceRef();
			QualifiedPartName targetConfig = config.getTargetRef();

			Type sourceType = util.getTLTypeOrNull(connection, sourceConfig.getOwner());
			if (sourceType == null) {
				log.info("No such source type '" + sourceConfig.getOwner().getName() + "', skipping moving links for '"
					+ sourceConfig.getName() + "'.", Log.WARN);
				return;
			}

			TypePart sourceRef = util.getTLTypePartOrFail(connection, sourceConfig);
			TypePart targetRef = util.getTLTypePartOrFail(connection, targetConfig);
			
			MOClass table = (MOClass) context.getPersistentRepository().getType(config.getTable());
			MOReference ref = (MOReference) table.getAttribute(config.getReferenceColumn());

			String tableName = table.getDBMapping().getDBName();
			String refColumn = ref.getColumn(ReferencePart.name).getDBName();

			CompiledStatement update = query(
				update(
					table(tableName),
					eqSQL(literal(DBType.ID, sourceRef.getDefinition()), column(refColumn)),
					columnNames(refColumn),
					expressions(literal(DBType.ID, targetRef.getDefinition())))).toSql(connection.getSQLDialect());
			
			int cnt = update.executeUpdate(connection);

			log.info("Changed reference of " + cnt + " links in table '" + tableName + "' from "
				+ toString(sourceConfig, sourceRef) + " to " + toString(targetConfig, targetRef) + ".");
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to change reference of links in table '" + config.getTable() + "' from '"
				+ config.getSourceRef().getName() + "' to '" + config.getTargetRef().getName() + "'.", ex);
		}
	}

	private static String toString(QualifiedPartName sourceConfig, TypePart sourceRef) {
		return "'" + sourceConfig.getName() + "' (" + sourceRef.getID() + ", def: " + sourceRef.getDefinition()
			+ ")";
	}

}
