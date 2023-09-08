/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.schema.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.dob.MOAlternativeBuilder;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.meta.DeferredIndex;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.schema.config.AlternativeConfig;
import com.top_logic.dob.schema.config.AlternativeConfig.TypeChoice;
import com.top_logic.dob.schema.config.AssociationConfig;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.IndexConfig;
import com.top_logic.dob.schema.config.IndexPartConfig;
import com.top_logic.dob.schema.config.KBXMLConstants;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dob.schema.config.PrimaryKeyConfig;
import com.top_logic.dob.xml.DOXMLConstants;
import com.top_logic.dsa.util.ConfigResourceLoader;

/**
 * Fills a {@link MORepository} with {@link MetaObject}s created from a {@link MetaObjectsConfig}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MORepositoryBuilder {

	/**
	 * Root tag of the {@link MetaObjectsConfig} configuration file.
	 */
	public static final String ROOT_TAG = "objectlist";

	private final Log _log;

	private final MOFactory _typeFactory;

	private final MORepository _repository;

	private Map<String, MetaObject> _typesByName = new HashMap<>();

	final InstantiationContext _context;

	/**
	 * Creates a new {@link MORepositoryBuilder}.
	 * 
	 * @param log
	 *        The error log.
	 * @param typeFactory
	 *        The factory that is used to create new objects in {@link MetaObject} hierarchy.
	 * @param repository
	 *        THe repository to fill with types.
	 */
	public MORepositoryBuilder(Log log, MOFactory typeFactory, MORepository repository) {
		_log = log;
		_typeFactory = typeFactory;
		_repository = repository;
		_context = new DefaultInstantiationContext(_log);
	}

	/**
	 * Fills the stored {@link MORepository} with {@link MetaObject}s derived from the given
	 * {@link MetaObjectsConfig}.
	 */
	public void build(MetaObjectsConfig config) {
		createMetaObjects(config);
		addToRepository();
	}

	private void addToRepository() {
		for (MetaObject type : _typesByName.values()) {
			try {
				repository().addMetaObject(type);
			} catch (DuplicateTypeException ex) {
				error("Duplicate type", ex);
			}
		}
	}

	private void createMetaObjects(MetaObjectsConfig config) {
		ArrayList<Resolver> resolvers = new ArrayList<>();
		for (MetaObjectName typeConfig : config.getTypes().values()) {
			Resolver resolver;
			if (typeConfig instanceof AssociationConfig) {
				resolver = addAssociationMetaObject((AssociationConfig) typeConfig);
			}
			else if (typeConfig instanceof AlternativeConfig) {
				resolver = createAlternative((AlternativeConfig) typeConfig);
			}
			else {
				resolver = createClass((MetaObjectConfig) typeConfig);
			}

			addType(resolver.getType());
			resolvers.add(resolver);
		}

		for (Resolver resolver : resolvers) {
			resolver.resolveSuperType();
		}
		for (Resolver resolver : resolvers) {
			resolver.resolveAttributes(_log);
		}
		for (Resolver resolver : resolvers) {
			resolver.resolveIndices(_log);
		}
	}

	private Resolver createAlternative(AlternativeConfig config) {
		final MOAlternativeBuilder alternative = typeFactory().createAlternativeBuilder(config.getObjectName());
		return new AlternativeResolver(config, alternative);
	}

	private ClassResolver addAssociationMetaObject(AssociationConfig config) {
		MOClass type = createClassBase(config);
		type.setAssociation(true);

		return new AssociationResolver(config, type);
	}

	abstract class Resolver {

		public abstract MetaObject getType();

		public abstract void resolveSuperType();

		public abstract void resolveAttributes(Log log);
		
		public abstract void resolveIndices(Log log);

	}

	class AssociationResolver extends ClassResolver {

		public AssociationResolver(AssociationConfig config, MOClass type) {
			super(config, type);
		}

	}

	class ClassResolver extends Resolver {
		final MetaObjectConfig _config;

		final MOClass _type;

		public ClassResolver(MetaObjectConfig config, MOClass type) {
			_config = config;
			_type = type;
		}

		@Override
		public MetaObject getType() {
			return _type;
		}

		@Override
		public void resolveSuperType() {
			String superClassName = _config.getSuperClass();
			if (superClassName != null) {
				MetaObject superType = lookupType(_config, superClassName);

				if (superType instanceof MOAlternativeBuilder) {
					((MOAlternativeBuilder) superType).registerSpecialisation(_type);
				} else {
					MOClass superClass = (MOClass) superType;
					_type.setSuperclass(superClass);
				}
			}
		}

		@Override
		public void resolveAttributes(Log log) {
			for (AttributeConfig attributeConfig : _config.getAttributes()) {
				String name = attributeConfig.getAttributeName();

				boolean override = attributeConfig.isOverride();
				MOAttribute attribute = _context.getInstance(attributeConfig);

				try {
					if (override) {
						// attribute is an overridden attribute
						_type.overrideAttribute(attribute);
					} else {
						_type.addAttribute(attribute);
					}
				} catch (DuplicateAttributeException ex) {
					log.error("Duplicate Attribute '" + name + "' in type '" + _type.getName() + "'.", ex);
				}
			}
		}

		@Override
		public void resolveIndices(Log log) {
			List<IndexConfig> indexes = _config.getIndex();
			if (!indexes.isEmpty()) {
				for (IndexConfig indexConfig : indexes) {
					addIndex(log, indexConfig);
				}
			}

			PrimaryKeyConfig primaryKey = _config.getPrimaryKey();
			if (primaryKey != null) {
				addPrimaryKey(log, primaryKey);
			}
		}

		private void addPrimaryKey(Log log, PrimaryKeyConfig config) {
			List<IndexPartConfig> partConfigs = config.getIndexParts();
			if (partConfigs.isEmpty()) {
				log.error("Primary key with no columns, ignored");
				return;
			}
			List<Pair<String, ReferencePart>> attributeParts =
				new ArrayList<>(partConfigs.size());
			for (IndexPartConfig partConfig : partConfigs) {
				attributeParts.add(new Pair<>(partConfig.getName(), partConfig.getPart()));
			}
			((MOStructureImpl) _type).setPrimaryKey(DeferredIndex.newPrimaryKey(attributeParts));
		}

		private void addIndex(Log log, IndexConfig indexConfig) {
			String indexName = indexConfig.getName();
			List<IndexPartConfig> partConfigs = indexConfig.getIndexParts();
			if (partConfigs.isEmpty()) {
				log.error("Index " + indexName + " with no columns, ignored");
				return;
			}
			DeferredIndex index =
				new DeferredIndex(indexName, indexConfig.getDBNameEffective(), indexConfig.isUnique(),
					indexConfig.isCustom(), indexConfig.isInMemory(), indexConfig.getDBCompress());
			List<Pair<String, ReferencePart>> attributeParts =
				new ArrayList<>(partConfigs.size());
			for (IndexPartConfig partConfig : partConfigs) {
				attributeParts.add(new Pair<>(partConfig.getName(), partConfig.getPart()));
			}
			index.setAttributeParts(attributeParts);

			((MOClassImpl) _type).addIndex(index);
		}
	}

	private final class AlternativeResolver extends Resolver {

		private final AlternativeConfig _config;

		private final MOAlternativeBuilder _type;

		public AlternativeResolver(AlternativeConfig config, MOAlternativeBuilder alternative) {
			_type = alternative;
			_config = config;
		}

		@Override
		public MetaObject getType() {
			return _type;
		}

		@Override
		public void resolveSuperType() {
			for (TypeChoice specializationConfig : _config.getSpecialisations()) {
				_type.registerSpecialisation(lookupType(specializationConfig, specializationConfig.getName()));
			}
		}

		@Override
		public void resolveAttributes(Log log) {
			// Ignore.
		}

		@Override
		public void resolveIndices(Log log) {
			// Ignore.
		}
	}

	private ClassResolver createClass(MetaObjectConfig config) {
		MOClass type = createClassBase(config);
		return new ClassResolver(config, type);
	}

	private MOClass createClassBase(MetaObjectConfig config) {
		checkCorrectMOType(config);
		MOClassImpl type = (MOClassImpl) typeFactory().createMOClass(config.getObjectName());
		type.setAbstract(config.isAbstract());
		Decision versioned = config.isVersioned();
		if (versioned != Decision.DEFAULT) {
			type.setVersioned(versioned.toBoolean(true));
		}
		type.setDBName(config.getDBNameEffective());
		type.setPkeyStorage(config.isUsePKS());
		type.setCompress(config.getDBCompress());
		type.setDefiningResource(config.location().getResource());
		for (MOAnnotation annotation : config.getAnnotations()) {
			type.addAnnotation(annotation);
		}
		return type;
	}

	MetaObject lookupType(ConfigurationItem context, String name) {
		MetaObject result = _typesByName.get(name);
		if (result == null) {
			// Type not yet registered. Create deferred MO
			result = new DeferredMetaObject(name);
		}
		return result;
	}

	void addType(MetaObject type) {
		_typesByName.put(type.getName(), type);
	}

	/**
	 * Checks that the deprecated property {@link MetaObjectConfig#getObjectType()} is not set to
	 * unexpected value.
	 */
	@SuppressWarnings({ "deprecation", "javadoc" })
	private void checkCorrectMOType(MetaObjectConfig moConfig) {
		if (!CollectionUtil.equals(moConfig.getObjectType(), KBXMLConstants.MO_KNOWLEDGE_OBJECT_TYPE_VALUE)) {
			error("Attribute '" + DOXMLConstants.MO_TYPE_ATTRIBUTE + "' in metaobject '"
				+ moConfig.getObjectName() + "' is not supported.");
		}
	}

	void error(String message) {
		_log.error(message);
	}

	void error(String message, Throwable ex) {
		_log.error(message, ex);
	}

	private MOFactory typeFactory() {
		return _typeFactory;
	}

	private MORepository repository() {
		return _repository;
	}

	/**
	 * Fills the given {@link MORepository} with types read from the given declarations.
	 * 
	 * @param context
	 *        The error log.
	 * @param typeFactory
	 *        The {@link MOFactory} to create type instances.
	 * @param typeDefinitions
	 *        The resources containing the type definitions.
	 * @param repository
	 *        the {@link MORepository} to fill.
	 */
	public static void buildRepository(InstantiationContext context, MOFactory typeFactory,
			List<ResourceDeclaration> typeDefinitions, MORepository repository) throws ConfigurationException {
		MetaObjectsConfig config = readTypeSystem(context, typeDefinitions);
		new MORepositoryBuilder(context, typeFactory, repository).build(config);
	}

	/**
	 * Creates the configuration of the {@link MetaObject}s defined in the given configurations.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param typeDefinitions
	 *        {@link ResourceDeclaration} which holds the definitions of the {@link MetaObject}s.
	 */
	public static MetaObjectsConfig readTypeSystem(InstantiationContext context,
			List<ResourceDeclaration> typeDefinitions) throws ConfigurationException {
		if (typeDefinitions.isEmpty()) {
			// nothing to import
			return TypedConfiguration.newConfigItem(MetaObjectsConfig.class);
		}
		MetaObjectsConfig config =
			ConfigResourceLoader.loadDeclarations(context, ROOT_TAG, MetaObjectsConfig.class, typeDefinitions);
		new ConstraintChecker().check(context, config);
		context.checkErrors();
		return config;
	}

}
