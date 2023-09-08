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
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link MigrationProcessor} creating a new {@link TLClass}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLClassProcessor extends AbstractConfiguredInstance<CreateTLClassProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLClassProcessor}.
	 */
	@TagName("create-class")
	public interface Config
			extends PolymorphicConfiguration<CreateTLClassProcessor>, AnnotatedConfig<TLTypeAnnotation> {

		/**
		 * Qualified name of the new {@link TLClass}.
		 */
		@Mandatory
		QualifiedTypeName getName();

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

		/**
		 * Name of the primary generalization for the new {@link TLClass}.
		 */
		QualifiedTypeName getPrimaryGeneralization();

	}

	/**
	 * Creates a {@link CreateTLClassProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTLClassProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Class creation failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedTypeName className = getConfig().getName();
		Util.createTLClass(connection, className,
			getConfig().isAbstract(), getConfig().isFinal(),
			getConfig());
		log.info("Created class " + Util.qualifiedName(className));

		addPrimaryGeneralization(log, connection, className);
	}

	private void addPrimaryGeneralization(Log log, PooledConnection connection, QualifiedTypeName newClass) {
		QualifiedTypeName primaryGeneralization = getConfig().getPrimaryGeneralization();
		if (primaryGeneralization == null) {
			if (!TlModelFactory.TL_MODEL_STRUCTURE.equals(newClass.getModuleName())) {
				log.error("No primary generalization given for new class '" + Util.qualifiedName(newClass)
					+ "'. Use at least "
					+ TLModelUtil.qualifiedName(TlModelFactory.TL_MODEL_STRUCTURE, TLObject.TL_OBJECT_TYPE) + ".");
			}
			return;
		}
		AddTLClassGeneralization.Config addGeneralization =
			TypedConfiguration.newConfigItem(AddTLClassGeneralization.Config.class);
		addGeneralization.setName(newClass);
		AddTLClassGeneralization.Generalization generalization =
			TypedConfiguration.newConfigItem(AddTLClassGeneralization.Generalization.class);
		generalization.setType(primaryGeneralization);
		addGeneralization.getGeneralizations().add(generalization);
		AddTLClassGeneralization processor =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(addGeneralization);
		processor.doMigration(log, connection);
	}

}
