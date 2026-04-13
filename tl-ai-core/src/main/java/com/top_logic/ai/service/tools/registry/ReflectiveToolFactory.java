/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;

/**
 * A {@link ToolFactory} that reflectively discovers tools from a Java class with {@code @Tool}
 * annotated methods.
 *
 * <p>
 * This factory extracts tool specifications from the configured class and creates corresponding
 * executors for each tool.
 * </p>
 *
 * @see ToolSpecifications
 */
public class ReflectiveToolFactory implements ToolFactory {

	/**
	 * Configuration for {@link ReflectiveToolFactory}.
	 */
	public interface Config extends PolymorphicConfiguration<ReflectiveToolFactory> {

		/**
		 * The Java class containing {@code @Tool} annotated methods.
		 *
		 * <p>
		 * This class will be scanned for tool definitions. All public methods annotated with
		 * {@code @Tool} will be exposed as AI tools.
		 * </p>
		 */
		@Mandatory
		Class<?> getToolClass();

	}

	private final List<ToolImplementation> _tools;

	/**
	 * Creates a new {@link ReflectiveToolFactory} from the given configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The configuration.
	 */
	public ReflectiveToolFactory(InstantiationContext context, Config config) throws ConfigurationException {
		Class<?> toolClass = config.getToolClass();

		// Create an instance of the tool class using ConfigUtil
		Object toolInstance = ConfigUtil.getInstance(toolClass);
		if (toolInstance == null) {
			context.error("Failed to instantiate tool class " + toolClass.getName()
				+ ": Class must have a public no-argument constructor.");
		}

		// Extract tool specifications and create executors from the class
		if (toolInstance != null) {
			_tools = new ArrayList<>();

			// Get all tool specifications from the class
			for (ToolSpecification spec : ToolSpecifications.toolSpecificationsFrom(toolClass)) {
				// Find the method for this tool specification
				Method method = findMethodForToolSpec(toolClass, spec.name());
				if (method != null) {
					// Create executor for this method
					ToolExecutor executor = new DefaultToolExecutor(toolInstance, method);
					_tools.add(new ToolImplementation(spec, executor));
				}
			}
		} else {
			_tools = List.of();
		}
	}

	/**
	 * Finds the method with @Tool annotation matching the given tool name.
	 */
	private static Method findMethodForToolSpec(Class<?> clazz, String toolName) {
		for (Method method : clazz.getDeclaredMethods()) {
			Tool toolAnnotation = method.getAnnotation(Tool.class);
			if (toolAnnotation != null) {
				String name = toolAnnotation.name().isEmpty() ? method.getName() : toolAnnotation.name();
				if (name.equals(toolName)) {
					return method;
				}
			}
		}
		return null;
	}

	@Override
	public List<com.top_logic.ai.service.tools.registry.ToolImplementation> createTools() {
		return _tools;
	}

}
