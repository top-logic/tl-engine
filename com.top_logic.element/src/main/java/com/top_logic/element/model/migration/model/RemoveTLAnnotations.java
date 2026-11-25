/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.Module;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} for removing annotations from a model element.
 * 
 * @see AddTLAnnotations
 * @see UpdateTLAnnotations
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveTLAnnotations extends TLModelBaseLineMigrationProcessor<RemoveTLAnnotations.Config> {

	/**
	 * Configuration options of {@link RemoveTLAnnotations}.
	 */
	@TagName("remove-annotations")
	public interface Config extends TLModelBaseLineMigrationProcessor.Config<RemoveTLAnnotations> {

		/**
		 * Qualified name of the {@link TLModelPart} to remove annotations from.
		 */
		@Mandatory
		String getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(String value);

		/**
		 * The annotation types to remove.
		 */
		@DefaultContainer
		@Key(AnnotationConfig.CLASS)
		List<AnnotationConfig> getAnnotations();

	}

	/**
	 * Configuration of an annotation type.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface AnnotationConfig extends ConfigurationItem {

		/** Configuration name of {@link #getAnnotationClass()}. */
		String CLASS = "class";

		/**
		 * The annotation type to remove.
		 */
		@Name(CLASS)
		@Mandatory
		Class<? extends TLAnnotation> getAnnotationClass();

		/**
		 * Setter for {@link #getAnnotationClass()}.
		 */
		void setAnnotationClass(Class<? extends TLAnnotation> value);
	}

	private Util _util;

	/**
	 * Creates a {@link RemoveTLAnnotations} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RemoveTLAnnotations(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.getSQLUtils();
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Removing part annotations failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws Exception {
		String name = getConfig().getName();
		Set<Class<? extends TLAnnotation>> toRemove =
			getConfig().getAnnotations()
				.stream()
				.map(AnnotationConfig::getAnnotationClass)
				.collect(Collectors.toSet());
		int moduleTypeSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (moduleTypeSepIdx < 0) {
			_util.removeModuleAnnotations(log, connection, name, toRemove);
			boolean updateModelBaseline =
				tlModel == null ? false : MigrationUtils.removeModuleAnnotations(log, tlModel, name, toRemove);
			log.info("Removed annotations from module " + name + ".");
			return updateModelBaseline;
		}
		String moduleName = name.substring(0, moduleTypeSepIdx);
		Module module = _util.getTLModule(connection, TLContext.TRUNK_ID, moduleName);
		if (module == null) {
			log.info("No module with name '" + moduleName + "' found.", Log.WARN);
		}
		int typePartSepIdx = name.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR, moduleTypeSepIdx + 1);
		if (typePartSepIdx < 0) {
			String typeName = name.substring(moduleTypeSepIdx + 1);
			if (module != null) {
				_util.removeTypeAnnotations(log, connection, module, typeName, toRemove);
			}
			boolean updateModelBaseline = tlModel == null ? false :
				MigrationUtils.removeTypeAnnotations(log, tlModel, moduleName, typeName, toRemove);
			log.info("Removed annotation from type " + TLModelUtil.qualifiedName(moduleName, typeName) + ".");
			return updateModelBaseline;
		}

		String typeName = name.substring(moduleTypeSepIdx + 1, typePartSepIdx);
		String partName = name.substring(typePartSepIdx + 1);
		if (module != null) {
			Type type = _util.getTLTypeOrNull(connection, module, typeName);
			if (type == null) {
				log.info("No type '" + TLModelUtil.qualifiedName(moduleName, typeName) + "' found.", Log.WARN);
			} else {
				_util.removeTypePartAnnotations(log, connection, type, partName, toRemove);
			}
		}
		boolean updateModelBaseline = tlModel == null ? false :
			MigrationUtils.removeTypePartAnnotations(log, tlModel, moduleName, typeName, partName, toRemove);
		log.info("Removed annotation from type part "
			+ TLModelUtil.qualifiedName(moduleName, typeName, partName) + ".");
		return updateModelBaseline;
	}

}
