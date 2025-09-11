/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Name;

/**
 * Base class for classes that have functions to call in TL-Script.
 * 
 * <p>
 * All "public static" in extensions of this class are offered as TL-Script functions.
 * </p>
 * 
 * <p>
 * The name of the methods prefixed by the {@link Class#getSimpleName() name} of the owner class are
 * the names of available TL-Scripts. A different prefix than the name of the class can be
 * determined by using the {@link ScriptPrefix} annotation; a different name for suffix can be
 * determined by using the {@link Name} annotation at the method.
 * </p>
 * 
 * @impl This class is used in TLDoclet. It must be adapted, when this class is moved.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLScriptFunctions {

	// No functions here.

}

