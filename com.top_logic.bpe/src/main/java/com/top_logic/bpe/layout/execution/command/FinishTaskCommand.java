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
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.layout.execution.SelectTransitionDialog;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} that completes a task and advances the process to the next step(s).
 * 
 * <p>
 * A confirm-dialog is shown before switching to the next task. If a manual {@link Gateway} follows
 * the completed task, the user can explicitly choose the next step.
 * </p>
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
@Label("Complete task")
@InApp(classifiers = "workflow")
public class FinishTaskCommand extends AbstractCommandHandler implements WithPostCreateActions {

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
	public interface Config extends AbstractCommandHandler.Config, WithPostCreateActions.Config {
		@Override
		@FormattedDefault("css:ICON_PLAY")
		ThemeImage getImage();
	}

	private final List<PostCreateAction> _actions;

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

		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		Token token = (Token) model;

		HandlerResult store = storeIfNecessary(aContext, aComponent);
		if (!store.isSuccess()) {
			return store;
		}

		Object context = someArguments.get(CONTEXT);
		if (context == CANCEL) {
			// Ignore.
			return HandlerResult.DEFAULT_RESULT;
		}

		if (context == null) {
			// Confirm/decide about how to progress.
			GuiEngine engine = GuiEngine.getInstance();
			Edge outgoing = engine.getSingleOutgoingEdge(token);
			Node next = outgoing.getTarget();
			if (engine.needsDecision(next)) {
				return confirmFinish(aContext, token);
			} else {
				ResKey error = engine.checkError(token, outgoing);
				if (error != null) {
					return MessageBox.newBuilder(MessageType.CONFIRM)
						.message(Fragments.message(error))
						.buttons(MessageBox.button(ButtonType.OK))
						.confirm(aContext.getWindowScope());
				}
				return confirmFinish(aContext, token);
			}
		}

		// Decision is already made.
		Edge edge = (Edge) context;
		try (Transaction tx = token.tKnowledgeBase().beginTransaction()) {
			ExecutionEngine.getInstance().execute(token, edge, null);
			tx.commit();
		}

		WithPostCreateActions.processCreateActions(_actions, aComponent, token.getProcessExecution());
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult confirmFinish(DisplayContext context, Token token) {
		HandlerResult suspended = HandlerResult.suspended();
		new SelectTransitionDialog(token, suspended).open(context);
		return suspended;
	}

	@Override
	public ResKey getResourceKey(LayoutComponent component) {
		Object model = CommandHandlerUtil.getTargetModel(this, component, Collections.emptyMap());
		if (model instanceof Token task) {
			Set<? extends Edge> edges = task.getNode().getOutgoing();
			if (edges.size() == 1) {
				Edge edge = edges.iterator().next();
				ResKey edgeLabel = edge.getLabel();
				if (edgeLabel != null) {
					return edgeLabel;
				}
			}
		}

		return super.getResourceKey(component);
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