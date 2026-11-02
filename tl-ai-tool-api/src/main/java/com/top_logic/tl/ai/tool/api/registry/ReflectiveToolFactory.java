/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api.registry;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMethod;

/**
 * A {@link ToolFactory} that reflectively discovers tools from a Java class with {@link ToolMethod}
 * annotated methods.
 *
 * <p>
 * This factory extracts tool specifications from the configured class and creates corresponding
 * {@link Tool} instances for each annotated method.
 * </p>
 *
 * <p>
 * The object whose methods are called is either created by the no-argument constructor of the
 * {@link Config#getToolClass() tool class}, or instantiated from the {@link Config#getTool() tool}
 * configuration, when the tool has configuration options of its own:
 * </p>
 *
 * <pre>
 * &lt;implementation class="com.top_logic.tl.ai.tool.api.registry.ReflectiveToolFactory"
 *     tool-class="com.example.MyTool"/&gt;
 *
 * &lt;implementation class="com.top_logic.tl.ai.tool.api.registry.ReflectiveToolFactory"&gt;
 *   &lt;tool class="com.example.MyConfiguredTool" my-option="..."/&gt;
 * &lt;/implementation&gt;
 * </pre>
 *
 * @see AnnotatedTools
 */
public class ReflectiveToolFactory implements ToolFactory {

	/**
	 * Configuration for {@link ReflectiveToolFactory}.
	 */
	public interface Config extends PolymorphicConfiguration<ReflectiveToolFactory> {

		/** Configuration property name for {@link #getToolClass()}. */
		String TOOL_CLASS = "tool-class";

		/** Configuration property name for {@link #getTool()}. */
		String TOOL = "tool";

		/**
		 * The Java class containing {@link ToolMethod} annotated methods.
		 *
		 * <p>
		 * This class will be scanned for tool definitions. All methods annotated with
		 * {@code @ToolMethod} will be exposed as AI tools. The class is instantiated with its
		 * no-argument constructor.
		 * </p>
		 *
		 * <p>
		 * Exactly one of the tool class and the {@link #getTool() tool} must be given.
		 * </p>
		 */
		@Name(TOOL_CLASS)
		Class<?> getToolClass();

		/**
		 * Configuration of the object whose {@link ToolMethod} annotated methods are exposed as AI
		 * tools.
		 *
		 * <p>
		 * Used instead of a {@link #getToolClass() tool class} for a tool with configuration
		 * options of its own. Exactly one of the two must be given.
		 * </p>
		 */
		@Name(TOOL)
		@Nullable
		PolymorphicConfiguration<?> getTool();

	}

	private final List<Tool> _tools;

	/**
	 * Creates a new {@link ReflectiveToolFactory} from the given configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The configuration.
	 */
	public ReflectiveToolFactory(InstantiationContext context, Config config) {
		Class<?> toolClass = config.getToolClass();
		PolymorphicConfiguration<?> toolConfig = config.getTool();
		List<Tool> tools = List.of();
		if (toolConfig != null) {
			if (toolClass != null) {
				context.error("Either '" + Config.TOOL_CLASS + "' or '" + Config.TOOL + "' must be given, not both: "
					+ toolClass.getName());
			} else {
				Object toolInstance = context.getInstance(toolConfig);
				if (toolInstance != null) {
					tools = AnnotatedTools.from(toolInstance);
				}
			}
		} else if (toolClass == null) {
			context.error("Neither '" + Config.TOOL_CLASS + "' nor '" + Config.TOOL + "' is given.");
		} else {
			try {
				Object toolInstance = toolClass.getDeclaredConstructor().newInstance();
				tools = AnnotatedTools.from(toolInstance);
			} catch (Exception e) {
				context.error("Failed to instantiate tool class: " + toolClass.getName(), e);
			}
		}
		_tools = tools;
	}

	@Override
	public List<Tool> createTools() {
		return _tools;
	}

}
