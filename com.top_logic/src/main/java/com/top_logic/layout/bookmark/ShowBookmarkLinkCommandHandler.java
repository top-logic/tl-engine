/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.bookmark;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.BookmarkRenderer;
import com.top_logic.layout.basic.Icons;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * {@link CommandHandler} showing a dialog with a bookmark link for a given target model.
 * 
 * @implNote This handler is registered globally in the {@link CommandHandlerFactory}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ShowBookmarkLinkCommandHandler extends PreconditionCommandHandler {

	/**
	 * Configuration options for {@link ShowBookmarkLinkCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Whether to include the concrete view in the bookmark, where the bookmark was created.
		 * 
		 * <p>
		 * If <code>false</code>, the bookmark navigates to the default view of the object.
		 * </p>
		 */
		boolean shouldIncludeView();

	}

	/**
	 * Creates a {@link ShowBookmarkLinkCommandHandler}.
	 */
	public ShowBookmarkLinkCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		BoundComponent targetComponent = targetView(component);

		if (!GotoHandler.canShow(model, targetComponent)) {
			return new Hide();
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				MessageBox.newBuilder(MessageType.INFO)
					.title(I18NConstants.CREATE_BOOKMARK_TITLE__MODEL.fill(model))
					.layout(new DefaultLayoutData(DisplayDimension.px(350), 100, DisplayDimension.px(200), 100,
						Scrolling.AUTO))
					.message(this::renderContents)
					.buttons(MessageBox.button(ButtonType.CLOSE))
					.confirm(context.getWindowScope());
			}
			
			private void renderContents(DisplayContext context, TagWriter out) throws IOException {
				out.beginTag(PARAGRAPH);
				{
					out.writeText(
						context.getResources() .getString(I18NConstants.CREATE_BOOKMARK__MODEL.fill(model)));
				}
				out.endTag(PARAGRAPH);

				out.beginTag(BLOCKQUOTE);
				{
					String label = MetaResourceProvider.INSTANCE.getLabel(model);
					String url = BookmarkRenderer.getBookmarkURL(context, model, targetComponent);

					out.beginBeginTag(BookmarkRenderer.ANCHOR);
					out.writeAttribute(BookmarkRenderer.HREF_ATTR, url);
					OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
						BookmarkRenderer.getBookmarkTooltipLabel(context, label));

					// Open browser context menu instead of TL context menu.
					out.writeAttribute(BookmarkRenderer.TL_BROWSER_MENU_ATTR, true);
					BookmarkRenderer.writeOnClick(out, label, url);
					out.endBeginTag();
					{
						Icons.ADD_BOOKMARK.writeWithCss(context, out, FormConstants.INPUT_IMAGE_CSS_CLASS);
						out.writeText(BookmarkRenderer.NBSP);
						out.writeText(label);
					}
					out.endTag(BookmarkRenderer.ANCHOR);
				}

				out.endTag(BLOCKQUOTE);
			}
		};
	}

	private BoundComponent targetView(LayoutComponent component) {
		if (((Config) getConfig()).shouldIncludeView()) {
			return component instanceof BoundComponent ? (BoundComponent) component : null;
		} else {
			return null;
		}
	}

}
