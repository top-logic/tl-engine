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
	private static final String UML_DESIGNER_SYSTEM = """
			You are a UML system architect for TopLogic model creation.
			Create or revise a UML specification that can be executed by TopLogic tools.
			Follow strict modeling rules:
			- Properties use only primitive types (STRING, INT, BOOLEAN, FLOAT, DATE, TRISTATE, BINARY).
			- Enumerations are linked via references, never used as property types.
			- References must declare role name, target, multiplicity, and kind (association/composition).
			- Use composition only for true ownership/lifecycle containment.
			Return only the UML specification.
			""";

	private static final String UML_DESIGNER_USER = """
			You are a UML system architect. Generate or revise a UML specification.

			Requirements: {{businessRequirement}}

			Current UML specification:
			{{umlSpec}}

			Critique:
			{{critique}}

			If critique is empty, create an initial specification from requirements.
			Otherwise, revise the current UML spec by fixing the critique issues.
			""";

	private static final String UML_CRITIC_SYSTEM = """
			You are a TopLogic UML reviewer.
			Evaluate UML specs for compliance and requirement coverage.
			Focus on hard-rule violations and return structured JSON review.
			Critical examples:
			- enum used as property type
			- missing reference metadata (kind/multiplicity)
			- invalid composition ownership
			""";

	private static final String UML_CRITIC_USER = """
			Critically evaluate the UML specification and return JSON critique.

			UML specification: {{umlSpec}}
			Requirements: {{businessRequirement}}
			""";

	private static final String UML_SCORER_USER = """
			Given a JSON critique of a UML specification, compute a score between 0.0 and 1.0.
			Heuristic:
			- if there are critical issues, return below 0.5
			- with no critical issues, reduce from 1.0 by 0.05 per important issue
			- if there are no issues, return 1.0
			Return only the numeric score.

			Critique: {{critique}}
			""";

	private static final String UML_PARSER_SYSTEM = """
			You are a deterministic parser for TopLogic UML specifications.
			Return JSON only. Do not invent missing model elements.
			Extract modules, classes, properties, references, enums, and global constraints.
			""";

	private static final String UML_PARSER_USER = """
			Parse this UML design specification and extract all information into structured JSON:
			{{umlSpec}}
			""";

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
		UntypedAgent designer = AgenticServices.agentBuilder()
			.chatModel(MISTRAL_LARGE_MODEL)
			.systemMessage(UML_DESIGNER_SYSTEM)
			.userMessage(UML_DESIGNER_USER)
			.inputKeys(String.class, "businessRequirement", String.class, "critique", String.class, "umlSpec")
			.returnType(String.class)
			.outputKey("umlSpec") // the designer writes its spec here
			.build();

		UntypedAgent critic = AgenticServices.agentBuilder()
			.chatModel(MISTRAL_LARGE_MODEL)
			.systemMessage(UML_CRITIC_SYSTEM)
			.userMessage(UML_CRITIC_USER)
			.inputKeys(String.class, "umlSpec", String.class, "businessRequirement")
			.returnType(String.class)
			.outputKey("critique") // the critic writes its JSON feedback here
			.build();

		UntypedAgent scorer = AgenticServices.agentBuilder()
			.chatModel(PIXTRAL_MODEL)
			.userMessage(UML_SCORER_USER)
			.inputKey(String.class, "critique")
			.returnType(Double.class)
			.outputKey("score") // the scorer writes its numeric score here
			.build();

		UntypedAgent umlParser = AgenticServices.agentBuilder()
			.chatModel(PIXTRAL_MODEL)
			.systemMessage(UML_PARSER_SYSTEM)
			.userMessage(UML_PARSER_USER)
			.inputKey(String.class, "umlSpec")
			.returnType(String.class)
			.outputKey("modelRequirements")
			.build();

		McpToolProvider toolProvider = createMCPToolProvider();

		UntypedAgent modelCreator = AgenticServices.agentBuilder()
			.chatModel(MISTRAL_LARGE_MODEL)
			.userMessage("Create or modify TopLogic model elements based on these requirements: {{message}}")
			.inputKey(String.class, "message")
			.returnType(String.class)
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
