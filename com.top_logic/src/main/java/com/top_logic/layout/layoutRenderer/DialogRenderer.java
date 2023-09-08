/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControlRenderer;
import com.top_logic.layout.structure.DialogWindowControl;

/**
 * The class {@link DialogRenderer} is used to render the content of a {@link DialogWindowControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DialogRenderer extends AbstractControlRenderer<DialogWindowControl> {

	/**
	 * Singleton {@link DialogRenderer} instance.
	 */
	public static final DialogRenderer INSTANCE = new DialogRenderer();


	private DialogRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, DialogWindowControl control) throws IOException {
		Icons.DIALOG_WINDOW_TEMPLATE.get().write(context, out, control);
	}

}
