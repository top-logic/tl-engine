/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Cache of the result of a {@link KnowledgeBase} query.
 * 
 * <p>
 * The result of the cache can be accessed by calling {@link QueryCache#getValue()}. The result must
 * not be modified.
 * </p>
 * 
 * <p>
 * The {@link QueryCache} must be {@link QueryCache#invalidate() invalidated} if it is not longer
 * used.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface QueryCache<E> {

	/**
	 * Returns the value of this cache.
	 */
	List<E> getValue();

	/**
	 * Invalidates this cache. The cache must not be used after it was invalidated.
	 */
	void invalidate();

}

