/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration of an action that processes a single value of the arguments of its action chain.
 *
 * <p>
 * Example: write the result of a script to one channel and the object the command was invoked on
 * to another:
 * </p>
 *
 * <pre>
 * &lt;execute-script function="$ticket.get(`demo.tickets:Ticket#assignee`)"&gt;
 *   &lt;with-result&gt;
 *     &lt;write-channel name="person"/&gt;
 *     &lt;write-channel name="ticket" value="selection"/&gt;
 *   &lt;/with-result&gt;
 * &lt;/execute-script&gt;
 * </pre>
 *
 * @implNote Offered by the configurations of actions processing a single value. The value is
 *           selected by the chain step
 *           {@link ViewActions#instantiate(com.top_logic.basic.config.InstantiationContext, java.util.List, ActionScope)}
 *           wraps the action into, not by the action itself.
 */
public interface WithValueArgument extends ConfigPart {

	/** Configuration name for {@link #getValue()}. */
	String VALUE = "value";

	/**
	 * The name of the argument this action processes.
	 *
	 * <p>
	 * Without a name, the action processes the innermost result of an enclosing action, or the main
	 * argument of what triggered the chain (e.g. the input of a command) outside of follow-up
	 * actions. The name must be one of the arguments defined at the position of the action.
	 * </p>
	 */
	@Name(VALUE)
	@Nullable
	String getValue();

}
