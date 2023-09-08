/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLProperty;

/**
 * {@link MigrationProcessor} deleting no longer used {@link TLProperty}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeleteTLPropertyProcessor extends AbstractConfiguredInstance<DeleteTLPropertyProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link DeleteTLPropertyProcessor}.
	 */
	@TagName("delete-property")
	public interface Config extends PolymorphicConfiguration<DeleteTLPropertyProcessor> {

		/**
		 * Qualified name of the {@link TLProperty} to delete.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * The list of {@link MetaObject tables} in which the the owner type of {@link #getName()}
		 * stores the data.
		 * 
		 * <p>
		 * When the attribute is deleted, data in the {@link AbstractFlexDataManager#FLEX_DATA}
		 * table are deleted for elements storing in one of the given tables.
		 * </p>
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getTypeTables();

	}

	/**
	 * Creates a {@link DeleteTLPropertyProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteTLPropertyProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws SQLException {
		QualifiedPartName partToDelete = getConfig().getName();

		TypePart typePart;
		try {
			typePart = Util.getTLTypePartOrFail(connection, partToDelete);
		} catch (MigrationException ex) {
			log.info(
				"No part with name '" + Util.qualifiedName(partToDelete) + "' to delete available at "
					+ getConfig().location(),
				Log.WARN);
			return;
		}

		Util.deleteModelPart(connection, typePart);
		log.info("Deleted model part " + Util.toString(typePart));

		if (!typePart.getID().equals(typePart.getDefinition())) {
			// attribute is an overridden attribute. Nothing to do more.
			return;
		}
		if (!getConfig().getTypeTables().isEmpty()) {
			CompiledStatement delete = query(
			parameters(
				parameterDef(DBType.LONG, "branch"),
				parameterDef(DBType.STRING, "attr"),
				setParameterDef("types", DBType.STRING)),
			delete(
				table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
				and(
					eqSQL(
						column(BasicTypes.BRANCH_DB_NAME),
						parameter(DBType.LONG, "branch")),
					eqSQL(
						column(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
						parameter(DBType.STRING, "attr")),
					inSet(
						column(AbstractFlexDataManager.TYPE_DBNAME),
						setParameter("types", DBType.STRING))))).toSql(connection.getSQLDialect());

			int deletedRows =
				delete.executeUpdate(connection, typePart.getBranch(), partToDelete.getPartName(),
					getConfig().getTypeTables());
			log.info("Deleted " + deletedRows + " assignments for deleted part " + Util.toString(typePart));
		}
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Delete tl property migration failed at " + getConfig().location(), ex);
		}
	}

}
