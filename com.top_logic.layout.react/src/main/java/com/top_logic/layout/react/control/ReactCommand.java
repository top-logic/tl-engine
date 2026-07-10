/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;

/**
 * A command invocation on an addressed control, as a typed configuration item.
 *
 * <p>
 * A command handler declares a subtype of this interface as its argument parameter instead of a raw
 * {@code Map<String, Object>}. The client JSON arguments bind into the typed instance (via the same
 * JSON property names declared by {@link com.top_logic.basic.config.annotation.Name @Name}), and the
 * handler reads typed getters. The one interface is then the single source for the call arguments,
 * the advertised {@link com.top_logic.basic.json.schema.model.Schema JSON schema} (built from the
 * {@link com.top_logic.basic.config.ConfigurationDescriptor descriptor}), and the human-readable
 * rendering of a recorded step (the interface's {@link Label label} is the rendering template).
 * </p>
 *
 * <p>
 * The base properties make the item a complete, self-contained record of the invocation: the
 * {@link #getName() command name} and the {@link #getAddress() semantic address} of the target
 * control. A recorded script is a list of these items; everything derived from a step — its replay
 * dispatch, its wire form, its human-readable description — is computed from the item, nothing is
 * stored beside it.
 * </p>
 *
 * @implNote Dispatch still goes through
 *           {@link ReactControl#executeCommand(String, java.util.Map)} — the typed arguments only
 *           change the shape the handler receives, not the behavior path the browser exercises. The
 *           base properties are never sent by the client and are stripped from the advertised
 *           argument schema; they are set when a dispatched command is captured as a recorded step.
 */
@Label("Command '{name}' on '{target}'")
public interface ReactCommand extends ConfigurationItem {

	/** @see #getAddress() */
	String ADDRESS = "address";

	/** @see #getName() */
	String NAME = "name";

	/** @see #getTarget() */
	String TARGET = "target";

	/**
	 * The semantic address of the target control (as
	 * {@link com.top_logic.layout.react.headless.AgentSession#resolve(String)} accepts), or
	 * {@code null} if the target could not be addressed.
	 */
	@Name(ADDRESS)
	@Nullable
	String getAddress();

	/** @see #getAddress() */
	void setAddress(String value);

	/**
	 * The command identifier dispatched to the target control (a
	 * {@link ReactCommandHandler#value() handler id}, e.g. {@code "selectTab"}).
	 */
	@Name(NAME)
	@Nullable
	String getName();

	/** @see #getName() */
	void setName(String value);

	/**
	 * The semantic name of the addressed control: the last {@code [name]} segment of the
	 * {@link #getAddress() address} — the container-supplied identity (a form field name, a button
	 * label) that the control itself may not know. E.g. {@code …/formField[members]/textInput}
	 * yields {@code members} and {@code …/button[Neu]} yields {@code Neu}. {@code null} if the
	 * address carries no name.
	 *
	 * <p>
	 * Available to {@link Label label} templates, so a command identified by <em>where</em> it sits
	 * rather than by its arguments (a button click, a field edit) renders with its target's name.
	 * </p>
	 */
	@Name(TARGET)
	@Derived(fun = TargetName.class, args = @Ref(ADDRESS))
	String getTarget();

	/**
	 * Computes {@link ReactCommand#getTarget()} from {@link ReactCommand#getAddress()}.
	 */
	class TargetName extends Function1<String, String> {

		@Override
		public String apply(String address) {
			if (address == null) {
				return null;
			}
			int close = address.lastIndexOf(']');
			if (close < 0) {
				return null;
			}
			int open = address.lastIndexOf('[', close);
			return open < 0 ? null : address.substring(open + 1, close);
		}
	}
}
