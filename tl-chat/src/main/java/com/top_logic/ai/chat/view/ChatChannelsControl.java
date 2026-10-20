/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Server side of {@link ChatChannelsElement}: keeps the shell's channels in the window's
 * {@link ChatChannels} register for as long as the shell exists.
 *
 * <p>
 * A control that shows nothing. It exists because a control lives exactly as long as its view.
 * </p>
 */
public class ChatChannelsControl extends ReactControl {

	/**
	 * The client module that draws nothing.
	 *
	 * @see "tl-chat/ui/src/main.tsx"
	 */
	static final String INVISIBLE = "TLChatInvisible";

	/** Creates a {@link ChatChannelsControl}. */
	public ChatChannelsControl(ReactContext context) {
		super(context, null, INVISIBLE);
	}

	/**
	 * Puts a channel into the register of the given window, until this control is disposed.
	 *
	 * @param window
	 *        The sub-session of the browser window. <code>null</code> does nothing.
	 */
	void publish(TypedAnnotatable window, String name, ViewChannel channel) {
		if (window == null) {
			return;
		}
		ChatChannels.publish(window, name, channel);
		addCleanupAction(() -> ChatChannels.withdraw(window, name, channel));
	}

	/** Structural: an invisible helper, left out of the headless projection. */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}

}
