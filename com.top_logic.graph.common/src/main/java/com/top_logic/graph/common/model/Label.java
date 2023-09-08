/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

import com.top_logic.graph.common.model.layout.LabelLayout;

/**
 * A label attached to some {@link LabelOwner}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Label extends GraphPart {

	/**
	 * @see #getOwner()
	 */
	String OWNER = "owner";

	/**
	 * @see #getText()
	 */
	String TEXT = "text";

	/**
	 * @see #getLayout()
	 */
	String LAYOUT = "layout";

	/**
	 * The labeled instance.
	 */
	LabelOwner getOwner();

	/**
	 * Initializer for {@link #getOwner()}.
	 * <p>
	 * Has to be called exactly once.
	 * </p>
	 */
	void initOwner(LabelOwner owner);

	/**
	 * The text to display on the {@link #getOwner()}.
	 */
	String getText();

	/**
	 * @see #getText()
	 */
	void setText(String value);

	/**
	 * Layout algorithm to use for displaying this {@link Label}.
	 */
	LabelLayout getLayout();

	/**
	 * @see #getLayout()
	 */
	void setLayout(LabelLayout value);

}
