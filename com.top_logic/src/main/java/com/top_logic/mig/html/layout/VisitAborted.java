/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

/**
 * {@link RuntimeException} to terminate a visit ahead of time (e.g. if some
 * checked exception is caught that cannot be re-thrown).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VisitAborted extends RuntimeException {

	public VisitAborted() {
		super();
	}

	public VisitAborted(Throwable cause) {
		super(cause);
	}
	
}
