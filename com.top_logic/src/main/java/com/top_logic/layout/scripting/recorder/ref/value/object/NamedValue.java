/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * {@link ContextRef} that identifies a value with a structured name.
 * 
 * <p>
 * A {@link NamedValue} can only be retrieved from a context with choices by matching.
 * </p>
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @see ValueNamingScheme
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface NamedValue extends ContextRef, ValueNamingScheme.NameBase {

	// Pure sum interface.

}
