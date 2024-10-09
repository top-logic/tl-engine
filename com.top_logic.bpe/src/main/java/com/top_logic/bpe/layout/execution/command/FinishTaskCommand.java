/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.command;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.layout.execution.SelectTransitionDialog;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContext;

/**
 * {@link CommandHandler} to finish a task. Shows a confirm-dialog before switching to the next
 * task. In case of manual {@link Gateway}s the user can choose the next step.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
@InApp(classifiers = "workflow")
public class FinishTaskCommand extends AbstractCommandHandler {

	/**
	 * Command argument that selects the next edge to walk.
	 */
	public static final String CONTEXT = "nextTask";

	/**
	 * Special value for {@link #CONTEXT} that skipps this command.
	 */
	public static final NamedConstant CANCEL = new NamedConstant("cancel");

	/**
	 * Configuration options for {@link FinishTaskCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * @see #getTokenTable()
		 */
		String TOKEN_TABLE = "tokenTable";

		/**
		 * Context component that selects the active task.
		 */
		@Name(TOKEN_TABLE)
		ComponentName getTokenTable();

	}

	/**
	 * Creates a {@link FinishTaskCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FinishTaskCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		Token token = (Token) aComponent.getModel();

		HandlerResult store = storeIfNecessary(aContext, aComponent);
		if (!store.isSuccess()) {
			return store;
		}

		Object context = someArguments.get(CONTEXT);
		if (context == null) {
			GuiEngine engine = GuiEngine.getInstance();
			Node nextNode = engine.getNextNode(token);
			if (engine.isManual(nextNode)) {
				HandlerResult suspended = HandlerResult.suspended();
				new SelectTransitionDialog(token, suspended).open(aContext);
				return suspended;
			} else {
				Edge edge = engine.getSingleOutgoingEdge(token);
				ResKey error = engine.checkError(edge, token);
				if (error != null) {
					return MessageBox.newBuilder(MessageType.CONFIRM)
						.message(Fragments.message(error))
						.buttons(MessageBox.button(ButtonType.OK))
						.confirm(aContext.getWindowScope());
				} else {
					HandlerResult suspended = HandlerResult.suspended();
					Command continuation = suspended.resumeContinuation(Collections.singletonMap(CONTEXT, edge));
					MessageBox.newBuilder(MessageType.CONFIRM)
						.message(Fragments.message(I18NConstants.CONFIRM_FINISH_TASK))
						.buttons(MessageBox.button(ButtonType.YES, continuation), MessageBox.button(ButtonType.NO))
						.confirm(aContext.getWindowScope());
					return suspended;
				}
			}
		} else if (context instanceof Edge edge) {
			KnowledgeBase kb = token.tKnowledgeBase();
			try (Transaction tx = kb.beginTransaction()) {
				ExecutionEngine.getInstance().execute(token, edge);
				tx.commit();
			}

			new StepOutHelper(aComponent).stepOut(aContext);
			/* Stepping out leads to a deferred display of the outer tile, so also displaying the
			 * active task component must occur deferred. */
			aContext.getLayoutContext().notifyInvalid(displayContext -> {
				showActiveTask(aComponent, token.getProcessExecution(), context);
				aComponent.getComponentByName(config().getTokenTable()).invalidate();
			});
			return HandlerResult.DEFAULT_RESULT;
		} else if (context == CANCEL) {
			// Ignore.
			return HandlerResult.DEFAULT_RESULT;
		} else {
			throw new UnreachableAssertion("Invalid context: " + context);
		}
	}

	private void showActiveTask(LayoutComponent aComponent, ProcessExecution pe, Object context) {
		Node target = target(context);
		if (target != null) {

			Set<? extends Token> tokens = pe.getUserRelevantTokens();
			Person person = TLContext.currentUser();
			for (Token token : tokens) {
				if (GuiEngine.getInstance().isActor(person, token)) {
					if (token.getNode() == target) {
						LayoutComponent listComponent = componentByName(aComponent, config().getTokenTable());
						if (listComponent != null) {
							listComponent.makeVisible();
							((Selectable) listComponent).setSelected(token);
						}
						break;
					}
				}
			}
		}
	}

	private LayoutComponent componentByName(LayoutComponent aComponent, ComponentName name) {
		if (name == null) {
			return null;
		}
		return aComponent.getMainLayout().getComponentByName(name);
	}

	private Node target(Object context) {
		if (context instanceof Edge) {
			return ((Edge) context).getTarget();
		} else if (context instanceof Node) {
			return (Node) context;
		}
		return null;
	}

	private HandlerResult storeIfNecessary(DisplayContext aContext, LayoutComponent aComponent) {
		EditComponent editComponent = (EditComponent) aComponent;
		FormContext formContext = editComponent.getFormContext();
		if (formContext.isChanged()) {
			if (formContext.checkAll()) {
				if (editComponent.isInEditMode()) {
					HandlerResult res = editComponent.getApplyClosure().executeCommand(aContext);
					editComponent.setViewMode();
					return res;
				}
			} else {
				HandlerResult res = new HandlerResult();
				AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, res);
				return res;
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}