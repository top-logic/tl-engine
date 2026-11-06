/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.ai.chat.view.ChatConversationElement.AgentCommand;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.util.error.TopLogicException;

/**
 * A command of a displayed page that an agent run in its window may run, as an
 * {@link AgentCommand} of a {@code <chat-conversation>} lists it.
 *
 * <p>
 * Built by {@link ChatConversationControl} in the view context of the page and offered on the
 * {@link ChatViewBinding} while that control is displayed. The agent thread knows a name and
 * nothing else; everything the command needs to run - its channels, its rules, the window's dialogs
 * - comes from the context it was built in.
 * </p>
 */
public final class PageCommand {

	private final String _name;

	private final String _description;

	private final ViewCommandModel _model;

	private final ViewContext _context;

	/**
	 * Creates a {@link PageCommand}.
	 *
	 * @param name
	 *        The name the model runs the command by.
	 * @param description
	 *        What the command does, for the model.
	 * @param model
	 *        The command, with its inputs, executability rules and access checks.
	 * @param context
	 *        The view context the command was built in and runs in.
	 */
	public PageCommand(String name, String description, ViewCommandModel model, ViewContext context) {
		_name = name;
		_description = description;
		_model = model;
		_context = context;
	}

	/** The name the model runs this command by. */
	public String name() {
		return _name;
	}

	/** What this command does, as the page tells the model. */
	public String description() {
		return _description;
	}

	/**
	 * Runs the command as a click on its button runs it: its rules decide over its inputs as they
	 * are now, and only then its action chain runs.
	 *
	 * <p>
	 * Must be called within the interaction of the window the command is displayed in, see
	 * {@link ChatWindowService#inWindow(com.top_logic.base.context.TLSubSessionContext, Runnable)}.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         The {@link CommandErrors#notExecutable(com.top_logic.tool.execution.ExecutableState)
	 *         refusal} carrying the reason when its rules reject it now, or the failure of the
	 *         command.
	 *
	 * @see ViewCommandModel#perform(com.top_logic.layout.react.ReactContext)
	 */
	public void run() {
		_model.perform(_context);
	}

	@Override
	public String toString() {
		return "PageCommand(" + _name + ")";
	}
}
