/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} updating a {@link TLClass}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateTLClassProcessor extends AbstractConfiguredInstance<UpdateTLClassProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLClassProcessor}.
	 * 
	 * @see CreateTLClassifierProcessor.Config
	 */
	@TagName("update-class")
	public interface Config
			extends PolymorphicConfiguration<UpdateTLClassProcessor>, AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the {@link TLClass} to update.
		 */
		QualifiedTypeName getName();

		/**
		 * New name of the class including the new module.
		 */
		@Nullable
		QualifiedTypeName getNewName();

		/**
		 * See {@link ClassConfig#isAbstract()}.
		 */
		@Name(ClassConfig.ABSTRACT)
		boolean isAbstract();

		/**
		 * See {@link ClassConfig#isFinal()}.
		 */
		@Name(ClassConfig.FINAL)
		boolean isFinal();
	}

	private Util _util;

	/**
	 * Creates a {@link UpdateTLClassProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLClassProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Update class migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName typeName = getConfig().getName();
		Type type;
		try {
			type = _util.getTLTypeOrFail(connection, typeName);
		} catch (MigrationException ex) {
			log.info(
				"Unable to find class to update " + _util.qualifiedName(typeName) + " at " + getConfig().location(),
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
		String className;
		if (newName == null || typeName.getTypeName().equals(newName.getTypeName())) {
			className = null;
		} else {
			className = newName.getTypeName();
		}
		_util.updateTLStructuredType(connection, type, newModule, className, getConfig().isAbstract(),
			getConfig().isFinal(), getConfig());
		log.info("Updated type " + _util.qualifiedName(typeName));

		if (newModule != null || className != null) {
			String assNamePrefix = typeName.getName() + "$";
			List<Type> allTypes = new ArrayList<>();
			_util.addTLStructuredTypeIdentifiers(connection, type.getModule(), allTypes);
			for (Type typeInModule : allTypes) {
				if (typeInModule.getTypeName().startsWith(assNamePrefix)) {
					Type associationInModule = typeInModule;
					// Association for a reference in the renamed or moved type.
					String newAssociationName;
					if (className != null) {
						newAssociationName = TLStructuredTypeColumns.syntheticAssociationName(className,
							associationInModule.getTypeName().substring(assNamePrefix.length()));
					} else {
						newAssociationName = null;
					}
					_util.updateTLStructuredType(connection, associationInModule, newModule, newAssociationName, null,
						null, (String) null);
					log.info("Updated association " + _util.toString(associationInModule));
				}
			}
		}
	}

}
