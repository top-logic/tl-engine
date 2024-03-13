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
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.basic.func.misc.AlwaysNull;
import com.top_logic.basic.func.misc.AlwaysTrue;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLReference;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.Reference;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} deleting no longer used {@link TLReference}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeleteTLReferenceProcessor extends AbstractConfiguredInstance<DeleteTLReferenceProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link DeleteTLReferenceProcessor}.
	 */
	@TagName("delete-reference")
	public interface Config extends PolymorphicConfiguration<DeleteTLReferenceProcessor>,
			TLModelBaseLineMigrationProcessor.SkipModelBaselineApaption {

		/**
		 * Qualified name of the {@link TLReference} to delete.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedPartName value);

		/**
		 * Whether the reference to delete is an inverse reference.
		 */
		@Hidden
		@Derived(fun = AlwaysFalse.class, args = {})
		boolean isInverse();

		/**
		 * Name of the {@link ApplicationObjectUtil#WRAPPER_ATTRIBUTE_ASSOCIATION_BASE} table in
		 * which the data for the reference to delete are stored.
		 * 
		 * <p>
		 * Note: This value must only be set, when the reference is not an overridden attribute.
		 * </p>
		 */
		@Nullable
		@StringDefault(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION)
		String getAssociationTable();
	}

	/**
	 * Special configuration to remove inverse references.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("delete-inverse-reference")
	public interface InverseConfig extends Config {

		/**
		 * Qualified name of the inverse {@link TLReference} to delete.
		 */
		@Override
		@Mandatory
		QualifiedPartName getName();

		@Override
		@Derived(fun = AlwaysTrue.class, args = {})
		boolean isInverse();

		@Override
		@Derived(fun = AlwaysNull.class, args = {})
		String getAssociationTable();
	}

	private Util _util;

	/**
	 * Creates a {@link DeleteTLReferenceProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteTLReferenceProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel)
			throws SQLException, MigrationException {
		QualifiedPartName partToDelete = getConfig().getName();

		Reference typePart;
		try {
			typePart = (Reference) _util.getTLTypePartOrFail(connection, partToDelete);
		} catch (MigrationException ex) {
			log.info(
				"No part with name '" + _util.qualifiedName(partToDelete) + "' to delete available at "
					+ getConfig().location(),
				Log.WARN);
			return false;
		}
		_util.deleteModelPart(connection, typePart);
		boolean updateModelBaseline;
		if (getConfig().isSkipModelBaselineChange()) {
			updateModelBaseline = false;
		} else {
			updateModelBaseline = MigrationUtils.deleteTypePart(log, tlModel, partToDelete);
		}
		log.info("Deleted reference " + _util.toString(typePart));

		if (!typePart.getID().equals(typePart.getDefinition())) {
			// Reference is an overridden attribute. Nothing to do more.
			return updateModelBaseline;
		}
		if (!getConfig().isInverse()) {
			Type association = _util.getTLTypeOrFail(connection, typePart.getOwner().getModule(),
				TLStructuredTypeColumns.syntheticAssociationName(typePart.getOwner().getTypeName(), typePart.getPartName()));
			_util.deleteTLType(connection, association, false);
			if (getConfig().getAssociationTable() != null) {
				CompiledStatement delete = query(
					parameters(
						_util.branchParamDef(),
						parameterDef(DBType.ID, "id")),
					delete(
						table(SQLH.mangleDBName(getConfig().getAssociationTable())),
						and(
							_util.eqBranch(),
							eqSQL(
								column(_util.refID(ApplicationObjectUtil.META_ATTRIBUTE_ATTR)),
								parameter(DBType.ID, "id"))))).toSql(connection.getSQLDialect());

				int deletedRows = delete.executeUpdate(connection, typePart.getBranch(), typePart.getID());
				log.info("Deleted " + deletedRows + " assignments for deleted part " + _util.toString(typePart));
			}
		}
		return updateModelBaseline;
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Delete tl reference migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

}
