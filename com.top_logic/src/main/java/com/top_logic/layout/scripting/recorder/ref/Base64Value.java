/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

/**
 * Base64 encoded {@link DataItemValue}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Base64Value extends DataItemValue {

	/**
	 * The base 64 encoded binary contents.
	 */
	String getBase64Data();

	/** @see #getBase64Data() */
	void setBase64Data(String base64);

}
