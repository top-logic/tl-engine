/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;

/**
 * Creates a {@link ButtonControl} which uses an {@link ImageButtonRenderer}.
 * 
 * @author    <a href="mailto:tbe@top-logic.com">Till Bentz</a>
 */
public class ImageButtonControlProvider extends DefaultFormFieldControlProvider {

    public static ImageButtonControlProvider INSTANCE = new ImageButtonControlProvider();

	@Override
	public Control visitCommandField(CommandField member, Void arg) {
		return new ButtonControl(member, ImageButtonRenderer.INSTANCE);
	}
}
