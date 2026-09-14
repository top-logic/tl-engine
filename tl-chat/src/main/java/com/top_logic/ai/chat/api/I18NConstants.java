/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * I18N constants of the chat's REST API.
 *
 * <p>
 * The agent lines below are the human-facing half of an agent definition. An agent's
 * {@code description} is written for a model that has to decide whether to delegate to it - it
 * names arguments and forbids pasting the user's message - and putting that in front of a person
 * choosing whom to talk to is how the picker came to show a wall of English instructions. These
 * say, in the user's language, what the agent is for. An agent without one is offered by name
 * alone.
 * </p>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * The all-round assistant, described for the agent picker.
	 *
	 * @en Knows its way around the whole application: explores and explains the model, changes it,
	 *     runs TL-Script, and writes pages.
	 */
	public static ResKey AGENT_TL_SINGLE_AGENT;

	/**
	 * The coordinator, described for the agent picker.
	 *
	 * @en Splits a larger request between the specialists and puts their results back together.
	 */
	public static ResKey AGENT_TL_ORCHESTRATOR;

	/**
	 * The model explorer, described for the agent picker.
	 *
	 * @en Answers questions about the application's model and data without changing anything.
	 */
	public static ResKey AGENT_TL_MODEL_EXPLORER;

	/**
	 * The TL-Script agent, described for the agent picker.
	 *
	 * @en Writes and runs TL-Script to query or compute over the application's data.
	 */
	public static ResKey AGENT_TL_TL_SCRIPT_AGENT;

	/**
	 * The model editor, described for the agent picker.
	 *
	 * @en Changes the application's model: types, attributes and the views built on them.
	 */
	public static ResKey AGENT_TL_MODEL_EDITOR;

	static {
		initConstants(I18NConstants.class);
	}
}
