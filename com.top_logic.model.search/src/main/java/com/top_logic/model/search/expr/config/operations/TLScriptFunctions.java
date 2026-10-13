/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.trace.ScriptTracer;

/**
 * Base class for classes that have functions to call from TL-Script.
 * 
 * <p>
 * All "public static" in extensions of this class are offered as TL-Script methods.
 * </p>
 * 
 * <p>
 * The name of the methods prefixed by the {@link Class#getSimpleName() name} of the owner class are
 * the names of TL-Script functions. A different prefix than the name of the class can be determined
 * by using the {@link ScriptPrefix} annotation; a different name for suffix can be determined by
 * using the {@link Name} annotation at the method.
 * </p>
 * 
 * <p>
 * Each method parameter becomes a TL-Script parameter.
 * <ol>
 * <li>To mark a parameter as mandatory, annotate the method parameter as {@link Mandatory
 * mandatory}.</li>
 * <li>It is possible to define a default value for a primitive parameter type using the
 * corresponding default annotation. For example, use {@link StringDefault} for strings and
 * {@link LongDefault} for long values.</li>
 * <li>When a parameter cannot be natively be converted from TL-Script values (e.g., long to
 * String), it is possible to annotate a {@link ValueConverter} using the annotation
 * {@link ScriptConversion}.</li>
 * </ol>
 * A <code>boolean</code> parameter annotated with {@link UsesSecurity} is not a TL-Script parameter,
 * but receives whether the script is evaluated with the current user's access rights.
 * </p>
 * 
 * <p>
 * A function reads attributes through {@link TLObject#tValue(TLStructuredTypePart)},
 * {@link TLObject#tValueByName(String)} or a generated getter. In an evaluation by a
 * {@link ScriptTracer}, each such read is recorded as a dependency of the calling script and answered
 * from the values currently edited in the form, without any cooperation of the function.
 * </p>
 * 
 * @impl This class is used in TLDoclet. It must be adapted, when this class is moved.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TLScriptFunctions {

	// No functions here.

}

