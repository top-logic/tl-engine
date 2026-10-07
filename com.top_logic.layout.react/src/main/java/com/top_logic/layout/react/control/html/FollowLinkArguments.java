/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.html;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactHtmlControl} command that follows a link of the displayed
 * content: the target the content names in the {@link ReactHtmlControl#LINK_ATTRIBUTE} of the link
 * the user clicked.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Follow link '{link}'")
public interface FollowLinkArguments extends ReactCommand {

	/** @see #getLink() */
	String LINK = "link";

	/**
	 * The link target, as the content names it.
	 */
	@Name(LINK)
	@Mandatory
	String getLink();

}
