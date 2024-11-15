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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedTypeName;

/**
 * {@link MigrationProcessor} creating a {@link TLAssociation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLAssociationProcessor
		extends TLModelBaseLineMigrationProcessor<CreateTLAssociationProcessor.Config> {

	/**
	 * Configuration options of {@link CreateTLAssociationProcessor}.
	 */
	@TagName("create-association")
	public interface Config
			extends TLModelBaseLineMigrationProcessor.Config<CreateTLAssociationProcessor>,
			AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the {@link TLAssociation} to create.
		 */
		@Mandatory
		QualifiedTypeName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedTypeName value);

	}

	private Util _util;

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Creating association migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		QualifiedTypeName typeName = getConfig().getName();
		_util.createTLAssociation(connection, typeName, getConfig());
		boolean updateModelBaseline;
		if (tlModel == null || TLStructuredTypeColumns.isSyntheticAssociationName(typeName.getTypeName())) {
			/* AssociationEnd is an end of an internal TLAssociation which is not defined in the
			 * model baseline. */
			updateModelBaseline = false;
		} else {
			MigrationUtils.createAssociationType(log, tlModel, typeName, getConfig());
			updateModelBaseline = true;
		}
		log.info("Created association " + _util.qualifiedName(typeName));
		return updateModelBaseline;
	}

}
