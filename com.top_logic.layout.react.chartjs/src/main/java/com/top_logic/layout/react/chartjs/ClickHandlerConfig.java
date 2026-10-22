/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ViewCommand;

/**
 * Configuration for a named click handler that executes a {@link ViewCommand}.
 *
 * <p>
 * The {@link #getName() name} is referenced from TL-Script dataset output via
 * {@code onClick} / {@code onLegendClick} keys to connect chart interactions
 * with server-side commands.
 * </p>
 */
public interface ClickHandlerConfig extends ConfigurationItem, ActionScopeDefinition {

	/** Access to the default methods of this interface for the configuration proxy. */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getAction()}. */
	String ACTION = "action";

	/** Configuration name for {@link #getAs()}. */
	String AS = "as";

	/** Name of the clicked element's object when no other name is given. */
	String DEFAULT_AS = "element";

	/**
	 * The handler name, referenced from TL-Script dataset {@code onClick} / {@code onLegendClick}.
	 */
	@Mandatory
	@Name(NAME)
	String getName();

	/**
	 * The command to execute when the click occurs.
	 *
	 * <p>
	 * Its main argument is the metadata object of the clicked element, named by
	 * {@link #getAs() as}.
	 * </p>
	 */
	@Mandatory
	@Name(ACTION)
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends ViewCommand> getAction();

	/**
	 * The name of the argument holding the metadata object of the clicked element.
	 */
	@Name(AS)
	@StringDefault(DEFAULT_AS)
	String getAs();

	/**
	 * The {@link #getAction() command} runs with the clicked element's object as argument named by
	 * {@link #getAs()}.
	 */
	@Override
	default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
		if (part != null && part == getAction()) {
			return ActionScope.single(getAs());
		}
		return outer;
	}
}
