/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.io.IOException;

import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
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
import com.top_logic.util.TLContextManager;

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

		// Bind this window's sub-session to the tracker so the JSON API (ChatServlet) can resolve
		// the same tracker - and the originating sub-session - from the client-provided window id.
		registerTrackerWithSubSession(taskTracker);

		// Create hidden field with custom ControlProvider that embeds ChatControl.
		// ChatControl gets its Chat from taskTracker.getChat() which delegates to
		// ChatSessionManager, so it always renders the active session's entries.
		HiddenField chatField = FormFactory.newHiddenField("chat");
		chatField.setControlProvider(new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				return new FragmentControl(new HTMLFragment() {
					@Override
					public void write(DisplayContext displayContext, TagWriter out) throws IOException {
						new ChatControl(taskTracker.getChat(), taskTracker).write(displayContext, out);
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
				String loginName = context.getPerson().getName();
				taskTracker.setUserName(loginName);

				String firstName = resolveFirstName(context.getPerson());
				taskTracker.setWelcomeName(firstName);
			}
		} catch (Exception e) {
			Logger.debug("Could not resolve user name from TLContext", e, ChatComponentBuilder.class);
		}
	}

	/**
	 * Extracts the first name from a Person for the welcome greeting.
	 *
	 * <p>
	 * Tries {@code Person.getFullName()} first and takes the first word. Falls back to
	 * {@code Person.getName()} (login name).
	 * </p>
	 */
	private static String resolveFirstName(com.top_logic.knowledge.wrap.person.Person person) {
		try {
			String fullName = person.getFullName();
			if (fullName != null && !fullName.isBlank()) {
				String trimmed = fullName.trim();
				int space = trimmed.indexOf(' ');
				return space > 0 ? trimmed.substring(0, space) : trimmed;
			}
		} catch (Exception ignored) {
			// getFullName() may not be available on all Person implementations
		}
		return person.getName();
	}

	/**
	 * Stores the tracker on the current sub-session and records its window id, so the JSON API
	 * ({@code ChatServlet}) can resolve this exact tracker from the client-provided window id.
	 *
	 * <p>
	 * Runs while the chat component is rendered, where the application window's sub-session is bound
	 * to the current thread (see {@link ChatSessionRegistry}).
	 * </p>
	 */
	private static void registerTrackerWithSubSession(TaskProgressTracker taskTracker) {
		try {
			TLSubSessionContext subSession = TLContextManager.getSubSession();
			if (subSession == null) {
				return;
			}
			TLSessionContext session = subSession.getSessionContext();
			taskTracker.setWindowId(ChatSessionRegistry.windowId(session, subSession));
			ChatSessionRegistry.register(subSession, taskTracker);
		} catch (Exception e) {
			Logger.debug("Could not bind chat tracker to sub-session (JSON API will not resolve this window)", e,
				ChatComponentBuilder.class);
		}
	}

}
