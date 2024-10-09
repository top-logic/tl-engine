/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.command;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.layout.execution.ProcessExecutionCreateComponent;
import com.top_logic.bpe.layout.execution.command.FinishTaskCommand.SelectTransitionDialog;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.ComponentName;
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
public class StartProcessExecutionHandler extends AbstractCommandHandler {

	private static final String PROCESS_EXECUTION = "processExecution";

	public interface Config extends AbstractCommandHandler.Config {

		String TOKEN_TABLE = "tokenTable";

		@Name(TOKEN_TABLE)
		ComponentName getTokenTableName();

	}

	public StartProcessExecutionHandler(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {

		ProcessExecution pe = (ProcessExecution) someArguments.get(PROCESS_EXECUTION);
		if (pe == null) {
			CommandHandler handler = CommandHandlerFactory.getInstance().getHandler("createProcessExecution");
			HandlerResult res = handler.handleCommand(aContext, aComponent, model, someArguments);
			if (res.isSuccess()) {
				pe = retrieveProcessExecution(res);
				someArguments.put(PROCESS_EXECUTION, pe);
			} else {
				return res;
			}

		}

		HandlerResult res = skipFirstTask(pe, aContext, aComponent, someArguments);
		return res;
	}

	private HandlerResult skipFirstTask(ProcessExecution pe, DisplayContext aContext, LayoutComponent aComponent,
			Map<String, Object> someArguments) {
		Token token = findSingleToken(pe);
		if (token != null) {
			Object context = someArguments.get(FinishTaskCommand.CONTEXT);
			if (context == null) {
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
					
					KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
					Transaction tx = null;
					try {
						tx = kb.beginTransaction();
						ExecutionEngine.getInstance().execute(token, (Edge) context);
						tx.commit();
						
					} catch (Exception ex) {
						throw new RuntimeException("Can not finish Task for token '" + token + "'.", ex);
					} finally {
						if (tx != null) {
							tx.rollback();
						}
					}
				}

			}
		}

		StepOutHelper helper = new StepOutHelper(aComponent);
		helper.stepOut(aContext);
		/* Stepping out leads to a deferred display of the outer tile, so also displaying the active
		 * task component must occur deferred. */
		aContext.getLayoutContext().notifyInvalid(displayContext -> showActiveTask(aComponent, pe));

		return HandlerResult.DEFAULT_RESULT;
	}

	private Token findSingleToken(ProcessExecution pe) {
		Set<? extends Token> tokens = pe.getUserRelevantTokens();
		Filter filter = new Filter<Token>() {
			Person _person = TLContext.currentUser();

			@Override
			public boolean accept(Token token) {
				return GuiEngine.getInstance().isActor(_person, token);
			}
		};
		tokens = FilterUtil.filterSet(filter, tokens);
		if (tokens.size() == 1) {
			return (Token) CollectionUtil.getSingleValueFromCollection(tokens);
		} else {
			return null;
		}
	}

	private void showActiveTask(LayoutComponent aComponent, ProcessExecution pe) {
		Set<? extends Token> tokens = pe.getUserRelevantTokens();
		PersonManager r = PersonManager.getManager();
		Person person = TLContext.currentUser();
		for (Token token : tokens) {
			if (GuiEngine.getInstance().isActor(person, token)) {
				LayoutComponent listComponent = componentByName(aComponent, config().getTokenTableName());
				if (listComponent != null) {
					listComponent.makeVisible();
					((Selectable) listComponent).setSelected(token);
				}
				break;
			}
		}
	}

	private LayoutComponent componentByName(LayoutComponent aComponent, ComponentName name) {
		if (name == null) {
			return null;
		}
		return aComponent.getMainLayout().getComponentByName(name);
	}

	private ProcessExecution retrieveProcessExecution(HandlerResult res) {
		ProcessExecution pe = (ProcessExecution) CollectionUtil.getFirst(res.getProcessed());
		return pe;
	}

}
