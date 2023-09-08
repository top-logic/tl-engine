/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@link LabelProvider} proxy that caches created labels for a short time.
 * 
 * <p>
 * A {@link CachedLabelProvider} is especially useful, when a large number of
 * {@link #getLabel(Object)} calls happen in a short amount of time and a label is requested
 * multiple times for the same object. This happens e.g. when sorting objects by label.
 * </p>
 * 
 * <p>
 * Note: An instance of {@link CachedLabelProvider} must not be statically cached to avoid a
 * memory-leak.
 * </p>
 * 
 * <p>
 * Note: An instance of this class must not be used in multiple threads since the used cache is not
 * thread-safe.
 * </p>
 * 
 * @see LabelComparator
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CachedLabelProvider implements LabelProvider {

	private static final long TIMEOUT = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);

	/**
	 * Creates a {@link CachedLabelProvider} delegating to the given {@link LabelProvider}.
	 * 
	 * @param providerImpl
	 *        The internal label provider.
	 */
	public static LabelProvider newInstance(LabelProvider providerImpl) {
		return newInstance(providerImpl, 16);
	}

	/**
	 * Creates a {@link CachedLabelProvider} using an explicit initial caches size.
	 * 
	 * @param providerImpl
	 *        The internal label provider.
	 * @param initialCacheSize
	 *        The initial size of the allocated cache.
	 */
	public static LabelProvider newInstance(LabelProvider providerImpl, int initialCacheSize) {
		return new CachedLabelProvider(providerImpl, initialCacheSize);
	}

	/** Map of objects to their label. */
	private final Map<Object, String> _labelByKey;

	/** The internal label provider creating labels. */
	private final LabelProvider _providerImpl;

	private long _lastUsed;

	/**
	 * Creates a {@link CachedLabelProvider} using an initial caches size.
	 * 
	 * @param providerImpl
	 *        The internal label provider.
	 * @param initialCacheSize
	 *        The initial size of the allocated cache.
	 * 
	 * @see #newInstance(LabelProvider, int)
	 */
	protected CachedLabelProvider(LabelProvider providerImpl, int initialCacheSize) {
		_providerImpl = providerImpl;
		_labelByKey = new HashMap<>(initialCacheSize);
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		} else {
			Map<Object, String> cache = _labelByKey;
			long now = System.nanoTime();
			if (now - _lastUsed > TIMEOUT) {
				// Drop potentially out-dated cache.
				cache.clear();
			} else {
				String cachedLabel = cache.get(object);
				if (cachedLabel != null) {
					return cachedLabel;
				}
			}
			_lastUsed = now;

			String newLabel = _providerImpl.getLabel(object);
			cache.put(object, newLabel);
			return newLabel;
		}
	}

}
