/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
 * Declarative {@code <chat-sources/>} element: what this conversation has cited, as a rail section.
 *
 * <p>
 * One row per source, in the order the answers first cited them, with the marker numbers that point
 * at it - a number scrolls to its answer, the row opens the source. A long list gets a filter. The
 * optional {@code badge} channel receives how many sources there are, so the rail's badge counts
 * the ones cited since the user last looked.
 * </p>
 */
public class ChatSourcesElement extends ChatSectionElement {

	/** Configuration for {@link ChatSourcesElement}. */
	@TagName("chat-sources")
	public interface Config extends ChatSectionElement.Config {

		/**
		 * The element implementation.
		 *
		 * Hidden in the layout editor: the tag already fixes it, and changing it gives an element
		 * that no longer matches the options shown beside it.
		 */
		@Override
		@Hidden
		@ClassDefault(ChatSourcesElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Registered React element that renders this section.
		 *
		 * Defaults to {@code TLChatSources}. Hidden in the layout editor: the value must match a
		 * registration in {@code TypeScript}.
		 */
		@Override
		@Name(REACT_ELEMENT)
		@Label("React element")
		@Hidden
		@StringDefault("TLChatSources")
		String getReactElement();
	}

	private final Config _config;

	/** Creates a {@link ChatSourcesElement}. */
	@CalledByReflection
	public ChatSourcesElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new ChatSectionControl(context, _config, "chat-sources", "TLChatSources");
	}
}
