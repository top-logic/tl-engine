/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation naming the {@link ConfigControlProvider} that edits the annotated property.
 *
 * <p>
 * On a getter, it decides the control for that property. On a configuration interface, it decides
 * the control for properties whose value type is that interface. It is the configuration-side
 * counterpart of {@code TLInputControl} and takes precedence over every other step of
 * {@code ConfigControlService}'s resolution.
 * </p>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("config-control")
public @interface ConfigControl {

	/**
	 * The provider creating the control. Must have a public constructor without arguments.
	 */
	Class<? extends ConfigControlProvider> value();

}
