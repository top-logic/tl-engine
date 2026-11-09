/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Stand-in for the configuration of an implementation of the given type, resolved by the doclet
 * by its name.
 *
 * @param <T>
 *        The implemented type.
 */
public interface PolymorphicConfiguration<T> extends ConfigurationItem {
	// Marker.
}
