/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.layout;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	@CustomKey("element.comments.commentTable.quotetext")
	public static ResKey QUOTETEXT;

	@CustomKey("tl.command.save")
	public static ResKey SAVE;

	@CustomKey("element.wrapper.comment.tooltip")
	public static ResKey3 COMMENT_TOOLTIP;

	static {
		initConstants(I18NConstants.class);
	}
}
