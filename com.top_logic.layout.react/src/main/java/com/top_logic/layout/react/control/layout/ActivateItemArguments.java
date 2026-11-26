/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.identity.DisplayedObjectId;

/**
 * Typed arguments of the {@link ReactLayoutControl#CMD_ACTIVATE_ITEM} command: the item of an
 * activatable layout container the user opened.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans. The item is given by the client key the
 * container displays it under, which a recorded step carries as the business key of the item's
 * object, see {@link DisplayedObjectId}.
 * </p>
 */
@Label("Activate item {key}")
public interface ActivateItemArguments extends ReactCommand {

	/** @see #getKey() */
	String KEY = "key";

	/**
	 * The key of the activated item, as the container published it in its item keys.
	 */
	@Name(KEY)
	@Mandatory
	@DisplayedObjectId
	String getKey();

}
