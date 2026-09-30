/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.base.taglibs.basic.LinkTagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * This Tag writes a favicon which is necessary for browsers to find. This tag must not be used if
 * the {@link ThemeBasedCSSTag} is already used.
 *
 * <p>
 * It provides also utility methods to create favicons without a {@link FaviconTag}.
 * </p>
 *
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FaviconTag extends TagSupport {

	/**
	 * Link relation under which a page icon is announced to the browser.
	 *
	 * @implNote The legacy relation <code>shortcut icon</code> is intentionally not written. It was
	 *           introduced by the Internet Explorer and never became part of any standard. Since
	 *           <code>rel</code> is a list of relations, <code>shortcut</code> is just an unknown
	 *           relation that browsers ignore, which makes the legacy form equivalent to this one.
	 */
	private static final String ICON_RELATION = "icon";

	private static final Map<String, String> TYPES = Stream.of(new String[][] {
		{ "ico", TYPE_ICO },
		{ "gif", TYPE_GIF },
		{ "png", TYPE_PNG },
		{ "svg", TYPE_SVG },
	}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

	private String _icon;

	// The context path of this application.
	private String _contextPath;

	/**
	 * Overwrites the default value for the icon.
	 *
	 * @param icon
	 *        Relative path to the icon file.
	 */
	public void setIcon(String icon) {
		_icon = icon;
	}

	@Override
	public int doStartTag() throws JspException {
		write(pageContext.getOut());

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		reset();

		return super.doEndTag();
	}

	@Override
	public void release() {
		reset();

		super.release();
	}

	/**
	 * Drops the state of a single tag invocation.
	 *
	 * <p>
	 * A tag handler instance is reused for other occurrences of the tag, which do not necessarily
	 * specify the same attributes. Therefore, values that were explicitly set must not survive the
	 * invocation they were set for.
	 * </p>
	 */
	private void reset() {
		_icon = null;
		_contextPath = null;
	}

	private void write(JspWriter out) {
		_contextPath = LinkTagUtil.getContextPath(this.pageContext, _contextPath);
		writeIcon(out, iconLink(_contextPath, _icon));
	}

	/**
	 * Writes the link that announces the given icon to the browser.
	 *
	 * @param out
	 *        The {@link TagWriter} where to write the link.
	 * @param icon
	 *        The path of the icon to write.
	 */
	public static void writeIcon(TagWriter out, String icon) {
		if (icon != null) {
			String type = getType(icon);

			out.beginBeginTag(LINK);
			out.writeAttribute(REL_ATTR, ICON_RELATION);
			if (type != null) {
				out.writeAttribute(TYPE_ATTR, type);
			}
			out.writeAttribute(HREF_ATTR, icon);
			out.endEmptyTag();
		}
	}

	/**
	 * Writes the link that announces the given icon to the browser.
	 *
	 * @param out
	 *        The {@link JspWriter} where to write the link.
	 * @param icon
	 *        The path of the icon to write.
	 */
	public static void writeIcon(JspWriter out, String icon) {
		try {
			if (icon != null) {
				String type = getType(icon);

				out.write("<");
				out.write(LINK + " ");
				out.write(REL_ATTR + "=\"" + ICON_RELATION + "\" ");
				if (type != null) {
					out.write(TYPE_ATTR + "=\"" + type + "\" ");
				}
				out.write(HREF_ATTR + "=\"" + icon + "\"");
				out.println("/>");
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static String getType(String icon) {
		String[] iconParts = icon.split("\\.");
		String fileExtension = iconParts[iconParts.length - 1];

		return TYPES.get(fileExtension);
	}

	/**
	 * Writes the default icon.
	 *
	 * @see #write(TagWriter, String)
	 */
	public static void write(DisplayContext context, TagWriter out) {
		String thePath = context.getContextPath();
		write(out, thePath);
	}

	/**
	 * Writes the default icon.
	 *
	 * @param contextPath
	 *        The application's context path.
	 * @see #writeIcon(TagWriter, String)
	 */
	public static void write(TagWriter out, String contextPath) {
		writeIcon(out, iconLink(contextPath, null));
	}

	/**
	 * Writes the default icon.
	 *
	 * @param contextPath
	 *        The application's context path.
	 * @see #writeIcon(JspWriter, String)
	 */
	public static void write(JspWriter out, String contextPath) {
		writeIcon(out, iconLink(contextPath, null));
	}

	/**
	 * Builds the link to announce for the given icon.
	 *
	 * @param icon
	 *        The explicitly requested icon, or <code>null</code> to use the application's default
	 *        icon.
	 * @implNote The icon is also written on the health check page <code>SystemState.jsp</code>,
	 *           whose purpose is to report which services of a partially started application are
	 *           unavailable. An unavailable {@link ThemeFactory} must therefore not be the reason
	 *           that page fails. This is why the default is taken from
	 *           {@link ThemeVar#defaultValue()} instead of the theme, and why the theme-local
	 *           variant of the image is only looked up when the service is there.
	 */
	private static String iconLink(String contextPath, String icon) {
		String imgPath;
		if (ThemeFactory.Module.INSTANCE.isActive()) {
			imgPath = ThemeFactory.getTheme()
				.getFileLink(icon != null ? icon : Icons.DEFAULT_ICON.get());
		} else {
			imgPath = icon != null ? icon : Icons.DEFAULT_ICON.defaultValue();
		}

		return LinkTagUtil.getLink(contextPath, imgPath);
	}
}
