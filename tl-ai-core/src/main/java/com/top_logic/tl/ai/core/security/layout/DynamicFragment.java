/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.io.IOException;
import java.util.function.Supplier;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;

/**
 * A block of a view that is recomputed and repainted when a value below it changes.
 *
 * <p>
 * Needed because the surrounding template is written once: a plain fragment has no identity in the
 * page and therefore cannot be replaced. Only the blocks that actually depend on a value carry one.
 * </p>
 */
final class DynamicFragment extends AbstractConstantControl {

	private final Supplier<HTMLFragment> _content;

	DynamicFragment(Supplier<HTMLFragment> content) {
		_content = content;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		_content.get().write(context, out);
		out.endTag(SPAN);
	}

	@Override
	protected String getTypeCssClass() {
		return "aiSecurityDynamic";
	}
}
