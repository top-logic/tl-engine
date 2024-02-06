/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link MigrationProcessor} for adding annotations to a model element. Already existing
 * annotations are replaced.
 * 
 * @see RemoveTLAnnotations
 * @see UpdateTLAnnotations
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AddTLAnnotations extends AbstractConfiguredInstance<AddTLAnnotations.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link AddTLAnnotations}.
	 */
	@TagName("add-annotations")
	public interface Config extends PolymorphicConfiguration<AddTLAnnotations>,
			AnnotatedConfig<TLAnnotation> {

		/**
		 * Qualified name of the {@link TLModelPart} to add annotations to.
		 */
		@Mandatory
		String getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(String value);

	}

	private Util _util;

	/**
	 * Creates a {@link AddTLAnnotations} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AddTLAnnotations(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Adding part annotations failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		String name = getConfig().getName();
		int moduleTypeSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleTypeSepIdx < 0) {
			_util.addModuleAnnotations(log, connection, name, getConfig());
			log.info("Added annotations to module '" + name + "'.");
			return;
		}
		Module module = _util.getTLModuleOrFail(connection, name.substring(0, moduleTypeSepIdx));
		int typePartSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR, moduleTypeSepIdx + 1);
		if (typePartSepIdx < 0) {
			String typeName = name.substring(moduleTypeSepIdx + 1);
			_util.addTypeAnnotations(log, connection, module, typeName, getConfig());
			log.info("Added annotation to type '" + TLModelUtil.qualifiedName(module.getModuleName(), typeName) + "'.");
			return;
		}

		Type type = _util.getTLTypeOrFail(connection, module, name.substring(moduleTypeSepIdx + 1, typePartSepIdx));
		String partName = name.substring(typePartSepIdx + 1);
		_util.addTypePartAnnotations(log, connection, type, partName, getConfig());
		log.info("Added annotation to type part '"
				+ TLModelUtil.qualifiedTypePartName(module.getModuleName(), type.getTypeName(), partName) + "'.");
	}

}
