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
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.layout.execution.ActiveTaskComponent;
import com.top_logic.bpe.layout.execution.SelectTransitionDialog;
import com.top_logic.bpe.layout.execution.SelectTransitionDialog.Decision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.SelfCheckProvider;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WarningsDialog;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
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
	 * Command argument passed by the transition dialog containing a form model with additional
	 * data.
	 */
	public static final String ADDITIONAL = "additional";

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

		@Override
		@ImplementationClassDefault(SelfCheckProvider.class)
		@ItemDefault
		PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();
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
		// Force model validation to ensure latest model data is used.
		// Without explicit validation, concurrent user modifications
		// could lead to outdated model being processed, causing data inconsistencies.
		aComponent.getMainLayout().globallyValidateModel(aContext);
		if (!ComponentUtil.isValid(model)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}

		Token token = ((ActiveTaskComponent) aComponent).getToken(model);
		
		Object context = someArguments.get(CONTEXT);
		EditComponent editComponent = (EditComponent) aComponent;

		if (context == CANCEL) {
			return HandlerResult.DEFAULT_RESULT;
		}

		// Handle initial execution without context
		if (context == null) {
			// check if there is already a lock otherwise create it
			if (!editComponent.isInEditMode()) {
				editComponent.getLockHandler().acquireLock(model);
			}
			// Confirm/decide about how to progress.
			GuiEngine engine = GuiEngine.getInstance();
			Edge outgoing = GuiEngine.getSingleOutgoingEdge(token);
			Node next = outgoing.getTarget();

			List<ResKey> warnings = engine.checkWarnings(token, outgoing);
			if (engine.needsDecision(next)) {
				boolean warningsAcknowledged = Utils.isTrue((Boolean) someArguments.get("warningsAcknowledged"));
				// Show warnings dialog if needed and not already acknowledged
				if (!warnings.isEmpty() && !warningsAcknowledged) {
					// Create suspended state for warning acknowledgment
					HandlerResult suspended = HandlerResult.suspended();
					Command acknowledge = new Command() {
						@Override
						public HandlerResult executeCommand(DisplayContext anInnerContext) {
							return suspended.resumeContinuation(Collections.singletonMap("warningsAcknowledged", true))
								.executeCommand(anInnerContext);
						}
					};

					// Show warnings dialog, before showing SelectTransitionDialog
					WarningsDialog.openWarningsDialogWithoutFormContext(aContext.getWindowScope(),
						ResKey.legacy("workflow"),
						warnings, acknowledge, Command.DO_NOTHING);

					return suspended;
				}
				// no warnings or warnings are already acknowledged
				return confirmFinish(aContext, token, warnings, editComponent);
			} else {
				// Check for errors when no decision is needed
				List<ResKey> errors = engine.checkErrors(token, outgoing);
				if (!errors.isEmpty()) {
					return MessageBox.newBuilder(MessageType.CONFIRM)
						.message(Fragments.messageList(errors))
						.buttons(MessageBox.button(ButtonType.OK))
						.confirm(aContext.getWindowScope());
				}
				return confirmFinish(aContext, token, warnings, editComponent);
			}
		}


		// Decision is already made.
		Decision decision = (Decision) context;
		try (Transaction tx = token.tKnowledgeBase().beginTransaction()) {
			Object additional = someArguments.get(ADDITIONAL);
			ExecutionEngine.getInstance().execute(token, decision.getPath(), additional);
			tx.commit();
		}

		WithPostCreateActions.processCreateActions(_actions, aComponent, token.getProcessExecution());

		if (editComponent.isInEditMode()) {
			// force permission re-evaluation for new context by resetting edit mode and delegating
			// validation to reswitchToEdit()
			editComponent.setViewMode();
			editComponent.reswitchToEdit();
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult confirmFinish(DisplayContext context, Token token, List<ResKey> warnings,
			EditComponent editComponent) {
		HandlerResult suspended = HandlerResult.suspended();
		SelectTransitionDialog selectTransitionDialog = new SelectTransitionDialog(token, suspended);
		selectTransitionDialog.getDialogModel().addListener(DialogModel.CLOSED_PROPERTY, new DialogClosedListener() {

			@Override
			public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
				// if transition dialog is closed release lock if not in EditMode
				if (!editComponent.isInEditMode()) {
					editComponent.getLockHandler().releaseLock();
				}
			}
		});
		selectTransitionDialog.open(context);
		return suspended;
	}

	@Override
	public ResKey getResourceKey(LayoutComponent component) {
		Object model = CommandHandlerUtil.getTargetModel(this, component, Collections.emptyMap());
		if (model instanceof Token task) {
			Set<? extends Edge> edges = task.getNode().getOutgoing();
			if (edges.size() == 1) {
				Edge edge = edges.iterator().next();
				ResKey edgeLabel = edge.getTitle();
				if (edgeLabel != null) {
					return edgeLabel;
				}
			}
		}

		return super.getResourceKey(component);
	}

}