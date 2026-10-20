/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.view.channel.CommaSeparatedInputRefs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputRefFormat;

/**
 * {@link ExecutabilityConfig} whose rules decide over the value of a single input channel.
 *
 * <p>
 * Used by operations that are not commands, such as dragging and dropping, whose rules decide for
 * an element as a whole. The rules see the value of the {@link #getInput() input} under the name of
 * the input, like the rules of a command see its inputs. Further rule properties of an extending
 * configuration - rules deciding for a single item - declare the value they decide over with
 * {@link com.top_logic.layout.view.channel.Supplies}.
 * </p>
 *
 * @implNote The arguments of the rules are {@link #arguments(InputExecutabilityConfig)}, the scope of
 *           a rule property declaring a supplied value is
 *           {@link ViewExecutabilityRules#scope(ConfigurationItem, String)}.
 */
@Abstract
public interface InputExecutabilityConfig extends ExecutabilityConfig, ConfigPart, ActionScopeDefinition {

	/** Access to the default methods of this interface for the configuration proxy. */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration name for {@link #getInput()}. */
	String INPUT = "input";

	/**
	 * Reference to a channel whose value the {@link #getExecutability() executability} rules
	 * decide over.
	 *
	 * <p>
	 * A script rule sees the value under the name of the channel, or by the name given with
	 * {@value CommaSeparatedInputRefs#AS}: <code>input="selection as ticket"</code>. Without an
	 * input, the rules decide over nothing, a script rule sees the variable
	 * {@value CommandArguments#DEFAULT_ARGUMENT} without value.
	 * </p>
	 */
	@Name(INPUT)
	@Nullable
	@Format(InputRefFormat.class)
	InputRef getInput();

	/**
	 * The {@link #getExecutability() executability} rules decide over the
	 * {@link #arguments(InputExecutabilityConfig) arguments} of the input, the rules of a property
	 * declaring a supplied value over that value.
	 */
	@Override
	default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
		if (ActionScopes.contains(getExecutability(), part)) {
			return arguments(this).scope();
		}
		ActionScope supplied = ViewExecutabilityRules.suppliedScope(this, part);
		if (supplied != null) {
			return supplied;
		}
		return outer;
	}

	/**
	 * The arguments the {@link #getExecutability() executability} rules of the given configuration
	 * decide over: the value of its {@link #getInput() input}, as a command without a trigger
	 * would see it.
	 */
	static CommandArguments arguments(InputExecutabilityConfig config) {
		InputRef input = config.getInput();
		return new CommandArguments(input == null ? List.of() : List.of(input), null);
	}

}
