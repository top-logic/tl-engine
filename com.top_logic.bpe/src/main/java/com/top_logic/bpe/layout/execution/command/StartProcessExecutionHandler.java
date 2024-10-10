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
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.layout.execution.ProcessExecutionCreateComponent;
import com.top_logic.bpe.layout.execution.SelectTransitionDialog;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

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
		// Pure sum interface.
	}

	private final List<PostCreateAction> _actions;

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
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		ProcessExecution processExecution = (ProcessExecution) someArguments.get(PROCESS_EXECUTION);
		if (processExecution == null) {
			// TODO: Remove bullshit.
			CommandHandler handler = CommandHandlerFactory.getInstance().getHandler("createProcessExecution");
			HandlerResult res = handler.handleCommand(aContext, aComponent, model, someArguments);
			if (res.isSuccess()) {
				processExecution = retrieveProcessExecution(res);
				someArguments.put(PROCESS_EXECUTION, processExecution);
			} else {
				return res;
			}
		}

		Token token = findSingleToken(processExecution);
		if (token != null) {
			Object context = someArguments.get(FinishTaskCommand.CONTEXT);
			if (context == null) {
				// Decide about the next step.
				HandlerResult suspended = HandlerResult.suspended();
				Node node = GuiEngine.getInstance().getNextNode(token);

				if (GuiEngine.getInstance().isManual(node)) {
					new SelectTransitionDialog(token, suspended).open(aContext);
				} else {
					Edge edge = GuiEngine.getInstance().getSingleOutgoingEdge(token);
					Command continuationYes = suspended.resumeContinuation(Collections.singletonMap(FinishTaskCommand.CONTEXT, edge));
					Command continuationNo = suspended.resumeContinuation(Collections.singletonMap(FinishTaskCommand.CONTEXT, FinishTaskCommand.CANCEL));
					String message = Resources.getInstance().getString(I18NConstants.CONFIRM_FINISH_TASK);
					MessageBox.confirm(aContext.getWindowScope(), MessageType.CONFIRM, message,
						MessageBox.button(ButtonType.YES, continuationYes), MessageBox.button(ButtonType.NO, continuationNo));
				}
				return suspended;
			} else {
				if (context instanceof Edge) {
					// Advance process to next step(s).
					KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
					try (Transaction tx = kb.beginTransaction()) {
						ExecutionEngine.getInstance().execute(token, (Edge) context);
						tx.commit();
					} catch (Exception ex) {
						throw new RuntimeException("Can not complete task for token '" + token + "'.", ex);
					}
				}
			}
		}

		Token firstTask = firstTask(processExecution);
		WithPostCreateActions.processCreateActions(_actions, aComponent, firstTask);

		return HandlerResult.DEFAULT_RESULT;
	}

	private Token firstTask(ProcessExecution processExecution) {
		Person person = TLContext.currentUser();
		for (Token token : processExecution.getUserRelevantTokens()) {
			if (GuiEngine.getInstance().isActor(person, token)) {
				return token;
			}
		}
		return null;
	}

	private Token findSingleToken(ProcessExecution processExecution) {
		Set<? extends Token> tokens = processExecution.getUserRelevantTokens();
		Filter<Token> filter = new Filter<>() {
			Person _person = TLContext.currentUser();

			@Override
			public boolean accept(Token token) {
				return GuiEngine.getInstance().isActor(_person, token);
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

}
