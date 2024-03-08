/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.sql.SQLException;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} deleting no longer used {@link TLPrimitive}.
 * 
 * @see DeleteTLClassProcessor
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeleteTLDatatypeProcessor extends AbstractConfiguredInstance<DeleteTLDatatypeProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link DeleteTLDatatypeProcessor}.
	 */
	@TagName("delete-datatype")
	public interface Config extends PolymorphicConfiguration<DeleteTLDatatypeProcessor>,
			TLModelBaseLineMigrationProcessor.SkipModelBaselineApaption {

		/**
		 * Qualified name of the {@link TLPrimitive} to delete.
		 */
		@Mandatory
		QualifiedTypeName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedTypeName value);

	}

	private Util _util;

	/**
	 * Creates a {@link DeleteTLDatatypeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteTLDatatypeProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel)
			throws SQLException, MigrationException {
		QualifiedTypeName datatypeToDelete = getConfig().getName();

		Type type;
		try {
			type = _util.getTLTypeOrFail(connection, datatypeToDelete);
		} catch (MigrationException ex) {
			log.info(
				"No datatype with name '" + _util.qualifiedName(datatypeToDelete) + "' to delete available at "
					+ getConfig().location(),
				Log.WARN);
			return false;
		}
		if (type.getKind() != Type.Kind.DATATYPE) {
			log.error("Type '" + _util.qualifiedName(datatypeToDelete) + "' to delete is not a data type but of kind '"
					+ type.getKind() + "' at " + getConfig().location());
			return false;
		}

		_util.deleteTLType(connection, type, false);
		log.info("Deleted type " + _util.toString(type) + ".");

		if (getConfig().isSkipModelBaselineChange()) {
			return false;
		}
		return MigrationUtils.deleteType(log, tlModel, datatypeToDelete);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Delete datatype migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

}
