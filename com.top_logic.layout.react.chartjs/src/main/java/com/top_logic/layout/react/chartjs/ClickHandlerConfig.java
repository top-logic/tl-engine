/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
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
public interface ClickHandlerConfig extends ConfigurationItem {

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getAction()}. */
	String ACTION = "action";

	/**
	 * The handler name, referenced from TL-Script dataset {@code onClick} / {@code onLegendClick}.
	 */
	@Mandatory
	@Name(NAME)
	String getName();

	/**
	 * The command to execute when the click occurs. Receives the metadata TL object as argument.
	 */
	@Mandatory
	@Name(ACTION)
	PolymorphicConfiguration<? extends ViewCommand> getAction();
}
