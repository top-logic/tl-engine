/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
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

		// Create in-memory Chat model
		final Chat chat = new Chat();
		chat.addMessage(new ChatMessage("System", "Welcome to TopLogic Chat Demo!"));

		// Get or create task tracker from WindowScope
		final TaskProgressTracker taskTracker = TaskProgressTracker.getInstance(
			component.getMainLayout().getWindowScope());

		// Create hidden field with custom ControlProvider that embeds ChatControl
		HiddenField chatField = FormFactory.newHiddenField("chat");
		chatField.setControlProvider(new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				return new FragmentControl(new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						new ChatControl(chat, taskTracker).write(context, out);
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

}
