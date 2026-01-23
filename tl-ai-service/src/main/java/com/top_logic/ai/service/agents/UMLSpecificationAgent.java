/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.Map;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.workflow.impl.SequentialPlanner;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;

/**
 * Multi-agent workflow for generating and refining TopLogic UML specifications.
 *
 * <p>
 * This agent orchestrates a workflow that iteratively designs, critiques, and scores UML
 * specifications before parsing and creating the corresponding model elements.
 * </p>
 */
public class UMLSpecificationAgent {
	private static final ChatModel PIXTRAL_MODEL = MistralAiChatModel.builder()
		.apiKey("xxx")
		.baseUrl("https://api.mistral.ai/v1")
		.modelName("pixtral-12b")
		.logRequests(false)
		.logResponses(true)
		.build();

	private static final ChatModel MISTRAL_LARGE_MODEL = MistralAiChatModel.builder()
		.apiKey("xxx")
		.baseUrl("https://api.mistral.ai/v1")
		.modelName("mistral-large-latest")
		.logRequests(false)
		.logResponses(true)
		.build();

	/**
	 * Executes the UML specification workflow.
	 *
	 * @param businessRequirement
	 *        The business requirements to translate into a UML specification.
	 * @return The result of model creation.
	 */
	public static String execute(String businessRequirement) {
		// Step 1: build the agents
		UMLDesigner designer = AgenticServices.agentBuilder(UMLDesigner.class)
			.chatModel(MISTRAL_LARGE_MODEL)
			.outputKey("umlSpec") // the designer writes its spec here
			.build();

		UMLCritic critic = AgenticServices.agentBuilder(UMLCritic.class)
			.chatModel(MISTRAL_LARGE_MODEL)
			.outputKey("critique") // the critic writes its JSON feedback here
			.build();

		UMLScorer scorer = AgenticServices.agentBuilder(UMLScorer.class)
			.chatModel(PIXTRAL_MODEL)
			.outputKey("score") // the scorer writes its numeric score here
			.build();

		UMLParser umlParser = AgenticServices.agentBuilder(UMLParser.class)
			.chatModel(PIXTRAL_MODEL)
			.outputKey("modelRequirements")
			.build();

		McpToolProvider toolProvider = createMCPToolProvider();

		ModelCreator modelCreator = AgenticServices.agentBuilder(ModelCreator.class)
			.chatModel(MISTRAL_LARGE_MODEL)
			.outputKey("result")
			.toolProvider(toolProvider)
			.build();

		UntypedAgent umlRevisionLoop = AgenticServices.loopBuilder()
			.subAgents(designer, critic, scorer)
			.maxIterations(5)
			.exitCondition(scope -> {
				Number score = scope.readState("score", 0.0);
				return score.doubleValue() >= 0.8;
			})
			.outputKey("umlSpec") // return the final UML specification from the scope
			.build();

		// Step 3: incorperate into planner
		UntypedAgent umlWorkflow = AgenticServices.plannerBuilder()
			.subAgents(umlRevisionLoop, umlParser, modelCreator)
			.outputKey("result") // final output
			.planner(SequentialPlanner::new) // call agents in order
			.build();

		// Step 3: invoke planner
		String result = (String) umlWorkflow.invoke(
			Map.of(
				"businessRequirement", businessRequirement,
				"umlSpec", "",
				"critique", "",
				"score", 0.0,
				"modelRequirements", "",
				"result", ""
			));

		return result;

	}

	/**
	 * Creates and configures the MCP tool provider.
	 */
	private static McpToolProvider createMCPToolProvider() {
		McpTransport transport = StreamableHttpMcpTransport.builder()
			.url("http://localhost:8080/tl-ai-demo/mcp")
			.logRequests(false)
			.logResponses(false)
			.build();

		McpClient mcpClient = DefaultMcpClient.builder()
			.key("UMLSpecificationAgent")
			.transport(transport)
			.build();

		return McpToolProvider.builder()
			.mcpClients(mcpClient)
			.build();
	}

	/**
	 * Main method for testing the UML specification workflow.
	 *
	 * @param args
	 *        Command line arguments (not used).
	 */
	public static void main(String[] args) {
		String businessRequirement = """
				Create a comprehensive project management system for software development teams.
				The system should handle projects with milestones, tasks with assignments and priorities,
				team members with roles and skills, time tracking with daily entries and approvals,
				and reporting with dashboards and metrics.
				""";
		String result = execute(businessRequirement);
		System.out.println(result);
	}
}
