/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.config.WithPlainContextMenuFactory;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;
import com.top_logic.layout.structure.LayoutControlProvider.Strategy;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Base {@link Layouting} for leaf components.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractContentLayouting<C extends AbstractContentLayouting.Config<?>>
		extends AbstractConfiguredInstance<C> implements Layouting {

	private ContextMenuFactory _contextMenu;

	/**
	 * Configuration options for {@link AbstractContentLayouting}.
	 */
	public interface Config<I extends AbstractContentLayouting<?>>
			extends PolymorphicConfiguration<I>, WithPlainContextMenuFactory {
		// Pure sum interface.
	}

	/**
	 * Creates a {@link AbstractContentLayouting} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractContentLayouting(InstantiationContext context, C config) {
		super(context, config);
		_contextMenu = context.getInstance(config.getContextMenuFactory());
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		final ContentControl contentControl =
			ContentControl.create(component, _contextMenu.createContextMenuProvider(component));
		if (!(component instanceof ControlRepresentable)) {
			contentControl.setView(createContentDisplay(component));
		}
		return contentControl;
	}

	protected abstract HTMLFragment createContentDisplay(LayoutComponent component);

}
