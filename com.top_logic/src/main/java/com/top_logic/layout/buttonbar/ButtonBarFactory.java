/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.buttonbar;

import java.util.List;

import com.top_logic.knowledge.gui.layout.ButtonBar;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlRenderer;

/**
 * Factory to create {@link ButtonBarControl}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ButtonBarFactory {

	/**
	 * The default renderer to use with {@link ButtonBarControl}s.
	 */
	public static final DefaultButtonBarRenderer DEFAULT_RENDERER =
		new DefaultButtonBarRenderer(ButtonBar.DEFAULT_CSS_CLASS);

	/**
	 * Creates a {@link ButtonBarControl} based on a {@link ButtonBar}
	 */
	public static ButtonBarControl createButtonBar(ButtonBarModel buttonBarModel, String masterCssClass) {
		DefaultButtonBarRenderer renderer = new DefaultButtonBarRenderer(masterCssClass);
		return createButtonBar(buttonBarModel, renderer);
	}

	/**
	 * Creates a {@link ButtonBarControl} for the constant list of commands.
	 */
	public static ButtonBarControl createButtonBar(List<CommandModel> buttons) {
		return createButtonBar(new SimpleButtonBarModel(buttons));
	}

	/**
	 * Creates a {@link ButtonBarControl} for the a variable {@link ButtonBarModel}.
	 */
	public static ButtonBarControl createButtonBar(ButtonBarModel buttonBarModel) {
		return createButtonBar(buttonBarModel, DEFAULT_RENDERER);
	}

	/**
	 * Creates a {@link ButtonBarControl}.
	 * 
	 * @param buttonBarModel
	 *        The buttons to show.
	 * @param renderer
	 *        The renderer for the bar, see {@link #DEFAULT_RENDERER}.
	 */
	public static ButtonBarControl createButtonBar(ButtonBarModel buttonBarModel,
			ControlRenderer<? super ButtonBarControl> renderer) {
		ButtonBarControl buttonBarControl = new ButtonBarControl(buttonBarModel);
		buttonBarControl.setRenderer(renderer);
		return buttonBarControl;
	}

}
