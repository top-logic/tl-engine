/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.util.TLContextManager;

/**
 * Declarative {@code <page-selection/>} element: tells the chat which object this page has
 * selected.
 *
 * <p>
 * Put it into the view of a page. It shows nothing. While the page is on screen, it writes the
 * selection of the page into a shell channel that {@code <chat-channels>} names. The conversation
 * states that channel with {@code <selection>}. When the page leaves the screen, the element removes
 * what it wrote.
 * </p>
 *
 * <pre>
 * &lt;page-selection to="pageSelection"&gt;
 *   &lt;inputs&gt;
 *     &lt;input channel="selectedSection"/&gt;
 *     &lt;input channel="document"/&gt;
 *   &lt;/inputs&gt;
 * &lt;/page-selection&gt;
 * </pre>
 *
 * <p>
 * It works where a {@code <bind>} on the page's {@code <view-ref>} cannot: in a tile-stack frame and
 * in a page that a control builds itself. See {@link ChatChannels}.
 * </p>
 */
public class PageSelectionElement implements UIElement {

	/** Configuration for {@link PageSelectionElement}. */
	@TagName("page-selection")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getTo()}. */
		String TO = "to";

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/**
		 * The element implementation.
		 *
		 * Hidden in the layout editor: the tag already fixes it.
		 */
		@Override
		@Hidden
		@ClassDefault(PageSelectionElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The shell channel that gets the selection of this page.
		 *
		 * <p>
		 * One of the names in {@code <chat-channels channels="...">} of the shell. It is not a channel
		 * of this view.
		 * </p>
		 */
		@Name(TO)
		@Label("Shell channel")
		@Mandatory
		String getTo();

		/**
		 * The channels of this page that hold its selection, the most specific first.
		 *
		 * <p>
		 * The first channel that holds one object gives the value, for example the section before
		 * the document that contains it. A channel that holds a set of exactly one object gives that
		 * object. A channel that holds nothing or several objects is skipped. When no channel holds
		 * one object, the page has no selection.
		 * </p>
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();
	}

	private final Config _config;

	/** Creates a {@link PageSelectionElement} from configuration. */
	@CalledByReflection
	public PageSelectionElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new PageSelectionControl(context, TLContextManager.getSubSession(), _config.getTo(),
			_config.getInputs().stream().map(context::resolveChannel).toList());
	}

}
