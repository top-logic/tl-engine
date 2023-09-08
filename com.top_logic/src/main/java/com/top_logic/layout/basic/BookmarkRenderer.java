/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkFactory;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * {@link ResourceRenderer} that appends an "add bookmark" icon to the rendered contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BookmarkRenderer extends ResourceRenderer<ResourceRenderer.Config<BookmarkRenderer>> {

	/**
	 * Singleton {@link BookmarkRenderer} instance using {@link MetaResourceProvider}.
	 */
	@SuppressWarnings("hiding")
	public static final BookmarkRenderer INSTANCE = createBookmarkRenderer(MetaResourceProvider.INSTANCE);

	/**
	 * Creates a {@link BookmarkRenderer}.
	 * 
	 * @param resourceProvider
	 *        See {@link ResourceRenderer#newResourceRenderer(ResourceProvider)}.
	 */
	public static BookmarkRenderer createBookmarkRenderer(ResourceProvider resourceProvider) {
		return TypedConfigUtil.createInstance(createConfig(BookmarkRenderer.class, resourceProvider));
	}

	/**
	 * Creates a new {@link BookmarkRenderer}.
	 */
	public BookmarkRenderer(InstantiationContext context, Config<BookmarkRenderer> config) {
		super(context, config);
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		super.write(context, out, value);

		writeBookmarkIcon(context, out, getResourceProvider(), value);
	}

	/**
	 * Writes a bookmark icon that allows the user to bookmark the given object.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param labelProvider
	 *        The {@link LabelProvider} that creates the bookmark name.
	 * @param bookmarkObject
	 *        The object to bookmark.
	 */
	public static void writeBookmarkIcon(DisplayContext context, TagWriter out, LabelProvider labelProvider,
			Object bookmarkObject) throws IOException {
		writeBookmarkIcon(context, out, labelProvider, bookmarkObject, null);
	}

	/**
	 * Writes a bookmark icon that allows the user to bookmark the given object.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param labelProvider
	 *        The {@link LabelProvider} that creates the bookmark name.
	 * @param bookmarkObject
	 *        The object to bookmark.
	 * @param targetComponent
	 *        The target component of the bookmark. May be <code>null</code> for default view.
	 */
	public static void writeBookmarkIcon(DisplayContext context, TagWriter out, LabelProvider labelProvider,
			Object bookmarkObject, BoundComponent targetComponent) throws IOException {
		if (GotoHandler.canShow(bookmarkObject, targetComponent)) {
			String label = labelProvider.getLabel(bookmarkObject);
			String url = getBookmarkURL(context, bookmarkObject, targetComponent);

			out.writeText(NBSP);
			out.beginBeginTag(ANCHOR);
			out.writeAttribute(HREF_ATTR, url);
			// open browser context menu instead of TL context menu.
			out.writeAttribute(TL_BROWSER_MENU_ATTR, true);
			writeOnClick(out, getBookmarkLabel(context, label, targetComponent), url);
			out.endBeginTag();
			{
				Icons.ADD_BOOKMARK.writeWithCssTooltip(context, out, FormConstants.INPUT_IMAGE_CSS_CLASS,
					getBookmarkTooltipLabel(context, label));
			}
			out.endTag(ANCHOR);
		}
	}

	/**
	 * Creates the bookmark URL.
	 * 
	 * @see BookmarkFactory#getBookmarkURL(DisplayContext, TLObject, ComponentName)
	 */
	public static String getBookmarkURL(DisplayContext context, Object model, BoundComponent targetComponent) {
		ComponentName componentName;
		if (targetComponent == null) {
			componentName = null;
		} else {
			componentName = targetComponent.getName();
		}
		return BookmarkFactory.getBookmarkURL(context, (TLObject) model, componentName);
	}

	private static String getBookmarkLabel(DisplayContext context, String label, BoundComponent targetComponent) {
		return StringServices.isEmpty(label) ? context.getResources().getString(targetComponent.getTitleKey()) : label;
	}

	/**
	 * Produces a tooltip for the bookmark icon.
	 */
	public static String getBookmarkTooltipLabel(DisplayContext context, String label) {
		return StringServices.isEmpty(label)
			? context.getResources().getString(I18NConstants.BOOKMARK_TOOLTIP__LABEL_EMPTY)
			: context.getResources().getString(I18NConstants.BOOKMARK_TOOLTIP__LABEL.fill(label));
	}

	/**
	 * Creates the "add bookmark" onclick script.
	 */
	public static void writeOnClick(TagWriter out, String label, String url) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("BAL.addBookmark(");
		out.writeJsString(label);
		out.append(", ");
		out.writeJsString(url);
		out.append("); return false;");
		out.endAttribute();
	}

}
