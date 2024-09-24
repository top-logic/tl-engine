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
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
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

		log.info("Inverting links " + (invertedReference == null ? "" : "of reference '" + invertedReference + "' ")
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

			String srcTypeCol = srcRef.getColumn(ReferencePart.type).getDBName();
			String srcIdCol = srcRef.getColumn(ReferencePart.name).getDBName();
			String destTypeCol = destRef.getColumn(ReferencePart.type).getDBName();
			String destIdCol = destRef.getColumn(ReferencePart.name).getDBName();

			CompiledStatement sql = query(
				update(
					table(table.getDBMapping().getDBName()),
					reference == null || metaRef == null ? 
						SQLFactory.literalTrueLogical() : 
						eqSQL(column(metaRef.getColumn(ReferencePart.name).getDBName()),
							literal(DBType.ID, reference.getDefinition())),
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
		} catch (SQLException | MigrationException ex) {
			log.error("Failed to invert links in table '" + table.getName() + "': " + ex.getMessage());
		}
	}

}
