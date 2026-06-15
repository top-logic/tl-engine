/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
import com.top_logic.model.search.configured.ScriptParameter;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.tl.ai.tool.api.registry.ToolFactory;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Read/mutate access to the TL-Script tools held by the {@code custom}
 * {@link ScriptToolFactory} inside a {@link ToolRegistryService.Config}.
 *
 * <p>
 * Pure in-memory operations with no I/O: callers persist the wrapped config separately. All
 * mutations validate the TL-Script body and parameter types before applying.
 * </p>
 */
public class ScriptToolStore {

	/**
	 * Name of the {@link ScriptToolFactory} entry that holds agent-created TL-Script tools.
	 */
	public static final String CUSTOM_FACTORY = "custom";

	/**
	 * Declaration of one TL-Script tool parameter as supplied by the caller.
	 */
	public record ParamSpec(String name, String type, String description, boolean mandatory,
			boolean multiple) {
		// data carrier for tool input
	}

	private final ToolRegistryService.Config<?> _config;

	/**
	 * Creates a {@link ScriptToolStore} over the given registry configuration.
	 */
	public ScriptToolStore(ToolRegistryService.Config<?> config) {
		_config = config;
	}

	/**
	 * Creates a new TL-Script tool in the {@code custom} factory.
	 *
	 * @throws IllegalArgumentException
	 *         If the name is blank or already used, a parameter type does not resolve, or the
	 *         script body does not parse/compile.
	 */
	public void create(String name, String label, String description, List<ParamSpec> parameters,
			String implementation, String mode, boolean concurrencySafe) {
		requireName(name);
		Map<String, ScriptToolConfig> tools = customTools();
		if (tools.containsKey(name)) {
			throw new IllegalArgumentException("A script tool named '" + name + "' already exists.");
		}
		tools.put(name, buildConfig(name, label, description, parameters, implementation, mode, concurrencySafe));
	}

	/**
	 * Returns the named tool config, or {@code null} if absent.
	 */
	public ScriptToolConfig get(String name) {
		return customTools().get(name);
	}

	/**
	 * Returns all TL-Script tools in the {@code custom} factory.
	 */
	public List<ScriptToolConfig> list() {
		return new ArrayList<>(customTools().values());
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
			p.setDescription(ResKey.text(spec.description()));
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
	private Map<String, ScriptToolConfig> customTools() {
		ToolRegistryService.ToolFactoryConfig factoryConfig =
			_config.getToolFactories().get(CUSTOM_FACTORY);
		if (factoryConfig == null) {
			factoryConfig = TypedConfiguration.newConfigItem(ToolRegistryService.ToolFactoryConfig.class);
			factoryConfig.setName(CUSTOM_FACTORY);
			factoryConfig.setImplementation(TypedConfiguration.newConfigItem(ScriptToolFactory.Config.class));
			_config.getToolFactories().put(CUSTOM_FACTORY, factoryConfig);
		}
		PolymorphicConfiguration<? extends ToolFactory> impl = factoryConfig.getImplementation();
		if (!(impl instanceof ScriptToolFactory.Config scriptConfig)) {
			throw new IllegalStateException("The '" + CUSTOM_FACTORY
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
