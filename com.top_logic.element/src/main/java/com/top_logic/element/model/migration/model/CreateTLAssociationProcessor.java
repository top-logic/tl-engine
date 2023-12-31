/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link MigrationProcessor} creating a {@link TLAssociation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLAssociationProcessor extends AbstractConfiguredInstance<CreateTLAssociationProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLAssociationProcessor}.
	 */
	@TagName("create-association")
	public interface Config
			extends PolymorphicConfiguration<CreateTLAssociationProcessor>, AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the {@link TLAssociation} to create.
		 */
		@Mandatory
		QualifiedTypeName getName();

	}

	/**
	 * Creates a {@link CreateTLAssociationProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLAssociationProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating association migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName typeName = getConfig().getName();
		Util.createTLAssociation(connection, typeName, getConfig());
		log.info("Created association " + Util.qualifiedName(typeName));
	}

}
