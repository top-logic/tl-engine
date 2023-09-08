/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.apply;

/**
 * Problem that occurs during {@link XApply}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XDiffSyntaxError extends RuntimeException {

	public XDiffSyntaxError() {
		super();
	}

	public XDiffSyntaxError(String message, Throwable cause) {
		super(message, cause);
	}

	public XDiffSyntaxError(String message) {
		super(message);
	}

	public XDiffSyntaxError(Throwable cause) {
		super(cause);
	}

}
