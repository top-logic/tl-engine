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
 * {@link MigrationProcessor} for updating the annotations of a model element.
 * 
 * @see AddTLAnnotations
 * @see RemoveTLAnnotations
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateTLAnnotations extends AbstractConfiguredInstance<UpdateTLAnnotations.Config>
		implements TLModelBaseLineMigrationProcessor {

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

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(String value);

	}

	private Util _util;

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Updating part annotations failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		String name = getConfig().getName();
		int moduleTypeSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleTypeSepIdx < 0) {
			_util.updateModuleAnnotations(connection, name, getConfig());
			boolean updateModelBaseline = MigrationUtils.setModuleAnnotations(log, tlModel, name, getConfig());
			log.info("Updated annotation of module " + name + ".");
			return updateModelBaseline;
		}
		String moduleName = name.substring(0, moduleTypeSepIdx);
		Module module = _util.getTLModuleOrFail(connection, moduleName);
		int typePartSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR, moduleTypeSepIdx + 1);
		if (typePartSepIdx < 0) {
			String typeName = name.substring(moduleTypeSepIdx + 1);
			_util.updateTypeAnnotations(connection, module, typeName, getConfig());
			boolean updateModelBaseline =
				MigrationUtils.setTypeAnnotations(log, tlModel, moduleName, typeName, getConfig());
			log.info("Updated annotation of type " + TLModelUtil.qualifiedName(module.getModuleName(), typeName) + ".");
			return updateModelBaseline;
		}

		String typeName = name.substring(moduleTypeSepIdx + 1, typePartSepIdx);
		Type type = _util.getTLTypeOrFail(connection, module, typeName);
		String partName = name.substring(typePartSepIdx + 1);
		_util.updateTypePartAnnotations(connection, type, partName, getConfig());
		boolean updateModelBaseline =
			MigrationUtils.setTypePartAnnotations(log, tlModel, moduleName, typeName, partName, getConfig());
		log.info("Updated annotation of type part "
				+ TLModelUtil.qualifiedTypePartName(module.getModuleName(), type.getTypeName(), partName) + ".");
		return updateModelBaseline;
	}

}
