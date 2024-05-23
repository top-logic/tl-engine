/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * 
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
		 * The name of the source end of the association.
		 */
		@StringDefault(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME)
		String getSrc();

		/**
		 * The name of the destination end of the association.
		 */
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		String getDest();
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
		log.info("Inverting links in table '" + tableName + "'. ");

		MORepository repository = context.getPersistentRepository();
		MOStructure table = (MOStructure) repository.getMetaObject(tableName);

		MOReference srcRef = (MOReference) table.getAttribute(getConfig().getSrc());
		MOReference destRef = (MOReference) table.getAttribute(getConfig().getDest());

		try {
			String srcTypeCol = srcRef.getColumn(ReferencePart.type).getDBName();
			String srcIdCol = srcRef.getColumn(ReferencePart.name).getDBName();
			String destTypeCol = destRef.getColumn(ReferencePart.type).getDBName();
			String destIdCol = destRef.getColumn(ReferencePart.name).getDBName();
			CompiledStatement sql = query(
				update(
					table(table.getDBMapping().getDBName()),
					literal(DBType.BOOLEAN, Boolean.TRUE),
					columnNames(
						// columnDef(srcRef.getColumn(ReferencePart.branch).getDBName()),
						// columnDef(srcRef.getColumn(ReferencePart.revision).getDBName()),
						srcTypeCol,
						srcIdCol,
						// columnDef(destRef.getColumn(ReferencePart.branch).getDBName()),
						// columnDef(destRef.getColumn(ReferencePart.revision).getDBName()),
						destTypeCol,
						destIdCol),
					expressions(
						column(destTypeCol),
						column(destIdCol),
						column(srcTypeCol),
						column(srcIdCol))))
				.toSql(connection.getSQLDialect());

			int cnt = sql.executeUpdate(connection);
			log.info("Inverted " + cnt + " links in table '" + table.getName() + "'.");
		} catch (SQLException ex) {
			log.error("Failed to invert links in table '" + table.getName() + "': " + ex.getMessage());
		}
	}

}
