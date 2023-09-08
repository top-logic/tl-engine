/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.EncodingConfigurationValueProvider;

/**
 * Annotation to mark properties whose values are encrypted in the configuration.
 * 
 * <p>
 * Values for annotated properties must be encoded using
 * {@link ConfigurationEncryption#encrypt(String)} in the configuration. The property in the
 * {@link ConfigurationItem} holds the unencrypted value.
 * </p>
 * 
 * @see EncodingConfigurationValueProvider
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("encrypted")
public @interface Encrypted {

	// marker only

}

