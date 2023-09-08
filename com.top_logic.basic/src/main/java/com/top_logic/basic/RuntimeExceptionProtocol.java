/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link RuntimeExceptionProtocol} that throws {@link RuntimeException}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public final class RuntimeExceptionProtocol extends ImmediateFailureProtocol {

	/** The instance of the {@link RuntimeExceptionProtocol}. */
	public static final RuntimeExceptionProtocol INSTANCE = new RuntimeExceptionProtocol();

	@Override
	protected RuntimeException createFailure(String message, Throwable ex) {
		return new RuntimeException(message, ex);
	}

}
