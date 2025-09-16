/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.layout.execution.ProcessExecutionCreateComponent;
import com.top_logic.bpe.layout.execution.SelectTransitionDialog;
import com.top_logic.bpe.layout.execution.SelectTransitionDialog.Decision;
import com.top_logic.bpe.layout.execution.init.ProcessInitializer;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} to start a {@link ProcessExecution} and skip the first {@link Task} if it
 * has already been merged into start-gui (see {@link ProcessExecutionCreateComponent}).
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
@InApp(classifiers = "workflow")
public class StartProcessExecutionHandler extends AbstractCommandHandler implements WithPostCreateActions {

	private static final String PROCESS_EXECUTION = "processExecution";

	/**
	 * Configuration options for {@link StartProcessExecutionHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config, WithPostCreateActions.Config {

		/**
		 * Optional initialization of a newly created process execution.
		 */
		PolymorphicConfiguration<? extends ProcessInitializer> getInitializer();

	}

	private final List<PostCreateAction> _actions;

	private ProcessInitializer _init;

	/**
	 * Creates a {@link StartProcessExecutionHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StartProcessExecutionHandler(InstantiationContext context, Config config) {
		super(context, config);

		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
		_init = context.getInstance(config.getInitializer());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		// create Process Instance and add to Arguments to skip the creation when method is called
		// again
		ProcessExecution processExecution = (ProcessExecution) someArguments.get(PROCESS_EXECUTION);
		if (processExecution == null) {
			// TODO: Remove bullshit.
			CommandHandler handler = CommandHandlerFactory.getInstance().getHandler("createProcessExecution");

			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(I18NConstants.STARTED_PROCESS)) {
				HandlerResult res = handler.handleCommand(aContext, aComponent, model, someArguments);
				if (res.isSuccess()) {
					processExecution = retrieveProcessExecution(res);
					init(aComponent, model, processExecution);
					tx.commit();
					someArguments.put(PROCESS_EXECUTION, processExecution);
				} else {
					return res;
				}
			}
		}

		StartEvent startEvent = ((ProcessExecutionCreateComponent) aComponent).startEvent();

		// automatically move to next token as user already inputed datat for first token in
		// creation dialog
		if (!startEvent.getRequireReview()) {
			Token token = findSingleToken(processExecution);
			if (token != null) {
				Object context = someArguments.get(FinishTaskCommand.CONTEXT);
				if (context == null) {
					// Decide about the next step.
					Node node = GuiEngine.getInstance().getNextNode(token);

					if (GuiEngine.getInstance().needsDecision(node)) {
						HandlerResult suspended = HandlerResult.suspended();
						new SelectTransitionDialog(token, suspended).open(aContext);
						return suspended;
					} else {
						Edge edge = GuiEngine.getInstance().getSingleOutgoingEdge(token);
						if (edge instanceof SequenceFlow) {
							executeTransition(token, Collections.singletonList(edge));
						}
					}
				} else if (context instanceof Decision decision) {
					executeTransition(token, decision.getPath());
				}
			}
		}

		WithPostCreateActions.processCreateActions(_actions, aComponent, processExecution);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Initializes the given process model after creation and before advancing the workflow.
	 */
	protected void init(LayoutComponent component, Object model, ProcessExecution processExecution) {
		if (_init != null) {
			_init.initialize(component, model, processExecution);
		}
	}

	private Token findSingleToken(ProcessExecution processExecution) {
		Set<? extends Token> tokens = processExecution.getActiveTokens();
		Filter<Token> filter = new Filter<>() {
			@Override
			public boolean accept(Token token) {
				return token.getActive();
			}
		};
		tokens = FilterUtil.filterSet(filter, tokens);
		if (tokens.size() == 1) {
			return CollectionUtil.getSingleValueFromCollection(tokens);
		} else {
			return null;
		}
	}

	private ProcessExecution retrieveProcessExecution(HandlerResult res) {
		return (ProcessExecution) CollectionUtil.getFirst(res.getProcessed());
	}

	private void executeTransition(Token token, List<Edge> edges) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.COMPLETED_START_TASK__NAME
			.fill(token.getNode().getName()))) {
			ExecutionEngine.getInstance().execute(token, edges, null);
			tx.commit();
		} catch (Exception ex) {
			throw new RuntimeException("Can not complete task for token '" + token + "'.", ex);
		}
	}

}