/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link MigrationProcessor} updating a {@link TLAssociation}.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven F�rster</a>
 */
public class UpdateTLAssociationProcessor extends AbstractConfiguredInstance<UpdateTLAssociationProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLAssociationProcessor}.
	 */
	@TagName("update-association")
	public interface Config
			extends PolymorphicConfiguration<UpdateTLAssociationProcessor>, AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the {@link TLAssociation} to update.
		 */
		QualifiedTypeName getName();

		/**
		 * New name of the association including the new module.
		 */
		@Nullable
		QualifiedTypeName getNewName();

	}

	/**
	 * Creates a {@link UpdateTLAssociationProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLAssociationProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Update association migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName typeName = getConfig().getName();
		Type association;
		try {
			association = Util.getTLTypeOrFail(connection, typeName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find association to update " + Util.qualifiedName(typeName) + " at "
					+ getConfig().location(),
				Log.WARN);
			return;
		}
		Module newModule;
		QualifiedTypeName newName = getConfig().getNewName();
		if (newName == null || typeName.getModuleName().equals(newName.getModuleName())) {
			newModule = null;
		} else {
			newModule = Util.getTLModuleOrFail(connection, newName.getModuleName());
		}
		String newAssociationName;
		if (newName == null || typeName.getTypeName().equals(newName.getTypeName())) {
			newAssociationName = null;
		} else {
			newAssociationName = newName.getTypeName();
		}

		Util.updateTLStructuredType(connection, association, newModule, newAssociationName, null,
			null, (String) null);
		log.info("Updated association " + Util.toString(association));
	}

}
