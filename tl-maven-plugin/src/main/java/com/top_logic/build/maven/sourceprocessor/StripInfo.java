/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.sourceprocessor;

import com.top_logic.tool.stacktrace.internal.IntRanges;

/**
 * Stripping information from a transformed file.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StripInfo {

	private final int _lineCnt;

	private final IntRanges _excluded;

	public StripInfo(int lineCnt, IntRanges excluded) {
		_lineCnt = lineCnt;
		_excluded = excluded;
	}

	public int getLineCnt() {
		return _lineCnt;
	}

	public IntRanges getExcludeRanges() {
		return _excluded;
	}

	@Override
	public String toString() {
		return _lineCnt + ", " + _excluded;
	}

}
