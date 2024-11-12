/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.Module;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} creating {@link TLModule}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLModuleProcessor extends TLModelBaseLineMigrationProcessor<CreateTLModuleProcessor.Config> {

	/**
	 * Configuration options of {@link CreateTLModuleProcessor}.
	 */
	@TagName("create-module")
	public interface Config
			extends TLModelBaseLineMigrationProcessor.Config<CreateTLModuleProcessor>, NamedConfigMandatory,
			AnnotatedConfig<TLModuleAnnotation> {
		// Sum interface
	}

	private Util _util;

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			internalDoMigration(log, connection, tlModel);
			return true;
		} catch (Exception ex) {
			log.error("Create module migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		Module existing = _util.getTLModule(connection, TLContext.TRUNK_ID, getConfig().getName());
		if (existing != null) {
			log.info("Module already exists: " + getConfig().getName());
			return;
		}

		_util.createTLModule(connection, getConfig().getName(), getConfig());
		if (tlModel != null) {
			MigrationUtils.createModule(log, tlModel, getConfig().getName(), getConfig());
		}
		log.info("Created module " + getConfig().getName());
	}

}
