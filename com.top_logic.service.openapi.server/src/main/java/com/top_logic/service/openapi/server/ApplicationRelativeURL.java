/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.ControlAnnotation;

/**
 * States that a string property holds a URL path relative to the URL of the application.
 *
 * <p>
 * The full URL is the URL of the application followed by the value of the property. A form
 * displays the full URL as link.
 * </p>
 *
 * @see OpenApiServer.Config#getBaseURL()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("application-relative-url")
@ControlAnnotation
public @interface ApplicationRelativeURL {

	// Pure marker.

}
