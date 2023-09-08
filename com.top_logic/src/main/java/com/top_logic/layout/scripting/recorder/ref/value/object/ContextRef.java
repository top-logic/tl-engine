/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.scripting.recorder.ref.ContextDependent;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * Abstract {@link ValueRef} that must be resolved within an additional context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
@Abstract
public interface ContextRef extends ValueRef, ContextDependent {

	// Pure marker interface.

}
