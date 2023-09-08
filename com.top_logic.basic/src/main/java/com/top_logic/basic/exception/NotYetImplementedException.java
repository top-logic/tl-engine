/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.exception;

/**
 * An {@link UnsupportedOperationException} for things that are not yet implemented but should be,
 * eventually, in contrast to things that will never be supported.
 * <p>
 * Using this exception has the additional advantage that it makes it much easier to find missing
 * code parts.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NotYetImplementedException extends UnsupportedOperationException {

	/**
	 * Creates an {@link NotYetImplementedException}.
	 */
	public NotYetImplementedException(String message) {
		super(message);
	}

}
