/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.contextmenu.config.ContextMenuCommandsProvider;
import com.top_logic.layout.renderers.RendererRegistryPDFRenderer;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.export.pdf.PDFRenderer;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Services looking up functional classes assigned to objects.
 * 
 * @see #getLabelProvider(Object)
 * @see #getResourceProvider(Object)
 * @see #getRenderer(Object)
 * @see #getMapping(Object)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	ModelService.Module.class,

	// Some providers may depend on configured formats.
	FormatterService.Module.class,

	// Context command lookup.
	CommandHandlerFactory.Module.class,
})
public class LabelProviderService extends ManagedClass implements UpdateListener, ContextMenuCommandsProvider {

	/**
	 * Configuration options for {@link LabelProviderService}.
	 */
	public interface Config extends ServiceConfiguration<LabelProviderService> {

		/**
		 * The provider to use, if none is found otherwise.
		 */
		@Mandatory
		@Name("default-provider")
		PolymorphicConfiguration<LabelProvider> getDefaultProvider();

		/**
		 * Configured {@link LabelProvider}s for types.
		 * 
		 * <p>
		 * The {@link LabelProvider}s defined in this section are used when the context requests
		 * only a plain label (no type image or link). If no {@link LabelProvider} is found in this
		 * section for a certain type, a {@link ResourceProvider} from
		 * {@link #getResourceProviders()} is used as fallback before considering a
		 * {@link LabelProvider} for a super type.
		 * </p>
		 * 
		 * @see LabelProviderService#getLabelProvider(Object)
		 * @see MetaLabelProvider
		 * @see ProviderConfig#getType()
		 */
		@Name("label-providers")
		@EntryTag("provider")
		@Key(value = ProviderConfig.TYPE)
		List<ProviderConfig> getLabelProviders();

		/**
		 * Configured {@link ResourceProvider}s for types.
		 * 
		 * <p>
		 * The {@link ResourceProvider}s defined in this section are used when the context requests
		 * more than a plain label. If no {@link ResourceProvider} is found in this section for a
		 * certain type, a {@link LabelProvider} from {@link #getLabelProviders()} is used as
		 * fallback before considering a {@link ResourceProvider} for a super type. Therefore, it
		 * might be reasonable to define a custom {@link ResourceProvider} for a certain type, if
		 * the {@link LabelProvider} defined for that type is not de-facto a
		 * {@link ResourceProvider}.
		 * </p>
		 * 
		 * @see LabelProviderService#getResourceProvider(Object)
		 * @see MetaResourceProvider
		 * @see ProviderConfig#getType()
		 */
		@Name("resource-providers")
		@EntryTag("provider")
		@Key(value = ProviderConfig.TYPE)
		List<ProviderConfig> getResourceProviders();

		/**
		 * Configured {@link Renderer}s for types.
		 * 
		 * @see RendererConfig#getType()
		 */
		@Name("renderers")
		@EntryTag("renderer")
		@Key(value = RendererConfig.TYPE)
		List<RendererConfig> getRenderers();

		/**
		 * Configured {@link PDFRenderer PDF renderers} for types.
		 * 
		 * @see PDFRendererConfig#getType()
		 */
		@Name("pdf-renderers")
		@Key(value = PDFRendererConfig.TYPE)
		List<PDFRendererConfig> getPDFRenderers();

		/**
		 * Configured {@link Mapping}s for types. These mappings that map an object to a potentially
		 * different object that is used to identify objects in compare views.
		 * 
		 * @see MappingConfig#getType()
		 */
		@Name("mappings")
		@EntryTag("mapping")
		@Key(value = MappingConfig.TYPE)
		List<MappingConfig> getMappings();

		/**
		 * Configuration of type-specific context menu entries.
		 */
		@Name("context-menus")
		@Key(value = ContextMenuConfig.TYPE)
		List<ContextMenuConfig> getContextMenus();
	}

	/**
	 * {@link LabelProvider} assignment to a {@link ProviderConfig#TYPE}.
	 */
	public interface ProviderConfig extends RegisterableConfig {

		/**
		 * @see #getImplementation()
		 */
		String IMPLEMENTATION = "implementation";

		/**
		 * The {@link LabelProvider} implementation for the given {@link #getType()}.
		 */
		@Name(IMPLEMENTATION)
		@Mandatory
		PolymorphicConfiguration<LabelProvider> getImplementation();
	}

	/**
	 * {@link Renderer} assignment to a {@link RendererConfig#TYPE}.
	 */
	public interface RendererConfig extends RegisterableConfig {
		/**
		 * @see #getImplementation()
		 */
		String IMPLEMENTATION = "implementation";

		/**
		 * The {@link Renderer} implementation for the given {@link #getType()}.
		 */
		@Name(IMPLEMENTATION)
		@Mandatory
		PolymorphicConfiguration<Renderer<?>> getImplementation();
	}

	/**
	 * {@link PDFRenderer} assignment to a {@link PDFRendererConfig#TYPE}.
	 */
	public interface PDFRendererConfig extends RegisterableConfig {
		/**
		 * @see #getImplementation()
		 */
		String IMPLEMENTATION = "implementation";

		/**
		 * The {@link PDFRenderer} implementation for the given {@link #getType()}.
		 */
		@Name(IMPLEMENTATION)
		@Mandatory
		PolymorphicConfiguration<PDFRenderer> getImplementation();
	}

	/**
	 * {@link Mapping} assignment to a {@link MappingConfig#TYPE}.
	 */
	public interface MappingConfig extends RegisterableConfig {
		/**
		 * @see #getImplementation()
		 */
		String IMPLEMENTATION = "implementation";

		/**
		 * The {@link Mapping} implementation for the given {@link #getType()}.
		 */
		@Name(IMPLEMENTATION)
		@Mandatory
		PolymorphicConfiguration<Mapping<?, ?>> getImplementation();
	}
	
	/**
	 * Type-specific context menu commands configuration.
	 */
	public interface ContextMenuConfig extends RegisterableConfig {

		/**
		 * @see #getCommands()
		 */
		String COMMANDS = "commands";

		/**
		 * Type-specific commands to add to context menus for the given {@link #getType()}.
		 */
		@Name(COMMANDS)
		@DefaultContainer
		List<CommandHandler.ConfigBase<? extends CommandHandler>> getCommands();

	}

	/**
	 * A type for which a functional class can be registered.
	 */
	@Abstract
	public interface RegisterableConfig extends ConfigurationItem {

		/**
		 * @see #getKind()
		 */
		String KIND = "kind";

		/**
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * The kind of {@link #getType()}.
		 */
		@Name(KIND)
		@Mandatory
		TypeKind getKind();

		/**
		 * The type for which an implementation can be assigned.
		 */
		@Name(TYPE)
		@Mandatory
		String getType();
	}

	/**
	 * Kind of supported types.
	 */
	public enum TypeKind {
		/**
		 * A Java type, the type name is a fully-qualified class name.
		 */
		CODE() {
			@Override
			public Object resolve(String name) throws ConfigurationException {
				return ConfigUtil.getClassForNameMandatory(Object.class, RegisterableConfig.TYPE, name);
			}
		},

		/**
		 * A model type, the type name is a fully-qualified model type name.
		 */
		MODEL() {
			@Override
			public Object resolve(String name) throws ConfigurationException {
				TLType type = TLModelUtil.findType(name);
				if (!(type instanceof TLStructuredType)) {
					throw new ConfigurationException(I18NConstants.ERROR_NO_STRUCTURED_TYPE__TYPE.fill(name),
						RegisterableConfig.TYPE,
						name);
				}
				return WrapperHistoryUtils.getUnversionedIdentity(type);
			}
		};

		/**
		 * Resolves a type name.
		 */
		public abstract Object resolve(String name) throws ConfigurationException;
	}

	private ProviderRegistry _labelProviders;

	private ProviderRegistry _resourceProviders;

	private RendererRegistry _renderers;

	private MappingRegistry _mappings;

	private ContextCommandRegistry _contextCommands;

	private MetaObject _classTable;

	private MetaObject _generalizationTable;

	private PDFRendererRegistry _pdfRenderers;

	private static final Mapping<Object, String> TO_STRING_MAPPING = new Mapping<>() {
		@Override
		public String map(Object input) {
			return String.valueOf(input);
		}
	};

	/**
	 * Creates a {@link LabelProviderService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LabelProviderService(InstantiationContext context, Config config) {
		super(context, config);

		ResourceProvider defaultProvider = LabelResourceProvider.toResourceProvider(context.getInstance(config.getDefaultProvider()));

		_labelProviders = new ProviderRegistry(context, defaultProvider, config.getLabelProviders());
		_resourceProviders = new ProviderRegistry(context, defaultProvider, config.getResourceProviders());
		_renderers = new RendererRegistry(context, ResourceRenderer.INSTANCE, config.getRenderers());
		_pdfRenderers =
			new PDFRendererRegistry(context, RendererRegistryPDFRenderer.INSTANCE, config.getPDFRenderers());
		_mappings = new MappingRegistry(context, TO_STRING_MAPPING, config.getMappings());
		_contextCommands = new ContextCommandRegistry(context, config.getContextMenus());

		_labelProviders.useAsDefault(_resourceProviders);
		_resourceProviders.useAsDefault(_labelProviders);
	}

	@Override
	protected void startUp() {
		super.startUp();

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		_classTable = kb.getMORepository().getTypeOrNull(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE);
		_generalizationTable = kb.getMORepository().getTypeOrNull(ApplicationObjectUtil.META_ELEMENT_GENERALIZATIONS);
		kb.addUpdateListener(this);
	}

	@Override
	protected void shutDown() {
		PersistencyLayer.getKnowledgeBase().removeUpdateListener(this);

		super.shutDown();
	}

	@Override
	public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
		for (ObjectKey key : event.getUpdatedObjectKeys()) {
			if (key.getObjectType() == _classTable) {
				clearCache();
				return;
			}
		}
		for (ObjectKey key : event.getCreatedObjectKeys()) {
			if (key.getObjectType() == _generalizationTable) {
				clearCache();
				return;
			}
		}
		for (ObjectKey key : event.getDeletedObjectKeys()) {
			if (key.getObjectType() == _classTable) {
				removeFromCache(key);
			} else if (key.getObjectType() == _generalizationTable) {
				clearCache();
				return;
			}
		}
	}

	private void removeFromCache(ObjectKey key) {
		_labelProviders.removeFromCache(key);
		_resourceProviders.removeFromCache(key);
	}

	private void clearCache() {
		_labelProviders.clearCache();
		_resourceProviders.clearCache();
	}

	/**
	 * Computes a label for the given object using a configured {@link LabelProvider}.
	 * 
	 * <p>
	 * Note: In contrast to {@link #getLabelProvider(Object)}, this method also works, if the
	 * service is not active. In this case, the {@link Object#toString()} representation of the
	 * object is returned.
	 * </p>
	 */
	public static String getLabel(Object object) {
		if (object == null) {
			return "----";
		}

		Module instance = LabelProviderService.Module.INSTANCE;
		if (instance.isActive()) {
			return instance.getImplementationInstance().getLabelProvider(object).getLabel(object);
		} else {
			return object.toString();
		}
	}

	/**
	 * The {@link LabelProvider} to use for the given object.
	 */
	public LabelProvider getLabelProvider(Object obj) {
		return _labelProviders.lookup(obj);
	}

	/**
	 * The {@link LabelProvider} to use for a given {@link TLStructuredType} without considering any
	 * fall-backs to implementation classes and storage tables.
	 */
	public LabelProvider getLabelProviderForType(TLStructuredType type) {
		return _labelProviders.searchModel(type);
	}

	/**
	 * The {@link ResourceProvider} to use for the given object.
	 */
	public ResourceProvider getResourceProvider(Object obj) {
		return _resourceProviders.lookup(obj);
	}

	/**
	 * The {@link ResourceProvider} to use for a given {@link TLStructuredType} without considering
	 * any fall-backs to implementation classes and storage tables.
	 */
	public ResourceProvider getResourceProviderForType(TLStructuredType type) {
		return _resourceProviders.searchModel(type);
	}

	/**
	 * The {@link Renderer} to use for the given object.
	 */
	public <T> Renderer<? super T> getRenderer(T obj) {
		@SuppressWarnings("unchecked")
		Renderer<? super T> result = (Renderer<? super T>) _renderers.lookup(obj);
		return result;
	}

	/**
	 * The {@link PDFRenderer} to use as PDF renderer for the given object.
	 */
	public PDFRenderer getPDFRenderer(Object obj) {
		return _pdfRenderers.lookup(obj);
	}

	/**
	 * The {@link Mapping} to use for the given object.
	 */
	public Mapping<?, ?> getMapping(Object obj) {
		return _mappings.lookup(obj);
	}

	@Override
	public boolean hasContextMenuCommands(Object obj) {
		return _contextCommands.hasContextMenuCommands(obj);
	}

	/**
	 * Context menu commands configured for certain types.
	 * 
	 * @see Config#getContextMenus()
	 */
	@Override
	public List<CommandHandler> getContextCommands(Object obj) {
		return _contextCommands.getContextCommands(obj);
	}

	/**
	 * The {@link LabelProviderService} singleton.
	 */
	public static LabelProviderService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * A {@link Registry} for {@link ResourceProvider}s.
	 */
	static class ProviderRegistry extends Registry<ResourceProvider> {

		/**
		 * Creates a {@link ProviderRegistry}.
		 */
		public ProviderRegistry(InstantiationContext context, ResourceProvider defaultProvider,
				List<ProviderConfig> list) {
			super(defaultProvider);
			for (ProviderConfig config : list) {
				register(context, config);
			}
		}
	
		private void register(InstantiationContext context, ProviderConfig registerableConfig) {
			LabelProvider labelProvider = context.getInstance(registerableConfig.getImplementation());
			register(context, registerableConfig, LabelResourceProvider.toResourceProvider(labelProvider));
		}
	}

	/**
	 * A {@link Registry} for {@link Renderer}s.
	 *
	 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
	 */
	static class RendererRegistry extends Registry<Renderer<?>> {

		/**
		 * Creates a {@link RendererRegistry}.
		 */
		public RendererRegistry(InstantiationContext context, Renderer<?> defaultRenderer, List<RendererConfig> list) {
			super(defaultRenderer);

			for (RendererConfig config : list) {
				register(context, config, context.getInstance(config.getImplementation()));
			}
		}
	}

	/**
	 * A {@link Registry} for {@link PDFRenderer}s.
	 *
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	static class PDFRendererRegistry extends Registry<PDFRenderer> {

		/**
		 * Creates a {@link RendererRegistry}.
		 */
		public PDFRendererRegistry(InstantiationContext context, PDFRenderer defaultRenderer,
				List<PDFRendererConfig> list) {
			super(defaultRenderer);

			for (PDFRendererConfig config : list) {
				register(context, config, context.getInstance(config.getImplementation()));
			}
		}
	}

	/**
	 * A {@link Registry} for {@link Mapping}s.
	 *
	 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
	 */
	static class MappingRegistry extends Registry<Mapping<?, ?>> {

		/**
		 * Creates a {@link MappingRegistry}.
		 */
		public MappingRegistry(InstantiationContext context, Mapping<?, ?> defaultMapping, List<MappingConfig> list) {
			super(defaultMapping);

			for (MappingConfig config : list) {
				register(context, config, context.getInstance(config.getImplementation()));
			}
		}
	}

	/**
	 * Type-specific registry for {@link CommandHandler}s.
	 */
	public static class ContextCommandRegistry extends Registry<List<CommandHandler>>
			implements ContextMenuCommandsProvider {

		/**
		 * Creates a {@link ContextCommandRegistry}.
		 */
		public ContextCommandRegistry(InstantiationContext context, List<ContextMenuConfig> list) {
			super(Collections.emptyList());

			CommandHandlerFactory factory = CommandHandlerFactory.getInstance();
			for (ContextMenuConfig config : list) {
				List<CommandHandler> handlers = new ArrayList<>(list.size());
				for (CommandHandler.ConfigBase<? extends CommandHandler> handlerConfig : config.getCommands()) {
					CommandHandler handler = factory.getCommand(context, handlerConfig);
					handlers.add(handler);
				}
				register(context, config, Collections.unmodifiableList(handlers));
			}
		}

		@Override
		public boolean hasContextMenuCommands(Object obj) {
			return !lookup(obj).isEmpty();
		}

		@Override
		public List<CommandHandler> getContextCommands(Object obj) {
			return lookup(obj);
		}
	}

	/**
	 * A registry for functional classes that can be assigned to an object.
	 */
	protected abstract static class Registry<T> {
		/** The default class to return when a lookup of the registered class fails. */
		private final T _defaultValue;

		/** The actual registry that maps object type to assigned class instance. */
		private final Map<Object, T> _valueByKey;

		/** The cache. */
		private final ConcurrentMap<Object, T> _cache = new ConcurrentHashMap<>();

		/** The reference queue. */
		private final ReferenceQueue<TLStructuredType> _refQueue = new ReferenceQueue<>();

		/**
		 * Creates a {@link Registry}.
		 */
		public Registry(T defaultValue) {
			_defaultValue = defaultValue;
			_valueByKey = new HashMap<>();
		}

		/**
		 * Adds all registered in the given other {@link Registry} that are not registered in this one.
		 */
		public void useAsDefault(Registry<T> other) {
			other._valueByKey.entrySet()
				.stream()
				.filter(e -> !_valueByKey.containsKey(e.getKey()))
				.forEach(e -> _valueByKey.put(e.getKey(), e.getValue()));
		}

		/**
		 * Removes the entry with the given key from the cache.
		 */
		public void removeFromCache(Object key) {
			_cache.remove(key);
		}

		/**
		 * Drops the complete cache.
		 */
		public void clearCache() {
			_cache.clear();
		}

		static Object key(RegisterableConfig config) throws ConfigurationException {
			return config.getKind().resolve(config.getType());
		}

		/**
		 * Lookup the registered class for the given object.
		 */
		public T lookup(Object obj) {
			flushCache();
			if (obj instanceof TLObject) {
				return lookupModel((TLObject) obj);
			} else if (obj == null) {
				return getDefaultValue();
			} else {
				return lookupJava(obj);
			}
		}

		private void flushCache() {
			while (true) {
				Key.Ref ref = (Key.Ref) _refQueue.poll();
				if (ref == null) {
					break;
				}
				_cache.remove(ref.getKey());
			}
		}

		private T lookupModel(TLObject obj) {
			if (!obj.tValid()) {
				return getDefaultValue();
			}

			TLStructuredType type = obj.tType();
			if (type == null) {
				// Compatibility with test cases in modules not having dynamic types.
				return lookupJava(obj);
			}

			ObjectKey id = id(type);
			if (id != null) {
				T cacheResult = _cache.get(id);
				if (cacheResult != null) {
					return cacheResult;
				}
			}

			T result = searchModel(type);
			if (result == null) {
				result = lookupJava(obj);
			}
			return addToCache(type, result);
		}

		/**
		 * The registered functional class to use for a given {@link TLStructuredType} without considering
		 * any fall-backs to implementation classes and storage tables.
		 */
		public T searchModel(TLStructuredType type) {
			ObjectKey id = id(type);
			T directResult = get(id);
			if (directResult != null) {
				return directResult;
			}
			TLClass generalization = TLModelUtil.getPrimaryGeneralization(type);
			if (generalization == null) {
				return null;
			}
			return searchModel(generalization);
		}

		private ObjectKey id(TLStructuredType type) {
			ObjectKey id = type.tId();
			if (id == null) {
				return null;
			}
			if (id.getHistoryContext() != Revision.CURRENT_REV || id.getBranchContext() != TLContext.TRUNK_ID) {
				// Find resource providers for historic instances, too.
				id = WrapperHistoryUtils.getUnversionedIdentity(type);
			}
			return id;
		}

		private T lookupJava(Object obj) {
			Class<? extends Object> type = obj.getClass();

			T result = _cache.get(type);
			if (result == null) {
				// Cache miss.
				result = addToCache(type, searchClass(type));
			}
			return result;
		}

		private T searchClass(Class<? extends Object> type) {
			T result = null;
			Class<?> generalization = type;
			while (generalization != null) {
				result = get(generalization);
				if (result != null) {
					return result;
				}
				generalization = generalization.getSuperclass();
			}
			result = searchInterfaces(type);
			if (result != null) {
				return result;
			}
			return getDefaultValue();
		}

		private T searchInterfaces(Class<? extends Object> type) {
			Class<?> generalization = type;
			while (generalization != null) {
				T result = searchInterfacesRecursively(generalization);
				if (result != null) {
					return result;
				}
				generalization = generalization.getSuperclass();
			}
			return null;
		}

		private T searchInterfacesRecursively(Class<? extends Object> type) {
			Class<?>[] generalizations = type.getInterfaces();
			for (Class<?> generalization : generalizations) {
				T result = get(generalization);
				if (result != null) {
					return result;
				}
				result = searchInterfacesRecursively(generalization);
				if (result != null) {
					return result;
				}
			}
			return null;
		}

		private T get(Object key) {
			return _valueByKey.get(key);
		}

		private T addToCache(TLStructuredType type, T result) {
			ObjectKey id = id(type);
			if (id == null) {
				// Transient object, do not cache.
				return result;
			}
			Key localKey = new Key(id);
			T actual = MapUtil.putIfAbsent(_cache, localKey, result);
			if (actual == result) {
				// Newly inserted, register for GC removal of type.
				localKey.makeRef(_refQueue, type);
			}
			return actual;
		}

		private T addToCache(Class<?> key, T result) {
			return MapUtil.putIfAbsent(_cache, key, result);
		}

		/**
		 * Getter for {@link #_defaultValue}.
		 */
		protected T getDefaultValue() {
			return _defaultValue;
		}

		/**
		 * Adds the given implementation for the given configuration.
		 */
		protected void register(InstantiationContext context, RegisterableConfig config, T impl) {
			if (impl == null) {
				// Error reported elsewhere.
				return;
			}
			try {
				_valueByKey.put(key(config), impl);
			} catch (ConfigurationException ex) {
				context.error("Cannot resolve type key '" + config.getType() + "' at " + config.location() + ".", ex);
			}
		}

		// Similar to DBObjectKey to observe the GC activity.
		private static final class Key extends DefaultObjectKey {
			@SuppressWarnings("unused")
			private Ref _ref;

			public Key(ObjectKey id) {
				super(TLContext.TRUNK_ID, Revision.CURRENT_REV, id.getObjectType(), id.getObjectName());
			}

			public void makeRef(ReferenceQueue<TLStructuredType> refQueue, TLStructuredType type) {
				_ref = new Ref(refQueue, this, type);
			}

			static class Ref extends SoftReference<TLStructuredType> {
				private ObjectKey _key;

				public Ref(ReferenceQueue<TLStructuredType> refQueue, ObjectKey key, TLStructuredType referent) {
					super(referent, refQueue);
					_key = key;
				}

				public ObjectKey getKey() {
					return _key;
				}
			}
		}

	}

	/**
	 * Singleton reference to {@link LabelProviderService}.
	 */
	public static final class Module extends TypedRuntimeModule<LabelProviderService> {

		/**
		 * Singleton {@link LabelProviderService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<LabelProviderService> getImplementation() {
			return LabelProviderService.class;
		}

	}
}
