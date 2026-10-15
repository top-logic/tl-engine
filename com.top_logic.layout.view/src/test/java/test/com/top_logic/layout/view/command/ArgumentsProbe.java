/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ArgumentsViewAction;
import com.top_logic.layout.view.command.Continuation;

/**
 * Configurable test action recording the {@link ActionScope} it is instantiated in and the
 * {@link ActionArguments} it runs with.
 *
 * <p>
 * Resumes the chain with the default argument.
 * </p>
 */
public class ArgumentsProbe extends ArgumentsViewAction {

	/**
	 * Configuration of an {@link ArgumentsProbe}.
	 */
	public interface Config extends PolymorphicConfiguration<ArgumentsProbe> {

		@Override
		@ClassDefault(ArgumentsProbe.class)
		Class<? extends ArgumentsProbe> getImplementationClass();
	}

	/** The scopes of all probes instantiated since the last {@link #reset()}. */
	public static final List<ActionScope> SCOPES = new ArrayList<>();

	/** The arguments of all probe executions since the last {@link #reset()}. */
	public static final List<ActionArguments> SEEN = new ArrayList<>();

	/**
	 * Creates an {@link ArgumentsProbe}, recording the scope of the given context.
	 */
	@CalledByReflection
	public ArgumentsProbe(InstantiationContext context, Config config) {
		SCOPES.add(ActionScope.of(context));
	}

	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		SEEN.add(arguments);
		continuation.resume(arguments.value());
	}

	/**
	 * Forgets all recorded scopes and arguments.
	 */
	public static void reset() {
		SCOPES.clear();
		SEEN.clear();
	}
}
