/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

/**
 * An {@link Exception} that is thrown to abort processing, when the server
 * detects that AJAX requests arrive out-of-order.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AJAXOutOfSequenceException extends Exception {

	/**
	 * Creates a new {@link AJAXOutOfSequenceException}.
	 */
	public AJAXOutOfSequenceException(String message) {
		super(message);
	}

}
