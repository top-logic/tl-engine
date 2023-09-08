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
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link MigrationProcessor} for updating the annotations of a model element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateTLAnnotations extends AbstractConfiguredInstance<UpdateTLAnnotations.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link UpdateTLAnnotations}.
	 */
	@TagName("update-annotations")
	public interface Config extends PolymorphicConfiguration<UpdateTLAnnotations>,
			AnnotatedConfig<TLAnnotation> {

		/**
		 * Qualified name of the {@link TLModelPart} whose annotations must be updated.
		 */
		@Mandatory
		String getName();

	}

	/**
	 * Creates a {@link UpdateTLAnnotations} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateTLAnnotations(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error("Creating part migration failed at " + getConfig().location(), ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		String name = getConfig().getName();
		int moduleTypeSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleTypeSepIdx < 0) {
			Util.updateModuleAnnotations(connection, name, getConfig());
			log.info("Updated annotation of module " + name + ".");
			return;
		}
		Module module = Util.getTLModuleOrFail(connection, name.substring(0, moduleTypeSepIdx));
		int typePartSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR, moduleTypeSepIdx + 1);
		if (typePartSepIdx < 0) {
			String typeName = name.substring(moduleTypeSepIdx + 1);
			Util.updateTypeAnnotations(connection, module, typeName, getConfig());
			log.info("Updated annotation of type " + TLModelUtil.qualifiedName(module.getModuleName(), typeName) + ".");
			return;
		}

		Type type = Util.getTLTypeOrFail(connection, module, name.substring(moduleTypeSepIdx + 1, typePartSepIdx));
		String partName = name.substring(typePartSepIdx + 1);
		Util.updateTypePartAnnotations(connection, type, partName, getConfig());
		log.info("Updated annotation of type part "
			+ TLModelUtil.qualifiedName(module.getModuleName(), type.getTypeName(), partName) + ".");
	}

}
