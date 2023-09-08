/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.scripting.recorder.ref.value.NamedValueRef;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * Abstract {@link ValueRef} representing the contents of a {@link BinaryData}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DataItemValue extends NamedValueRef {

	/**
	 * The file name.
	 * 
	 * @see BinaryData#getName()
	 */
	@Override
	String getName();

	/**
	 * The content type of the represented {@link BinaryData}.
	 * 
	 * @see BinaryData#getContentType()
	 */
	String getContentType();

	/** @see #getContentType() */
	void setContentType(String name);

}
