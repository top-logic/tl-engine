/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import com.top_logic.ai.service.agents.handoff.AgentWorkOrder;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Reusable delegation tools for agent-to-agent calls.
 * <p>
 * Each inner class exposes a single {@code @Tool} method that delegates to a specific specialist
 * agent via {@link AgentInvoker}. Agents receive only the inner class instances they need, so the
 * LLM only sees the delegation targets it is allowed to call.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>
 * // ModelEditor gets exploration + TLScript delegation
 * .tools(creatorTool, new AgentDelegationTools.Exploration("ModelEditor"),
 *     new AgentDelegationTools.TLScript("ModelEditor"))
 *
 * // TLScriptAgent gets exploration delegation only
 * .tools(scriptTool, new AgentDelegationTools.Exploration("TLScriptAgent"))
 * </pre>
 */
public final class AgentDelegationTools {

	/**
	 * Delegates to the {@link ModelExplorer} for read-only model inspection.
	 */
	public static final class Exploration {

		/**
		 * Configuration for XML-based instantiation.
		 */
		public interface Config extends PolymorphicConfiguration<Exploration> {

			/** @see #getFromAgent() */
			String FROM_AGENT = "from-agent";

			/**
			 * Name of the calling agent for trace recording.
			 */
			@Name(FROM_AGENT)
			String getFromAgent();
		}

		private final String _fromAgent;

		/**
		 * Creates from configuration.
		 */
		public Exploration(InstantiationContext context, Config config) {
			_fromAgent = config.getFromAgent();
		}

		/**
		 * @param fromAgent
		 *        Name of the calling agent for trace recording.
		 */
		public Exploration(String fromAgent) {
			_fromAgent = fromAgent;
		}

		/**
		 * Requests model exploration with a distilled work order.
		 *
		 * @param goal
		 *        Short intent (e.g. "Check if module 'sales' exists").
		 * @param task
		 *        Concrete question or instruction.
		 * @param context
		 *        Optional hints: type/part names, module names.
		 * @return The {@code ModelExplorer}'s answer.
		 */
		@Tool("Delegate to the Model Explorer for read-only model inspection. Call to explore, inspect, or verify the TopLogic application model: modules, types, parts, annotations, search. Provide goal (short intent), task (concrete question), context (module/type/part names if known). Do NOT paste the full user message; distill a clear exploration request.")
		public String requestModelExploration(
				@P("Short intent, e.g. 'List all types in module sales'") String goal,
				@P("Concrete question or instruction") String task,
				@P("Optional: module/type/part names, entity hints") String context) {
			AgentWorkOrder workOrder = AgentWorkOrder.of(goal, task, context != null ? context : "", "");
			return AgentInvoker.invokeExplorer(_fromAgent, workOrder);
		}
	}

	/**
	 * Delegates to the {@link ModelEditor} for model creation or modification.
	 */
	public static final class Creation {

		/**
		 * Configuration for XML-based instantiation.
		 */
		public interface Config extends PolymorphicConfiguration<Creation> {

			/** @see #getFromAgent() */
			String FROM_AGENT = "from-agent";

			/**
			 * Name of the calling agent for trace recording.
			 */
			@Name(FROM_AGENT)
			String getFromAgent();
		}

		private final String _fromAgent;

		/**
		 * Creates from configuration.
		 */
		public Creation(InstantiationContext context, Config config) {
			_fromAgent = config.getFromAgent();
		}

		/**
		 * @param fromAgent
		 *        Name of the calling agent for trace recording.
		 */
		public Creation(String fromAgent) {
			_fromAgent = fromAgent;
		}

		/**
		 * Requests model creation or modification.
		 *
		 * @param goal
		 *        Short intent (e.g. "Create Order type with properties").
		 * @param task
		 *        Concrete instructions or requirements.
		 * @param context
		 *        Optional: target module/type names.
		 * @return The creation result.
		 */
		@Tool("Delegate to the Model Creator for model creation or modification. Call when the user wants to create or modify the TopLogic model: add types, properties, references, enumerations. Provide goal (short intent), task (concrete instructions or requirements), context (module/type names if relevant). Do NOT paste the full user message; distill the creation request.")
		public String requestModelCreation(
				@P("Short intent, e.g. 'Create Order type with properties'") String goal,
				@P("Concrete instructions or requirements") String task,
				@P("Optional: target module/type names") String context) {
			AgentWorkOrder workOrder = AgentWorkOrder.of(goal, task, context != null ? context : "", "");
			return AgentInvoker.invokeCreator(_fromAgent, workOrder);
		}
	}

	/**
	 * Delegates to the {@link TLScriptAgent} for TLScript questions and validation.
	 */
	public static final class TLScript {

		/**
		 * Configuration for XML-based instantiation.
		 */
		public interface Config extends PolymorphicConfiguration<TLScript> {

			/** @see #getFromAgent() */
			String FROM_AGENT = "from-agent";

			/**
			 * Name of the calling agent for trace recording.
			 */
			@Name(FROM_AGENT)
			String getFromAgent();
		}

		private final String _fromAgent;

		/**
		 * Creates from configuration.
		 */
		public TLScript(InstantiationContext context, Config config) {
			_fromAgent = config.getFromAgent();
		}

		/**
		 * @param fromAgent
		 *        Name of the calling agent for trace recording.
		 */
		public TLScript(String fromAgent) {
			_fromAgent = fromAgent;
		}

		/**
		 * Requests TLScript help.
		 *
		 * @param goal
		 *        Short intent (e.g. "Validate TLScript expression").
		 * @param task
		 *        Concrete question or code to validate.
		 * @param context
		 *        Optional: type/part names if the question mentions model elements.
		 * @return The {@code TLScriptAgent}'s answer.
		 */
		@Tool("Delegate to the TLScript specialist for TLScript syntax, function documentation, script validation, or code generation. Provide goal (short intent), task (concrete question or code snippet), context (type/part names if relevant). Do NOT paste the full user message; distill the TLScript question.")
		public String requestTLScriptHelp(
				@P("Short intent, e.g. 'Validate TLScript expression'") String goal,
				@P("Concrete question or code to validate") String task,
				@P("Optional: type/part names if the question mentions model elements") String context) {
			AgentWorkOrder workOrder = AgentWorkOrder.of(goal, task, context != null ? context : "", "");
			return AgentInvoker.invokeTLScript(_fromAgent, workOrder);
		}
	}

	private AgentDelegationTools() {
		// utility — use inner classes directly
	}
}
