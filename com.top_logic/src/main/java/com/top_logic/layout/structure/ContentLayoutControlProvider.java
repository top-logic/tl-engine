/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.config.WithPlainContextMenuFactory;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Base class for {@link LayoutControlProvider} implementations that provide a display for leaf
 * components.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ContentLayoutControlProvider<C extends ContentLayoutControlProvider.Config<?>>
		extends DecoratingLayoutControlProvider<C> {

	/**
	 * Configuration options for {@link ContentLayoutControlProvider}.
	 */
	public interface Config<I extends ContentLayoutControlProvider<?>>
			extends PolymorphicConfiguration<I>, WithPlainContextMenuFactory {

		/**
		 * An additional custom CSS class to set in the layout control tag.
		 * 
		 * @see ControlRepresentableCP.Config#getCssClass()
		 */
		@Nullable
		String getCssClass();

	}

	private ContextMenuFactory _contextMenu;

	/**
	 * Creates a {@link ContentLayoutControlProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ContentLayoutControlProvider(InstantiationContext context, C config) {
		super(context, config);
		_contextMenu = context.getInstance(config.getContextMenuFactory());
	}

	@Override
	public Control mkLayout(Strategy strategy, LayoutComponent component) {
		ContentControl contentControl =
			ContentControl.create(component, _contextMenu.createContextMenuProvider(component));
		contentControl.setConstraint(DefaultLayoutData.NO_SCROLL_CONSTRAINT);
		contentControl.setView(createView(component));
		contentControl.setCssClass(getConfig().getCssClass());
		return contentControl;
	}

	/**
	 * Creates the fragment actually displaying the given component.
	 * 
	 * @param component
	 *        The leaf component to render.
	 * @return The fragment displaying the given component.
	 */
	protected abstract HTMLFragment createView(LayoutComponent component);

}
