/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.text.Format;
import java.text.ParsePosition;

/**
 * An extension of {@link ParsePosition} that can be augmented with
 * user-readable problem descriptions, if parsing of a {@link Format} fails.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DescriptiveParsePosition extends ParsePosition {

	private String errorMessage;
	private String errorDetail;
	
	/**
	 * @see ParsePosition#ParsePosition(int)
	 */
	public DescriptiveParsePosition(int index) {
		super(index);
	}

	/**
	 * @see #setErrorDescription(String, String)
	 */
	public void setErrorDescription(String errorMessage) {
		setErrorDescription(errorMessage, null);
	}

	/**
	 * Sets an (internationalized) error message that explains the problem
	 * reported at position {@link #getErrorIndex()}.
	 * 
	 * @param errorMessage
	 *     A short internationalized error message. <code>null</code>
	 *     means "no description".
	 * @param errorDetail
	 *     An (optional) detailed description of the problem (with an
	 *     advice how to change the parsed value to become valid
	 *     according to the format).
	 */
	public void setErrorDescription(String errorMessage, String errorDetail) {
		this.errorMessage = errorMessage;
		this.errorDetail = errorDetail;
	}

	/**
	 * @see #setErrorDescription(String, String)
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * @see #setErrorDescription(String, String)
	 */
	public String getErrorDetail() {
		return errorDetail;
	}

	/**
	 * @see #setErrorDescription(String, String)
	 */
	public boolean hasErrorMessage() {
		return errorMessage != null;
	}

	/**
	 * @see #setErrorDescription(String, String)
	 */
	public boolean hasErrorDetail() {
		return errorDetail != null;
	}

	/**
	 * Reset this parse position object for reuse.
	 */
	public void reset() {
		errorDetail = null;
		errorMessage = null;
		setIndex(0);
		setErrorIndex(-1);
	}
}
