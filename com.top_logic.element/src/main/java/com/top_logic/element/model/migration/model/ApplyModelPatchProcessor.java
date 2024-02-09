/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.diff.apply.CreateMigrationProcessors;
import com.top_logic.element.model.diff.apply.CreateMigrationProcessors.MigrationProcessors;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLModel;
import com.top_logic.model.config.ModelPartConfig;

/**
 * {@link MigrationProcessor} applying a list of model patches.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ApplyModelPatchProcessor extends AbstractConfiguredInstance<ApplyModelPatchProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Typed configuration interface definition for {@link ApplyModelPatchProcessor}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ApplyModelPatchProcessor> {

		/** Tag name of {@link Config}. */
		String TAG_NAME = "apply-patches";

		/**
		 * The {@link DiffElement}s that describe the patch to apply.
		 */
		@DefaultContainer
		List<DiffElement> getDiff();

	}

	/**
	 * Create a {@link ApplyModelPatchProcessor}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public ApplyModelPatchProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			internalDoMigration(context, log, connection);
			log.info("Model patch applied.");
		} catch (SQLException ex) {
			log.error("Applying model patch failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(MigrationContext context, Log log, PooledConnection connection)
			throws SQLException {
		String storedModel = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY,
			DynamicModelService.APPLICATION_MODEL_PROPERTY);
		if (StringServices.isEmpty(storedModel)) {
			log.error("No stored model to apply model patch available.");
			return;
		}
		BufferingProtocol protocol = new BufferingProtocol();
		TLModel oldModel;
		try {
			oldModel = DynamicModelService.loadTransientModel(protocol, storedModel);
			protocol.getErrors().forEach(log::error);
			protocol.getInfos().forEach(log::info);
			if (protocol.hasErrors()) {
				return;
			}
		} catch (ConfigurationException | RuntimeException ex) {
			protocol.getErrors().forEach(log::error);
			protocol.getInfos().forEach(log::info);
			log.error("Unable to load stored model.", ex);
			return;
		}

		protocol = new BufferingProtocol();
		MigrationProcessors processors =
			CreateMigrationProcessors.createProcessors(protocol, oldModel, getConfig().getDiff());
		protocol.getErrors().forEach(log::error);
		protocol.getInfos().forEach(log::info);
		if (protocol.hasErrors()) {
			return;
		}

		log.info("Applying processors created from model patch.");
		for (var processor : processors.getProcessors()) {
			TypedConfigUtil.createInstance(processor).doMigration(context, log, connection);
		}

		log.info("Storing final model configuration.");
		ModelPartConfig finalModelConfig = new ModelConfigExtractor().visitModel(oldModel, null);
		
		// Note: One must not pretty print the stored configuration, since this would change
		// multi-line text properties (such as search expression source code). In consequence, the
		// stored model would not match the current configuration and the model would be migrated
		// during each boot.
		String currentConfigXML = TypedConfiguration.toStringRaw(finalModelConfig);
		DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
			DynamicModelService.APPLICATION_MODEL_PROPERTY, currentConfigXML);
	}

}

