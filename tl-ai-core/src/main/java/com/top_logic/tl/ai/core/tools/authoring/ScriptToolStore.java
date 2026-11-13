/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.tl.ai.core.tools.registry.ToolDescriptions;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.registry.ToolFactory;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Read/mutate access to TL-Script tools held by a named {@link ScriptToolFactory}
 * inside a {@link com.top_logic.tl.ai.tool.api.registry.ToolRegistryService.Config}.
 *
 * <p>
 * Pure in-memory operations with no I/O: callers persist the wrapped config separately. All
 * mutations validate the TL-Script body and the parameter declaration before applying.
 * </p>
 */
public class ScriptToolStore {

	/**
	 * Default factory name for agent-created TL-Script tools.
	 */
	public static final String DEFAULT_FACTORY = "custom";

	/**
	 * Declaration of one TL-Script tool parameter as supplied by the caller.
	 *
	 * <p>
	 * Describes one property of the tool's {@link ScriptToolConfig#getParameters() parameter
	 * declaration}: a value parameter has a Java {@link #type()}, an object parameter a model
	 * {@link #objectType()}.
	 * </p>
	 *
	 * @param name
	 *        Parameter name, bound as {@code $name} in the script body.
	 * @param type
	 *        Java type of a value parameter: {@code String}, {@code int}, {@code long},
	 *        {@code double}, {@code boolean}, or the qualified name of a Java enum class. Empty for
	 *        {@code String}; ignored for an object parameter.
	 * @param objectType
	 *        Qualified model type of an object parameter, e.g. {@code my.module:MyType}; the agent
	 *        passes the id of an object of this type or of a specialization. Empty for a value
	 *        parameter.
	 * @param description
	 *        What the parameter means, told to the model.
	 * @param mandatory
	 *        Whether the parameter must be supplied.
	 * @param multiple
	 *        Whether the parameter holds a list of values: of texts, or of objects.
	 * @param defaultValue
	 *        The value used when an optional parameter is omitted, in its text form; empty/null for
	 *        none.
	 * @param options
	 *        Qualified name of an existing function class computing the allowed values (as
	 *        {@code <options fun="..."/>}); empty/null for none.
	 */
	public record ParamSpec(String name, String type, String objectType, String description, boolean mandatory,
			boolean multiple, String defaultValue, String options) {

		/** JSON field of {@link #name()}. */
		public static final String NAME = "name";

		/** JSON field of {@link #type()}. */
		public static final String TYPE = "type";

		/** JSON field of {@link #objectType()}. */
		public static final String OBJECT_TYPE = "objectType";

		/** JSON field of {@link #description()}. */
		public static final String DESCRIPTION = "description";

		/** JSON field of {@link #mandatory()}. */
		public static final String MANDATORY = "mandatory";

		/** JSON field of {@link #multiple()}. */
		public static final String MULTIPLE = "multiple";

		/** JSON field of {@link #defaultValue()}. */
		public static final String DEFAULT_VALUE = "defaultValue";

		/** JSON field of {@link #options()}. */
		public static final String OPTIONS = "options";

		/**
		 * How the model is told what a parameter declaration consists of.
		 */
		public static final String SCHEMA_DESCRIPTION = "Declared parameters, bound as $name in the body. "
			+ "Each object has: " + NAME + " (string), " + DESCRIPTION + " (string, tells the caller what to "
			+ "pass), " + MANDATORY + " (boolean). For a value: " + TYPE + " - String (default), int, long, "
			+ "double, boolean, or the qualified class name of a Java enum, whose constants are the allowed "
			+ "values. For a model object: " + OBJECT_TYPE + " - the qualified model type, e.g. "
			+ "my.module:MyType; the caller passes the id of an object as tools report it, and the body gets "
			+ "the object. Optional: " + MULTIPLE + " (boolean, a list of texts or of objects, passed as "
			+ "JSON array), " + DEFAULT_VALUE + " (a fixed value in text form, used when an optional "
			+ "parameter is omitted - not an expression), " + OPTIONS + " (the qualified class name of an "
			+ "existing options function computing the allowed values, as in <options fun=\"...\"/>; only "
			+ "an existing class can be named).";
	}

	private final ToolRegistryService.Config<?> _config;

	private final String _factoryName;

	/**
	 * Creates a {@link ScriptToolStore} targeting the {@link #DEFAULT_FACTORY}.
	 */
	public ScriptToolStore(ToolRegistryService.Config<?> config) {
		this(config, DEFAULT_FACTORY);
	}

	/**
	 * Creates a {@link ScriptToolStore} targeting the given factory name.
	 */
	public ScriptToolStore(ToolRegistryService.Config<?> config, String factoryName) {
		_config = config;
		_factoryName = factoryName;
	}

	/**
	 * Creates a new TL-Script tool in the target factory.
	 *
	 * @throws IllegalArgumentException
	 *         If the name is blank or already used, a parameter type does not resolve, or the
	 *         script body does not parse/compile.
	 */
	public void create(String name, String label, String description, List<ParamSpec> parameters,
			String implementation, String mode, boolean concurrencySafe, String commitMessage) {
		requireName(name);
		Map<String, ScriptToolConfig> tools = factoryTools();
		if (tools.containsKey(name)) {
			throw new IllegalArgumentException("A script tool named '" + name + "' already exists.");
		}
		tools.put(name, buildConfig(name, label, description, ScriptToolParameters.declaration(parameters),
			implementation, mode, concurrencySafe, commitMessage, null));
	}

	/**
	 * Returns the named tool config, or {@code null} if absent.
	 */
	public ScriptToolConfig get(String name) {
		return factoryTools().get(name);
	}

	/**
	 * Returns all TL-Script tools in the {@code custom} factory.
	 */
	public List<ScriptToolConfig> list() {
		return new ArrayList<>(factoryTools().values());
	}

	/**
	 * A tool configuration from the given values.
	 *
	 * @param parameters
	 *        The declaration of the parameters; {@code null} for none.
	 * @param commitMessage
	 *        The source of the {@link ScriptToolConfig#getCommitMessage() commit message} expression;
	 *        <code>null</code> or blank for none.
	 * @param existing
	 *        The configuration the result replaces, whose properties this store does not author are
	 *        kept; <code>null</code> for a new tool.
	 */
	private ScriptToolConfig buildConfig(String name, String label, String description,
			ConfigurationDescriptorConfig parameters, String implementation, String mode, boolean concurrencySafe,
			String commitMessage, ScriptToolConfig existing) {
		ScriptToolConfig cfg = TypedConfiguration.newConfigItem(ScriptToolConfig.class);
		cfg.setName(name);
		if (label != null && !label.isBlank()) {
			cfg.setLabel(ResKey.text(label));
		}
		if (description != null && !description.isBlank()) {
			cfg.setDescription(new DefaultHtmlResKey(ResKey.text(description)));
		}
		cfg.setImplementation(parseScript(implementation));
		cfg.setParameters(parameters);
		cfg.update(cfg.descriptor().getProperty(ScriptToolConfig.MODE), parseMode(mode));
		cfg.update(cfg.descriptor().getProperty(ScriptToolConfig.CONCURRENCY_SAFE),
			Boolean.valueOf(concurrencySafe));
		if (commitMessage != null && !commitMessage.isBlank()) {
			cfg.update(cfg.descriptor().getProperty(ScriptToolConfig.COMMIT_MESSAGE),
				parseExpression(ScriptToolConfig.COMMIT_MESSAGE, commitMessage));
		}
		if (existing != null) {
			cfg.update(cfg.descriptor().getProperty(ScriptToolConfig.SUMMARY), existing.getSummary());
			cfg.update(cfg.descriptor().getProperty(ScriptToolConfig.TRUNCATION_HINT),
				Integer.valueOf(existing.getTruncationHint()));
		}
		compileCheck(cfg);
		return cfg;
	}

	private static Expr parseScript(String body) {
		if (body == null || body.isBlank()) {
			throw new IllegalArgumentException("The TL-Script implementation must not be empty.");
		}
		try {
			return ExprFormat.INSTANCE.getValue("implementation", body);
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException("The TL-Script body does not parse: " + ex.getMessage(), ex);
		}
	}

	private static Expr parseExpression(String property, String source) {
		try {
			return ExprFormat.INSTANCE.getValue(property, source);
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException("The TL-Script of '" + property + "' does not parse: "
				+ ex.getMessage(), ex);
		}
	}

	private static ToolMode parseMode(String mode) {
		if (mode == null || mode.isBlank()) {
			return ToolMode.DATA_WRITE;
		}
		try {
			return ToolMode.valueOf(mode.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			// Listed from the enum rather than spelled out: the message is read by the model, which
			// then retries with one of the names in it, so a hand-written list that misses a
			// constant makes that constant unreachable.
			throw new IllegalArgumentException("Unknown tool mode '" + mode + "'. Expected one of "
				+ Arrays.stream(ToolMode.values()).map(Enum::name).collect(Collectors.joining(", "))
				+ ".", ex);
		}
	}

	/**
	 * Updates an existing TL-Script tool. Null arguments leave the corresponding field unchanged;
	 * a non-null {@code parameters} replaces the full parameter list, and a blank
	 * {@code commitMessage} removes the commit message expression.
	 *
	 * @throws IllegalArgumentException
	 *         If no tool with the given name exists, or a new value fails validation.
	 */
	public void update(String name, String label, String description, List<ParamSpec> parameters,
			String implementation, String mode, Boolean concurrencySafe, String commitMessage) {
		requireName(name);
		Map<String, ScriptToolConfig> tools = factoryTools();
		ScriptToolConfig existing = tools.get(name);
		if (existing == null) {
			throw new IllegalArgumentException("No script tool named '" + name + "'. Existing: "
				+ tools.keySet() + ".");
		}
		ScriptToolConfig merged = buildConfig(
			name,
			label != null ? label : labelOf(existing),
			description != null ? description : descriptionOf(existing),
			parameters != null ? ScriptToolParameters.declaration(parameters) : copyOf(existing.getParameters()),
			implementation != null ? implementation
				: ExprFormat.INSTANCE.getSpecification(existing.getImplementation()),
			mode != null ? mode : existing.getMode().name(),
			concurrencySafe != null ? concurrencySafe.booleanValue() : existing.isConcurrencySafe(),
			commitMessage != null ? commitMessage : commitMessageOf(existing),
			existing);
		tools.put(name, merged);
	}

	/**
	 * Removes a TL-Script tool.
	 *
	 * @throws IllegalArgumentException
	 *         If no tool with the given name exists.
	 */
	public void delete(String name) {
		requireName(name);
		Map<String, ScriptToolConfig> tools = factoryTools();
		if (tools.remove(name) == null) {
			throw new IllegalArgumentException("No script tool named '" + name + "'. Existing: "
				+ factoryTools().keySet() + ".");
		}
	}

	/**
	 * A copy of the given parameter declaration, kept as it is by an update that does not replace
	 * it.
	 */
	private static ConfigurationDescriptorConfig copyOf(ConfigurationDescriptorConfig parameters) {
		return parameters == null ? null : TypedConfiguration.copy(parameters);
	}

	/**
	 * The source of the given tool's {@link ScriptToolConfig#getCommitMessage() commit message}
	 * expression, or <code>null</code> when it has none.
	 */
	public static String commitMessageOf(ScriptToolConfig cfg) {
		return cfg.getCommitMessage() == null ? null
			: ExprFormat.INSTANCE.getSpecification(cfg.getCommitMessage());
	}

	private static String labelOf(ScriptToolConfig cfg) {
		return cfg.getLabel() == null ? null
			: ToolDescriptions.englishPlainText(cfg.getLabel());
	}

	private static String descriptionOf(ScriptToolConfig cfg) {
		return cfg.getDescription() == null ? null
			: ToolDescriptions.englishPlainText(cfg.getDescription());
	}

	/**
	 * Builds the executor for the single tool to surface any compile error at authoring time
	 * (the same path {@link ScriptToolFactory} runs at startup).
	 */
	private static void compileCheck(ScriptToolConfig cfg) {
		ScriptToolFactory.Config factoryConfig = TypedConfiguration.newConfigItem(ScriptToolFactory.Config.class);
		factoryConfig.getTools().put(cfg.getName(), cfg);
		try {
			new ScriptToolFactory(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, factoryConfig)
				.createTools();
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException(
				"The TL-Script tool does not compile: " + ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, ScriptToolConfig> factoryTools() {
		ToolRegistryService.ToolFactoryConfig factoryConfig =
			_config.getToolFactories().get(_factoryName);
		if (factoryConfig == null) {
			factoryConfig = TypedConfiguration.newConfigItem(ToolRegistryService.ToolFactoryConfig.class);
			factoryConfig.setName(_factoryName);
			factoryConfig.setImplementation(TypedConfiguration.newConfigItem(ScriptToolFactory.Config.class));
			_config.getToolFactories().put(_factoryName, factoryConfig);
		}
		PolymorphicConfiguration<? extends ToolFactory> impl = factoryConfig.getImplementation();
		if (!(impl instanceof ScriptToolFactory.Config scriptConfig)) {
			throw new IllegalStateException("The '" + _factoryName
				+ "' tool factory is not a ScriptToolFactory; cannot author TL-Script tools there.");
		}
		return scriptConfig.getTools();
	}

	private static void requireName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("A script tool name is required.");
		}
	}
}
