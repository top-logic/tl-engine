/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.execution;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageLinkButtonRenderer;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.layout.messagebox.MessageArea;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.renderers.ThrowableRenderer;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.util.ActionResourceProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Driver of a script execution in a live application.
 * 
 * <p>
 * After each action execution, the current request is completed, the client updates are rendered
 * and the client is forced to immediately post another request.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptDriver {

	private FrameScope _frameScope;

	private LiveActionExecutor _executor;

	/**
	 * Creates a {@link ScriptDriver}.
	 */
	public ScriptDriver(FrameScope frameScope, LiveActionExecutor executor) {
		_frameScope = frameScope;
		_executor = executor;
	}

	/**
	 * Performs the next action and eventually schedules itself for further execution.
	 */
	public HandlerResult next(DisplayContext displayContext) {
		try {
			return processNext(displayContext);
		} catch (WaitTimeoutException ex) {
			// Make a round-trip to the client to check, whether the client is still there.
			scheduleNextStep();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private HandlerResult processNext(DisplayContext displayContext) throws WaitTimeoutException {
		if (_executor.isStopped()) {
			// Operation was canceled.
			onStop();
			return HandlerResult.DEFAULT_RESULT;
		}

		try {
			try {
				_executor.processNext(displayContext);
			} catch (EnvironmentMismatch ex) {
				onStop();
				return handleEnvironmentMismatch(ex);
			}
		} catch (WaitTimeoutException ex) {
			throw ex;
		} catch (Throwable ex) {
			onStop();

			return openErrorDialog(_executor.scriptPosition(), Collections.<ResKey> emptyList(), ex);
		}
		HandlerResult result = checkForErrors(_executor.getLog());

		if (_executor.hasNext()) {
			if (result.isSuccess()) {
				scheduleNextStep();

				// Note: Produce less updates on the GUI. The following code would display the
				// currently executed action after each step which greatly decreases test
				// execution
				// performance.
				//
				// selectNext(scriptRecorderTree, executor.current());
			} else {
				onStop();
			}
		} else if (_executor.isMaster()) {
			onStop();
		}

		if (!result.isSuccess()) {
			return openErrorDialog(_executor.scriptPosition(), result.getEncodedErrors(), result.getException());
		}

		return result;
	}

	private void scheduleNextStep() {
		ClientAction executeNext =
			JSSnipplet.createServerCallback(new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					return next(context);
				}
			});
		scheduleAction(executeNext);
	}

	private void scheduleAction(ClientAction executeNext) {
		_frameScope.addClientAction(executeNext);
	}

	private WindowScope windowScope() {
		return _frameScope.getWindowScope();
	}

	/**
	 * Hook called, before the replay ends.
	 */
	protected void onStop() {
		_executor.stop();
	}

	private HandlerResult handleEnvironmentMismatch(EnvironmentMismatch ex) {
		String message = Resources.getInstance().getString(ex.getErrorKey());
		return MessageBox.confirm(windowScope(), MessageType.ERROR, message,
			MessageBox.button(ButtonType.OK));
	}

	private HandlerResult openErrorDialog(final TLTreeNode<?> currentNode, final List<ResKey> errors,
			final Throwable exception) {
		DisplayValue title = new ResourceText(I18NConstants.ACTION_FAILED_TITLE);
		HTMLFragment message = new MessageArea(MessageBox.MessageType.ERROR.getTypeImage()) {

			@Override
			protected void writeMessage(DisplayContext context, TagWriter out) throws IOException {
				ApplicationAction currentAction = (ApplicationAction) currentNode.getBusinessObject();

				String actionLabel = ActionResourceProvider.newInstance().getLabel(currentAction);

				String failureMessage = customFailureMessage(currentNode);
				for (ResKey error : errors) {
					failureMessage =
						(StringServices.isEmpty(failureMessage) ? "" : failureMessage + " ")
							+ context.getResources().getString(error);
				}
				if (exception != null) {
					failureMessage =
						(StringServices.isEmpty(failureMessage) ? "" : failureMessage + " ")
							+ exception.getMessage();
				}
				String errorMessage = context.getResources().getString(
					I18NConstants.ACTION_FAILED_MESSAGE__ACTION_FAILURE.fill(actionLabel,
						failureMessage));
				out.writeText(errorMessage);

				if (exception != null) {
					final VisibilityModel visibility = new VisibilityModel.Default(false);

					class ToggleCommand extends AbstractCommandModel {
						@Override
						protected HandlerResult internalExecuteCommand(DisplayContext executionContext) {
							boolean isExpanded = !visibility.isVisible();
							visibility.setVisible(isExpanded);
							setImage(isExpanded ? Icons.EXPANDED_ICON : Icons.COLLAPSED_ICON);
							setLabel(executionContext.getResources()
								.getString(isExpanded ? I18NConstants.HIDE_DETAILS : I18NConstants.SHOW_DETAILS));
							return HandlerResult.DEFAULT_RESULT;
						}
					}

					CommandModel toggle = new ToggleCommand();
					ScriptingRecorder.annotateAsDontRecord(toggle);
					toggle.setImage(Icons.COLLAPSED_ICON);
					toggle.setLabel(context.getResources().getString(I18NConstants.SHOW_DETAILS));

					p(new ButtonControl(toggle, ImageLinkButtonRenderer.INSTANCE)).write(context, out);

					conditional(visibility, div(rendered(ThrowableRenderer.INSTANCE, exception))).write(context, out);
				}
			}

			private String customFailureMessage(TLTreeNode<?> node) {
				while (node != null) {
					String result = ((ApplicationAction) node.getBusinessObject()).getFailureMessage();
					if (!StringServices.isEmpty(result)) {
						return result;
					}
					node = node.getParent();
				}
				return null;
			}

		};
		CommandModel okButton = MessageBox.button(ButtonType.OK);
		ScriptingRecorder.annotateAsDontRecord(okButton);
		return MessageBox.confirm(
			windowScope(), DefaultLayoutData.scrollingLayout(400, 200), true, title, message,
			okButton);
	}

	private HandlerResult checkForErrors(BufferingProtocol protocol) {
		if (protocol.hasErrors()) {
			return HandlerResult.getErrorResult(protocol);
		} else {
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}