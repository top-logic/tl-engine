/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.strategy.StrategyCollection;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.config.annotation.TLLocks;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link LockService} that can be configured.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	ModelService.Module.class,
	TokenService.Module.class,
})
public class ConfiguredLockService<C extends ConfiguredLockService.Config<?>> extends LockService
		implements ConfiguredInstance<C> {

	private static final String GLOBAL_LOCK_TYPE = "global";

	private C _config;

	/**
	 * Configuration options for {@link ConfiguredLockService}.
	 */
	public interface Config<I extends ConfiguredLockService<?>> extends ServiceConfiguration<I> {

		/**
		 * Default duration in milliseconds an acquired {@link Lock} keeps valid.
		 */
		@Name("lock-timeout")
		@LongDefault(30 * DateUtil.MINUTE_MILLIS)
		@Format(MillisFormat.class)
		long getLockTimeout();

		/**
		 * The locking strategies for various types.
		 */
		@Name("types")
		@DefaultContainer
		List<TypeConfig> getTypes();

		/**
		 * {@link LockStrategy} configuration for a certain type.
		 */
		@Abstract
		interface TypeConfig extends ConfigurationItem {

			/**
			 * The locking strategies for various abstract operations.
			 */
			@DefaultContainer
			@Name("operations")
			@Key(OperationConfig.NAME_ATTRIBUTE)
			List<OperationConfig> getOperations();

			/**
			 * {@link LockStrategy} configuration for a certain abstract operation.
			 */
			interface OperationConfig extends NamedConfigMandatory, StrategyContainerConfig {

				/**
				 * The name of the abstract operation for which a lock strategy is defined.
				 */
				@Override
				String getName();

				/**
				 * The timeout duration for a lock created for this {@link #getName() operation}.
				 * 
				 * <p>
				 * If not given, the timeout defaults to the global timeout
				 * {@link ConfiguredLockService.Config#getLockTimeout()}.
				 * </p>
				 */
				@Name("lock-timeout")
				@Format(MillisFormat.class)
				Long getLockTimeout();

			}

			@Abstract
			interface StrategyContainerConfig extends ConfigurationItem {
				/**
				 * {@link LockStrategy} to use.
				 */
				@Name("strategies")
				@DefaultContainer
				@Options(fun = AllInAppImplementations.class)
				List<PolymorphicConfiguration<? extends LockStrategy<?>>> getStrategies();
			}
		}

		/**
		 * {@link TypeConfig} for model types.
		 */
		@TagName("model")
		interface ModelTypeConfig extends TypeConfig {
			/**
			 * The model type this configuration is for.
			 */
			@Name("type")
			@Mandatory
			String getType();
		}

		/**
		 * {@link TypeConfig} for Java implementation types.
		 */
		@TagName("java")
		interface JavaTypeConfig extends TypeConfig {
			/**
			 * The model type this configuration is for.
			 */
			@Name("impl")
			@Mandatory
			Class<?> getImpl();
		}

		/**
		 * {@link TypeConfig} for global operations (without base object).
		 */
		@TagName("global")
		interface GlobalTypeConfig extends TypeConfig {
			// Pure marker interface.
		}

	}

	/**
	 * Mapping of type (either {@link Class} or {@link TLClass}) to operation names to used
	 * {@link LockStrategy}.
	 */
	private final Map<Object, Map<String, Pair<Long, LockStrategy<Object>>>> _strategies;

	/**
	 * Creates a {@link ConfiguredLockService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredLockService(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
		_config = config;
		_strategies = build(context, config);
	}

	private Map<Object, Map<String, Pair<Long, LockStrategy<Object>>>> build(InstantiationContext context, C config)
			throws ConfigurationException {
		Map<Object, Map<String, Pair<Long, LockStrategy<Object>>>> strategyByTypeAndOperation = new HashMap<>();
		TLModel model = ModelService.getInstance().getModel();
		for (Config.TypeConfig typeConfig : config.getTypes()) {
			Object type = type(model, typeConfig);
			Map<String, Pair<Long, LockStrategy<Object>>> operations = strategyByTypeAndOperation.get(type);
			if (operations == null) {
				operations = new HashMap<>();
				strategyByTypeAndOperation.put(type, operations);
			}
			for (Config.TypeConfig.OperationConfig operationConfig : typeConfig.getOperations()) {
				String operationName = operationConfig.getName();
				LockStrategy<Object> strategy = createStrategy(context, operationConfig.getStrategies());
				operations.put(operationName, new Pair<>(timeout(operationConfig.getLockTimeout()), strategy));
			}
		}
		return strategyByTypeAndOperation;
	}

	/**
	 * Instantiates a {@link LockStrategy} from a list of
	 * {@link com.top_logic.base.locking.strategy.LockStrategy.Config} configurations.
	 */
	public static LockStrategy<Object> createStrategy(InstantiationContext context,
			List<? extends PolymorphicConfiguration<? extends LockStrategy<?>>> strategiesConfigs) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<LockStrategy<? super Object>> strategies =
			(List) TypedConfiguration.getInstanceList(context, strategiesConfigs);
		return strategies.size() == 1 ? strategies.get(0) : new StrategyCollection<>(strategies);
	}

	private Object type(TLModel model, Config.TypeConfig typeConfig) throws ConfigurationException {
		if (typeConfig instanceof Config.ModelTypeConfig) {
			return TLModelUtil.findType(model, ((Config.ModelTypeConfig) typeConfig).getType());
		} else if (typeConfig instanceof Config.JavaTypeConfig) {
			return ((Config.JavaTypeConfig) typeConfig).getImpl();
		} else {
			return GLOBAL_LOCK_TYPE;
		}
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public long getLockTimeout() {
		return _config.getLockTimeout();
	}

	@Override
	public Set<String> getOperations() {
		return _strategies.values().stream().flatMap(s -> s.keySet().stream()).collect(Collectors.toSet());
	}

	@Override
	public Lock createLock(String operation, Object... models) {
		List<Token> allTokens = new ArrayList<>();
		Long customTimeout = null;
		for (Object model : models) {
			Pair<Long, LockStrategy<Object>> resolution = resolveStrategy(operation, model);
			Long operationTimeout = resolution.getFirst();
			customTimeout = maxTimeout(customTimeout, operationTimeout);
			LockStrategy<Object> strategy = resolution.getSecond();
			List<Token> tokens = strategy.createTokens(model);
			allTokens.addAll(tokens);
		}
		return Lock.createLock(timeout(customTimeout), allTokens);
	}

	private Long maxTimeout(Long t1, Long t2) {
		if (t1 == null || (t2 != null && t2.longValue() > t1.longValue())) {
			return t2;
		} else {
			return t1;
		}
	}

	private Pair<Long, LockStrategy<Object>> resolveStrategy(String operation, Object model) {
		Pair<Long, LockStrategy<Object>> result = null;

		Object type = "unknown";
		if (model instanceof TLObject) {
			TLStructuredType modelType = ((TLObject) model).tType();
			result = findStrategy(modelType, operation);
			type = modelType;
		}

		if (result == null) {
			if (model == null) {
				type = GLOBAL_LOCK_TYPE;
				Map<String, Pair<Long, LockStrategy<Object>>> operations = _strategies.get(type);
				result = operations.get(operation);
			} else {
				Class<? extends Object> javaType = model.getClass();
				result = findStrategy(javaType, operation);
				type = javaType;
			}
		}

		if (result == null) {
			throw new TopLogicException(
				I18NConstants.NO_LOCK_STRATEGY_FOR_OPERATION__TYPE_OPERATION.fill(type.toString(), operation));
		}
		return result;
	}

	private Pair<Long, LockStrategy<Object>> findStrategy(TLStructuredType type, String operation) {
		TLLocks annotation = type.getAnnotation(TLLocks.class);
		if (annotation != null) {
			Config.TypeConfig.OperationConfig operationConfig = annotation.getOperations().get(operation);
			if (operationConfig != null) {
				LockStrategy<Object> strategy = createStrategy(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
					operationConfig.getStrategies());
				return new Pair<>(timeout(operationConfig.getLockTimeout()), strategy);
			}
		}
		
		Map<String, Pair<Long, LockStrategy<Object>>> operations = _strategies.get(type);
		if (operations != null) {
			Pair<Long, LockStrategy<Object>> result = operations.get(operation);
			if (result != null) {
				return result;
			}
		}

		TLClass superType = TLModelUtil.getPrimaryGeneralization(type);
		if (superType == null) {
			return null;
		}

		return findStrategy(superType, operation);
	}

	private Long timeout(Long customTimeout) {
		return customTimeout != null ? customTimeout : getLockTimeout();
	}

	private Pair<Long, LockStrategy<Object>> findStrategy(Class<?> type, String operation) {
		Pair<Long, LockStrategy<Object>> result = findStrategyByClass(type, operation);
		if (result != null) {
			return result;
		}

		return findStrategyByInterface(type, operation);
	}

	private Pair<Long, LockStrategy<Object>> findStrategyByClass(Class<?> type, String operation) {
		Pair<Long, LockStrategy<Object>> result = getLocalLockStrategy(type, operation);
		if (result != null) {
			return result;
		}

		Class<?> superType = type.getSuperclass();
		if (superType == null) {
			return null;
		}

		return findStrategyByClass(superType, operation);
	}

	private Pair<Long, LockStrategy<Object>> findStrategyByInterface(Class<?> type, String operation) {
		for (Class<?> intf : type.getInterfaces()) {
			Pair<Long, LockStrategy<Object>> result = getLocalLockStrategy(intf, operation);
			if (result != null) {
				return result;
			}

			result = findStrategyByInterface(intf, operation);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private Pair<Long, LockStrategy<Object>> getLocalLockStrategy(Class<?> type, String operation) {
		Map<String, Pair<Long, LockStrategy<Object>>> operations = _strategies.get(type);
		if (operations != null) {
			return operations.get(operation);
		} else {
			return null;
		}
	}

}
