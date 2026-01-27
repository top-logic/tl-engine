/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import com.top_logic.ai.service.agents.handoff.AgentWorkOrder;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tools the orchestrator LLM can call to delegate to specialist agents.
 * <p>
 * Each tool invokes {@link AgentInvoker} with a work order. The LLM decides intent
 * and fills goal/task/context from the user message; it must not paste the full message.
 * </p>
 */
public class OrchestratorTools {

	private static final String FROM_AGENT = "Orchestrator";

	@Tool("Delegate to the Model Explorer. Call when the user wants to explore, understand, or inspect the TopLogic application model: modules, types, parts, annotations, where-is-X-defined, list modules/types, model structure, read-only questions. Provide goal (short intent), task (concrete question or instruction), context (type/part/module names if known). Do NOT paste the full user message; distill a clear exploration request.")
	public String delegateToModelExplorer(
			@P("Short intent, e.g. 'List application modules and types'") String goal,
			@P("Concrete question or instruction, e.g. 'What parts does sales:Order have?'") String task,
			@P("Optional: type/part/module names, entity hints") String context) {
		AgentWorkOrder workOrder = AgentWorkOrder.of(goal, task, context != null ? context : "", "");
		return AgentInvoker.invokeExplorer(FROM_AGENT, workOrder);
	}

	@Tool("Delegate to the Model Creator. Call when the user wants to create or modify the TopLogic model: add types, properties, references, annotations, change relationships, create from UML/requirements. Provide goal (short intent), task (concrete instructions or requirements text), context (module/type names if relevant). Do NOT paste the full user message; distill the creation/modification request.")
	public String delegateToModelCreator(
			@P("Short intent, e.g. 'Create Order type with properties'") String goal,
			@P("Concrete instructions or requirements (UML, JSON, or natural language)") String task,
			@P("Optional: target module/type names") String context) {
		AgentWorkOrder workOrder = AgentWorkOrder.of(goal, task, context != null ? context : "", "");
		return AgentInvoker.invokeCreator(FROM_AGENT, workOrder);
	}

	@Tool("Delegate to the TLScript specialist. Call when the user asks about TLScript: syntax, how to write expressions, what functions exist, function docs, script validation, get/set/chaining, how to do X in TLScript. Provide goal (short intent), task (concrete question or code snippet to validate), context (type/part names if the question refers to model elements). Do NOT paste the full user message; distill the TLScript question.")
	public String delegateToTLScriptSpecialist(
			@P("Short intent, e.g. 'Explain get vs set in TLScript'") String goal,
			@P("Concrete question or code to validate") String task,
			@P("Optional: type/part names if the question mentions model elements") String context) {
		AgentWorkOrder workOrder = AgentWorkOrder.of(goal, task, context != null ? context : "", "");
		return AgentInvoker.invokeTLScript(FROM_AGENT, workOrder);
	}
}
