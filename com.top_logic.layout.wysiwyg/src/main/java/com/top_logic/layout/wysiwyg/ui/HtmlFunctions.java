/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions for the structured text of HTML attributes.
 *
 * <p>
 * A value of such an attribute is a {@link StructuredText}: its HTML source plus the images the
 * source refers to. These functions take that source apart and put it together again, so that a
 * script can compose the content of an HTML attribute.
 * </p>
 */
@ScriptPrefix("html")
public class HtmlFunctions extends TLScriptFunctions {

	/**
	 * The HTML source of the given structured text.
	 *
	 * @param content
	 *        The value of an HTML attribute.
	 * @return The HTML source, empty for content that has none.
	 */
	@Label("HTML source of structured text")
	@SideEffectFree
	public static String source(StructuredText content) {
		return content == null ? StringServices.EMPTY_STRING : content.getSourceCode();
	}

	/**
	 * Structured text with the given HTML source, to store in an HTML attribute.
	 *
	 * @param source
	 *        The HTML source, taken as it is.
	 * @return The value to write to an HTML attribute.
	 */
	@Label("Structured text with HTML source")
	@SideEffectFree
	public static StructuredText text(String source) {
		return new StructuredText(StringServices.nonNull(source));
	}

	/**
	 * A link leading to the given object, as HTML source to embed into structured text.
	 *
	 * <p>
	 * Displayed structured text offers such a link as a link to the object: following it leads to
	 * the place the application shows objects of that kind.
	 * </p>
	 *
	 * @param object
	 *        The object the link leads to.
	 * @param label
	 *        The text the link is displayed with. Empty uses the object's own label.
	 * @return The link as an HTML anchor element.
	 */
	@Label("Link to an object")
	@SideEffectFree
	public static String objectLink(@Mandatory TLObject object, String label) {
		return TLObjectLinkUtil.getLink(object, label, null);
	}

}
