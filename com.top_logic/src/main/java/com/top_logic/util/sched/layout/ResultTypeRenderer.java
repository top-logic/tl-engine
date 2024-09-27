/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Render an image for the {@link ResultType}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ResultTypeRenderer implements Renderer<ResultType> {

	/**
	 * Singleton {@link ResultTypeRenderer} instance.
	 */
	public static final ResultTypeRenderer INSTANCE = new ResultTypeRenderer();

	private ResultTypeRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext aContext, TagWriter anOut, ResultType aValue) throws IOException {
		ResultType theType = (aValue != null) ? (ResultType) aValue : ResultType.UNKNOWN;

		theType.getIcon().writeWithPlainTooltip(aContext, anOut, theType.getMessageI18N());
	}
}

