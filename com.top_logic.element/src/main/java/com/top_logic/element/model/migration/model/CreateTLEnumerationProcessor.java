/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link MigrationProcessor} creating a new {@link TLEnumeration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateTLEnumerationProcessor extends AbstractConfiguredInstance<CreateTLEnumerationProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLEnumerationProcessor}.
	 */
	@TagName("create-enumeration")
	public interface Config
			extends PolymorphicConfiguration<CreateTLEnumerationProcessor>, AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the new {@link TLEnumeration}.
		 */
		QualifiedTypeName getName();

	}

	/**
	 * Creates a {@link CreateTLEnumerationProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLEnumerationProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating enumeration migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName enumName = getConfig().getName();
		Util.createTLEnumeration(connection, enumName, getConfig());
		log.info("Created enumeration " + Util.qualifiedName(enumName));
	}

}
