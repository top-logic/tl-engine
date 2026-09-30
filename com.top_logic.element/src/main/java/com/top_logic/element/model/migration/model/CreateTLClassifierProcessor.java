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
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLClassifierAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} creating a new {@link TLClassifier}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateTLClassifierProcessor extends TLModelBaseLineMigrationProcessor<CreateTLClassifierProcessor.Config> {

	/**
	 * Configuration options of {@link CreateTLClassifierProcessor}.
	 */
	@TagName("create-classifier")
	public interface Config
			extends TLModelBaseLineMigrationProcessor.Config<CreateTLClassifierProcessor>,
			AnnotatedConfig<TLClassifierAnnotation> {

		/**
		 * Qualified name of the new {@link TLClassifier}.
		 */
		QualifiedPartName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedPartName value);

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			internalDoMigration(log, connection, tlModel);
			return true;
		} catch (Exception ex) {
			log.error("Creating classifier migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedPartName classifierName = getConfig().getName();

		Type ownerType = _util.getTLTypeOrNull(connection, TLContext.TRUNK_ID, classifierName.getModuleName(),
			classifierName.getTypeName());
		if (ownerType == null) {
			log.info("Enumeration of classifier '" + classifierName.getName() + "' does not exist.", Log.WARN);
			return;
		}

		TypePart part = _util.getTLTypePart(connection, ownerType, classifierName.getPartName());
		if (part != null) {
			log.info("Classifier '" + classifierName.getName() + "' already exists.", Log.WARN);
			return;
		}
		_util.createTLClassifier(connection, classifierName, getConfig());
		if (tlModel != null) {
			MigrationUtils.createClassifier(log, tlModel, classifierName, getConfig());
		}
		log.info("Created classifier " + _util.qualifiedName(classifierName));
	}

}
