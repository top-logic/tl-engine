/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.border;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.Boxes;
import com.top_logic.layout.form.boxes.model.FragmentBox;

/**
 * {@link Box} rendering a HTML <code>hr</code> tag as content.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class HorizontalHRBorder {

	private static final HTMLFragment BORDER_CONTENTS = Fragments.hr();

	/**
	 * Creates a {@link HorizontalHRBorder}.
	 */
	public static FragmentBox createHorizontalHRBorder() {
		return Boxes.contentBox(BORDER_CONTENTS);
	}

	private HorizontalHRBorder() {
		super();
	}

}