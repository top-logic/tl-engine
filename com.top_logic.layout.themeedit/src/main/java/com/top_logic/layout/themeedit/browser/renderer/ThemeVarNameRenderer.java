/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.renderer;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.tooltip.HtmlToolTip;
import com.top_logic.layout.tooltip.ToolTip;

/**
 * {@link Renderer} for shortened {@link ThemeSetting} names.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeVarNameRenderer implements Renderer<String> {

	/**
	 * Singleton {@link ThemeVarNameRenderer} instance.
	 */
	public static final ThemeVarNameRenderer INSTANCE = new ThemeVarNameRenderer();

	private ThemeVarNameRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, String varName) throws IOException {
		String helpHtml = context.getResources().getString(ResKey.internalCreate(varName), null);

		String shortened = KeyProvider.INSTANCE.map(varName);
		if (shortened.equals(varName) && helpHtml == null) {
			out.writeText(varName);
		} else {
			out.beginBeginTag(SPAN);
			new ToolTip() {
				@Override
				public boolean hasCaption() {
					return true;
				}

				@Override
				public void writeCaption(DisplayContext context, TagWriter out) throws IOException {
					out.write(varName);
				}

				@Override
				public void writeContent(DisplayContext context, TagWriter out) throws IOException {
					if (helpHtml != null) {
						out.writeContent(HtmlToolTip.ensureSafeHTMLTooltip(helpHtml));
					}
				}
			}.writeAttribute(context, out);
			out.endBeginTag();
			{
				out.writeText(shortened);
			}
			out.endTag(SPAN);
		}
	}

	/**
	 * Sort key provider {@link Mapping} corresponding to {@link ThemeVarNameRenderer}.
	 */
	public static class KeyProvider implements Mapping<String, String> {

		/**
		 * Singleton {@link ThemeVarNameRenderer.KeyProvider} instance.
		 */
		@SuppressWarnings("hiding")
		public static final KeyProvider INSTANCE = new KeyProvider();

		private KeyProvider() {
			// Singleton constructor.
		}

		@Override
		public String map(String input) {
			if (input.startsWith("mime.")) {
				return input.substring("mime.".length());
			}

			int sepIdx = input.lastIndexOf('.');
			if (sepIdx >= 0) {
				return input.substring(sepIdx + 1);
			} else {
				return input;
			}
		}

	}

}
