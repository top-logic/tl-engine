/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.create;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Form type with a single name field.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder(value = { NewForm.NAME, NewForm.ICON })
public interface NewForm extends ConfigurationItem {

	/**
	 * @see #getIcon()
	 */
	String ICON = "icon";

	/**
	 * @see #getName()
	 */
	String NAME = "name";

	/**
	 * The name of the new workflow.
	 */
	@Mandatory
	@Name(NAME)
	String getName();

	/**
	 * The icon to use for this workflow.
	 */
	@Name(ICON)
	ThemeImage getIcon();

}
