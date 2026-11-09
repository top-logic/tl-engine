/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Arguments of a command addressing the part of a control that displays the business object with
 * the given {@link ModelName identity} - a row, a tree node, a card.
 *
 * <p>
 * The key is the {@link ScriptingModelKey business identity} the headless projection puts on that
 * part, so an agent copies it verbatim into the command, and a recorded step resolves in a fresh
 * session. A control declares a sub-interface per command whose {@link com.top_logic.basic.config.annotation.Label}
 * names what the command does - the label doubles as the
 * {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider} template that renders a recorded
 * step for humans, the {@link ModelName} rendering through its own label, i.e. by the object's
 * readable identity.
 * </p>
 */
public interface ModelKeyArguments extends ReactCommand {

	/** @see #getKey() */
	String KEY = "key";

	/**
	 * The business identity of the object the command addresses.
	 */
	@Name(KEY)
	@Mandatory
	ModelName getKey();

	/** @see #getKey() */
	void setKey(ModelName value);

}
