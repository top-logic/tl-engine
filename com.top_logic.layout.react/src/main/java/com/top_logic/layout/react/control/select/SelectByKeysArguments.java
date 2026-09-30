/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * The {@link ReactDropdownSelectControl} {@code selectByKey} command: sets the selection to the
 * options with the given {@link ModelName identities}.
 *
 * <p>
 * The keys are the identities the headless projection puts on the options, so an agent copies an
 * option's {@code key} verbatim into this command. The {@link Label} doubles as the
 * {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider} template that renders a recorded
 * step for humans — each {@link ModelName} renders through its own label, i.e. by the option's
 * readable identity.
 * </p>
 */
@Label("Select {keys} in '{target}'")
public interface SelectByKeysArguments extends ReactCommand {

	/** @see #getKeys() */
	String KEYS = "keys";

	/**
	 * The business identities of the options to select (each the {@code key} projected onto its
	 * option).
	 */
	@Name(KEYS)
	List<ModelName> getKeys();

}
