/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.CommaSeparatedChannelRefs;
import com.top_logic.util.TLContextManager;

/**
 * Declarative {@code <chat-channels/>} element: makes channels of the shell reachable from every
 * page of the browser window.
 *
 * <p>
 * Write it once, in the shell view, next to the {@code <chat-dock>}. It shows nothing. A
 * {@code <page-selection>} on a page writes into a channel named here, also from a tile-stack frame,
 * which a {@code <bind>} cannot reach. See {@link ChatChannels} for why.
 * </p>
 *
 * <pre>
 * &lt;channels&gt;&lt;channel name="pageSelection"/&gt;&lt;/channels&gt;
 * ...
 * &lt;chat-channels channels="pageSelection"/&gt;
 * &lt;chat-dock ...&gt; ... &lt;selection channel="pageSelection"/&gt; ... &lt;/chat-dock&gt;
 * </pre>
 */
public class ChatChannelsElement implements UIElement {

	/** Configuration for {@link ChatChannelsElement}. */
	@TagName("chat-channels")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getChannels()}. */
		String CHANNELS = "channels";

		/**
		 * The element implementation.
		 *
		 * Hidden in the layout editor: the tag already fixes it.
		 */
		@Override
		@Hidden
		@ClassDefault(ChatChannelsElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channels of this view that the pages of the browser window can write, comma-separated.
		 *
		 * <p>
		 * Each channel is reachable under its own name. A {@code <page-selection to="...">} names one
		 * of them. Do not name a channel that tables of the pages share through {@code <bind>}: a
		 * page writes its selection into the channel, and the tables would get it as their selection.
		 * </p>
		 */
		@Name(CHANNELS)
		@Format(CommaSeparatedChannelRefs.class)
		List<ChannelRef> getChannels();
	}

	private final List<ChannelRef> _channels;

	/** Creates a {@link ChatChannelsElement} from configuration. */
	@CalledByReflection
	public ChatChannelsElement(InstantiationContext context, Config config) {
		_channels = config.getChannels();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ChatChannelsControl control = new ChatChannelsControl(context);
		for (ChannelRef ref : _channels) {
			control.publish(TLContextManager.getSubSession(), ref.getChannelName(), context.resolveChannel(ref));
		}
		return control;
	}

}
