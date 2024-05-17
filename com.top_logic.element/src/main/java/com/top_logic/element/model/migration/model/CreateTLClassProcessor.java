/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link MigrationProcessor} creating a new {@link TLClass}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTLClassProcessor extends AbstractConfiguredInstance<CreateTLClassProcessor.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link CreateTLClassProcessor}.
	 */
	@TagName("create-class")
	public interface Config
			extends PolymorphicConfiguration<CreateTLClassProcessor>, AnnotatedConfig<TLTypeAnnotation> {

		/** Name for {@link #isAbstract()}. */
		String ABSTRACT = ClassConfig.ABSTRACT;

		/** Name for {@link #isFinal()} */
		String FINAL = ClassConfig.FINAL;

		/**
		 * Qualified name of the new {@link TLClass}.
		 */
		@Mandatory
		QualifiedTypeName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedTypeName value);

		/**
		 * See {@link ClassConfig#isAbstract()}.
		 */
		@Name(ABSTRACT)
		boolean isAbstract();

		/**
		 * Setter for {@link #isAbstract()}.
		 */
		void setAbstract(boolean value);

		/**
		 * See {@link ClassConfig#isFinal()}.
		 */
		@Name(FINAL)
		boolean isFinal();

		/**
		 * Setter for {@link #isFinal()}.
		 */
		void setFinal(boolean value);

		/**
		 * Name of the primary generalization for the new {@link TLClass}.
		 * 
		 * @implNote This value is mandatory for all types not in
		 *           {@link TlModelFactory#TL_MODEL_STRUCTURE} unless
		 *           {@link #isWithoutPrimaryGeneralization()}. At least
		 *           {@link TLObject#TL_OBJECT_TYPE} can be used.
		 */
		QualifiedTypeName getPrimaryGeneralization();

		/**
		 * Setter for {@link #getPrimaryGeneralization()}.
		 */
		void setPrimaryGeneralization(QualifiedTypeName value);

		/**
		 * Whether no primary generalization is given.
		 * 
		 * <p>
		 * Each type needs a primary generalization. At least {@link TLObject#TL_OBJECT_TYPE} can be
		 * used. If {@link #isWithoutPrimaryGeneralization()} is set, the user <b>must</b> ensure
		 * that a later {@link MigrationProcessor} creates a generalization for the type.
		 * </p>
		 */
		@Hidden
		boolean isWithoutPrimaryGeneralization();

		/**
		 * Setter for {@link #isWithoutPrimaryGeneralization()}.
		 */
		void setWithoutPrimaryGeneralization(boolean value);
	}

	private Util _util;

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			internalDoMigration(context, log, connection, tlModel);
			return true;
		} catch (Exception ex) {
			log.error("Class creation failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private void internalDoMigration(MigrationContext context, Log log, PooledConnection connection, Document tlModel)
			throws Exception {
		QualifiedTypeName className = getConfig().getName();
		_util.createTLClass(connection, className,
			getConfig().isAbstract(), getConfig().isFinal(),
			getConfig());
		if (tlModel != null) {
			MigrationUtils.createClassType(log, tlModel, className, getConfig().isAbstract(),
				MigrationUtils.nullIfUnset(getConfig(), Config.FINAL), getConfig());
		}
		log.info("Created class " + _util.qualifiedName(className));

		addPrimaryGeneralization(context, log, connection, tlModel, className);
	}

	private void addPrimaryGeneralization(MigrationContext context, Log log, PooledConnection connection,
			Document tlModel, QualifiedTypeName newClass) {
		QualifiedTypeName primaryGeneralization = getConfig().getPrimaryGeneralization();
		if (primaryGeneralization == null) {
			if (getConfig().isWithoutPrimaryGeneralization()) {
				log.info("Skip generalization creation for '" + _util.qualifiedName(newClass) + "'.");
				return;
			}
			if (!TlModelFactory.TL_MODEL_STRUCTURE.equals(newClass.getModuleName())) {
				log.error("No primary generalization given for new class '" + _util.qualifiedName(newClass)
					+ "'. Use at least "
					+ TLModelUtil.qualifiedName(TlModelFactory.TL_MODEL_STRUCTURE, TLObject.TL_OBJECT_TYPE) + ".");
			}
			return;
		}
		AddTLClassGeneralization processor = primaryGeneralizationProcessor(newClass, primaryGeneralization);
		processor.migrateTLModel(context, log, connection, tlModel);
	}

	private AddTLClassGeneralization primaryGeneralizationProcessor(QualifiedTypeName newClass,
			QualifiedTypeName primaryGeneralization) {
		AddTLClassGeneralization.Config addGeneralization =
			TypedConfiguration.newConfigItem(AddTLClassGeneralization.Config.class);
		addGeneralization.setName(newClass);
		AddTLClassGeneralization.Generalization generalization =
			TypedConfiguration.newConfigItem(AddTLClassGeneralization.Generalization.class);
		generalization.setType(primaryGeneralization);
		addGeneralization.getGeneralizations().add(generalization);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(addGeneralization);
	}

}
