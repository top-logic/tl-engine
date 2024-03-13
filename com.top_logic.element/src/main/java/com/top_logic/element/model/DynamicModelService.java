/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.io.IOError;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ExtendsConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.element.meta.MetaElementException;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.meta.schema.HolderType;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.structured.wrap.StructuredElementWrapperFactory;
import com.top_logic.element.util.model.ElementModelService;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.generated.TLObjectBase;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Service managing the dynamic application model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	InitialGroupManager.Module.class,
	CompatibilityService.Module.class,
	WrapperMetaAttributeUtil.Module.class,
})
public class DynamicModelService extends ElementModelService implements TLFactory {

	/**
	 * Sequence of {@link MigrationProcessor} that can be execute to apply a model patch within a
	 * migration.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface MigrationProcessors extends ConfigurationItem {

		/**
		 * {@link MigrationProcessor}s to execute.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends MigrationProcessor>> getProcessors();

	}

	/**
	 * Property in {@link DBProperties} that stores the factory defaults of the application model.
	 * 
	 * <p>
	 * When new application versions are shipped, this version is used as base-line for applying
	 * model patches to the current application model that may have changed due to in-app
	 * development.
	 * </p>
	 */
	public static final String APPLICATION_MODEL_PROPERTY = "applicationModel";

	/**
	 * Configuration options for {@link DynamicModelService}
	 */
	public interface Config<I extends DynamicModelService> extends ElementModelService.Config<I> {

		/** Property name for {@link #getDeclarations()} */
		String DECLARATIONS = "declarations";

		/** Property name for {@link #getSettings()} */
		String SETTINGS = "settings";

		/**
		 * Automatically adjusts the persistent application model to changes performed in the static
		 * model configuration during boot.
		 */
		@Name("auto-upgrade")
		boolean getAutoUpgrade();

		/**
		 * List of model file references that together build up the the application model.
		 */
		@Name(DECLARATIONS)
		@Key(DeclarationConfig.FILE)
		List<DeclarationConfig> getDeclarations();

		/**
		 * All configured modules of the dynamic application model.
		 */
		@Name(SETTINGS)
		@Key(ModuleConfig.NAME)
		@EntryTag(ModuleConfig.TAG_NAME)
		Map<String, ModuleSetting> getSettings();

		/**
		 * Reference to a model file.
		 */
		interface DeclarationConfig extends ConfigurationItem {

			/** Property name of {@link #getFile()}. */
			String FILE = "file";

			/**
			 * Configuration file with the model types.
			 */
			@Name(FILE)
			@Mandatory
			String getFile();

			/** @see #getFile() */
			void setFile(String config);

		}

		/**
		 * Configuration options for a module of the dynamic application model.
		 * 
		 * <p>
		 * Overlay configuration to {@link ModuleConfig}.
		 * </p>
		 * 
		 * <p>
		 * Note: Ideally, {@link ModuleSetting} would be exactly {@link ModuleConfig} and should
		 * automatically be interpreted as overlay configuration to the unified configuration read
		 * from all model declaration files. Unfortunately, this is not possible by using the type
		 * {@link ModelConfig} as (super interface of) {@link ModuleSetting}, because inheriting of
		 * (mandatory) attributes from the base configuration does not work: The base configuration
		 * is not known at the time, the settings are read. This would require to duplicate all
		 * mandatory attribute values in the settings overlay (e.g. the singleton types, even if
		 * only the role assignments should be specified in the settings).
		 * </p>
		 */
		public interface ModuleSetting extends AnnotatedConfig<TLModuleAnnotation> {

			/** Property name of {@link #isEnabled()}. */
			String ENABLED = "enabled";

			/**
			 * The structure name
			 */
			@Name(ModuleConfig.NAME)
			@Mandatory
			public String getName();

			/**
			 * Whether the module is active in the current application setup.
			 */
			@BooleanDefault(true)
			@Name(ENABLED)
			boolean isEnabled();

			@Override
			@DefaultContainer
			Collection<TLModuleAnnotation> getAnnotations();
		}
	}

	private final ConcurrentMap<String, ModelFactory> _factories = new ConcurrentHashMap<>();

	private ModelConfig _modelConfig;

	/**
	 * Creates a {@link DynamicModelService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DynamicModelService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The (configuration of the) application model.
	 */
	public ModelConfig getModelConfig() {
		return _modelConfig;
	}

	@Override
	public TLFactory getFactory() {
		return this;
	}

	/**
	 * The factory for types in the module of the given name.
	 */
	public ModelFactory getFactory(String moduleName) {
		TLModule module = getModel().getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("No such module: " + moduleName);
		}
		return getFactory(module);
	}

	/**
	 * The factory for types in the given module.
	 */
	public ModelFactory getFactory(TLModule module) {
		String moduleName = module.getName();
		ModelFactory result = _factories.get(moduleName);
		if (result != null) {
			if (result.getModule() != module && module.tValid() && !result.getModule().tValid()) {
				// This may happen when deleting and re-creating modules with the same name in an
				// in-app develompemnt/test cycle.
				_factories.remove(moduleName);
			} else {
				return result;
			}
		}

		try {
			return registerFactory(ApplicationConfig.getInstance().getServiceStartupContext(), module,
				_modelConfig.getModule(moduleName));
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	@Override
	public TLObject createObject(TLClass type, TLObject context, ValueProvider initialValues) {
		return getFactory(type.getModule()).createObject(type, context, initialValues);
	}

	/**
	 * All active factories.
	 */
	public Collection<ModelFactory> getFactories() {
		return Collections.unmodifiableCollection(_factories.values());
	}

	@Override
	protected void startUpInContext() throws ConfigurationException, KnowledgeBaseException {
		super.startUpInContext();

		InstantiationContext context = ApplicationConfig.getInstance().getServiceStartupContext();
		
		Transaction tx = kb().beginTransaction();
		try {
			_modelConfig = new ModelConfigLoader().load(context, config());
			if (_modelConfig == null) {
				// Boot without configuration, read existing configuration from DB.
				_modelConfig = parseConfig(loadStoredConfig(startupConnection()));
			} else {
				new ConstraintChecker().check(context, _modelConfig);
				setupModel();
			}

			startModules(context);
			context.checkErrors();

			tx.commit();
		} catch (KnowledgeBaseException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Adjusts the current application model to changes in the application configuration.
	 */
	private void setupModel() throws SQLException {
		PooledConnection connection = startupConnection();
		String oldConfigXML = loadStoredConfig(connection);
		if (oldConfigXML == null) {
			initModel(connection);
		} else {
			upgradeModel(connection, oldConfigXML);
		}
	}

	private PooledConnection startupConnection() {
		return KBUtils.getCurrentContext(kb()).getConnection();
	}

	private String loadStoredConfig(PooledConnection connection) throws SQLException {
		return DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY, APPLICATION_MODEL_PROPERTY);
	}

	private void initModel(PooledConnection connection) throws SQLException {
		// No old model, initial setup or legacy upgrade.

		Protocol log = log();
		ModelResolver modelResolver = new ModelResolver(log, getModel(), getFactory());
		modelResolver.createModel(_modelConfig);
		modelResolver.complete();

		storeModelConfig(connection);
	}

	private void upgradeModel(PooledConnection connection, String oldConfigXML) throws SQLException {
		if (!config().getAutoUpgrade()) {
			// No incremental update.
			Logger.info("Automatic model upgrade disabled.", DynamicModelService.class);
			return;
		}

		Protocol log = log();

		TLModel oldModel;
		try {
			oldModel = loadTransientModel(log, oldConfigXML);
		} catch (ConfigurationException ex) {
			throw new IllegalStateException("Cannot parse old model configuration, no schema upgrade possible.", ex);
		}

		TLModel newModel = loadTransientModel(log, _modelConfig);
		log.checkErrors();

		CreateModelPatch patchCreator = new CreateModelPatch();
		patchCreator.addPatch(oldModel, newModel);

		List<DiffElement> patch = patchCreator.getPatch();
		if (!patch.isEmpty()) {
			Logger.info("Started incremental model upgrade: " + patch, DynamicModelService.class);

			MigrationProcessors processors = TypedConfiguration.newConfigItem(MigrationProcessors.class);
			ApplyModelPatch.applyPatch(log, getModel(), getFactory(), patch, processors.getProcessors());
			new ConstraintChecker().check(log(), processors);

			storeModelConfig(connection);

			Logger.info("Ended incremental model upgrade.", DynamicModelService.class);

			Logger.info(
				"Note: The following processors can be used in automatic data migration to avoid the incremental model upgrade: "
						+ processors,
				DynamicModelService.class);

		} else {
			Logger.info("No incremental model upgrade necessary.", DynamicModelService.class);
		}
	}

	/**
	 * Parses the given XML as {@link ModelConfig} and instantiates a transient {@link TLModel}.
	 */
	public static TLModel loadTransientModel(Protocol log, String modelXML) throws ConfigurationException {
		ModelConfig modelConfig = parseConfig(modelXML);
		TLModel oldModel = loadTransientModel(log, modelConfig);
		log.checkErrors();
		return oldModel;
	}

	private static ModelConfig parseConfig(String oldConfigXML) throws ConfigurationException {
		/* Note: Unlike the configuration which is read from the file system, the constraints are
		 * not checked on the content from the database. This would only cause problems if a new
		 * constraint was added */
		return (ModelConfig) TypedConfiguration.parse(CharacterContents.newContent(oldConfigXML));
	}

	private void storeModelConfig(PooledConnection connection) throws SQLException {
		// Note: One must not pretty print the stored configuration, since this would change
		// multi-line text properties (such as search expression source code). In consequence, the
		// stored model would not match the current configuration and the model would be migrated
		// during each boot.
		String currentConfigXML = TypedConfiguration.toStringRaw(_modelConfig);
		DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, APPLICATION_MODEL_PROPERTY, currentConfigXML);
	}

	/**
	 * Instantiates the given {@link ModelConfig} into a transient {@link TLModel}.
	 */
	public static TLModel loadTransientModel(Protocol log, ModelConfig modelConfig) {
		return loadModel(log, new TLModelImpl(), null, modelConfig);
	}

	/**
	 * Instantiates the given {@link ModelConfig} in the given {@link TLModel}.
	 */
	private static TLModel loadModel(Protocol log, TLModel model, TLFactory factory, ModelConfig modelConfig) {
		ModelResolver resolver = new ModelResolver(log, model, factory);
		resolver.createModel(modelConfig);
		resolver.complete();

		return model;
	}

	@Override
	protected void initEnums() throws ConfigurationException, IOError, KnowledgeBaseException {
		// Deactivated, even legacy enums are now directly created within their module.
	}

	private Config<?> config() {
		return (Config<?>) getConfig();
	}

	private void startModules(InstantiationContext context) {
		for (TLModule module : getModel().getModules()) {
			startModule(context, module);
		}
	}

	private void startModule(InstantiationContext context, TLModule module) {
		context.info("Starting module '" + module + "'.");
		getFactory(module);
	}

	/**
	 * @see ApplicationObjectUtil#iTableName(TLType)
	 * 
	 * @see #iTableTypeName(String)
	 */
	public static String iTableName(TLType tableType) {
		return ApplicationObjectUtil.iTableName(tableType);
	}

	/**
	 * @see ApplicationObjectUtil#iTableTypeName(String)
	 * 
	 * @see #iTableName(TLType)
	 */
	public static String iTableTypeName(String tableName) {
		return ApplicationObjectUtil.iTableTypeName(tableName);
	}

	private ModelFactory registerFactory(final InstantiationContext context, TLModule module, ModuleConfig moduleConfig)
			throws ConfigurationException {
		String moduleName = module.getName();

		ModelFactory newFactory = createFactory(context, moduleName);
		newFactory.startUp(moduleConfig, module);

		return MapUtil.putIfAbsent(_factories, moduleName, newFactory);
	}

	private ModelFactory createFactory(InstantiationContext context, String moduleName) throws ConfigurationException {
		String factoryClassName = TLModelNamingConvention.factoryClassName(getModel().getModule(moduleName));
		Class<?> implClass;
		if (factoryClassName == null) {
			implClass = StructuredElementWrapperFactory.class;
		} else {
			try {
				implClass = Class.forName(factoryClassName);
				if (!ModelFactory.class.isAssignableFrom(implClass)) {
					context.info(
						"Factory '" + factoryClassName + "' is no '" + ModelFactory.class.getName()
							+ "', using default.",
						Log.WARN);
					implClass = StructuredElementWrapperFactory.class;
				}
			} catch (ClassNotFoundException ex) {
				context.info(
					"No custom factory '" + factoryClassName + "' found for module '" + moduleName
						+ "', using default.");
				implClass = StructuredElementWrapperFactory.class;
			}
		}

		return (ModelFactory) ConfigUtil.newInstance(implClass);
	}

	@Override
	protected void shutDown() {
		shutDownFactories();
		_modelConfig = null;

		super.shutDown();
	}

	private void shutDownFactories() {
		for (ModelFactory factory : _factories.values()) {
			factory.shutDown();
		}
		_factories.clear();
	}

	/**
	 * Adds a model fragment to the existing model.
	 * 
	 * @param modelFragment
	 *        The model to be merged with the existing model.
	 */
	public void installFragment(ModelConfig modelFragment) {
		for (ModuleConfig moduleConf : modelFragment.getModules()) {
			shutdownModule(moduleConf.getName());
		}

		Protocol log = log();
		ModelResolver modelResolver = new ModelResolver(log, getModel(), getFactory());
		modelResolver.createModel(modelFragment);
		modelResolver.complete();
			
		InstantiationContext context = ApplicationConfig.getInstance().getServiceStartupContext();
		for (ModuleConfig moduleConf : modelFragment.getModules()) {
			startModule(context, modelResolver.getModel().getModule(moduleConf.getName()));
		}
	}

	private void shutdownModule(String moduleName) {
		ModelFactory factory = _factories.remove(moduleName);
		if (factory != null) {
			factory.shutDown();
		}
	}

	private static Protocol log() {
		return new BufferingProtocol(new LogProtocol(DynamicModelService.class));
	}

	/**
	 * @see #getFactory(String)
	 */
	public static ModelFactory getFactoryFor(String moduleName) {
		return getInstance().getFactory(moduleName);
	}

	/**
	 * @see #getFactories()
	 */
	public static Iterable<ModelFactory> getAllFactories() {
		return getInstance().getFactories();
	}

	/**
	 * Access to the {@link DynamicModelService} singleton.
	 */
	public static DynamicModelService getInstance() {
		return (DynamicModelService) ModelService.getInstance();
	}

	/**
	 * @see ModelFactory#setupLocalScope(TLModule, TLScope, String, TLFactory)
	 */
	public void setupLocalScope(TLModule module, TLScope scopeInstance, String className) {
		getFactory(module).setupLocalScope(module, scopeInstance, className, getFactory());
	}

	/**
	 * @see DynamicModelService#lookupType(TLModel, Object, String, String, String)
	 */
	public TLType lookupType(Object scopeBase, String scopeRef, String moduleName, String interfaceName) {
		return lookupType(getModel(), scopeBase, scopeRef, moduleName, interfaceName);
	}

	/**
	 * Find the {@link TLType} with the given meta element type relative to the given attributed
	 * object.
	 * 
	 * <p>
	 * The search of the requested {@link TLType} is done according to the scope reference
	 * specification:
	 * </p>
	 * 
	 * <ul>
	 * <li>{@link HolderType#GLOBAL} for a search for a global {@link TLType}.</li>
	 * <li>{@link HolderType#THIS} for looking up the {@link TLType} within the given scope base
	 * object.</li>
	 * <li>{@link HolderType#PARENT} for looking up the {@link TLType} within the parent node of the
	 * scope base object (in a structure).</li>
	 * <li>An {@link ClassConfig#getName() class type name} to search the {@link TLType} in the
	 * first ancestor of the given scope base object that is of the given type.</li>
	 * </ul>
	 * 
	 * @param scopeBase
	 *        The original object relative to which the {@link TLType} is searched. May be
	 *        <code>null</code> in a search for global {@link TLType}s.
	 * @param scopeRef
	 *        See above.
	 * @param moduleName
	 *        The module that defines the class type.
	 * @param interfaceName
	 *        The type of the searched {@link TLType} (must not be <code>null</code>).
	 * @return The {@link TLType} matching the search criteria, or <code>null</code>, if none is
	 *         found.
	 */
	public static TLType lookupType(TLModel model, Object scopeBase, String scopeRef, String moduleName,
			String interfaceName) {
		if (StringServices.isEmpty(interfaceName)) {
			return null;
		}
		if (StringServices.isEmpty(scopeRef)) {
			throw new IllegalArgumentException(
				"Missing scope reference in search for interface '" + interfaceName + "'.");
		}
	
		TLScope scope;
		try {
			scope = HolderType.findScope(scopeBase, scopeRef);
		} catch (IllegalArgumentException ex) {
			throw new MetaElementException("Scope lookup '" + scopeRef + "' in '" + scopeBase + "' failed.", ex);
		}
		if (scope == null) {
			if (interfaceName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) != -1) {
				return TLModelUtil.findType(model, interfaceName);
			} else {
				TLModule module = model.getModule(moduleName);
				if (module == null) {
					return null;
				}
				return module.getType(interfaceName);
			}
		} else {
			return scope.getType(interfaceName);
		}
	}

	/**
	 * Ensures that all object types in the given module have at least
	 * {@link TLObjectBase#TL_OBJECT_TYPE} as super type.
	 * 
	 * @param moduleConfig
	 *        Module to adapt.
	 */
	public static void addTLObjectExtension(ModuleConfig moduleConfig) {
		if (moduleConfig.getName().equals(TlModelFactory.TL_MODEL_STRUCTURE)) {
			return;
		}
		for (TypeConfig type : moduleConfig.getTypes()) {
			if (!(type instanceof ObjectTypeConfig)) {
				continue;
			}
			addTLObjectExtension((ObjectTypeConfig) type);
		}
	}

	private static List<ExtendsConfig> addTLObjectExtension(ObjectTypeConfig objectType) {
		List<ExtendsConfig> generalizations = objectType.getGeneralizations();
		if (generalizations.isEmpty()) {
			generalizations.add(newTLObjectExtension());
		}
		return generalizations;
	}

	private static ExtendsConfig newTLObjectExtension() {
		ExtendsConfig extension = TypedConfiguration.newConfigItem(ExtendsConfig.class);
		extension.setQualifiedTypeName(
			TLModelUtil.qualifiedName(TlModelFactory.TL_MODEL_STRUCTURE, TLObject.TL_OBJECT_TYPE));
		return extension;
	}

	/**
	 * Loads the given XML content and extends the given model with the loaded {@link ModelConfig}.
	 *
	 * @param log
	 *        The {@link Log} to report progress to.
	 * @param model
	 *        The {@link TLModel} to extend.
	 * @param factory
	 *        The {@link TLFactory} to create instances in the given model.
	 * @param modelXml
	 *        The serialized {@link ModelConfig} to load and apply.
	 *
	 * @see DynamicModelService#extendModel(Protocol, TLModel, TLFactory, ModelConfig)
	 */
	public static void extendModel(Protocol log, TLModel model, TLFactory factory, BinaryContent modelXml) {
		extendModel(log, model, factory, DefinitionReader.readElementConfig(modelXml));
	}

	/**
	 * Extends the given model with the given {@link ModelConfig}.
	 * 
	 * @param log
	 *        The {@link Log} to report progress to.
	 * @param tlModel
	 *        The {@link TLModel} to extend.
	 * @param factory
	 *        The {@link TLFactory} to create instances in the given model.
	 * @param modelConfig
	 *        {@link ModelConfig} to instantiate in within the given {@link TLModel}.
	 */
	public static void extendModel(Protocol log, TLModel tlModel, TLFactory factory, ModelConfig modelConfig) {
		ModelResolver modelResolver = new ModelResolver(log, tlModel, factory);
		extendModel(log, tlModel, modelResolver, modelConfig);
		modelResolver.complete();
	}

	/**
	 * Extends the given model with the given {@link ModelConfig}.
	 *
	 * @param log
	 *        The {@link Log} to report progress to.
	 * @param model
	 *        The {@link TLModel} to extend.
	 * @param modelResolver
	 *        The algorithm for model element instantiation.
	 * @param modelConfig
	 *        {@link ModelConfig} to instantiate in within the given {@link TLModel}.
	 */
	public static void extendModel(Log log, TLModel model,
			ModelResolver modelResolver, ModelConfig modelConfig) {
		modelResolver.createModel(modelConfig);
	}

}
