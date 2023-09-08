/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Collections;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;

/**
 * The class {@link ControlViewAdapter} is an adapter to use arbitrary {@link View views} as
 * {@link Control controls}. This {@link Control} simply writes the {@link View} it based on.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ControlViewAdapter extends AbstractConstantControlBase {

	private final View view;

	public ControlViewAdapter(View view) {
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
		return view.isVisible();
	}

}
