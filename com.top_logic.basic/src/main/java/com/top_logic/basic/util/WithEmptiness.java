/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * A value that can report whether it represents an empty value.
 *
 * <p>
 * Types implementing this interface participate in the generic emptiness check
 * {@link Utils#isEmpty(Object)} the same way {@link java.util.Collection}, {@link java.util.Map} and
 * {@link String} do. This allows opaque value objects (that are neither collection nor string) to
 * declare their empty state, e.g. so that TL-Script's <code>isEmpty()</code> and equality with
 * <code>null</code> treat an "empty" instance like an empty string.
 * </p>
 *
 * @author <a href="mailto:bernhard.haumacher@top-logic.com">Bernhard Haumacher</a>
 */
public interface WithEmptiness {

	/**
	 * Whether this value represents no content.
	 */
	boolean isEmpty();

}
