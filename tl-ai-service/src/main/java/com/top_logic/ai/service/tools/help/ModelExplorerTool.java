/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.help;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.agents.ModelExplorer;
import com.top_logic.ai.service.tools.AnnotationDefinitionExplorerTool;
import com.top_logic.ai.service.tools.InstanceBrowserTool;
import com.top_logic.ai.service.tools.ModelAnnotationBrowserTool;
import com.top_logic.ai.service.tools.ModelBrowserTool;
import com.top_logic.ai.service.tools.ModelSearchTool;
import com.top_logic.ai.service.tools.TypeAnalysisTool;
import com.top_logic.ai.service.tools.help.messages.ErrorResult;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for exploring the TopLogic application model.
 * <p>
 * This tool wraps the ModelExplorer agent to allow TLScriptAgent to delegate
 * model exploration questions when users ask about the actual application structure.
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
	@Tool("Explore the TopLogic application model to answer questions about modules, types, properties, references, and model structure. Use this when the user asks about the actual application model.")
	public String exploreModel(@P("The user's question about the TopLogic application model") String question) {
		return ThreadContextManager.inSystemInteraction(
			ModelExplorerTool.class,
			() -> {
				try {
					// Log tool call
					System.out.println("ModelExplorerTool.exploreModel() called with question: " + question);

					// Validate parameters
					if (question == null || question.trim().isEmpty()) {
						String error = "Question is required";
						Logger.error("exploreModel() error: " + error, ModelExplorerTool.class);
						return buildErrorJson(error);
					}

					// Get the model from AIService
					AIService aiService = AIService.getInstance();
					if (aiService == null) {
						String error = "AIService is not available";
						Logger.error("exploreModel() error: " + error, ModelExplorerTool.class);
						return buildErrorJson(error);
					}

					// Use mistral-large-latest model (same as TLScriptAgent)
					var explorationModel = aiService.getChatModel("mistral-large-latest");

					// Create internal tool instances
					ModelBrowserTool browserTool = new ModelBrowserTool();
					InstanceBrowserTool instanceTool = new InstanceBrowserTool();
					TypeAnalysisTool analysisTool = new TypeAnalysisTool();
					ModelSearchTool searchTool = new ModelSearchTool();
					ModelAnnotationBrowserTool annotationTool = new ModelAnnotationBrowserTool();
					AnnotationDefinitionExplorerTool definitionTool = new AnnotationDefinitionExplorerTool();

					// Build the ModelExplorer agent with all required tools
					ModelExplorer explorer = AgenticServices.agentBuilder(ModelExplorer.class)
						.chatModel(explorationModel)
						.tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool, definitionTool)
						.build();

					// Execute the exploration
					String result = explorer.explore(question);

					// Log result
					System.out.println("ModelExplorerTool.exploreModel() result: " + result);
					Logger.info("exploreModel() completed successfully", ModelExplorerTool.class);
					return result;

				} catch (Exception ex) {
					String error = "Failed to explore model: " + ex.getMessage();
					Logger.error(error, ex, ModelExplorerTool.class);
					System.err.println("ModelExplorerTool.exploreModel() error: " + error);
					return buildErrorJson(error);
				}
			});
	}

	/**
	 * Builds an error JSON response using msgbuf data object.
	 *
	 * @param errorMessage
	 *        The error message.
	 * @return JSON string containing the error.
	 */
	private static String buildErrorJson(String errorMessage) {
		ErrorResult error = ErrorResult.create().setError(errorMessage);
		return error.toString();
	}
}
