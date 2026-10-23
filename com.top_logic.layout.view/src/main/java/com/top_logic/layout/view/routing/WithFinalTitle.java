/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.routing;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.routing.RoutingParticipant;

/**
 * Declaration of a route whose label can be the final title of the page.
 *
 * <p>
 * Mixed into the declarations that give a route a label - a {@code <nav-item>} of a sidebar, a
 * {@code <tab>} of a tab bar, a {@code <frame>} of a tile stack, a {@code <bind>} of a view's
 * parameter bindings.
 * </p>
 *
 * @see RoutingParticipant#titlesContent()
 */
public interface WithFinalTitle extends ConfigurationItem {

	/** Configuration name for {@link #getFinalTitle()}. */
	String FINAL_TITLE = "final-title";

	/**
	 * Whether the label of this route is the final title of the page.
	 *
	 * <p>
	 * The browser tab is titled after the innermost route of the display that has a label. A route
	 * displayed inside the content of this one would therefore rename the page: a tab bar inside a
	 * frame showing a person titles the page "Details" instead of the name of the person. With this
	 * option, the routes inside this one's content keep their URL segments but do not contribute
	 * their labels to the title, which stays the label of this route.
	 * </p>
	 *
	 * <p>
	 * Effective only while this route is part of the URL: a declaration with {@code route="none"},
	 * or a binding whose channel holds no value, names no page.
	 * </p>
	 */
	@Name(FINAL_TITLE)
	boolean getFinalTitle();
}
