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
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;

/**
 * {@link MigrationProcessor} creating a {@link TLReference} for an existing
 * {@link TLAssociationEnd}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLEndReferenceProcessor extends AbstractConfiguredInstance<CreateTLEndReferenceProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLEndReferenceProcessor}.
	 */
	@TagName("create-end-reference")
	public interface Config
			extends PolymorphicConfiguration<CreateTLEndReferenceProcessor>, AnnotatedConfig<TLAttributeAnnotation> {

		/**
		 * Qualified name of the new {@link TLReference}.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * Qualified name of the corresponding association end.
		 */
		@Mandatory
		QualifiedPartName getEnd();

	}

	private Util _util;

	/**
	 * Creates a {@link CreateTLEndReferenceProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLEndReferenceProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating part migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName partName = getConfig().getName();
		QualifiedPartName associationEnd = getConfig().getEnd();

		_util.createTLEndReference(connection, partName, associationEnd, getConfig());
		log.info(
			"Created reference " + _util.qualifiedName(partName) + " for end " + _util.qualifiedName(associationEnd));
	}

}
