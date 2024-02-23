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
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.PartConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLProperty;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;

/**
 * {@link MigrationProcessor} creating a primitive attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLPropertyProcessor extends AbstractConfiguredInstance<CreateTLPropertyProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLPropertyProcessor}.
	 */
	@TagName("create-property")
	public interface Config
			extends PolymorphicConfiguration<CreateTLPropertyProcessor>, AnnotatedConfig<TLAttributeAnnotation> {

		/**
		 * Qualified name for the new {@link TLProperty}.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * See {@link PartConfig#getTypeSpec()}.
		 */
		@Mandatory
		@Name(PartConfig.TYPE_SPEC)
		QualifiedTypeName getType();

		/**
		 * See {@link PartConfig#getMandatory()}.
		 */
		@Name(PartConfig.MANDATORY)
		boolean isMandatory();

		/**
		 * See {@link PartConfig#isMultiple()}.
		 */
		@Name(PartConfig.MULTIPLE_PROPERTY)
		boolean isMultiple();

		/**
		 * See {@link PartConfig#isOrdered()}.
		 */
		@Name(PartConfig.ORDERED_PROPERTY)
		Boolean isOrdered();

		/**
		 * See {@link PartConfig#isBag()}.
		 */
		@Name(PartConfig.BAG_PROPERTY)
		Boolean isBag();

	}

	private Util _util;

	/**
	 * Creates a {@link CreateTLPropertyProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLPropertyProcessor(InstantiationContext context, Config config) {
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
		QualifiedTypeName targetType = (getConfig().getType());
		_util.createTLProperty(connection, partName, targetType,
			getConfig().isMandatory(), getConfig().isMultiple(), getConfig().isBag(),
			getConfig().isOrdered(), getConfig());
		log.info("Created part " + _util.qualifiedName(partName));
	}

}
