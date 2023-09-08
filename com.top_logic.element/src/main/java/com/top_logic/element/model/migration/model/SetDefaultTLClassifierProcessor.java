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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLClassifierAnnotation;

/**
 * {@link MigrationProcessor} defining the default {@link TLClassifier} in a {@link TLEnumeration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetDefaultTLClassifierProcessor extends AbstractConfiguredInstance<SetDefaultTLClassifierProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link SetDefaultTLClassifierProcessor}.
	 */
	@TagName("set-default-classifier")
	public interface Config
			extends PolymorphicConfiguration<SetDefaultTLClassifierProcessor>, AnnotatedConfig<TLClassifierAnnotation> {

		/**
		 * Qualified name of the {@link TLEnumeration} to update.
		 */
		QualifiedTypeName getEnumeration();

		/**
		 * Name of the classifier in {@link #getEnumeration()} that must be
		 * {@link TLClassifier#isDefault() default}. If <code>null</code>, no classifier will be
		 * default.
		 */
		@Nullable
		String getDefaultClassifier();

	}

	/**
	 * Creates a {@link SetDefaultTLClassifierProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SetDefaultTLClassifierProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Setting default classifier migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName enumName = getConfig().getEnumeration();
		Util.setDefaultTLClassifier(connection, enumName, getConfig().getDefaultClassifier());
		if (getConfig().getDefaultClassifier() == null) {
			log.info("Removed default classifier in " + Util.qualifiedName(enumName));
		} else {
			log.info("Set default classifier '" + getConfig().getDefaultClassifier() + "' in "
				+ Util.qualifiedName(enumName));
		}
	}

}
