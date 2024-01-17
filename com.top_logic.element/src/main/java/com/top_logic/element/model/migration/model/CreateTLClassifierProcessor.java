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
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLClassifierAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;

/**
 * {@link MigrationProcessor} creating a new {@link TLClassifier}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateTLClassifierProcessor extends AbstractConfiguredInstance<CreateTLClassifierProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLClassifierProcessor}.
	 */
	@TagName("create-classifier")
	public interface Config
			extends PolymorphicConfiguration<CreateTLClassifierProcessor>, AnnotatedConfig<TLClassifierAnnotation> {

		/**
		 * Qualified name of the new {@link TLClassifier}.
		 */
		QualifiedPartName getName();

		/**
		 * Sort order of the new classifier. If not set, the new {@link TLClassifier} will be
		 * appended to the list of classifiers.
		 */
		@IntDefault(Util.NO_SORT_ORDER)
		int getSortOrder();

	}

	private Util _util;

	/**
	 * Creates a {@link CreateTLClassifierProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLClassifierProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating classifier migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName classifierName = getConfig().getName();
		_util.createTLClassifier(connection, classifierName, getConfig().getSortOrder(), getConfig());
		log.info("Created classifier " + _util.qualifiedName(classifierName));
	}

}
