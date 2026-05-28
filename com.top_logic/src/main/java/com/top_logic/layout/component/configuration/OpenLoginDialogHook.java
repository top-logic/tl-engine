/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import java.util.Collections;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.mig.html.layout.LoginHook;
import com.top_logic.mig.html.layout.LoginHooks;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.DefaultBookmarkHandler;
import com.top_logic.util.TLContext;

/**
 * {@link LoginHook} opening a login window for the anonymous user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenLoginDialogHook implements LoginHook {

	private static final Property<Command> BOOKMARK = TypedAnnotatable.property(Command.class, "bookmark");

	@Override
	public void handleLogin(MainLayout mainLayout, Runnable callback) {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		TLSessionContext sessionContext = context.getSessionContext();

		// run attached bookmark handler
		Command bookmarkHandler = sessionContext.reset(BOOKMARK);
		if (bookmarkHandler != null) {
			bookmarkHandler.executeCommand(context);
		}

		if (!TLContext.isAnonymous()) {
			callback.run();
			return;
		}
		SubsessionHandler subsessionHandler = findSubSessionHandler(context);
		HttpServletRequest request = context.asRequest();
		Map<String, ?> arguments = request.getParameterMap();

		CommandHandler handler;
		Map<String, Object> gotoArguments;
		if (!arguments.isEmpty()) {
			handler = CommandHandlerFactory.getInstance().getHandler(DefaultBookmarkHandler.COMMAND_RESOLVE_BOOKMARK);
			if (handler != null) {
				gotoArguments = subsessionHandler.resolveBookmarkArguments(request, arguments);
			} else {
				gotoArguments = Collections.emptyMap();
			}
		} else {
			handler = null;
			gotoArguments = Collections.emptyMap();
		}
		
		Runnable closeCallback;
		if (!gotoArguments.isEmpty()) {
			context.set(SubsessionHandler.BOOKMARK_HANDLED, true);
			Command displayBookmark = ctx -> {
				findSubSessionHandler(ctx).processBookmark(ctx, handler, gotoArguments);
				return HandlerResult.DEFAULT_RESULT;
			};

			closeCallback = () -> {
				DisplayContext dc = DefaultDisplayContext.getDisplayContext();
				TLSessionContext activeSessionContext = dc.getSessionContext();
				if (activeSessionContext != sessionContext) {
					/* A new Session was created! This happens when the user logged in. A new
					 * MainLayout will be installed. Store a callback at the session to execute when
					 * running this login hook with the new MainLayout. */
					activeSessionContext.set(BOOKMARK, displayBookmark);
				} else {
					displayBookmark.executeCommand(dc);
				}
				callback.run();
			};
		} else {
			closeCallback = callback;
		}
		
		LoginViewDialog dialog = new LoginViewDialog();
		LoginHooks.runOnClose(dialog.getDialogModel(), closeCallback);
		dialog.open(context);
		
	}

	private SubsessionHandler findSubSessionHandler(DisplayContext ctx) {
		return (SubsessionHandler) ctx.getLayoutContext();
	}

}

