/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.tl.ai.core.tools.AnnotatedTools;
import com.top_logic.tl.ai.core.tools.Tool;

/**
 * A {@link ToolFactory} that reflectively discovers tools from a Java class with
 * {@link com.top_logic.tl.ai.core.tools.ToolMethod @ToolMethod} annotated methods.
 *
 * <p>
 * This factory extracts tool specifications from the configured class and creates corresponding
 * {@link Tool} instances for each annotated method.
 * </p>
 *
 * @see AnnotatedTools
 */
public class ReflectiveToolFactory implements ToolFactory {

	/**
	 * Configuration for {@link ReflectiveToolFactory}.
	 */
	public interface Config extends PolymorphicConfiguration<ReflectiveToolFactory> {

		/**
		 * The Java class containing {@link com.top_logic.tl.ai.core.tools.ToolMethod @ToolMethod}
		 * annotated methods.
		 *
		 * <p>
		 * This class will be scanned for tool definitions. All methods annotated with
		 * {@code @ToolMethod} will be exposed as AI tools.
		 * </p>
		 */
		@Mandatory
		Class<?> getToolClass();

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
		List<Tool> tools;
		try {
			Object toolInstance = toolClass.getDeclaredConstructor().newInstance();
			tools = AnnotatedTools.from(toolInstance);
		} catch (Exception e) {
			context.error("Failed to instantiate tool class: " + toolClass.getName(), e);
			tools = List.of();
		}
		_tools = tools;
	}

	@Override
	public List<Tool> createTools() {
		return _tools;
	}

}
