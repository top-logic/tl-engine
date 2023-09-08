/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.Builder;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * {@link CommandHandler} opening a confirm dialog for the user before executing the actual command.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfirmCommandHandler extends AbstractCommandHandler {

	/** Key in the argument that is set when the execution was confirmed by the user. */
	protected static final String CONFIRMED_KEY = "__execution_confirmed";

	/**
	 * Creates a new {@link ConfirmCommandHandler}.
	 */
	public ConfirmCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		if (!someArguments.containsKey(CONFIRMED_KEY) && showConfirm(aContext, aComponent, model, someArguments)) {
			final HandlerResult suspended = HandlerResult.suspended();

			HashMap<String, Object> enhancedArgs = new HashMap<>(someArguments);
			enhancedArgs.put(CONFIRMED_KEY, Boolean.TRUE);
			Command continuation = suspended.resumeContinuation(enhancedArgs);
			openConfirmDialog(aContext, aComponent, model, someArguments, continuation);
			return suspended;
		}
		return internalHandleCommand(aContext, aComponent, model, someArguments);
	}

	/**
	 * Opens the dialog created by
	 * {@link #createConfirmDialog(DisplayContext, LayoutComponent, Object, Map, Command)}.
	 */
	protected void openConfirmDialog(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments, Command continuation) {
		DialogWindowControl dialog = createConfirmDialog(context, component, model, someArguments, continuation);
		context.getWindowScope().openDialog(dialog);
	}

	/**
	 * Creates dialog that allows the user to answer the confirm question or to input some necessary
	 * parameters.
	 * 
	 * <p>
	 * The default implementation creates a {@link MessageBox} with given
	 * {@link #getCallbackLayout() layout size} and
	 * {@link #getConfirmMessage(DisplayContext, LayoutComponent, Object, Map) message}.
	 * </p>
	 * 
	 * @param context
	 *        The {@link DisplayContext} in which the command is initially triggered.
	 * @param component
	 *        Component as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param model
	 *        Model as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param someArguments
	 *        Arguments as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param continuation
	 *        {@link Command} to execute when the user has confirmed the confirm question. This
	 *        command is essentially
	 *        {@link #internalHandleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected DialogWindowControl createConfirmDialog(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments, Command continuation) {
		MessageType type = MessageType.CONFIRM;
		Resources resources = context.getResources();
		Builder builder = MessageBox.newBuilder(type)
			.layout(getCallbackLayout())
			.title(resources.getString(type.getTitleKey()))
			.message(resources.getString(getConfirmMessage(context, component, model, someArguments)))
			.buttons(MessageBox.button(ButtonType.YES, continuation), MessageBox.button(ButtonType.NO));
		return builder.toDialog();
	}

	/**
	 * Defines the layout of the {@link MessageBox} as opened by
	 * {@link #openConfirmDialog(DisplayContext, LayoutComponent, Object, Map, Command)} in this
	 * implementation.
	 */
	protected LayoutData getCallbackLayout() {
		return MessageBox.getDefaultLayout();
	}

	/**
	 * The {@link ResKey} to display in the {@link MessageBox} opened by
	 * {@link #openConfirmDialog(DisplayContext, LayoutComponent, Object, Map, Command)} in this
	 * implementation.
	 * 
	 * @param context
	 *        The {@link DisplayContext} in which the command is initially triggered.
	 * @param component
	 *        Component as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param model
	 *        Model as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param someArguments
	 *        Arguments as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected ResKey getConfirmMessage(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		Object targetModel = CommandHandlerUtil.getTargetModel(this, component, someArguments);
		return (targetModel == null) ?
			I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND.fill(getResourceKey(component))
			: I18NConstants.DEFAULT_CONFIRM_MESSAGE__COMMAND_MODEL.fill(getResourceKey(component), targetModel);
	}

	/**
	 * Whether the {@link #openConfirmDialog(DisplayContext, LayoutComponent, Object, Map, Command)
	 * confirm dialog} must be opened.
	 * 
	 * @param context
	 *        The {@link DisplayContext} in which the command is initially triggered.
	 * @param component
	 *        Component as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param model
	 *        Model as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param someArguments
	 *        Arguments as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected boolean showConfirm(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		return true;
	}

	/**
	 * Actual implementation of {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * when user confirms the command or no confirm message is displayed.
	 * 
	 * @param context
	 *        The {@link DisplayContext} in which the command is initially triggered.
	 * @param component
	 *        Component as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param model
	 *        Model as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param someArguments
	 *        Arguments as passed in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * 
	 * @return Value as result of for
	 *         {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected abstract HandlerResult internalHandleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> someArguments);

}
