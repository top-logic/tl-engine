/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} deleting no longer used {@link TLStructuredType}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeleteTLClassProcessor extends AbstractConfiguredInstance<DeleteTLClassProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link DeleteTLClassProcessor}.
	 */
	@TagName("delete-class")
	public interface Config extends PolymorphicConfiguration<DeleteTLClassProcessor>,
			TLModelBaseLineMigrationProcessor.SkipModelBaselineApaption {

		/**
		 * Qualified name of the {@link TLClass} to delete.
		 */
		@Mandatory
		QualifiedTypeName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedTypeName value);

		/**
		 * Whether it is a failure if the class is not empty.
		 */
		boolean isFailOnExistingAttributes();

		/**
		 * The {@link MetaObject table} in which {@link #getName()} stores the data.
		 * 
		 * <p>
		 * When the class is deleted, data for the class in the given table are removed.
		 * </p>
		 */
		@Nullable
		String getTypeTable();

	}

	private Util _util;

	/**
	 * Creates a {@link DeleteTLClassProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteTLClassProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel)
			throws SQLException, MigrationException {
		QualifiedTypeName classToDelete = getConfig().getName();

		Type type;
		try {
			type = _util.getTLTypeOrFail(connection, classToDelete);
		} catch (MigrationException ex) {
			log.info(
				"No class with name '" + _util.qualifiedName(classToDelete) + "' to delete available at "
					+ getConfig().location(),
				Log.WARN);
			return false;
		}

		_util.deleteTLType(connection, type, getConfig().isFailOnExistingAttributes());
		log.info("Deleted type " + _util.toString(type) + ".");

		if (getConfig().getTypeTable() != null) {
			CompiledStatement delete = query(
				parameters(
					_util.branchParamDef(),
					parameterDef(DBType.ID, "type")),
				delete(
					table(SQLH.mangleDBName(getConfig().getTypeTable())),
					and(
						_util.eqBranch(),
						eqSQL(
							column(_util.refID(PersistentObject.TYPE_REF)),
							parameter(DBType.ID, "type"))))).toSql(connection.getSQLDialect());

			int deletedRows = delete.executeUpdate(connection, type.getBranch(), type.getID());
			log.info("Deleted " + deletedRows + " instances of type '" + _util.toString(type) + "' from table "
				+ getConfig().getTypeTable() + ".");
		}
		if (getConfig().isSkipModelBaselineChange()) {
			return false;
		}
		if (type.getKind() == Type.Kind.ASSOCIATION) {
			if (TLStructuredTypeColumns.isSyntheticAssociationName(type.getTypeName())) {
				// Synthetic associations are not contained in the model baseline.
				return false;
			}
		}
		return MigrationUtils.deleteType(log, tlModel, classToDelete);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Delete tl class migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

}
