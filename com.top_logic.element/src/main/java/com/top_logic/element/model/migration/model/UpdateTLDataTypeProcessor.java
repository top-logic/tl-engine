/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.DatatypeConfig;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} updating a {@link TLPrimitive}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateTLDataTypeProcessor extends AbstractConfiguredInstance<UpdateTLDataTypeProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLDataTypeProcessor}.
	 * 
	 * @see com.top_logic.model.migration.CreateTLDatatypeProcessor.Config
	 */
	@TagName("update-datatype")
	public interface Config extends PolymorphicConfiguration<UpdateTLDataTypeProcessor>,
			AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the {@link TLPrimitive} to update.
		 */
		QualifiedTypeName getName();

		/**
		 * New name of the data type including the new module.
		 */
		@Nullable
		QualifiedTypeName getNewName();

		/**
		 * See {@link DatatypeConfig#getKind()}.
		 */
		@Name(DatatypeConfig.KIND)
		@NullDefault
		TLPrimitive.Kind getKind();

		/**
		 * New way in which values of this data type are stored in the database.
		 * 
		 * @see TLPrimitive#getDBType()
		 * @see TLPrimitive#getDBSize()
		 * @see TLPrimitive#getDBPrecision()
		 */
		DBColumnType getDBMapping();

		/**
		 * The {@link StorageMapping} to apply when loading and storing values.
		 * 
		 * @see DatatypeConfig#getStorageMapping()
		 */
		@Name(DatatypeConfig.STORAGE_MAPPING)
		@DefaultContainer
		@Nullable
		PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();

	}

	private Util _util;

	/**
	 * Creates a {@link UpdateTLDataTypeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLDataTypeProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Update datatype migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName typeName = getConfig().getName();
		Type type;
		try {
			type = _util.getTLTypeOrFail(connection, typeName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find datatype to update " + _util.qualifiedName(typeName) + " at " + getConfig().location(),
				Log.WARN);
			return;
		}
		Module newModule;
		QualifiedTypeName newName = getConfig().getNewName();
		if (newName == null || typeName.getModuleName().equals(newName.getModuleName())) {
			newModule = null;
		} else {
			newModule = _util.getTLModuleOrFail(connection, newName.getModuleName());
		}
		String dataTypeName;
		if (newName == null || typeName.getTypeName().equals(newName.getTypeName())) {
			dataTypeName = null;
		} else {
			dataTypeName = newName.getTypeName();
		}

		DBColumnType columnType = getConfig().getDBMapping();
		PolymorphicConfiguration<StorageMapping<?>> storageMapping = getConfig().getStorageMapping();
		AnnotatedConfig<TLTypeAnnotation> annotations = getConfig();
		_util.updateTLDataType(connection, type, newModule, dataTypeName, getConfig().getKind(), columnType,
			storageMapping, annotations);

		log.info("Updated datatype " + _util.qualifiedName(typeName));
	}

}
