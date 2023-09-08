/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

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

	private static enum Relation {
		SHORTCUT_ICON("shortcut icon"), ICON("icon");

		Relation(String name) {
			_name = name;
		}

		private String _name;

		public String getName() {
			return _name;
		}
	}

	private static final Map<String, String> TYPES = Stream.of(new String[][] {
		{ "ico", TYPE_ICO },
		{ "gif", TYPE_GIF },
		{ "png", TYPE_PNG },
		{ "svg", TYPE_SVG },
	}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

	private static final String SHORTCUT_ICON_DEFAULT = "/images/shortcut.ico";

	private static final String ICON_DEFAULT = SHORTCUT_ICON_DEFAULT;

	private String _shortcutIcon = SHORTCUT_ICON_DEFAULT;

	private String _icon = ICON_DEFAULT;

	// The context path of this application.
	private String _contextPath;

	/**
	 * Overwrites the default value for the shortcut icon.
	 * 
	 * @param shortcutIcon
	 *        Relative path to the shortcut icon file.
	 */
	public void setShortcutIcon(String shortcutIcon) {
		_shortcutIcon = shortcutIcon;
	}

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

	private void write(JspWriter out) {
		_contextPath = LinkTagUtil.getContextPath(this.pageContext, _contextPath);
		String shortcutIcon = LinkTagUtil.getLink(_contextPath, _shortcutIcon);
		String icon = LinkTagUtil.getLink(_contextPath, _icon);
		write(out, shortcutIcon, icon);
	}

	/**
	 * Writes a link for a given shortcut icon and icon. Ones for the relation 'shortcut icon' and
	 * ones for 'icon'.
	 * 
	 * @param out
	 *        The {@link TagWriter} where to write the link.
	 * @param shortcutIcon
	 *        The path for the shortcut icon to write.
	 * @param icon
	 *        The path for the shortcut icon to write.
	 */
	public static void write(TagWriter out, String shortcutIcon, String icon) {
		write(out, Relation.SHORTCUT_ICON, shortcutIcon);
		write(out, Relation.ICON, icon);
	}

	/**
	 * Writes a link for a given shortcut icon and icon. Ones for the relation 'shortcut icon' and
	 * ones for 'icon'.
	 * 
	 * @param out
	 *        The {@link JspWriter} where to write the link.
	 * @param shortcutIcon
	 *        The path for the shortcut icon to write.
	 * @param icon
	 *        The path for the shortcut icon to write.
	 */
	public static void write(JspWriter out, String shortcutIcon, String icon) {
		write(out, Relation.SHORTCUT_ICON, shortcutIcon);
		write(out, Relation.ICON, icon);
	}

	private static void write(JspWriter out, Relation relation, String icon) {
		try {
			if (icon != null) {
				String type = getType(icon);

				out.write("<");
				out.write(LINK + " ");
				out.write(REL_ATTR + "=\"" + relation.getName() + "\" ");
				if (type != null) {
					out.write(TYPE_ATTR + "=\"" + type + "\" ");
				}
				out.write(HREF_ATTR + "=\"" + icon + "\"");
				out.println("/>");
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private static void write(TagWriter out, Relation relation, String icon) {
		if (icon != null) {
			String type = getType(icon);

			out.beginBeginTag(LINK);
			out.writeAttribute(REL_ATTR, relation.getName());
			if (type != null) {
				out.writeAttribute(TYPE_ATTR, type);
			}
			out.writeAttribute(HREF_ATTR, icon);
			out.endEmptyTag();
		}
	}

	private static String getType(String icon) {
		String[] iconParts = icon.split("\\.");
		String fileExtension = iconParts[iconParts.length - 1];
		
		return TYPES.get(fileExtension);
	}

	/**
	 * Writes a default shortcut icon and default icon.
	 * 
	 * @see #write(TagWriter, String)
	 */
	public static void write(DisplayContext context, TagWriter out) {
		String thePath = context.getContextPath();
		write(out, thePath);
	}

	/**
	 * Writes a default shortcut icon and icon.
	 * 
	 * @param contextPath
	 *        The application's context path.
	 * @see #write(TagWriter, String, String)
	 */
	public static void write(TagWriter out, String contextPath) {
		String shortcutIcon = LinkTagUtil.getLink(contextPath, SHORTCUT_ICON_DEFAULT);
		String icon = LinkTagUtil.getLink(contextPath, ICON_DEFAULT);
		write(out, shortcutIcon, icon);
	}

	/**
	 * Writes a default shortcut icon and icon.
	 * 
	 * @param contextPath
	 *        The application's context path.
	 * @see #write(JspWriter, String, String)
	 */
	public static void write(JspWriter out, String contextPath) {
		String shortcutIcon = LinkTagUtil.getLink(contextPath, SHORTCUT_ICON_DEFAULT);
		String icon = LinkTagUtil.getLink(contextPath, ICON_DEFAULT);
		write(out, shortcutIcon, icon);
	}
}
