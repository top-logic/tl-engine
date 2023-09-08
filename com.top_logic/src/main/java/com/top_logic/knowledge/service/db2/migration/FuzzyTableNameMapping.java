/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBTableMetaObject;

/**
 * {@link TypeMapping} based on a {@link MORepository} which indexes the {@link MetaObject} either
 * by the {@link MetaObject#getName() name} or by its {@link DBTableMetaObject#getDBName() database
 * name}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FuzzyTableNameMapping implements TypeMapping {

	private Map<String, MetaObject> _moByName = new HashMap<>();

	private final Config _config;

	private MORepository _repository;

	/**
	 * Configuration of a {@link FuzzyTableNameMapping}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<FuzzyTableNameMapping> {

		/** Configuration name of {@link #getRenames()}. */
		String RENAMES = "renames";

		/**
		 * The {@link Rename}s of the {@link FuzzyTableNameMapping}.
		 */
		@Name(RENAMES)
		@Key(Rename.NAME_ATTRIBUTE)
		List<Rename> getRenames();

		/**
		 * Configuration of a renaming applied by the {@link FuzzyTableNameMapping}.
		 * 
		 * <p>
		 * A {@link Rename} defines an additional name to find a {@link MetaObject}.
		 * </p>
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		interface Rename extends NamedConfigMandatory {

			/** Name of {@link #getTargetName()} */
			String TARGET_NAME = "target-name";

			/**
			 * The name under which the {@link MetaObject} with "name" {@link #getName()} must also
			 * be found.
			 */
			@Name(TARGET_NAME)
			@Nullable
			String getTargetName();

			/**
			 * The name of the type to which must also be found using {@link #getTargetName()}.
			 */
			@Override
			String getName();
		}
	}

	/**
	 * Creates a {@link FuzzyTableNameMapping} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FuzzyTableNameMapping(InstantiationContext context, Config config) {
		_config = config;
	}

	/**
	 * Initializes the type mapping.
	 * 
	 * @param repository
	 *        The target type
	 */
	@Inject
	@Override
	public void initTypeRepository(MORepository repository) {
		_repository = repository;
		try {
			_moByName.clear();
			installDefaultMapping(repository);
			applyConfiguration(repository);
		} catch (UnknownTypeException ex) {
			throw new ConfigurationError("Invalid type mapping: " + ex.getMessage(), ex);
		}
	}

	private void applyConfiguration(MORepository repository) throws UnknownTypeException {
		for (Config.Rename rename : _config.getRenames()) {
			String targetName = rename.getTargetName();
			MOStructure targetType;
			if (targetName == null) {
				targetType = null;
			} else {
				targetType = (MOStructure) repository.getMetaObject(targetName);
			}
			putMapping(targetType, rename.getName());
		}
	}

	private void installDefaultMapping(MORepository repository) {
		Collection<? extends MetaObject> metaObjects = repository.getMetaObjects();
		for (MetaObject mo : metaObjects) {
			if (!(mo instanceof MOStructure)) {
				continue;
			}
			MOStructure moStructure = (MOStructure) mo;
			if (moStructure instanceof MOClass && ((MOClass) moStructure).isAbstract()) {
				// No table for abstract types
				continue;
			}
			String dbName = moStructure.getDBMapping().getDBName();
			putMapping(moStructure, dbName);
			putMapping(moStructure, mo.getName());
		}
	}

	private void putMapping(MOStructure structure, String dbName) {
		_moByName.put(dbName, structure);
		_moByName.put(nameKey(dbName), structure);
	}

	private String nameKey(String dbName) {
		return dbName.toLowerCase();
	}

	@Override
	public MetaObject getType(String typeName) {
		MetaObject result = _moByName.get(typeName);
		if (result != null || _moByName.containsKey(typeName)) {
			return result;
		}

		String nameKey = nameKey(typeName);
		result = _moByName.get(nameKey);
		if (result != null || _moByName.containsKey(nameKey)) {
			// Remember short-cut.
			_moByName.put(nameKey, result);
			return result;
		}

		try {
			// Repository may create a result on demand
			return _repository.getMetaObject(typeName);
		} catch (UnknownTypeException ex) {
			MetaObject mo = new DeferredMetaObject(typeName);
			_moByName.put(nameKey, mo);
			return mo;
		}
	}

	@Override
	public MOStructure getTableType(String tableName) {
		MetaObject result = getType(tableName);
		if (result instanceof MOStructure) {
			return (MOStructure) result;
		}

		return null;
	}

}
