/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for the React WYSIWYG module.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Image upload failed.
	 */
	public static ResKey ERROR_IMAGE_UPLOAD_FAILED;

	/** @en Bold */
	@CustomKey("js.wysiwyg.bold")
	public static ResKey JS_WYSIWYG_BOLD;

	/** @en Italic */
	@CustomKey("js.wysiwyg.italic")
	public static ResKey JS_WYSIWYG_ITALIC;

	/** @en Underline */
	@CustomKey("js.wysiwyg.underline")
	public static ResKey JS_WYSIWYG_UNDERLINE;

	/** @en Strikethrough */
	@CustomKey("js.wysiwyg.strikethrough")
	public static ResKey JS_WYSIWYG_STRIKETHROUGH;

	/** @en Heading */
	@CustomKey("js.wysiwyg.heading")
	public static ResKey JS_WYSIWYG_HEADING;

	/** @en Paragraph */
	@CustomKey("js.wysiwyg.paragraph")
	public static ResKey JS_WYSIWYG_PARAGRAPH;

	/** @en Heading 1 */
	@CustomKey("js.wysiwyg.heading1")
	public static ResKey JS_WYSIWYG_HEADING1;

	/** @en Heading 2 */
	@CustomKey("js.wysiwyg.heading2")
	public static ResKey JS_WYSIWYG_HEADING2;

	/** @en Heading 3 */
	@CustomKey("js.wysiwyg.heading3")
	public static ResKey JS_WYSIWYG_HEADING3;

	/** @en Heading 4 */
	@CustomKey("js.wysiwyg.heading4")
	public static ResKey JS_WYSIWYG_HEADING4;

	/** @en Heading 5 */
	@CustomKey("js.wysiwyg.heading5")
	public static ResKey JS_WYSIWYG_HEADING5;

	/** @en Heading 6 */
	@CustomKey("js.wysiwyg.heading6")
	public static ResKey JS_WYSIWYG_HEADING6;

	/** @en Bullet list */
	@CustomKey("js.wysiwyg.bulletList")
	public static ResKey JS_WYSIWYG_BULLET_LIST;

	/** @en Numbered list */
	@CustomKey("js.wysiwyg.orderedList")
	public static ResKey JS_WYSIWYG_ORDERED_LIST;

	/** @en Lists */
	@CustomKey("js.wysiwyg.lists")
	public static ResKey JS_WYSIWYG_LISTS;

	/** @en Blockquote */
	@CustomKey("js.wysiwyg.blockquote")
	public static ResKey JS_WYSIWYG_BLOCKQUOTE;

	/** @en Link */
	@CustomKey("js.wysiwyg.link")
	public static ResKey JS_WYSIWYG_LINK;

	/** @en URL */
	@CustomKey("js.wysiwyg.linkUrl")
	public static ResKey JS_WYSIWYG_LINK_URL;

	/** @en Apply */
	@CustomKey("js.wysiwyg.linkApply")
	public static ResKey JS_WYSIWYG_LINK_APPLY;

	/** @en Remove link */
	@CustomKey("js.wysiwyg.linkRemove")
	public static ResKey JS_WYSIWYG_LINK_REMOVE;

	/** @en Image */
	@CustomKey("js.wysiwyg.image")
	public static ResKey JS_WYSIWYG_IMAGE;

	/** @en Table */
	@CustomKey("js.wysiwyg.table")
	public static ResKey JS_WYSIWYG_TABLE;

	/** @en Code block */
	@CustomKey("js.wysiwyg.codeBlock")
	public static ResKey JS_WYSIWYG_CODE_BLOCK;

	/** @en Undo */
	@CustomKey("js.wysiwyg.undo")
	public static ResKey JS_WYSIWYG_UNDO;

	/** @en Redo */
	@CustomKey("js.wysiwyg.redo")
	public static ResKey JS_WYSIWYG_REDO;

	static {
		initConstants(I18NConstants.class);
	}
}
