/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.config.WithPlainContextMenuFactory;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} for {@link ControlRepresentable} components.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ControlRepresentableCP extends DecoratingLayoutControlProvider<ControlRepresentableCP.Config> {

	/**
	 * Configuration options for {@link ControlRepresentableCP}.
	 */
	public interface Config extends PolymorphicConfiguration<ControlRepresentableCP>, WithPlainContextMenuFactory {

		/**
		 * An additional custom CSS class to set in the layout control tag.
		 * 
		 * @see ContentLayoutControlProvider.Config#getCssClass()
		 */
		@Nullable
		String getCssClass();

	}

	private final ContextMenuFactory _contextMenu;

	/**
	 * Creates a {@link ControlRepresentableCP} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ControlRepresentableCP(InstantiationContext context, Config config) {
		super(context, config);
		_contextMenu = context.getInstance(config.getContextMenuFactory());
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		final ContentControl contentControl =
			ContentControl.create(component, _contextMenu.createContextMenuProvider(component));
		contentControl.setCssClass(getConfig().getCssClass());

		if (component instanceof ControlRepresentable) {
			// Note: The rendering control of ControlRepresentable components must be allocated
			// lazily, since it may depend on the component model being set during validation.
			//
			// contentControl.setView(((ControlRepresentable) component).getRenderingControl());
		} else {
			contentControl.setView(new WriteBodyInline(component));
		}

		return contentControl;
	}

}
