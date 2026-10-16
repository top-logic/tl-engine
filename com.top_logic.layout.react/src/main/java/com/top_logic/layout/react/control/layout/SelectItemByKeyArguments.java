/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.react.control.selection.SelectByNameArguments;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Typed arguments of the {@link ReactLayoutControl#CMD_SELECT_ITEM_BY_KEY} command: selects the
 * item of a selectable layout container displaying the object with the given {@link ModelName
 * identity}.
 *
 * <p>
 * This is the form an item selection is recorded in, so that it survives a fresh session. The
 * {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans - the {@link ModelName} renders through its own
 * label, i.e. by the object's readable identity.
 * </p>
 */
@Label("Select item {key}")
public interface SelectItemByKeyArguments extends SelectByNameArguments {

	// The item is named by the inherited key.

}
