/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.Locale;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.text.AbstractMessageFormat;

/**
 * Internal base interface for {@link I18NBundle} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface I18NBundleSPI extends I18NBundle {

	/**
	 * The {@link ResourcesModule} that created this instance.
	 */
	ResourcesModule owner();

	/**
	 * The fallback {@link Locale} to use, if a resource was not found, or <code>null</code>, if
	 * there is no fallback.
	 */
	I18NBundleSPI getFallback();

	/**
	 * Check, whether the resource with the given internal key exists in this bundle.
	 */
	@FrameworkInternal
	boolean existsResource(String key);

	/**
	 * Looks up the resource for the given internal key representation.
	 */
	@FrameworkInternal
	String lookup(String key);

	/**
	 * Marks the given key as unknown.
	 * 
	 * @param key
	 *        The unknown key.
	 */
	@FrameworkInternal
	void handleUnknownKey(ResKey key);

	/**
	 * Reports that a deprecated key has been resolved.
	 * 
	 * @param deprecatedKey
	 *        The deprecated key.
	 * @param origKey
	 *        The key that was originally resolved.
	 */
	@FrameworkInternal
	void handleDeprecatedKey(ResKey deprecatedKey, ResKey origKey);

	/**
	 * Transforms the given object as value for a {@link AbstractMessageFormat}.
	 * 
	 * @param obj
	 *        Argument of an {@link ResKey} to inject into a message string.
	 * @return Either the given object or a label for it.
	 */
	@FrameworkInternal
	default Object transformMessageArgument(Object obj) {
		return obj;
	}

	/**
	 * Marks this bundle as invalid. Must be refetched from {@link ResourcesModule}.
	 */
	void invalidate();

}
