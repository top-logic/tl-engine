/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import static com.top_logic.basic.util.Utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Resolver for {@link NamedModel}s based on their model names.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	TypeIndex.Module.class,
})
public class ModelResolver extends ManagedClass {

	/**
	 * Configuration options for {@link ModelResolver}
	 */
	public interface Config extends ServiceConfiguration<ModelResolver> {

		/** Property name of {@link #getPriorities()}. */
		String PRIORITIES = "priorities";

		/**
		 * List of {@link ModelNamingScheme}s in ascending order of relevance.
		 */
		@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
		List<ModelNamingScheme.Config> getSchemes();

		/**
		 * The declared priority levels.
		 * <p>
		 * The first has the highest priority.
		 * </p>
		 * <p>
		 * These priority levels take precedence over the declaration order priority.
		 * </p>
		 * <p>
		 * This is called "priority level" in the code, to distinguish it from the declaration order
		 * priority ({@link NamingSchemePriority} vs {@link NamingSchemePriorityLevel}. But in the
		 * xml it's called "priority" for simplicity: There is no other "priority" property, it
		 * could be confused with.
		 * </p>
		 */
		@ListBinding()
		@Name(PRIORITIES)
		List<String> getPriorities();

	}

	private static final Object NO_CONTEXT = new Object();

	/**
	 * The name of the default {@link NamingSchemePriorityLevel}.
	 */
	public static final String DEFAULT_PRIORITY_LEVEL_NAME = "default";

	private final Map<Class<?>, ModelNamingScheme<?, ?, ?>> namingSchemeByNameType;

	private final Map<Class<?>, List<ModelNamingScheme<?, ?, ?>>> namingSchemeByModelType;

	private final Map<String, NamingSchemePriorityLevel> _priorityLevels;

	private final Comparator<? super ModelNamingScheme<?, ?, ?>> _priorityOrder;

	private final ConcurrentMap<Class<?>, List<ModelNamingScheme<?, ?, ?>>> _effectiveSchemesByModelType;
	
	private final ConcurrentMap<List<ModelNamingScheme<?, ?, ?>>, List<ModelNamingScheme<?, ?, ?>>> _schemeCombinations;

	/**
	 * Creates a {@link ModelResolver} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ModelResolver(InstantiationContext context, Config config) {
		super(context, config);

		namingSchemeByNameType = new HashMap<>();
		namingSchemeByModelType = new HashMap<>();
		_priorityLevels = createPriorityLevelsMap(config.getPriorities());
		final Map<ModelNamingScheme<?, ?, ?>, NamingSchemePriority> priorityByScheme = new IdentityHashMap<>();

		int priority = 0;
		for (ModelNamingScheme.Config schemeConfig : config.getSchemes()) {
			ModelNamingScheme<?, ?, ?> scheme = context.getInstance(schemeConfig);
			if (scheme != null) {
				enterNameClass(scheme);
				enterModelClass(scheme);

				String priorityLevelName = schemeConfig.getPriority();
				NamingSchemePriorityLevel priorityLevel = _priorityLevels.get(priorityLevelName);
				if (priorityLevel == null) {
					context.error("Unknown NamingSchemePriorityLevel '" + priorityLevelName + "'."
						+ " Known levels: " + _priorityLevels.keySet()
						+ ". Ignoring affected ModelNamingScheme: '" + debug(scheme) + "'.");
					continue;
				}
				priorityByScheme.put(scheme, new NamingSchemePriority(priorityLevel, priority++));
			}
		}

		_priorityOrder = new Comparator<ModelNamingScheme<?, ?, ?>>() {
			@Override
			public int compare(ModelNamingScheme<?, ?, ?> o1, ModelNamingScheme<?, ?, ?> o2) {
				return priorityByScheme.get(o1).compareTo(priorityByScheme.get(o2));
			}
		};

		_effectiveSchemesByModelType = new ConcurrentHashMap<>();
		_schemeCombinations = new ConcurrentHashMap<>();
	}

	private Map<String, NamingSchemePriorityLevel> createPriorityLevelsMap(List<String> priorityLevelNames) {
		Map<String, NamingSchemePriorityLevel> priorityLevels = new ConcurrentHashMap<>();
		int priorityLevel = 0;
		for (String priorityLevelName : priorityLevelNames) {
			priorityLevels.put(priorityLevelName, new NamingSchemePriorityLevel(priorityLevelName, priorityLevel));
			priorityLevel += 1;
		}
		return priorityLevels;
	}

	private void enterModelClass(ModelNamingScheme<?, ?, ?> scheme) {
		Class<?> modelClass = scheme.getModelClass();
		if (modelClass == null) {
			return;
		}
		List<ModelNamingScheme<?, ?, ?>> schemes = namingSchemeByModelType.get(modelClass);
		if (schemes == null) {
			schemes = new ArrayList<>(2);
			namingSchemeByModelType.put(modelClass, schemes);
		}
		schemes.add(scheme);
	}

	private void enterNameClass(ModelNamingScheme<?, ?, ?> scheme) {
		Class<?> nameClass = scheme.getNameClass();
		Collection<Class<?>> concreteNameClasses =
			TypeIndex.getInstance().getSpecializations(nameClass, true, true, false);
		if (concreteNameClasses.isEmpty()) {
			/* This may happen, e.g. when the ModelName of a ModelNamingScheme is just a "template"
			 * for concrete extensions which are also handled by the ModelNamingScheme. When the
			 * concrete ModelNames only exist in an extended project, no concrete name type can be
			 * found. */
			Logger.info("No concrete name types found for name type '" + nameClass.getName() + "' and naming scheme '"
				+ scheme.getClass().getName() + "'.", ModelResolver.class);
		} else {
			if (concreteNameClasses.size() > 1) {
				Logger.info("Registering naming scheme '" + scheme.getClass().getName() + "' for name types: "
					+ concreteNameClasses, ModelResolver.class);
			} else {
				Logger.info("Registering naming scheme '" + scheme.getClass().getName() + "' for name type '"
					+ concreteNameClasses.iterator().next() + "'.", ModelResolver.class);
			}
		}
		for (Class<?> concreteNameClass : concreteNameClasses) {
			ModelNamingScheme<?, ?, ?> clash = namingSchemeByNameType.put(concreteNameClass, scheme);
			if (clash != null) {
				Logger.error("Multiple naming schemes for name type '" + concreteNameClass.getName() + "': '"
					+ scheme.getClass().getName() + "' and '" + clash.getClass().getName() + "'.",
					ModelResolver.class);
			}
		}
	}

	/**
	 * Globally locates the model named by the {@link ModelName} in the given
	 * {@link TLSessionContext}.
	 * 
	 * @param context
	 *        The {@link TLSessionContext} for the locate operation.
	 * @param name
	 *        The {@link ModelName} to resolve. The name was build by
	 *        {@link #buildModelName(Object)}.
	 * 
	 * @return The located model.
	 */
	public static Object locateModel(ActionContext context, ModelName name) {
		return locateModel(context, NO_CONTEXT, name);
	}

	public static <T> List<T> locateModels(ActionContext context, Class<T> exprectedType,
			Collection<? extends ModelName> names) {
		return locateModels(context, exprectedType, NO_CONTEXT, names);
	}

	/**
	 * Locates the model named by the {@link ModelName} in the given {@link TLSessionContext}.
	 * 
	 * @param context
	 *        The {@link TLSessionContext} for the locate operation.
	 * @param valueContext
	 *        The object in whose context the given name was {@link #buildModelName(Object, Object)
	 *        created}.
	 * @param name
	 *        The {@link ModelName} to resolve. The name was build by
	 *        {@link #buildModelName(Object)}.
	 * 
	 * @return The located model.
	 */
	public static Object locateModel(ActionContext context, Object valueContext, ModelName name) {
		return getInstance().locateModelImpl(context, valueContext, name);
	}

	public static <T> List<T> locateModels(ActionContext context, Class<T> expectedType,
			Object valueContext, Collection<? extends ModelName> names) {
		ModelResolver self = getInstance();
		List<T> result = new ArrayList<>(names.size());
		for (ModelName name : names) {
			Object value = self.locateModelImpl(context, valueContext, name);
		
			if ((value == null) || expectedType.isInstance(value)) {
				result.add(expectedType.cast(value));
			} else {
				throw ApplicationAssertions.fail(name, "Expected element of type '" + expectedType.getName()
					+ "' but got an instance of '" + value.getClass().getName() + "': " + value);
			}
		}
		return result;
	}

	/**
	 * Implementation of {@link #locateModel(ActionContext, ModelName)}
	 */
	public Object locateModelImpl(ActionContext context, Object valueContext, ModelName name) {
		if (name == null) {
			return null;
		}
		if (valueContext == null) {
			valueContext = NO_CONTEXT;
		}
		Class<?>[] allInterfaces = name.getClass().getInterfaces();
		if (allInterfaces.length != 1) {
			throw new IllegalArgumentException(
				"Expected configuration item that implements exactly one configuration interface: " + name);
		}

		ModelNamingScheme<?, ?, ?> modelNamingScheme = getSchemeForNameType(allInterfaces[0]);

		return genericLocate(modelNamingScheme, context, valueContext, name);
	}

	private ModelNamingScheme<?, ?, ?> getSchemeForNameType(Class<?> type) {
		ModelNamingScheme<?, ?, ?> modelNamingScheme = namingSchemeByNameType.get(type);
		if (modelNamingScheme == null) {
			throw new AssertionError("No scheme registered for name type: " + type);
		}
		return modelNamingScheme;
	}

	/**
	 * Call {@link ModelNamingScheme#locateModel(ActionContext, Object, ModelName)} in a generic
	 * way.
	 * 
	 * <p>
	 * The guarantee that concrete types match the type parameter is enforced by selecting the
	 * matching scheme for the concrete {@link ModelName} interface.
	 * </p>
	 * 
	 * @param modelNamingScheme
	 *        The {@link ModelNamingScheme} to use for locating the model.
	 * @param context
	 *        The {@link TLSessionContext} for the locate operation.
	 * @param name
	 *        The {@link ModelName} to resolve.
	 * 
	 * @return The located model.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object genericLocate(ModelNamingScheme<?, ?, ?> modelNamingScheme, ActionContext context,
			Object valueContext, ModelName name) {
		assert modelNamingScheme.getNameClass().isInstance(name) : "Model name mismatch, expected '"
			+ modelNamingScheme.getNameClass().getName() + "', got '" + name.getClass().getName() + "'.";

		assert modelNamingScheme.getContextClass().isInstance(valueContext) : "Context type mismatch, expected '"
			+ modelNamingScheme.getContextClass().getName() + "', got '" + valueContext.getClass().getName() + "'.";

		return ((ModelNamingScheme) modelNamingScheme).locateModel(context, valueContext, name);
	}

	/**
	 * The singleton {@link ModelResolver} service instance.
	 */
	public static ModelResolver getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Creates a globally resolvable {@link ModelName} for the given model.
	 * 
	 * @param model
	 *        The model to build a name for.
	 * @return The {@link ModelName} that resolves to the given model, if passed to
	 *         {@link #locateModel(ActionContext, ModelName)}, if a corresponding
	 *         {@link ModelNamingScheme} is registered.
	 */
	public static ModelName buildModelName(Object model) {
		return buildModelName(NO_CONTEXT, model);
	}

	/**
	 * Creates a potentially local {@link ModelName} for the given model.
	 * 
	 * <p>
	 * Make sure to {@link #locateModel(ActionContext, Object, ModelName) resolve} the resulting
	 * name only in the same value context given during name creation. Otherwise, resolving will
	 * fail during replay.
	 * </p>
	 * 
	 * @param valueContext
	 *        The context in which the the created {@link ModelName} is resolved later on.
	 * @param model
	 *        The model to build a name for.
	 * @return The {@link ModelName} that resolves to the given model, if passed to
	 *         {@link #locateModel(ActionContext, ModelName)}, if a corresponding
	 *         {@link ModelNamingScheme} is registered.
	 */
	public static ModelName buildModelName(Object valueContext, Object model) {
		if (model == null) {
			return null;
		}

		Maybe<? extends ModelName> result = getInstance().buildModelNameImpl(valueContext, model);
		if (!result.hasValue()) {
			throw ApplicationAssertions.fail(null,
				"No model naming scheme available for '" + model + "'.");
		}
		return result.get();
	}

	/**
	 * Creates a globally resolvable {@link ModelName} for the given model.
	 * 
	 * @param model
	 *        The model to build a name for.
	 * @return The {@link ModelName} that resolves to the given model, if passed to
	 *         {@link #locateModel(ActionContext, ModelName)}, if a corresponding
	 *         {@link ModelNamingScheme} is registered.
	 */
	public static Maybe<? extends ModelName> buildModelNameIfAvailable(Object model) {
		return buildModelNameIfAvailable(NO_CONTEXT, model);
	}

	/**
	 * Creates a {@link ModelName} for the given model.
	 * 
	 * @param model
	 *        The model to build a name for.
	 * @return The {@link ModelName} that resolves to the given model, if passed to
	 *         {@link #locateModel(ActionContext, ModelName)}, if a corresponding
	 *         {@link ModelNamingScheme} is registered.
	 */
	public static Maybe<? extends ModelName> buildModelNameIfAvailable(Object valueContext, Object model) {
		if (model == null) {
			return Maybe.toMaybeButTreatNullAsValidValue(null);
		}

		return getInstance().buildModelNameImpl(valueContext, model);
	}

	/**
	 * Implementation of {@link #buildModelName(Object)}.
	 */
	public Maybe<? extends ModelName> buildModelNameImpl(Object valueContext, Object model) {
		if (valueContext == null) {
			valueContext = NO_CONTEXT;
		}
		Class<?> type = model.getClass();
		List<ModelNamingScheme<?, ?, ?>> schemes = lookupSchemes(type);
		return buildName(schemes, valueContext, model);
	}

	private List<ModelNamingScheme<?, ?, ?>> lookupSchemes(Class<?> type) {
		List<ModelNamingScheme<?, ?, ?>> schemes = _effectiveSchemesByModelType.get(type);
		if (schemes == null) {
			schemes = new ArrayList<>();
			fillSchemes(type, schemes);
			Collections.sort(schemes, _priorityOrder);
			removeDuplicates(schemes);

			if (schemes.isEmpty()) {
				schemes = Collections.emptyList();
			} else if (schemes.size() == 1) {
				schemes = Collections.<ModelNamingScheme<?, ?, ?>> singletonList(schemes.get(0));
			}

			// Only keep one unique list of applicable schemes.
			schemes = MapUtil.putIfAbsent(_schemeCombinations, schemes, schemes);

			schemes = MapUtil.putIfAbsent(_effectiveSchemesByModelType, type, schemes);
		}
		return schemes;
	}

	private void removeDuplicates(List<ModelNamingScheme<?, ?, ?>> schemes) {
		int ptr = 1;
		for (int n = 1, size = schemes.size(); n < size; n++) {
			ModelNamingScheme<?, ?, ?> scheme = schemes.get(n);
			if (scheme != schemes.get(ptr - 1)) {
				schemes.set(ptr++, scheme);
			}
		}
		while (schemes.size() > ptr) {
			schemes.remove(schemes.size() - 1);
		}
	}

	private void fillSchemes(Class<?> type, List<ModelNamingScheme<?, ?, ?>> result) {
		fillDirect(type, result);
		fillSuperInterfaces(type, result);
		fillSuperClass(type, result);
	}

	private void fillDirect(Class<?> type, List<ModelNamingScheme<?, ?, ?>> result) {
		List<ModelNamingScheme<?, ?, ?>> directSchemes = namingSchemeByModelType.get(type);
		if (directSchemes != null) {
			result.addAll(directSchemes);
		}
	}

	private void fillSuperInterfaces(Class<?> type, List<ModelNamingScheme<?, ?, ?>> result) {
		for (Class<?> interfaceType : type.getInterfaces()) {
			fillRecursive(result, interfaceType);
		}
	}

	private void fillSuperClass(Class<?> type, List<ModelNamingScheme<?, ?, ?>> result) {
		Class<?> superClass = type.getSuperclass();
		if (superClass != null) {
			fillRecursive(result, superClass);
		}
	}

	private void fillRecursive(List<ModelNamingScheme<?, ?, ?>> result, Class<?> superClass) {
		result.addAll(lookupSchemes(superClass));
	}

	private Maybe<? extends ModelName> buildName(List<ModelNamingScheme<?, ?, ?>> schemes, Object valueContext,
			Object model) {
		// Use reverse order to ensure that the scheme that is registered last can override another
		// scheme for the same model type.
		for (int n = schemes.size() - 1; n >= 0; n--) {
			ModelNamingScheme<?, ?, ?> scheme = schemes.get(n);

			if (!scheme.getContextClass().isInstance(valueContext)) {
				// Not applicable in current context.
				continue;
			}

			@SuppressWarnings("unchecked")
			ModelNamingScheme<Object, Object, ?> genericScheme = (ModelNamingScheme<Object, Object, ?>) scheme;

			Maybe<? extends ModelName> result = buildNameSafe(valueContext, model, genericScheme);
			if (result.hasValue()) {
				return result;
			}
		}

		return Maybe.none();
	}

	private Maybe<? extends ModelName> buildNameSafe(Object valueContext, Object model,
			ModelNamingScheme<Object, Object, ?> genericScheme) {
		try {
			return genericScheme.buildName(valueContext, model);
		} catch (RuntimeException ex) {
			/* Unable to use this ModelNamingScheme. This can happen, as not every NamingScheme can
			 * be used in every situation. The caller will try the next one. */
			Logger.warn("Model naming scheme '" + genericScheme + "' crashed when examining object '" + model + "'.",
				ex, ModelResolver.class);
			return Maybe.none();
		}
	}

	/**
	 * Creates a reference for each given value in the given context, if all elements of the given
	 * collection can be referenced.
	 * 
	 * @param values
	 *        The values to create references for.
	 * 
	 * @return The list of created referenced if possible.
	 * 
	 * @throws AssertionError
	 *         if it fails to reference any of the values
	 */
	public static Maybe<List<ModelName>> buildModelNamesIfAvailable(Collection<?> values) {
		return buildModelNamesIfAvailable(NO_CONTEXT, values);
	}

	/**
	 * Creates a reference for each given value in the given context, if all elements of the given
	 * collection can be referenced.
	 * 
	 * @param valueContext
	 *        The context in which the the created {@link ModelName} is resolved later on.
	 * @param values
	 *        The values to create references for.
	 * 
	 * @return The list of created referenced if possible.
	 * 
	 * @throws AssertionError
	 *         if it fails to reference any of the values
	 */
	public static Maybe<List<ModelName>> buildModelNamesIfAvailable(Object valueContext, Collection<?> values) {
		List<ModelName> result = CollectionUtil.createList();
		for (Object value : values) {
			Maybe<? extends ModelName> valueRef = buildModelNameIfAvailable(valueContext, value);
			if (!valueRef.hasValue()) {
				return Maybe.none();
			}
			result.add(valueRef.get());
		}
		return Maybe.some(result);
	}

	/**
	 * Creates a reference for each given value in the given context.
	 * 
	 * @param values
	 *        The values to create references for.
	 * 
	 * @return The list of created referenced.
	 * 
	 * @throws AssertionError
	 *         if it fails to reference any of the values
	 */
	public static List<ModelName> buildModelNames(Collection<?> values) {
		return buildModelNames(NO_CONTEXT, values);
	}

	/**
	 * Creates a reference for each given value in the given context.
	 * 
	 * @param valueContext
	 *        The context in which the the created {@link ModelName} is resolved later on.
	 * @param values
	 *        The values to create references for.
	 * 
	 * @return The list of created referenced.
	 * 
	 * @throws AssertionError
	 *         if it fails to reference any of the values
	 */
	public static List<ModelName> buildModelNames(Object valueContext, Collection<?> values) {
		ArrayList<ModelName> result = new ArrayList<>(values.size());
		for (Object entry : values) {
			result.add(buildModelName(valueContext, entry));
		}
		return result;
	}

	/**
	 * Singleton reference for {@link ModelResolver}.
	 */
	public static class Module extends TypedRuntimeModule<ModelResolver> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ModelResolver> getImplementation() {
			return ModelResolver.class;
		}

	}
}
