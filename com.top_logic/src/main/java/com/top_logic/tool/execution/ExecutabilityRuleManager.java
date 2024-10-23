/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ExecutabilityConfig;

/**
 * Registry of configured {@link ExecutabilityRule} instances.
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
@ServiceDependencies({
	// Cached rules may have references to model elements and therefore to the persistency layer.
	PersistencyLayer.Module.class,
})
public final class ExecutabilityRuleManager extends ManagedClass {

	/**
	 * Simplified static access to {@link #getExecutabilityRule(String)}.
	 * 
	 * @param ruleId
	 *        The {@link ExecutabilityRule} ID as defined in the configuration.
	 * 
	 * @return The {@link ExecutabilityRule} defined in the application configuration.
	 */
	public static ExecutabilityRule getRule(String ruleId) {
		return getInstance().getExecutabilityRule(ruleId);
	}

	/**
	 * Key of pre-defined rule {@link InViewModeExecutable}.
	 */
    public static final String KEY_IN_VIEW_MODE = "InViewMode";

	/**
	 * Key of pre-defined rule {@link InEditModeExecutable}.
	 */
    public static final String KEY_IN_EDIT_MODE = "InEditMode";

	/**
	 * Key of pre-defined rule {@link AlwaysExecutable}.
	 */
    public static final String KEY_ALWAYS = "Always";

	/**
	 * Key of pre-defined rule {@link AlwaysDisabled}.
	 */
	public static final String KEY_DISABLED = "Disabled";

	/**
	 * Key of pre-defined rule {@link AlwaysHidden}.
	 */
    public static final String KEY_HIDDEN = "Hidden";

	/**
	 * Rule ID for apply commands.
	 */
	public static final String KEY_GENERAL_APPLY = "GeneralApply";

	/**
	 * Rule ID for save commands.
	 */
	public static final String KEY_GENERAL_SAVE = "GeneralSave";

	/**
	 * Rule ID for create commands.
	 */
	public static final String KEY_GENERAL_CREATE = "GeneralCreate";

	/**
	 * Rule ID for delete commands.
	 */
	public static final String KEY_GENERAL_DELETE = "GeneralDelete";

	/**
	 * Configuration options of {@link ExecutabilityRuleManager}.
	 */
	public interface Config extends ServiceConfiguration<ExecutabilityRuleManager> {

		/**
		 * @see #getDefinitions()
		 */
		String DEFINITIONS_PROPERTY = "definitions";

		/**
		 * Configured rules by their {@link RuleDefinition#getId()}.
		 */
		@Key(RuleDefinition.ID_PROPERTY)
		@Name(DEFINITIONS_PROPERTY)
		Map<String, RuleDefinition> getDefinitions();

	}

	/**
	 * Configuration of a single rule.
	 */
	public interface RuleDefinition extends CommandHandler.ExecutabilityConfig {

		/**
		 * @see #getId()
		 */
		String ID_PROPERTY = "id";

		/**
		 * The rule identifier.
		 * 
		 * @see ExecutabilityRuleManager#getExecutabilityRule(String)
		 */
		@Name(ID_PROPERTY)
		@Format(IdFormat.class)
		@Mandatory
		String getId();

		/**
		 * {@link ConfigurationValueProvider} for {@link String}s restricting the value to letters,
		 * digits and the underscore sign.
		 */
		class IdFormat extends AbstractConfigurationValueProvider<String> {

			/**
			 * Singleton {@link IdFormat} instance.
			 */
			public static final IdFormat INSTANCE = new IdFormat();

			private static final Pattern PATTERN = Pattern.compile("[\\p{IsAlphabetic}\\p{Digit}_]+");

			private IdFormat() {
				super(String.class);
			}

			@Override
			protected String getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				String result = propertyValue.toString();
				if (!PATTERN.matcher(result).matches()) {
					throw new ConfigurationException(I18NConstants.ERROR_INVALID_ID, propertyName, propertyValue);
				}
				return result;
			}

			@Override
			protected String getSpecificationNonNull(String configValue) {
				return configValue;
			}
		}

	}

    /**
     * Cache of rule instances
     */
	private Map<String, ExecutabilityRule> _rules;

	/**
	 * Creates a {@link ExecutabilityRuleManager} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExecutabilityRuleManager(InstantiationContext context, Config config) {
		super(context, config);

		_rules = loadRules(context, config);
		
		Logger.info("Registered " + _rules.size() + " executability rules.", ExecutabilityRuleManager.class);
	}

	private static Map<String, ExecutabilityRule> loadRules(InstantiationContext context, Config config) {
		Map<String, ExecutabilityRule> rules = new ConcurrentHashMap<>();

		for (RuleDefinition entry : config.getDefinitions().values()) {
			ExecutabilityRule createdRule = createRule(context, entry);
			rules.put(entry.getId(), createdRule);
		}
		
		class ExecutabilityRuleAnalyzer implements ReferenceResolver.Analyzer<String, ExecutabilityRule> {
			@Override
			public boolean isAggregation(ExecutabilityRule item) {
				return item instanceof CombinedExecutabilityRule;
			}

			@Override
			public List<ExecutabilityRule> decompose(ExecutabilityRule aggregation) {
				return ((CombinedExecutabilityRule) aggregation).getCombinedRules();
			}

			@Override
			public ExecutabilityRule compose(List<ExecutabilityRule> items) {
				return CombinedExecutabilityRule.combine(items);
			}

			@Override
			public boolean isReference(ExecutabilityRule item) {
				return item instanceof RuleReference;
			}

			@Override
			public String getReferenceId(ExecutabilityRule reference) {
				return ((RuleReference) reference).getRuleId();
			}
		}

		ReferenceResolver<String, ExecutabilityRule> resolver =
			new ReferenceResolver<>(context, new ExecutabilityRuleAnalyzer(), rules);
		resolver.resolve();

		return rules;
	}

	private static ExecutabilityRule createRule(InstantiationContext context, RuleDefinition config) {
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> ruleConfigs = config.getExecutability();
		int ruleCnt = ruleConfigs.size();
		switch (ruleCnt) {
			case 0:
				return AlwaysExecutable.INSTANCE;
			case 1:
				return createRuleAtomic(context, ruleConfigs.get(0));
			default:
				ArrayList<ExecutabilityRule> rules = new ArrayList<>(ruleCnt);
				for (PolymorphicConfiguration<? extends ExecutabilityRule> ruleConfig : ruleConfigs) {
					rules.add(createRuleAtomic(context, ruleConfig));
				}
				return CombinedExecutabilityRule.combine(rules);
		}
	}

	private static ExecutabilityRule createRuleAtomic(InstantiationContext context,
			PolymorphicConfiguration<? extends ExecutabilityRule> ruleConfig) {
		ExecutabilityRule result = context.getInstance(ruleConfig);
		if (result == null) {
			// Error in the configuration, the problem has already been logged.
			return AlwaysExecutable.INSTANCE;
		}
		return result;
	}

	/**
	 * Return a {@link ExecutabilityRule} which is known to the manager.
	 * <p>
	 * If the key is not known the key is assumed to be a class name which will be instantiated as
	 * {@link ExecutabilityRule}
	 * </p>
	 * 
	 * <p>
	 * The key can be a comma separated list of keys/class names. If so, a combined rule will be
	 * returned therefore.
	 * </p>
	 * 
	 * @see #getRule(String) For simplified static access.
	 */
	public ExecutabilityRule getExecutabilityRule(String ruleId) {
		return lookup(null, ruleId, null);
	}

	private ExecutabilityRule lookup(InstantiationContext context, String ruleId, Location location) {
		ExecutabilityRule result = _rules.get(ruleId);
		if (result == null) {
			String message = "Reference to undefined executability rule '" + ruleId + "'"
				+ (location == null ? "." : ": " + location);
			if (context == null) {
				Logger.error(message, ExecutabilityRuleManager.class);
			} else {
				context.error(message);
			}
			return AlwaysExecutable.INSTANCE;
		}
		return result;
	}

	/**
	 * The {@link ExecutabilityRuleManager} service instance.
	 */
    public static synchronized ExecutabilityRuleManager getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
	@Override
	protected void shutDown() {
		_rules = null;

		super.shutDown();
	}

	/**
	 * Resolves the given configuration to an {@link ExecutabilityRule}.
	 * 
	 * @return An {@link ExecutabilityRule} for the given configuration with resolved references.
	 */
	public static ExecutabilityRule resolveRules(InstantiationContext context, ExecutabilityConfig config) {
		return resolveRules(context, config.getExecutability());
	}

	/**
	 * Resolves the given configuration to an {@link ExecutabilityRule}.
	 * 
	 * @return An {@link ExecutabilityRule} for the given configuration with resolved references.
	 */
	public static ExecutabilityRule resolveRules(InstantiationContext context,
			List<PolymorphicConfiguration<? extends ExecutabilityRule>> executability) {
		return getInstance().resolve(context, executability);
	}

	private ExecutabilityRule resolve(InstantiationContext context,
			List<PolymorphicConfiguration<? extends ExecutabilityRule>> executability) {
		List<ExecutabilityRule> rules = new ArrayList<>();
		resolveComposite(rules, context, executability);
		return CombinedExecutabilityRule.combine(rules);
	}

	private void resolve(List<ExecutabilityRule> result,
			InstantiationContext context, PolymorphicConfiguration<? extends ExecutabilityRule> config) {
		Class<?> impl = config.getImplementationClass();
		if (impl == RuleReference.class) {
			// Resolve short-cut.
			String ruleId = ((RuleReference.Config) config).getRuleId();
			result.add(lookup(context, ruleId, config.location()));
		} else if (impl == CombinedExecutabilityRule.class) {
			// Directly use contents.
			resolveComposite(result, context, (ExecutabilityConfig) config);
		} else {
			result.add(context.getInstance(config));
		}
	}

	private void resolveComposite(List<ExecutabilityRule> result, InstantiationContext context, ExecutabilityConfig config) {
		resolveComposite(result, context, config.getExecutability());
	}

	private void resolveComposite(List<ExecutabilityRule> result, InstantiationContext context,
			List<PolymorphicConfiguration<? extends ExecutabilityRule>> executability) {
		for (PolymorphicConfiguration<? extends ExecutabilityRule> config : executability) {
			resolve(result, context, config);
		}
	}

	/**
	 * Module for {@link ExecutabilityRuleManager}.
	 */
	public static final class Module extends TypedRuntimeModule<ExecutabilityRuleManager> {

		/**
		 * {@link Module} singleton.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ExecutabilityRuleManager> getImplementation() {
			return ExecutabilityRuleManager.class;
		}
	}
}
