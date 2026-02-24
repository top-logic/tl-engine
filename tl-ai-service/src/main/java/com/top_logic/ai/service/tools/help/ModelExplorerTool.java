/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.help;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.ai.service.agents.AgentRegistry;
import com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse;
import com.top_logic.ai.service.tools.util.DataClassSerializer;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.UntypedAgent;

/**
 * Tool for exploring the TopLogic application model.
 * <p>
 * This tool wraps the configured {@code ModelExplorer} agent to allow TL-Script agents to delegate model
 * exploration questions when users ask about the actual application structure.
 * </p>
 */
public class ModelExplorerTool {

	/**
	 * Explores the TopLogic application model to answer questions about model structure.
	 * <p>
	 * Use this tool when the user asks about:
	 * <ul>
	 * <li>Application modules and their types</li>
	 * <li>Type definitions, properties, and references</li>
	 * <li>Model relationships and dependencies</li>
	 * <li>Where types are used</li>
	 * <li>Annotations and metadata</li>
	 * <li>Instance data (when relevant)</li>
	 * </ul>
	 * </p>
	 *
	 * @param question
	 *        The user's question about the model.
	 * @return The exploration result as a formatted text response.
	 */
	@Tool("Explore the TopLogic application model to answer questions about modules, types, parts, references, " +
		"and model structure. Provide a clear, specific question—for example, 'What parts does sales:Order have?' " +
		"or 'Where is sales:Order used?' The Model Explorer will run the appropriate model tools, return its findings "
		+
		"in concise bullet points, optionally suggest a next exploration step, and include a source Reference (citations) "
		+
		"linking each fact to the tool output. Use this for read‑only exploration; it never modifies the model.")
	public String exploreModel(@P("The user's question about the TopLogic application model") String question) {
		Map<String, Object> args = new HashMap<>();
		args.put("question", question);

		try {
			// Log tool call
			System.out.println("ModelExplorerTool.exploreModel() called with question: " + question);

			// Validate parameters
			if (question == null || question.trim().isEmpty()) {
				String error = "Question is required";
				Logger.error("exploreModel() error: " + error, ModelExplorerTool.class);
				return DataClassSerializer.errorToJson(error, ModelExplorerTool.class, "exploreModel()", args);
			}

			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null) {
				String error = "AgentRegistry is not available";
				Logger.error("exploreModel() error: " + error, ModelExplorerTool.class);
				return DataClassSerializer.errorToJson(error, ModelExplorerTool.class, "exploreModel()", args);
			}

			UntypedAgent explorer = registry.buildUntypedAgent("ModelExplorer");

			// Execute the exploration
			String result = ThreadContextManager.inSystemInteraction(
				ModelExplorerTool.class,
				() -> (String) explorer.invoke(Map.of("message", question)));

			// Create response object with the exploration result
			ModelExplorerResponse response = ModelExplorerResponse.create();
			response.setResult(result);

			return DataClassSerializer.toJson(response, ModelExplorerTool.class, "exploreModel()", args);

		} catch (Exception ex) {
			String error = "Failed to explore model: " + ex.getMessage();
			Logger.error(error, ex, ModelExplorerTool.class);
			System.err.println("ModelExplorerTool.exploreModel() error: " + error);
			return DataClassSerializer.errorToJson(error, ModelExplorerTool.class, "exploreModel()", args);
		}
	}
}
