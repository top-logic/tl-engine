/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.i18n.I18NConstantsBase;

/**
 * Annotation at constants in {@link I18NConstantsBase} to mark such constant that the translation
 * is used in <i>JavaScript</i> code.
 * 
 * @see AJAXCommandHandler
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Retention(RUNTIME)
@Target(FIELD)
@FrameworkInternal
public @interface JavaScriptResKey {

	// Marker annotation.

}

