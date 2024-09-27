/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Render an image for the combination of {@link TaskState} and {@link ResultType}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CombinedStateRenderer implements Renderer<Object> {

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		ThemeImage image;
		ResKey messageKey;
		if (value instanceof ResultType) {
			ResultType type = (ResultType) value;
			messageKey = type.getMessageI18N();
			image = type.getIcon();
		} else if (value instanceof TaskState) {
			TaskState state = (TaskState) value;
			messageKey = state.getMessageI18N();
			image = state.getIcon();
		} else if (value == null) {
			messageKey = ResultType.UNKNOWN.getMessageI18N();
			image = ResultType.UNKNOWN.getIcon();
		} else {
			throw new RuntimeException("Don't know how to handle this value: "
				+ StringServices.getObjectDescription(value));
		}

		image.writeWithPlainTooltip(context, out, messageKey);
	}
}

