package com.top_logic.ai.service.tools.registry;

import java.util.List;

/**
 * Factory for creating {@link ToolImplementation} instances.
 *
 * <p>
 * A tool factory is responsible for producing a list of tools that can be used by AI agents. Each
 * factory can create multiple tools, and the tools are identified by their names within the
 * factory's namespace.
 * </p>
 */
public interface ToolFactory {

	/**
	 * Creates the list of tools provided by this factory.
	 *
	 * <p>
	 * This method is typically called once during service initialization. The returned tools will
	 * be registered in the {@link ToolRegistry} and made available to AI agents.
	 * </p>
	 *
	 * @return A list of tools created by this factory. Must not be null.
	 */
	List<ToolImplementation> createTools();

}
