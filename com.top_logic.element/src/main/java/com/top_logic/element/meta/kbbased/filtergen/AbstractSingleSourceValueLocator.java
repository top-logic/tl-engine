/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.shared.collection.CollectionUtilShared;

/**
 * Base class for {@link AttributeValueLocator}s that create a value based on a single object (not a
 * {@link Collection}).
 * 
 * <p>
 * The {@link #locateAttributeValue(Object)} implementation canonically extends the implementation
 * in {@link #internalLocateAttributeValue(Object)} to collections.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSingleSourceValueLocator extends AbstractAttributeValueLocator {
	
	@Override
	public final Object locateAttributeValue(Object value) {
		if (value == null) {
			return null;
		}
		
		if (value instanceof Collection<?>) {
			Collection<?> sources = (Collection<?>) value;

			if (sources.isEmpty()) {
				return Collections.emptySet();
			} else if (sources.size() == 1) {
				Object singleton = sources.iterator().next();

				return asCollection(internalLocateAttributeValue(singleton));
			} else {
				Set<Object> targets = new HashSet<>();
				for (Object source : sources) {
					Object target = internalLocateAttributeValue(source);
					if (target == null) {
						continue;
					}

					if (target instanceof Collection<?>) {
						targets.addAll((Collection<?>) target);
					} else {
						targets.add(target);
					}
				}

				if (targets.isEmpty()) {
					return Collections.emptySet();
				} else {
					return targets;
				}
			}
		} else {
			return internalLocateAttributeValue(value);
		}
	}

	private static Object asCollection(Object result) {
		if (result instanceof Collection<?>) {
			return result;
		} else {
			return CollectionUtilShared.singletonOrEmptySet(result);
		}
	}

	/**
	 * Compute the locator result for a single object.
	 * 
	 * @param source
	 *        The single source object (not a {@link Collection}).
	 * @return The locator result.
	 */
	protected abstract Object internalLocateAttributeValue(Object source);
	
}
