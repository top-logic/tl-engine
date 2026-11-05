/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.selection;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Arguments of a command setting the selection of a control to the objects with the given
 * {@link ModelName identities} - the options of a dropdown, the rows of a table, the nodes of a
 * tree.
 *
 * <p>
 * The keys are the identities the headless projection puts on the selectable parts, so an agent
 * copies them verbatim into the command, and a recorded selection resolves in a fresh session. The
 * {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans - each {@link ModelName} renders through its own
 * label, i.e. by the object's readable identity.
 * </p>
 */
@Label("Select {keys} in '{target}'")
public interface SelectByKeysArguments extends ReactCommand {

	/** @see #getKeys() */
	String KEYS = "keys";

	/**
	 * The business identities of the objects to select (each the {@code key} projected onto its
	 * option, row or node).
	 */
	@Name(KEYS)
	List<ModelName> getKeys();

}
