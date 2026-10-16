/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.selection.SelectionModifiers;

/**
 * Typed arguments of the {@link ReactLayoutControl#CMD_SELECT_ITEM} command: the item of a
 * selectable layout container the user clicked.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans. An item selection is recorded as a
 * {@link SelectItemByKeyArguments} naming the item's business object, because the item key is
 * allocated per session.
 * </p>
 */
@Label("Select item {key}")
public interface SelectItemArguments extends SelectionModifiers {

	/** @see #getKey() */
	String KEY = "key";

	/**
	 * The key of the selected item, as the container published it in its item keys.
	 */
	@Name(KEY)
	@Mandatory
	String getKey();

}
