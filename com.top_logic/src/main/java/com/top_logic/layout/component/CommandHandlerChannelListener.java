/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Objects;

import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ChannelLinking} executing a {@link CommandHandler} with the new channel value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandHandlerChannelListener implements ChannelListener {

	private CommandHandler _command;

	private LayoutComponent _component;

	/**
	 * Creates a {@link CommandHandlerChannelListener}.
	 */
	public CommandHandlerChannelListener(CommandHandler command, LayoutComponent component) {
		_command = command;
		_component = component;
	}

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		_command.handleCommand(DefaultDisplayContext.getDisplayContext(), _component, newValue, CommandHandler.NO_ARGS);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_command, _component);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandHandlerChannelListener other = (CommandHandlerChannelListener) obj;
		return Objects.equals(_command, other._command) && Objects.equals(_component, other._component);
	}

}
