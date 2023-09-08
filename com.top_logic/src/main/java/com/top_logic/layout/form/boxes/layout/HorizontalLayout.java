/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.layout;

/**
 * {@link BoxLayout} that positions contents horizontally.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HorizontalLayout extends LinearLayout {

	/**
	 * Singleton {@link HorizontalLayout} instance.
	 */
	public static final HorizontalLayout INSTANCE = new HorizontalLayout();

	private HorizontalLayout() {
		super(true);
	}

}
