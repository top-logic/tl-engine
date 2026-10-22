/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * Declares that the script of the annotated property sees the inputs of a containing configuration
 * of the given type, instead of the inputs of the configuration holding the script.
 *
 * <p>
 * A script of a part of an element is evaluated with the inputs of that element. Without this
 * annotation, the inputs of the nearest configuration with inputs are taken, starting at the
 * configuration holding the script and continuing with its containers (each part on the way must be
 * a {@link ConfigPart}). The annotation is needed where the configuration holding the script has
 * inputs of its own that its scripts are not evaluated with, e.g. a command whose scripts are
 * evaluated with the inputs of the table it exports.
 * </p>
 *
 * @see InputScript
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InputsOf {

	/**
	 * The type of the containing configuration whose {@link Inputs} or {@link SingleInput} the
	 * script sees.
	 */
	Class<? extends ConfigurationItem> value();

}
