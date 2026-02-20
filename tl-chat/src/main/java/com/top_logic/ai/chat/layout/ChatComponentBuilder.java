/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

import jakarta.servlet.http.HttpSession;

/**
 * {@link ModelBuilder} that creates a {@link FormContext} with an embedded {@link ChatControl}.
 *
 */
public class ChatComponentBuilder implements ModelBuilder {

	/**
	 * Singleton instance.
	 */
	public static final ChatComponentBuilder INSTANCE = new ChatComponentBuilder();

	private ChatComponentBuilder() {
		// Singleton
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		FormContext context = new FormContext(component);

		final TaskProgressTracker taskTracker = TaskProgressTracker.getInstance(
			component.getMainLayout().getWindowScope());

		// Store user name from TLContext (available here) so ChatServlet can use it as fallback
		storeUserName(taskTracker);

		// So the JSON API (ChatServlet) uses the same tracker as this UI when in the same session
		registerTrackerWithSession(component, taskTracker);

		// Create hidden field with custom ControlProvider that embeds ChatControl.
		// ChatControl gets its Chat from taskTracker.getChat() which delegates to
		// ChatSessionManager, so it always renders the active session's entries.
		HiddenField chatField = FormFactory.newHiddenField("chat");
		chatField.setControlProvider(new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				return new FragmentControl(new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						new ChatControl(taskTracker.getChat(), taskTracker).write(context, out);
					}
				});
			}
		});

		context.addMember(chatField);

		return context;
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return true;
	}

	/**
	 * Stores the current user's display name on the tracker. Called in TL framework context where
	 * {@link TLContext} is available, so that {@code ChatServlet} (which may not have TLContext)
	 * can fall back to this stored name.
	 */
	private static void storeUserName(TaskProgressTracker taskTracker) {
		try {
			TLContext context = TLContext.getContext();
			if (context != null && context.getPerson() != null) {
				taskTracker.setUserName(context.getPerson().getName());
			}
		} catch (Exception e) {
			Logger.debug("Could not resolve user name from TLContext", e, ChatComponentBuilder.class);
		}
	}

	/**
	 * Registers the given tracker with the current HTTP session when possible, so
	 * {@link ChatSessionRegistry#getOrCreate(jakarta.servlet.http.HttpSession)} returns this
	 * tracker for the JSON API (same state as the UI).
	 */
	private static void registerTrackerWithSession(LayoutComponent component, TaskProgressTracker taskTracker) {
		try {
			Object scope = component.getMainLayout().getWindowScope();
			Object request = scope.getClass().getMethod("getRequest").invoke(scope);
			if (request != null && request.getClass().getName().startsWith("jakarta.servlet.")) {
				Object session = request.getClass().getMethod("getSession", boolean.class).invoke(request, Boolean.FALSE);
				if (session != null) {
					ChatSessionRegistry.registerSession((HttpSession) session, taskTracker);
				}
			}
		} catch (Exception e) {
			Logger.debug("Could not register chat tracker with session (JSON API may use a separate tracker)", e,
				ChatComponentBuilder.class);
		}
	}

}
