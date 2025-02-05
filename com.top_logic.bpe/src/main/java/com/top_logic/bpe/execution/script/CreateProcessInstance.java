/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.script;

import java.io.StringReader;
import java.util.List;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * A {@link GenericMethod} implementation that creates and initializes a process instance based on a
 * participant name.
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public class CreateProcessInstance extends GenericMethod {

	/** Fallback name when no explicit name provided */
	private static final String DEFAULT_PROCESS_NAME = "newProcessInstance";

	/**
	 * Creates a new {@link CreateProcessInstance}.
	 *
	 */
	protected CreateProcessInstance(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CreateProcessInstance(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	/**
	 * Instantiates and initializes a process for the given participant
	 * 
	 * @param arguments
	 *        script arguments where: args[0]: participant name (required) args[1]: process instance
	 *        name (optional)
	 * @param definitions
	 *        evaluation context
	 * @return initialized process execution or null if participant not found
	 */
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String participantName = asString(arguments[0]);
		if (participantName == null || participantName.isEmpty()) {
			return null;
		}

		String processName = arguments.length > 1 ? asString(arguments[1]) : DEFAULT_PROCESS_NAME;
		if (processName == null || processName.isEmpty()) {
			processName = DEFAULT_PROCESS_NAME;
		}

		// Find start event using query
		StartEvent startEvent = findStartEvent(participantName);
		if (startEvent == null) {
			return null;
		}

		// Create process instance
		ProcessExecution processExecution = createProcessModel(startEvent);
		processExecution.setProcess(startEvent.getProcess());
		processExecution.setCollaboration(processExecution.getProcess().getCollaboration());

		// Set the name
		processExecution.tSetData("name", processName);

		// Initialize the process execution
		ExecutionEngine.getInstance().init(processExecution, startEvent);

		return processExecution;
	}

	/**
	 * Instantiates process model based on participant's model type or default type
	 * 
	 * @param startEvent
	 *        event containing process and participant configuration
	 * @return new process execution instance
	 */
	private ProcessExecution createProcessModel(StartEvent startEvent) {
		TLClass modelType = startEvent.getProcess().getParticipant().getModelType();
		if (modelType == null) {
			modelType = TlBpeExecutionFactory.getProcessExecutionType();
		}
		return (ProcessExecution) ModelService.getInstance().getFactory().createObject(modelType);
	}

	/**
	 * Locates start event by querying participant with given name
	 * 
	 * @param participantName
	 *        of the participant to find
	 * @return matching start event or null if not found
	 */
	private StartEvent findStartEvent(String participantName) {
		try {
			String searchExpression = String.format("""
					all(`tl.bpe.bpml:Participant`)
					.filter(p -> $p.get(`tl.bpe.bpml:Participant#name`) == "%s")
					.singleElement()
					.get(`tl.bpe.bpml:Participant#process`)
					.get(`tl.bpe.bpml:Process#nodes`)
					.filter(n -> $n.instanceOf(`tl.bpe.bpml:StartEvent`))
					.singleElement()
					""", participantName);

			Expr expr = new SearchExpressionParser(new StringReader(searchExpression)).expr();
			QueryExecutor executor = QueryExecutor.compile(expr);
			return (StartEvent) executor.execute();
		} catch (ParseException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link CreateProcessInstance} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<CreateProcessInstance> {

		/** Description of parameters for a {@link CreateProcessInstance}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("participant")
			.optional("name")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public CreateProcessInstance build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new CreateProcessInstance(getConfig().getName(), args);
		}

	}

}
