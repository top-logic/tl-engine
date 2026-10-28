/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.config.annotation.TypeIcon;

/**
 * Resolves the {@link TypeIcon} of a configuration type along its type hierarchy.
 */
public final class TypeIcons {

	/** Marker for a type without an icon in the cache, which holds no {@code null} values. */
	private static final String NO_ICON = "";

	private static final Map<ConfigurationDescriptor, String> CACHE = new ConcurrentHashMap<>();

	private TypeIcons() {
		// Utility class.
	}

	/**
	 * The {@link TypeIcon#value() icon key} of the given configuration type.
	 *
	 * <p>
	 * The icon is the one the type is annotated with. A type without an annotation of its own takes
	 * the icon of the most specific annotated type among the types it extends: an annotated type
	 * that another annotated type of the hierarchy extends is not considered. Where several annotated
	 * types remain, the first one in the order the types are extended wins, visiting the super types
	 * of each type depth-first in their declaration order.
	 * </p>
	 *
	 * @return The icon key, {@code null} if neither the type nor any type it extends is annotated.
	 */
	public static String iconKey(ConfigurationDescriptor descriptor) {
		String result = CACHE.computeIfAbsent(descriptor, TypeIcons::lookup);
		return result.isEmpty() ? null : result;
	}

	private static String lookup(ConfigurationDescriptor descriptor) {
		Set<ConfigurationDescriptor> hierarchy = new LinkedHashSet<>();
		collect(hierarchy, descriptor);

		List<ConfigurationDescriptor> annotated = new ArrayList<>();
		for (ConfigurationDescriptor type : hierarchy) {
			if (ownIcon(type) != null) {
				annotated.add(type);
			}
		}
		for (ConfigurationDescriptor candidate : annotated) {
			if (!hasMoreSpecific(candidate, annotated)) {
				return ownIcon(candidate);
			}
		}
		return NO_ICON;
	}

	private static boolean hasMoreSpecific(ConfigurationDescriptor candidate, List<ConfigurationDescriptor> annotated) {
		for (ConfigurationDescriptor other : annotated) {
			if (other != candidate && other.isSubDescriptorOf(candidate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds the given type and the types it extends, depth-first in declaration order.
	 */
	private static void collect(Set<ConfigurationDescriptor> result, ConfigurationDescriptor type) {
		if (!result.add(type)) {
			return;
		}
		for (ConfigurationDescriptor superType : type.getSuperDescriptors()) {
			collect(result, superType);
		}
	}

	private static String ownIcon(ConfigurationDescriptor type) {
		TypeIcon annotation = type.getConfigurationInterface().getAnnotation(TypeIcon.class);
		return annotation == null ? null : annotation.value();
	}

}
