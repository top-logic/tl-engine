/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.CommandListener;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link DummyCommandListener} is for test use only.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DummyCommandListener extends DummyNotifyListener implements CommandListener {

	private final String		id;
	protected final Collection	commands;

	public DummyCommandListener(String id, Collection commands) {
		this.id = id;
		this.commands = commands == null ? Collections.EMPTY_SET : commands;
	}

	/**
	 * Returns {@link HandlerResult#DEFAULT_RESULT} if <code>commandName</code>
	 * was registered. Otherwise it throws an {@link IllegalArgumentException}.
	 */
	@Override
	public HandlerResult executeCommand(DisplayContext context, String commandName, Map<String, Object> arguments) {
		if (commands.contains(commandName)) {
			return HandlerResult.DEFAULT_RESULT;
		} else {
			throw new IllegalArgumentException("Command '" + commandName + "' not registered.");
		}
	}

	@Override
	public String getID() {
		return id;
	}

}
