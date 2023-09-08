/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.layout;

/**
 * {@link BoxLayout} that positions contents vertically (one box below the last one).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VerticalLayout extends LinearLayout {

	/**
	 * Singleton {@link VerticalLayout} instance.
	 */
	public static final VerticalLayout INSTANCE = new VerticalLayout();

	private VerticalLayout() {
		super(false);
	}

}
