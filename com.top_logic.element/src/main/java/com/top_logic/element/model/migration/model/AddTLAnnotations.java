/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import com.top_logic.model.migration.data.MigrationException;
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
		implements TLModelBaseLineMigrationProcessor {

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
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Adding part annotations failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		String name = getConfig().getName();
		int moduleTypeSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleTypeSepIdx < 0) {
			_util.addModuleAnnotations(log, connection, name, getConfig());
			if (tlModel != null) {
				try {
					MigrationUtils.addModuleAnnotations(log, tlModel, name, getConfig());
				} catch (MigrationException ex) {
					log.error("Unable to add annotations to module '" + name + "'.", ex);
					return false;
				}
			}
			log.info("Added annotations to module '" + name + "'.");
			return true;
		}
		String moduleName = name.substring(0, moduleTypeSepIdx);
		Module module = _util.getTLModuleOrFail(connection, moduleName);
		int typePartSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR, moduleTypeSepIdx + 1);
		if (typePartSepIdx < 0) {
			String typeName = name.substring(moduleTypeSepIdx + 1);
			_util.addTypeAnnotations(log, connection, module, typeName, getConfig());
			if (tlModel != null) {
				try {
					addTypeAnnotations(log, tlModel, moduleName, typeName);
				} catch (MigrationException ex) {
					log.error(
						"Unable to add annotations to type '" + TLModelUtil.qualifiedName(moduleName, typeName) + "'.",
						ex);
					return false;
				}
			}
			log.info("Added annotation to type '" + TLModelUtil.qualifiedName(module.getModuleName(), typeName) + "'.");
			return true;
		}

		String typeName = name.substring(moduleTypeSepIdx + 1, typePartSepIdx);
		Type type = _util.getTLTypeOrFail(connection, module, typeName);
		String partName = name.substring(typePartSepIdx + 1);
		_util.addTypePartAnnotations(log, connection, type, partName, getConfig());
		if (tlModel != null) {
			try {
				addTypePartAnnotations(log, tlModel, moduleName, typeName, partName);
			} catch (MigrationException ex) {
				log.error("Unable to add annotations to type part '"
					+ TLModelUtil.qualifiedTypePartName(moduleName, typeName, partName) + "'.",
					ex);
				return false;
			}
		}

		log.info("Added annotation to type part '"
				+ TLModelUtil.qualifiedTypePartName(module.getModuleName(), type.getTypeName(), partName) + "'.");
		return true;
	}

	private void addTypePartAnnotations(Log log, Document document, String moduleName, String typeName,
			String partName) throws MigrationException {
		Element module = MigrationUtils.getTLModuleOrFail(document, moduleName);
		Element type = MigrationUtils.getTLTypeOrFail(log, module, typeName);
		MigrationUtils.addTypePartAnnotations(log, type, partName, getConfig());
	}

	private void addTypeAnnotations(Log log, Document document, String moduleName, String typeName)
			throws MigrationException {
		Element module = MigrationUtils.getTLModuleOrFail(document, moduleName);
		MigrationUtils.addTypeAnnotations(log, module, typeName, getConfig());
	}

}
