/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.config.WithPlainContextMenuFactory;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControlProvider} that embeds any legacy {@link LayoutComponent}
 * through an {@link InlineContentView} in div layout.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IFrameLayoutControlProvider extends DecoratingLayoutControlProvider<IFrameLayoutControlProvider.Config> {
	
	/**
	 * Configuration options for {@link IFrameLayoutControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<IFrameLayoutControlProvider>, WithPlainContextMenuFactory {

		/**
		 * An additional custom CSS class to set in the layout control tag.
		 */
		@Nullable
		String getCssClass();

		/**
		 * Whether the displayed component should get scroll bars.
		 */
		Scrolling getScrolling();

	}

	/**
	 * Singleton {@link IFrameLayoutControlProvider} instance.
	 */
	public static final LayoutControlProvider INSTANCE = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
		.getInstance(TypedConfiguration.newConfigItem(Config.class));

	private final String _cssClass;

	private final Scrolling _scrolling;

	private ContextMenuFactory _contextMenu;

	/**
	 * Creates a {@link IFrameLayoutControlProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public IFrameLayoutControlProvider(InstantiationContext context, Config config) {
		super(context, config);
		_contextMenu = context.getInstance(config.getContextMenuFactory());
		_cssClass = config.getCssClass();
		_scrolling = config.getScrolling();
	}
	
	@Override
	public Control mkLayout(Strategy strategy, LayoutComponent component) {
		ContentControl contentControl =
			ContentControl.create(component, _contextMenu.createContextMenuProvider(component));
		contentControl.setCssClass(_cssClass);
		contentControl.setConstraint(
			new DefaultLayoutData(DisplayDimension.HUNDERED_PERCENT, 100, DisplayDimension.HUNDERED_PERCENT, 100,
				_scrolling));
		return contentControl;
	}

}
