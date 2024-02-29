/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Provides helper methods for displaying problems..
 * 
 * @author <a href="mailto:dkh@top-logic.com">Dirk K&ouml;hlhoff</a>
 */
public class ErrorHandlingHelper extends ManagedClass {

	private static final int DIALOG_HEIGHT = 250;

	private static final int DIALOG_WIDTH = 500;
	
	private static final int MAX_DIALOG_WIDTH = 100;
	
	private static final int MAX_DIALOG_HEIGHT = 100;

	/**
	 * Configuration of the {@link ErrorHandlingHelper}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ServiceConfiguration<ErrorHandlingHelper> {

		/**
		 * Configuration of the renderer that actually renders the error dialog
		 */
		PolymorphicConfiguration<? extends Renderer<? super HandlerResult>> getRenderer();

	}

	private Renderer<? super HandlerResult> _renderer;

	/**
	 * Creates an {@link ErrorHandlingHelper}.
	 * 
	 * @param config
	 *        configuration of the new {@link ErrorHandlingHelper}
	 */
	public ErrorHandlingHelper(InstantiationContext context, Config config) {
		super(context, config);
		_renderer = context.getInstance(config.getRenderer());
	}

	/**
	 * the content renderer of the error dialog
	 */
	protected Renderer<? super HandlerResult> getRenderer() {
		return _renderer;
	}

	/**
	 * Display potential error messages in the given {@link HandlerResult}.
	 * 
	 * @param windowScope
	 *        The window in which to show the error dialog.
	 * @param handlerResult
	 *        The {@link HandlerResult} to check for errors.
	 * @return Direct {@link ClientAction} that are marshaled back to the client.
	 */
	public static ClientAction[] transformHandlerResult(WindowScope windowScope, final HandlerResult handlerResult) {
		if (handlerResult == null) {
			return ClientAction.NO_ACTIONS;
		}

		if (handlerResult.isSuccess() || handlerResult.isSuspended()) {
			return ClientAction.NO_ACTIONS;
		}

		return Module.INSTANCE.getImplementationInstance().openErrorDialogActions(windowScope, handlerResult);
	}

	/**
	 * Opens the dialog with the error message.
	 * 
	 * @param windowScope
	 *        the window to open the dialog.
	 * @param handlerResult
	 *        must not be null.
	 */
	protected ClientAction[] openErrorDialogActions(WindowScope windowScope, final HandlerResult handlerResult) {
		handlerResult.logError();
		Command errorContinuation = handlerResult.errorContinuation();

		HTMLFragment detailFragment = createDetailFragment(handlerResult);
		ResKey title = handlerResult.getErrorTitle();

		if (errorContinuation == Command.DO_NOTHING) {
			handleErrorAsInfoServiceItem(handlerResult, detailFragment, title);
		} else {
			HTMLFragment titleFragment = createTitleFragment(handlerResult);
			HTMLFragment titleHtmlFragment = Fragments.message(title);
			handleErrorAsDialog(windowScope, titleFragment, detailFragment, titleHtmlFragment, errorContinuation);
		}

		return ClientAction.NO_ACTIONS;
	}

	private HTMLFragment createTitleFragment(HandlerResult handlerResult) {
		return (context, out) -> writeErrorMessageTitle(context, out, handlerResult);
	}

	private HTMLFragment createDetailFragment(HandlerResult handlerResult) {
		return (context, out) -> getRenderer().write(context, out, handlerResult);
	}

	private void writeErrorMessageTitle(DisplayContext context, TagWriter out, HandlerResult handlerResult)
			throws IOException {
		out.beginTag(H3);
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "tl-info-service-item__error-icon");
		out.endBeginTag();
		HTMLFragment titleIcon = getMessageTypeIcon(handlerResult);
		titleIcon.write(context, out);
		out.endTag(SPAN);

		String title = title(context, handlerResult);
		if (title != null) {
			title = title.replaceAll("<br\\s*/?>", " ");
		}

		out.writeText(title);
		out.endTag(H3);
	}

	private String title(DisplayContext context, HandlerResult handlerResult) {
		return context.getResources().getString(handlerResult.getErrorTitle());
	}

	private void handleErrorAsDialog(WindowScope windowScope, HTMLFragment titleFragment, HTMLFragment detail,
			HTMLFragment messageTitle, Command errorContinuation) {
		LayoutData layout = createLayoutData();
		HTMLFragment messageBox = Fragments.concat(titleFragment, detail);
		CommandModel button = MessageBox.button(ButtonType.OK, errorContinuation);
		MessageBox.confirm(windowScope, layout, true, messageTitle, messageBox, button);
	}

	private void handleErrorAsInfoServiceItem(HandlerResult handlerResult, HTMLFragment detail, ResKey title) {
		switch (handlerResult.getErrorSeverity()) {
			case ERROR:
				InfoService.showError(title, detail);
				break;
			case SYSTEM_FAILURE:
				InfoService.showError(title, detail);
				break;
			case WARNING:
				InfoService.showWarning(title, detail);
				break;
			case INFO:
				InfoService.showInfo(title, detail);
				break;
		}
	}

	private HTMLFragment getMessageTypeIcon(HandlerResult handlerResult) {
		switch (handlerResult.getErrorSeverity()) {
			case ERROR:
				return Icons.ERROR;
			case SYSTEM_FAILURE:
				return Icons.SYSTEM_FAILURE;
			case WARNING:
				return Icons.WARNING;
			case INFO:
				return Icons.INFO;
		}
		return Icons.ERROR;
	}

	/**
	 * Determines the size of the opened dialog
	 */
	protected DefaultLayoutData createLayoutData() {
		return new DefaultLayoutData(dim(DIALOG_WIDTH, PIXEL), MAX_DIALOG_WIDTH, dim(DIALOG_HEIGHT, PIXEL), MAX_DIALOG_HEIGHT, Scrolling.AUTO);
	}

	/**
	 * Determines whether the given {@link Throwable} bases on an internal problem (cannot be
	 * explained to the user in full detail).
	 * 
	 * @param problem
	 *        The {@link Throwable} to inspect
	 * @return whether the given {@link Throwable} has (hereditarily) a cause which is not a
	 *         {@link Throwable}.
	 */
	public static boolean isInternalError(Throwable problem) {
		if (!(problem instanceof I18NFailure)) {
			return true;
		}

		Throwable cause = problem.getCause();
		if (cause == null || cause == problem) {
			return false;
		}
		return isInternalError(cause);
	}

	/**
	 * {@link TypedRuntimeModule} that creates the application wide {@link ErrorHandlingHelper}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<ErrorHandlingHelper> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<ErrorHandlingHelper> getImplementation() {
			return ErrorHandlingHelper.class;
		}

	}

}