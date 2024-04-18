/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * Annotation for {@link CommandHandler} class opening dialogs.
 * 
 * <p>
 * By default the {@link ExecutabilityRule} of the open handler checks whether the current used is
 * allowed to see the dialog. If this annotation is present at the handler class and
 * {@link #value()} is true. no such check occurs.
 * </p>
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(value = { ElementType.TYPE })
@Retention(RUNTIME)
public @interface NoDialogContentCheck {

	/**
	 * Whether the dialog content must be checked .
	 */
	boolean value();

}

