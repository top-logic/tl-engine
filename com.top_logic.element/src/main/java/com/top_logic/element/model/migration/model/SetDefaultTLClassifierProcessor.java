/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedTypeName;

/**
 * {@link MigrationProcessor} defining the default {@link TLClassifier} in a {@link TLEnumeration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetDefaultTLClassifierProcessor
		extends TLModelBaseLineMigrationProcessor<SetDefaultTLClassifierProcessor.Config> {

	/**
	 * Configuration options of {@link SetDefaultTLClassifierProcessor}.
	 */
	@TagName("set-default-classifier")
	public interface Config extends TLModelBaseLineMigrationProcessor.Config<SetDefaultTLClassifierProcessor> {

		/**
		 * Qualified name of the {@link TLEnumeration} to update.
		 */
		@Mandatory
		QualifiedTypeName getEnumeration();

		/**
		 * Setter for {@link #getEnumeration()}.
		 */
		void setEnumeration(QualifiedTypeName value);

		/**
		 * Name of the classifier in {@link #getEnumeration()} that must be
		 * {@link TLClassifier#isDefault() default}. If <code>null</code>, no classifier will be
		 * default.
		 */
		@Nullable
		String getDefaultClassifier();

		/**
		 * Setter for {@link #getDefaultClassifier()}.
		 */
		void setDefaultClassifier(String value);

	}

	private Util _util;

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Setting default classifier migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedTypeName enumName = getConfig().getEnumeration();
		_util.setDefaultTLClassifier(connection, enumName, getConfig().getDefaultClassifier());
		boolean updateModelBaseline = tlModel == null ? false : 
			MigrationUtils.setDefaultClassifier(log, tlModel, enumName, getConfig().getDefaultClassifier());
		if (getConfig().getDefaultClassifier() == null) {
			log.info("Removed default classifier in " + _util.qualifiedName(enumName));
		} else {
			log.info("Set default classifier '" + getConfig().getDefaultClassifier() + "' in "
				+ _util.qualifiedName(enumName));
		}
		return updateModelBaseline;
	}

}
