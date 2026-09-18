/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * Declarative {@code <chat-progress/>} element: what the agent is working on, as a rail section.
 *
 * <p>
 * Lists the steps of the current (or last) run from the agent's own task store ({@code createTask}
 * / {@code updateTask}), the one in progress by its active form. Open approvals are not shown here:
 * they are cards above the prompt field, where the user answers them. The optional {@code badge}
 * channel receives how many steps still want something.
 * </p>
 */
public class ChatProgressElement extends ChatSectionElement {

	/** Configuration for {@link ChatProgressElement}. */
	@TagName("chat-progress")
	public interface Config extends ChatSectionElement.Config {

		/**
		 * The element implementation.
		 *
		 * Hidden in the layout editor: the tag already fixes it, and changing it gives an element
		 * that no longer matches the options shown beside it.
		 */
		@Override
		@Hidden
		@ClassDefault(ChatProgressElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Registered React element that renders this section.
		 *
		 * Defaults to {@code TLChatProgress}. Hidden in the layout editor: the value must match a
		 * registration in {@code TypeScript}.
		 */
		@Override
		@Name(REACT_ELEMENT)
		@Label("React element")
		@Hidden
		@StringDefault("TLChatProgress")
		String getReactElement();
	}

	private final Config _config;

	/** Creates a {@link ChatProgressElement}. */
	@CalledByReflection
	public ChatProgressElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatSectionControl(context, _config, "TLChatProgress");
	}
}
