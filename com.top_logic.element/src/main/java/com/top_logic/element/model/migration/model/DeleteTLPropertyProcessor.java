/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.UnreachableAssertion;
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
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLProperty;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} deleting no longer used {@link TLProperty}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeleteTLPropertyProcessor extends AbstractConfiguredInstance<DeleteTLPropertyProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

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
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedPartName value);

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

	private Util _util;

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

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws SQLException {
		QualifiedPartName partToDelete = getConfig().getName();

		TypePart typePart;
		try {
			typePart = _util.getTLTypePartOrFail(connection, partToDelete);
		} catch (MigrationException ex) {
			log.info(
				"No part with name '" + _util.qualifiedName(partToDelete) + "' to delete available at "
					+ getConfig().location(),
				Log.WARN);
			return false;
		}

		_util.deleteModelPart(connection, typePart);
		boolean updateModelBaseline =
			tlModel == null ? false : deleteTypePartFromModelBaseline(log, tlModel, partToDelete, typePart);
		log.info("Deleted model part " + _util.toString(typePart));

		if (!typePart.getID().equals(typePart.getDefinition())) {
			// attribute is an overridden attribute. Nothing to do more.
			return updateModelBaseline;
		}
		if (!getConfig().getTypeTables().isEmpty()) {
			CompiledStatement delete = query(
				parameters(
					_util.branchParamDef(),
					parameterDef(DBType.STRING, "attr"),
					setParameterDef("types", DBType.STRING)),
				delete(
					table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
					and(
						_util.eqBranch(),
						eqSQL(
							column(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
							parameter(DBType.STRING, "attr")),
						inSet(
							column(AbstractFlexDataManager.TYPE_DBNAME),
							setParameter("types", DBType.STRING))))).toSql(connection.getSQLDialect());

			int deletedRows =
				delete.executeUpdate(connection, typePart.getBranch(), partToDelete.getPartName(),
					getConfig().getTypeTables());
			log.info("Deleted " + deletedRows + " assignments for deleted part " + _util.toString(typePart));
		}
		return updateModelBaseline;
	}

	private boolean deleteTypePartFromModelBaseline(Log log, Document tlModel, QualifiedPartName partName,
			TypePart part) {
		switch (part.getKind()) {
			case ASSOCIATION_END:
			case ASSOCIATION_PROPERTY:
				return false;
			case CLASSIFIER:
			case CLASS_PROPERTY:
			case REFERENCE:
				return MigrationUtils.deleteTypePart(log, tlModel, partName);
		}
		throw new UnreachableAssertion("Uncovered kind: " + part.getKind());
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Delete tl property migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

}
