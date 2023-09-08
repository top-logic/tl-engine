/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.net.URL;

/**
 * {@link DataItemValue} that is filled from an file in the workspace.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExternalDataValue extends DataItemValue {

	/**
	 * The {@link URL} of the contents to take the contents from.
	 */
	String getUrl();

	/** @see #getUrl() */
	void setUrl(String value);

}
