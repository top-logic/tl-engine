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
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLModuleAnnotation;

/**
 * {@link MigrationProcessor} creating {@link TLModule}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLModuleProcessor extends AbstractConfiguredInstance<CreateTLModuleProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLModuleProcessor}.
	 */
	@TagName("create-module")
	public interface Config extends PolymorphicConfiguration<CreateTLModuleProcessor>, NamedConfigMandatory,
			AnnotatedConfig<TLModuleAnnotation> {
		// Sum interface
	}

	/**
	 * Creates a {@link CreateTLModuleProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLModuleProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Create module migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		Util.createTLModule(connection, getConfig().getName(), getConfig());
		log.info("Created module " + getConfig().getName());
	}

}
