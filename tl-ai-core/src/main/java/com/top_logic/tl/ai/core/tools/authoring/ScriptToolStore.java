/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.model.search.configured.ScriptParameter;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
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
 * mutations validate the TL-Script body and parameter types before applying.
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
	 * @param name
	 *        Parameter name, bound as {@code $name} in the script body.
	 * @param type
	 *        Qualified TL type, e.g. {@code tl.core:String}.
	 * @param description
	 *        Human-readable description of the parameter.
	 * @param mandatory
	 *        Whether the parameter must be supplied.
	 * @param multiple
	 *        Whether the parameter accepts a list of values.
	 * @param defaultValue
	 *        Optional TL-Script default expression used when an optional parameter is omitted;
	 *        empty/null for none.
	 */
	public record ParamSpec(String name, String type, String description, boolean mandatory,
			boolean multiple, String defaultValue) {
		// data carrier for tool input
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
			String implementation, String mode, boolean concurrencySafe) {
		requireName(name);
		Map<String, ScriptToolConfig> tools = factoryTools();
		if (tools.containsKey(name)) {
			throw new IllegalArgumentException("A script tool named '" + name + "' already exists.");
		}
		tools.put(name, buildConfig(name, label, description, parameters, implementation, mode, concurrencySafe));
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

	private ScriptToolConfig buildConfig(String name, String label, String description,
			List<ParamSpec> parameters, String implementation, String mode, boolean concurrencySafe) {
		ScriptToolConfig cfg = TypedConfiguration.newConfigItem(ScriptToolConfig.class);
		cfg.setName(name);
		if (label != null && !label.isBlank()) {
			cfg.setLabel(ResKey.text(label));
		}
		if (description != null && !description.isBlank()) {
			cfg.setDescription(new DefaultHtmlResKey(ResKey.text(description)));
		}
		cfg.setImplementation(parseScript(implementation));
		if (parameters != null) {
			for (ParamSpec spec : parameters) {
				cfg.getParameters().add(buildParam(spec));
			}
		}
		cfg.update(cfg.descriptor().getProperty(ScriptToolConfig.MODE), parseMode(mode));
		cfg.update(cfg.descriptor().getProperty(ScriptToolConfig.CONCURRENCY_SAFE),
			Boolean.valueOf(concurrencySafe));
		compileCheck(cfg);
		return cfg;
	}

	private static ScriptParameter buildParam(ParamSpec spec) {
		if (spec.name() == null || spec.name().isBlank()) {
			throw new IllegalArgumentException("Each parameter needs a non-empty name.");
		}
		TLModelPartRef typeRef = TLModelPartRef.ref(spec.type());
		try {
			typeRef.resolveType();
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException(
				"Parameter '" + spec.name() + "' has an unknown type '" + spec.type()
					+ "'. Use a qualified TL type such as tl.core:String.", ex);
		}
		ScriptParameter p = TypedConfiguration.newConfigItem(ScriptParameter.class);
		p.setName(spec.name());
		p.setType(typeRef);
		p.setMandatory(spec.mandatory());
		p.setMultiple(spec.multiple());
		if (spec.description() != null && !spec.description().isBlank()) {
			p.setDescription(new DefaultHtmlResKey(ResKey.text(spec.description())));
		}
		if (spec.defaultValue() != null && !spec.defaultValue().isBlank()) {
			try {
				p.setDefaultValue(ExprFormat.INSTANCE.getValue("defaultValue", spec.defaultValue()));
			} catch (ConfigurationException ex) {
				throw new IllegalArgumentException("Parameter '" + spec.name()
					+ "' has an invalid default expression: " + ex.getMessage(), ex);
			}
		}
		return p;
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

	private static ToolMode parseMode(String mode) {
		if (mode == null || mode.isBlank()) {
			return ToolMode.DATA_WRITE;
		}
		try {
			return ToolMode.valueOf(mode.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Unknown tool mode '" + mode
				+ "'. Expected one of READ, USER_SCOPED, DATA_WRITE, STRUCTURAL.", ex);
		}
	}

	/**
	 * Updates an existing TL-Script tool. Null arguments leave the corresponding field unchanged;
	 * a non-null {@code parameters} replaces the full parameter list.
	 *
	 * @throws IllegalArgumentException
	 *         If no tool with the given name exists, or a new value fails validation.
	 */
	public void update(String name, String label, String description, List<ParamSpec> parameters,
			String implementation, String mode, Boolean concurrencySafe) {
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
			parameters != null ? parameters : specsOf(existing),
			implementation != null ? implementation
				: ExprFormat.INSTANCE.getSpecification(existing.getImplementation()),
			mode != null ? mode : existing.getMode().name(),
			concurrencySafe != null ? concurrencySafe.booleanValue() : existing.isConcurrencySafe());
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

	private static List<ParamSpec> specsOf(ScriptToolConfig cfg) {
		List<ParamSpec> specs = new ArrayList<>();
		for (ScriptParameter p : cfg.getParameters()) {
			String def = p.getDefaultValue() == null ? null
				: ExprFormat.INSTANCE.getSpecification(p.getDefaultValue());
			specs.add(new ParamSpec(p.getName(), p.getType().qualifiedName(),
				descriptionText(p.getDescription()), p.isMandatory(), p.isMultiple(), def));
		}
		return specs;
	}

	private static String labelOf(ScriptToolConfig cfg) {
		return cfg.getLabel() == null ? null
			: ToolDescriptions.englishPlainText(cfg.getLabel());
	}

	private static String descriptionOf(ScriptToolConfig cfg) {
		return cfg.getDescription() == null ? null
			: ToolDescriptions.englishPlainText(cfg.getDescription());
	}

	private static String descriptionText(HtmlResKey key) {
		return key == null ? null
			: ToolDescriptions.englishPlainText(key);
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
