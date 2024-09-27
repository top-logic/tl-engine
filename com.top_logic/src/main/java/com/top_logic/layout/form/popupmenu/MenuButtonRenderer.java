/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.popupmenu;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Renderer for menu entries in a popup menu.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MenuButtonRenderer extends AbstractButtonRenderer<MenuButtonRenderer.Config>
{

	/**
	 * Singleton {@link MenuButtonRenderer} instance.
	 */
	public static final MenuButtonRenderer INSTANCE = TypedConfigUtil.newConfiguredInstance(MenuButtonRenderer.class);

	/**
	 * Typed configuration interface definition for {@link MenuButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractButtonRenderer.Config<MenuButtonRenderer> {
		// configuration interface definition
	}

	/**
	 * Create a {@link MenuButtonRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public MenuButtonRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ResKey getTooltip(AbstractButtonControl<?> aButtonControl) {
		return null; // Tooltip supression for commands of command menu.
	}

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return "cMenuButton";
	}

	@Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button) throws IOException {
		MenuLinkRenderer.INSTANCE.writeLink(context, out, button);
	}


	@Override
	protected ThemeImage getMissingIcon(boolean isDisabled) {
		return Icons.BUTTON_EMPTY;
	}

	@Override
	public void appendControlCSSClasses(Appendable out, AbstractButtonControl<?> self) throws IOException {
		super.appendControlCSSClasses(out, self);
		HTMLUtil.appendCSSClass(out, MenuLinkRenderer.getCssClass(self));
	}

}
