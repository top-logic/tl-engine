/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Base package for the typed configuration.
 *
 * <p>
 * A typed configuration is described by a custom interface extending
 * {@link com.top_logic.basic.config.ConfigurationItem}. Each getter method in this custom interface
 * represents a property of this configuration type.
 * </p>
 *
 * <p>
 * A configuration type is never implemented directly in application code. Instead, an instance of a
 * configuration type can either be unmarshalled from XML using
 * {@link com.top_logic.basic.config.ConfigurationReader}, or it can be constructed using the
 * utility {@link com.top_logic.basic.config.TypedConfiguration#newConfigItem(Class)} passing the
 * configuration type.
 * </p>
 * 
 * <p>
 * Properties of a configuration type can be customized by adding
 * {@link com.top_logic.basic.config.annotation annotations} to the getter method of the property.
 * E.g. a property can be marked mandatory by adding the
 * {@link com.top_logic.basic.config.annotation.Mandatory} annotation.
 * </p>
 *
 * @see com.top_logic.basic.config.annotation Annotations that further customize properties of typed
 *      configuration items.
 */
package com.top_logic.basic.config;