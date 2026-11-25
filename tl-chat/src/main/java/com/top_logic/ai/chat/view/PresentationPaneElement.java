/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Declarative {@code <presentation-pane input="…">} element: shows whatever stands on a channel, for
 * any type, and remembers what it showed before.
 *
 * <p>
 * One object is shown as its form, several as a table with the columns of their type, and what the
 * agent's {@code present} tool put on the channel as the table, chart or form it asked for. A row
 * clicked in a table opens that object's form, and "back" returns to the table as it was. Closing
 * the pane writes {@code null} to the channel.
 * </p>
 *
 * <p>
 * Name the channel the {@code <chat-conversation present="…">} writes, so that the agent can show
 * things here. In a {@code <side-rail>}, the pane is the content of a section that is visible while
 * the channel holds something:
 * </p>
 *
 * <pre>
 * &lt;section name="presented" width="420"&gt;
 *   &lt;visibility inputs="paneObject" expr="!isEmpty($paneObject)"/&gt;
 *   &lt;presentation-pane input="paneObject"/&gt;
 * &lt;/section&gt;
 * </pre>
 *
 * @see PresentationPane
 */
public class PresentationPaneElement implements UIElement {

	/** Configuration for {@link PresentationPaneElement}. */
	@TagName("presentation-pane")
	public interface Config extends UIElement.Config {

		/**
		 * Configuration property name for {@link #getInput()}.
		 *
		 * @see #getInput()
		 */
		String INPUT = "input";

		@Override
		@ClassDefault(PresentationPaneElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding what the pane shows.
		 *
		 * <p>
		 * One object is shown as its form, several as a table, and what the agent presented as the
		 * table, chart or form it asked for. The pane's close button writes {@code null} back, its
		 * back button the value it showed before.
		 * </p>
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final Config _config;

	/** Creates a {@link PresentationPaneElement}. */
	@CalledByReflection
	public PresentationPaneElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		PresentationPaneControl control =
			new PresentationPaneControl(context, context.resolveChannel(_config.getInput()));
		control.setCssClass(_config.getCssClass());
		return control;
	}
}
