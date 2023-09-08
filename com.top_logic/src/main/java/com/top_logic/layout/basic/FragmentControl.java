/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Collections;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;

/**
 * Constant {@link Control} that displays a {@link HTMLFragment}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FragmentControl extends AbstractConstantControlBase {

	private final HTMLFragment view;

	/**
	 * Creates a {@link FragmentControl}.
	 * 
	 * @param view
	 *        The {@link HTMLFragment} to display.
	 */
	public FragmentControl(HTMLFragment view) {
		super(Collections.<String, ControlCommand>emptyMap());
		this.view = view;
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		view.write(context, out);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected void internalRequestRepaint() {
		// Cannot repaint, since no ID was written to the UI.
	}

}
