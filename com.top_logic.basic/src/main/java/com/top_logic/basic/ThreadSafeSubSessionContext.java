/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link AbstractSubSessionContext} which can be used thread safe.
 *
 * @since 5.8.0
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ThreadSafeSubSessionContext extends AbstractSubSessionContext {

	@Override
	protected Map<Property<?>, Object> createStorage() {
		return new ConcurrentHashMap<>();
	}

}

