/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;

/**
 * Whether the content of an element keeps a distance from the border of its container.
 *
 * <p>
 * Containers are flush: their content reaches up to their border. Content that flows - texts,
 * stacks, grids, alerts, buttons, a form - sets {@link #getInset()} where it would otherwise glue
 * to that border. Content that fills - a table, a diagram, a split - leaves it unset and stays
 * edge to edge.
 * </p>
 */
public interface InsetOptions extends ConfigurationItem {

	/** Configuration name for {@link #getInset()}. */
	String INSET = "with-inset";

	/**
	 * Whether the content is inset from the container border.
	 *
	 * <p>
	 * Inset content keeps the page inset as distance to the border of its container, exactly as an
	 * {@link InsetElement} around it does. Without it, the content reaches up to the border, which
	 * is what content wants that fills its container, or that stands in a container keeping a
	 * distance of its own.
	 * </p>
	 *
	 * <p>
	 * Content is inset once: an element setting this option does not hold further content that
	 * sets it, too, or that stands in an {@link InsetElement}.
	 * </p>
	 */
	@Name(INSET)
	@BooleanDefault(false)
	boolean getInset();

	/**
	 * The control displaying the given content, inset from the container border if the
	 * configuration asks for it.
	 *
	 * @param context
	 *        The context to create the inset in.
	 * @param options
	 *        The configuration of the element displaying the content.
	 * @param content
	 *        The control displaying the content.
	 * @return The given content, or a {@link ReactInsetControl} holding it if
	 *         {@link #getInset()} is set.
	 */
	static ReactControl insetIfRequested(ReactContext context, InsetOptions options, ReactControl content) {
		if (!options.getInset()) {
			return content;
		}
		return new ReactInsetControl(context, content);
	}

}
