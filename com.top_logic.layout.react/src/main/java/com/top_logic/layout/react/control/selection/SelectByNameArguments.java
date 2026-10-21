/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.selection;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Arguments of a command selecting the item displaying the object with the given
 * {@link ModelName identity}.
 *
 * <p>
 * This is the form an item selection is recorded in by an {@link ItemSelection}, so that it
 * survives a fresh session. A control declares a
 * sub-interface whose {@link Label} names what it displays - the label doubles as the
 * {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider} template that renders a recorded
 * step for humans, the {@link ModelName} rendering through its own label, i.e. by the object's
 * readable identity.
 * </p>
 */
@Label("Select {key}")
public interface SelectByNameArguments extends SelectionModifiers {

	/** @see #getKey() */
	String KEY = "key";

	/**
	 * The business identity of the object whose item is selected.
	 */
	@Name(KEY)
	@Mandatory
	ModelName getKey();

	/** @see #getKey() */
	void setKey(ModelName value);

}
