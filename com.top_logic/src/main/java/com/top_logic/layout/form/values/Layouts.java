/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.FlowLayoutControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutData;

/**
 * Factory for {@link LayoutControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Layouts {

	public static void layoutAdapter(FlowLayoutControl parent, HTMLFragment control) {
		layoutAdapter(parent, control, DefaultLayoutData.DEFAULT_CONSTRAINT);
	}

	public static void layoutAdapter(FlowLayoutControl parent, HTMLFragment control, LayoutData constraint) {
		LayoutControlAdapter pane = new LayoutControlAdapter(control);
		pane.setConstraint(constraint);
		parent.addChild(pane);
	}

}
