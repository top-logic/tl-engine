/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * A {@link Renderer} for a {@link BooleanField} displaying if warnings exist.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class WarningsFieldRenderer implements Renderer<FormField> {

	/** The singleton {@link WarningsFieldRenderer} instance. */
	public static final WarningsFieldRenderer INSTANCE = new WarningsFieldRenderer();

	private WarningsFieldRenderer() {
		// Reduce visibility
	}

	@Override
	public void write(DisplayContext context, TagWriter out, FormField value) throws IOException {
		FormField field = value;
		Boolean hasWarnings = (Boolean) field.getValue();
		if (hasWarnings) {
			ResKey tooltip = field.getTooltip();
			ResKey tooltipCaption = field.getTooltipCaption();
			TaskResult.ResultType.WARNING.getIcon().write(context, out, null, tooltip, tooltipCaption);
		}
	}

}