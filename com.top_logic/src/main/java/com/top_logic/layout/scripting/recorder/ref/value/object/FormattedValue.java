/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.basic.io.TLFormat;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * Uses the given {@link TLFormat} to parse the value stored in {@link #getFormattedValue()}.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public interface FormattedValue extends ValueRef {

	/** The value formatted with {@link #getFormat()}. */
	String getFormattedValue();

	/** @see #getFormattedValue() */
	void setFormattedValue(String formattedValue);

	/** The {@link TLFormat} that is used for formatting and parsing {@link #getFormattedValue()}. */
	TLFormat<?> getFormat();

	/** @see #getFormat() */
	void setFormat(TLFormat<?> format);

}
