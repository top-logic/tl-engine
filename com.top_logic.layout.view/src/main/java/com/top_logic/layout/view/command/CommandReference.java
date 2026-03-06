/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactDisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that delegates to a named command from the enclosing {@link CommandScope}.
 *
 * <p>
 * Used in {@code <button>} elements to reference a command declared on the enclosing panel:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *     &lt;command-ref name="approve"/&gt;
 * &lt;/button&gt;
 * </pre>
 */
public class CommandReference implements ViewCommand {

	/**
	 * Configuration for {@link CommandReference}.
	 */
	@TagName("command-ref")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(CommandReference.class)
		Class<? extends ViewCommand> getImplementationClass();

		@Override
		@Name(NAME)
		@Mandatory
		String getName();
	}

	private final String _name;

	/**
	 * Creates a new {@link CommandReference} from configuration.
	 */
	@CalledByReflection
	public CommandReference(InstantiationContext context, Config config) {
		_name = config.getName();
	}

	/**
	 * The referenced command name.
	 */
	public String getReferenceName() {
		return _name;
	}

	@Override
	public HandlerResult execute(ReactDisplayContext context, Object input) {
		throw new UnsupportedOperationException(
			"CommandReference must be resolved to the actual command before execution.");
	}
}
