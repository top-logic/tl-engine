/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.recorder.specialcases.LabelScope;

/**
 * {@link ContextRef} that identifies a value in a {@link LabelScope}.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface LabeledValue extends ContextRef {

	/**
	 * The identifying label of the value.
	 */
	String getLabel();

	/** @see #getLabel() */
	void setLabel(String value);

}
