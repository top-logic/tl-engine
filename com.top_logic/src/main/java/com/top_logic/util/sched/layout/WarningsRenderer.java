/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * Render an image for warnings with a tooltip containing the texts of the warnings.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class WarningsRenderer implements Renderer<List<String>> {

	/** The singleton {@link WarningsRenderer} instance. */
	public static final WarningsRenderer INSTANCE = new WarningsRenderer();

	private WarningsRenderer() {
		// Reduce visibility
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, List<String> warnings) throws IOException {
		if (CollectionUtil.isEmptyOrNull(warnings)) {
			return;
		}
		ResKey tooltipHTML = TaskFormUtil.createWarningsTooltipText(warnings);
		ResKey tooltipCaption = TaskFormUtil.createWarningsTooltipCaption(warnings);
		TaskResult.ResultType.WARNING.getIcon().write(displayContext, out, null, tooltipHTML, tooltipCaption);
	}

}
