/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collections;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * General {@link AbstractCommandHandler} asking for confirmation for further processing.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class AbstractConfirmationHandler extends AbstractCommandHandler {

	private static final String IS_RESUMING = "isResuming";

	/**
	 * Creates a {@link AbstractConfirmationHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AbstractConfirmationHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> args) {
		if (args.get(IS_RESUMING) == null) {
			return executeConfirmation(context, model);
		}

		return handleCommandInternal(context, component, model, args);
	}

	private HandlerResult executeConfirmation(DisplayContext context, Object model) {
		HandlerResult result = HandlerResult.suspended();

		createConfirmation(context, model, createContinuationCommand(result));

		return result;
	}

	private HandlerResult createConfirmation(DisplayContext context, Object model, Command resume) {
		WindowScope scope = context.getWindowScope();

		CommandModel noButton = getNoButton();
		CommandModel yesButton = getYesButton(resume);

		DefaultLayoutData layout = createLayoutData();

		return MessageBox.confirm(scope, layout, true, getTitle(model), getMessage(model), noButton, yesButton);
	}

	/**
	 * Button model to accept the dialog and resuming the given command.
	 */
	protected CommandModel getYesButton(Command resume) {
		return MessageBox.button(ButtonType.YES, resume);
	}

	/**
	 * Button model to cancel the dialog.
	 */
	protected CommandModel getNoButton() {
		return MessageBox.button(ButtonType.NO);
	}

	private DefaultLayoutData createLayoutData() {
		DisplayDimension width = getConfirmationDialogWidth();
		DisplayDimension height = getConfirmationDialogHeight();

		return new DefaultLayoutData(width, 100, height, 100, Scrolling.AUTO);
	}

	private DisplayDimension getConfirmationDialogHeight() {
		return DisplayDimension.dim(250, DisplayUnit.PIXEL);
	}

	private DisplayDimension getConfirmationDialogWidth() {
		return DisplayDimension.dim(500, DisplayUnit.PIXEL);
	}

	private Command createContinuationCommand(HandlerResult result) {
		return result.resumeContinuation(Collections.singletonMap(IS_RESUMING, Boolean.TRUE));
	}

	/**
	 * Command handling executed after confirmation.
	 */
	protected abstract HandlerResult handleCommandInternal(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> args);

	/**
	 * Title of the confirmation dialog.
	 */
	protected abstract HTMLFragment getTitle(Object model);

	/**
	 * Message body of the confirmation dialog.
	 */
	protected abstract HTMLFragment getMessage(Object model);

}
