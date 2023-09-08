/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.config.WithPlainContextMenuFactory;
import com.top_logic.layout.structure.ContentControl;
import com.top_logic.layout.structure.DecoratingLayoutControlProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.MediaQueryControl;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DecoratingLayoutControlProvider} creating a {@link LayoutControl} using a
 * {@link DirectFormDisplay}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DirectFormControlProvider<C extends DirectFormControlProvider.Config<?>>
		extends DecoratingLayoutControlProvider<C> {

	/**
	 * Configuration of the {@link DirectFormControlProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends DirectFormControlProvider<?>>
			extends PolymorphicConfiguration<I>, WithPlainContextMenuFactory, DirectFormDisplay.Config {

		// Pure sum interface.
	}

	private final ContextMenuFactory _contextMenu;

	private final DirectFormDisplay _formDisplay;

	/**
	 * Creates a new {@link DirectFormControlProvider}.
	 */
	public DirectFormControlProvider(InstantiationContext context, C config) {
		super(context, config);
		_formDisplay = new DirectFormDisplay(config);
		_contextMenu = context.getInstance(config.getContextMenuFactory());
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		ContentControl contentControl =
			ContentControl.create(component, _contextMenu.createContextMenuProvider(component));
		contentControl.setConstraint(DefaultLayoutData.NO_SCROLL_CONSTRAINT);
		contentControl.setView(new FormDisplayContentView(component, _formDisplay));

		MediaQueryControl mediaQueryControl = new MediaQueryControl(contentControl);

		return mediaQueryControl;
	}

}

