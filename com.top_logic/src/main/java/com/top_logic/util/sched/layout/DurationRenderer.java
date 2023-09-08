/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.io.IOException;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * Display a long value as duration in "mm:ss,msec".
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DurationRenderer implements Renderer<Long> {

	/** 
	 * Creates a {@link DurationRenderer}.
	 * 
	 */
	public DurationRenderer() {
	}

	@Override
	public void write(DisplayContext aContext, TagWriter anOut, Long aValue) throws IOException {
		if (aValue != null) {
			anOut.writeText(DurationRenderer.getLabel(aValue));
        }
	}

	/** 
	 * Convert the given long to a duration in 'mm:ss,msec'.
	 * 
	 * @param    aValue    The value to be formatted, must not be <code>null</code>.
	 * @return   The requested string representation, never <code>null</code>.
	 */
	public static CharSequence getLabel(Long aValue) {
		return DebugHelper.toDuration(aValue);
	}
}

