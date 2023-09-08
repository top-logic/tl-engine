/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Settings;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.event.CommitChecker;
import com.top_logic.knowledge.service.event.CommitVetoException;
import com.top_logic.layout.form.values.edit.AllQualifiedTLTypeNames;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLQuery;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.TLRange;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.TLUpdateMode;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.ScopeConfig;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.filter.ModelFilterConfig;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.internal.PersistentQuery;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.list.ListInitializationUtil;
import com.top_logic.util.model.check.AttributeChecker;
import com.top_logic.util.model.check.CombinedChecker;
import com.top_logic.util.model.check.GenericMandatoryCheck;
import com.top_logic.util.model.check.InstanceCheck;
import com.top_logic.util.model.check.NumberRangeCheck;
import com.top_logic.util.model.check.StringSizeCheck;

/**
 * Provides the business model, this application is working on.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	ResourcesModule.Module.class,
	Settings.Module.class,
	AttributeSettings.Module.class
})
public class ModelService extends ConfiguredManagedClass<ModelService.Config<?>> implements CommitChecker {

	/**
	 * Configuration options for {@link ModelService}
	 */
	public interface Config<I extends ModelService> extends ConfiguredManagedClass.Config<I>, ModelFilterConfig {

		/**
		 * @see #getClassifications()
		 */
		String CLASSIFICATIONS = "classifications";

		/**
		 * Configuration of operational aspects of the model.
		 */
		PersistentModelConfig getModel();

		/**
		 * Registered classifications.
		 */
		@Name(CLASSIFICATIONS)
		@Key(ClassificationConfig.NAME_ATTRIBUTE)
		Map<String, ClassificationConfig> getClassifications();
	}

	/**
	 * Configuration options for {@link TLModel}s.
	 */
	public interface PersistentModelConfig extends ConfigurationItem {

		/** @see #getQueries() */
		String QUERIES = "queries";

		/**
		 * Installed query resolvers.
		 */
		@Name(QUERIES)
		@EntryTag("query")
		@Key(QueryConfig.INTERFACE)
		List<QueryConfig> getQueries();

		/**
		 * Configuration of a query resolver implementation.
		 */
		public interface QueryConfig extends ConfigurationItem {
			/** @see #getQueryInterface() */
			String INTERFACE = "interface";

			/** @see #getQueryImplementation() */
			String IMPL = "impl";

			/**
			 * The query interface.
			 * 
			 * @see #getQueryImplementation()
			 * @see TLModel#getQuery(Class)
			 */
			@Name(INTERFACE)
			Class<? extends TLQuery> getQueryInterface();

			/**
			 * The query resolver that implements the {@link #getQueryInterface()}.
			 */
			@Name(IMPL)
			PolymorphicConfiguration<PersistentQuery> getQueryImplementation();
		}
	}

	/**
	 * Configuration of the usage of a single classification.
	 */
	public interface ClassificationConfig extends NamedConfiguration {
	
		/**
		 * @see #getMode()
		 */
		String MODE = "mode";
	
		/**
		 * How the classification is used and updated.
		 */
		@Name(MODE)
		TLUpdateMode.Mode getMode();

	}

	/** The represented model of the application. */
	private TLModel _model;

	private Map<Class<? extends TLQuery>, TLQuery> _queries;

	private final KnowledgeBase _kb;

	private ConcurrentMap<TLStructuredType, InstanceCheck> _checkForType = new ConcurrentHashMap<>();

	/**
	 * Creates a {@link ModelService} from module configuration.
	 */
	public ModelService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	@Override
	protected final void startUp() {
		super.startUp();
		ThreadContext.inSystemContext(ModelService.class, new InContext() {
			@Override
			public void inContext() {
				try {
					startUpInContext();
				} catch (KnowledgeBaseException ex) {
					throw new KnowledgeBaseRuntimeException(ex);
				} catch (ConfigurationException ex) {
					throw new ConfigurationError("Configuration problem during startup.", ex);
				}
			}
		});
	}

	/**
	 * Startup code in {@link ThreadContext#inSystemContext(Class, InContext) system context}.
	 */
	protected void startUpInContext() throws ConfigurationException, KnowledgeBaseException {
		KnowledgeBase kb = kb();
		kb.addCommitChecker(this);

		_model = fetchModel(kb);
		_queries = initQueries();

		initEnums();
	}

	private HashMap<Class<? extends TLQuery>, TLQuery> initQueries() {
		InstantiationContext context = ApplicationConfig.getInstance().getServiceStartupContext();
		PersistentModelConfig modelConfig = getConfig().getModel();
		HashMap<Class<? extends TLQuery>, TLQuery> queries = new HashMap<>();
		if (modelConfig != null) {
			for (PersistentModelConfig.QueryConfig queryConfig : modelConfig.getQueries()) {
				PersistentQuery impl = context.getInstance(queryConfig.getQueryImplementation());
				if (impl != null) {
					{
						impl.init(kb());

						queries.put(queryConfig.getQueryInterface(), impl);
					}
				}
			}
		}
		return queries;
	}

	/**
	 * Build enum types from legacy configurations directly (without creating them within a
	 * {@link TLModule}.
	 */
	protected void initEnums() throws ConfigurationException, IOError, KnowledgeBaseException {
		ScopeConfig enumScope = TypedConfiguration.newConfigItem(ScopeConfig.class);
		try {
			loadLegacyEnums(enumScope);
		} catch (IOException ex) {
			throw new IOError(ex);
		}

		Transaction tx = kb().beginTransaction(Messages.CREATING_CLASSIFICATIONS);
		initEnums(enumScope);
		tx.commit();
	}

	/**
	 * Returns the {@link KnowledgeBase}, this {@link ModelService} uses.
	 */
	protected final KnowledgeBase kb() {
		return _kb;
	}

	@Override
	protected void shutDown() {
		kb().removeCommitChecker(this);

		_model = null;
		_queries = null;

		super.shutDown();
	}

	@Override
	public void checkCommit(UpdateEvent event) throws CommitVetoException {
		dropInvalidCachedChecks(event);

		Set<KnowledgeItem> objectsToCheck = findObjectsToCheck(event);

		List<ResKey> allProblems = new ArrayList<>();
		Sink<ResKey> problems = problem -> {
			if (allProblems.size() < 10) {
				allProblems.add(problem);
			}
		};
		checkConstraints(problems, objectsToCheck);

		TopLogicException ex = null;
		for (ResKey message : allProblems) {
			ex = new TopLogicException(message, ex);
		}
		if (ex != null) {
			throw ex;
		}
	}

	private void dropInvalidCachedChecks(UpdateEvent event) {
		for (KnowledgeItem item : event.getUpdatedObjects().values()) {
			if (item.tTable().getName().equals(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)) {
				TLStructuredType type = ((TLStructuredTypePart) item.getWrapper()).getOwner();
				_checkForType.remove(type);
			}
		}
		for (KnowledgeItem item : event.getCachedDeletedObjects()) {
			if (item.tTable().getName().equals(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE)) {
				_checkForType.remove(item.getWrapper());
			}
		}
	}

	private Set<KnowledgeItem> findObjectsToCheck(UpdateEvent event) {
		Set<KnowledgeItem> result = new LinkedHashSet<>();
		Collection<KnowledgeItem> createdObjects = event.getCreatedObjects().values();
		result.addAll(createdObjects);
		result.addAll(event.getUpdatedObjects().values());
		KnowledgeBase kb = event.getKnowledgeBase();
		addAssociationSources(kb, result, createdObjects);
		addAssociationSources(kb, result, event.getUpdatedObjects().values());

		if (!event.getDeletedObjectKeys().isEmpty()) {
			Set<ObjectKey> affectedByDeletion = new HashSet<>();
			kb.withoutModifications(() -> {
				for (KnowledgeItem item : event.getCachedDeletedObjects()) {
					if (item instanceof KnowledgeAssociation) {
						KnowledgeAssociation link = (KnowledgeAssociation) item;
						affectedByDeletion.add(link.getSourceIdentity());
					}
				}
				return null;
			});

			// Note: IDs must resolve in current context to check only those objects not deleted in
			// the upcoming revision.
			affectedByDeletion.stream().map(kb::resolveObjectKey).filter(Objects::nonNull)
				.filter(KnowledgeItem::isAlive).forEach(result::add);
		}
		return result;
	}

	private void addAssociationSources(KnowledgeBase kb, Set<KnowledgeItem> result, Iterable<KnowledgeItem> potentialLins) {
		for (KnowledgeItem item : potentialLins) {
			if (item instanceof KnowledgeAssociation) {
				KnowledgeAssociation link = (KnowledgeAssociation) item;
				addIfNonNull(result, lookup(kb, link.getSourceIdentity()));
			}
		}
	}

	private static void addIfNonNull(Set<KnowledgeItem> result, KnowledgeItem item) {
		if (item != null) {
			result.add(item);
		}
	}

	private static KnowledgeItem lookup(KnowledgeBase kb, ObjectKey id) {
		return kb.resolveObjectKey(id);
	}

	private void checkConstraints(Sink<ResKey> problems, Collection<? extends KnowledgeItem> items) {
		for (KnowledgeItem item : items) {
			TLObject object = item.getWrapper();
			checkConstraint(problems, object);
		}
	}

	private void checkConstraint(Sink<ResKey> problems, TLObject object) {
		TLStructuredType type = object.tType();
		if (type != null) {
			InstanceCheck check = checkForType(type);
			check.check(problems, object);
		}
	}

	private InstanceCheck checkForType(TLStructuredType type) {
		InstanceCheck result = _checkForType.get(type);
		if (result != null) {
			return result;
		}

		return MapUtil.putIfAbsent(_checkForType, type, createCheck(type));
	}

	private static InstanceCheck createCheck(TLStructuredType type) {
		List<InstanceCheck> checks = new ArrayList<>();
		for (TLStructuredTypePart attribute : type.getAllParts()) {
			if (attribute.isDerived()) {
				// Never create checks for derived attributes. If such a constraint was violated,
				// nobody can do anything about it.
				continue;
			}
			if (attribute.isMandatory()) {
				checks.add(new GenericMandatoryCheck(attribute));
			}

			TLSize sizeAnnotation = attribute.getAnnotation(TLSize.class);
			if (sizeAnnotation != null) {
				checks.add(
					new StringSizeCheck(attribute, sizeAnnotation.getLowerBound(), sizeAnnotation.getUpperBound()));
			}

			TLRange rangeAnnotation = attribute.getAnnotation(TLRange.class);
			if (rangeAnnotation != null) {
				checks.add(
					new NumberRangeCheck(attribute, rangeAnnotation.getMinimum(), rangeAnnotation.getMaximum()));
			}

			TLConstraints constraintAnnotation = attribute.getAnnotation(TLConstraints.class);
			if (constraintAnnotation != null) {
				for (PolymorphicConfiguration<? extends ConstraintCheck> checkConfig : constraintAnnotation
					.getConstraints()) {
					ConstraintCheck check =
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(checkConfig);
					checks.add(new AttributeChecker(check, attribute));
				}
			}
		}
		return CombinedChecker.combine(checks);
	}

	/**
	 * Finds or creates the {@link TLModel} for the model service.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} of the application.
	 */
	protected TLModel fetchModel(KnowledgeBase kb) {
		return new TLModelImpl();
	}

	/**
	 * Loads classifications from legacy configuration files that do not specify a module.
	 */
	private void loadLegacyEnums(ScopeConfig enumScope) throws IOException, ConfigurationException {
		Collection<ClassificationConfig> enumReferences = getConfig().getClassifications().values();
		ListInitializationUtil.loadLegacyEnums(enumScope, enumReferences);
	}

	/**
	 * Instantiates classifications.
	 */
	private void initEnums(ScopeConfig enumScope) {
		for (TypeConfig typeConfig : enumScope.getTypes()) {
			if (typeConfig instanceof EnumConfig) {
				EnumConfig enumConfig = (EnumConfig) typeConfig;
				ListInitializationUtil.checkEnum(kb(), enumConfig);
			}
		}
	}

	/**
	 * Return the application model handled by this service.
	 * 
	 * @return The application model, never <code>null</code>.
	 */
	public TLModel getModel() {
		return _model;
	}

	/**
	 * The generic instance factory for creating instances in this {@link #getModel()}.
	 */
	public TLFactory getFactory() {
		throw new UnsupportedOperationException("No generic object creation without a dynamic model.");
	}

	/**
	 * Implementation of the given query interface type.
	 * 
	 * @param queryInterface
	 *        The query interface.
	 * @return The query resolver implementation.
	 * @throws UnsupportedOperationException
	 *         If this model does not support the requested query resolver.
	 * 
	 * @see TLModel#getQuery(Class)
	 */
	public <T extends TLQuery> T getQuery(Class<T> queryInterface) {
		@SuppressWarnings("unchecked")
		T result = (T) _queries.get(queryInterface);
		if (result == null) {
			throw new UnsupportedOperationException("Query interface '" + queryInterface.getName() + "' not supported.");
		}
		return result;
	}

	/**
	 * Return the application model handled by the {@link ModelService}.
	 * 
	 * @return The requested application model, never <code>null</code>.
	 */
	public static TLModel getApplicationModel() {
		return getInstance().getModel();
	}

	/**
	 * The {@link ModelService} intance.
	 */
	public static ModelService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module definition for the {@link ModelService}.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class Module extends TypedRuntimeModule<ModelService> {

		// Constants

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ModelService> getImplementation() {
			return ModelService.class;
		}

	}

	/**
	 * Filters which {@link TLModelPart}s are available in the current application.
	 * 
	 * Is used for instance by the dynamic layout creation.
	 * 
	 * @see AllQualifiedTLTypeNames
	 * 
	 * @param modelParts
	 *        Is allowed to be null. Is not allowed to contain null.
	 * @return A modifiable and resizable {@link List} containing the all available model parts.
	 */
	public <T extends TLModelPart> List<T> filterModel(Collection<? extends T> modelParts) {
		return getConfig().getModelFilter().filterModel(modelParts);
	}

}
