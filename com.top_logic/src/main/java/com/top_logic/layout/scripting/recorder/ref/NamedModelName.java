/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;

/**
 * {@link ModelName} for a model with a name.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface NamedModelName extends ModelName, NamedConfiguration {

	// Pure sum interface

}

