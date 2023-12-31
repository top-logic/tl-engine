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
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.element.config.DatatypeConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link MigrationProcessor} creating a new {@link TLPrimitive}..
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLDatatypeProcessor extends AbstractConfiguredInstance<CreateTLDatatypeProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLDatatypeProcessor}.
	 */
	@TagName("create-datatype")
	public interface Config extends PolymorphicConfiguration<CreateTLDatatypeProcessor>,
			AnnotatedConfig<TLTypeAnnotation>, DBColumnType {

		/**
		 * Qualified name of the new {@link TLPrimitive}.
		 */
		@Mandatory
		QualifiedTypeName getName();

		/**
		 * See {@link DatatypeConfig#getKind()}.
		 */
		@Name(DatatypeConfig.KIND)
		@Mandatory
		TLPrimitive.Kind getKind();

		/**
		 * The {@link StorageMapping} to apply when loading and storing values.
		 * 
		 * @see DatatypeConfig#getStorageMapping()
		 */
		@Name(DatatypeConfig.STORAGE_MAPPING)
		@Mandatory
		@DefaultContainer
		PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();

	}

	/**
	 * Creates a {@link CreateTLDatatypeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLDatatypeProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating datatype migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName typeName = getConfig().getName();
		Util.createTLDatatype(connection, typeName, getConfig().getKind(), getConfig(), getConfig().getStorageMapping(),
			getConfig());
		log.info("Created datatype " + Util.qualifiedName(typeName));
	}

}
