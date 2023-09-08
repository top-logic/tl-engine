/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import com.top_logic.basic.DebugHelper;
import com.top_logic.layout.LabelProvider;

/**
 * Output of a time value represented as {@link Long} nanoseconds as <code>mm:ss,SSS</code>.
 * 
 * @see NanoSecondsFormat
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NanoSecondsLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link NanoSecondsLabelProvider} instance.
	 */
	public static final NanoSecondsLabelProvider INSTANCE = new NanoSecondsLabelProvider();

	private NanoSecondsLabelProvider() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof Long) {
			long nanos = ((Long) object).longValue();
			return DebugHelper.toDuration(nanos / 1000000L);
		}
		return null;
	}

}
